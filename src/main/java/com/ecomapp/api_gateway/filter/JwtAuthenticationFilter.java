package com.ecomapp.api_gateway.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.ecomapp.api_gateway.service.JwtService;

import reactor.core.publisher.Mono;

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

      // Skip auth for login/register routes
      if (path.contains("/auth/login") || path.contains("/auth/register") || path.contains("/health")) {
        return chain.filter(exchange);
      }

      String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
      }

      String token = authHeader.substring(7);
      try {
        jwtService.validateToken(token);
        String userId = jwtService.extractUserId(token);

        // Forward userId to downstream services
        var modifiedRequest = exchange.getRequest()
            .mutate()
            .header("X-User-Id", userId)
            .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());

      } catch (Exception e) {
        return onError(exchange, "Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
      }
    };
  }

  private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
    exchange.getResponse().setStatusCode(httpStatus);
    return exchange.getResponse().setComplete();
  }

  public static class Config {
  }
}
