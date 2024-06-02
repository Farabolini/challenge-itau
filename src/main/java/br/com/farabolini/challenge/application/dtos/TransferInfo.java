package br.com.farabolini.challenge.application.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransferInfo (
        @JsonProperty("idOrigem")
        @Schema(name = "idOrigem", example = "6a7fa3e4-b060-4c47-847a-db298c2f7a49", title = "Sender Account ID")
        UUID senderAccountId,

        @JsonProperty("idDestino")
        @Schema(name = "idDestino", example = "6a7fa3e4-b060-4c47-847a-db298c2f7a49", title = "Recipient Account ID")
        UUID recipientAccountId
) { }
