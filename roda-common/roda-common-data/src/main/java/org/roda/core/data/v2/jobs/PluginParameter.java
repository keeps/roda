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
    AIP_ID;
  }

  /**
   * Datetime
   */
  // public static final String TYPE_DATETIME = "datetime";

  private String id = null;
  private String name = null;
  private PluginParameterType type = null;
  private String defaultValue = null;
  private List<String> possibleValues = new ArrayList<String>();
  private boolean mandatory = true;
  private boolean readonly = false;
  private String description = null;

  /**
   * Constructs an empty {@link PluginParameter}.
   */
  public PluginParameter() {
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
      parameter.getPossibleValues(), parameter.isMandatory(), parameter.isReadonly(), parameter.getDescription());
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "PluginParameter [id=" + id + ", name=" + name + ", type=" + type + ", value=" + defaultValue
      + ", possibleValues=" + possibleValues + ", mandatory=" + mandatory + ", readonly=" + readonly + ", description="
      + description + "]";
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    boolean equal = false;

    if (obj != null && obj instanceof PluginParameter) {

      PluginParameter other = (PluginParameter) obj;

      equal = getName() == other.getName() || getName().equals(other.getName());
      equal &= getId() == other.getId() || getId().equals(other.getId());
      equal &= getType() == other.getType() || getType().equals(other.getType());
      equal &= getDefaultValue() == other.getDefaultValue() || getDefaultValue().equals(other.getDefaultValue());
      equal &= possibleValues.equals(other.possibleValues);

    } else {
      equal = false;
    }

    return equal;
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
  public void setPossibleValues(List<String> possibleValues) {
    this.possibleValues.clear();
    if (possibleValues != null) {
      this.possibleValues.addAll(possibleValues);
    }
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

}
