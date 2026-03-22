package com.bank.consumer.service;

import com.bank.consumer.dto.TransferMessage;
import com.bank.consumer.entity.Account;
import com.bank.consumer.entity.Transfer;
import com.bank.consumer.enums.Status;
import com.bank.consumer.repository.AccountRepository;
import com.bank.consumer.repository.TransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TransferProcessorService {

    private static final Logger log = LoggerFactory.getLogger(TransferProcessorService.class);
    
    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;

    public TransferProcessorService(AccountRepository accountRepository, TransferRepository transferRepository) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
    }

    /**
     * Обрабатывает одно сообщение о переводе.
     * @return true если успешно, false если ошибка
     */
    @Transactional
    public boolean processTransfer(TransferMessage message) {
        log.info("Начало обработки сообщения: {}", message.getTransferId());
        
        try {
            // 1. Валидация счетов
            Optional<Account> fromAccountOpt = accountRepository.findByIdWithLock(message.getFromAccountId());
            Optional<Account> toAccountOpt = accountRepository.findByIdWithLock(message.getToAccountId());
            
            if (fromAccountOpt.isEmpty() || toAccountOpt.isEmpty()) {
                log.error("Ошибка валидации: счета не найдены для перевода {}", message.getTransferId());
                createTransferRecord(message, Status.ERROR);
                return false;
            }
            
            Account fromAccount = fromAccountOpt.get();
            Account toAccount = toAccountOpt.get();
            
            // 2. Проверка достаточности баланса
            if (fromAccount.getBalance().compareTo(message.getAmount()) < 0) {
                log.error("Ошибка валидации: недостаточно средств на счете {} для перевода {}", 
                         message.getFromAccountId(), message.getTransferId());
                createTransferRecord(message, Status.ERROR);
                return false;
            }
            
            // 3. Обновление балансов (в транзакции)
            fromAccount.setBalance(fromAccount.getBalance().subtract(message.getAmount()));
            toAccount.setBalance(toAccount.getBalance().add(message.getAmount()));
            
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);
            
            // 4. Создание записи перевода со статусом DONE
            createTransferRecord(message, Status.DONE);
            
            log.info("Успешная обработка сообщения: {}", message.getTransferId());
            return true;
            
        } catch (Exception e) {
            log.error("Ошибка транзакции обработки сообщения {}: {}", 
                     message.getTransferId(), e.getMessage(), e);
            // При падении транзакции создаём запись со статусом ERROR
            try {
                createTransferRecord(message, Status.ERROR);
            } catch (Exception ex) {
                log.error("Не удалось создать запись об ошибке для перевода {}: {}", 
                         message.getTransferId(), ex.getMessage(), ex);
            }
            return false;
        }
    }
    
    /**
     * Создаёт запись о переводе в БД
     */
    private void createTransferRecord(TransferMessage message, Status status) {
        Transfer transfer = Transfer.builder()
                .fromAccountId(message.getFromAccountId())
                .toAccountId(message.getToAccountId())
                .amount(message.getAmount())
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
        transferRepository.save(transfer);
    }
}