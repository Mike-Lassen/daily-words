package io.grann.words.importer;

import java.util.Locale;
import java.util.Map;

public enum WordClass {

    NOUN("noun"),
    VERB("verb"),
    ADJECTIVE("adjective"),
    ADVERB("adverb"),
    NUMERAL("numeral"),

    ARTICLE("article"),
    DETERMINER("determiner"),
    PREPOSITION("preposition"),
    CONJUNCTION("conjunction"),
    PRONOUN("pronoun"),
    AUXILIARY("auxiliary"),
    PARTICLE("particle"),

    INTERJECTION("interjection"),
    ABBREVIATION("abbreviation"),

    UNKNOWN("unknown");

    private final String canonicalName;

    WordClass(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    private static final Map<String, WordClass> ABBREVIATION_TO_WORD_CLASS = Map.ofEntries(
            Map.entry("n", NOUN),
            Map.entry("v", VERB),
            Map.entry("adj", ADJECTIVE),
            Map.entry("adv", ADVERB),
            Map.entry("num", NUMERAL),

            Map.entry("art", ARTICLE),
            Map.entry("det", DETERMINER),
            Map.entry("prep", PREPOSITION),
            Map.entry("conj", CONJUNCTION),
            Map.entry("pron", PRONOUN),
            Map.entry("pro", PRONOUN),
            Map.entry("aux", AUXILIARY),
            Map.entry("part", PARTICLE),

            Map.entry("int", INTERJECTION),
            Map.entry("abr", ABBREVIATION)
    );

    public static WordClass fromRawValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return UNKNOWN;
        }

        String normalizedRawValue = rawValue.trim().toLowerCase(Locale.ROOT);

        // If the file already contains canonical names like "noun"
        for (WordClass wordClass : values()) {
            if (wordClass.canonicalName.equals(normalizedRawValue)) {
                return wordClass;
            }
        }

        // Otherwise, treat it as an abbreviation like "n", "v", "prep"
        return ABBREVIATION_TO_WORD_CLASS.getOrDefault(normalizedRawValue, UNKNOWN);
    }
}
