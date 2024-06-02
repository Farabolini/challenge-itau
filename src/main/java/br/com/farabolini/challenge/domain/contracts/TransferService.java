package br.com.farabolini.challenge.domain.contracts;

import java.util.UUID;

public interface TransferService {

    UUID transfer(UUID senderAccountId, UUID recipientAccountId, Double amount);

}
