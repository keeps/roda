/**
 * 
 */
package pt.gov.dgarq.roda.common.convert.db.model.exception;

/**
 * @author Luis Faria
 * 
 */
public class InvalidDataException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5706032629612775911L;

	/**
	 * Invalid data exception empty constructor
	 * 
	 */
	public InvalidDataException() {
		super();
	}

	/**
	 * Invalid data exception constructior
	 * 
	 * @param mesg
	 *            error message
	 */
	public InvalidDataException(String mesg) {
		super(mesg);
	}

}
