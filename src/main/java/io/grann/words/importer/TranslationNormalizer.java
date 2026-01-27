package io.grann.words.importer;

import java.util.Set;
import java.util.regex.Pattern;

public final class TranslationNormalizer {

    private TranslationNormalizer() {}

    // Build a regex like: \b(?:n|v|adj|adv|...)\.\s*
    private static final Set<String> WORD_CLASS_ABBREVIATIONS = Set.of(
            "n", "v", "adj", "adv", "num",
            "art", "det", "prep", "conj",
            "pron", "pro", "aux", "part",
            "int", "abr"
    );

    private static final Pattern WORD_CLASS_MARKER_PATTERN =
            Pattern.compile(
                    "\\b(?:"
                            + String.join("|", WORD_CLASS_ABBREVIATIONS)
                            + ")\\.\\s*",
                    Pattern.CASE_INSENSITIVE
            );

    public static void normalizeTranslation(ImportedWord importedWord) {
        String rawTranslation = importedWord.getTranslation();
        if (rawTranslation == null || rawTranslation.isBlank()) {
            return;
        }

        // Remove all word-class markers
        String cleaned = WORD_CLASS_MARKER_PATTERN
                .matcher(rawTranslation)
                .replaceAll("");

        // Normalize whitespace
        cleaned = cleaned.replaceAll("\\s{2,}", " ").trim();
        importedWord.setTranslation(cleaned);
    }
}
