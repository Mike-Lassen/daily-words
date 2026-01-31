package io.grann.words;

import io.grann.words.importer.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class FrenchTransformApplication {
    private static final String RESOURCE_NAME = "french-500.csv";
    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(FrenchTransformApplication.class, args);
        InputStream resourceStream = FrenchTransformApplication.class.getClassLoader().getResourceAsStream(RESOURCE_NAME);
        if (resourceStream == null) {
            throw new IllegalStateException("Could not find resource on classpath: " + RESOURCE_NAME);
        }
        CsvMapImporter importer = new CsvMapImporter();
        try {
            List<Map<String, String>> rows = importer.importToMaps(resourceStream);
            rows.stream().limit(10)
                    .forEach(map -> {
                        System.out.println(map.toString());
                    });
            List<ImportedWord> importedWords = rows.stream().map(ImportedWordRowMapper::fromRowMap).toList();
            importedWords.forEach(WordClassNormalizer::normalizeWordClass);
            importedWords.forEach(TranslationNormalizer::normalizeTranslation);
            LevelAssigner.assignLevelsInPlace(importedWords);
            importedWords.stream()
                    .limit(10)
                    .forEach(word -> {
                        System.out.println(word.toString());
                    });
            OutputStream outputStream = new FileOutputStream("french-500-out.csv");
            ImportedWordCsvExporter.exportToCsv(importedWords, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
