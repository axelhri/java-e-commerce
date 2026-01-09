package neora.config;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import neora.model.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtAuthenticationFilter jwtAuthFilter;
  private final RateLimitingFilter rateLimitingFilter;
  private final AuthenticationProvider authenticationProvider;
  private final LogoutHandler logoutHandler;

  @Value("${app.cors.allowed-origins}")
  private List<String> allowedOrigins;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-ui/**",
                        "/swagger-ui.html")
                    .permitAll()
                    .requestMatchers("/api/v1/payments/webhook")
                    .permitAll()
                    .requestMatchers("/api/v1/auth/**")
                    .permitAll()
                    .requestMatchers("/api/v1/email/confirm")
                    .permitAll()
                    .requestMatchers("/api/v1/user/**")
                    .authenticated()
                    .requestMatchers("/api/v1/vendors")
                    .hasRole(Role.ADMIN.name())
                    .requestMatchers(HttpMethod.POST, "/api/v1/categories")
                    .hasRole(Role.ADMIN.name())
                    .requestMatchers(HttpMethod.GET, "/api/v1/categories")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/products")
                    .hasRole(Role.ADMIN.name())
                    .requestMatchers(HttpMethod.GET, "/api/v1/products/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .logout(
            logout ->
                logout
                    .logoutUrl("/api/v1/auth/logout")
                    .addLogoutHandler(logoutHandler)
                    .logoutSuccessHandler(
                        (request, response, authentication) ->
                            SecurityContextHolder.clearContext()));

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(allowedOrigins);

    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(
        Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
