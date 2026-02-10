package io.grann.words.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @OneToMany(
            mappedBy = "word",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<WordAnnotation> annotations = new ArrayList<>();

    public void setAnnotationValue(WordAnnotationType type, String value) {
        for (WordAnnotation annotation : getAnnotations()) {
            if (annotation.getType() == type) {
                annotation.setValue(value);
                return;
            }
        }
        WordAnnotation annotation = WordAnnotation.builder()
                .word(this)
                .type(type)
                .value(value).build();
        getAnnotations().add(annotation);
    }
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
    public String getSummary() {
        return getAnnotationValue(WordAnnotationType.SUMMARY);
    }

}
