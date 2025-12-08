package org.example.lotterysystem;

import org.example.lotterysystem.common.Util.MailUtil;
import org.example.lotterysystem.service.VerificationCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class VerificationCodeServiceTest {

    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private MailUtil mailUtil;

    @Test
    void tesSend(){
        verificationCodeService.sendVerificationCode("yuhzhong67@gmail.com");
    }

    @Test
    void sendSampleMail() {
        mailUtil.sendSampleMail("178@163.com","标题","正文");
    }
}
