/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.model.structure.type;

/**
 * Date and time according to ISO 8601.
 * 
 * @author Luis Faria
 */
public class SimpleTypeDateTime extends Type {

	private Boolean timeDefined;

	private Boolean timeZoneDefined;

	/**
	 * DateTime type constructor. All fields are required.
	 * 
	 * @param timeDefined
	 *            If time is defined in date time declaration, i.e. hour,
	 *            minutes, seconds or milliseconds.
	 * @param timeZoneDefined
	 *            If time zone is defined in date time declaration.
	 */
	public SimpleTypeDateTime(Boolean timeDefined, Boolean timeZoneDefined) {
		this.timeDefined = timeDefined;
		this.timeZoneDefined = timeZoneDefined;
	}

	/**
	 * @return If time is defined in date time declaration, i.e. hour, minutes,
	 *         seconds or milliseconds.
	 */
	public Boolean getTimeDefined() {
		return timeDefined;
	}

	/**
	 * @param timeDefined
	 *            If time is defined in date time declaration, i.e. hour,
	 *            minutes, seconds or milliseconds.
	 */
	public void setTimeDefined(Boolean timeDefined) {
		this.timeDefined = timeDefined;
	}

	/**
	 * @return If time zone is defined in date time declaration.
	 */
	public Boolean getTimeZoneDefined() {
		return timeZoneDefined;
	}

	/**
	 * @param timeZoneDefined
	 *            If time zone is defined in date time declaration.
	 */
	public void setTimeZoneDefined(Boolean timeZoneDefined) {
		this.timeZoneDefined = timeZoneDefined;
	}

}
