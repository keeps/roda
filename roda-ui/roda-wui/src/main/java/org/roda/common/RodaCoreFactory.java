/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.common;

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
import org.roda.action.antivirus.AntivirusAction;
import org.roda.action.characterization.CharacterizationAction;
import org.roda.action.fixity.FixityAction;
import org.roda.action.ingest.bagit.BagitToAIPAction;
import org.roda.action.ingest.characterization.Droid.DroidAction;
import org.roda.action.ingest.characterization.ExifTool.ExifToolAction;
import org.roda.action.ingest.characterization.FFProbe.FFProbeAction;
import org.roda.action.ingest.characterization.FITS.FITSAction;
import org.roda.action.ingest.characterization.JHOVE.JHOVEAction;
import org.roda.action.ingest.characterization.MediaInfo.MediaInfoAction;
import org.roda.action.ingest.characterization.jpylyzer.JpylyzerAction;
import org.roda.action.ingest.fulltext.FullTextAction;
import org.roda.action.ingest.premis.PremisUpdateFromToolsAction.PremisUpdateFromToolsAction;
import org.roda.action.ingest.premis.skeleton.PremisSkeletonAction;
import org.roda.action.orchestrate.ActionOrchestrator;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.actions.ReindexAction;
import org.roda.action.orchestrate.actions.RemoveOrphansAction;
import org.roda.action.orchestrate.embed.AkkaEmbeddedActionOrchestrator;
import org.roda.action.utils.logCleaner.LogCleanerAction;
import org.roda.action.utils.premis.V2ToV3PremisAction;
import org.roda.action.validation.AIPValidationAction;
import org.roda.common.monitor.FolderMonitorNIO;
import org.roda.common.monitor.FolderObservable;
import org.roda.common.monitor.FolderObserver;
import org.roda.core.common.RodaConstants;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.EventPreservationObject;
import org.roda.core.data.v2.Group;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.RODAMember;
import org.roda.core.data.v2.RepresentationFilePreservationObject;
import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.core.data.v2.User;
import org.roda.index.IndexFolderObserver;
import org.roda.index.IndexService;
import org.roda.index.IndexServiceException;
import org.roda.index.utils.SolrUtils;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fs.FileStorageService;

import config.i18n.server.Messages;

public class RodaCoreFactory {
  private static final Logger LOGGER = Logger.getLogger(RodaCoreFactory.class);

  private static final String LDAP_DEFAULT_HOST = "localhost";
  private static final int LDAP_DEFAULT_PORT = 10389;

  private static boolean instantiated = false;

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
  private static ActionOrchestrator actionOrchestrator;

  private static ApacheDS ldap;

  private static FolderObservable sipFolderMonitor;
  private static FolderObserver sipFolderObserver;

  private static Path rodaApacheDsConfigDirectory = null;
  private static Path rodaApacheDsDataDirectory = null;

  private static Configuration rodaConfiguration = null;
  private static Map<String, String> loginProperties = null;
  private static Map<Locale, Messages> i18nMessages = new HashMap<Locale, Messages>();

