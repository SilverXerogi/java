package com.bank.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferMessage implements Serializable {
    
    private Long transferId;          // Уникальный ID перевода
    private Long fromAccountId;       // ID счета списания
    private Long toAccountId;         // ID счета зачисления
    private BigDecimal amount;        // Сумма перевода
    
    // Для логирования
    @Override
    public String toString() {
        return "TransferMessage{" +
                "transferId=" + transferId +
                ", fromAccountId=" + fromAccountId +
                ", toAccountId=" + toAccountId +
                ", amount=" + amount +
                '}';
    }
}