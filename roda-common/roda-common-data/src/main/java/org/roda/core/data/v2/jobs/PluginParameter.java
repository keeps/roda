/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.index.filter.Filter;

/**
 * @author Rui Castro
 * @author Luis Faria <lfaria@keep.pt>
 */
public class PluginParameter implements Serializable {
  private static final long serialVersionUID = -431821437136556726L;

  public enum PluginParameterType {
    /**
     * Normal string
     */
    STRING,

    /**
     * Password type parameter, should not be echoed
     */
    // PASSWORD,

    /**
     * Checkbox
     */
    BOOLEAN,

    /**
     * Integer box
     */
    INTEGER,

    /**
     * String which will be translated into the canonical class name of sip to
     * aip plugin
     */
    PLUGIN_SIP_TO_AIP,

    /**
     * Metadata type select box
     */
    METADATA_TYPE,

    /**
     * Interface to select an AIP Id
     */
    AIP_ID,

    /**
     * Interface to select an Risk Id
     */
    RISK_ID,

    /**
     * Interface to select a severity level
     */
    SEVERITY,

    /**
     * Interface to select a representation type
     */
    REPRESENTATION_TYPE,

    /**
     * Interface to select a RODA object
     */
    RODA_OBJECT,

    /**
     * Interface to select a RODA object (AIP, representation, file) fields
     */
    AIP_FIELDS, REPRESENTATION_FIELDS, FILE_FIELDS,

    /**
     * Interface to select permission types
     */
    PERMISSION_TYPES;
  }

 
  private String id = null;
  private String name = null;
  private PluginParameterType type = null;
  private String defaultValue = null;
  private List<String> possibleValues = new ArrayList<>();
  private boolean mandatory = true;
  private boolean readonly = false;
  private String description = null;

  private RenderingHints renderingHings = null;

  /**
   * Constructs an empty {@link PluginParameter}.
   */
  public PluginParameter() {
    // do nothing
  }

  /**
   * Constructs a new {@link PluginParameter} with the given parameters.
   * 
   * @param id
   * @param name
   * @param type
   * @param defaultValue
   * @param mandatory
   * @param readonly
   * @param description
   */
  public PluginParameter(String id, String name, PluginParameterType type, String defaultValue, boolean mandatory,
    boolean readonly, String description) {
    setId(id);
    setName(name);
    setType(type);
    setDefaultValue(defaultValue);
    // setPossibleValues(); no possible values, value is free text
    setMandatory(mandatory);
    setReadonly(readonly);
    setDescription(description);
  }

  /**
   * Constructs a new {@link PluginParameter} with the given parameters.
   * 
   * @param id
   * @param name
   * @param type
   * @param value
   * @param possibleValues
   * @param mandatory
   * @param readonly
   * @param description
   */
  public PluginParameter(String id, String name, PluginParameterType type, String value, List<String> possibleValues,
    boolean mandatory, boolean readonly, String description) {
    setId(id);
    setName(name);
    setType(type);
    setDefaultValue(value);
    setPossibleValues(possibleValues);
    setMandatory(mandatory);
    setReadonly(readonly);
    setDescription(description);
  }

  /**
   * Constructs a new {@link PluginParameter} cloning an existing
   * {@link PluginParameter}.
   * 
   * @param parameter
   *          the {@link PluginParameter} to clone.
   */
  public PluginParameter(PluginParameter parameter) {
    this(parameter.getId(), parameter.getName(), parameter.getType(), parameter.getDefaultValue(),
      new ArrayList<>(parameter.getPossibleValues()), parameter.isMandatory(), parameter.isReadonly(),
      parameter.getDescription());
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "PluginParameter [id=" + id + ", name=" + name + ", type=" + type + ", defaultValue=" + defaultValue
      + ", possibleValues=" + possibleValues + ", mandatory=" + mandatory + ", readonly=" + readonly + ", description="
      + description + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + (mandatory ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((possibleValues == null) ? 0 : possibleValues.hashCode());
    result = prime * result + (readonly ? 1231 : 1237);
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    if (!(obj instanceof PluginParameter)) {
      return false;
    }
    PluginParameter other = (PluginParameter) obj;
    if (defaultValue == null) {
      if (other.defaultValue != null) {
        return false;
      }
    } else if (!defaultValue.equals(other.defaultValue)) {
      return false;
    }
    if (description == null) {
      if (other.description != null) {
        return false;
      }
    } else if (!description.equals(other.description)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (mandatory != other.mandatory) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (possibleValues == null) {
      if (other.possibleValues != null) {
        return false;
      }
    } else if (!possibleValues.equals(other.possibleValues)) {
      return false;
    }
    if (readonly != other.readonly) {
      return false;
    }
    if (type != other.type) {
      return false;
    }
    return true;
  }

  public String getId() {
    return id;
  }

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
   * @return the type
   */
  public PluginParameterType getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(PluginParameterType type) {
    this.type = type;
  }

  /**
   * @return the value
   */
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  /**
   * @return the possibleValues
   */
  public List<String> getPossibleValues() {
    return possibleValues;
  }

  /**
   * @param possibleValues
   *          the possibleValues to set
   */
  public PluginParameter setPossibleValues(List<String> possibleValues) {
    this.possibleValues.clear();
    if (possibleValues != null) {
      this.possibleValues.addAll(possibleValues);
    }
    return this;
  }

  /**
   * @return the mandatory
   */
  public boolean isMandatory() {
    return mandatory;
  }

  /**
   * @param mandatory
   *          the mandatory to set
   */
  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }

  /**
   * @return the readonly
   */
  public boolean isReadonly() {
    return readonly;
  }

  /**
   * @param readonly
   *          the readonly to set
   */
  public void setReadonly(boolean readonly) {
    this.readonly = readonly;
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
   * @return the renderingHings
   */
  public RenderingHints getRenderingHings() {
    return renderingHings;
  }

  /**
   * @param renderingHings
   *          the renderingHings to set
   */
  public void setRenderingHings(RenderingHints renderingHings) {
    this.renderingHings = renderingHings;
  }

}
