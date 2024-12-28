package com.metaorta.kaspi.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class JWTUtil {

    @Value("${jwt.secret:thisissecret}")
    private String secret;

    public String generateToken(String token) {
        return JWT.create()
                .withSubject("AZIMBEST")
                .withClaim("token", token)
                .withIssuedAt(new Date())
                .withIssuer("https://www.HZ_CHTO_TUT.kz")
                .withExpiresAt(Date.from(ZonedDateTime.now().plusHours(5).toInstant()))
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateTokenAndRetrieveData(String token) {
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("https://www.HZ_CHTO_TUT.kz")
                .withSubject("AZIMBEST")
                .build();

        return jwtVerifier.verify(token).getClaim("token").asString();
    }
}
