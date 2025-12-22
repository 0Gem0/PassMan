package com.Passman.Manager.Auth.config;

import com.Passman.Manager.Auth.Filters.JsonUsernamePasswordAuthenticationFilter;
import com.Passman.Manager.Auth.Services.MyUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final MyUserDetailsService myUserDetailsService;

    public SecurityConfig(MyUserDetailsService myUserDetailsService) {
        this.myUserDetailsService = myUserDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JsonUsernamePasswordAuthenticationFilter jsonFilter =
                new JsonUsernamePasswordAuthenticationFilter(authManager(http));

        jsonFilter.setAuthenticationSuccessHandler((req, res, auth) -> {
            res.setStatus(HttpServletResponse.SC_OK);
        });

        jsonFilter.setAuthenticationFailureHandler((req, res, ex) -> {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
        });
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilter(jsonFilter)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**").permitAll()
//                        .requestMatchers("/v3/api-docs").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/vault/**").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        return builder.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(myUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}