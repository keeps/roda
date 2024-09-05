/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.pekko.messages.jobs;

import org.roda.core.data.v2.SerializableOptional;
import org.roda.core.plugins.Plugin;

import java.io.Serial;
import java.util.Optional;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JobStateDetailsUpdated extends JobPartialUpdate {
  @Serial
  private static final long serialVersionUID = 1946036502369851214L;

  private final Plugin<?> plugin;
  private final SerializableOptional<String> stateDetails;

  public JobStateDetailsUpdated(Plugin<?> plugin, Optional<String> stateDetails) {
    super();
    this.plugin = plugin;
    this.stateDetails = SerializableOptional.setOptional(stateDetails);
  }

  public JobStateDetailsUpdated(Plugin<?> plugin, Throwable throwable) {
    this(plugin, Optional.of(throwable.getClass().getName() + ": " + throwable.getMessage()));
  }

  public Plugin<?> getPlugin() {
    return plugin;
  }

  public Optional<String> getStateDetails() {
    return stateDetails.getOptional();
  }

  @Override
  public String toString() {
    return "JobStateDetailsUpdated [plugin=" + plugin + ", stateDetails=" + stateDetails + "]";
  }
}
