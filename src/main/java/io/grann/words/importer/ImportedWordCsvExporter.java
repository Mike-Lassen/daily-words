package io.grann.words.importer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ImportedWordCsvExporter {

    private ImportedWordCsvExporter() {}

    public static void exportToCsv(
            List<ImportedWord> importedWords,
            OutputStream outputStream
    ) throws IOException {

        Objects.requireNonNull(importedWords, "importedWords");
        Objects.requireNonNull(outputStream, "outputStream");

        List<ImportedWord> sortedWords = importedWords.stream()
                .sorted(
                        Comparator
                                .comparing(ImportedWord::getLevel, Comparator.nullsLast(Integer::compareTo))
                                .thenComparing(ImportedWord::getRank, Comparator.nullsLast(Integer::compareTo))
                )
                .toList();

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
             CSVPrinter csvPrinter = new CSVPrinter(
                     writer,
                     CSVFormat.DEFAULT
                             .builder()
                             .setDelimiter(';')
                             .setHeader(
                                     "Level",
                                     "Rank",
                                     "Word",
                                     "Alternate",
                                     "Translation",
                                     "Note",
                                     "WordClass",
                                     "AllWordClasses"
                             )
                             .build()
             )) {

            for (ImportedWord importedWord : sortedWords) {
                csvPrinter.printRecord(
                        importedWord.getLevel(),
                        importedWord.getRank(),
                        importedWord.getWord(),
                        importedWord.getAlternate(),
                        importedWord.getTranslation(),
                        importedWord.getNote(),
                        importedWord.getWordClass(),
                        importedWord.getAllWordClasses()
                );
            }

            csvPrinter.flush();
        }
    }
}