  public static void instantiate() {
    if (!instantiated) {
      try {
        // determine RODA HOME
        rodaHomePath = determineRodaHomePath();

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
        System.setProperty("solr.data.dir.actionlog", indexPath.resolve("actionlog").toString());
        System.setProperty("solr.data.dir.sipreport", indexPath.resolve("sipreport").toString());
        System.setProperty("solr.data.dir.members", indexPath.resolve("members").toString());
        System.setProperty("solr.data.dir.othermetadata", indexPath.resolve("othermetadata").toString());
        System.setProperty("solr.data.dir.sip", indexPath.resolve("sip").toString());
        // FIXME added missing cores

        // start embedded solr
        solr = new EmbeddedSolrServer(solrHome, "test");

        // instantiate index related object
        index = new IndexService(solr, model, configPath);

      } catch (StorageServiceException e) {
        LOGGER.error(e);
      } catch (URISyntaxException e) {
        LOGGER.error(e);
      }

      // actionOrchestrator = new EmbeddedActionOrchestrator();
      actionOrchestrator = new AkkaEmbeddedActionOrchestrator();

      try {
        rodaConfiguration = getConfiguration("roda-wui.properties");
        processLoginRelatedProperties();
      } catch (ConfigurationException e) {
        LOGGER.error("Error loading roda-wui properties", e);
      }

      startApacheDS();

      try {
        startSIPFolderMonitor();
      } catch (Exception e) {
        LOGGER.error("Error starting SIP Monitor: " + e.getMessage());
      }

      instantiated = true;
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

  public static void ensureAllEssentialDirectoriesExist() {
    List<Path> essentialDirectories = new ArrayList<Path>();
    essentialDirectories.add(configPath);
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

  public static void reloadRodaConfigurationsAfterFileChange() {
    processLoginRelatedProperties();
    i18nMessages.clear();
    LOGGER.info("Reloaded roda configurations after file change!");
  }

  private static void checkForChangesInI18N() {
    // i18n is cached and that cache is re-done when changes occur to
    // roda-wui.properties (for convenience)
    getRodaConfiguration().getString("");
  }

  public static void shutdown() throws IOException {
    if (instantiated) {
      solr.close();
      stopApacheDS();
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

  public static void startSIPFolderMonitor() throws Exception {
    Configuration rodaConfig = RodaCoreFactory.getRodaConfiguration();
    String SIPFolderPath = rodaConfig.getString("sip.folder");
    int SIPTimeout = rodaConfig.getInt("sip.timeout");
    Path sipFolderPath = dataPath.resolve(SIPFolderPath);

    sipFolderMonitor = new FolderMonitorNIO(sipFolderPath, SIPTimeout);
    // sipFolderMonitor = new FolderMonitor(sipFolderPath, SIPTimeout);
    sipFolderObserver = new IndexFolderObserver(solr);
    sipFolderMonitor.addFolderObserver(sipFolderObserver);
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

  public static ActionOrchestrator getActionOrchestrator() {
    return actionOrchestrator;
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

  public static Configuration getConfiguration(String configurationFile) throws ConfigurationException {
    Path config = RodaCoreFactory.getConfigPath().resolve(configurationFile);
    PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
    propertiesConfiguration.setDelimiterParsingDisabled(true);
    propertiesConfiguration.setEncoding("UTF-8");

    if (Files.exists(config)) {
      LOGGER.debug("Loading configuration from file " + config);
      propertiesConfiguration.load(config.toFile());
      RodaCorePropertiesReloadStrategy rodaCorePropertiesReloadStrategy = new RodaCorePropertiesReloadStrategy();
      rodaCorePropertiesReloadStrategy.setRefreshDelay(5000);
      propertiesConfiguration.setReloadingStrategy(rodaCorePropertiesReloadStrategy);
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

  public static Map<String, String> getLoginRelatedProperties() {
    return loginProperties;
  }

  private static void processLoginRelatedProperties() {
    loginProperties = new HashMap<String, String>();

    Configuration configuration = RodaCoreFactory.getRodaConfiguration();
    Iterator<String> keys = configuration.getKeys();
    while (keys.hasNext()) {
      String key = String.class.cast(keys.next());
      String value = configuration.getString(key, "");
      if (key.startsWith("ui.menu.") || key.startsWith("ui.role.")) {
        loginProperties.put(key, value);
      }
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

  /*
   * Command-line accessible functionalities
   */
  public static void runReindexAipsAction() {
    Plugin<AIP> reindexAction = new ReindexAction();
    getActionOrchestrator().runActionOnAllAIPs(reindexAction);
  }

  public static void runReindexAipsAction(List<String> aipIds) {
    Plugin<AIP> reindexAction = new ReindexAction();
    ((ReindexAction) reindexAction).setClearIndexes(false);
    getActionOrchestrator().runActionOnAIPs(reindexAction, aipIds);
  }

  public static void runRemoveOrphansAction(String parentId) {
    try {
      Filter filter = new Filter(new EmptyKeyFilterParameter(RodaConstants.AIP_PARENT_ID));
      RemoveOrphansAction removeOrphansAction = new RemoveOrphansAction();
      removeOrphansAction.setNewParent(model.retrieveAIP(parentId));
      getActionOrchestrator().runActionFromIndex(SimpleDescriptionObject.class, filter, removeOrphansAction);
    } catch (ModelServiceException e) {
      e.printStackTrace();
    }
  }

  private static void runFixityAction() {
    Plugin<AIP> fixityAction = new FixityAction();
    getActionOrchestrator().runActionOnAllAIPs(fixityAction);
  }

  private static void runAntivirusAction() {
    Plugin<AIP> antivirusAction = new AntivirusAction();
    getActionOrchestrator().runActionOnAllAIPs(antivirusAction);
  }

  private static void runDroidAction() {
    Plugin<AIP> droidAction = new DroidAction();
    getActionOrchestrator().runActionOnAllAIPs(droidAction);
  }

  private static void runFulltextAction() {
    Plugin<AIP> fulltextAction = new FullTextAction();
    getActionOrchestrator().runActionOnAllAIPs(fulltextAction);
  }

  private static void runJhoveAction() {
    Plugin<AIP> jhoveAction = new JHOVEAction();
    getActionOrchestrator().runActionOnAllAIPs(jhoveAction);
  }

  private static void runFitsAction() {
    Plugin<AIP> fitsAction = new FITSAction();
    getActionOrchestrator().runActionOnAllAIPs(fitsAction);
  }

  private static void runBagitAction() {
    try {
      Path bagitFolder = RodaCoreFactory.getDataPath().resolve("bagit");
      Plugin<String> bagitAction = new BagitToAIPAction();
      Stream<Path> bagits = Files.list(bagitFolder);
      List<Path> bagitsList = bagits.collect(Collectors.toList());
      bagits.close();
      getActionOrchestrator().runActionOnFiles(bagitAction, bagitsList);
    } catch (IOException e) {
      LOGGER.error("Error running bagit action: " + e.getMessage(), e);
    }
  }

  private static void runCharacterizationAction() {
    Plugin<AIP> characterizationAction = new CharacterizationAction();
    getActionOrchestrator().runActionOnAllAIPs(characterizationAction);
  }

  private static void runPremisSkeletonAction() {
    Plugin<AIP> premisSkeletonAction = new PremisSkeletonAction();
    getActionOrchestrator().runActionOnAllAIPs(premisSkeletonAction);
  }

  private static void runPremisV2toV3Action() {
    Plugin<AIP> premisUpdateAction = new V2ToV3PremisAction();
    getActionOrchestrator().runActionOnAllAIPs(premisUpdateAction);
  }

  private static void runValidationAction() {
    Plugin<AIP> validationAction = new AIPValidationAction();
    getActionOrchestrator().runActionOnAllAIPs(validationAction);
  }

  private static void runLogCleanAction() {
    Plugin<AIP> logCleanAction = new LogCleanerAction();
    getActionOrchestrator().runActionOnAllAIPs(logCleanAction);
  }

  private static void runExifToolAction() {
    Plugin<AIP> exifToolAction = new ExifToolAction();
    getActionOrchestrator().runActionOnAllAIPs(exifToolAction);
  }

  private static void runMediaInfoAction() {
    Plugin<AIP> mediaInfoAction = new MediaInfoAction();
    getActionOrchestrator().runActionOnAllAIPs(mediaInfoAction);
  }

  private static void runFFProbeAction() {
    Plugin<AIP> ffProbeAction = new FFProbeAction();
    getActionOrchestrator().runActionOnAllAIPs(ffProbeAction);
  }

  private static void runJpylyzerAction() {
    Plugin<AIP> jpylyzerAction = new JpylyzerAction();
    getActionOrchestrator().runActionOnAllAIPs(jpylyzerAction);
  }

  private static void runPremisUpdateAction() {
    Plugin<AIP> jpylyzerAction = new PremisUpdateFromToolsAction();
    getActionOrchestrator().runActionOnAllAIPs(jpylyzerAction);
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

  public static void main(String[] argsArray) throws IndexServiceException {

    List<String> args = Arrays.asList(argsArray);
    if (args.size() > 0) {
      instantiate();
      if ("index".equals(args.get(0))) {
        if ("list".equals(args.get(1)) && ("users".equals(args.get(2)) || "groups".equals(args.get(2)))) {
          Filter filter = new Filter(
            new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, "users".equals(args.get(2)) ? "true" : "false"));
          printIndexMembers(args, filter, null, new Sublist(0, 10000), null);
        } else if ("reindex".equals(args.get(1)) && args.size() == 2) {
          runReindexAipsAction();
        } else if ("reindex".equals(args.get(1)) && args.size() >= 3) {
          runReindexAipsAction(args.subList(2, args.size()));
        } else if ("query".equals(args.get(1)) && args.size() == 4 && StringUtils.isNotBlank(args.get(2))
          && StringUtils.isNotBlank(args.get(3))) {
          runSolrQuery(args);
        }
      } else if ("orphans".equals(args.get(0)) && args.size() == 2 && StringUtils.isNotBlank(args.get(1))) {
        runRemoveOrphansAction(args.get(1));
      } else if ("fixity".equals(args.get(0))) {
        runFixityAction();
      } else if ("antivirus".equals(args.get(0))) {
        runAntivirusAction();
      } else if ("premisskeleton".equals(args.get(0))) {
        runPremisSkeletonAction();
      } else if ("droid".equals(args.get(0))) {
        runDroidAction();
      } else if ("fulltext".equals(args.get(0))) {
        runFulltextAction();
      } else if ("jhove".equals(args.get(0))) {
        runJhoveAction();
      } else if ("fits".equals(args.get(0))) {
        runFitsAction();
      } else if ("bagit".equals(args.get(0))) {
        runBagitAction();
      } else if ("events".equals(args.get(0))) {
        Filter filter = null;
        printPreservationEvents(filter, null, new Sublist(0, 10000), null);
      } else if ("files".equals(args.get(0))) {
        Filter filter = null;
        printPreservationFiles(filter, null, new Sublist(0, 10000), null);
      } else if ("characterization".equals(args.get(0))) {
        runCharacterizationAction();
      } else if ("v2tov3".equals(args.get(0))) {
        runPremisV2toV3Action();
      } else if ("validation".equals(args.get(0))) {
        runValidationAction();
      } else if ("logClean".equals(args.get(0))) {
        runLogCleanAction();
      } else if ("exifTool".equals(args.get(0))) {
        runExifToolAction();
      } else if ("mediaInfo".equals(args.get(0))) {
        runMediaInfoAction();
      } else if ("ffprobe".equals(args.get(0))) {
        runFFProbeAction();
      } else if ("jpylyzer".equals(args.get(0))) {
        runJpylyzerAction();
      } else if ("premisupdate".equals(args.get(0))) {
        runPremisUpdateAction();
      } else {
        printMainUsage();
      }
    } else {
      printMainUsage();
    }
    System.exit(0);
  }

}
