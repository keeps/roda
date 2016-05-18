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

import org.roda.core.plugins.Plugin;

public class JobInfo implements Serializable {
  private static final long serialVersionUID = 5962481824986980596L;

  private Map<Plugin<?>, JobPluginInfo> pluginsInfo;
  private int objectsCount;
  private boolean hasTimeoutOccurred;

  public JobInfo() {
    pluginsInfo = new HashMap<>();
    objectsCount = 0;
    hasTimeoutOccurred = false;
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

  public boolean isHasTimeoutOccurred() {
    return hasTimeoutOccurred;
  }

  public void setHasTimeoutOccurred(boolean hasTimeoutOccurred) {
    this.hasTimeoutOccurred = hasTimeoutOccurred;
  }

  public <T extends Serializable> void put(Plugin<T> innerPlugin, JobPluginInfo jobPluginInfo, int objectsCount) {
    pluginsInfo.put(innerPlugin, jobPluginInfo);
    this.objectsCount += objectsCount;
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
