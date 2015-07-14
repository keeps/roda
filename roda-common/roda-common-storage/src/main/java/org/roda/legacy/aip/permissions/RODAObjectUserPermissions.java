package org.roda.legacy.aip.permissions;

import java.io.Serializable;

/**
 * @author Rui Castro
 */
public class RODAObjectUserPermissions implements Serializable {
	private static final long serialVersionUID = 2070347387398900052L;

	private String objectID = null;
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
	@Override
	public String toString() {
		return "RODAObjectUserPermissions(objectID=" + getObjectID()
				+ ", username=" + getUsername() + ", modify=" + getModify()
				+ ", remove=" + getRemove() + ", grant=" + getGrant() + ")";
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RODAObjectUserPermissions) {
			RODAObjectUserPermissions other = (RODAObjectUserPermissions) obj;
			return (getObjectID() == other.getObjectID() || getObjectID()
					.equals(other.getObjectID()))
					&& (getUsername() == other.getUsername() || getUsername()
							.equals(other.getUsername()))
					&& getModify() == other.getModify()
					&& getRemove() == other.getRemove()
					&& getGrant() == other.getGrant();
		} else {
			return false;
		}
	}

	/**
	 * @return the objectID
	 */
	public String getObjectID() {
		return objectID;
	}

	/**
	 * @param objectID
	 *            the objectID to set
	 */
	public void setObjectID(String objectID) {
		this.objectID = objectID;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
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
	 *            the modify to set
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
	 *            the remove to set
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
	 *            the grant to set
	 */
	public void setGrant(boolean grant) {
		this.grant = grant;
	}
}
