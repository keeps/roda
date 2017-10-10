/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.validation.Schema;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.roda.core.common.LdapUtility;
import org.roda.core.common.Messages;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.UserUtility;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.monitor.TransferUpdateStatus;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.NodeType;
import org.roda.core.data.common.RodaConstants.PreservationAgentType;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.common.RodaConstants.SolrType;
import org.roda.core.data.common.RodaConstants.StorageType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.RoleAlreadyExistsException;
import org.roda.core.data.v2.common.Pair;
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
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.migration.MigrationManager;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.PluginManager;
import org.roda.core.plugins.PluginManagerException;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.AkkaDistributedPluginOrchestrator;
import org.roda.core.plugins.orchestrate.AkkaEmbeddedPluginOrchestrator;
import org.roda.core.plugins.orchestrate.akka.distributed.AkkaDistributedPluginWorker;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fedora.FedoraStorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class RodaCoreFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(RodaCoreFactory.class);

  private static boolean instantiated = false;
  private static boolean instantiatedWithoutErrors = true;
  private static NodeType nodeType;
  private static boolean migrationMode = false;
  private static List<Path> toDeleteDuringShutdown = new ArrayList<>();

  // Core related objects
  private static Path rodaHomePath;
  private static Path storagePath;
  private static Path indexDataPath;
  private static Optional<Path> tempIndexConfigsPath;
  private static Path dataPath;
  private static Path logPath;
  private static Path configPath;
  private static Path workingDirectoryPath;
  private static Path reportDirectoryPath;
  private static Path exampleConfigPath;
  private static Path defaultPath;

  private static StorageService storage;
  private static ModelService model;
  private static IndexService index;
  private static SolrClient solr;
  private static boolean FEATURE_OVERRIDE_INDEX_CONFIGS = true;

  private static boolean TEST_DEPLOY_SOLR = true;
  private static boolean TEST_DEPLOY_LDAP = true;
  private static boolean TEST_DEPLOY_SCANNER = true;
  private static boolean TEST_DEPLOY_ORCHESTRATOR = true;
  private static boolean TEST_DEPLOY_PLUGIN_MANAGER = true;
  private static boolean TEST_DEPLOY_DEFAULT_RESOURCES = true;
  private static SolrType TEST_SOLR_TYPE = null;

  // Metrics related objects
  private static MetricRegistry metricsRegistry;
  private static JmxReporter jmxMetricsReporter;

  // Orchestrator related objects
  private static PluginManager pluginManager;
  // FIXME we should have only "one" orchestrator
  private static PluginOrchestrator pluginOrchestrator;
  private static AkkaDistributedPluginOrchestrator akkaDistributedPluginOrchestrator;
  private static AkkaDistributedPluginWorker akkaDistributedPluginWorker;
  private static boolean FEATURE_DISTRIBUTED_AKKA = false;

  private static LdapUtility ldapUtility;
  private static Path rodaApacheDSDataDirectory = null;

  // TransferredResources related objects
  private static TransferredResourcesScanner transferredResourcesScanner;

  // Configuration related objects
  private static CompositeConfiguration rodaConfiguration = null;
  private static List<String> configurationFiles = null;

  // Caches
  private static CacheLoader<Pair<String, String>, Optional<Schema>> RODA_SCHEMAS_LOADER = new SchemasCacheLoader();
  private static LoadingCache<Pair<String, String>, Optional<Schema>> RODA_SCHEMAS_CACHE = CacheBuilder.newBuilder()
    .build(RODA_SCHEMAS_LOADER);

  private static LoadingCache<Locale, Messages> I18N_CACHE = CacheBuilder.newBuilder()
    .build(new CacheLoader<Locale, Messages>() {

      @Override
      public Messages load(Locale locale) throws Exception {
        return new Messages(locale, getConfigPath().resolve(RodaConstants.CORE_I18N_FOLDER));
      }
    });

  private static Map<String, Map<String, String>> rodaPropertiesCache = null;

  /** Private empty constructor */
  private RodaCoreFactory() {

  }

  public static boolean instantiatedWithoutErrors() {
    return instantiatedWithoutErrors;
  }

  public static void instantiate() {
    NodeType nodeType = NodeType
      .valueOf(getSystemProperty(RodaConstants.CORE_NODE_TYPE, RodaConstants.DEFAULT_NODE_TYPE.name()));

    if (nodeType == RodaConstants.NodeType.MASTER) {
      instantiateMaster();
    } else if (nodeType == RodaConstants.NodeType.TEST) {
      instantiateTest();
    } else if (nodeType == RodaConstants.NodeType.WORKER) {
      instantiateWorker();
    } else {
      LOGGER.error("Unknown node type '{}'", nodeType);
    }
  }

  public static void instantiateMaster() {
    instantiate(NodeType.MASTER);
  }

  public static void instantiateWorker() {
    instantiate(NodeType.WORKER);
  }

  public static void instantiateTest(boolean deploySolr, boolean deployLdap, boolean deployTransferredResourcesScanner,
    boolean deployOrchestrator, boolean deployPluginManager, boolean deployDefaultResources) {
    TEST_DEPLOY_SOLR = deploySolr;
    TEST_DEPLOY_LDAP = deployLdap;
    TEST_DEPLOY_SCANNER = deployTransferredResourcesScanner;
    TEST_DEPLOY_ORCHESTRATOR = deployOrchestrator;
    TEST_DEPLOY_PLUGIN_MANAGER = deployPluginManager;
    TEST_DEPLOY_DEFAULT_RESOURCES = deployDefaultResources;
    instantiated = false;
    instantiate(NodeType.TEST);
  }

  public static void instantiateTest(boolean deploySolr, boolean deployLdap, boolean deployTransferredResourcesScanner,
    boolean deployOrchestrator, boolean deployPluginManager, boolean deployDefaultResources, SolrType solrType) {
    TEST_SOLR_TYPE = solrType;
    instantiateTest(deploySolr, deployLdap, deployTransferredResourcesScanner, deployOrchestrator, deployPluginManager,
      deployDefaultResources);
  }

  public static void instantiateTest() {
    instantiated = false;
    instantiate(NodeType.TEST);
  }

  private static void instantiate(NodeType nodeType) {
    RodaCoreFactory.nodeType = nodeType;

    if (!instantiated) {
      try {
        // determine RODA HOME
        rodaHomePath = determineRodaHomePath();
        LOGGER.debug("RODA HOME is {}", rodaHomePath);

        // instantiate essential directories
        instantiateEssentialDirectories(nodeType);
        LOGGER.debug("Finished instantiating essential directories");

        // load core configurations
        rodaConfiguration = new CompositeConfiguration();
        configurationFiles = new ArrayList<>();
        rodaPropertiesCache = new HashMap<>();
        addConfiguration("roda-core.properties");
        addConfiguration("roda-core-formats.properties");
        LOGGER.debug("Finished loading roda-core.properties & roda-core-formats.properties");
        addConfiguration("roda-roles.properties");
        LOGGER.debug("Finished loading roda-roles.properties");

        // initialize working directory
        initializeWorkingDirectory();

        // initialize reports directory
        initializeReportsDirectory();

        // initialize metrics stuff
        initializeMetrics();

        // instantiate storage and model service
        instantiateStorageAndModel();
        LOGGER.debug("Finished instantiating storage & model");

        // instantiate solr and index service
        instantiateSolrAndIndexService();
        LOGGER.debug("Finished instantiating solr & index");

        instantiateNodeSpecificObjects(nodeType);
        LOGGER.debug("Finished instantiating node specific objects");

        // verify if is necessary to perform a model/index migration
        MigrationManager migrationManager = new MigrationManager(dataPath);
        if (NodeType.MASTER == nodeType
          && migrationManager.isNecessaryToPerformMigration(getSolr(), tempIndexConfigsPath)) {
          // migrationManager.setupModelMigrations();
          // migrationManager.performModelMigrations();
          throw new GenericException("It's necessary to do a model/index migration");
        }

        instantiateDefaultObjects();
        LOGGER.debug("Finished instantiating default objects");

        // instantiate plugin manager
        // 20160920 hsilva: this must be the last thing to be instantiated as
        // problems may araise when instantiating objects at the same time the
        // plugin manager is loading both internal & external plugins (it looks
        // like Reflections is the blame)
        instantiatePluginManager();
        LOGGER.debug("Finished instantiating plugin manager");

        instantiated = true;

      } catch (ConfigurationException e) {
        LOGGER.error("Error loading roda properties", e);
        instantiatedWithoutErrors = false;
      } catch (URISyntaxException e) {
        LOGGER.error("Error instantiating solr/index model", e);
        instantiatedWithoutErrors = false;
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

  private static void initializeWorkingDirectory() {
    try {
      workingDirectoryPath = Paths
        .get(getRodaConfiguration().getString("core.workingdirectory", getSystemProperty("java.io.tmpdir", "tmp")));
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
  }

  private static Path determineRodaHomePath() {
    Path rodaHomePath;
    if (System.getProperty(RodaConstants.INSTALL_FOLDER_SYSTEM_PROPERTY) != null) {
      rodaHomePath = Paths.get(System.getProperty(RodaConstants.INSTALL_FOLDER_SYSTEM_PROPERTY));
    } else if (System.getenv(RodaConstants.INSTALL_FOLDER_ENVIRONEMNT_VARIABLE) != null) {
      rodaHomePath = Paths.get(System.getenv(RodaConstants.INSTALL_FOLDER_ENVIRONEMNT_VARIABLE));
    } else {
      // last attempt (using user home and hidden directory called .roda)
      String userHome = System.getProperty("user.home");
      rodaHomePath = Paths.get(userHome, ".roda");
      if (!FSUtils.exists(rodaHomePath)) {
        try {
          Files.createDirectories(rodaHomePath);
        } catch (IOException e) {
          throw new RuntimeException("Unable to create RODA HOME " + rodaHomePath + ". Aborting...", e);
        }
      }
    }
    // set roda.home in order to correctly configure logging even if no
    // property has been defined
    System.setProperty(RodaConstants.INSTALL_FOLDER_SYSTEM_PROPERTY, rodaHomePath.toString());

    // instantiate essential directories
    configPath = getEssentialDirectoryPath(rodaHomePath, RodaConstants.CORE_CONFIG_FOLDER);
    exampleConfigPath = getEssentialDirectoryPath(rodaHomePath, RodaConstants.CORE_EXAMPLE_CONFIG_FOLDER);
    defaultPath = getEssentialDirectoryPath(rodaHomePath, RodaConstants.CORE_DEFAULT_FOLDER);
    dataPath = getEssentialDirectoryPath(rodaHomePath, RodaConstants.CORE_DATA_FOLDER);
    logPath = getEssentialDirectoryPath(dataPath, RodaConstants.CORE_LOG_FOLDER);
    storagePath = getEssentialDirectoryPath(dataPath, RodaConstants.CORE_STORAGE_FOLDER);
    indexDataPath = getEssentialDirectoryPath(dataPath, RodaConstants.CORE_INDEX_FOLDER);

    // configure logback
    if (nodeType != NodeType.TEST) {
      configureLogback();
    }

    return rodaHomePath;
  }

  private static Path getEssentialDirectoryPath(Path basePath, String directoryName) {
    String configuredPath = System.getenv(RodaConstants.CORE_ESSENTIAL_DIRECTORY_PREFIX + directoryName.toUpperCase());

    Path ret;

    if (StringUtils.isNotBlank(configuredPath)) {
      ret = Paths.get(configuredPath);
    } else {
      ret = basePath.resolve(directoryName);
    }

    return ret;
  }

  private static void configureLogback() {
    try {
      LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(context);
      context.reset();
      configurator.doConfigure(getConfigurationFile("logback.xml"));
    } catch (JoranException e) {
      LOGGER.error("Error configuring logback", e);
    }
  }

  private static void instantiateEssentialDirectories(NodeType nodeType) {
    List<Path> essentialDirectories = new ArrayList<>();
    essentialDirectories.add(configPath);
    essentialDirectories.add(rodaHomePath.resolve(RodaConstants.CORE_LOG_FOLDER));
    essentialDirectories.add(dataPath);
    essentialDirectories.add(logPath);
    essentialDirectories.add(storagePath);
    essentialDirectories.add(indexDataPath);
    essentialDirectories.add(exampleConfigPath);

    for (Path path : essentialDirectories) {
      try {
        if (!FSUtils.exists(path)) {
          Files.createDirectories(path);
        }
      } catch (IOException e) {
        LOGGER.error("Unable to create " + path, e);
        instantiatedWithoutErrors = false;
      }
    }

    if (!nodeType.equals(NodeType.TEST)) {
      // copy configs folder from classpath to example folder
      try {
        FSUtils.deletePathQuietly(exampleConfigPath);
        Files.createDirectories(exampleConfigPath);
        copyFilesFromClasspath(RodaConstants.CORE_CONFIG_FOLDER + "/", exampleConfigPath, true,
          Arrays.asList(RodaConstants.CORE_CONFIG_FOLDER + "/" + RodaConstants.CORE_LDAP_FOLDER,
            RodaConstants.CORE_CONFIG_FOLDER + "/" + RodaConstants.CORE_INDEX_FOLDER,
            RodaConstants.CORE_CONFIG_FOLDER + "/" + RodaConstants.CORE_I18N_FOLDER + "/"
              + RodaConstants.CORE_I18N_CLIENT_FOLDER,
            RodaConstants.CORE_CONFIG_FOLDER + "/" + RodaConstants.CORE_I18N_FOLDER + "/"
              + RodaConstants.CORE_I18_GWT_XML_FILE));
      } catch (IOException e) {
        LOGGER.error("Unable to create " + exampleConfigPath, e);
        instantiatedWithoutErrors = false;
      }
    }

  }

  private static void instantiateDefaultObjects() {
    if (TEST_DEPLOY_DEFAULT_RESOURCES) {
      try {
        CloseableIterable<Resource> resources = storage.listResourcesUnderContainer(DefaultStoragePath.parse(""), true);
        Iterator<Resource> resourceIterator = resources.iterator();
        boolean hasFileResources = false;

        while (resourceIterator.hasNext() && !hasFileResources) {
          Resource resource = resourceIterator.next();
          if (!resource.isDirectory()) {
            hasFileResources = true;
          }
        }
        IOUtils.closeQuietly(resources);

        if (!hasFileResources) {
          copyFilesFromClasspath(RodaConstants.CORE_DEFAULT_FOLDER + "/", rodaHomePath, true);

          // 20160712 hsilva: it needs to be this way as the resources are
          // copied to the file system and storage can be of a different type
          // (e.g. fedora)
          FileStorageService fileStorageService = new FileStorageService(storagePath);

          index.reindexRisks(fileStorageService);
          index.reindexFormats(fileStorageService);
          index.reindexRepresentationInformation(fileStorageService);
          index.reindexAIPs();
          // reindex other default objects HERE
        }
      } catch (AuthorizationDeniedException | RequestNotValidException | NotFoundException | GenericException e) {
        LOGGER.error("Cannot load default objects", e);
      }
    }
  }

  private static void copyFilesFromClasspath(String classpathPrefix, Path destinationDirectory,
    boolean removeClasspathPrefixFromFinalPath) {
    copyFilesFromClasspath(classpathPrefix, destinationDirectory, removeClasspathPrefixFromFinalPath,
      Collections.emptyList());
  }

  private static void copyFilesFromClasspath(String classpathPrefix, Path destinationDirectory,
    boolean removeClasspathPrefixFromFinalPath, List<String> excludePaths) {

    List<ClassLoader> classLoadersList = new LinkedList<>();
    classLoadersList.add(ClasspathHelper.contextClassLoader());

    Reflections reflections = new Reflections(
      new ConfigurationBuilder().setScanners(new ResourcesScanner()).setUrls(ClasspathHelper.forPackage(classpathPrefix,
        ClasspathHelper.contextClassLoader(), ClasspathHelper.staticClassLoader())));

    Set<String> resources = reflections.getResources(Pattern.compile(".*"));
    resources = resources.stream().filter(r -> !shouldExclude(r, classpathPrefix, excludePaths))
      .collect(Collectors.toSet());

    LOGGER.info("Copying files from classpath prefix={}, destination={}, removePrefix={}, excludePaths={}",
      classpathPrefix, destinationDirectory, removeClasspathPrefixFromFinalPath, excludePaths, resources.size());

    for (String resource : resources) {

      InputStream originStream = RodaCoreFactory.class.getClassLoader().getResourceAsStream(resource);
      Path destinyPath;

      String resourceFileName = resource;

      // Removing ":" escape
      resourceFileName = resourceFileName.replace("::", ":");

      if (removeClasspathPrefixFromFinalPath) {
        destinyPath = destinationDirectory.resolve(resourceFileName.replaceFirst(classpathPrefix, ""));
      } else {
        destinyPath = destinationDirectory.resolve(resourceFileName);
      }

      try {
        // create all parent directories
        Files.createDirectories(destinyPath.getParent());
        // copy file
        Files.copy(originStream, destinyPath, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        LOGGER.error("Error copying file from classpath: {} to {} (reason: {})", originStream, destinyPath,
          e.getMessage());
        instantiatedWithoutErrors = false;
      } finally {
        RodaUtils.closeQuietly(originStream);
      }

    }
  }

  private static boolean shouldExclude(String resource, String classpathPrefix, List<String> excludePaths) {
    boolean exclude = false;

    if (resource.startsWith(classpathPrefix)) {

      for (String excludePath : excludePaths) {
        if (resource.startsWith(excludePath)) {
          exclude = true;
          break;
        }
      }
    } else {
      exclude = true;
    }
    return exclude;
  }

  private static void copyFilesFromClasspath(String classpathPrefix, Path destinationDirectory) {
    copyFilesFromClasspath(classpathPrefix, destinationDirectory, false);
  }

  private static void instantiatePluginManager() {
    if (nodeType == NodeType.MASTER || nodeType == NodeType.WORKER || TEST_DEPLOY_PLUGIN_MANAGER) {
      try {
        pluginManager = PluginManager.instantiatePluginManager(getConfigPath(), getPluginsPath());
      } catch (PluginManagerException e) {
        LOGGER.error("Error instantiating PluginManager", e);
        instantiatedWithoutErrors = false;
      }
    }
  }

  private static void instantiateStorageAndModel() throws GenericException {
    storage = instantiateStorage();
    LOGGER.debug("Finished instantiating storage...");
    model = new ModelService(storage);
    LOGGER.debug("Finished instantiating model...");
  }

  private static StorageService instantiateStorage() throws GenericException {
    StorageType storageType = StorageType.valueOf(
      getRodaConfiguration().getString(RodaConstants.CORE_STORAGE_TYPE, RodaConstants.DEFAULT_STORAGE_TYPE.toString()));
    if (storageType == RodaConstants.StorageType.FEDORA4) {
      String url = getRodaConfiguration().getString(RodaConstants.CORE_STORAGE_FEDORA4_URL,
        "http://localhost:8983/solr/");
      String username = getRodaConfiguration().getString(RodaConstants.CORE_STORAGE_FEDORA4_USERNAME, "");
      String password = getRodaConfiguration().getString(RodaConstants.CORE_STORAGE_FEDORA4_PASSWORD, "");

      if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
        LOGGER.debug("Going to instantiate Fedora with url '{}' username '{}' & password '{}'", url, username,
          password);
        return new FedoraStorageService(url, username, password);
      } else {
        LOGGER.debug("Going to instantiate Fedora with url '{}'", url);
        return new FedoraStorageService(url);
      }
    } else if (storageType == RodaConstants.StorageType.FILESYSTEM) {
      LOGGER.debug("Going to instantiate Filesystem on '{}'", storagePath);
      String trashDirName = getRodaConfiguration().getString("core.storage.filesystem.trash", "trash");
      return new FileStorageService(storagePath, trashDirName);
    } else {
      LOGGER.error("Unknown storage service '{}'", storageType.name());
      throw new GenericException();
    }
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
   */
  private static void instantiateSolrAndIndexService() throws URISyntaxException {
    if (nodeType == NodeType.MASTER) {
      tempIndexConfigsPath = Optional.empty();
      Path solrHome = configPath.resolve(RodaConstants.CORE_INDEX_FOLDER);
      if (!FSUtils.exists(solrHome) || FEATURE_OVERRIDE_INDEX_CONFIGS) {
        try {
          Path tempConfig = Files.createTempDirectory(getWorkingDirectory(), RodaConstants.CORE_INDEX_FOLDER);
          toDeleteDuringShutdown.add(tempConfig);
          tempIndexConfigsPath = Optional.of(tempConfig);
          copyFilesFromClasspath(RodaConstants.CORE_CONFIG_FOLDER + "/" + RodaConstants.CORE_INDEX_FOLDER + "/",
            tempConfig);
          solrHome = tempConfig.resolve(RodaConstants.CORE_CONFIG_FOLDER).resolve(RodaConstants.CORE_INDEX_FOLDER);
          LOGGER.info("Using SOLR home: {}", solrHome);
        } catch (IOException e) {
          LOGGER.error("Error creating temporary SOLR home", e);
          instantiatedWithoutErrors = false;
        }
      }

      // instantiate solr
      solr = instantiateSolr(solrHome);

      // instantiate index related object
      index = new IndexService(solr, model);
    } else if (nodeType == NodeType.TEST && TEST_DEPLOY_SOLR) {
      try {
        Path tempConfig = Files.createTempDirectory(getWorkingDirectory(), RodaConstants.CORE_INDEX_FOLDER);
        copyFilesFromClasspath(RodaConstants.CORE_CONFIG_FOLDER + "/" + RodaConstants.CORE_INDEX_FOLDER + "/",
          tempConfig, true);

        // instantiate solr
        solr = instantiateSolr(tempConfig);

        // instantiate index related object
        index = new IndexService(solr, model);
      } catch (IOException e) {
        LOGGER.error("Unable to instantiate Solr in TEST mode", e);
        instantiatedWithoutErrors = false;
      }
    }
  }

  private static String getConfigurationString(String key, String defaultValue) {
    String envKey = "RODA_" + key.toUpperCase().replace('.', '_');
    String value = System.getenv(envKey);
    if (value == null) {
      value = getRodaConfiguration().getString(key, defaultValue);
    }

    return value;
  }

  private static SolrClient instantiateSolr(Path solrHome) {
    SolrType solrType = SolrType
      .valueOf(getConfigurationString(RodaConstants.CORE_SOLR_TYPE, RodaConstants.DEFAULT_SOLR_TYPE.toString()));

    if (TEST_SOLR_TYPE != null) {
      solrType = TEST_SOLR_TYPE;
    }

    if (solrType == RodaConstants.SolrType.HTTP) {
      String solrBaseUrl = getConfigurationString(RodaConstants.CORE_SOLR_HTTP_URL, "http://localhost:8983/solr/");
      LOGGER.info("Instantiating SOLR HTTP at {}", solrBaseUrl);
      return new HttpSolrClient(solrBaseUrl);
    } else if (solrType == RodaConstants.SolrType.HTTP_CLOUD || solrType == RodaConstants.SolrType.CLOUD) {
      String solrCloudZooKeeperUrls = getConfigurationString(RodaConstants.CORE_SOLR_CLOUD_URLS, getConfigurationString(
        RodaConstants.CORE_SOLR_HTTP_CLOUD_URLS, "localhost:2181,localhost:2182,localhost:2183"));
      LOGGER.info("Instantiating SOLR Cloud at {}", solrCloudZooKeeperUrls);
      CloudSolrClient cloudSolrClient = new CloudSolrClient(solrCloudZooKeeperUrls);
      bootstrap(cloudSolrClient, solrHome);
      return cloudSolrClient;
    } else {
      // default to Embedded
      setSolrSystemProperties();
      LOGGER.info("Instantiating SOLR Embedded");
      return new EmbeddedSolrServer(solrHome, "test");
    }
  }

  private static void bootstrap(CloudSolrClient cloudSolrClient, Path solrHome) {
    CollectionAdminRequest.List req = new CollectionAdminRequest.List();
    try {
      CollectionAdminResponse response = req.process(cloudSolrClient);

      @SuppressWarnings("unchecked")
      List<String> existingCollections = (List<String>) response.getResponse().get("collections");
      if (existingCollections == null) {
        existingCollections = new ArrayList<>();
      }

      Map<String, Path> defaultCollections = getDefaultCollections(solrHome);

      for (String defaultCollection : defaultCollections.keySet()) {
        if (!existingCollections.contains(defaultCollection)) {
          createCollection(cloudSolrClient, defaultCollection, defaultCollections.get(defaultCollection));
        }
      }

    } catch (SolrServerException | IOException e) {
      LOGGER.error("Solr bootstrap failed", e);
    }
  }

  private static Map<String, Path> getDefaultCollections(Path solrHome) throws IOException {
    Map<String, Path> collections = new HashMap<>();
    Files.list(solrHome).forEach(p -> {
      if (Files.isDirectory(p)) {
        collections.put(p.getFileName().toString(), p);
      }
    });
    return collections;
  }

  private static void createCollection(CloudSolrClient cloudSolrClient, String collection, Path configPath) {
    CollectionAdminRequest.Create req = new CollectionAdminRequest.Create();
    try {

      LOGGER.info("Creating SOLR collection {}", collection);
      Path collectionConf = configPath.resolve("conf");
      Path solrCoreProperties = collectionConf.resolve("solrcore.properties");
      try (BufferedWriter solrCorePropertiesWriter = Files.newBufferedWriter(solrCoreProperties)) {
        IOUtils.write(String.format("name=%1$s\nsolr.data.dir.%2$s=data", collection, collection.toLowerCase()),
          solrCorePropertiesWriter);
      }

      cloudSolrClient.uploadConfig(collectionConf, collection);

      req.setCollectionName(collection).setConfigName(collection);

      req.setNumShards(getEnvInt("SOLR_NUM_SHARDS", 1));
      req.setMaxShardsPerNode(getEnvInt("SOLR_MAX_SHARDS_PER_NODE", 1));
      req.setReplicationFactor(getEnvInt("SOLR_REPLICATION_FACTOR", 1));
      req.setAutoAddReplicas(getEnvBoolean("SOLR_AUTO_ADD_REPLICAS", false));

      CollectionAdminResponse response = req.process(cloudSolrClient);
      if (!response.isSuccess()) {
        LOGGER.error("Could not create collection {}: {}", collection, response.getErrorMessages());
      }
    } catch (SolrServerException | SolrException | IOException e) {
      LOGGER.error("Error creating collection {}", collection, e);
    }

  }

  private static String getEnvString(String name, String defaultValue) {
    String envString = System.getenv(name);
    return StringUtils.isNotBlank(envString) ? envString : defaultValue;
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

  private static void setSolrSystemProperties() {
    System.setProperty("solr.data.dir", indexDataPath.toString());
    System.setProperty("solr.data.dir.aip", indexDataPath.resolve(RodaConstants.CORE_AIP_FOLDER).toString());
    System.setProperty("solr.data.dir.representations",
      indexDataPath.resolve(RodaConstants.CORE_REPRESENTATION_FOLDER).toString());
    System.setProperty("solr.data.dir.file", indexDataPath.resolve(RodaConstants.CORE_FILE_FOLDER).toString());
    System.setProperty("solr.data.dir.preservationevent",
      indexDataPath.resolve(RodaConstants.CORE_PRESERVATIONEVENT_FOLDER).toString());
    System.setProperty("solr.data.dir.preservationagent",
      indexDataPath.resolve(RodaConstants.CORE_PRESERVATIONAGENT_FOLDER).toString());
    System.setProperty("solr.data.dir.actionlog",
      indexDataPath.resolve(RodaConstants.CORE_ACTIONLOG_FOLDER).toString());
    System.setProperty("solr.data.dir.members", indexDataPath.resolve(RodaConstants.CORE_MEMBERS_FOLDER).toString());
    System.setProperty("solr.data.dir.transferredresource",
      indexDataPath.resolve(RodaConstants.CORE_TRANSFERREDRESOURCE_FOLDER).toString());
    System.setProperty("solr.data.dir.job", indexDataPath.resolve(RodaConstants.CORE_JOB_FOLDER).toString());
    System.setProperty("solr.data.dir.jobreport",
      indexDataPath.resolve(RodaConstants.CORE_JOBREPORT_FOLDER).toString());
    System.setProperty("solr.data.dir.risk", indexDataPath.resolve(RodaConstants.CORE_RISK_FOLDER).toString());
    System.setProperty("solr.data.dir.format", indexDataPath.resolve(RodaConstants.CORE_FORMAT_FOLDER).toString());
    System.setProperty("solr.data.dir.agent", indexDataPath.resolve(RodaConstants.CORE_AGENT_FOLDER).toString());
    System.setProperty("solr.data.dir.notification",
      indexDataPath.resolve(RodaConstants.CORE_NOTIFICATION_FOLDER).toString());
    System.setProperty("solr.data.dir.riskincidence",
      indexDataPath.resolve(RodaConstants.CORE_RISKINCIDENCE_FOLDER).toString());
    System.setProperty("solr.data.dir.dip", indexDataPath.resolve(RodaConstants.CORE_DIP_FOLDER).toString());
    System.setProperty("solr.data.dir.dipfile", indexDataPath.resolve(RodaConstants.CORE_DIP_FILE_FOLDER).toString());
    System.setProperty("solr.data.dir.representation-information",
      indexDataPath.resolve(RodaConstants.CORE_REPRESENTATION_INFORMATION_FOLDER).toString());
  }

  private static void instantiateNodeSpecificObjects(NodeType nodeType) {
    if (nodeType == NodeType.MASTER) {
      instantiateMasterNodeSpecificObjects();
    } else if (nodeType == NodeType.WORKER) {
      instantiateWorkerNodeSpecificObjects();
    } else if (nodeType == NodeType.TEST) {
      instantiateTestNodeSpecificObjects();
    } else {
      LOGGER.error("Unknown node type '{}'", nodeType);
      instantiatedWithoutErrors = false;
    }
  }

  private static void instantiateMasterNodeSpecificObjects() {
    if (FEATURE_DISTRIBUTED_AKKA) {
      akkaDistributedPluginOrchestrator = new AkkaDistributedPluginOrchestrator(
        getSystemProperty(RodaConstants.CORE_NODE_HOSTNAME, RodaConstants.DEFAULT_NODE_HOSTNAME),
        getSystemProperty(RodaConstants.CORE_NODE_PORT, RodaConstants.DEFAULT_NODE_PORT));
      akkaDistributedPluginOrchestrator.cleanUnfinishedJobs();
    } else {
      // pluginOrchestrator = new EmbeddedActionOrchestrator();
      pluginOrchestrator = new AkkaEmbeddedPluginOrchestrator();
      pluginOrchestrator.cleanUnfinishedJobs();
    }

    startApacheDS();

    instantiateTransferredResourcesScanner();

    processPreservationEventTypeProperties();
  }

  private static void instantiateWorkerNodeSpecificObjects() {
    akkaDistributedPluginWorker = new AkkaDistributedPluginWorker(
      getSystemProperty(RodaConstants.CORE_CLUSTER_HOSTNAME, RodaConstants.DEFAULT_NODE_HOSTNAME),
      getSystemProperty(RodaConstants.CORE_CLUSTER_PORT, RodaConstants.DEFAULT_NODE_PORT),
      getSystemProperty(RodaConstants.CORE_NODE_HOSTNAME, RodaConstants.DEFAULT_NODE_HOSTNAME),
      getSystemProperty(RodaConstants.CORE_NODE_PORT, "0"));
  }

  private static void instantiateTestNodeSpecificObjects() {
    if (TEST_DEPLOY_LDAP) {
      startApacheDS();
    }

    if (TEST_DEPLOY_SCANNER) {
      instantiateTransferredResourcesScanner();
    }

    if (TEST_DEPLOY_ORCHESTRATOR) {
      pluginOrchestrator = new AkkaEmbeddedPluginOrchestrator();
    }
  }

  public static void addLogger(String loggerConfigurationFile) {
    URL loggerConfigurationFileUrl = getConfigurationFile(loggerConfigurationFile);
    if (loggerConfigurationFileUrl != null) {
      System.setProperty("roda.logback.include", loggerConfigurationFileUrl.toString());
      configureLogback();
    }
  }

  private static String getSystemProperty(String property, String defaultValue) {
    String ret = defaultValue;
    if (System.getProperty(property) != null) {
      ret = System.getProperty(property);
    }
    return ret;
  }

  public static void shutdown() throws IOException {
    if (instantiated) {

      if (nodeType == NodeType.MASTER) {
        solr.close();
        stopApacheDS();
        pluginManager.shutdown();
        pluginOrchestrator.shutdown();
      } else if (nodeType == NodeType.WORKER) {
        pluginManager.shutdown();
      } else if (nodeType == NodeType.TEST) {
        if (TEST_DEPLOY_SOLR) {
          solr.close();
        }
        if (TEST_DEPLOY_LDAP) {
          stopApacheDS();
        }
        if (TEST_DEPLOY_ORCHESTRATOR) {
          pluginOrchestrator.shutdown();
        }
        // final cleanup
        FSUtils.deletePathQuietly(workingDirectoryPath);
      }

      // stop jmx metrics reporter
      if (getSystemProperty("com.sun.management.jmxremote", null) != null) {
        jmxMetricsReporter.stop();
      }

      // delete resources that are no longer needed
      toDeleteDuringShutdown.forEach(e -> FSUtils.deletePathQuietly(e));

    }
  }

  public static MetricRegistry getMetrics() {
    return metricsRegistry;
  }

  /**
   * Start ApacheDS.
   */
  private static void startApacheDS() {
    rodaApacheDSDataDirectory = getEssentialDirectoryPath(dataPath, RodaConstants.CORE_LDAP_FOLDER);

    try {
      final Configuration rodaConfig = RodaCoreFactory.getRodaConfiguration();

      final boolean ldapStartServer = rodaConfig.getBoolean("ldap.startServer", false);
      final int ldapPort = rodaConfig.getInt("ldap.port", RodaConstants.CORE_LDAP_DEFAULT_PORT);
      final String ldapBaseDN = rodaConfig.getString("ldap.baseDN", "dc=roda,dc=org");
      final String ldapPeopleDN = rodaConfig.getString("ldap.peopleDN", "ou=users,dc=roda,dc=org");
      final String ldapGroupsDN = rodaConfig.getString("ldap.groupsDN", "ou=groups,dc=roda,dc=org");
      final String ldapRolesDN = rodaConfig.getString("ldap.rolesDN", "ou=groups,dc=roda,dc=org");
      final String ldapAdminDN = rodaConfig.getString("ldap.adminDN", "ou=groups,dc=roda,dc=org");
      final String ldapAdminPassword = rodaConfig.getString("ldap.adminPassword", "roda");
      final String ldapPasswordDigestAlgorithm = rodaConfig.getString("ldap.passwordDigestAlgorithm", "MD5");
      final List<String> ldapProtectedUsers = RodaUtils.copyList(rodaConfig.getList("ldap.protectedUsers"));
      final List<String> ldapProtectedGroups = RodaUtils.copyList(rodaConfig.getList("ldap.protectedGroups"));
      final String rodaGuestDN = rodaConfig.getString("ldap.rodaGuestDN", "uid=guest,ou=users,dc=roda,dc=org");
      final String rodaAdminDN = rodaConfig.getString("ldap.rodaAdminDN", "uid=admin,ou=users,dc=roda,dc=org");
      final String rodaAdministratorsDN = rodaConfig.getString("ldap.rodaAdministratorsDN",
        "cn=administrators,ou=groups,dc=roda,dc=org");

      RodaCoreFactory.ldapUtility = new LdapUtility(ldapStartServer, ldapPort, ldapBaseDN, ldapPeopleDN, ldapGroupsDN,
        ldapRolesDN, ldapAdminDN, ldapAdminPassword, ldapPasswordDigestAlgorithm, ldapProtectedUsers,
        ldapProtectedGroups, rodaGuestDN, rodaAdminDN, rodaApacheDSDataDirectory);
      ldapUtility.setRODAAdministratorsDN(rodaAdministratorsDN);

      UserUtility.setLdapUtility(ldapUtility);

      if (!FSUtils.exists(rodaApacheDSDataDirectory)) {
        Files.createDirectories(rodaApacheDSDataDirectory);
        final List<String> ldifFileNames = Arrays.asList("users.ldif", "groups.ldif", "roles.ldif");
        final List<String> ldifs = new ArrayList<>();
        for (String ldifFileName : ldifFileNames) {
          final InputStream ldifInputStream = RodaCoreFactory
            .getConfigurationFileAsStream(RodaConstants.CORE_LDAP_FOLDER + "/" + ldifFileName);
          ldifs.add(IOUtils.toString(ldifInputStream, RodaConstants.DEFAULT_ENCODING));
          RodaUtils.closeQuietly(ldifInputStream);
        }

        RodaCoreFactory.ldapUtility.initDirectoryService(ldifs);
        indexUsersAndGroupsFromLDAP();
      } else {
        RodaCoreFactory.ldapUtility.initDirectoryService();
      }

      createRoles(rodaConfig);
      indexUsersAndGroupsFromLDAP();

    } catch (final Exception e) {
      LOGGER.error("Error starting up embedded ApacheDS", e);
      instantiatedWithoutErrors = false;
    }

  }

  private static void stopApacheDS() {
    try {
      RodaCoreFactory.ldapUtility.stopService();
    } catch (final Exception e) {
      LOGGER.error("Error while shutting down ApacheDS embedded server", e);
    }
  }

  /**
   * For each role in roda-roles.properties create the role in LDAP if it don't
   * exist already.
   * 
   * @param rodaConfig
   *          roda configuration
   * @throws GenericException
   *           if something unexpected happens creating roles.
   */
  private static void createRoles(final Configuration rodaConfig) throws GenericException {
    final Iterator<String> keys = rodaConfig.getKeys("core.roles");
    final Set<String> roles = new HashSet<>();
    while (keys.hasNext()) {
      roles.addAll(Arrays.asList(rodaConfig.getStringArray(keys.next())));
    }
    for (final String role : roles) {
      try {
        if (StringUtils.isNotBlank(role)) {
          RodaCoreFactory.ldapUtility.addRole(role);
          LOGGER.debug("Created LDAP role {}", role);
        }
      } catch (final RoleAlreadyExistsException e) {
        LOGGER.debug("Role {} already exists.", role);
        LOGGER.trace(e.getMessage(), e);
      }
    }
  }

  private static void indexUsersAndGroupsFromLDAP()
    throws GenericException, IllegalOperationException, NotFoundException, AlreadyExistsException {
    for (User user : model.listUsers()) {
      LOGGER.debug("User to be indexed: {}", user);
      model.notifyUserUpdated(user);
    }
    for (Group group : model.listGroups()) {
      LOGGER.debug("Group to be indexed: {}", group);
      model.notifyGroupUpdated(group);
    }
  }

  public static void instantiateTransferredResourcesScanner() {
    try {
      String transferredResourcesFolder = getConfigurationString("transferredResources.folder",
        RodaConstants.CORE_TRANSFERREDRESOURCE_FOLDER);
      Path transferredResourcesFolderPath = dataPath.resolve(transferredResourcesFolder);

      if (!FSUtils.exists(transferredResourcesFolderPath)) {
        Files.createDirectories(transferredResourcesFolderPath);
      }

      transferredResourcesScanner = new TransferredResourcesScanner(transferredResourcesFolderPath, getIndexService());
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

  public static StorageService getStorageService() {
    return storage;
  }

  public static ModelService getModelService() {
    return model;
  }

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

  public static AkkaDistributedPluginOrchestrator getAkkaDistributedPluginOrchestrator() {
    return akkaDistributedPluginOrchestrator;
  }

  public static TransferredResourcesScanner getTransferredResourcesScanner() {
    return transferredResourcesScanner;
  }

  public static NodeType getNodeType() {
    return nodeType;
  }

  public static Path getRodaHomePath() {
    return rodaHomePath;
  }

  public static Path getConfigPath() {
    return configPath;
  }

  public static Path getDefaultPath() {
    return defaultPath;
  }

  public static Path getWorkingDirectory() {
    return workingDirectoryPath;
  }

  public static Path getReportsDirectory() {
    return reportDirectoryPath;
  }

  public static Path getDataPath() {
    return dataPath;
  }

  public static Path getStoragePath() {
    return storagePath;
  }

  public static Path getLogPath() {
    return logPath;
  }

  public static Path getPluginsPath() {
    return configPath.resolve(RodaConstants.CORE_PLUGINS_FOLDER);
  }

  public static void closeSolrServer() {
    try {
      solr.close();
    } catch (IOException e) {
      LOGGER.error("Error while shutting down solr", e);
    }
  }

  /*
   * Configuration related functionalities
   */
  public static void addConfiguration(String configurationFile) throws ConfigurationException {
    Configuration configuration = getConfiguration(configurationFile);
    rodaConfiguration.addConfiguration(configuration);
    configurationFiles.add(configurationFile);
  }

  public static void addExternalConfiguration(Path configurationPath) throws ConfigurationException {
    Configuration configuration = getExternalConfiguration(configurationPath);
    rodaConfiguration.addConfiguration(configuration);
  }

  private static Configuration getConfiguration(String configurationFile) throws ConfigurationException {
    Path config = RodaCoreFactory.getConfigPath().resolve(configurationFile);

    Configuration configuration;

    if (FSUtils.exists(config)) {
      configuration = getExternalConfiguration(config);
    } else {
      configuration = getInternalConfiguration(configurationFile);
    }

    return configuration;
  }

  private static PropertiesConfiguration initConfiguration() {
    PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
    propertiesConfiguration.setDelimiterParsingDisabled(true);
    propertiesConfiguration.setEncoding(RodaConstants.DEFAULT_ENCODING);
    return propertiesConfiguration;
  }

  private static Configuration getInternalConfiguration(String configurationFile) throws ConfigurationException {
    PropertiesConfiguration propertiesConfiguration = initConfiguration();
    InputStream inputStream = RodaCoreFactory.class
      .getResourceAsStream("/" + RodaConstants.CORE_CONFIG_FOLDER + "/" + configurationFile);
    if (inputStream != null) {
      LOGGER.trace("Loading configuration from classpath {}", configurationFile);
      propertiesConfiguration.load(inputStream);

    } else {
      LOGGER.trace("Configuration {} doesn't exist", configurationFile);
    }

    // do variable interpolation
    Configuration configuration = propertiesConfiguration.interpolatedConfiguration();

    return configuration;
  }

  private static Configuration getExternalConfiguration(Path config) throws ConfigurationException {
    PropertiesConfiguration propertiesConfiguration = initConfiguration();
    LOGGER.trace("Loading configuration from file {}", config);
    propertiesConfiguration.load(config.toFile());
    RodaPropertiesReloadStrategy rodaPropertiesReloadStrategy = new RodaPropertiesReloadStrategy();
    rodaPropertiesReloadStrategy.setRefreshDelay(5000);
    propertiesConfiguration.setReloadingStrategy(rodaPropertiesReloadStrategy);

    // do variable interpolation
    Configuration configuration = propertiesConfiguration.interpolatedConfiguration();

    return configuration;

  }

  public static URL getConfigurationFile(String configurationFile) {
    Path config = RodaCoreFactory.getConfigPath().resolve(configurationFile);
    URL configUri;
    if (FSUtils.exists(config) && !FSUtils.isDirectory(config)
      && config.toAbsolutePath().startsWith(getConfigPath().toAbsolutePath().toString())) {
      try {
        configUri = config.toUri().toURL();
      } catch (MalformedURLException e) {
        LOGGER.error("Configuration {} doesn't exist", configurationFile);
        configUri = null;
      }
    } else {
      URL resource = RodaCoreFactory.class
        .getResource("/" + RodaConstants.CORE_CONFIG_FOLDER + "/" + configurationFile);
      if (resource != null) {
        configUri = resource;
      } else {
        LOGGER.error("Configuration {} doesn't exist", configurationFile);
        configUri = null;
      }
    }

    return configUri;
  }

  public static InputStream getConfigurationFileAsStream(String configurationFile) {
    Path config = getConfigPath().resolve(configurationFile);
    InputStream inputStream = null;
    try {
      if (FSUtils.exists(config) && !FSUtils.isDirectory(config)
        && config.toAbsolutePath().startsWith(getConfigPath().toAbsolutePath().toString())) {
        inputStream = Files.newInputStream(config);
        LOGGER.trace("Loading configuration from file {}", config);
      }
    } catch (IOException e) {
      // do nothing
    }
    if (inputStream == null) {
      inputStream = RodaCoreFactory.class
        .getResourceAsStream("/" + RodaConstants.CORE_CONFIG_FOLDER + "/" + configurationFile);
      LOGGER.trace("Loading configuration from classpath {}", configurationFile);
    }
    return inputStream;
  }

  public static InputStream getConfigurationFileAsStream(String configurationFile, String fallbackConfigurationFile) {
    InputStream inputStream = getConfigurationFileAsStream(configurationFile);
    if (inputStream == null) {
      inputStream = getConfigurationFileAsStream(fallbackConfigurationFile);
    }
    return inputStream;
  }

  public static InputStream getDefaultFileAsStream(String defaultFile, ClassLoader... extraClassLoaders) {
    Path defaultPath = getDefaultPath().resolve(defaultFile);
    InputStream inputStream = null;
    try {
      if (FSUtils.exists(defaultPath) && !FSUtils.isDirectory(defaultPath)
        && defaultPath.toAbsolutePath().startsWith(getDefaultPath().toAbsolutePath().toString())) {
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
    rodaPropertiesCache.clear();
    RODA_SCHEMAS_CACHE.invalidateAll();
    I18N_CACHE.invalidateAll();
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

  public static Configuration getRodaConfiguration() {
    return rodaConfiguration;
  }

  public static String getConfigurationKey(String... keyParts) {
    StringBuilder sb = new StringBuilder();
    for (String part : keyParts) {
      if (sb.length() != 0) {
        sb.append('.');
      }
      sb.append(part);
    }
    return sb.toString();
  }

  public static String getRodaConfigurationAsString(String... keyParts) {
    return rodaConfiguration.getString(getConfigurationKey(keyParts));
  }

  public static int getRodaConfigurationAsInt(int defaultValue, String... keyParts) {
    return rodaConfiguration.getInt(getConfigurationKey(keyParts), defaultValue);
  }

  public static List<String> getRodaConfigurationAsList(String... keyParts) {
    String[] array = rodaConfiguration.getStringArray(getConfigurationKey(keyParts));
    return Arrays.asList(array).stream().filter(v -> StringUtils.isNotBlank(v)).collect(Collectors.toList());
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

  public static Set<String> getFilenamesInsideConfigFolder(String folder) throws IOException {

    Set<String> fileNames = new HashSet<>();

    // get from external config
    Set<String> externalFileNames = new HashSet<>();
    Path configPath = RodaCoreFactory.getConfigPath().resolve(folder);
    Files.walkFileTree(configPath, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        externalFileNames.add(file.getFileName().toString());
        return FileVisitResult.CONTINUE;
      }
    });

    fileNames.addAll(externalFileNames);

    // get from internal config
    List<ClassLoader> classLoadersList = new LinkedList<>();
    classLoadersList.add(ClasspathHelper.contextClassLoader());
    Set<String> internalFilesPath = new Reflections(new ConfigurationBuilder()
      .filterInputsBy(
        new FilterBuilder().include(FilterBuilder.prefix("" + RodaConstants.CORE_CONFIG_FOLDER + "/" + folder)))
      .setScanners(new ResourcesScanner())
      .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0]))))
        .getResources(Pattern.compile(".*"));
    for (String internalFilePath : internalFilesPath) {
      fileNames.add(Paths.get(internalFilePath).getFileName().toString());
    }

    return fileNames;
  }

  public static Map<String, String> getPropertiesFromCache(String cacheName, List<String> prefixesToCache) {
    if (rodaPropertiesCache.get(cacheName) == null) {
      fillInPropertiesToCache(cacheName, prefixesToCache);
    }
    return rodaPropertiesCache.get(cacheName);
  }

  private static void fillInPropertiesToCache(String cacheName, List<String> prefixesToCache) {
    if (rodaPropertiesCache.get(cacheName) == null) {
      HashMap<String, String> newCacheEntry = new HashMap<>();

      Configuration configuration = RodaCoreFactory.getRodaConfiguration();
      Iterator<String> keys = configuration.getKeys();
      while (keys.hasNext()) {
        String key = String.class.cast(keys.next());
        String value = configuration.getString(key, "");
        for (String prefixToCache : prefixesToCache) {
          if (key.startsWith(prefixToCache)) {
            newCacheEntry.put(key, value);
            break;
          }
        }
      }
      rodaPropertiesCache.put(cacheName, newCacheEntry);
    }
  }

  public static Messages getI18NMessages(Locale locale) {
    checkForChangesInI18N();
    try {
      return I18N_CACHE.get(locale);
    } catch (ExecutionException e) {
      LOGGER.debug("Could not load messages", e);
      return null;
    }
  }

  private static void checkForChangesInI18N() {
    // i18n is cached and that cache is re-done when changes occur to
    // roda-*.properties (for convenience)
    getRodaConfiguration().getString("");
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
        } catch (IllegalOperationException | GenericException | NotFoundException | AlreadyExistsException e) {
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
    } catch (SolrServerException | IOException e) {
      e.printStackTrace(System.err);
    }
  }

  private static void printIndexMembers(List<String> args, Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    System.out.println("Index list " + args.get(2));
    IndexResult<RODAMember> users = index.find(RODAMember.class, filter, sorter, sublist, facets, new ArrayList<>());
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
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_SEARCH, "OLA-OLÁ-1234-XXXX_K"));
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

  private static String readPassword(final String message) throws IOException {
    final Console console = System.console();
    if (console == null) {
      final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      System.out.print(String.format("%s (INSECURE - password will be shown): ", message));
      return reader.readLine();
    } else {
      return new String(console.readPassword("%s: ", message));
    }
  }

  private static void resetAdminAccess() throws GenericException {
    try {
      final String password = readPassword("New admin password");
      final String passwordConfirmation = readPassword("Repeat admin password");
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
        MigrationManager migrationManager = new MigrationManager(RodaCoreFactory.dataPath);
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

  public static void main(final String[] argsArray)
    throws InterruptedException, GenericException, RequestNotValidException {
    final List<String> args = Arrays.asList(argsArray);

    preInstantiateSteps(args);
    instantiate();
    if (getNodeType() == NodeType.MASTER) {
      if (!args.isEmpty()) {
        mainMasterTasks(args);
      } else {
        printMainUsage();
      }
    } else if (getNodeType() == NodeType.WORKER) {
      Thread.currentThread().join();
    } else {
      printMainUsage();
    }

    System.exit(0);
  }
}
