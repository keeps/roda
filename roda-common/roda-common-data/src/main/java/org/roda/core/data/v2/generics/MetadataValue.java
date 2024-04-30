/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.generics;


import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 08-02-2016.
 */
public class MetadataValue implements Comparable<MetadataValue>, Serializable {

  @Serial
  private static final long serialVersionUID = 51528625577483594L;

  public static final String LABEL = "label";
  private String id;
  private Map<String, String> options;

  public MetadataValue() {
    options = new HashMap<>();
  }

  /**
   * Creates a new MetadataValue object.
   *
   * @param id
   *          The id of the MetadataValue object.
   * @param options
   *          The options map of the MetadataValue
   */
  public MetadataValue(String id, Map<String, String> options) {
    this.id = id;
    if (options == null || options.isEmpty()) {
      this.options = new HashMap<>();
    } else {
      this.options = options;
    }

    if (!this.options.containsKey(LABEL)) {
      this.options.put(LABEL, id);
    }
  }

  /**
   * @return The ID of the object.
   */
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String get(String key) {
    return options.get(key);
  }

  public void set(String key, String value) {
    options.put(key, value);
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }

  /**
   * @return The options of the object.
   */
  public Map<String, String> getOptions() {
    return options;
  }

  public MetadataValue copy() {
    HashMap<String, String> newOptions = new HashMap<>();
    for (Map.Entry<String, String> entry : options.entrySet()) {
      newOptions.put(entry.getKey(), entry.getValue());
    }
    return new MetadataValue(this.id, newOptions);
  }

  @Override
  public int compareTo(MetadataValue o) {
    if (o == this) {
      return 0;
    }

    String selfOrder = get("order");
    String mvOrder = o.get("order");

    if (selfOrder != null) {
      if (mvOrder != null) {
        int selfInt = Integer.parseInt(selfOrder);
        int mvInt = Integer.parseInt(mvOrder);
        int result = Integer.compare(selfInt, mvInt);

        if (result != 0) {
          return result;
        }
      }
      return -1;
    } else if (mvOrder != null) {
      return 1;
    }

    // Compare the labels as a fallback
    String selfLabel = get(LABEL);
    String mvLabel = o.get(LABEL);
    if (selfLabel != null) {
      return selfLabel.compareTo(mvLabel);
    }
    return 0;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((options == null) ? 0 : options.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MetadataValue other = (MetadataValue) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (options == null) {
      if (other.options != null)
        return false;
    } else if (!options.equals(other.options))
      return false;
    return true;
  }
}
