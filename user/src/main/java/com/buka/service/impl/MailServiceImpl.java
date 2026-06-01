package com.buka.service.impl;

import com.buka.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * @className: MailServiceImpl
 * @author: LZX
 * @date: 2025/2/10 14:31
 * @Version: 1.0
 * @description:
 */
@Service
@Slf4j
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.from}")
    private String from;
    /**
     * @description:发送邮件
     * @author: LZX
     * @date: 2025/2/13 12:47
     * @param: [to, subject, text]
     * @return: void
     **/
    @Override
    public void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
        log.info("邮件发送成功:{}",message.toString());
    }
}


