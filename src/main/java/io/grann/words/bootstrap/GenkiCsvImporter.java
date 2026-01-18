package io.grann.words.bootstrap;

import io.grann.words.domain.*;
import io.grann.words.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GenkiCsvImporter  {

    private final DeckRepository deckRepository;
    private final WordRepository wordRepository;
    private final WordAnnotationRepository annotationRepository;

    @Transactional
    public void run() throws Exception {

        // ----- Create deck + level (idempotent enough for MVP) -----

        Deck deck = Deck.builder()
                .name("Genki I")
                .description("Genki I textbook vocabulary")
                .build();

        Level lesson1 = Level.builder()
                .name("Lesson 1")
                .orderIndex(1)
                .deck(deck)
                .build();

        deck.setLevels(List.of(lesson1));
        deckRepository.save(deck);

        // ----- Read CSV -----

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        getClass().getClassLoader().getResourceAsStream("genki-deck.csv"),
                        StandardCharsets.UTF_8
                )
        );

        String line;
        List<Word> words = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            if (line.isBlank() || line.startsWith("Foreign")) {
                continue;
            }

            String[] columns = line.split(";");
            String foreignText = columns[0];
            String kana = columns[1];
            String nativeText = columns[2];

            Word word = Word.builder()
                    .foreignText(foreignText)
                    .nativeText(nativeText)
                    .level(lesson1)
                    .status(WordStatus.LEARNING)
                    .build();


            //wordRepository.save(word);
            words.add(word);

            WordAnnotation kanaAnnotation = WordAnnotation.builder()
                    .word(word)
                    .type(WordAnnotationType.KANA)
                    .value(kana)
                    .build();

            word.getAnnotations().add(kanaAnnotation);
            //annotationRepository.save(kanaAnnotation);
        }

        wordRepository.saveAll(words);
    }
}
