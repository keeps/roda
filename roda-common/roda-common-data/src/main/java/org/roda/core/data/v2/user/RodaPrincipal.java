/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.user;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public abstract class RodaPrincipal implements Serializable, RODAMember {
  private static final long serialVersionUID = 8254886345679485761L;

  private String id;
  private String name;
  private String fullName;

  private boolean active = true;

  private Set<String> allRoles = new HashSet<String>();
  private Set<String> directRoles = new HashSet<String>();
  private Set<String> allGroups = new HashSet<String>();
  private Set<String> directGroups = new HashSet<String>();

  public RodaPrincipal() {
    this(null, null);
  }

  public RodaPrincipal(final String id, final String name) {
    this(id, name, new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), new HashSet<String>());
  }

  public RodaPrincipal(final String id, final String name, final Set<String> allRoles, final Set<String> directRoles,
    final Set<String> allGroups, final Set<String> directGroups) {
    this.id = id;
    this.name = name;
    this.allRoles = allRoles;
    this.directRoles = directRoles;
    this.allGroups = allGroups;
    this.directGroups = directGroups;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
    result = prime * result + (active ? 1231 : 1237);
    result = prime * result + ((allGroups == null) ? 0 : allGroups.hashCode());
    result = prime * result + ((allRoles == null) ? 0 : allRoles.hashCode());
    result = prime * result + ((directGroups == null) ? 0 : directGroups.hashCode());
    result = prime * result + ((directRoles == null) ? 0 : directRoles.hashCode());
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("RodaGroup [id=");
    builder.append(id);
    builder.append(", name=");
    builder.append(name);
    builder.append(", fullName=");
    builder.append(fullName);
    builder.append(", active=");
    builder.append(active);
    builder.append(", allRoles=");
    builder.append(allRoles);
    builder.append(", directRoles=");
    builder.append(directRoles);
    builder.append(", allGroups=");
    builder.append(allGroups);
    builder.append(", directGroups=");
    builder.append(directGroups);
    builder.append("]");
    return builder.toString();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final RodaPrincipal other = (RodaPrincipal) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (fullName == null) {
      if (other.fullName != null) {
        return false;
      }
    } else if (!fullName.equals(other.fullName)) {
      return false;
    }
    if (active != other.active) {
      return false;
    }
    if (allGroups == null) {
      if (other.allGroups != null) {
        return false;
      }
    } else if (!allGroups.equals(other.allGroups)) {
      return false;
    }
    if (allRoles == null) {
      if (other.allRoles != null) {
        return false;
      }
    } else if (!allRoles.equals(other.allRoles)) {
      return false;
    }
    if (directGroups == null) {
      if (other.directGroups != null) {
        return false;
      }
    } else if (!directGroups.equals(other.directGroups)) {
      return false;
    }
    if (directRoles == null) {
      if (other.directRoles != null) {
        return false;
      }
    } else if (!directRoles.equals(other.directRoles)) {
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

  public Set<String> getAllGroups() {
    return allGroups;
  }

  public void setAllGroups(Set<String> allGroups) {
    this.allGroups = allGroups;
  }

  public Set<String> getDirectGroups() {
    return directGroups;
  }

  public void setDirectGroups(Set<String> directGroups) {
    this.directGroups = directGroups;
  }

  @Override
  public String getUUID() {
    // TODO needs prefix to distinguish from user?
    return getId();
  }

  // TODO: implement validation...
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

  public void addDirectGroup(String group) {
    if (directGroups == null) {
      directGroups = new HashSet<String>();
    }
    directGroups.add(group);
  }

  public void addGroup(String group) {
    if (allGroups == null) {
      allGroups = new HashSet<String>();
    }
    allGroups.add(group);
  }

  public void removeDirectRole(String role) {
    if (directRoles.contains(role)) {
      directRoles.remove(role);
    }
  }

  public void removeGroup(String group) {
    if (directGroups.contains(group)) {
      directGroups.remove(group);
    }
  }

}
