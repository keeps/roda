package org.roda;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import pt.gov.dgarq.roda.core.data.v2.RepresentationState;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 *
 */
public class CorporaConstants {
	// Corpora constants
	public static final String SOURCE_AIP_CONTAINER = "AIP";
	public static final String SOURCE_PRESERVATION_CONTAINER = "Preservation";
	public static final String SOURCE_AIP_ID = "AIP_1";
	public static final String SOURCE_AIP_ID_3 = "AIP_3";
	public static final String SOURCE_AIP_BUGGY_ID = "AIP_BUGGY";
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

	public static final String F0_PREMIS_XML = "F0.premis.xml";
	public static final String TEXT_XML = "text/xml";
	public static final String EVENT_RODA_398_PREMIS_XML = "event_roda_398.premis.xml";
	public static final String INGESTION = "ingestion";
	public static final String RODA_8 = "roda:8";
	public static final String SUCCESS = "success";
	public static final String REPRESENTATION_PREMIS_XML = "representation.premis.xml";
	public static final String PRESERVATION_LEVEL_FULL = "full";

	public static final String AGENT_RODA_8 = "roda:8";
	public static final String METS_XML = "METS.xml";
	public static final String SOFTWARE_INGEST_TASK = "software:ingest_task";
	public static final String INGEST_CREATE_AIP = "Ingest/Create AIP/1.0";
	public static final String AGENT_RODA_8_PREMIS_XML = "agent_roda_8.premis.xml";
	public static final String HTML_EVENT_PRESERVATION_OBJECT = "eventPreservationObject";
	public static final String HTML_AIPID = "aipID";
	public static final String HTML_VALUE = "value";
	public static final String HTML_FIELD = "field";
	public static final String HTML_TYPE = "type";
	public static final String HTML_REPRESENTATION_FILE_PRESERVATION_OBJECT = "representationFilePreservationObject";
	public static final String HTML_MIMETYPE = "mimetype";
	public static final String HTML_DESCRIPTIVE_METADATA = "descriptiveMetadata";
	public static final String HTML_LEVEL = "level";
	public static final String HTML_FONDS = "fonds";
	public static final String HTML_REPOSITORY_CODE = "odd.ead-c.did.unitid.repositorycode_txt";
	public static final String HTML_PT_KEEPS = "PT-KEEPS";
	public static final String HTML_PREMIS = "premis";
	public static final String HTML_PRESERVATION_LEVEL = "preservationLevel";
	public static final String HTML_FULL = "full";
	public static final String HTML_TITLE = "title";
	public static final String HTML_MY_EXAMPLE = "My example";
	public static final String HTML_EVENT_TYPE = "eventType";
	public static final String HTML_INGESTION = "ingestion";

	// FIXME this should not be used: use RodaConstants instead
	// public static final String LOG_ACTION = "action";
	// public static final String LOG_ADDRESS = "address";
	// public static final String LOG_DATETIME = "datetime";
	// public static final String LOG_DURATION = "duration";
	// public static final String LOG_ID = "id";
	// public static final String LOG_RELATED_OBJECT_ID = "relatedObject";
	// public static final String LOG_USERNAME = "username";
	// public static final String LOG_PARAMETERS = "parameters";
	// public static final String LOG_FILE_ID = "fileID";
	public static final int YEAR_1213 = 1213;
	public static final int YEAR_2003 = 2003;

}
