/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceTest;
import org.roda.core.plugins.plugins.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.internal.DeleteRODAObjectPlugin;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL})
public class FilterTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(FilterTest.class);

  private static Path basePath;

  private static Path corporaPath;
  private static StorageService corporaService;

  private static ModelService model;
  private static IndexService index;

  private static final int PAGE_SIZE = 5;
  private static final int PREMIS_CORPORA_SIZE = 8474;
  private static final String REPRESENTATION_ID = CorporaConstants.REPRESENTATION_1_ID;

  @BeforeClass
  public static void setUp() throws URISyntaxException, GenericException {
    URL corporaURL = ModelServiceTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);
    LOGGER.debug("Running model test under storage: {}", basePath);
  }

  @BeforeClass
  public void init() throws IOException {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources);

    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    IterableIndexResult.injectSearchPageSize(PAGE_SIZE);
  }

  @AfterClass
  public void cleanup() throws NotFoundException, GenericException, IOException {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testFiltersWhenDeletingAIPRecursively() throws RODAException {
    AIP aip = model.createAIP(IdUtils.createUUID(), corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_EMPTY),
      RodaConstants.ADMIN);

    LOGGER.info("Generating testing corpora");
    for (int i = 0; i < 10000; i++) {
      AIP childAIP = model.createAIP(IdUtils.createUUID(), corporaService,
        DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_EMPTY),
        RodaConstants.ADMIN);
      childAIP.setParentId(aip.getId());
      model.updateAIP(childAIP, RodaConstants.ADMIN);
    }
    LOGGER.info("Done generating testing corpora");

    index.commitAIPs();
    TestsHelper.executeJob(DeleteRODAObjectPlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(IndexedAIP.class, aip.getId()));

    index.commitAIPs();
    assertEquals(0, index.count(IndexedAIP.class, Filter.ALL).intValue());
  }

  @Test
  public void testRunFromFilter() throws RODAException {
    AIP aip = model.createAIP(IdUtils.createUUID(), corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    final List<String> filePath = new ArrayList<>();

    LOGGER.info("Generating testing corpora");
    for (int i = 0; i < PREMIS_CORPORA_SIZE; i++) {
      model.createFile(aip.getId(), REPRESENTATION_ID, filePath, "file_" + i, new StringContentPayload(""), true);
    }
    LOGGER.info("Done generating testing corpora");

    index.commit(IndexedFile.class);
    Filter filter = new Filter(new EmptyKeyFilterParameter(RodaConstants.FILE_HASH));
    SelectedItemsFilter selectedItems = new SelectedItemsFilter(filter, IndexedFile.class.getName(), false);

    Job premisJob = TestsHelper.executeJob(PremisSkeletonPlugin.class, PluginType.AIP_TO_AIP, selectedItems);

    assertTrue(premisJob.getJobStats().getSourceObjectsCount() > 0);
    assertTrue(premisJob.getJobStats().getSourceObjectsProcessedWithSuccess() > 0);

    index.commit(IndexedFile.class);
    assertEquals(0, index.count(IndexedFile.class, filter).intValue());

    // cleanup
    model.deleteAIP(aip.getId());
  }

  @Test
  public void testRunFromFilterWithDeleteThread() throws RODAException {
    AIP aip = model.createAIP(IdUtils.createUUID(), corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    final List<String> filePath = new ArrayList<>();

    LOGGER.info("Generating testing corpora");
    for (int i = 0; i < PREMIS_CORPORA_SIZE; i++) {
      String id = "file_" + i;
      model.createFile(aip.getId(), REPRESENTATION_ID, filePath, id, new StringContentPayload(""), true);
    }
    LOGGER.info("Done generating testing corpora");

    index.commit(IndexedFile.class);
    Filter filter = new Filter(new EmptyKeyFilterParameter(RodaConstants.FILE_HASH));
    SelectedItemsFilter selectedItems = new SelectedItemsFilter(filter, IndexedFile.class.getName(), false);

    Thread removeThread = new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < PREMIS_CORPORA_SIZE; i++) {
          try {
            String id = "file_" + i;
            String uuid = IdUtils.getFileId(aip.getId(), REPRESENTATION_ID, filePath, id);
            index.delete(IndexedFile.class, Arrays.asList(uuid));

            if (i % 5 == 0) {
              index.commit(IndexedFile.class);
            }

            Thread.sleep(10);
          } catch (RequestNotValidException | GenericException | InterruptedException e) {
            // do nothing
          }
        }
      }
    });

    removeThread.start();
    Job premisJob = TestsHelper.executeJob(PremisSkeletonPlugin.class, PluginType.AIP_TO_AIP, selectedItems);
    removeThread.stop();

    assertTrue(premisJob.getJobStats().getSourceObjectsCount() > 0);
    assertTrue(premisJob.getJobStats().getSourceObjectsProcessedWithSuccess() > 0);
    index.commit(IndexedFile.class);

    for (int i = 0; i < PREMIS_CORPORA_SIZE; i++) {
      try {
        String id = "file_" + i;
        String uuid = IdUtils.getFileId(aip.getId(), REPRESENTATION_ID, filePath, id);
        index.retrieve(IndexedFile.class, uuid, Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.FILE_HASH));

        // assertTrue("File " + id + " has no hash field", 0 <
        // file.getHash().size());
      } catch (NotFoundException e) {
        // do nothing (it is normal)
      }
    }

    // cleanup
    model.deleteAIP(aip.getId());
  }

  @Test
  public void testRunFromFilterWithCreateThread() throws RODAException {
    AIP aip = model.createAIP(IdUtils.createUUID(), corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    final List<String> filePath = new ArrayList<>();

    LOGGER.info("Generating testing corpora");
    for (int i = 0; i < PREMIS_CORPORA_SIZE; i++) {
      String id = "file_" + i;
      model.createFile(aip.getId(), REPRESENTATION_ID, filePath, id, new StringContentPayload(""), true);
    }
    LOGGER.info("Done generating testing corpora");

    index.commit(IndexedFile.class);
    Filter filter = new Filter(new EmptyKeyFilterParameter(RodaConstants.FILE_HASH));
    SelectedItemsFilter selectedItems = new SelectedItemsFilter(filter, IndexedFile.class.getName(), false);

    Thread createThread = new Thread(new Runnable() {
      @Override
      public void run() {
        for (int i = PREMIS_CORPORA_SIZE; i < 10000; i++) {
          try {
            String id = "file_" + i;
            model.createFile(aip.getId(), REPRESENTATION_ID, filePath, id, new StringContentPayload(""), true);

            if (i % 20 == 0) {
              index.commit(IndexedFile.class);
            }

            Thread.sleep(3000);
          } catch (RequestNotValidException | GenericException | InterruptedException | AlreadyExistsException
            | AuthorizationDeniedException | NotFoundException e) {
            // do nothing
          }
        }
      }
    });

    createThread.start();
    Job premisJob = TestsHelper.executeJob(PremisSkeletonPlugin.class, PluginType.AIP_TO_AIP, selectedItems);
    createThread.stop();

    assertTrue(premisJob.getJobStats().getSourceObjectsCount() > 0);
    assertTrue(premisJob.getJobStats().getSourceObjectsProcessedWithSuccess() > 0);
    index.commit(IndexedFile.class);

    for (int i = 0; i < PREMIS_CORPORA_SIZE; i++) {
      String id = "file_" + i;

      try {
        String uuid = IdUtils.getFileId(aip.getId(), REPRESENTATION_ID, filePath, id);
        IndexedFile file = index.retrieve(IndexedFile.class, uuid,
          Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.FILE_HASH));

        assertTrue("File " + id + " has no hash field", 0 < file.getHash().size());
      } catch (NotFoundException e) {
        assertTrue("File " + id + " was not found on index", false);
      }
    }

    // cleanup
    model.deleteAIP(aip.getId());
  }

}
