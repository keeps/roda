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
  @Deprecated
  public static final String SOLRDATEFORMAT = ISO8601;
  public static final String SHA1 = "SHA-1";
  public static final String SHA256 = "SHA-256";
  public static final String MD5 = "MD5";
  public static final String LOCALE = "locale";
  public static final String DEFAULT_ENCODING = "UTF-8";
  public static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
  public static final int DEFAULT_PAGINATION_VALUE = 100;

  public enum DateGranularity {
    YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, MILLISECOND
  }

  public static final String CROSSWALKS_DISSEMINATION_HTML_PATH = "crosswalks/dissemination/html";
  public static final String UI_BROWSER_METADATA_DESCRIPTIVE_TYPES = "ui.browser.metadata.descriptive.types";
  public static final String I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX = "ui.browse.metadata.descriptive.type.";
  public static final String I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX = "crosswalks.dissemination.html.";
  public static final String I18N_UI_FACETS_PREFIX = "ui.facets.";

  /*
   * Installation (and most probably classpath as well) related variables
   */
  public static final String CORE_DESCRIPTION_LEVELS_FILE = "roda-description-levels-hierarchy.properties";
  public static final String CORE_CONFIG_FOLDER = "config";
  public static final String CORE_EXAMPLE_CONFIG_FOLDER = "example-config";
  public static final String CORE_I18N_FOLDER = "i18n";
  public static final String CORE_DATA_FOLDER = "data";
  public static final String CORE_STORAGE_FOLDER = "storage";
  public static final String CORE_STORAGE_HISTORY_FOLDER = "storage-history";
  public static final String CORE_INDEX_FOLDER = "index";
  public static final String CORE_CROSSWALKS_FOLDER = "crosswalks";
  public static final String CORE_ORCHESTRATOR_FOLDER = "orchestrator";
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
  public static final String CORE_THEME_FOLDER = "theme";
  public static final String CORE_EXAMPLE_THEME_FOLDER = "example-theme";
  public static final String CORE_RISK_FOLDER = "risk";
  public static final String CORE_AGENT_FOLDER = "agent";
  public static final String CORE_FORMAT_FOLDER = "format";
  public static final String CORE_NOTIFICATION_FOLDER = "notification";
  public static final String CORE_RISKINCIDENCE_FOLDER = "riskincidence";
  public static final String CORE_MAIL_TEMPLATE_FOLDER = "mail/templates";
  public static final String CORE_CERTIFICATES_FOLDER = "certificates";

  public static final String DEFAULT_NODE_HOSTNAME = "localhost";
  public static final String DEFAULT_NODE_PORT = "2551";

  /*
   * API related
   */
  public static final String API_SEP = "/";
  public static final String API_REST_V1_AIPS = "api/v1/aips/";
  public static final String API_REST_V1_REPRESENTATIONS = "api/v1/representations/";
  public static final String API_REST_V1_FILES = "api/v1/files/";
  public static final String API_REST_V1_TRANSFERRED = "api/v1/transferred";
  public static final String API_REST_V1_THEME = "api/v1/theme";
  // sub-resources strings
  public static final String API_DATA = "data";
  public static final Object API_FILE = "file";
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
  public static final String API_PATH_PARAM_FILE_UUID = "file_uuid";
  public static final String API_PATH_PARAM_METADATA_ID = "metadata_id";
  public static final String API_QUERY_PARAM_VERSION = "version";
  // http headers used
  public static final String API_HTTP_HEADER_ACCEPT = "Accept";
  // job related params
  public static final String API_PATH_PARAM_JOB_ID = "jobId";
  public static final String API_PATH_PARAM_JOB_JUST_FAILED = "jobJustFailed";

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
  public static final String INDEX_RISK = "Risk";
  public static final String INDEX_AGENT = "Agent";
  public static final String INDEX_FORMAT = "Format";
  public static final String INDEX_NOTIFICATION = "Notification";
  public static final String INDEX_RISK_INCIDENCE = "RiskIncidence";
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
  // public static final String STORAGE_CONTAINER_SIP = "SIP";
  public static final String STORAGE_CONTAINER_AIP = "aip";
  public static final String STORAGE_CONTAINER_PRESERVATION = "preservation";
  public static final String STORAGE_CONTAINER_ACTIONLOG = "action-log";
  public static final String STORAGE_CONTAINER_JOB = "job";
  public static final String STORAGE_CONTAINER_JOB_REPORT = "job-report";
  public static final String STORAGE_CONTAINER_RISK = "risk";
  public static final String STORAGE_CONTAINER_RISK_INCIDENCE = "risk-incidence";
  public static final String STORAGE_CONTAINER_AGENT = "agent";
  public static final String STORAGE_CONTAINER_FORMAT = "format";
  public static final String STORAGE_CONTAINER_NOTIFICATION = "notification";

  /*
   * STORAGE DIRECTORIES
   */
  public static final String STORAGE_DIRECTORY_METADATA = "metadata";
  public static final String STORAGE_DIRECTORY_DESCRIPTIVE = "descriptive";
  public static final String STORAGE_DIRECTORY_PRESERVATION = "preservation";
  public static final String STORAGE_DIRECTORY_OTHER_TECH_METADATA = "otherTechMd";
  public static final String STORAGE_DIRECTORY_REPRESENTATIONS = "representations";
  public static final String STORAGE_DIRECTORY_SUBMISSION = "submission";
  public static final String STORAGE_DIRECTORY_DOCUMENTATION = "documentation";
  public static final String STORAGE_DIRECTORY_SCHEMAS = "schemas";
  public static final String STORAGE_DIRECTORY_DATA = "data";
  public static final String STORAGE_DIRECTORY_AGENTS = "agents";
  public static final String STORAGE_DIRECTORY_OTHER = "other";

  public static final String STORAGE_AIP_METADATA_FILENAME = "aip.json";

  /*
   * OTHER METADATA TYPES
   */
  public static final String OTHER_METADATA_TYPE_RISK_INCIDENCE = "RiskIncidence";
  public static final String OTHER_METADATA_TYPE_SIEGFRIED = "Siegfried";
  public static final String OTHER_METADATA_TYPE_APACHE_TIKA = "ApacheTika";
  public static final String OTHER_METADATA_TYPE_DIGITAL_SIGNATURE = "DigitalSignature";
  public static final String OTHER_METADATA_TYPE_DROID = "DROID";
  public static final String OTHER_METADATA_TYPE_EXIFTOOL = "ExifTool";
  public static final String OTHER_METADATA_TYPE_FFPROBE = "FFProbe";
  public static final String OTHER_METADATA_TYPE_FITS = "FITS";
  public static final String OTHER_METADATA_TYPE_JHOVE = "JHOVE";
  public static final String OTHER_METADATA_TYPE_JPYLYZER = "jpylyzer";
  public static final String OTHER_METADATA_TYPE_MEDIAINFO = "MediaInfo";

  /*
   * Permissions
   */
  public static final String INDEX_PERMISSION_USERS_PREFIX = "permission_users_";
  public static final String INDEX_PERMISSION_GROUPS_PREFIX = "permission_groups_";

  /*
   * AIP FIELDS
   */
  public static final String AIP_ID = "id";
  public static final String AIP_PARENT_ID = "parentId";
  public static final String AIP_ANCESTORS = "ancestors";
  public static final String STATE = "state";
  public static final String INGEST_SIP_ID = "ingestSIPId";
  public static final String INGEST_JOB_ID = "ingestJobId";

  public static final String AIP_DESCRIPTIVE_METADATA_ID = "descriptiveMetadataId";
  public static final String AIP_REPRESENTATION_ID = "representationId";
  public static final String AIP_HAS_REPRESENTATIONS = "hasRepresentations";
  // public static final String AIP_PRESERVATION_OBJECTS_ID =
  // "preservationObjectsId";
  // public static final String AIP_PRESERVATION_EVENTS_ID =
  // "preservationEventsId";

  public static final String AIP_NUMBER_OF_SUBMISSION_FILES = "numberOfSubmissionFiles";
  public static final String AIP_NUMBER_OF_DOCUMENTATION_FILES = "numberOfDocumentationFiles";
  public static final String AIP_NUMBER_OF_SCHEMA_FILES = "numberOfSchemaFiles";

  public static final String AIP_LEVEL = "level";
  public static final String AIP_TITLE = "title";
  public static final String AIP_TITLE_SORT = "title_sort";
  public static final String AIP_DATE_INITIAL = "dateInitial";
  public static final String AIP_DATE_FINAL = "dateFinal";
  public static final String AIP_CHILDREN_COUNT = "childrenCount";
  public static final String AIP_DESCRIPTION = "description";
  public static final String AIP_STATE = "state";
  public static final String AIP_LABEL = "label";
  public static final String AIP_SEARCH = "search";

  // AIP types
  public static final String AIP_TYPE_MIXED = "MIXED";
  /*
   * Representation FIELDS
   */
  public static final String REPRESENTATION_UUID = "uuid";
  public static final String REPRESENTATION_ID = "id";
  public static final String REPRESENTATION_AIP_ID = "aipId";
  public static final String REPRESENTATION_ORIGINAL = "original";
  public static final String REPRESENTATION_TYPE = "type";
  public static final String REPRESENTATION_SIZE_IN_BYTES = "sizeInBytes";
  public static final String REPRESENTATION_NUMBER_OF_DATA_FILES = "numberOfDataFiles";
  public static final String REPRESENTATION_NUMBER_OF_DOCUMENTATION_FILES = "numberOfDocumentationFiles";
  public static final String REPRESENTATION_NUMBER_OF_SCHEMA_FILES = "numberOfSchemaFiles";
  public static final String REPRESENTATION_SEARCH = "search";

  // Representation types
  public static final String REPRESENTATION_TYPE_MIXED = "MIXED";
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
  public static final String OBJECT_PERMISSIONS_USER_GROUP = "users";

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
  public static final String LOG_SEARCH = "search";

  // MEMBER: USER or GROUP
  public static final String MEMBERS_ID = "id";
  public static final String MEMBERS_NAME = "name";
  // public static final String MEMBERS_FULLNAME = "fullName";
  public static final String MEMBERS_IS_ACTIVE = "isActive";
  public static final String MEMBERS_IS_USER = "isUser";
  public static final String MEMBERS_GROUPS_ALL = "groupsAll";
  public static final String MEMBERS_ROLES_ALL = "rolesAll";

  public static final String TRANSFERRED_RESOURCE_ID = "id";
  public static final String TRANSFERRED_RESOURCE_UUID = "uuid";
  public static final String TRANSFERRED_RESOURCE_FULLPATH = "fullPath";
  public static final String TRANSFERRED_RESOURCE_PARENT_ID = "parentId";
  public static final String TRANSFERRED_RESOURCE_PARENT_UUID = "parentUUID";
  public static final String TRANSFERRED_RESOURCE_RELATIVEPATH = "relativePath";
  public static final String TRANSFERRED_RESOURCE_DATE = "date";
  public static final String TRANSFERRED_RESOURCE_ISFILE = "isFile";
  public static final String TRANSFERRED_RESOURCE_NAME = "name";
  public static final String TRANSFERRED_RESOURCE_SIZE = "size";
  public static final String TRANSFERRED_RESOURCE_ANCESTORS = "ancestors";
  public static final String TRANSFERRED_RESOURCE_LAST_SCAN_DATE = "lastScanDate";
  public static final String TRANSFERRED_RESOURCE_SEARCH = "search";
  // REST
  public static final String TRANSFERRED_RESOURCE_DIRECTORY_NAME = "name";
  public static final String TRANSFERRED_RESOURCE_RESOURCE_ID = "resourceId";

  public static final String JOB_ID = "id";
  public static final String JOB_NAME = "name";
  public static final String JOB_USERNAME = "username";
  public static final String JOB_START_DATE = "startDate";
  public static final String JOB_END_DATE = "endDate";
  public static final String JOB_STATE = "state";
  public static final String JOB_STATE_DETAILS = "stateDetails";
  public static final String JOB_COMPLETION_PERCENTAGE = "completionPercentage";
  public static final String JOB_SOURCE_OBJECTS_COUNT = "sourceObjectsCount";
  public static final String JOB_SOURCE_OBJECTS_WAITING_TO_BE_PROCESSED = "sourceObjectsWaitingToBeProcessed";
  public static final String JOB_SOURCE_OBJECTS_BEING_PROCESSED = "sourceObjectsBeingProcessed";
  public static final String JOB_SOURCE_OBJECTS_PROCESSED_WITH_SUCCESS = "sourceObjectsProcessedWithSuccess";
  public static final String JOB_SOURCE_OBJECTS_PROCESSED_WITH_FAILURE = "sourceObjectsProcessedWithFailure";
  public static final String JOB_OUTCOME_OBJECTS_WITH_MANUAL_INTERVENTION = "outcomeObjectsWithManualIntervention";
  public static final String JOB_PLUGIN = "plugin";
  public static final String JOB_PLUGIN_TYPE = "pluginType";
  public static final String JOB_PLUGIN_PARAMETERS = "pluginParameters";
  public static final String JOB_RESOURCE_TYPE = "resourceType";
  public static final String JOB_SOURCE_OBJECTS = "sourceObjects";
  public static final String JOB_OUTCOME_OBJECTS_CLASS = "outcomeObjectsClass";
  public static final String JOB_FILE_EXTENSION = ".json";

  /* Plugins related parameters */
  public static final String PLUGIN_PARAMS_JOB_ID = "job.id";

  public static final String PLUGIN_PARAMS_DO_FULLTEXT_EXTRACTION = "parameter.do_fulltext_extraction";
  public static final String PLUGIN_PARAMS_TOTAL_STEPS = "parameter.total_steps";
  public static final String PLUGIN_PARAMS_INT_VALUE = "parameter.int_value";
  public static final String PLUGIN_PARAMS_STRING_VALUE = "parameter.string_value";
  public static final String PLUGIN_PARAMS_BOOLEAN_VALUE = "parameter.boolean_value";
  public static final String PLUGIN_PARAMS_CLEAR_INDEXES = "parameter.clear_indexes";
  public static final String PLUGIN_PARAMS_RECURSIVE_LISTING = "parameter.recursive_listing";
  public static final String PLUGIN_PARAMS_CLASS_CANONICAL_NAME = "parameter.class_canonical_name";
  public static final String PLUGIN_PARAMS_SIP_TO_AIP_CLASS = "parameter.sip_to_aip_class";
  public static final String PLUGIN_PARAMS_PARENT_ID = "parameter.parent_id";
  public static final String PLUGIN_PARAMS_FORCE_PARENT_ID = "parameter.force_parent_id";
  public static final String PLUGIN_PARAMS_RISK_ID = "parameter.risk_id";
  public static final String PLUGIN_PARAMS_RISK_NAME = "parameter.risk_name";
  public static final String PLUGIN_PARAMS_RISK_CATEGORY = "parameter.risk_category";
  public static final String PLUGIN_PARAMS_DO_VIRUS_CHECK = "parameter.do_virus_check";
  public static final String PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION = "parameter.do_descriptive_metadata_validation";
  public static final String PLUGIN_PARAMS_CREATE_PREMIS_SKELETON = "parameter.create.premis.skeleton";
  public static final String PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION = "parameter.do_file_format_identification";
  public static final String PLUGIN_PARAMS_DO_PDFTOPDFA_CONVERSION = "parameter.do_pdf_to_pdfa_conversion";
  public static final String PLUGIN_PARAMS_DO_VERAPDF_CHECK = "parameter.do_verapdf_check";
  public static final String PLUGIN_PARAMS_DO_FEATURE_EXTRACTION = "parameter.do_feature_extraction";
  public static final String PLUGIN_PARAMS_DO_FULL_TEXT_EXTRACTION = "parameter.do_fulltext_extraction";
  public static final String PLUGIN_PARAMS_DO_DIGITAL_SIGNATURE_VALIDATION = "parameter.do_digital_signature_validation";
  public static final String PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK = "parameter.do_producer_authorization_check";
  public static final String PLUGIN_PARAMS_DO_AUTO_ACCEPT = "parameter.do_auto_accept";
  public static final String PLUGIN_PARAMS_EMAIL_NOTIFICATION = "parameter.email_notification";
  public static final String PLUGIN_PARAMS_CREATES_PLUGIN_EVENT = "createsPluginEvent";
  public static final String PLUGIN_PARAMS_CREATE_SUBMISSION = "parameter.create_submission";
  public static final String PLUGIN_PARAMS_IGNORE_OTHER_FILES = "parameter.ignore_other_files";
  public static final String PLUGIN_PARAMS_PDF_PROFILE = "parameter.pdf_profile";
  public static final String PLUGIN_PARAMS_SIGNATURE_VERIFY = "parameter.signature_verify";
  public static final String PLUGIN_PARAMS_SIGNATURE_EXTRACT = "parameter.signature_extract";
  public static final String PLUGIN_PARAMS_SIGNATURE_STRIP = "parameter.signature_strip";
  public static final String PLUGIN_PARAMS_INPUT_FORMAT = "parameter.input_format";
  public static final String PLUGIN_PARAMS_OUTPUT_FORMAT = "parameter.output_format";
  public static final String PLUGIN_PARAMS_COMMAND_ARGUMENTS = "parameter.command_arguments";
  public static final String PLUGIN_PARAMS_REPORTING_CLASS = "parameter.reporting_class";

  public static final String PLUGIN_CATEGORY_FORMAT_CONVERSION = "format_conversion";
  public static final String PLUGIN_CATEGORY_CHARACTERISATION = "characterisation";
  public static final String PLUGIN_CATEGORY_FORMAT_VALIDATION = "format_validation";
  public static final String PLUGIN_CATEGORY_RISK_MANAGEMENT = "risk_management";
  public static final String PLUGIN_CATEGORY_FEATURE_EXTRACTION = "feature_extraction";
  public static final String PLUGIN_CATEGORY_FORMAT_IDENTIFICATION = "format_identification";
  public static final String PLUGIN_CATEGORY_INGEST = "ingest";
  public static final String PLUGIN_CATEGORY_FIXITY_CHECK = "fixity_check";
  public static final String PLUGIN_CATEGORY_REINDEX = "reindex";
  public static final String PLUGIN_CATEGORY_REPLICATION = "replication";

  public static final String PLUGIN_CATEGORY_NOT_LISTABLE = "not_listable";

  /* Job Report */
  public static final String JOB_REPORT_ID = "id";
  public static final String JOB_REPORT_JOB_ID = "jobId";
  public static final String JOB_REPORT_SOURCE_OBJECT_ID = "sourceObjectId";
  public static final String JOB_REPORT_SOURCE_OBJECT_CLASS = "sourceObjectClass";
  public static final String JOB_REPORT_SOURCE_OBJECT_ORIGINAL_ID = "sourceObjectOriginalId";
  public static final String JOB_REPORT_OUTCOME_OBJECT_ID = "outcomeObjectId";
  public static final String JOB_REPORT_OUTCOME_OBJECT_CLASS = "outcomeObjectClass";
  public static final String JOB_REPORT_OUTCOME_OBJECT_STATE = "outcomeObjectState";
  public static final String JOB_REPORT_TITLE = "title";
  public static final String JOB_REPORT_DATE_CREATED = "dateCreated";
  public static final String JOB_REPORT_DATE_UPDATE = "dateUpdated";
  public static final String JOB_REPORT_COMPLETION_PERCENTAGE = "completionPercentage";
  public static final String JOB_REPORT_STEPS_COMPLETED = "stepsCompleted";
  public static final String JOB_REPORT_TOTAL_STEPS = "totalSteps";
  public static final String JOB_REPORT_PLUGIN = "plugin";
  public static final String JOB_REPORT_PLUGIN_VERSION = "pluginVersion";
  public static final String JOB_REPORT_PLUGIN_STATE = "pluginState";
  public static final String JOB_REPORT_PLUGIN_DETAILS = "pluginDetails";
  public static final String JOB_REPORT_HTML_PLUGIN_DETAILS = "htmlPluginDetails";
  public static final String JOB_REPORT_REPORTS = "reports";
  public static final String JOB_REPORT_FILE_EXTENSION = ".json";

  public static final String FILE_UUID = "uuid";
  public static final String FILE_PATH = "path";
  public static final String FILE_PARENT_UUID = "parentUUID";
  public static final String FILE_AIPID = "aipId";
  public static final String FILE_FORMAT_MIMETYPE = "formatMimetype";
  public static final String FILE_FORMAT_VERSION = "formatVersion";
  public static final String FILE_ID = "id";
  public static final String FILE_FILEID = "fileId";
  public static final String FILE_REPRESENTATION_UUID = "representationUUID";
  public static final String FILE_REPRESENTATION_ID = "representationId";
  public static final String FILE_STORAGE_PATH = "storagePath";
  public static final String FILE_ISENTRYPOINT = "isEntryPoint";
  public static final String FILE_FILEFORMAT = "fileFormat";
  public static final String FILE_STORAGEPATH = "storagePath";
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

  /* Risks */
  public static final String RISK_ID = "id";
  public static final String RISK_NAME = "name";
  public static final String RISK_DESCRIPTION = "description";
  public static final String RISK_IDENTIFIED_ON = "identifiedOn";
  public static final String RISK_IDENTIFIED_BY = "identifiedBy";
  public static final String RISK_CATEGORY = "category";
  public static final String RISK_NOTES = "notes";

  public static final String RISK_PRE_MITIGATION_PROBABILITY = "preMitigationProbability";
  public static final String RISK_PRE_MITIGATION_IMPACT = "preMitigationImpact";
  public static final String RISK_PRE_MITIGATION_SEVERITY = "preMitigationSeverity";
  public static final String RISK_PRE_MITIGATION_SEVERITY_LEVEL = "preMitigationSeverityLevel";
  public static final String RISK_PRE_MITIGATION_NOTES = "preMitigationNotes";

  public static final String RISK_POS_MITIGATION_PROBABILITY = "posMitigationProbability";
  public static final String RISK_POS_MITIGATION_IMPACT = "posMitigationImpact";
  public static final String RISK_POS_MITIGATION_SEVERITY = "posMitigationSeverity";
  public static final String RISK_POS_MITIGATION_SEVERITY_LEVEL = "posMitigationSeverityLevel";
  public static final String RISK_POS_MITIGATION_NOTES = "posMitigationNotes";

  public static final String RISK_MITIGATION_STRATEGY = "mitigationStrategy";
  public static final String RISK_MITIGATION_OWNER_TYPE = "mitigationOwnerType";
  public static final String RISK_MITIGATION_OWNER = "mitigationOwner";
  public static final String RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_TYPE = "mitigationRelatedEventIdentifierType";
  public static final String RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_VALUE = "mitigationRelatedEventIdentifierValue";
  public static final String RISK_OBJECTS_SIZE = "objectsSize";

  public static final String RISK_FILE_EXTENSION = ".json";
  public static final String RISK_SEARCH = "search";

  public static final String RISK_INCIDENCE_ID = "id";
  public static final String RISK_INCIDENCE_AIP_ID = "aipId";
  public static final String RISK_INCIDENCE_REPRESENTATION_ID = "representationId";
  public static final String RISK_INCIDENCE_FILE_PATH = "filePath";
  public static final String RISK_INCIDENCE_FILE_ID = "fileId";
  public static final String RISK_INCIDENCE_OBJECT_CLASS = "objectClass";
  public static final String RISK_INCIDENCE_RISKS = "risks";

  public static final String RISK_INCIDENCE_FILE_EXTENSION = ".json";
  public static final String RISK_INCIDENCE_SEARCH = "search";

  /* Agents */
  public static final String AGENT_ID = "id";
  public static final String AGENT_NAME = "name";
  public static final String AGENT_TYPE = "type";
  public static final String AGENT_DESCRIPTION = "description";
  public static final String AGENT_CATEGORY = "category";
  public static final String AGENT_VERSION = "version";
  public static final String AGENT_LICENSE = "license";
  public static final String AGENT_POPULARITY = "popularity";
  public static final String AGENT_DEVELOPER = "developer";
  public static final String AGENT_INITIAL_RELEASE = "initialRelease";
  public static final String AGENT_WEBSITE = "website";
  public static final String AGENT_DOWNLOAD = "download";
  public static final String AGENT_PROVENANCE_INFORMATION = "provenanceInformation";
  public static final String AGENT_PLATFORMS = "platforms";
  public static final String AGENT_EXTENSIONS = "extensions";
  public static final String AGENT_MIMETYPES = "mimetypes";
  public static final String AGENT_PRONOMS = "pronoms";
  public static final String AGENT_UTIS = "utis";
  public static final String AGENT_FORMAT_IDS = "formatIds";
  public static final String AGENT_AGENTS_REQUIRED = "agentsRequired";

  public static final String AGENT_FILE_EXTENSION = ".json";

  public static final String AGENT_SEARCH = "search";

  /* Formats */
  public static final String FORMAT_ID = "id";
  public static final String FORMAT_NAME = "name";
  public static final String FORMAT_DEFINITION = "definition";
  public static final String FORMAT_CATEGORY = "category";
  public static final String FORMAT_LATEST_VERSION = "latestVersion";
  public static final String FORMAT_POPULARITY = "popularity";
  public static final String FORMAT_DEVELOPER = "developer";
  public static final String FORMAT_INITIAL_RELEASE = "initialRelease";
  public static final String FORMAT_STANDARD = "standard";
  public static final String FORMAT_IS_OPEN_FORMAT = "isOpenFormat";
  public static final String FORMAT_WEBSITE = "website";
  public static final String FORMAT_PROVENANCE_INFORMATION = "provenanceInformation";
  public static final String FORMAT_EXTENSIONS = "extensions";
  public static final String FORMAT_MIMETYPES = "mimetypes";
  public static final String FORMAT_PRONOMS = "pronoms";
  public static final String FORMAT_UTIS = "utis";

  public static final String FORMAT_FILE_EXTENSION = ".json";

  public static final String FORMAT_SEARCH = "search";

  /* Messages */
  public static final String NOTIFICATION_ID = "id";
  public static final String NOTIFICATION_SUBJECT = "subject";
  public static final String NOTIFICATION_BODY = "body";
  public static final String NOTIFICATION_SENT_ON = "sentOn";
  public static final String NOTIFICATION_FROM_USER = "fromUser";
  public static final String NOTIFICATION_RECIPIENT_USERS = "recipientUsers";
  public static final String NOTIFICATION_ACKNOWLEDGE_TOKEN = "acknowledgeToken";
  public static final String NOTIFICATION_IS_ACKNOWLEDGED = "isAcknowledged";
  public static final String NOTIFICATION_ACKNOWLEDGED_USERS = "acknowledgedUsers";

  public static final String NOTIFICATION_FILE_EXTENSION = ".json";
  public static final String NOTIFICATION_SEARCH = "search";
  public static final String NOTIFICATION_VARIOUS_RECIPIENT_USERS = "various-recipient-users";

  /* View representation */
  public static final String VIEW_REPRESENTATION_DESCRIPTION_LEVEL = "description-level-representation";
  public static final String VIEW_REPRESENTATION_REPRESENTATION = "representation";
  public static final String VIEW_REPRESENTATION_FOLDER = "folder";
  public static final String VIEW_REPRESENTATION_FILE = "file";

  /*
   * PREMIS
   */
  public static final String PREMIS_IDENTIFIER_TYPE_LOCAL = "local";
  public static final String PREMIS_FILE_SUFFIX = ".file.premis.xml";
  public static final String PREMIS_EVENT_SUFFIX = ".event.premis.xml";
  public static final String PREMIS_REPRESENTATION_SUFFIX = ".representation.premis.xml";
  public static final String PREMIS_AGENT_SUFFIX = ".agent.premis.xml";

  /*
   * Other Preservation metadata
   */
  public static final String OTHER_TECH_METADATA_FILE_SUFFIX = ".other.tech.md";

  /* Preservation events fields */
  public static final String PRESERVATION_EVENT_ID = "id";
  public static final String PRESERVATION_EVENT_AIP_ID = "aipID";
  public static final String PRESERVATION_EVENT_REPRESENTATION_ID = "representationID";
  public static final String PRESERVATION_EVENT_REPRESENTATION_UUID = "representationUUID";
  public static final String PRESERVATION_EVENT_FILE_ID = "fileID";
  public static final String PRESERVATION_EVENT_DATETIME = "eventDateTime";
  public static final String PRESERVATION_EVENT_DETAIL = "eventDetail";
  public static final String PRESERVATION_EVENT_TYPE = "eventType";
  public static final String PRESERVATION_EVENT_OUTCOME = "eventOutcome";
  public static final String PRESERVATION_EVENT_OUTCOME_DETAIL_EXTENSION = "eventOutcomeDetailExtension";
  public static final String PRESERVATION_EVENT_OUTCOME_DETAIL_NOTE = "eventOutcomeDetailNote";
  public static final String PRESERVATION_EVENT_LINKING_AGENT_IDENTIFIER = "linkingAgentIdentifier";
  public static final String PRESERVATION_EVENT_LINKING_OUTCOME_OBJECT_IDENTIFIER = "linkingOutcomeObjectIdentifier";
  public static final String PRESERVATION_EVENT_LINKING_SOURCE_OBJECT_IDENTIFIER = "linkingSourceObjectIdentifier";

  /* Preservation agents fields */
  public static final String PRESERVATION_AGENT_ID = "id";
  public static final String PRESERVATION_AGENT_NAME = "name";
  public static final String PRESERVATION_AGENT_TYPE = "type";
  public static final String PRESERVATION_AGENT_EXTENSION = "extension";
  public static final Object PRESERVATION_AGENT_VERSION = "version";
  public static final String PRESERVATION_AGENT_NOTE = "note";
  public static final String PRESERVATION_AGENT_ROLES = "roles";

  public static final String PRESERVATION_TYPE_AGENT = "agent";
  public static final String PRESERVATION_TYPE_FILE = "file";
  public static final String PRESERVATION_TYPE_EVENT = "type";

  public static final String PRESERVATION_EVENT_AGENT_ROLE_INGEST_TASK = "ingest task";
  public static final String PRESERVATION_EVENT_AGENT_ROLE_PRESERVATION_TASK = "preservation task";
  public static final String PRESERVATION_EVENT_AGENT_ROLE_VALIDATION_TASK = "validation task";
  public static final String PRESERVATION_EVENT_AGENT_ROLE_EXECUTING_PROGRAM_TASK = "executing program task";

  public static final String PRESERVATION_EVENT_OBJECT_ROLE_TARGET = "target";
  public static final String PRESERVATION_LEVEL_FULL = "full";

  /**
   * Only file integrity is preserved
   */
  public static final String PRESERVATION_LEVEL_BITLEVEL = "bitlevel";
  public static final String PRESERVATION_REGISTRY_PRONOM = "pronom";
  public static final String PRESERVATION_REGISTRY_MIME = "mime";

  public static final String PREMIS_RELATIONSHIP_TYPE_STRUCTURAL = "structural";
  public static final String PREMIS_RELATIONSHIP_SUBTYPE_HASPART = "hasPart";

  public static final String LINKING_IDENTIFIER_VALUE = "value";
  public static final String LINKING_IDENTIFIER_TYPE = "type";
  public static final String LINKING_IDENTIFIER_ROLES = "roles";

  public static final String PRESERVATION_LINKING_OBJECT_SOURCE = "source";
  public static final String PRESERVATION_LINKING_OBJECT_OUTCOME = "outcome";

  public enum PreservationEventType {
    CREATION("creation"), DEACCESSION("deaccession"), DECOMPRESSION("decompression"), NORMALIZATION("normalization"),
    FIXITY_CHECK("fixity check"), VALIDATION("validation"), CAPTURE("capture"), REPLICATION("replication"),
    MESSAGE_DIGEST_CALCULATION("message digest calculation"), VIRUS_CHECK("virus check"),
    DIGITAL_SIGNATURE_VALIDATION("digital signature validation"), COMPRESSION("compression"), DELETION("deletion"),
    MIGRATION("migration"), INGESTION("ingestion"), DECRYPTION("decryption"),
    // extra types...
    WELLFORMEDNESS_CHECK("wellformedness check"), UNPACKING("unpacking"), METADATA_EXTRACTION("metadata extraction"),
    ACCESSION("accession"), AUTHORIZATION_CHECK("authorization check"), FORMAT_IDENTIFICATION("format identification"),
    FORMAT_VALIDATION("format validation"), INGEST_START("ingest start"), INGEST_END("ingest end"),
    RISK_MANAGEMENT("risk management");

    private final String text;

    private PreservationEventType(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }
  }

  public enum ExportType {
    SINGLE_ZIP, MULTI_ZIP, FOLDER;
  }

  public enum PreservationAgentType {
    HARDWARE("hardware"), ORGANIZATION("organization"), PERSON("person"), SOFTWARE("software");

    private final String text;

    private PreservationAgentType(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }
  }

  /*
   * WUI related
   */
  public static final String THEME_RESOURCES_PATH = "/org/roda/wui/public/theme/";

  public static final String SEARCH_LIST_BOX_ITEMS = "items";
  public static final String SEARCH_LIST_BOX_REPRESENTATIONS = "representations";
  public static final String SEARCH_LIST_BOX_FILES = "files";
  public static final String SEARCH_FIELD_TYPE_TEXT = "text";
  public static final String SEARCH_FIELD_TYPE_DATE = "date";
  public static final String SEARCH_FIELD_TYPE_DATE_INTERVAL = "date_interval";
  public static final String SEARCH_FIELD_TYPE_NUMERIC = "numeric";
  public static final String SEARCH_FIELD_TYPE_NUMERIC_INTERVAL = "numeric_interval";
  public static final String SEARCH_FIELD_TYPE_STORAGE = "storage";
  public static final String SEARCH_FIELD_TYPE_BOOLEAN = "boolean";
  public static final String SEARCH_FIELD_TYPE_SUGGEST = "suggest";

  public static final String METADATA_VERSION_SEPARATOR = "_";

  public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  /* Template Constants */
  public static final String INGEST_EMAIL_TEMPLATE = "ingestion-template.vm";
  public static final String VERIFICATION_EMAIL_TEMPLATE = "emailverification_html.vm";
  public static final String RECOVER_LOGIN_EMAIL_TEMPLATE = "recoverlogin_html.vm";
  public static final String NOTIFY_PRODUCER_EMAIL_TEMPLATE = "notifyproducer_html.vm";

}
