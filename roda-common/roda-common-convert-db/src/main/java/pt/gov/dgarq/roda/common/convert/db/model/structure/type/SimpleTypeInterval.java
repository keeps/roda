/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.model.structure.type;

/**
 * Time intervals according to ISO 8601.
 * 
 * @author Luis Faria
 */
public class SimpleTypeInterval extends Type {

	/**
	 * In ISO 8601, there are four ways to express a time interval
	 */
	public enum IntervalType {
		/**
		 * Start and end, such as "2007-03-01T13:00:00Z/2008-05-11T15:30:00Z"
		 */
		STARTDATE_ENDDATE,
		/**
		 * Start and duration, such as "2007-03-01T13:00:00Z/P1Y2M10DT2H30M"
		 */
		STARTDATE_DURATION,
		/**
		 * Duration and end, such as "P1Y2M10DT2H30M/2008-05-11T15:30:00Z"
		 */
		DURATION_ENDDATE,
		/**
		 * Duration only, such as "P1Y2M10DT2H30M", with additional context
		 * information
		 */
		DURATION
	};

	private IntervalType type;

	/**
	 * Time Interval type constructor, all fields are required.
	 * 
	 * @param type
	 *            the type of the time interval according to ISO 8601
	 */
	public SimpleTypeInterval(IntervalType type) {
		this.type = type;
	}

	/**
	 * @return the type of the time interval according to ISO 8601
	 */
	public IntervalType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type of the time interval according to ISO 8601
	 */
	public void setType(IntervalType type) {
		this.type = type;
	}

}
