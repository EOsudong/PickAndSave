package org.sy.pickandsave.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

	private final Key key;
	private final long expiration;

	public JwtTokenProvider(
			@Value("${jwt.secret}") String secretKey,
			@Value("${jwt.expiration}") long expiration
	) {
		this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
		this.expiration = expiration;
	}

	public String createToken(Long userId, String role) {
		Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
		claims.put("role", role);

		Date now = new Date();
		Date validity = new Date(now.getTime() + expiration);

		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(validity)
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	public Long getUserId(String token) {
		return Long.parseLong(
				Jwts.parserBuilder().setSigningKey(key).build()
						.parseClaimsJws(token).getBody().getSubject()
		);
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}