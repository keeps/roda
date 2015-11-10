/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.storage.Binary;
import org.roda.core.storage.Container;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.Directory;
import org.roda.core.storage.Resource;
import org.roda.core.storage.StoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageServiceException;

/**
 * 
 * @author Luis Faria <lfaria@keep.pt>
 * @author Helder Silva <hsilva@keep.pt>
 * @author Sebastien Leroux <sleroux@keep.pt>
 *
 * @param <T>
 *          the storage service implementation
 */
public abstract class AbstractStorageServiceTest<T extends StorageService> {

  /**
   * Get current instance of storage.
   * 
   * @return
   */
  protected abstract T getStorage();

  /**
   * Clean up storage, making it empty.
   */
  @After
  public abstract void cleanUp();

  @Test
  public abstract void testClassInstantiation() throws StorageServiceException;

  @Test
  public void testListContainer() throws StorageServiceException {

    // 1) empty list of containers
    Iterator<Container> iterator = getStorage().listContainers().iterator();
    assertFalse(iterator.hasNext());

    // 2) container list with one element (which implies creating one
    // container)
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();

    getStorage().createContainer(containerStoragePath, containerMetadata);

    iterator = getStorage().listContainers().iterator();
    assertTrue(iterator.hasNext());
    Container next = iterator.next();
    assertNotNull(next);
    assertEquals(containerStoragePath, next.getStoragePath());
    assertEquals(containerMetadata, next.getMetadata());
    assertFalse(iterator.hasNext());

    // 3) list after cleanup
    getStorage().deleteContainer(containerStoragePath);
    iterator = getStorage().listContainers().iterator();
    assertFalse(iterator.hasNext());
  }

