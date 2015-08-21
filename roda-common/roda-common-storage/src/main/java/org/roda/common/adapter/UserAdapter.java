package org.roda.common.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.gov.dgarq.roda.core.adapter.jndi.JndiEntityAdapter;
import pt.gov.dgarq.roda.core.data.v2.User;

/**
 * @author Rui Castro
 */
public class UserAdapter extends RODAMemberAdapter<User> {

	private static final String[] attributeNames = new String[] { "name",
			"fullname", "iddocumenttype", "iddocument", "iddocumentlocation",
			"iddocumentdate", "birthcountry", "postaladdress", "postalcode",
			"localityname", "countryname", "telephonenumber", "fax", "email",
			"businesscategory" };

	private static final String[] jndiAttributeNames = new String[] { "uid",
			"cn", "documentTitle", "documentIdentifier", "documentLocation",
			"documentVersion", "friendlyCountryName", "postalAddress",
			"postalCode", "localityName", "countryName", "telephoneNumber",
			"fax", "email", "businessCategory" };

	/**
	 * Returns the list of attributes for the entity.
	 * 
	 * @return a {@link List} of {@link String} names of the attributes
	 *         supported.
	 */
	public List<String> getAttributeNames() {
		List<String> memberAttrs = new ArrayList<String>(super
				.getAttributeNames());
		memberAttrs.addAll(Arrays.asList(attributeNames));
		return memberAttrs;
	}

	/**
	 * Returns the list of names of the JNDI attributes that match the
	 * attributes in {@link JndiEntityAdapter#getAttributeNames()}.
	 * 
	 * @return a {@link List} of {@link String} names of the JNDI attributes
	 *         supported.
	 */
	public List<String> getJndiAttributeNames() {
		List<String> memberAttrs = new ArrayList<String>(super
				.getJndiAttributeNames());
		memberAttrs.addAll(Arrays.asList(jndiAttributeNames));
		return memberAttrs;
	}

}
