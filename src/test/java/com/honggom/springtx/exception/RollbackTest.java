package com.honggom.springtx.exception;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
public class RollbackTest {

    @Autowired
    RollbackService rollbackService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        RollbackService rollbackService() {
            return new RollbackService();
        }
    }

    @Test
    void runtimeException() { // 롤백
        assertThatThrownBy(() -> rollbackService.runtimeException())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void checkedException() { // 커밋
        assertThatThrownBy(() -> rollbackService.checkedException())
                .isInstanceOf(MyException.class);
    }

    @Test
    void checkedExceptionWithRollback() { // 롤백
        assertThatThrownBy(() -> rollbackService.checkedExceptionWithRollback())
                .isInstanceOf(MyException.class);
    }

    @Slf4j
    static class RollbackService {

        // 런타임 예외 발생: 롤백
        @Transactional
        public void runtimeException() {
            log.info("call runtimeException");
            throw new RuntimeException();
        }

        // 체크 예외 발생: 커밋
        @Transactional
        public void checkedException() throws MyException {
            log.info("call checkedException");
            throw new MyException();
        }

        // 롤백이 필요한 경우
        @Transactional(rollbackFor = MyException.class)
        public void checkedExceptionWithRollback() throws MyException {
            log.info("call checkedExceptionWithRollback");
            throw new MyException();
        }
    }

    static class MyException extends Exception {
    }
}
