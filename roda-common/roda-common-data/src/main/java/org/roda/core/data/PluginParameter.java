/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rui Castro
 */
public class PluginParameter implements Serializable {
  private static final long serialVersionUID = -431821437136556726L;

  /**
   * Normal string
   */
  public static final String TYPE_STRING = "string";

  /**
   * Password type parameter, should not be echoed
   */
  public static final String TYPE_PASSWORD = "password";

  /**
   * Boolean
   */
  public static final String TYPE_BOOLEAN = "boolean";

  /**
   * Datetime
   */
  // public static final String TYPE_DATETIME = "datetime";

  private String name = null;
  private String type = null;
  private String value = null;
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
   * @param name
   * @param type
   * @param value
   * @param mandatory
   * @param readonly
   * @param description
   */
  public PluginParameter(String name, String type, String value, boolean mandatory, boolean readonly, String description) {
    setName(name);
    setType(type);
    setValue(value);
    // setPossibleValues(); no possible values, value is free text
    setMandatory(mandatory);
    setReadonly(readonly);
    setDescription(description);
  }

  /**
   * Constructs a new {@link PluginParameter} with the given parameters.
   * 
   * @param name
   * @param type
   * @param value
   * @param possibleValues
   * @param mandatory
   * @param readonly
   * @param description
   */
  public PluginParameter(String name, String type, String value, String[] possibleValues, boolean mandatory,
    boolean readonly, String description) {
    setName(name);
    setType(type);
    setValue(value);
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
    this(parameter.getName(), parameter.getType(), parameter.getValue(), parameter.getPossibleValues(), parameter
      .isMandatory(), parameter.isReadonly(), parameter.getDescription());
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "Parameter(type=" + getType() + ", name=" + getName() + ", value=" + getValue() + ", possibleValues="
      + Arrays.toString(getPossibleValues()) + ", mandatory=" + isMandatory() + ", readonly=" + isReadonly()
      + ", description=" + getDescription() + ")";
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    boolean equal = false;

    if (obj != null && obj instanceof PluginParameter) {

      PluginParameter other = (PluginParameter) obj;

      equal = getName() == other.getName() || getName().equals(other.getName());
      equal &= getType() == other.getType() || getType().equals(other.getType());
      equal &= getValue() == other.getValue() || getValue().equals(other.getValue());
      equal &= possibleValues.equals(other.possibleValues);

    } else {
      equal = false;
    }

    return equal;
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
  public String getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * @return the possibleValues
   */
  public String[] getPossibleValues() {
    return possibleValues.toArray(new String[possibleValues.size()]);
  }

  /**
   * @param possibleValues
   *          the possibleValues to set
   */
  public void setPossibleValues(String[] possibleValues) {
    this.possibleValues.clear();
    if (possibleValues != null) {
      this.possibleValues.addAll(Arrays.asList(possibleValues));
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
