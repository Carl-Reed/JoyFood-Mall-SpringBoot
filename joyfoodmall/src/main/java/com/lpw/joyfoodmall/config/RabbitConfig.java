package com.lpw.joyfoodmall.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    // 定义支持延迟的交换机
    @Bean
    public CustomExchange orderDelayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange("order.delay.exchange", "x-delayed-message", true, false, args);
    }

    // 定义接收超时消息的取消队列
    @Bean
    public Queue orderCancelQueue() {
        // durable=true 保证服务重启后队列还在
        return QueueBuilder.durable("order.cancel.queue").build();
    }

    // 将队列绑定到延迟交换机
    @Bean
    public Binding orderCancelBinding(Queue orderCancelQueue, CustomExchange orderDelayExchange) {
        return BindingBuilder.bind(orderCancelQueue).to(orderDelayExchange).with("order.cancel.routing.key").noargs();
    }
}
