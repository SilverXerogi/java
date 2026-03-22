package com.bank.producer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_NAME = "bank-transfers";

    @Bean
    public NewTopic bankTransfersTopic() {
        return TopicBuilder.name(TOPIC_NAME)
                .partitions(3)                // 3 партиции
                .replicas(3)                  // 3 реплики (на 3 брокера)
                .config("retention.ms", "300000") // 5 минут жизнь сообщения
                .config("min.insync.replicas", "2") // Подтверждение от 2 брокеров
                .build();
    }
}