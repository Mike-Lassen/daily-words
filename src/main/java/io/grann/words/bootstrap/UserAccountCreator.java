package io.grann.words.bootstrap;

import io.grann.words.domain.Deck;
import io.grann.words.domain.DeckProgress;
import io.grann.words.domain.UserAccount;
import io.grann.words.repository.DeckProgressRepository;
import io.grann.words.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAccountCreator {
    private final UserAccountRepository userAccountRepository;
    private final DeckProgressRepository deckProgressRepository;


    public UserAccount createUserAccount(String fullName, String email, Deck deck) {
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElse(intializeUserAccount(fullName, email));
        DeckProgress deckProgress = deckProgressRepository.findByUserAccountAndDeck(userAccount, deck)
                .orElse(initializeDeckProgress(userAccount, deck));
        return userAccount;
    }

    private UserAccount intializeUserAccount(String fullName, String email) {
        UserAccount userAccount = UserAccount.builder()
                .email(email)
                .name(fullName)
                .build();
        userAccountRepository.save(userAccount);
        return userAccount;
    }
    private DeckProgress initializeDeckProgress(UserAccount userAccount, Deck deck) {
        DeckProgress deckProgress = DeckProgress.builder()
                .deck(deck)
                .userAccount(userAccount)
                .currentOrderIndex(1).build();
        deckProgressRepository.save(deckProgress);
        return deckProgress;
    }
}

