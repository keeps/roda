/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.pekko.messages.plugins;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.plugins.Plugin;

import java.io.Serial;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PluginAfterAllExecuteIsReady<T extends IsRODAObject> extends PluginMethodIsReady<T> {
  @Serial
  private static final long serialVersionUID = 8852688692792086166L;

  public PluginAfterAllExecuteIsReady(Plugin<T> plugin) {
    super(plugin);
  }

  @Override
  public String toString() {
    return "PluginAfterAllExecuteIsReady [plugin=" + getPlugin() + "]";
  }
}
