package io.grann.words.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"deck_id", "orderIndex"}
        )
)
public class Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Deck deck;

    @Column(nullable = false)
    private String name; // e.g. "Lesson 1", "Chapter 3"

    @Column(nullable = false)
    private int orderIndex;
}
