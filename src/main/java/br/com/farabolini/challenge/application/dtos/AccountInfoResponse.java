package br.com.farabolini.challenge.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record AccountInfoResponse(
        @JsonProperty("id") UUID id,
        @JsonProperty("clienteId") UUID customerId,
        @JsonProperty("saldo") Double balance,
        @JsonProperty("ativo") boolean active,
        @JsonProperty("limiteDiario") Double dailyLimit
) { }
