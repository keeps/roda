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
import java.nio.charset.Charset;
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
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

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
import org.roda.core.common.LdapUtilityException;
import org.roda.core.common.Messages;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.UserUtility;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.monitor.TransferUpdateStatus;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.common.validation.ResourceResolver;
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
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.RoleAlreadyExistsException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.SolrUtils;
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
import org.xml.sax.SAXException;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

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

  // Core related objects
  private static Path rodaHomePath;
  private static Path storagePath;
  private static Path indexDataPath;
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

  // Orchestrator related objects
  private static PluginManager pluginManager;
  // FIXME we should have only "one" orchestrator
  private static PluginOrchestrator pluginOrchestrator;
  private static AkkaDistributedPluginOrchestrator akkaDistributedPluginOrchestrator;
  private static AkkaDistributedPluginWorker akkaDistributedPluginWorker;
  private static boolean FEATURE_DISTRIBUTED_AKKA = false;
  private static Config akkaConfig;

  // ApacheDS related objects
  private static ApacheDS ldap;
  private static Path rodaApacheDSDataDirectory = null;

  // TransferredResources related objects
  private static TransferredResourcesScanner transferredResourcesScanner;

  // Configuration related objects
  private static CompositeConfiguration rodaConfiguration = null;
  private static List<String> configurationFiles = null;
  private static Map<String, Map<String, String>> rodaPropertiesCache = null;
  private static Map<String, Schema> rodaSchemasCache = new HashMap<String, Schema>();
  private static Map<Locale, Messages> i18nMessages = new HashMap<Locale, Messages>();
  private static DescriptionLevelManager descriptionLevelManager = null;

  /** Private empty constructor */
  private RodaCoreFactory() {

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
        configurationFiles = new ArrayList<String>();
        rodaPropertiesCache = new HashMap<String, Map<String, String>>();
        addConfiguration("roda-core.properties");
        addConfiguration("roda-core-formats.properties");
        LOGGER.debug("Finished loading roda-core.properties & roda-core-formats.properties");
        addConfiguration("roda-roles.properties");
        LOGGER.debug("Finished loading roda-roles.properties");

        // initialize working directory
        initializeWorkingDirectory();

        // initialize reports directory
        initializeReportsDirectory();

        // instantiate storage and model service
        instantiateStorageAndModel();
        LOGGER.debug("Finished instantiating storage & model");

        // instantiate solr and index service
        instantiateSolrAndIndexService();
        LOGGER.debug("Finished instantiating solr & index");

        // load description level information
        loadDescriptionLevelInformation();
        LOGGER.debug("Finished loading description levels");

        // instantiate plugin manager
        instantiatePluginManager();
        LOGGER.debug("Finished instantiating plugin manager");

        instantiateNodeSpecificObjects(nodeType);
        LOGGER.debug("Finished instantiating node specific objects");

        instantiateDefaultObjects();
        LOGGER.debug("Finished instantiating default objects");

        instantiated = true;

      } catch (ConfigurationException e) {
        LOGGER.error("Error loading roda properties", e);
        instantiatedWithoutErrors = false;
      } catch (URISyntaxException e) {
        LOGGER.error("Error instantiating solr/index model", e);
        instantiatedWithoutErrors = false;
      } catch (GenericException e) {
        LOGGER.error("Error instantiating storage model", e);
        instantiatedWithoutErrors = false;
      } catch (Exception e) {
        LOGGER.error("Error instantiating " + RodaCoreFactory.class.getSimpleName(), e);
        instantiatedWithoutErrors = false;
      }

      // last log message that state if system was loaded without errors or not
      LOGGER.info("RODA Core loading completed {}", (instantiatedWithoutErrors ? "with success!"
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
    exampleConfigPath = rodaHomePath.resolve(RodaConstants.CORE_EXAMPLE_CONFIG_FOLDER);
    defaultPath = rodaHomePath.resolve(RodaConstants.CORE_DEFAULT_FOLDER);
    dataPath = rodaHomePath.resolve(RodaConstants.CORE_DATA_FOLDER);
    logPath = dataPath.resolve(RodaConstants.CORE_LOG_FOLDER);
    storagePath = dataPath.resolve(RodaConstants.CORE_STORAGE_FOLDER);
    indexDataPath = dataPath.resolve(RodaConstants.CORE_INDEX_FOLDER);

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

  private static void instantiateEssentialDirectories(NodeType nodeType) {
    List<Path> essentialDirectories = new ArrayList<Path>();
    essentialDirectories.add(configPath);
    essentialDirectories.add(rodaHomePath.resolve(RodaConstants.CORE_LOG_FOLDER));
    essentialDirectories.add(dataPath);
    essentialDirectories.add(logPath);
    essentialDirectories.add(storagePath);
    essentialDirectories.add(indexDataPath);
    essentialDirectories.add(exampleConfigPath);

    for (Path path : essentialDirectories) {
      try {
        if (!Files.exists(path)) {
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
        try {
          FSUtils.deletePath(exampleConfigPath);
        } catch (NotFoundException e) {
          // do nothing and carry on
        }
        Files.createDirectories(exampleConfigPath);
        copyFilesFromClasspath(RodaConstants.CORE_CONFIG_FOLDER + "/", exampleConfigPath, true,
          Arrays.asList(RodaConstants.CORE_CONFIG_FOLDER + "/" + RodaConstants.CORE_LDAP_FOLDER,
            RodaConstants.CORE_CONFIG_FOLDER + "/" + RodaConstants.CORE_INDEX_FOLDER));
      } catch (GenericException | IOException e) {
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
          // copied
          // to the file system and storage can be of a different type (e.g.
          // fedora)
          FileStorageService fileStorageService = new FileStorageService(storagePath);

          index.reindexRisks(fileStorageService);
          index.reindexFormats(fileStorageService);
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
      Collections.EMPTY_LIST);
  }

  private static void copyFilesFromClasspath(String classpathPrefix, Path destinationDirectory,
    boolean removeClasspathPrefixFromFinalPath, List<String> excludePaths) {

    List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
    classLoadersList.add(ClasspathHelper.contextClassLoader());

    Reflections reflections = new Reflections(
      new ConfigurationBuilder().filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(classpathPrefix)))
        .setScanners(new ResourcesScanner())
        .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[] {}))));

    Set<String> resources = reflections.getResources(Pattern.compile(".*"));

    LOGGER.info("Copy files from classpath prefix={}, destination={}, removePrefix={}, excludePaths={}, #resources={}",
      classpathPrefix, destinationDirectory, removeClasspathPrefixFromFinalPath, excludePaths, resources.size());

    for (String resource : resources) {
      boolean exclude = false;
      for (String excludePath : excludePaths) {
        if (resource.startsWith(excludePath)) {
          exclude = true;
          break;
        }
      }
      if (exclude) {
        continue;
      }

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
        IOUtils.closeQuietly(originStream);
      }
    }
  }

  private static void copyFilesFromClasspath(String classpathPrefix, Path destinationDirectory) {
    copyFilesFromClasspath(classpathPrefix, destinationDirectory, false);
  }

  private static void instantiatePluginManager() {
    if (nodeType == NodeType.MASTER || nodeType == NodeType.WORKER || TEST_DEPLOY_PLUGIN_MANAGER) {
      try {
        pluginManager = PluginManager.getDefaultPluginManager(getConfigPath(), getPluginsPath());
      } catch (PluginManagerException e) {
        LOGGER.error("Error instantiating PluginManager", e);
        instantiatedWithoutErrors = false;
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
      instantiatedWithoutErrors = false;
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
    if (nodeType == NodeType.MASTER) {
      Path solrHome = configPath.resolve(RodaConstants.CORE_INDEX_FOLDER);
      if (!Files.exists(solrHome) || FEATURE_OVERRIDE_INDEX_CONFIGS) {
        try {
          Path tempConfig = Files.createTempDirectory(getWorkingDirectory(), RodaConstants.CORE_INDEX_FOLDER);
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

  private static SolrClient instantiateSolr(Path solrHome) {
    SolrType solrType = SolrType.valueOf(
      getRodaConfiguration().getString(RodaConstants.CORE_SOLR_TYPE, RodaConstants.DEFAULT_SOLR_TYPE.toString()));

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
    System.setProperty("solr.data.dir.agent", indexDataPath.resolve(RodaConstants.CORE_AGENT_FOLDER).toString());
    System.setProperty("solr.data.dir.format", indexDataPath.resolve(RodaConstants.CORE_FORMAT_FOLDER).toString());
    System.setProperty("solr.data.dir.notification",
      indexDataPath.resolve(RodaConstants.CORE_NOTIFICATION_FOLDER).toString());
    System.setProperty("solr.data.dir.riskincidence",
      indexDataPath.resolve(RodaConstants.CORE_RISKINCIDENCE_FOLDER).toString());
  }

  private static void instantiateNodeSpecificObjects(NodeType nodeType) {
    // FIXME 20160531 hsilva: this should be moved to somewhere closely to
    // Akka instantiation code
    loadAkkaConfiguration();

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

  private static void loadAkkaConfiguration() {
    InputStream originStream = RodaCoreFactory.class.getClassLoader().getResourceAsStream(
      RodaConstants.CORE_CONFIG_FOLDER + "/" + RodaConstants.CORE_ORCHESTRATOR_FOLDER + "/application.conf");

    try {
      String configAsString = IOUtils.toString(originStream, Charset.forName(RodaConstants.DEFAULT_ENCODING));
      akkaConfig = ConfigFactory.parseString(configAsString);
    } catch (IOException e) {
      LOGGER.error("Could not load Akka configuration", e);
    } finally {
      IOUtils.closeQuietly(originStream);
    }

  }

  public static Config getAkkaConfiguration() {
    return akkaConfig;
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

    }
  }

  public static void startApacheDS() {
    ldap = new ApacheDS();
    rodaApacheDSDataDirectory = RodaCoreFactory.getDataPath().resolve(RodaConstants.CORE_LDAP_FOLDER);

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

      if (!Files.exists(rodaApacheDSDataDirectory)) {
        Files.createDirectories(rodaApacheDSDataDirectory);
        List<InputStream> ldifs = new ArrayList<>();
        ldifs.add(RodaCoreFactory.getConfigurationFileAsStream(RodaConstants.CORE_LDAP_FOLDER + "/users.ldif"));
        ldifs.add(RodaCoreFactory.getConfigurationFileAsStream(RodaConstants.CORE_LDAP_FOLDER + "/groups.ldif"));
        ldifs.add(RodaCoreFactory.getConfigurationFileAsStream(RodaConstants.CORE_LDAP_FOLDER + "/roles.ldif"));
        ldap.initDirectoryService(rodaApacheDSDataDirectory, ldapAdminPassword, ldifs);
        ldap.startServer(ldapUtility, ldapPort);
        indexUsersAndGroupsFromLDAP();
      } else {
        ldap.instantiateDirectoryService(rodaApacheDSDataDirectory);
        ldap.startServer(ldapUtility, ldapPort);
      }

      createRoles(rodaConfig, ldapUtility);

    } catch (Exception e) {
      LOGGER.error("Error starting up embedded ApacheDS", e);
      instantiatedWithoutErrors = false;
    }

  }

  /**
   * For each role in roda-roles.properties create the role in LDAP if it don't
   * exist already.
   * 
   * @param rodaConfig
   *          roda configuration
   * @param ldapUtility
   *          LDAP utility class.
   * @throws GenericException
   *           if something unexpected happens creating roles.
   */
  private static void createRoles(Configuration rodaConfig, LdapUtility ldapUtility) throws GenericException {
    final Iterator<String> keys = rodaConfig.getKeys("core.roles");
    final Set<String> roles = new HashSet<>();
    while (keys.hasNext()) {
      roles.addAll(Arrays.asList(rodaConfig.getStringArray(keys.next())));
    }
    for (final String role : roles) {
      try {
        ldapUtility.addRole(role);
        LOGGER.info("Created LDAP role {}", role);
      } catch (RoleAlreadyExistsException e) {
        LOGGER.info("Role {} already exists.", role);
      } catch (LdapUtilityException e) {
        throw new GenericException("Error creating role '" + role + "'", e);
      }
    }
  }

  private static void indexUsersAndGroupsFromLDAP()
    throws LdapUtilityException, GenericException, EmailAlreadyExistsException, UserAlreadyExistsException,
    IllegalOperationException, NotFoundException, AlreadyExistsException {
    for (User user : UserUtility.getLdapUtility().getUsers(new Filter())) {
      LOGGER.debug("User to be indexed: {}", user);
      RodaCoreFactory.getModelService().addUser(user, false, true);
    }
    for (Group group : UserUtility.getLdapUtility().getGroups(new Filter())) {
      LOGGER.debug("Group to be indexed: {}", group);
      RodaCoreFactory.getModelService().addGroup(group, false, true);
    }
  }

  public static void instantiateTransferredResourcesScanner() {
    try {
      String transferredResourcesFolder = getRodaConfiguration().getString("transferredResources.folder",
        RodaConstants.CORE_TRANSFERREDRESOURCE_FOLDER);
      Path transferredResourcesFolderPath = dataPath.resolve(transferredResourcesFolder);
      if (!Files.exists(transferredResourcesFolderPath)) {
        Files.createDirectories(transferredResourcesFolderPath);
      }

      transferredResourcesScanner = new TransferredResourcesScanner(transferredResourcesFolderPath, getIndexService());
    } catch (Exception e) {
      LOGGER.error("Error starting Transferred Resources Scanner: " + e.getMessage(), e);
      instantiatedWithoutErrors = false;
    }
  }

  public static boolean getTransferredResourcesScannerUpdateStatus() {
    return TransferUpdateStatus.getInstance().isUpdatingStatus();
  }

  public static void setTransferredResourcesScannerUpdateStatus(boolean isUpdating) {
    TransferUpdateStatus.getInstance().setUpdatingStatus(isUpdating);
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
      InputStream inputStream = RodaCoreFactory.class
        .getResourceAsStream("/" + RodaConstants.CORE_CONFIG_FOLDER + "/" + configurationFile);
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
    if (Files.exists(config) && !Files.isDirectory(config)
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
      if (Files.exists(config) && !Files.isDirectory(config)
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

  public static InputStream getDefaultFileAsStream(String defaultFile) {
    Path defaultPath = getDefaultPath().resolve(defaultFile);
    InputStream inputStream = null;
    try {
      if (Files.exists(defaultPath) && !Files.isDirectory(defaultPath)
        && defaultPath.toAbsolutePath().startsWith(getDefaultPath().toAbsolutePath().toString())) {
        inputStream = Files.newInputStream(defaultPath);
        LOGGER.trace("Loading default from file {}", defaultPath);
      }
    } catch (IOException e) {
      // do nothing
    }
    if (inputStream == null) {
      inputStream = RodaCoreFactory.class
        .getResourceAsStream("/" + RodaConstants.CORE_DEFAULT_FOLDER + "/" + defaultFile);
      LOGGER.trace("Loading default file from classpath {}", defaultFile);
    }
    return inputStream;
  }

  public static void clearRodaCachableObjectsAfterConfigurationChange() {
    rodaPropertiesCache.clear();
    rodaSchemasCache.clear();
    i18nMessages.clear();
    LOGGER.info("Reloaded roda configurations after file change!");
  }

  public static Optional<Schema> getRodaSchema(String metadataType, String metadataVersion) {
    Optional<Schema> schema = Optional.empty();
    InputStream schemaStream = null;

    String type = metadataType != null ? metadataType.toLowerCase() : "";
    String version = metadataVersion != null ? metadataVersion.toLowerCase() : "";
    String schemaPathWithVersion = RodaConstants.CORE_SCHEMAS_FOLDER + "/" + type
      + RodaConstants.METADATA_VERSION_SEPARATOR + version + ".xsd";
    String schemaPathWithoutVersion = RodaConstants.CORE_SCHEMAS_FOLDER + "/" + type + ".xsd";

    if (rodaSchemasCache.containsKey(schemaPathWithVersion)) {
      schema = Optional.ofNullable(rodaSchemasCache.get(schemaPathWithVersion));
    } else if (rodaSchemasCache.containsKey(schemaPathWithoutVersion)) {
      schema = Optional.ofNullable(rodaSchemasCache.get(schemaPathWithoutVersion));
    } else {

      if (!"".equals(type)) {
        boolean withVersion = true;
        synchronized (rodaSchemasCache) {
          if (!"".equals(version)) {
            LOGGER.debug("Trying to load XML Schema '{}'", schemaPathWithVersion);
            schemaStream = RodaCoreFactory.getConfigurationFileAsStream(schemaPathWithVersion);
          }

          if (schemaStream == null) {
            LOGGER.debug("Trying to load XML Schema '{}'", schemaPathWithoutVersion);
            schemaStream = RodaCoreFactory.getConfigurationFileAsStream(schemaPathWithoutVersion);
            withVersion = false;
          }

          if (schemaStream != null) {
            // FIXME 20160531 hsilva: what about 1.1 xsds???
            SchemaFactory schemaFactory = SchemaFactory.newInstance(RodaConstants.W3C_XML_SCHEMA_NS_URI);
            schemaFactory.setResourceResolver(new ResourceResolver());
            try {
              Schema xmlSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
              rodaSchemasCache.put(withVersion ? schemaPathWithVersion : schemaPathWithoutVersion, xmlSchema);
              schema = Optional.ofNullable(xmlSchema);
            } catch (SAXException e) {
              LOGGER.error("Error while loading XML Schema", e);
            } finally {
              IOUtils.closeQuietly(schemaStream);
            }
          }
        }
      }
    }
    return schema;

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

  public static List<String> getRodaConfigurationAsList(String... keyParts) {
    return Arrays.asList(rodaConfiguration.getStringArray(getConfigurationKey(keyParts)));
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
      rodaPropertiesCache.put(cacheName, newCacheEntry);
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

  public static void runReindex(List<String> args) {
    String entity = args.get(2);
    if (StringUtils.isNotBlank(entity)) {
      if ("users_and_groups".equalsIgnoreCase(entity)) {
        try {
          indexUsersAndGroupsFromLDAP();
        } catch (EmailAlreadyExistsException | UserAlreadyExistsException | IllegalOperationException
          | LdapUtilityException | GenericException | NotFoundException | AlreadyExistsException e) {
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
      e.printStackTrace();
    }
  }

  private static void printIndexMembers(List<String> args, Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    System.out.println("index list " + args.get(2));
    IndexResult<RODAMember> users = index.find(RODAMember.class, filter, sorter, sublist, facets);
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

  private static void printFiles(Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_SEARCH, "OLA-OLÁ-1234-XXXX_K"));
    IndexResult<IndexedFile> res = index.find(IndexedFile.class, filter, sorter, sublist);

    for (IndexedFile sf : res.getResults()) {
      System.out.println(sf.toString());
    }
  }

  private static void printEvents(Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_TYPE, "format identification"));
    IndexResult<IndexedPreservationEvent> res = index.find(IndexedPreservationEvent.class, filter, sorter, sublist);

    for (IndexedPreservationEvent ipe : res.getResults()) {
      System.out.println(ipe.toString());
    }
  }

  private static void printAgents(Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.PRESERVATION_AGENT_TYPE, PreservationAgentType.SOFTWARE.toString()));
    IndexResult<IndexedPreservationAgent> res = index.find(IndexedPreservationAgent.class, filter, sorter, sublist);

    for (IndexedPreservationAgent ipa : res.getResults()) {
      System.out.println(ipa.toString());
    }
  }

  private static void printMainUsage() {
    // System.err.println("WARNING: if using Apache Solr embedded, the index
    // related commands");
    // System.err.println("cannot be run with RODA running (i.e. deployed in
    // Tomcat for example)");
    // System.err.println("Syntax:");
    // System.err.println(
    // "java -jar x.jar index reindex
    // aip|job|risk|agent|format|notification|transferred_resources|actionlogs|users_and_groups");
    // System.err.println("java -jar x.jar index list
    // users|groups|sips|file");
    // System.err.println("java -jar x.jar orphans [newParentID]");
    // System.err.println("java -jar x.jar fixity");
    // System.err.println("java -jar x.jar antivirus");
    // System.err.println("java -jar x.jar premisskeleton");
  }

  private static void mainMasterTasks(List<String> args) throws GenericException, RequestNotValidException {
    if ("index".equals(args.get(0))) {
      if ("list".equals(args.get(1)) && ("users".equals(args.get(2)) || "groups".equals(args.get(2)))) {
        Filter filter = new Filter(
          new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, "users".equals(args.get(2)) ? "true" : "false"));
        printIndexMembers(args, filter, null, new Sublist(0, 10000), null);
      } else if ("list".equals(args.get(1)) && ("sips".equals(args.get(2)))) {
        printCountSips(null, new Sublist(0, 10000), null);
      } else if ("list".equals(args.get(1)) && ("file".equals(args.get(2)))) {
        printFiles(null, new Sublist(0, 10000), null);
      } else if ("list".equals(args.get(1)) && ("event".equals(args.get(2)))) {
        printEvents(null, new Sublist(0, 10000), null);
      } else if ("list".equals(args.get(1)) && ("agent".equals(args.get(2)))) {
        printAgents(null, new Sublist(0, 10000), null);
      } else if ("query".equals(args.get(1)) && args.size() == 4 && StringUtils.isNotBlank(args.get(2))
        && StringUtils.isNotBlank(args.get(3))) {
        runSolrQuery(args);
      } else if ("reindex".equals(args.get(1))) {
        runReindex(args);
      }
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
