package com.mugon.backend.configurations;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    // https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter

    @Value("${emercast.cors.allowed.url}")
    private String corsAllowedUrl;

    @Value("${emercast.jwt.private-key}")
    private String jwtPrivateKey;

    @Value("${emercast.jwt.expiration}")
    private long jwtExpiration;

    @Bean
    public SecurityFilterChain configureJWT(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors((cors) -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(List.of(corsAllowedUrl));
                    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "HEAD", "DELETE"));
                    configuration.applyPermitDefaultValues();
                    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                    source.registerCorsConfiguration("/**", configuration);
                    cors.configurationSource(source);
                })
                .authorizeHttpRequests((auth) -> auth.anyRequest().authenticated())
                //.authorizeHttpRequests((auth) -> auth.anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults())
                .oauth2ResourceServer(config -> config.jwt((jwt) -> jwt.decoder(getDecoder())));
        return http.build();
    }

    public JwtDecoder getDecoder() {
        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) throws JwtException {
                try {
                    var decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                    var headers = new HashMap<String, Object>();
                    headers.put("alg", "HS256");
                    headers.put("typ", "JWT");
                    return new Jwt(decodedToken.getUid(), null, null, headers, decodedToken.getClaims());
                } catch (FirebaseAuthException e) {
                    throw new JwtException(e.getMessage());
                }
            }
        };
    }
}
