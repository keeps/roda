package pt.gov.dgarq.roda.core.metadata.mets;

import gov.loc.mets.FileType;
import gov.loc.mets.MetsDocument;
import pt.gov.dgarq.roda.core.metadata.MetadataException;

/**
 * Thrown to indicate that a {@link FileType} already exists inside a
 * {@link MetsDocument}.
 * 
 * @author Luís Faria
 * @author Rui Castro
 */
public class MetsFileAlreadyExistsException extends MetadataException {
	private static final long serialVersionUID = -6804558406230272070L;

	/**
	 * Construct a new {@link MetsFileAlreadyExistsException}.
	 */
	public MetsFileAlreadyExistsException() {
		super();
	}

	/**
	 * Construct a new {@link MetsFileAlreadyExistsException} with the given
	 * message.
	 * 
	 * @param message
	 *            the error message.
	 */
	public MetsFileAlreadyExistsException(String message) {
		super(message);
	}

}
