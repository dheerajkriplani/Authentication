package com.SpringBoot.Authentication.service;

import com.SpringBoot.Authentication.dto.LoginResponseDto;
import com.SpringBoot.Authentication.entity.RefreshToken;
import com.SpringBoot.Authentication.entity.User;
import com.SpringBoot.Authentication.repo.RefreshTokenRepository;
import com.SpringBoot.Authentication.repo.UserRepository;
import com.SpringBoot.Authentication.util.AuthUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${REFRESH_TOKEN_TTL}")
    private long refreshTTL;//60 sec

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private AuthUtil authUtil;

    public RefreshToken generateRefreshToken(String userName){

        User user=userRepository.findByUsername(userName).orElse(null);
        RefreshToken refreshToken=user.getRefreshToken();
        if(refreshToken==null){
            refreshToken=RefreshToken.builder()
                    .refreshToken(UUID.randomUUID().toString())
                    .expiresIn(Instant.now().plusMillis(refreshTTL))
                    .user(user)
                    .build();
        }else{
            refreshToken.setExpiresIn(Instant.now().plusMillis(refreshTTL));
        }

        user.setRefreshToken(refreshToken);


        refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    public LoginResponseDto verifyRefreshToken(String refreshToken){

        RefreshToken refreshTokenObj=refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(()-> new JwtException("No Refresh token found in database"));

        if(refreshTokenObj.getExpiresIn().compareTo(Instant.now())<0){
            refreshTokenRepository.delete(refreshTokenObj);
            throw new JwtException("Refresh Token Expired!!");
        }

        User user =refreshTokenObj.getUser();

        String newJwtToken= authUtil.generateAccessToken(user);

        LoginResponseDto loginResponseDto = new LoginResponseDto();

        loginResponseDto.setJwtToken(newJwtToken);
        loginResponseDto.setUserId(user.getId());
        loginResponseDto.setRefreshToken(refreshTokenObj.getRefreshToken());
        return loginResponseDto;

    }
}
