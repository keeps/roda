/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Rui Castro
 * @author Luis Faria <lfaria@keep.pt>
 */
public class AIPPermissions implements Serializable {
  private static final long serialVersionUID = -3534275853026959624L;

  private Set<String> insertUsers = new HashSet<String>();
  private Set<String> insertGroups = new HashSet<String>();

  private Set<String> readUsers = new HashSet<String>();
  private Set<String> readGroups = new HashSet<String>();

  private Set<String> modifyUsers = new HashSet<String>();
  private Set<String> modifyGroups = new HashSet<String>();

  private Set<String> removeUsers = new HashSet<String>();
  private Set<String> removeGroups = new HashSet<String>();

  private Set<String> grantUsers = new HashSet<String>();
  private Set<String> grantGroups = new HashSet<String>();

  /**
   * Constructs an empty {@link AIPPermissions}.
   */
  public AIPPermissions() {
  }

  /**
   * @return the insertUsers
   */
  public Set<String> getInsertUsers() {
    return insertUsers;
  }

  /**
   * @param insertUsers
   *          the insertUsers to set
   */
  public void setInsertUsers(Collection<String> insertUsers) {
    this.insertUsers.clear();
    if (insertUsers != null) {
      this.insertUsers.addAll(insertUsers);
    }
  }

  /**
   * @param insertUsers
   *          the insertUsers to add
   */
  public void addInsertUsers(String[] insertUsers) {
    if (insertUsers != null) {
      this.insertUsers.addAll(Arrays.asList(insertUsers));
    }
  }

  /**
   * Remove a user from the list of insertUsers
   * 
   * @param username
   */
  public void removeInsertUser(String username) {
    this.insertUsers.remove(username);
  }

  /**
   * @return the insertGroups
   */
  public Set<String> getInsertGroups() {
    return insertGroups;
  }

  /**
   * @param insertGroups
   *          the insertGroups to set
   */
  public void setInsertGroups(Collection<String> insertGroups) {
    this.insertGroups.clear();
    if (insertGroups != null) {
      this.insertGroups.addAll(insertGroups);
    }
  }

  /**
   * @param insertGroups
   *          the insertGroups to add
   */
  public void addInsertGroups(String[] insertGroups) {
    if (insertUsers != null) {
      this.insertGroups.addAll(Arrays.asList(insertGroups));
    }
  }

  /**
   * Remove a group from the list of insertGroups
   * 
   * @param groupname
   */
  public void removeInsertGroup(String groupname) {
    this.insertGroups.remove(groupname);
  }

  /**
   * @return the readUsers
   */
  public Set<String> getReadUsers() {
    return readUsers;
  }

  /**
   * @param readUsers
   *          the readUsers to set
   */
  public void setReadUsers(Collection<String> readUsers) {
    this.readUsers.clear();
    if (readUsers != null) {
      this.readUsers.addAll(readUsers);
    }
  }

  /**
   * @param readUsers
   *          the readUsers to add
   */
  public void addReadUsers(String[] readUsers) {
    if (readUsers != null) {
      this.readUsers.addAll(Arrays.asList(readUsers));
    }
  }

  /**
   * Remove a user from the list of readUsers
   * 
   * @param username
   */
  public void removeReadUser(String username) {
    this.readUsers.remove(username);
  }

  /**
   * @return the readGroups
   */
  public Set<String> getReadGroups() {
    return readGroups;
  }

  /**
   * @param readGroups
   *          the readGroups to set
   */
  public void setReadGroups(Collection<String> readGroups) {
    this.readGroups.clear();
    if (readGroups != null) {
      this.readGroups.addAll(readGroups);
    }
  }

  /**
   * @param readGroups
   *          the readGroups to add
   */
  public void addReadGroups(String[] readGroups) {
    if (readUsers != null) {
      this.readGroups.addAll(Arrays.asList(readGroups));
    }
  }

  /**
   * Remove a group from the list of readGroups
   * 
   * @param groupname
   */
  public void removeReadGroup(String groupname) {
    this.readGroups.remove(groupname);
  }

