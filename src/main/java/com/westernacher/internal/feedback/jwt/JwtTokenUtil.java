package com.westernacher.internal.feedback.jwt;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.westernacher.internal.feedback.domain.Person;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
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

    public String extractEmailId(String token) {
        DecodedJWT jwt = JWT.decode(token);
        Map<String, Claim> claims =jwt.getClaims();
        return claims.get("preferred_username").asString();
    }

    public void verifyToken(String token) {
        try{
            DecodedJWT jwt = JWT.decode(token);

            if (jwt.getExpiresAt().before(Calendar.getInstance().getTime())) {
                throw new RuntimeException("Exired token!");
            }
            JwkProvider provider = new UrlJwkProvider("http://localhost:4200");
            Jwk jwk = provider.get(jwt.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            algorithm.verify(jwt);
        }catch(JwkException e) {

        }
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
            //Object bbb = jwtUtil.parseToken(token);
            DecodedJWT jwt = JWT.decode(token);
            /*claimsJws = Jwts.parser().parseClaimsJws(token);*/
            System.out.println("");
        }catch (Exception e) {
            e.printStackTrace();
        }
        return claimsJws.getBody();
    }

    public static Optional<RSAPublicKey> getParsedPublicKey() {
        // public key content...excluding '---PUBLIC KEY---' and '---END PUBLIC KEY---'
        String PUB_KEY = "3sKcJSD4cHwTY5jYm5lNEzqk3wON1CaARO5EoWIQt5u-X-ZnW61CiRZpWpfhKwRYU153td5R8p-AJDWT-NcEJ0MHU3KiuIEPmbgJpS7qkyURuHRucDM2lO4L4XfIlvizQrlyJnJcd09uLErZEO9PcvKiDHoois2B4fGj7CsAe5UZgExJvACDlsQSku2JUyDmZUZP2_u_gCuqNJM5o0hW7FKRI3MFoYCsqSEmHnnumuJ2jF0RHDRWQpodhlAR6uKLoiWHqHO3aG7scxYMj5cMzkpe1Kq_Dm5yyHkMCSJ_JaRhwymFfV_SWkqd3n-WVZT0ADLEq0RNi9tqZ43noUnO_w";

        // removes white spaces or char 20
        String PUBLIC_KEY = "";
        if (!PUB_KEY.isEmpty()) {
            PUBLIC_KEY = PUB_KEY.replace(" ", "");
        }

        try {
            byte[] decode = com.google.api.client.util.Base64.decodeBase64(PUBLIC_KEY);
            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(decode);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(keySpecX509);
            return Optional.of(pubKey);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            System.out.println("Exception block | Public key parsing error ");
            return Optional.empty();
        }
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
