package com.buka.mq;

import com.buka.model.CouponRecordMessage;
import com.buka.service.CouponRecordService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName CouponMQListener
 * @date 2025/3/8 16:11
 */

@Slf4j
@Component
@RabbitListener(queues = "${mqconfig.coupon_release_queue}")
public class CouponMQListener {

    @Autowired
    private CouponRecordService couponRecordService;


    /**
     * 处理RabbitMQ消息的方法，用于释放优惠券记录。
     * 该方法监听RabbitMQ队列中的消息，并根据消息内容执行相应的业务逻辑。
     *
     * @param recordMessage 包含优惠券记录信息的消息对象，用于业务处理。
     * @param message RabbitMQ原始消息对象，包含消息的元数据，如消息标签等。
     * @param channel RabbitMQ通道对象，用于确认消息的消费状态。
     * @throws Exception 如果在处理消息过程中发生异常，则抛出。
     */
    @RabbitHandler
    public void process(CouponRecordMessage recordMessage, Message message, Channel channel) throws Exception {
        // 记录接收到的消息内容
        log.info("监听到消息：releaseCouponRecord消息内容：{}", recordMessage);

        // 获取消息的唯一标识符（deliveryTag），用于后续确认消息消费状态
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        // 调用服务层方法，尝试释放优惠券记录
        boolean flag = couponRecordService.releaseCouponRecord(recordMessage);

        // 根据业务处理结果，确认消息的消费状态
        if (flag) {
            // 业务处理成功，确认消息消费成功
            channel.basicAck(deliveryTag, false);
        } else {
            // 业务处理失败，拒绝消息并重新入队
            channel.basicReject(deliveryTag, true);
        }
    }
}
