package com.metaorta.kaspi.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.metaorta.kaspi.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final UserService userService;

    @Autowired
    public JWTFilter(JWTUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) {
        String authHeaders = request.getHeader("Authorization");

        if (authHeaders != null && !authHeaders.isBlank() && authHeaders.startsWith("Bearer ")) {
            String jwt = authHeaders.substring(7);

            if (jwt.isBlank()) {
                try {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Authentication Failed! Invalid JWT Token!");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    String username = jwtUtil.validateTokenAndRetrieveData(jwt);
                    UserDetails userDetails = userService.findByUsername(username);

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());

                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                } catch (JWTVerificationException e) {
                    try {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Authentication Failed! Invalid JWT Token!");
                        return;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
