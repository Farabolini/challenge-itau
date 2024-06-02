package br.com.farabolini.challenge.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApplicationErrorResponse (
        @Schema(name = "code", example = "500", title = "HTTP Status code")
        int code,

        @Schema(name = "message", example = "Some error happened, due to...", title = "Error description")
        String message
) { }
