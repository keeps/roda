/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fs;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.storage.AbstractStorageServiceTest;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.RandomMockContentPayload;
import org.roda.core.storage.StoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for File System based StorageService
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 * 
 * @see StorageService
 * @see FileStorageService
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({FSYamlMetadataUtils.class, FSUtils.class})
public class FileStorageServiceTest extends AbstractStorageServiceTest<FileStorageService> {

  private static Path basePathForTests;
  private static FileStorageService storage;

  @BeforeClass
  public static void setUp() throws IOException, GenericException {
    basePathForTests = Files.createTempDirectory("fsTests");
    storage = new FileStorageService(basePathForTests);
  }

  @AfterClass
  public static void tearDown() throws NotFoundException, GenericException {
    FSUtils.deletePath(basePathForTests);
  }

  final Logger logger = LoggerFactory.getLogger(FileStorageServiceTest.class);

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
      fail(e.getMessage());
    }
  }

  @Override
  protected FileStorageService getStorage() {
    return storage;
  }

  @Override
  public void cleanUp() {
    logger.trace("Cleanning up");
    try {
      // recursively delete directory
      Files.walkFileTree(basePathForTests, new SimpleFileVisitor<Path>() {
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
      Files.createDirectory(basePathForTests);
    } catch (IOException e) {
      logger.error("Could not clean up", e);
    }
  }

  @Test
  public void testCreateContainerWhileIOError() throws IOException, RODAException {

    // Force IO error occur while creating some file or folder
    PowerMockito.mockStatic(FSYamlMetadataUtils.class);
    PowerMockito.doThrow(new IOException()).when(FSYamlMetadataUtils.class);
    FSYamlMetadataUtils.createPropertiesDirectory(org.mockito.Matchers.any(Path.class));

    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    try {
      storage.createContainer(containerStoragePath, containerMetadata);
      fail(
        "An exception should have been thrown while creating a container because an IOException occured but it didn't happened!");
    } catch (GenericException e) {
      // do nothing
    }

    // check if container was not created
    try {
      getStorage().getContainer(containerStoragePath);
      fail("Container should not have been created, and yet it was.");
    } catch (NotFoundException e) {
      // do nothing
    }
  }

  @Test
  public void testCreateDirectoryWhileIOError() throws IOException, RODAException {

    // set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // Force IO error occur while creating some file or folder
    PowerMockito.mockStatic(FSYamlMetadataUtils.class);
    PowerMockito.doThrow(new IOException()).when(FSYamlMetadataUtils.class);
    FSYamlMetadataUtils.createPropertiesDirectory(org.mockito.Matchers.any(Path.class));

    // create directory
    StoragePath directoryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> directoryMetadata = StorageTestUtils.generateRandomMetadata();

    try {
      getStorage().createDirectory(directoryStoragePath, directoryMetadata);
      fail(
        "An exception should have been thrown while creating a container because an IOException occured but it didn't happened!");
    } catch (GenericException e) {
      // do nothing
    }

    // check if directory was not created
    try {
      getStorage().getDirectory(directoryStoragePath);
      fail("Directory should not have been created, and yet it was.");
    } catch (NotFoundException e) {
      // do nothing
    }
  }

  // TODO test get binary while IO Error occurs

  @Test
  public void testCreateBinaryWhileIOError() throws IOException, RODAException {

    // set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> binaryMetadata = StorageTestUtils.generateRandomMetadata();
    final ContentPayload payload = new RandomMockContentPayload();

    // Force IO error occur while creating some file or folder
    final ContentPayload spyPayload = Mockito.spy(payload);
    PowerMockito.doThrow(new IOException()).when(spyPayload);
    spyPayload.writeToPath(Matchers.any(Path.class));

    // create binary
    try {
      getStorage().createBinary(binaryStoragePath, binaryMetadata, spyPayload, false);
      fail(
        "An exception should have been thrown while creating a container because an IOException occured but it didn't happened!");
    } catch (GenericException e) {
      // do nothing
    }

    // check if binary was not created
    try {
      getStorage().getBinary(binaryStoragePath);
      fail("Binary should not have been created, and yet it was.");
    } catch (NotFoundException e) {
      // do nothing
    }
  }

  // TODO test create binary as reference while IO Error occurs
  // TODO test update binary while IO Error occurs

}
