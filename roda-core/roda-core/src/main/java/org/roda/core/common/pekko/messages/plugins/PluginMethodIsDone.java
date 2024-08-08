package org.roda.core.common.pekko.messages.plugins;

import org.roda.core.common.pekko.messages.AbstractMessage;
import org.roda.core.plugins.Plugin;

import java.io.Serial;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PluginMethodIsDone extends AbstractMessage {
  @Serial
  private static final long serialVersionUID = -8701179264086005994L;

  private final Plugin<?> plugin;
  private final boolean withError;
  private String errorMessage = "";

  public PluginMethodIsDone(Plugin<?> plugin, boolean withError) {
    super();
    this.plugin = plugin;
    this.withError = withError;
  }

  public PluginMethodIsDone(Plugin<?> plugin, boolean withError, String errorMessage) {
    super();
    this.plugin = plugin;
    this.withError = withError;
    this.errorMessage = errorMessage;
  }

  public Plugin<?> getPlugin() {
    return plugin;
  }

  public boolean isWithError() {
    return withError;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public String toString() {
    return "PluginMethodIsDone [plugin=" + plugin + ", withError=" + withError + ", errorMessage=" + errorMessage
        + "]";
  }
}
