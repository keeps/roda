package org.roda.common;

public final class RodaConstants {

	/*
	 * Misc
	 */
	public final static String LINE_SEPARATOR = System.getProperty("line.separator");
	public final static String ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public final static String SHA1 = "SHA-1";
	public final static String MD5 = "MD5";

	/*
	 * INDEX NAMES
	 */
	public static final String INDEX_AIP = "AIP";
	public static final String INDEX_SDO = "SDO";
	public static final String INDEX_DESCRIPTIVE_METADATA = "DescriptiveMetadata";
	public static final String INDEX_PRESERVATION_EVENTS = "PreservationEvent";
	public static final String INDEX_PRESERVATION_OBJECTS = "PreservationObject";
	public static final String INDEX_REPRESENTATIONS = "Representation";
	public static final String INDEX_NOTIFICATIONS = "Notification";
	public static final String INDEX_PRESERVATION_AGENTS = "PreservationAgent";
	public static final String INDEX_PRESERVATION_PLANS = "PreservationPlan";
	public static final String INDEX_PRESERVATION_RISKS = "PreservationRisk";
	public static final String INDEX_USER_LOG = "UserLog";
	public static final String INDEX_ACTION_LOG = "ActionLog";
	public static final String INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX = "odd";

	/*
	 * STORAGE CONTAINERS
	 */
	public static final String STORAGE_CONTAINER_SIP = "SIP";
	public static final String STORAGE_CONTAINER_AIP = "AIP";
	public static final String STORAGE_CONTAINER_NOTIFICATIONS = "Notifications";
	public static final String STORAGE_CONTAINER_PRESERVATION = "Preservation";
	public static final String STORAGE_CONTAINER_USERLOG = "User log";
	public static final String STORAGE_CONTAINER_ACTIONLOG = "Action log";

	/*
	 * STORAGE DIRECTORIES
	 */
	public static final String STORAGE_DIRECTORY_METADATA = "metadata";
	public static final String STORAGE_DIRECTORY_DESCRIPTIVE = "descriptive";
	public static final String STORAGE_DIRECTORY_PRESERVATION = "preservation";
	public static final String STORAGE_DIRECTORY_REPRESENTATION_PREFIX = "representation_";
	public static final String STORAGE_DIRECTORY_DATA = "data";

	/*
	 * STORAGE METADATA
	 */
	public static final String STORAGE_META_PARENT_ID = "parentId";
	public static final String STORAGE_META_TYPE = "type";
	public static final String STORAGE_META_ACTIVE = "active";
	public static final String STORAGE_META_ENTRYPOINT = "entryPoint";
	public static final String STORAGE_META_FORMAT_MIME = "format.mimetype";
	public static final String STORAGE_META_FORMAT_VERSION = "format.version";
	public static final String STORAGE_META_DATE_CREATED = "date.created";
	public static final String STORAGE_META_DATE_MODIFIED = "date.modified";
	public static final String STORAGE_META_AIP_ID = "aip.id";
	public static final String STORAGE_META_REPRESENTATION_ID = "representation.id";
	public static final String STORAGE_META_REPRESENTATION_STATUSES = "representation.statuses";
	public static final String STORAGE_META_DIGEST_SHA1 = "digest.sha1";

	/*
	 * SIP FIELDS
	 */
	public static final String SIP_NAME = "name";
	public static final String SIP_SUBMISSION_DATE = "submissionDate";
	public static final String SIP_STATE_CURRENT = "state";
	public static final String SIP_PERCENTAGE = "percentage";
	public static final String SIP_PRODUCER = "producer";
	public static final String SIP_CREATED_AIP = "createdAIPs";

	/*
	 * AIP FIELDS
	 */
	public static final String AIP_ID = "id";
	public static final String AIP_PARENT_ID = "parentId";
	public static final String AIP_ACTIVE = "active";

	public static final String AIP_DATE_CREATED = "dateCreated";
	public static final String AIP_DATE_MODIFIED = "dateModified";
	public static final String AIP_DESCRIPTIVE_METADATA_ID = "descriptiveMetadataId";
	public static final String AIP_REPRESENTATION_ID = "representationId";

	/*
	 * SDO FIELDS
	 */
	public static final String SDO_LEVEL = "level";
	public static final String SDO_TITLE = "title";
	public static final String SDO_DATE_INITIAL = "dateInitial";
	public static final String SDO_DATE_FINAL = "dateFinal";
	public static final String SDO_CHILDREN_COUNT = "childrenCount";
	public static final String SDO_DESCRIPTION = "description";
	public static final String SDO_STATE = "state";
	public static final String SDO_LABEL = "label";

	/**
	 * Descriptive Metadata fields
	 */
	public static final String DESCRIPTIVE_METADATA_ID = "id";

	/*
	 * SRO FIELDS
	 */
	public static final String SRO_UUID = "uuid";
	public static final String SRO_ID = "id";
	public static final String SRO_AIP_ID = "aipId";
	public static final String SRO_ACTIVE = "active";
	public static final String SRO_LABEL = "label";
	public static final String SRO_TYPE = "type";
	public static final String SRO_SUBTYPE = "subtype";
	public static final String SRO_DATE_MODIFICATION = "dateModified";
	public static final String SRO_DATE_CREATION = "dateCreated";
	public static final String SRO_STATUS = "status";
	public static final String SRO_FILE_IDS = "fileId";

	/*
	 * OTHER FIELDS
	 */
	public static final String PERMISSIONS_PRODUCERS_USERS = "producersUsers";
	public static final String PERMISSIONS_PRODUCERS_GROUPS = "producersGroups";
	public static final String PERMISSIONS_MODIFY_USERS = "modifyUsers";
	public static final String PERMISSIONS_MODIFY_GROUPS = "modifyGroups";
	public static final String PERMISSIONS_REMOVE_USERS = "removeUsers";
	public static final String PERMISSIONS_REMOVE_GROUPS = "removeGroups";
	public static final String PERMISSIONS_GRANT_USERS = "grantUsers";
	public static final String PERMISSIONS_GRANT_GROUPS = "grantGroups";

}
