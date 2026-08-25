package com.example.chatapp.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /*
    CORS origins used to be a hardcoded List.of("http://192.168.1.41:3000", ...).
    That is the single most common way this app breaks: someone's router
    hands out a different LAN address, the frontend's default IP no
    longer matches the whitelist, and every API call - profile search,
    starting a chat, sending a message - fails as a CORS error that
    looks nothing like a CORS error in the browser console. Contacts
    stop appearing and messages stop sending, and neither failure says
    "CORS" anywhere.

    Two changes fix this for good rather than for this one IP:

    1. The list is now a Spring property (app.cors.allowed-origins),
       so pointing the frontend at a new machine never requires a
       recompile.

    2. It's matched with setAllowedOriginPatterns rather than
       setAllowedOrigins, so entries can use a wildcard like
       "http://192.168.*.*:*" and cover an entire LAN subnet instead
       of one exact address. setAllowedOrigins requires an exact
       string match and forbids "*" outright when credentials are
       allowed; setAllowedOriginPatterns exists specifically to allow
       wildcards together with credentials.
    */
    @Value("${app.cors.allowed-origins:"
            + "http://localhost:*,"
            + "http://127.0.0.1:*,"
            + "http://192.168.*.*:*,"
            + "http://10.*.*.*:*,"
            + "http://172.16.*.*:*,"
            + "http://172.17.*.*:*,"
            + "http://172.18.*.*:*,"
            + "http://172.19.*.*:*,"
            + "http://172.2*.*.*:*,"
            + "http://172.3*.*.*:*"
            + "}")
    private String allowedOriginPatterns;

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/ws/**",
                                "/users/**",
                                "/private/**",
                                "/public/**",
                                "/api/auth/**",
                                "/api/login/**",
                                "/api/profile/**",
                                "/api/groups/**",
                                "/api/keys/**",
                                "/api/private-room/**",
                                "/api/app/**",
                                "/apk/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Note for anyone tempted to add {@code @CrossOrigin} back onto a
     * controller: it won't do anything. Spring Security's CORS filter
     * runs ahead of the MVC dispatcher and is authoritative for every
     * request once {@code .cors(...)} is configured here, so a
     * controller-level annotation is silently ignored rather than
     * additive. This bean is the one and only place CORS is decided.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> patterns = new ArrayList<>();
        for (String raw : allowedOriginPatterns.split(",")) {
            String trimmed = raw.trim();
            if (!trimmed.isEmpty()) {
                patterns.add(trimmed);
            }
        }

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(patterns);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
