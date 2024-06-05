package org.roda.core.data.v2.jobs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PluginInfoList implements Serializable {

  @Serial
  private static final long serialVersionUID = -1672632175473486128L;

  @JsonProperty(value = "pluginInfoList")
  private List<PluginInfo> list = new ArrayList<>();

  public PluginInfoList() {
    // empty constructor
  }

  public PluginInfoList(List<PluginInfo> pluginInfoList) {
    this.list = pluginInfoList;
  }

  public List<PluginInfo> getPluginInfoList() {
    return list;
  }

  public void setPluginInfoList(List<PluginInfo> pluginInfoList) {
    this.list = pluginInfoList;
  }

  public void addObject(PluginInfo pluginInfo) {
    this.list.add(pluginInfo);
  }

  public void addObjects(List<PluginInfo> list) {
    this.list.addAll(list);
  }
}
