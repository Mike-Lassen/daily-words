package io.grann.words;

import io.grann.words.bootstrap.DeckCsvImporter;
import io.grann.words.domain.Deck;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DailyWordsApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DailyWordsApplication.class, args);
		try {
			DeckCsvImporter deckCsvImporter = context.getBean(DeckCsvImporter.class);
			Deck deck = deckCsvImporter.importFromClasspath("genki-deck.csv");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
