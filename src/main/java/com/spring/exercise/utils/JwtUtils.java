package com.spring.exercise.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {

    private String secretKey;

    public JwtUtils(@Value ("${jwt.secret}")String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean validateToken(String token, String username) {
        String userMail = extractUsername(username);
        return userMail.equals(username) && !isTokenExpired(token);
    }

    public String generateToken(Authentication authentication, String userId) {

        Date now = new Date(System.currentTimeMillis());
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

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }


    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

}
