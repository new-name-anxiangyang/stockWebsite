package com.stock.Service;

import com.stock.Entity.SysUser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService{
    private final PrivateKey privateKey;
    private final String issuer;
    private final long expireSeconds;
    public JwtService(
            @Value("${jwt.private-key-location}") Resource privateKeyResource,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.expire-seconds}") long expireSeconds) throws Exception {

        this.privateKey = loadPrivateKey(privateKeyResource);
        this.issuer = issuer;
        this.expireSeconds = expireSeconds;
    }

    public String createToken(SysUser user) {
        Date now = new Date();
        Date expireAt = new Date(
                now.getTime() + expireSeconds * 1000
        );

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("roles", user.getRoles())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    private PrivateKey loadPrivateKey(Resource resource) throws Exception {
        try (InputStream inputStream = resource.getInputStream()) {
            String content = new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );

            String keyContent = content
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyContent);

            PKCS8EncodedKeySpec keySpec =
                    new PKCS8EncodedKeySpec(keyBytes);

            KeyFactory keyFactory =
                    KeyFactory.getInstance("RSA");

            return keyFactory.generatePrivate(keySpec);
        }
    }
}
