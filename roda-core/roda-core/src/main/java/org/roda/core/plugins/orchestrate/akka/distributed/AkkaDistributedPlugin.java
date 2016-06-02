/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka.distributed;

import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.storage.StorageService;

public class AkkaDistributedPlugin {
  private final StorageService storage;
  private final ModelService model;
  private final IndexService index;

  public AkkaDistributedPlugin() {
    storage = RodaCoreFactory.getStorageService();
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();
  }

  public StorageService getStorage() {
    return storage;
  }

  public ModelService getModel() {
    return model;
  }

  public IndexService getIndex() {
    return index;
  }

  public class PluginMessage<T> {
    private List<? extends T> list;
    private Plugin<? extends T> plugin;

    public PluginMessage(List<? extends T> list, Plugin<? extends T> plugin) {
      this.list = list;
      this.plugin = plugin;
    }

    public List<? extends T> getList() {
      return list;
    }

    public void setList(List<? extends T> list) {
      this.list = list;
    }

    public Plugin<? extends T> getPlugin() {
      return plugin;
    }

    public void setPlugin(Plugin<? extends T> plugin) {
      this.plugin = plugin;
    }

  }

}
