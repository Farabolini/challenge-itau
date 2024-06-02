package br.com.farabolini.challenge.unit.application.controllers;

import br.com.farabolini.challenge.application.controllers.TransferController;
import br.com.farabolini.challenge.application.dtos.BankTransfer;
import br.com.farabolini.challenge.application.dtos.TransferInfo;
import br.com.farabolini.challenge.application.dtos.TransferResponse;
import br.com.farabolini.challenge.domain.contracts.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class TransferControlerTest {

    @Mock
    private TransferService transferService;

    @InjectMocks
    private TransferController transferController;

    @BeforeEach
    public void setup() {
        openMocks(this);
    }

    @Test
    public void shouldReturnBankTransferId() {
        BankTransfer bankTransfer = new BankTransfer(UUID.randomUUID(), 10.0, new TransferInfo(UUID.randomUUID(), UUID.randomUUID()));
        UUID bankTransferId = UUID.randomUUID();

        when(transferService.transfer(bankTransfer.transferInfo().senderAccountId(), bankTransfer.transferInfo().recipientAccountId(), bankTransfer.amount())).thenReturn(bankTransferId);

        ResponseEntity<TransferResponse> response = transferController.transfer(bankTransfer);
        assertEquals(bankTransferId, response.getBody().transferId());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

}
