/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.common;

public final class RodaConstants {

  /*
   * RODA Core properties (provided via -D in the command-line)
   */
  public static final String CORE_NODE_TYPE = "roda.node.type";
  public static final String CORE_CLUSTER_HOSTNAME = "roda.cluster.hostname";
  public static final String CORE_CLUSTER_PORT = "roda.cluster.port";
  public static final String CORE_NODE_HOSTNAME = "roda.node.hostname";
  public static final String CORE_NODE_PORT = "roda.node.port";
  public static final String INSTALL_FOLDER_SYSTEM_PROPERTY = "roda.home";

  /*
   * RODA Core properties (provided via configuration file)
   */
  public static final String CORE_STORAGE_TYPE = "core.storage.type";
  public static final String CORE_STORAGE_FEDORA4_URL = "core.storage.fedora4.url";
  public static final String CORE_STORAGE_FEDORA4_USERNAME = "core.storage.fedora4.username";
  public static final String CORE_STORAGE_FEDORA4_PASSWORD = "core.storage.fedora4.password";
  public static final String CORE_SOLR_TYPE = "core.solr.type";
  public static final String CORE_SOLR_HTTP_URL = "core.solr.http.url";
  public static final String CORE_SOLR_HTTP_CLOUD_URLS = "core.solr.http_cloud.urls";

  /*
   * Misc
   */
  public static final String INSTALL_FOLDER_ENVIRONEMNT_VARIABLE = "RODA_HOME";
  public static final String ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  public static final String SOLRDATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  public static final String SHA1 = "SHA-1";
  public static final String SHA256 = "SHA-256";
  public static final String MD5 = "MD5";
  public static final String LOCALE = "locale";

  public enum DateGranularity {
    YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, MILLISECOND
  }

  public static final String CROSSWALKS_DISSEMINATION_HTML_PATH = "crosswalks/dissemination/html";
  public static final String I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX = "ui.browse.metadata.descriptive.type.";
  public static final String I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX = "crosswalks.dissemination.html.";
  public static final String I18N_UI_FACETS_PREFIX = "ui.facets.";

  /*
   * Installation (and most probably classpath as well) related variables
   */
  public static final String CORE_DESCRIPTION_LEVELS_FILE = "roda-description-levels-hierarchy.properties";
  public static final String CORE_CONFIG_FOLDER = "config";
  public static final String CORE_I18N_FOLDER = "i18n";
  public static final String CORE_DATA_FOLDER = "data";
  public static final String CORE_STORAGE_FOLDER = "storage";
  public static final String CORE_INDEX_FOLDER = "index";
  public static final String CORE_CROSSWALKS_FOLDER = "crosswalks";
  public static final String CORE_JOBREPORT_FOLDER = "jobreport";
  public static final String CORE_JOB_FOLDER = "job";
  public static final String CORE_TRANSFERREDRESOURCE_FOLDER = "transferredresource";
  public static final String CORE_MEMBERS_FOLDER = "members";
  public static final String CORE_ACTIONLOG_FOLDER = "actionlog";
  public static final String CORE_PRESERVATIONEVENT_FOLDER = "preservationevent";
  public static final String CORE_PRESERVATIONAGENT_FOLDER = "preservationagent";
  public static final String CORE_FILE_FOLDER = "file";
  public static final String CORE_REPRESENTATION_FOLDER = "representation";
  public static final String CORE_AIP_FOLDER = "aip";
  public static final String CORE_PLUGINS_FOLDER = "plugins";
  public static final String CORE_DISSEMINATION_FOLDER = "dissemination";
  public static final String CORE_HTML_FOLDER = "html";
  public static final String CORE_INGEST_FOLDER = "ingest";
  public static final String CORE_LOG_FOLDER = "log";
  public static final String CORE_SCHEMAS_FOLDER = "schemas";
  public static final String CORE_LDAP_FOLDER = "ldap";

