package io.grann.words.api.deck;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@Tag(name = "Decks", description = "Operations for managing word decks")
public class DeckRestController {

    private final DeckService deckService;

    public DeckRestController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping
    @Operation(
            summary = "Get all decks",
            description = "Returns all available decks."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Decks returned successfully"
    )
    public List<DeckResponse> getDecks() {
        return deckService.getDecks();
    }

    @GetMapping("/{deckId}")
    @Operation(
            summary = "Get a deck",
            description = "Returns a single deck by its ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Deck returned successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Deck not found",
                    content = @Content
            )
    })
    public DeckResponse getDeck(
            @Parameter(description = "ID of the deck", example = "1")
            @PathVariable Long deckId
    ) {
        return deckService.getDeck(deckId);
    }

    @PostMapping
    @Operation(
            summary = "Create a deck",
            description = "Creates a new deck. The name is required and cannot be blank. The description is optional."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Deck created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content
            )
    })
    public DeckResponse createDeck(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Deck creation request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DeckCreateRequest.class))
            )
            @RequestBody DeckCreateRequest request
    ) {
        return deckService.createDeck(request);
    }

    @PutMapping("/{deckId}")
    @Operation(
            summary = "Update a deck",
            description = """
                    Partially updates a deck.

                    Omitted fields are left unchanged.
                    The name cannot be null or blank if provided.
                    The description can be set to null to clear it.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Deck updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Deck not found",
                    content = @Content
            )
    })
    public DeckResponse updateDeck(
            @Parameter(description = "ID of the deck", example = "1")
            @PathVariable Long deckId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Deck update request. Omitted fields are left unchanged.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DeckUpdateRequest.class))
            )
            @RequestBody DeckUpdateRequest request
    ) {
        return deckService.updateDeck(deckId, request);
    }

    @DeleteMapping("/{deckId}")
    @Operation(
            summary = "Delete a deck",
            description = "Deletes a deck by its ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Deck deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Deck not found",
                    content = @Content
            )
    })
    public void deleteDeck(
            @Parameter(description = "ID of the deck", example = "1")
            @PathVariable Long deckId
    ) {
        deckService.deleteDeck(deckId);
    }
}