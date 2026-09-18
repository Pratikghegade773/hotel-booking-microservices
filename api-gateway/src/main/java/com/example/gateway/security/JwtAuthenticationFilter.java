package com.example.gateway.security;

import com.example.common.constant.SecurityHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/api/auth/",
            "/api/ai/",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // 1. Check if public read endpoint
        boolean isPublicGet = HttpMethod.GET.equals(method) && (
                path.startsWith("/api/hotels") ||
                path.startsWith("/api/rooms") ||
                path.startsWith("/api/offers")
        );

        boolean isPublicAuth = PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // If public route and no auth header, proceed directly
        if ((isPublicAuth || isPublicGet) && (authHeader == null || !authHeader.startsWith("Bearer "))) {
            return chain.filter(exchange);
        }

        // 2. Protected route or authenticated public request: Validate Token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7).trim();
        if (!jwtUtils.validateToken(token)) {
            return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
        }

        String email = jwtUtils.getEmail(token);
        String role = jwtUtils.getRole(token);
        Long userId = jwtUtils.getId(token);
        Long hotelId = jwtUtils.getHotelId(token);

        // 3. Role-based routing validation
        if (path.startsWith("/api/owner") && !"OWNER".equals(role)) {
            return onError(exchange, "Access denied: OWNER role required", HttpStatus.FORBIDDEN);
        }

        if (path.startsWith("/api/manager") && !"MANAGER".equals(role) && !"OWNER".equals(role)) {
            return onError(exchange, "Access denied: MANAGER or OWNER role required", HttpStatus.FORBIDDEN);
        }

        // 4. Enrich downstream request with identity headers
        ServerHttpRequest.Builder builder = request.mutate();
        if (userId != null) {
            builder.header(SecurityHeaders.USER_ID, String.valueOf(userId));
        }
        if (email != null) {
            builder.header(SecurityHeaders.USER_EMAIL, email);
        }
        if (role != null) {
            builder.header(SecurityHeaders.USER_ROLE, role);
        }
        if (hotelId != null) {
            builder.header(SecurityHeaders.HOTEL_ID, String.valueOf(hotelId));
        }

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"error\": \"%s\", \"status\": %d}", err, status.value());
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // Execute before routing
    }
}
