package io.grann.words.dashboard;

import io.grann.words.domain.Deck;
import io.grann.words.domain.Word;
import io.grann.words.repository.DeckRepository;
import io.grann.words.repository.WordRepository;
import io.grann.words.session.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/decks/{deckId}/words")
public class DeckWordsController {

    private static final int PAGE_SIZE = 200;

    private final DeckRepository deckRepository;
    private final WordRepository wordRepository;
    private final UserSession userSession;

    @GetMapping
    public String deckWords(
            @PathVariable("deckId") Long deckId,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            Model model
    ) {
        if (userSession.getUserAccountId() == null) {
            return "redirect:/";
        }

        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found: " + deckId));

        int safePage = Math.max(page, 0);
        String safeQ = normalizeQuery(q);

        List<Word> words = wordRepository.findDeckWords(deckId, safeQ, PageRequest.of(safePage, PAGE_SIZE));
        long totalCount = wordRepository.countDeckWords(deckId, safeQ);

        model.addAttribute("deck", deck);
        model.addAttribute("deckId", deckId);
        model.addAttribute("q", safeQ);
        model.addAttribute("page", safePage);
        model.addAttribute("size", PAGE_SIZE);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("words", words);

        return "deck-words";
    }

    @GetMapping("/table")
    public String deckWordsTable(
            @PathVariable("deckId") Long deckId,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            Model model
    ) {
        if (userSession.getUserAccountId() == null) {
            return "redirect:/";
        }

        int safePage = Math.max(page, 0);
        String safeQ = normalizeQuery(q);

        List<Word> words = wordRepository.findDeckWords(deckId, safeQ, PageRequest.of(safePage, PAGE_SIZE));
        long totalCount = wordRepository.countDeckWords(deckId, safeQ);

        model.addAttribute("deckId", deckId);
        model.addAttribute("q", safeQ);
        model.addAttribute("page", safePage);
        model.addAttribute("size", PAGE_SIZE);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("words", words);

        return "fragments/deck-words-table :: table";
    }

    private static String normalizeQuery(String q) {
        if (q == null) {
            return "";
        }
        String trimmed = q.trim();
        return trimmed;
    }
}

