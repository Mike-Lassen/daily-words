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

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DeckChooserController {

    private final UserAccountRepository userAccountRepository;
    private final DeckRepository deckRepository;
    private final DeckProgressRepository deckProgressRepository;
    private final UserSession userSession;

    @GetMapping("/choose-deck")
    public String chooseDeck(Model model) {
        Long userId = userSession.getUserAccountId();
        if (userId == null) {
            return "redirect:/";
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Selected user not found: " + userId));

        List<Deck> decks = deckRepository.findAll();

        // Preload existing progress so the page can display Continue/Start
        Map<Long, Long> deckIdToProgressId = deckProgressRepository.findByUserAccount(user).stream()
                .collect(Collectors.toMap(
                        dp -> dp.getDeck().getId(),
                        dp -> dp.getId()
                ));

        model.addAttribute("user", user);
        model.addAttribute("decks", decks);
        model.addAttribute("deckIdToProgressId", deckIdToProgressId); // in Thymeleaf: map lookup
        return "deck-chooser";
    }

    @PostMapping("/select-deck")
    public String selectDeck(@RequestParam("deckId") Long deckId) {
        Long userId = userSession.getUserAccountId();
        if (userId == null) {
            return "redirect:/";
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Selected user not found: " + userId));

        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found: " + deckId));

        DeckProgress progress = deckProgressRepository.findByUserAccountAndDeck(user, deck)
                .orElseGet(() -> {
                    DeckProgress dp = new DeckProgress();
                    dp.setUserAccount(user);
                    dp.setDeck(deck);

                    // MVP default. Better: initialize from the deck’s first level orderIndex.
                    dp.setCurrentOrderIndex(1);

                    return deckProgressRepository.save(dp);
                });

        userSession.setDeckProgressId(progress.getId());
        return "redirect:/dashboard";
    }
}
