package neora.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.repository.TokenRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService implements LogoutHandler {

  private final TokenRepository tokenRepository;
  private final JwtService jwtService;

  @Override
  public void logout(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

    // Clear both cookies
    ResponseCookie cleanRefreshCookie = jwtService.getCleanRefreshTokenCookie();
    ResponseCookie cleanAccessCookie = jwtService.getCleanAccessTokenCookie();

    response.addHeader(HttpHeaders.SET_COOKIE, cleanRefreshCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, cleanAccessCookie.toString());

    String jwt = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("access_token".equals(cookie.getName())) {
          jwt = cookie.getValue();
          break;
        }
      }
    }

    if (jwt == null) {
      log.debug("Logout attempt without access token cookie");
      return;
    }

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
