package org.kz.minibank.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.kz.minibank.converter.CurrencyConverter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Random;

@Entity
@Getter
@Setter
@Table(name = "accounts")
@EntityListeners(AuditingEntityListener.class)
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @NotNull
    @Column(unique = true, length = 26)
    private String accountNumber;

    @NotNull
    @Min(0)
    private BigDecimal balance = BigDecimal.ZERO;

    @NotNull
    @Convert(converter = CurrencyConverter.class)
    private Currency currency;

    @NotNull
    @ManyToOne
    private User user;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "sourceAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> outGoingTransactions = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "targetAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> incomingTransactions = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public Account(){
        this.accountNumber = generateAccountNumber();
    }

    public Account(Currency currency, User user) {
        this.currency = currency;
        this.user = user;
        this.accountNumber = generateAccountNumber();
    }

    private String generateAccountNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        Random random = new Random();
        StringBuilder randomPart = new StringBuilder();
        int randomLength = 26 - timestamp.length();

        for (int i = 0; i < randomLength; i++) {
            randomPart.append(random.nextInt(10));
        }
        return timestamp + randomPart.toString();
    }

    public void addOutGoingTransaction(Transaction transaction) {
        this.outGoingTransactions.add(transaction);
        transaction.setSourceAccount(this);
    }

    public void addIncomingTransaction(Transaction transaction) {
        this.incomingTransactions.add(transaction);
        transaction.setTargetAccount(this);
    }

    public void removeTransaction(Transaction transaction) {
        this.outGoingTransactions.remove(transaction);
        transaction.setTargetAccount(null);
    }
}
