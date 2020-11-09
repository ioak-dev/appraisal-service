package com.westernacher.internal.feedback.jwt;

import com.westernacher.internal.feedback.domain.Person;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    public String extractUserId(String token) {
        Claims claims = null;
        try{
            claims = extractAllClaims(token);
        }catch(Exception e) {
            e.printStackTrace();
        }

        return claims.get("userId").toString();
    }

    public String extractUser(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email").toString();
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        Jws<Claims> claimsJws = null;
        try {
            claimsJws = Jwts.parser().setSigningKey("secret".getBytes("UTF-8")).parseClaimsJws(token);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return claimsJws.getBody();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        return createToken(claims);
    }

    private String createToken(Map<String, Object> claims) {

        String token =  Jwts.builder().setClaims(claims).setSubject("Appraisal User Authorization").setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(SignatureAlgorithm.HS256, secret).compact();

        log.warn("appraisal TEST MESSAGE - token generated - " + token);

        return token;
    }

}
