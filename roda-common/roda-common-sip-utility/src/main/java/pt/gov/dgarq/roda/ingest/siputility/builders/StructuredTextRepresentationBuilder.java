package pt.gov.dgarq.roda.ingest.siputility.builders;

import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;

import pt.gov.dgarq.roda.common.FormatUtility;
import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.SIPException;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.StreamRepresentationObject;

/**
 * Representation builder for structured text.
 * 
 * @author Rui Castro
 */
public class StructuredTextRepresentationBuilder extends RepresentationBuilder {

	private static final Logger logger = Logger
			.getLogger(StructuredTextRepresentationBuilder.class);

	/**
	 * Returns the subtype of the given {@link RepresentationObject}.
	 * 
	 * @param rObject
	 *            the {@link RepresentationObject}.
	 * 
	 * @return a {@link String} with the subtype of the given
	 *         {@link RepresentationObject} or <code>null</code> if the subtype
	 *         couldn't be determined.
	 */
	public static String getRepresentationSubtype(RepresentationObject rObject) {

		String subType = null;

		if (rObject.getRootFile() != null) {
			subType = rObject.getRootFile().getMimetype();
		}

		return subType;
	}

	/**
	 * @see RepresentationBuilder#createRepresentation(List, List)
	 */
	@Override
	public StreamRepresentationObject createRepresentation(
			List<String> filenames, List<InputStream> streams)
			throws SIPException {

		if (filenames == null || streams == null
				|| filenames.size() != streams.size() || filenames.isEmpty()) {
			throw new IllegalArgumentException(
					"filenames and streams cannot be null or empty");
		}

		String filename = filenames.get(0);
		InputStream fileStream = streams.get(0);

		if (filenames.size() > 1) {
			logger
					.warn("list of files for this type of representation should contain only 1 file. It contains "
							+ filenames.size());
		}

		String subType = FormatUtility.getMimetype(filename);

		return createRepresentation(SIPRepresentationObject.STRUCTURED_TEXT,
				subType, createNewRepresentationID(), filename, fileStream,
				null, null);

	}

}
