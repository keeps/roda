package pt.gov.dgarq.roda.core.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains information about a plugin.
 * 
 * @author Rui Castro
 */
public class PluginInfo implements Serializable {
  private static final long serialVersionUID = -7977449299190436877L;

  /**
   * The ID is the plugin classname.
   */
  private String id = null;

  private String name = null;

  private float version = 0f;

  private String description = null;

  private List<PluginParameter> parameters = new ArrayList<PluginParameter>();

  /**
   * Constructs a new {@link PluginInfo}.
   */
  public PluginInfo() {
  }

  /**
   * Constructs a new {@link PluginInfo} cloning an existing {@link PluginInfo}.
   * 
   * @param pluginInfo
   *          the {@link PluginInfo} to clone.
   */
  public PluginInfo(PluginInfo pluginInfo) {
    this(pluginInfo.getId(), pluginInfo.getName(), pluginInfo.getVersion(), pluginInfo.getDescription(), pluginInfo
      .getParameters());
  }

  /**
   * Constructs a new {@link PluginInfo} with the given parametes.
   * 
   * @param id
   * @param name
   * @param version
   * @param description
   * @param parameters
   */
  public PluginInfo(String id, String name, float version, String description, PluginParameter[] parameters) {
    setId(id);
    setName(name);
    setVersion(version);
    setDescription(description);
    setParameters(parameters);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof PluginInfo) {
      PluginInfo other = (PluginInfo) obj;
      return getId().equals(other.getId());
    } else {
      return false;
    }
  }

  /**
   * @see Object#toString()
   */
  public String toString() {

    List<PluginParameter> parameters = null;

    if (getParameters() != null) {
      parameters = Arrays.asList(getParameters());
    }

    return "PluginInfo(" + "id=" + getId() + ", name=" + getName() + ", version=" + getVersion() + ", description="
      + getDescription() + ", parameters=" + parameters + ")";
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the version
   */
  public float getVersion() {
    return version;
  }

  /**
   * @param version
   *          the version to set
   */
  public void setVersion(float version) {
    this.version = version;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the parameters
   */
  public PluginParameter[] getParameters() {
    return parameters.toArray(new PluginParameter[parameters.size()]);
  }

  /**
   * @param parameters
   *          the parameters to set
   */
  public void setParameters(PluginParameter[] parameters) {
    this.parameters.clear();
    if (parameters != null) {
      this.parameters.addAll(Arrays.asList(parameters));
    }
  }

}
