package com.personal_projects.cloud_application.backend.config;

import com.personal_projects.cloud_application.backend.services.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration class for the cloud application backend.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final UserService userService;

  /**
   * Constructor for SecurityConfiguration.
   *
   * @param jwtAuthenticationFilter the JWT authentication filter
   * @param userService the user service
   */
  public SecurityConfiguration(
          final JwtAuthenticationFilter jwtAuthenticationFilter,
          final UserService userService) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.userService = userService;
  }

  /**
   * Configures the security filter chain.
   *
   * @param http the HttpSecurity object
   * @return the SecurityFilterChain object
   * @throws Exception if an error occurs
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            request ->
                request
                    .requestMatchers("/**")
                    .permitAll()
                    //                        .requestMatchers("/auth/**").permitAll()
                    //                        .requestMatchers("/owner/**").permitAll()
                    //
                    // .requestMatchers("/admin").hasAnyAuthority(Role.ADMIN.name(),
                    // Role.OWNER.name())
                    //
                    // .requestMatchers("/user").hasAnyAuthority(Role.USER.name(),
                    // Role.ADMIN.name(), Role.OWNER.name())
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  /**
   * Configures the authentication provider.
   *
   * @return the AuthenticationProvider object
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userService.userDetailsService());
    authenticationProvider.setPasswordEncoder(passwordEncoder());
    return authenticationProvider;
  }

  /**
   * Configures the password encoder.
   *
   * @return the PasswordEncoder object
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Configures the authentication manager.
   *
   * @param config the AuthenticationConfiguration object
   * @return the AuthenticationManager object
   * @throws Exception if an error occurs
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
          throws Exception {
    return config.getAuthenticationManager();
  }
}