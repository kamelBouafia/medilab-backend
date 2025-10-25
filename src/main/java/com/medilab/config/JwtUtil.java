//package com.medilab.config;
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.security.Key;
//import java.util.Date;
//import java.util.Map;
//
//@Component
//public class JwtUtil {
//
//    private final Key key;
//
//    public JwtUtil(@Value("${app.jwt-secret}") String secret) {
//        this.key = Keys.hmacShaKeyFor(secret.getBytes());
//    }
//
//    public String generateToken(Map<String, Object> claims, long ttlMs) {
//        return Jwts.builder()
//                .setClaims(claims)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + ttlMs))
//                .signWith(key)
//                .compact();
//    }
//
//    public Jws<Claims> parseToken(String token) throws JwtException {
//        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
//    }
//}
