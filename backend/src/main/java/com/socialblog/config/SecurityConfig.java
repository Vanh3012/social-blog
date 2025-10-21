package com.socialblog.config;

import com.socialblog.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomUserDetailsService userDetailsService;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/", "/home",
                                                                "/login", "/register",
                                                                "/css/**", "/js/**", "/images/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/register").permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .usernameParameter("username")
                                                .passwordParameter("password")
                                                .defaultSuccessUrl("/home", true)
                                                .failureUrl("/login?error=true")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout=true")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .userDetailsService(userDetailsService)
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/register") // Tạm thời disable CSRF cho
                                                                                      // register nếu cần
                                );

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}