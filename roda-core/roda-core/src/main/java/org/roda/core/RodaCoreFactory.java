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
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
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
import org.roda.core.data.common.InvalidParameterException;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.NodeType;
import org.roda.core.data.eadc.DescriptionLevelManager;
import org.roda.core.data.v2.EventPreservationObject;
import org.roda.core.data.v2.Group;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.RODAMember;
import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.core.data.v2.TransferredResource;
import org.roda.core.data.v2.User;
import org.roda.core.index.IndexFolderObserver;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexServiceException;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginManager;
import org.roda.core.plugins.PluginManagerException;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.AkkaDistributedPluginOrchestrator;
import org.roda.core.plugins.orchestrate.AkkaDistributedPluginWorker;
import org.roda.core.plugins.orchestrate.AkkaEmbeddedPluginOrchestrator;
import org.roda.core.plugins.plugins.antivirus.AntivirusPlugin;
import org.roda.core.plugins.plugins.base.AIPValidationPlugin;
import org.roda.core.plugins.plugins.base.CharacterizationPlugin;
import org.roda.core.plugins.plugins.base.FixityPlugin;
import org.roda.core.plugins.plugins.base.LogCleanerPlugin;
import org.roda.core.plugins.plugins.base.ReindexPlugin;
import org.roda.core.plugins.plugins.base.RemoveOrphansPlugin;
import org.roda.core.plugins.plugins.base.V2ToV3PremisPlugin;
import org.roda.core.plugins.plugins.ingest.BagitToAIPPlugin;
import org.roda.core.plugins.plugins.ingest.EARKSIPToAIPPlugin;
import org.roda.core.plugins.plugins.ingest.TransferredResourceToAIPPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.DroidPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.ExifToolPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.FFProbePlugin;
import org.roda.core.plugins.plugins.ingest.characterization.FITSPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.FastCharacterizationPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.JHOVEPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.JpylyzerPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.MediaInfoPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.PremisUpdateFromToolsPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPlugin;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class RodaCoreFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(RodaCoreFactory.class);

  private static boolean instantiated = false;
  private static NodeType nodeType;

  // Core related objects
  private static Path rodaHomePath;
  private static Path storagePath;
  private static Path indexPath;
  private static Path dataPath;
  private static Path logPath;
  private static Path configPath;
  private static StorageService storage;
  private static ModelService model;
  private static IndexService index;
  private static SolrClient solr;
  private static boolean FEATURE_OVERRIDE_INDEX_CONFIGS = true;

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

  private static FolderMonitorNIO sipFolderMonitor;
  private static FolderObserver sipFolderObserver;

  // Configuration related objects
  private static CompositeConfiguration rodaConfiguration = null;
  private static List<String> configurationFiles = null;
  private static Map<String, Map<String, String>> propertiesCache = null;
  private static Map<Locale, Messages> i18nMessages = new HashMap<Locale, Messages>();
  private static DescriptionLevelManager descriptionLevelManager = null;

  public static void instantiate() {
    if ("master".equalsIgnoreCase(getSystemProperty(RodaConstants.CORE_NODE_TYPE, "master"))) {
      instantiateMaster();
    } else if ("test".equalsIgnoreCase(getSystemProperty(RodaConstants.CORE_NODE_TYPE, "test"))) {
      instantiateTest();
    } else {
      instantiateWorker();
    }
  }

  public static void instantiateMaster() {
    instantiate(NodeType.MASTER);
  }

  public static void instantiateWorker() {
    instantiate(NodeType.WORKER);
  }

  public static void instantiateTest() {
    instantiate(NodeType.TEST);
  }

  private static void instantiate(NodeType nodeType) {
    RodaCoreFactory.nodeType = nodeType;

    if (!instantiated) {
      try {
        // determine RODA HOME
        rodaHomePath = determineRodaHomePath();

        // instantiate essential directories and objects
        instantiateEssentialDirectoriesAndObjects();

        // instantiate solr and index service
        instantiateSolrAndIndexService();

        // load core configurations
        rodaConfiguration = new CompositeConfiguration();
        configurationFiles = new ArrayList<String>();
        propertiesCache = new HashMap<String, Map<String, String>>();
        addConfiguration("roda-core.properties");

        // load description level information
        loadDescriptionLevelInformation();

      } catch (ConfigurationException e) {
        LOGGER.error("Error loading roda properties", e);
      } catch (StorageServiceException e) {
        LOGGER.error("Error instantiating storage model", e);
      } catch (URISyntaxException e) {
        LOGGER.error("Error instantiating solr/index model", e);
      }

      try {
        pluginManager = PluginManager.getDefaultPluginManager(getConfigPath(), getPluginsPath());
      } catch (PluginManagerException e) {
        LOGGER.error("Error instantiating PluginManager", e);
      }

      instantiateNodeSpecificObjects(nodeType);

      instantiated = true;
    }
  }

  private static void loadDescriptionLevelInformation() {
    Properties descriptionLevelConfiguration = new Properties();
    try {
      descriptionLevelConfiguration
        .load(getConfigurationFile("roda-description-levels-hierarchy.properties").openStream());
    } catch (IOException e) {
      // do nothing and instantiate description level manager from empty
      // properties object
    }
    LOGGER.trace("Description level configurations being loaded: " + descriptionLevelConfiguration);
    descriptionLevelManager = new DescriptionLevelManager(descriptionLevelConfiguration);
  }

  private static void instantiateEssentialDirectoriesAndObjects() throws StorageServiceException {
    // make sure all essential directories exist
    ensureAllEssentialDirectoriesExist();

    if (nodeType != NodeType.TEST) {
      // instantiate model related objects
      storage = instantiateStorageService();
      model = new ModelService(storage);
    }
  }

  private static StorageService instantiateStorageService() throws StorageServiceException {
    // FIXME the type of storage to be used should be configurable
    return new FileStorageService(storagePath);
  }

  private static void instantiateSolrAndIndexService() throws URISyntaxException {
    if (nodeType == NodeType.MASTER) {
      Path solrHome = configPath.resolve("index");
      if (!Files.exists(solrHome) || FEATURE_OVERRIDE_INDEX_CONFIGS) {
        copyIndexConfigsFromClasspathToRodaHome();
      }

      System.setProperty("solr.data.dir", indexPath.toString());
      System.setProperty("solr.data.dir.aip", indexPath.resolve("aip").toString());
      System.setProperty("solr.data.dir.sdo", indexPath.resolve("sdo").toString());
      System.setProperty("solr.data.dir.representations", indexPath.resolve("representation").toString());
      System.setProperty("solr.data.dir.preservationevent", indexPath.resolve("preservationevent").toString());
      System.setProperty("solr.data.dir.preservationobject", indexPath.resolve("preservationobject").toString());
      System.setProperty("solr.data.dir.actionlog", indexPath.resolve("actionlog").toString());
      System.setProperty("solr.data.dir.jobreport", indexPath.resolve("jobreport").toString());
      System.setProperty("solr.data.dir.members", indexPath.resolve("members").toString());
      System.setProperty("solr.data.dir.othermetadata", indexPath.resolve("othermetadata").toString());
      System.setProperty("solr.data.dir.sip", indexPath.resolve("sip").toString());
      System.setProperty("solr.data.dir.job", indexPath.resolve("job").toString());
      System.setProperty("solr.data.dir.file", indexPath.resolve("file").toString());

      // instantiate & start solr
      solr = instantiateSolr(solrHome);

      // instantiate index related object
      index = new IndexService(solr, model);
    }
  }

  private static void copyIndexConfigsFromClasspathToRodaHome() {
    List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
    classLoadersList.add(ClasspathHelper.contextClassLoader());
    // classLoadersList.add(ClasspathHelper.staticClassLoader());

    Set<String> resources = new Reflections(
      new ConfigurationBuilder().filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("config/index/")))
        .setScanners(new ResourcesScanner())
        .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0]))))
          .getResources(Pattern.compile(".*"));

    for (String resource : resources) {
      InputStream originStream = RodaCoreFactory.class.getClassLoader().getResourceAsStream(resource);
      Path destinyPath = rodaHomePath.resolve(resource);
      try {
        // create all parent directories
        Files.createDirectories(destinyPath.getParent());
        // copy file
        Files.copy(originStream, destinyPath, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        LOGGER.error("Error copying file from classpath: {} to {} (reason: {})", originStream, destinyPath,
          e.getMessage());
      }
    }
  }

  private static SolrClient instantiateSolr(Path solrHome) {
    // FIXME the type of solr to be used should be configurable
    return new EmbeddedSolrServer(solrHome, "test");
  }

  private static void instantiateNodeSpecificObjects(NodeType nodeType) {
    if (nodeType == NodeType.MASTER) {
      if (FEATURE_DISTRIBUTED_AKKA) {

        akkaDistributedPluginOrchestrator = new AkkaDistributedPluginOrchestrator(
          getSystemProperty(RodaConstants.CORE_NODE_HOSTNAME, "localhost"),
          getSystemProperty(RodaConstants.CORE_NODE_PORT, "2551"));
      } else {
        // pluginOrchestrator = new EmbeddedActionOrchestrator();
        pluginOrchestrator = new AkkaEmbeddedPluginOrchestrator();
      }

      startApacheDS();

      try {
        startSIPFolderMonitor();
      } catch (Exception e) {
        LOGGER.error("Error starting SIP Monitor: " + e.getMessage(), e);
      }

    } else if (nodeType == NodeType.WORKER) {
      akkaDistributedPluginWorker = new AkkaDistributedPluginWorker(
        getSystemProperty(RodaConstants.CORE_CLUSTER_HOSTNAME, "localhost"),
        getSystemProperty(RodaConstants.CORE_CLUSTER_PORT, "2551"),
        getSystemProperty(RodaConstants.CORE_NODE_HOSTNAME, "localhost"),
        getSystemProperty(RodaConstants.CORE_NODE_PORT, "0"));
    } else if (nodeType == NodeType.TEST) {
      // do nothing
    } else {
      LOGGER.error("Unknown node type \"" + nodeType + "\"");
      throw new RuntimeException("Unknown node type \"" + nodeType + "\"");
    }
  }

  private static Path determineRodaHomePath() {
    Path rodaHomePath;
    if (System.getProperty("roda.home") != null) {
      rodaHomePath = Paths.get(System.getProperty("roda.home"));
    } else if (System.getenv("RODA_HOME") != null) {
      rodaHomePath = Paths.get(System.getenv("RODA_HOME"));
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
      // property
      // has been defined
      System.setProperty("roda.home", rodaHomePath.toString());
    }

    // instantiate essential directories
    configPath = rodaHomePath.resolve("config");
    dataPath = rodaHomePath.resolve("data");
    logPath = dataPath.resolve("log");
    storagePath = dataPath.resolve("storage");
    indexPath = dataPath.resolve("index");

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
    essentialDirectories.add(configPath.resolve("crosswalks"));
    essentialDirectories.add(configPath.resolve("crosswalks").resolve("ingest"));
    essentialDirectories.add(configPath.resolve("crosswalks").resolve("dissemination"));
    essentialDirectories.add(configPath.resolve("crosswalks").resolve("dissemination").resolve("html"));
    essentialDirectories.add(configPath.resolve("i18n"));
    essentialDirectories.add(configPath.resolve("ldap"));
    essentialDirectories.add(configPath.resolve("plugins"));
    essentialDirectories.add(configPath.resolve("schemas"));
    essentialDirectories.add(rodaHomePath.resolve("log"));
    essentialDirectories.add(dataPath);
    essentialDirectories.add(logPath);
    essentialDirectories.add(storagePath);
    essentialDirectories.add(indexPath);

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
  }

  public static void shutdown() throws IOException {
    if (instantiated) {
      solr.close();
      pluginManager.shutdown();
      pluginOrchestrator.shutdown();
      sipFolderMonitor.stopWatch();

      if (nodeType == NodeType.MASTER) {
        stopApacheDS();
      }
    }
  }

  public static void startApacheDS() {
    ldap = new ApacheDS();
    rodaApacheDsConfigDirectory = RodaCoreFactory.getConfigPath().resolve("ldap");
    rodaApacheDsDataDirectory = RodaCoreFactory.getDataPath().resolve("ldap");

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
          LOGGER.debug("User to be indexed: " + user);
          RodaCoreFactory.getModelService().addUser(user, false, true);
        }
        for (Group group : UserUtility.getLdapUtility().getGroups(new Filter())) {
          LOGGER.debug("Group to be indexed: " + group);
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

  public static void startSIPFolderMonitor() throws Exception {

    Configuration rodaConfig = RodaCoreFactory.getRodaConfiguration();
    String SIPFolderPath = rodaConfig.getString("sip.folder");
    Path sipFolderPath = dataPath.resolve(SIPFolderPath);
    Date date = getFolderMonitorDate(sipFolderPath);
    sipFolderObserver = new IndexFolderObserver(solr, sipFolderPath);
    sipFolderMonitor = new FolderMonitorNIO(sipFolderPath, date, solr);
    sipFolderMonitor.addFolderObserver(sipFolderObserver);
    LOGGER.debug("Transfer folder monitor is fully initialized? " + getFolderMonitor().isFullyInitialized());
  }

  public static Date getFolderMonitorDate(Path sipFolderPath) {
    Date folderMonitorDate = null;
    try {
      Path dateFile = sipFolderPath.resolve(".date");
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

  public static void setFolderMonitorDate(Path sipFolderPath, Date d) {
    try {
      Path dateFile = sipFolderPath.resolve(".date");
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

  public static PluginManager getPluginManager() {
    return pluginManager;
  }

  public static PluginOrchestrator getPluginOrchestrator() {
    return pluginOrchestrator;
  }

  public static AkkaDistributedPluginOrchestrator getAkkaDistributedPluginOrchestrator() {
    return akkaDistributedPluginOrchestrator;
  }

  public static FolderMonitorNIO getFolderMonitor() {
    return sipFolderMonitor;
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

  public static Path getDataPath() {
    return dataPath;
  }

  public static Path getLogPath() {
    return logPath;
  }

  public static Path getJobsPath() {
    return dataPath.resolve("jobs");
  }

  public static Path getPluginsPath() {
    return configPath.resolve("plugins");
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
      LOGGER.debug("Loading configuration from file " + config);
      propertiesConfiguration.load(config.toFile());
      RodaPropertiesReloadStrategy rodaPropertiesReloadStrategy = new RodaPropertiesReloadStrategy();
      rodaPropertiesReloadStrategy.setRefreshDelay(5000);
      propertiesConfiguration.setReloadingStrategy(rodaPropertiesReloadStrategy);
    } else {
      InputStream inputStream = RodaCoreFactory.class.getResourceAsStream("/config/" + configurationFile);
      if (inputStream != null) {
        LOGGER.debug("Loading configuration from classpath " + configurationFile);
        propertiesConfiguration.load(inputStream);
      } else {
        LOGGER.error("Configuration " + configurationFile + " doesn't exist");
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
        LOGGER.error("Configuration " + configurationFile + " doesn't exist");
        configUri = null;
      }
    } else {
      URL resource = RodaCoreFactory.class.getResource("/config/" + configurationFile);
      if (resource != null) {
        configUri = resource;
      } else {
        LOGGER.error("Configuration " + configurationFile + " doesn't exist");
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
        LOGGER.debug("Loading configuration from file " + config);
      }
    } catch (IOException e) {
      // do nothing
    }
    if (inputStream == null) {
      inputStream = RodaUtils.class.getResourceAsStream("/config/" + configurationFile);
      LOGGER.debug("Loading configuration from classpath " + configurationFile);
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

  public static String getRodaConfigurationAsString(String... keyParts) {
    StringBuilder sb = new StringBuilder();
    for (String part : keyParts) {
      if (sb.length() != 0) {
        sb.append('.');
      }
      sb.append(part);
    }

    return rodaConfiguration.getString(sb.toString());
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
      messages = new Messages(locale, getConfigPath().resolve("i18n"));
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
    Plugin<AIP> reindexPlugin = new ReindexPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(reindexPlugin);
  }

  public static void runReindexAipsPlugin(List<String> aipIds) {
    Plugin<AIP> reindexPlugin = new ReindexPlugin();
    ((ReindexPlugin) reindexPlugin).setClearIndexes(false);
    getPluginOrchestrator().runPluginOnAIPs(reindexPlugin, aipIds);
  }

  public static void runRemoveOrphansPlugin(String parentId) {
    try {
      Filter filter = new Filter(new EmptyKeyFilterParameter(RodaConstants.AIP_PARENT_ID));
      RemoveOrphansPlugin removeOrphansPlugin = new RemoveOrphansPlugin();
      removeOrphansPlugin.setNewParent(model.retrieveAIP(parentId));
      getPluginOrchestrator().runPluginFromIndex(SimpleDescriptionObject.class, filter, removeOrphansPlugin);
    } catch (ModelServiceException e) {
      e.printStackTrace();
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

  private static void runCharacterizationPlugin() {
    Plugin<AIP> characterizationPlugin = new CharacterizationPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(characterizationPlugin);
  }

  private static void runPremisSkeletonPlugin() {
    Plugin<AIP> premisSkeletonPlugin = new PremisSkeletonPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(premisSkeletonPlugin);
  }

  private static void runPremisV2toV3Plugin() {
    Plugin<AIP> premisUpdatePlugin = new V2ToV3PremisPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(premisUpdatePlugin);
  }

  private static void runValidationPlugin() {
    Plugin<AIP> validationPlugin = new AIPValidationPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(validationPlugin);
  }

  private static void runLogCleanPlugin() {
    Plugin<AIP> logCleanPlugin = new LogCleanerPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(logCleanPlugin);
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

  private static void runPremisUpdatePlugin() {
    Plugin<AIP> jpylyzerPlugin = new PremisUpdateFromToolsPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(jpylyzerPlugin);
  }

  private static void runFastCharacterizationPlugin() {
    Plugin<AIP> fastCharacterizationPlugin = new FastCharacterizationPlugin();
    getPluginOrchestrator().runPluginOnAllAIPs(fastCharacterizationPlugin);
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
    } catch (InvalidParameterException | IOException ipe) {
      LOGGER.error(ipe.getMessage(), ipe);
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
    } catch (InvalidParameterException ipe) {
      LOGGER.error(ipe.getMessage(), ipe);
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
    System.err.println("java -jar x.jar index list users|groups|sips");
    System.err.println("java -jar x.jar orphans [newParentID]");
    System.err.println("java -jar x.jar fixity");
    System.err.println("java -jar x.jar antivirus");
    System.err.println("java -jar x.jar premisskeleton");
    System.err.println("java -jar x.jar droid");
    System.err.println("java -jar x.jar fulltext");
    System.err.println("java -jar x.jar jhove");
    System.err.println("java -jar x.jar fits");
    System.err.println("java -jar x.jar characterization");
    System.err.println("java -jar x.jar v2tov3");
    System.err.println("java -jar x.jar validation");
    System.err.println("java -jar x.jar logClean");
    System.err.println("java -jar x.jar exifTool");
    System.err.println("java -jar x.jar mediaInfo");
    System.err.println("java -jar x.jar ffprobe");
    System.err.println("java -jar x.jar jpylyzer");
    System.err.println("java -jar x.jar premisupdate");
    System.err.println("java -jar x.jar fastcharacterization");
    System.err.println("java -jar x.jar eark");
  }

  private static void printIndexMembers(List<String> args, Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws IndexServiceException {
    System.out.println("index list " + args.get(2));
    IndexResult<RODAMember> users = index.find(RODAMember.class, filter, sorter, sublist, facets);
    for (RODAMember rodaMember : users.getResults()) {
      System.out.println("\t" + rodaMember);
    }
  }

  private static void printCountSips(Sorter sorter, Sublist sublist, Facets facets) throws IndexServiceException {
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

  private static void printPreservationEvents(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws IndexServiceException {
    index.find(EventPreservationObject.class, filter, sorter, sublist, facets);
    IndexResult<EventPreservationObject> events = index.find(EventPreservationObject.class, filter, sorter, sublist,
      facets);
    for (EventPreservationObject event : events.getResults()) {
      System.out.println("\t" + event);
    }
  }

  private static void printPreservationFiles(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws IndexServiceException {
    index.find(RepresentationFilePreservationObject.class, filter, sorter, sublist, facets);
    IndexResult<RepresentationFilePreservationObject> files = index.find(RepresentationFilePreservationObject.class,
      filter, sorter, sublist, facets);
    for (RepresentationFilePreservationObject file : files.getResults()) {
      System.out.println("\t" + file);
    }
  }

  private static void mainMasterTasks(List<String> args) throws IndexServiceException {
    if ("index".equals(args.get(0))) {
      if ("list".equals(args.get(1)) && ("users".equals(args.get(2)) || "groups".equals(args.get(2)))) {
        Filter filter = new Filter(
          new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, "users".equals(args.get(2)) ? "true" : "false"));
        printIndexMembers(args, filter, null, new Sublist(0, 10000), null);
      } else if ("list".equals(args.get(1)) && ("sips".equals(args.get(2)))) {
        printCountSips(null, new Sublist(0, 10000), null);
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
    } else if ("jhove".equals(args.get(0))) {
      runJhovePlugin();
    } else if ("fits".equals(args.get(0))) {
      runFitsPlugin();
    } else if ("bagit".equals(args.get(0))) {
      runBagitPlugin();
    } else if ("events".equals(args.get(0))) {
      Filter filter = null;
      printPreservationEvents(filter, null, new Sublist(0, 10000), null);
    } else if ("files".equals(args.get(0))) {
      Filter filter = null;
      printPreservationFiles(filter, null, new Sublist(0, 10000), null);
    } else if ("characterization".equals(args.get(0))) {
      runCharacterizationPlugin();
    } else if ("v2tov3".equals(args.get(0))) {
      runPremisV2toV3Plugin();
    } else if ("validation".equals(args.get(0))) {
      runValidationPlugin();
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
    } else if ("premisupdate".equals(args.get(0))) {
      runPremisUpdatePlugin();
    } else if ("fastcharacterization".equals(args.get(0))) {
      runFastCharacterizationPlugin();
    } else if ("eark".equals(args.get(0))) {
      runEARKPlugin();
    } else if ("transferredResource".equals(args.get(0))) {
      runTransferredResourceToAIPPlugin();
    } else {
      printMainUsage();
    }
  }

  public static void main(String[] argsArray) throws IndexServiceException, InterruptedException {
    List<String> args = Arrays.asList(argsArray);

    instantiate();
    if (getNodeType() == NodeType.MASTER) {
      if (args.size() > 0) {
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
