package io.grann.words.session;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Getter
@Setter
@ToString
@Component
@SessionScope
public class UserSession {
    private Long userAccountId;
    private Long deckProgressId;
}