  public static final String DEFAULT_NODE_HOSTNAME = "localhost";
  public static final String DEFAULT_NODE_PORT = "2551";

  /*
   * API related
   */
  public static final String API_SEP = "/";
  public static final String API_REST_V1_AIPS = "api/v1/aips/";
  public static final String API_REST_V1_TRANSFERRED = "api/v1/transferred";
  // sub-resources strings
  public static final String API_DATA = "data";
  public static final String API_DESCRIPTIVE_METADATA = "descriptive_metadata";
  public static final String API_PRESERVATION_METADATA = "preservation_metadata";
  // "http query string" related strings
  public static final String API_QUERY_START = "?";
  public static final String API_QUERY_ASSIGN_SYMBOL = "=";
  public static final String API_QUERY_SEP = "&";
  public static final String API_QUERY_KEY_ACCEPT_FORMAT = "acceptFormat";
  public static final String API_QUERY_VALUE_ACCEPT_FORMAT_BIN = "bin";
  public static final String API_QUERY_VALUE_ACCEPT_FORMAT_XML = "xml";
  public static final String API_QUERY_VALUE_ACCEPT_FORMAT_HTML = "html";
  public static final String API_QUERY_VALUE_ACCEPT_FORMAT_JSON = "json";
  public static final String API_QUERY_KEY_LANG = "lang";
  public static final String API_QUERY_VALUE_LANG_DEFAULT = RodaConstants.API_QUERY_VALUE_LANG_PT_PT;
  public static final String API_QUERY_VALUE_LANG_PT_PT = "pt_PT";
  public static final String API_QUERY_VALUE_LANG_EN_US = "en_US";
  public static final String API_QUERY_KEY_START = "start";
  public static final String API_QUERY_KEY_LIMIT = "limit";
  // "http path param" related strings
  public static final String API_PATH_PARAM_AIP_ID = "aip_id";
  public static final String API_PATH_PARAM_REPRESENTATION_ID = "representation_id";
  public static final String API_PATH_PARAM_FILE_ID = "file_id";
  public static final String API_PATH_PARAM_METADATA_ID = "metadata_id";
  // http headers used
  public static final String API_HTTP_HEADER_ACCEPT = "Accept";

  /*
   * Controllers related
   */
  public static final String CONTROLLER_FILTER_PARAM = "filter";
  public static final String CONTROLLER_SORTER_PARAM = "sorter";
  public static final String CONTROLLER_SUBLIST_PARAM = "sublist";

  /*
   * Core (storage, index, orchestrator, etc.)
   */
  public enum StorageType {
    FILESYSTEM, FEDORA4
  }

  public static final StorageType DEFAULT_STORAGE_TYPE = StorageType.FILESYSTEM;

  public enum SolrType {
    EMBEDDED, HTTP, HTTP_CLOUD
  }

  public static final SolrType DEFAULT_SOLR_TYPE = SolrType.EMBEDDED;

  public enum NodeType {
    MASTER, WORKER, TEST
  }

  public static final NodeType DEFAULT_NODE_TYPE = NodeType.MASTER;

  public enum OrchestratorType {
    EMBEDDED, AKKA, AKKA_DISTRIBUTED
  }

  public static final OrchestratorType DEFAULT_ORCHESTRATOR_TYPE = OrchestratorType.EMBEDDED;

  public static final String CORE_LDAP_DEFAULT_HOST = "localhost";
  public static final int CORE_LDAP_DEFAULT_PORT = 10389;

