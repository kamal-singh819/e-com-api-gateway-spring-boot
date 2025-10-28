package com.ecomapp.api_gateway.service;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ecomapp.api_gateway.exception.CustomException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private final Key JWT_SECRET_KEY;

    public JwtService(@Value("${jwt.secret}") String JWT_SECRET_KEY) {
        this.JWT_SECRET_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET_KEY));
    }

    public Map<String, String> decodeJwt(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                        .setSigningKey(JWT_SECRET_KEY)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
            Map<String, String> userInfo = new HashMap<>();
            String userId = claims.getSubject();
            userInfo.put("userId", userId);
            userInfo.put("role", claims.get("role", String.class));
            return userInfo;
        } catch (ExpiredJwtException e) {
            throw new CustomException("JWT Token has expired", HttpStatus.UNAUTHORIZED);
        } catch (UnsupportedJwtException e) {
            throw new CustomException("Unsupported JWT token format", HttpStatus.UNAUTHORIZED);
        } catch (MalformedJwtException e) {
            throw new CustomException("Invalid JWT Token", HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            throw new CustomException("JWT token is empty or null", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            throw new CustomException("An unexpected JWT error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
