package io.grann.words.dashboard;

import io.grann.words.domain.UserAccount;
import io.grann.words.repository.UserAccountRepository;
import io.grann.words.session.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class NewUserController {

    private final UserAccountRepository userAccountRepository;
    private final UserSession userSession;

    @GetMapping("/new")
    public String newUserForm() {
        return "user-new";
    }

    @PostMapping
    public String createUser(@RequestParam String name,
                             @RequestParam String email) {

        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserAccount u = new UserAccount();
                    u.setName(name);
                    u.setEmail(email);
                    return userAccountRepository.save(u);
                });

        userSession.setUserAccountId(user.getId());
        userSession.setDeckProgressId(null);

        return "redirect:/choose-deck";
    }
}
