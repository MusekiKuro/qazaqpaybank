package kz.qazaqpay.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j  // <-- ДОБАВИЛ
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        
        log.info("=== JWT Filter ===");
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Auth Header: {}", authHeader);

        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No Bearer token found");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        log.info("JWT extracted: {}...", jwt.substring(0, Math.min(20, jwt.length())));
        
        try {
            userEmail = jwtUtil.extractUsername(jwt);
            log.info("Username from JWT: {}", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                log.info("User loaded: {}", userDetails.getUsername());

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    log.info("Token is valid!");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Authentication set in SecurityContext");
                } else {
                    log.error("Token validation FAILED!");
                }
            }
        } catch (Exception e) {
            log.error("Error in JWT filter: {}", e.getMessage(), e);
        }
        
        filterChain.doFilter(request, response);
    }
}
