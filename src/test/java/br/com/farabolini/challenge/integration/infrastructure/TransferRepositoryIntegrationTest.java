package br.com.farabolini.challenge.integration.infrastructure;

import br.com.farabolini.challenge.domain.entities.Transfer;
import br.com.farabolini.challenge.infrastructure.repositories.TransferRepository;
import br.com.farabolini.challenge.integration.TestContainersTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Sql(
        scripts = {"/scripts/drop_database.sql", "/scripts/transfer_repository.sql"}
)
public class TransferRepositoryIntegrationTest extends TestContainersTest {

    @Autowired
    private TransferRepository transferRepository;

    @Test
    public void shouldSetBacenNotified() {
        UUID transferId = UUID.fromString("1cb6519e-920b-4535-9f1d-f3b33b7e019e");

        transferRepository.setBacenNotified(transferId);

        Optional<Transfer> transfer = transferRepository.findById(transferId);
        assertFalse(transfer.isEmpty());
        assertTrue(transfer.get().isBacenNotified());
    }

}
