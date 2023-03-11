package bio.terra.discovery;

import java.io.IOException;

/**
 * Interface used to discover AWS Support Resources in a single Terra AWS Environment and its
 * associated Landing Zones.
 */
public interface EnvironmentDiscovery {
  /**
   * Get an {@link Environment} class instance describing all the Support Resources for a Terra AWS
   * Environment and its associated Landing Zones.
   *
   * @return an {@link Environment} instance representing an Environment's Support Resources
   * @throws IOException
   */
  public Environment discoverEnvironment() throws IOException;
}
