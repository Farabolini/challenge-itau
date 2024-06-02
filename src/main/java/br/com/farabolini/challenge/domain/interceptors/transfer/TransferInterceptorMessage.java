package br.com.farabolini.challenge.domain.interceptors.transfer;

public record TransferInterceptorMessage (
        boolean senderAccountIsActive,
        boolean recipientAccountIsActive,
        Double amount,
        Double senderBalance,
        Double senderDailyLimit
) { }
