package org.example.lotterysystem;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Key;

@SpringBootTest
public class JWTTest {

    /**⽣成密钥*/
    @Test
    public void genKey(){
        // 创建了⼀个密钥对象，使⽤HS256签名算法。
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        // 将密钥编码为Base64字符串。

        String secretString = Encoders.BASE64.encode(key.getEncoded());
        System.out.println(secretString);
        // 结果：LkKvYSQwz5TiSrp2TJZhr589aTDl9bNwFf7/yuvi0YA=
    }

}