  /*
   * INDEX NAMES
   */
  public static final String INDEX_AIP = "AIP";
  public static final String INDEX_PRESERVATION_EVENTS = "PreservationEvent";
  public static final String INDEX_REPRESENTATION = "Representation";
  public static final String INDEX_PRESERVATION_AGENTS = "PreservationAgent";
  public static final String INDEX_ACTION_LOG = "ActionLog";
  public static final String INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX = "odd";
  public static final String INDEX_MEMBERS = "Members";
  public static final String INDEX_TRANSFERRED_RESOURCE = "TransferredResource";
  public static final String INDEX_JOB = "Job";
  public static final String INDEX_JOB_REPORT = "JobReport";
  public static final String INDEX_FILE = "File";
  // FIXME not in use. are the following to remove?
  // public static final String INDEX_DESCRIPTIVE_METADATA =
  // "DescriptiveMetadata";
  // public static final String INDEX_NOTIFICATIONS = "Notification";
  // public static final String INDEX_PRESERVATION_PLANS = "PreservationPlan";
  // public static final String INDEX_PRESERVATION_RISKS = "PreservationRisk";
  // public static final String INDEX_USER_LOG = "UserLog";
  // public static final String INDEX_CHARACTERIZATION = "Characterization";

  /*
   * STORAGE CONTAINERS
   */
  public static final String STORAGE_CONTAINER_SIP = "SIP";
  public static final String STORAGE_CONTAINER_AIP = "AIP";
  public static final String STORAGE_CONTAINER_PRESERVATION = "Preservation";
  public static final String STORAGE_CONTAINER_ACTIONLOG = "Action log";
  public static final String STORAGE_CONTAINER_JOB = "Job";
  public static final String STORAGE_CONTAINER_JOB_REPORT = "Job report";
  // FIXME not in use. are the following to remove?
  // public static final String STORAGE_CONTAINER_NOTIFICATIONS =
  // "Notifications";
  // public static final String STORAGE_CONTAINER_USERLOG = "User log";

  /*
   * STORAGE DIRECTORIES
   */
  public static final String STORAGE_DIRECTORY_METADATA = "metadata";
  public static final String STORAGE_DIRECTORY_DESCRIPTIVE = "descriptive";
  public static final String STORAGE_DIRECTORY_PRESERVATION = "preservation";
  public static final String STORAGE_DIRECTORY_DATA = "data";
  public static final String STORAGE_DIRECTORY_AGENTS = "agents";
  public static final String STORAGE_DIRECTORY_OTHER = "other";
  // FIXME not in use. are the following to remove?
  // public static final String STORAGE_DIRECTORY_REPRESENTATION_PREFIX =
  // "representation_";

  /*
   * STORAGE METADATA
   */
  public static final String STORAGE_META_PARENT_ID = "parentId";
  public static final String STORAGE_META_TYPE = "type";
  // public static final String STORAGE_META_ACTIVE = "active";
  // public static final String STORAGE_META_SIZE_IN_BYTES = "sizeInBytes";
  // public static final String STORAGE_META_ENTRYPOINT = "entryPoint";
  // public static final String STORAGE_META_FORMAT_MIME = "format.mimetype";
  // public static final String STORAGE_META_FORMAT_VERSION = "format.version";
  // public static final String STORAGE_META_DATE_CREATED = "date.created";
  // public static final String STORAGE_META_DATE_MODIFIED = "date.modified";
  // public static final String STORAGE_META_AIP_ID = "aip.id";
  // public static final String STORAGE_META_REPRESENTATION_ID =
  // "representation.id";
  // public static final String STORAGE_META_REPRESENTATION_STATUSES =
  // "representation.statuses";
  // public static final String STORAGE_META_DIGEST_SHA1 = "digest.sha1";
  // public static final String STORAGE_META_PERMISSION_GRANT_USERS =
  // "permission.grant.users";
  // public static final String STORAGE_META_PERMISSION_GRANT_GROUPS =
  // "permission.grant.groups";
  // public static final String STORAGE_META_PERMISSION_READ_USERS =
  // "permission.read.users";
  // public static final String STORAGE_META_PERMISSION_READ_GROUPS =
  // "permission.read.groups";
  // XXX the following two constants formerly were known as producers
  // permissions
  public static final String STORAGE_META_PERMISSION_INSERT_USERS = "permission.insert.users";
  public static final String STORAGE_META_PERMISSION_INSERT_GROUPS = "permission.insert.groups";
  public static final String STORAGE_META_PERMISSION_MODIFY_USERS = "permission.modify.users";
  public static final String STORAGE_META_PERMISSION_MODIFY_GROUPS = "permission.modify.groups";
  public static final String STORAGE_META_PERMISSION_REMOVE_USERS = "permission.remove.users";
  public static final String STORAGE_META_PERMISSION_REMOVE_GROUPS = "permission.remove.groups";

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

