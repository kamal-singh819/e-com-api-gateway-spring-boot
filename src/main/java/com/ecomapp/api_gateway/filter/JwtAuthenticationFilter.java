package com.ecomapp.api_gateway.filter;

import org.springframework.stereotype.Component;

import com.ecomapp.api_gateway.exception.CustomException;
import com.ecomapp.api_gateway.service.JwtService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {
  @Autowired
  private JwtService jwtService;

  public JwtAuthenticationFilter() {
    super(Config.class);
  }

  /**
   * Apply the JWT authentication filter.
   * <p>
   * This filter checks for the presence and validity of a JWT token in the Authorization header.
   * If the token is valid, it extracts the user ID from the token and forwards it to downstream services
   * in the X-User-Id header.
   * <p>
   * If the token is invalid or missing, it returns a 401 Unauthorized response.
   * @param config the filter configuration
   * @return the gateway filter
   */
  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      String path = exchange.getRequest().getURI().getPath();

      // Skip auth for login and health routes
      if (path.contains("/auth/send-otp") || path.contains("/auth/verify-otp") || path.contains("/health")) {
        return chain.filter(exchange);
      }

      String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        throw new CustomException("Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
      }

      String token = authHeader.substring(7);
      try {
        Map<String,String> userInfo = jwtService.decodeJwt(token);

        // Forward userId to downstream services
        var modifiedRequest = exchange.getRequest()
            .mutate()
            .header("X-User-Id", userInfo.get("userId"))
            .header("X-User-Role", userInfo.get("role"))
            .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());

      } catch (Exception e) {
        throw new CustomException("Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
      }
    };
  }

  public static class Config {
  }
}
