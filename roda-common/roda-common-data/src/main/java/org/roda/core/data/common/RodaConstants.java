/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.roda.core.data.v2.ip.Permissions.PermissionType;

public final class RodaConstants {

  /*
   * RODA Core properties (provided via -D in the command-line)
   */
  public static final String CORE_NODE_TYPE = "roda.node.type";
  public static final String CORE_NODE_INSTANCE_ID = "roda.node.instanceId";
  public static final String CORE_NODE_PORT = "roda.node.port";
  public static final String INSTALL_FOLDER_SYSTEM_PROPERTY = "roda.home";

  /*
   * RODA Core properties (provided via environment)
   */
  public static final String CORE_ESSENTIAL_DIRECTORY_PREFIX = "RODA_CORE_DIRECTORY_";

  /*
   * RODA Core properties (provided via configuration file)
   */
  public static final String CORE_STORAGE_TYPE = "core.storage.type";
  public static final String CORE_STORAGE_NEW_SERVICE = "core.storage.new_service";
  public static final String CORE_STORAGE_FEDORA4_URL = "core.storage.fedora4.url";
  public static final String CORE_STORAGE_FEDORA4_USERNAME = "core.storage.fedora4.username";
  public static final String CORE_STORAGE_FEDORA4_PASSWORD = "core.storage.fedora4.password";

  public static final String CORE_STAGING_STORAGE_PATH = "core.staging.storage.path";
  public static final String CORE_SOLR_TYPE = "core.solr.type";
  public static final String CORE_SOLR_HTTP_URL = "core.solr.http.url";
  public static final String CORE_SOLR_CLOUD_URLS = "core.solr.cloud.urls";
  public static final String CORE_SOLR_STEMMING_LANGUAGE = "core.solr.stemming.language";

  public static final String CORE_EVENTS_ENABLED = "core.events.enabled";
  public static final String CORE_EVENTS_NOTIFIER_AND_HANDLER_ARE_THE_SAME = "core.events.notifier_and_handler_are_the_same";
  public static final String CORE_EVENTS_NOTIFIER_CLASS = "core.events.notifier_class";
  public static final String CORE_EVENTS_HANDLER_CLASS = "core.events.handler_class";

  public static final String CORE_ACTION_LOGS_PRIMARY_USER = "core.action_logs.primary.user";
  public static final String CORE_ACTION_LOGS_PRIMARY_PASS = "core.action_logs.primary.pass";
  public static final String CORE_ACTION_LOGS_PRIMARY_URL = "core.action_logs.primary.url";
  public static final String CORE_ACTION_LOGS_PRIMARY_RESOURCE = "core.action_logs.primary.resource";
  public static final String CORE_ACTION_LOGS_REPLICA_WRITE_IN_SOLR = "core.action_logs.replica.write.solr";

  public static final String CORE_SYNCHRONIZATION_BUNDLE_PATH = "core.synchronization.bundle.path";

  public static final String CORE_WEB_BASIC_AUTH_DISABLE = "core.web.basicAuth.disable";
  public static final String CORE_WEB_BASIC_AUTH_WHITELIST = "core.web.basicAuth.whitelist[]";
  public static final String CORE_API_BASIC_AUTH_DISABLE = "core.api.basicAuth.disable";
  public static final String CORE_API_BASIC_AUTH_WHITELIST = "core.api.basicAuth.whitelist[]";

  public static final String TRASH_CONTAINER = "trash";

  public static final String TRANSFERRED_RESOURCES_PROCESSED_FOLDER = "PROCESSED";
  public static final String TRANSFERRED_RESOURCES_SUCCESSFULLY_INGESTED_FOLDER = "SUCCESSFULLY_INGESTED";
  public static final String TRANSFERRED_RESOURCES_UNSUCCESSFULLY_INGESTED_FOLDER = "UNSUCCESSFULLY_INGESTED";
  public static final String CORE_TRANSFERRED_RESOURCES_INGEST_MOVE_WHEN_AUTOACCEPT = "core.ingest.processed.move_when_autoaccept";
  public static final String CORE_TRANSFERRED_RESOURCES_DELETE_WHEN_SUCCESSFULLY_INGESTED = "core.ingest.delete_transfer_resource_after_successfully_ingested";
  public static final String CORE_INGEST_SKIP_FIX_PARENTS = "core.ingest.skip.fix.parents";

  public static final String CORE_STORAGE_LEGACY_IMPLEMENTATION_ENABLED = "core.storage.legacy.implementation.enabled";

  public static final String CORE_EXTERNAL_AUTH_GROUP_MAPPING_ENABLED = "core.authorization.external.group_mapping";
  public static final String CORE_EXTERNAL_AUTH_GROUPS_ATTRIBUTE = "core.authorization.external.attribute";
  public static final String CORE_EXTERNAL_AUTH_GROUP_MAPPINGS = "core.authorization.external.mappings[]";
  public static final String CORE_EXTERNAL_AUTH_GROUP_MAPPING_PREFIX = "core.authorization.external.mapping";
  public static final String CORE_EXTERNAL_AUTH_GROUP_MAPPING_INTERNAL_SUFFIX = "internal.groups[]";
  public static final String CORE_EXTERNAL_AUTH_GROUP_MAPPING_EXTERNAL_SUFFIX = "external.group";
  /*
   * Misc
   */
  public static final String INSTALL_FOLDER_ENVIRONMENT_VARIABLE = "RODA_HOME";
  public static final String GWT_RPC_BASE_URL = "gwtrpc/";

  /**
   * XXX Use DateTimeFormatter.ISO_INSTANT instead when GWT supports Instant
   */
  public static final String ISO8601_NO_MILLIS = "yyyy-MM-dd'T'HH:mm:ssX";
  public static final String ISO8601_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

  public static final String SIMPLE_DATE_FORMATTER = "yyyy-MM-dd";
  public static final String SHA1 = "SHA-1";
  public static final String SHA256 = "SHA-256";
  public static final String MD5 = "MD5";
  public static final String LOCALE = "locale";
  public static final String DEFAULT_ENCODING = "UTF-8";
  public static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
  public static final int DEFAULT_PAGINATION_VALUE = 100;
  public static final String DEFAULT_PAGINATION_STRING_VALUE = "100";

  public static final List<String> DEFAULT_ALGORITHMS = Arrays.asList(SHA256);

  public enum DateGranularity {
    YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, MILLISECOND
  }

  public static final String CROSSWALKS_DISSEMINATION_HTML_PATH = "crosswalks/dissemination/html/";
  public static final String CROSSWALKS_DISSEMINATION_HTML_EVENT_PATH = "crosswalks/dissemination/html/event.xslt";
  public static final String CROSSWALKS_DISSEMINATION_OTHER_PATH = "crosswalks/other/";
  public static final String UI_BROWSER_METADATA_DESCRIPTIVE_TYPES = "ui.browser.metadata.descriptive.types";
  public static final String I18N_UI_BROWSE_METADATA_DESCRIPTIVE_TYPE_PREFIX = "ui.browse.metadata.descriptive.type.";
  public static final String I18N_UI_BROWSE_METADATA_TECHNICAL_TYPE_PREFIX = "ui.browse.metadata.technical.type.";
  public static final String I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX = "crosswalks.dissemination.html.";
  public static final String I18N_CROSSWALKS_DISSEMINATION_HTML_PREFIX_EVENT = "crosswalks.dissemination.html.event.";
  public static final String I18N_UI_FACETS_PREFIX = "ui.facets";
  public static final String I18N_UI_APPRAISAL = "ui.appraisal";

  public static final String UI_DATE_FORMAT_TITLE = "ui.date.format.title";
  public static final String UI_DATE_FORMAT_SIMPLE = "ui.date.format.simple";
  public static final String UI_DATE_TIME_FORMAT_SIMPLE = "ui.dateTime.format.simple";
  public static final String UI_DATE_TIME_FORMAT_UTC = "ui.dateTime.format.UTC";

  public static final String UI_COOKIES_ACTIVE_PROPERTY = "ui.cookies.active";

  public static final String UI_EXPIRED_SESSION_DETECTOR_ACTIVE = "ui.expired.session.detector.active";
  public static final String UI_EXPIRED_SESSION_DETECTOR_TIME = "ui.expired.session.detector.time";

  public static final String UI_GOOGLE_ANALYTICS_CODE_PROPERTY = "ui.google.analytics.code";
  public static final String UI_GOOGLE_RECAPTCHA_CODE_PROPERTY = "ui.google.recaptcha.code";

  public static final String UI_LISTS_PROPERTY = "ui.lists";

  public static final String UI_LISTS_FACETS_QUERY_PROPERTY = "facets.query";
  public static final String UI_LISTS_FACETS_PARAMETERS_PROPERTY = "facets.parameters";
  public static final String UI_LISTS_FACETS_PARAMETERS_TYPE_PROPERTY = "type";
  public static final String UI_LISTS_FACETS_PARAMETERS_ARGS_PROPERTY = "args";
  public static final String UI_LISTS_FACETS_PARAMETERS_ARGS_NAME_PROPERTY = "name";
  public static final String UI_LISTS_FACETS_PARAMETERS_ARGS_START_PROPERTY = "start";
  public static final String UI_LISTS_FACETS_PARAMETERS_ARGS_END_PROPERTY = "end";
  public static final String UI_LISTS_FACETS_PARAMETERS_ARGS_GAP_PROPERTY = "gap";
  public static final String UI_LISTS_FACETS_PARAMETERS_ARGS_LIMIT_PROPERTY = "limit";
  public static final String UI_LISTS_FACETS_PARAMETERS_ARGS_SORT_PROPERTY = "sort";
  public static final String UI_LISTS_FACETS_PARAMETERS_ARGS_VALUES_PROPERTY = "values";
  public static final String UI_LISTS_FACETS_PARAMETERS_ARGS_MINCOUNT_PROPERTY = "minCount";

  public static final String UI_LISTS_ENABLE_CUSTOMIZATION = "customization.enabled";

  public static final String UI_LISTS_SEARCH_ENABLED_PROPERTY = "search.enabled";
  public static final String UI_LISTS_SEARCH_PREFILTERS_VISIBLE_PROPERTY = "search.prefilters.visible";
  public static final String UI_LISTS_SEARCH_ADVANCED_ENABLED_PROPERTY = "search.advanced.enabled";
  public static final String UI_LISTS_SEARCH_SELECTEDINFO_ALWAYSVISIBLE_PROPERTY = "search.selectedInfo.alwaysVisible";
  public static final String UI_LISTS_SEARCH_SELECTEDINFO_ICON_PROPERTY = "search.selectedInfo.icon";
  public static final String UI_LISTS_SEARCH_SELECTEDINFO_LABEL_SELECTED_I18N_SINGLE_PROPERTY = "search.selectedInfo.label.selected.i18n.single";
  public static final String UI_LISTS_SEARCH_SELECTEDINFO_LABEL_SELECTED_I18N_MULTIPLE_PROPERTY = "search.selectedInfo.label.selected.i18n.multiple";
  public static final String UI_LISTS_SEARCH_SELECTEDINFO_LABEL_DEFAULT_I18N_PROPERTY = "search.selectedInfo.label.default.i18n";

  public static final String UI_LISTS_COLUMNS_PROPERTY = "columns[]";
  public static final String UI_LISTS_COLUMNS_FIELD_PROPERTY = "field";
  public static final String UI_LISTS_COLUMNS_HEADER_PROPERTY = "header";
  public static final String UI_LISTS_COLUMNS_NOWRAP_PROPERTY = "nowrap";
  public static final String UI_LISTS_COLUMNS_ALIGNRIGHT_PROPERTY = "alignRight";
  public static final String UI_LISTS_COLUMNS_WIDTH_PROPERTY = "width";
  public static final String UI_LISTS_COLUMNS_WIDTHUNIT_PROPERTY = "widthUnit";
  public static final String UI_LISTS_COLUMNS_SORTABLE_PROPERTY = "sortable";
  public static final String UI_LISTS_COLUMNS_SORTBY_PROPERTY = "sortBy";
  public static final String UI_LISTS_COLUMNS_RENDERINGHINT_PROPERTY = "renderingHint";
  public static final String UI_LISTS_COLUMNS_DEFAULTSORTLIST_COLUMNNAME = "defaultSortList.columnName";
  public static final String UI_LISTS_COLUMNS_DEFAULTSORTLIST_ASCENDING = "defaultSortList.ascending";

  public static final String UI_ICONS_CLASS = "ui.icons.class";
  public static final String UI_SERVICE_DROPFOLDER_URL = "ui.service.dropfolder.url";
  public static final String UI_SERVICE_CAS_URL = "ui.service.cas.url";
  public static final String UI_SERVICE_MARKETPLACE_URL = "ui.service.marketplace.url";
  public static final String UI_SERVICE_MONITORING_URL = "ui.service.monitoring.url";
  public static final String UI_SERVICE_REPORTING_URL = "ui.service.reporting.url";
  public static final String UI_SERVICE_REPORTING_ACTIVE = "ui.service.reporting.active";
  public static final String UI_SERVICE_CAS_ACTIVE = "ui.service.cas.active";
  public static final String UI_SERVICE_MULTI_METHOD_AUTHENTICATION_ACTIVE = "ui.service.multi.method.authentication.active";
  public static final String UI_SERVICE_MULTI_METHOD_AUTHENTICATION_LIST = "ui.service.multi.method.authentication.item[]";
  public static final String UI_SERVICE_DROPFOLDER_ACTIVE = "ui.service.dropfolder.active";

  public static final String UI_SERVICE_MONITORING_DEFAULT_URL = "https://www.roda-enterprise.com";
  public static final String UI_SERVICE_CAS_DEFAULT_URL = "https://www.roda-enterprise.com";
  public static final String UI_SERVICE_MARKETPLACE_DEFAULT_URL = "https://marketplace.roda-community.org";
  /*
   * RODA objects
   */

  public static final String RODA_OBJECT_AIP = "aip";
  public static final String RODA_OBJECT_REPRESENTATION = "representation";
  public static final String RODA_OBJECT_FILE = "file";
  public static final String RODA_OBJECT_DIP = "dip";
  public static final String RODA_OBJECT_DIPFILE = "dip_file";
  public static final String RODA_OBJECT_PRESERVATION_AGENT = "preservation_agent";
  public static final String RODA_OBJECT_PRESERVATION_EVENT = "preservation_event";
  public static final String RODA_OBJECT_JOB = "job";
  public static final String RODA_OBJECT_REPORT = "report";
  public static final String RODA_OBJECT_LOG = "log";
  public static final String RODA_OBJECT_NOTIFICATION = "notification";
  public static final String RODA_OBJECT_RISK = "risk";
  public static final String RODA_OBJECT_INCIDENCE = "incidence";
  public static final String RODA_OBJECT_TRANSFERRED_RESOURCE = "transferred_resource";
  public static final String RODA_OBJECT_USER = "user";
  public static final String RODA_OBJECT_GROUP = "group";
  public static final String RODA_OBJECT_DESCRIPTIVE_METADATA = "descriptive_metadata";
  public static final String RODA_OBJECT_PRESERVATION_METADATA = "preservation_metadata";
  public static final String RODA_OBJECT_OTHER_METADATA = "other_metadata";
  public static final String RODA_OBJECT_MEMBER = "member";
  public static final String RODA_OBJECT_REPRESENTATION_INFORMATION = "representation_information";
  public static final String RODA_OBJECT_DISPOSAL_AIP_METADATA = "disposal_aip_metadata";
  public static final String RODA_OBJECT_DISPOSAL_SCHEDULE_AIP_METADATA = "disposal_schedule_aip_metadata";
  public static final String RODA_OBJECT_DISPOSAL_HOLD_AIP_METADATA = "disposal_schedule_hold_aip_metadata";
  public static final String RODA_OBJECT_DISPOSAL_HOLDS_AIP_METADATA = "disposal_schedule_holds_aip_metadata";
  public static final String RODA_OBJECT_DISPOSAL_CONFIRMATION_AIP_METADATA = "disposal_schedule_confirmation_aip_metadata";
  public static final String RODA_OBJECT_DISPOSAL_DESTRUCTION_AIP_METADATA = "disposal_schedule_destruction_aip_metadata";
  public static final String RODA_OBJECT_DISPOSAL_TRANSITIVE_HOLD_AIP_METADATA = "disposal_transitive_hold_aip_metadata";
  public static final String RODA_OBJECT_DISPOSAL_TRANSITIVE_SCHEDULE_AIP_METADATA = "disposal_transitive_schedule_aip_metadata";
  public static final String RODA_OBJECT_DISPOSAL_RULES = "disposal_rules";
  public static final String RODA_OBJECT_DISPOSAL_RULE = "disposal_rule";
  public static final String RODA_OBJECT_DISPOSAL_SCHEDULE = "disposal_schedule";
  public static final String RODA_OBJECT_DISPOSAL_SCHEDULES = "disposal_schedules";
  public static final String RODA_OBJECT_DISPOSAL_HOLD = "disposal_hold";
  public static final String RODA_OBJECT_DISPOSAL_HOLDS = "disposal_holds";
  public static final String RODA_OBJECT_DISPOSAL_CONFIRMATION_METADATA = "disposal_confirmation_metadata";
  public static final String RODA_OBJECT_DISPOSAL_CONFIRMATIONS_METADATA = "disposal_confirmations_metadata";
  public static final String RODA_OBJECT_DISPOSAL_HOLD_ASSOCIATION = "disposal_hold_association";
  public static final String RODA_OBJECT_SHALLOW_FILE = "shallow_file";
  public static final String RODA_OBJECT_SHALLOWS_FILE = "shallow_files";
  public static final String RODA_OBJECT_DISTRIBUTED_INSTANCES = "distributed_instances";
  public static final String RODA_OBJECT_DISTRIBUTED_INSTANCE = "distributed_instance";
  public static final String RODA_OBJECT_LOCAL_INSTANCE = "local_instance";
  public static final String RODA_OBJECT_ACCESS_KEY = "access_key";
  public static final String RODA_OBJECT_ACCESS_KEYS = "access_keys";
  public static final String RODA_OBJECT_ACCESS_TOKEN = "access_token";

  public static final String RODA_OBJECT_MEMBERS = "members";
  public static final String RODA_OBJECT_USERS = "users";
  public static final String RODA_OBJECT_GROUPS = "groups";
  public static final String RODA_OBJECT_RISKS = "risks";
  public static final String RODA_OBJECT_INCIDENCES = "incidences";
  public static final String RODA_OBJECT_NOTIFICATIONS = "notifications";
  public static final String RODA_OBJECT_LOGS = "logs";
  public static final String RODA_OBJECT_JOBS = "jobs";
  public static final String RODA_OBJECT_REPORTS = "reports";
  public static final String RODA_OBJECT_DESCRIPTIVE_METADATA_LIST = "descriptive_metadata_list";
  public static final String RODA_OBJECT_OTHER_METADATA_LIST = "other_metadata_list";
  public static final String RODA_OBJECT_PRESERVATION_METADATA_LIST = "preservation_metadata_list";
  public static final String RODA_OBJECT_AIPS = "aips";
  public static final String RODA_OBJECT_REPRESENTATIONS = "representations";
  public static final String RODA_OBJECT_FILES = "files";
  public static final String RODA_OBJECT_DIPS = "dips";
  public static final String RODA_OBJECT_DIPFILES = "dip_files";
  public static final String RODA_OBJECT_TRANSFERRED_RESOURCES = "transferredResources";
  public static final String RODA_OBJECT_REPRESENTATION_INFORMATION_LIST = "representation_information_list";

