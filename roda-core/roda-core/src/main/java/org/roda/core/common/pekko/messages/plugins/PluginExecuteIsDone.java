/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.pekko.messages.plugins;

import org.roda.core.plugins.Plugin;

import java.io.Serial;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PluginExecuteIsDone extends PluginMethodIsDone {
  @Serial
  private static final long serialVersionUID = -5136014936634139026L;

  public PluginExecuteIsDone(Plugin<?> plugin, boolean withError) {
    super(plugin, withError);
  }

  public PluginExecuteIsDone(Plugin<?> plugin, boolean withError, String errorMessage) {
    super(plugin, withError, errorMessage);
  }

  @Override
  public String toString() {
    return "PluginExecuteIsDone [getPlugin()=" + getPlugin() + ", isWithError()=" + isWithError()
        + ", getErrorMessage()=" + getErrorMessage() + "]";
  }
}
