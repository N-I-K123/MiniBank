package org.kz.minibank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kz.minibank.model.Account;
import org.kz.minibank.model.CurrencyPair;
import org.kz.minibank.model.Transaction;
import org.kz.minibank.model.TransactionStatus;
import org.kz.minibank.model.User;
import org.kz.minibank.repository.AccountRepository;
import org.kz.minibank.repository.TransactionRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private TransactionService transactionService;

    private Account sourceAccount;
    private Account targetAccount;

    @BeforeEach
    void setUp() {
        sourceAccount = new Account(Currency.getInstance("USD"), new User());
        sourceAccount.setId(1L);
        sourceAccount.setAccountNumber("SRC123");
        sourceAccount.setBalance(new BigDecimal("1000.00"));

        targetAccount = new Account(Currency.getInstance("USD"), new User());
        targetAccount.setId(2L);
        targetAccount.setAccountNumber("TGT456");
        targetAccount.setBalance(new BigDecimal("500.00"));
    }

    @Test
    void createTransactionBetweenAccounts_SameCurrency_Success() {
        when(accountRepository.findByAccountNumber("SRC123")).thenReturn(sourceAccount);
        when(accountRepository.findByAccountNumber("TGT456")).thenReturn(targetAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction result = transactionService.createTransactionBetweenAccounts("SRC123", "TGT456",
                new BigDecimal("100.00"), "Payment");

        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        assertEquals(new BigDecimal("900.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("600.00"), targetAccount.getBalance());
        verify(exchangeRateService, never()).convert(any(), any());
    }

    @Test
    void createTransactionBetweenAccounts_DifferentCurrency_Success() {
        targetAccount.setCurrency(Currency.getInstance("PLN"));

        when(accountRepository.findByAccountNumber("SRC123")).thenReturn(sourceAccount);
        when(accountRepository.findByAccountNumber("TGT456")).thenReturn(targetAccount);
        when(exchangeRateService.convert(eq(new BigDecimal("100.00")), any(CurrencyPair.class)))
                .thenReturn(new BigDecimal("400.00"));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction result = transactionService.createTransactionBetweenAccounts("SRC123", "TGT456",
                new BigDecimal("100.00"), "Payment");

        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        assertEquals(new BigDecimal("900.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("900.00"), targetAccount.getBalance()); // 500 + 400
        assertTrue(result.getTitle().contains("[FX: 100.00 USD -> 400.00 PLN]"));
    }

    @Test
    void createTransactionBetweenAccounts_InsufficientFunds_Fails() {
        when(accountRepository.findByAccountNumber("SRC123")).thenReturn(sourceAccount);
        when(accountRepository.findByAccountNumber("TGT456")).thenReturn(targetAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction result = transactionService.createTransactionBetweenAccounts("SRC123", "TGT456",
                new BigDecimal("2000.00"), "Payment");

        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertEquals("Insufficient funds!", result.getFailureReason());
        assertEquals(new BigDecimal("1000.00"), sourceAccount.getBalance());
    }

    @Test
    void createTransactionBetweenAccounts_SameAccount_Fails() {
        when(accountRepository.findByAccountNumber("SRC123")).thenReturn(sourceAccount);
        when(accountRepository.findByAccountNumber("SRC123")).thenReturn(sourceAccount); // calling same method
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction result = transactionService.createTransactionBetweenAccounts("SRC123", "SRC123",
                new BigDecimal("100.00"), "Payment");

        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertEquals("Source and target accounts must be different!", result.getFailureReason());
    }

    @Test
    void deposit_Success() {
        when(accountRepository.findByAccountNumber("SRC123")).thenReturn(sourceAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction result = transactionService.deposit("SRC123", new BigDecimal("100.00"));

        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        assertEquals(new BigDecimal("1100.00"), sourceAccount.getBalance());

        verify(accountRepository).save(sourceAccount);
    }

    @Test
    void deposit_NegativeAmount_ThrowsException() {
        when(accountRepository.findByAccountNumber("SRC123")).thenReturn(sourceAccount);
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.deposit("SRC123", new BigDecimal("-100.00")));
    }

    @Test
    void withdraw_Success() {
        when(accountRepository.findByAccountNumber("SRC123")).thenReturn(sourceAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction result = transactionService.withdraw("SRC123", new BigDecimal("100.00"));

        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        assertEquals(new BigDecimal("900.00"), sourceAccount.getBalance());

        verify(accountRepository).save(sourceAccount);
    }

    @Test
    void withdraw_InsufficientFunds_ReturnsFailedStatus() {
        when(accountRepository.findByAccountNumber("SRC123")).thenReturn(sourceAccount);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction result = transactionService.withdraw("SRC123", new BigDecimal("2000.00"));

        assertEquals(TransactionStatus.FAILED, result.getStatus());
        assertEquals("Insufficient funds!", result.getFailureReason());
        assertEquals(new BigDecimal("1000.00"), sourceAccount.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}
