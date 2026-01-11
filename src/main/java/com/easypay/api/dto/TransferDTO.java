package com.easypay.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferDTO(

    @NotBlank(message = "Sender document is mandatory")
    String senderDocument,

    @NotBlank(message = "Receiver document is mandatory")
    String receiverDocument,

    @NotNull(message = "Amount is mandatory")
    BigDecimal amount,

    @NotBlank(message = "Password is mandatory")
    String senderPassword,

    String idempotencyKey
) {}
