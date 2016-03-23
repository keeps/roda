/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

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
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.roda.core.common.ApacheDS;
import org.roda.core.common.LdapUtility;
import org.roda.core.common.Messages;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.UserUtility;
import org.roda.core.common.monitor.FolderMonitorNIO;
import org.roda.core.common.monitor.FolderObserver;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.FilterParameter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.NodeType;
import org.roda.core.data.common.RodaConstants.PreservationAgentType;
import org.roda.core.data.common.RodaConstants.SolrType;
import org.roda.core.data.common.RodaConstants.StorageType;
import org.roda.core.data.descriptionLevels.DescriptionLevelManager;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexFolderObserver;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginManager;
import org.roda.core.plugins.PluginManagerException;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.AkkaDistributedPluginOrchestrator;
import org.roda.core.plugins.orchestrate.AkkaDistributedPluginWorker;
import org.roda.core.plugins.orchestrate.AkkaEmbeddedPluginOrchestrator;
import org.roda.core.plugins.plugins.antivirus.AntivirusPlugin;
import org.roda.core.plugins.plugins.base.DescriptiveMetadataValidationPlugin;
import org.roda.core.plugins.plugins.base.FixityPlugin;
import org.roda.core.plugins.plugins.base.LogCleanerPlugin;
import org.roda.core.plugins.plugins.base.ReindexAIPPlugin;
import org.roda.core.plugins.plugins.base.RemoveOrphansPlugin;
import org.roda.core.plugins.plugins.ingest.BagitToAIPPlugin;
import org.roda.core.plugins.plugins.ingest.EARKSIPToAIPPlugin;
import org.roda.core.plugins.plugins.ingest.TransferredResourceToAIPPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.DroidPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.ExifToolPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.FFProbePlugin;
import org.roda.core.plugins.plugins.ingest.characterization.FITSPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.JHOVEPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.JpylyzerPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.MediaInfoPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.SiegfriedPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPlugin;
import org.roda.core.plugins.plugins.ingest.migration.AvconvConvertPlugin;
import org.roda.core.plugins.plugins.ingest.migration.GeneralCommandConvertPlugin;
import org.roda.core.plugins.plugins.ingest.migration.GhostScriptConvertPlugin;
import org.roda.core.plugins.plugins.ingest.migration.ImageMagickConvertPlugin;
import org.roda.core.plugins.plugins.ingest.migration.PdfToPdfaPlugin;
import org.roda.core.plugins.plugins.ingest.migration.SoxConvertPlugin;
import org.roda.core.plugins.plugins.ingest.migration.UnoconvConvertPlugin;
import org.roda.core.plugins.plugins.ingest.validation.DigitalSignatureDIPPlugin;
import org.roda.core.plugins.plugins.ingest.validation.DigitalSignaturePlugin;
import org.roda.core.plugins.plugins.ingest.validation.FileFormatPlugin;
import org.roda.core.plugins.plugins.ingest.validation.VeraPDFPlugin;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fedora.FedoraStorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class RodaCoreFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(RodaCoreFactory.class);

  private static boolean instantiated = false;
  private static NodeType nodeType;

  // Core related objects
  private static Path rodaHomePath;
  private static Path storagePath;
  private static Path indexDataPath;
  private static Path dataPath;
  private static Path logPath;
  private static Path configPath;
  private static Path themePath;
  private static Path exampleThemePath;

  private static StorageService storage;
  private static ModelService model;
  private static IndexService index;
  private static SolrClient solr;
  private static boolean FEATURE_OVERRIDE_INDEX_CONFIGS = true;

  private static boolean TEST_DEPLOY_SOLR = true;
  private static boolean TEST_DEPLOY_LDAP = true;
  private static boolean TEST_DEPLOY_FOLDER_MONITOR = true;
  private static boolean TEST_DEPLOY_ORCHESTRATOR = true;
  private static boolean TEST_DEPLOY_PLUGIN_MANAGER = true;

  // Core related constants
  private static final String TRANSFERRED_RESOURCES_LAST_MONITORED_DATE_FILENAME = ".transferredResourcesLastMonitoredDate";

  // Orchestrator related objects
  private static PluginManager pluginManager;
  // FIXME we should have only "one" orchestrator
  private static PluginOrchestrator pluginOrchestrator;
  private static AkkaDistributedPluginOrchestrator akkaDistributedPluginOrchestrator;
  private static AkkaDistributedPluginWorker akkaDistributedPluginWorker;
  private static boolean FEATURE_DISTRIBUTED_AKKA = false;

  // ApacheDS related objects
  private static ApacheDS ldap;
  private static Path rodaApacheDsConfigDirectory = null;
  private static Path rodaApacheDsDataDirectory = null;

  // TransferredResources related objects
  private static FolderMonitorNIO transferredResourcesFolderMonitor;
  private static FolderObserver transferredResourcesFolderObserver;

  // Configuration related objects
  private static CompositeConfiguration rodaConfiguration = null;
  private static List<String> configurationFiles = null;
  private static Map<String, Map<String, String>> propertiesCache = null;
  private static Map<Locale, Messages> i18nMessages = new HashMap<Locale, Messages>();
  private static DescriptionLevelManager descriptionLevelManager = null;

  // 20160211 hsilva: this constant should be deleted
  // Test objects
  private static String aipId = "8785acb5-9dc2-450d-a65e-048a83d5f1ed";

  /** Private empty constructor */
  private RodaCoreFactory() {

  }

  public static void instantiate() {
    NodeType nodeType = NodeType.valueOf(getSystemProperty(RodaConstants.CORE_NODE_TYPE,
      RodaConstants.DEFAULT_NODE_TYPE.name()));

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

  public static void instantiateTest(boolean deploySolr, boolean deployLdap, boolean deployFolderMonitor,
    boolean deployOrchestrator, boolean deployPluginManager) {
    TEST_DEPLOY_SOLR = deploySolr;
    TEST_DEPLOY_LDAP = deployLdap;
    TEST_DEPLOY_FOLDER_MONITOR = deployFolderMonitor;
    TEST_DEPLOY_ORCHESTRATOR = deployOrchestrator;
    TEST_DEPLOY_PLUGIN_MANAGER = deployPluginManager;
    instantiated = false;
    instantiate(NodeType.TEST);
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

        // instantiate essential directories
        instantiateEssentialDirectories();

        // load core configurations
        rodaConfiguration = new CompositeConfiguration();
        configurationFiles = new ArrayList<String>();
        propertiesCache = new HashMap<String, Map<String, String>>();
        addConfiguration("roda-core.properties");
        addConfiguration("roda-core-formats.properties");

        // instantiate storage and model service
        instantiateStorageAndModel();

        // instantiate solr and index service
        instantiateSolrAndIndexService();

        // load description level information
        loadDescriptionLevelInformation();

        // instantiate plugin manager
        instantiatePluginManager();

        instantiateNodeSpecificObjects(nodeType);

        instantiated = true;

      } catch (ConfigurationException e) {
        LOGGER.error("Error loading roda properties", e);
      } catch (URISyntaxException e) {
        LOGGER.error("Error instantiating solr/index model", e);
      } catch (GenericException e) {
        LOGGER.error("Error instantiating storage model", e);
      }

    }
  }

  private static void instantiatePluginManager() {
    if (nodeType == NodeType.MASTER || nodeType == NodeType.WORKER || TEST_DEPLOY_PLUGIN_MANAGER) {
      try {
        pluginManager = PluginManager.getDefaultPluginManager(getConfigPath(), getPluginsPath());
      } catch (PluginManagerException e) {
        LOGGER.error("Error instantiating PluginManager", e);
      }
    }
  }

  private static void loadDescriptionLevelInformation() {
    Properties descriptionLevelConfiguration = new Properties();
    try {
      descriptionLevelConfiguration.load(getConfigurationFile(RodaConstants.CORE_DESCRIPTION_LEVELS_FILE).openStream());
    } catch (IOException e) {
      // do nothing and instantiate description level manager from empty
      // properties object
    }
    LOGGER.trace("Description level configurations being loaded: {}", descriptionLevelConfiguration);
    try {
      descriptionLevelManager = new DescriptionLevelManager(descriptionLevelConfiguration);
    } catch (RequestNotValidException e) {
      LOGGER.error("Error loading description levels", e);
    }
  }

  private static void instantiateEssentialDirectories() {
    // make sure all essential directories exist
    ensureAllEssentialDirectoriesExist();
  }

  private static void instantiateStorageAndModel() throws GenericException {
    storage = instantiateStorage();
    model = new ModelService(storage);
  }

  private static StorageService instantiateStorage() throws GenericException {
    StorageType storageType = StorageType.valueOf(getRodaConfiguration().getString(RodaConstants.CORE_STORAGE_TYPE,
      RodaConstants.DEFAULT_STORAGE_TYPE.toString()));
    if (storageType == RodaConstants.StorageType.FEDORA4) {
      String url = getRodaConfiguration().getString(RodaConstants.CORE_STORAGE_FEDORA4_URL,
        "http://localhost:8983/solr/");
      String username = getRodaConfiguration().getString(RodaConstants.CORE_STORAGE_FEDORA4_USERNAME, "");
      String password = getRodaConfiguration().getString(RodaConstants.CORE_STORAGE_FEDORA4_PASSWORD, "");

      if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
        return new FedoraStorageService(url, username, password);
      } else {
        return new FedoraStorageService(url);
      }
    } else if (storageType == RodaConstants.StorageType.FILESYSTEM) {
      return new FileStorageService(storagePath);
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
    LOGGER
      .info("Warnings like '2016-03-21 11:21:34,319 WARN  org.apache.solr.core.Config - Beginning with Solr 5.5, <maxMergeDocs> is deprecated, configure it on the relevant <mergePolicyFactory> instead.'"
        + " are due to a bug, explained in https://issues.apache.org/jira/browse/SOLR-8734, as we don't declare those parameters in RODA solr configurations."
        + " The warning will be removed and as soon as that happens, this messages should be deleted as well.");
    if (nodeType == NodeType.MASTER) {
      Path solrHome = configPath.resolve(RodaConstants.CORE_INDEX_FOLDER);
      if (!Files.exists(solrHome) || FEATURE_OVERRIDE_INDEX_CONFIGS) {
        try {
          Path tempConfig = Files.createTempDirectory(RodaConstants.CORE_INDEX_FOLDER);
          copyFilesFromClasspath(RodaConstants.CORE_CONFIG_FOLDER + "/" + RodaConstants.CORE_INDEX_FOLDER + "/",
            tempConfig);
          solrHome = tempConfig.resolve(RodaConstants.CORE_CONFIG_FOLDER).resolve(RodaConstants.CORE_INDEX_FOLDER);
          LOGGER.info("Using SOLR home: {}", solrHome);
        } catch (IOException e) {
          LOGGER.error("Error creating temporary SOLR home", e);
          // TODO throw exception?
        }
      }

      // instantiate solr
      solr = instantiateSolr(solrHome);

      // instantiate index related object
      index = new IndexService(solr, model);
    } else if (nodeType == NodeType.TEST && TEST_DEPLOY_SOLR) {
      try {
        URL solrConfigURL = RodaCoreFactory.class.getResource("/" + RodaConstants.CORE_CONFIG_FOLDER + "/"
          + RodaConstants.CORE_INDEX_FOLDER + "/solr.xml");
        Path solrConfigPath = Paths.get(solrConfigURL.toURI());
        Files.copy(solrConfigPath, indexDataPath.resolve("solr.xml"));
        Path aipSchema = indexDataPath.resolve(RodaConstants.CORE_AIP_FOLDER);
        Files.createDirectories(aipSchema);
        Files.createFile(aipSchema.resolve("core.properties"));

        Path solrHome = Paths.get(RodaCoreFactory.class.getResource(
          "/" + RodaConstants.CORE_CONFIG_FOLDER + "/" + RodaConstants.CORE_INDEX_FOLDER + "/").toURI());

        // instantiate solr
        solr = instantiateSolr(solrHome);

        // instantiate index related object
        index = new IndexService(solr, model);
      } catch (IOException e) {
        LOGGER.error("Unable to instantiate Solr in TEST mode", e);
      }
    }
  }

  private static void copyFilesFromClasspath(String classpathPrefix, Path destionationDirectory) {
    copyFilesFromClasspath(classpathPrefix, destionationDirectory, false);
  }

  private static void copyFilesFromClasspath(String classpathPrefix, Path destionationDirectory,
    boolean removeClasspathPrefixFromFinalPath) {
    List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
    classLoadersList.add(ClasspathHelper.contextClassLoader());

    Set<String> resources = new Reflections(new ConfigurationBuilder()
      .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(classpathPrefix)))
      .setScanners(new ResourcesScanner())
      .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[5])))).getResources(Pattern
      .compile(".*"));

    for (String resource : resources) {
      InputStream originStream = RodaCoreFactory.class.getClassLoader().getResourceAsStream(resource);
      Path destinyPath;
      if (removeClasspathPrefixFromFinalPath) {
        destinyPath = destionationDirectory.resolve(resource.replaceFirst(classpathPrefix, ""));
      } else {
        destinyPath = destionationDirectory.resolve(resource);
      }

      try {
        // create all parent directories
        Files.createDirectories(destinyPath.getParent());
        // copy file
        Files.copy(originStream, destinyPath, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        LOGGER.error("Error copying file from classpath: {} to {} (reason: {})", originStream, destinyPath,
          e.getMessage());
      } finally {
        IOUtils.closeQuietly(originStream);
      }
    }
  }

  private static SolrClient instantiateSolr(Path solrHome) {
    SolrType solrType = SolrType.valueOf(getRodaConfiguration().getString(RodaConstants.CORE_SOLR_TYPE,
      RodaConstants.DEFAULT_SOLR_TYPE.toString()));

    if (solrType == RodaConstants.SolrType.HTTP) {
      String solrBaseUrl = getRodaConfiguration().getString(RodaConstants.CORE_SOLR_HTTP_URL,
        "http://localhost:8983/solr/");
      return new HttpSolrClient(solrBaseUrl);
    } else if (solrType == RodaConstants.SolrType.HTTP_CLOUD) {
      String solrCloudZooKeeperUrls = getRodaConfiguration().getString(RodaConstants.CORE_SOLR_HTTP_CLOUD_URLS,
        "zkServerA:2181,zkServerB:2181,zkServerC:2181/solr");
      return new CloudSolrClient(solrCloudZooKeeperUrls);
    } else {
      // default to Embedded
      setSolrSystemProperties();
      return new EmbeddedSolrServer(solrHome, "test");
    }
  }

  private static void setSolrSystemProperties() {
    System.setProperty("solr.data.dir", indexDataPath.toString());
    System.setProperty("solr.data.dir.aip", indexDataPath.resolve(RodaConstants.CORE_AIP_FOLDER).toString());
    System.setProperty("solr.data.dir.representations", indexDataPath.resolve(RodaConstants.CORE_REPRESENTATION_FOLDER)
      .toString());
    System.setProperty("solr.data.dir.file", indexDataPath.resolve(RodaConstants.CORE_FILE_FOLDER).toString());
    System.setProperty("solr.data.dir.preservationevent",
      indexDataPath.resolve(RodaConstants.CORE_PRESERVATIONEVENT_FOLDER).toString());
    System.setProperty("solr.data.dir.preservationagent",
      indexDataPath.resolve(RodaConstants.CORE_PRESERVATIONAGENT_FOLDER).toString());
    System
      .setProperty("solr.data.dir.actionlog", indexDataPath.resolve(RodaConstants.CORE_ACTIONLOG_FOLDER).toString());
    System.setProperty("solr.data.dir.members", indexDataPath.resolve(RodaConstants.CORE_MEMBERS_FOLDER).toString());
    System.setProperty("solr.data.dir.transferredresource",
      indexDataPath.resolve(RodaConstants.CORE_TRANSFERREDRESOURCE_FOLDER).toString());
    System.setProperty("solr.data.dir.job", indexDataPath.resolve(RodaConstants.CORE_JOB_FOLDER).toString());
    System
      .setProperty("solr.data.dir.jobreport", indexDataPath.resolve(RodaConstants.CORE_JOBREPORT_FOLDER).toString());
    System.setProperty("solr.data.dir.risk", indexDataPath.resolve(RodaConstants.CORE_RISK_FOLDER).toString());
    System.setProperty("solr.data.dir.agent", indexDataPath.resolve(RodaConstants.CORE_AGENT_FOLDER).toString());
    System.setProperty("solr.data.dir.format", indexDataPath.resolve(RodaConstants.CORE_FORMAT_FOLDER).toString());
    System.setProperty("solr.data.dir.message", indexDataPath.resolve(RodaConstants.CORE_MESSAGE_FOLDER).toString());
  }

  private static void instantiateNodeSpecificObjects(NodeType nodeType) {
    if (nodeType == NodeType.MASTER) {
      if (FEATURE_DISTRIBUTED_AKKA) {
        akkaDistributedPluginOrchestrator = new AkkaDistributedPluginOrchestrator(getSystemProperty(
          RodaConstants.CORE_NODE_HOSTNAME, RodaConstants.DEFAULT_NODE_HOSTNAME), getSystemProperty(
          RodaConstants.CORE_NODE_PORT, RodaConstants.DEFAULT_NODE_PORT));
      } else {
        // pluginOrchestrator = new EmbeddedActionOrchestrator();
        pluginOrchestrator = new AkkaEmbeddedPluginOrchestrator();
      }

      startApacheDS();

      try {
        startTransferredResourcesFolderMonitor();
      } catch (Exception e) {
        LOGGER.error("Error starting Transferred Resources Monitor: " + e.getMessage(), e);
      }

    } else if (nodeType == NodeType.WORKER) {
      akkaDistributedPluginWorker = new AkkaDistributedPluginWorker(getSystemProperty(
        RodaConstants.CORE_CLUSTER_HOSTNAME, RodaConstants.DEFAULT_NODE_HOSTNAME), getSystemProperty(
        RodaConstants.CORE_CLUSTER_PORT, RodaConstants.DEFAULT_NODE_PORT), getSystemProperty(
        RodaConstants.CORE_NODE_HOSTNAME, RodaConstants.DEFAULT_NODE_HOSTNAME), getSystemProperty(
        RodaConstants.CORE_NODE_PORT, "0"));
    } else if (nodeType == NodeType.TEST) {
      if (TEST_DEPLOY_LDAP) {
        startApacheDS();
      }

      if (TEST_DEPLOY_FOLDER_MONITOR) {
        try {
          startTransferredResourcesFolderMonitor();
        } catch (Exception e) {
          LOGGER.error("Error starting Transferred Resources Monitor: " + e.getMessage(), e);
        }
      }

      if (TEST_DEPLOY_ORCHESTRATOR) {
        pluginOrchestrator = new AkkaEmbeddedPluginOrchestrator();
      }
    } else {
      LOGGER.error("Unknown node type '{}'", nodeType);
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
      if (!Files.exists(rodaHomePath)) {
        try {
          Files.createDirectories(rodaHomePath);
        } catch (IOException e) {
          throw new RuntimeException("Unable to create RODA HOME " + rodaHomePath + ". Aborting...", e);
        }
      }

      // set roda.home in order to correctly configure logging even if no
      // property has been defined
      System.setProperty(RodaConstants.INSTALL_FOLDER_SYSTEM_PROPERTY, rodaHomePath.toString());
    }

    // instantiate essential directories
    configPath = rodaHomePath.resolve(RodaConstants.CORE_CONFIG_FOLDER);
    dataPath = rodaHomePath.resolve(RodaConstants.CORE_DATA_FOLDER);
    logPath = dataPath.resolve(RodaConstants.CORE_LOG_FOLDER);
    storagePath = dataPath.resolve(RodaConstants.CORE_STORAGE_FOLDER);
    indexDataPath = dataPath.resolve(RodaConstants.CORE_INDEX_FOLDER);
    // FIXME the following block should be invoked/injected from RodaWuiServlet
    // (and avoid any cyclic dependency)
    themePath = configPath.resolve(RodaConstants.CORE_THEME_FOLDER);
    exampleThemePath = configPath.resolve(RodaConstants.CORE_EXAMPLE_THEME_FOLDER);

    // configure logback
    if (nodeType != NodeType.TEST) {
      configureLogback();
    }

    return rodaHomePath;
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

  public static void ensureAllEssentialDirectoriesExist() {
    List<Path> essentialDirectories = new ArrayList<Path>();
    essentialDirectories.add(configPath);
    essentialDirectories.add(configPath.resolve(RodaConstants.CORE_CROSSWALKS_FOLDER));
    essentialDirectories.add(configPath.resolve(RodaConstants.CORE_CROSSWALKS_FOLDER).resolve(
      RodaConstants.CORE_INGEST_FOLDER));
    essentialDirectories.add(configPath.resolve(RodaConstants.CORE_CROSSWALKS_FOLDER)
      .resolve(RodaConstants.CORE_DISSEMINATION_FOLDER).resolve(RodaConstants.CORE_HTML_FOLDER));
    essentialDirectories.add(configPath.resolve(RodaConstants.CORE_I18N_FOLDER));
    essentialDirectories.add(configPath.resolve(RodaConstants.CORE_LDAP_FOLDER));
    essentialDirectories.add(configPath.resolve(RodaConstants.CORE_PLUGINS_FOLDER));
    essentialDirectories.add(configPath.resolve(RodaConstants.CORE_SCHEMAS_FOLDER));
    essentialDirectories.add(rodaHomePath.resolve(RodaConstants.CORE_LOG_FOLDER));
    essentialDirectories.add(dataPath);
    essentialDirectories.add(logPath);
    essentialDirectories.add(storagePath);
    essentialDirectories.add(indexDataPath);
    // FIXME the following block should be invoked/injected from RodaWuiServlet
    // (and avoid any cyclic dependency)
    essentialDirectories.add(themePath);

    for (Path path : essentialDirectories) {
      try {
        if (!Files.exists(path)) {
          Files.createDirectories(path);
        }
      } catch (IOException e) {
        LOGGER.error("Unable to create " + path + ". Aborting...", e);
        throw new RuntimeException("Unable to create " + path + ". Aborting...", e);
      }
    }

    // FIXME the following block should be invoked/injected from RodaWuiServlet
    // (and avoid any cyclic dependency)
    try {
      try {
        FSUtils.deletePath(exampleThemePath);
      } catch (NotFoundException e) {
        // do nothing and carry on
      }
      Files.createDirectories(exampleThemePath);
      copyFilesFromClasspath(RodaConstants.THEME_RESOURCES_PATH.replaceFirst("/", ""), exampleThemePath, true);
    } catch (GenericException | IOException e) {
      LOGGER.error("Unable to create " + exampleThemePath, e);
    }
  }

  public static void shutdown() throws IOException {
    if (instantiated) {

      if (nodeType == NodeType.MASTER) {
        solr.close();
        stopApacheDS();
        pluginManager.shutdown();
        transferredResourcesFolderMonitor.stopWatch();
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
        if (TEST_DEPLOY_FOLDER_MONITOR) {
          transferredResourcesFolderMonitor.stopWatch();
        }
        if (TEST_DEPLOY_ORCHESTRATOR) {
          pluginOrchestrator.shutdown();
        }
      }

    }
  }

  public static void startApacheDS() {
    ldap = new ApacheDS();
    rodaApacheDsConfigDirectory = RodaCoreFactory.getConfigPath().resolve(RodaConstants.CORE_LDAP_FOLDER);
    rodaApacheDsDataDirectory = RodaCoreFactory.getDataPath().resolve(RodaConstants.CORE_LDAP_FOLDER);

    try {
      Configuration rodaConfig = RodaCoreFactory.getRodaConfiguration();

      String ldapHost = rodaConfig.getString("ldap.host", RodaConstants.CORE_LDAP_DEFAULT_HOST);
      int ldapPort = rodaConfig.getInt("ldap.port", RodaConstants.CORE_LDAP_DEFAULT_PORT);
      String ldapPeopleDN = rodaConfig.getString("ldap.peopleDN");
      String ldapGroupsDN = rodaConfig.getString("ldap.groupsDN");
      String ldapRolesDN = rodaConfig.getString("ldap.rolesDN");
      String ldapAdminDN = rodaConfig.getString("ldap.adminDN");
      String ldapAdminPassword = rodaConfig.getString("ldap.adminPassword");
      String ldapPasswordDigestAlgorithm = rodaConfig.getString("ldap.passwordDigestAlgorithm");
      List<String> ldapProtectedUsers = RodaUtils.copyList(rodaConfig.getList("ldap.protectedUsers"));
      List<String> ldapProtectedGroups = RodaUtils.copyList(rodaConfig.getList("ldap.protectedGroups"));

      LdapUtility ldapUtility = new LdapUtility(ldapHost, ldapPort, ldapPeopleDN, ldapGroupsDN, ldapRolesDN,
        ldapAdminDN, ldapAdminPassword, ldapPasswordDigestAlgorithm, ldapProtectedUsers, ldapProtectedGroups);

      if (!Files.exists(rodaApacheDsDataDirectory)) {
        Files.createDirectories(rodaApacheDsDataDirectory);
        ldap.initDirectoryService(rodaApacheDsConfigDirectory, rodaApacheDsDataDirectory, ldapAdminPassword);
        ldap.startServer(ldapUtility, ldapPort);
        for (User user : UserUtility.getLdapUtility().getUsers(new Filter())) {
          LOGGER.debug("User to be indexed: {}", user);
          RodaCoreFactory.getModelService().addUser(user, false, true);
        }
        for (Group group : UserUtility.getLdapUtility().getGroups(new Filter())) {
          LOGGER.debug("Group to be indexed: {}", group);
          RodaCoreFactory.getModelService().addGroup(group, false, true);
        }
      } else {
        ldap.instantiateDirectoryService(rodaApacheDsDataDirectory);
        ldap.startServer(ldapUtility, ldapPort);
      }

    } catch (Exception e) {
      LOGGER.error("Error starting up embedded ApacheDS", e);
    }

  }

  public static void startTransferredResourcesFolderMonitor() throws Exception {

    String transferredResourcesFolder = getRodaConfiguration().getString("transferredResources.folder",
      RodaConstants.CORE_TRANSFERREDRESOURCE_FOLDER);
    Path transferredResourcesFolderPath = dataPath.resolve(transferredResourcesFolder);
    if (!Files.exists(transferredResourcesFolderPath)) {
      Files.createDirectories(transferredResourcesFolderPath);
    }
    Date date = getFolderMonitorDate();
    transferredResourcesFolderObserver = new IndexFolderObserver(solr, transferredResourcesFolderPath);
    transferredResourcesFolderMonitor = new FolderMonitorNIO(transferredResourcesFolderPath, date, solr);
    transferredResourcesFolderMonitor.addFolderObserver(transferredResourcesFolderObserver);
    LOGGER.debug("Transferred resources folder monitor is fully initialized? {}", getFolderMonitor()
      .isFullyInitialized());
  }

  public static Date getFolderMonitorDate() {
    Date folderMonitorDate = null;
    try {
      Path dateFile = dataPath.resolve(TRANSFERRED_RESOURCES_LAST_MONITORED_DATE_FILENAME);
      if (Files.exists(dateFile)) {
        String dateFromFile = new String(Files.readAllBytes(dateFile));
        SimpleDateFormat df = new SimpleDateFormat(RodaConstants.SOLRDATEFORMAT);
        folderMonitorDate = df.parse(dateFromFile);
      }
    } catch (IOException | ParseException e) {
      LOGGER.error("Error getting last monitoring date: " + e.getMessage(), e);
    }
    return folderMonitorDate;
  }

  public static void setFolderMonitorDate(Date d) {
    try {
      Path dateFile = dataPath.resolve(TRANSFERRED_RESOURCES_LAST_MONITORED_DATE_FILENAME);
      if (!Files.exists(dateFile)) {
        Files.createFile(dateFile);
      }
      SimpleDateFormat df = new SimpleDateFormat(RodaConstants.SOLRDATEFORMAT);
      String date = df.format(d);
      Files.write(dateFile, date.getBytes());
    } catch (IOException e) {
      LOGGER.error("Error setting last monitoring date: " + e.getMessage(), e);
    }
  }

  public static void stopApacheDS() {
    try {
      ldap.stop();
    } catch (Exception e) {
      LOGGER.error("Error while shutting down ApacheDS embedded server", e);
    }
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
    return pluginManager;
  }

  public static PluginOrchestrator getPluginOrchestrator() {
    return pluginOrchestrator;
  }

  public static int getNumberOfPluginWorkers() {
    int defaultNumberOfWorkers = Runtime.getRuntime().availableProcessors() + 1;

    return getRodaConfiguration().getInt("core.orchestrator.nr_of_workers", defaultNumberOfWorkers);
  }

  public static AkkaDistributedPluginOrchestrator getAkkaDistributedPluginOrchestrator() {
    return akkaDistributedPluginOrchestrator;
  }

  public static FolderMonitorNIO getFolderMonitor() {
    return transferredResourcesFolderMonitor;
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

  public static Path getThemePath() {
    return themePath;
  }

  public static Path getDataPath() {
    return dataPath;
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

  public static Configuration getConfiguration(String configurationFile) throws ConfigurationException {
    Path config = RodaCoreFactory.getConfigPath().resolve(configurationFile);
    PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
    propertiesConfiguration.setDelimiterParsingDisabled(true);
    propertiesConfiguration.setEncoding("UTF-8");

    if (Files.exists(config)) {
      LOGGER.trace("Loading configuration from file {}", config);
      propertiesConfiguration.load(config.toFile());
      RodaPropertiesReloadStrategy rodaPropertiesReloadStrategy = new RodaPropertiesReloadStrategy();
      rodaPropertiesReloadStrategy.setRefreshDelay(5000);
      propertiesConfiguration.setReloadingStrategy(rodaPropertiesReloadStrategy);
    } else {
      InputStream inputStream = RodaCoreFactory.class.getResourceAsStream("/" + RodaConstants.CORE_CONFIG_FOLDER + "/"
        + configurationFile);
      if (inputStream != null) {
        LOGGER.trace("Loading configuration from classpath {}", configurationFile);
        propertiesConfiguration.load(inputStream);
      } else {
        LOGGER.trace("Configuration {} doesn't exist", configurationFile);
      }
    }

    return propertiesConfiguration;
  }

  public static URL getConfigurationFile(String configurationFile) {
    Path config = RodaCoreFactory.getConfigPath().resolve(configurationFile);
    URL configUri;
    if (Files.exists(config)) {
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
    Path config = RodaCoreFactory.getConfigPath().resolve(configurationFile);
    InputStream inputStream = null;
    try {
      if (Files.exists(config)) {
        inputStream = Files.newInputStream(config);
        LOGGER.trace("Loading configuration from file {}", config);
      }
    } catch (IOException e) {
      // do nothing
    }
    if (inputStream == null) {
      inputStream = RodaUtils.class.getResourceAsStream("/" + RodaConstants.CORE_CONFIG_FOLDER + "/"
        + configurationFile);
      LOGGER.trace("Loading configuration from classpath {}", configurationFile);
    }
    return inputStream;
  }

  public static void reloadRodaConfigurationsAfterFileChange() {
    propertiesCache.clear();
    i18nMessages.clear();
    LOGGER.info("Reloaded roda configurations after file change!");
  }

  public static Configuration getRodaConfiguration() {
    return rodaConfiguration;
  }

  private static String getConfigurationKey(String... keyParts) {
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

  public static int getRodaConfigurationAsInt(String... keyParts) {
    return getRodaConfigurationAsInt(0, keyParts);
  }

  public static Set<String> getFilenamesInsideConfigFolder(String folder) throws IOException {

    Set<String> fileNames = new HashSet<>();

    // get from external config
    Set<String> externalFileNames = new HashSet<String>();
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
    List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
    classLoadersList.add(ClasspathHelper.contextClassLoader());
    Set<String> internalFilesPath = new Reflections(new ConfigurationBuilder()
      .filterInputsBy(
        new FilterBuilder().include(FilterBuilder.prefix("" + RodaConstants.CORE_CONFIG_FOLDER + "/" + folder)))
      .setScanners(new ResourcesScanner())
      .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))).getResources(Pattern
      .compile(".*"));
    for (String internalFilePath : internalFilesPath) {
      fileNames.add(Paths.get(internalFilePath).getFileName().toString());
    }

    return fileNames;
  }

  public static Map<String, String> getPropertiesFromCache(String cacheName, List<String> prefixesToCache) {
    if (propertiesCache.get(cacheName) == null) {
      fillInPropertiesToCache(cacheName, prefixesToCache);
    }
    return propertiesCache.get(cacheName);
  }

  private static void fillInPropertiesToCache(String cacheName, List<String> prefixesToCache) {
    if (propertiesCache.get(cacheName) == null) {
      HashMap<String, String> newCacheEntry = new HashMap<String, String>();

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
      propertiesCache.put(cacheName, newCacheEntry);
    }
  }

  public static Messages getI18NMessages(Locale locale) {
    checkForChangesInI18N();
    Messages messages = i18nMessages.get(locale);
    if (messages == null) {
      messages = new Messages(locale, getConfigPath().resolve(RodaConstants.CORE_I18N_FOLDER));
      i18nMessages.put(locale, messages);
    }
    return messages;
  }

  private static void checkForChangesInI18N() {
    // i18n is cached and that cache is re-done when changes occur to
    // roda-*.properties (for convenience)
    getRodaConfiguration().getString("");
  }

  public static DescriptionLevelManager getDescriptionLevelManager() {
    return descriptionLevelManager;
  }

  /*
   * Command-line accessible functionalities
   */
  public static void runReindexAipsPlugin() {
    Plugin<AIP> reindexPlugin = new ReindexAIPPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(reindexPlugin);
  }

  public static void runReindexAipsPlugin(List<String> aipIds) {
    Plugin<AIP> reindexPlugin = new ReindexAIPPlugin();
    ((ReindexAIPPlugin) reindexPlugin).setClearIndexes(false);
    getPluginOrchestrator().runPluginOnAIPs(reindexPlugin, aipIds);
  }

  public static void runRemoveOrphansPlugin(String parentId) {
    try {
      Filter filter = new Filter(new EmptyKeyFilterParameter(RodaConstants.AIP_PARENT_ID));
      RemoveOrphansPlugin removeOrphansPlugin = new RemoveOrphansPlugin();
      removeOrphansPlugin.setNewParent(model.retrieveAIP(parentId));
      getPluginOrchestrator().runPluginFromIndex(IndexedAIP.class, filter, removeOrphansPlugin);
    } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error running remove orphans plugin", e);
    }
  }

  private static void runFixityPlugin() {
    Plugin<AIP> fixityPlugin = new FixityPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(fixityPlugin);
  }

  private static void runAntivirusPlugin() {
    Plugin<AIP> antivirusPlugin = new AntivirusPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(antivirusPlugin);
  }

  private static void runDroidPlugin() {
    Plugin<AIP> droidPlugin = new DroidPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(droidPlugin);
  }

  private static void runFulltextPlugin() {
    Plugin<AIP> fulltextPlugin = new TikaFullTextPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(fulltextPlugin);
  }

  private static void runVeraPDFPlugin(String profile, String hasFeatures) {
    try {
      Plugin<AIP> veraPDFPlugin = new VeraPDFPlugin();
      Map<String, String> params = new HashMap<String, String>();
      params.put("profile", profile);
      params.put("hasFeatures", hasFeatures);
      veraPDFPlugin.setParameterValues(params);
      getPluginOrchestrator().runPluginOnAllAIPs(veraPDFPlugin);
    } catch (InvalidParameterException e) {
      LOGGER.error("Error while running PDF validation plugin", e);
    }
  }

  private static void runPDFtoPDFAPlugin() {
    Plugin<AIP> plugin = new PdfToPdfaPlugin<AIP>();
    getPluginOrchestrator().runPluginOnAllAIPs(plugin);
  }

  private static void runImageMagickConvertPlugin(String inputFormat, String outputFormat) {
    try {

      Plugin<AIP> plugin = new ImageMagickConvertPlugin<AIP>();
      Map<String, String> params = new HashMap<String, String>();
      params.put("inputFormat", "");
      params.put("outputFormat", outputFormat);
      params.put("commandArguments", "");
      plugin.setParameterValues(params);
      getPluginOrchestrator().runPluginOnAIPs(plugin, Arrays.asList(aipId));
    } catch (InvalidParameterException ipe) {
      LOGGER.error(ipe.getMessage(), ipe);
    }
  }

  private static void runImageMagickConvertRepresentationPlugin(String inputFormat, String outputFormat) {
    try {

      Plugin<Representation> plugin = new ImageMagickConvertPlugin<Representation>();
      Map<String, String> params = new HashMap<String, String>();
      params.put("inputFormat", "");
      params.put("outputFormat", outputFormat);
      params.put("commandArguments", "");
      plugin.setParameterValues(params);
      getPluginOrchestrator().runPluginOnAllRepresentations(plugin);
    } catch (InvalidParameterException ipe) {
      LOGGER.error(ipe.getMessage(), ipe);
    }
  }

  private static void runImageMagickConvertFilePlugin(String inputFormat, String outputFormat) {
    try {
      Plugin<File> plugin = new ImageMagickConvertPlugin<File>();
      Map<String, String> params = new HashMap<String, String>();
      params.put("inputFormat", "");
      params.put("outputFormat", outputFormat);
      params.put("commandArguments", "");
      plugin.setParameterValues(params);
      getPluginOrchestrator().runPluginOnAllFiles(plugin);
    } catch (InvalidParameterException ipe) {
      LOGGER.error(ipe.getMessage(), ipe);
    }
  }

  private static void runSoxConvertPlugin(String inputFormat, String outputFormat) {
    try {
      Plugin<AIP> plugin = new SoxConvertPlugin<AIP>();
      Map<String, String> params = new HashMap<String, String>();
      params.put("inputFormat", "");
      params.put("outputFormat", outputFormat);
      params.put("commandArguments", "");
      plugin.setParameterValues(params);
      getPluginOrchestrator().runPluginOnAIPs(plugin, Arrays.asList(aipId));
    } catch (InvalidParameterException ipe) {
      LOGGER.error(ipe.getMessage(), ipe);
    }
  }

  private static void runSoxConvertRepresentationPlugin(String inputFormat, String outputFormat) {
    try {
      Plugin<Representation> plugin = new SoxConvertPlugin<Representation>();
      Map<String, String> params = new HashMap<String, String>();
      params.put("inputFormat", "");
      params.put("outputFormat", outputFormat);
      params.put("commandArguments", "");
      plugin.setParameterValues(params);
      getPluginOrchestrator().runPluginOnAllRepresentations(plugin);
    } catch (InvalidParameterException ipe) {
      LOGGER.error(ipe.getMessage(), ipe);
    }
  }

  private static void runGhostScriptConvertPlugin(String inputFormat, String outputFormat) {
    try {
      Plugin<AIP> plugin = new GhostScriptConvertPlugin<AIP>();
      Map<String, String> params = new HashMap<String, String>();
      params.put("inputFormat", "");
      params.put("outputFormat", outputFormat);
      params.put("commandArguments", "");
      plugin.setParameterValues(params);
      getPluginOrchestrator().runPluginOnAIPs(plugin, Arrays.asList(aipId));
    } catch (InvalidParameterException ipe) {
      LOGGER.error(ipe.getMessage(), ipe);
    }
  }

  private static void runUnoconvConvertPlugin(String inputFormat, String outputFormat) {
    try {
      Plugin<AIP> plugin = new UnoconvConvertPlugin<AIP>();
      Map<String, String> params = new HashMap<String, String>();
      params.put("inputFormat", "");
      params.put("outputFormat", outputFormat);
      params.put("commandArguments", "");
      plugin.setParameterValues(params);
      getPluginOrchestrator().runPluginOnAIPs(plugin, Arrays.asList(aipId));
    } catch (InvalidParameterException ipe) {
      LOGGER.error(ipe.getMessage(), ipe);
    }
  }

  private static void runUnoconvConvertRepresentationPlugin(String inputFormat, String outputFormat) {
    try {
      Plugin<Representation> plugin = new UnoconvConvertPlugin<Representation>();
      Map<String, String> params = new HashMap<String, String>();
      params.put("inputFormat", "");
      params.put("outputFormat", outputFormat);
      params.put("commandArguments", "");
      plugin.setParameterValues(params);
      getPluginOrchestrator().runPluginOnAllRepresentations(plugin);
    } catch (InvalidParameterException ipe) {
      LOGGER.error(ipe.getMessage(), ipe);
    }
  }

  private static void runAvconvConvertPlugin(String inputFormat, String outputFormat) {
    try {
      Plugin<Representation> plugin = new AvconvConvertPlugin<Representation>();
      Map<String, String> params = new HashMap<String, String>();
      params.put("inputFormat", "");
      params.put("outputFormat", outputFormat);
      params.put("commandArguments", "");
      plugin.setParameterValues(params);
      getPluginOrchestrator().runPluginOnAllRepresentations((Plugin<Representation>) plugin);
    } catch (InvalidParameterException ipe) {
      LOGGER.error(ipe.getMessage(), ipe);
    }
  }

  private static void runGeneralCommandConvertPlugin(String inputFormat, String outputFormat) {
    try {
      Plugin<AIP> plugin = new GeneralCommandConvertPlugin<AIP>();
      Map<String, String> params = new HashMap<String, String>();
      params.put("inputFormat", "png");
      params.put("outputFormat", "tiff");
      params.put("commandArguments", "/usr/bin/convert -regard-warnings {input_file} {output_file}");
      plugin.setParameterValues(params);
      getPluginOrchestrator().runPluginOnAIPs(plugin, Arrays.asList(aipId));
    } catch (InvalidParameterException ipe) {
      LOGGER.error(ipe.getMessage(), ipe);
    }
  }

  private static void runGeneralCommandConvertRepresentationPlugin(String inputFormat, String outputFormat) {
    try {
      Plugin<Representation> plugin = new GeneralCommandConvertPlugin<Representation>();
      Map<String, String> params = new HashMap<String, String>();
      params.put("inputFormat", "png");
      params.put("outputFormat", "tiff");
      params.put("commandArguments", "/usr/bin/convert -regard-warnings {input_file} {output_file}");
      plugin.setParameterValues(params);
      getPluginOrchestrator().runPluginOnAllRepresentations(plugin);
    } catch (InvalidParameterException ipe) {
      LOGGER.error(ipe.getMessage(), ipe);
    }
  }

  private static void runDigitalSignaturePlugin(String doVerify, String doExtract, String doStrip,
    String verificationAffectsOnOutcome) {
    try {
      Plugin<Representation> plugin = new DigitalSignaturePlugin();
      Map<String, String> params = new HashMap<String, String>();
      params.put("doVerify", doVerify);
      params.put("doExtract", doExtract);
      params.put("doStrip", doStrip);
      params.put("verificationAffectsOnOutcome", verificationAffectsOnOutcome);
      plugin.setParameterValues(params);
      getPluginOrchestrator().runPluginOnAllRepresentations(plugin);
    } catch (InvalidParameterException ipe) {
      LOGGER.error(ipe.getMessage(), ipe);
    }
  }

  private static void runDigitalSignatureDIPPlugin() {
    Plugin<Representation> plugin = new DigitalSignatureDIPPlugin();
    getPluginOrchestrator().runPluginOnAllRepresentations(plugin);
  }

  private static void runFileFormatPlugin() {
    Plugin<Representation> plugin = new FileFormatPlugin();
    getPluginOrchestrator().runPluginOnAllRepresentations(plugin);
  }

  private static void runReindexingPlugins() {
    try {
      Plugin<AIP> psp = new PremisSkeletonPlugin();
      Plugin<AIP> sfp = new SiegfriedPlugin();
      Plugin<AIP> ttp = new TikaFullTextPlugin();

      Map<String, String> params = new HashMap<String, String>();
      params.put(RodaConstants.PLUGIN_PARAMS_CREATES_PLUGIN_EVENT, "false");
      psp.setParameterValues(params);
      sfp.setParameterValues(params);
      ttp.setParameterValues(params);

      getPluginOrchestrator().runPluginOnAIPs(psp, Arrays.asList(aipId));
      getPluginOrchestrator().runPluginOnAIPs(sfp, Arrays.asList(aipId));
      getPluginOrchestrator().runPluginOnAIPs(ttp, Arrays.asList(aipId));

      model.notifyAIPUpdated(aipId);

    } catch (InvalidParameterException | RequestNotValidException | GenericException | NotFoundException
      | AuthorizationDeniedException ipe) {
      LOGGER.error(ipe.getMessage(), ipe);
    }
  }

  private static void runJhovePlugin() {
    Plugin<AIP> jhovePlugin = new JHOVEPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(jhovePlugin);
  }

  private static void runFitsPlugin() {
    Plugin<AIP> fitsPlugin = new FITSPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(fitsPlugin);
  }

  private static void runBagitPlugin() {
    // Path bagitFolder = RodaCoreFactory.getDataPath().resolve("bagit");
    Plugin<TransferredResource> bagitPlugin = new BagitToAIPPlugin();
    // FIXME collect proper list of transferred resources
    List<TransferredResource> bagitsList = new ArrayList<TransferredResource>();
    // Stream<Path> bagits = Files.list(bagitFolder);
    // List<Path> bagitsList = bagits.collect(Collectors.toList());
    // bagits.close();
    getPluginOrchestrator().runPluginOnTransferredResources(bagitPlugin, bagitsList);
  }

  private static void runPremisSkeletonPlugin() {
    Plugin<AIP> premisSkeletonPlugin = new PremisSkeletonPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(premisSkeletonPlugin);
  }

  private static void runValidationPlugin(String premis, String metadataType) {
    try {
      Plugin<AIP> validationPlugin = new DescriptiveMetadataValidationPlugin();
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("parameter.validate_premis", premis);
      parameters.put("parameter.metadata_type", metadataType);
      validationPlugin.setParameterValues(parameters);
      getPluginOrchestrator().runPluginOnAllAIPs(validationPlugin);
    } catch (Exception e) {
      LOGGER.error("Error while running validation plugin", e);
    }
  }

  private static void runLogCleanPlugin() {
    Plugin<LogEntry> logCleanPlugin = new LogCleanerPlugin();
    getPluginOrchestrator().runPlugin(logCleanPlugin);
  }

  private static void runExifToolPlugin() {
    Plugin<AIP> exifToolPlugin = new ExifToolPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(exifToolPlugin);
  }

  private static void runMediaInfoPlugin() {
    Plugin<AIP> mediaInfoPlugin = new MediaInfoPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(mediaInfoPlugin);
  }

  private static void runFFProbePlugin() {
    Plugin<AIP> ffProbePlugin = new FFProbePlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(ffProbePlugin);
  }

  private static void runJpylyzerPlugin() {
    Plugin<AIP> jpylyzerPlugin = new JpylyzerPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(jpylyzerPlugin);
  }

  private static void runSiegfriedPlugin() {
    Plugin<AIP> siegfriedPlugin = new SiegfriedPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(siegfriedPlugin);
  }

  private static void runEARKPlugin() {
    try {
      EARKSIPToAIPPlugin eark = new EARKSIPToAIPPlugin();
      eark.setParameterValues(new HashMap<String, String>());
      List<TransferredResource> transferredResourceList = new ArrayList<TransferredResource>();
      EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
      Files.walkFileTree(Paths.get("/home/sleroux/earksip"), opts, Integer.MAX_VALUE, new FileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          TransferredResource tr = new TransferredResource();
          tr.setFullPath(file.toAbsolutePath().toString());
          transferredResourceList.add(tr);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          return FileVisitResult.CONTINUE;
        }
      });
      getPluginOrchestrator().runPluginOnTransferredResources(eark, transferredResourceList);
    } catch (InvalidParameterException | IOException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  private static void runTransferredResourceToAIPPlugin() {
    try {
      TransferredResourceToAIPPlugin converter = new TransferredResourceToAIPPlugin();
      converter.setParameterValues(new HashMap<String, String>());
      List<TransferredResource> transferredResourceList = new ArrayList<TransferredResource>();
      // TransferredResource tr = new TransferredResource();
      // tr.setFullPath("/home/sleroux/Fonts");
      // tr.setName("Fonts");
      // transferredResourceList.add(tr);
      getPluginOrchestrator().runPluginOnTransferredResources(converter, transferredResourceList);
    } catch (InvalidParameterException e) {
      LOGGER.error("Error while running Transferred resources to AIP plugin", e);
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
      e.printStackTrace();
    }
  }

  private static void printMainUsage() {
    System.err.println("Syntax:");
    System.err.println("java -jar x.jar index reindex");
    System.err.println("java -jar x.jar index list users|groups|sips|file");
    System.err.println("java -jar x.jar orphans [newParentID]");
    System.err.println("java -jar x.jar fixity");
    System.err.println("java -jar x.jar antivirus");
    System.err.println("java -jar x.jar premisskeleton");
    System.err.println("java -jar x.jar droid");
    System.err.println("java -jar x.jar fulltext");
    System.err.println("java -jar x.jar jhove");
    System.err.println("java -jar x.jar fits");
    System.err.println("java -jar x.jar validation");
    System.err.println("java -jar x.jar logClean");
    System.err.println("java -jar x.jar exifTool");
    System.err.println("java -jar x.jar mediaInfo");
    System.err.println("java -jar x.jar ffprobe");
    System.err.println("java -jar x.jar jpylyzer");
    System.err.println("java -jar x.jar premisupdate");
    System.err.println("java -jar x.jar fastcharacterization");
    System.err.println("java -jar x.jar eark");
    System.err.println("java -jar x.jar siegfried");
    System.err.println("java -jar x.jar verapdf");
    System.err.println("java -jar x.jar pdftopdfa");
    System.err.println("java -jar x.jar imagemagickconvert");
    System.err.println("java -jar x.jar soxconvert");
    System.err.println("java -jar x.jar ffmpegconvert");
    System.err.println("java -jar x.jar jodconverter");
    System.err.println("java -jar x.jar ghostscriptconvert");
    System.err.println("java -jar x.jar mencoderconvert");
    System.err.println("java -jar x.jar unoconvconvert");
    System.err.println("java -jar x.jar generalcommandconvert");
    System.err.println("java -jar x.jar digitalsignature");
    System.err.println("java -jar x.jar fileformat");
  }

  private static void printIndexMembers(List<String> args, Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    System.out.println("index list " + args.get(2));
    IndexResult<RODAMember> users = index.find(RODAMember.class, filter, sorter, sublist, facets);
    for (RODAMember rodaMember : users.getResults()) {
      System.out.println("\t" + rodaMember);
    }
  }

  private static void printCountSips(Sorter sorter, Sublist sublist, Facets facets) throws GenericException,
    RequestNotValidException {
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

  private static void printFiles(Sorter sorter, Sublist sublist, Facets facets) throws GenericException,
    RequestNotValidException {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_SEARCH, "OLA-OLÁ-1234-XXXX_K"));
    IndexResult<IndexedFile> res = index.find(IndexedFile.class, filter, sorter, sublist);

    for (IndexedFile sf : res.getResults()) {
      System.out.println(sf.toString());
    }
  }

  private static void printEvents(Sorter sorter, Sublist sublist, Facets facets) throws GenericException,
    RequestNotValidException {
    Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_TYPE, "format identification"));
    IndexResult<IndexedPreservationEvent> res = index.find(IndexedPreservationEvent.class, filter, sorter, sublist);

    for (IndexedPreservationEvent ipe : res.getResults()) {
      System.out.println(ipe.toString());
    }
  }

  private static void printAgents(Sorter sorter, Sublist sublist, Facets facets) throws GenericException,
    RequestNotValidException {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.PRESERVATION_AGENT_TYPE,
      PreservationAgentType.SOFTWARE.toString()));
    IndexResult<IndexedPreservationAgent> res = index.find(IndexedPreservationAgent.class, filter, sorter, sublist);

    for (IndexedPreservationAgent ipa : res.getResults()) {
      System.out.println(ipa.toString());
    }
  }

  private static void mainMasterTasks(List<String> args) throws GenericException, RequestNotValidException {
    if ("index".equals(args.get(0))) {
      if ("list".equals(args.get(1)) && ("users".equals(args.get(2)) || "groups".equals(args.get(2)))) {
        Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER,
          "users".equals(args.get(2)) ? "true" : "false"));
        printIndexMembers(args, filter, null, new Sublist(0, 10000), null);
      } else if ("list".equals(args.get(1)) && ("sips".equals(args.get(2)))) {
        printCountSips(null, new Sublist(0, 10000), null);
      } else if ("list".equals(args.get(1)) && ("file".equals(args.get(2)))) {
        printFiles(null, new Sublist(0, 10000), null);
      } else if ("list".equals(args.get(1)) && ("event".equals(args.get(2)))) {
        printEvents(null, new Sublist(0, 10000), null);
      } else if ("list".equals(args.get(1)) && ("agent".equals(args.get(2)))) {
        printAgents(null, new Sublist(0, 10000), null);
      } else if ("reindex".equals(args.get(1)) && args.size() == 2) {
        runReindexAipsPlugin();
      } else if ("reindex".equals(args.get(1)) && args.size() >= 3) {
        runReindexAipsPlugin(args.subList(2, args.size()));
      } else if ("query".equals(args.get(1)) && args.size() == 4 && StringUtils.isNotBlank(args.get(2))
        && StringUtils.isNotBlank(args.get(3))) {
        runSolrQuery(args);
      }
    } else if ("orphans".equals(args.get(0)) && args.size() == 2 && StringUtils.isNotBlank(args.get(1))) {
      runRemoveOrphansPlugin(args.get(1));
    } else if ("fixity".equals(args.get(0))) {
      runFixityPlugin();
    } else if ("antivirus".equals(args.get(0))) {
      runAntivirusPlugin();
    } else if ("premisskeleton".equals(args.get(0))) {
      runPremisSkeletonPlugin();
    } else if ("droid".equals(args.get(0))) {
      runDroidPlugin();
    } else if ("fulltext".equals(args.get(0))) {
      runFulltextPlugin();
    } else if ("verapdf".equals(args.get(0))) {
      runVeraPDFPlugin(args.get(1), args.get(2));
    } else if ("pdftopdfa".equals(args.get(0))) {
      runPDFtoPDFAPlugin();
    } else if ("imagemagickconvert".equals(args.get(0))) {
      runImageMagickConvertPlugin(args.get(1), args.get(2));
    } else if ("imagemagickrepresentationconvert".equals(args.get(0))) {
      runImageMagickConvertRepresentationPlugin(args.get(1), args.get(2));
    } else if ("imagemagickfileconvert".equals(args.get(0))) {
      runImageMagickConvertFilePlugin(args.get(1), args.get(2));
    } else if ("soxconvert".equals(args.get(0))) {
      runSoxConvertPlugin(args.get(1), args.get(2));
    } else if ("soxrepresentationconvert".equals(args.get(0))) {
      runSoxConvertRepresentationPlugin(args.get(1), args.get(2));
    } else if ("ghostscriptconvert".equals(args.get(0))) {
      runGhostScriptConvertPlugin(args.get(1), args.get(2));
    } else if ("unoconvconvert".equals(args.get(0))) {
      runUnoconvConvertPlugin(args.get(1), args.get(2));
    } else if ("unoconvrepresentationconvert".equals(args.get(0))) {
      runUnoconvConvertRepresentationPlugin(args.get(1), args.get(2));
    } else if ("avconvconvert".equals(args.get(0))) {
      runAvconvConvertPlugin(args.get(1), args.get(2));
    } else if ("generalcommandconvert".equals(args.get(0))) {
      runGeneralCommandConvertPlugin(args.get(1), args.get(2));
    } else if ("generalcommandrepresentationconvert".equals(args.get(0))) {
      runGeneralCommandConvertRepresentationPlugin(args.get(1), args.get(2));
    } else if ("digitalsignature".equals(args.get(0))) {
      runDigitalSignaturePlugin(args.get(1), args.get(2), args.get(3), args.get(4));
    } else if ("digitalsignaturedip".equals(args.get(0))) {
      runDigitalSignatureDIPPlugin();
    } else if ("fileformat".equals(args.get(0))) {
      runFileFormatPlugin();
    } else if ("reindexer".equals(args.get(0))) {
      runReindexingPlugins();
    } else if ("jhove".equals(args.get(0))) {
      runJhovePlugin();
    } else if ("fits".equals(args.get(0))) {
      runFitsPlugin();
    } else if ("bagit".equals(args.get(0))) {
      runBagitPlugin();
    } else if ("validation".equals(args.get(0))) {
      runValidationPlugin(args.get(1), args.get(2));
    } else if ("logClean".equals(args.get(0))) {
      runLogCleanPlugin();
    } else if ("exifTool".equals(args.get(0))) {
      runExifToolPlugin();
    } else if ("mediaInfo".equals(args.get(0))) {
      runMediaInfoPlugin();
    } else if ("ffprobe".equals(args.get(0))) {
      runFFProbePlugin();
    } else if ("jpylyzer".equals(args.get(0))) {
      runJpylyzerPlugin();
    } else if ("eark".equals(args.get(0))) {
      runEARKPlugin();
    } else if ("transferredResource".equals(args.get(0))) {
      runTransferredResourceToAIPPlugin();
    } else if ("siegfried".equals(args.get(0))) {
      runSiegfriedPlugin();
    } else {
      printMainUsage();
    }
  }

  public static void main(String[] argsArray) throws InterruptedException, GenericException, RequestNotValidException {
    List<String> args = Arrays.asList(argsArray);

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