  public static final String AIP_DESCRIPTIVE_METADATA_ID = "descriptiveMetadataId";
  public static final String AIP_REPRESENTATION_ID = "representationId";
  public static final String AIP_HAS_REPRESENTATIONS = "hasRepresentations";
  public static final String AIP_PRESERVATION_OBJECTS_ID = "preservationObjectsId";
  public static final String AIP_PRESERVATION_EVENTS_ID = "preservationEventsId";

  // TODO remove
  // public static final String AIP_PERMISSION_GRANT_USERS =
  // STORAGE_META_PERMISSION_GRANT_USERS;
  // public static final String AIP_PERMISSION_GRANT_GROUPS =
  // STORAGE_META_PERMISSION_GRANT_GROUPS;
  // public static final String AIP_PERMISSION_READ_USERS =
  // STORAGE_META_PERMISSION_READ_USERS;
  // public static final String AIP_PERMISSION_READ_GROUPS =
  // STORAGE_META_PERMISSION_READ_GROUPS;
  // XXX the following two constants formerly were known as producers
  // permissions
  // public static final String AIP_PERMISSION_INSERT_USERS =
  // STORAGE_META_PERMISSION_INSERT_USERS;
  // public static final String AIP_PERMISSION_INSERT_GROUPS =
  // STORAGE_META_PERMISSION_INSERT_GROUPS;
  // public static final String AIP_PERMISSION_MODIFY_USERS =
  // STORAGE_META_PERMISSION_MODIFY_USERS;
  // public static final String AIP_PERMISSION_MODIFY_GROUPS =
  // STORAGE_META_PERMISSION_MODIFY_GROUPS;
  // public static final String AIP_PERMISSION_REMOVE_USERS =
  // STORAGE_META_PERMISSION_REMOVE_USERS;
  // public static final String AIP_PERMISSION_REMOVE_GROUPS =
  // STORAGE_META_PERMISSION_REMOVE_GROUPS;
  public static final String AIP_LEVEL = "level";
  public static final String AIP_TITLE = "title";
  public static final String AIP_TITLE_SORT = "title_sort";
  public static final String AIP_DATE_INITIAL = "dateInitial";
  public static final String AIP_DATE_FINAL = "dateFinal";
  public static final String AIP_CHILDREN_COUNT = "childrenCount";
  public static final String AIP_DESCRIPTION = "description";
  public static final String AIP_STATE = "state";
  public static final String AIP_LABEL = "label";
  public static final String AIP__ALL = "_all";

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
  public static final String SRO_ORIGINAL = "original";
  public static final String SRO_EVENT_PRESERVATION_IDS = "eventId";
  public static final String SRO_REPRESENTATION_PRESERVATION_IDS = "representationId";
  public static final String SRO_SIZE_IN_BYTES = "sizeInBytes";
  public static final String SRO_TOTAL_NUMBER_OF_FILES = "totalNumberOfFiles";

