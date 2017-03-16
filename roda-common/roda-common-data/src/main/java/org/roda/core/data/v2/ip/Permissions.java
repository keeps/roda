/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Rui Castro
 * @author Luis Faria <lfaria@keep.pt>
 */
public class Permissions implements Serializable {
  private static final long serialVersionUID = -3534275853026959624L;

  public enum PermissionType {
    CREATE, READ, UPDATE, DELETE, GRANT;
  }

  private Map<PermissionType, Set<String>> users;
  private Map<PermissionType, Set<String>> groups;

  /**
   * Constructs an empty {@link Permissions}.
   */
  public Permissions() {
    users = new EnumMap<>(PermissionType.class);
    groups = new EnumMap<>(PermissionType.class);
    init(users);
    init(groups);
  }

  public Permissions(Permissions ps) {
    users = ps.getUsers();
    groups = ps.getGroups();
  }

  private void init(Map<PermissionType, Set<String>> permissionsMap) {

    for (PermissionType type : PermissionType.values()) {
      permissionsMap.put(type, new HashSet<String>());
    }
  }

  public Map<PermissionType, Set<String>> getUsers() {
    return users;
  }

  @JsonIgnore
  public Set<String> getUsernames() {
    HashSet<String> usernames = new HashSet<>();

    for (Set<String> values : users.values()) {
      usernames.addAll(values);
    }

    return usernames;
  }

  public void setUsers(Map<PermissionType, Set<String>> users) {
    this.users = users;
  }

  public Map<PermissionType, Set<String>> getGroups() {
    return groups;
  }

  @JsonIgnore
  public Set<String> getGroupnames() {
    HashSet<String> groupnames = new HashSet<>();

    for (Set<String> values : groups.values()) {
      groupnames.addAll(values);
    }

    return groupnames;
  }

  public void setGroups(Map<PermissionType, Set<String>> groups) {
    this.groups = groups;
  }

  private Set<PermissionType> getPermissions(String name, Map<PermissionType, Set<String>> permissionMap) {
    Set<PermissionType> ret = new HashSet<>();

    for (Entry<PermissionType, Set<String>> entry : permissionMap.entrySet()) {
      if (entry.getValue().contains(name)) {
        ret.add(entry.getKey());
      }
    }

    return ret;
  }

  private void setPermissions(final String name, final Set<PermissionType> permissions,
    final Map<PermissionType, Set<String>> permissionMap) {
    Set<PermissionType> remaining = new HashSet<>(permissions);

    // update already existing permission member list
    for (Entry<PermissionType, Set<String>> entry : permissionMap.entrySet()) {
      if (permissions.contains(entry.getKey())) {
        // add user permission
        entry.getValue().add(name);

        // remove from remaining
        remaining.remove(entry.getKey());
      } else {
        // remove user permission
        entry.getValue().remove(name);
      }
    }

    // create non-existing permission member list
    for (PermissionType permissionType : remaining) {
      Set<String> members = new HashSet<>();
      members.add(name);
      permissionMap.put(permissionType, members);
    }
  }

  public Set<PermissionType> getUserPermissions(String username) {
    return getPermissions(username, users);
  }

  public void setUserPermissions(final String username, final Set<PermissionType> permissions) {
    setPermissions(username, permissions, users);
  }

  public Set<PermissionType> getGroupPermissions(String groupname) {
    return getPermissions(groupname, groups);
  }

  public void setGroupPermissions(String groupname, Set<PermissionType> permissions) {
    setPermissions(groupname, permissions, groups);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((groups == null) ? 0 : groups.hashCode());
    result = prime * result + ((users == null) ? 0 : users.hashCode());
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
    Permissions other = (Permissions) obj;
    if (groups == null) {
      if (other.groups != null)
        return false;
    } else if (!groups.equals(other.groups))
      return false;
    if (users == null) {
      if (other.users != null)
        return false;
    } else if (!users.equals(other.users))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Permissions [users=" + users + ", groups=" + groups + "]";
  }

}
