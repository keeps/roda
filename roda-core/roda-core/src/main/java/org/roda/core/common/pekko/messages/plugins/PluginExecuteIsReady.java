/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.pekko.messages.plugins;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.plugins.Plugin;

import java.io.Serial;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PluginExecuteIsReady<T extends IsRODAObject> extends PluginMethodIsReady<T> {
  @Serial
  private static final long serialVersionUID = 1821489252490235130L;

  private final List<LiteOptionalWithCause> list;
  private boolean hasBeenForwarded = false;

  public PluginExecuteIsReady(Plugin<T> plugin, List<LiteOptionalWithCause> list) {
    super(plugin);
    this.list = list;
  }

  public List<LiteOptionalWithCause> getList() {
    return list;
  }

  public void setHasBeenForwarded() {
    this.hasBeenForwarded = true;
  }

  @Override
  public String toString() {
    return "PluginExecuteIsReady [list=" + list + ", hasBeenForwarded=" + hasBeenForwarded + ", getPlugin()="
        + getPlugin() + "]";
  }
}
