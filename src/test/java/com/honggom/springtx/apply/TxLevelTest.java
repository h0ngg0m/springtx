package com.honggom.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
public class TxLevelTest {

    @Autowired
    LevelService levelService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        LevelService levelService() {
            return new LevelService();
        }
    }

    @Test
    void tx() {
        levelService.write();
        levelService.read();
    }

    @Slf4j
    @Transactional(readOnly = true)
    static class LevelService {

        @Transactional(readOnly = false)
        public void write() {
            log.info("call write");
            printTxInfo();
        }

        public void read() {
            log.info("call read");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("txActive: {}", txActive);
            boolean readonly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            log.info("readonly: {}", readonly);
        }
    }
}
