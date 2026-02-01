package io.grann.words.repository;

import io.grann.words.domain.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {
    long countByLevel(Level level);

    @Query("""
                select w
                from Word w
                left join fetch w.annotations
                where w.id = :id
            """)
    Optional<Word> findByIdWithAnnotations(@Param("id") Long id);

    boolean existsByLevelAndForeignTextAndNativeText(Level level, String foreignText, String nativeText);

    @Query("""
        select w
        from DeckProgress dp
        join dp.deck d
        join Level l
            on l.deck = d
           and l.orderIndex <= dp.currentOrderIndex
        join Word w
            on w.level = l
        where dp = :deckProgress
          and not exists (
              select 1
              from ReviewState rs
              where rs.deckProgress = dp
                and rs.word = w
          )
        order by w.id
    """)
    List<Word> findUnlockedWordsWithoutReviewState(
            @Param("deckProgress") DeckProgress deckProgress,
            Pageable pageable
    );

    @Query("""
        select count(w)
        from DeckProgress dp
        join dp.deck d
        join Level l
            on l.deck = d
           and l.orderIndex <= dp.currentOrderIndex
        join Word w
            on w.level = l
        where dp = :deckProgress
          and not exists (
              select 1
              from ReviewState rs
              where rs.deckProgress = dp
                and rs.word = w
          )
    """)
    long countUnlockedWordsWithoutReviewState(
            @Param("deckProgress") DeckProgress deckProgress
    );


}
