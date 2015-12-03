package org.roda.core.common.monitor;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.ApacheDS;
import org.roda.core.common.LdapUtility;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.UserUtility;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.Group;
import org.roda.core.data.v2.User;
import org.roda.core.index.IndexFolderObserver;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexServiceTest;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceTest;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorIndexTest {

  private static Path basePath;
  private static Path indexPath;
  private static Path logPath;
  private static StorageService storage;
  private static ModelService model;
  private static IndexService index;

  private static Path corporaPath;
  private static StorageService corporaService;

  private static ApacheDS apacheDS;

  private static final Logger logger = LoggerFactory.getLogger(ModelServiceTest.class);

  private static EmbeddedSolrServer solr;

  @BeforeClass
  public static void setUp() throws Exception {

    basePath = Files.createTempDirectory("modelTests");
    logPath = basePath.resolve("log");
    Files.createDirectory(logPath);
    indexPath = Files.createTempDirectory("indexTests");
    storage = new FileStorageService(basePath);
    model = new ModelService(storage);

    System.setProperty("roda.home", basePath.toString());
    RodaCoreFactory.instantiateTest();

    // Configure Solr
    URL solrConfigURL = IndexServiceTest.class.getResource("/config/index/solr.xml");
    Path solrConfigPath = Paths.get(solrConfigURL.toURI());
    Files.copy(solrConfigPath, indexPath.resolve("solr.xml"));
    Path aipSchema = indexPath.resolve("aip");
    Files.createDirectories(aipSchema);
    Files.createFile(aipSchema.resolve("core.properties"));

    Path solrHome = Paths.get(IndexServiceTest.class.getResource("/config/index/").toURI());
    System.setProperty("solr.data.dir", indexPath.toString());
    System.setProperty("solr.data.dir.aip", indexPath.resolve("aip").toString());
    System.setProperty("solr.data.dir.sdo", indexPath.resolve("sdo").toString());
    System.setProperty("solr.data.dir.representations", indexPath.resolve("representation").toString());
    System.setProperty("solr.data.dir.preservationobject", indexPath.resolve("preservationobject").toString());
    System.setProperty("solr.data.dir.preservationevent", indexPath.resolve("preservationevent").toString());
    System.setProperty("solr.data.dir.actionlog", indexPath.resolve("actionlog").toString());
    System.setProperty("solr.data.dir.sip", indexPath.resolve("sip").toString());
    // start embedded solr
    solr = new EmbeddedSolrServer(solrHome, "test");

    index = new IndexService(solr, model, null);

    URL corporaURL = IndexServiceTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    // set of properties that will most certainly be in a default roda
    // installation
    Configuration rodaConfig = setAndRetrieveRodaProperties();

    // start ApacheDS
    apacheDS = new ApacheDS();
    Files.createDirectories(basePath.resolve("ldapData"));
    Path ldapConfigs = Paths.get(IndexServiceTest.class.getResource("/config/ldap/").toURI());
    String ldapHost = rodaConfig.getString("ldap.host", "localhost");
    int ldapPort = rodaConfig.getInt("ldap.port", 10389);
    String ldapPeopleDN = rodaConfig.getString("ldap.peopleDN");
    String ldapGroupsDN = rodaConfig.getString("ldap.groupsDN");
    String ldapRolesDN = rodaConfig.getString("ldap.rolesDN");
    String ldapAdminDN = rodaConfig.getString("ldap.adminDN");
    String ldapAdminPassword = rodaConfig.getString("ldap.adminPassword");
    String ldapPasswordDigestAlgorithm = rodaConfig.getString("ldap.passwordDigestAlgorithm");
    List<String> ldapProtectedUsers = RodaUtils.copyList(rodaConfig.getList("ldap.protectedUsers"));
    List<String> ldapProtectedGroups = RodaUtils.copyList(rodaConfig.getList("ldap.protectedGroups"));
    apacheDS.initDirectoryService(ldapConfigs, basePath.resolve("ldapData"), ldapAdminPassword);
    apacheDS.startServer(new LdapUtility(ldapHost, ldapPort, ldapPeopleDN, ldapGroupsDN, ldapRolesDN, ldapAdminDN,
      ldapAdminPassword, ldapPasswordDigestAlgorithm, ldapProtectedUsers, ldapProtectedGroups), 10389);
    for (User user : UserUtility.getLdapUtility().getUsers(new Filter())) {
      model.addUser(user, false, true);
    }
    for (Group group : UserUtility.getLdapUtility().getGroups(new Filter())) {
      model.addGroup(group, false, true);
    }

    logger.debug("Running model test under storage: " + basePath);
  }

  private static Configuration setAndRetrieveRodaProperties() {
    Configuration rodaConfig = new BaseConfiguration();
    rodaConfig.addProperty("ldap.host", "localhost");
    rodaConfig.addProperty("ldap.port", "10389");
    rodaConfig.addProperty("ldap.peopleDN", "ou=users\\,dc=roda\\,dc=org");
    rodaConfig.addProperty("ldap.groupsDN", "ou=groups\\,dc=roda\\,dc=org");
    rodaConfig.addProperty("ldap.rolesDN", "ou=roles\\,dc=roda\\,dc=org");
    rodaConfig.addProperty("ldap.adminDN", "uid=admin\\,ou=system");
    rodaConfig.addProperty("ldap.adminPassword", "secret");
    rodaConfig.addProperty("ldap.passwordDigestAlgorithm", "MD5");
    rodaConfig.addProperty("ldap.protectedUsers",
      Arrays.asList("admin", "guest", "roda-ingest-task", "roda-wui", "roda-disseminator"));
    rodaConfig.addProperty("ldap.protectedGroups",
      Arrays.asList("administrators", "archivists", "producers", "users", "guests"));
    return rodaConfig;
  }

  @AfterClass
  public static void tearDown() throws Exception {
    apacheDS.stop();
    FSUtils.deletePath(basePath);
    FSUtils.deletePath(indexPath);
  }

  @Test
  public void testRenameOwner() {
    try {
      Path sips = Files.createTempDirectory("sips");
      IndexFolderObserver ifo = new IndexFolderObserver(solr, sips);
      WatchDir watch = new WatchDir(sips, true, null, null, Arrays.asList(ifo));
      Thread threadWatch = new Thread(watch, "FolderWatcher");
      threadWatch.start();
      Thread.sleep(1000);
      populate(sips);
      Thread.sleep(1000);
      MonitorVariables.getInstance().getTaskBlocker().acquire();
      File[] children = sips.toFile().listFiles();
      for (File f : children) {
        File parent = f.getParentFile();
        File newFolder = new File(parent, UUID.randomUUID().toString());
        if (f.isDirectory()) { // rename all owners
          FileUtils.moveDirectory(f, newFolder);
        }
      }
      MonitorVariables.getInstance().getTaskBlocker().release();
      Thread.sleep(1000);
      MonitorVariables.getInstance().getTaskBlocker().acquire();
      EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
      FileVisitorChecker fvc = new FileVisitorChecker(sips, index);
      Files.walkFileTree(sips, opts, Integer.MAX_VALUE, fvc);
      MonitorVariables.getInstance().getTaskBlocker().release();
      assertTrue(fvc.isOk());
    } catch (InterruptedException | IOException | SolrServerException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testCopyFolder() {
    try {
      Path sips = Files.createTempDirectory("sips");
      IndexFolderObserver ifo = new IndexFolderObserver(solr, sips);
      WatchDir watch = new WatchDir(sips, true, null, null, Arrays.asList(ifo));
      Thread threadWatch = new Thread(watch, "FolderWatcher");
      threadWatch.start();
      Thread.sleep(1000);
      populate(sips);
      Thread.sleep(1000);
      MonitorVariables.getInstance().getTaskBlocker().acquire();
      File[] children = sips.toFile().listFiles();
      for (File f : children) {
        File parent = f.getParentFile();
        File newFolder = new File(parent, UUID.randomUUID().toString());
        if (f.isDirectory()) {
          FileUtils.copyDirectory(f, newFolder);
        }
      }
      MonitorVariables.getInstance().getTaskBlocker().release();
      Thread.sleep(1000);
      MonitorVariables.getInstance().getTaskBlocker().acquire();
      EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
      FileVisitorChecker fvc = new FileVisitorChecker(sips, index);
      Files.walkFileTree(sips, opts, Integer.MAX_VALUE, fvc);
      MonitorVariables.getInstance().getTaskBlocker().release();
      assertTrue(fvc.isOk());
    } catch (InterruptedException | IOException | SolrServerException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testAddEmptyFolder() {
    try {
      Path sips = Files.createTempDirectory("sips");
      IndexFolderObserver ifo = new IndexFolderObserver(solr, sips);
      WatchDir watch = new WatchDir(sips, true, null, null, Arrays.asList(ifo));
      Thread threadWatch = new Thread(watch, "FolderWatcher");
      threadWatch.start();
      Thread.sleep(1000);
      populate(sips);
      Thread.sleep(1000);
      MonitorVariables.getInstance().getTaskBlocker().acquire();
      File[] children = sips.toFile().listFiles();
      for (File f : children) {
        if (f.isDirectory()) {
          File emptyFolder = new File(f, UUID.randomUUID().toString());
          emptyFolder.mkdir();
        }
      }
      MonitorVariables.getInstance().getTaskBlocker().release();
      Thread.sleep(1000);
      MonitorVariables.getInstance().getTaskBlocker().acquire();
      EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
      FileVisitorChecker fvc = new FileVisitorChecker(sips, index);
      Files.walkFileTree(sips, opts, Integer.MAX_VALUE, fvc);
      MonitorVariables.getInstance().getTaskBlocker().release();
      assertTrue(fvc.isOk());
    } catch (InterruptedException | IOException | SolrServerException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testBase() {
    try {
      Path sips = Files.createTempDirectory("sips");
      IndexFolderObserver ifo = new IndexFolderObserver(solr, sips);
      WatchDir watch = new WatchDir(sips, true, null, null, Arrays.asList(ifo));
      Thread threadWatch = new Thread(watch, "FolderWatcher");
      threadWatch.start();
      Thread.sleep(1000);
      populate(sips);
      Thread.sleep(1000);
      MonitorVariables.getInstance().getTaskBlocker().acquire();

      EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
      FileVisitorChecker fvc = new FileVisitorChecker(sips, index);
      Files.walkFileTree(sips, opts, Integer.MAX_VALUE, fvc);
      MonitorVariables.getInstance().getTaskBlocker().release();
      assertTrue(fvc.isOk());
    } catch (InterruptedException | IOException | SolrServerException t) {
      t.printStackTrace();
    }
  }

  public static void populate(Path basePath) throws IOException {
    Random randomno = new Random();
    int numberOfItemsByLevel = nextIntInRange(2, 4, randomno);
    int numberOfLevels = nextIntInRange(2, 4, randomno);
    populate(basePath, numberOfItemsByLevel, numberOfLevels, 0, randomno);

  }

  private static void populate(Path path, int numberOfItemsByLevel, int numberOfLevels, int currentLevel,
    Random randomno) throws IOException {
    currentLevel++;
    for (int i = 0; i < numberOfItemsByLevel; i++) {
      Path p = null;
      if (i % 2 == 0) {
        if (currentLevel > 1) {
          p = Files.createFile(path.resolve(UUID.randomUUID().toString() + ".txt"));
          Files.write(p, "NUNCAMAISACABA".getBytes());
        }
      } else {
        p = Files.createDirectory(path.resolve(UUID.randomUUID().toString()));
        if (currentLevel <= numberOfLevels) {
          populate(p, numberOfItemsByLevel, numberOfLevels, currentLevel, randomno);
        } else {
          if (currentLevel > 1) {
            for (int j = 0; j < numberOfItemsByLevel; j++) {
              Path temp = Files.createFile(p.resolve(UUID.randomUUID().toString() + ".txt"));
              Files.write(temp, "NUNCAMAISACABA".getBytes());
            }
          }
        }
      }
    }

  }

  static int nextIntInRange(int min, int max, Random rng) {
    if (min > max) {
      throw new IllegalArgumentException("Cannot draw random int from invalid range [" + min + ", " + max + "].");
    }
    int diff = max - min;
    if (diff >= 0 && diff != Integer.MAX_VALUE) {
      return (min + rng.nextInt(diff + 1));
    }
    int i;
    do {
      i = rng.nextInt();
    } while (i < min || i > max);
    return i;
  }
}
