package org.kz.minibank.controller;

import jakarta.validation.Valid;
import org.kz.minibank.DTO.CreateDepositDTO;
import org.kz.minibank.DTO.CreateTransactionRequest;
import org.kz.minibank.DTO.TransactionResponseDTO;
import org.kz.minibank.model.Transaction;
import org.kz.minibank.service.AccountService;
import org.kz.minibank.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;

    public TransactionController(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody CreateTransactionRequest request, Principal principal) {
        validateOwnership(request.sourceAccountNumber(), principal.getName());
        Transaction transaction = transactionService.createTransactionBetweenAccounts(
                request.sourceAccountNumber(), request.targetAccountNumber(), request.amount(), request.title()
        );
        return ResponseEntity.status(201).body(new TransactionResponseDTO(
                transaction.getAmount(),
                transaction.getTargetAccount().getCurrency().getCurrencyCode(),
                transaction.getSourceAccount().getAccountNumber(),
                transaction.getTargetAccount().getAccountNumber(),
                transaction.getTitle(),
                transaction.getStatus()
        ));
    }
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDTO> createDeposit(@Valid @RequestBody CreateDepositDTO request, Principal principal) {
        validateOwnership(request.targetAccountNumber(), principal.getName());
        Transaction transaction = transactionService.deposit(request.targetAccountNumber(), request.amount());

        return ResponseEntity.status(201).body(new TransactionResponseDTO(
                transaction.getAmount(),
                transaction.getTargetAccount().getCurrency().getCurrencyCode(),
                transaction.getSourceAccount().getAccountNumber(),
                transaction.getTargetAccount().getAccountNumber(),
                transaction.getTitle(),
                transaction.getStatus()
        ));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDTO> createWithdraw(@Valid @RequestBody CreateDepositDTO request, Principal principal) {
        validateOwnership(request.targetAccountNumber(), principal.getName());
        Transaction transaction = transactionService.withdraw(request.targetAccountNumber(), request.amount());

        return ResponseEntity.status(201).body(new TransactionResponseDTO(
                transaction.getAmount(),
                transaction.getTargetAccount().getCurrency().getCurrencyCode(),
                transaction.getSourceAccount().getAccountNumber(),
                transaction.getTargetAccount().getAccountNumber(),
                transaction.getTitle(),
                transaction.getStatus()
        ));
    }

    private void validateOwnership(String accountNumber, String email){
        boolean isOwner = accountService.isAccountOwner(accountNumber, email);
        if (!isOwner) { throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this account!");
        }
    }
}
