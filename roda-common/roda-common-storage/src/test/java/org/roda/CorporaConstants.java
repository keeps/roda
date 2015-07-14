package org.roda;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.roda.model.RepresentationState;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class CorporaConstants {
	// Corpora constants
	public static final String SOURCE_AIP_CONTAINER = "AIP";
	public static final String SOURCE_AIP_ID = "AIP_1";
	public static final String DATE_CREATED = "2015-06-06T15:30:00.000+0000";
	public static final String DATE_MODIFIED = "2015-06-06T16:30:00.000+0000";

	public static final String DESCRIPTIVE_METADATA_ID = "ead-c.xml";
	public static final String DESCRIPTIVE_METADATA_TYPE = "eadc-2008";

	public static final String REPRESENTATION_1_ID = "representation_1";
	public static final String REPRESENTATION_1_TYPE = "digitalized_work";
	public static final Set<RepresentationState> REPRESENTATION_1_STATUSES = new HashSet<RepresentationState>(
			Arrays.asList(RepresentationState.ORIGINAL));

	public static final String REPRESENTATION_1_FILE_1_ID = "METS.xml";
	public static final String REPRESENTATION_1_FILE_1_FORMAT_MIMETYPE = "application/xml";
	public static final String REPRESENTATION_1_FILE_1_FORMAT_VERSION = "1.0";
	// puid=fmt/101

	public static final String REPRESENTATION_1_FILE_2_ID = "fil_7516.jpg";
	public static final String REPRESENTATION_1_FILE_2_FORMAT_MIMETYPE = "image/jpeg";
	public static final String REPRESENTATION_1_FILE_2_FORMAT_VERSION = "1.01";
	// puid=fmt/43

	public static final String REPRESENTATION_2_ID = "representation_2";
	public static final String REPRESENTATION_2_TYPE = "digitalized_work";
	public static final Set<RepresentationState> REPRESENTATION_2_STATUSES = new HashSet<RepresentationState>(
			Arrays.asList(RepresentationState.NORMALIZED));

	public static final String REPRESENTATION_2_FILE_1_ID = "fil_7516.jpg.tiff";
	public static final String REPRESENTATION_2_FILE_1_FORMAT_MIMETYPE = "image/tiff";
	public static final String REPRESENTATION_2_FILE_1_FORMAT_VERSION = null;

	public static final String REPRESENTATION_2_FILE_2_ID = "METS.xml";
	public static final String REPRESENTATION_2_FILE_2_FORMAT_MIMETYPE = "application/xml";
	public static final String REPRESENTATION_2_FILE_2_FORMAT_VERSION = "1.0";

	// public static final String OTHER_AIP_PATH = "AIP/AIP_2";
	public static final String OTHER_AIP_ID = "AIP_2";
	public static final String OTHER_DESCRIPTIVE_METADATA_STORAGEPATH = "AIP/AIP_2/metadata/descriptive/dc.xml";
	public static final String OTHER_DESCRIPTIVE_METADATA_TYPE = "dcxml";
	public static final String OTHER_REPRESENTATION_STORAGEPATH = "AIP/AIP_2/data/representation_1";
	public static final String OTHER_FILE_STORAGEPATH = "AIP/AIP_2/data/representation_1/2012-roda-promo-en.pdf";
	// puid=fmt/101

	public static final String SOURCE_DESC_METADATA_CONTAINER = "DescriptiveMetadata";
	public static final String STRANGE_DESC_METADATA_FILE = "strange.xml";
}
