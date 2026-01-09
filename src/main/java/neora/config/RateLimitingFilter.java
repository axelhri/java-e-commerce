package neora.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import neora.dto.RateLimitResult;
import neora.service.RateLimitingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

  private final RateLimitingService rateLimitingService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    RateLimitResult result = rateLimitingService.check(request);

    if (result.allowed()) {
      response.setHeader("X-Rate-Limit-Remaining", String.valueOf(result.remainingTokens()));
      filterChain.doFilter(request, response);
      return;
    }

    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
    response.setContentType("application/json");

    response
        .getWriter()
        .write(
            """
      {
        "error": "rate_limit_exceeded",
        "retryAfterSeconds": %d
      }
      """
                .formatted(result.retryAfterSeconds()));
  }
}
