package neora.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class RateLimitKeyResolver {

  public String resolve(HttpServletRequest request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
      return "USER:" + auth.getName();
    }

    return "IP:" + resolveClientIp(request);
  }

  private String resolveClientIp(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader("X-Forwarded-For"))
        .map(v -> v.split(",")[0].trim())
        .filter(v -> !v.isBlank())
        .orElse(request.getRemoteAddr());
  }
}
