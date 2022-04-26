package com.spring.exercise.security;

import com.spring.exercise.service.UserServiceImpl;
import com.spring.exercise.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
public class JwtFilterRequest extends OncePerRequestFilter {

    private static final int BEARER_SUBSTRING_LENGTH = 7;

    private final JwtUtils jwtUtils;

    private final UserServiceImpl userServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Optional<String> jwtToken = parseJwt(request);

        if (jwtToken.isPresent()) {
            String userName = jwtUtils.extractUsername(jwtToken.get());
            UserDetails userDetails = userServiceImpl.loadUserByUsername(userName);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                    Collections.emptyList());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return Optional.of(headerAuth.substring(BEARER_SUBSTRING_LENGTH));
        }

        return Optional.ofNullable(headerAuth);
    }

}
