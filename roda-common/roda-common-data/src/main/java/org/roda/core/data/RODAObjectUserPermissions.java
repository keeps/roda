package org.roda.core.data;

import java.io.Serializable;

/**
 * @author Rui Castro
 */
public class RODAObjectUserPermissions implements Serializable {
  private static final long serialVersionUID = 2070347387398900052L;

  private String objectPID = null;
  private String username = null;

  private boolean modify = false;
  private boolean remove = false;
  private boolean grant = false;

  /**
   * Constructs an empty {@link RODAObjectUserPermissions}.
   */
  public RODAObjectUserPermissions() {
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "RODAObjectUserPermissions(objectPID=" + getObjectPID() + ", username=" + getUsername() + ", modify="
      + getModify() + ", remove=" + getRemove() + ", grant=" + getGrant() + ")";
  }

  /**
   * @see Object#equals(Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof RODAObjectUserPermissions) {
      RODAObjectUserPermissions other = (RODAObjectUserPermissions) obj;
      return (getObjectPID() == other.getObjectPID() || getObjectPID().equals(other.getObjectPID()))
        && (getUsername() == other.getUsername() || getUsername().equals(other.getUsername()))
        && getModify() == other.getModify() && getRemove() == other.getRemove() && getGrant() == other.getGrant();
    } else {
      return false;
    }
  }

  /**
   * @return the objectPID
   */
  public String getObjectPID() {
    return objectPID;
  }

  /**
   * @param objectPID
   *          the objectPID to set
   */
  public void setObjectPID(String objectPID) {
    this.objectPID = objectPID;
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username
   *          the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * @return the modify
   */
  public boolean getModify() {
    return modify;
  }

  /**
   * @param modify
   *          the modify to set
   */
  public void setModify(boolean modify) {
    this.modify = modify;
  }

  /**
   * @return the remove
   */
  public boolean getRemove() {
    return remove;
  }

  /**
   * @param remove
   *          the remove to set
   */
  public void setRemove(boolean remove) {
    this.remove = remove;
  }

  /**
   * @return the grant
   */
  public boolean getGrant() {
    return grant;
  }

  /**
   * @param grant
   *          the grant to set
   */
  public void setGrant(boolean grant) {
    this.grant = grant;
  }
}
