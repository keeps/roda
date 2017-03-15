/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 08-02-2016.
 */
public class MetadataValue implements Comparable, Serializable {
  private static final long serialVersionUID = 51528625577483594L;
  private String id;
  private HashMap<String, String> options;

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
  public MetadataValue(String id, HashMap<String, String> options) {
    this.id = id;
    if (options == null || options.isEmpty()) {
      this.options = new HashMap<>();
    } else {
      this.options = options;
    }

    if (!this.options.containsKey("label")) {
      this.options.put("label", id);
    }
  }

  /**
   * @return The ID of the object.
   */
  public String getId() {
    return id;
  }

  public String get(String key) {
    return options.get(key);
  }

  public void set(String key, String value) {
    options.put(key, value);
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setOptions(HashMap<String, String> options) {
    this.options = options;
  }

  /**
   * @return The options of the object.
   */
  public Map<String, String> getOptions() {
    return options;
  }

  protected MetadataValue copy() {
    HashMap<String, String> newOptions = new HashMap<>();
    for (Entry<String, String> entry : options.entrySet()) {
      newOptions.put(entry.getKey(), entry.getValue());
    }
    return new MetadataValue(this.id, newOptions);
  }

  @Override
  public int compareTo(Object o) {
    if (o == this) {
      return 0;
    }
    if (o instanceof MetadataValue) {
      // Compare the order option
      MetadataValue mv = (MetadataValue) o;
      Object selfOrder = get("order");
      Object mvOrder = mv.get("order");
      if (selfOrder != null) {
        if (mvOrder != null) {
          int selfInt = selfOrder instanceof String ? Integer.parseInt((String) selfOrder) : (Integer) selfOrder;
          int mvInt = mvOrder instanceof String ? Integer.parseInt((String) mvOrder) : (Integer) mvOrder;
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
      String selfLabel = get("label");
      String mvLabel = mv.get("label");
      if (selfLabel != null) {
        return selfLabel.compareTo(mvLabel);
      }
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
