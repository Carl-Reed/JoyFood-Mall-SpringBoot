package com.lpw.joyfoodmall.component.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderProducer {

    private final RabbitTemplate rabbitTemplate;

    public static final String ORDER_DELAY_EXCHANGE = "order.delay.exchange";
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel.routing.key";

    /** 发送订单超时取消的延迟消息 */
    public void sendOrderCancelDelayMsg(Long orderId) {

        rabbitTemplate.convertAndSend(ORDER_DELAY_EXCHANGE, ORDER_CANCEL_ROUTING_KEY, orderId, message -> {
            // 设置消息延迟时间为30分钟
            message.getMessageProperties().setDelayLong(30 * 20 * 1000L);
            return message;
        });

    }

}
