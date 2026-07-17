package org.roda.core.data.v2.aip;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MembersLookupResponse implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private Map<String, String> users;
  private Map<String, String> groups;

  /**
   * Empty constructor required by Jackson and GWT serialization.
   */
  public MembersLookupResponse() {
    this.users = new HashMap<>();
    this.groups = new HashMap<>();
  }

  public MembersLookupResponse(Map<String, String> users, Map<String, String> groups) {
    this.users = users != null ? users : new HashMap<>();
    this.groups = groups != null ? groups : new HashMap<>();
  }

  public Map<String, String> getUsers() {
    return users;
  }

  public void setUsers(Map<String, String> users) {
    this.users = users;
  }

  public Map<String, String> getGroups() {
    return groups;
  }

  public void setGroups(Map<String, String> groups) {
    this.groups = groups;
  }

  public String getUserDisplayName(String username) {
    return this.users.getOrDefault(username, username);
  }

  public String getGroupDisplayName(String groupName) {
    return this.groups.getOrDefault(groupName, groupName);
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
    MembersLookupResponse other = (MembersLookupResponse) obj;
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
    return "MembersLookupResponse [users=" + users + ", groups=" + groups + "]";
  }
}
