package org.kz.minibank.service;


import org.kz.minibank.model.Account;
import org.kz.minibank.model.Transaction;
import org.kz.minibank.model.User;
import org.kz.minibank.repository.AccountRepository;
import org.kz.minibank.repository.TransactionRepository;
import org.kz.minibank.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }


    @Transactional
    public Account createAccount(String email, String currencyCode) {
        if (email.isBlank() || currencyCode == null) { throw new IllegalArgumentException("All fields are required!"); }
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found!"));

        Currency currency = Currency.getInstance(currencyCode.toUpperCase());

        return accountRepository.save(new Account(currency, user));
    }

    public BigDecimal getBalance(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow(() -> new IllegalArgumentException("Account not found!")).getBalance();
    }

    public List<Account> getAccountsByUserId(String email){
        return accountRepository.findAllByUserEmail(email);
    }

    public Page<Transaction> getTransactionsByAccountId(Long accountId, Pageable pageable){
        return transactionRepository.findAllByAccountId(accountId, pageable);
    }

    public List<Transaction> getIncomingTransactionsByAccountId(Long accountId){
        return transactionRepository.findByTargetAccountIdOrderByTimestampDesc(accountId);
    }

    public List<Transaction> getOutcomingTransactionsByAccountId(Long accountId){
        return transactionRepository.findBySourceAccountIdOrderByTimestampDesc(accountId);
    }

    @Transactional
    public void deleteAccount(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new IllegalArgumentException("Account not found!"));

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Account balance must be zero to delete it!");
        }
        accountRepository.deleteById(accountId);
    }
    public boolean isAccountOwner(Long accountId, String email){
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new IllegalArgumentException("Account not found!"));
        return account.getUser().getEmail().equals(email);
    }

    public boolean isAccountOwner(String accountNumber, String email){
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) { throw new IllegalArgumentException("Account not found!"); }
        return account.getUser().getEmail().equals(email);
    }
}
