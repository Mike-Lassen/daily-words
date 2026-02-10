package io.grann.words.bootstrap;

import io.grann.words.domain.*;
import io.grann.words.importer.CsvMapImporter;
import io.grann.words.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class DeckCsvImporter {
    private final DeckRepository deckRepository;
    private final LevelRepository levelRepository;
    private final DeckProgressRepository deckProgressRepository;
    private final WordRepository wordRepository;

    /**
     * Imports a deck CSV from the classpath.
     *
     * File format:
     *   Line 1: "<deck-name>: <description>"
     *   Line 2: CSV headers (semicolon delimited)
     *   Line 3+: data rows
     *
     * Required headers: foreign, native, level
     * Optional headers: kana (or more, if you extend it)
     */
    @Transactional
    public Deck importFromClasspath(String resourceName) throws Exception {
        CsvMapImporter csvMapImporter = new CsvMapImporter();
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (resourceStream == null) {
            throw new IllegalStateException("Could not find resource on classpath: " + resourceName);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream, StandardCharsets.UTF_8))) {

            // 1) Deck header line
            String firstLine = reader.readLine();
            if (firstLine == null || firstLine.isBlank()) {
                throw new IllegalStateException("CSV is empty or missing deck header line");
            }

            DeckHeader deckHeader = parseDeckHeader(firstLine);
            Deck deck = getOrCreateDeck(deckHeader);

            if (deck.getLevels() == null) {
                deck.setLevels(new ArrayList<>());
            }

            Map<String, Level> levelsByName = new HashMap<>();
            for (Level level : deck.getLevels()) {
                levelsByName.put(level.getName(), level);
            }

            // 2) Remaining content (starts with CSV header row)
            String remainder = readRemaining(reader);
            if (remainder.isBlank()) {
                return null; // only deck header + no CSV body
            }

            List<Map<String, String>> rows = csvMapImporter.importToMaps(
                    new ByteArrayInputStream(remainder.getBytes(StandardCharsets.UTF_8))
            );

            validateHeaders(rows);

            List<Word> wordsToInsert = new ArrayList<>();

            for (Map<String, String> row : rows) {
                String foreignText = get(row, "word");
                String nativeText = get(row, "translation");
                String levelName = get(row, "level");

                if (isBlank(foreignText) || isBlank(nativeText) || isBlank(levelName)) {
                    continue;
                }

                foreignText = foreignText.trim();
                nativeText = nativeText.trim();
                levelName = levelName.trim();

                Level level = getOrCreateLevel(deck, levelsByName, levelName);

                Optional<Word> existing = wordRepository.findByLevelAndForeignText(level, foreignText);

                Word word = existing.orElse(
                        Word.builder()
                                .foreignText(foreignText)
                                .nativeText(nativeText)
                                .level(level)
                                .build()
                );

                word.setNativeText(nativeText);

                // Optional annotations
                addOptionalAnnotation(word, row, "kana", WordAnnotationType.KANA);
                addOptionalAnnotation(word, row, "summary", WordAnnotationType.SUMMARY);
                addOptionalAnnotation(word, row, "note", WordAnnotationType.NOTE);

                wordsToInsert.add(word);
            }

            if (!wordsToInsert.isEmpty()) {
                wordRepository.saveAll(wordsToInsert);
            }
            return deck;
            //ensureDeckProgressInitialized(deck);
        }
    }

    private void validateHeaders(List<Map<String, String>> rows) {
        // CsvMapImporter lowercases headers :contentReference[oaicite:2]{index=2}
        // If file has header row but no data rows, we can't infer headers from rows.
        // So: allow empty rows silently. If you prefer "fail fast", change this.
        if (rows.isEmpty()) {
            return;
        }

        Map<String, String> firstRow = rows.get(0);
        requireHeader(firstRow, "word");
        requireHeader(firstRow, "translation");
        requireHeader(firstRow, "level");
    }

    private void requireHeader(Map<String, String> row, String header) {
        if (!row.containsKey(header)) {
            throw new IllegalStateException("Missing required CSV header: " + header);
        }
    }

    private void addOptionalAnnotation(Word word, Map<String, String> row, String key, WordAnnotationType type) {
        String value = get(row, key);
        if (isBlank(value)) {
            return;
        }
        word.setAnnotationValue(type, value.trim());
    }

    private static String get(Map<String, String> row, String key) {
        // CsvMapImporter normalizes headers to lowercase :contentReference[oaicite:3]{index=3}
        return row.get(key);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isBlank();
    }

    private static String readRemaining(BufferedReader reader) throws Exception {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private void ensureDeckProgressInitialized(Deck deck) {
        if (deckProgressRepository.findByDeck(deck).isPresent()) {
            return;
        }

        Level firstLevel = levelRepository.findFirstByDeckOrderByOrderIndexAsc(deck)
                .orElse(null);

        if (firstLevel == null) {
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

        Deck savedDeck = deckRepository.save(deck);

        Level savedLevel = savedDeck.getLevels().stream()
                .filter(level -> levelName.equals(level.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Level was not persisted: " + levelName));

        levelsByName.put(levelName, savedLevel);
        return savedLevel;
    }

    private int guessOrderIndex(String levelName, List<Level> existingLevels) {
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(levelName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        int max = 0;
        for (Level level : existingLevels) {
            max = Math.max(max, level.getOrderIndex());
        }
        return max + 1;
    }

    private DeckHeader parseDeckHeader(String line) {
        int colonIndex = line.indexOf(':');
        if (colonIndex < 0) {
            return new DeckHeader(line.trim(), "");
        }

        String deckName = line.substring(0, colonIndex).trim();
        String description = line.substring(colonIndex + 1).trim();
        return new DeckHeader(deckName, description);
    }

    private record DeckHeader(String deckName, String description) {}
}
