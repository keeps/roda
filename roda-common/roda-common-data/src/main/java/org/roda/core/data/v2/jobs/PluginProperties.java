/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author António Lindo <alindo@keep.pt>
 */

  @JsonRootName("plugin")
  public class PluginProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -2328914839866065966L;
    @JsonProperty("objectClasses")
    private Set<String> objectClasses = new HashSet<>();
    @JsonProperty("categories")
    private List<String> categories = null;
    @JsonProperty("type")
    private PluginType type = PluginType.MISC;

    public PluginProperties() {
    }

    PluginProperties(Set<String> objectClasses, List<String> categories, PluginType type) {
      this.objectClasses = objectClasses;
      this.categories = categories;
      this.type = type;
    }

    public List<String> getCategories() {
      return categories;
    }

    public Set<String> getObjectClasses() {
      return objectClasses;
    }

    public PluginType getType() {
      return type;
    }

    public void setCategories(List<String> categories) {
      this.categories = categories;
    }

    public void setObjectClasses(Set<String> objectClasses) {
      this.objectClasses = objectClasses;
    }

    public void addObjectClass(String objectClass) {
      this.objectClasses.add(objectClass);
    }

    public void setType(PluginType type) {
      this.type = type;
    }
  }
