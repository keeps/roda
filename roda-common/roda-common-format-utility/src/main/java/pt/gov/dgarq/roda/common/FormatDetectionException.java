package pt.gov.dgarq.roda.common;

/**
 * Format detection exception
 * 
 * @author Ing. Vladislav Korecký [vladislav_korecky@gordic.cz] - GORDIC spol. s
 *         r.o.
 *
 */
public class FormatDetectionException extends Exception {
	private static final long serialVersionUID = -430879872156589449L;

	public FormatDetectionException() {
		super();
	}

	public FormatDetectionException(String message) {
		super(message);
	}

	public FormatDetectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public FormatDetectionException(Throwable cause) {
		super(cause);
	}

}
