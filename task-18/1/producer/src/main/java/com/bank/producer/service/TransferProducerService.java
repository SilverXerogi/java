package com.bank.producer.service;

import com.bank.producer.dto.TransferMessage;
import com.bank.producer.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferProducerService {

    private final KafkaTemplate<String, TransferMessage> kafkaTemplate;
    
    // Счётчик для генерации уникальных ID переводов
    private long transferIdCounter = 1;

    /**
     * Генерация 5 сообщений в секунду.
     * fixedDelay = 200мс гарантирует, что следующая генерация начнётся
     * только после завершения предыдущей (даже если она шла дольше 200мс).
     */
    @Scheduled(fixedDelay = 200)
    public void generateAndSendTransfers() {
        try {
            TransferMessage message = generateTransferMessage();
            sendMessageToKafka(message);
        } catch (Exception e) {
            log.error("Ошибка при генерации или отправке сообщения: {}", e.getMessage(), e);
        }
    }

    private TransferMessage generateTransferMessage() {
        // Получаем все ID счетов из кэша
        List<Long> accountIds = AccountInitializer.ACCOUNTS_CACHE.keySet().stream().toList();
        
        // Выбираем 2 разных случайных счета
        Long fromAccountId, toAccountId;
        do {
            fromAccountId = accountIds.get(ThreadLocalRandom.current().nextInt(accountIds.size()));
            toAccountId = accountIds.get(ThreadLocalRandom.current().nextInt(accountIds.size()));
        } while (fromAccountId.equals(toAccountId)); // Нельзя переводить самому себе
        
        // Генерируем случайную сумму от 1 до 500
        BigDecimal amount = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1, 501))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        
        return new TransferMessage(
                transferIdCounter++,
                fromAccountId,
                toAccountId,
                amount
        );
    }

    private void sendMessageToKafka(TransferMessage message) {
        log.info("Отправка сообщения в топик {}: {}", KafkaConfig.TOPIC_NAME, message);
        
        // Асинхронная отправка с обработкой результата
        CompletableFuture<SendResult<String, TransferMessage>> future = 
                kafkaTemplate.send(KafkaConfig.TOPIC_NAME, message);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Сообщение {} успешно отправлено в партицию {}, оффсет {}", 
                        message.getTransferId(), 
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Ошибка при отправке сообщения {}: {}", message.getTransferId(), ex.getMessage(), ex);
            }
        });
    }
}