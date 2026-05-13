package io.grann.words.api.deck;

import io.grann.words.domain.Deck;
import io.grann.words.repository.DeckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DeckService {
    private final DeckRepository deckRepository;

    public DeckService(DeckRepository deckRepository) {
        this.deckRepository = deckRepository;
    }
    public List<DeckResponse> getDecks() {
        return deckRepository
                .findAll()
                .stream()
                .map(DeckResponse::from)
                .toList();
    }
    public DeckResponse getDeck(long id) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new DeckNotFoundException(id));
        return DeckResponse.from(deck);
    }
    public DeckResponse createDeck(DeckCreateRequest request) {
        Deck deck = Deck.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        Deck saved = deckRepository.save(deck);
        return DeckResponse.from(saved);
    }
    public DeckResponse updateDeck(long id, DeckUpdateRequest request) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new DeckNotFoundException(id));

        if (request.getName().isPresent()) {
            String name = request.getName().get();

            if (name == null || name.isBlank()) {
                throw new BadRequestException("name must not be null or blank");
            }

            deck.setName(name);
        }

        if (request.getDescription().isPresent()) {
            deck.setDescription(request.getDescription().get());
        }

        Deck saved = deckRepository.save(deck);

        return DeckResponse.from(saved);
    }
    public void deleteDeck(long id) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new DeckNotFoundException(id));
        deckRepository.delete(deck);
    }

}
