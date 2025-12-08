package org.example.lotterysystem.service.impl;

import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.example.lotterysystem.common.Util.CaptchaUtil;
import org.example.lotterysystem.common.Util.RedisUtil;
import org.example.lotterysystem.common.Util.RegexUtil;
import org.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import org.example.lotterysystem.common.exception.ServiceException;
import org.example.lotterysystem.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    //对于redis里面的key需要标准化：为了区分业务，应该给key定义前缀
    //VerificationCode_123@xx.com:123456   User_123@xx.com:userInfo

    private static final String VERIFICATION_CODE_PREFIX ="VERIFICATION_CODE_";
    private static final Long VERIFICATION_CODE_TIMEOUT = 120L;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    JavaMailSender mailSender;

    @Override
    public void sendVerificationCode(String email) {
        //校验email
        if(!RegexUtil.checkMail(email)) {
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_ERROR);
        }
        //生成随机验证码
        String code = CaptchaUtil.getCaptcha(6);

        //发送验证码
        sendCode(code, email);

        //缓存验证码
        redisUtil.set(VERIFICATION_CODE_PREFIX +email, code, VERIFICATION_CODE_TIMEOUT);
    }


    @Override
    public String getVerificationCode(String email) {
        if(!RegexUtil.checkMail(email)) {
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_ERROR);
        }

        return redisUtil.get(VERIFICATION_CODE_PREFIX + email);
    }

    private void sendCode(String code, String email) {
        // 构建邮件内容（MimeMessagePreparator 用于复杂邮件构建）
        MimeMessagePreparator preparator = mimeMessage -> {
            // 1. 设置收件人（TO=直接接收；CC=抄送；BCC=密送）
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(email));

            // 2. 设置发件人（必须与配置中的 spring.mail.username 完全一致！）
            mimeMessage.setFrom(new InternetAddress("3299308759@qq.com"));

            // 3. 设置邮件主题（支持中文，UTF-8 编码已配置）
            mimeMessage.setSubject("【验证码通知】你的验证码已生成", "UTF-8");

            // 4. 设置邮件正文（纯文本格式，也可改为 HTML 格式）
            String content = String.format("Dear 用户：\n\n你的验证码为：%s\n\n验证码有效期 2 分钟，请及时使用！" +
                                    "如非本人操作，请忽略本邮件。为了确保您的帐号安全，请不要将此邮件转发给任何人", code);
            mimeMessage.setText(content, "UTF-8");  // 第二个参数指定编码，避免乱码
        };

        try {
            mailSender.send(preparator);
        } catch (MailException ex) {
            // simply log it and go on...
            System.err.println(ex.getMessage());
        }
    }
}
