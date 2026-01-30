package org.kz.minibank.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kz.minibank.converter.CurrencyPairConverter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rates")
@NoArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class ExchangeRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = CurrencyPairConverter.class)
    private CurrencyPair pair;

    @NotNull
    @Column(precision = 10, scale = 4)
    private BigDecimal rate;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime timestamp;

    public ExchangeRate(CurrencyPair pair, BigDecimal rate) {
        this.pair = pair;
        this.rate = rate;
        this.timestamp = LocalDateTime.now();
    }
}
