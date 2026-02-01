package io.grann.words.dashboard;

import io.grann.words.repository.UserAccountRepository;
import io.grann.words.session.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class UserChooserController {

    private final UserAccountRepository userAccountRepository;
    private final UserSession userSession;

    /**
     * User Chooser landing page
     */
    @GetMapping("/")
    public String userChooser(Model model) {
        // If you prefer: keep them on chooser even if session exists.
        // But this "continue" redirect is a nice returning-user default:
        if (userSession.getDeckProgressId() != null) {
            return "redirect:/dashboard";
        }

        model.addAttribute("users", userAccountRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        return "user-chooser";
    }

    /**
     * Select an existing user
     */
    @PostMapping("/select-user")
    public String selectUser(@RequestParam("userId") Long userId) {
        // Optional: verify exists to fail fast
        if (!userAccountRepository.existsById(userId)) {
            return "redirect:/";
        }

        userSession.setUserAccountId(userId);
        userSession.setDeckProgressId(null); // critical: prevent cross-user deck leakage
        return "redirect:/choose-deck";
    }

    /**
     * Optional helper endpoint: "Switch user" action
     */
    @PostMapping("/switch-user")
    public String switchUser() {
        userSession.setUserAccountId(null);
        userSession.setDeckProgressId(null);
        return "redirect:/";
    }
}
