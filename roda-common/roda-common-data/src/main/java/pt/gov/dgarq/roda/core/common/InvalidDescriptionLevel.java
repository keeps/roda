package pt.gov.dgarq.roda.core.common;

import pt.gov.dgarq.roda.core.data.eadc.DescriptionLevel;

/**
 * Thrown to indicate that an invalid description level has been specified.
 * Valid values for description level are:
 * <ul>
 * <li>{@link DescriptionLevel#FONDS},</li>
 * <li>{@link DescriptionLevel#SUBFONDS},</li>
 * <li>{@link DescriptionLevel#CLASS},</li>
 * <li>{@link DescriptionLevel#SUBCLASS},</li>
 * <li>{@link DescriptionLevel#SERIES},</li>
 * <li>{@link DescriptionLevel#SUBSERIES},</li>
 * <li>{@link DescriptionLevel#FILE},</li>
 * <li>{@link DescriptionLevel#ITEM}.</li>
 * </ul>
 * 
 * @author Rui Castro
 */
public class InvalidDescriptionLevel extends RODARuntimeException {
	private static final long serialVersionUID = -1898256310507091558L;

	/**
	 * Constructs a new InvalidDescriptionLevel.
	 */
	public InvalidDescriptionLevel() {
	}

	/**
	 * Constructs a new InvalidDescriptionLevel with the given error message.
	 * 
	 * @param message
	 *            the error message
	 */
	public InvalidDescriptionLevel(String message) {
		super(message);
	}

	/**
	 * Constructs a new InvalidDescriptionLevel with the given cause exception.
	 * 
	 * @param cause
	 *            the cause exception
	 */
	public InvalidDescriptionLevel(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new InvalidDescriptionLevel with the given error message and
	 * cause exception.
	 * 
	 * @param message
	 *            the error message
	 * @param cause
	 *            the cause exception
	 */
	public InvalidDescriptionLevel(String message, Throwable cause) {
		super(message, cause);
	}
}
