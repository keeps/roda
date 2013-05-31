package pt.gov.dgarq.roda.common.convert.db.model.structure.type;

/**
 * An approximate numeric is essentially a floating point and for each a
 * precision may be optionally specified.
 * 
 * @author Luis Faria
 * 
 */
public class SimpleTypeNumericApproximate extends Type {

	private Integer precision;

	/**
	 * Aproximate numeric, like floating point
	 * 
	 */
	public SimpleTypeNumericApproximate() {

	}

	/**
	 * Exact numeric, like floating point, with optional fields.
	 * 
	 * @param precision
	 *            the number of digits (optional)
	 * 
	 */
	public SimpleTypeNumericApproximate(int precision) {
		this.precision = precision;
	}

	/**
	 * @return the number of digits, or null if undefined
	 */
	public Integer getPrecision() {
		return precision;
	}

	/**
	 * @param precision
	 *            the number of digits, or null if undefined
	 */
	public void setPrecision(Integer precision) {
		this.precision = precision;
	}

}
