package com.zbinyds.reggie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * @author zbinyds
 * @time 2022/08/30 14:53
 */
@SpringBootTest
public class SendEmailTests {
    @Autowired(required = false)
    private JavaMailSender sender;

    @Test
    public void test1(){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("【聚香楼】验证消息");
        message.setText("你正在进行登录操作，验证码：336956，切勿将验证码泄露给他人，本条验证码有效期2分钟。");
        message.setTo("2298157772@qq.com");
        message.setFrom("zbinyds@126.com");
        sender.send(message);
    }
}
