package pt.gov.dgarq.roda.core.data.v2;

import java.util.HashSet;
import java.util.Set;

public class RodaUser extends RodaSimpleUser {
	private static final long serialVersionUID = -718342371831706371L;

	private Set<String> roles = new HashSet<String>();
	private Set<String> directRoles = new HashSet<String>();
	private Set<String> groups = new HashSet<String>();
	private Set<String> allGroups = new HashSet<String>();

	public RodaUser() {
		super();
	}

	public RodaUser(String username, String email, boolean guest) {
		super(username, email, guest);
	}

	public RodaUser(RodaSimpleUser user) {
		super(user.getUsername(), user.getEmail(), user.isGuest());
	}

	public RodaUser(String username, String email, boolean guest, Set<String> roles, Set<String> directRoles,
			Set<String> groups, Set<String> allGroups) {
		super(username, email, guest);
		this.roles = roles;
		this.directRoles = directRoles;
		this.groups = groups;
		this.allGroups = allGroups;
	}

	public RodaUser(RodaSimpleUser user, Set<String> roles, Set<String> directRoles, Set<String> groups,
			Set<String> allGroups) {
		super(user.getUsername(), user.getEmail(), user.isGuest());
		this.roles = roles;
		this.directRoles = directRoles;
		this.groups = groups;
		this.allGroups = allGroups;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public Set<String> getDirectRoles() {
		return directRoles;
	}

	public void setDirectRoles(Set<String> directRoles) {
		this.directRoles = directRoles;
	}

	public Set<String> getGroups() {
		return groups;
	}

	public void setGroups(Set<String> groups) {
		this.groups = groups;
	}

	public Set<String> getAllGroups() {
		return allGroups;
	}

	public void setAllGroups(Set<String> allGroups) {
		this.allGroups = allGroups;
	}

	@Override
	public String toString() {
		return "RodaUser [roles=" + roles + ", directRoles=" + directRoles + ", groups=" + groups + ", allGroups="
				+ allGroups + ", getUsername()=" + getUsername() + ", getEmail()=" + getEmail() + ", isGuest()="
				+ isGuest() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((allGroups == null) ? 0 : allGroups.hashCode());
		result = prime * result + ((directRoles == null) ? 0 : directRoles.hashCode());
		result = prime * result + ((groups == null) ? 0 : groups.hashCode());
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof RodaUser)) {
			return false;
		}
		RodaUser other = (RodaUser) obj;
		if (allGroups == null) {
			if (other.allGroups != null) {
				return false;
			}
		} else if (!allGroups.equals(other.allGroups)) {
			return false;
		}
		if (directRoles == null) {
			if (other.directRoles != null) {
				return false;
			}
		} else if (!directRoles.equals(other.directRoles)) {
			return false;
		}
		if (groups == null) {
			if (other.groups != null) {
				return false;
			}
		} else if (!groups.equals(other.groups)) {
			return false;
		}
		if (roles == null) {
			if (other.roles != null) {
				return false;
			}
		} else if (!roles.equals(other.roles)) {
			return false;
		}
		return true;
	}
}
