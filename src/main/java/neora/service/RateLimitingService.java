package neora.service;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import neora.config.BucketCache;
import neora.config.RateLimitKeyResolver;
import neora.config.RateLimitPolicyResolver;
import neora.dto.RateLimitResult;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimitingService {

  private final RateLimitKeyResolver keyResolver;
  private final RateLimitPolicyResolver policyResolver;
  private final BucketCache bucketCache;

  public RateLimitResult check(HttpServletRequest request) {
    String key = keyResolver.resolve(request);
    var bandwidth = policyResolver.resolve(request);
    Bucket bucket = bucketCache.get(key + ":" + bandwidth.hashCode(), bandwidth);

    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

    if (probe.isConsumed()) {
      return RateLimitResult.allowed(probe.getRemainingTokens());
    }

    long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
    return RateLimitResult.rejected(retryAfterSeconds);
  }
}
