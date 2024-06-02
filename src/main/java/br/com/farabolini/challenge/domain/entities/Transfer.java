package br.com.farabolini.challenge.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Transfer {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    private UUID id;

    @Column(name = "sender_id")
    private UUID senderId;

    @Column(name = "sender_account_id")
    private UUID senderAccountId;

    @Column(name = "recipient_id")
    private UUID recipientId;

    @Column(name = "recipient_account_id")
    private UUID recipientAccountId;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "bacen_notified")
    private boolean bacenNotified;

    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

}
