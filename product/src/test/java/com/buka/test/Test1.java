package com.buka.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @className: Test1
 * @author: LZX
 * @date: 2025/2/20 16:27
 * @Version: 1.0
 * @description:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class Test1 {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void test1() {
     rabbitTemplate.convertAndSend("stock.event.exchange","stock.release.delay.routing.key","hello");
    }
}


