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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import jakarta.transaction.TransactionalException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
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
import org.testng.annotations.AfterMethod;
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

  @AfterMethod
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
  public void testCreateGetDeleteContainer() throws RODAException {
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
    // 2.2) get created container
    Container container = storage2.getContainer(containerStoragePath);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());
    // 2.4) assert that the container is correct
    assertNotNull(container);
    assertEquals(containerStoragePath, container.getStoragePath());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) create container that already exists
    try {
      storage3.createContainer(containerStoragePath);
      transactionManager.endTransaction(context3.transactionLog().getId());
      Assert.fail("An exception should have been thrown while creating a container that already exists");
    } catch (AlreadyExistsException | RODATransactionException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 4.2) delete container
    storage4.deleteContainer(containerStoragePath);
    // 4.3) end fourth transaction
    transactionManager.endTransaction(context4.transactionLog().getId());

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) test delete container
    try {
      storage5.getContainer(containerStoragePath);
      Assert.fail("An exception should have been thrown while getting a container that was deleted");
    } catch (NotFoundException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
  }

  @Override
  @Test
  public void testGetContainerThatDoesntExist() throws RODAException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) get container that doesn't exist
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    try {
      storage1.getContainer(containerStoragePath);
      Assert.fail("An exception should have been thrown while getting a container that doesn't exist");
    } catch (NotFoundException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
  }

  @Override
  @Test
  public void testGetContainerThatIsActuallyADirectory() throws RODAException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) create a container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(containerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) create a directory under the container
    final StoragePath directoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    storage2.createDirectory(directoryStoragePath);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) get directory as it was a binary
    try {
      storage3.getContainer(directoryStoragePath);
      Assert.fail("An exception should have been thrown while getting a container which is actually a directory");
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
  }

  @Override
  @Test
  public void testGetContainerThatIsActuallyABinary() throws RODAException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(containerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) continue set up
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload payload = new RandomMockContentPayload();
    storage2.createBinary(binaryStoragePath, payload, false);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) get binary as it was a directory
    try {
      storage3.getContainer(binaryStoragePath);
      Assert.fail("An exception should have been thrown while getting a container which is actually a binary");
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
  }

  @Override
  @Test
  public void testDeleteContainerThatDoesntExist() throws RODAException {
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
    // 2.2) delete container that exists
    storage2.deleteContainer(containerStoragePath);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) delete container that no longer exists
    storage3.deleteContainer(containerStoragePath);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());
  }

  public void testListResourcesUnderContainer() throws RODAException {
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
    // 2.2) list empty container
    Iterable<Resource> resources = storage2.listResourcesUnderContainer(containerStoragePath, false);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());
    // 2.4) assert that there are no resources
    assertNotNull(resources);
    assertNotNull(resources.iterator());
    assertFalse(resources.iterator().hasNext());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) create directories
    StoragePath directoryStoragePath1 = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    storage3.createDirectory(directoryStoragePath1);
    StoragePath directoryStoragePath2 = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    storage3.createDirectory(directoryStoragePath2);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 4.2) list container with 2 resources beneath (directories)
    resources = storage4.listResourcesUnderContainer(containerStoragePath, false);
    // 4.3) end fourth transaction
    transactionManager.endTransaction(context4.transactionLog().getId());
    // 4.4) assert that there are 2 resources
    assertNotNull(resources);
    assertThat(resources, Matchers.<Resource> iterableWithSize(2));

    // TODO test recursive listing

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) cleanup
    storage5.deleteResource(containerStoragePath);
    // 5.3) end fifth transaction
    transactionManager.endTransaction(context5.transactionLog().getId());
  }

  public void testDeleteNonEmptyContainer() throws RODAException {
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
    // 2.2) create directory under container
    StoragePath directoryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    storage2.createDirectory(directoryStoragePath);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) delete container recursively
    storage3.deleteContainer(containerStoragePath);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 4.2) test delete container
    try {
      storage4.getContainer(containerStoragePath);
      Assert.fail("An exception should have been thrown while getting a container that doesn't exist");
    } catch (NotFoundException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
    // 4.3) end fourth transaction
    transactionManager.endTransaction(context4.transactionLog().getId());

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) test getting a directory that doesn't exist
    try {
      storage5.getDirectory(directoryStoragePath);
      Assert.fail("An exception should have been thrown while getting a container that doesn't exist");
    } catch (NotFoundException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
    // 5.3) end fifth transaction
    transactionManager.endTransaction(context5.transactionLog().getId());

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

  @Test
  public void testListDeletedContainerResourceCreatedInMain() throws RODAException {
    // 1.1) create container with main storage
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    mainStorage.createContainer(containerStoragePath);

    // 2.1) create a binary with main storage
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload payload = new RandomMockContentPayload();
    mainStorage.createBinary(binaryStoragePath, payload, false);

    // 2.2) create a second binary with main storage
    final StoragePath binaryStoragePath2 = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload payload2 = new RandomMockContentPayload();
    mainStorage.createBinary(binaryStoragePath2, payload2, false);

    // 3.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 3.2) list resources
    Iterable<Resource> resources = storage1.listResourcesUnderContainer(containerStoragePath, false);
    // 3.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());
    // 3.4) assert that there is a resource
    Resource resource = resources.iterator().next();
    assertNotNull(resource);

    // 4.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 4.2) delete binary
    storage2.deleteResource(binaryStoragePath);
    // 4.3) list resources using same transaction from delete
    resources = storage2.listResourcesUnderContainer(containerStoragePath, false);
    for (Resource resource1 : resources) {
      LOGGER.info("Resource: {}", resource1.getStoragePath());
    }
    try {
      // 4.4) assert that there isn't a resource
      resources.iterator().next();
      Assert.fail("An exception should have been thrown while listing a deleted resource");
    } catch (NoSuchElementException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
    // 4.5) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 5.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 5.2) cleanup
    storage4.deleteContainer(containerStoragePath);
    // 5.3) end fourth transaction
    transactionManager.endTransaction(context4.transactionLog().getId());
  }

  @Test
  public void testListDeletedDirectoryResourceCreatedInMain() throws RODAException {
    // 1.1) create container with main storage
    final StoragePath directoryStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    mainStorage.createDirectory(directoryStoragePath);

    // 2.1) create a binary with main storage
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(directoryStoragePath);
    final ContentPayload payload = new RandomMockContentPayload();
    mainStorage.createBinary(binaryStoragePath, payload, false);

    // 2.2) create a second binary with main storage
    final StoragePath binaryStoragePath2 = StorageTestUtils.generateRandomResourceStoragePathUnder(directoryStoragePath);
    final ContentPayload payload2 = new RandomMockContentPayload();
    mainStorage.createBinary(binaryStoragePath2, payload2, false);

    // 3.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 3.2) list resources
    Iterable<Resource> resources = storage1.listResourcesUnderDirectory(directoryStoragePath, false);
    // 3.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());
    // 3.4) assert that there is a resource
    Resource resource = resources.iterator().next();
    assertNotNull(resource);

    // 4.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 4.2) delete binary
    storage2.deleteResource(binaryStoragePath);
    // 4.3) list resources using same transaction from delete
    resources = storage2.listResourcesUnderDirectory(directoryStoragePath, false);
    for (Resource resource1 : resources) {
        LOGGER.info("Resource: {}", resource1.getStoragePath());
    }
    try {
      // 4.4) assert that there isn't a resource
      resources.iterator().next();
      Assert.fail("An exception should have been thrown while listing a deleted resource");
    } catch (NoSuchElementException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
    // 4.5) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 5.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 5.2) cleanup
    storage4.deleteResource(directoryStoragePath);
    // 5.3) end fourth transaction
    transactionManager.endTransaction(context4.transactionLog().getId());
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

  @Override
  @Test
  public void testUpdateBinaryThatDoesntExist() throws RODAException, IOException {
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
    // 2.2) update binary content from binary that doesn't exist
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload payload = new RandomMockContentPayload();
    final boolean asReference = false;
    try {
      storage2.updateBinaryContent(binaryStoragePath, payload, asReference, false);
      Assert.fail("An exception should have been thrown while updating a binary that doesn't exist");
    } catch (NotFoundException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) update binary content now with createIfNotExists=true
    Binary updatedBinaryContent = storage3.updateBinaryContent(binaryStoragePath, payload, asReference, true);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());
    // 3.4) assert that the binary content is valid
    testBinaryContent(updatedBinaryContent, payload);

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
  public void testGetBinaryThatDoesntExist() throws RODAException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(containerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) get a binary that doesn't exist
    StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    try {
      storage2.getBinary(binaryStoragePath);
      Assert.fail("An exception should have been thrown while getting a binary that doesn't exist");
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
  public void testGetBinaryThatIsActuallyADirectory() throws RODAException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(containerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) create a directory
    final StoragePath directoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    storage2.createDirectory(directoryStoragePath);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) get directory as it was a binary
    try {
      storage3.getBinary(directoryStoragePath);
      Assert.fail("An exception should have been thrown while getting a binary which is actually a directory");
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
  public void testGetBinaryThatIsActuallyAContainer() throws RODAException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(containerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) get container as it was a binary
    try {
      storage2.getBinary(containerStoragePath);
      Assert.fail("An exception should have been thrown while getting a binary which is actually a container");
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

  @Override
  @Test
  public void testDeleteNonEmptyDirectory() throws RODAException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(containerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) create a directory
    StoragePath directoryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    storage2.createDirectory(directoryStoragePath);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) create a binary under the directory
    StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(directoryStoragePath);
    final ContentPayload binaryPayload = new RandomMockContentPayload();
    storage3.createBinary(binaryStoragePath, binaryPayload, false);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 4.2) test recursively delete directory
    storage4.deleteResource(directoryStoragePath);
    // 4.3) end fourth transaction
    transactionManager.endTransaction(context4.transactionLog().getId());

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) test get sub-resource
    try {
      storage5.getBinary(binaryStoragePath);
      Assert.fail("An exception should have been thrown while getting a binary that doesn't exist");
    } catch (NotFoundException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
    // 5.3) end fifth transaction
    transactionManager.endTransaction(context5.transactionLog().getId());

    // 6.1) start sixth transaction
    TransactionalContext context6 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage6 = context6.transactionalStorageService();
    // 6.2) test specific cleanup
    storage6.deleteContainer(containerStoragePath);
    // 6.3) end sixth transaction
    transactionManager.endTransaction(context6.transactionLog().getId());
  }

  @Override
  @Test
  public void testCopyContainerToSameStorage() throws RODAException, IOException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) create and populate source container
    final StoragePath sourceContainerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(sourceContainerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) populate
    StorageTestUtils.populate(storage2, sourceContainerStoragePath);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) copy
    final StoragePath targetContainerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage3.copy(storage3, sourceContainerStoragePath, targetContainerStoragePath);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 4.2) test copy validity
    StorageTestUtils.testEntityEqualRecursively(storage4, sourceContainerStoragePath, storage4,
      targetContainerStoragePath);

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) cleanup
    storage5.deleteContainer(sourceContainerStoragePath);
    storage5.deleteContainer(targetContainerStoragePath);
    // 5.3) end fourth transaction
    transactionManager.endTransaction(context5.transactionLog().getId());
  }

  @Override
  @Test
  public void testCopyDirectoryToSameStorage() throws RODAException, IOException {
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
    // 2.2) create and populate source directory
    final StoragePath sourceDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    storage2.createDirectory(sourceDirectoryStoragePath);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) populate source directory
    StorageTestUtils.populate(storage3, sourceDirectoryStoragePath);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 4.2) copy
    final StoragePath targetDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    storage4.copy(storage4, sourceDirectoryStoragePath, targetDirectoryStoragePath);
    // 4.3) end fourth transaction
    transactionManager.endTransaction(context4.transactionLog().getId());

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) test copy validity
    StorageTestUtils.testEntityEqualRecursively(storage5, sourceDirectoryStoragePath, storage5,
      targetDirectoryStoragePath);
    // 5.3) end fifth transaction
    transactionManager.endTransaction(context5.transactionLog().getId());

    // 6.1) start fifth transaction
    TransactionalContext context6 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage6 = context6.transactionalStorageService();
    // 6.2) cleanup
    storage6.deleteContainer(containerStoragePath);
    // 6.3) end fifth transaction
    transactionManager.endTransaction(context6.transactionLog().getId());
  }

  @Override
  @Test
  public void testCopyBinaryToSameStorage() throws RODAException, IOException {
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
    final StoragePath sourceBinaryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload sourcePayload = new RandomMockContentPayload();
    storage2.createBinary(sourceBinaryStoragePath, sourcePayload, false);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) copy
    final StoragePath targetBinaryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    storage3.copy(storage3, sourceBinaryStoragePath, targetBinaryStoragePath);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 4.2) get source and target binaries
    final Binary sourceBinary = storage4.getBinary(sourceBinaryStoragePath);
    final Binary targetBinary = storage4.getBinary(targetBinaryStoragePath);
    // 4.3) end fourth transaction
    transactionManager.endTransaction(context4.transactionLog().getId());
    // 4.4) test copy validity
    assertEquals(sourceBinary.isDirectory(), targetBinary.isDirectory());
    assertEquals(sourceBinary.getContentDigest(), targetBinary.getContentDigest());
    assertEquals(sourceBinary.getSizeInBytes(), targetBinary.getSizeInBytes());
    assertEquals(sourceBinary.isReference(), targetBinary.isReference());
    assertTrue(IOUtils.contentEquals(sourceBinary.getContent().createInputStream(),
      targetBinary.getContent().createInputStream()));

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) cleanup
    storage5.deleteContainer(containerStoragePath);
    // 5.3) end fifth transaction
    transactionManager.endTransaction(context5.transactionLog().getId());
  }

  @Override
  @Test
  public void testMoveContainerToSameStorage() throws RODAException, IOException {
    // 1.1) start first transaction
    TransactionalContext context1 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage1 = context1.transactionalStorageService();
    // 1.2) create and populate source container
    final StoragePath sourceContainerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage1.createContainer(sourceContainerStoragePath);
    // 1.3) end first transaction
    transactionManager.endTransaction(context1.transactionLog().getId());

    // 2.1) start second transaction
    TransactionalContext context2 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage2 = context2.transactionalStorageService();
    // 2.2) populate
    StorageTestUtils.populate(storage2, sourceContainerStoragePath);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) copy for comparison test
    final StoragePath copyContainerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage3.copy(storage3, sourceContainerStoragePath, copyContainerStoragePath);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 4.2) move
    final StoragePath targetContainerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    storage4.move(storage4, sourceContainerStoragePath, targetContainerStoragePath);
    // 4.3) end fourth transaction
    transactionManager.endTransaction(context4.transactionLog().getId());

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) test move validity
    StorageTestUtils.testEntityEqualRecursively(storage5, copyContainerStoragePath, storage5,
      targetContainerStoragePath);
    // 5.3) end fifth transaction
    transactionManager.endTransaction(context5.transactionLog().getId());

    // 6.1) start sixth transaction
    TransactionalContext context6 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage6 = context6.transactionalStorageService();
    // 6.2) test source does not exist
    try {
      storage6.getContainer(sourceContainerStoragePath);
      Assert.fail("An exception should have been thrown while getting a container that was moved");
    } catch (NotFoundException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
    // 6.3) end sixth transaction
    transactionManager.endTransaction(context6.transactionLog().getId());

    // 7.1) start seventh transaction
    TransactionalContext context7 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage7 = context7.transactionalStorageService();
    // 7.2) cleanup
    storage7.deleteContainer(copyContainerStoragePath);
    storage7.deleteContainer(targetContainerStoragePath);
    // 7.3) end seventh transaction
    transactionManager.endTransaction(context7.transactionLog().getId());
  }

  @Override
  @Test
  public void testMoveDirectoryToSameStorage() throws RODAException, IOException {
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
    // 2.2) create source directory
    final StoragePath sourceDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    storage2.createDirectory(sourceDirectoryStoragePath);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) populate source directory
    StorageTestUtils.populate(storage3, sourceDirectoryStoragePath);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 4.2) copy for comparison test
    final StoragePath copyDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    storage4.copy(storage4, sourceDirectoryStoragePath, copyDirectoryStoragePath);
    // 4.3) move
    final StoragePath targetDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    storage4.move(storage4, sourceDirectoryStoragePath, targetDirectoryStoragePath);
    // 4.4) end fourth transaction
    transactionManager.endTransaction(context4.transactionLog().getId());

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) check with copy
    StorageTestUtils.testEntityEqualRecursively(storage5, copyDirectoryStoragePath, storage5,
      targetDirectoryStoragePath);
    // 5.3) end fifth transaction
    transactionManager.endTransaction(context5.transactionLog().getId());

    // 6.1) start sixth transaction
    TransactionalContext context6 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage6 = context6.transactionalStorageService();
    // 6.2) test source does not exist
    try {
      storage6.getDirectory(sourceDirectoryStoragePath);
      Assert.fail("An exception should have been thrown while getting a directory that was moved");
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
  public void testMoveBinaryToSameStorage() throws RODAException, IOException {
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
    final StoragePath sourceBinaryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload sourcePayload = new RandomMockContentPayload();
    storage2.createBinary(sourceBinaryStoragePath, sourcePayload, false);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) copy for comparison test
    final StoragePath copyBinaryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    storage3.copy(storage3, sourceBinaryStoragePath, copyBinaryStoragePath);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 4.2) move
    final StoragePath targetBinaryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    storage4.move(storage4, sourceBinaryStoragePath, targetBinaryStoragePath);
    // 4.3) end fourth transaction
    transactionManager.endTransaction(context4.transactionLog().getId());

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) get source and target binaries
    final Binary copyBinary = storage5.getBinary(copyBinaryStoragePath);
    final Binary targetBinary = storage5.getBinary(targetBinaryStoragePath);
    // 5.3) check with copy
    assertEquals(copyBinary.isDirectory(), targetBinary.isDirectory());
    assertEquals(copyBinary.getContentDigest(), targetBinary.getContentDigest());
    assertEquals(copyBinary.getSizeInBytes(), targetBinary.getSizeInBytes());
    assertEquals(copyBinary.isReference(), targetBinary.isReference());
    assertTrue(IOUtils.contentEquals(copyBinary.getContent().createInputStream(),
      targetBinary.getContent().createInputStream()));
    // 5.4) end fifth transaction
    transactionManager.endTransaction(context5.transactionLog().getId());

    // 6.1) start sixth transaction
    TransactionalContext context6 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage6 = context6.transactionalStorageService();
    // 6.2) test source does not exist
    try {
      storage6.getBinary(sourceBinaryStoragePath);
      Assert.fail("An exception should have been thrown while getting a binary that was moved");
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
  public void testBinaryVersions() throws RODAException, IOException {
    // 0) set up
    Map<String, String> properties = new HashMap<>();
    properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());

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
    // 2.2) create binary under the container
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload payload1 = new RandomMockContentPayload();
    storage2.createBinary(binaryStoragePath, payload1, false);
    // 2.3) end second transaction
    transactionManager.endTransaction(context2.transactionLog().getId());

    // 3.1) start third transaction
    TransactionalContext context3 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage3 = context3.transactionalStorageService();
    // 3.2) create binary version
    String message1 = "v1";
    properties.put(RodaConstants.VERSION_MESSAGE, message1);
    BinaryVersion v1 = storage3.createBinaryVersion(binaryStoragePath, properties);
    // 3.3) end third transaction
    transactionManager.endTransaction(context3.transactionLog().getId());

    // 4.1) start fourth transaction
    TransactionalContext context4 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage4 = context4.transactionalStorageService();
    // 4.2) update binary
    final ContentPayload payload2 = new RandomMockContentPayload();
    storage4.updateBinaryContent(binaryStoragePath, payload2, false, false);
    // 4.3) end fourth transaction
    transactionManager.endTransaction(context4.transactionLog().getId());

    // 5.1) start fifth transaction
    TransactionalContext context5 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage5 = context5.transactionalStorageService();
    // 5.2) create binary version 2
    String message2 = "v2";
    properties.put(RodaConstants.VERSION_MESSAGE, message2);
    storage5.createBinaryVersion(binaryStoragePath, properties);
    // 5.3) end fifth transaction
    transactionManager.endTransaction(context5.transactionLog().getId());

    // 6.1) start sixth transaction
    TransactionalContext context6 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage6 = context6.transactionalStorageService();
    // 6.2) create a version with a message that already exists
    storage6.createBinaryVersion(binaryStoragePath, properties);
    // 6.3) end sixth transaction
    transactionManager.endTransaction(context6.transactionLog().getId());

    // 7.1) start seventh transaction
    TransactionalContext context7 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage7 = context7.transactionalStorageService();
    // 7.2) list binary versions
    CloseableIterable<BinaryVersion> binaryVersions = storage7.listBinaryVersions(binaryStoragePath);
    List<BinaryVersion> reusableBinaryVersions = new ArrayList<>();
    // Iterables.addAll(reusableBinaryVersions, binaryVersions);
    binaryVersions.forEach(reusableBinaryVersions::add);
    // 7.3) end seventh transaction
    transactionManager.endTransaction(context7.transactionLog().getId());
    // 7.4) test binary versions total
    assertEquals(3, reusableBinaryVersions.size());

    // 8.1) start eighth transaction
    TransactionalContext context8 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage8 = context8.transactionalStorageService();
    // 8.2) get binary version
    BinaryVersion binaryVersion1 = storage8.getBinaryVersion(binaryStoragePath, v1.getId());
    // 8.3) end eighth transaction
    transactionManager.endTransaction(context8.transactionLog().getId());
    // 8.4) asserts
    // TODO compare properties
    assertEquals(message1, binaryVersion1.getProperties().get(RodaConstants.VERSION_MESSAGE));
    assertNotNull(binaryVersion1.getCreatedDate());
    assertTrue(
      IOUtils.contentEquals(payload1.createInputStream(), binaryVersion1.getBinary().getContent().createInputStream()));

    // 9.1) start ninth transaction
    TransactionalContext context9 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage9 = context9.transactionalStorageService();
    // 9.2) revert to previous version
    storage9.revertBinaryVersion(binaryStoragePath, v1.getId());
    // 9.3) end ninth transaction
    transactionManager.endTransaction(context9.transactionLog().getId());

    // 10.1) start tenth transaction
    TransactionalContext context10 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage10 = context10.transactionalStorageService();
    // 10.2) get binary version
    Binary binary = storage10.getBinary(binaryStoragePath);
    // 10.3) end tenth transaction
    transactionManager.endTransaction(context10.transactionLog().getId());
    // 10.4) test binary content
    testBinaryContent(binary, payload1);

    // 11.1) start eleventh transaction
    TransactionalContext context11 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage11 = context11.transactionalStorageService();
    // 11.2) delete binary version
    storage11.deleteBinaryVersion(binaryStoragePath, v1.getId());
    // 11.3) end eleventh transaction
    transactionManager.endTransaction(context11.transactionLog().getId());

    // 12.1) start twelfth transaction
    TransactionalContext context12 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage12 = context12.transactionalStorageService();
    try {
      // 12.2) try to get deleted binary version
      storage12.getBinaryVersion(binaryStoragePath, v1.getId());
      Assert.fail("Should have thrown NotFoundException");
    } catch (NotFoundException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
    // 12.3) end twelfth transaction
    transactionManager.endTransaction(context12.transactionLog().getId());

    // 13.1) start thirteenth transaction
    TransactionalContext context13 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage13 = context13.transactionalStorageService();
    // 13.2) delete binary and all its history
    storage13.deleteResource(binaryStoragePath);
    // 13.3) end thirteenth transaction
    transactionManager.endTransaction(context13.transactionLog().getId());

    // 14.1) start fourteenth transaction
    TransactionalContext context14 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage14 = context14.transactionalStorageService();
    try {
      // 14.2) try to get deleted binary
      storage14.getBinaryVersion(binaryStoragePath, v1.getId());
      Assert.fail("Should have thrown NotFoundException");
    } catch (NotFoundException e) {
      LOGGER.info("Caught expected exception: {}", e.getMessage());
    }
    // 14.3) end fourteenth transaction
    transactionManager.endTransaction(context14.transactionLog().getId());

    // 15.1) start fifteenth transaction
    TransactionalContext context15 = transactionManager.beginTestTransaction(mainStorage);
    StorageService storage15 = context15.transactionalStorageService();
    // 15.2) cleanup
    storage15.deleteContainer(containerStoragePath);
    // 15.3) end fifteenth transaction
    transactionManager.endTransaction(context15.transactionLog().getId());
  }
}
