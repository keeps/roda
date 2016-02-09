/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.roda.core.common.monitor.FolderMonitorNIO;
import org.roda.core.common.monitor.FolderObserver;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceTest;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.plugins.ingest.TransferredResourceToAIPPlugin;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalPluginsTest {

  private static Path basePath;
  private static Path logPath;
  private static ModelService model;
  private static IndexService index;

  private static Path corporaPath;
  private static StorageService corporaService;

  private static final Logger logger = LoggerFactory.getLogger(ModelServiceTest.class);

  @BeforeClass
  public static void setUp() throws Exception {

    basePath = Files.createTempDirectory("indexTests");
    System.setProperty("roda.home", basePath.toString());
    RodaCoreFactory.instantiateTest();
    logPath = RodaCoreFactory.getLogPath();
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = InternalPluginsTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    logger.info("Running internal plugins tests under storage {}", basePath);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testIngestTransferredResource() throws IOException, InterruptedException, RODAException {

    AIP root = model.createAIP(null);

    Plugin<TransferredResource> plugin = new TransferredResourceToAIPPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    plugin.setParameterValues(parameters);

    FolderMonitorNIO f = RodaCoreFactory.getFolderMonitor();

    FolderObserver observer = Mockito.mock(FolderObserver.class);
    f.addFolderObserver(observer);

    while (!f.isFullyInitialized()) {
      logger.info("Waiting for folder monitor to initialize...");
      Thread.sleep(1000);
    }

    Assert.assertTrue(f.isFullyInitialized());

    // Path corpora = corporaPath.resolve(RodaConstants.STORAGE_CONTAINER_AIP)
    // .resolve(CorporaConstants.SOURCE_AIP_REP_WITH_SUBFOLDERS).resolve(RodaConstants.STORAGE_DIRECTORY_DATA)
    // .resolve(CorporaConstants.REPRESENTATION_1_ID);
    //
    // FSUtils.copy(corpora, f.getBasePath().resolve("test"), true);

    f.createFolder(null, "test");
    f.createFolder("test", "test1");
    f.createFolder("test", "test2");
    f.createFolder("test", "test3");

    ByteArrayInputStream inputStream = new ByteArrayInputStream(RandomStringUtils.random(100).getBytes());
    f.createFile("test", "test1.txt", inputStream);
    f.createFile("test", "test2.txt", inputStream);
    f.createFile("test", "test3.txt", inputStream);
    f.createFile("test/test1", "test1.txt", inputStream);
    f.createFile("test/test1", "test2.txt", inputStream);
    f.createFile("test/test1", "test3.txt", inputStream);
    f.createFile("test/test2", "test1.txt", inputStream);
    f.createFile("test/test2", "test2.txt", inputStream);
    f.createFile("test/test2", "test3.txt", inputStream);
    f.createFile("test/test3", "test1.txt", inputStream);
    f.createFile("test/test3", "test2.txt", inputStream);
    f.createFile("test/test3", "test3.txt", inputStream);

    // TODO check if 4 times is the expected
    Mockito.verify(observer, Mockito.times(4));

    logger.info("Waiting 3s for soft-commit");
    Thread.sleep(3000);

    TransferredResource transferredResource = index.retrieve(TransferredResource.class, "test");
    Assert.assertNotNull(transferredResource);

    RodaCoreFactory.getPluginOrchestrator().runPluginOnTransferredResources(plugin, Arrays.asList(transferredResource));

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, root.getId())), null, new Sublist(0, 10));

    Assert.assertEquals(1L, find.getTotalCount());
    IndexedAIP indexedAIP = find.getResults().get(0);

    AIP aip = model.retrieveAIP(indexedAIP.getId());
    Assert.assertEquals(1, aip.getRepresentations().size());

    ClosableIterable<File> allFiles = model.listAllFiles(aip.getId(), aip.getRepresentations().get(0).getId());
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles, allFiles);

    logger.info("All files: " + reusableAllFiles);

    Assert.assertEquals(12, reusableAllFiles.size());
  }
}
