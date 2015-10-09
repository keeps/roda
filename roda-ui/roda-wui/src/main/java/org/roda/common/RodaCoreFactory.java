package org.roda.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.roda.action.antivirus.AntivirusAction;
import org.roda.action.fixity.FixityAction;
import org.roda.action.ingest.fastCharacterization.FastCharacterizationAction;
import org.roda.action.ingest.fulltext.FullTextAction;
import org.roda.action.ingest.premisSkeleton.PremisSkeletonAction;
import org.roda.action.orchestrate.ActionOrchestrator;
import org.roda.action.orchestrate.Plugin;
import org.roda.action.orchestrate.actions.ReindexAction;
import org.roda.action.orchestrate.actions.RemoveOrphansAction;
import org.roda.action.orchestrate.embed.AkkaEmbeddedActionOrchestrator;
import org.roda.core.common.RodaConstants;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.Group;
import org.roda.core.data.v2.IndexResult;
import org.roda.core.data.v2.RODAMember;
import org.roda.core.data.v2.SimpleDescriptionObject;
import org.roda.core.data.v2.User;
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
  private static Path rodaApacheDsConfigDirectory = null;
  private static Path rodaApacheDsDataDirectory = null;

  private static Configuration rodaConfiguration = null;
  private static Map<String, String> loginProperties = null;
  private static Map<Locale, Messages> i18nMessages = new HashMap<Locale, Messages>();

  public static void instantiate() {
    if (!instantiated) {
      try {
        String RODA_HOME;
        if (System.getProperty("roda.home") != null) {
          RODA_HOME = System.getProperty("roda.home");
        } else if (System.getenv("RODA_HOME") != null) {
          RODA_HOME = System.getenv("RODA_HOME");
        } else {
          RODA_HOME = null;
        }

        dataPath = Paths.get(RODA_HOME, "data");
        logPath = dataPath.resolve("log");
        configPath = Paths.get(RODA_HOME, "config");

        storagePath = dataPath.resolve("storage");
        indexPath = dataPath.resolve("index");

        storage = new FileStorageService(storagePath);
        model = new ModelService(storage);

        // Configure Solr
        Path solrHome = Paths.get(RODA_HOME, "config", "index");
        if (!Files.exists(solrHome)) {
          solrHome = Paths.get(RodaCoreFactory.class.getResource("/index/").toURI());
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
        // FIXME added missing cores

        // start embedded solr
        solr = new EmbeddedSolrServer(solrHome, "test");

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

      instantiated = true;
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

  public static InputStream getConfigurationFile(String relativePath) {
    InputStream ret;
    Path staticConfig = getConfigPath().resolve(relativePath);

    if (Files.exists(staticConfig)) {
      try {
        ret = new FileInputStream(staticConfig.toFile());
        LOGGER.info("Using static configuration");
      } catch (FileNotFoundException e) {
        LOGGER.warn("Couldn't find static configuration file - " + staticConfig);
        LOGGER.info("Using internal configuration");
        ret = RodaCoreFactory.class.getResourceAsStream("/config/" + relativePath);
      }
    } else {
      LOGGER.info("Using internal configuration");
      ret = RodaCoreFactory.class.getResourceAsStream("/config/" + relativePath);
    }
    return ret;
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
      InputStream inputStream = RodaCoreFactory.class.getResourceAsStream(configurationFile);
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

  private static void runFastCharacterizationAction() {
    Plugin<AIP> fastCharacterizationAction = new FastCharacterizationAction();
    getActionOrchestrator().runActionOnAllAIPs(fastCharacterizationAction);
  }

  private static void runFulltextAction() {
    Plugin<AIP> fulltextAction = new FullTextAction();
    getActionOrchestrator().runActionOnAllAIPs(fulltextAction);
  }

  private static void runPremisSkeletonAction() {
    Plugin<AIP> premisSkeletonAction = new PremisSkeletonAction();
    getActionOrchestrator().runActionOnAllAIPs(premisSkeletonAction);
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
    System.err.println("java -jar x.jar fastcharacterization");
    System.err.println("java -jar x.jar fulltext");
  }

  private static void printIndexMembers(List<String> args, Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws IndexServiceException {
    System.out.println("index list " + args.get(2));
    IndexResult<RODAMember> users = index.find(RODAMember.class, filter, sorter, sublist, facets);
    for (RODAMember rodaMember : users.getResults()) {
      System.out.println("\t" + rodaMember);
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
      } else if ("fastcharacterization".equals(args.get(0))) {
        runFastCharacterizationAction();
      } else if ("fulltext".equals(args.get(0))) {
        runFulltextAction();
      } else {
        printMainUsage();
      }
    } else {
      printMainUsage();
    }
    System.exit(0);
  }

}
