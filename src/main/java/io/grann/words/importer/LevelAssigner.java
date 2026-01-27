package io.grann.words.importer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class LevelAssigner {

    private static final int LEVEL_COUNT = 5;
    private static final int WORDS_PER_LEVEL = 100;

    // Per-level quotas tuned to your observed distribution:
    // 202 nouns, 69 verbs, 78 adjectives (others fill the rest).
    private static final int[] NOUNS_PER_LEVEL = {40, 40, 40, 41, 41};
    private static final int[] VERBS_PER_LEVEL = {20, 18, 15, 10, 6};   // total 69
    private static final int[] ADJECTIVES_PER_LEVEL = {15, 16, 16, 15, 16};

    private LevelAssigner() {}

    public static void assignLevelsInPlace(List<ImportedWord> importedWords) {
        Objects.requireNonNull(importedWords, "importedWords");

        if (importedWords.size() != LEVEL_COUNT * WORDS_PER_LEVEL) {
            throw new IllegalArgumentException(
                    "Expected exactly " + (LEVEL_COUNT * WORDS_PER_LEVEL) + " words, but got " + importedWords.size()
            );
        }

        // Clear any existing level values (important for re-runs)
        for (ImportedWord importedWord : importedWords) {
            importedWord.setLevel(null);
        }

        List<ImportedWord> nounWords = new ArrayList<>();
        List<ImportedWord> verbWords = new ArrayList<>();
        List<ImportedWord> adjectiveWords = new ArrayList<>();
        List<ImportedWord> otherWords = new ArrayList<>();

        for (ImportedWord importedWord : importedWords) {
            String learningGroup = toLearningGroup(importedWord.getWordClass());

            if ("noun".equals(learningGroup)) {
                nounWords.add(importedWord);
            } else if ("verb".equals(learningGroup)) {
                verbWords.add(importedWord);
            } else if ("adjective".equals(learningGroup)) {
                adjectiveWords.add(importedWord);
            } else {
                otherWords.add(importedWord);
            }
        }

        Comparator<ImportedWord> byRankAscending =
                Comparator.comparing(ImportedWord::getRank, Comparator.nullsLast(Integer::compareTo));

        nounWords.sort(byRankAscending);
        verbWords.sort(byRankAscending);
        adjectiveWords.sort(byRankAscending);
        otherWords.sort(byRankAscending);

        int nounIndex = 0;
        int verbIndex = 0;
        int adjectiveIndex = 0;
        int otherIndex = 0;

        for (int level = 1; level <= LEVEL_COUNT; level++) {
            int levelIndex = level - 1;

            int targetNouns = NOUNS_PER_LEVEL[levelIndex];
            int targetVerbs = VERBS_PER_LEVEL[levelIndex];
            int targetAdjectives = ADJECTIVES_PER_LEVEL[levelIndex];

            List<ImportedWord> levelWords = new ArrayList<>(WORDS_PER_LEVEL);

            // 1) Take quota-based slices (frequency-ordered within each bucket)
            nounIndex += takeNextAndAppend(nounWords, nounIndex, targetNouns, levelWords);
            verbIndex += takeNextAndAppend(verbWords, verbIndex, targetVerbs, levelWords);
            adjectiveIndex += takeNextAndAppend(adjectiveWords, adjectiveIndex, targetAdjectives, levelWords);

            // 2) Fill the remaining slots to reach 100 with the most frequent remaining words overall
            while (levelWords.size() < WORDS_PER_LEVEL) {
                ImportedWord nextMostFrequentRemaining = pickNextMostFrequentRemaining(
                        nounWords, nounIndex,
                        verbWords, verbIndex,
                        adjectiveWords, adjectiveIndex,
                        otherWords, otherIndex,
                        byRankAscending
                );

                if (nextMostFrequentRemaining == null) {
                    throw new IllegalStateException(
                            "Not enough words to fill level " + level + ". Current size: " + levelWords.size()
                    );
                }

                levelWords.add(nextMostFrequentRemaining);

                // Advance the index for whichever bucket we took from
                String learningGroup = toLearningGroup(nextMostFrequentRemaining.getWordClass());
                if ("noun".equals(learningGroup)) {
                    nounIndex++;
                } else if ("verb".equals(learningGroup)) {
                    verbIndex++;
                } else if ("adjective".equals(learningGroup)) {
                    adjectiveIndex++;
                } else {
                    otherIndex++;
                }
            }

            // 3) Optional: sort within level by rank so each level still feels frequency-driven
            levelWords.sort(byRankAscending);

            // 4) Write the level number onto the objects
            for (ImportedWord importedWord : levelWords) {
                importedWord.setLevel(level);
            }
        }

        long assignedCount = importedWords.stream()
                .filter(importedWord -> importedWord.getLevel() != null)
                .count();

        if (assignedCount != LEVEL_COUNT * WORDS_PER_LEVEL) {
            throw new IllegalStateException(
                    "Expected to assign " + (LEVEL_COUNT * WORDS_PER_LEVEL) + " words, but assigned " + assignedCount
            );
        }
    }

    private static int takeNextAndAppend(
            List<ImportedWord> sourceWords,
            int startIndex,
            int count,
            List<ImportedWord> targetList
    ) {
        int endIndexExclusive = Math.min(startIndex + count, sourceWords.size());
        if (startIndex >= endIndexExclusive) {
            return 0;
        }

        targetList.addAll(sourceWords.subList(startIndex, endIndexExclusive));
        return endIndexExclusive - startIndex;
    }

    private static ImportedWord pickNextMostFrequentRemaining(
            List<ImportedWord> nounWords, int nounIndex,
            List<ImportedWord> verbWords, int verbIndex,
            List<ImportedWord> adjectiveWords, int adjectiveIndex,
            List<ImportedWord> otherWords, int otherIndex,
            Comparator<ImportedWord> byRankAscending
    ) {
        ImportedWord nextNoun = nounIndex < nounWords.size() ? nounWords.get(nounIndex) : null;
        ImportedWord nextVerb = verbIndex < verbWords.size() ? verbWords.get(verbIndex) : null;
        ImportedWord nextAdjective = adjectiveIndex < adjectiveWords.size() ? adjectiveWords.get(adjectiveIndex) : null;
        ImportedWord nextOther = otherIndex < otherWords.size() ? otherWords.get(otherIndex) : null;

        ImportedWord bestCandidate = null;

        bestCandidate = pickBetterCandidate(bestCandidate, nextNoun, byRankAscending);
        bestCandidate = pickBetterCandidate(bestCandidate, nextVerb, byRankAscending);
        bestCandidate = pickBetterCandidate(bestCandidate, nextAdjective, byRankAscending);
        bestCandidate = pickBetterCandidate(bestCandidate, nextOther, byRankAscending);

        return bestCandidate;
    }

    private static ImportedWord pickBetterCandidate(
            ImportedWord currentBestCandidate,
            ImportedWord newCandidate,
            Comparator<ImportedWord> byRankAscending
    ) {
        if (newCandidate == null) {
            return currentBestCandidate;
        }
        if (currentBestCandidate == null) {
            return newCandidate;
        }

        int comparisonResult = byRankAscending.compare(newCandidate, currentBestCandidate);
        return comparisonResult < 0 ? newCandidate : currentBestCandidate;
    }

    private static String toLearningGroup(String wordClass) {
        if (wordClass == null) {
            return "other";
        }
        return switch (wordClass) {
            case "noun" -> "noun";
            case "verb" -> "verb";
            case "adjective" -> "adjective";
            default -> "other";
        };
    }
}
