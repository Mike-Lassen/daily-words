package io.grann.words.importer;

import io.grann.words.domain.WordStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class WordClassNormalizer {

    private WordClassNormalizer() {}

    public static void normalizeWordClass(ImportedWord importedWord) {
        Objects.requireNonNull(importedWord, "importedWord");
        String abbreviations = importedWord.getWordClass();
        String[] values = abbreviations.split(" ");
        List<WordClass> wordClassList = new ArrayList<>();
        for (String value : values) {
            WordClass wordClass = WordClass.fromRawValue(value);
            wordClassList.add(wordClass);
        }
        WordClass mainWordClass = wordClassList.get(0);
        String fullNames = String.join(" ", wordClassList.stream().map(WordClass::getCanonicalName).toList());
        importedWord.setAllWordClasses(fullNames);
        importedWord.setWordClass(mainWordClass.getCanonicalName());
    }
}
