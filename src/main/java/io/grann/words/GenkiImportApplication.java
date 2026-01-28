package io.grann.words;

import io.grann.words.bootstrap.DeckCsvImporter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class GenkiImportApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(GenkiImportApplication.class, args);

        try {
            DeckCsvImporter deckCsvImporter = context.getBean(DeckCsvImporter.class);
            deckCsvImporter.importFromClasspath("french-500-deck.csv");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        SpringApplication.exit(context);
    }
}