  /**
   * @return the modifyUsers
   */
  public Set<String> getModifyUsers() {
    return modifyUsers;
  }

  /**
   * @param modifyUsers
   *          the modifyUsers to set
   */
  public void setModifyUsers(Collection<String> modifyUsers) {
    this.modifyUsers.clear();
    if (modifyUsers != null) {
      this.modifyUsers.addAll(modifyUsers);
    }
  }

  /**
   * @param modifyUsers
   *          the modifyUsers to add
   */
  public void addModifyUsers(String[] modifyUsers) {
    if (modifyUsers != null) {
      this.modifyUsers.addAll(Arrays.asList(modifyUsers));
    }
  }

  /**
   * Remove a user from the list of modifyUsers
   * 
   * @param username
   */
  public void removeModifyUser(String username) {
    this.modifyUsers.remove(username);
  }

  /**
   * @return the modifyGroups
   */
  public Set<String> getModifyGroups() {
    return modifyGroups;
  }

  /**
   * @param modifyGroups
   *          the modifyGroups to set
   */
  public void setModifyGroups(Collection<String> modifyGroups) {
    this.modifyGroups.clear();
    if (modifyGroups != null) {
      this.modifyGroups.addAll(modifyGroups);
    }
  }

  /**
   * @param modifyGroups
   *          the modifyGroups to add
   */
  public void addModifyGroups(String[] modifyGroups) {
    if (modifyGroups != null) {
      this.modifyGroups.addAll(Arrays.asList(modifyGroups));
    }
  }

  /**
   * Remove a group from the list of modifyGroups
   * 
   * @param groupname
   */
  public void removeModifyGroup(String groupname) {
    this.modifyGroups.remove(groupname);
  }

  /**
   * @return the removeUsers
   */
  public Set<String> getRemoveUsers() {
    return removeUsers;
  }

  /**
   * @param removeUsers
   *          the removeUsers to set
   */
  public void setRemoveUsers(Collection<String> removeUsers) {
    this.removeUsers.clear();
    if (removeUsers != null) {
      this.removeUsers.addAll(removeUsers);
    }
  }

  /**
   * @param removeUsers
   *          the removeUsers to add
   */
  public void addRemoveUsers(String[] removeUsers) {
    if (removeUsers != null) {
      this.removeUsers.addAll(Arrays.asList(removeUsers));
    }
  }

  /**
   * Remove a user from the list of removeUsers
   * 
   * @param username
   */
  public void removeRemoveUser(String username) {
    this.removeUsers.remove(username);
  }

  /**
   * @return the removeGroups
   */
  public Set<String> getRemoveGroups() {
    return removeGroups;
  }

  /**
   * @param removeGroups
   *          the removeGroups to set
   */
  public void setRemoveGroups(Collection<String> removeGroups) {
    this.removeGroups.clear();
    if (removeGroups != null) {
      this.removeGroups.addAll(removeGroups);
    }
  }

  /**
   * @param removeGroups
   *          the removeGroups to add
   */
  public void addRemoveGroups(String[] removeGroups) {
    if (removeGroups != null) {
      this.removeGroups.addAll(Arrays.asList(removeGroups));
    }
  }

  /**
   * Remove a group from the list of removeGroups
   * 
   * @param groupname
   */
  public void removeRemoveGroup(String groupname) {
    this.removeGroups.remove(groupname);
  }

  /**
   * @return the grantUsers
   */
  public Set<String> getGrantUsers() {
    return grantUsers;
  }

  /**
   * @param grantUsers
   *          the grantUsers to set
   */
  public void setGrantUsers(Collection<String> grantUsers) {
    this.grantUsers.clear();
    if (grantUsers != null) {
      this.grantUsers.addAll(grantUsers);
    }
  }

  /**
   * @param grantUsers
   *          the grantUsers to add
   */
  public void addGrantUsers(String[] grantUsers) {
    if (grantUsers != null) {
      this.grantUsers.addAll(Arrays.asList(grantUsers));
    }
  }

