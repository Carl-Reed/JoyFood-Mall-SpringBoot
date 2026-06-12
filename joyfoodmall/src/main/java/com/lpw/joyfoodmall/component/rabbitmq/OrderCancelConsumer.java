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
    @RabbitListener(queues = "order.cancel.queue", concurrency = "1")
    public void handleOrderTimeout(Long orderId) {
        log.info("[x] 收到超时未支付订单消息, 准备执行自动取消, 订单ID: {}", orderId);

        try {
            boolean success = orderService.cancelOrderInternal(orderId);

            if (success) {
                log.info("[√] 订单 {} 已成功自动取消并释放资源", orderId);
            } else {
                log.warn("[-] 订单 {} 状态已变更(可能已支付)，跳过自动取消", orderId);
            }

        } catch (Exception e) {
            log.error("[!] 订单 {} 自动取消执行异常，将触发重试机制: {}", orderId, e.getMessage(), e);

            throw new RuntimeException("订单自动取消失败，等待重试", e);
        }
    }
}
