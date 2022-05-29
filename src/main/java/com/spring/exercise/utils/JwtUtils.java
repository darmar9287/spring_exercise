package com.spring.exercise.utils;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

@Component
public class JwtUtils {

    private String secretKey;
    public String sub;
    public String jti;
    private static final int BEARER_SUBSTRING_LENGTH = 7;


    public JwtUtils(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String generateToken(Authentication authentication, String userId) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date until = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10);
        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setId(userId)
                .setIssuedAt(new Date())
                .setExpiration(until)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public Optional<String> parseJwt(String token) {
        Optional<String> parsedJwt = Optional.empty();
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            parsedJwt = Optional.of(token.substring(BEARER_SUBSTRING_LENGTH));
        }
        return parsedJwt;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractId(String token) {
        return extractClaim(token, Claims::getId);
    }

    public String fetchUserIdFromToken(String token) {
        String parsedJwt = parseJwt(token).get();
        return extractId(parsedJwt);
    }

    public String extractIat(String token) {
        return String.valueOf(extractClaim(token, Claims::getIssuedAt));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
