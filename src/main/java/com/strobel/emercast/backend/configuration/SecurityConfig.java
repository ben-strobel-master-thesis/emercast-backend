package com.strobel.emercast.backend.configuration;

import com.strobel.emercast.backend.filters.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(@Autowired JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    // https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter

    @Value("${emercast.cors.allowed.url}")
    private String corsAllowedUrl;

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
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/authority/hash", "/authority", "authorities").permitAll()
                        .requestMatchers(HttpMethod.GET, "/broadcastMessage", "/broadcastMessage/hash").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, SecurityContextPersistenceFilter.class)
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
