package org.roda.core.data.v2.aip;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MembersLookupRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private Set<String> usernames;
  private Set<String> groupNames;

  public MembersLookupRequest() {
    this.usernames = new HashSet<>();
    this.groupNames = new HashSet<>();
  }

  public MembersLookupRequest(Set<String> usernames, Set<String> groupNames) {
    this.usernames = usernames != null ? usernames : new HashSet<>();
    this.groupNames = groupNames != null ? groupNames : new HashSet<>();
  }

  public Set<String> getUsernames() {
    return usernames;
  }

  public void setUsernames(Set<String> usernames) {
    this.usernames = usernames;
  }

  public Set<String> getGroupNames() {
    return groupNames;
  }

  public void setGroupNames(Set<String> groupNames) {
    this.groupNames = groupNames;
  }
}
