/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fs;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.roda.core.TestsHelper;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.storage.AbstractStorageServiceTest;
import org.roda.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for File System based StorageService
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 * 
 * @see StorageService
 * @see FileStorageService
 */
// @PrepareForTest({FSUtils.class})
public class FileStorageServiceTest extends AbstractStorageServiceTest<FileStorageService> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageServiceTest.class);

  private static Path basePath;
  private static FileStorageService storage;

  @BeforeMethod
  public static void setUp() throws IOException, GenericException {
    basePath = TestsHelper.createBaseTempDir(FileStorageServiceTest.class, true);
    storage = new FileStorageService(basePath);
  }

  @AfterMethod
  public static void tearDown() throws NotFoundException, GenericException {
    FSUtils.deletePath(basePath);
    FSUtils.deletePath(basePath.getParent().resolve(basePath.getFileName() + FileStorageService.HISTORY_SUFFIX));
  }

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
  protected FileStorageService getStorage() {
    return storage;
  }

  @Override
  public void cleanUp() {
    LOGGER.debug("Cleanning up");
    try {
      // recursively delete directory
      Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
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

  @Test
  public void testListContainer() throws RODAException {
    super.testListContainer();
  }

  @Test
  public void testCreateGetDeleteContainer() throws RODAException {
    super.testCreateGetDeleteContainer();
  }

  @Test
  public void testGetContainerThatDoesntExist() throws RODAException {
    super.testGetContainerThatDoesntExist();
  }

  @Test
  public void testGetContainerThatIsActuallyADirectory() throws RODAException {
    super.testGetContainerThatIsActuallyADirectory();
  }

  @Test
  public void testGetContainerThatIsActuallyABinary() throws RODAException {
    super.testGetContainerThatIsActuallyABinary();
  }

  @Test
  public void testDeleteContainerThatDoesntExist() throws RODAException {
    super.testDeleteContainerThatDoesntExist();
  }

  @Test
  public void testDeleteNonEmptyContainer() throws RODAException {
    super.testDeleteNonEmptyContainer();
  }

  @Test
  public void testListResourcesUnderContainer() throws RODAException {
    super.testListResourcesUnderContainer();
  }

  @Test
  public void testCreateGetDeleteDirectory() throws RODAException {
    super.testCreateGetDeleteDirectory();
  }

  @Test
  public void testGetDirectoryThatDoesntExist() throws RODAException {
    super.testGetDirectoryThatDoesntExist();
  }

  @Test
  public void testGetDirectoryThatIsActuallyABinary() throws RODAException {
    super.testGetDirectoryThatIsActuallyABinary();
  }

  @Test
  public void testGetDirectoryThatIsActuallyAContainer() throws RODAException {
    super.testGetDirectoryThatIsActuallyAContainer();
  }

  @Test
  public void testListResourcesUnderDirectory() throws RODAException, IOException {
    super.testListResourcesUnderDirectory();
  }

  @Test
  public void testCreateGetDeleteBinary() throws RODAException, IOException {
    super.testCreateGetDeleteBinary();
  }

  @Test
  public void testCreateGetDeleteBinaryAsReference() throws RODAException, IOException {
    super.testCreateGetDeleteBinaryAsReference();
  }

  @Test
  public void testUpdateBinaryContent() throws RODAException, IOException {
    super.testUpdateBinaryContent();
  }

  @Test
  public void testUpdateBinaryThatDoesntExist() throws RODAException, IOException {
    super.testUpdateBinaryThatDoesntExist();
  }

  @Test
  public void testGetBinaryThatDoesntExist() throws RODAException {
    super.testGetBinaryThatDoesntExist();
  }

  @Test
  public void testGetBinaryThatIsActuallyADirectory() throws RODAException {
    super.testGetBinaryThatIsActuallyADirectory();
  }

  @Test
  public void testGetBinaryThatIsActuallyAContainer() throws RODAException {
    super.testGetBinaryThatIsActuallyAContainer();
  }

  @Test
  public void testDeleteNonEmptyDirectory() throws RODAException {
    super.testDeleteNonEmptyDirectory();
  }

  @Test
  public void testCopyContainerToSameStorage() throws RODAException, IOException {
    super.testCopyContainerToSameStorage();
  }

  @Test
  public void testCopyDirectoryToSameStorage() throws RODAException, IOException {
    super.testCopyDirectoryToSameStorage();
  }

  @Test
  public void testCopyBinaryToSameStorage() throws RODAException, IOException {
    super.testCopyBinaryToSameStorage();
  }

  @Test
  public void testMoveContainerToSameStorage() throws RODAException, IOException {
    super.testMoveContainerToSameStorage();
  }

  @Test
  public void testMoveDirectoryToSameStorage() throws RODAException, IOException {
    super.testMoveDirectoryToSameStorage();
  }

  @Test
  public void testMoveBinaryToSameStorage() throws RODAException, IOException {
    super.testMoveBinaryToSameStorage();
  }

  @Test
  public void testBinaryVersions() throws RODAException, IOException {
    super.testBinaryVersions();
  }

  // TODO test get binary while IO Error occurs
  // TODO test create binary as reference while IO Error occurs
  // TODO test update binary while IO Error occurs

}
