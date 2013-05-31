package pt.gov.dgarq.roda.core.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Rui Castro
 */
public class RODAObjectPermissions implements Serializable {
	private static final long serialVersionUID = -3534275853026959624L;

	private String objectPID = null;

	private Set<String> readUsers = new HashSet<String>();
	private Set<String> readGroups = new HashSet<String>();

	private Set<String> modifyUsers = new HashSet<String>();
	private Set<String> modifyGroups = new HashSet<String>();

	private Set<String> removeUsers = new HashSet<String>();
	private Set<String> removeGroups = new HashSet<String>();

	private Set<String> grantUsers = new HashSet<String>();
	private Set<String> grantGroups = new HashSet<String>();

	/**
	 * Constructs an empty {@link RODAObjectPermissions}.
	 */
	public RODAObjectPermissions() {
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "RODAObjectPermissions(objectPID=" + getObjectPID()
				+ ", readUsers=" + readUsers + ", readGroups=" + readGroups
				+ ", modifyUsers=" + modifyUsers + ", modifyGroups="
				+ modifyGroups + ", removeUsers=" + removeUsers
				+ ", removeGroups=" + removeGroups + ", grantUsers="
				+ grantUsers + ", grantGroups=" + grantGroups + ")";
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof RODAObjectPermissions) {
			RODAObjectPermissions other = (RODAObjectPermissions) obj;
			return (getObjectPID() == other.getObjectPID() || getObjectPID()
					.equals(other.getObjectPID()))
					&& readUsers.equals(other.readUsers)
					&& readGroups.equals(other.readGroups)
					&& modifyUsers.equals(other.modifyUsers)
					&& modifyGroups.equals(other.modifyGroups)
					&& removeUsers.equals(other.removeUsers)
					&& removeGroups.equals(other.removeGroups)
					&& grantUsers.equals(other.grantUsers)
					&& grantGroups.equals(other.grantGroups);
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
	 *            the objectPID to set
	 */
	public void setObjectPID(String objectPID) {
		this.objectPID = objectPID;
	}

	/**
	 * @return the readUsers
	 */
	public String[] getReadUsers() {
		return (String[]) readUsers.toArray(new String[readUsers.size()]);
	}

	/**
	 * @param readUsers
	 *            the readUsers to set
	 */
	public void setReadUsers(String[] readUsers) {
		this.readUsers.clear();
		if (readUsers != null) {
			this.readUsers.addAll(Arrays.asList(readUsers));
		}
	}

	/**
	 * @param readUsers
	 *            the readUsers to add
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
	public String[] getReadGroups() {
		return (String[]) readGroups.toArray(new String[readGroups.size()]);
	}

	/**
	 * @param readGroups
	 *            the readGroups to set
	 */
	public void setReadGroups(String[] readGroups) {
		this.readGroups.clear();
		if (readGroups != null) {
			this.readGroups.addAll(Arrays.asList(readGroups));
		}
	}

	/**
	 * @param readGroups
	 *            the readGroups to add
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
	public String[] getModifyUsers() {
		return (String[]) modifyUsers.toArray(new String[modifyUsers.size()]);
	}

	/**
	 * @param modifyUsers
	 *            the modifyUsers to set
	 */
	public void setModifyUsers(String[] modifyUsers) {
		this.modifyUsers.clear();
		if (modifyUsers != null) {
			this.modifyUsers.addAll(Arrays.asList(modifyUsers));
		}
	}

	/**
	 * @param modifyUsers
	 *            the modifyUsers to add
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
	public String[] getModifyGroups() {
		return (String[]) modifyGroups.toArray(new String[modifyGroups.size()]);
	}

	/**
	 * @param modifyGroups
	 *            the modifyGroups to set
	 */
	public void setModifyGroups(String[] modifyGroups) {
		this.modifyGroups.clear();
		if (modifyGroups != null) {
			this.modifyGroups.addAll(Arrays.asList(modifyGroups));
		}
	}

	/**
	 * @param modifyGroups
	 *            the modifyGroups to add
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
	public String[] getRemoveUsers() {
		return (String[]) removeUsers.toArray(new String[removeUsers.size()]);
	}

	/**
	 * @param removeUsers
	 *            the removeUsers to set
	 */
	public void setRemoveUsers(String[] removeUsers) {
		this.removeUsers.clear();
		if (removeUsers != null) {
			this.removeUsers.addAll(Arrays.asList(removeUsers));
		}
	}

	/**
	 * @param removeUsers
	 *            the removeUsers to add
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
	public String[] getRemoveGroups() {
		return (String[]) removeGroups.toArray(new String[removeGroups.size()]);
	}

	/**
	 * @param removeGroups
	 *            the removeGroups to set
	 */
	public void setRemoveGroups(String[] removeGroups) {
		this.removeGroups.clear();
		if (removeGroups != null) {
			this.removeGroups.addAll(Arrays.asList(removeGroups));
		}
	}

	/**
	 * @param removeGroups
	 *            the removeGroups to add
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
	public String[] getGrantUsers() {
		return (String[]) grantUsers.toArray(new String[grantUsers.size()]);
	}

	/**
	 * @param grantUsers
	 *            the grantUsers to set
	 */
	public void setGrantUsers(String[] grantUsers) {
		this.grantUsers.clear();
		if (grantUsers != null) {
			this.grantUsers.addAll(Arrays.asList(grantUsers));
		}
	}

	/**
	 * @param grantUsers
	 *            the grantUsers to add
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
	public String[] getGrantGroups() {
		return (String[]) grantGroups.toArray(new String[grantGroups.size()]);
	}

	/**
	 * @param grantGroups
	 *            the grantGroups to set
	 */
	public void setGrantGroups(String[] grantGroups) {
		this.grantGroups.clear();
		addGrantGroups(grantGroups);
		if (grantGroups != null) {
			this.grantGroups.addAll(Arrays.asList(grantGroups));
		}
	}

	/**
	 * @param grantGroups
	 *            the grantGroups to add
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


}
