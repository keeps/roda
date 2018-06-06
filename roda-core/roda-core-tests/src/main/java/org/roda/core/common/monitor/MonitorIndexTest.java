/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.monitor;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.index.IndexService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class MonitorIndexTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorIndexTest.class);

  private static Path basePath;
  private static IndexService index;
  private static int fileCounter;

  @BeforeClass
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(MonitorIndexTest.class, true);

    boolean deploySolr = true;
    boolean deployLdap = false;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources);

    index = RodaCoreFactory.getIndexService();
    fileCounter = 0;

    LOGGER.info("Running folder monitor tests under storage {}", basePath);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testUpdate() throws Exception {
    String transferredResourcesFolder = RodaCoreFactory.getRodaConfiguration().getString("transferredResources.folder",
      RodaConstants.CORE_TRANSFERREDRESOURCE_FOLDER);
    Path sips = RodaCoreFactory.getDataPath().resolve(transferredResourcesFolder);
    TransferredResourcesScanner monitor = new TransferredResourcesScanner(sips, index);
    populate(sips);
    monitor.updateTransferredResources(Optional.empty(), true);

    int folderToIndex = -1;
    IndexResult<TransferredResource> transferredResources = index.find(TransferredResource.class, null, null,
      new Sublist(0, fileCounter), new ArrayList<>());
    int result1 = transferredResources.getResults().size();
    assertTrue(result1 > 0);

    for (int i = 0; i < result1; i++) {
      TransferredResource tr = transferredResources.getResults().get(i);

      if (!tr.isFile()) {
        folderToIndex = i;
      }
    }

    TransferredResource resource = transferredResources.getResults().get(folderToIndex);
    Path parent = Paths.get(resource.getFullPath());
    Path p = Files.createFile(parent.resolve(IdUtils.createUUID() + ".txt"));
    Files.write(p, "NUNCAMAISACABA".getBytes());

    monitor.updateTransferredResources(Optional.of(resource.getRelativePath()), true);

    IndexResult<TransferredResource> transferredResources2 = index.find(TransferredResource.class, null, null,
      new Sublist(0, fileCounter + 1), new ArrayList<>());
    int result2 = transferredResources2.getResults().size();
    assertTrue(result2 == result1 + 1);
  }

  @Test
  public void testRemoveFile() throws Exception {
    String transferredResourcesFolder = RodaCoreFactory.getRodaConfiguration().getString("transferredResources.folder",
      RodaConstants.CORE_TRANSFERREDRESOURCE_FOLDER);
    Path sips = RodaCoreFactory.getDataPath().resolve(transferredResourcesFolder);
    TransferredResourcesScanner monitor = new TransferredResourcesScanner(sips, index);
    populate(sips);
    monitor.updateTransferredResources(Optional.empty(), true);

    int toRemove1 = -1;
    int toRemove2 = -1;
    IndexResult<TransferredResource> transferredResources = index.find(TransferredResource.class, null, null,
      new Sublist(0, fileCounter), new ArrayList<>());
    int resultBeforeRemoves = transferredResources.getResults().size();

    for (int i = 0; i < resultBeforeRemoves; i++) {
      TransferredResource tr = transferredResources.getResults().get(i);

      if (tr.isFile() && toRemove1 == -1) {
        toRemove1 = i;
        continue;
      }

      if (tr.isFile() && toRemove1 != -1) {
        toRemove2 = i;
      }
    }

    TransferredResource resource = transferredResources.getResults().get(toRemove1);
    File firstFileRemoved = new File(resource.getFullPath());
    firstFileRemoved.delete();

    TransferredResource resource2 = transferredResources.getResults().get(toRemove2);
    File secondFileRemoved = new File(resource2.getFullPath());
    secondFileRemoved.delete();

    monitor.updateTransferredResources(Optional.empty(), true);

    IndexResult<TransferredResource> transferredResources2 = index.find(TransferredResource.class, null, null,
      new Sublist(0, fileCounter), new ArrayList<>());
    int resultAfterRemoves = transferredResources2.getResults().size();

    assertEquals(resultBeforeRemoves, resultAfterRemoves + 2);
  }

  @Test
  public void testRemoveFolder() throws Exception {
    String transferredResourcesFolder = RodaCoreFactory.getRodaConfiguration().getString("transferredResources.folder",
      RodaConstants.CORE_TRANSFERREDRESOURCE_FOLDER);
    Path sips = RodaCoreFactory.getDataPath().resolve(transferredResourcesFolder);
    TransferredResourcesScanner monitor = new TransferredResourcesScanner(sips, index);
    populate(sips);
    monitor.updateTransferredResources(Optional.empty(), true);

    int toRemove = -1;
    IndexResult<TransferredResource> transferredResources = index.find(TransferredResource.class, null, null,
      new Sublist(0, fileCounter), new ArrayList<>());
    int resultBeforeRemoves = transferredResources.getResults().size();

    for (int i = 0; i < resultBeforeRemoves; i++) {
      TransferredResource tr = transferredResources.getResults().get(i);

      if (!tr.isFile() && toRemove == -1) {
        toRemove = i;
      }
    }

    TransferredResource resource = transferredResources.getResults().get(toRemove);
    FileUtils.deleteDirectory(new File(resource.getFullPath()));

    monitor.updateTransferredResources(Optional.empty(), true);

    IndexResult<TransferredResource> transferredResources2 = index.find(TransferredResource.class, null, null,
      new Sublist(0, fileCounter), new ArrayList<>());
    int resultAfterRemoves = transferredResources2.getResults().size();
    assertTrue(resultBeforeRemoves > resultAfterRemoves);
  }

  @Test
  public void testRemoveFileOnSpecificFolder() throws Exception {
    String transferredResourcesFolder = RodaCoreFactory.getRodaConfiguration().getString("transferredResources.folder",
      RodaConstants.CORE_TRANSFERREDRESOURCE_FOLDER);
    Path sips = RodaCoreFactory.getDataPath().resolve(transferredResourcesFolder);
    TransferredResourcesScanner monitor = new TransferredResourcesScanner(sips, index);
    populate(sips);

    monitor.updateTransferredResources(Optional.empty(), true);

    int toRemove = -1;
    String folder = "";
    int folderIndex = -1;
    IndexResult<TransferredResource> transferredResources = index.find(TransferredResource.class, null, null,
      new Sublist(0, fileCounter), new ArrayList<>());
    int resultBeforeRemoves = transferredResources.getResults().size();

    for (int i = 0; i < resultBeforeRemoves; i++) {
      TransferredResource tr = transferredResources.getResults().get(i);

      if (!tr.isFile() && folderIndex == -1) {
        folder = tr.getRelativePath();
        folderIndex = i;
        break;
      }
    }

    for (int i = 0; i < resultBeforeRemoves; i++) {
      TransferredResource tr = transferredResources.getResults().get(i);

      if (tr.isFile() && toRemove == -1 && tr.getAncestorsPaths().contains(folder)) {
        toRemove = i;
      }
    }

    TransferredResource resource = transferredResources.getResults().get(toRemove);
    File fileRemoved = new File(resource.getFullPath());
    fileRemoved.delete();

    TransferredResource transferredResourceFolder = transferredResources.getResults().get(folderIndex);
    monitor.updateTransferredResources(Optional.of(transferredResourceFolder.getRelativePath()), true);

    IndexResult<TransferredResource> transferredResources2 = index.find(TransferredResource.class, null, null,
      new Sublist(0, fileCounter), new ArrayList<>());
    int resultAfterRemoves = transferredResources2.getResults().size();

    assertEquals(resultBeforeRemoves, resultAfterRemoves + 1);
  }

  private static void populate(Path basePath) throws IOException {
    Random randomno = new Random();
    int numberOfItemsByLevel = nextIntInRange(2, 3, randomno);
    int numberOfLevels = nextIntInRange(2, 3, randomno);
    populate(basePath, numberOfItemsByLevel, numberOfLevels, 0, randomno);
  }

  private static void populate(Path path, int numberOfItemsByLevel, int numberOfLevels, int currentLevel,
    Random randomno) throws IOException {
    currentLevel++;
    for (int i = 0; i < numberOfItemsByLevel; i++) {
      Path p;
      if (i % 2 == 0) {
        if (currentLevel > 1) {
          p = Files.createFile(path.resolve(IdUtils.createUUID() + ".txt"));
          Files.write(p, "NUNCAMAISACABA".getBytes());
          fileCounter++;
        }
      } else {
        p = Files.createDirectory(path.resolve(IdUtils.createUUID()));
        fileCounter++;
        if (currentLevel <= numberOfLevels) {
          populate(p, numberOfItemsByLevel, numberOfLevels, currentLevel, randomno);
        } else {
          if (currentLevel > 1) {
            for (int j = 0; j < numberOfItemsByLevel; j++) {
              Path temp = Files.createFile(p.resolve(IdUtils.createUUID() + ".txt"));
              Files.write(temp, "NUNCAMAISACABA".getBytes());
              fileCounter++;
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
      return min + rng.nextInt(diff + 1);
    }
    int i;
    do {
      i = rng.nextInt();
    } while (i < min || i > max);
    return i;
  }
}
