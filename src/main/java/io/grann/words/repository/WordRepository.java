package io.grann.words.repository;

import io.grann.words.domain.Level;
import io.grann.words.domain.Word;
import io.grann.words.domain.WordStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {
    List<Word> findTop5ByStatusOrderByIdAsc(WordStatus status);

    @Query("""
            select w
            from Word w
            where w.status = io.grann.words.domain.WordStatus.LEARNING
              and w.level.deck.id = :deckId
              and w.level.orderIndex <= :maxOrderIndex
            order by w.level.orderIndex asc, w.id asc
            """)
    List<Word> findTop5LearningAvailableInDeckUpToOrderIndex(
            @Param("deckId") Long deckId,
            @Param("maxOrderIndex") int maxOrderIndex
    );

    @Query("""
            select w.id
            from Word w
            where w.status = io.grann.words.domain.WordStatus.LEARNING
              and w.level.deck.id = :deckId
              and w.level.orderIndex <= :maxOrderIndex
            order by w.level.orderIndex asc, w.id asc
            """)
    List<Long> findLearningIdsAvailableInDeckUpToOrderIndex(
            @Param("deckId") Long deckId,
            @Param("maxOrderIndex") int maxOrderIndex,
            Pageable pageable
    );

    @Query("""
            select distinct w
            from Word w
            left join fetch w.annotations
            where w.id in :ids
            """)
    List<Word> findByIdInWithAnnotations(@Param("ids") List<Long> ids);

    long countByLevelAndStatus(Level level, WordStatus status);

    @Query("""
            select w
            from Word w
            join w.reviewState rs
            where w.status = io.grann.words.domain.WordStatus.REVIEWING
              and rs.nextReviewAt <= :now
            order by rs.nextReviewAt asc, w.id asc
            """)
    List<Word> findWordsDueForReview(@Param("now") LocalDateTime now);

    @Query("""
            select count(w)
            from Word w
            join w.reviewState rs
            where w.status = io.grann.words.domain.WordStatus.REVIEWING
              and rs.nextReviewAt <= :now
            """)
    long countWordsDueForReview(@Param("now") LocalDateTime now);

    @Query("""
                select w
                from Word w
                left join fetch w.annotations
                where w.id = :id
            """)
    Optional<Word> findByIdWithAnnotations(@Param("id") Long id);

    long countByStatus(WordStatus status);

    boolean existsByLevelAndForeignTextAndNativeText(Level level, String foreignText, String nativeText);

}
