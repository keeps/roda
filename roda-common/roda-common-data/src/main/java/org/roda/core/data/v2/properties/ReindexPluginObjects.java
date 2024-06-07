package org.roda.core.data.v2.properties;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ReindexPluginObjects implements Serializable {

  @Serial
  private static final long serialVersionUID = -8094375078384437496L;

  private Set<ReindexPluginObject> pluginsObjects;

  public ReindexPluginObjects() {
    pluginsObjects = new HashSet<>();
  }

  public ReindexPluginObjects(Set<ReindexPluginObject> pluginsObjects) {
    this.pluginsObjects = pluginsObjects;
  }

  public Set<ReindexPluginObject> getPluginsObjects() {
    return pluginsObjects;
  }

  public void setPluginsObjects(Set<ReindexPluginObject> pluginsObjects) {
    this.pluginsObjects = pluginsObjects;
  }

  public boolean addObject(ReindexPluginObject pluginInfo) {
    return this.pluginsObjects.add(pluginInfo);
  }

  public boolean addObjects(Set<ReindexPluginObject> list) {
    return this.pluginsObjects.addAll(list);
  }
}
