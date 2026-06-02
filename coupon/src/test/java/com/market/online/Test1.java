package com.market.online;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author zhangyadong
 * @version 1.0
 * @ClassName Test1
 * @date 2025/3/8 15:38
 */

@SpringBootTest(classes = CouponServiceApplication.class)
@RunWith(SpringRunner.class)
public class Test1 {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void test() {
        rabbitTemplate.convertAndSend("coupon.event.exchange","coupon.release.delay.routing.key","5qeqweqw");    }
}
