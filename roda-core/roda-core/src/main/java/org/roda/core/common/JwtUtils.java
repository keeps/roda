package org.roda.core.common;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.AuthenticationDeniedException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class JwtUtils {

  public static String generateToken(String subject, Date expirationDate) {
    return generateToken(subject, expirationDate, new HashMap<>());
  }

  public static String generateToken(String subject, Date expirationDate, Map<String, Object> claims) {
    return Jwts.builder().signWith(SignatureAlgorithm.HS256, RodaCoreFactory.getApiSecretKey())
      .setIssuedAt(new Date(System.currentTimeMillis())).setSubject(subject).setExpiration(expirationDate)
      .addClaims(claims).compact();
  }

  public static String regenerateToken(String token) throws AuthenticationDeniedException {
    Claims claims = getClaimsFromToken(token);
    String subject = getSubjectFromToken(token);
    Date expiredDate = getExpiredDateFromToken(token);

    return generateToken(subject, expiredDate, claims);
  }

  private static Claims getClaimsFromToken(String token) throws AuthenticationDeniedException {
    try {
      return Jwts.parser().setSigningKey(RodaCoreFactory.getApiSecretKey()).parseClaimsJws(token).getBody();
    } catch (Exception e) {
      throw new AuthenticationDeniedException("invalid token");
    }
  }

  public static String getSubjectFromToken(String token) throws AuthenticationDeniedException {
    Claims claims = getClaimsFromToken(token);
    return claims.getSubject();
  }

  private static Date getExpiredDateFromToken(String token) throws AuthenticationDeniedException {
    Claims claims = getClaimsFromToken(token);
    return claims.getExpiration();
  }

  public static boolean isTokenExpired(String token) throws AuthenticationDeniedException {
    Date expiredDate = getExpiredDateFromToken(token);
    return expiredDate.before(new Date());
  }

  public static boolean validateToken(String token) {
    try {
      getSubjectFromToken(token);
      return true;
    } catch (AuthenticationDeniedException e) {
      return false;
    }
  }

}
