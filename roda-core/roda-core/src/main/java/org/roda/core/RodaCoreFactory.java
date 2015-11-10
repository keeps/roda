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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.roda.core.common.ApacheDS;
import org.roda.core.common.LdapUtility;
import org.roda.core.common.Messages;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.EventPreservationObject;
import org.roda.core.data.v2.Group;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.RODAMember;
import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.core.data.v2.User;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexServiceException;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.AIP;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceException;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.PluginOrchestrator;
import org.roda.core.plugins.orchestrate.AkkaDistributedPluginOrchestrator;
import org.roda.core.plugins.orchestrate.AkkaDistributedPluginWorker;
import org.roda.core.plugins.plugins.antivirus.AntivirusPlugin;
import org.roda.core.plugins.plugins.base.AIPValidationPlugin;
import org.roda.core.plugins.plugins.base.CharacterizationPlugin;
import org.roda.core.plugins.plugins.base.FixityPlugin;
import org.roda.core.plugins.plugins.base.LogCleanerPlugin;
import org.roda.core.plugins.plugins.base.ReindexPlugin;
import org.roda.core.plugins.plugins.base.RemoveOrphansPlugin;
import org.roda.core.plugins.plugins.base.V2ToV3PremisPlugin;
import org.roda.core.plugins.plugins.ingest.BagitToAIPPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.DroidPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.ExifToolPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.FFProbePlugin;
import org.roda.core.plugins.plugins.ingest.characterization.FITSPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.JHOVEPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.JpylyzerPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.MediaInfoPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.PremisUpdateFromToolsPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPlugin;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;
import org.roda.core.storage.fs.FileStorageService;

public class RodaCoreFactory {
  private static final Logger LOGGER = Logger.getLogger(RodaCoreFactory.class);

  public enum NODE_TYPE_ENUM {
    MASTER, WORKER
  }

  private static final String LDAP_DEFAULT_HOST = "localhost";
  private static final int LDAP_DEFAULT_PORT = 10389;

  // FIXME perhaps move this to RodaConstants
  // RODA system properties (provided with -D in the command-line)
  private static final String NODE_TYPE = "roda.node.type";
  private static final String CLUSTER_HOSTNAME = "roda.cluster.hostname";
  private static final String CLUSTER_PORT = "roda.cluster.port";
  private static final String NODE_HOSTNAME = "roda.node.hostname";
  private static final String NODE_PORT = "roda.node.port";

  private static boolean instantiated = false;
  private static NODE_TYPE_ENUM nodeType;

  private static Path rodaHomePath;
  private static Path storagePath;
  private static Path indexPath;
  private static Path dataPath;
  private static Path logPath;
  private static Path configPath;
  private static StorageService storage;
  private static ModelService model;
  private static IndexService index;
  private static EmbeddedSolrServer solr;

  // FIXME we should have only "one" orchestrator
  private static PluginOrchestrator pluginOrchestrator;
  private static AkkaDistributedPluginOrchestrator akkaDistributedPluginOrchestrator;
  private static AkkaDistributedPluginWorker akkaDistributedPluginWorker;
  private static boolean FEATURE_AKKA_ENABLED = false;

  private static ApacheDS ldap;
  private static Path rodaApacheDsConfigDirectory = null;
  private static Path rodaApacheDsDataDirectory = null;

  private static CompositeConfiguration rodaConfiguration = null;
  private static List<String> configurationFiles = null;
  private static Map<String, Map<String, String>> propertiesCache = null;
  private static Map<Locale, Messages> i18nMessages = new HashMap<Locale, Messages>();

  public static void instantiate() {
    if ("master".equalsIgnoreCase(getSystemProperty(NODE_TYPE, "master"))) {
      instantiateMaster();
    } else {
      instantiateWorker();
    }
  }

  public static void instantiateMaster() {
    instantiate(NODE_TYPE_ENUM.MASTER);
  }

  public static void instantiateWorker() {
    instantiate(NODE_TYPE_ENUM.WORKER);
  }

  private static void instantiate(NODE_TYPE_ENUM nodeType) {
    RodaCoreFactory.nodeType = nodeType;

    if (!instantiated) {
      try {
        // determine RODA HOME
        rodaHomePath = determineRodaHomePath();

        // instantiate essential directories and objects
        instantiateEssentialDirectoriesAndObjects();

        // instantiate solr and index service
        instantiateSolrAndIndexService(nodeType);

        // load core configurations
        rodaConfiguration = new CompositeConfiguration();
        configurationFiles = new ArrayList<String>();
        propertiesCache = new HashMap<String, Map<String, String>>();
        addConfiguration("roda-core.properties");

      } catch (ConfigurationException e) {
        LOGGER.error("Error loading roda properties", e);
      } catch (StorageServiceException e) {
        LOGGER.error(e);
      } catch (URISyntaxException e) {
        LOGGER.error(e);
      }

      instantiatePluginsRelatedObjects(nodeType);

      instantiated = true;
    }
  }

  private static void instantiateEssentialDirectoriesAndObjects() throws StorageServiceException {
    // instantiate essential directories
    configPath = rodaHomePath.resolve("config");
    dataPath = rodaHomePath.resolve("data");
    logPath = dataPath.resolve("log");
    storagePath = dataPath.resolve("storage");
    indexPath = dataPath.resolve("index");

    // make sure all essential directories exist
    ensureAllEssentialDirectoriesExist();

    // instantiate model related objects
    storage = new FileStorageService(storagePath);
    model = new ModelService(storage);
  }

