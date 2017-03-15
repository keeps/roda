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
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(FileStorageServiceTest.class, true);
    storage = new FileStorageService(basePath);
  }

  @AfterMethod
  public static void tearDown() throws RODAException {
    FSUtils.deletePath(basePath);
    FSUtils.deletePath(basePath.getParent().resolve(basePath.getFileName() + FileStorageService.HISTORY_SUFFIX));
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

  // TODO test get binary while IO Error occurs
  // TODO test create binary as reference while IO Error occurs
  // TODO test update binary while IO Error occurs

}
