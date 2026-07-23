package com.bankingecommerce.accountservice.controller;

import com.bankingecommerce.accountservice.dto.AccountResponse;
import com.bankingecommerce.accountservice.dto.WalletCreditRequest;
import com.bankingecommerce.accountservice.dto.WalletDebitRequest;
import com.bankingecommerce.accountservice.model.Account;
import com.bankingecommerce.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable UUID id){
        Account account = accountService.getAccountById(id);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @GetMapping("/user/{userid}")
    public ResponseEntity<AccountResponse> getAccountByUserId(@PathVariable UUID userid){
        Account account = accountService.getAccountByUserId(userid);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @PostMapping("/credit")
    public ResponseEntity<AccountResponse> creditWallet(@Valid @RequestBody WalletCreditRequest request) {
        Account account = accountService.getOrCreateAndCredit(request);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }

    @PostMapping("/debit")
    public ResponseEntity<AccountResponse> debitWallet(@Valid @RequestBody WalletDebitRequest request) {
        Account account = accountService.debitWallet(request);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }
}
