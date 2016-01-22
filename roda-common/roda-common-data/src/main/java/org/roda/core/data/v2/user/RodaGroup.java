/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.user;

import java.util.HashSet;
import java.util.Set;

public class RodaGroup extends RodaPrincipal implements RODAMember {
  private static final long serialVersionUID = -718342371831706371L;

  private boolean active = true;
  private Set<String> allRoles = new HashSet<String>();
  private Set<String> directRoles = new HashSet<String>();
  private Set<String> allGroups = new HashSet<String>();
  private Set<String> directGroups = new HashSet<String>();

  public RodaGroup() {
    super();
  }

  public RodaGroup(String id, String name) {
    super(id, name);
  }

  public RodaGroup(String id, String name, Set<String> allRoles, Set<String> directRoles, Set<String> allGroups,
    Set<String> directGroups) {
    super(id, name);
    this.allRoles = allRoles;
    this.directRoles = directRoles;
    this.allGroups = allGroups;
    this.directGroups = directGroups;
  }

  @Override
  public boolean isUser() {
    return false;
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
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (active ? 1231 : 1237);
    result = prime * result + ((allGroups == null) ? 0 : allGroups.hashCode());
    result = prime * result + ((allRoles == null) ? 0 : allRoles.hashCode());
    result = prime * result + ((directGroups == null) ? 0 : directGroups.hashCode());
    result = prime * result + ((directRoles == null) ? 0 : directRoles.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    RodaGroup other = (RodaGroup) obj;
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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("RodaGroup [getId()=");
    builder.append(getId());
    builder.append(", getName()=");
    builder.append(getName());
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

  public boolean isNameValid() {
    return true;
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
