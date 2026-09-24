package com.SpringBoot.Authentication.util;

import com.SpringBoot.Authentication.entity.AuthProviderType;
import com.SpringBoot.Authentication.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;

import io.jsonwebtoken.security.Keys;

@Slf4j
@Component
public class AuthUtil {

    @Value("${JWT_SECRET_KEY}")
    private String jwtSecretKey;

    public SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }
    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .issuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
                .signWith(getSecretKey())
                .compact();
    }

    public String getUserNameFromToken(String token) {
        Claims claims=Jwts.parser()
                .verifyWith((getSecretKey()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public AuthProviderType getProviderTypeFromRegistrationId(String registrationId) {
        if (registrationId.equalsIgnoreCase("google")) {
            return AuthProviderType.GOOGLE;
        } else if (registrationId.equalsIgnoreCase("facebook")) {
            return AuthProviderType.FACEBOOK;
        } else if (registrationId.equalsIgnoreCase("github")) {
            return AuthProviderType.GITHUB;
        } else {
            throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        }
    }

    public String determineProviderIdFromOAuth2User(OAuth2User oAuth2User,String registrationId) {
        String providerId=switch(registrationId.toLowerCase()){
            case "google" -> oAuth2User.getAttribute("sub");
            case "facebook" -> oAuth2User.getAttribute("id").toString();
            case "github" -> oAuth2User.getAttribute("id").toString();
            default ->{
                log.error("Unsupported OAuth2 provider: {}", registrationId);
                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
            }
        };
        if(providerId==null || providerId.isBlank()){
            log.error("Unable to determine providerId from OAuth2User for registrationId: {}", registrationId);
            throw new IllegalArgumentException("Unable to determine providerId from OAuth2User for registrationId: " + registrationId);
        }
        return providerId;
    }

    public String determineUsernameFromOAuth2User(OAuth2User oAuth2User,String registrationId ,String providerId) {
        String email=oAuth2User.getAttribute("email");
        if(email!=null && !email.isBlank()){
            return email;
        }
        return switch(registrationId.toLowerCase()){
            case "google" -> oAuth2User.getAttribute("sub");
            case "github" -> oAuth2User.getAttribute("login");
            default -> providerId;
        };
    }
}