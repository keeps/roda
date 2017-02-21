/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.user;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.data.v2.IsModelObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class RodaPrincipal implements Serializable, RODAMember, IsModelObject {
  private static final long serialVersionUID = 8254886345679485761L;

  private String id;
  private String name;
  private String fullName;

  private boolean active = true;

  private Set<String> allRoles = new HashSet<String>();
  private Set<String> directRoles = new HashSet<String>();

  public RodaPrincipal() {
    this(null, null);
  }

  public RodaPrincipal(final String id, final String name) {
    this(id, name, new HashSet<String>(), new HashSet<String>());
  }

  public RodaPrincipal(final String id, final String name, final Set<String> allRoles, final Set<String> directRoles) {
    this(id, name, name, true, allRoles, directRoles);
  }

  public RodaPrincipal(final String id, final String name, final String fullName, final boolean active,
    final Set<String> allRoles, final Set<String> directRoles) {
    this.id = id;
    this.name = name;
    this.fullName = fullName;
    this.active = active;
    this.allRoles = allRoles;
    this.directRoles = directRoles;

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (active ? 1231 : 1237);
    result = prime * result + ((allRoles == null) ? 0 : allRoles.hashCode());
    result = prime * result + ((directRoles == null) ? 0 : directRoles.hashCode());
    result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final RodaPrincipal other = (RodaPrincipal) obj;
    if (active != other.active)
      return false;
    if (allRoles == null) {
      if (other.allRoles != null)
        return false;
    } else if (!allRoles.equals(other.allRoles))
      return false;
    if (directRoles == null) {
      if (other.directRoles != null)
        return false;
    } else if (!directRoles.equals(other.directRoles))
      return false;
    if (fullName == null) {
      if (other.fullName != null)
        return false;
    } else if (!fullName.equals(other.fullName))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "RodaPrincipal [id=" + id + ", name=" + name + ", fullName=" + fullName + ", active=" + active
      + ", allRoles=" + allRoles + ", directRoles=" + directRoles + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Set<String> getAllRoles() {
    return allRoles;
  }

  public void setAllRoles(Set<String> allRoles) {
    this.allRoles = allRoles;
  }

  public Set<String> getDirectRoles() {
    return directRoles;
  }

  public void setDirectRoles(Set<String> directRoles) {
    this.directRoles = directRoles;
  }

  @Override
  public String getUUID() {
    // TODO needs prefix to distinguish from user?
    return getId();
  }

  // TODO: implement validation...
  @JsonIgnore
  public boolean isNameValid() {
    return true;
  }

  public boolean hasRole(String role) {
    return allRoles.contains(role);
  }

  public void addDirectRole(String role) {
    if (directRoles == null) {
      directRoles = new HashSet<String>();
    }
    directRoles.add(role);
  }

  public void removeDirectRole(String role) {
    if (directRoles.contains(role)) {
      directRoles.remove(role);
    }
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "name", "fullName", "isActive", "isUser", "allRoles", "directRoles");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(id, name, fullName, isActive(), isUser(), allRoles, directRoles);
  }

}
