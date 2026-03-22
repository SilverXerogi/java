package com.bank.consumer.dto;

import java.math.BigDecimal;
import java.io.Serializable;

public class TransferMessage implements Serializable {
    
    private Long transferId;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    
    public TransferMessage() {}
    
    public TransferMessage(Long transferId, Long fromAccountId, Long toAccountId, BigDecimal amount) {
        this.transferId = transferId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }

    // Геттеры
    public Long getTransferId() { return transferId; }
    public Long getFromAccountId() { return fromAccountId; }
    public Long getToAccountId() { return toAccountId; }
    public BigDecimal getAmount() { return amount; }
    
    // Сеттеры
    public void setTransferId(Long transferId) { this.transferId = transferId; }
    public void setFromAccountId(Long fromAccountId) { this.fromAccountId = fromAccountId; }
    public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
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