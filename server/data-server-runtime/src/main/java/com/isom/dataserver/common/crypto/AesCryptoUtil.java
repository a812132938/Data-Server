package com.isom.dataserver.common.crypto;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.symmetric.AES;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Arrays;

@Component
public class AesCryptoUtil {

    @Value("${encrypt.key}")
    private String encryptKey;

    private AES aes;

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
        byte[] keyBytes = Arrays.copyOf(encryptKey.getBytes(StandardCharsets.UTF_8), 32);
        byte[] ivBytes = Arrays.copyOf(keyBytes, 16);
        this.aes = new AES("CBC", "PKCS7Padding", keyBytes, ivBytes);
    }

    public String encrypt(String plainText) {
        byte[] encrypted = aes.encrypt(plainText.getBytes(StandardCharsets.UTF_8));
        return "v1:" + Base64.encode(encrypted);
    }

    public String decrypt(String cipherText) {
        if (cipherText == null || !cipherText.contains(":")) {
            throw new IllegalArgumentException("Invalid cipher text format");
        }
        String[] parts = cipherText.split(":", 2);
        byte[] decoded = Base64.decode(parts[1]);
        return new String(aes.decrypt(decoded), StandardCharsets.UTF_8);
    }
}
