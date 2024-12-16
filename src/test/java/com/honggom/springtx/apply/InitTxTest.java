package com.honggom.springtx.apply;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
public class InitTxTest {

    @Autowired Hello hello;

    @TestConfiguration
    static class TestConfig {
        @Bean
        Hello hello() {
            return new Hello();
        }
    }

    @Test
    void go() {
        // @PostConstruct 에서는 트랜잭션이 적용되지 않음
        // 초기화 코드는 스프링이 초기화 시점에 적용한다.
    }

    @Slf4j
    static class Hello {

        @PostConstruct
        @Transactional
        public void initV1() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("txActive1: {}", txActive); // 생성 시점에는 false
        }

        @EventListener(ApplicationReadyEvent.class)
        @Transactional
        public void initV2() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("txActive2: {}", txActive); // true
        }

    }
}
