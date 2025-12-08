package org.example.lotterysystem.service;

public interface VerificationCodeService {
    //发送验证码
    void sendVerificationCode(String email);
    //从缓存中获取验证码
    String getVerificationCode(String email);
}
