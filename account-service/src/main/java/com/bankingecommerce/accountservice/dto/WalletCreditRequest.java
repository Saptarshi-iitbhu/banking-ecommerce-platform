package com.bankingecommerce.accountservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletCreditRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    private String ownerName;

    @NotBlank
    @Email
    private String email;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String idempotencyKey;
}
