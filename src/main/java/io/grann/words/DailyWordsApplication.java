package io.grann.words;

import io.grann.words.bootstrap.DeckCsvImporter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DailyWordsApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DailyWordsApplication.class, args);

		boolean seedEnabled = Boolean.parseBoolean(
				context.getEnvironment().getProperty("app.seed.enabled", "true")
		);
		if (!seedEnabled) {
			return;
		}

		try {
			DeckCsvImporter deckCsvImporter = context.getBean(DeckCsvImporter.class);
			deckCsvImporter.importFromClasspath("genki-deck.csv");
			deckCsvImporter.importFromClasspath("french-500-deck.csv");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
