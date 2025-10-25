package dev.crt.api.gateway.jwt;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtFilter implements GlobalFilter, Ordered {
    private final JwtUtils jwtUtils;
    private static final String AUTH_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String X_CREATOR_ID_HEADER = "X-Creator-ID";

    private record ExcludedPath(HttpMethod method, String path) {
        public boolean matches(HttpMethod requestMethod, String requestPath) {
            return (method == null || method.equals(requestMethod)) && requestPath.startsWith(path);
        }
    }

    private static final List<ExcludedPath> EXCLUDED_PATHS = List.of(
            new ExcludedPath(HttpMethod.POST, "/api/v1/users/login"),
            new ExcludedPath(HttpMethod.POST, "/api/v1/users/register"),

            new ExcludedPath(HttpMethod.GET, "/api/v1/users/avatars"),
            new ExcludedPath(HttpMethod.GET, "/api/v1/catalog/images")
    );

    public JwtFilter(JwtUtils jwtUtils){
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        if(EXCLUDED_PATHS.stream().anyMatch(p -> p.matches(method, path))){
            return chain.filter(exchange);
        }

        String token = getTokenFromRequest(request);

        if (token == null) {
            token = request.getQueryParams().getFirst("token");
        }

        if(token == null || !jwtUtils.validateToken(token)){
            return onError(exchange, "Authentication required or invalid token");
        }

        String creatorId = jwtUtils.extractCreatorId(token);

        if(creatorId == null){
            return onError(exchange, "Invalid token claims: Missing user ID");
        }

        ServerHttpRequest modifiedRequest = request.mutate()
                .header(X_CREATOR_ID_HEADER, creatorId)
                .headers(headers -> headers.remove(AUTH_HEADER))
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private String getTokenFromRequest(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errorMessage) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        String jsonError = "{\"error\": \"" + HttpStatus.UNAUTHORIZED.getReasonPhrase() + "\", \"message\": \"" + errorMessage + "\"}";

        return response.writeWith(Mono.just(response.bufferFactory().wrap(jsonError.getBytes())));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
