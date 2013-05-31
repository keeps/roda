package pt.gov.dgarq.roda.core.data.adapter.filter;

/**
 * @author Rui Castro
 */
public class RangeFilterParameter extends FilterParameter {
	private static final long serialVersionUID = -2923383960685420739L;

	private String fromValue;
	private String toValue;

	/**
	 * Constructs an empty {@link RangeFilterParameter}.
	 */
	public RangeFilterParameter() {
	}

	/**
	 * Constructs a {@link RangeFilterParameter} cloning an existing
	 * {@link RangeFilterParameter}.
	 * 
	 * @param rangeFilterParameter
	 *            the {@link RangeFilterParameter} to clone.
	 */
	public RangeFilterParameter(RangeFilterParameter rangeFilterParameter) {
		this(rangeFilterParameter.getName(), rangeFilterParameter
				.getFromValue(), rangeFilterParameter.getToValue());
	}

	/**
	 * Constructs a {@link RangeFilterParameter} with the given parameters.
	 * 
	 * @param name
	 * @param fromValue
	 * @param toValue
	 */
	public RangeFilterParameter(String name, String fromValue, String toValue) {
		setName(name);
		setFromValue(fromValue);
		setToValue(toValue);
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "RangeFilterParameter(name=" + getName() + ", fromValue="
				+ getFromValue() + ", toValue=" + getToValue() + ")";
	}

	/**
	 * @see FilterParameter#equals(Object)
	 */
	public boolean equals(Object obj) {
		boolean equal = true;

		if (obj != null && obj instanceof RangeFilterParameter) {
			RangeFilterParameter other = (RangeFilterParameter) obj;
			equal = equal && super.equals(other);
			equal = equal
					&& (getFromValue() == other.getFromValue() || getFromValue()
							.equals(other.getFromValue()));
			equal = equal
					&& (getToValue() == other.getToValue() || getToValue()
							.equals(other.getToValue()));
		} else {
			equal = false;
		}

		return equal;
	}

	/**
	 * @return the fromValue
	 */
	public String getFromValue() {
		return fromValue;
	}

	/**
	 * @param fromValue
	 *            the fromValue to set
	 */
	public void setFromValue(String fromValue) {
		this.fromValue = fromValue;
	}

	/**
	 * @return the toValue
	 */
	public String getToValue() {
		return toValue;
	}

	/**
	 * @param toValue
	 *            the toValue to set
	 */
	public void setToValue(String toValue) {
		this.toValue = toValue;
	}

}