  /*
   * SEPM FIELDS
   */
  public static final String SEPM_AGENT_ID = "agentId";
  public static final String SEPM_CREATED_DATE = "dateCreated";
  public static final String SEPM_ID = "id";
  public static final String SEPM_LABEL = "label";
  public static final String SEPM_LAST_MODIFIED_DATE = "dateModified";
  public static final String SEPM_STATE = "state";
  public static final String SEPM_TARGET_ID = "targetId";
  public static final String SEPM_TYPE = "type";
  public static final String SEPM_AIP_ID = "aipId";
  public static final String SEPM_DATETIME = "datetime";
  public static final String SEPM_NAME = "name";
  public static final String SEPM_DESCRIPTION = "description";
  public static final String SEPM_OUTCOME_RESULT = "outcomeResult";
  public static final String SEPM_OUTCOME_DETAILS = "outcomeDetails";
  public static final String SEPM_REPRESENTATION_ID = "representationId";
  public static final String SEPM_FILE_ID = "fileId";

  /*
   * SRPM FIELDS
   */
  public static final String SRPM_CREATED_DATE = "dateCreated";
  public static final String SRPM_ID = "id";
  public static final String SRPM_LABEL = "label";
  public static final String SRPM_LAST_MODIFIED_DATE = "dateModified";
  public static final String SRPM_REPRESENTATION_OBJECT_ID = "representationObjectId";
  public static final String SRPM_STATE = "state";
  public static final String SRPM_TYPE = "type";
  public static final String SRPM_AIP_ID = "aipId";
  public static final String SRPM_MODEL = "model";
  public static final String SRPM_REPRESENTATION_ID = "representationId";
  public static final String SRPM_FILE_ID = "fileId";

  /*
   * SRFM
   */
  public static final String SRFM_CREATED_DATE = "dateCreated";
  public static final String SRFM_ID = "id";
  public static final String SRFM_LABEL = "label";
  public static final String SRFM_LAST_MODIFIED_DATE = "dateModified";
  public static final String SRFM_REPRESENTATION_OBJECT_ID = "representationObjectId";
  public static final String SRFM_STATE = "state";
  public static final String SRFM_TYPE = "type";
  public static final String SRFM_AIP_ID = "aipId";
  public static final String SRFM_REPRESENTATION_ID = "representationId";
  public static final String SRFM_FILE_ID = "fileId";
  public static final String SRFM_HASH = "hash";
  public static final String SRFM_MIMETYPE = "mimetype";
  public static final String SRFM_PRONOM_ID = "pronomId";
  public static final String SRFM_SIZE = "size";

  /*
   * OTHER FIELDS
   */
  public static final String OBJECT_PERMISSIONS_PRODUCERS_USERS = "producersUsers";
  public static final String OBJECT_PERMISSIONS_PRODUCERS_GROUPS = "producersGroups";
  public static final String OBJECT_PERMISSIONS_MODIFY_USERS = "modifyUsers";
  public static final String OBJECT_PERMISSIONS_MODIFY_GROUPS = "modifyGroups";
  public static final String OBJECT_PERMISSIONS_REMOVE_USERS = "removeUsers";
  public static final String OBJECT_PERMISSIONS_REMOVE_GROUPS = "removeGroups";
  public static final String OBJECT_PERMISSIONS_GRANT_USERS = "grantUsers";
  public static final String OBJECT_PERMISSIONS_GRANT_GROUPS = "grantGroups";

  public static final String REPOSITORY_PERMISSIONS_METADATA_EDITOR = "administration.metadata_editor";
  public static final String LOG_ACTION_COMPONENT = "actionComponent";
  public static final String LOG_ACTION_METHOD = "actionMethod";
  public static final String LOG_ADDRESS = "address";
  public static final String LOG_DATETIME = "datetime";
  public static final String LOG_DURATION = "duration";
  public static final String LOG_ID = "id";
  public static final String LOG_RELATED_OBJECT_ID = "relatedObject";
  public static final String LOG_USERNAME = "username";
  public static final String LOG_PARAMETERS = "parameters";
  public static final String LOG_FILE_ID = "fileID";

