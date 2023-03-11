package bio.terra.aws.resource.discovery;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CachedEnvironmentDiscoveryTest extends EnvironmentDiscoveryTestBase {
  @Test
  public void validation() throws IOException {
    Duration expirationPeriod = Duration.ofMillis(500);
    EnvironmentDiscovery discovery =
        new FilesystemEnvironmentDiscovery(getValidationTestDataPath());
    CachedEnvironmentDiscovery cachedEnvironmentDiscovery =
        new CachedEnvironmentDiscovery(discovery, expirationPeriod);
    validationTestLogic(cachedEnvironmentDiscovery);
  }

  @Test
  public void noLandingZones() throws IOException {
    Duration expirationPeriod = Duration.ofMillis(500);
    EnvironmentDiscovery discovery =
        new FilesystemEnvironmentDiscovery(getNoLandingZonesTestDataPath());
    CachedEnvironmentDiscovery cachedEnvironmentDiscovery =
        new CachedEnvironmentDiscovery(discovery, expirationPeriod);
    noLandingZonesTestLogic(cachedEnvironmentDiscovery);
  }

  @Test
  public void missingEnvironmentConfig() throws IOException {
    Duration expirationPeriod = Duration.ofMillis(500);
    EnvironmentDiscovery discovery =
        new FilesystemEnvironmentDiscovery(getMissingEnvironmentConfigTestDataPath());
    CachedEnvironmentDiscovery cachedEnvironmentDiscovery =
        new CachedEnvironmentDiscovery(discovery, expirationPeriod);
    missingEnvironmentConfigTestLogic(cachedEnvironmentDiscovery);
  }

  @Test
  public void notebookLifecycleMismatch() throws IOException {
    Duration expirationPeriod = Duration.ofMillis(500);
    EnvironmentDiscovery discovery =
        new FilesystemEnvironmentDiscovery(getNotebookLifecycleMismatchTestDataPath());
    CachedEnvironmentDiscovery cachedEnvironmentDiscovery =
        new CachedEnvironmentDiscovery(discovery, expirationPeriod);
    notebookLifecycleMismatchTestLogic(cachedEnvironmentDiscovery);
  }

  @Test
  public void expiration() throws IOException, InterruptedException {
    Duration expirationPeriod = Duration.ofMillis(500);
    EnvironmentDiscovery discovery =
        new FilesystemEnvironmentDiscovery(getValidationTestDataPath());
    CachedEnvironmentDiscovery cachedEnvironmentDiscovery =
        new CachedEnvironmentDiscovery(discovery, expirationPeriod);

    CachedEnvironmentDiscovery.CachedEnvironment firstCachedEnvironment =
        cachedEnvironmentDiscovery.getOrDiscoverEnvironment();

    CachedEnvironmentDiscovery.CachedEnvironment cacheHitEnvironment =
        cachedEnvironmentDiscovery.getOrDiscoverEnvironment();

    // This should have been a cache hit, but we should only test for this if we know that our call
    // finished before the expiration time (to avoid test flakiness if the running VM gets preempted
    // between the first two calls).
    if (Instant.now().isBefore(firstCachedEnvironment.expirationTime())) {
      Assertions.assertSame(
          firstCachedEnvironment.environment(), cacheHitEnvironment.environment());
      Assertions.assertSame(
          firstCachedEnvironment.expirationTime(), cacheHitEnvironment.expirationTime());
    }

    // Wait for last cached entry to expire to trigger another discovery.
    while (Instant.now().isBefore(cacheHitEnvironment.expirationTime())) {
      Thread.sleep(expirationPeriod.toMillis());
    }

    CachedEnvironmentDiscovery.CachedEnvironment secondCachedEnvironment =
        cachedEnvironmentDiscovery.getOrDiscoverEnvironment();

    // Content should be the same, but expiration should be later (meaning cache expiration
    // triggered a new discovery).

    Assertions.assertEquals(
        firstCachedEnvironment.environment(), secondCachedEnvironment.environment());
    Assertions.assertTrue(
        secondCachedEnvironment.expirationTime().isAfter(firstCachedEnvironment.expirationTime()));
  }
}
