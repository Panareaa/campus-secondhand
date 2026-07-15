package cn.ynu.campus.relife.user.security;

import cn.ynu.campus.relife.user.config.JwtProperties;
import cn.ynu.campus.relife.user.domain.Account;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Account account) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtProperties.getExpireSeconds());
        String role = account.getRole() != null && account.getRole() == 1 ? "ADMIN" : "USER";
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(account.getId()))
                .claim("loginName", account.getLoginName())
                .claim("campusId", account.getCampusId())
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey)
                .compact();
    }

    public long getExpireSeconds() {
        return jwtProperties.getExpireSeconds();
    }
}