  // MEMBER: USER or GROUP
  public static final String MEMBERS_ID = "id";
  public static final String MEMBERS_NAME = "name";
  public static final String MEMBERS_FULLNAME = "fullName";
  public static final String MEMBERS_IS_ACTIVE = "isActive";
  public static final String MEMBERS_IS_USER = "isUser";
  public static final String MEMBERS_GROUPS_ALL = "groupsAll";
  public static final String MEMBERS_ROLES_ALL = "rolesAll";

  public static final String TRANSFERRED_RESOURCE_ID = "id";
  public static final String TRANSFERRED_RESOURCE_FULLPATH = "fullPath";
  public static final String TRANSFERRED_RESOURCE_PARENT_ID = "parentId";
  public static final String TRANSFERRED_RESOURCE_RELATIVEPATH = "relativePath";
  public static final String TRANSFERRED_RESOURCE_DATE = "date";
  public static final String TRANSFERRED_RESOURCE_ISFILE = "isFile";
  public static final String TRANSFERRED_RESOURCE_NAME = "name";
  public static final String TRANSFERRED_RESOURCE_SIZE = "size";
  public static final String TRANSFERRED_RESOURCE_ANCESTORS = "ancestors";

  public static final String JOB_ID = "id";
  public static final String JOB_NAME = "name";
  public static final String JOB_USERNAME = "username";
  public static final String JOB_START_DATE = "startDate";
  public static final String JOB_END_DATE = "endDate";
  public static final String JOB_STATE = "state";
  public static final String JOB_COMPLETION_PERCENTAGE = "completionPercentage";
  public static final String JOB_PLUGIN = "plugin";
  public static final String JOB_PLUGIN_TYPE = "pluginType";
  public static final String JOB_PLUGIN_PARAMETERS = "pluginParameters";
  public static final String JOB_RESOURCE_TYPE = "resourceType";
  public static final String JOB_ORCHESTRATOR_METHOD = "orchestratorMethod";
  public static final String JOB_OBJECT_IDS = "objectIds";
  public static final String JOB_OBJECT_IDS_TO_AIP_REPORT = "objectIdsToAipReport";

  public static final String PLUGIN_PARAMS_JOB_ID = "job.id";
  public static final String PLUGIN_PARAMS_PARENT_ID = "parameter.parent_id";
  public static final String PLUGIN_PARAMS_FORCE_PARENT_ID = "parameter.force_parent_id";
  public static final String PLUGIN_PARAMS_INT_VALUE = "parameter.int_value";
  public static final String PLUGIN_PARAMS_STRING_VALUE = "parameter.string_value";
  public static final String PLUGIN_PARAMS_BOOLEAN_VALUE = "parameter.boolean_value";

  /* Job Report */
  public static final String JOB_REPORT_ID = "id";
  public static final String JOB_REPORT_AIP_ID = "aipId";
  public static final String JOB_REPORT_OBJECT_ID = "objectId";
  public static final String JOB_REPORT_JOB_ID = "jobId";
  public static final String JOB_REPORT_REPORT = "report";
  public static final String JOB_REPORT_DATE_CREATED = "dateCreated";
  public static final String JOB_REPORT_DATE_UPDATE = "dateUpdated";
  public static final String JOB_REPORT_LAST_PLUGIN_RAN = "lastPluginRan";
  public static final String JOB_REPORT_LAST_PLUGIN_RAN_STATE = "lastPluginRanState";

