package com.medilab.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private Long expiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // Try to decode the configured secret as Base64 first; if that fails, use raw
        // UTF-8 bytes.
        // If the resulting key material is shorter than 32 bytes (256 bits), derive a
        // 256-bit key via SHA-256.
        try {
            byte[] keyBytes;
            try {
                keyBytes = Decoders.BASE64.decode(secret);
            } catch (IllegalArgumentException ex) {
                keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            }

            if (keyBytes.length < 32) {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                keyBytes = digest.digest(keyBytes);
            }

            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            // fallback: use raw secret bytes (not recommended)
            this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String generateToken(AuthenticatedUser authenticatedUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", authenticatedUser.getId());
        claims.put("labId", authenticatedUser.getLabId());
        claims.put("username", authenticatedUser.getUsername()); // Added username as a custom claim
        claims.put("authorities", authenticatedUser.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        claims.put("type", authenticatedUser.getUserType());
        claims.put("forcePasswordChange", authenticatedUser.isForcePasswordChange());
        claims.put("gdprAccepted", authenticatedUser.isGdprAccepted());

        return createToken(claims, authenticatedUser.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(secretKey, SignatureAlgorithm.HS256).compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
