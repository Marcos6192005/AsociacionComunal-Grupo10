package com.sv.grupo10.asociacioncomunal.backendasociacioncomunal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class Config {

    private final JwtFilter jwtfFilter;

    public Config(JwtFilter jwtfFilter){
        this.jwtfFilter = jwtfFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/admin/**", "/api/usuarios/**").hasAnyRole(
                                Roles.ADMINISTRACION,
                                Roles.PRESIDENTE,
                                Roles.SECRETARIO,
                                Roles.TESORERO
                        )
                        .requestMatchers("/api/comunidad/**").hasRole(Roles.VECINO)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtfFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
