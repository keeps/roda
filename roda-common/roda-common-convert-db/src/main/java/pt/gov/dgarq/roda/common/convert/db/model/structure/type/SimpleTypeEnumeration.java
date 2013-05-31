/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.model.structure.type;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A list of possible values for this field. Each value is represented by a
 * string.
 * 
 * @author Luis Faria
 * 
 */
public class SimpleTypeEnumeration extends Type {

	/**
	 * A map where the keys are the possible options and the values are the
	 * descriptions of each option
	 */
	private Map<String, String> options;

	/**
	 * Empty Enumeration type contructor
	 *
	 */
	public SimpleTypeEnumeration() {
		this.options = new HashMap<String, String>();
	}
	
	/**
	 * Enumeration type contructor
	 * 
	 * @param options
	 *            the allowed options for values of columns of this type
	 */
	public SimpleTypeEnumeration(Set<String> options) {
		this.options = new HashMap<String, String>();
		for (String option : options) {
			this.options.put(option, null);
		}
	}

	/**
	 * Enumeration type constructor, with descriptions
	 * 
	 * @param options
	 *            A map where the keys are the possible options and the values
	 *            are the descriptions of each option
	 */
	public SimpleTypeEnumeration(Map<String, String> options) {
		super();
		this.options = options;
	}

	/**
	 * @return the allowed options for values of columns of this type
	 */
	public Set<String> getOptions() {
		return options.keySet();
	}

	/**
	 * 
	 * @return A map where the keys are the possible options and the values are
	 *         the descriptions of each option
	 */
	public Map<String, String> getOptionsWithDescription() {
		return options;
	}

	/**
	 * @param options
	 *            the allowed options for values of columns of this type
	 */
	public void setOptions(Set<String> options) {
		this.options.clear();
		for (String option : options) {
			this.options.put(option, null);
		}
	}

	/**
	 * 
	 * @param options
	 *            A map where the keys are the possible options and the values
	 *            are the descriptions of each option
	 */
	public void setOptionsWithDescription(Map<String, String> options) {
		this.options = options;
	}

	/**
	 * Get the description of an option
	 * 
	 * @param option
	 *            the option
	 * @return the description or null if no description was set
	 */
	public String getOptionDescription(String option) {
		return options.get(option);
	}

	/**
	 * Set the description option. If the option doesn't exist, it will be
	 * created
	 * 
	 * @param option
	 *            the option
	 * @param description
	 *            the option description
	 */
	public void setOptionDescription(String option, String description) {
		options.remove(option);
		options.put(option, description);
	}

}
