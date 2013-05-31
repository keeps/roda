package pt.gov.dgarq.roda.ingest.siputility.builders;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pt.gov.dgarq.roda.core.data.RepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.SIPException;
import pt.gov.dgarq.roda.ingest.siputility.data.SIPRepresentationObject;
import pt.gov.dgarq.roda.ingest.siputility.data.StreamRepresentationObject;

/**
 * Representation builder for relational databases.
 * 
 * @author Rui Castro
 */
public class RelationalDatabaseRepresentationBuilder extends
		RepresentationBuilder {

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

		if (rObject.getPartFiles() != null && rObject.getPartFiles().length > 0) {
			subType = "application/dbml+octet-stream";
		} else {
			subType = "application/dbml";
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
					"filenames and streams cannot be null, empty or in diferent number");
		}

		String dbmlFilename = filenames.get(0);
		InputStream dbmlStream = streams.get(0);

		List<String> blobFilenames;
		List<InputStream> blobStreams;
		String subType;

		if (filenames.size() > 1) {
			subType = "application/dbml+octet-stream";
			blobFilenames = filenames.subList(1, filenames.size());
			blobStreams = streams.subList(1, streams.size());
		} else {
			subType = "application/dbml";
			blobFilenames = new ArrayList<String>();
			blobStreams = new ArrayList<InputStream>();
		}

		return createRepresentation(
				SIPRepresentationObject.RELATIONAL_DATABASE, subType,
				createNewRepresentationID(), dbmlFilename, dbmlStream,
				blobFilenames, blobStreams);

	}

}
