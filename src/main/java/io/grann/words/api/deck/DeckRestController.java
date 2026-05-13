package io.grann.words.api.deck;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
public class DeckRestController {
    private DeckService deckService;
    public DeckRestController(DeckService deckService) {
        this.deckService = deckService;
    }
    @GetMapping
    public List<DeckResponse> getDecks() {
        return deckService.getDecks();
    }

    @GetMapping("/{deckId}")
    public DeckResponse getDeck(@PathVariable Long deckId) {
        return deckService.getDeck(deckId);
    }

    @PostMapping
    public DeckResponse createDeck(@RequestBody DeckCreateRequest request) {
        return deckService.createDeck(request);
    }

    @PutMapping("/{deckId}")
    public DeckResponse updateDeck(
            @PathVariable Long deckId,
            @RequestBody DeckUpdateRequest request
    ) {
        return deckService.updateDeck(deckId, request);
    }

    @DeleteMapping("/{deckId}")
    public void deleteDeck(@PathVariable Long deckId) {
        deckService.deleteDeck(deckId);
    }
}
