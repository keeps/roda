/**
 * 
 */
package pt.gov.dgarq.roda.sipcreator.representation;


/**
 * @author Luis Faria
 * 
 *         Sub Type Option class (or MIME type option)
 */
public class SubTypeOption {
	private String subType;
	private String label;

	/**
	 * Create a new sub type option (or MIME type option)
	 * 
	 * @param subType
	 *            the sub type, or MIME type
	 * @param label
	 *            the label of the radio button
	 */
	public SubTypeOption(String subType,
			String label) {

		this.subType = subType;
		this.label = label;
	}

	/**
	 * Get the sub type, or MIME type
	 * 
	 * @return the sub type, or MIME type
	 */
	public String getSubType() {
		return subType;
	}

	/**
	 * Set the sub type (MIME type)
	 * 
	 * @param subType
	 *            the sub type (MIME type)
	 */
	public void setSubType(String subType) {
		this.subType = subType;
	}

	/**
	 * Get the label of the option
	 * 
	 * @return the sub type label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set the label of the option
	 * 
	 * @param label
	 *            the sub type label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

}
