package com.honggom.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    /*
    대원칙
    1. 모든 논리 트랜잭션이 커밋되어야 물리 트랜잭션이 커밋된다.
    2. 하나의 논리 트랜잭션이라도 롤백되면 물리 트랜잭션은 롤백된다.
     */

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager txManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 커밋 시작");
        txManager.commit(status);
        log.info("트랜잭션 커밋 완료");
    }

    @Test
    void rollback() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 롤백 시작");
        txManager.rollback(status);
        log.info("트랜잭션 롤백 완료");
    }

    @Test
    void doubleCommit() { // 두개가 분리된 트랜잭션
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋 시작");
        txManager.commit(tx1);

        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋 시작");
        txManager.commit(tx2);
    }

    @Test
    void doubleCommitRollback() {
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋 시작");
        txManager.commit(tx1);

        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 롤백");
        txManager.rollback(tx2);
    }

    @Test
    void innerCommit() { // 내부 트랜잭션이 외부 트랜잭션에 참여함, 내부 트랜잭션이 새로운 DB 커넥션을 획득하는 것이 아니라 외부 트랜잭션과 같은 DB 커넥션을 사용함
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction={}", inner.isNewTransaction());
        log.info("내부 트랜잭션 커밋 시작");
        txManager.commit(inner);

        log.info("외부 트랜잭션 커밋 시작");
        txManager.commit(outer);
    }

    @Test
    void outerRollback() { // 외부 트랜잭션 롤백시 내부 트랜잭션도 롤백됨
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 트랜잭션 커밋 시작");
        txManager.commit(inner);

        log.info("외부 트랜잭션 커밋 시작");
        txManager.rollback(outer);
    }

    @Test
    void innerRollback() { // 내부 트랜잭션 롤백시 내부에서 롤백 마크 표시를 하고 외부 트랜잭션 커밋시 롤백됨
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 트랜잭션 롤백 시작");
        txManager.rollback(inner);

        log.info("외부 트랜잭션 커밋 시작");
        Assertions.assertThatThrownBy(() -> txManager.commit(outer))
                .isInstanceOf(UnexpectedRollbackException.class);
    }

    @Test
    void innerRollbackRequiresNew() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction={}", outer.isNewTransaction()); // true

        log.info("내부 트랜잭션 시작");
        DefaultTransactionAttribute def = new DefaultTransactionAttribute();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus inner = txManager.getTransaction(def);
        log.info("inner.isNewTransaction={}", inner.isNewTransaction()); // true

        log.info("내부 트랜잭션 롤백 시작");
        txManager.rollback(inner);

        log.info("외부 트랜잭션 커밋 시작");
        txManager.commit(outer);
    }

}
