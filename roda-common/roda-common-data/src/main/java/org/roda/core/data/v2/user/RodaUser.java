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

public class RodaUser extends RodaSimpleUser implements RODAMember {
  private static final long serialVersionUID = -718342371831706371L;

  private boolean active = true;
  private String ipAddress = "";
  private Set<String> allRoles = new HashSet<String>();
  private Set<String> directRoles = new HashSet<String>();
  private Set<String> allGroups = new HashSet<String>();
  private Set<String> directGroups = new HashSet<String>();

  public RodaUser() {
    super();
  }

  public RodaUser(String id, String name, String email, boolean guest) {
    super(id, name, email, guest);
  }

  public RodaUser(RodaSimpleUser user) {
    super(user.getId(), user.getName(), user.getEmail(), user.isGuest());
  }

  public RodaUser(String id, String name, String email, boolean guest, String ipAddress, Set<String> allRoles,
    Set<String> directRoles, Set<String> allGroups, Set<String> directGroups) {
    super(id, name, email, guest);
    this.ipAddress = ipAddress;
    this.allRoles = allRoles;
    this.directRoles = directRoles;
    this.allGroups = allGroups;
    this.directGroups = directGroups;
  }

  public RodaUser(RodaSimpleUser user, String ipAddress, Set<String> allRoles, Set<String> directRoles,
    Set<String> allGroups, Set<String> directGroups) {
    super(user.getId(), user.getName(), user.getEmail(), user.isGuest());
    this.ipAddress = ipAddress;
    this.allRoles = allRoles;
    this.directRoles = directRoles;
    this.allGroups = allGroups;
    this.directGroups = directGroups;
  }

  @Override
  public boolean isUser() {
    return true;
  }

  public boolean isActive() {
    return active;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
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
    result = prime * result + ((ipAddress == null) ? 0 : ipAddress.hashCode());
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
    if (!(obj instanceof RodaUser)) {
      return false;
    }
    RodaUser other = (RodaUser) obj;
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
    if (ipAddress == null) {
      if (other.ipAddress != null) {
        return false;
      }
    } else if (!ipAddress.equals(other.ipAddress)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "RodaUser [active=" + active + ", ipAddress=" + ipAddress + ", allRoles=" + allRoles + ", directRoles="
      + directRoles + ", allGroups=" + allGroups + ", directGroups=" + directGroups + ", isUser()=" + isUser() + ", "
      + super.toString() + "]";
  }

  public void addDirectRole(String role) {
    if (directRoles == null) {
      directRoles = new HashSet<String>();
    }
    directRoles.add(role);
  }

  public void addGroup(String group) {
    if (allGroups == null) {
      allGroups = new HashSet<String>();
    }
    allGroups.add(group);
  }

  public boolean isNameValid() {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean hasRole(String role) {
    return allRoles.contains(role);
  }
}
