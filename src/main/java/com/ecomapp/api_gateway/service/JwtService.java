package com.ecomapp.api_gateway.service;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    public static final String JWT_SECRET_KEY = "jwt_secret_key";

    public void validateToken(String token) {
      Jwts.parserBuilder()
          .setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()))
          .build()
          .parseClaimsJws(token);
    }

    public String extractUserId(final String token) {
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()))
          .build()
          .parseClaimsJws(token)
          .getBody();
      return claims.getSubject();
    }
}
