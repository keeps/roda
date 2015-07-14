package org.roda.legacy.aip.permissions;

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

	private String descriptionObjectID = null;

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
	 * @param doID
	 *            the ID of the fonds description object
	 * @param users
	 *            the users
	 * @param groups
	 *            the groups.
	 */
	public Producers(String doID, String[] users, String[] groups) {
		setDescriptionObjectID(doID);
		setUsers(users);
		setGroups(groups);
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Producers) {
			Producers other = (Producers) obj;
			return (getDescriptionObjectID() == other.getDescriptionObjectID() || getDescriptionObjectID()
					.equals(other.getDescriptionObjectID()))
					&& users.equals(other.users) && groups.equals(other.groups);
		} else {
			return false;
		}
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return "Producers(descriptionObjectID=" + getDescriptionObjectID()
				+ ", users=" + users + ", groups=" + groups + ")";
	}

	/**
	 * @return the descriptionObjectID
	 */
	public String getDescriptionObjectID() {
		return descriptionObjectID;
	}

	/**
	 * @param descriptionObjectID
	 *            the descriptionObjectID to set
	 */
	public void setDescriptionObjectID(String descriptionObjectID) {
		this.descriptionObjectID = descriptionObjectID;
	}

	/**
	 * @return the users
	 */
	public String[] getUsers() {
		return users.toArray(new String[users.size()]);
	}

	/**
	 * @param users
	 *            the users to set
	 */
	public void setUsers(String[] users) {
		this.users.clear();
		addUsers(users);
	}

	/**
	 * @param users
	 *            the users to add
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
		return groups.toArray(new String[groups.size()]);
	}

	/**
	 * @param groups
	 *            the groups to set
	 */
	public void setGroups(String[] groups) {
		this.groups.clear();
		addGroups(groups);
	}

	/**
	 * @param groups
	 *            the groups to add
	 */
	public void addGroups(String[] groups) {
		if (groups != null) {
			this.groups.addAll(Arrays.asList(groups));
		}
	}

}
