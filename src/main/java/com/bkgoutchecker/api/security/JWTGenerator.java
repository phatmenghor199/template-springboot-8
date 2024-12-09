package com.bkgoutchecker.api.security;

import java.util.Calendar;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import java.security.Key;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JWTGenerator {
	// private static final KeyPair keyPair =
	// Keys.keyPairFor(SignatureAlgorithm.RS256);
	// private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
	private static final String SECRET_KEY = "da2bd42bef8f9f957d647ed13571d35b59c7055fb723eacdb05154878da17d77d3180385fc232cab38bded0fc7dbda0d439cc275e91164ade266d64d50633bc7";
	private static final Key key = new javax.crypto.spec.SecretKeySpec(SECRET_KEY.getBytes(),
			SignatureAlgorithm.HS512.getJcaName());

	public String generateToken(Authentication authentication) {
		String username = authentication.getName();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1); // Add 1 year
		Date expireDate = calendar.getTime();
		String token = Jwts.builder()

				.setIssuedAt(new Date())
				.setExpiration(expireDate)
				.setSubject(username)
				.signWith(key, SignatureAlgorithm.HS512)
				.compact();
		System.out.println("New token :");
		System.out.println(token);
		return token;
	}

	public String getUsernameFromJWT(String token) {
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();
		return claims.getSubject();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
					.setSigningKey(key)
					.build()
					.parseClaimsJws(token);
			return true;
		} catch (Exception ex) {
			throw new AuthenticationCredentialsNotFoundException("JWT was exprired or incorrect",
					ex.fillInStackTrace());
		}
	}

}
