package org.roda.core.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a RODA Member. A member can be a User or a Group.
 * 
 * @author Rui Castro
 */
public class RODAMember implements Serializable {

  private static final long serialVersionUID = 3609728486405921628L;

  // LDAP shadowInactive
  private boolean active = true;
  // LDAP uid
  private String name = null;
  // LDAP cn
  private String fullName = null;

  private Set<String> roles = new HashSet<String>();
  private Set<String> directRoles = new HashSet<String>();
  private Set<String> groups = new HashSet<String>();
  private Set<String> allGroups = new HashSet<String>();

  /**
   * Constructs an emtpy RODAMember.
   */
  public RODAMember() {
  }

  /**
   * Constructs a new RODAMember cloning an existing RODAMember.
   * 
   * @param member
   *          a RODAMember.
   */
  public RODAMember(RODAMember member) {
    this(member.isActive(), member.getName(), member.getFullName(), member.getGroups(), member.getAllGroups(), member
      .getDirectRoles(), member.getRoles());
  }

  /**
   * Constructs a new member with the given name.
   * 
   * @param active
   *          the active flag.
   * 
   * @param name
   *          the name of the new member.
   * @param fullName
   *          the fullname of the new member.
   * @param groups
   *          the groups of this member.
   * @param allGroups
   * @param directRoles
   * @param roles
   *          the roles of this member.
   */
  public RODAMember(boolean active, String name, String fullName, String[] groups, String[] allGroups,
    String[] directRoles, String[] roles) {

    setActive(active);
    setName(name);
    setFullName(fullName);

    setGroups(groups);
    setAllGroups(allGroups);

    setDirectRoles(directRoles);
    setRoles(roles);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {

    boolean equal;

    if (obj != null && obj instanceof RODAMember) {
      RODAMember otherUser = (RODAMember) obj;
      equal = getName().equals(otherUser.getName());
    } else {
      equal = false;
    }

    return equal;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "RODAMember (active=" + (isActive() ? "active" : "inactive") + ", name=" + getName() + ", fullName="
      + getFullName() + ", groups=" + Arrays.toString(getGroups()) + ", allGroups=" + Arrays.toString(getAllGroups())
      + ", directRoles=" + Arrays.toString(getDirectRoles()) + ", roles=" + Arrays.toString(getRoles()) + ")";
  }

  /**
   * @return the active
   */
  public boolean isActive() {
    return active;
  }

  /**
   * @param active
   *          the active to set
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the fullName
   */
  public String getFullName() {
    return fullName;
  }

  /**
   * @param fullName
   *          the fullName to set
   */
  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  /**
   * Returns the roles assigned to this user.
   * 
   * @return the roles
   */
  // FIXME change array to list
  public String[] getRoles() {
    return (String[]) roles.toArray(new String[roles.size()]);
  }

  /**
   * Sets the Set of roles assigned to this user.
   * 
   * @param roles
   *          the roles to set
   */
  public void setRoles(String[] roles) {
    this.roles.clear();
    if (roles != null) {
      this.roles.addAll(Arrays.asList(roles));
    }
  }

  /**
   * Returns the groups to which this user belongs.
   * 
   * @return an array of Strings with the names of the groups.
   */
  public String[] getGroups() {
    return (String[]) groups.toArray(new String[groups.size()]);
  }

  /**
   * Sets the Set of groups to which this user belongs.
   * 
   * @param groups
   *          the groups to set
   */
  public void setGroups(String[] groups) {
    this.groups.clear();
    if (groups != null) {
      this.groups.addAll(Arrays.asList(groups));
    }
  }

  /**
   * Adds a new group name to the list of groups.
   * 
   * @param group
   *          the name of the new group to add.
   * @return true if the group was added, false otherwise.
   */
  public boolean addGroup(String group) {
    return this.groups.add(group);
  }

  /**
   * Removes a group name to the list of groups.
   * 
   * @param group
   *          the name of the group to remove.
   * @return <code>true</code> if the group was removed, <code>false</code>
   *         otherwise.
   */
  public boolean removeGroup(String group) {
    return this.groups.remove(group);
  }

  /**
   * @return the allGroups
   */
  public String[] getAllGroups() {
    return (String[]) this.allGroups.toArray(new String[this.allGroups.size()]);
  }

  /**
   * @param allGroups
   *          the allGroups to set
   */
  public void setAllGroups(String[] allGroups) {
    this.allGroups.clear();
    if (allGroups != null) {
      this.allGroups.addAll(Arrays.asList(allGroups));
    }
  }

  /**
   * Returns <code>true</code> if this member has the specified role and
   * <code>false</code> otherwise.
   * 
   * @param role
   *          the role to verify.
   * @return <code>true</code> if this member has the specified role and
   *         <code>false</code> otherwise.
   */
  public boolean hasRole(String role) {
    return this.roles.contains(role);
  }

  /**
   * @return the directRoles
   */
  public String[] getDirectRoles() {
    return (String[]) directRoles.toArray(new String[directRoles.size()]);
  }

  /**
   * @param roles
   *          the directRoles to set
   */
  public void setDirectRoles(String[] roles) {
    this.directRoles.clear();
    if (roles != null) {
      this.directRoles.addAll(Arrays.asList(roles));
    }
  }

  /**
   * Adds a new role name to the list of roles.
   * 
   * @param role
   *          the name of the new role to add.
   * @return true if the role was added, false otherwise.
   */
  public boolean addDirectRole(String role) {
    return this.directRoles.add(role);
  }

  /**
   * Removes a role name to the list of roles.
   * 
   * @param role
   *          the name of the role to remove.
   * @return <code>true</code> if the role was removed, <code>false</code>
   *         otherwise.
   */
  public boolean removeDirectRole(String role) {
    return this.directRoles.remove(role);
  }

  /**
   * Tests if the given name is a valid {@link RODAMember} name.
   * 
   * @param name
   *          the name to test.
   * @return <code>true<code> if the name is valid,<code>false<code> otherwise.
   */
  public boolean isNameValid(String name) {
    boolean isValid = true;
    for (int i = 0; i < name.length(); i++) {
      char currentChar = name.charAt(i);
      isValid &= Character.isLetterOrDigit(currentChar) || currentChar == '.' || currentChar == '_'
        || currentChar == '-';
    }
    return isValid;
  }
}