  private static void instantiateSolrAndIndexService(NODE_TYPE_ENUM nodeType) throws URISyntaxException {
    if (nodeType == NODE_TYPE_ENUM.MASTER) {
      // configure Solr (first try RODA HOME and then fallback to classpath)
      Path solrHome = configPath.resolve("index");
      if (!Files.exists(solrHome)) {
        // FIXME perhaps these files should be copied from classpath to
        // install dir ir they cannot be used from classpath
        solrHome = Paths.get(RodaCoreFactory.class.getResource("/config/index/").toURI());
      }

      System.setProperty("solr.data.dir", indexPath.toString());
      System.setProperty("solr.data.dir.aip", indexPath.resolve("aip").toString());
      System.setProperty("solr.data.dir.sdo", indexPath.resolve("sdo").toString());
      System.setProperty("solr.data.dir.representations", indexPath.resolve("representation").toString());
      System.setProperty("solr.data.dir.preservationevent", indexPath.resolve("preservationevent").toString());
      System.setProperty("solr.data.dir.preservationobject", indexPath.resolve("preservationobject").toString());
      System.setProperty("solr.data.dir.Pluginlog", indexPath.resolve("Pluginlog").toString());
      System.setProperty("solr.data.dir.sipreport", indexPath.resolve("sipreport").toString());
      System.setProperty("solr.data.dir.members", indexPath.resolve("members").toString());
      System.setProperty("solr.data.dir.othermetadata", indexPath.resolve("othermetadata").toString());
      // FIXME added missing cores

      // start embedded solr
      solr = new EmbeddedSolrServer(solrHome, "test");

      // instantiate index related object
      index = new IndexService(solr, model, configPath);
    }
  }

  private static void instantiatePluginsRelatedObjects(NODE_TYPE_ENUM nodeType) {
    if (nodeType == NODE_TYPE_ENUM.MASTER) {
      if (FEATURE_AKKA_ENABLED) {
        // PluginOrchestrator = new EmbeddedActionOrchestrator();
        // pluginOrchestrator = new AkkaEmbeddedPluginOrchestrator();
        akkaDistributedPluginOrchestrator = new AkkaDistributedPluginOrchestrator(
          getSystemProperty(NODE_HOSTNAME, "localhost"), getSystemProperty(NODE_PORT, "2551"));
      }

      startApacheDS();
    } else if (nodeType == NODE_TYPE_ENUM.WORKER) {
      akkaDistributedPluginWorker = new AkkaDistributedPluginWorker(getSystemProperty(CLUSTER_HOSTNAME, "localhost"),
        getSystemProperty(CLUSTER_PORT, "2551"), getSystemProperty(NODE_HOSTNAME, "localhost"),
        getSystemProperty(NODE_PORT, "0"));
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

      // set roda.home in order to correctly configure log4j even if no property
      // has been defined
      System.setProperty("roda.home", rodaHomePath.toString());
      LogManager.resetConfiguration();
      DOMConfigurator.configure(RodaCoreFactory.class.getResource("/log4j.xml"));
    }

    return rodaHomePath;
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
    essentialDirectories.add(configPath.resolve("i18n"));
    essentialDirectories.add(configPath.resolve("ldap"));
    essentialDirectories.add(configPath.resolve("schemas"));
    essentialDirectories.add(configPath.resolve("crosswalks"));
    essentialDirectories.add(configPath.resolve("crosswalks").resolve("ingest"));
    essentialDirectories.add(configPath.resolve("crosswalks").resolve("dissemination"));
    essentialDirectories.add(configPath.resolve("crosswalks").resolve("dissemination").resolve("html"));
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
      if (nodeType == NODE_TYPE_ENUM.MASTER) {
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

      String ldapHost = rodaConfig.getString("ldap.host", LDAP_DEFAULT_HOST);
      int ldapPort = rodaConfig.getInt("ldap.port", LDAP_DEFAULT_PORT);
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

  public static PluginOrchestrator getPluginOrchestrator() {
    return pluginOrchestrator;
  }

  public static AkkaDistributedPluginOrchestrator getAkkaDistributedPluginOrchestrator() {
    return akkaDistributedPluginOrchestrator;
  }

  public static NODE_TYPE_ENUM getNodeType() {
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

  public static void closeSolrServer() {
    try {
      solr.close();
    } catch (IOException e) {
      LOGGER.error(e);
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
    try {
      Path bagitFolder = RodaCoreFactory.getDataPath().resolve("bagit");
      Plugin<String> bagitPlugin = new BagitToAIPPlugin();
      Stream<Path> bagits = Files.list(bagitFolder);
      List<Path> bagitsList = bagits.collect(Collectors.toList());
      bagits.close();
      getPluginOrchestrator().runPluginOnFiles(bagitPlugin, bagitsList);
    } catch (IOException e) {
      LOGGER.error("Error running bagit Plugin: " + e.getMessage(), e);
    }
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
    System.err.println("java -jar x.jar index list users|groups");
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
  }

  private static void printIndexMembers(List<String> args, Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws IndexServiceException {
    System.out.println("index list " + args.get(2));
    IndexResult<RODAMember> users = index.find(RODAMember.class, filter, sorter, sublist, facets);
    for (RODAMember rodaMember : users.getResults()) {
      System.out.println("\t" + rodaMember);
    }
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
    } else {
      printMainUsage();
    }
  }

  public static void main(String[] argsArray) throws IndexServiceException, InterruptedException {
    List<String> args = Arrays.asList(argsArray);

    instantiate();
    if (getNodeType() == NODE_TYPE_ENUM.MASTER) {
      if (args.size() > 0) {
        mainMasterTasks(args);
      } else {
        printMainUsage();
      }
    } else if (getNodeType() == NODE_TYPE_ENUM.WORKER) {
      Thread.currentThread().join();
    } else {
      printMainUsage();
    }

    System.exit(0);
  }

}
