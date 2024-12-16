package com.honggom.springtx.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void order(Order order) throws NotEnoughMoneyException {
        log.info("order 호출");
        orderRepository.save(order);

        log.info("결제 프로세스 진입");

        if (order.getName().equals("예외")) { // 시스템 예외: 복구 불가능한 예외
            log.info("시스템 예외 발생");
            throw new RuntimeException("시스템 예외");
        } else if (order.getName().equals("잔고부족")) { // 비즈니스 예외: 복구 가능한 예외
            log.info("잔고 부족 비즈니스 예외 발생");
            order.setPayStatus("대기");
            throw new NotEnoughMoneyException("잔고 부족");
        } else {
            log.info("결제 완료");
            order.setPayStatus("완료");
        }
        log.info("결제 프로세스 종료");
    }
}
