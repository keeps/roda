package pt.gov.dgarq.roda.ingest.siputility.builders;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.SIPException;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.StreamRepresentationObject;

/**
 * Representation builder for unknown representation type.
 * 
 * @author Rui Castro
 */
public class UnknownRepresentationBuilder extends RepresentationBuilder {

	/**
	 * Returns the subtype of the given {@link RepresentationObject}.
	 * 
	 * @param rObject
	 *            the audio {@link RepresentationObject}.
	 * 
	 * @return a {@link String} with the subtype of the given
	 *         {@link RepresentationObject} or <code>null</code> if the subtype
	 *         couldn't be determined.
	 */
	public static String getRepresentationSubtype(RepresentationObject rObject) {
		return null;
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

		String rootFilename = filenames.get(0);
		InputStream rootStream = streams.get(0);

		List<String> partFilenames;
		List<InputStream> partStreams;

		if (filenames.size() > 1) {
			partFilenames = filenames.subList(1, filenames.size());
			partStreams = streams.subList(1, streams.size());
		} else {
			partFilenames = new ArrayList<String>();
			partStreams = new ArrayList<InputStream>();
		}

		return createRepresentation(SIPRepresentationObject.UNKNOWN,
				createNewRepresentationID(), rootFilename, rootStream,
				partFilenames, partStreams);
	}

}
