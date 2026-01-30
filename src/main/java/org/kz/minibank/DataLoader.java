package org.kz.minibank;

import org.kz.minibank.model.Transaction;
import org.kz.minibank.model.User;
import org.kz.minibank.model.Account; // Importy Twoich klas
import java.util.Currency;
import org.kz.minibank.repository.UserRepository;
import org.kz.minibank.repository.AccountRepository;
import org.kz.minibank.service.AccountService;
import org.kz.minibank.service.TransactionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, AccountRepository accountRepository, TransactionService transactionService,
                      AccountService accountService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        User user = new User("Jan", "Kowalski", "jan@test.com", passwordEncoder.encode("haslo123"));
        userRepository.save(user);

        Account account = accountService.createAccount(user.getEmail(), "PLN");
        Account account2 = accountService.createAccount(user.getEmail(), "Eur");
        accountRepository.save(account);
        accountRepository.save(account2);

        System.out.println("--- BAZA DANYCH ZAŁADOWANA POPRAWNIE ---");
        System.out.println("User ID: " + user.getId());
        System.out.println("Account ID: " + account.getId());
        System.out.println("Account Balance: " + account.getBalance() + " " + account.getCurrency() + "");

        transactionService.deposit(account.getAccountNumber(), BigDecimal.valueOf(1000));
        account = accountRepository.findById(account.getId()).orElseThrow();
        System.out.println("Deposited 1000 PLN to account " + account.getId());
        System.out.println("Account Balance: " + account.getBalance() + " " + account.getCurrency() + "");

        transactionService.withdraw(account.getAccountNumber(), BigDecimal.valueOf(500));
        account = accountRepository.findById(account.getId()).orElseThrow();
        System.out.println("Withdrawn 500 PLN from account " + account.getId());
        System.out.println("Account Balance: " + account.getBalance() + " " + account.getCurrency() + "");

        transactionService.createTransactionBetweenAccounts(account.getAccountNumber(), account2.getAccountNumber(), BigDecimal.valueOf(100), "Test transaction from pln to eur");
        System.out.println("Created transaction between accounts");
        account = accountRepository.findById(account.getId()).orElseThrow();
        account2 = accountRepository.findById(account2.getId()).orElseThrow();
        System.out.println("Account1 Balance: " + account.getBalance() + " " + account.getCurrency() + "");
        System.out.println("Account2 Balance: " + account2.getBalance() + " " + account2.getCurrency() + "");

    }
}