  public static final String FILE_ID = "id";
  public static final String FILE_AIPID = "aipId";
  public static final String FILE_FORMAT_MIMETYPE = "formatMimetype";
  public static final String FILE_FORMAT_VERSION = "formatVersion";
  public static final String FILE_FILEID = "fileId";
  public static final String FILE_REPRESENTATIONID = "representationId";
  public static final String FILE_STORAGE_PATH = "storagePath";
  public static final String FILE_ISENTRYPOINT = "isEntryPoint";
  public static final String FILE_FILEFORMAT = "fileFormat";
  public static final String FILE_STORAGEPATH = "storagePath";
  public static final String FILE_UUID = "uuid";
  public static final String FILE_ORIGINALNAME = "originalName";
  public static final String FILE_SIZE = "size";
  public static final String FILE_ISDIRECTORY = "isDirectory";
  public static final String FILE_PRONOM = "formatPronom";
  public static final String FILE_EXTENSION = "extension";
  public static final String FILE_CREATING_APPLICATION_NAME = "creatingApplicationName";
  public static final String FILE_CREATING_APPLICATION_VERSION = "creatingApplicationVersion";
  public static final String FILE_DATE_CREATED_BY_APPLICATION = "dateCreatedByApplication";
  public static final String FILE_HASH = "hash";
  public static final String FILE_FULLTEXT = "fulltext";
  public static final String FILE_SEARCH = "search";

  public static final String REPORT_ATTR_AIP_ID = "aip.id";
  public static final String REPORT_ATTR_DATETIME = "datetime";
  public static final String REPORT_ATTR_OUTCOME = "outcome";
  public static final String REPORT_ATTR_OUTCOME_SUCCESS = "success";
  public static final String REPORT_ATTR_OUTCOME_FAILURE = "failure";
  public static final String REPORT_ATTR_OUTCOME_DETAILS = "outcomeDetails";
  public static final String REPORT_ATTR_REASON = "reason";

  /* View representation */
  public static final String VIEW_REPRESENTATION_DESCRIPTION_LEVEL = "description-level-representation";
  public static final String VIEW_REPRESENTATION_REPRESENTATION = "representation";
  public static final String VIEW_REPRESENTATION_FOLDER = "folder";
  public static final String VIEW_REPRESENTATION_FILE = "file";

  /************************************************
   * PREMIS
   ************************************************/

  public static final String PREMIS_IDENTIFIER_TYPE_LOCAL = "local";
  public static final String PREMIS_FILE_SUFFIX = ".file.premis.xml";
  public static final String PREMIS_EVENT_SUFFIX = ".event.premis.xml";
  public static final String PREMIS_REPRESENTATION_SUFFIX = ".representation.premis.xml";
  public static final String PREMIS_AGENT_SUFFIX = ".agent.premis.xml";

  /* Preservation events fields */
  public static final String PRESERVATION_EVENT_ID = "id";
  public static final String PRESERVATION_EVENT_AIP_ID = "aipID";
  public static final String PRESERVATION_EVENT_REPRESENTATION_ID = "representationID";
  public static final String PRESERVATION_EVENT_FILE_ID = "fileID";
  public static final String PRESERVATION_EVENT_DATETIME = "eventDateTime";
  public static final String PRESERVATION_EVENT_DETAIL = "eventDetail";
  public static final String PRESERVATION_EVENT_TYPE = "eventType";
  public static final String PRESERVATION_EVENT_OUTCOME = "eventOutcome";
  public static final String PRESERVATION_EVENT_OUTCOME_DETAIL_EXTENSION = "eventOutcomeDetailExtension";
  public static final String PRESERVATION_EVENT_LINKING_AGENT_IDENTIFIER = "linkingAgentIdentifier";
  public static final String PRESERVATION_EVENT_LINKING_OUTCOME_OBJECT_IDENTIFIER = "linkingOutcomeObjectIdentifier";
  public static final Object PRESERVATION_EVENT_LINKING_SOURCE_OBJECT_IDENTIFIER = "linkingSourceObjectIdentifier";

  /* Preservation agents fields */
  public static final String PRESERVATION_AGENT_ID = "id";
  public static final String PRESERVATION_AGENT_ROLE = "role";
  public static final String PRESERVATION_AGENT_TITLE = "title";
  public static final String PRESERVATION_AGENT_TYPE = "type";
  public static final String PRESERVATION_AGENT_IDENTIFIER_TYPE = "identifierType";
  public static final String PRESERVATION_AGENT_IDENTIFIER_VALUE = "identifierValue";
  public static final String PRESERVATION_AGENT_VERSION = "version";

