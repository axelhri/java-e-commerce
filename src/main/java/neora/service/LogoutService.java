package neora.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.repository.TokenRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService implements LogoutHandler {

  private final TokenRepository tokenRepository;

  @Override
  public void logout(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.debug("Logout attempt without Authorization header");
      return;
    }
    jwt = authHeader.substring(7);
    var storedToken = tokenRepository.findByJwtToken(jwt).orElse(null);
    if (storedToken != null) {
      log.info("Logging out user with token for user ID: {}", storedToken.getUser().getId());
      storedToken.setExpired(true);
      storedToken.setRevoked(true);
      tokenRepository.save(storedToken);
      SecurityContextHolder.clearContext();
      log.info("User logged out successfully");
    } else {
      log.warn("Logout attempt with invalid token");
    }
  }
}
