package org.roda.core.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.transaction.RODATransactionException;
import org.roda.core.transaction.RODATransactionManager;
import org.roda.core.transaction.TransactionalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class TransactionalStorageServiceTest extends AbstractStorageServiceTest<TransactionalStorageService> {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalStorageServiceTest.class);

  private static LdapUtilityTestHelper ldapUtilityTestHelper;
  private static Path basePath;
  private static StorageService mainStorage;
  private static TransactionalStorageService currentTransactionalStorageService;

  @Autowired
  private RODATransactionManager transactionManager;

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

    mainStorage = new FileStorageService(basePath);
  }

  @AfterClass
  public void cleanUp() {
    LOGGER.debug("Cleanning up");
    try {
      // recursively delete directory
      Files.walkFileTree(basePath, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }

      });
      // re-create directory
      Files.createDirectory(basePath);
    } catch (IOException e) {
      LOGGER.error("Could not clean up", e);
    }
  }

  @Override
  protected TransactionalStorageService getStorage() {
    return null;
  }

  @Override
  @Test
  public void testClassInstantiation() throws RODAException {

    try {
      // 1) Directory doesn't exist and is located under a read-only
      // directory
      Path parentDirForFolderCreationFailure = Files.createTempDirectory("xpto_dir");
      parentDirForFolderCreationFailure.toFile().setExecutable(true);
      parentDirForFolderCreationFailure.toFile().setReadable(true);
      parentDirForFolderCreationFailure.toFile().setWritable(false);
      Path folderCreationFailurePath = parentDirForFolderCreationFailure.resolve("subfolder");
      try {
        new FileStorageService(folderCreationFailurePath);
      } catch (GenericException e) {
        // do nothing
      }

      // 2) basePath is a file
      Path fileAsBasePath = Files.createTempFile("xpto", null);
      try {
        new FileStorageService(fileAsBasePath);
      } catch (GenericException e) {
        // do nothing
      }

      // 3) no read permission on basePath
      Path directoryWithoutReadPermission = Files.createTempDirectory("xpto_dir");
      directoryWithoutReadPermission.toFile().setReadable(false);
      try {
        new FileStorageService(directoryWithoutReadPermission);
      } catch (GenericException e) {
        // do nothing
      }

      // 4) no write permission on basePath
      Path directoryWithoutWritePermission = Files.createTempDirectory("xpto_dir");
      directoryWithoutWritePermission.toFile().setWritable(false);
      try {
        new FileStorageService(directoryWithoutWritePermission);
      } catch (GenericException e) {
        // do nothing
      }

      // test specific cleanup
      FSUtils.deletePath(parentDirForFolderCreationFailure);
      FSUtils.deletePath(fileAsBasePath);
      FSUtils.deletePath(directoryWithoutReadPermission);
      FSUtils.deletePath(directoryWithoutWritePermission);

    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Override
  @Test
  public void testListContainer() throws RODAException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) list containers
    CloseableIterable<Container> iterable = storage1.listContainers();
    // 1.3) end first transaction and assert empty
    transactionManager.endTransaction(context1.transactionLog().getId());
    assertThat(iterable, Matchers.iterableWithSize(0));

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage2.createContainer(containerStoragePath);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) list containers
    Iterator<Container> iterator = storage3.listContainers().iterator();
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());
    // 3.4) assert that the previously created container is there
    assertTrue(iterator.hasNext());
    Container next = iterator.next();
    assertNotNull(next);
    assertEquals(containerStoragePath, next.getStoragePath());
    assertFalse(iterator.hasNext());

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 4.2) delete previously created container
    storage4.deleteContainer(containerStoragePath);
    // 4.3) end transaction
    transactionManager.endTransaction(context4.transactionLog().getId());

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) list containers
    iterator = storage5.listContainers().iterator();
    // 5.3) check if the container is gone
    assertFalse(iterator.hasNext());
  }

  @Override
  @Test
  public void testCreateGetDeleteDirectory() throws RODAException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(containerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) create directory
    StoragePath directoryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    storage2.createDirectory(directoryStoragePath);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) get created directory
    Directory directory = storage3.getDirectory(directoryStoragePath);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());
    // 3.4) assert that the directory is correct
    assertNotNull(directory);
    assertEquals(directoryStoragePath, directory.getStoragePath());
    assertTrue(directory.isDirectory());

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    try {
      // 4.2) create directory that already exists
      storage4.createDirectory(directoryStoragePath);
      // 4.3) end fourth transaction
      transactionManager.endTransaction(context4.transactionLog().getId());
      Assert.fail("An exception should have been thrown while creating a directory that already exists");
    } catch (AlreadyExistsException | RODATransactionException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) delete directory
    storage5.deleteResource(directoryStoragePath);
    // 5.3) end fifth transaction
    transactionManager.endTransaction(context5.transactionLog().getId());

    // 6.1) start sixth transaction
    TransactionalContext context6 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage6 = context6.transactionalStorageService();
    try {
      storage6.getDirectory(directoryStoragePath);
      Assert.fail("An exception should have been thrown while getting a directory that was deleted");
    } catch (NotFoundException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
    // 6.2) end sixth transaction
    transactionManager.endTransaction(context6.transactionLog().getId());

    // 7.1) start seventh transaction
    TransactionalContext context7 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage7 = context7.transactionalStorageService();
    // 7.2) cleanup
    storage7.deleteContainer(containerStoragePath);
    // 7.3) end seventh transaction
    transactionManager.endTransaction(context7.transactionLog().getId());
  }

  @Override
  @Test
  public void testGetDirectoryThatDoesntExist() throws RODAException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(containerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) get directory that doesn't exist
    StoragePath directoryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    try {
      storage2.getDirectory(directoryStoragePath);
      Assert.fail("An exception should have been thrown while getting a directory that doesn't exist");
    } catch (NotFoundException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) cleanup
    storage3.deleteContainer(containerStoragePath);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());
  }

  @Override
  @Test
  public void testGetDirectoryThatIsActuallyABinary() throws RODAException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(containerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) create a binary
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload payload = new RandomMockContentPayload();
    storage2.createBinary(binaryStoragePath, payload, false);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    try {
      // 3.2) get binary using getDirectory
      storage3.getDirectory(binaryStoragePath);
      Assert.fail("An exception should have been thrown while getting a directory which is actually a binary");
    } catch (RequestNotValidException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 4.2) cleanup
    storage4.deleteContainer(containerStoragePath);
    // 4.3) end fourth transaction
    transactionManager.endTransaction(context4.transactionLog().getId());
  }

  @Override
  @Test
  public void testGetDirectoryThatIsActuallyAContainer() throws RODAException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(containerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    try {
      // 2.2) get container using getDirectory
      storage2.getDirectory(containerStoragePath);
      Assert.fail("An exception should have been thrown while getting a directory which is actually a container");
    } catch (RequestNotValidException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) cleanup
    storage3.deleteContainer(containerStoragePath);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());
  }

  public void testListResourcesUnderDirectory() throws RODAException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(containerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) create directory
    StoragePath directoryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    storage2.createDirectory(directoryStoragePath);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) list resources under directory
    Iterable<Resource> resources = storage3.listResourcesUnderDirectory(directoryStoragePath, false);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());
    // 3.4) assert that no resources are listed
    assertNotNull(resources);
    assertFalse(resources.iterator().hasNext());

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 4.2) create directory with 2 resources beneath (directories)
    final StoragePath subDirectoryStoragePath1 = StorageTestUtils
      .generateRandomResourceStoragePathUnder(directoryStoragePath);
    storage4.createDirectory(subDirectoryStoragePath1);
    final StoragePath subDirectoryStoragePath2 = StorageTestUtils
      .generateRandomResourceStoragePathUnder(directoryStoragePath);
    storage4.createDirectory(subDirectoryStoragePath2);
    // 4.3) end fourth transaction
    transactionManager.endTransaction(context4.transactionLog().getId());

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) add grand-child to ensure it is not listed
    final StoragePath subSubDirectoryStoragePath1 = StorageTestUtils
      .generateRandomResourceStoragePathUnder(subDirectoryStoragePath1);
    storage5.createDirectory(subSubDirectoryStoragePath1);
    // 5.3) end fifth transaction
    transactionManager.endTransaction(context5.transactionLog().getId());

    // 6.1) start sixth transaction
    TransactionalContext context6 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage6 = context6.transactionalStorageService();
    // 6.2) list resources under directory
    resources = storage6.listResourcesUnderDirectory(directoryStoragePath, false);
    // 6.3) end sixth transaction
    transactionManager.endTransaction(context6.transactionLog().getId());
    // 6.4)
    assertNotNull(resources);
    assertThat(resources, Matchers.iterableWithSize(2));

    // TODO test recursive listing

    // 7.1) start seventh transaction
    TransactionalContext context7 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage7 = context7.transactionalStorageService();
    // 7.2) cleanup
    storage7.deleteContainer(containerStoragePath);
    // 7.3) end seventh transaction
    transactionManager.endTransaction(context7.transactionLog().getId());
  }

  public void testCreateGetDeleteBinary() throws RODAException, IOException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(containerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) create binary
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload payload = new RandomMockContentPayload();
    storage2.createBinary(binaryStoragePath, payload, false);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) get binary
    Binary binary = storage3.getBinary(binaryStoragePath);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());
    // 3.4) assert that the binary is correct
    assertNotNull(binary);
    assertEquals(binaryStoragePath, binary.getStoragePath());
    assertFalse(binary.isDirectory());
    assertFalse(binary.isReference());
    testBinaryContent(binary, payload);

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    try {
      // 4.2) create binary that already exists
      storage4.createBinary(binaryStoragePath, payload, false);
      // 4.3) end fourth transaction
      transactionManager.endTransaction(context4.transactionLog().getId());
      Assert.fail("An exception should have been thrown while creating a binary that already exists");
    } catch (AlreadyExistsException | RODATransactionException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) delete binary
    storage5.deleteResource(binaryStoragePath);
    // 5.3) end fifth transaction
    transactionManager.endTransaction(context5.transactionLog().getId());

    // 6.1) start sixth transaction
    TransactionalContext context6 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage6 = context6.transactionalStorageService();
    try {
      // 6.2) get binary that was deleted
      storage6.getBinary(binaryStoragePath);
      Assert.fail("An exception should have been thrown while getting a binary that was deleted");
    } catch (NotFoundException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
    // 6.3) end sixth transaction
    transactionManager.endTransaction(context6.transactionLog().getId());

    // 7.1) start seventh transaction
    TransactionalContext context7 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage7 = context7.transactionalStorageService();
    // 7.2) cleanup
    storage7.deleteContainer(containerStoragePath);
    // 7.3) end seventh transaction
    transactionManager.endTransaction(context7.transactionLog().getId());
  }

  @Override
  @Test
  public void testCreateGetDeleteBinaryAsReference() throws RODAException, IOException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(containerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) create binary
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload payload = new RandomMockContentPayload();
    storage2.createBinary(binaryStoragePath, payload, true);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) get binary
    Binary binary = storage3.getBinary(binaryStoragePath);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());
    // 3.4) assert that the binary is correct
    assertNotNull(binary);
    assertEquals(binaryStoragePath, binary.getStoragePath());
    assertFalse(binary.isDirectory());
    // TODO
    // assertTrue(binary.isReference());
    testBinaryContent(binary, payload);

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    try {
      // 4.2) create binary that already exists
      storage4.createBinary(binaryStoragePath, payload, false);
      // 4.3) end fourth transaction
      transactionManager.endTransaction(context4.transactionLog().getId());
      Assert.fail("An exception should have been thrown while creating a binary that already exists");
    } catch (AlreadyExistsException | RODATransactionException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) delete binary
    storage5.deleteResource(binaryStoragePath);
    // 5.3) end fifth transaction
    transactionManager.endTransaction(context5.transactionLog().getId());

    // 6.1) start sixth transaction
    TransactionalContext context6 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage6 = context6.transactionalStorageService();
    try {
      // 6.2) test deleted binary
      storage6.getBinary(binaryStoragePath);
      Assert.fail("An exception should have been thrown while getting a binary that was deleted");
    } catch (NotFoundException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
    // 6.3) end sixth transaction
    transactionManager.endTransaction(context6.transactionLog().getId());

    // 7.1) start seventh transaction
    TransactionalContext context7 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage7 = context7.transactionalStorageService();
    // 7.2) cleanup
    storage7.deleteContainer(containerStoragePath);
  }

  @Override
  @Test
  public void testUpdateBinaryContent() throws RODAException, IOException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(containerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) create binary
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload payload = new RandomMockContentPayload();
    storage2.createBinary(binaryStoragePath, payload, false);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) get created binary
    final Binary binaryCreated = storage3.getBinary(binaryStoragePath);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());
    // 3.4) assert that the binary is correct
    assertNotNull(binaryCreated);
    // 3.5) save content to file
    final Path original = Files.createTempFile(FilenameUtils.normalize(binaryCreated.getStoragePath().getName()),
      ".tmp");
    binaryCreated.getContent().writeToPath(original);

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 4.2) update binary content
    final ContentPayload newPayload = new RandomMockContentPayload();
    final Binary binaryUpdated = storage4.updateBinaryContent(binaryStoragePath, newPayload, false, false);
    // 4.3) end fourth transaction
    transactionManager.endTransaction(context4.transactionLog().getId());
    // 4.4) assert that the binary was updated
    assertNotNull(binaryUpdated);

    // 5.1) compare original and updated binary content
    try (InputStream stream = Files.newInputStream(original)) {
      assertFalse(IOUtils.contentEquals(stream, binaryUpdated.getContent().createInputStream()));
    }
    testBinaryContent(binaryUpdated, newPayload);

    // 6.1) start sixth transaction
    TransactionalContext context6 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage6 = context6.transactionalStorageService();
    // 6.2) cleanup
    storage6.deleteContainer(containerStoragePath);
  }
}
