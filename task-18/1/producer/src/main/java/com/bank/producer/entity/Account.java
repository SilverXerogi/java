package com.bank.producer.entity; 

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    // Конструктор для удобства создания
    public Account(BigDecimal balance) {
        this.balance = balance;
    }
}