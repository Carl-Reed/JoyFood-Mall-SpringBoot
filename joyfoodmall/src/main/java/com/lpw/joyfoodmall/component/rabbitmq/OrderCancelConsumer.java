package com.lpw.joyfoodmall.component.rabbitmq;

import com.lpw.joyfoodmall.service.OmsOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancelConsumer {

    private final OmsOrderService orderService;

    /** 监听订单超时取消队列 */
    @RabbitListener(queues = "order.cancel.queue")
    public void handleOrderTimeout(Long orderId) {
        log.info("[x] 收到超时未支付订单消息, 准备执行自动取消, 订单ID: {}", orderId);

        try {
            orderService.cancelOrderInternal(orderId);
        } catch (Exception e) {
            log.error("[!] 订单 {} 自动取消执行异常: {}", orderId, e.getMessage(), e);
        }
    }
}
