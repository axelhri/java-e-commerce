package agorafolk.api.springboot_agorafolk.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;

@TestConfiguration
public class SecurityConfigTest {

  @Bean
  public JwtAuthenticationFilter jwtAuthFilter () {
    return Mockito.mock(JwtAuthenticationFilter.class);
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    return Mockito.mock(AuthenticationProvider.class);
  }
}
