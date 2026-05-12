package io.grann.words.api.deck;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.openapitools.jackson.nullable.JsonNullable;

@Data
public class DeckUpdateRequest {

    @Schema(
        description = "Omit to leave unchanged. Must not be null if provided.",
        nullable = false
    )
    private JsonNullable<String> name = JsonNullable.undefined();

    @Schema(
        description = "Omit to leave unchanged. Set to null to clear the description.",
        nullable = true
    )
    private JsonNullable<String> description = JsonNullable.undefined();
}