  @Test
  public void testCreateGetDeleteContainer() throws StorageServiceException {
    // 1) create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // 2) get created container
    Container container = getStorage().getContainer(containerStoragePath);
    assertNotNull(container);
    assertEquals(containerStoragePath, container.getStoragePath());
    assertEquals(containerMetadata, container.getMetadata());

    // 3) create container that already exists
    try {
      getStorage().createContainer(containerStoragePath, containerMetadata);
      fail("An exception should have been thrown while creating a container that already exists but it didn't happen!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.ALREADY_EXISTS, e.getCode());
    }

    // 4) delete container
    getStorage().deleteContainer(containerStoragePath);

    // 5) test delete container
    try {
      getStorage().getContainer(containerStoragePath);
      fail("An exception should have been thrown while getting a container that was deleted but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
    }
  }

  @Test
  public void testGetContainerThatDoesntExist() throws StorageServiceException {
    // 1) get container that doesn't exist
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    try {
      getStorage().getContainer(containerStoragePath);
      fail("An exception should have been thrown while getting a container that doesn't exist but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
    }
  }

  @Test
  public void testGetContainerThatIsActuallyADirectory() throws StorageServiceException {
    // set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    final StoragePath directoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> directoryMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createDirectory(directoryStoragePath, directoryMetadata);

    // get directory as it was a binary
    try {
      getStorage().getContainer(directoryStoragePath);
      fail(
        "An exception should have been thrown while getting a container which actually isn't a container but instead a directory but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.BAD_REQUEST, e.getCode());
    }

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testGetContainerThatIsActuallyABinary() throws StorageServiceException {
    // set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> binaryMetadata = StorageTestUtils.generateRandomMetadata();
    final ContentPayload payload = new RandomMockContentPayload();
    getStorage().createBinary(binaryStoragePath, binaryMetadata, payload, false);

    // get binary as it was a directory
    try {
      getStorage().getContainer(binaryStoragePath);
      fail(
        "An exception should have been thrown while getting a container which actually isn't a container but instead a binary but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.BAD_REQUEST, e.getCode());
    }

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testDeleteContainerThatDoesntExist() throws StorageServiceException {

    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // 1) delete container that exists
    getStorage().deleteContainer(containerStoragePath);

    // 2) delete container that no longer exists
    try {
      getStorage().deleteContainer(containerStoragePath);
      fail(
        "An exception should have been thrown while deleting a container that no longer exists but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
    }

  }

  @Test
  public void testDeleteNonEmptyContaienr() throws StorageServiceException {
    // Set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);
    StoragePath directoryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> directoryMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createDirectory(directoryStoragePath, directoryMetadata);

    // 1) delete container recursively
    getStorage().deleteContainer(containerStoragePath);

    // 2) test delete container
    try {
      getStorage().getContainer(containerStoragePath);
      fail("An exception should have been thrown while getting a container that doesn't exist but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
    }

    try {
      getStorage().getDirectory(directoryStoragePath);
      fail("An exception should have been thrown while getting a container that doesn't exist but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
    }

  }

  @Test
  public void testListResourcesUnderContainer() throws StorageServiceException {
    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // 1) list empty container
    Iterable<Resource> resources = getStorage().listResourcesUnderContainer(containerStoragePath);
    assertNotNull(resources);
    assertNotNull(resources.iterator());
    assertFalse(resources.iterator().hasNext());

    // 2) list container with 2 resources beneath (directories)
    StoragePath directoryStoragePath1 = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> directoryMetadata1 = StorageTestUtils.generateRandomMetadata();
    getStorage().createDirectory(directoryStoragePath1, directoryMetadata1);

    StoragePath directoryStoragePath2 = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> directoryMetadata2 = StorageTestUtils.generateRandomMetadata();
    getStorage().createDirectory(directoryStoragePath2, directoryMetadata2);

    resources = getStorage().listResourcesUnderContainer(containerStoragePath);
    assertNotNull(resources);
    // Resource r1 = new DefaultDirectory(directoryStoragePath1,
    // directoryMetadata1);
    // Resource r2 = new DefaultDirectory(directoryStoragePath2,
    // directoryMetadata2);
    // assertThat(resources, containsInAnyOrder(r1, r2));
    assertThat(resources, Matchers.<Resource> iterableWithSize(2));

    // cleanup
    getStorage().deleteResource(containerStoragePath);
  }

  @Test
  public void testCreateGetDeleteDirectory() throws StorageServiceException {

    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // 1) create directory
    StoragePath directoryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> directoryMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createDirectory(directoryStoragePath, directoryMetadata);

    // 2) get created directory
    Directory directory = getStorage().getDirectory(directoryStoragePath);
    assertNotNull(directory);
    assertEquals(directoryStoragePath, directory.getStoragePath());
    directory.getMetadata().remove(RodaConstants.STORAGE_META_SIZE_IN_BYTES);
    assertEquals(directoryMetadata, directory.getMetadata());
    assertTrue(directory.isDirectory());

    // 3) create directory that already exists
    try {
      getStorage().createDirectory(directoryStoragePath, directoryMetadata);
      fail(
        "An exception should have been thrown while creating a directory that already exists but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.ALREADY_EXISTS, e.getCode());
    }

    // 4) delete directory
    getStorage().deleteResource(directoryStoragePath);

    // 5) test deleted directory
    try {
      getStorage().getDirectory(directoryStoragePath);
      fail("An exception should have been thrown while getting a directory that was deleted but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
    }

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testGetDirectoryThatDoesntExist() throws StorageServiceException {

    // set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);
    StoragePath directoryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);

    // get directory that doesn't exist
    try {
      getStorage().getDirectory(directoryStoragePath);
      fail("An exception should have been thrown while getting a directory that doesn't exist but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
    }

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testGetDirectoryThatIsActuallyABinary() throws StorageServiceException {
    // set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> binaryMetadata = StorageTestUtils.generateRandomMetadata();
    final ContentPayload payload = new RandomMockContentPayload();
    getStorage().createBinary(binaryStoragePath, binaryMetadata, payload, false);

    // get binary as it was a directory
    try {
      getStorage().getDirectory(binaryStoragePath);
      fail(
        "An exception should have been thrown while getting a directory which actually isn't a directory but instead a binary but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.BAD_REQUEST, e.getCode());
    }

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testGetDirectoryThatIsActuallyAContainer() throws StorageServiceException {
    // set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // get container as it was a directory
    try {
      getStorage().getDirectory(containerStoragePath);
      fail(
        "An exception should have been thrown while getting a directory which actually isn't a directory but instead a container but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.BAD_REQUEST, e.getCode());
    }

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testListResourcesUnderDirectory() throws StorageServiceException, IOException {

    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // create directory
    StoragePath directoryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> directoryMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createDirectory(directoryStoragePath, directoryMetadata);

    // 1) list empty directory
    Iterable<Resource> resources = getStorage().listResourcesUnderDirectory(directoryStoragePath);
    assertNotNull(resources);
    assertFalse(resources.iterator().hasNext());

    // 2) list directory with 2 resources beneath (directories)
    final StoragePath subDirectoryStoragePath1 = StorageTestUtils
      .generateRandomResourceStoragePathUnder(directoryStoragePath);
    final Map<String, Set<String>> subDirectoryMetadata1 = StorageTestUtils.generateRandomMetadata();
    getStorage().createDirectory(subDirectoryStoragePath1, subDirectoryMetadata1);

    final StoragePath subDirectoryStoragePath2 = StorageTestUtils
      .generateRandomResourceStoragePathUnder(directoryStoragePath);
    final Map<String, Set<String>> subDirectoryMetadata2 = StorageTestUtils.generateRandomMetadata();
    getStorage().createDirectory(subDirectoryStoragePath2, subDirectoryMetadata2);

    // add grand-child to ensure it is not listed
    final StoragePath subSubDirectoryStoragePath1 = StorageTestUtils
      .generateRandomResourceStoragePathUnder(subDirectoryStoragePath1);
    final Map<String, Set<String>> subSubDirectoryMetadata1 = StorageTestUtils.generateRandomMetadata();
    getStorage().createDirectory(subSubDirectoryStoragePath1, subSubDirectoryMetadata1);

    resources = getStorage().listResourcesUnderDirectory(directoryStoragePath);
    assertNotNull(resources);

    // Resource r1 = new DefaultDirectory(subDirectoryStoragePath1,
    // subDirectoryMetadata1);
    // Resource r2 = new DefaultDirectory(subDirectoryStoragePath2,
    // subDirectoryMetadata2);
    // assertThat(resources, containsInAnyOrder(r1, r2));
    assertThat(resources, Matchers.<Resource> iterableWithSize(2));

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  public void testBinaryContent(Binary binary, ContentPayload providedPayload) throws IOException {
    // check if content is the same
    assertTrue(IOUtils.contentEquals(providedPayload.createInputStream(), binary.getContent().createInputStream()));

    Path tempFile = Files.createTempFile("test", ".tmp");
    Files.copy(binary.getContent().createInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

    // check size in bytes
    assertEquals(Long.valueOf(Files.size(tempFile)), binary.getSizeInBytes());

    // TODO test content digest properly
    assertFalse(binary.getContentDigest().isEmpty());

    // delete temp file
    Files.delete(tempFile);
  }

  @Test
  public void testCreateGetDeleteBinary() throws StorageServiceException, IOException {

    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // 1) create binary
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> binaryMetadata = StorageTestUtils.generateRandomMetadata();
    final ContentPayload payload = new RandomMockContentPayload();
    getStorage().createBinary(binaryStoragePath, binaryMetadata, payload, false);

    // 2) get binary
    Binary binary = getStorage().getBinary(binaryStoragePath);
    assertNotNull(binary);
    assertEquals(binaryStoragePath, binary.getStoragePath());
    assertEquals(binaryMetadata, binary.getMetadata());
    assertFalse(binary.isDirectory());
    assertFalse(binary.isReference());
    testBinaryContent(binary, payload);

    // 3) create binary that already exists
    try {
      getStorage().createBinary(binaryStoragePath, binaryMetadata, payload, false);
      fail("An exception should have been thrown while creating a binary that already exists but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.ALREADY_EXISTS, e.getCode());
    }

    // 4) delete binary
    getStorage().deleteResource(binaryStoragePath);

    // 5) test deleted binary
    try {
      getStorage().getBinary(binaryStoragePath);
      fail("An exception should have been thrown while getting a binary that was deleted but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
    }

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testCreateGetDeleteBinaryAsReference() throws StorageServiceException, IOException {

    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // 1) create binary
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> binaryMetadata = StorageTestUtils.generateRandomMetadata();
    final ContentPayload payload = new RandomMockContentPayload();

    try {
      getStorage().createBinary(binaryStoragePath, binaryMetadata, payload, true);

      // 2) get binary
      Binary binary = getStorage().getBinary(binaryStoragePath);
      assertNotNull(binary);
      assertEquals(binaryStoragePath, binary.getStoragePath());
      assertEquals(binaryMetadata, binary.getMetadata());
      assertFalse(binary.isDirectory());
      assertTrue(binary.isReference());
      testBinaryContent(binary, payload);

      // 3) create binary that already exists
      try {
        getStorage().createBinary(binaryStoragePath, binaryMetadata, payload, false);
        fail(
          "An exception should have been thrown while creating a binary that already exists but it didn't happened!");
      } catch (StorageServiceException e) {
        assertEquals(StorageServiceException.ALREADY_EXISTS, e.getCode());
      }

      // 4) delete binary
      getStorage().deleteResource(binaryStoragePath);

      // 5) test deleted binary
      try {
        getStorage().getBinary(binaryStoragePath);
        fail("An exception should have been thrown while getting a binary that was deleted but it didn't happened!");
      } catch (StorageServiceException e) {
        assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
      }
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.NOT_IMPLEMENTED, e.getCode());
    }

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testUpdateBinaryContent() throws StorageServiceException, IOException {

    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // create binary
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> binaryMetadata = StorageTestUtils.generateRandomMetadata();
    final ContentPayload payload = new RandomMockContentPayload();
    getStorage().createBinary(binaryStoragePath, binaryMetadata, payload, false);

    // get created binary and save content to file
    final Binary binaryCreated = getStorage().getBinary(binaryStoragePath);
    assertNotNull(binaryCreated);

    final Path original = Files.createTempFile(binaryCreated.getStoragePath().getName(), ".tmp");
    binaryCreated.getContent().writeToPath(original);

    // 1) update binary content
    final ContentPayload newPayload = new RandomMockContentPayload();
    final Binary binaryUpdated = getStorage().updateBinaryContent(binaryStoragePath, newPayload, false, false);
    assertNotNull(binaryUpdated);
    assertFalse(IOUtils.contentEquals(Files.newInputStream(original), binaryUpdated.getContent().createInputStream()));

    testBinaryContent(binaryUpdated, newPayload);

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testUpdateBinaryThatDoesntExist() throws StorageServiceException, IOException {
    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // update binary content from binary that doesn't exist
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload payload = new RandomMockContentPayload();
    final boolean asReference = false;
    try {
      getStorage().updateBinaryContent(binaryStoragePath, payload, asReference, false);
      fail("An exception should have been thrown while updating a binary that doesn't exist but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
    }

    // update binary content now with createIfNotExists=true
    Binary updatedBinaryContent = getStorage().updateBinaryContent(binaryStoragePath, payload, asReference, true);
    testBinaryContent(updatedBinaryContent, payload);

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  // TODO test update binary from reference to reference
  // TODO test update binary from non-reference to reference
  // TODO test update binary from reference to non-reference

  @Test
  public void testGetBinaryThatDoesntExist() throws StorageServiceException {

    // set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);
    StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);

    // get binary that doesn't exist
    try {
      getStorage().getBinary(binaryStoragePath);
      fail("An exception should have been thrown while getting a binary that doesn't exist but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
    }

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testGetBinaryThatIsActuallyADirectory() throws StorageServiceException {
    // set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    final StoragePath directoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> directoryMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createDirectory(directoryStoragePath, directoryMetadata);

    // get directory as it was a binary
    try {
      getStorage().getBinary(directoryStoragePath);
      fail(
        "An exception should have been thrown while getting a binary which actually isn't a binary but instead a directory but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.BAD_REQUEST, e.getCode());
    }

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testGetBinaryThatIsActuallyAContainer() throws StorageServiceException {
    // set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // get container as it was a binary
    try {
      getStorage().getBinary(containerStoragePath);
      fail(
        "An exception should have been thrown while getting a binary which actually isn't a binary but instead a container but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.BAD_REQUEST, e.getCode());
    }

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testDeleteNonEmptyDirectory() throws StorageServiceException {

    // Set up
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);
    StoragePath directoryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> directoryMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createDirectory(directoryStoragePath, directoryMetadata);
    StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(directoryStoragePath);
    final Map<String, Set<String>> binaryMetadata = StorageTestUtils.generateRandomMetadata();
    final ContentPayload binaryPayload = new RandomMockContentPayload();
    getStorage().createBinary(binaryStoragePath, binaryMetadata, binaryPayload, false);

    // test recursively delete directory
    getStorage().deleteResource(directoryStoragePath);

    // test get sub-resource
    try {
      getStorage().getBinary(binaryStoragePath);
      fail("An exception should have been thrown while getting a binary that doesn't exist but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
    }

    // test specific cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testGetMetadataFromContainer() throws StorageServiceException, IOException {
    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // get directory metadata
    Map<String, Set<String>> metadata = getStorage().getMetadata(containerStoragePath);
    assertNotNull(metadata);
    assertEquals(containerMetadata, metadata);

    // test specific cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testGetMetadataFromDirectory() throws StorageServiceException {
    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // create directory
    final StoragePath directoryStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> directoryMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createDirectory(directoryStoragePath, directoryMetadata);

    // get directory metadata
    Map<String, Set<String>> metadata = getStorage().getMetadata(directoryStoragePath);
    assertNotNull(metadata);
    assertEquals(directoryMetadata, metadata);

    // test specific cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testGetMetadataFromBinary() throws StorageServiceException {
    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // create directory
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> binaryMetadata = StorageTestUtils.generateRandomMetadata();
    final ContentPayload binaryPayload = new RandomMockContentPayload();
    getStorage().createBinary(binaryStoragePath, binaryMetadata, binaryPayload, false);

    // get directory metadata
    Map<String, Set<String>> metadata = getStorage().getMetadata(binaryStoragePath);
    assertNotNull(metadata);
    assertEquals(binaryMetadata, metadata);

    // test specific cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  // TODO test get metadata from binary using reference

  @Test
  public void testUpdateMetadataFromContainer() throws StorageServiceException, IOException {

    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    final Map<String, Set<String>> containerMetadata2 = StorageTestUtils.generateRandomMetadata();

    // update metadata
    getStorage().updateMetadata(containerStoragePath, containerMetadata2, true);

    final Map<String, Set<String>> metadata = getStorage().getMetadata(containerStoragePath);
    assertNotNull(metadata);
    assertEquals(containerMetadata2, metadata);

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testUpdateMetadataFromDirectory() throws StorageServiceException, IOException {

    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    StoragePath directoryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> directoryMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createDirectory(directoryStoragePath, directoryMetadata);

    final Map<String, Set<String>> directoryMetadata2 = StorageTestUtils.generateRandomMetadata();

    // update metadata
    getStorage().updateMetadata(directoryStoragePath, directoryMetadata2, true);

    final Map<String, Set<String>> metadata = getStorage().getMetadata(directoryStoragePath);
    assertNotNull(metadata);
    assertEquals(directoryMetadata2, metadata);

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testUpdateMetadataFromBinary() throws StorageServiceException, IOException {

    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> binaryMetadata = StorageTestUtils.generateRandomMetadata();
    final ContentPayload payload = new RandomMockContentPayload();
    getStorage().createBinary(binaryStoragePath, binaryMetadata, payload, false);

    final Map<String, Set<String>> binaryMetadata2 = StorageTestUtils.generateRandomMetadata();

    // update metadata
    getStorage().updateMetadata(binaryStoragePath, binaryMetadata2, true);

    final Map<String, Set<String>> metadata = getStorage().getMetadata(binaryStoragePath);
    assertNotNull(metadata);
    assertEquals(binaryMetadata2, metadata);

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  // TODO test update metadata from binary as reference
  // TODO test update metadata with replaceAll=false

  @Test
  public void testCopyContainerToSameStorage() throws StorageServiceException, IOException {
    // create and populate source container
    final StoragePath sourceContainerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> sourceContainerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(sourceContainerStoragePath, sourceContainerMetadata);

    StorageTestUtils.populate(getStorage(), sourceContainerStoragePath);

    final StoragePath targetContainerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();

    getStorage().copy(getStorage(), sourceContainerStoragePath, targetContainerStoragePath);
    StorageTestUtils.testEntityEqualRecursively(getStorage(), sourceContainerStoragePath, getStorage(),
      targetContainerStoragePath);

    // cleanup
    getStorage().deleteContainer(sourceContainerStoragePath);
    getStorage().deleteContainer(targetContainerStoragePath);
  }

  @Test
  public void testCopyDirectoryToSameStorage() throws StorageServiceException, IOException {
    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // create and populate source directory
    final StoragePath sourceDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> sourceDirectoryMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createDirectory(sourceDirectoryStoragePath, sourceDirectoryMetadata);

    StorageTestUtils.populate(getStorage(), sourceDirectoryStoragePath);

    final StoragePath targetDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);

    getStorage().copy(getStorage(), sourceDirectoryStoragePath, targetDirectoryStoragePath);
    StorageTestUtils.testEntityEqualRecursively(getStorage(), sourceDirectoryStoragePath, getStorage(),
      targetDirectoryStoragePath);

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testCopyBinaryToSameStorage() throws StorageServiceException, IOException {
    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // create binary
    final StoragePath sourceBinaryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> sourceBinaryMetadata = StorageTestUtils.generateRandomMetadata();
    final ContentPayload sourcePayload = new RandomMockContentPayload();
    getStorage().createBinary(sourceBinaryStoragePath, sourceBinaryMetadata, sourcePayload, false);

    final StoragePath targetBinaryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);

    getStorage().copy(getStorage(), sourceBinaryStoragePath, targetBinaryStoragePath);

    final Binary sourceBinary = getStorage().getBinary(sourceBinaryStoragePath);
    final Binary targetBinary = getStorage().getBinary(targetBinaryStoragePath);
    assertEquals(sourceBinary.getMetadata(), targetBinary.getMetadata());
    assertEquals(sourceBinary.isDirectory(), targetBinary.isDirectory());
    assertEquals(sourceBinary.getContentDigest(), targetBinary.getContentDigest());
    assertEquals(sourceBinary.getSizeInBytes(), targetBinary.getSizeInBytes());
    assertEquals(sourceBinary.isReference(), targetBinary.isReference());
    assertTrue(IOUtils.contentEquals(sourceBinary.getContent().createInputStream(),
      targetBinary.getContent().createInputStream()));

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  // TODO test copy from different storage

  @Test
  public void testMoveContainerToSameStorage() throws StorageServiceException, IOException {
    // create and populate source container
    final StoragePath sourceContainerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> sourceContainerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(sourceContainerStoragePath, sourceContainerMetadata);

    StorageTestUtils.populate(getStorage(), sourceContainerStoragePath);

    // copy for comparison test
    final StoragePath copyContainerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();

    getStorage().copy(getStorage(), sourceContainerStoragePath, copyContainerStoragePath);

    // move
    final StoragePath targetContainerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();

    getStorage().move(getStorage(), sourceContainerStoragePath, targetContainerStoragePath);

    // check with copy
    StorageTestUtils.testEntityEqualRecursively(getStorage(), copyContainerStoragePath, getStorage(),
      targetContainerStoragePath);

    // test source does not exist
    try {
      getStorage().getContainer(sourceContainerStoragePath);
      fail("An exception should have been thrown while getting a container that was moved but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
    }

    // cleanup
    getStorage().deleteContainer(copyContainerStoragePath);
    getStorage().deleteContainer(targetContainerStoragePath);
  }

  @Test
  public void testMoveDirectoryToSameStorage() throws StorageServiceException, IOException {
    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // create and populate source directory
    final StoragePath sourceDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> sourceDirectoryMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createDirectory(sourceDirectoryStoragePath, sourceDirectoryMetadata);

    StorageTestUtils.populate(getStorage(), sourceDirectoryStoragePath);

    // copy for comparison test
    final StoragePath copyDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    getStorage().copy(getStorage(), sourceDirectoryStoragePath, copyDirectoryStoragePath);

    // move
    final StoragePath targetDirectoryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);

    getStorage().move(getStorage(), sourceDirectoryStoragePath, targetDirectoryStoragePath);

    // check with copy
    StorageTestUtils.testEntityEqualRecursively(getStorage(), copyDirectoryStoragePath, getStorage(),
      targetDirectoryStoragePath);

    // test source does not exist
    try {
      getStorage().getDirectory(sourceDirectoryStoragePath);
      fail("An exception should have been thrown while getting a directory that was moved but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
    }

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test
  public void testMoveBinaryToSameStorage() throws StorageServiceException, IOException {
    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    final Map<String, Set<String>> containerMetadata = StorageTestUtils.generateRandomMetadata();
    getStorage().createContainer(containerStoragePath, containerMetadata);

    // create binary
    final StoragePath sourceBinaryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    final Map<String, Set<String>> sourceBinaryMetadata = StorageTestUtils.generateRandomMetadata();
    final ContentPayload sourcePayload = new RandomMockContentPayload();
    getStorage().createBinary(sourceBinaryStoragePath, sourceBinaryMetadata, sourcePayload, false);

    // copy for comparison test
    final StoragePath copyBinaryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);

    getStorage().copy(getStorage(), sourceBinaryStoragePath, copyBinaryStoragePath);

    // move
    final StoragePath targetBinaryStoragePath = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);

    getStorage().move(getStorage(), sourceBinaryStoragePath, targetBinaryStoragePath);

    // check with copy
    final Binary copyBinary = getStorage().getBinary(copyBinaryStoragePath);
    final Binary targetBinary = getStorage().getBinary(targetBinaryStoragePath);
    assertEquals(copyBinary.getMetadata(), targetBinary.getMetadata());
    assertEquals(copyBinary.isDirectory(), targetBinary.isDirectory());
    assertEquals(copyBinary.getContentDigest(), targetBinary.getContentDigest());
    assertEquals(copyBinary.getSizeInBytes(), targetBinary.getSizeInBytes());
    assertEquals(copyBinary.isReference(), targetBinary.isReference());
    assertTrue(IOUtils.contentEquals(copyBinary.getContent().createInputStream(),
      targetBinary.getContent().createInputStream()));

    // test source does not exist
    try {
      getStorage().getBinary(sourceBinaryStoragePath);
      fail("An exception should have been thrown while getting a binary that was moved but it didn't happened!");
    } catch (StorageServiceException e) {
      assertEquals(StorageServiceException.NOT_FOUND, e.getCode());
    }

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  // TODO test move from different storage

}
