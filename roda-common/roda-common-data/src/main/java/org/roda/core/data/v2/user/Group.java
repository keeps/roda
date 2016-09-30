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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This is a group in RODA.
 * 
 * @author Rui Castro
 * @author Luis Faria <lfaria@keep.pt>
 */
public class Group extends RodaPrincipal {

  private static final long serialVersionUID = -4051946961307715630L;

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
  public Group(String name) {
    super(name, name);
  }

  /**
   * Constructs a new Group cloning a given Group.
   * 
   * @param group
   *          the Group to be cloned.
   */
  public Group(Group group) {
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
  public void setUsers(Set<String> users) {
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
  public boolean addMemberUser(String memberUserName) {
    return this.users.add(memberUserName);
  }

  /**
   * Removes a member name to the list of member names.
   * 
   * @param memberUserName
   *          the name of the member to remove.
   * @return true if the member was removed, false otherwise.
   */
  public boolean removeMemberUser(String memberUserName) {
    return this.users.remove(memberUserName);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Group [");
    if (users != null) {
      builder.append("users=");
      builder.append(users);
    }
    builder.append("]");
    return builder.toString();
  }

  @Override
  public String[] toCsvHeaders() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] toCsvValues() {
    throw new UnsupportedOperationException();
  }

}
