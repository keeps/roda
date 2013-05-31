package pt.gov.dgarq.roda.core.ingest;

/**
 * This is the result of an {@link IngestTask}.
 * 
 * @author Rui Castro
 */
public class IngestTaskResult {

	private boolean passed = false;
	private String outcomeMessage = null;

	/**
	 * Constructs a new {@link IngestTaskResult} with the given parameters.
	 * 
	 * @param passed
	 * @param outcomeMessage
	 */
	public IngestTaskResult(boolean passed, String outcomeMessage) {
		setPassed(passed);
		setOutcomeMessage(outcomeMessage);
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "( passed=" + isPassed()
				+ ", outcomeMessage=" + getOutcomeMessage() + " )";
	}

	/**
	 * @return the passed
	 */
	public boolean isPassed() {
		return passed;
	}

	/**
	 * @param passed
	 *            the passed to set
	 */
	public void setPassed(boolean passed) {
		this.passed = passed;
	}

	/**
	 * @return the outcomeMessage
	 */
	public String getOutcomeMessage() {
		return outcomeMessage;
	}

	/**
	 * @param outcomeMessage
	 *            the outcomeMessage to set
	 */
	public void setOutcomeMessage(String outcomeMessage) {
		this.outcomeMessage = outcomeMessage;
	}

}
