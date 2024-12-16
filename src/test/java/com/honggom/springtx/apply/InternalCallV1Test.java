package com.honggom.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    @Autowired CallService callService;

    @Test
    void printProxy() {
        log.info("aop class={}", callService.getClass());
    }

    @Test
    void callInternal() {
        callService.internal();
    }

    @Test
    void callExternal() {
        callService.external();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        CallService callService() {
            return new CallService();
        }
    }

    @Slf4j
    static class CallService {

        public void external() {
            log.info("call external");
            printTxInfo();
            internal(); // 이 호출은 트랜잭션 적용이 안됨!!
        }

        @Transactional
        public void internal() {
            log.info("call internal");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("txActive: {}", txActive);
        }

    }
}
