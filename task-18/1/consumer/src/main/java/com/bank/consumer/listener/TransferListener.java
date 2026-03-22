package com.bank.consumer.listener;

import com.bank.consumer.dto.TransferMessage;
import com.bank.consumer.service.TransferProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TransferListener {

    private static final Logger log = LoggerFactory.getLogger(TransferListener.class);
    
    private final TransferProcessorService processorService;

    public TransferListener(TransferProcessorService processorService) {
        this.processorService = processorService;
    }

    /**
     * Слушает топик bank-transfers и обрабатывает сообщения пачками.
     * batch = "true" обеспечивает чтение нескольких сообщений за один вызов.
     */
    @KafkaListener(
            topics = "bank-transfers",
            groupId = "${spring.kafka.consumer.group-id:bank-consumers}",
            batch = "true"
    )
    public void listenTransfers(List<TransferMessage> messages, Acknowledgment ack) {
        log.info("Получена пачка из {} сообщений", messages.size());
        
        int successCount = 0;
        int errorCount = 0;
        
        for (TransferMessage message : messages) {
            try {
                boolean result = processorService.processTransfer(message);
                if (result) {
                    successCount++;
                } else {
                    errorCount++;
                }
            } catch (Exception e) {
                log.error("Критическая ошибка при обработке сообщения {}: {}", 
                         message.getTransferId(), e.getMessage(), e);
                errorCount++;
            }
        }
        
        log.info("Обработка пачки завершена. Успешно: {}, Ошибок: {}", successCount, errorCount);
        
        // Подтверждаем обработку всей пачки (коммит оффсета)
        ack.acknowledge();
    }
}