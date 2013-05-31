/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client;

import pt.gov.dgarq.roda.core.common.RODAException;

/**
 * @author Luis Faria
 * 
 */
public class BadHistoryTokenException extends RODAException {

	/**
	 * Create a new bad history token exception
	 */
	public BadHistoryTokenException() {
		super();
	}

	/**
	 * Create a new bad history token exception
	 * 
	 * @param message
	 */
	public BadHistoryTokenException(String message) {
		super(message);
	}

}