  public static final String PRESERVATION_TYPE_AGENT = "agent";
  public static final String PRESERVATION_TYPE_FILE = "file";
  public static final String PRESERVATION_TYPE_EVENT = "type";

  /*
   * accession adding emulation information appraisal capture compression
   * creation data carrier migration deaccession decompression decryption
   * deletion deselection digital signature validation dissemination file
   * extension change file system analysis file system extraction filename
   * change
   */
  public static final String PRESERVATION_EVENT_TYPE_FIXITY_CHECK = "fixity check";
  /*
   * forensic feature analysis
   */
  public static final String PRESERVATION_EVENT_TYPE_FORMAT_IDENTIFICATION = "format identification";

  public static final String PRESERVATION_EVENT_TYPE_FORMAT_VALIDATION = "format validation";
  /*
   * identifier assignment imaging Information Package merging Information
   * Package splitting ingest end ingest start
   */
  public static final String PRESERVATION_EVENT_TYPE_INGESTION = "ingestion";
  /*
   * message digest calculation metadata extraction (propertyExtraction)
   * metadata modification
   */
  public static final String PRESERVATION_EVENT_TYPE_MIGRATION = "migration";
  public static final String PRESERVATION_EVENT_TYPE_NORMALIZATION = "normalization";
  /*
   * object modification object validation quality review quarantine recovery
   * redaction replication SIP creation storage migration unpacking unquarantine
   */
  public static final String PRESERVATION_EVENT_TYPE_ANTIVIRUS_CHECK = "virus check";
  /*
   * wellformedness check
   */

  public static final String PRESERVATION_EVENT_AGENT_ROLE_INGEST_TASK = "ingest task";
  public static final String PRESERVATION_EVENT_AGENT_ROLE_PRESERVATION_TASK = "preservation task";
  public static final String PRESERVATION_EVENT_AGENT_ROLE_VALIDATION_TASK = "validation task";
  public static final String PRESERVATION_EVENT_AGENT_ROLE_EXECUTING_PROGRAM_TASK = "executing program task";

  public static final String PRESERVATION_EVENT_OBJECT_ROLE_TARGET = "target";

  public static final String PRESERVATION_AGENT_TYPE_INGEST_TASK = "software:ingest_task";
  public static final String PRESERVATION_AGENT_TYPE_MIGRATOR = "software:migrator";
  public static final String PRESERVATION_AGENT_TYPE_DIGITALIZATION = "software:digitalization";
  public static final String PRESERVATION_AGENT_TYPE_FIXITY_CHECK_PLUGIN = "software:plugin:fixity_check";
  public static final String PRESERVATION_AGENT_TYPE_CHARACTERIZATION_PLUGIN = "software:plugin:characterization";
  public static final String PRESERVATION_AGENT_TYPE_VIRUS_CHECK_PLUGIN = "software:plugin:virus_check";
  public static final String PRESERVATION_AGENT_TYPE_VERAPDF_CHECK_PLUGIN = "software:plugin:verapdf_check";
  public static final String PRESERVATION_AGENT_TYPE_CONVERSION_PLUGIN = "software:plugin:conversion";
  public static final String PRESERVATION_AGENT_TYPE_UNKNOWN_PREFIX = "unknown:";

  public static final String PRESERVATION_LEVEL_FULL = "full";

  /**
   * Only file integrity is preserved
   */
  public static final String PRESERVATION_LEVEL_BITLEVEL = "bitlevel";
  public static final String PRESERVATION_REGISTRY_PRONOM = "pronom";

  public static final String PREMIS_RELATIONSHIP_TYPE_STRUCTURAL = "structural";
  public static final String PREMIS_RELATIONSHIP_SUBTYPE_HASPART = "hasPart";

}
