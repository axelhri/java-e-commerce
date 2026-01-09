package neora.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class BucketCache {

  private final Cache<String, Bucket> cache =
      Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).maximumSize(100_000).build();

  public Bucket get(String key, Bandwidth bandwidth) {
    return cache.get(key, k -> Bucket.builder().addLimit(bandwidth).build());
  }
}
