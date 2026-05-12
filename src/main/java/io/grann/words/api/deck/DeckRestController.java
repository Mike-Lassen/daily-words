package io.grann.words.api.deck;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
public class DeckRestController {

    @GetMapping
    public List<DeckResponse> getDecks() {
        return null;
    }

    @GetMapping("/{deckId}")
    public DeckResponse getDeck(@PathVariable Long deckId) {
        return null;
    }

    @PostMapping
    public DeckResponse createDeck(@RequestBody DeckCreateRequest request) {
        return null;
    }

    @PutMapping("/{deckId}")
    public DeckResponse updateDeck(
            @PathVariable Long deckId,
            @RequestBody DeckUpdateRequest request
    ) {
        return null;
    }

    @DeleteMapping("/{deckId}")
    public void deleteDeck(@PathVariable Long deckId) {

    }
}