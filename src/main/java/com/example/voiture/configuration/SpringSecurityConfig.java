package com.example.voiture.configuration;

import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.voiture.authentification.JwtAuthorizationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SpringSecurityConfig {
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    public SpringSecurityConfig(CustomUserDetailsService customUserDetailsService,
            JwtAuthorizationFilter jwtAuthorizationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(NoOpPasswordEncoder noOpPasswordEncoder) throws Exception {
        return new CustomAuthenticationManager(noOpPasswordEncoder, customUserDetailsService);
    }

    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService);
    }

    // @Bean
    // public AuthenticationManager authenticationManager(HttpSecurity http,
    // NoOpPasswordEncoder noOpPasswordEncoder)
    // throws Exception {
    // AuthenticationManagerBuilder authenticationManagerBuilder = http
    // .getSharedObject(AuthenticationManagerBuilder.class);
    // authenticationManagerBuilder.userDetailsService(customUserDetailsService)
    // .passwordEncoder(noOpPasswordEncoder);
    // //
    // authenticationManagerBuilder.authenticationProvider(customAuthenticationProvider);
    // return authenticationManagerBuilder.build();
    // }

    @SuppressWarnings("deprecated")
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests().requestMatchers("/rest/auth/**").permitAll().requestMatchers("/")
                .hasRole("USER")
                .anyRequest()
                .authenticated().and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public UserDetailsService users() {
        UserDetails user = User.builder().username("user").password(passwordEncoder().encode("user")).roles("USER")
                .build();
        UserDetails admin = User.builder().username("admin").password(passwordEncoder().encode("admin"))
                .roles("USER", "ADMIN").build();
        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public NoOpPasswordEncoder passwordEncoder() {
        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
    }

    // @Bean
    // public BCryptPasswordEncoder passwordEncoder() {
    // return new BCryptPasswordEncoder();
    // }
}
