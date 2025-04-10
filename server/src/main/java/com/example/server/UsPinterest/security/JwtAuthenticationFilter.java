package com.example.server.UsPinterest.security;

import com.example.server.UsPinterest.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            logger.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
            String jwt = parseJwt(request);
            if (jwt != null) {
                logger.debug("JWT token found in request: {}", request.getRequestURI());
                try {
                    if (jwtTokenUtil.validateJwtToken(jwt)) {
                        String username = jwtTokenUtil.getUsernameFromToken(jwt);
                        if (username != null) {
                            logger.debug("Username from token: {}", username);
                            try {
                                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                                if (userDetails != null) {
                                    // Убедимся, что userDetails является экземпляром UserPrincipal
                                    if (!(userDetails instanceof UserPrincipal)) {
                                        logger.warn("UserDetails is not an instance of UserPrincipal: {}", userDetails.getClass().getName());
                                    }

                                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());
                                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                                    SecurityContextHolder.getContext().setAuthentication(authentication);
                                    logger.info("Authenticated user: {}, URI: {}, token valid until: {}",
                                            username, request.getRequestURI(), new Date(jwtTokenUtil.getExpirationDateFromToken(jwt).getTime()));

                                    // Для POST /api/posts/with-image специально добавляем детали
                                    if (request.getRequestURI().equals("/api/posts/with-image") &&
                                            request.getMethod().equals("POST")) {
                                        logger.info("POST to /api/posts/with-image - User authenticated as: {}, Principal: {}, Authorities: {}",
                                                username,
                                                SecurityContextHolder.getContext().getAuthentication().getPrincipal().getClass().getName(),
                                                SecurityContextHolder.getContext().getAuthentication().getAuthorities());
                                    }
                                } else {
                                    logger.warn("UserDetails is null for username: {}", username);
                                }
                            } catch (Exception e) {
                                logger.error("Error loading user by username {}: {}", username, e.getMessage());
                            }
                        } else {
                            logger.warn("Username is null from token, URI: {}", request.getRequestURI());
                        }
                    } else {
                        logger.warn("JWT token validation failed for URI: {}", request.getRequestURI());
                    }
                } catch (ExpiredJwtException e) {
                    logger.warn("JWT токен просрочен: {}, URI: {}", e.getMessage(), request.getRequestURI());
                    logger.info("Пользователь с просроченным токеном: {}", e.getClaims().getSubject());
                    response.setHeader("X-Token-Expired", "true");
                } catch (SignatureException e) {
                    logger.error("Неверная подпись JWT: {}, URI: {}", e.getMessage(), request.getRequestURI());
                } catch (MalformedJwtException e) {
                    logger.error("Неверный формат токена: {}, URI: {}", e.getMessage(), request.getRequestURI());
                } catch (UnsupportedJwtException e) {
                    logger.error("Неподдерживаемый JWT токен: {}, URI: {}", e.getMessage(), request.getRequestURI());
                } catch (IllegalArgumentException e) {
                    logger.error("JWT строка пуста: {}, URI: {}", e.getMessage(), request.getRequestURI());
                } catch (JwtException e) {
                    logger.error("Ошибка проверки JWT: {}, URI: {}", e.getMessage(), request.getRequestURI());
                }
            } else {
                // Только для важных эндпоинтов логируем отсутствие токена
                if (request.getRequestURI().startsWith("/api/posts") ||
                        request.getRequestURI().startsWith("/api/auth") ||
                        request.getRequestURI().startsWith("/api/piner")) {
                    logger.debug("No JWT token found in request: {}", request.getRequestURI());

                    // Для multipart запросов с изображениями проверяем отдельно
                    if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
                        logger.info("Multipart form request detected without JWT token: {}", request.getRequestURI());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Не удалось установить аутентификацию пользователя: {}, URI: {}", e.getMessage(), request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (headerAuth == null && request.getContentType() != null &&
                request.getContentType().startsWith("multipart/form-data")) {
            // Debug для multipart запросов
            logger.debug("Checking Authorization header in multipart request to: {}", request.getRequestURI());
        }

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
} 