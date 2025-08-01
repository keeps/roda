/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.config.TestConfig;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.index.IndexService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.orchestrate.JobsHelper;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.RandomMockContentPayload;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageTestUtils;
import org.roda.core.storage.TransactionalStorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.transaction.RODATransactionException;
import org.roda.core.transaction.RODATransactionManager;
import org.roda.core.transaction.TransactionLogService;
import org.roda.core.transaction.TransactionalContext;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@SpringBootTest(classes = TestConfig.class)
public class TransactionalModelServiceTest extends AbstractTestNGSpringContextTests {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalModelServiceTest.class);

  private static Path basePath;
  private static Path logPath;
  private static StorageService storage;
  private static ModelService model;
  private static StorageService corporaService;
  private static int fileCounter = 0;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;

  @Autowired
  private RODATransactionManager transactionManager;

  @Autowired
  private TransactionLogService transactionLogService;

  @Autowired
  private Environment environment;

  @BeforeClass
  public static void setUp() throws IOException, URISyntaxException, GenericException {
    URL corporaURL = ModelServiceTest.class.getResource("/corpora");
    Path corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    LOGGER.debug("Running model test under storage: {}", basePath);
  }

  @BeforeClass
  public void init() throws IOException, GenericException {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = false;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false, ldapUtilityTestHelper.getLdapUtility());

    logPath = RodaCoreFactory.getLogPath();
    storage = RodaCoreFactory.getStorageService();
    model = RodaCoreFactory.getModelService();

    transactionManager.setMainModelService(model);
    transactionManager.setInitialized(true);
  }

  @AfterClass
  public void cleanup() throws NotFoundException, GenericException, IOException {
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testCreateAIP() throws RODAException {
    TransactionalContext context = transactionManager.beginTransaction();
    assertNotNull(context);

    TransactionalModelService transactionalModelService = context.transactionalModelService();
    TransactionalStorageService transactionalStorageService = context.transactionalStorageService();

    // generate AIP ID
    final String aipId = IdUtils.createUUID();

    AIP aip = transactionalModelService.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_VERSION_EAD_UNKNOWN),
      RodaConstants.ADMIN);

    assertNotNull(aip);

    StoragePath aipStoragePath = ModelUtils.getAIPStoragePath(aip.getId());

    assertTrue(transactionalStorageService.exists(aipStoragePath),
      "AIP storage path should exist in the transactional storage service: " + aipStoragePath);
    assertFalse(storage.exists(aipStoragePath),
      "AIP storage path should not exist in the storage service service before transaction commit: " + aipStoragePath);

    // Commit the transaction
    // TODO: use move method from StoragePath instead copy
    transactionManager.endTransaction(context.transactionLog().getId());
    // assertFalse(transactionalStorageService.exists(aipStoragePath), "AIP storage
    // path should not exist in the transactional storage service after transaction
    // commit: " + aipStoragePath);
    assertTrue(storage.exists(aipStoragePath),
      "AIP storage path should exist in the storage service after transaction commit: " + aipStoragePath);
  }

  @Test
  public void testTransactionalLock() throws RODAException {
    final String aipId = IdUtils.createUUID();
    JobsHelper.setLockRequestTimeout(5);

    TransactionalContext context1 = transactionManager.beginTransaction();
    assertNotNull(context1);
    TransactionalModelService transactionalModelService1 = context1.transactionalModelService();

    AIP aip = transactionalModelService1.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_VERSION_EAD_UNKNOWN),
      RodaConstants.ADMIN);

    TransactionalContext context2 = transactionManager.beginTransaction();
    assertNotNull(context2);
    TransactionalModelService transactionalModelService2 = context2.transactionalModelService();

    try {
      transactionalModelService2.createAIP(aipId, corporaService, DefaultStoragePath.parse(
        CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_VERSION_EAD_UNKNOWN), RodaConstants.ADMIN);
    } catch (IllegalArgumentException e) {
      // Expected exception, as the AIP should be locked by the first transaction
      LOGGER.debug("Expected IllegalArgumentException: {}", e.getMessage());
    } catch (RODAException e) {
      Assert.fail("Unexpected exception: " + e.getMessage());
    } finally {
      transactionManager.endTransaction(context2.transactionLog().getId());
    }

    transactionManager.endTransaction(context1.transactionLog().getId());

    StoragePath aipStoragePath = ModelUtils.getAIPStoragePath(aip.getId());
    assertTrue(storage.exists(aipStoragePath),
      "AIP storage path should exist in the storage service after transaction commit: " + aipStoragePath);
  }

  @Test
  public void testTransactionalDirectAccess() throws RODAException, IOException {
    final String aipId = IdUtils.createUUID();
    AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    StoragePath aipStoragePath = ModelUtils.getAIPStoragePath(aipId);

    List<StoragePath> aipStoragePathList = new ArrayList<>();
    try (CloseableIterable<Resource> listResourcesUnderDirectory = storage.listResourcesUnderDirectory(aipStoragePath,
      true)) {
      for (Resource resource : listResourcesUnderDirectory) {
        aipStoragePathList.add(resource.getStoragePath());
      }
    }

    TransactionalContext context = transactionManager.beginTransaction();
    assertNotNull(context);
    TransactionalModelService transactionalModelService = context.transactionalModelService();
    TransactionalStorageService transactionalStorageService = context.transactionalStorageService();

    transactionalModelService.updateAIP(aip, RodaConstants.ADMIN);
    transactionalStorageService.getDirectAccess(aipStoragePath);

    List<StoragePath> stagingAipStoragePathList = new ArrayList<>();
    try (CloseableIterable<Resource> listResourcesUnderDirectory = transactionalStorageService
      .listResourcesUnderDirectory(aipStoragePath, true)) {
      for (Resource resource : listResourcesUnderDirectory) {
        stagingAipStoragePathList.add(resource.getStoragePath());
      }
    }

    assertTrue(stagingAipStoragePathList.containsAll(aipStoragePathList),
      "Missing files in the staging area: " + stagingAipStoragePathList);

    transactionManager.endTransaction(context.transactionLog().getId());
  }

  @Test
  public void testListTransactionalResourcesUnderContainer() throws RODAException {
    TransactionalContext context = transactionManager.beginTransaction();
    TransactionalStorageService transactionalStorage = context.transactionalStorageService();

    // Create a common container in both storage and transactional storage
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage.createContainer(containerStoragePath);
    transactionalStorage.createContainer(containerStoragePath);

    // Create directories in both storage and transactional storage, this should be
    // listed once
    StoragePath directoryStoragePath1 = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    storage.createDirectory(directoryStoragePath1);
    transactionalStorage.createDirectory(directoryStoragePath1);

    // Create directory in storage only
    StoragePath directoryStoragePath2 = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    storage.createDirectory(directoryStoragePath2);

    // Create directory in transactional storage only
    StoragePath directoryStoragePath3 = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    transactionalStorage.createDirectory(directoryStoragePath3);

    for (Resource resource : storage.listResourcesUnderContainer(containerStoragePath, true)) {
      System.out.println("Resource in storage: " + resource.getStoragePath());
    }

    for (Resource resource : transactionalStorage.listResourcesUnderContainer(containerStoragePath, true)) {
      System.out.println("Resource in transactionalStorage: " + resource.getStoragePath());
    }
  }

  @Test
  public void testListTransactionalResourcesUnderDirectory() throws RODAException {
    TransactionalContext context = transactionManager.beginTransaction();
    TransactionalStorageService transactionalStorage = context.transactionalStorageService();

    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    StoragePath directoryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    storage.createContainer(containerStoragePath);
    storage.createDirectory(directoryStoragePath);
    transactionalStorage.createContainer(containerStoragePath);
    transactionalStorage.createDirectory(directoryStoragePath);

    // Create a subdirectory in both storage and transactional storage, this should
    // be listed once
    StoragePath subDirectoryStoragePath1 = StorageTestUtils
      .generateRandomResourceStoragePathUnder(directoryStoragePath);
    storage.createDirectory(subDirectoryStoragePath1);
    transactionalStorage.createDirectory(subDirectoryStoragePath1);

    // Create a subdirectory in storage only
    StoragePath subDirectoryStoragePath2 = StorageTestUtils
      .generateRandomResourceStoragePathUnder(directoryStoragePath);
    storage.createDirectory(subDirectoryStoragePath2);

    // Create a subdirectory in transactional storage only
    StoragePath subDirectoryStoragePath3 = StorageTestUtils
      .generateRandomResourceStoragePathUnder(directoryStoragePath);
    transactionalStorage.createDirectory(subDirectoryStoragePath3);

    for (Resource resource : storage.listResourcesUnderDirectory(directoryStoragePath, true)) {
      System.out.println("Resource in storage: " + resource.getStoragePath());
    }

    for (Resource resource : transactionalStorage.listResourcesUnderDirectory(directoryStoragePath, true)) {
      System.out.println("Resource in transactionalStorage: " + resource.getStoragePath());
    }
  }

  @Test
  public void testGetBinary() throws RODAException {
    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage.createContainer(containerStoragePath);

    // 1) create binary
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload payload = new RandomMockContentPayload();
    storage.createBinary(binaryStoragePath, payload, false);

    // 2) get binary using transactional methods
    TransactionalContext context = transactionManager.beginTransaction();
    TransactionalStorageService transactionalStorage = context.transactionalStorageService();

    Binary binary = transactionalStorage.getBinary(binaryStoragePath);
    AssertJUnit.assertNotNull(binary);

    // 3) create the same binary again in the transactional storage
    transactionalStorage.createBinary(binaryStoragePath, payload, false);

    // 4) get binary again using transactional methods
    Binary transactionalBinary = transactionalStorage.getBinary(binaryStoragePath);
    AssertJUnit.assertNotNull(transactionalBinary);

  }

  @Test
  public void testTransactionalCopy() throws RODAException {
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage.createContainer(containerStoragePath);

    final StoragePath targetContainerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage.createContainer(targetContainerStoragePath);

    // 1) create folders
    final StoragePath directoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    final StoragePath subDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(directoryStoragePath);
    storage.createDirectory(directoryStoragePath);
    storage.createDirectory(subDirectoryStoragePath);

    final StoragePath targetDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(targetContainerStoragePath);
    System.out.println("Copying from " + directoryStoragePath + " to " + targetDirectoryStoragePath);

    // 2) create binary
    final StoragePath binaryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(subDirectoryStoragePath);
    final ContentPayload payload = new RandomMockContentPayload();
    storage.createBinary(binaryStoragePath, payload, false);

    TransactionalContext context = transactionManager.beginTransaction();
    TransactionalStorageService transactionalStorage = context.transactionalStorageService();

    transactionalStorage.copy(storage, directoryStoragePath, targetDirectoryStoragePath);
    transactionManager.endTransaction(context.transactionLog().getId());

    List<StoragePath> storagePathsUnderDirectory = new ArrayList<>();
    for (Resource resource : storage.listResourcesUnderContainer(targetContainerStoragePath, true)) {
      storagePathsUnderDirectory.add(resource.getStoragePath());
    }
    // check if the directory was copied
    AssertJUnit.assertTrue("Directory should be copied to target container",
      storagePathsUnderDirectory.contains(targetDirectoryStoragePath));
  }

  @Test
  public void testTransactionalMove() throws RODAException {
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage.createContainer(containerStoragePath);

    final StoragePath targetContainerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage.createContainer(targetContainerStoragePath);

    // 1) create folders
    final StoragePath directoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    final StoragePath subDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(directoryStoragePath);
    storage.createDirectory(directoryStoragePath);
    storage.createDirectory(subDirectoryStoragePath);

    final StoragePath targetDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(targetContainerStoragePath);

    // 2) create binary
    final StoragePath binaryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(subDirectoryStoragePath);
    final ContentPayload payload = new RandomMockContentPayload();
    storage.createBinary(binaryStoragePath, payload, false);

    TransactionalContext context = transactionManager.beginTransaction();
    TransactionalStorageService transactionalStorage = context.transactionalStorageService();

    transactionalStorage.move(storage, directoryStoragePath, targetDirectoryStoragePath);
    System.out.println("Moving from " + directoryStoragePath.asList() + " to " + targetDirectoryStoragePath.asList());
    transactionManager.endTransaction(context.transactionLog().getId());
    try {
      storage.getDirectory(directoryStoragePath);
      Assert.fail("Should have thrown NotFoundException");
    } catch (NotFoundException e) {
      // do nothing
    }
  }

  @Test
  public void testTransactionalDeleteAfterCopy() throws RODAException {
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage.createContainer(containerStoragePath);

    final StoragePath targetContainerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage.createContainer(targetContainerStoragePath);

    // 1) create folders
    final StoragePath directoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    final StoragePath subDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(directoryStoragePath);
    storage.createDirectory(directoryStoragePath);
    storage.createDirectory(subDirectoryStoragePath);

    final StoragePath targetDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(targetContainerStoragePath);
    System.out.println("Copying from " + directoryStoragePath + " to " + targetDirectoryStoragePath);

    // 2) create binary
    final StoragePath binaryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(subDirectoryStoragePath);
    final ContentPayload payload = new RandomMockContentPayload();
    storage.createBinary(binaryStoragePath, payload, false);
    final StoragePath binaryStoragePath2 = StorageTestUtils
      .generateRandomResourceStoragePathUnder(subDirectoryStoragePath);
    final ContentPayload payload2 = new RandomMockContentPayload();
    storage.createBinary(binaryStoragePath2, payload2, false);

    TransactionalContext context = transactionManager.beginTransaction();
    TransactionalStorageService transactionalStorage = context.transactionalStorageService();

    transactionalStorage.copy(storage, directoryStoragePath, targetDirectoryStoragePath);
    transactionalStorage.deleteResource(binaryStoragePath);
    transactionManager.endTransaction(context.transactionLog().getId());
  }

  /*
   * Rollback tests
   */
  @Test
  public void testAIPRollbackAfterCreate() throws RODAException {
    TransactionalContext context = transactionManager.beginTransaction();
    IndexService indexService = context.indexService();

    AIP aip = createTestAIP(context.transactionalModelService());
    StoragePath storagePath = ModelUtils.getAIPStoragePath(aip.getId());

    testRollbackAfterCreate(storagePath, context, IndexedRepresentation.class, aip);

    // Check if all representations are rolled back from the index
    Filter repAipId = new Filter();
    repAipId.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aip.getId()));
    IndexResult<IndexedRepresentation> reps = indexService.find(IndexedRepresentation.class, repAipId, null,
      new Sublist(0, 10), Collections.emptyList());
    assertEquals(0, reps.getTotalCount());

    // Check if all files are rolled back from the index
    Filter fileAipId = new Filter();
    fileAipId.add(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, aip.getId()));
    IndexResult<IndexedFile> files = indexService.find(IndexedFile.class, fileAipId, null, new Sublist(0, 10),
      Collections.emptyList());
    assertEquals(0, files.getTotalCount());
  }

  @Test
  public void testRepresentationRollbackAfterCreate() throws RODAException {
    AIP aip = createTestAIP(model);

    TransactionalContext context = transactionManager.beginTransaction();
    IndexService indexService = context.indexService();

    Representation representation = createTestRepresentation(context.transactionalModelService(), aip.getId());
    StoragePath storagePath = ModelUtils.getRepresentationStoragePath(representation.getAipId(),
      representation.getId());

    testRollbackAfterCreate(storagePath, context, IndexedRepresentation.class, representation);

    // Check if all files are rolled back from the index
    Filter fileAipId = new Filter();
    fileAipId.add(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, aip.getId()));
    IndexResult<IndexedFile> files = indexService.find(IndexedFile.class, fileAipId, null, new Sublist(0, 10),
      Collections.emptyList());
    assertEquals(0, files.getTotalCount());
  }

  @Test
  public void testFileRollbackAfterCreate() throws RODAException {
    AIP aip = createTestAIP(model);
    TransactionalContext context = transactionManager.beginTransaction();

    File file = createTestFile(context.transactionalModelService(), aip.getId());
    StoragePath storagePath = ModelUtils.getFileStoragePath(file);

    testRollbackAfterCreate(storagePath, context, IndexedRepresentation.class, file);
  }

  @Test
  public void testDescriptiveMetadataRollbackAfterCreate() throws RODAException {
    AIP aip = createTestAIP(model);
    TransactionalContext context = transactionManager.beginTransaction();

    DescriptiveMetadata descriptiveMetadata = createTestDescriptiveMetadata(context.transactionalModelService(),
      aip.getId());
    StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(descriptiveMetadata);

    testRollbackAfterCreate(storagePath, context, IndexedRepresentation.class, descriptiveMetadata);
  }

  private AIP createTestAIP(ModelService modelService) throws RODAException {
    final String aipId = IdUtils.createUUID();

    AIP aip = modelService.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_VERSION_EAD_UNKNOWN),
      RodaConstants.ADMIN);
    assertNotNull(aip);
    return aip;
  }

  private Representation createTestRepresentation(ModelService modelService, String aipId) throws RODAException {
    final String representationId = IdUtils.createUUID();

    Representation representation = modelService.createRepresentation(aipId, representationId,
      CorporaConstants.REPRESENTATION_1_ORIGINAL, CorporaConstants.REPRESENTATION_1_TYPE, corporaService,
      DefaultStoragePath.parse(CorporaConstants.OTHER_REPRESENTATION_STORAGEPATH), false, RodaConstants.ADMIN);
    assertNotNull(representation);
    return representation;
  }

  private File createTestFile(ModelService modelService, String aipId) throws RODAException {
    final String fileId = IdUtils.createUUID();

    final Binary binary = corporaService.getBinary(DefaultStoragePath.parse(CorporaConstants.OTHER_FILE_STORAGEPATH));
    File file = modelService.createFile(aipId, CorporaConstants.REPRESENTATION_1_ID, new ArrayList<>(), fileId,
      binary.getContent(), RodaConstants.ADMIN, true);
    assertNotNull(file);
    return file;
  }

  private DescriptiveMetadata createTestDescriptiveMetadata(ModelService modelService, String aipId)
    throws RODAException {
    final String descriptiveMetadataId = IdUtils.createUUID();

    final Binary binary = corporaService
      .getBinary(DefaultStoragePath.parse(CorporaConstants.OTHER_DESCRIPTIVE_METADATA_STORAGEPATH));

    final DescriptiveMetadata descriptiveMetadata = modelService.createDescriptiveMetadata(aipId, descriptiveMetadataId,
      binary.getContent(), CorporaConstants.OTHER_DESCRIPTIVE_METADATA_TYPE,
      CorporaConstants.OTHER_DESCRIPTIVE_METADATA_VERSION, RodaConstants.ADMIN);
    assertNotNull(descriptiveMetadata);
    return descriptiveMetadata;
  }

  private <T extends IsIndexed> void testRollbackAfterCreate(StoragePath storagePath, TransactionalContext context,
    Class<T> indexedClass, IsRODAObject rodaObject) throws RODATransactionException {
    assertTrue(context.transactionalStorageService().exists(storagePath),
      "Storage path should exist in the transactional storage service: " + storagePath);
    assertFalse(storage.exists(storagePath),
      "Storage path should not exist in the storage service service before transaction commit: " + storagePath);

    // Rollback the transaction
    transactionManager.rollbackTransaction(context.transactionLog().getId());

    assertFalse(storage.exists(storagePath),
      "Storage path should not exist in the storage service after transaction rollback: " + storagePath);

    try {
      context.indexService().retrieve(indexedClass, rodaObject.getId(), new ArrayList<>());
      Assert.fail("Indexed Entity should not exist after transaction rollback");
    } catch (NotFoundException e) {
      // Expected exception, as the AIP should not be indexed
      LOGGER.debug("Expected NotFoundException: {}", e.getMessage());
    } catch (RODAException e) {
      Assert.fail("Unexpected exception: " + e.getMessage());
    }
  }

  @Test
  public void testUnfinishedTransactions() throws RODAException {
    TransactionalContext context = transactionManager.beginTransaction();

    AIP aip = createTestAIP(context.transactionalModelService());
    StoragePath storagePath = ModelUtils.getAIPStoragePath(aip.getId());
    context.indexService().commit(IndexedAIP.class);

    assertTrue(context.transactionalStorageService().exists(storagePath),
      "Storage path should exist in the transactional storage service: " + storagePath);

    // Check if all representations are rolled back from the index
    Filter AipIdFilter = new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, aip.getId()));
    IndexResult<IndexedAIP> results = context.indexService().find(IndexedAIP.class, AipIdFilter, null, new Sublist(0, 10),
            Collections.emptyList());
    assertEquals("There should be one AIP indexed before transaction cleanup", 1, results.getTotalCount());

    transactionManager.cleanUnfinishedTransactions();
    context.indexService().commit(IndexedAIP.class);

    assertFalse(storage.exists(storagePath),
      "Storage path should not exist in the main storage service after transaction cleanup: " + storagePath);

    assertTrue(transactionLogService.getUnfinishedTransactions().isEmpty(),
      "There should be no unfinished transactions after cleanup");

    IndexResult<IndexedAIP> resultsAfterCleanup = context.indexService().find(IndexedAIP.class, AipIdFilter, null, new Sublist(0, 10),
      Collections.emptyList());
    assertEquals("There should be no AIPs indexed after transaction cleanup", 0, resultsAfterCleanup.getTotalCount());
  }

}
