package br.com.farabolini.challenge.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record CustomerInfoResponse(
        @JsonProperty("id") UUID id,
        @JsonProperty("nome") String name,
        @JsonProperty("telefone") String phoneNumber,
        @JsonProperty("tipoPessoa") String type
) { }
