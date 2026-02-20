package io.grann.words.repository;

import io.grann.words.domain.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {
    long countByLevel(Level level);

    boolean existsByLevelAndForeignTextAndNativeText(Level level, String foreignText, String nativeText);

    Optional<Word> findByLevelAndForeignText(Level level, String foreignText);

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


    @Query("""
        select w
        from Word w
        join w.level l
        join l.deck d
        where d.id = :deckId
          and (
              :q is null
              or :q = ''
              or lower(w.foreignText) like lower(concat('%', :q, '%'))
              or lower(w.nativeText) like lower(concat('%', :q, '%'))
          )
        order by l.orderIndex asc, w.id asc
    """)
    List<Word> findDeckWords(
            @Param("deckId") Long deckId,
            @Param("q") String q,
            Pageable pageable
    );

    @Query("""
        select count(w)
        from Word w
        join w.level l
        join l.deck d
        where d.id = :deckId
          and (
              :q is null
              or :q = ''
              or lower(w.foreignText) like lower(concat('%', :q, '%'))
              or lower(w.nativeText) like lower(concat('%', :q, '%'))
          )
    """)
    long countDeckWords(
            @Param("deckId") Long deckId,
            @Param("q") String q
    );

}
