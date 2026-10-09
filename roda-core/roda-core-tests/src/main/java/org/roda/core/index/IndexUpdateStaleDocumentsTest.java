/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * AIP and representation updates reindex in place and remove only the
 * documents of objects that no longer exist.
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class IndexUpdateStaleDocumentsTest {
  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;
  private static StorageService corporaService;

  @BeforeClass
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(IndexUpdateStaleDocumentsTest.class, true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = false;
    boolean deployPluginManager = false;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false, ldapUtilityTestHelper.getLdapUtility());

    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = IndexUpdateStaleDocumentsTest.class.getResource("/corpora");
    corporaService = new FileStorageService(Paths.get(corporaURL.toURI()));
  }

  @AfterClass
  public static void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @AfterMethod
  public void cleanUp() throws RODAException {
    try (
      IterableIndexResult<IndexedAIP> result = index.findAll(IndexedAIP.class, Filter.ALL, Collections.emptyList())) {
      for (IndexedAIP aip : result) {
        try {
          model.deleteAIP(aip.getId());
        } catch (NotFoundException e) {
          // do nothing
        }
      }
      index.clearAIPs();
    } catch (IOException e) {
      throw new RODAException(e);
    } finally {
      index.commitAIPs();
    }
  }

  private AIP createAIP() throws RODAException {
    String aipId = IdUtils.createUUID();
    AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);
    index.commitAIPs();
    return aip;
  }

  /** Removes a file from storage without notifying the index. */
  private void removeFileFromStorage(String aipId, String representationId, List<String> path, String fileId)
    throws RODAException {
    RodaCoreFactory.getStorageService()
      .deleteResource(ModelUtils.getFileStoragePath(aipId, representationId, path, fileId));
  }

  private long countFiles(String aipId) throws RODAException {
    return index.count(IndexedFile.class, new Filter(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, aipId)));
  }

  private boolean fileIsIndexed(String aipId, String representationId, List<String> path, String fileId)
    throws RODAException {
    try {
      index.retrieve(IndexedFile.class, IdUtils.getFileId(aipId, representationId, path, fileId),
        Collections.emptyList());
      return true;
    } catch (NotFoundException e) {
      return false;
    }
  }

  @Test
  public void testRepresentationUpdateRemovesOnlyStaleFiles() throws RODAException {
    AIP aip = createAIP();
    String repId = CorporaConstants.REPRESENTATION_1_ID;
    long filesBefore = countFiles(aip.getId());
    Assert.assertTrue(fileIsIndexed(aip.getId(), repId, CorporaConstants.REPRESENTATION_1_FILE_2_PATH,
      CorporaConstants.REPRESENTATION_1_FILE_2_ID));

    removeFileFromStorage(aip.getId(), repId, CorporaConstants.REPRESENTATION_1_FILE_2_PATH,
      CorporaConstants.REPRESENTATION_1_FILE_2_ID);
    Representation representation = model.retrieveRepresentation(aip.getId(), repId);
    model.notifyRepresentationUpdated(representation).failOnError();
    index.commitAIPs();

    Assert.assertEquals(countFiles(aip.getId()), filesBefore - 1);
    Assert.assertFalse(fileIsIndexed(aip.getId(), repId, CorporaConstants.REPRESENTATION_1_FILE_2_PATH,
      CorporaConstants.REPRESENTATION_1_FILE_2_ID));
    Assert.assertTrue(fileIsIndexed(aip.getId(), repId, CorporaConstants.REPRESENTATION_1_FILE_1_PATH,
      CorporaConstants.REPRESENTATION_1_FILE_1_ID));
    // the representation itself is still indexed
    index.retrieve(IndexedRepresentation.class, IdUtils.getRepresentationId(aip.getId(), repId),
      Collections.emptyList());
  }

  @Test
  public void testAIPUpdateRemovesOnlyStaleDocuments() throws RODAException {
    AIP aip = createAIP();
    String repId = CorporaConstants.REPRESENTATION_1_ID;
    long filesBefore = countFiles(aip.getId());
    long representationsBefore = index.count(IndexedRepresentation.class,
      new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aip.getId())));

    removeFileFromStorage(aip.getId(), repId, CorporaConstants.REPRESENTATION_1_FILE_2_PATH,
      CorporaConstants.REPRESENTATION_1_FILE_2_ID);
    model.notifyAipUpdated(aip.getId());
    index.commitAIPs();

    Assert.assertEquals(countFiles(aip.getId()), filesBefore - 1);
    Assert.assertFalse(fileIsIndexed(aip.getId(), repId, CorporaConstants.REPRESENTATION_1_FILE_2_PATH,
      CorporaConstants.REPRESENTATION_1_FILE_2_ID));
    Assert.assertEquals(index.count(IndexedRepresentation.class,
      new Filter(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aip.getId()))), representationsBefore);
    IndexedAIP indexedAIP = index.retrieve(IndexedAIP.class, aip.getId(), Collections.emptyList());
    Assert.assertEquals(indexedAIP.getTitle(), "My example");
  }

  @Test
  public void testAIPUpdateWithoutChangesKeepsAllDocuments() throws RODAException {
    AIP aip = createAIP();
    long filesBefore = countFiles(aip.getId());

    model.notifyAipUpdated(aip.getId());
    index.commitAIPs();

    Assert.assertEquals(countFiles(aip.getId()), filesBefore);
    Assert.assertTrue(filesBefore > 0);
  }
}
