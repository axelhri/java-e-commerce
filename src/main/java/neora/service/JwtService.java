package neora.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.expiration}")
  private Long jwtExpiration;

  @Value("${jwt.refresh.expiration}")
  private Long jwtRefreshExpiration;

  private static final String sameSiteStrict = "Strict";

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, jwtExpiration);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, jwtRefreshExpiration);
  }

  public String buildToken(
      Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSignInKey())
        .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSignInKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public ResponseCookie generateAccessTokenCookie(String token) {
    return ResponseCookie.from("access_token", token)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(jwtExpiration / 1000)
        .sameSite(sameSiteStrict)
        .build();
  }

  public ResponseCookie generateRefreshTokenCookie(String token) {
    return ResponseCookie.from("refresh_token", token)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(jwtRefreshExpiration / 1000)
        .sameSite(sameSiteStrict)
        .build();
  }

  public ResponseCookie getCleanAccessTokenCookie() {
    return ResponseCookie.from("access_token", "")
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(0)
        .sameSite(sameSiteStrict)
        .build();
  }

  public ResponseCookie getCleanRefreshTokenCookie() {
    return ResponseCookie.from("refresh_token", "")
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(0)
        .sameSite(sameSiteStrict)
        .build();
  }
}
