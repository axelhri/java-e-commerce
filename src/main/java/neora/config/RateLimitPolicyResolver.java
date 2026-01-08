package neora.config;

import io.github.bucket4j.Bandwidth;
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
      return Bandwidth.builder().capacity(10).refillIntervally(10, Duration.ofMinutes(1)).build();
    }

    if (path.startsWith("/api/v1/products") && HttpMethod.GET.matches(method)) {
      return Bandwidth.builder()
          .capacity(1000)
          .refillIntervally(1000, Duration.ofMinutes(1))
          .build();
    }

    return Bandwidth.builder().capacity(100).refillIntervally(100, Duration.ofMinutes(1)).build();
  }
}
