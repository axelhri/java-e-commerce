package neora.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class RateLimitPolicyResolver {

  public Bandwidth resolve(HttpServletRequest request) {
    String path = request.getRequestURI();
    String method = request.getMethod();

    if (path.startsWith("/api/v1/auth")) {
      return Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
    }

    if (path.startsWith("/api/v1/products") && HttpMethod.GET.matches(method)) {
      return Bandwidth.classic(1000, Refill.greedy(1000, Duration.ofMinutes(1)));
    }

    return Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
  }
}
