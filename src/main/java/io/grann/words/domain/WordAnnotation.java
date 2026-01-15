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
                columnNames = {"word_id", "type"}
        )
)
public class WordAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Word word;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WordAnnotationType type;

    @Column(nullable = false, columnDefinition = "text")
    private String value;
}
