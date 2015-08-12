package pt.gov.dgarq.roda.core.data.adapter.filter;

import java.util.List;

/**
 * This filter match one the of the values.
 * 
 * @author Rui Castro
 */
public class OneOfManyFilterParameter extends FilterParameter {
	private static final long serialVersionUID = -8705013718226758378L;

	private List<String> values = null;

	/**
	 * Constructs an empty {@link OneOfManyFilterParameter}.
	 */
	public OneOfManyFilterParameter() {
	}

	/**
	 * Constructs a {@link OneOfManyFilterParameter} cloning an existing
	 * {@link OneOfManyFilterParameter}.
	 * 
	 * @param oneOfManyFilterParameter
	 *            the {@link OneOfManyFilterParameter} to clone.
	 */
	public OneOfManyFilterParameter(OneOfManyFilterParameter oneOfManyFilterParameter) {
		this(oneOfManyFilterParameter.getName(), oneOfManyFilterParameter.getValues());
	}

	/**
	 * Constructs a {@link OneOfManyFilterParameter} from a list of values.
	 * 
	 * @param name
	 *            the name of the attribute.
	 * @param values
	 *            the list of values for this filter.
	 */
	public OneOfManyFilterParameter(String name, List<String> values) {
		setName(name);
		setValues(values);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "OneOfManyFilterParameter(name=" + getName() + ", values=" + getValues() + ")";
	}

	/**
	 * @see FilterParameter#equals(Object)
	 */
	public boolean equals(Object obj) {
		boolean equal = true;

		if (obj != null && obj instanceof OneOfManyFilterParameter) {
			OneOfManyFilterParameter other = (OneOfManyFilterParameter) obj;
			equal = super.equals(other) && (getValues() == other.getValues() || getValues().equals(other.getValues()));
		} else {
			equal = false;
		}

		return equal;
	}

	/**
	 * @return the values
	 */
	public List<String> getValues() {
		return values;
	}

	/**
	 * @param values
	 *            the values to set
	 */
	public void setValues(List<String> values) {
		this.values = values;
	}

}
