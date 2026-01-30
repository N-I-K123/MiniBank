package org.kz.minibank.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Min(0)
    private BigDecimal amount;

    @CreatedDate
    private LocalDateTime timestamp;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @NotBlank
    private String title;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.PENDING;

    private String failureReason;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "target_account_id")
    private Account targetAccount;

    public Transaction(Account sourceAccount, Account targetAccount, BigDecimal amount, String title) {
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
        this.title = title;
        this.timestamp = LocalDateTime.now();
    }


    public void setTimeStamp(){
        this.timestamp = LocalDateTime.now();
    }
}
