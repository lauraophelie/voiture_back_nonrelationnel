package com.example.voiture.authentification;

import io.jsonwebtoken.*;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.example.voiture.entity.Utilisateur;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    private final String secret_key = "mysecretkey";
    private long accessTokenValidity = 86400; // Durée de validité en secondes (ici, 24 heures)

    private final JwtParser jwtParser;

    private final String TOKEN_HEADER = "Authorization";
    private final String TOKEN_PREFIX = "Bearer ";

    public JwtUtil() {
        this.jwtParser = Jwts.parser().setSigningKey(secret_key);
    }

    public String createToken(Utilisateur user) {
        Claims claims = Jwts.claims().setSubject(user.getEmail());
        claims.put("id", user.getId());        
        claims.put("role", user.getRole());
        claims.put("firstName", user.getEmail());
        claims.put("password", user.getPassword());
        Date tokenCreateTime = new Date();
        Date tokenValidity = new Date(tokenCreateTime.getTime() + TimeUnit.MINUTES.toMillis(accessTokenValidity));
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(tokenValidity)
                .signWith(SignatureAlgorithm.HS256, secret_key)
                .compact();
    }

    public Claims parseJwtClaims(String token) {
        return jwtParser.parseClaimsJws(token).getBody();
    }

    public Claims resolveClaims(HttpServletRequest req) {
        try {
            String token = resolveToken(req);
            if (token != null) {
                return parseJwtClaims(token);
            }
            return null;
        } catch (ExpiredJwtException ex) {
            req.setAttribute("expired", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            req.setAttribute("invalid", ex.getMessage());
            throw ex;
        }
    }

    public String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    public boolean validateClaims(Claims claims) throws AuthenticationException {
        try {
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            throw e;
        }
    }

    public String getEmail(Claims claims) {
        return claims.getSubject();
    }

    public static String getUserId(Claims claims) {
        return claims.get("id", String.class);
    }
    
}
