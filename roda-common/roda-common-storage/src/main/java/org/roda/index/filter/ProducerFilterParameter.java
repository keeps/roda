package org.roda.index.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.legacy.aip.metadata.descriptive.SimpleDescriptionObject;

/**
 * This filter parameter is used to filter {@link SimpleDescriptionObject}'s
 * that belong to Fonds for which the specified {@link User} is a producer.
 * 
 * @author Rui Castro
 */
public class ProducerFilterParameter extends FilterParameter {
	private static final long serialVersionUID = -26511190928007994L;

	private String username = null;
	private List<String> groups = new ArrayList<String>();

	/**
	 * Constructs an empty {@link ProducerFilterParameter}.
	 */
	public ProducerFilterParameter() {
		this((String) null);
	}

	/**
	 * Constructs a {@link ProducerFilterParameter} cloning an existing
	 * {@link ProducerFilterParameter}.
	 * 
	 * @param producerFilterParameter
	 *            the {@link ProducerFilterParameter} to clone.
	 */
	public ProducerFilterParameter(
			ProducerFilterParameter producerFilterParameter) {
		this(producerFilterParameter.getUsername());
	}

	/**
	 * Constructs a {@link ProducerFilterParameter} with the given parameters.
	 * 
	 * @param username
	 */
	public ProducerFilterParameter(String username) {
		setName("produceruser");
		setUsername(username);
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return "ProducerFilterParameter(username=" + getUsername() + ")";
	}

	/**
	 * @see FilterParameter#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean equal = true;

		if (obj != null && obj instanceof ProducerFilterParameter) {
			ProducerFilterParameter other = (ProducerFilterParameter) obj;
			equal = equal && super.equals(other);
			equal = equal
					&& (getUsername() == other.getUsername() || getUsername()
							.equals(other.getUsername()));
		} else {
			equal = false;
		}

		return equal;
	}

	/**
	 * @return the username of the producer
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username of the producer
	 */
	public void setUsername(String username) {
		this.username = username;
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
		this.groups.addAll(Arrays.asList(groups));
	}

}
