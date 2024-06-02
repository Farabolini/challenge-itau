package br.com.farabolini.challenge.infrastructure.repositories;

import br.com.farabolini.challenge.domain.entities.Transfer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    @Modifying
    @Query(value = """
        UPDATE transfers SET bacen_notified = true
        WHERE id = :transferId
    """, nativeQuery = true)
    @Transactional
    void setBacenNotified(UUID transferId);

}
