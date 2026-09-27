package com.SpringBoot.Authentication.service;

import com.SpringBoot.Authentication.dto.LoginRequestDto;
import com.SpringBoot.Authentication.dto.LoginResponseDto;
import com.SpringBoot.Authentication.dto.SignUpRequestDto;
import com.SpringBoot.Authentication.dto.SignUpResponseDto;
import com.SpringBoot.Authentication.entity.AuthProviderType;
import com.SpringBoot.Authentication.entity.Patient;
import com.SpringBoot.Authentication.entity.RoleType;
import com.SpringBoot.Authentication.entity.User;
import com.SpringBoot.Authentication.repo.PatientRepository;
import com.SpringBoot.Authentication.repo.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.SpringBoot.Authentication.util.AuthUtil;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final  AuthenticationManager authenticationManager;

    private final AuthUtil authUtil;

    private final UserRepository userRepository;

    private final PatientRepository patientRepository;

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

    public User signUpInternal(SignUpRequestDto signUpRequestDto,AuthProviderType authProviderType,String providerId) {
        User user = userRepository.findByUsername(signUpRequestDto.getUsername()).orElse(null);

        if (user != null) {
            throw new IllegalArgumentException("User Already Exists");
        }

        user = User.builder()
                .username(signUpRequestDto.getUsername())
                .providerId(providerId)
                .authProviderType(authProviderType)
                .roles(signUpRequestDto.getRoles())      //RoleType.PATIENT
                .build();

        if (authProviderType == AuthProviderType.EMAIL) {
            user.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        }

        user=userRepository.save(user);

        Patient patient=Patient.builder()
                .name(signUpRequestDto.getName())
                .username(signUpRequestDto.getUsername())
                .email(signUpRequestDto.getUsername())
                .user(user)
                .build();

        patientRepository.save(patient);

        return user;
    }

    public SignUpResponseDto signup(SignUpRequestDto signUpRequestDto) {
        User user =signUpInternal(signUpRequestDto,AuthProviderType.EMAIL,null);
        return new SignUpResponseDto(user.getId(), user.getUsername());
    }

    @Transactional
    public ResponseEntity<LoginResponseDto> handleOAuth2LoginRequest(OAuth2User oAuth2User, String registrationId) {
        //if user has account :directly login
        // otherwise first signup and login
        AuthProviderType providerType = authUtil.getProviderTypeFromRegistrationId(registrationId);
        String providerId=authUtil.determineProviderIdFromOAuth2User(oAuth2User, registrationId);

        User user=userRepository.findByProviderIdAndAuthProviderType(providerId,providerType).orElse(null);

        String emailId=oAuth2User.getAttribute("email");
        String name=oAuth2User.getAttribute("name");

        User emailUser=userRepository.findByUsername(emailId).orElse(null);

        if(user==null && emailUser==null){
            //signup
            String username=authUtil.determineUsernameFromOAuth2User(oAuth2User, registrationId, providerId);
            user = signUpInternal(new SignUpRequestDto(username, null,name,Set.of(RoleType.PATIENT)), providerType, providerId);
        }else if(user!=null){
            if(emailId!=null && !emailId.isBlank() && !emailId.equals(user.getUsername())){
                user.setUsername(emailId);
                userRepository.save(user);
            }
        }else{
            throw new BadCredentialsException("This email is already registered with provider "+emailUser.getAuthProviderType());
        }

        //login
        LoginResponseDto loginResponseDto = new LoginResponseDto(authUtil.generateAccessToken(user),user.getId());

        return ResponseEntity.ok(loginResponseDto);
    }
}
