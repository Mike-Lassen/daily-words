package io.grann.words.session;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSession {
    private Long userAccountId;
    private Long deckProgressId;
}
