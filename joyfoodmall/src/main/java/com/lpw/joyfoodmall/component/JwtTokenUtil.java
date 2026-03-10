package com.lpw.joyfoodmall.component;

import com.lpw.joyfoodmall.entity.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secretKey}")
    private String secretKey;
    private static final long EXPIRATION = 86400000; // 24小时

    private SecretKey getSigningKey() {
        // 将配置的字符串密钥转换为HS512所需的SecretKey
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(SecurityUser  securityUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", securityUser.getId()); // 用户ID
        // 提取角色名
        claims.put("roles", securityUser.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList()));
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(securityUser.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey(),SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }

    String getUsernameFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            System.out.println("Token解析失败: "+ e.getMessage());
            throw new RuntimeException("无效的Token", e);
        }
    }

    public Integer getUserIdFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("userId", Integer.class);
        } catch (Exception e) {
            throw new RuntimeException("解析用户ID失败", e);
        }
    }
}
