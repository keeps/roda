package pt.gov.dgarq.roda.core.data.v2;

import java.util.HashSet;
import java.util.Set;

public class RodaUser extends RodaSimpleUser {
	private Set<String> roles = new HashSet<String>();
	private Set<String> directRoles = new HashSet<String>();
	private Set<String> groups = new HashSet<String>();
	private Set<String> allGroups = new HashSet<String>();
	
	public RodaUser(RodaSimpleUser rsu) {
		setUsername(rsu.getUsername());
		setEmail(rsu.getEmail());
		setGuest(rsu.isGuest());
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
	
	

}
