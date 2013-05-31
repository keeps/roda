package pt.gov.dgarq.roda.plugins.description;


/**
 * @author Rui Castro
 * 
 */
public class UnitdateInferenceResult implements DescriptionTraverserResult {

	private String initialDate;
	private String finalDate;

	public UnitdateInferenceResult(String initialDate, String finalDate) {
		setInitialDate(initialDate);
		setFinalDate(finalDate);
	}

	/**
	 * @return the initialDate
	 */
	public String getInitialDate() {
		return initialDate;
	}

	/**
	 * @param initialDate
	 *            the initialDate to set
	 */
	public void setInitialDate(String initialDate) {
		this.initialDate = initialDate;
	}

	/**
	 * @return the finalDate
	 */
	public String getFinalDate() {
		return finalDate;
	}

	/**
	 * @param finalDate
	 *            the finalDate to set
	 */
	public void setFinalDate(String finalDate) {
		this.finalDate = finalDate;
	}

}
