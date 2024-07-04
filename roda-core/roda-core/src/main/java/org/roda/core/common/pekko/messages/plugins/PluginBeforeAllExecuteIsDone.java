package org.roda.core.common.pekko.messages.plugins;

import org.roda.core.plugins.Plugin;

import java.io.Serial;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PluginBeforeAllExecuteIsDone extends PluginMethodIsDone {
  @Serial
  private static final long serialVersionUID = 7449486178368177015L;

  public PluginBeforeAllExecuteIsDone(Plugin<?> plugin, boolean withError) {
    super(plugin, withError);
  }

  @Override
  public String toString() {
    return "PluginBeforeAllExecuteIsDone [getPlugin()=" + getPlugin() + ", isWithError()=" + isWithError() + "]";
  }
}
