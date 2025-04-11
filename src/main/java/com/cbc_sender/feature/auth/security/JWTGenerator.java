package com.cbc_sender.feature.auth.security;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import io.jsonwebtoken.Jwts;

import java.security.Key;
import java.util.List;
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;

@Component
public class JWTGenerator {

    @Value("${jwt.secret.key}")
    private String secretKey; // Injected secret key from properties

    @Value("${jwt.expiration-min}")
    private long jwtExpirationInMinutes;

    private Key getSigningKey() {
        return new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS512.getJcaName());
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // Get authorities/roles
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date currentDate = new Date();
        long expirationTimeInMs = jwtExpirationInMinutes * 60 * 1000;
        Date expireDate = new Date(currentDate.getTime() + expirationTimeInMs);

        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .setSubject(username)
                .claim("roles", roles) // Add roles to the token
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT was expired or incorrect",
                    ex.fillInStackTrace());
        }
    }

}
