package com.example.server.UsPinterest.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpirationMs;

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtExpirationMs))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8))).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            System.err.println("Неверная JWT подпись: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("Неверная JWT структура: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("JWT токен просрочен: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("Неподдерживаемый JWT: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("JWT claims строка пуста: " + e.getMessage());
        }
        return false;
    }
} 