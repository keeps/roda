/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
import java.util.Random;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.index.IndexFolderObserver;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.InternalPluginsTest;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorIndexTest {
  private static final int AUTO_COMMIT_TIMEOUT = 3000;

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorIndexTest.class);

  private static Path basePath;
  private static Path logPath;
  private static ModelService model;
  private static IndexService index;
  private static SolrClient solr;

  private static Path corporaPath;
  private static StorageService corporaService;

  @BeforeClass
  public static void setUp() throws Exception {

    basePath = Files.createTempDirectory("indexTests");
    System.setProperty("roda.home", basePath.toString());

    boolean deploySolr = true;
    boolean deployLdap = false;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager);
    logPath = RodaCoreFactory.getLogPath();
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();
    solr = RodaCoreFactory.getSolr();

    URL corporaURL = InternalPluginsTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    LOGGER.info("Running folder monitor tests under storage {}", basePath);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testCopyFolder() throws InterruptedException, IOException, SolrServerException, GenericException {
    String transferredResourcesFolder = RodaCoreFactory.getRodaConfiguration().getString("transferredResources.folder",
      RodaConstants.CORE_TRANSFERREDRESOURCE_FOLDER);
    Path sips = RodaCoreFactory.getDataPath().resolve(transferredResourcesFolder);
    IndexFolderObserver ifo = new IndexFolderObserver(solr, sips);
    WatchDir watch = new WatchDir(sips, true, null, null, Arrays.asList(ifo));
    Thread threadWatch = new Thread(watch, "FolderWatcher");
    threadWatch.start();
    Thread.sleep(AUTO_COMMIT_TIMEOUT);
    populate(sips);
    Thread.sleep(AUTO_COMMIT_TIMEOUT);
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
    Thread.sleep(AUTO_COMMIT_TIMEOUT);
    MonitorVariables.getInstance().getTaskBlocker().acquire();

    index.commit(TransferredResource.class);

    FileVisitorChecker fvc = new FileVisitorChecker(sips, index);
    EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
    Files.walkFileTree(sips, opts, Integer.MAX_VALUE, fvc);
    MonitorVariables.getInstance().getTaskBlocker().release();
    assertTrue("Could not find " + fvc.getPathsNotFound().size() + " paths, other errors in log", fvc.isOk());
  }

  @Test
  public void testAddEmptyFolder() throws IOException, InterruptedException, SolrServerException, GenericException {

    String transferredResourcesFolder = RodaCoreFactory.getRodaConfiguration().getString("transferredResources.folder",
      RodaConstants.CORE_TRANSFERREDRESOURCE_FOLDER);
    Path sips = RodaCoreFactory.getDataPath().resolve(transferredResourcesFolder);
    IndexFolderObserver ifo = new IndexFolderObserver(solr, sips);
    WatchDir watch = new WatchDir(sips, true, null, null, Arrays.asList(ifo));
    Thread threadWatch = new Thread(watch, "FolderWatcher");
    threadWatch.start();
    Thread.sleep(AUTO_COMMIT_TIMEOUT);
    populate(sips);
    Thread.sleep(AUTO_COMMIT_TIMEOUT);
    MonitorVariables.getInstance().getTaskBlocker().acquire();
    File[] children = sips.toFile().listFiles();
    for (File f : children) {
      if (f.isDirectory()) {
        File emptyFolder = new File(f, UUID.randomUUID().toString());
        emptyFolder.mkdir();
      }
    }
    MonitorVariables.getInstance().getTaskBlocker().release();
    Thread.sleep(AUTO_COMMIT_TIMEOUT);
    MonitorVariables.getInstance().getTaskBlocker().acquire();

    index.commit(TransferredResource.class);

    FileVisitorChecker fvc = new FileVisitorChecker(sips, index);
    EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
    Files.walkFileTree(sips, opts, Integer.MAX_VALUE, fvc);
    MonitorVariables.getInstance().getTaskBlocker().release();
    assertTrue("Could not find " + fvc.getPathsNotFound().size() + " paths, other errors in log", fvc.isOk());

  }

  @Test
  public void testBase() throws IOException, InterruptedException, SolrServerException, GenericException {
    String transferredResourcesFolder = RodaCoreFactory.getRodaConfiguration().getString("transferredResources.folder",
      RodaConstants.CORE_TRANSFERREDRESOURCE_FOLDER);
    Path sips = RodaCoreFactory.getDataPath().resolve(transferredResourcesFolder);
    IndexFolderObserver ifo = new IndexFolderObserver(solr, sips);
    WatchDir watch = new WatchDir(sips, true, null, null, Arrays.asList(ifo));
    Thread threadWatch = new Thread(watch, "FolderWatcher");
    threadWatch.start();
    Thread.sleep(AUTO_COMMIT_TIMEOUT);
    populate(sips);
    Thread.sleep(AUTO_COMMIT_TIMEOUT);
    MonitorVariables.getInstance().getTaskBlocker().acquire();

    index.commit(TransferredResource.class);

    FileVisitorChecker fvc = new FileVisitorChecker(sips, index);
    EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
    Files.walkFileTree(sips, opts, Integer.MAX_VALUE, fvc);
    MonitorVariables.getInstance().getTaskBlocker().release();
    assertTrue("Could not find " + fvc.getPathsNotFound().size() + " paths, other errors in log", fvc.isOk());
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
