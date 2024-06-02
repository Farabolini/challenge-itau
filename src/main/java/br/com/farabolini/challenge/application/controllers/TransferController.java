package br.com.farabolini.challenge.application.controllers;

import br.com.farabolini.challenge.application.dtos.ApplicationErrorResponse;
import br.com.farabolini.challenge.application.dtos.BankTransfer;
import br.com.farabolini.challenge.application.dtos.TransferResponse;
import br.com.farabolini.challenge.domain.contracts.TransferService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/transferencia")
@Validated
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer successfully completed.",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = TransferResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Unable to complete transfer due to some error coming from client request.",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Unable to complete transfer due to data not found.",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Possible transfer duplicity.",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApplicationErrorResponse.class))})
    })
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody BankTransfer bankTransfer) {
        UUID transferId = transferService.transfer(bankTransfer.transferInfo().senderAccountId(), bankTransfer.transferInfo().recipientAccountId(), bankTransfer.amount());
        return ResponseEntity.ok(new TransferResponse(transferId));
    }

}
