package org.kz.minibank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kz.minibank.model.Account;
import org.kz.minibank.model.User;
import org.kz.minibank.repository.AccountRepository;
import org.kz.minibank.repository.TransactionRepository;
import org.kz.minibank.repository.UserRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        user = new User("John", "Doe", "john@example.com", "password");
        user.setId(1L);
        account = new Account(Currency.getInstance("USD"), user);
        account.setId(1L);
        account.setBalance(BigDecimal.ZERO);
    }

    @Test
    void createAccount_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account created = accountService.createAccount("john@example.com", "usd");

        assertNotNull(created);
        assertEquals(user, created.getUser());
        assertEquals("USD", created.getCurrency().getCurrencyCode());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> accountService.createAccount("unknown@example.com", "USD"));
    }

    @Test
    void createAccount_InvalidInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> accountService.createAccount("", "usd"));
        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount("john@example.com", null));
    }

    @Test
    void getBalance_Success() {
        account.setBalance(new BigDecimal("100.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        BigDecimal balance = accountService.getBalance(1L);

        assertEquals(new BigDecimal("100.00"), balance);
    }

    @Test
    void getBalance_AccountNotFound_ThrowsException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> accountService.getBalance(99L));
    }

    @Test
    void getAccountsByUserId_Success() {
        when(accountRepository.findAllByUserEmail("john@example.com")).thenReturn(List.of(account));

        List<Account> accounts = accountService.getAccountsByUserId("john@example.com");

        assertFalse(accounts.isEmpty());
        assertEquals(1, accounts.size());
    }

    @Test
    void deleteAccount_Success() {
        account.setBalance(BigDecimal.ZERO);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        accountService.deleteAccount(1L);

        verify(accountRepository).deleteById(1L);
    }

    @Test
    void deleteAccount_NonZeroBalance_ThrowsException() {
        account.setBalance(new BigDecimal("10.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(IllegalArgumentException.class, () -> accountService.deleteAccount(1L));
        verify(accountRepository, never()).deleteById(anyLong());
    }

    @Test
    void isAccountOwner_ById_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertTrue(accountService.isAccountOwner(1L, "john@example.com"));
        assertFalse(accountService.isAccountOwner(1L, "other@example.com"));
    }

    @Test
    void isAccountOwner_ByAccountNumber_Success() {
        when(accountRepository.findByAccountNumber("123")).thenReturn(account);

        assertTrue(accountService.isAccountOwner("123", "john@example.com"));
    }
}