  public static final String RODA_OBJECT_PERMISSION = "permission";
  public static final String RODA_OBJECT_INDEX_RESULT = "index_result";
  public static final String RODA_OBJECT_FACET_FIELD_VALUE = "facet_field_value";
  public static final String RODA_OBJECT_FACET_VALUE = "facet_value";
  public static final String RODA_OBJECT_OTHERS = "results";
  public static final String RODA_OBJECT_OTHER = "result";

  /*
   * Installation (and most probably classpath as well) related variables
   */
  public static final String CORE_DESCRIPTION_LEVELS_FILE = "roda-description-levels-hierarchy.properties";
  public static final String CORE_CONFIG_FOLDER = "config";
  public static final String CORE_EXAMPLE_CONFIG_FOLDER = "example-config";
  public static final String CORE_DEFAULT_FOLDER = "default";
  public static final String CORE_I18N_FOLDER = "i18n";
  public static final String CORE_DATA_FOLDER = "data";
  public static final String CORE_STORAGE_FOLDER = "storage";
  public static final String CORE_STAGING_STORAGE_FOLDER = "staging-storage";
  public static final String CORE_STAGING_TRANSACTIONS_LOG_BACKUP_FOLDER = "transactions-log-backup";
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
  public static final String CORE_PLUGINS_DEPENDENCIES_FOLDER = "dependencies";
  public static final String CORE_PLUGINS_SHARED_FOLDER = "shared";
  public static final String CORE_PLUGINS_DISABLED_FOLDER = "disabled";
  public static final String CORE_PROTOCOLS_FOLDER = "protocols";
  public static final String CORE_PROTOCOLS_DEPENDENCIES_FOLDER = "dependencies";
  public static final String CORE_PROTOCOLS_SHARED_FOLDER = "shared";
  public static final String CORE_PROTOCOLS_DISABLED_FOLDER = "disabled";
  public static final String CORE_DISSEMINATION_FOLDER = "dissemination";
  public static final String CORE_HTML_FOLDER = "html";
  public static final String CORE_INGEST_FOLDER = "ingest";
  public static final String CORE_LOG_FOLDER = "log";
  public static final String CORE_SCHEMAS_FOLDER = "schemas";
  public static final String CORE_LDAP_FOLDER = "ldap";
  public static final String CORE_THEME_FOLDER = "theme";
  public static final String CORE_MARKDOWN_FOLDER = "documentation";
  public static final String CORE_LICENSE_MARKDOWN_FILE = "LICENSE.md";
  public static final String CORE_RISK_FOLDER = "risk";
  public static final String CORE_AGENT_FOLDER = "agent";
  public static final String CORE_NOTIFICATION_FOLDER = "notification";
  public static final String CORE_RISKINCIDENCE_FOLDER = "riskincidence";
  public static final String CORE_MAIL_TEMPLATE_FOLDER = "mail/templates";
  public static final String CORE_CERTIFICATES_FOLDER = "certificates";
  public static final String CORE_REPORT_FOLDER = "reports";
  public static final String CORE_DIP_FOLDER = "dip";
  public static final String CORE_DIP_FILE_FOLDER = "dipfile";
  public static final String CORE_REPRESENTATION_INFORMATION_FOLDER = "representation-information";
  public static final String CORE_DISPOSAL_BIN_FOLDER = "disposal-bin";
  public static final String CORE_FILE_SHALLOW_TMP_FOLDER = "file-shallow";
  public static final String CORE_SYNCHRONIZATION_FOLDER = "synchronization";
  public static final String CORE_SYNCHRONIZATION_OUTCOME_FOLDER = "outcome";
  public static final String CORE_SYNCHRONIZATION_INCOMING_FOLDER = "incoming";
  public static final String CORE_LOCAL_INSTANCE_FOLDER = "local-instance";
  public static final String CORE_JOB_ATTACHMENTS_FOLDER = "job-attachments";
  public static final String CORE_MARKET_FOLDER = "market";
  public static final String CORE_MARKET_FILE = "marketInfo.jsonl";
  public static final String CORE_PLUGINS_DOCUMENTATION_FILE = "README.md";

  public static final String CORE_I18N_CLIENT_FOLDER = "client";
  public static final String CORE_I18_GWT_XML_FILE = "I18N.gwt.xml";

  public static final String CORE_CROSSWALKS_INGEST = "crosswalks/ingest/";
  public static final String CORE_CROSSWALKS_INGEST_OTHER = "crosswalks/ingest/other/";
  public static final String CORE_CROSSWALKS_TECHNICAL = "crosswalks/technical/";
  public static final String CORE_DISPOSAL_METADATA_TRANSFORMERS = "disposal/destruction/";

  public static final String PREMIS_METADATA_TYPE = "premis";
  public static final String PREMIS_METADATA_VERSION = "3";

  public static final String DEFAULT_NODE_HOSTNAME = "localhost";
  public static final String DEFAULT_NODE_PORT = "2551";

  /*
   * API related
   */
  public static final String API_SEP = "/";
  public static final String API_REST_V1_AIPS = "api/v1/aips/";
  public static final String API_REST_V1_REPRESENTATIONS = "api/v1/representations/";
  public static final String API_REST_V1_FILES = "api/v1/files/";
  public static final String API_REST_V1_INDEX = "api/v1/index/";
  public static final String API_REST_V1_DIPS = "api/v1/dips/";
  public static final String API_REST_V1_DIPFILES = "api/v1/dipfiles/";
  public static final String API_REST_V1_DISTRIBUTED_INSTANCE = "api/v1/distributed_instances/";

  public static final String API_REST_V1_REPRESENTATION_OTHER_METADATA = "otherMetadata";

  /**
   * API related (Version 2)
   */

  // common
  public static final String API_REST_V2_DOWNLOAD_HANDLER = "/download";
  public static final String API_REST_V2_PREVIEW_HANDLER = "/preview";
  public static final String API_REST_V2_REPRESENTATION_OTHER_METADATA = "/other-metadata";
  public static final String API_REST_V2_REPRESENTATION_BINARY = "/binary";

  // aips
  public static final String API_REST_V2_AIPS = "api/v2/aips/";

  // representations
  public static final String API_REST_V2_REPRESENTATIONS_ENDPOINT = "api/v2/representations";
  public static final String API_REST_V2_SUB_RESOURCE_REPRESENTATIONS = "representations";
  public static final String API_REST_V2_SUB_RESOURCE_METADATA = "metadata";
  public static final String API_REST_V2_SUB_RESOURCE_DESCRIPTIVE = "descriptive";
  public static final String API_REST_V2_SUB_RESOURCE_DOWNLOAD = "download";

  // files
  public static final String API_REST_V2_FILES = "api/v2/files/";
  public static final String API_REST_V2_FILES_TECHNICAL_METADATA_TYPE_HTML = "metadata/technical";
  public static final String API_REST_V2_FILES_TECHNICAL_METADATA_TYPE_HTML_SUFFIX = "/html";
  public static final String API_REST_V2_FILES_TECHNICAL_METADATA_HTML = "metadata/preservation/html";
  public static final String API_REST_V2_FILES_TECHNICAL_METADATA_DOWNLOAD = "metadata/preservation/download";

  // transferred resource
  public static final String API_REST_V2_TRANSFERRED_RESOURCES = "api/v2/transfers/";
  public static final String API_REST_V2_TRANSFERRED_RESOURCE_CREATE_RESOURCE = "create/resource";

  // Report
  public static final String API_REST_V2_JOB_REPORT = "api/v2/job-report/";

  // jobs
  public static final String API_REST_V2_JOBS = "api/v2/jobs/";

  // audit logs
  public static final String API_REST_V2_AUDIT_LOGS = "api/v2/audit-logs/";

  // notifications
  public static final String API_REST_V2_NOTIFICATIONS = "api/v2/notifications/";

  // dips
  public static final String API_REST_V2_DIPS = "api/v2/dips/";

  // representation-information
  public static final String API_REST_V2_REPRESENTATION_INFORMATION = "api/v2/representation-information/";

  // risks
  public static final String API_REST_V2_RISKS = "api/v2/risks/";

  // preservation events
  public static final String API_REST_V2_PRESERVATION_EVENTS = "api/v2/preservation/events/";
  public static final String API_REST_V2_PRESERVATION_EVENTS_DETAILS_HTML = "/details/html";

  // preservation agents
  public static final String API_REST_V2_PRESERVATION_AGENTS = "api/v2/preservation/agents/";

  // members
  public static final String API_REST_V2_MEMBERS = "api/v2/members/";

  // disposal confirmation
  public static final String API_REST_V2_DISPOSAL_CONFIRMATION = "api/v2/disposal/confirmations/";
  public static final String API_REST_V2_DISPOSAL_CONFIRMATION_REPORT = "report";
  public static final String API_REST_V2_DISPOSAL_CONFIRMATION_REPORT_HTML = "html";
  public static final String API_REST_V2_DISPOSAL_CONFIRMATION_QUERY_PARAM_TO_PRINT = "to-print";

  // themes
  public static final String API_REST_V2_THEME = "api/v2/themes";
  public static final String API_V2_QUERY_PARAM_RESOURCE_ID = "resource-id";
  public static final String API_V2_QUERY_PARAM_DEFAULT_RESOURCE_ID = "default-resource-id";
  public static final String API_V2_QUERY_PARAM_RESOURCE_TYPE = "resource-type";

  // distributed instances
  public static final String API_REST_V2_DISTRIBUTED_INSTANCE = "api/v2/distributed-instances/";

  /**
   * END: API related (Version 2)
   */

  // sub-resources strings
  public static final String API_DATA = "data";
  public static final Object API_FILE = "file";
  public static final String API_DESCRIPTIVE_METADATA = RODA_OBJECT_DESCRIPTIVE_METADATA;
  public static final String API_PRESERVATION_METADATA = RODA_OBJECT_PRESERVATION_METADATA;
  public static final String API_OTHER_METADATA = RODA_OBJECT_OTHER_METADATA;
  public static final String API_FIND = "find";
  public static final String API_ACKNOWLEDGE = "acknowledge";
  public static final String API_STOP = "stop";
  public static final String API_REPORTS = "reports";

  // "http query string" related strings
  public static final String API_QUERY_START = "?";
  public static final String API_QUERY_ASSIGN_SYMBOL = "=";
  public static final String API_QUERY_SEP = "&";
  public static final String API_QUERY_KEY_ACCEPT_FORMAT = "acceptFormat";
  public static final String API_QUERY_KEY_INLINE = "inline";
  public static final String API_QUERY_KEY_JSONP_CALLBACK = "callback";
  public static final String API_QUERY_DEFAULT_JSONP_CALLBACK = "";
  public static final String API_QUERY_VALUE_ACCEPT_FORMAT_BIN = "bin";
  public static final String API_QUERY_VALUE_ACCEPT_FORMAT_XML = "xml";
  public static final String API_QUERY_VALUE_ACCEPT_FORMAT_HTML = "html";
  public static final String API_QUERY_VALUE_ACCEPT_FORMAT_JSON = "json";
  public static final String API_QUERY_VALUE_ACCEPT_FORMAT_ZIP = "zip";
  public static final String API_QUERY_VALUE_ACCEPT_FORMAT_CSV = "csv";
  public static final String API_QUERY_VALUE_ACCEPT_FORMAT_JSONP = "jsonp";
  public static final String API_QUERY_KEY_LANG = "lang";
  public static final String API_QUERY_KEY_VERSION_ID = "versionId";
  public static final String API_QUERY_VALUE_LANG_DEFAULT = RodaConstants.API_QUERY_VALUE_LANG_PT_PT;
  public static final String API_QUERY_VALUE_LANG_PT_PT = "pt_PT";
  public static final String API_QUERY_VALUE_LANG_EN_US = "en_US";
  public static final String API_QUERY_KEY_START = "start";
  public static final String API_QUERY_KEY_LIMIT = "limit";
  public static final String API_QUERY_IS_ORIGINAL = "isOriginal";
  public static final String API_QUERY_KEY_RETURN_CLASS = "returnClass";
  public static final String API_QUERY_KEY_ONLY_ACTIVE = "onlyActive";
  public static final String API_QUERY_KEY_FACET = "facet";
  public static final String API_QUERY_KEY_FILTER = "filter";
  public static final String API_QUERY_KEY_SORT = "sort";
  public static final String API_QUERY_KEY_FACET_LIMIT = "facetLimit";
  public static final String API_QUERY_KEY_EXPORT_FACETS = "exportFacets";
  public static final String API_QUERY_KEY_FILENAME = "filename";
  public static final String API_QUERY_KEY_FILE = "file";

  public static final String API_QUERY_KEY_CLASS = "class";
  public static final String API_QUERY_KEY_TYPE = "type";
  public static final String API_QUERY_JOB_DETAILS = "details";
  // "http path param" related strings
  public static final String API_PATH_PARAM_AIP_ID = "aip_id";
  public static final String API_PATH_PARAM_REPRESENTATION_ID = "representation_id";
  public static final String API_PATH_PARAM_REPRESENTATION_UUID = "representation_uuid";
  public static final String API_PATH_PARAM_FILE_ID = "file_id";
  public static final String API_PATH_PARAM_FILE_UUID = "file_uuid";
  public static final String API_PATH_PARAM_FOLDER = "folder";
  public static final String API_PATH_PARAM_METADATA_ID = "metadata_id";
  public static final String API_PATH_PARAM_TRANSFERRED_RESOURCE_ID = "transferred_resource_id";
  public static final String API_PATH_PARAM_TRANSFERRED_RESOURCE_UUID = "transferred_resource_uuid";
  public static final String API_PATH_PARAM_SIP_ID = "sip_id";
  public static final String API_PATH_PARAM_NOTIFICATION_ID = "notification_id";
  public static final String API_PATH_PARAM_AGENT_ID = "agent_id";
  public static final String API_PATH_PARAM_RISK_ID = "risk_id";
  public static final String API_PATH_PARAM_RISK_INCIDENCE_ID = "risk_incidence_id";
  public static final String API_PATH_PARAM_DIP_ID = "dip_id";
  public static final String API_PATH_PARAM_DIP_FILE_ID = "dip_file_id";
  public static final String API_PATH_PARAM_DIP_FILE_UUID = "dip_file_uuid";
  public static final String API_PATH_PARAM_REPRESENTATION_INFORMATION_ID = "representation_information_id";
  public static final String API_PATH_PARAM_DISPOSAL_RULE_ID = "disposal_rule_id";
  public static final String API_PATH_PARAM_DISPOSAL_SCHEDULE_ID = "disposal_schedule_id";
  public static final String API_PATH_PARAM_DISPOSAL_HOLD_ID = "disposal_hold_id";
  public static final String API_PATH_PARAM_DISPOSAL_CONFIRMATION_ID = "disposal_confirmation_id";
  public static final String API_PATH_PARAM_DISPOSAL_CONFIRMATION_REPORT = "report";
  public static final String API_PATH_PARAM_DISPOSAL_CONFIRMATION_REPORT_PRINT = "print";
  public static final String API_PATH_PARAM_AUTH_TOKEN = "token";
  public static final String API_PATH_PARAM_DISTRIBUTED_INSTANCE_REGISTER = "register";
  public static final String API_PATH_PARAM_DISTRIBUTED_INSTANCE_SYNC = "sync";
  public static final String API_PATH_PARAM_INSTANCE_IDENTIFIER = "instance_identifier";
  public static final String API_PATH_LAST_SYNC_STATUS = "sync/status";
  public static final String API_PATH_PARAM_DISTRIBUTED_INSTANCE_GET_UPDATES = "updates";

  public static final String API_PATH_PARAM_PART = "part";
  public static final String API_PATH_PARAM_NAME = "name";
  public static final String API_PATH_PARAM_USERNAME = "username";
  public static final String API_PATH_PARAM_PASSWORD = "password";
  public static final String API_PATH_PARAM_PERMISSION_TYPE = "permission_type";
  public static final String API_PATH_PARAM_OTHER_METADATA_TYPE = "type";
  public static final String API_PATH_PARAM_OTHER_METADATA_FILE_SUFFIX = "file_suffix";

  public static final String API_QUERY_PARAM_ID = "id";
  public static final String API_QUERY_PARAM_VERSION_ID = "version_id";
  public static final String API_QUERY_PARAM_NOTIFICATION_TOKEN = "token";
  public static final String API_QUERY_PARAM_RESOURCE_ID = "resource_id";
  public static final String API_QUERY_PARAM_DEFAULT_RESOURCE_ID = "default_resource_id";

  public static final String API_QUERY_PARAM_RESOURCE_TYPE = "resource_type";

  public static final String API_QUERY_PARAM_DEFAULT_RESOURCE_TYPE = "internal";
  public static final String API_QUERY_PARAM_INLINE = "inline";
  public static final String API_QUERY_PARAM_FILEPATH = "filepath";
  public static final String API_QUERY_PARAM_PARENT_ID = "parent_id";
  public static final String API_QUERY_PARAM_TYPE = "type";
  public static final String API_QUERY_PARAM_METADATA_TYPE = "metadataType";
  public static final String API_QUERY_PARAM_METADATA_VERSION = "metadataVersion";
  public static final String API_QUERY_PARAM_RISK_MESSAGE = "message";
  public static final String API_QUERY_PARAM_TEMPLATE = "template";
  public static final String API_QUERY_PARAM_PASSWORD = "password";
  public static final String API_QUERY_PARAM_SIZE_LIMIT = "size";
  public static final String API_QUERY_PARAM_DETAILS = "details";
  public static final String API_QUERY_PARAM_ONLY_DETAILS = "onlyDetails";
  public static final String API_QUERY_PARAM_COMMIT = "commit";
  public static final String API_PARAM_UPLOAD = "upl";
  public static final String API_PARAM_FILE = "file";
  public static final String API_FORM_PARAM_FIND_REQUEST = "findRequest";
  public static final String API_FORM_PARAM_EXPORT_FACETS = "exportFacets";
  public static final String API_FORM_PARAM_TYPE = "type";
  public static final String API_FORM_PARAM_FILENAME = "filename";
  // http headers used
  public static final String API_HTTP_HEADER_ACCEPT = "Accept";
  // job related params
  public static final String API_PATH_PARAM_JOB_ID = "jobId";
  public static final String API_PATH_PARAM_JOB_REPORT_ID = "report-id";
  public static final String API_PATH_PARAM_JOB_JUST_FAILED = "job-just-failed";
  public static final String API_PATH_PARAM_JOB_ATTACHMENT_ID = "attachmentId";

  public static final String API_DEFAULT_CSV_FILENAME = "export.csv";
  public static final String API_DEFAULT_JSON_FILENAME = "export.json";
  public static final String API_NOTIFICATION_DEFAULT_TEMPLATE = "test-email-template";

