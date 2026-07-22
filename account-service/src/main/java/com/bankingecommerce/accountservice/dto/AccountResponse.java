package com.bankingecommerce.accountservice.dto;

import com.bankingecommerce.accountservice.model.Account;
import com.bankingecommerce.accountservice.model.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    UUID id;
    UUID userId;
    String accountNumber;
    String ownerName;
    String email;
    BigDecimal balance;
    AccountStatus status;
    Instant createdAt;
    Instant updatedAt;

    public static AccountResponse fromEntity(Account account){
        return AccountResponse.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .accountNumber(account.getAccountNumber())
                .ownerName(account.getOwnerName())
                .email(account.getEmail())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
