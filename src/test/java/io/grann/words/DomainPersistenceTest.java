package io.grann.words;

import io.grann.words.domain.Deck;
import io.grann.words.domain.Level;
import io.grann.words.domain.Word;
import io.grann.words.repository.DeckRepository;
import io.grann.words.repository.WordRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
//@Transactional
class DomainPersistenceTest {

    @Autowired
    private DeckRepository deckRepository;
    @Autowired
    private WordRepository wordRepository;


    @Test
    @Disabled
    void persistDeckWithLevelAndWord() {
        Deck deck = Deck.builder()
                .name("Genki I")
                .build();

        Level level = Level.builder()
                .name("Lesson 1")
                .orderIndex(1)
                .deck(deck)
                .build();

        Word word = Word.builder()
                .foreignText("勉強する")
                .nativeText("to study")
                .level(level)
                .build();

        deck.setLevels(List.of(level));

        deckRepository.save(deck);
    }

    @Test
    public void persistWordsLesson1() {
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

        List<Word> words = List.of(
                word("あの", "um…", lesson1),
                word("今", "now", lesson1),
                word("英語", "English (language)", lesson1),
                word("ええ", "yes", lesson1),
                word("学生", "student", lesson1),
                word("〜語", "…language", lesson1),
                word("高校", "high school", lesson1),
                word("午後", "P.M.", lesson1),
                word("午前", "A.M.", lesson1),
                word("〜歳", "…years old", lesson1)
        );

        wordRepository.saveAll(words);
    }

    private Word word(String foreign, String nativeText, Level level) {
        return Word.builder()
                .foreignText(foreign)
                .nativeText(nativeText)
                .level(level)
                .build();
    }

}
