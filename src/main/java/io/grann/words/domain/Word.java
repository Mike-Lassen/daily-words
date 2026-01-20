package io.grann.words.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "text")
    private String foreignText;

    @Column(nullable = false, columnDefinition = "text")
    private String nativeText;

    @ManyToOne(optional = false)
    @ToString.Exclude
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

    @OneToMany(
            mappedBy = "word",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<WordAnnotation> annotations = new ArrayList<>();

    public String getAnnotationValue(WordAnnotationType type) {
        for (WordAnnotation annotation : getAnnotations()) {
            if (annotation.getType() == type) {
                return annotation.getValue();
            }
        }
        return null;
    }

    public String getKana() {
        return getAnnotationValue(WordAnnotationType.KANA);
    }
    public String getFurigana() {
        return getAnnotationValue(WordAnnotationType.FURIGANA);
    }
    public String getNote() {
        return getAnnotationValue(WordAnnotationType.NOTE);
    }

    public void enterReviewing(Clock clock) {
        // 1) Word state
        this.status = WordStatus.REVIEWING;

        // 2) Ensure ReviewState exists and is linked both ways
        if (this.reviewState == null) {
            ReviewState rs = ReviewState.builder()
                    .word(this)                // owning side (IMPORTANT)
                    .level(SrsLevel.LEVEL_1)   // adapt name to your enum
                    .nextReviewAt(LocalDateTime.now(clock).plusDays(1))
                    .lastReviewedAt(null)
                    .build();

            this.reviewState = rs;            // inverse side
        } else {
            // 3) If it exists already, enforce invariants for "newly learned" words
            this.reviewState.setWord(this); // ensure owning side is consistent
            this.reviewState.setLevel(SrsLevel.LEVEL_1);
            this.reviewState.setNextReviewAt(LocalDateTime.now(clock).plusDays(1));
            this.reviewState.setLastReviewedAt(null);
        }
    }
}
