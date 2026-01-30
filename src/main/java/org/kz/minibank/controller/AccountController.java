package org.kz.minibank.controller;


import jakarta.validation.Valid;
import org.kz.minibank.DTO.AccountResponseDTO;
import org.kz.minibank.DTO.CreateAccountRequest;
import org.kz.minibank.DTO.TransactionResponseDTO;
import org.kz.minibank.model.Account;
import org.kz.minibank.model.Transaction;
import org.kz.minibank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Currency;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody CreateAccountRequest request, Principal principal) {
        Account account = accountService.createAccount(principal.getName(), request.currencyCode());

        return ResponseEntity.status(201).body(new AccountResponseDTO(
                account.getId(),
                account.getAccountNumber(), account.getBalance(), account.getCurrency().getCurrencyCode(),
                account.getUser().getName(), account.getUser().getSurname(), account.getUser().getEmail()
        ));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable Long id, Principal principal){
        validateOwnership(id, principal.getName());
        return ResponseEntity.ok(accountService.getBalance(id));
    }

    @GetMapping("/{id}/transactionHistory")
    public ResponseEntity<Page<Transaction>> getTransactionHistory(@PathVariable Long id, Pageable pageable, Principal principal){
        validateOwnership(id, principal.getName());
        return ResponseEntity.ok(accountService.getTransactionsByAccountId(id, pageable));
    }

    @GetMapping("/{id}/incomingTransactions")
    public ResponseEntity<List<TransactionResponseDTO>> getIncomingTransactions(@PathVariable Long id, Principal principal){
        validateOwnership(id, principal.getName());
        List<Transaction> transactions = accountService.getIncomingTransactionsByAccountId(id);
        List<TransactionResponseDTO> response = transactions.stream().map(transaction -> new TransactionResponseDTO(
                transaction.getAmount(),
                transaction.getSourceAccount().getCurrency().getCurrencyCode(),
                transaction.getSourceAccount().getAccountNumber(),
                transaction.getTargetAccount().getAccountNumber(),
                transaction.getTitle(),
                transaction.getStatus()

        )).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/outgoingTransactions")
    public ResponseEntity<List<TransactionResponseDTO>> getOutgoingTransactions(@PathVariable Long id, Principal principal){
        validateOwnership(id, principal.getName());

        List<Transaction> transactions = accountService.getOutcomingTransactionsByAccountId(id);
        List<TransactionResponseDTO> response = transactions.stream().map(transaction -> new TransactionResponseDTO(
                transaction.getAmount(),
                transaction.getTargetAccount().getCurrency().getCurrencyCode(),
                transaction.getSourceAccount().getAccountNumber(),
                transaction.getTargetAccount().getAccountNumber(),
                transaction.getTitle(),
                transaction.getStatus()

        )).toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id, Principal principal) {
        validateOwnership(id, principal.getName());
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    private void validateOwnership(Long accountId, String email){
        boolean isOwner = accountService.isAccountOwner(accountId, email);
        if (!isOwner) { throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this account!");
        }
    }
}
