package com.zheng.aicommunitybackend;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;

@SpringBootTest
class AiCommunityBackendApplicationTests {

    @Test
    void contextLoads() {
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        System.out.println(secretKey.getAlgorithm());
        System.out.println(secretKey.getEncoded());
        System.out.println(secretKey.getFormat());
    }

}
