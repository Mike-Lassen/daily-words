package io.grann.words.repository;

import io.grann.words.domain.DeckProgress;
import io.grann.words.domain.Level;
import io.grann.words.domain.ReviewState;
import io.grann.words.domain.Word;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewStateRepository extends JpaRepository<ReviewState, Long> {
    Optional<ReviewState> findByWord(Word word);
    @Query("""
        select rs
        from ReviewState rs
        join fetch rs.word w
        where rs.deckProgress = :deckProgress
          and w.level = :level
        order by w.id
    """)
    List<ReviewState> findByDeckProgressAndWordLevel(
            @Param("deckProgress") DeckProgress deckProgress,
            @Param("level") Level level
    );
    @Query("""
        select rs
        from ReviewState rs
        join fetch rs.word w
        where rs.deckProgress = :deckProgress
          and rs.nextReviewAt <= :now
        order by rs.nextReviewAt asc, rs.id asc
    """)
    List<ReviewState> findDueByDeckProgress(
            @Param("deckProgress") DeckProgress deckProgress,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
