package org.kz.minibank.service;

import org.kz.minibank.model.Account;
import org.kz.minibank.model.CurrencyPair;
import org.kz.minibank.model.Transaction;
import org.kz.minibank.model.TransactionStatus;
import org.kz.minibank.repository.AccountRepository;
import org.kz.minibank.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ExchangeRateService exchangeRateService;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository,
            ExchangeRateService exchangeRateService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.exchangeRateService = exchangeRateService;
    }

    @Transactional
    public Transaction createTransactionBetweenAccounts(String sourceAccountNumber, String targetAccountNumber,
            BigDecimal amount, String title) {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setTitle(title);
        transaction.setStatus(TransactionStatus.PENDING);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive!");
        }

        Account sourceAccount = accountRepository.findByAccountNumber(sourceAccountNumber);
        Account targetAccount = accountRepository.findByAccountNumber(targetAccountNumber);

        if (sourceAccount == null || targetAccount == null) {
            throw new IllegalArgumentException("Account not found!");
        }
        if (title == null) {
            throw new IllegalArgumentException("Title is required!");
        }

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            return saveFailedTransaction(transaction, "Insufficient funds!");
        }

        if (sourceAccount.getId().equals(targetAccount.getId())) {
            return saveFailedTransaction(transaction, "Source and target accounts must be different!");
        }

        try {

            BigDecimal targetAmount = amount;

            if (!sourceAccount.getCurrency().equals(targetAccount.getCurrency())) {
                CurrencyPair pair = new CurrencyPair(sourceAccount.getCurrency(), targetAccount.getCurrency());
                targetAmount = exchangeRateService.convert(targetAmount, pair);
                title = title + String.format(" [FX: %s %s -> %s %s]",
                        amount, sourceAccount.getCurrency(), targetAmount, targetAccount.getCurrency());
            }

            sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
            targetAccount.setBalance(targetAccount.getBalance().add(targetAmount));

            accountRepository.save(sourceAccount);
            accountRepository.save(targetAccount);

            transaction.setSourceAccount(sourceAccount);
            transaction.setTargetAccount(targetAccount);
            transaction.setAmount(amount);
            transaction.setTitle(title);
            transaction.setStatus(TransactionStatus.SUCCESS);

            return transactionRepository.save(transaction);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public Transaction deposit(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found!");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive!");
        }
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setSourceAccount(account);
        transaction.setTargetAccount(account);
        transaction.setAmount(amount);
        transaction.setTitle("Deposit");

        transaction.setStatus(TransactionStatus.SUCCESS);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction withdraw(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Account not found!");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive!");
        }

        Transaction transaction = new Transaction();
        transaction.setSourceAccount(account);
        transaction.setTargetAccount(account);
        transaction.setAmount(amount);
        transaction.setTitle("Withdraw");
        if (account.getBalance().compareTo(amount) < 0) {
            return saveFailedTransaction(transaction, "Insufficient funds!");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        transaction.setStatus(TransactionStatus.SUCCESS);
        return transactionRepository.save(transaction);
    }

    private Transaction saveFailedTransaction(Transaction transaction, String reason) {
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setFailureReason(reason);
        return transactionRepository.save(transaction);
    }

}
