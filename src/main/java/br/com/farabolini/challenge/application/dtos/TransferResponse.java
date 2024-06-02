package br.com.farabolini.challenge.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record TransferResponse (
        @JsonProperty("id_transferencia")
        @Schema(name = "id_transferencia", title = "ID of completed transfer.")
        UUID transferId
) { }
