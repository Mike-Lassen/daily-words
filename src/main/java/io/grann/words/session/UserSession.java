package io.grann.words.session;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserSession {
    private Long userAccountId;
    private Long deckProgressId;
}
