package io.grann.words.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "text")
    private String foreignText;

    @Column(nullable = false, columnDefinition = "text")
    private String nativeText;

    @ManyToOne(optional = false)
    private Level level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WordStatus status;

    @OneToOne(
            mappedBy = "word",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private ReviewState reviewState;
}
