package io.grann.words.api.deck;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.grann.words.domain.Deck;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonPropertyOrder({ "id", "name", "description" })
public class DeckResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(nullable = true)
    private String description;

    public static DeckResponse from(Deck deck) {
        return DeckResponse.builder()
                .id(deck.getId())
                .name(deck.getName())
                .description(deck.getDescription())
                .build();
    }
}
