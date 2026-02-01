package io.grann.words.dashboard;

import io.grann.words.domain.Deck;
import io.grann.words.domain.DeckProgress;
import io.grann.words.domain.UserAccount;
import io.grann.words.repository.DeckProgressRepository;
import io.grann.words.repository.DeckRepository;
import io.grann.words.repository.UserAccountRepository;
import io.grann.words.session.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class LandingController {
    private final UserSession userSession;
    private final UserAccountRepository userAccountRepository;
    private final DeckRepository deckRepository;
    private final DeckProgressRepository deckProgressRepository;

    @GetMapping("/landing")
    public String landing(Model model) {

        if (userSession.getDeckProgressId() != null) {
            return "redirect:/dashboard";
        }

        model.addAttribute("decks", deckRepository.findAll());
        return "landing";
    }

    @PostMapping("/start")
    public String start(@RequestParam String name,
                        @RequestParam String email,
                        @RequestParam Long deckId) {

        UserAccount user = userAccountRepository
                .findByEmail(email)
                .orElseGet(() -> userAccountRepository.save(
                        UserAccount.builder()
                                .name(name)
                                .email(email).build()
                ));

        Deck deck = deckRepository.findById(deckId)
                .orElseThrow();

        DeckProgress progress = deckProgressRepository
                .findByUserAccountAndDeck(user, deck)
                .orElseGet(() -> deckProgressRepository.save(
                        DeckProgress.builder()
                                .userAccount(user)
                                .deck(deck)
                                .currentOrderIndex(1)
                                .build()
                ));

        userSession.setUserAccountId(user.getId());
        userSession.setDeckProgressId(progress.getId());

        return "redirect:/dashboard";
    }
}
