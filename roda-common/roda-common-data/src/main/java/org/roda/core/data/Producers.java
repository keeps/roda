/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This is the list of users and groups that can be producers of a specific
 * {@link DescriptionObject} (fonds).
 * 
 * @author Rui Castro
 */
public class Producers implements Serializable {
  private static final long serialVersionUID = -3607302592939240478L;

  private String descriptionObjectPID = null;

  private Set<String> users = new HashSet<String>();
  private Set<String> groups = new HashSet<String>();

  /**
   * Constructs a new instance of {@link Producers}.
   */
  public Producers() {
  }

  /**
   * Constructs a new instance of {@link Producers} with the given users and
   * groups.
   * 
   * @param doPID
   *          the PID of the fonds description object
   * @param users
   *          the users
   * @param groups
   *          the groups.
   */
  public Producers(String doPID, String[] users, String[] groups) {
    setDescriptionObjectPID(doPID);
    setUsers(users);
    setGroups(groups);
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof Producers) {
      Producers other = (Producers) obj;
      return (getDescriptionObjectPID() == other.getDescriptionObjectPID() || getDescriptionObjectPID().equals(
        other.getDescriptionObjectPID()))
        && users.equals(other.users) && groups.equals(other.groups);
    } else {
      return false;
    }
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "Producers(descriptionObjectPID=" + getDescriptionObjectPID() + ", users=" + users + ", groups=" + groups
      + ")";
  }

  /**
   * @return the descriptionObjectPID
   */
  public String getDescriptionObjectPID() {
    return descriptionObjectPID;
  }

  /**
   * @param descriptionObjectPID
   *          the descriptionObjectPID to set
   */
  public void setDescriptionObjectPID(String descriptionObjectPID) {
    this.descriptionObjectPID = descriptionObjectPID;
  }

  /**
   * @return the users
   */
  public String[] getUsers() {
    return (String[]) users.toArray(new String[users.size()]);
  }

  /**
   * @param users
   *          the users to set
   */
  public void setUsers(String[] users) {
    this.users.clear();
    addUsers(users);
  }

  /**
   * @param users
   *          the users to add
   */
  public void addUsers(String[] users) {
    if (users != null) {
      this.users.addAll(Arrays.asList(users));
    }
  }

  /**
   * @return the groups
   */
  public String[] getGroups() {
    return (String[]) groups.toArray(new String[groups.size()]);
  }

  /**
   * @param groups
   *          the groups to set
   */
  public void setGroups(String[] groups) {
    this.groups.clear();
    addGroups(groups);
  }

  /**
   * @param groups
   *          the groups to add
   */
  public void addGroups(String[] groups) {
    if (groups != null) {
      this.groups.addAll(Arrays.asList(groups));
    }
  }

}
