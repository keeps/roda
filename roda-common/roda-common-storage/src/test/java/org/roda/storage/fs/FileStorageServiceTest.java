package org.roda.storage.fs;

import static org.junit.Assert.assertEquals;
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
import org.roda.storage.AbstractStorageServiceTest;
import org.roda.storage.ContentPayload;
import org.roda.storage.RandomMockContentPayload;
import org.roda.storage.StorageActionException;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.StorageTestUtils;
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
 * */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ FSYamlMetadataUtils.class, FSUtils.class })
public class FileStorageServiceTest extends
		AbstractStorageServiceTest<FileStorageService> {

	private static Path basePathForTests;
	private static FileStorageService storage;

	@BeforeClass
	public static void setUp() throws IOException, StorageActionException {
		basePathForTests = Files.createTempDirectory("fsTests");
		storage = new FileStorageService(basePathForTests);
	}

	@AfterClass
	public static void tearDown() throws StorageActionException {
		FSUtils.deletePath(basePathForTests);
	}

	final Logger logger = LoggerFactory.getLogger(FileStorageServiceTest.class);

	@Test
	public void testClassInstantiation() throws StorageActionException {

		try {
			// 1) Directory doesn't exist and is located under a read-only
			// directory
			Path parentDirForFolderCreationFailure = Files
					.createTempDirectory("xpto_dir");
			parentDirForFolderCreationFailure.toFile().setExecutable(true);
			parentDirForFolderCreationFailure.toFile().setReadable(true);
			parentDirForFolderCreationFailure.toFile().setWritable(false);
			Path folderCreationFailurePath = parentDirForFolderCreationFailure
					.resolve("subfolder");
			try {
				new FileStorageService(folderCreationFailurePath);
			} catch (StorageActionException e) {
				assertEquals(StorageActionException.INTERNAL_SERVER_ERROR,
						e.getCode());
			}

			// 2) basePath is a file
			Path fileAsBasePath = Files.createTempFile("xpto", null);
			try {
				new FileStorageService(fileAsBasePath);
			} catch (StorageActionException e) {
				assertEquals(StorageActionException.INTERNAL_SERVER_ERROR,
						e.getCode());
			}

			// 3) no read permission on basePath
			Path directoryWithoutReadPermission = Files
					.createTempDirectory("xpto_dir");
			directoryWithoutReadPermission.toFile().setReadable(false);
			try {
				new FileStorageService(directoryWithoutReadPermission);
			} catch (StorageActionException e) {
				assertEquals(StorageActionException.INTERNAL_SERVER_ERROR,
						e.getCode());
			}

			// 4) no write permission on basePath
			Path directoryWithoutWritePermission = Files
					.createTempDirectory("xpto_dir");
			directoryWithoutWritePermission.toFile().setWritable(false);
			try {
				new FileStorageService(directoryWithoutWritePermission);
			} catch (StorageActionException e) {
				assertEquals(StorageActionException.INTERNAL_SERVER_ERROR,
						e.getCode());
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
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir,
						IOException exc) throws IOException {
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
	public void testCreateContainerWhileIOError() throws IOException,
			StorageActionException {

		// Force IO error occur while creating some file or folder
		PowerMockito.mockStatic(FSYamlMetadataUtils.class);
		PowerMockito.doThrow(new IOException()).when(FSYamlMetadataUtils.class);
		FSYamlMetadataUtils.createPropertiesDirectory(org.mockito.Matchers
				.any(Path.class));

		final StoragePath containerStoragePath = StorageTestUtils
				.generateRandomContainerStoragePath();
		final Map<String, Set<String>> containerMetadata = StorageTestUtils
				.generateRandomMetadata();
		try {
			storage.createContainer(containerStoragePath, containerMetadata);
			fail("An exception should have been thrown while creating a container because an IOException occured but it didn't happened!");
		} catch (StorageActionException e) {
			assertEquals(StorageActionException.INTERNAL_SERVER_ERROR,
					e.getCode());
		}

		// check if container was not created
		try {
			getStorage().getContainer(containerStoragePath);
			fail("Container should not have been created, and yet it was.");
		} catch (StorageActionException e) {
			assertEquals(StorageActionException.NOT_FOUND, e.getCode());
		}
	}

	@Test
	public void testCreateDirectoryWhileIOError() throws IOException,
			StorageActionException {

		// set up
		final StoragePath containerStoragePath = StorageTestUtils
				.generateRandomContainerStoragePath();
		final Map<String, Set<String>> containerMetadata = StorageTestUtils
				.generateRandomMetadata();
		getStorage().createContainer(containerStoragePath, containerMetadata);

		// Force IO error occur while creating some file or folder
		PowerMockito.mockStatic(FSYamlMetadataUtils.class);
		PowerMockito.doThrow(new IOException()).when(FSYamlMetadataUtils.class);
		FSYamlMetadataUtils.createPropertiesDirectory(org.mockito.Matchers
				.any(Path.class));

		// create directory
		StoragePath directoryStoragePath = StorageTestUtils
				.generateRandomResourceStoragePathUnder(containerStoragePath);
		final Map<String, Set<String>> directoryMetadata = StorageTestUtils
				.generateRandomMetadata();

		try {
			getStorage().createDirectory(directoryStoragePath,
					directoryMetadata);
			fail("An exception should have been thrown while creating a container because an IOException occured but it didn't happened!");
		} catch (StorageActionException e) {
			assertEquals(StorageActionException.INTERNAL_SERVER_ERROR,
					e.getCode());
		}

		// check if directory was not created
		try {
			getStorage().getDirectory(directoryStoragePath);
			fail("Directory should not have been created, and yet it was.");
		} catch (StorageActionException e) {
			assertEquals(e.getMessage(), StorageActionException.NOT_FOUND,
					e.getCode());
		}
	}

	// TODO test get binary while IO Error occurs

	@Test
	public void testCreateBinaryWhileIOError() throws IOException,
			StorageActionException {

		// set up
		final StoragePath containerStoragePath = StorageTestUtils
				.generateRandomContainerStoragePath();
		final Map<String, Set<String>> containerMetadata = StorageTestUtils
				.generateRandomMetadata();
		getStorage().createContainer(containerStoragePath, containerMetadata);

		final StoragePath binaryStoragePath = StorageTestUtils
				.generateRandomResourceStoragePathUnder(containerStoragePath);
		final Map<String, Set<String>> binaryMetadata = StorageTestUtils
				.generateRandomMetadata();
		final ContentPayload payload = new RandomMockContentPayload();

		// Force IO error occur while creating some file or folder
		final ContentPayload spyPayload = Mockito.spy(payload);
		PowerMockito.doThrow(new IOException()).when(spyPayload);
		spyPayload.writeToPath(Matchers.any(Path.class));

		// create binary
		try {
			getStorage().createBinary(binaryStoragePath, binaryMetadata,
					spyPayload, false);
			fail("An exception should have been thrown while creating a container because an IOException occured but it didn't happened!");
		} catch (StorageActionException e) {
			assertEquals(StorageActionException.INTERNAL_SERVER_ERROR,
					e.getCode());
		}

		// check if binary was not created
		try {
			getStorage().getBinary(binaryStoragePath);
			fail("Binary should not have been created, and yet it was.");
		} catch (StorageActionException e) {
			assertEquals(StorageActionException.NOT_FOUND, e.getCode());
		}
	}

	// TODO test create binary as reference while IO Error occurs
	// TODO test update binary while IO Error occurs

}
