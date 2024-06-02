package br.com.farabolini.challenge.application.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BankTransfer(
        @JsonIgnore
        UUID bankTransferId,

        @JsonProperty("valor")
        @DecimalMin(value = "0.0", inclusive = false, message = "valor must be greater than 0")
        @Schema(name = "valor", example = "100.00", title = "Amount to transfer")
        Double amount,

        @JsonProperty("conta") TransferInfo transferInfo
) { }
