package com.SpringBoot.Authentication.config;

import com.SpringBoot.Authentication.service.JwtAuthFilter;
import com.SpringBoot.Authentication.service.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig  {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))  //for disabling default in-memory session management
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/","/public/**","/auth/**").permitAll()
                .requestMatchers("/admin/**").authenticated()
                .anyRequest().authenticated()
        )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2Login -> oauth2Login.failureHandler(
                        (request, response, exception) -> {
                            log.error("OAuth2 login failed: {}", exception.getMessage());
                        })
                        .successHandler(oAuth2SuccessHandler)
                );

//        .formLogin(Customizer.withDefaults()); for default login page

        return http.build();
    }
}
