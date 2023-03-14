package bio.terra.aws.resource.discovery;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class CachedEnvironmentDiscovery implements EnvironmentDiscovery {

  private final EnvironmentDiscovery backingEnvironmentDiscovery;
  private final Duration expirationPeriod;
  private Environment cachedEnvironment;
  private Instant expirationTime;

  public CachedEnvironmentDiscovery(
      EnvironmentDiscovery backingEnvironmentDiscovery, Duration expirationPeriod) {
    this.backingEnvironmentDiscovery = backingEnvironmentDiscovery;
    this.expirationPeriod = expirationPeriod;
  }

  public record CachedEnvironment(Environment environment, Instant expirationTime) {}
  ;

  private boolean isExpired() {
    return Instant.now().isAfter(expirationTime);
  }

  @VisibleForTesting
  public synchronized CachedEnvironment getOrDiscoverEnvironment() throws IOException {
    if (cachedEnvironment == null || isExpired()) {
      cachedEnvironment = backingEnvironmentDiscovery.discoverEnvironment();
      expirationTime = Instant.now().plus(expirationPeriod);
    }

    return new CachedEnvironment(cachedEnvironment, expirationTime);
  }

  @Override
  public Environment discoverEnvironment() throws IOException {
    return getOrDiscoverEnvironment().environment();
  }
}