  /**
   * Remove a user from the list of grantUsers
   * 
   * @param username
   */
  public void removeGrantUser(String username) {
    this.grantUsers.remove(username);
  }

  /**
   * @return the grantGroups
   */
  public Set<String> getGrantGroups() {
    return grantGroups;
  }

  /**
   * @param grantGroups
   *          the grantGroups to set
   */
  public void setGrantGroups(Collection<String> grantGroups) {
    this.grantGroups.clear();
    if (grantGroups != null) {
      this.grantGroups.addAll(grantGroups);
    }
  }

  /**
   * @param grantGroups
   *          the grantGroups to add
   */
  public void addGrantGroups(String[] grantGroups) {
    if (grantGroups != null) {
      this.grantGroups.addAll(Arrays.asList(grantGroups));
    }
  }

  /**
   * Remove a group from the list of grantGroups
   * 
   * @param groupname
   */
  public void removeGrantGroup(String groupname) {
    this.grantGroups.remove(groupname);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((grantGroups == null) ? 0 : grantGroups.hashCode());
    result = prime * result + ((grantUsers == null) ? 0 : grantUsers.hashCode());
    result = prime * result + ((insertGroups == null) ? 0 : insertGroups.hashCode());
    result = prime * result + ((insertUsers == null) ? 0 : insertUsers.hashCode());
    result = prime * result + ((modifyGroups == null) ? 0 : modifyGroups.hashCode());
    result = prime * result + ((modifyUsers == null) ? 0 : modifyUsers.hashCode());
    result = prime * result + ((readGroups == null) ? 0 : readGroups.hashCode());
    result = prime * result + ((readUsers == null) ? 0 : readUsers.hashCode());
    result = prime * result + ((removeGroups == null) ? 0 : removeGroups.hashCode());
    result = prime * result + ((removeUsers == null) ? 0 : removeUsers.hashCode());
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
    AIPPermissions other = (AIPPermissions) obj;
    if (grantGroups == null) {
      if (other.grantGroups != null)
        return false;
    } else if (!grantGroups.equals(other.grantGroups))
      return false;
    if (grantUsers == null) {
      if (other.grantUsers != null)
        return false;
    } else if (!grantUsers.equals(other.grantUsers))
      return false;
    if (insertGroups == null) {
      if (other.insertGroups != null)
        return false;
    } else if (!insertGroups.equals(other.insertGroups))
      return false;
    if (insertUsers == null) {
      if (other.insertUsers != null)
        return false;
    } else if (!insertUsers.equals(other.insertUsers))
      return false;
    if (modifyGroups == null) {
      if (other.modifyGroups != null)
        return false;
    } else if (!modifyGroups.equals(other.modifyGroups))
      return false;
    if (modifyUsers == null) {
      if (other.modifyUsers != null)
        return false;
    } else if (!modifyUsers.equals(other.modifyUsers))
      return false;
    if (readGroups == null) {
      if (other.readGroups != null)
        return false;
    } else if (!readGroups.equals(other.readGroups))
      return false;
    if (readUsers == null) {
      if (other.readUsers != null)
        return false;
    } else if (!readUsers.equals(other.readUsers))
      return false;
    if (removeGroups == null) {
      if (other.removeGroups != null)
        return false;
    } else if (!removeGroups.equals(other.removeGroups))
      return false;
    if (removeUsers == null) {
      if (other.removeUsers != null)
        return false;
    } else if (!removeUsers.equals(other.removeUsers))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "AIPPermissions [insertUsers=" + insertUsers + ", insertGroups=" + insertGroups + ", readUsers=" + readUsers
      + ", readGroups=" + readGroups + ", modifyUsers=" + modifyUsers + ", modifyGroups=" + modifyGroups
      + ", removeUsers=" + removeUsers + ", removeGroups=" + removeGroups + ", grantUsers=" + grantUsers
      + ", grantGroups=" + grantGroups + "]";
  }
}
