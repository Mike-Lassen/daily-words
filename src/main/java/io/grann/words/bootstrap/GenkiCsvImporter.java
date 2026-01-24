package io.grann.words.bootstrap;

import io.grann.words.domain.*;
import io.grann.words.repository.DeckProgressRepository;
import io.grann.words.repository.DeckRepository;
import io.grann.words.repository.LevelRepository;
import io.grann.words.repository.WordAnnotationRepository;
import io.grann.words.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class GenkiCsvImporter {

    private static final String RESOURCE_NAME = "genki-deck.csv";

    private final DeckRepository deckRepository;
    private final LevelRepository levelRepository;
    private final DeckProgressRepository deckProgressRepository;
    private final WordRepository wordRepository;
    private final WordAnnotationRepository wordAnnotationRepository;

    @Transactional
    public void run() throws Exception {
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(RESOURCE_NAME);
        if (resourceStream == null) {
            throw new IllegalStateException("Could not find resource on classpath: " + RESOURCE_NAME);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream, StandardCharsets.UTF_8));

        String firstLine = reader.readLine();
        if (firstLine == null || firstLine.isBlank()) {
            throw new IllegalStateException("CSV is empty or missing deck header line");
        }

        DeckHeader deckHeader = parseDeckHeader(firstLine);
        Deck deck = getOrCreateDeck(deckHeader);

        // Ensure levels collection is initialized for in-memory management
        if (deck.getLevels() == null) {
            deck.setLevels(new ArrayList<>());
        }

        // Build a quick lookup map for existing levels
        Map<String, Level> levelsByName = new HashMap<>();
        for (Level level : deck.getLevels()) {
            levelsByName.put(level.getName(), level);
        }

        // Second line is headers (skip)
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return; // only deck header + no content
        }

        List<Word> wordsToInsert = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }

            List<String> columns = parseSemicolonCsvLine(line);
            if (columns.size() < 4) {
                // You could log and skip, but failing fast is often better for imports
                throw new IllegalStateException("Expected 4 columns but got " + columns.size() + " in line: " + line);
            }

            String foreignText = columns.get(0).trim();
            String kana = columns.get(1).trim();
            String nativeText = columns.get(2).trim();
            String levelName = columns.get(3).trim();

            if (foreignText.isBlank() || nativeText.isBlank() || levelName.isBlank()) {
                continue;
            }

            Level level = getOrCreateLevel(deck, levelsByName, levelName);

            boolean alreadyExists = wordRepository.existsByLevelAndForeignTextAndNativeText(level, foreignText, nativeText);
            if (alreadyExists) {
                // Optional: backfill kana annotation if missing
                // backfillKanaIfMissing(level, foreignText, nativeText, kana);
                continue;
            }

            Word word = Word.builder()
                    .foreignText(foreignText)
                    .nativeText(nativeText)
                    .level(level)
                    .status(WordStatus.LEARNING)
                    .build();

            if (word.getAnnotations() == null) {
                word.setAnnotations(new ArrayList<>());
            }

            if (!kana.isBlank()) {
                WordAnnotation kanaAnnotation = WordAnnotation.builder()
                        .word(word)
                        .type(WordAnnotationType.KANA)
                        .value(kana)
                        .build();
                word.getAnnotations().add(kanaAnnotation);
            }

            wordsToInsert.add(word);
        }

        if (!wordsToInsert.isEmpty()) {
            wordRepository.saveAll(wordsToInsert);
        }

        ensureDeckProgressInitialized(deck);
    }

    private void ensureDeckProgressInitialized(Deck deck) {
        if (deckProgressRepository.findByDeck(deck).isPresent()) {
            return;
        }

        Level firstLevel = levelRepository.findFirstByDeckOrderByOrderIndexAsc(deck)
                .orElse(null);

        if (firstLevel == null) {
            // No levels means no progress can be tracked yet.
            return;
        }

        DeckProgress progress = DeckProgress.builder()
                .deck(deck)
                .currentOrderIndex(firstLevel.getOrderIndex())
                .build();

        deckProgressRepository.save(progress);
    }

    private Deck getOrCreateDeck(DeckHeader deckHeader) {
        Optional<Deck> existingDeck = deckRepository.findByName(deckHeader.deckName());
        if (existingDeck.isPresent()) {
            Deck deck = existingDeck.get();
            String incomingDescription = deckHeader.description() == null ? "" : deckHeader.description().trim();
            String existingDescription = deck.getDescription() == null ? "" : deck.getDescription().trim();

            if (!existingDescription.equals(incomingDescription)) {
                deck.setDescription(incomingDescription);
            }

            return deck;
        }

        Deck newDeck = Deck.builder()
                .name(deckHeader.deckName())
                .description(deckHeader.description())
                .build();

        if (newDeck.getLevels() == null) {
            newDeck.setLevels(new ArrayList<>());
        }

        return deckRepository.save(newDeck);
    }

    private Level getOrCreateLevel(Deck deck, Map<String, Level> levelsByName, String levelName) {
        Level existingLevel = levelsByName.get(levelName);
        if (existingLevel != null) {
            return existingLevel;
        }

        Level newLevel = Level.builder()
                .name(levelName)
                .orderIndex(guessOrderIndex(levelName, deck.getLevels()))
                .deck(deck)
                .build();

        deck.getLevels().add(newLevel);

        // Persist by saving deck; assuming Deck -> Levels is cascade-persist.
        // If you don't have cascade, add a LevelRepository and save(newLevel) explicitly.
        Deck savedDeck = deckRepository.save(deck);

        // Re-bind the created level from the saved deck instance (safer with JPA)
        Level savedLevel = savedDeck.getLevels().stream()
                .filter(level -> levelName.equals(level.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Level was not persisted: " + levelName));

        levelsByName.put(levelName, savedLevel);
        return savedLevel;
    }

    private int guessOrderIndex(String levelName, List<Level> existingLevels) {
        // Try extract a number from e.g. "Lesson 12"
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(levelName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        // Fallback: append to end
        int max = 0;
        for (Level level : existingLevels) {
            max = Math.max(max, level.getOrderIndex());
        }
        return max + 1;
    }

    private DeckHeader parseDeckHeader(String line) {
        // Expected: "<deck-name>: <description>"
        int colonIndex = line.indexOf(':');
        if (colonIndex < 0) {
            // If no colon, treat whole line as deck name
            return new DeckHeader(line.trim(), "");
        }

        String deckName = line.substring(0, colonIndex).trim();
        String description = line.substring(colonIndex + 1).trim();
        return new DeckHeader(deckName, description);
    }

    /**
     * Parses a semicolon-separated CSV line with support for double quotes.
     * Example: 先生;せんせい;"teacher; Professor";Lesson 1
     */
    private List<String> parseSemicolonCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean inQuotes = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);

            if (character == '"') {
                // Handle escaped double quote inside a quoted field ("")
                boolean isEscapedQuote = inQuotes
                        && index + 1 < line.length()
                        && line.charAt(index + 1) == '"';

                if (isEscapedQuote) {
                    current.append('"');
                    index++; // skip the second quote
                    continue;
                }

                inQuotes = !inQuotes;
                continue;
            }

            if (character == ';' && !inQuotes) {
                columns.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(character);
        }

        columns.add(current.toString());
        return columns;
    }

    private record DeckHeader(String deckName, String description) {
    }
}
