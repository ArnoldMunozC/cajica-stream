package com.cajica.stream.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final UserDetailsService userDetailsService;

  @Autowired
  public SecurityConfig(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(
            AbstractHttpConfigurer::disable) // Desactivar CSRF temporalmente para facilitar pruebas
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers(
                        "/",
                        "/cursos",
                        "/cursos/*",
                        "/cursos/*/videos",
                        "/registro",
                        "/login",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/api/cursos/images/**",
                        "/api/inscripciones/**",
                        "/api/cursos/mis-cursos")
                    .permitAll()
                    .requestMatchers("/password/forgot", "/password/reset", "/password/reset/**")
                    .permitAll() // Permitir acceso a rutas de recuperación de contraseña
                    .requestMatchers("/cursos/*/videos/*")
                    .authenticated()
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/cursos", true).permitAll())
        .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll())
        .rememberMe(
            remember -> remember.key("cajicaStreamSecretKey").tokenValiditySeconds(86400) // 1 día
            );

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Configuration
  public static class AuthenticationConfiguration extends GlobalAuthenticationConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationConfiguration(
        UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
      this.userDetailsService = userDetailsService;
      this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
      auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }
  }
}
