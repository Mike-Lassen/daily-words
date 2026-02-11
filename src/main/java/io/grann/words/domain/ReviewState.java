package io.grann.words.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private DeckProgress deckProgress;

    @ManyToOne(optional = false)
    private Word word;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SrsLevel level;

    @Column(nullable = false)
    private LocalDateTime nextReviewAt;

    @Column
    private LocalDateTime lastReviewedAt;

    @Enumerated(EnumType.STRING)
    @Column
    private ReviewStateStatus status;
}
