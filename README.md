# E-Commerce API Gateway

This is the API Gateway service for the E-Commerce microservices application. It serves as the single entry point for all client-side requests, handling routing, authentication, and request/response transformation.

## Architecture Overview

The API Gateway is built using Spring Cloud Gateway and serves as the central routing and security layer for the following microservices:

1. User Service (Spring Boot) - Port 8081
   - Repo Link: https://github.com/kamal-singh819/e-com-user-service-spring-boot
   - Handles user authentication and management
   - Endpoints: `/api/users/**`

2. Inventory Service (Spring Boot) - Port 8082
   - Repo Link: https://github.com/kamal-singh819/e-com-inventory-service-spring-boot
   - Manages product inventory and catalog
   - Endpoints: `/api/inventory/**`

3. Order Service (Node.js/Express) - Port 8083
   - Repo Link: https://github.com/kamal-singh819/e-com-order-service-node
   - Handles order processing and management
   - Endpoints: `/api/orders/**`

## Features

### 1. Centralized Authentication
- JWT-based authentication
- Token validation and user information extraction
- Automatic user context propagation to microservices
- Specific endpoints (`/auth/send-otp`, `/auth/verify-otp`) are exempt from authentication

### 2. Request Routing
- Dynamic routing based on service endpoints
- Path-based routing with prefix stripping
- Load balancing ready

### 3. Security Features
- JWT token validation
- Role-based access control support
- Header transformation and enrichment
- Secure user context propagation

### 4. File Upload Support
- Configured for large file uploads
- Maximum file size: 50MB
- Maximum request size: 50MB

### 5. Monitoring and Health Checks
- Exposed health endpoints
- Detailed health information available
- Metrics and monitoring support

## Technical Configuration

### Port Configuration
```yaml
server:
  port: 4000
```

### Service Routes
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://user-service:8081
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - JwtAuthenticationFilter

        - id: inventory-service
          uri: http://inventory-service:8082
          predicates:
            - Path=/api/inventory/**

        - id: order-service
          uri: http://order-service:8083
          predicates:
            - Path=/api/orders/**
```

### File Upload Limits
```yaml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 50MB
      max-request-size: 50MB
```

## Security Implementation

### JWT Authentication
- Stateless authentication using JWT tokens
- Token validation and parsing
- User role and ID extraction
- Automatic header enrichment for downstream services

### Headers Forwarded to Microservices
- `X-User-Id`: Extracted user ID from JWT
- `X-User-Role`: User role information

## Docker Support

### Development Setup
```yaml
services:
  api-gateway:
    build:
      context: .
      dockerfile: Dockerfile.dev
    container_name: api-gateway
    ports:
      - "4000:4000"
    networks:
      - e_com_network
```

### Network Configuration
- Uses external network `e_com_network`
- Enables communication between microservices
- Container-to-container networking

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven
- Docker and Docker Compose
- Network `e_com_network` should be created

### Running Locally
1. Clone the repository
2. Build the application:
   ```bash
   ./mvnw clean install
   ```
3. Run using Docker Compose:
   ```bash
   docker-compose -f docker-compose.dev.yml up --build
   ```

### Environment Variables
- `JWT_SECRET`: Secret key for JWT token validation
- Additional environment variables can be configured in application.yml

## API Documentation

### Authentication Endpoints
- POST `/api/users/auth/send-otp` - Send OTP for authentication
- POST `/api/users/auth/verify-otp` - Verify OTP and get JWT token

### Protected Routes
All other routes require a valid JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Monitoring and Health

Health and monitoring endpoints are available at:
- `/actuator/health` - Health status
- `/actuator/info` - Application information

## Error Handling

The gateway implements centralized error handling for:
- Invalid/Expired JWT tokens
- Missing authentication
- Service unavailability
- Invalid requests

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

This project is licensed under the MIT License.
