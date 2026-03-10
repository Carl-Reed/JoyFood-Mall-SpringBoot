package com.lpw.joyfoodmall.component;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordGenerator {
    private final PasswordEncoder passwordEncoder;

    // 生成随机密码并返回明文和加密后的形式
    public PasswordGenerationResult generatePassword(int length) {
        String plainPassword = generateRandomPassword(length);
        String encodedPassword = passwordEncoder.encode(plainPassword);

        return new PasswordGenerationResult(plainPassword, encodedPassword);
    }

    // 生成随机密码的辅助方法（使用安全随机数）
    private String generateRandomPassword(int length) {
        // 字符集定义
        String chars = "abcdefghijklmnopqrstuvwxyz" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                "0123456789" +
                "!@#$%^&*()-_=+[]{}|;:'\",.<>/?";

        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    // 内部类：封装生成结果
    public static class PasswordGenerationResult {
        private final String plainPassword;
        private final String encodedPassword;

        public PasswordGenerationResult(String plainPassword, String encodedPassword) {
            this.plainPassword = plainPassword;
            this.encodedPassword = encodedPassword;
        }

        // getter方法
        public String getPlainPassword() { return plainPassword; }
        public String getEncodedPassword() { return encodedPassword; }
    }
}