/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.plugins.Plugin;

public class JobInfo implements Serializable {
  private static final long serialVersionUID = 5962481824986980596L;

  private Map<Plugin<?>, JobPluginInfo> pluginsInfo;
  private Map<Plugin<?>, Boolean> pluginsDone;
  private int objectsCount;
  private boolean initEnded;
  private boolean done;

  public JobInfo() {
    pluginsInfo = new HashMap<>();
    pluginsDone = new HashMap<>();
    objectsCount = 0;
    initEnded = false;
    done = false;
  }

  public Map<Plugin<?>, JobPluginInfo> getJobInfo() {
    return pluginsInfo;
  }

  public void setJobInfo(Map<Plugin<?>, JobPluginInfo> jobInfo) {
    this.pluginsInfo = jobInfo;
  }

  public int getObjectsCount() {
    return objectsCount;
  }

  public void setObjectsCount(int objectsCount) {
    this.objectsCount = objectsCount;
  }

  public boolean isInitEnded() {
    return initEnded;
  }

  public void setInitEnded(boolean initEnded) {
    this.initEnded = initEnded;
  }

  public boolean isDone() {
    boolean isDone = true;
    for (Entry<Plugin<?>, Boolean> entry : pluginsDone.entrySet()) {
      if (!entry.getValue()) {
        isDone = false;
        break;
      }
    }
    done = isDone;
    return done;
  }

  public <T extends IsRODAObject> void put(Plugin<T> innerPlugin, JobPluginInfo jobPluginInfo) {
    pluginsInfo.put(innerPlugin, jobPluginInfo);
  }

  public <T extends IsRODAObject> void setStarted(Plugin<T> innerPlugin) {
    pluginsDone.put(innerPlugin, false);
  }

  public <T extends IsRODAObject> void setDone(Plugin<T> innerPlugin) {
    pluginsDone.put(innerPlugin, true);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((pluginsInfo == null) ? 0 : pluginsInfo.hashCode());
    result = prime * result + objectsCount;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof JobInfo)) {
      return false;
    }
    JobInfo other = (JobInfo) obj;
    if (pluginsInfo == null) {
      if (other.pluginsInfo != null) {
        return false;
      }
    } else if (!pluginsInfo.equals(other.pluginsInfo)) {
      return false;
    }
    if (objectsCount != other.objectsCount) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "JobState [jobInfos=" + pluginsInfo + ", objectsCount=" + objectsCount + "]";
  }

}
