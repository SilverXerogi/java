package com.bank.producer.service;

import com.bank.producer.entity.Account;
import com.bank.producer.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    
    // Публичный Map для быстрого доступа при генерации сообщений
    // ConcurrentHashMap безопасен для многопоточного чтения
    public static final ConcurrentHashMap<Long, Account> ACCOUNTS_CACHE = new ConcurrentHashMap<>();

    @Override
    public void run(String... args) {
        long count = accountRepository.count();
        
        if (count == 0) {
            log.info("Таблица счетов пуста. Генерируем 1000 новых счетов...");
            generateAndSaveAccounts();
        } else {
            log.info("Таблица счетов содержит {} записей. Загружаем в кэш...", count);
            loadAccountsToCache();
        }
        log.info("Инициализация счетов завершена. В кэше: {} счетов", ACCOUNTS_CACHE.size());
    }

    private void generateAndSaveAccounts() {
        for (int i = 1; i <= 1000; i++) {
            // Генерируем случайный баланс от 1000 до 100000
            BigDecimal balance = BigDecimal.valueOf(Math.random() * 99000 + 1000)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            
            Account account = new Account(balance);
            Account saved = accountRepository.save(account);
            ACCOUNTS_CACHE.put(saved.getId(), saved);
        }
        log.info("Сгенерировано и сохранено 1000 счетов");
    }

    private void loadAccountsToCache() {
        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            ACCOUNTS_CACHE.put(account.getId(), account);
        }
    }

    // Метод для обновления баланса в кэше после успешной транзакции
    public void updateAccountInCache(Account updatedAccount) {
        ACCOUNTS_CACHE.put(updatedAccount.getId(), updatedAccount);
    }
}