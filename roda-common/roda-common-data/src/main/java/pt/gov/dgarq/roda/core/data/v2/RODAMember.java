package pt.gov.dgarq.roda.core.data.v2;

import java.io.Serializable;
import java.util.Set;

public interface RODAMember extends Serializable {

	public boolean isActive();

	public boolean isUser();

	public String getId();

	public String getName();

	public Set<String> getAllGroups();

	public Set<String> getDirectGroups();

	public Set<String> getAllRoles();

	public Set<String> getDirectRoles();

}
