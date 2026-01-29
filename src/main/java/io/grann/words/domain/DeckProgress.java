package io.grann.words.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"deck_id"})
)
public class DeckProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Deck deck;
    @ManyToOne(optional = false)
    private UserAccount userAccount;

    /**
     * Highest level orderIndex that is unlocked for learning in this deck (inclusive).
     *
     * MVP simplification: single user.
     */
    @Column(nullable = false)
    private int currentOrderIndex;
}
