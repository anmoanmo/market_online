package com.market.online.mq;

import com.market.online.model.OrderMessage;
import com.market.online.service.ProductOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @className: ProductOrderMQListener
 * @author: LZX
 * @date: 2025/3/15 18:53
 * @Version: 1.0
 * @description:
 */
@Component
@Slf4j
@RabbitListener(queues = "${mqconfig.order_close_queue}")
public class ProductOrderMQListener {
    @Autowired
    private ProductOrderService productOrderService;
    @RabbitHandler
    public void setProductOrderService(OrderMessage orderMessage, Message message, Channel channel) throws IOException {
        log.info("订单微服务收到信息:{}", orderMessage);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        boolean flag=  productOrderService.closeProductOrder(orderMessage.getOutTradeNo());
        if (flag) {
            channel.basicAck(deliveryTag, false);
        } else {
            channel.basicAck(deliveryTag,true);
        }

    }

}


