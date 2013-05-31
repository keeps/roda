package pt.gov.dgarq.roda.servlet;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.util.DateParser;

import pt.gov.dgarq.roda.core.data.User;

/**
 * This is a User that is registered in an LDAP server. This class defines the
 * methods to authenticate a user and get the user's attributes.
 * 
 * @author Rui Castro
 */
public class UserPrincipal extends User implements Principal {

	private static final long serialVersionUID = -3820175979410549280L;

	// static final private Logger logger = Logger
	// .getLogger(LdapUserPrincipal.class);

	/**
	 * Constructs a new {@link UserPrincipal} from an existing User. The new
	 * LdapUserPrincipal will have the same attributes as the User and it will
	 * be a {@link Principal}.
	 * 
	 * @param user
	 */
	public UserPrincipal(User user) {
		super(user);
	}

	/**
	 * @see User#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + super.toString()
				+ ", attributeMap=" + getAttributeMap() + ")";
	}

	/**
	 * @return the attributeMap
	 */
	public Map<String, Set<String>> getAttributeMap() {

		Map<String, Set<String>> attributeMap = new HashMap<String, Set<String>>();

		if (getName() != null) {
			getAttributeMapSet(attributeMap, "name").add(getName());
		}
		if (getFullName() != null) {
			getAttributeMapSet(attributeMap, "fullName").add(getFullName());
		}

		if (getIdDocumentType() != null) {
			getAttributeMapSet(attributeMap, "idDocumentType").add(
					getIdDocumentType());
		}
		if (getIdDocument() != null) {
			getAttributeMapSet(attributeMap, "idDocument").add(getIdDocument());
		}
		if (getIdDocumentLocation() != null) {
			getAttributeMapSet(attributeMap, "idDocumentLocation").add(
					getIdDocumentLocation());
		}
		if (getIdDocumentDate() != null) {
			getAttributeMapSet(attributeMap, "idDocumentDate").add(
					DateParser.getIsoDate(getIdDocumentDate()));
		}

		if (getBirthCountry() != null) {
			getAttributeMapSet(attributeMap, "birthCountry").add(
					getBirthCountry());
		}

		if (getPostalAddress() != null) {
			getAttributeMapSet(attributeMap, "postalAdress").add(
					getPostalAddress());
		}
		if (getPostalCode() != null) {
			getAttributeMapSet(attributeMap, "postalCode").add(getPostalCode());
		}
		if (getLocalityName() != null) {
			getAttributeMapSet(attributeMap, "localityName").add(
					getLocalityName());
		}
		if (getCountryName() != null) {
			getAttributeMapSet(attributeMap, "countryName").add(
					getCountryName());
		}
		if (getTelephoneNumber() != null) {
			getAttributeMapSet(attributeMap, "telephoneNumber").add(
					getTelephoneNumber());
		}
		if (getFax() != null) {
			getAttributeMapSet(attributeMap, "fax").add(getFax());
		}
		if (getEmail() != null) {
			getAttributeMapSet(attributeMap, "email").add(getEmail());
		}
		
		if (getBusinessCategory() != null) {
			getAttributeMapSet(attributeMap, "businessCategory").add(
					getBusinessCategory());
		}

		getAttributeMapSet(attributeMap, "groups").addAll(
				new HashSet<String>(Arrays.asList(getAllGroups())));

		getAttributeMapSet(attributeMap, "roles").addAll(
				new HashSet<String>(Arrays.asList(getRoles())));

		// The roles are also fedoraRole's
		attributeMap.put(LdapAuthenticationFilter.FEDORA_ROLE,
				getAttributeMapSet(attributeMap, "roles"));

		return attributeMap;
	}

	private Set<String> getAttributeMapSet(
			Map<String, Set<String>> attributeMap, String key) {
		if (attributeMap.get(key) == null) {
			attributeMap.put(key, new HashSet<String>());
		}
		return attributeMap.get(key);
	}

	/**
	 * @return the attributeMap for fedora
	 */
	public Map<String, String[]> getFedoraAttributeMap() {

		Map<String, String[]> fedoraAttributeMap = new HashMap<String, String[]>();

		Map<String, Set<String>> attributeMap = getAttributeMap();

		for (String attributeName : attributeMap.keySet()) {

			Set<String> values = attributeMap.get(attributeName);

			if (values != null) {
				fedoraAttributeMap.put(attributeName, values
						.toArray(new String[values.size()]));
			}
		}

		return fedoraAttributeMap;
	}
}
