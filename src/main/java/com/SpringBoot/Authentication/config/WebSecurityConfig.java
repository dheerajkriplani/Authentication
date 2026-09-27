package com.SpringBoot.Authentication.config;

import com.SpringBoot.Authentication.entity.RoleType;
import com.SpringBoot.Authentication.service.JwtAuthFilter;
import com.SpringBoot.Authentication.service.OAuth2SuccessHandler;
import com.SpringBoot.Authentication.util.HttpCookieOAuth2AuthorizationRequestRepository;
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
import org.springframework.web.servlet.HandlerExceptionResolver;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig  {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))  //for disabling default in-memory session management
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/public/**", "/auth/**").permitAll()
                        .requestMatchers("/admin/**").hasRole(RoleType.ADMIN.name())
                        .requestMatchers("/doctors/**").hasAnyRole(RoleType.DOCTOR.name(), RoleType.ADMIN.name())
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2Login ->
                        oauth2Login
                                .authorizationEndpoint(endpoint -> endpoint
                                        .authorizationRequestRepository(new HttpCookieOAuth2AuthorizationRequestRepository()))
                                .failureHandler(
                                        (request, response, exception) -> {
                                            log.error("OAuth2 login failed: {}", exception.getMessage());
                                            handlerExceptionResolver.resolveException(request, response, null, exception);
                                        })
                                .successHandler(oAuth2SuccessHandler)
                )
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint((request, response, authException) -> {
                                    log.error("Authentication failed: {}", authException.getMessage());
                                    handlerExceptionResolver.resolveException(request, response, null, authException);
                                })
                );


//        .formLogin(Customizer.withDefaults()); for default login page

        return http.build();
    }
}
