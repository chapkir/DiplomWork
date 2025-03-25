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
            logger.info("Processing request: {} {}", request.getMethod(), request.getRequestURI());
            String jwt = parseJwt(request);
            if (jwt != null) {
                logger.info("JWT token found in request: {}", request.getRequestURI());
                try {
                    if (jwtTokenUtil.validateJwtToken(jwt)) {
                        String username = jwtTokenUtil.getUsernameFromToken(jwt);
                        if (username != null) {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            logger.info("Authenticated user: {}, token valid until: {}",
                                    username, new Date(jwtTokenUtil.getExpirationDateFromToken(jwt).getTime()));
                        } else {
                            logger.warn("Username is null from token");
                        }
                    }
                } catch (ExpiredJwtException e) {
                    logger.warn("JWT токен просрочен: {}", e.getMessage());
                    logger.info("Пользователь с просроченным токеном: {}", e.getClaims().getSubject());
                    response.setHeader("X-Token-Expired", "true");
                } catch (SignatureException e) {
                    logger.error("Неверная подпись JWT: {}", e.getMessage());
                } catch (MalformedJwtException e) {
                    logger.error("Неверный формат токена: {}", e.getMessage());
                } catch (UnsupportedJwtException e) {
                    logger.error("Неподдерживаемый JWT токен: {}", e.getMessage());
                } catch (IllegalArgumentException e) {
                    logger.error("JWT строка пуста: {}", e.getMessage());
                } catch (JwtException e) {
                    logger.error("Ошибка проверки JWT: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Не удалось установить аутентификацию пользователя: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
} 