  // api method allowable values
  public enum ListMediaTypes {
    JSON("json"), XML("xml"), JSONP("jsonp");

    private String value;

    ListMediaTypes(String value) {
      this.value = value;
    }

    public void setValue(final String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  public enum APIMediaTypes {
    JSON("json"), XML("xml"), ZIP("zip"), JSONP("jsonp");

    private String value;

    APIMediaTypes(String value) {
      this.value = value;
    }

    public void setValue(final String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  public enum GetFileMediaTypes {
    JSON("json"), XML("xml"), BIN("bin"), JSONP("jsonp");

    private String value;

    GetFileMediaTypes(String value) {
      this.value = value;
    }

    public void setValue(final String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  public enum DescriptibeMetadataLanguages {
    PT_PT("pt_PT"), EN_US("en_US");

    private String value;

    DescriptibeMetadataLanguages(String value) {
      this.value = value;
    }

    public void setValue(final String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  public enum MetadataMediaTypes {
    JSON("json"), XML("xml"), HTML("html"), BIN("bin"), JSONP("jsonp");

    private String value;

    MetadataMediaTypes(String value) {
      this.value = value;
    }

    public void setValue(final String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  public enum ResourcesTypes {
    INTERNAL("internal"), PLUGINS("plugins");

    private String value;

    ResourcesTypes(String value) {
      this.value = value;
    }

    public void setValue(final String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  public static final String API_METRICS_TO_OBTAIN = "metricsToObtain";

  /*
   * Controllers related
   */
  public static final String CONTROLLER_ID_PARAM = "id";
  public static final String CONTROLLER_AIP_PARAM = RODA_OBJECT_AIP;
  public static final String CONTROLLER_AIPS_PARAM = RODA_OBJECT_AIPS;
  public static final String CONTROLLER_AIP_ID_PARAM = "aipId";
  public static final String CONTROLLER_REPRESENTATION_PARAM = RODA_OBJECT_REPRESENTATION;
  public static final String CONTROLLER_REPRESENTATION_ID_PARAM = "representationId";
  public static final String CONTROLLER_REPRESENTATION_UUID_PARAM = "representationUUID";
  public static final String CONTROLLER_TRANSFERRED_RESOURCE_PARAM = RODA_OBJECT_TRANSFERRED_RESOURCE;
  public static final String CONTROLLER_TRANSFERRED_RESOURCE_ID_PARAM = "transferredResourceId";
  public static final String CONTROLLER_TRANSFERRED_RESOURCE_NAME_PARAM = "transferredResourceName";
  public static final String CONTROLLER_FILE_PARAM = RODA_OBJECT_FILE;
  public static final String CONTROLLER_FILE_ID_PARAM = "fileId";
  public static final String CONTROLLER_FILE_UUID_PARAM = "fileUUID";
  public static final String CONTROLLER_METADATA_ID_PARAM = "metadataId";
  public static final String CONTROLLER_INDEX_PRESERVATION_EVENT_ID_PARAM = "indexedPreservationEventId";
  public static final String CONTROLLER_VERSION_ID_PARAM = "versionId";
  public static final String CONTROLLER_RESOURCE_ID_PARAM = "resourceId";
  public static final String CONTROLLER_DIP_PARAM = RODA_OBJECT_DIP;
  public static final String CONTROLLER_DIPS_PARAM = RODA_OBJECT_DIPS;
  public static final String CONTROLLER_DIP_ID_PARAM = "dipId";
  public static final String CONTROLLER_DIP_UUID_PARAM = "dipUUID";
  public static final String CONTROLLER_DIP_FILE_ID_PARAM = "dipFileId";
  public static final String CONTROLLER_DIP_FILE_UUID_PARAM = "dipFileUUID";
  public static final String CONTROLLER_AGENT_ID_PARAM = "agentId";

  public static final String CONTROLLER_DISPOSAL_RULE_PARAM = RODA_OBJECT_DISPOSAL_RULE;
  public static final String CONTROLLER_DISPOSAL_RULE_ID_PARAM = "disposalRuleId";
  public static final String CONTROLLER_DISPOSAL_RULE_OVERRIDE_MANUAL_PARAM = "overrideManual";

  public static final String CONTROLLER_DISPOSAL_SCHEDULE_PARAM = RODA_OBJECT_DISPOSAL_SCHEDULE;
  public static final String CONTROLLER_DISPOSAL_SCHEDULE_ID_PARAM = "disposalScheduleId";

  public static final String CONTROLLER_DISPOSAL_HOLD_PARAM = RODA_OBJECT_DISPOSAL_HOLD;
  public static final String CONTROLLER_DISPOSAL_HOLD_ID_PARAM = "disposalHoldId";
  public static final String CONTROLLER_DISPOSAL_HOLD_OVERRIDE_PARAM = "overrideDisposalHolds";
  public static final String CONTROLLER_DISPOSAL_HOLD_LIFT_ALL = "liftDisposalHold";
  public static final String CONTROLLER_DISPOSAL_HOLD_DISASSOCIATE_ALL = "disassociateAllDisposalHold";

  public static final String CONTROLLER_DISPOSAL_CONFIRMATION_METADATA_PARAM = RODA_OBJECT_DISPOSAL_CONFIRMATION_METADATA;
  public static final String CONTROLLER_DISPOSAL_CONFIRMATION_ID_PARAM = "disposalConfirmationId";

  public static final String CONTROLLER_DISTRIBUTED_INSTANCE_PARAM = RODA_OBJECT_DISTRIBUTED_INSTANCE;
  public static final String CONTROLLER_DISTRIBUTED_INSTANCE_ID_PARAM = "distributedInstanceId";
  public static final String CONTROLLER_DISTRIBUTED_INSTANCE_STATUS_PARAM = "activate";
  public static final String CONTROLLER_LOCAL_INSTANCE_PARAM = RODA_OBJECT_LOCAL_INSTANCE;
  public static final String CONTROLLER_LOCAL_INSTANCE_ID_PARAM = "localInstanceId";
  public static final String CONTROLLER_ACCESS_KEY_PARAM = RODA_OBJECT_ACCESS_KEY;

  public static final String CONTROLLER_ID_OBJECT_PARAM = "transferred_resource_uuid, transferred_resource_path, sip, transferred_resource_original_name";
  public static final String CONTROLLER_SIP_PARAM = "sip";
  public static final String CONTROLLER_ID_OBJECT_RESOURCE_PATH = "transferred_resource_path";
  public static final String CONTROLLER_ID_OBJECT_SOURCE_NAME = "transferred_resource_original_name";

  public static final String CONTROLLER_PERMISSIONS_PARAM = "permissions";
  public static final String CONTROLLER_RISK_PARAM = RODA_OBJECT_RISK;
  public static final String CONTROLLER_RISK_ID_PARAM = "riskId";
  public static final String CONTROLLER_MESSAGE_PARAM = "message";
  public static final String CONTROLLER_INCIDENCE_PARAM = RODA_OBJECT_INCIDENCE;
  public static final String CONTROLLER_TEMPLATE_PARAM = "template";
  public static final String CONTROLLER_NOTIFICATION_ID_PARAM = "notificationId";
  public static final String CONTROLLER_NOTIFICATION_TOKEN_PARAM = "token";
  public static final String CONTROLLER_JOB_PARAM = "job";
  public static final String CONTROLLER_JOB_CREATE_REQUEST = "createJobRequest";
  public static final String CONTROLLER_JOB_ID_PARAM = "jobId";
  public static final String CONTROLLER_JOB_JUST_FAILED_PARAM = "justFailed";
  public static final String CONTROLLER_JOB_REPORT_ID_PARAM = "jobReportId";
  public static final String CONTROLLER_JOB_ATTACHMENT_ID_PARAM = "attachmentId";

  public static final String CONTROLLER_REPRESENTATION_INFORMATION_PARAM = RODA_OBJECT_REPRESENTATION_INFORMATION;
  public static final String CONTROLLER_REPRESENTATION_INFORMATION_ID_PARAM = "representationInformationId";

  public static final String CONTROLLER_PRE_MITIGATION_PROBABILITY_PARAM = "preMitigationProbability";
  public static final String CONTROLLER_PRE_MITIGATION_IMPACT_PARAM = "preMitigationImpact";
  public static final String CONTROLLER_POS_MITIGATION_PROBABILITY_PARAM = "posMitigationProbability";
  public static final String CONTROLLER_POS_MITIGATION_IMPACT_PARAM = "posMitigationImpact";

  public static final String CONTROLLER_FILTER_PARAM = "filter";
  public static final String CONTROLLER_SORTER_PARAM = "sorter";
  public static final String CONTROLLER_SUBLIST_PARAM = "sublist";
  public static final String CONTROLLER_CLASS_PARAM = "class";
  public static final String CONTROLLER_JUST_ACTIVE_PARAM = "justActive";
  public static final String CONTROLLER_SELECTED_ITEMS_PARAM = "selectedItems";
  public static final String CONTROLLER_START_PARAM = "start";
  public static final String CONTROLLER_LIMIT_PARAM = "limit";
  public static final String CONTROLLER_PARENT_PARAM = "parent";
  public static final String CONTROLLER_PARENT_ID_PARAM = "parentId";
  public static final String CONTROLLER_FOLDERNAME_PARAM = "folderName";
  public static final String CONTROLLER_FORCE_COMMIT_PARAM = "forceCommit";
  public static final String CONTROLLER_FOLDER_RELATIVEPATH_PARAM = "folderRelativePath";
  public static final String CONTROLLER_FILENAME_PARAM = "filename";
  public static final String CONTROLLER_PATH_PARAM = "path";
  public static final String CONTROLLER_DIRECTORY_PATH_PARAM = "directoryPath";
  public static final String CONTROLLER_SUCCESS_PARAM = "success";
  public static final String CONTROLLER_ERROR_PARAM = "error";
  public static final String CONTROLLER_PART_PARAM = "part";
  public static final String CONTROLLER_START_AGENT_PARAM = "startAgent";
  public static final String CONTROLLER_LIMIT_AGENT_PARAM = "limitAgent";
  public static final String CONTROLLER_START_EVENT_PARAM = "startEvent";
  public static final String CONTROLLER_LIMIT_EVENT_PARAM = "limitEvent";
  public static final String CONTROLLER_START_FILE_PARAM = "startFile";
  public static final String CONTROLLER_LIMIT_FILE_PARAM = "limitFile";
  public static final String CONTROLLER_SELECTED_PARAM = "selected";
  public static final String CONTROLLER_TO_PARENT_PARAM = "toParent";
  public static final String CONTROLLER_FIELD_PARAM = "field";
  public static final String CONTROLLER_QUERY_PARAM = "query";
  public static final String CONTROLLER_QUERY_PARAMS = "queryParams";
  public static final String CONTROLLER_TYPE_PARAM = "type";
  public static final String CONTROLLER_ACCEPT_PARAM = "accept";
  public static final String CONTROLLER_REJECT_REASON_PARAM = "rejectReason";
  public static final String CONTROLLER_SELECTED_VERSION_PARAM = "selectedVersion";
  public static final String CONTROLLER_FILES_PARAM = "files";
  public static final String CONTROLLER_USERNAME_PARAM = "username";
  public static final String CONTROLLER_GROUPNAME_PARAM = "groupname";
  public static final String CONTROLLER_USER_PARAM = "user";
  public static final String CONTROLLER_GROUP_PARAM = "group";
  public static final String CONTROLLER_PERMISSION_TYPE_PARAM = "permissionType";
  public static final String CONTROLLER_NAME_PARAM = "name";
  public static final String CONTROLLER_STATES_PARAM = "states";
  public static final String CONTROLLER_REPRESENTATION_INFORMATION_FILTER_PARAM = "filter";
  public static final String CONTROLLER_ACTIVATE_PARAM = "activate";
  public static final String CONTROLLER_DETAILS_PARAM = "details";
  public static final String CONTROLLER_REQUEST_METRICS_PARAM = "metricsToObtain";
  public static final String CONTROLLER_LOGIN_METHOD_PARAM = "loginMethod";
  public static final String CONTROLLER_TRANSACTION_ID_PARAM = "transactionID";

  /*
   * Core (storage, index, orchestrator, etc.)
   */
  public enum StorageType {
    FILESYSTEM
  }

  public static final StorageType DEFAULT_STORAGE_TYPE = StorageType.FILESYSTEM;

  public enum SolrType {
    HTTP, CLOUD
  }

  public static final SolrType DEFAULT_SOLR_TYPE = SolrType.HTTP;

  public enum NodeType {
    PRIMARY, WORKER, TEST, CONFIGS, REPLICA
  }

  public static final NodeType DEFAULT_NODE_TYPE = NodeType.PRIMARY;

  public enum DistributedModeType {
    CENTRAL, LOCAL, BASE
  }

  public static final DistributedModeType DEFAULT_DISTRIBUTED_MODE_TYPE = DistributedModeType.BASE;
  public static final String DISTRIBUTED_MODE_TYPE_PROPERTY = "roda.distributed.mode.type";
  public static final String DEFAULT_API_SECRET_KEY = "WiJY0uxPwYwMAcjEsDKzQMEpBLiJDmPSTSfxmC06EI0=";
  public static final String API_SECRET_KEY_PROPERTY = "roda.distributed.api.secret";

  public static final String CENTRAL_INSTANCE_NAME_PROPERTY = "roda.distributed.localinstance.name";
  public static final String DEFAULT_CENTRAL_INSTANCE_NAME = "Central";

  public static final long DEFAULT_ACCESS_KEY_VALIDITY = 31536000000L; // 1 year in ms
  public static final String ACCESS_KEY_VALIDITY = "roda.distributed.api.access.key.validity";
  public static final long DEFAULT_ACCESS_TOKEN_VALIDITY = 7200000L; // 2 hours in ms
  public static final String ACCESS_TOKEN_VALIDITY = "roda.distributed.api.access.token.validity";

  // Plugins certificates
  public static final String PLUGINS_CERTIFICATE_OPT_IN_PROPERTY = "core.plugins.external.certificates.opt-in";
  public static final String PLUGINS_CERTIFICATE_DEFAULT_TRUSTSTORE_AUTH_TYPE = "RSA";
  public static final String PLUGINS_CERTIFICATE_DEFAULT_TRUSTSTORE_PATH = "/config/market/truststore/";
  public static final String PLUGINS_CERTIFICATE_RODA_TRUSTSTORE_TYPE = "PKCS12";
  public static final String PLUGINS_CERTIFICATE_RODA_TRUSTSTORE_NAME = "roda-truststore.p12";
  public static final String PLUGINS_CERTIFICATE_RODA_TRUSTSTORE_PASS = "changeit";

  public static final String PLUGINS_CERTIFICATE_CUSTOM_TRUSTSTORE_FOLDER = "market/truststore/";
  public static final String PLUGINS_CERTIFICATE_CUSTOM_TRUSTSTORE_ENABLE_PROPERTY = "core.plugins.external.certificates.custom.truststore.enable";
  public static final String PLUGINS_CERTIFICATE_CUSTOM_TRUSTSTORE_TYPE_PROPERTY = "core.plugins.external.certificates.custom.truststore.type";
  public static final String PLUGINS_CERTIFICATE_CUSTOM_TRUSTSTORE_NAME_PROPERTY = "core.plugins.external.certificates.custom.truststore.name";
  public static final String PLUGINS_CERTIFICATE_CUSTOM_TRUSTSTORE_PASS_PROPERTY = "core.plugins.external.certificates.custom.truststore.pass";

  // MARKET
  public static final String MARKET_INFO_URL_PROPERTY = "core.market.info.url";
  public static final String DEFAULT_MARKET_INFO_URL = "https://market.roda-community.org/api/v2/marketplace/filtered?type=plugin";
  public static final String KEEP_MARKET_PLUGIN_HOMEPAGE_URL_PROPERTY = "core.market.plugin.homepage.url";
  public static final String DEFAULT_KEEP_MARKET_PLUGIN_HOMEPAGE_URL = "http://docs.roda-enterprise.com/plugins/";
  public static final String DEFAULT_MARKET_SUPPORT_URL = "https://www.roda-enterprise.com";
  public static final String ENVIRONMENT_COLLECT_VERSION = "roda.environment.collect.version";
  public static final String DEFAULT_ENVIRONMENT_COLLECT_VERSION = "true";

  // Security Plugins
  public static final String SECURITY_PLUGINS_ENABLE_PROPERTY = "core.plugins.external.security.enable";
  public static final String SECURITY_PLUGINS_CONFIGURATIONS_PROPERTY = "core.plugins.external.security.configurations[]";

  public enum OrchestratorType {
    PEKKO, PEKKO_DISTRIBUTED
  }

  public static final OrchestratorType DEFAULT_ORCHESTRATOR_TYPE = OrchestratorType.PEKKO;
  public static final String ORCHESTRATOR_TYPE_PROPERTY = "core.orchestrator.type";
  public static final String CORE_ORCHESTRATOR_PREFIX = "core.orchestrator";
  public static final String CORE_ORCHESTRATOR_PROP_INTERNAL_JOBS_PRIORITY = "internal_jobs_priority";
  public static final String CORE_ORCHESTRATOR_PROP_INTERNAL_JOBS_PARALLELISM = "internal_jobs_parallelism";

  public static final String CORE_LDAP_DEFAULT_URL = "ldap://localhost";
  public static final int CORE_LDAP_DEFAULT_PORT = 10389;

  /*
   * Solr Retry
   */
  public static final String SOLR_RETRY_DELAY = "core.solr.retry.delay";
  public static final String SOLR_RETRY_MAX_DELAY = "core.solr.retry.maxDelay";
  public static final String SOLR_RETRY_DELAY_FACTOR = "core.solr.retry.delayFactor";
  public static final String SOLR_RETRY_MAX_RETRIES = "core.solr.retry.maxRetries";
  public static final String SOLR_RETRY_HANDLE_EXCEPTIONS = "core.solr.retry.handleExceptions[]";

  /*
   * USER REGISTRATION SETTINGS
   */
  public static final String USER_REGISTRATION_DISABLED = "core.user_registration.disabled";

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
  public static final String INDEX_NOTIFICATION = "Notification";
  public static final String INDEX_RISK_INCIDENCE = "RiskIncidence";
  public static final String INDEX_DIP = "DIP";
  public static final String INDEX_DIP_FILE = "DIPFile";
  public static final String INDEX_REPRESENTATION_INFORMATION = "RepresentationInformation";
  public static final String INDEX_DISPOSAL_CONFIRMATION = "DisposalConfirmation";

  /*
   * INDEXED CLASSES
   */
  public static final List<String> WHITELIST_CLASS_NAMES = Collections.unmodifiableList(
    Arrays.asList("org.roda.core.data.v2.ip.IndexedAIP", "org.roda.core.data.v2.ip.IndexedRepresentation",
      "org.roda.core.data.v2.ip.IndexedFile", "org.roda.core.data.v2.ip.IndexedDIP", "org.roda.core.data.v2.ip.DIPFile",
      "org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent",
      "org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent", "org.roda.core.data.v2.jobs.Job",
      "org.roda.core.data.v2.jobs.IndexedReport", "org.roda.core.data.v2.log.LogEntry",
      "org.roda.core.data.v2.notifications.Notification", "org.roda.core.data.v2.risks.IndexedRisk",
      "org.roda.core.data.v2.risks.RiskIncidence", "org.roda.core.data.v2.ri.RepresentationInformation",
      "org.roda.core.data.v2.ip.TransferredResource", "org.roda.core.data.v2.user.User",
      "org.roda.core.data.v2.user.Group", "org.roda.core.data.v2.user.RODAMember",
      "org.roda.core.data.v2.ip.disposal.DisposalConfirmation", "org.roda.core.data.v2.user.RodaPrincipal",
      "org.roda.core.data.v2.ip.AIP", "org.roda.core.data.v2.risks.Risk", "org.roda.core.events.pekko.CRDTWrapper",
      "org.roda.core.data.v2.ip.DIP", "org.roda.core.data.v2.ip.metadata.DescriptiveMetadata",
      "org.roda.core.data.v2.ip.disposal.DisposalConfirmationAIPEntry",
      "org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalConfirmationAIPMetadata",
      "org.roda.core.data.v2.ip.disposal.DisposalHold", "org.roda.core.data.v2.ip.disposal.DisposalRule",
      "org.roda.core.data.v2.ip.disposal.DisposalSchedule",
      "org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalScheduleAIPMetadata", "org.roda.core.data.v2.ip.File",
      "org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalHoldAIPMetadata",
      "org.roda.core.data.v2.ip.disposal.DisposalHoldAssociation", "org.roda.core.data.v2.jobs.Job",
      "org.roda.core.data.v2.ip.metadata.OtherMetadata", "org.roda.core.data.v2.ip.metadata.PreservationMetadata",
      "org.roda.core.data.v2.jobs.Report", "org.roda.core.data.v2.ip.Representation",
      "org.roda.core.protocols.AbstractProtocol", "org.roda.core.protocols.RODAProtocol",
      "org.roda.core.protocols.protocols.FileProtocol", "org.roda.core.protocols.protocols.HttpProtocol",
      "org.roda.core.protocols.protocols.HttpsProtocol", "org.roda.core.data.v2.accessKey.AccessKey",
      "org.roda.core.data.v2.synchronization.central.DistributedInstance",
      "org.roda.core.data.v2.synchronization.local.LocalInstance", "org.roda.core.data.v2.ip.ShallowFile"));

  /*
   * STORAGE CONTAINERS
   */
  public static final String STORAGE_CONTAINER_AIP = "aip";
  public static final String STORAGE_CONTAINER_PRESERVATION = "preservation";
  public static final String STORAGE_CONTAINER_ACTIONLOG = "action-log";
  public static final String STORAGE_CONTAINER_JOB = "job";
  public static final String STORAGE_CONTAINER_JOB_REPORT = "job-report";
  public static final String STORAGE_CONTAINER_RISK = "risk";
  public static final String STORAGE_CONTAINER_RISK_INCIDENCE = "risk-incidence";
  public static final String STORAGE_CONTAINER_AGENT = "agent";
  public static final String STORAGE_CONTAINER_NOTIFICATION = "notification";
  public static final String STORAGE_CONTAINER_PRESERVATION_AGENTS = "agents";
  public static final String STORAGE_CONTAINER_DIP = "dip";
  public static final String STORAGE_CONTAINER_REPRESENTATION_INFORMATION = "representation-information";
  public static final String STORAGE_CONTAINER_DISPOSAL_HOLD = "disposal-hold";
  public static final String STORAGE_CONTAINER_DISPOSAL_SCHEDULE = "disposal-schedule";
  public static final String STORAGE_CONTAINER_DISPOSAL_RULE = "disposal-rule";
  public static final String STORAGE_CONTAINER_DISTRIBUTED_INSTANCES = "distributed-instances";
  public static final String STORAGE_CONTAINER_ACCESS_KEYS = "access-keys";

  /*
   * Disposal Confirmation
   */
  public static final String STORAGE_CONTAINER_DISPOSAL_CONFIRMATION = "disposal-confirmation";
  public static final String STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_METADATA_FILENAME = "metadata.json";
  public static final String STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_AIPS_FILENAME = "aips.jsonl";
  public static final String STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_SCHEDULES_FILENAME = "schedules.jsonl";
  public static final String STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_HOLDS_FILENAME = "holds.jsonl";
  public static final String STORAGE_DIRECTORY_DISPOSAL_CONFIRMATION_TRANSITIVE_HOLDS_FILENAME = "transitive-holds.jsonl";

  public static final String STORAGE_HISTORY_CONTAINER_DATA = "data";
  public static final String STORAGE_HISTORY_CONTAINER_METADATA = "metadata";

  /*
   * STORAGE DIRECTORIES
   */
  public static final String STORAGE_DIRECTORY_METADATA = "metadata";
  public static final String STORAGE_DIRECTORY_DESCRIPTIVE = "descriptive";
  public static final String STORAGE_DIRECTORY_PRESERVATION = "preservation";
  public static final String STORAGE_DIRECTORY_TECHNICAL = "technical";
  public static final String STORAGE_DIRECTORY_OTHER_TECH_METADATA = "otherTechMd";
  public static final String STORAGE_DIRECTORY_REPRESENTATIONS = "representations";
  public static final String STORAGE_DIRECTORY_SUBMISSION = "submission";
  public static final String STORAGE_DIRECTORY_DOCUMENTATION = "documentation";
  public static final String STORAGE_DIRECTORY_SCHEMAS = "schemas";
  public static final String STORAGE_DIRECTORY_DATA = "data";
  public static final String STORAGE_DIRECTORY_AGENTS = "agents";
  public static final String STORAGE_DIRECTORY_EVENTS = "events";
  public static final String STORAGE_DIRECTORY_OTHER = "other";

  public static final String STORAGE_AIP_METADATA_FILENAME = "aip.json";
  public static final String STORAGE_DIP_METADATA_FILENAME = "dip.json";

  /*
   * OTHER METADATA TYPES
   */
  public static final String OTHER_METADATA_TYPE_SIEGFRIED = "Siegfried";
  public static final String OTHER_METADATA_TYPE_APACHE_TIKA = "ApacheTika";
  public static final String OTHER_METADATA_TYPE_DIGITAL_SIGNATURE = "DigitalSignature";
  public static final String OTHER_METADATA_TYPE_DROID = "DROID";
  public static final String OTHER_METADATA_TYPE_EXIFTOOL = "ExifTool";
  public static final String OTHER_METADATA_TYPE_AVPROBE = "Avprobe";
  public static final String OTHER_METADATA_TYPE_FITS = "FITS";
  public static final String OTHER_METADATA_TYPE_JHOVE = "JHOVE";
  public static final String OTHER_METADATA_TYPE_JPYLYZER = "jpylyzer";
  public static final String OTHER_METADATA_TYPE_MEDIAINFO = "MediaInfo";

  /*
   * Permissions
   */
  public static final String ADMIN = "admin";
  public static final String ADMINISTRATORS = "administrators";
  public static final String INDEX_PERMISSION_USERS_PREFIX = "permission_users_";
  public static final String INDEX_PERMISSION_GROUPS_PREFIX = "permission_groups_";

  /*
   * Index common fields
   */
  public static final String INDEX_UUID = "uuid";
  public static final String INDEX_ID = "id";
  public static final String INDEX_STATE = "state";
  public static final String INDEX_SEARCH = "search";
  public static final String INDEX_WILDCARD = "*";

  public static final String INDEX_INSTANCE_ID = "instanceId";
  public static final String INDEX_INSTANCE_NAME = "instanceName";

  public static final String INDEX_CREATION_DATE = "creationDate";

  /*
   * AIP FIELDS
   */
  /**
   * 20170213 hsilva: use this field with caution, i.e., is this the field to be
   * used or should it be RodaConstants.INDEX_UUID???
   */
  public static final String AIP_ID = "id";
  public static final String AIP_PARENT_ID = "parentId";
  public static final String AIP_ANCESTORS = "ancestors";
  public static final String INGEST_SIP_IDS = "ingestSIPIds";
  public static final String INGEST_JOB_ID = "ingestJobId";
  public static final String INGEST_UPDATE_JOB_IDS = "ingestUpdateJobIds";
  public static final String ALL_INGEST_JOB_IDS = "allIngestJobIds";

  public static final String AIP_DESCRIPTIVE_METADATA_ID = "descriptiveMetadataId";
  public static final String AIP_REPRESENTATION_ID = "representationId";
  public static final String AIP_HAS_REPRESENTATIONS = "hasRepresentations";
  public static final String AIP_GHOST = "ghost";
  public static final String AIP_HAS_SHALLOW_FILES = "hasShallowFiles";
  public static final String AIP_CHILDREN = "aip_children";

  public static final String AIP_DESCRIPTIVE_METADATA = "descriptiveMetadata";
  public static final String AIP_REPRESENTATIONS = "representations";

  public static final String AIP_NUMBER_OF_SUBMISSION_FILES = "numberOfSubmissionFiles";
  public static final String AIP_NUMBER_OF_DOCUMENTATION_FILES = "numberOfDocumentationFiles";
  public static final String AIP_NUMBER_OF_SCHEMA_FILES = "numberOfSchemaFiles";

  public static final String AIP_TYPE = "type";
  public static final String AIP_LEVEL = "level";
  public static final String AIP_TITLE = "title";
  public static final String AIP_TITLE_SORT = "title_sort";
  public static final String AIP_DATE_INITIAL = "dateInitial";
  public static final String AIP_DATE_FINAL = "dateFinal";
  public static final String AIP_CHILDREN_COUNT = "childrenCount";
  public static final String AIP_DESCRIPTION = "description";
  public static final String AIP_STATE = "state";
  public static final String AIP_LABEL = "label";

  public static final String AIP_CREATED_ON = "createdOn";
  public static final String AIP_CREATED_BY = "createdBy";
  public static final String AIP_UPDATED_ON = "updatedOn";
  public static final String AIP_UPDATED_BY = "updatedBy";

  public static final String AIP_DISPOSAL_SCHEDULE_ID = "disposalScheduleId";
  public static final String AIP_DISPOSAL_SCHEDULE_NAME = "disposalScheduleName";
  public static final String AIP_DISPOSAL_HOLDS_ID = "disposalHoldsId";
  public static final String AIP_TRANSITIVE_DISPOSAL_HOLDS_ID = "transitiveDisposalHoldsId";
  public static final String AIP_DESTROYED_ON = "destroyedOn";
  public static final String AIP_DESTROYED_BY = "destroyedBy";
  public static final String AIP_DISPOSAL_ACTION = "disposalAction";
  public static final String AIP_DISPOSAL_HOLD_STATUS = "disposalHoldStatus";
  public static final String AIP_OVERDUE_DATE = "overdueDate";
  public static final String AIP_DISPOSAL_CONFIRMATION_ID = "disposalConfirmationID";
  public static final String AIP_DISPOSAL_SCHEDULE_ASSOCIATION_TYPE = "disposalScheduleAssociationType";
  public static final String AIP_DISPOSAL_RETENTION_PERIOD_START_DATE = "retentionPeriodStartDate";
  public static final String AIP_DISPOSAL_RETENTION_PERIOD_DURATION = "retentionPeriodDuration";
  public static final String AIP_DISPOSAL_RETENTION_PERIOD_INTERVAL = "retentionPeriodInterval";
  public static final String AIP_DISPOSAL_RETENTION_PERIOD_DETAILS = "retentionPeriodDetails";
  public static final String AIP_DISPOSAL_RETENTION_PERIOD_CALCULATION = "retentionPeriodCalculation";

  // AIP types
  public static final String AIP_TYPE_MIXED = "MIXED";

  /*
   * Descriptive metadata FIELDS
   */
  public static final String DESCRIPTIVE_METADATA_AIP_ID = "aipId";
  public static final String DESCRIPTIVE_METADATA_REPRESENTATION_ID = "representationId";

  /*
   * Representation FIELDS
   */
  public static final String REPRESENTATION_ID = "id";
  public static final String REPRESENTATION_AIP_ID = "aipId";
  public static final String REPRESENTATION_ORIGINAL = "original";
  public static final String REPRESENTATION_TYPE = "type";
  public static final String REPRESENTATION_TITLE = "title";
  public static final String REPRESENTATION_SIZE_IN_BYTES = "sizeInBytes";
  public static final String REPRESENTATION_NUMBER_OF_DATA_FILES = "numberOfDataFiles";
  public static final String REPRESENTATION_NUMBER_OF_DATA_FOLDERS = "numberOfDataFolders";
  public static final String REPRESENTATION_NUMBER_OF_DOCUMENTATION_FILES = "numberOfDocumentationFiles";
  public static final String REPRESENTATION_NUMBER_OF_SCHEMA_FILES = "numberOfSchemaFiles";
  public static final String REPRESENTATION_ANCESTORS = "ancestors";
  public static final String REPRESENTATION_HAS_SHALLOW_FILES = "hasShallowFiles";

  public static final String REPRESENTATION_CREATED_ON = "createdOn";
  public static final String REPRESENTATION_CREATED_BY = "createdBy";
  public static final String REPRESENTATION_UPDATED_ON = "updatedOn";
  public static final String REPRESENTATION_UPDATED_BY = "updatedBy";
  public static final String REPRESENTATION_STATES = "representationStates";

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

  public static final String REPOSITORY_PERMISSIONS_AIP_APPRAISAL = "aip.appraisal";
  public static final String REPOSITORY_PERMISSIONS_AIP_CREATE_TOP = "aip.create.top";
  public static final String REPOSITORY_PERMISSIONS_AIP_CREATE_BELOW = "aip.create.below";
  public static final String REPOSITORY_PERMISSIONS_AIP_DELETE = "aip.delete";
  public static final String REPOSITORY_PERMISSIONS_AIP_READ = "aip.read";
  public static final String REPOSITORY_PERMISSIONS_AIP_UPDATE = "aip.update";

  public static final String REPOSITORY_PERMISSIONS_REPRESENTATION_CREATE = "representation.create";
  public static final String REPOSITORY_PERMISSIONS_REPRESENTATION_DELETE = "representation.delete";
  public static final String REPOSITORY_PERMISSIONS_REPRESENTATION_READ = "representation.read";
  public static final String REPOSITORY_PERMISSIONS_REPRESENTATION_UPDATE = "representation.update";

  public static final String REPOSITORY_PERMISSIONS_DESCRIPTIVE_METADATA_CREATE = "descriptive_metadata.create";
  public static final String REPOSITORY_PERMISSIONS_DESCRIPTIVE_METADATA_DELETE = "descriptive_metadata.delete";
  public static final String REPOSITORY_PERMISSIONS_DESCRIPTIVE_METADATA_READ = "descriptive_metadata.read";
  public static final String REPOSITORY_PERMISSIONS_DESCRIPTIVE_METADATA_UPDATE = "descriptive_metadata.update";

  public static final String REPOSITORY_PERMISSIONS_PRESERVATION_METADATA_CREATE = "preservation_metadata.create";
  public static final String REPOSITORY_PERMISSIONS_PRESERVATION_METADATA_DELETE = "preservation_metadata.delete";
  public static final String REPOSITORY_PERMISSIONS_PRESERVATION_METADATA_READ = "preservation_metadata.read";

  public static final String REPOSITORY_PERMISSIONS_TRANSFER_CREATE = "transfer.create";
  public static final String REPOSITORY_PERMISSIONS_TRANSFER_DELETE = "transfer.delete";
  public static final String REPOSITORY_PERMISSIONS_TRANSFER_READ = "transfer.read";
  public static final String REPOSITORY_PERMISSIONS_TRANSFER_UPDATE = "transfer.update";

  public static final String REPOSITORY_PERMISSIONS_JOB_MANAGE = "job.manage";
  public static final String REPOSITORY_PERMISSIONS_JOB_READ = "job.read";

  public static final String REPOSITORY_PERMISSIONS_MEMBER_MANAGE = "member.manage";
  public static final String REPOSITORY_PERMISSIONS_MEMBER_READ = "member.read";

  public static final String REPOSITORY_PERMISSIONS_NOTIFICATION_MANAGE = "notification.manage";
  public static final String REPOSITORY_PERMISSIONS_NOTIFICATION_READ = "notification.read";

  public static final String REPOSITORY_PERMISSIONS_LOG_ENTRY_MANAGE = "log_entry.manage";
  public static final String REPOSITORY_PERMISSIONS_LOG_ENTRY_READ = "log_entry.read";

  public static final String REPOSITORY_PERMISSIONS_RISK_MANAGE = "risk.manage";
  public static final String REPOSITORY_PERMISSIONS_RISK_READ = "risk.read";

  public static final String REPOSITORY_PERMISSIONS_REPRESENTATION_INFORMATION_MANAGE = "ri.manage";
  public static final String REPOSITORY_PERMISSIONS_REPRESENTATION_INFORMATION_READ = "ri.read";

  public static final String REPOSITORY_PERMISSIONS_DISPOSAL_SCHEDULE_READ = "disposal_schedule.read";

  public static final String REPOSITORY_PERMISSIONS_DISTRIBUTED_INSTANCES_MANAGE = "distributed_instances.manage";
  public static final String REPOSITORY_PERMISSIONS_DISTRIBUTED_INSTANCES_READ = "distributed_instances.read";

  public static final String REPOSITORY_PERMISSIONS_LOCAL_INSTANCES_MANAGE = "local_instance_configuration.manage";
  public static final String REPOSITORY_PERMISSIONS_LOCAL_INSTANCES_READ = "local_instance_configuration.read";

  public static final String LOG_ACTION_COMPONENT = "actionComponent";
  public static final String LOG_ACTION_METHOD = "actionMethod";
  public static final String LOG_ADDRESS = "address";
  public static final String LOG_DATETIME = "datetime";
  public static final String LOG_DURATION = "duration";
  public static final String LOG_ID = "id";
  public static final String LOG_RELATED_OBJECT_ID = "relatedObject";
  public static final String LOG_USERNAME = "username";
  public static final String LOG_PARAMETERS = "parameters";
  public static final String LOG_STATE = "state";
  public static final String LOG_FILE_ID = "fileID";
  public static final String LOG_LINE_NUMBER = "lineNumber";
  public static final String LOG_REQUEST_HEADER_UUID = "requestHeaderUUID";
  public static final String LOG_REQUEST_HEADER_REASON = "requestHeaderReason";
  public static final String LOG_REQUEST_HEADER_TYPE = "requestHeaderType";

  // MEMBER: USER or GROUP
  public static final String MEMBERS_ID = "id";
  public static final String MEMBERS_NAME = "name";
  public static final String MEMBERS_FULLNAME = "fullName";
  public static final String MEMBERS_IS_ACTIVE = "isActive";
  public static final String MEMBERS_IS_USER = "isUser";
  public static final String MEMBERS_GROUPS = "groups";
  public static final String MEMBERS_USERS = "users";
  public static final String MEMBERS_ROLES_DIRECT = "rolesDirect";
  public static final String MEMBERS_ROLES_ALL = "rolesAll";
  public static final String MEMBERS_EMAIL = "email";

  public static final String TRANSFERRED_RESOURCE_ID = "id";
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
  // REST
  public static final String TRANSFERRED_RESOURCE_DIRECTORY_NAME = "name";
  public static final String TRANSFERRED_RESOURCE_REPLACE_EXISTING = "replace_existing";
  public static final String TRANSFERRED_RESOURCE_RESOURCE_ID = "resourceId";

  public static final String JOB_ID = "id";
  public static final String JOB_NAME = "name";
  public static final String JOB_USERNAME = "username";
  public static final String JOB_START_DATE = "startDate";
  public static final String JOB_END_DATE = "endDate";
  public static final String JOB_STATE = "state";
  public static final String JOB_SCHEDULE_INFO = "scheduleInfo";
  public static final String JOB_EXECUTION_TYPE_APPROVAL = "APPROVAL";
  public static final String JOB_PARALLELISM = "parallelism";
  public static final String JOB_PRIORITY = "priority";
  public static final String JOB_STATE_DETAILS = "stateDetails";
  public static final String JOB_STATS = "jobStats";
  public static final String JOB_COMPLETION_PERCENTAGE = "completionPercentage";
  public static final String JOB_SOURCE_OBJECTS_COUNT = "sourceObjectsCount";
  public static final String JOB_SOURCE_OBJECTS_WAITING_TO_BE_PROCESSED = "sourceObjectsWaitingToBeProcessed";
  public static final String JOB_SOURCE_OBJECTS_BEING_PROCESSED = "sourceObjectsBeingProcessed";
  public static final String JOB_SOURCE_OBJECTS_PROCESSED_WITH_SUCCESS = "sourceObjectsProcessedWithSuccess";
  public static final String JOB_SOURCE_OBJECTS_PROCESSED_WITH_PARTIAL_SUCCESS = "sourceObjectsProcessedWithPartialSuccess";
  public static final String JOB_SOURCE_OBJECTS_PROCESSED_WITH_FAILURE = "sourceObjectsProcessedWithFailure";
  public static final String JOB_SOURCE_OBJECTS_PROCESSED_WITH_SKIPPED = "sourceObjectsProcessedWithSkipped";
  public static final String JOB_OUTCOME_OBJECTS_WITH_MANUAL_INTERVENTION = "outcomeObjectsWithManualIntervention";
  public static final String JOB_PLUGIN = "plugin";
  public static final String JOB_PLUGIN_TYPE = "pluginType";
  public static final String JOB_PLUGIN_PARAMETERS = "pluginParameters";
  public static final String JOB_RESOURCE_TYPE = "resourceType";
  public static final String JOB_SOURCE_OBJECTS = "sourceObjects";
  public static final String JOB_OUTCOME_OBJECTS_CLASS = "outcomeObjectsClass";
  public static final String JOB_IN_FINAL_STATE = "inFinalState";
  public static final String JOB_STOPPING = "stopping";
  public static final String JOB_HAS_FAILURES = "hasFailures";
  public static final String JOB_HAS_PARTIAL_SUCCESS = "hasPartialSuccess";
  public static final String JOB_HAS_SKIPPED = "hasSkipped";
  public static final String JOB_ATTACHMENTS = "attachments";
  public static final String JOB_FILE_EXTENSION = ".json";

  public static final String DISPOSAL_CONFIRMATION_CREATED_ON = "createdOn";
  public static final String DISPOSAL_CONFIRMATION_CREATED_BY = "createdBy";
  public static final String DISPOSAL_CONFIRMATION_EXECUTED_ON = "executedOn";
  public static final String DISPOSAL_CONFIRMATION_EXECUTED_BY = "executedBy";
  public static final String DISPOSAL_CONFIRMATION_RESTORED_ON = "restoredOn";
  public static final String DISPOSAL_CONFIRMATION_RESTORED_BY = "restoredBy";
  public static final String DISPOSAL_CONFIRMATION_NUMBER_OF_AIPS = "numberOfAIPs";
  public static final String DISPOSAL_CONFIRMATION_STATE = "state";
  public static final String DISPOSAL_CONFIRMATION_TITLE = "title";
  public static final String DISPOSAL_CONFIRMATION_EXTRA_INFO = "extraInformation";
  public static final String DISPOSAL_CONFIRMATION_STORAGE_SIZE = "size";

  /* Disposal related parameters */
  public static final String DISPOSAL_HOLD_FILE_EXTENSION = ".json";

  /* Distributed related parameters */
  public static final String DISTRIBUTED_INSTANCE_FILE_EXTENSION = ".json";

  /* Plugins related parameters */
  public static final String PLUGIN_PARAMS_LOCK_REQUEST_UUID = "parameter.lock_request_uuid";
  public static final String PLUGIN_PARAMS_JOB_ID = "job.id";
  public static final String PLUGIN_PARAMS_OTHER_JOB_ID = "other_job.id";
  public static final String PLUGIN_PARAMS_DO_FEATURE_EXTRACTION = "parameter.do_feature_extraction";
  public static final String PLUGIN_PARAMS_DO_FULLTEXT_EXTRACTION = "parameter.do_fulltext_extraction";
  public static final String PLUGIN_PARAMS_TOTAL_STEPS = "parameter.total_steps";
  public static final String PLUGIN_PARAMS_STRING_VALUE = "parameter.string_value";
  public static final String PLUGIN_PARAMS_BOOLEAN_VALUE = "parameter.boolean_value";
  public static final String PLUGIN_PARAMS_CLEAR_INDEXES = "parameter.clear_indexes";
  public static final String PLUGIN_PARAMS_OPTIMIZE_INDEXES = "parameter.optimize_indexes";
  public static final String PLUGIN_PARAMS_INSTANCE_IDENTIFIER = "parameter.instance_identifier";
  public static final String PLUGIN_PARAMS_OBJECT_CLASS = "parameter.object_class";
  public static final String PLUGIN_PARAMS_CLASS_CANONICAL_NAME = "parameter.class_canonical_name";
  public static final String PLUGIN_PARAMS_SIP_TO_AIP_CLASS = "parameter.sip_to_aip_class";
  public static final String PLUGIN_PARAMS_PARENT_ID = "parameter.parent_id";
  public static final String PLUGIN_PARAMS_FORCE_PARENT_ID = "parameter.force_parent_id";
  public static final String PLUGIN_PARAMS_RISK_ID = "parameter.risk_id";
  public static final String PLUGIN_PARAMS_RISK_NAME = "parameter.risk_name";
  public static final String PLUGIN_PARAMS_RISK_CATEGORY = "parameter.risk_category";
  public static final String PLUGIN_PARAMS_RISK_INCIDENCE_DESCRIPTION = "parameter.risk_incidence_description";
  public static final String PLUGIN_PARAMS_RISK_INCIDENCE_SEVERITY = "parameter.risk_incidence_severity";
  public static final String PLUGIN_PARAMS_RISK_INCIDENCE_STATUS = "parameter.risk_incidence_status";
  public static final String PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_ON = "parameter.risk_incidence_mitigated_on";
  public static final String PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_BY = "parameter.risk_incidence_mitigated_by";
  public static final String PLUGIN_PARAMS_RISK_INCIDENCE_MITIGATED_DESCRIPTION = "parameter.risk_incidence_mitigated_description";
  public static final String PLUGIN_PARAMS_DO_VIRUS_CHECK = "parameter.do_virus_check";
  public static final String PLUGIN_PARAMS_DO_DESCRIPTIVE_METADATA_VALIDATION = "parameter.do_descriptive_metadata_validation";
  public static final String PLUGIN_PARAMS_CREATE_PREMIS_SKELETON = "parameter.create.premis.skeleton";
  public static final String PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION = "parameter.do_file_format_identification";
  public static final String PLUGIN_PARAMS_DO_PDFTOPDFA_CONVERSION = "parameter.do_pdf_to_pdfa_conversion";
  public static final String PLUGIN_PARAMS_DO_PRODUCER_AUTHORIZATION_CHECK = "parameter.do_producer_authorization_check";
  public static final String PLUGIN_PARAMS_DO_APPLY_DISPOSAL_RULES = "parameter.do_apply_disposal_rules";
  public static final String PLUGIN_PARAMS_DO_AUTO_ACCEPT = "parameter.do_auto_accept";
  public static final String PLUGIN_PARAMS_EMAIL_NOTIFICATION = "parameter.email_notification";
  public static final String PLUGIN_PARAMS_NOTIFICATION_WHEN_FAILED = "parameter.notification_when_failed";
  public static final String PLUGIN_PARAMS_CREATES_PLUGIN_EVENT = "createsPluginEvent";
  public static final String PLUGIN_PARAMS_CREATE_SUBMISSION = "parameter.create_submission";
  public static final String PLUGIN_PARAMS_USERNAME = "parameter.username";
  public static final String PLUGIN_PARAMS_IGNORE_OTHER_FILES = "parameter.ignore_other_files";
  public static final String PLUGIN_PARAMS_VERAPDF_VALIDATION = "parameter.ignore_verapdf_validation";
  public static final String PLUGIN_PARAMS_PDF_PROFILE = "parameter.pdf_profile";
  public static final String PLUGIN_PARAMS_SIGNATURE_VERIFY = "parameter.signature_verify";
  public static final String PLUGIN_PARAMS_SIGNATURE_EXTRACT = "parameter.signature_extract";
  public static final String PLUGIN_PARAMS_SIGNATURE_STRIP = "parameter.signature_strip";
  public static final String PLUGIN_PARAMS_INPUT_FORMAT = "parameter.input_format";
  public static final String PLUGIN_PARAMS_OUTPUT_FORMAT = "parameter.output_format";
  public static final String PLUGIN_PARAMS_COMMAND_ARGUMENTS = "parameter.command_arguments";
  public static final String PLUGIN_PARAMS_OUTPUT_ARGUMENTS = "parameter.output_arguments";
  public static final String PLUGIN_PARAMS_CONVERSION_PROFILE = "parameter.conversion_profile";
  public static final String PLUGIN_PARAMS_DO_REPORT = "parameter.do_report";
  public static final String PLUGIN_PARAMS_REPORTING_CLASS = "parameter.reporting_class";
  public static final String PLUGIN_PARAMS_HAS_COMPRESSION = "parameter.has_compression";
  public static final String PLUGIN_PARAMS_HAS_SINGLE_REQUEST = "parameter.has_single_request";
  public static final String PLUGIN_PARAMS_REPRESENTATION_OR_DIP = "parameter.representation_or_dip";
  public static final String PLUGIN_PARAMS_DISSEMINATION_TITLE = "parameter.dissemination_title";
  public static final String PLUGIN_PARAMS_DISSEMINATION_DESCRIPTION = "parameter.dissemination_description";
  public static final String PLUGIN_PARAMS_AIP_PARENT_ID = "parameter.aip_parent_id";
  public static final String PLUGIN_PARAMS_AIP_ID = "parameter.aip_id";
  public static final String PLUGIN_PARAMS_ID = "parameter.id";
  public static final String PLUGIN_PARAMS_PERMISSIONS_JSON = "parameter.permissions_json";
  public static final String PLUGIN_PARAMS_DETAILS = "parameter.details";
  public static final String PLUGIN_PARAMS_OUTCOME_TEXT = "parameter.outcome_text";
  public static final String PLUGIN_PARAMS_EVENT_DESCRIPTION = "parameter.event_description";
  public static final String PLUGIN_PARAMS_DELETE_OLDER_THAN_X_DAYS = "parameter.delete_older_than_x_days";
  public static final String PLUGIN_PARAMS_SIP_UPDATE_INFORMATION = "parameter.sip_update_information";
  public static final String PLUGIN_PARAMS_SIEGFRIED_OVERWRITE_MANUAL = "parameter.siegfried_overwrite_manual";
  public static final String PLUGIN_PARAMS_REPRESENTATION_TYPE = "parameter.representation_type";
  public static final String PLUGIN_PARAMS_OUTCOMEOBJECTID_TO_SOURCEOBJECTID_MAP = "parameter.outcomeobjectid_to_sourceobjectid_map";
  public static final String PLUGIN_PARAMS_NEW_TYPE = "parameter.new_type";
  public static final String PLUGIN_PARAMS_NEW_STATUS = "parameter.new_status";
  public static final String PLUGIN_PARAMS_ACCEPT = "parameter.accept";
  public static final String PLUGIN_PARAMS_REJECT_REASON = "parameter.reject_reason";
  public static final String PLUGIN_PARAMS_RECURSIVE = "parameter.recursive";
  public static final String PLUGIN_PARAMS_DONT_CHECK_RELATIVES = "parameter.dont_check_relatives";
  public static final String PLUGIN_PARAMS_REPRESENTATION_INFORMATION_FILTER = "parameter.representation_information_filter";
  public static final String PLUGIN_PARAMS_RODA_MEMBER_ACTIVATE = "parameter.activate";

  public static final String PLUGIN_PARAMS_VALIDATE_DESCRIPTIVE_METADATA = "parameter.validate_descriptive_metadata";
  public static final String PLUGIN_PARAMS_DESCRIPTIVE_METADATA_TYPE = "parameter.metadata_type";
  public static final String PLUGIN_PARAMS_DESCRIPTIVE_METADATA_VERSION = "parameter.metadata_version";
  public static final String PLUGIN_PARAMS_DESCRIPTIVE_METADATA_FORCE_TYPE = "parameter.force_type";

  public static final String PLUGIN_PARAMS_INCLUDE_SELECTED_DESCRIPTIVE_METADATA = "parameter.include_selected_descriptive_metadata";
  public static final String PLUGIN_PARAMS_SELECTED_DESCRIPTIVE_METADATA = "parameter.selected_descriptive_metadata";
  public static final String PLUGIN_PARAMS_INCLUDE_ALL_PRESERVATION_METADATA = "parameter.include_all_preservation_metadata";
  public static final String PLUGIN_PARAMS_INCLUDE_SELECTED_OTHER_METADATA = "parameter.include_selected_other_metadata";
  public static final String PLUGIN_PARAMS_SELECTED_OTHER_METADATA = "parameter.selected_other_metadata";
  public static final String PLUGIN_PARAMS_INCLUDE_SELECTED_REPRESENTATIONS = "parameter.include_selected_representations";
  public static final String PLUGIN_PARAMS_SELECTED_REPRESENTATIONS = "parameter.selected_representations";
  public static final String PLUGIN_PARAMS_INCLUDE_SUBMISSION = "parameter.include_submission";
  public static final String PLUGIN_PARAMS_INCLUDE_SCHEMAS = "parameter.include_schemas";
  public static final String PLUGIN_PARAMS_INCLUDE_DOCUMENTATION = "parameter.include_documentation";

  public static final String PLUGIN_PARAMS_DISPOSAL_SCHEDULE_ID = "parameter.disposal_schedule_id";
  public static final String PLUGIN_PARAMS_DISPOSAL_SCHEDULE_OVERWRITE_ALL = "parameter.disposal_schedule_overwrite_all";
  public static final String PLUGIN_PARAMS_DISPOSAL_SCHEDULE_OVERWRITE_MANUAL = "parameter.disposal_schedule_overwrite_manual";

  public static final String PLUGIN_PARAMS_DISPOSAL_HOLD_ID = "parameter.disposal_hold_id";
  public static final String PLUGIN_PARAMS_DISPOSAL_HOLD_LIFT_ALL = "parameter.clear_all";
  public static final String PLUGIN_PARAMS_DISPOSAL_HOLD_DISASSOCIATE_ALL = "parameter.clear_all";
  public static final String PLUGIN_PARAMS_DISPOSAL_HOLD_OVERRIDE = "parameter.override";

  public static final String PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_ID = "parameter.disposal_confirmation_id";
  public static final String PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_TITLE = "parameter.disposal_confirmation_title";
  public static final String PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_EXTRA_INFO = "parameter.disposal_confirmation_extra";
  public static final String PLUGIN_PARAMS_DISPOSAL_CONFIRMATION_DESTROY_EXECUTE_AGAIN = "parameter.disposal_confirmation_re_execute";

  public static final String PLUGIN_PARAMS_BUNDLE_WORKING_PATH = "parameter.bundle_working_path";
  public static final String PLUGIN_PARAMS_BUNDLE_FROM_DATE = "parameter.bundle_from_date";
  public static final String PLUGIN_PARAMS_BUNDLE_TO_DATE = "parameter.bundle_to_date";
  public static final String PLUGIN_PARAMS_BUNDLE_PATH = "parameter.destination_path";
  public static final String PLUGIN_PARAMS_CENTRAL_INSTANCE_URL = "parameter.central_instance_url";
  public static final String PLUGIN_PARAMS_BUNDLE_NAME = "parameter.bundle_name";

  // Local Instance Register Plugin
  public static final String PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_AIP_PLUGIN = "parameter.do_instance_identifier_aip";
  public static final String PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_AIP_EVENT_PLUGIN = "parameter.do_instance_identifier_aip_event";
  public static final String PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_DIP_PLUGIN = "parameter.do_instance_identifier_dip";
  public static final String PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_REPRESENTATION_INFORMATION_PLUGIN = "parameter.do_instance_identifier_representation_information";
  public static final String PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_NOTIFICATION_PLUGIN = "parameter.do_instance_identifier_notification";
  public static final String PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_RISK_PLUGIN = "parameter.do_instance_identifier_risk";
  public static final String PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_RISK_INCIDENCE_PLUGIN = "parameter.do_instance_identifier_risk_incidence";
  public static final String PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_JOB_PLUGIN = "parameter.do_instance_identifier_job";
  public static final String PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_REPOSITORY_EVENT_PLUGIN = "parameter.do_instance_identifier_repository_event";
  public static final String PLUGIN_PARAMS_DO_INSTANCE_IDENTIFIER_PRESERVATION_AGENT_PLUGIN = "parameter.do_instance_identifier_preservation_agent";
  public static final String PLUGIN_PARAMS_DO_REGISTER_PLUGIN = "parameter.do_register";

  // Synchronize Plugin Parameters

  public static final String PLUGIN_PARAMS_DO_AIP_PACKAGE_PLUGIN = "parameter.do_aip_package";
  public static final String PLUGIN_PARAMS_DO_JOB_PACKAGE_PLUGIN = "parameter.do_job_package";
  public static final String PLUGIN_PARAMS_DO_DIP_PACKAGE_PLUGIN = "parameter.do_dip_package";
  public static final String PLUGIN_PARAMS_DO_RISK_INCIDENCE_PACKAGE_PLUGIN = "parameter.do_risk_incidence_package";
  public static final String PLUGIN_PARAMS_DO_REPOSITORY_EVENT_PACKAGE_PLUGIN = "parameter.do_repository_event_package";
  public static final String PLUGIN_PARAMS_DO_PRESERVATION_AGENT_PACKAGE_PLUGIN = "parameter.do_preservation_agent";
  public static final String PLUGIN_PARAMS_DO_BUILD_SYNC_MANIFEST_PLUGIN = "parameter.do_build_sync_manifest";
  public static final String PLUGIN_PARAMS_DO_SEND_SYNC_BUNDLE_PLUGIN = "parameter.do_send_sync_bundle";
  public static final String PLUGIN_PARAMS_DO_REQUEST_SYNC_BUNDLE_PLUGIN = "parameter.do_request_sync_bundle";

  // Edit File Format Plugin Parameters

  public static final String PLUGIN_PARAMS_EXTENSION = "parameter.extension";
  public static final String PLUGIN_PARAMS_MIMETYPE = "parameter.mime_type";
  public static final String PLUGIN_PARAMS_FORMAT = "parameter.format";
  public static final String PLUGIN_PARAMS_FORMAT_VERSION = "parameter.format_version";
  public static final String PLUGIN_PARAMS_PRONOM = "parameter.pronom";
  public static final String PLUGIN_PARAMS_CLEAR_INCIDENCES = "parameter.clear_incidences";

  public static final String PLUGIN_CATEGORY_CONVERSION = "conversion";
  public static final String PLUGIN_CATEGORY_CHARACTERIZATION = "characterization";
  public static final String PLUGIN_CATEGORY_RISK_MANAGEMENT = "risk_management";
  public static final String PLUGIN_CATEGORY_DISSEMINATION = "dissemination";
  public static final String PLUGIN_CATEGORY_INGEST = "ingest";
  public static final String PLUGIN_CATEGORY_REPLICATION = "replication";
  public static final String PLUGIN_CATEGORY_MANAGEMENT = "management";
  public static final String PLUGIN_CATEGORY_REINDEX = "reindex";
  public static final String PLUGIN_CATEGORY_VALIDATION = "validation";
  public static final String PLUGIN_CATEGORY_FORMAT_IDENTIFICATION = "format_identification";
  public static final String PLUGIN_CATEGORY_FEATURE_EXTRACTION = "feature_extraction";
  public static final String PLUGIN_CATEGORY_MISC = "misc";
  public static final String PLUGIN_CATEGORY_EXPERIMENTAL = "experimental";
  public static final String PLUGIN_CATEGORY_NOT_LISTABLE = "not_listable";
  public static final String PLUGIN_CATEGORY_DIGITAL_SIGNATURE = "digital_signature";
  public static final String PLUGIN_CATEGORY_RISK_ASSESSMENT = "risk_assessment";
  public static final String PLUGIN_CATEGORY_MAINTENANCE = "maintenance";
  public static final String PLUGIN_CATEGORY_E_ARCHIVING = "eArchiving";
  public static final String PLUGIN_CATEGORY_DATABASE_PRESERVATION = "database_preservation";

  public static final String PLUGIN_SELECT_ALL_RODA_OBJECTS = "All";

  public static final String JOB_PROCESS_ACTION = "action";
  public static final String JOB_PROCESS_INGEST = "ingest";

  /* Job Report */
  public static final String JOB_REPORT_ID = "id";
  public static final String JOB_REPORT_JOB_ID = "jobId";
  public static final String JOB_REPORT_SOURCE_OBJECT_ID = "sourceObjectId";
  public static final String JOB_REPORT_SOURCE_OBJECT_CLASS = "sourceObjectClass";
  public static final String JOB_REPORT_SOURCE_OBJECT_ORIGINAL_IDS = "sourceObjectOriginalIds";
  public static final String JOB_REPORT_SOURCE_OBJECT_ORIGINAL_NAME = "sourceObjectOriginalName";
  public static final String JOB_REPORT_OUTCOME_OBJECT_ID = "outcomeObjectId";
  public static final String JOB_REPORT_OUTCOME_OBJECT_CLASS = "outcomeObjectClass";
  public static final String JOB_REPORT_OUTCOME_OBJECT_STATE = "outcomeObjectState";
  public static final String JOB_REPORT_TITLE = "title";
  public static final String JOB_REPORT_DATE_CREATED = "dateCreated";
  public static final String JOB_REPORT_DATE_UPDATED = "dateUpdated";
  public static final String JOB_REPORT_INGEST_TYPE = "ingestType";
  public static final String JOB_REPORT_COMPLETION_PERCENTAGE = "completionPercentage";
  public static final String JOB_REPORT_STEPS_COMPLETED = "stepsCompleted";
  public static final String JOB_REPORT_TOTAL_STEPS = "totalSteps";
  public static final String JOB_REPORT_PLUGIN = "plugin";
  public static final String JOB_REPORT_PLUGIN_NAME = "pluginName";
  public static final String JOB_REPORT_PLUGIN_VERSION = "pluginVersion";
  public static final String JOB_REPORT_PLUGIN_STATE = "pluginState";
  public static final String JOB_REPORT_PLUGIN_DETAILS = "pluginDetails";
  public static final String JOB_REPORT_PLUGIN_IS_MANDATORY = "pluginIsMandatory";
  public static final String JOB_REPORT_HTML_PLUGIN_DETAILS = "htmlPluginDetails";
  public static final String JOB_REPORT_JOB_NAME = "jobName";
  public static final String JOB_REPORT_SOURCE_OBJECT_LABEL = "sourceObjectLabel";
  public static final String JOB_REPORT_OUTCOME_OBJECT_LABEL = "outcomeObjectLabel";
  public static final String JOB_REPORT_JOB_PLUGIN_TYPE = JOB_PLUGIN_TYPE;
  public static final String JOB_REPORT_SUCCESSFUL_PLUGINS = "successfulPlugins";
  public static final String JOB_REPORT_UNSUCCESSFUL_PLUGINS = "unsuccessfulPlugins";
  public static final String JOB_REPORT_UNSUCCESSFUL_PLUGINS_COUNTER = "unsuccessfulPluginsCounter";
  public static final String JOB_REPORT_FILE_EXTENSION = ".json";

  public static final String FILE_PATH = "path";
  public static final String FILE_ANCESTORS_PATH = "ancestorsPath";
  public static final String FILE_TECHNICAL_METADATA_ID = "technicalMetadataId";
  public static final String FILE_PARENT_UUID = "parentUUID";
  public static final String FILE_AIP_ID = "aipId";
  public static final String FILE_FORMAT_MIMETYPE = "formatMimetype";
  public static final String FILE_FORMAT_VERSION = "formatVersion";
  public static final String FILE_REPRESENTATION_UUID = "representationUUID";
  public static final String FILE_REPRESENTATION_ID = "representationId";
  public static final String FILE_STORAGE_PATH = "storagePath";
  public static final String FILE_ISENTRYPOINT = "isEntryPoint";
  public static final String FILE_FILEFORMAT = "fileFormat";
  public static final String FILE_STORAGEPATH = "storagePath";
  public static final String FILE_ORIGINALNAME = "originalName";
  public static final String FILE_SIZE = "size";
  public static final String FILE_ISDIRECTORY = "isDirectory";
  public static final String FILE_ISREFERENCE = "isReference";
  public static final String FILE_REFERENCE_UUID = "referenceUUID";
  public static final String FILE_REFERENCE_URL = "referenceUrl";
  public static final String FILE_REFERENCE_MANIFEST = "referenceManifest";
  public static final String FILE_PRONOM = "formatPronom";
  public static final String FILE_EXTENSION = "extension";
  public static final String FILE_CREATING_APPLICATION_NAME = "creatingApplicationName";
  public static final String FILE_CREATING_APPLICATION_VERSION = "creatingApplicationVersion";
  public static final String FILE_DATE_CREATED_BY_APPLICATION = "dateCreatedByApplication";
  public static final String FILE_HASH = "hash";
  public static final String FILE_FULLTEXT = "fulltext";
  public static final String FILE_ANCESTORS = "ancestors";
  public static final String FILE_ANCESTORS_LIST = "ancestorsPath";
  public static final String FILE_FORMAT_DESIGNATION = "formatDesignation";

  public static final String FILE_CREATED_ON = "createdOn";

  /* Risks */
  public static final String RISK_ID = "id";
  public static final String RISK_NAME = "name";
  public static final String RISK_DESCRIPTION = "description";
  public static final String RISK_IDENTIFIED_ON = "identifiedOn";
  public static final String RISK_IDENTIFIED_BY = "identifiedBy";
  public static final String RISK_CATEGORIES = "categories";
  public static final String RISK_NOTES = "notes";
  public static final String RISK_PRE_MITIGATION_PROBABILITY = "preMitigationProbability";
  public static final String RISK_PRE_MITIGATION_IMPACT = "preMitigationImpact";
  public static final String RISK_PRE_MITIGATION_SEVERITY = "preMitigationSeverity";
  public static final String RISK_PRE_MITIGATION_SEVERITY_LEVEL = "preMitigationSeverityLevel";
  public static final String RISK_PRE_MITIGATION_NOTES = "preMitigationNotes";

  public static final String RISK_POST_MITIGATION_PROBABILITY = "postMitigationProbability";
  public static final String RISK_POST_MITIGATION_IMPACT = "postMitigationImpact";
  public static final String RISK_POST_MITIGATION_SEVERITY = "postMitigationSeverity";
  public static final String RISK_POST_MITIGATION_SEVERITY_LEVEL = "postMitigationSeverityLevel";
  public static final String RISK_POST_MITIGATION_NOTES = "postMitigationNotes";

  public static final String RISK_CURRENT_SEVERITY_LEVEL = "currentSeverityLevel";

  public static final String RISK_MITIGATION_STRATEGY = "mitigationStrategy";
  public static final String RISK_MITIGATION_OWNER_TYPE = "mitigationOwnerType";
  public static final String RISK_MITIGATION_OWNER = "mitigationOwner";
  public static final String RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_TYPE = "mitigationRelatedEventIdentifierType";
  public static final String RISK_MITIGATION_RELATED_EVENT_IDENTIFIER_VALUE = "mitigationRelatedEventIdentifierValue";
  public static final String RISK_INCIDENCES_COUNT = "incidencesCount";
  public static final String RISK_UNMITIGATED_INCIDENCES_COUNT = "unmitigatedIncidencesCount";

  public static final String RISK_CREATED_ON = "createdOn";
  public static final String RISK_CREATED_BY = "createdBy";
  public static final String RISK_UPDATED_ON = "updatedOn";
  public static final String RISK_UPDATED_BY = "updatedBy";

  public static final String RISK_FILE_EXTENSION = ".json";

  public static final String RISK_INCIDENCE_ID = "id";
  public static final String RISK_INCIDENCE_AIP_ID = "aipId";
  public static final String RISK_INCIDENCE_REPRESENTATION_ID = "representationId";
  public static final String RISK_INCIDENCE_FILE_PATH = "filePath";
  public static final String RISK_INCIDENCE_FILE_ID = "fileId";
  public static final String RISK_INCIDENCE_OBJECT_CLASS = "objectClass";
  public static final String RISK_INCIDENCE_RISK_ID = "riskId";
  public static final String RISK_INCIDENCE_DESCRIPTION = "description";
  public static final String RISK_INCIDENCE_BYPLUGIN = "byPlugin";
  public static final String RISK_INCIDENCE_UPDATED_ON = "updatedOn";

  public static final String RISK_INCIDENCE_STATUS = "status";
  public static final String RISK_INCIDENCE_SEVERITY = "severity";
  public static final String RISK_INCIDENCE_DETECTED_ON = "detectedOn";
  public static final String RISK_INCIDENCE_DETECTED_BY = "detectedBy";
  public static final String RISK_INCIDENCE_MITIGATED_ON = "mitigatedOn";
  public static final String RISK_INCIDENCE_MITIGATED_BY = "mitigatedBy";
  public static final String RISK_INCIDENCE_MITIGATED_DESCRIPTION = "mitigatedDescription";

  public static final String RISK_INCIDENCE_FILE_PATH_COMPUTED = "filePathComputed";
  public static final String RISK_INCIDENCE_FILE_PATH_COMPUTED_SEPARATOR = "/";
  public static final String RISK_INCIDENCE_FILE_EXTENSION = ".json";

  /* Risk Ids */
  public static final String RISK_ID_SIEGFRIED_IDENTIFICATION_WARNING = "urn:siegfried:r1";

  /* Representation information */
  public static final String REPRESENTATION_INFORMATION_ID = "id";
  public static final String REPRESENTATION_INFORMATION_NAME = "name";
  public static final String REPRESENTATION_INFORMATION_NAME_SORT = "name_sort";
  public static final String REPRESENTATION_INFORMATION_DESCRIPTION = "description";
  public static final String REPRESENTATION_INFORMATION_FAMILY = "family";
  public static final String REPRESENTATION_INFORMATION_TAGS = "tags";
  public static final String REPRESENTATION_INFORMATION_TAGS_SORT = "tags_sort";
  public static final String REPRESENTATION_INFORMATION_EXTRAS = "extras";
  public static final String REPRESENTATION_INFORMATION_SUPPORT = "support";
  public static final String REPRESENTATION_INFORMATION_RELATIONS = "relations";
  public static final String REPRESENTATION_INFORMATION_RELATIONS_WITH_RI = "relationsWithRI";
  public static final String REPRESENTATION_INFORMATION_FILTERS = "filters";
  public static final String REPRESENTATION_INFORMATION_RELATION_TYPE = "relationType";
  public static final String REPRESENTATION_INFORMATION_OBJECT_TYPE = "objectType";
  public static final String REPRESENTATION_INFORMATION_LINK = "link";
  public static final String REPRESENTATION_INFORMATION_TITLE = "title";

  public static final String REPRESENTATION_INFORMATION_CREATED_ON = "createdOn";
  public static final String REPRESENTATION_INFORMATION_CREATED_BY = "createdBy";
  public static final String REPRESENTATION_INFORMATION_UPDATED_ON = "updatedOn";
  public static final String REPRESENTATION_INFORMATION_UPDATED_BY = "updatedBy";
  public static final String REPRESENTATION_INFORMATION_FILE_EXTENSION = ".xml";

  public static final String ONE_OF_FORMAT_FIELDS = "oneOfFormatFields";

  /* Notifications */
  public static final String NOTIFICATION_ID = "id";
  public static final String NOTIFICATION_SUBJECT = "subject";
  public static final String NOTIFICATION_BODY = "body";
  public static final String NOTIFICATION_SENT_ON = "sentOn";
  public static final String NOTIFICATION_FROM_USER = "fromUser";
  public static final String NOTIFICATION_RECIPIENT_USERS = "recipientUsers";
  public static final String NOTIFICATION_ACKNOWLEDGE_TOKEN = "acknowledgeToken";
  public static final String NOTIFICATION_IS_ACKNOWLEDGED = "isAcknowledged";
  public static final String NOTIFICATION_ACKNOWLEDGED_USERS = "acknowledgedUsers";
  public static final String NOTIFICATION_STATE = "state";
  public static final String NOTIFICATION_FILE_EXTENSION = ".json";
  public static final String NOTIFICATION_VARIOUS_RECIPIENT_USERS = "various-recipient-users";

  public static final String NOTIFICATION_HTTP_ENDPOINT = "ingest.http_notification.endpoint";
  public static final String NOTIFICATION_HTTP_TIMEOUT = "ingest.http_notification.timeout";

  /* DIPs */
  public static final String DIP_ID = "id";
  public static final String DIP_TITLE = "title";
  public static final String DIP_DESCRIPTION = "description";
  public static final String DIP_TYPE = "type";
  public static final String DIP_DATE_CREATED = "dateCreated";
  public static final String DIP_LAST_MODIFIED = "lastModified";
  public static final String DIP_IS_PERMANENT = "isPermanent";
  public static final String DIP_OPEN_EXTERNAL_URL = "openExternalURL";
  public static final String DIP_DELETE_EXTERNAL_URL = "deleteExternalURL";
  public static final String DIP_PROPERTIES = "properties";
  public static final String DIP_PERMISSIONS = "permissions";

  public static final String DIP_AIP_IDS = "aipIds";
  public static final String DIP_REPRESENTATION_IDS = "representationIds";
  public static final String DIP_FILE_IDS = "fileIds";
  public static final String DIP_AIP_UUIDS = "aipUUIDs";
  public static final String DIP_REPRESENTATION_UUIDS = "representationUUIDs";
  public static final String DIP_FILE_UUIDS = "fileUUIDs";
  public static final String DIP_ALL_AIP_UUIDS = "allAipUUIDs";
  public static final String DIP_ALL_REPRESENTATION_UUIDS = "allRepresentationUUIDs";

  public static final String DIP_FILE_EXTENSION = ".json";

  public static final String DIPFILE_ID = "id";
  public static final String DIPFILE_UUID = "uuid";
  public static final String DIPFILE_DIP_ID = "dipId";
  public static final String DIPFILE_PATH = "path";
  public static final String DIPFILE_ANCESTORS_UUIDS = "ancestorsUUIDs";
  public static final String DIPFILE_PARENT_UUID = "parentUUID";
  public static final String DIPFILE_SIZE = "size";
  public static final String DIPFILE_IS_DIRECTORY = "isDirectory";
  public static final String DIPFILE_STORAGE_PATH = "storagePath";
  public static final String DIPFILE_FILE_FORMAT = "fileFormat";
  public static final String DIPFILE_FORMAT_VERSION = "formatVersion";
  public static final String DIPFILE_FORMAT_MIMETYPE = "formatMimetype";
  public static final String DIPFILE_FORMAT_PRONOM = "formatPronom";
  public static final String DIPFILE_FORMAT_EXTENSION = "formatExtension";

  public static final String DIPFILE_FILE_EXTENSION = ".json";

  /* View representation */
  public static final String VIEW_REPRESENTATION_DESCRIPTION_LEVEL = "description-level-representation";
  public static final String VIEW_REPRESENTATION_REPRESENTATION = "representation_representation";
  public static final String VIEW_REPRESENTATION_FOLDER = "representation_folder";
  public static final String VIEW_REPRESENTATION_FILE = "representation_file";
  public static final String VIEW_REPRESENTATION_FILE_REFERENCE = "representation_file_reference";

  /*
   * PREMIS
   */
  public static final String PREMIS_IDENTIFIER_TYPE_URN = "URN";
  public static final String PREMIS_IDENTIFIER_TYPE_URN_LOCAL = "URN-local";
  public static final String PREMIS_SUFFIX = ".xml";

  /*
   * Other Preservation metadata
   */
  public static final String OTHER_TECH_METADATA_FILE_SUFFIX = ".other.tech.md";

  /* Preservation events fields */
  public static final String PRESERVATION_EVENT_ID = "id";
  public static final String PRESERVATION_EVENT_AIP_ID = "aipID";
  public static final String PRESERVATION_EVENT_REPRESENTATION_UUID = "representationUUID";
  public static final String PRESERVATION_EVENT_FILE_UUID = "fileUUID";
  public static final String PRESERVATION_EVENT_OBJECT_CLASS = "objectClass";
  public static final String PRESERVATION_EVENT_DATETIME = "eventDateTime";
  public static final String PRESERVATION_EVENT_DETAIL = "eventDetail";
  public static final String PRESERVATION_EVENT_TYPE = "eventType";
  public static final String PRESERVATION_EVENT_OUTCOME = "eventOutcome";
  public static final String PRESERVATION_EVENT_OUTCOME_DETAIL_EXTENSION = "eventOutcomeDetailExtension";
  public static final String PRESERVATION_EVENT_OUTCOME_DETAIL_NOTE = "eventOutcomeDetailNote";

  /* Disposal Rule */
  public static final String DISPOSAL_RULE_ID = "id";

  /* Disposal Schedule */
  public static final String DISPOSAL_SCHEDULE_ID = "id";

  /* Disposal Hold */
  public static final String DISPOSAL_HOLD_ID = "id";

  /* Disposal Confirmation */
  public static final String DISPOSAL_CONFIRMATION_ID = "id";

  /* Siegfriend payload fields */
  public static final String SIEGFRIED_PAYLOAD_MATCHES = "matches";
  public static final String SIEGFRIED_PAYLOAD_MATCH_WARNING = "warning";
  public static final String SIEGFRIED_PAYLOAD_MATCH_NS = "ns";
  public static final String SIEGFRIED_PAYLOAD_MATCH_NS_PRONOM = "pronom";
  public static final String SIEGFRIED_PAYLOAD_MATCH_MIMETYPE = "mime";
  public static final String SIEGFRIED_PAYLOAD_MATCH_ID = "id";
  public static final String SIEGFRIED_PAYLOAD_MATCH_FORMAT_DESIGNATION = "format";
  public static final String SIEGFRIED_PAYLOAD_MATCH_FORMAT_VERSION = "version";

  /* Preservation agents fields regex */
  public static final String REGEX_PUID = "(?:fmt|x-fmt)\\/[a-z0-9]+";
  public static final String REGEX_MIME = "\\w+\\/[-+.\\w]+";

  /* Preservation agents fields */
  public static final String PRESERVATION_AGENT_ID = "id";
  public static final String PRESERVATION_AGENT_NAME = "name";
  public static final String PRESERVATION_AGENT_TYPE = "type";
  public static final String PRESERVATION_AGENT_EXTENSION = "extension";
  public static final String PRESERVATION_AGENT_VERSION = "version";
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

  public static final String PRESERVATION_AGENT_CREATED_ON = "createdOn";

  /**
   * Only file integrity is preserved
   */
  public static final String PRESERVATION_LEVEL_BITLEVEL = "bitlevel";
  public static final String PRESERVATION_REGISTRY_PRONOM = "pronom";
  public static final String PRESERVATION_REGISTRY_MIME = "mime";

  public static final String PRESERVATION_FORMAT_NOTE_MANUAL = "manual";
  public static final String PRESERVATION_FORMAT_NOTE_SIEGFRIED_WARNING = "SIEGFRIED WARNING";

  public static final String PREMIS_RELATIONSHIP_TYPE_STRUCTURAL = "structural";
  public static final String PREMIS_RELATIONSHIP_SUBTYPE_HASPART = "hasPart";

  public static final String LINKING_IDENTIFIER_VALUE = "value";
  public static final String LINKING_IDENTIFIER_TYPE = "type";
  public static final String LINKING_IDENTIFIER_ROLES = "roles";

  public static final String PRESERVATION_LINKING_OBJECT_SOURCE = "source";
  public static final String PRESERVATION_LINKING_OBJECT_OUTCOME = "outcome";

  public static final String DISPOSAL_SCHEDULE_FILE_EXTENSION = ".json";

  public enum PreservationEventType {
    CREATION("creation"), DEACCESSION("deaccession"), DECOMPRESSION("decompression"), NORMALIZATION("normalization"),
    FIXITY_CHECK("fixity check"), VALIDATION("validation"), CAPTURE("capture"), REPLICATION("replication"),
    MESSAGE_DIGEST_CALCULATION("message digest calculation"), VIRUS_CHECK("virus check"),
    DIGITAL_SIGNATURE_VALIDATION("digital signature validation"), COMPRESSION("compression"), DELETION("deletion"),
    DESTRUCTION("destruction"), MIGRATION("migration"), INGESTION("ingestion"), DECRYPTION("decryption"),
    UPDATE("update"),
    // extra types...
    WELLFORMEDNESS_CHECK("wellformedness check"), UNPACKING("unpacking"), METADATA_EXTRACTION("metadata extraction"),
    ACCESSION("accession"), AUTHORIZATION_CHECK("authorization check"), FORMAT_IDENTIFICATION("format identification"),
    FORMAT_VALIDATION("format validation"), INGEST_START("ingest start"), INGEST_END("ingest end"),
    RISK_MANAGEMENT("risk management"), DISSEMINATION("dissemination"), PACKING("packing"), APPRAISAL("appraisal"),
    DIGITAL_SIGNATURE_GENERATION("digital signature generation"), ENCRYPTION("encryption"),
    FILENAME_CHANGE("filename change"), FORENSIC_FEATURE_ANALYSIS("forensic feature analysis"), IMAGING("imaging"),
    INFORMATION_PACKAGE_CREATION("information package creation"),
    INFORMATION_PACKAGE_MERGING("information package merging"),
    INFORMATION_PACKAGE_SPLITTING("information package splitting"), METADATA_MODIFICATION("metadata modification"),
    MODIFICATION("modification"), POLICY_ASSIGNMENT("policy assignment"), QUARANTINE("quarantine"),
    RECOVERY("recovery"), REDACTION("redaction"), REFRESHMENT("refreshment"), TRANSFER("transfer"),
    UNQUARANTINE("unquarantine"), NONE("none");

    private String originalText;
    private String text;

    PreservationEventType(final String text) {
      this.text = text;
      this.originalText = text;
    }

    public String getOriginalText() {
      return originalText;
    }

    public void setText(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }
  }

  public enum ExportType {
    ZIP, FOLDER
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

  public static PreservationAgentType getPreservationAgentType(String type) {
    return PreservationAgentType.valueOf(type);
  }

  public enum PreservationAgentRole {
    AUTHORIZER("authorizer"), EXECUTING_PROGRAM("executing program"), IMPLEMENTER("implementer"),
    VALIDATOR("validator");

    private final String text;

    private PreservationAgentRole(final String text) {
      this.text = text;
    }

    public String toString() {
      return text;
    }
  }

  /*
   * Versions control
   */
  public static final String VERSION_MESSAGE = "message";
  public static final String VERSION_USER = "user";
  public static final String VERSION_ACTION = "action";

  public enum VersionAction {
    CREATED("created"), REVERTED("reverted"), UPDATED("updated"), METADATA_TYPE_FORCED("metadata_type_forced"),
    UPDATE_FROM_SIP("update_from_sip");

    private final String text;

    private VersionAction(final String text) {
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

  public static final String SEARCH_ITEMS = "items";
  public static final String SEARCH_REPRESENTATIONS = "representations";
  public static final String SEARCH_FILES = "files";
  public static final String SEARCH_JOBS = "jobs";
  public static final String SEARCH_JOB_REPORTS = "job_reports";
  public static final String SEARCH_RISKS = "risks";
  public static final String SEARCH_INCIDENCES = "incidences";
  public static final String SEARCH_PRESERVATION_EVENTS = "preservation_events";
  public static final String SEARCH_ACTION_LOGS = "action_logs";
  public static final String SEARCH_NOTIFICATIONS = "notifications";
  public static final String SEARCH_TRANSFERRED_RESOURCES = "transferred_resources";

  public static final String SEARCH_FIELD_PREFIX = "ui.search.fields";

  public static final String SEARCH_FIELD_FIELDS = "fields";
  public static final String SEARCH_FIELD_TYPE = "type";
  public static final String SEARCH_FIELD_I18N = "i18n";
  public static final String SEARCH_FIELD_I18N_PREFIX = "i18nPrefix";
  public static final String SEARCH_FIELD_VALUES = "values";
  public static final String SEARCH_FIELD_FIXED = "fixed";
  public static final String SEARCH_FIELD_INVERSE = "inverse";

  public static final String SEARCH_FIELD_TYPE_TEXT = "text";
  public static final String SEARCH_FIELD_TYPE_DATE = "date";
  public static final String SEARCH_FIELD_TYPE_DATE_INTERVAL = "date_interval";
  public static final String SEARCH_FIELD_TYPE_NUMERIC = "numeric";
  public static final String SEARCH_FIELD_TYPE_NUMERIC_INTERVAL = "numeric_interval";
  public static final String SEARCH_FIELD_TYPE_STORAGE = "storage";
  public static final String SEARCH_FIELD_TYPE_BOOLEAN = "boolean";
  public static final String SEARCH_FIELD_TYPE_SUGGEST = "suggest";
  public static final String SEARCH_FIELD_TYPE_SUGGEST_FIELD = "suggestField";
  public static final String SEARCH_FIELD_TYPE_SUGGEST_PARTIAL = "suggestPartial";
  public static final String SEARCH_FIELD_TYPE_CONTROLLED = "controlled";
  public static final String SEARCH_WITH_PREFILTER_HANDLER = "$prefilter";
  public static final String SEARCH_WITH_SAVED_HANDLER = "$savedSearch";

  public static final String METADATA_VERSION_SEPARATOR = "_";
  public static final String METADATA_TEMPLATE_FOLDER = "templates";
  public static final String METADATA_TEMPLATE_EXTENSION = ".xml.hbs";
  public static final String USERS_TEMPLATE_FOLDER = "users";
  public static final String METADATA_REPRESENTATION_INFORMATION_TEMPLATE_FOLDER = "representation-information/templates";
  public static final String DISPOSAL_CONFIRMATION_INFORMATION_TEMPLATE_FOLDER = "disposal/templates";

  public static final String USER_EXTRA_METADATA_FILE = "user_extra.xml.hbs";
  public static final String DISPOSAL_CONFIRMATION_EXTRA_METADATA_FILE = "disposal_confirmation_extra.xml.hbs";

  public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  public static final String DISPOSAL_RULE_BLACKLIST_CONDITION = "ui.disposal.rule.blacklist.condition";

  /* Template Constants */
  public static final String INGEST_EMAIL_TEMPLATE = "ingestion-template.vm";
  public static final String GENERIC_EMAIL_TEMPLATE = "generic-template.vm";
  public static final String VERIFICATION_EMAIL_TEMPLATE = "emailverification_html.vm";
  public static final String VERIFICATION_EMAIL_TEMPLATE_SUBJECT_TRANSLATION = "email.verification.subject";
  public static final String VERIFICATION_EMAIL_TEMPLATE_FROM_TRANSLATION = "email.verification.from";
  public static final String RECOVER_LOGIN_EMAIL_TEMPLATE = "recoverlogin_html.vm";
  public static final String SET_PASSWORD_TEMPLATE = "setpassword_html.vm";
  public static final String RECOVER_LOGIN_EMAIL_TEMPLATE_SUBJECT_TRANSLATION = "email.recoverlogin.subject";
  public static final String SET_PASSWORD_EMAIL_TEMPLATE_SUBJECT_TRANSLATION = "email.setpassword.subject";
  public static final String RECOVER_LOGIN_EMAIL_TEMPLATE_FROM_TRANSLATION = "email.recoverlogin.from";
  public static final String NOTIFY_PRODUCER_EMAIL_TEMPLATE = "notifyproducer_html.vm";
  public static final String EMAIL_TEMPLATE_EXTENSION = ".vm";

  /* Tika Constants */
  public static final String TIKA_FILE_SUFFIX_FULLTEXT = ".fulltext.txt";
  public static final String TIKA_FILE_SUFFIX_METADATA = ".metadata.xml";

  /* URN Constants */
  public static final String URN_BASE = "urn";
  public static final String URN_RODA = "roda";
  public static final String URN_PREMIS = "premis";
  public static final String URN_OTHER = "other";
  public static final String URN_SEPARATOR = ":";
  public static final String URN_TYPE = "URN";
  public static final String URI_TYPE = "URI";

  public enum RODA_TYPE {
    AIP, REPRESENTATION, FILE, TRANSFERRED_RESOURCE
  }

  public static final List<String> SYSTEM_USERS = Arrays.asList("admin", "guest");

  public static final String CORS_ALLOW_ORIGIN = "ui.cors.allowOrigin";
  public static final String CORS_ALLOW_HEADERS = "ui.cors.allowHeaders";
  public static final String CORS_ALLOW_METHODS = "ui.cors.allowMethods";
  public static final String CORS_ALLOW_CREDENTIALS = "ui.cors.allowCredentials";
  public static final String CORS_MAX_AGE = "ui.cors.maxAge";
  public static final String CORS_EXPOSE_HEADERS = "ui.cors.exposeHeaders";

  /* Description level configuration Constants */
  public static final String LEVELS_CLASSIFICATION_PLAN = "levels.classificationplan";
  public static final String LEVELS_ICONS_PREFIX = "levels.icon";
  public static final String LEVELS_ICONS_AIP_GHOST = "levels.internal.icon.aip.ghost";
  public static final String LEVELS_ICONS_AIP_CHILDREN = "levels.internal.icon.aip.children";
  public static final String LEVELS_ICONS_AIP_DEFAULT = "levels.internal.icon.aip.default";
  public static final String LEVELS_ICONS_REPRESENTATION = "levels.internal.icon.representation.type.default";
  public static final String LEVELS_ICONS_REPRESENTATION_FOLDER = "levels.internal.icon.representation.folder";
  public static final String LEVELS_ICONS_REPRESENTATION_FILE = "levels.internal.icon.representation.file";
  public static final String LEVELS_ICONS_REPRESENTATION_FILE_REFERENCE = "levels.internal.icon.representation.file.reference";
  public static final String LEVELS_ICONS_REPRESENTATION_TYPES_PREFIX = "levels.internal.icon.representation.type";
  public static final String LEVEL_I18N_PREFIX = "level";
  public static final String REPRESENTATION_TYPE_DEFAULT = "default";
  public static final String NONE_SELECTED_LEVEL = "noneselected";

  /* Dialog filter limit */
  public static final int DIALOG_FILTER_LIMIT_NUMBER = 120;
  public static final String INDEX_SEARCH_SUFFIX = "_txt";
  public static final String MEDIA_TYPE_WILDCARD = "*/*";
  public static final String MEDIA_TYPE_APPLICATION_XML = "application/xml";
  public static final String MEDIA_TYPE_APPLICATION_ATOM_XML = "application/atom+xml";
  public static final String MEDIA_TYPE_APPLICATION_XHTML_XML = "application/xhtml+xml";
  public static final String MEDIA_TYPE_APPLICATION_SVG_XML = "application/svg+xml";
  public static final String MEDIA_TYPE_APPLICATION_JSON = "application/json";
  public static final String MEDIA_TYPE_APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
  public static final String MEDIA_TYPE_MULTIPART_FORM_DATA = "multipart/form-data";
  public static final String MEDIA_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";
  public static final String MEDIA_TYPE_TEXT_PLAIN = "text/plain";
  public static final String MEDIA_TYPE_TEXT_XML = "text/xml";
  public static final String MEDIA_TYPE_TEXT_HTML = "text/html";
  public static final String HTTP_HEADERS_WWW_AUTHENTICATE = "WWW-Authenticate";
  public static final int STATUS_OK = 200;

  /* Url operators for searches */
  public static final String OPERATOR_AND = "and";
  public static final String OPERATOR_OR = "or";

  /* Test groups */
  public static final String TEST_GROUP_ALL = "all";
  public static final String TEST_GROUP_DEV = "dev";
  public static final String TEST_GROUP_TRAVIS = "travis";
  public static final String TEST_GROUP_PLUGIN = "plugin";

  /* List threshold and action timeout */
  public static final int ACTION_TIMEOUT = 2000;
  public static final int DEFAULT_LIST_EXPORT_LIMIT = 1000;

  /* Verification passed value */
  public static final String SIGNATURE_VERIFICATION_PASSED = "Passed";

  /* DIP types */
  public static final String DIP_TYPE_CONVERSION = "conversion";
  public static final String DIP_TYPE_DIGITAL_SIGNATURE = "digital_signature";

  /* Common performance improvements fields to return */

  public static final List<String> AIP_PERMISSIONS_FIELDS_TO_RETURN = new ArrayList<>();
  static {
    AIP_PERMISSIONS_FIELDS_TO_RETURN.add(RodaConstants.INDEX_UUID);

    for (PermissionType type : PermissionType.values()) {
      AIP_PERMISSIONS_FIELDS_TO_RETURN.add(RodaConstants.INDEX_PERMISSION_USERS_PREFIX + type);
      AIP_PERMISSIONS_FIELDS_TO_RETURN.add(RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + type);
    }
  }

  public static final List<String> REPRESENTATION_FIELDS_TO_RETURN = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.REPRESENTATION_ID, RodaConstants.REPRESENTATION_AIP_ID);

  public static final List<String> FILE_FIELDS_TO_RETURN = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.INDEX_ID, RodaConstants.FILE_PATH, RodaConstants.FILE_REPRESENTATION_ID, RodaConstants.FILE_AIP_ID,
    RodaConstants.FILE_ISDIRECTORY);

  public static final List<String> FILE_FORMAT_FIELDS_TO_RETURN = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.INDEX_ID, RodaConstants.FILE_FORMAT_MIMETYPE, RodaConstants.FILE_PRONOM);

  public static final List<String> DIP_PERMISSIONS_FIELDS_TO_RETURN = new ArrayList<>();
  static {
    DIP_PERMISSIONS_FIELDS_TO_RETURN.add(RodaConstants.INDEX_UUID);
    DIP_PERMISSIONS_FIELDS_TO_RETURN.add(RodaConstants.DIP_ID);

    for (PermissionType type : PermissionType.values()) {
      DIP_PERMISSIONS_FIELDS_TO_RETURN.add(RodaConstants.INDEX_PERMISSION_USERS_PREFIX + type);
      DIP_PERMISSIONS_FIELDS_TO_RETURN.add(RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + type);
    }
  }

  public static final List<String> DIPFILE_FIELDS_TO_RETURN = Arrays.asList(RodaConstants.INDEX_UUID,
    RodaConstants.DIPFILE_ID, RodaConstants.DIPFILE_PATH, RodaConstants.DIPFILE_DIP_ID,
    RodaConstants.DIPFILE_IS_DIRECTORY);

  public static final String PERMISSION_METHOD_FIND_AIP = "org.roda.wui.api.v2.services.IndexService.find(IndexedAIP)";
  public static final String PERMISSION_METHOD_FIND_REPRESENTATION = "org.roda.wui.api.v2.services.IndexService.find(IndexedRepresentation)";
  public static final String PERMISSION_METHOD_FIND_FILE = "org.roda.wui.api.v2.services.IndexService.find(IndexedFile)";
  public static final String PERMISSION_METHOD_FIND_DIP = "org.roda.wui.api.v2.services.IndexService.find(IndexedDIP)";
  public static final String PERMISSION_METHOD_FIND_TRANSFERRED_RESOURCE = "org.roda.wui.api.v2.services.IndexService.find(TransferredResource)";
  public static final String PERMISSION_METHOD_FIND_RODA_MEMBER = "org.roda.wui.api.v2.services.IndexService.find(RODAMember)";
  public static final String PERMISSION_METHOD_FIND_JOB_REPORT = "org.roda.wui.api.v2.services.IndexService.find(IndexedReport)";
  public static final String PERMISSION_METHOD_FIND_JOB = "org.roda.wui.api.v2.services.IndexService.find(Job)";
  public static final String PERMISSION_METHOD_FIND_NOTIFICATION = "org.roda.wui.api.v2.services.IndexService.find(Notification)";
  public static final String PERMISSION_METHOD_FIND_RISK_INCIDENCE = "org.roda.wui.api.v2.services.IndexService.find(RiskIncidence)";
  public static final String PERMISSION_METHOD_FIND_PRESERVATION_EVENT = "org.roda.wui.api.v2.services.IndexService.find(IndexedPreservationEvent)";
  public static final String PERMISSION_METHOD_FIND_PRESERVATION_AGENT = "org.roda.wui.api.v2.services.IndexService.find(IndexedPreservationAgent)";
  public static final String PERMISSION_METHOD_FIND_LOG_ENTRY = "org.roda.wui.api.v2.services.IndexService.find(LogEntry)";
  public static final String PERMISSION_METHOD_FIND_REPRESENTATION_INFORMATION = "org.roda.wui.api.v2.services.IndexService.find(RepresentationInformation)";
  public static final String PERMISSION_METHOD_FIND_RISK = "org.roda.wui.api.v2.services.IndexService.find(IndexedRisk)";

  public static final String PERMISSION_METHOD_DELETE_AIP = "org.roda.wui.api.v2.controller.AIPController.deleteAIPs";
  public static final String PERMISSION_METHOD_DELETE_DIP = "org.roda.wui.api.v2.controller.DIPController.deleteIndexedDIPs";
  public static final String PERMISSION_METHOD_DELETE_REPRESENTATION = "org.roda.wui.api.v2.controller.RepresentationController.deleteRepresentation";
  public static final String PERMISSION_METHOD_DELETE_FILE = "org.roda.wui.api.v2.controller.FilesController.deleteFiles";
  public static final String PERMISSION_METHOD_DELETE_REPRESENTATION_INFORMATION = "org.roda.wui.api.v2.controller.RepresentationInformationController.deleteMultipleRepresentationInformation";
  public static final String PERMISSION_METHOD_DELETE_RISK = "org.roda.wui.api.v2.controller.RiskController.deleteRisk";
  public static final String PERMISSION_METHOD_DELETE_RISK_INCIDENCE = "org.roda.wui.api.v2.controller.RiskIncidenceController.deleteRiskIncidences";
  public static final String PERMISSION_METHOD_DELETE_TRANSFERRED_RESOURCE = "org.roda.wui.api.v2.controller.TransferredResourceController.deleteMultipleResources";

  public static final String PERMISSION_METHOD_LIST_AIP_DESCRIPTIVE_METADATA = "org.roda.wui.api.v2.controller.AIPController.getDescriptiveMetadata";

  public static final String PERMISSION_METHOD_CREATE_AIP_BELOW = "org.roda.wui.api.v2.controller.AIPController.createAIPBelow";
  public static final String PERMISSION_METHOD_CREATE_AIP_TOP = "org.roda.wui.api.v2.controller.AIPController.createAIPTop";
  public static final String PERMISSION_METHOD_CREATE_REPRESENTATION = "org.roda.wui.api.v2.controller.RepresentationController.createRepresentation";
  public static final String PERMISSION_METHOD_MOVE_AIP_IN_HIERARCHY = "org.roda.wui.api.v2.controller.AIPController.moveAIPInHierarchy";
  public static final String PERMISSION_METHOD_UPDATE_AIP_PERMISSIONS = "org.roda.wui.api.v2.controller.AIPController.updatePermissions";
  public static final String PERMISSION_METHOD_UPDATE_DIP_PERMISSIONS = "org.roda.wui.api.v2.controller.DIPController.updatePermissions";
  public static final String PERMISSION_METHOD_CREATE_JOB = "org.roda.wui.api.v2.controller.JobsController.createJob";
  public static final String PERMISSION_METHOD_APPRAISAL = "org.roda.wui.api.v2.controller.AIPController.appraisal";
  public static final String PERMISSION_METHOD_CHANGE_AIP_TYPE = "org.roda.wui.api.v2.controller.AIPController.changeAIPType";
  public static final String PERMISSION_METHOD_CHANGE_REPRESENTATION_TYPE = "org.roda.wui.api.v2.controller.RepresentationController.changeRepresentationType";
  public static final String PERMISSION_METHOD_RENAME_FOLDER = "org.roda.wui.api.v2.controller.FilesController.renameFolder";
  public static final String PERMISSION_METHOD_MOVE_FILES = "org.roda.wui.api.v2.controller.FilesController.moveFileToFolder";
  public static final String PERMISSION_METHOD_CREATE_FILE = "org.roda.wui.api.v2.controller.FilesController.uploadFileResource";
  public static final String PERMISSION_METHOD_CHANGE_REPRESENTATION_STATES = "org.roda.wui.api.v2.controller.RepresentationController.changeRepresentationStatus";
  public static final String PERMISSION_METHOD_CREATE_FOLDER = "org.roda.wui.api.v2.controller.FilesController.createFolderUnderRepresentation";
  public static final String PERMISSION_METHOD_CREATE_REPRESENTATION_INFORMATION = "org.roda.wui.api.v2.controller.RepresentationInformationController.createRepresentationInformation";
  public static final String PERMISSION_METHOD_UPDATE_REPRESENTATION_INFORMATION = "org.roda.wui.api.v2.controller.RepresentationInformationController.updateRepresentationInformation";
  public static final String PERMISSION_METHOD_CREATE_RISK = "org.roda.wui.api.v2.controller.RiskController.createRisk";
  public static final String PERMISSION_METHOD_UPDATE_RISK = "org.roda.wui.api.v2.controller.RiskController.updateRisk";
  public static final String PERMISSION_METHOD_RETRIEVE_RISK_VERSIONS = "org.roda.wui.api.v2.controller.RiskController.retrieveRiskVersions";
  public static final String PERMISSION_METHOD_UPDATE_RISK_INCIDENCE = "org.roda.wui.api.v2.controller.RiskIncidenceController.updateRiskIncidence";
  public static final String PERMISSION_METHOD_RENAME_TRANSFERRED_RESOURCE = "org.roda.wui.api.v2.controller.TransferredResourceController.renameTransferredResource";
  public static final String PERMISSION_METHOD_MOVE_TRANSFERRED_RESOURCE = "org.roda.wui.api.v2.controller.TransferredResourceController.moveTransferredResources";
  public static final String PERMISSION_METHOD_CREATE_TRANSFERRED_RESOURCE_FILE = "org.roda.wui.api.v2.controller.TransferredResourceController.createTransferredResource";
  public static final String PERMISSION_METHOD_CREATE_TRANSFERRED_RESOURCE_FOLDER = "org.roda.wui.api.v2.controller.TransferredResourceController.createTransferredResourcesFolder";
  public static final String PERMISSION_METHOD_REVERT_DESCRIPTIVE_METADATA_VERSION = "org.roda.wui.api.v2.controller.AIPController.revertDescriptiveMetadataVersion";
  public static final String PERMISSION_METHOD_DELETE_DESCRIPTIVE_METADATA_VERSION = "org.roda.wui.api.v2.controller.AIPController.deleteDescriptiveMetadataVersion";
  public static final String PERMISSION_METHOD_CREATE_DESCRIPTIVE_METADATA_FILE = "org.roda.wui.api.v2.controller.AIPController.createDescriptiveMetadataFile";
  public static final String PERMISSION_METHOD_UPDATE_AIP_DESCRIPTIVE_METADATA_FILE = "org.roda.wui.api.v2.controller.AIPController.updateAIPDescriptiveMetadataFile";

  public static final String PERMISSION_METHOD_UPDATE_REPRESENTATION_DESCRIPTIVE_METADATA_FILE = "org.roda.wui.api.v2.controller.AIPController.updateRepresentationDescriptiveMetadataFile";
  public static final String PERMISSION_METHOD_DELETE_DESCRIPTIVE_METADATA_FILE = "org.roda.wui.api.v2.controller.AIPController.deleteDescriptiveMetadataFile";
  public static final String PERMISSION_METHOD_RETRIEVE_AIP_DESCRIPTIVE_METADATA_VERSIONS = "org.roda.wui.api.v2.controller.AIPController.retrieveDescriptiveMetadataVersions";
  public static final String PERMISSION_METHOD_RETRIEVE_REPRESENTATION_DESCRIPTIVE_METADATA_VERSIONS = "org.roda.wui.api.v2.controller.AIPController.retrieveRepresentationDescriptiveMetadataVersions";
  public static final String PERMISSION_METHOD_LIST_USERS = "org.roda.wui.api.v2.services.IndexService.find(RODAMember)";

  public static final String PERMISSION_METHOD_CREATE_USER = "org.roda.wui.api.v2.controller.MembersController.createUser";
  public static final String PERMISSION_METHOD_CREATE_GROUP = "org.roda.wui.api.v2.controller.MembersController.createGroup";
  public static final String PERMISSION_METHOD_UPDATE_USER = "org.roda.wui.api.v2.controller.MembersController.updateUser";
  public static final String PERMISSION_METHOD_DELETE_USER = "org.roda.wui.api.v2.controller.MembersController.deleteUser";

  public static final String PERMISSION_METHOD_CREATE_ACCESS_KEY = "org.roda.wui.api.v2.controller.MembersController.createAccessKey";

  public static final String PERMISSION_METHOD_CREATE_DISPOSAL_RULE = "org.roda.wui.api.v2.controller.DisposalRuleController.createDisposalRule";
  public static final String PERMISSION_METHOD_UPDATE_DISPOSAL_RULE = "org.roda.wui.api.v2.controller.DisposalRuleController.updateDisposalRule";

  public static final String PERMISSION_METHOD_CREATE_DISPOSAL_SCHEDULE = "org.roda.wui.api.v2.controller.DisposalScheduleController.createDisposalSchedule";
  public static final String PERMISSION_METHOD_UPDATE_DISPOSAL_SCHEDULE = "org.roda.wui.api.v2.controller.DisposalScheduleController.updateDisposalSchedule";
  public static final String PERMISSION_METHOD_DELETE_DISPOSAL_SCHEDULE = "org.roda.wui.api.v2.controller.DisposalScheduleController.deleteDisposalSchedule";
  public static final String PERMISSION_METHOD_ASSOCIATE_DISPOSAL_SCHEDULE = "org.roda.wui.api.v2.controller.DisposalScheduleController.associatedDisposalSchedule";

  public static final String PERMISSION_METHOD_CREATE_DISPOSAL_HOLD = "org.roda.wui.api.v2.controller.DisposalHoldController.createDisposalHold";
  public static final String PERMISSION_METHOD_UPDATE_DISPOSAL_HOLD = "org.roda.wui.api.v2.controller.DisposalHoldController.updateDisposalHold";
  public static final String PERMISSION_METHOD_ASSOCIATE_DISPOSAL_HOLD = "org.roda.wui.api.v2.controller.DisposalHoldController.applyDisposalHold";

  public static final String PERMISSION_METHOD_CREATE_DISPOSAL_CONFIRMATION = "org.roda.wui.api.v2.controller.DisposalConfirmationController.createDisposalConfirmation";
  public static final String PERMISSION_METHOD_DELETE_DISPOSAL_CONFIRMATION = "org.roda.wui.api.v2.controller.DisposalConfirmationController.deleteDisposalConfirmation";
  public static final String PERMISSION_METHOD_DESTROY_RECORDS_DISPOSAL_CONFIRMATION = "org.roda.wui.api.v2.controller.DisposalConfirmationController.destroyRecordsInDisposalConfirmation";
  public static final String PERMISSION_METHOD_RESTORE_RECORDS_DISPOSAL_CONFIRMATION = "org.roda.wui.api.v2.controller.DisposalConfirmationController.restoreDisposalConfirmation";
  public static final String PERMISSION_METHOD_PERMANENTLY_DELETE_RECORDS_DISPOSAL_CONFIRMATION = "org.roda.wui.api.v2.controller.DisposalConfirmationController.permanentlyDeleteRecordsInDisposalConfirmation";
  public static final String PERMISSION_METHOD_RETRIEVE_DISPOSAL_CONFIRMATION_REPORT = "org.roda.wui.api.v2.controller.DisposalConfirmationController.retrieveDisposalConfirmationReport";

  public static final String PERMISSION_METHOD_LIST_DISPOSAL_RULES = "org.roda.wui.api.v2.controller.DisposalRuleController.listDisposalRules";
  public static final String PERMISSION_METHOD_LIST_DISPOSAL_SCHEDULES = "org.roda.wui.api.v2.controller.DisposalScheduleController.listDisposalSchedules";
  public static final String PERMISSION_METHOD_LIST_DISPOSAL_HOLDS = "org.roda.wui.api.v2.controller.DisposalHoldController.listDisposalHolds";

  public static final String PERMISSION_METHOD_STOP_JOB = "org.roda.wui.api.v2.controller.JobsController.stopJob";

  public static final String PERMISSION_METHOD_APPROVE_JOB = "org.roda.wui.api.v2.controller.JobsController.approveJob";

  public static final String PERMISSION_METHOD_REJECT_JOB = "org.roda.wui.api.v2.controller.JobsController.rejectJob";

  public static final String ENV_CONFIG_SYMBOLIC_LINKS_FORBIDDEN = "RODA_CONFIG_SYMBOLIC_LINKS_FORBIDDEN";

  public static final String RODA_NODE_TYPE_KEY = "nodeType";

  /* Shallow File */
  public static final String RODA_MANIFEST_EXTERNAL_FILES = "external_files.jsonl";

  public static final String AIP_DEFAULT_PACKAGE_TYPE = "UNKNOWN";

  /* Distributed instances */
  public static final String DISTRIBUTED_INSTANCE_USER_PREFIX = "DISTRIBUTED_";
  public static final String DISTRIBUTED_INSTANCE_GROUP_NAME = "Distributed group";
  public static final String DISTRIBUTED_INSTANCE_ACCESS_KEY_PREFIX = DISTRIBUTED_INSTANCE_USER_PREFIX;
  public static final String DISTRIBUTED_INSTANCE_ACCESS_KEY_SUFFIX = "_KEY";
  public static final String DISTRIBUTED_INSTANCE_STATISTIC_PLACEHOLDER = "INSTANCEID";

  /* HTTP Response codes */
  public static final int HTTP_RESPONSE_CODE_SUCCESS = 200;
  public static final int HTTP_RESPONSE_CODE_CREATED = 201;
  public static final int HTTP_RESPONSE_CODE_BAD_REQUEST = 400;
  public static final int HTTP_RESPONSE_CODE_NO_CONTENT = 204;
  public static final int HTTP_RESPONSE_CODE_UNAUTHORIZED = 401;
  public static final int HTTP_RESPONSE_CODE_NOT_FOUND = 404;
  public static final int HTTP_RESPONSE_CODE_REQUEST_CONFLICT = 409;
  public static final int HTTP_RESPONSE_CODE_SERVER_ERROR = 500;

  /* Synchronization */
  public static final String SYNCHRONIZATION_CONFIG_LOCAL_INSTANCE_FOLDER = "local-instance";
  public static final String SYNCHRONIZATION_CONFIG_LOCAL_INSTANCE_FILE = "config.yaml";
  public static final String PROTOCOL_SEPARATOR = "://";

  public static final String SYNCHRONIZATION_BUNDLE_NAME = "bundle_name";
  public static final String SYNCHRONIZATION_BUNDLE_DIRECTORY = "bundle_directory";

  public static final String SYNCHRONIZATION_VALIDATION_AIP_FILE_PATH = "validation/aip_validation_list.json";
  public static final String SYNCHRONIZATION_VALIDATION_DIP_FILE_PATH = "validation/dip_validation_list.json";
  public static final String SYNCHRONIZATION_VALIDATION_RISK_INCIDENT_FILE_PATH = "validation/risk_incident_validation_list.json";
  public static final String SYNCHRONIZATION_VALIDATION_REPRESENTATION_INFORMATION_FILE_PATH = "validation/representation_information_validation_list.json";
  public static final String SYNCHRONIZATION_VALIDATION_RISK_FILE_PATH = "validation/risk_validation_list.json";

  public static final String SYNCHRONIZATION_REPORT_FILE = "last_synchronization";
  public static final String SYNCHRONIZATION_ISSUES_FILE = "issues";
  public static final String SYNCHRONIZATION_REMOVED_FILE = "removed";
  public static final String SYNCHRONIZATION_ISSUE_TYPE_MISSING = "missing";

  public static final String SYNCHRONIZATION_REPORT_KEY_UUID = "uuid";
  public static final String SYNCHRONIZATION_REPORT_KEY_INSTANCE_ID = "instanceId";
  public static final String SYNCHRONIZATION_REPORT_KEY_FROM_DATE = "fromDate";
  public static final String SYNCHRONIZATION_REPORT_KEY_STATUS = "status";
  public static final String SYNCHRONIZATION_REPORT_KEY_JOB = "job";

  public static final String SYNCHRONIZATION_REPORT_KEY_SUMMARY = "summary";
  public static final String SYNCHRONIZATION_REPORT_KEY_UPDATED_AND_ADDED = "updated/added";
  public static final String SYNCHRONIZATION_REPORT_KEY_REMOVED = "removed";
  public static final String SYNCHRONIZATION_REPORT_KEY_ISSUES = "issues";

  public static final String SYNCHRONIZATION_REPORT_KEY_DESCRIPTION = "description";
  public static final String SYNCHRONIZATION_REPORT_KEY_COUNT = "count";
  public static final String SYNCHRONIZATION_REPORT_KEY_ENTITY_CLASS = "entityClass";

  public static final String SYNCHRONIZATION_REPORT_VALUE_ADDED_DESCRIPTION = "entities added in central instance";
  public static final String SYNCHRONIZATION_REPORT_VALUE_UPDATED_AND_ADDED_DESCRIPTION = "entities updated or added in central instance";
  public static final String SYNCHRONIZATION_REPORT_VALUE_REMOVED_DESCRIPTION = "entities removed from central "
    + "instance because they were removed from local instance";
  public static final String SYNCHRONIZATION_REPORT_VALUE_ISSUES_DESCRIPTION = "An entity was found is central "
    + "instance but is not listed in local instance or is listed as being removed from local instance";
  public static final String SYNCHRONIZATION_REPORT_VALUE_STATUS_SUCCESS = "Successful Synchronization";
  public static final String SYNCHRONIZATION_REPORT_VALUE_STATUS_ERROR = "Synchronization with errors";
  public static final String SYNCHRONIZATION_ENTITY_SUMMARY_TYPE_REMOVED = "removed";
  public static final String SYNCHRONIZATION_ENTITY_SUMMARY_TYPE_UPDATED = "updated";
  public static final String SYNCHRONIZATION_ENTITY_SUMMARY_TYPE_ISSUE = "issue";

  public static final String PLUGIN_PARAMS_CONVERSION_REPRESENTATION = "rep";
  public static final String PLUGIN_PARAMS_CONVERSION_DISSEMINATION = "dip";

  /** Private empty constructor */
  private RodaConstants() {
    // do nothing
  }
}
