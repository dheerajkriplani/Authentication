package com.SpringBoot.Authentication.service;

import com.SpringBoot.Authentication.dto.LoginRequestDto;
import com.SpringBoot.Authentication.dto.LoginResponseDto;
import com.SpringBoot.Authentication.dto.SignUpRequestDto;
import com.SpringBoot.Authentication.dto.SignUpResponseDto;
import com.SpringBoot.Authentication.entity.User;
import com.SpringBoot.Authentication.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.SpringBoot.Authentication.util.AuthUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final  AuthenticationManager authenticationManager;

    private final AuthUtil authUtil;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getUsername(),
                        loginRequestDto.getPassword()
                )
        );
        User user =(User) authentication.getPrincipal();

        String token = authUtil.generateAccessToken(user);

        LoginResponseDto loginResponseDto = new LoginResponseDto();

        loginResponseDto.setJwtToken(token);
        loginResponseDto.setUserId(user.getId());
        return loginResponseDto;
    }


    public SignUpResponseDto signup(SignUpRequestDto signUpRequestDto) {

        User user =userRepository.findByUsername(signUpRequestDto.getUsername()).orElse(null);
        if(user != null) {
            throw new IllegalArgumentException("User Already Exists");
        }
        user=userRepository.save(User.builder()
                .username(signUpRequestDto.getUsername())
                .password(passwordEncoder.encode(signUpRequestDto.getPassword()))
                .build());
        return new SignUpResponseDto(user.getId(), user.getUsername());
    }
}
