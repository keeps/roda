/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.user;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * This is a group in RODA.
 * 
 * @author Rui Castro
 * @author Luis Faria <lfaria@keep.pt>
 */
@XmlRootElement(name = RodaConstants.RODA_OBJECT_GROUP)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Group extends RodaPrincipal {

  private static final long serialVersionUID = -4051946961307715630L;

  /** Users that belong to this group. */
  private Set<String> users = new HashSet<>();

  /**
   * Constructs a new empty group.
   */
  public Group() {
    super();
  }

  /**
   * Constructs a new Group with the given name.
   * 
   * @param name
   *          the name of the group.
   */
  public Group(final String name) {
    super(name, name);
  }

  /**
   * Constructs a new Group cloning a given Group.
   * 
   * @param group
   *          the Group to be cloned.
   */
  public Group(final Group group) {
    super(group.getId(), group.getName(), group.getDirectRoles(), group.getAllRoles());
    setActive(true);
    setUsers(group.getUsers());
  }

  @JsonIgnore
  @Override
  public boolean isUser() {
    return false;
  }

  /**
   * @return the memberUserNames
   */
  public Set<String> getUsers() {
    return users;
  }

  /**
   * @param users
   *          the memberUserNames to set
   */
  public void setUsers(final Set<String> users) {
    this.users.clear();
    if (users != null) {
      this.users.addAll(users);
    }
  }

  /**
   * Adds a new member name to the list of member names.
   * 
   * @param memberUserName
   *          the name of the new member to add.
   * @return true if the member was added, false otherwise.
   */
  public boolean addMemberUser(final String memberUserName) {
    return this.users.add(memberUserName);
  }

  /**
   * Removes a member name to the list of member names.
   * 
   * @param memberUserName
   *          the name of the member to remove.
   * @return true if the member was removed, false otherwise.
   */
  public boolean removeMemberUser(final String memberUserName) {
    return this.users.remove(memberUserName);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((users == null) ? 0 : users.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Group other = (Group) obj;
    if (users == null) {
      if (other.users != null) {
        return false;
      }
    } else if (!users.equals(other.users)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("Group [");
    if (users != null) {
      builder.append("users=");
      builder.append(users);
    }
    builder.append("]");
    return builder.toString();
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.MEMBERS_NAME);
  }

  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 1;
  }

}
