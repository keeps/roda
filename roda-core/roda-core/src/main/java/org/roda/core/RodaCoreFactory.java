/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import javax.xml.validation.Schema;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.ZkClientClusterStateProvider;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.CollectionAdminRequest.Create;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.cloud.ClusterState;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.zookeeper.KeeperException;
import org.roda.core.common.MarketUtils;
import org.roda.core.common.Messages;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.monitor.TransferUpdateStatus;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.config.ConfigurationManager;
import org.roda.core.config.DirectoryInitializer;
import org.roda.core.config.SpringContext;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.DistributedModeType;
import org.roda.core.data.common.RodaConstants.NodeType;
import org.roda.core.data.common.RodaConstants.OrchestratorType;
import org.roda.core.data.common.RodaConstants.PreservationAgentType;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.common.RodaConstants.SolrType;
import org.roda.core.data.common.RodaConstants.StorageType;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.MarketException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.utils.YamlUtils;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.events.EventsHandler;
import org.roda.core.events.EventsManager;
import org.roda.core.events.EventsNotifier;
import org.roda.core.index.IndexService;
import org.roda.core.index.schema.Field;
import org.roda.core.index.schema.SolrBootstrapUtils;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.index.utils.ZkController;
import org.roda.core.migration.MigrationManager;
import org.roda.core.model.DefaultModelService;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.LdapUtility;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.PluginManager;
import org.roda.core.plugins.PluginManagerException;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.PekkoEmbeddedPluginOrchestrator;
import org.roda.core.protocols.Protocol;
import org.roda.core.protocols.ProtocolManager;
import org.roda.core.protocols.ProtocolManagerException;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceWrapper;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.transaction.RODATransactionManager;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class RodaCoreFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(RodaCoreFactory.class);
  private static boolean instantiated = false;
  private static boolean instantiatedWithoutErrors = true;
  private static ConfigurationManager configurationManager;
  private static DirectoryInitializer directoryInitializer;
  private static NodeType nodeType;
  private static String instanceId = "";
  private static boolean migrationMode = false;
  private static List<Path> toDeleteDuringShutdown = new ArrayList<>();

  // Distributed instance related objects
  private static LocalInstance localInstance;
  private static String apiSecretKey;
  private static long accessKeyValidity;
  private static long accessTokenValidity;

  // Core related objects
  private static Optional<Path> tempIndexConfigsPath;
  private static Path workingDirectoryPath;
  private static Path reportDirectoryPath;
  private static Path disposalBinDirectoryPath;
  private static Path fileShallowTmpDirectoryPath;
  private static Path localInstanceConfigPath;
  private static Path jobAttachmentsDirectoryPath;

  private static Path marketDirectoryPath;

  private static StorageService storage;
  private static ModelService model;
  private static IndexService index;
  private static SolrClient solr;
  private static boolean FEATURE_OVERRIDE_INDEX_CONFIGS = true;

  // instantiation toggles, all true by default, disable them in specific cases
  private static boolean INSTANTIATE_SOLR = true;
  private static SolrType INSTANTIATE_SOLR_TYPE = null;
  private static boolean INSTANTIATE_LDAP = true;
  private static boolean INSTANTIATE_SCANNER = true;
  private static boolean INSTANTIATE_PLUGIN_ORCHESTRATOR = true;
  private static boolean INSTANTIATE_PLUGIN_MANAGER = true;
  private static boolean INSTANTIATE_PROTOCOL_MANAGER = true;
  private static boolean INSTANTIATE_DEFAULT_RESOURCES = true;
  private static boolean INSTANTIATE_CONFIGURE_LOGBACK = true;
  private static boolean INSTANTIATE_EXAMPLE_RESOURCES = true;

  // Metrics related objects
  private static MetricRegistry metricsRegistry;
  private static JmxReporter jmxMetricsReporter;

  // Orchestrator related objects
  private static PluginManager pluginManager;
  private static PluginOrchestrator pluginOrchestrator = null;

  private static ProtocolManager protocolManager;

  // Events related
  private static EventsManager eventsManager;

  private static LdapUtility ldapUtility;

  // TransferredResources related objects
  private static TransferredResourcesScanner transferredResourcesScanner;

  // Caches
  private static CacheLoader<Pair<String, String>, Optional<Schema>> RODA_SCHEMAS_LOADER = new SchemasCacheLoader();
  private static LoadingCache<Pair<String, String>, Optional<Schema>> RODA_SCHEMAS_CACHE = CacheBuilder.newBuilder()
    .build(RODA_SCHEMAS_LOADER);

  private static LoadingCache<String, DisposalSchedule> DISPOSAL_SCHEDULE_CACHE = CacheBuilder.newBuilder()
    .build(new CacheLoader<String, DisposalSchedule>() {
      @Override
      public DisposalSchedule load(String disposalScheduleId) throws Exception {
        return model.retrieveDisposalSchedule(disposalScheduleId);
      }
    });
  private static LoadingCache<String, DisposalHold> DISPOSAL_HOLD_CACHE = CacheBuilder.newBuilder()
    .expireAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<String, DisposalHold>() {
      @Override
      public DisposalHold load(String disposalHoldId) throws Exception {
        return model.retrieveDisposalHold(disposalHoldId);
      }
    });

  private static HTTPServer prometheusMetricsServer;

  private static Map<String, Function<Locale, ResourceBundle>> pluginMessageRegistry = new HashMap<>();

  private static RODATransactionManager RODATransactionManager;

  private static RodaCoreFactory instance;

  /** Private empty constructor */
  private RodaCoreFactory() {
    // do nothing
  }

  public static void addPluginMessagesProvider(String pluginId, Function<Locale, ResourceBundle> provider) {
    LOGGER.info("Added resource bundle provider for {}", pluginId);
    pluginMessageRegistry.put(pluginId, provider);
  }

  public static ResourceBundle getPluginMessages(String pluginId, Locale locale) {
    return pluginMessageRegistry.get(pluginId).apply(locale);
  }

  public static boolean instantiatedWithoutErrors() {
    return instantiatedWithoutErrors;
  }

  public static void checkIfWriteIsAllowedAndIfFalseThrowException(NodeType nodeType)
    throws AuthorizationDeniedException {
    if (!checkIfWriteIsAllowed(nodeType)) {
      throwExceptionIfWriteIsNotAllowed();
    }
  }

  public static void throwExceptionIfWriteIsNotAllowed() throws AuthorizationDeniedException {
    throw new AuthorizationDeniedException("Cannot execute non read-only method in read-only instance");
  }

  public static ReturnWithExceptions<Void, ModelObserver> checkIfWriteIsAllowedAndIfFalseReturn(NodeType nodeType) {
    ReturnWithExceptions<Void, ModelObserver> ret = new ReturnWithExceptions<>();
    try {
      checkIfWriteIsAllowedAndIfFalseThrowException(nodeType);
    } catch (AuthorizationDeniedException e) {
      ret.add(e);
    }
    return ret;
  }

  public static boolean checkIfWriteIsAllowed(NodeType nodeType) {
    return nodeType != NodeType.REPLICA;
  }

  public static void instantiate() {
    configurationManager = ConfigurationManager.getInstance();
    NodeType nodeType = configurationManager.getNodeType();
    if (nodeType == NodeType.PRIMARY) {
      instantiate(NodeType.PRIMARY);
    } else if (nodeType == NodeType.TEST) {
      instantiateTest();
    } else if (nodeType == NodeType.WORKER) {
      instantiateWorker();
    } else if (nodeType == NodeType.CONFIGS) {
      instantiateInConfigsMode();
    } else if (nodeType == NodeType.REPLICA) {
      instantiateSlaveMode();
    } else {
      LOGGER.error("Unknown node type '{}'", nodeType);
    }

    Runtime.getRuntime().addShutdownHook(new Thread(RodaCoreFactory::shutdown));
  }

  public static void instantiateTest(boolean deploySolr, boolean deployLdap, boolean deployTransferredResourcesScanner,
    boolean deployOrchestrator, boolean deployPluginManager, boolean deployDefaultResources) {
    instantiateTest(deploySolr, deployLdap, deployTransferredResourcesScanner, deployOrchestrator, deployPluginManager,
      deployDefaultResources, false);
  }

  public static void instantiateTest(boolean deploySolr, boolean deployLdap, boolean deployTransferredResourcesScanner,
    boolean deployOrchestrator, boolean deployPluginManager, boolean deployDefaultResources,
    boolean deployProtocolManager) {
    INSTANTIATE_SOLR = deploySolr;
    INSTANTIATE_LDAP = deployLdap;
    INSTANTIATE_SCANNER = deployTransferredResourcesScanner;
    INSTANTIATE_PLUGIN_ORCHESTRATOR = deployOrchestrator;
    INSTANTIATE_PLUGIN_MANAGER = deployPluginManager;
    INSTANTIATE_DEFAULT_RESOURCES = deployDefaultResources;
    INSTANTIATE_PROTOCOL_MANAGER = deployProtocolManager;
    instantiateTest();
  }

  public static void instantiateTest(boolean deploySolr, boolean deployLdap, boolean deployTransferredResourcesScanner,
    boolean deployOrchestrator, boolean deployPluginManager, boolean deployDefaultResources,
    boolean deployProtocolManager, LdapUtility ldapUtility) {
    INSTANTIATE_SOLR = deploySolr;
    INSTANTIATE_LDAP = deployLdap;
    INSTANTIATE_SCANNER = deployTransferredResourcesScanner;
    INSTANTIATE_PLUGIN_ORCHESTRATOR = deployOrchestrator;
    INSTANTIATE_PLUGIN_MANAGER = deployPluginManager;
    INSTANTIATE_DEFAULT_RESOURCES = deployDefaultResources;
    INSTANTIATE_PROTOCOL_MANAGER = deployProtocolManager;
    RodaCoreFactory.ldapUtility = ldapUtility;
    instantiateTest();
  }

  public static void instantiateTest(boolean deploySolr, boolean deployLdap, boolean deployTransferredResourcesScanner,
    boolean deployOrchestrator, boolean deployPluginManager, boolean deployDefaultResources, SolrType solrType) {
    INSTANTIATE_SOLR_TYPE = solrType;
    instantiateTest(deploySolr, deployLdap, deployTransferredResourcesScanner, deployOrchestrator, deployPluginManager,
      deployDefaultResources, false);
  }

  private static void instantiateTest() {
    INSTANTIATE_CONFIGURE_LOGBACK = false;
    INSTANTIATE_EXAMPLE_RESOURCES = false;
    instantiated = false;
    configurationManager = ConfigurationManager.getInstance();
    configurationManager.setNodeType(NodeType.TEST);
    instantiate(NodeType.TEST);
  }

  private static void instantiateWorker() {
    INSTANTIATE_SOLR = false;
    INSTANTIATE_LDAP = false;
    INSTANTIATE_SCANNER = false;
    INSTANTIATE_PLUGIN_ORCHESTRATOR = false;
    INSTANTIATE_DEFAULT_RESOURCES = false;
    INSTANTIATE_EXAMPLE_RESOURCES = false;
    instantiate(NodeType.WORKER);
  }

  private static void instantiateInConfigsMode() {
    INSTANTIATE_SOLR = false;
    INSTANTIATE_LDAP = false;
    INSTANTIATE_SCANNER = false;
    INSTANTIATE_PLUGIN_ORCHESTRATOR = false;
    INSTANTIATE_DEFAULT_RESOURCES = false;
    INSTANTIATE_CONFIGURE_LOGBACK = false;
    INSTANTIATE_EXAMPLE_RESOURCES = false;
    INSTANTIATE_PLUGIN_MANAGER = false;
    INSTANTIATE_PROTOCOL_MANAGER = false;
    instantiate(NodeType.CONFIGS);
  }

  private static void instantiateSlaveMode() {
    INSTANTIATE_SOLR = true;
    INSTANTIATE_LDAP = true;
    INSTANTIATE_PLUGIN_MANAGER = true;

    INSTANTIATE_SCANNER = false;
    INSTANTIATE_PLUGIN_ORCHESTRATOR = false;
    INSTANTIATE_DEFAULT_RESOURCES = false;
    instantiate(NodeType.REPLICA);
  }

  private static void instantiate(NodeType nodeType) {
    RodaCoreFactory.nodeType = nodeType;

    if (!instantiated) {
      try {
        // load core configurations
        if (INSTANTIATE_CONFIGURE_LOGBACK) {
          configurationManager.configureLogback();
          LOGGER.debug("Finished logback configuration");
        }

        // instantiate essential directories
        instantiateEssentialDirectories();
        LOGGER.debug("Finished instantiating essential directories");

        // initialize working directory
        initializeWorkingDirectory();
        LOGGER.debug("Finished instantiating working directory");

        // initialize reports directory
        initializeReportsDirectory();
        LOGGER.debug("Finished instantiating reports directory");

        // initialize metrics stuff
        initializeMetrics();
        LOGGER.debug("Finished instantiating metrics");

        // instantiate events manager
        instantiateEventsManager();
        LOGGER.debug("Finished instantiating events manager");

        // instantiate storage and model service
        instantiateStorageAndModel();
        LOGGER.debug("Finished instantiating storage & model");

        if (!configurationManager.isLegacyImplementationEnabled()) {
          instantiateTransactionManager(nodeType);
          LOGGER.debug("Finished instantiating transaction manager");
        }

        // initialize disposal bin directory
        initializeDisposalBinDirectory();
        LOGGER.debug("Finished instantiating disposal bin directory");

        // initialize file shallow temporary directory
        initializeFileShallowTmpDirectoryPath();
        LOGGER.debug("Finished instantiating Shallow temporary directory");

        // initialize synchronization directory
        initializeSynchronizationStateDir();
        LOGGER.debug("Finished instantiating synchronization state directory");

        // initialize synchronization directory
        initializeJobAttachmentsDir();
        LOGGER.debug("Finished instantiating Job attachments directory");

        instantiateAccessTokens();
        LOGGER.debug("Finished instantiating Access tokens");

        instantiateDistributedMode();
        LOGGER.debug("Finished instantiating distributed mode");

        // instantiate solr and index service
        instantiateSolrAndIndexService(nodeType);
        LOGGER.debug("Finished instantiating solr & index");

        instantiateNodeSpecificObjects(nodeType);
        LOGGER.debug("Finished instantiating node specific objects");

        // verify if is necessary to perform a model/index migration
        MigrationManager migrationManager = new MigrationManager(configurationManager.getDataPath());
        if (NodeType.PRIMARY == nodeType
          && migrationManager.isNecessaryToPerformMigration(getSolr(), tempIndexConfigsPath)) {
          // migrationManager.setupModelMigrations();
          // migrationManager.performModelMigrations();
          throw new GenericException("It's necessary to do a model/index migration");
        }
        LOGGER.debug("Finished migration verification");

        instantiateDefaultObjects();
        LOGGER.debug("Finished instantiating default objects");

        instantiateProtocolManager();
        LOGGER.debug("Finished instantiating protocol manager");

        // instantiate plugin manager
        // 20160920 hsilva: this must be the last thing to be instantiated as
        // problems may araise when instantiating objects at the same time the
        // plugin manager is loading both internal & external plugins (it looks
        // like Reflections is the blame)
        instantiatePluginManager();
        LOGGER.debug("Finished instantiating plugin manager");

        // now that plugin manager is up, lets do some tasks that can only be
        // done after it
        if (nodeType == NodeType.PRIMARY && pluginOrchestrator != null) {
          pluginOrchestrator.cleanUnfinishedJobsAsync();
          LOGGER.debug("Finished clean unfinished jobs operation (doing jobs clean up asynchronously)");
        }

        instanceId = getProperty(RodaConstants.CORE_NODE_INSTANCE_ID, "");

        if (!configurationManager.isLegacyImplementationEnabled() && nodeType == NodeType.PRIMARY
          && RODATransactionManager != null && RODATransactionManager.isInitialized()) {
          RODATransactionManager.cleanUnfinishedTransactions();
          LOGGER.debug("Finished clean unfinished transactions operation");
        }

        instantiated = true;

      } catch (GenericException e) {
        if (!migrationMode) {
          LOGGER.error("Error instantiating storage model", e);
        }
        instantiatedWithoutErrors = false;
      } catch (Exception e) {
        LOGGER.error("Error instantiating " + RodaCoreFactory.class.getSimpleName(), e);
        instantiatedWithoutErrors = false;
      }

      // last log message that state if system was loaded without errors or not
      LOGGER.info("RODA Core loading completed {}",
        migrationMode ? "(migration mode)"
          : (instantiatedWithoutErrors ? "with success!"
            : "with some errors!!! See logs because these errors might cause instability in the system."));
    }
  }

  private static void initializeDisposalBinDirectory() {
    try {
      String disposalBinFolder = configurationManager.getConfigurationString("disposal_bin.folder",
        RodaConstants.CORE_DISPOSAL_BIN_FOLDER);
      disposalBinDirectoryPath = getDataPath().resolve(disposalBinFolder);
      Files.createDirectories(disposalBinDirectoryPath);
    } catch (IOException e) {
      throw new RuntimeException(
        "Unable to create RODA Disposal bin DIRECTORY " + disposalBinDirectoryPath + ". Aborting...", e);
    }
  }

  private static void initializeFileShallowTmpDirectoryPath() {
    try {
      String fileShallowTmpFolder = configurationManager.getConfigurationString("file_shallow_tmp.folder",
        RodaConstants.CORE_FILE_SHALLOW_TMP_FOLDER);
      fileShallowTmpDirectoryPath = Files.createTempDirectory(getWorkingDirectory(), fileShallowTmpFolder);
      toDeleteDuringShutdown.add(fileShallowTmpDirectoryPath);
    } catch (IOException e) {
      throw new RuntimeException(
        "Unable to create RODA file shallow temporary DIRECTORY " + fileShallowTmpDirectoryPath + ". Aborting...", e);
    }
  }

  private static void initializeLocalInstanceConfigDirectory() {
    try {
      final String localInstanceFolder = configurationManager.getConfigurationString("local_instance.folder",
        RodaConstants.CORE_LOCAL_INSTANCE_FOLDER);
      localInstanceConfigPath = getConfigPath().resolve(localInstanceFolder);
      Files.createDirectories(localInstanceConfigPath);
    } catch (final IOException e) {
      throw new RuntimeException(
        "Unable to create RODA local instance config DIRECTORY " + localInstanceConfigPath + ". Aborting...", e);
    }
  }

  private static void initializeSynchronizationStateDir() {
    try {
      String synchronizationFolder = configurationManager.getConfigurationString("synchronization.folder",
        RodaConstants.CORE_SYNCHRONIZATION_FOLDER);
      Path synchronizationDirectoryPath = getDataPath().resolve(synchronizationFolder);
      configurationManager.setSynchronizationDirectoryPath(synchronizationDirectoryPath);
      Files.createDirectories(synchronizationDirectoryPath);
      Files.createDirectories(synchronizationDirectoryPath.resolve(RodaConstants.CORE_SYNCHRONIZATION_INCOMING_FOLDER));
      Files.createDirectories(synchronizationDirectoryPath.resolve(RodaConstants.CORE_SYNCHRONIZATION_OUTCOME_FOLDER));
    } catch (IOException e) {
      throw new RuntimeException("Unable to create Synchronization DIRECTORY , Aborting...", e);
    }
  }

  private static void initializeJobAttachmentsDir() {
    try {
      String jobAttachmentsFolder = configurationManager.getConfigurationString("jobAttachments.folder",
        RodaConstants.CORE_JOB_ATTACHMENTS_FOLDER);
      jobAttachmentsDirectoryPath = getDataPath().resolve(jobAttachmentsFolder);
      Files.createDirectories(jobAttachmentsDirectoryPath);
    } catch (IOException e) {
      throw new RuntimeException(
        "Unable to create job-attachments DIRECTORY " + jobAttachmentsDirectoryPath + ", Aborting...", e);
    }
  }

  private static void initializeMarketDir() {
    try {
      String marketFolder = configurationManager.getConfigurationString("market.folder",
        RodaConstants.CORE_MARKET_FOLDER);
      marketDirectoryPath = getConfigPath().resolve(marketFolder);
      Files.createDirectories(marketDirectoryPath);
    } catch (IOException e) {
      throw new RuntimeException("Unable to create market DIRECTORY " + marketDirectoryPath + ", Aborting...", e);
    }
  }

  public static void setLdapUtility(LdapUtility ldapUtility) {
    RodaCoreFactory.ldapUtility = ldapUtility;
  }

  public static void setConfigurationManager(ConfigurationManager configurationManager) {
    RodaCoreFactory.configurationManager = configurationManager;
  }

  public static String getProperty(String property, String defaultValue) {
    return configurationManager.getProperty(property, defaultValue);
  }

  /**
   * Try to get property from 1) system property (passed in command-line via -D;
   * if property does not start by "roda.", it will be prepended); 2) environment
   * variable (upper case, replace '.' by '_' and if property does not start by
   * "RODA_" after replacements, it will be prepended); 3) RODA configuration
   * files (with original property value, ensuring that it does not start by
   * "roda."); 4) return default value
   *
   * <p>
   * Example 1: for property = 'roda.node.type' this method will try to find the
   * following:
   * <ul>
   * <li>system property: roda.node.type</li>
   * <li>environment variable: RODA_NODE_TYPE</li>
   * <li>configuration files: node.type</li>
   * </p>
   * <p>
   * Example 2: for property = 'node.type' this method will try to find the
   * following:
   * <ul>
   * <li>system property: roda.node.type</li>
   * <li>environment variable: RODA_NODE_TYPE</li>
   * <li>configuration files: node.type</li>
   * </p>
   */
  public static boolean getProperty(String property, boolean defaultValue) {
    return Boolean.parseBoolean(getProperty(property, Boolean.toString(defaultValue)));
  }

  /**
   * Try to get property from 1) system property (passed in command-line via -D;
   * if property does not start by "roda.", it will be prepended); 2) environment
   * variable (upper case, replace '.' by '_' and if property does not start by
   * "RODA_" after replacements, it will be prepended); 3) RODA configuration
   * files (with original property value, ensuring that it does not start by
   * "roda."); 4) return default value
   *
   * <p>
   * Example 1: for property = 'roda.node.type' this method will try to find the
   * following:
   * <ul>
   * <li>system property: roda.node.type</li>
   * <li>environment variable: RODA_NODE_TYPE</li>
   * <li>configuration files: node.type</li>
   * </p>
   * <p>
   * Example 2: for property = 'node.type' this method will try to find the
   * following:
   * <ul>
   * <li>system property: roda.node.type</li>
   * <li>environment variable: RODA_NODE_TYPE</li>
   * <li>configuration files: node.type</li>
   * </p>
   */
  public static int getProperty(String property, int defaultValue) {
    return Integer.parseInt(getProperty(property, Integer.toString(defaultValue)));
  }

  public static void setConfigSymbolicLinksAllowed(boolean configSymbolicLinksAllowed) {
    configurationManager.setConfigSymbolicLinksAllowed(configSymbolicLinksAllowed);
  }

  private static void instantiateEssentialDirectories() {
    directoryInitializer = DirectoryInitializer.getInstance(configurationManager);
    if (INSTANTIATE_EXAMPLE_RESOURCES) {
      directoryInitializer.instantiateExampleResources();
    }
    if (!directoryInitializer.isInstantiatedWithoutErrors()) {
      instantiatedWithoutErrors = false;
    }
  }

  private static void initializeWorkingDirectory() {
    try {
      String systemTmpDir = getSystemProperty("java.io.tmpdir", "tmp");
      Path defaultRodaWorkingDirectory = Files.createTempDirectory(Paths.get(systemTmpDir), "rodaWorkingDirectory");
      workingDirectoryPath = Paths
        .get(getRodaConfiguration().getString("core.workingdirectory", defaultRodaWorkingDirectory.toString()))
        .normalize();
      Files.createDirectories(workingDirectoryPath);
    } catch (IOException e) {
      throw new RuntimeException("Unable to create RODA WORKING DIRECTORY " + workingDirectoryPath + ". Aborting...",
        e);
    }
  }

  private static void initializeReportsDirectory() {
    try {
      reportDirectoryPath = getRodaHomePath().resolve(RodaConstants.CORE_REPORT_FOLDER);
      Files.createDirectories(reportDirectoryPath);
    } catch (IOException e) {
      throw new RuntimeException("Unable to create RODA Reports DIRECTORY " + reportDirectoryPath + ". Aborting...", e);
    }
  }

  private static void initializeMetrics() {
    metricsRegistry = new MetricRegistry();
    if (getSystemProperty("com.sun.management.jmxremote", null) != null) {
      jmxMetricsReporter = JmxReporter.forRegistry(metricsRegistry).inDomain("RODA").build();
      jmxMetricsReporter.start();
    }
    int prometheusMetricsPort = getEnvInt("PROMETHEUS_METRICS_PORT", 0);
    if (prometheusMetricsPort != 0) {
      LOGGER.info("Initializing prometheus metrics at port {}", prometheusMetricsPort);

      new DropwizardExports(metricsRegistry).register();
      DefaultExports.initialize();

      try {
        prometheusMetricsServer = new HTTPServer(prometheusMetricsPort);
        // Add metrics about CPU, JVM memory etc.
      } catch (IOException e) {
        LOGGER.error("Error initializing Prometheus metrics server", e);
      }
    }
  }

  private static void instantiateDefaultObjects() {
    if (INSTANTIATE_DEFAULT_RESOURCES) {
      try (CloseableIterable<Resource> resources = storage.listResourcesUnderContainer(DefaultStoragePath.parse(""),
        true)) {

        Iterator<Resource> resourceIterator = resources.iterator();
        boolean hasFileResources = false;

        while (resourceIterator.hasNext() && !hasFileResources) {
          Resource resource = resourceIterator.next();
          if (!resource.isDirectory()
            && !resource.getStoragePath().getContainerName().equals(RodaConstants.STORAGE_CONTAINER_PRESERVATION)) {
            hasFileResources = true;
          }
        }

        if (!hasFileResources) {
          try {
            RodaUtils.copyFilesFromClasspath(RodaConstants.CORE_DEFAULT_FOLDER + "/",
              configurationManager.getRodaHomePath(), true);
          } catch (IOException e) {
            instantiatedWithoutErrors = false;
          }
          Path staticDataDefaultFolder = configurationManager.getRodaHomePath()
            .resolve(RodaConstants.CORE_DEFAULT_FOLDER).resolve(RodaConstants.CORE_DATA_FOLDER);
          Path targetPath = configurationManager.getRodaHomePath().resolve(RodaConstants.CORE_DATA_FOLDER);

          // TODO: We should avoid using FileSystem if we want to add support for other
          // storage types
          if (FSUtils.exists(staticDataDefaultFolder)) {
            try {
              Files.walkFileTree(staticDataDefaultFolder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                  try {
                    if (dir.equals(staticDataDefaultFolder)
                      || dir.equals(staticDataDefaultFolder.resolve(RodaConstants.CORE_STORAGE_FOLDER))) {
                      return FileVisitResult.CONTINUE;
                    } else {
                      Path storageDir = targetPath.resolve(staticDataDefaultFolder.relativize(dir));
                      if (Files.exists(storageDir)) {
                        FSUtils.deletePath(storageDir);
                      }

                      FSUtils.copy(dir, storageDir, true);
                      return FileVisitResult.SKIP_SUBTREE;
                    }
                  } catch (NotFoundException | GenericException | AlreadyExistsException e) {
                    LOGGER.error("Could not copy directory {}", dir.toString(), e);
                    return FileVisitResult.SKIP_SUBTREE;
                  }
                }
              });
            } catch (IOException e) {
              throw new GenericException("Cannot load static default objects", e);
            }
          }

          // 20160712 hsilva: it needs to be this way as the resources are
          // copied to the file system and storage can be of a different type
          // (e.g. fedora)
          FileStorageService fileStorageService = new FileStorageService(configurationManager.getStoragePath());

          getIndexService().reindexRisks(fileStorageService);
          getIndexService().reindexRepresentationInformation(fileStorageService);
        }
      } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException
        | IOException e) {
        LOGGER.error("Cannot load default objects", e);
      }
    }
  }

  private static void instantiatePluginManager() {
    if (INSTANTIATE_PLUGIN_MANAGER) {
      try {
        initializeMarketDir();
        // Retrieve plugin info list from market
        MarketUtils.retrievePluginsListFromAPI(getLocalInstance());
      } catch (GenericException e) {
        LOGGER.error("Unable to retrieve instance config file", e);
        instantiatedWithoutErrors = false;
      } catch (MarketException e) {
        LOGGER.warn(e.getMessage());
      }

      try {
        pluginManager = PluginManager.instantiatePluginManager(configurationManager);
      } catch (PluginManagerException e) {
        LOGGER.error("Error instantiating PluginManager", e);
        instantiatedWithoutErrors = false;
      }
    }
  }

  private static void instantiateProtocolManager() {
    if (INSTANTIATE_PROTOCOL_MANAGER) {
      try {
        protocolManager = ProtocolManager.instantiateProtocolManager(getConfigPath(), getProtocolsPath());
      } catch (ProtocolManagerException e) {
        LOGGER.error("Error instantiating ProtocolManager", e);
        instantiatedWithoutErrors = false;
      }
    }
  }

  private static void instantiateEventsManager() throws ReflectiveOperationException {
    EventsNotifier eventsNotifier = null;
    EventsHandler eventsHandler = null;

    boolean enabled = getProperty(RodaConstants.CORE_EVENTS_ENABLED, false);
    if (enabled) {
      boolean notifierAndHandlerAreTheSame = getProperty(RodaConstants.CORE_EVENTS_NOTIFIER_AND_HANDLER_ARE_THE_SAME,
        false);
      String notifierClass = getProperty(RodaConstants.CORE_EVENTS_NOTIFIER_CLASS, "");
      eventsNotifier = instantiateEventsProcessorClass(EventsNotifier.class, notifierClass);
      if (notifierAndHandlerAreTheSame) {
        eventsHandler = (EventsHandler) eventsNotifier;
      } else {
        String handlerClass = getProperty(RodaConstants.CORE_EVENTS_HANDLER_CLASS, "");
        eventsHandler = instantiateEventsProcessorClass(EventsHandler.class, handlerClass);
      }
    }

    eventsManager = new EventsManager(eventsNotifier, eventsHandler, nodeType, enabled);
  }

  private static <T extends Serializable> T instantiateEventsProcessorClass(Class<T> clazz, String eventsProcessorClass)
    throws ReflectiveOperationException {
    try {
      LOGGER.debug("Going to instantiate events related class '{}'", eventsProcessorClass);
      Class<?> eventsProcessor = Class.forName(eventsProcessorClass);
      Constructor<?> constructor = eventsProcessor.getConstructor();

      return (T) constructor.newInstance();
    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException
      | InvocationTargetException e) {
      LOGGER.warn("Error instantiating events related class '{}'", eventsProcessorClass, e);
      throw e;
    }
  }

  private static void instantiateStorageAndModel() throws GenericException {
    storage = new StorageServiceWrapper(instantiateStorage(), nodeType);
    LOGGER.debug("Finished instantiating storage...");
    model = new DefaultModelService(storage, eventsManager, nodeType, instanceId);
    LOGGER.debug("Finished instantiating model...");
  }

  private static StorageService instantiateStorage() throws GenericException {
    String newStorageService = getRodaConfiguration().getString(RodaConstants.CORE_STORAGE_NEW_SERVICE);
    if (StringUtils.isNotBlank(newStorageService)) {
      try {
        Class<?> storageClass = Class.forName(newStorageService);
        Constructor<?> constructor = storageClass.getConstructor(Path.class, String.class);

        LOGGER.debug("Going to instantiate '{}' on '{}'", storageClass.getSimpleName(),
          configurationManager.getStoragePath());
        String trashDirName = getRodaConfiguration().getString("core.storage.filesystem.trash",
          RodaConstants.TRASH_CONTAINER);

        return (StorageService) constructor.newInstance(configurationManager.getStoragePath(), trashDirName);
      } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException
        | InvocationTargetException e) {
        LOGGER.warn("Error instantiating storage service defined on properties, falling back to a default service", e);
      }
    }

    StorageType storageType = StorageType.valueOf(
      getRodaConfiguration().getString(RodaConstants.CORE_STORAGE_TYPE, RodaConstants.DEFAULT_STORAGE_TYPE.toString()));
    if (storageType == RodaConstants.StorageType.FILESYSTEM) {
      LOGGER.debug("Going to instantiate Filesystem on '{}'", configurationManager.getStoragePath());
      String trashDirName = getRodaConfiguration().getString("core.storage.filesystem.trash",
        RodaConstants.TRASH_CONTAINER);
      StorageService fileStorageService = new FileStorageService(configurationManager.getStoragePath(), trashDirName);
      return fileStorageService;
    } else {
      LOGGER.error("Unknown storage service '{}'", storageType.name());
      throw new GenericException();
    }

  }

  private static void instantiateTransactionManager(NodeType nodeType) throws GenericException {
    LOGGER.warn("RODA transactions are an experimental feature and may be unstable. Use with caution.");
    if (nodeType.equals(NodeType.TEST)) {
      // TODO: Handle test mode
      return;
    }
    if (SpringContext.isContextInitialized()) {
      RODATransactionManager = SpringContext.getBean(RODATransactionManager.class);
      RODATransactionManager.setMainModelService(model);
      RODATransactionManager.setInitialized(true);
    } else {
      throw new GenericException(
        "Unable to instantiate RODA transaction manager, because Spring context is not initialized");
    }
  }

  public static RODATransactionManager getTransactionManager() throws GenericException {
    if (nodeType.equals(NodeType.TEST)) {
      // TODO: Handle test mode
      return null;
    }
    if (RODATransactionManager == null && !configurationManager.isLegacyImplementationEnabled()) {
      instantiateTransactionManager(nodeType);
      LOGGER.debug("Finished instantiating transaction manager");
    }
    return RODATransactionManager;
  }

  /**
   * <p>
   * Warnings like
   * </p>
   * <code>2016-03-21 11:21:34,319 WARN  org.apache.solr.core.Config - Beginning with Solr 5.5, <maxMergeDocs> is deprecated, configure it on the relevant <mergePolicyFactory> instead.</code>
   * <br/>
   * <code>2016-03-21 11:21:34,327 WARN  org.apache.solr.core.Config - Beginning with Solr 5.5, <mergeFactor> is deprecated, configure it on the relevant <mergePolicyFactory> instead.</code>
   * <p>
   * are due to a bug, explained in
   * https://issues.apache.org/jira/browse/SOLR-8734, as we don't declare those
   * parameters in RODA solr configurations. The warning will be removed and as
   * soon as that happens, this messages should be deleted as well.
   * </p>
   *
   * @throws GenericException
   * @throws InterruptedException
   *
   */
  private static void instantiateSolrAndIndexService(NodeType nodeType) throws GenericException, InterruptedException {
    if (INSTANTIATE_SOLR) {
      Path solrHome = null;

      if (nodeType == NodeType.PRIMARY || nodeType == NodeType.REPLICA) {
        tempIndexConfigsPath = Optional.empty();
        solrHome = configurationManager.getConfigPath().resolve(RodaConstants.CORE_INDEX_FOLDER);
        if (!FSUtils.exists(solrHome) || FEATURE_OVERRIDE_INDEX_CONFIGS) {
          try {
            Path tempConfig = Files.createTempDirectory(getWorkingDirectory(), RodaConstants.CORE_INDEX_FOLDER);
            toDeleteDuringShutdown.add(tempConfig);
            tempIndexConfigsPath = Optional.of(tempConfig);
            solrHome = tempConfig.resolve(RodaConstants.CORE_CONFIG_FOLDER).resolve(RodaConstants.CORE_INDEX_FOLDER);
            LOGGER.info("Using SOLR home: {}", solrHome);
          } catch (IOException e) {
            LOGGER.error("Error creating temporary SOLR home", e);
            instantiatedWithoutErrors = false;
          }
        }
      } else if (nodeType == NodeType.TEST) {
        try {
          solrHome = Files.createTempDirectory(getWorkingDirectory(), RodaConstants.CORE_INDEX_FOLDER);
        } catch (IOException e) {
          LOGGER.error("Unable to instantiate Solr in TEST mode", e);
          instantiatedWithoutErrors = false;
        }
      }

      // sanity check
      if (instantiatedWithoutErrors && solrHome == null) {
        LOGGER.error("Unable to instantiate Solr because no solrHome has been defined!");
        instantiatedWithoutErrors = false;
      }

      if (instantiatedWithoutErrors) {
        boolean writeIsAllowed = checkIfWriteIsAllowed(getNodeType());

        // instantiate solr
        solr = instantiateSolr(solrHome, writeIsAllowed);

        if (writeIsAllowed) {
          SolrBootstrapUtils.bootstrapSchemas(solr);
        }

        // instantiate index related object
        index = new IndexService(solr, model, metricsRegistry, getRodaConfiguration(), nodeType);
      }
    }

  }

  private static SolrClient instantiateSolr(Path solrHome, boolean writeIsAllowed)
    throws GenericException, InterruptedException {
    SolrType solrType = SolrType.valueOf(configurationManager.getConfigurationString(RodaConstants.CORE_SOLR_TYPE,
      RodaConstants.DEFAULT_SOLR_TYPE.toString()));

    if (INSTANTIATE_SOLR_TYPE != null) {
      solrType = INSTANTIATE_SOLR_TYPE;
    }

    Field.initialize();

    String solrCloudZooKeeperUrls = configurationManager.getConfigurationString(RodaConstants.CORE_SOLR_CLOUD_URLS,
      "localhost:2181,localhost:2182,localhost:2183");
    LOGGER.info("Instantiating SOLR Cloud at {}", solrCloudZooKeeperUrls);

    try {
      ZkController.checkChrootPath(solrCloudZooKeeperUrls, true);
    } catch (KeeperException e) {
      LOGGER.error("Could not check zookeeper chroot path", e);
    }

    List<String> zkHosts;
    Optional<String> zkChroot;

    // parse config
    int indexOfSlash = solrCloudZooKeeperUrls.indexOf('/');

    if (indexOfSlash > 0) {
      // has chroot
      zkHosts = Arrays.asList(solrCloudZooKeeperUrls.substring(0, indexOfSlash).split(","));
      zkChroot = Optional.of(solrCloudZooKeeperUrls.substring(indexOfSlash));
    } else {
      // does not have chroot
      zkHosts = Arrays.asList(solrCloudZooKeeperUrls.split(","));
      zkChroot = Optional.empty();
    }

    CloudSolrClient cloudSolrClient = new CloudSolrClient.Builder(zkHosts, zkChroot).build();

    waitForSolrCluster(cloudSolrClient);

    if (writeIsAllowed) {
      bootstrap(cloudSolrClient, solrHome);
    }
    return cloudSolrClient;
  }

  private static void waitForSolrCluster(CloudSolrClient cloudSolrClient)
    throws GenericException, InterruptedException {
    int retries = getRodaConfiguration().getInt("core.solr.cloud.healthcheck.retries", 100);
    long timeout = getRodaConfiguration().getInt("core.solr.cloud.healthcheck.timeout_ms", 10000);

    boolean recovering;

    while ((recovering = !checkSolrCluster(cloudSolrClient)) && retries > 0) {
      LOGGER.info("Solr Cluster not yet ready, waiting {}ms, retries: {}", timeout, retries);
      retries--;
      try {
        Thread.sleep(timeout);
      } catch (InterruptedException e) {
        LOGGER.warn("Sleep interrupted");
        throw e;
      }
    }

    if (recovering) {
      LOGGER.error("Timeout while waiting for Solr Cluster to recover collections");
      throw new GenericException("Timeout while waiting for Solr Cluster to recover collections");
    }

  }

  private static boolean checkSolrCluster(CloudSolrClient cloudSolrClient)
    throws GenericException, InterruptedException {
    int connectTimeout = getRodaConfiguration().getInt("core.solr.cloud.connect.timeout_ms", 60000);

    try {
      LOGGER.info("Connecting to Solr Cloud with a timeout of {} ms...", connectTimeout);
      cloudSolrClient.connect(connectTimeout, TimeUnit.MILLISECONDS);
      LOGGER.info("Connected to Solr Cloud");
    } catch (TimeoutException e) {
      throw new GenericException("Could not connect to Solr Cloud", e);
    }

    ClusterState clusterState = cloudSolrClient.getClusterState();
    LOGGER.info("Live nodes: {}", clusterState.getLiveNodes());

    if (clusterState.getLiveNodes().isEmpty()) {
      return false;
    }

    Map<String, DocCollection> collectionStates = clusterState.getCollectionsMap();
    Set<String> allCollections = new HashSet<>();
    Set<String> healthyCollections = new HashSet<>();

    boolean healthy;

    for (Entry<String, DocCollection> entry : collectionStates.entrySet()) {
      String col = entry.getKey();
      DocCollection docs = entry.getValue();

      if (docs != null) {
        allCollections.add(col);

        Collection<Slice> slices = docs.getActiveSlices();

        boolean collectionHealthy = true;
        // collection healthy if all slices are healthy

        for (Slice slice : slices) {
          boolean sliceHealthy = false;

          // if at least one replica is active then the slice is healthy
          for (Replica replica : slice.getReplicas()) {
            if (Replica.State.ACTIVE.equals(replica.getState())) {
              sliceHealthy = true;
              break;
            } else {
              LOGGER.info("Replica {} on node {} is {}", replica.getName(), replica.getNodeName(), replica.getState());
            }
          }

          collectionHealthy &= sliceHealthy;
        }

        try {
          SolrPingResponse ping = cloudSolrClient.ping(col);
          if (ping.getStatus() != 0) {
            collectionHealthy = false;
            LOGGER.info("Ping collection {} return code: {}", col, ping);
          }
        } catch (SolrServerException | IOException | SolrException e) {
          LOGGER.info("Ping test failed: [{}] {}", e.getClass().getSimpleName(), e.getMessage());
          collectionHealthy = false;
        }

        if (collectionHealthy) {
          healthyCollections.add(col);
        }
      }
    }

    if (healthyCollections.containsAll(allCollections)) {
      healthy = true;
      LOGGER.info("All available Solr Cloud collections are healthy, collections: {}", healthyCollections);
    } else {
      healthy = false;

      Set<String> unhealthyCollections = new HashSet<>(allCollections);
      unhealthyCollections.removeAll(healthyCollections);

      LOGGER.info("Solr Cloud healthy collections:   " + healthyCollections);
      LOGGER.info("Solr Cloud unhealthy collections: " + unhealthyCollections);
    }

    return healthy;
  }

  private static void bootstrap(CloudSolrClient cloudSolrClient, Path solrHome) {
    CollectionAdminRequest.List req = new CollectionAdminRequest.List();
    try {
      CollectionAdminResponse response = req.process(cloudSolrClient);

      List<String> existingCollections = (List<String>) response.getResponse().get("collections");
      if (existingCollections == null) {
        existingCollections = new ArrayList<>();
      }

      Path commonConf = solrHome.resolve(SolrUtils.COMMON).resolve(SolrUtils.CONF);

      try {
        RodaUtils.copyFilesFromClasspath(RodaConstants.CORE_CONFIG_FOLDER + "/" + RodaConstants.CORE_INDEX_FOLDER + "/"
          + SolrUtils.COMMON + "/" + SolrUtils.CONF + "/", commonConf, true);
      } catch (IOException e) {
        instantiatedWithoutErrors = false;
      }

      for (String collection : SolrCollectionRegistry.registryIndexNames()) {
        if (!existingCollections.contains(collection)) {
          createCollection(cloudSolrClient, collection, commonConf);
        }
      }

    } catch (SolrServerException | IOException e) {
      LOGGER.error("Solr bootstrap failed", e);
    }
  }

  private static void createCollection(CloudSolrClient cloudSolrClient, String collection, Path configPath) {

    try {
      LOGGER.info("Creating SOLR collection {}", collection);

      int numShards = getEnvInt("SOLR_NUM_SHARDS", 1);
      int numReplicas = getEnvInt("SOLR_REPLICATION_FACTOR", 1);

      SolrZkClient zkClient = ZkClientClusterStateProvider.from(cloudSolrClient).getZkStateReader().getZkClient();
      ZkController.uploadConfig(zkClient, collection, configPath);

      Create createCollection = CollectionAdminRequest.createCollection(collection, collection, numShards, numReplicas);

      CollectionAdminResponse response = createCollection.process(cloudSolrClient);
      if (!response.isSuccess()) {
        LOGGER.error("Could not create collection {}: {}", collection, response.getErrorMessages());
      }
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Error creating collection {}", collection, e);
    }
  }

  private static Integer getEnvInt(String name, Integer defaultValue) {
    Integer envInt;
    try {
      String envString = System.getenv(name);
      envInt = envString != null ? Integer.valueOf(envString) : defaultValue;
    } catch (NumberFormatException e) {
      envInt = defaultValue;
      LOGGER.error("Invalid value for " + name + ", using default " + defaultValue, e);
    }
    return envInt;
  }

  private static Boolean getEnvBoolean(String name, Boolean defaultValue) {
    Boolean envInt;
    String envString = System.getenv(name);
    envInt = envString != null ? Boolean.valueOf(envString) : defaultValue;
    return envInt;
  }

  private static void instantiateNodeSpecificObjects(NodeType nodeType) {
    if (INSTANTIATE_LDAP) {
      LOGGER.debug("Instantiate LDAP Server");
      initializeLdapServer(nodeType);
      LOGGER.debug("Finishing instantiate LDAP Server");
    }

    if (INSTANTIATE_SCANNER) {
      instantiateTransferredResourcesScanner();
      LOGGER.debug("Finished instantiating transferred resource scanner");
    }

    if (INSTANTIATE_PLUGIN_ORCHESTRATOR) {
      instantiateOrchestrator();
      LOGGER.debug("Finished instantiating orchestrator");
    }

    if (nodeType == NodeType.PRIMARY) {
      processPreservationEventTypeProperties();
    } else if (nodeType == NodeType.TEST && !INSTANTIATE_LDAP && INSTANTIATE_SOLR) {
      try {
        getIndexService().create(RODAMember.class, new User(RodaConstants.ADMIN));
        getIndexService().commit(RODAMember.class);
      } catch (GenericException | AuthorizationDeniedException e) {
        LOGGER.warn("Could not create user admin in index for test mode", e);
      }
    }
  }

  private static void instantiateAccessTokens() {
    apiSecretKey = getProperty(RodaConstants.API_SECRET_KEY_PROPERTY, RodaConstants.DEFAULT_API_SECRET_KEY);

    if (apiSecretKey.equals(RodaConstants.DEFAULT_API_SECRET_KEY)) {
      LOGGER.warn("It is HIGHLY recommend to change the default JWT secret key. \n"
        + "In order to do that please set the following environment variable with your secret key: RODA_DISTRIBUTED_API_SECRET");
    }

    accessKeyValidity = RodaCoreFactory.getRodaConfiguration().getLong(RodaConstants.ACCESS_KEY_VALIDITY,
      RodaConstants.DEFAULT_ACCESS_KEY_VALIDITY);
    accessTokenValidity = RodaCoreFactory.getRodaConfiguration().getLong(RodaConstants.ACCESS_TOKEN_VALIDITY,
      RodaConstants.DEFAULT_ACCESS_TOKEN_VALIDITY);
  }

  private static void instantiateDistributedMode() {
    initializeLocalInstanceConfigDirectory();
    DistributedModeType distributedModeType = DistributedModeType.valueOf(
      getProperty(RodaConstants.DISTRIBUTED_MODE_TYPE_PROPERTY, RodaConstants.DEFAULT_DISTRIBUTED_MODE_TYPE.name()));
    configurationManager.setDistributedModeType(distributedModeType);
    try {
      if (DistributedModeType.CENTRAL.equals(distributedModeType)) {
        if (getLocalInstance() == null) {
          final LocalInstance rodaCentralInstance = new LocalInstance();
          rodaCentralInstance.setId(IdUtils.createUUID());
          rodaCentralInstance.setStatus(SynchronizingStatus.ACTIVE);
          rodaCentralInstance.setName(
            getProperty(RodaConstants.CENTRAL_INSTANCE_NAME_PROPERTY, RodaConstants.DEFAULT_CENTRAL_INSTANCE_NAME));
          rodaCentralInstance.setIsSubscribed(true);
          createOrUpdateLocalInstance(rodaCentralInstance);
        }
      } else if (DistributedModeType.BASE.equals(distributedModeType)) {
        if (getLocalInstance() == null) {
          final LocalInstance rodaBaseInstance = new LocalInstance();
          rodaBaseInstance.setId(IdUtils.createUUID());
          createOrUpdateLocalInstance(rodaBaseInstance);
        }
      }
    } catch (final GenericException e) {
      LOGGER.error("Can't initialize distributed mode", e);
      instantiatedWithoutErrors = false;
    }
  }

  private static void instantiateOrchestrator() {
    OrchestratorType orchestratorType = getOrchestratorType();
    if (orchestratorType == OrchestratorType.PEKKO) {
      pluginOrchestrator = new PekkoEmbeddedPluginOrchestrator();
    } else {
      LOGGER.error("Orchestrator type '{}' is invalid or not supported. No plugin orchestrator will be started!",
        orchestratorType);
    }
  }

  private static OrchestratorType getOrchestratorType() {
    OrchestratorType res = RodaConstants.DEFAULT_ORCHESTRATOR_TYPE;
    try {
      res = OrchestratorType.valueOf(RodaCoreFactory.getRodaConfiguration()
        .getString(RodaConstants.ORCHESTRATOR_TYPE_PROPERTY, RodaConstants.DEFAULT_ORCHESTRATOR_TYPE.name()));
    } catch (IllegalArgumentException e) {
      // do nothing & return default value
    }

    return res;
  }

  public static void addLogger(String loggerConfigurationFile) {
    URL loggerConfigurationFileUrl = getConfigurationFile(loggerConfigurationFile);
    if (loggerConfigurationFileUrl != null) {
      System.setProperty("roda.logback.include", loggerConfigurationFileUrl.toString());
      configurationManager.configureLogback();
    }
  }

  private static String getSystemProperty(String property, String defaultValue) {
    String ret = defaultValue;
    if (System.getProperty(property) != null) {
      ret = System.getProperty(property);
    }
    return ret;
  }

  public static void shutdown() {
    if (instantiated) {
      if (INSTANTIATE_SOLR) {
        try {
          solr.close();
        } catch (IOException e) {
          LOGGER.error("Error shutting down SOLR", e);
        }
      }
      if (INSTANTIATE_PLUGIN_MANAGER) {
        pluginManager.shutdown();
      }
      if (INSTANTIATE_PROTOCOL_MANAGER) {
        protocolManager.shutdown();
      }
      if (INSTANTIATE_PLUGIN_ORCHESTRATOR) {
        pluginOrchestrator.shutdown();
      }

      if (getProperty(RodaConstants.CORE_EVENTS_ENABLED, false)) {
        eventsManager.shutdown();
      }

      // stop jmx metrics reporter
      if (getSystemProperty("com.sun.management.jmxremote", null) != null) {
        jmxMetricsReporter.stop();
      }

      // stop prometheus metrics server
      if (prometheusMetricsServer != null) {
        prometheusMetricsServer.stop();
      }

      if (nodeType == NodeType.TEST) {
        // final cleanup
        FSUtils.deletePathQuietly(workingDirectoryPath);
        ConfigurationManager.resetInstanceAfterTest();
        DirectoryInitializer.resetInstanceAfterTest();
        configurationManager = null;
        directoryInitializer = null;
      }

      // delete resources that are no longer needed
      toDeleteDuringShutdown.forEach(e -> FSUtils.deletePathQuietly(e));
    }
  }

  public static MetricRegistry getMetrics() {
    return metricsRegistry;
  }

  /**
   * Instantiate Ldap server.
   */
  private static void initializeLdapServer(NodeType nodeType) {
    try {
      RodaCoreFactory.ldapUtility.initialize(nodeType);
      UserUtility.setLdapUtility(ldapUtility);

      if (checkIfWriteIsAllowed(getNodeType())) {
        indexUsersAndGroupsFromLDAP();
      }
    } catch (Exception e) {
      LOGGER.error("Error starting ldap server", e);
      instantiatedWithoutErrors = false;
    }
  }

  private static void indexUsersAndGroupsFromLDAP() throws GenericException {
    for (User user : getModelService().listUsers()) {
      getModelService().notifyUserUpdated(user).failOnError();
      if (INSTANTIATE_SOLR) {
        try {
          PremisV3Utils.createOrUpdatePremisUserAgentBinary(user.getName(), getModelService(), getIndexService(), true);
        } catch (ValidationException | NotFoundException | RequestNotValidException | AuthorizationDeniedException
          | AlreadyExistsException e) {
          LOGGER.error("Could not create PREMIS agent for default users");
        }
      }
    }

    for (Group group : getModelService().listGroups()) {
      getModelService().notifyGroupUpdated(group).failOnError();
    }
  }

  public static void instantiateTransferredResourcesScanner() {
    try {
      String transferredResourcesFolder = configurationManager.getConfigurationString("transferredResources.folder",
        RodaConstants.CORE_TRANSFERREDRESOURCE_FOLDER);
      Path transferredResourcesFolderPath = configurationManager.getDataPath().resolve(transferredResourcesFolder);

      if (!FSUtils.exists(transferredResourcesFolderPath)) {
        Files.createDirectories(transferredResourcesFolderPath);
      }

      transferredResourcesScanner = new TransferredResourcesScanner(transferredResourcesFolderPath, getIndexService(),
        nodeType);
    } catch (final Exception e) {
      LOGGER.error("Error starting Transferred Resources Scanner: " + e.getMessage(), e);
      instantiatedWithoutErrors = false;
    }
  }

  public static boolean getTransferredResourcesScannerUpdateStatus(Optional<String> folderRelativePath) {
    return TransferUpdateStatus.getInstance().isUpdatingStatus(folderRelativePath);
  }

  public static void setTransferredResourcesScannerUpdateStatus(Optional<String> folderRelativePath,
    boolean isUpdating) {
    TransferUpdateStatus.getInstance().setUpdatingStatus(folderRelativePath, isUpdating);
  }

  @Deprecated
  public static StorageService getStorageService() {
    return storage;
  }

  @Deprecated
  public static ModelService getModelService() {
    return model;
  }

  @Deprecated
  public static IndexService getIndexService() {
    return index;
  }

  public static SolrClient getSolr() {
    return solr;
  }

  public static void setSolr(SolrClient solr) {
    RodaCoreFactory.solr = solr;
  }

  public static PluginManager getPluginManager() {
    return PluginManager.getInstance();
  }

  public static PluginOrchestrator getPluginOrchestrator() {
    return pluginOrchestrator;
  }

  public static EventsManager getEventsManager() {
    return eventsManager;
  }

  public static TransferredResourcesScanner getTransferredResourcesScanner() {
    return transferredResourcesScanner;
  }

  public static ConfigurationManager getConfigurationManager() {
    return configurationManager;
  }

  public static NodeType getNodeType() {
    return nodeType;
  }

  public static String getInstanceId() {
    return instanceId;
  }

  @Deprecated
  public static DistributedModeType getDistributedModeType() {
    return configurationManager.getDistributedModeType();
  }

  @Deprecated
  public static Path getRodaHomePath() {
    return configurationManager.getRodaHomePath();
  }

  @Deprecated
  public static Path getConfigPath() {
    return configurationManager.getConfigPath();
  }

  @Deprecated
  public static Path getDefaultPath() {
    return configurationManager.getDefaultPath();
  }

  public static Path getWorkingDirectory() {
    return workingDirectoryPath;
  }

  @Deprecated
  public static Path getReportsDirectory() {
    return configurationManager.getReportPath();
  }

  public static Path getDisposalBinDirectoryPath() {
    return disposalBinDirectoryPath;
  }

  public static Path getFileShallowTmpDirectoryPath() {
    return fileShallowTmpDirectoryPath;
  }

  @Deprecated
  public static Path getSynchronizationDirectoryPath() {
    return configurationManager.getSynchronizationDirectoryPath();
  }

  public static Path getLocalInstanceConfigPath() {
    return localInstanceConfigPath;
  }

  public static Path getJobAttachmentsDirectoryPath() {
    return jobAttachmentsDirectoryPath;
  }

  public static Path getMarketDirectoryPath() {
    return marketDirectoryPath;
  }

  @Deprecated
  public static Path getDataPath() {
    return configurationManager.getDataPath();
  }

  @Deprecated
  public static Path getStoragePath() {
    return configurationManager.getStoragePath();
  }

  @Deprecated
  public static Path getLogPath() {
    return configurationManager.getLogPath();
  }

  @Deprecated
  public static Path getPluginsPath() {
    return configurationManager.getPluginsPath();
  }

  @Deprecated
  public static Path getProtocolsPath() {
    return configurationManager.getProtocolsPath();
  }

  public static ProtocolManager getProtocolManager() {
    return protocolManager;
  }

  public static String getApiSecretKey() {
    return apiSecretKey;
  }

  public static long getAccessTokenValidity() {
    return accessTokenValidity;
  }

  public static long getAccessKeyValidity() {
    return accessKeyValidity;
  }

  /*
   * Configuration related functionalities
   */
  @Deprecated
  public static void addConfiguration(String configurationFile) throws ConfigurationException {
    configurationManager.addConfiguration(configurationFile);
  }

  @Deprecated
  public static void addExternalConfiguration(Path configurationPath) throws ConfigurationException {
    configurationManager.addExternalConfiguration(configurationPath);
  }

  @Deprecated
  public static boolean checkPathIsWithin(Path path, Path folder) {
    return configurationManager.checkPathIsWithin(path, folder);
  }

  @Deprecated
  public static URL getConfigurationFile(String configurationFile) {
    return configurationManager.getConfigurationFile(configurationFile);
  }

  @Deprecated
  public static InputStream getScopedConfigurationFileAsStream(Path relativeConfigPath, String untrustedUserPath)
    throws GenericException {
    return configurationManager.getScopedConfigurationFileAsStream(relativeConfigPath, untrustedUserPath);
  }

  @Deprecated
  public static InputStream getConfigurationFileAsStream(String configurationFile) {
    return getConfigurationFileAsStream(getConfigPath(), configurationFile);
  }

  @Deprecated
  public static InputStream getConfigurationFileAsStream(Path baseConfigPath, String configurationFile) {
    return configurationManager.getConfigurationFileAsStream(baseConfigPath, configurationFile);
  }

  public static InputStream getConfigurationFileAsStream(String configurationFile, String fallbackConfigurationFile) {
    return configurationManager.getConfigurationFileAsStream(configurationFile, fallbackConfigurationFile);
  }

  public static InputStream getDefaultFileAsStream(String defaultFile, ClassLoader... extraClassLoaders) {
    Path defaultPath = getDefaultPath().resolve(defaultFile);
    InputStream inputStream = null;
    try {
      if (FSUtils.exists(defaultPath) && !FSUtils.isDirectory(defaultPath)
        && checkPathIsWithin(defaultPath, getDefaultPath())) {
        inputStream = Files.newInputStream(defaultPath);
        LOGGER.debug("Trying to load default from file {}", defaultPath);
      }
    } catch (IOException e) {
      // do nothing
    }

    if (inputStream == null) {
      String fileClassPath = "/" + RodaConstants.CORE_DEFAULT_FOLDER + "/" + defaultFile;
      inputStream = RodaCoreFactory.class.getResourceAsStream(fileClassPath);
      LOGGER.debug("Trying to load default file from classpath {}", fileClassPath);
    }

    if (inputStream == null) {
      String fileClassPath = RodaConstants.CORE_DEFAULT_FOLDER + "/" + defaultFile;
      for (ClassLoader classLoader : extraClassLoaders) {
        LOGGER.debug("Trying to load default file from extra class loader {}", fileClassPath);
        inputStream = classLoader.getResourceAsStream(fileClassPath);
        if (inputStream != null) {
          break;
        }
      }
    }

    return inputStream;
  }

  public static void clearRodaCachableObjectsAfterConfigurationChange() {
    configurationManager.clearRodaCachableObjectsAfterConfigurationChange();
    RODA_SCHEMAS_CACHE.invalidateAll();
    DISPOSAL_SCHEDULE_CACHE.invalidateAll();
    DISPOSAL_HOLD_CACHE.invalidateAll();
    processPreservationEventTypeProperties();

    LOGGER.info("Reloaded roda configurations after file change!");
  }

  private static void processPreservationEventTypeProperties() {
    String prefix = "core.preservation_event_type";

    for (PreservationEventType preservationEventType : PreservationEventType.values()) {
      String value = getRodaConfigurationAsString(prefix, preservationEventType.name());
      if (StringUtils.isNotBlank(value)) {
        preservationEventType.setText(value);
      } else {
        preservationEventType.setText(preservationEventType.getOriginalText());
      }
    }
  }

  public static Optional<Schema> getRodaSchema(String metadataType, String metadataVersion) {
    Optional<Schema> schema = Optional.empty();
    try {
      schema = RODA_SCHEMAS_CACHE.get(Pair.of(metadataType, metadataVersion));
    } catch (ExecutionException e) {
      if (StringUtils.isNotBlank(metadataType)) {
        try {
          schema = RODA_SCHEMAS_CACHE.get(Pair.of(metadataType, null));
        } catch (ExecutionException e2) {
          // Do nothing
        }
      }
    }
    return schema;
  }

  @Deprecated
  public static Configuration getRodaConfiguration() {
    return configurationManager.getRodaConfiguration();
  }

  @Deprecated
  public static String getRodaConfigurationAsString(String... keyParts) {
    return configurationManager.getRodaConfigurationAsString(keyParts);
  }

  @Deprecated
  public static int getRodaConfigurationAsInt(int defaultValue, String... keyParts) {
    return configurationManager.getRodaConfigurationAsInt(defaultValue, keyParts);
  }

  @Deprecated
  public static List<String> getRodaConfigurationAsList(String... keyParts) {
    return configurationManager.getRodaConfigurationAsList(keyParts);
  }

  public static Map<String, List<String>> getRodaSharedProperties(Locale locale) {
    return configurationManager.getRodaSharedProperties(locale);
  }

  public static int getRodaConfigurationAsInt(String... keyParts) {
    return getRodaConfigurationAsInt(0, keyParts);
  }

  public static List<String> getFixityAlgorithms() {
    List<String> algorithms = RodaCoreFactory.getRodaConfigurationAsList("core", "premis", "fixity", "algorithms");
    if (algorithms == null || algorithms.isEmpty()) {
      algorithms = RodaConstants.DEFAULT_ALGORITHMS;
    }
    return algorithms;
  }

  public Map<String, String> getPropertiesFromCache(String cacheName, List<String> prefixesToCache) {
    return configurationManager.getPropertiesFromCache(cacheName, prefixesToCache);
  }

  public static DisposalSchedule getDisposalSchedule(String disposalScheduleId) {
    try {
      return DISPOSAL_SCHEDULE_CACHE.get(disposalScheduleId);
    } catch (ExecutionException e) {
      LOGGER.debug("Could not get disposal schedule", e);
      return null;
    }
  }

  public static DisposalHold getDisposalHold(String disposalHoldId) {
    try {
      return DISPOSAL_HOLD_CACHE.get(disposalHoldId);
    } catch (ExecutionException e) {
      LOGGER.debug("Could not get disposal hold", e);
      return null;
    }
  }

  public static Protocol getProtocol(URI uri) throws GenericException {
    if (protocolManager != null) {
      try {
        return protocolManager.getProtocol(uri);
      } catch (ProtocolManagerException e) {
        throw new GenericException(e.getMessage(), e);
      }
    }
    throw new GenericException(
      "The Application was unable to configure the protocols correctly, please check the logs ");
  }

  public static LocalInstance getLocalInstance() throws GenericException {
    String configuration = localInstanceConfigPath.resolve(RodaConstants.SYNCHRONIZATION_CONFIG_LOCAL_INSTANCE_FILE)
      .toString();
    InputStream configurationFileAsStream = RodaCoreFactory.getConfigurationFileAsStream(configuration);
    if (configurationFileAsStream != null) {
      localInstance = YamlUtils.getObjectFromYaml(configurationFileAsStream, LocalInstance.class);
    }

    return localInstance;
  }

  public static void createOrUpdateLocalInstance(LocalInstance newLocalInstance) throws GenericException {
    Path configuration = localInstanceConfigPath.resolve(RodaConstants.SYNCHRONIZATION_CONFIG_LOCAL_INSTANCE_FILE);
    if (Files.exists(configuration)) {
      try {
        Files.delete(configuration);
      } catch (IOException e) {
        throw new GenericException("Cannot remove current local instance configuration");
      }
    }
    if (newLocalInstance != null) {
      YamlUtils.writeObjectToFile(newLocalInstance, configuration);
    }
    localInstance = newLocalInstance;
  }

  public static Messages getI18NMessages(Locale locale) {
    return configurationManager.getI18NMessages(locale);
  }

  /*
   * Command-line accessible functionalities
   */

  public static void runReindex(List<String> args) {
    String entity = args.get(2);
    if (StringUtils.isNotBlank(entity)) {
      if ("users_and_groups".equalsIgnoreCase(entity)) {
        try {
          indexUsersAndGroupsFromLDAP();
        } catch (GenericException e) {
          LOGGER.error("Unable to reindex users & groups from LDAP.", e);
        }
      }
    }
  }

  private static void runSolrQuery(List<String> args) {
    String collection = args.get(2);
    String solrQueryString = args.get(3);
    try {
      QueryResponse executeSolrQuery = SolrUtils.executeSolrQuery(solr, collection, solrQueryString);
      SolrDocumentList results = executeSolrQuery.getResults();
      System.out.println("Size: " + results.getNumFound() + "; Returned: " + results.size());
      for (SolrDocument solrDocument : results) {
        System.out.println(">" + solrDocument);
      }
    } catch (SolrServerException | IOException | SolrException e) {
      e.printStackTrace(System.err);
    }
  }

  private static void printIndexMembers(List<String> args, Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    System.out.println("Index list " + args.get(2));
    IndexResult<RODAMember> users = getIndexService().find(RODAMember.class, filter, sorter, sublist, facets,
      new ArrayList<>());
    for (RODAMember rodaMember : users.getResults()) {
      System.out.println("\t" + rodaMember);
    }
  }

  private static void printCountSips(Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_ISFILE, "true"));
    long countFiles = index.count(TransferredResource.class, filter);
    filter = new Filter(new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_ISFILE, "false"));
    long countDirectories = index.count(TransferredResource.class, filter);
    Filter f1 = new Filter();
    FilterParameter p1 = new EmptyKeyFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENT_ID);
    FilterParameter p2 = new BasicSearchFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_ISFILE, "false");
    f1.add(p1);
    f1.add(p2);
    long countSIP = index.count(TransferredResource.class, f1);

    System.out.println("Total number of directories: " + countDirectories);
    System.out.println("Total number of files: " + countFiles);
    System.out.println("Total number of SIPs: " + countSIP);
  }

  private static void printFiles(Sorter sorter, Sublist sublist) throws GenericException, RequestNotValidException {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.INDEX_SEARCH, "OLA-OLÁ-1234-XXXX_K"));
    IndexResult<IndexedFile> res = index.find(IndexedFile.class, filter, sorter, sublist, new ArrayList<>());

    for (IndexedFile sf : res.getResults()) {
      System.out.println(sf.toString());
    }
  }

  private static void printEvents(Sorter sorter, Sublist sublist) throws GenericException, RequestNotValidException {
    Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_TYPE, "format identification"));
    IndexResult<IndexedPreservationEvent> res = index.find(IndexedPreservationEvent.class, filter, sorter, sublist,
      new ArrayList<>());

    for (IndexedPreservationEvent ipe : res.getResults()) {
      System.out.println(ipe.toString());
    }
  }

  private static void printAgents(Sorter sorter, Sublist sublist) throws GenericException, RequestNotValidException {
    Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.PRESERVATION_AGENT_TYPE, PreservationAgentType.SOFTWARE.toString()));
    IndexResult<IndexedPreservationAgent> res = index.find(IndexedPreservationAgent.class, filter, sorter, sublist,
      new ArrayList<>());

    for (IndexedPreservationAgent ipa : res.getResults()) {
      System.out.println(ipa.toString());
    }
  }

  private static SecureString readPassword(final String message) throws IOException {
    final Console console = System.console();
    if (console == null) {
      final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      System.out.print(String.format("%s (INSECURE - password will be shown): ", message));
      return new SecureString(reader.readLine().toCharArray());
    } else {
      return new SecureString(new String(console.readPassword("%s: ", message)).toCharArray());
    }
  }

  private static void resetAdminAccess() throws GenericException {
    try (SecureString password = readPassword("New admin password");
      SecureString passwordConfirmation = readPassword("Repeat admin password")) {
      if (password.equals(passwordConfirmation)) {
        RodaCoreFactory.ldapUtility.resetAdminAccess(password);
        try {
          indexUsersAndGroupsFromLDAP();
        } catch (final Exception e) {
          LOGGER.warn("Error reindexing users and groups - " + e.getMessage(), e);
          System.err.println("Error reindexing users and groups (" + e.getMessage() + ").");
        }
        System.out.println("Password for 'admin' changed successfully.");
      } else {
        throw new GenericException("Passwords don't match.");
      }
    } catch (final IOException e) {
      throw new GenericException(e.getMessage(), e);
    } finally {
      try {
        RodaCoreFactory.shutdown();
      } catch (final Exception e) {
        e.printStackTrace(System.err);
      }
    }
  }

  private static void printMainUsage() {
    System.err.println("WARNING: if using Apache Solr embedded, the index related commands");
    System.err.println("cannot be run while RODA is running (i.e. deployed in Tomcat for example).");
    System.err.println("Stop RODA before running index commands.");
    System.err.println();
    System.err.println("Usage: java -jar roda-core.jar command [arguments]");
    System.err.println("Available commands:");
    System.err.println("\tindex");
    System.err
      .println("\t\treindex aip|job|risk|agent|format|notification|transferred_resources|actionlogs|users_and_groups");
    System.err.println("\t\tlist users|groups|sips|file");
    System.err.println("\torphans [newParentID]");
    System.err.println("\tfixity");
    System.err.println("\tantivirus");
    System.err.println("\tpremisskeleton");
    System.err.println("\treset admin");
    System.err.println("\tmigrate model");
    System.err.println("\tmigrate index");
  }

  private static void printResetUsage() {
    System.err.println("Reset command parameters:");
    System.err.println("\tadmin - resets admin user password and grant it all permissions.");
  }

  private static void printMigrateUsage() {
    System.err.println("Migrate command parameters:");
    System.err.println("\tmodel - performs model related migrations.");
  }

  private static void mainMasterTasks(final List<String> args) throws GenericException, RequestNotValidException {
    if ("index".equals(args.get(0))) {
      if ("list".equals(args.get(1)) && ("users".equals(args.get(2)) || "groups".equals(args.get(2)))) {
        final Filter filter = new Filter(
          new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, "users".equals(args.get(2)) ? "true" : "false"));
        printIndexMembers(args, filter, null, new Sublist(0, 10000), null);
      } else if ("list".equals(args.get(1)) && ("sips".equals(args.get(2)))) {
        printCountSips(null, new Sublist(0, 10000), null);
      } else if ("list".equals(args.get(1)) && ("file".equals(args.get(2)))) {
        printFiles(null, new Sublist(0, 10000));
      } else if ("list".equals(args.get(1)) && ("event".equals(args.get(2)))) {
        printEvents(null, new Sublist(0, 10000));
      } else if ("list".equals(args.get(1)) && ("agent".equals(args.get(2)))) {
        printAgents(null, new Sublist(0, 10000));
      } else if ("query".equals(args.get(1)) && args.size() == 4 && StringUtils.isNotBlank(args.get(2))
        && StringUtils.isNotBlank(args.get(3))) {
        runSolrQuery(args);
      } else if ("reindex".equals(args.get(1))) {
        runReindex(args);
      }
    } else if ("reset".equals(args.get(0))) {
      final List<String> resetParams = args.subList(1, args.size());
      if (resetParams.isEmpty()) {
        printResetUsage();
      } else {
        final String resetParam = resetParams.get(0);
        if ("admin".equals(resetParam)) {
          resetAdminAccess();
        } else {
          System.err.println("ERROR: Unknown parameter '" + resetParam + "'");
          printResetUsage();
        }
      }
    } else if ("migrate".equals(args.get(0))) {
      final List<String> migrateParams = args.subList(1, args.size());
      if (migrateParams.isEmpty()) {
        printMigrateUsage();
      } else {
        final String migrateParam = migrateParams.get(0);
        MigrationManager migrationManager = new MigrationManager(configurationManager.getDataPath());
        if ("model".equals(migrateParam)) {
          migrationManager.setupModelMigrations();
          migrationManager.performModelMigrations();
        } else {
          printMigrateUsage();
        }
      }
    } else {
      printMainUsage();
    }
  }

  private static void preInstantiateSteps(List<String> args) {
    if (!args.isEmpty() && "migrate".equals(args.get(0))) {
      migrationMode = true;
    }
  }

  private static void mainConfigsTasks(final List<String> args) {
    if ("generatePluginsMarkdown".equals(args.get(0))) {
      generatePluginsMarkdownTask(args);
    } else if ("generatePluginsMarketInformation".equals(args.get(0))) {
      generatePluginsMarketInformationTask(args);
    } else {
      printConfigsUsage();
    }
  }

  private static void generatePluginsMarkdownTask(final List<String> args) {
    if (args.size() == 5 && StringUtils.isNotBlank(args.get(1)) && StringUtils.isNotBlank(args.get(2))
      && StringUtils.isNotBlank(args.get(3)) && StringUtils.isNotBlank(args.get(4))
      && Files.exists(Paths.get(FilenameUtils.normalize(args.get(4))))) {

      List<Pair<String, String>> pluginsNameAndState = new ArrayList<>();

      String[] pluginsName = args.get(1).split(" ");
      String[] pluginsState = args.get(2).split(";");
      String rodaVersion = args.get(3);
      String outputDir = args.get(4);

      if (pluginsName.length == pluginsState.length) {
        for (int i = 0, pluginsNameLength = pluginsName.length; i < pluginsNameLength; i++) {
          pluginsNameAndState.add(Pair.of(pluginsName[i], pluginsState[i]));
        }

        try {
          PluginManager.writePluginInformationAsMarkdown(pluginsNameAndState, rodaVersion, outputDir);
        } catch (IOException e) {
          System.err
            .println("Error while writing plugin/plugins information in markdown format! Reason: " + e.getMessage());
        }
      } else {
        printConfigsUsage();
      }
    } else {
      printConfigsUsage();
    }
  }

  private static void generatePluginsMarketInformationTask(final List<String> args) {
    if (args.size() == 3 && StringUtils.isNotBlank(args.get(1)) && StringUtils.isNotBlank(args.get(2))
      && Files.exists(Paths.get(FilenameUtils.normalize(args.get(2))))) {
      String pluginFolder = args.get(1);
      try {
        String pluginsJson = PluginManager.getPluginsMarketInformationAsJsonLines(pluginFolder);
        if (pluginsJson != null) {
          Files.write(Paths.get(FilenameUtils.normalize(args.get(2)), "pluginInfo.jsonl"), pluginsJson.getBytes());
        }
      } catch (IOException e) {
        System.err
          .println("Error while writing plugin/plugins information in jsonlines format! Reason: " + e.getMessage());
      }
    } else {
      printConfigsUsage();
    }
  }

  private static void printConfigsUsage() {
    System.err.println("Configs command parameters:");
    System.err.println(
      "\tgeneratePluginsMarkdown PLUGIN_OR_PLUGINS DEVELOPMENT_STATUS_PER_PLUGIN OUTPUT_FOLDER - generates plugin representation in markdown format. Development status if many please separate with ;\n"
        + "\tgeneratePluginsMarketInformation PLUGIN_DIR OUTPUT_FOLDER - generates plugin market information in jsonlines format.");
  }

  public static void main(final String[] argsArray)
    throws InterruptedException, GenericException, RequestNotValidException {
    final List<String> args = Arrays.asList(argsArray);
    NodeType nodeType = NodeType
      .valueOf(getProperty(RodaConstants.CORE_NODE_TYPE, RodaConstants.DEFAULT_NODE_TYPE.name()));

    preInstantiateSteps(args);
    instantiate();
    if (nodeType == NodeType.PRIMARY) {
      if (!args.isEmpty()) {
        mainMasterTasks(args);
      } else {
        printMainUsage();
      }
    } else if (nodeType == NodeType.WORKER) {
      Thread.currentThread().join();
    } else if (nodeType == NodeType.CONFIGS) {
      if (!args.isEmpty()) {
        mainConfigsTasks(args);
      } else {
        printConfigsUsage();
      }
    } else {
      printMainUsage();
    }

    System.exit(0);
  }
}
