package io.grann.words.dashboard;

import io.grann.words.domain.*;
import io.grann.words.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DashboardService {

    private final DeckRepository deckRepository;
    private final WordRepository wordRepository;
    private final Clock clock;
    private final DeckProgressRepository deckProgressRepository;
    private final ReviewStateRepository reviewStateRepository;
    private final LevelRepository levelRepository;

    @Transactional
    public DashboardSummary getDashboardSummary(Long deckProgressId) {

        DeckProgress progress = deckProgressRepository.findById(deckProgressId)
                .orElseThrow(() -> new IllegalStateException("No deck-progress: " + deckProgressId));

        Deck deck = progress.getDeck();
        Level level = levelRepository.findByDeckAndOrderIndex(deck, progress.getCurrentOrderIndex())
                .orElseThrow();

        long total = wordRepository.countByLevel(level);
        List<ReviewState> reviewStates = reviewStateRepository.findByDeckProgressAndWordLevel(progress, level);
        long notInReview = total - reviewStates.size();
        List<SrsLevel> traineeLevels = SrsLevel.getTraineeLevels();
        List<SrsLevel> expertLevels = SrsLevel.getExpertLevels();

        long trainee = reviewStates.stream().filter(rs -> traineeLevels.contains(rs.getLevel())).count();
        long expert = reviewStates.stream().filter(rs -> expertLevels.contains(rs.getLevel())).count();

        LocalDateTime now = LocalDateTime.now(clock);
        long newWordsAvailable = wordRepository.countUnlockedWordsWithoutReviewState(progress);
        long reviewsDue = reviewStateRepository.findDueByDeckProgress(progress, now, Pageable.ofSize(1000)).size();

        return new DashboardSummary(level.getName(), newWordsAvailable, reviewsDue, total, notInReview, trainee, expert);
    }

    public record DashboardSummary(String levelName,
                                   long newWordsAvailable,
                                   long reviewsDue,
                                   long total,
                                   long notInReview,
                                   long trainee,
                                   long expert) {}
}
