package io.grann.words.api.deck;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeckCreateRequest {
    @NotBlank
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(nullable = true)
    private String description;
}
