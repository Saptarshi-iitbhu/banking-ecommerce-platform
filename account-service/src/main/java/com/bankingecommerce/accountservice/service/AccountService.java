package com.bankingecommerce.accountservice.service;

import com.bankingecommerce.accountservice.dto.WalletCreditRequest;
import com.bankingecommerce.accountservice.dto.WalletDebitRequest;
import com.bankingecommerce.accountservice.exception.AccountNotFoundException;
import com.bankingecommerce.accountservice.exception.InsufficientFundsException;
import com.bankingecommerce.accountservice.model.Account;
import com.bankingecommerce.accountservice.model.AccountStatus;
import com.bankingecommerce.accountservice.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final long INITIAL_BACKOFF_MS = 50;

    public AccountService(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }

    public Account getAccountById(UUID id){
        return accountRepository.findByUserId(id)
                .orElseThrow(()-> new AccountNotFoundException("Account nnot found: " + id));
    }

    public Account getAccountByUserId(UUID userId){
        return accountRepository.findByUserId(userId)
                .orElseThrow(()-> new AccountNotFoundException("No wallet found for user: " + userId));
    }

    public Account getOrCreateAndCredit(WalletCreditRequest request){
        return withOptimisticRetry(()->doCredit(request));
    }

    public Account debitWallet(WalletDebitRequest request){
        return withOptimisticRetry(()->doDebit(request));
    }

    @Transactional
    protected Account doCredit(WalletCreditRequest request){
        Account account = accountRepository.findByUserId(request.getUserId())
                .orElseGet(()->buildNewAccount(request));

        account.setBalance(account.getBalance().add(request.getAmount()));
        return accountRepository.save(account);
    }

    @Transactional
    protected Account doDebit(WalletDebitRequest request){
        Account account = getAccountByUserId(request.getUserId());

        if(account.getStatus() != AccountStatus.ACTIVE){
            throw new InsufficientFundsException(
                    "Account is not Active: " + request.getUserId()
            );
        }

        if(account.getBalance().compareTo(request.getAmount()) < 0){
            throw new InsufficientFundsException(
                    "Insufficient wallet Balance: " +request.getUserId()
            );
        }
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        return accountRepository.save(account);
    }

    private Account withOptimisticRetry(Supplier<Account> action){
        int attempts = 0;
        long backoff = INITIAL_BACKOFF_MS;

        while(true){
            try{
                return action.get();
            } catch (OptimisticLockingFailureException ex){
                attempts++;
                if(attempts > MAX_RETRY_ATTEMPTS){
                    throw ex;
                }
                sleep(backoff);
                backoff *= 2;
            }
        }
    }

    private void sleep(long millis){
        try{
            Thread.sleep(millis);
        } catch (InterruptedException ex){
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry Interupted", ex   );
        }
    }

    private Account buildNewAccount(WalletCreditRequest request){
        return Account.builder()
                .userId(request.getUserId())
                .ownerName(request.getOwnerName())
                .email(request.getEmail())
                .accountNumber(generateAccountNumber())
                .balance(BigDecimal.ZERO)
                .build();
    }

    private String generateAccountNumber() {
        long random = ThreadLocalRandom.current().nextLong(1_000_000_0000L, 9_999_999_9999L);
        return "ACC" + random;
    }
}
