package io.grann.words.api.deck;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeckResponse {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(nullable = true)
    private String description;
}
