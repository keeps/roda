/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fedora;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Iterator;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.AbstractStorageServiceTest;
import org.roda.core.storage.Binary;
import org.roda.core.storage.BinaryVersion;
import org.roda.core.storage.Container;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.RandomMockContentPayload;
import org.roda.core.storage.StorageTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(enabled = false)
public class FedoraStorageServiceTestDelegate extends AbstractStorageServiceTest<FedoraStorageService> {

  public static int determineOpenDoorAndSetFedoraProperty(int defaultDoor) {
    int openDoor = defaultDoor;
    try {
      ServerSocket s = new ServerSocket(0);
      openDoor = s.getLocalPort();
      s.close();
    } catch (IOException e) {
      openDoor = defaultDoor;
    }
    System.setProperty("fcrepo.dynamic.test.port", openDoor + "");
    return openDoor;
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(FedoraStorageServiceTest.class);

  private static final String PROTOCOL = "http";
  private static final String HOSTNAME = "localhost";
  private static final int SERVER_PORT = determineOpenDoorAndSetFedoraProperty(9999);
  private static final String serverAddress = PROTOCOL + "://" + HOSTNAME + ":" + SERVER_PORT + "/";

  private final FedoraStorageService storage = new FedoraStorageService(serverAddress);

  @Override
  protected FedoraStorageService getStorage() {
    return storage;
  }

  @Override
  public void cleanUp() {
    try {
      for (Container container : storage.listContainers()) {
        storage.deleteContainer(container.getStoragePath());
      }
    } catch (RODAException e) {
      LOGGER.error("Error cleaning up", e);
    }
  }

  @Override
  public void testClassInstantiation() throws RODAException {
    final HttpClient client = HttpClientBuilder.create().setMaxConnPerRoute(Integer.MAX_VALUE)
      .setMaxConnTotal(Integer.MAX_VALUE).build();
    final HttpGet request = new HttpGet(serverAddress);
    HttpResponse response;
    try {
      response = client.execute(request);
      assertEquals(Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test(enabled = false)
  public void testListContainer() throws RODAException {
    super.testListContainer();
  }

  @Test(enabled = false)
  public void testCreateGetDeleteContainer() throws RODAException {
    super.testCreateGetDeleteContainer();
  }

  @Test(enabled = false)
  public void testGetContainerThatDoesntExist() throws RODAException {
    super.testGetContainerThatDoesntExist();
  }

  @Test(enabled = false)
  public void testGetContainerThatIsActuallyADirectory() throws RODAException {
    super.testGetContainerThatIsActuallyADirectory();
  }

  @Test(enabled = false)
  public void testGetContainerThatIsActuallyABinary() throws RODAException {
    super.testGetContainerThatIsActuallyABinary();
  }

  @Test(enabled = false)
  public void testDeleteContainerThatDoesntExist() throws RODAException {
    super.testDeleteContainerThatDoesntExist();
  }

  @Test(enabled = false)
  public void testDeleteNonEmptyContaienr() throws RODAException {
    super.testDeleteNonEmptyContaienr();
  }

  @Test(enabled = false)
  public void testListResourcesUnderContainer() throws RODAException {
    super.testListResourcesUnderContainer();
  }

  @Test(enabled = false)
  public void testCreateGetDeleteDirectory() throws RODAException {
    super.testCreateGetDeleteDirectory();
  }

  @Test(enabled = false)
  public void testGetDirectoryThatDoesntExist() throws RODAException {
    super.testGetDirectoryThatDoesntExist();
  }

  @Test(enabled = false)
  public void testGetDirectoryThatIsActuallyABinary() throws RODAException {
    super.testGetDirectoryThatIsActuallyABinary();
  }

  @Test(enabled = false)
  public void testGetDirectoryThatIsActuallyAContainer() throws RODAException {
    super.testGetDirectoryThatIsActuallyAContainer();
  }

  @Test(enabled = false)
  public void testListResourcesUnderDirectory() throws RODAException, IOException {
    super.testListResourcesUnderDirectory();
  }

  @Override
  public void testCreateGetDeleteBinary() throws RODAException, IOException {

    // create container
    final StoragePath containerStoragePath = StorageTestUtils.generateRandomContainerStoragePath();
    getStorage().createContainer(containerStoragePath);

    // 1) create binary
    final StoragePath binaryStoragePath = StorageTestUtils.generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload payload = new RandomMockContentPayload();
    getStorage().createBinary(binaryStoragePath, payload, false);

    // 2) get binary
    Binary binary = getStorage().getBinary(binaryStoragePath);
    assertNotNull(binary);
    assertEquals(binaryStoragePath, binary.getStoragePath());
    assertFalse(binary.isDirectory());
    assertFalse(binary.isReference());
    testBinaryContent(binary, payload);

    // 3) create binary that already exists
    // XXX Fedora does not handle this, i.e. to create a new
    // datastream when it already exists, very well and therefore catching
    // the already exists exception will no be tested
    try {
      getStorage().createBinary(binaryStoragePath, payload, false);
      // fail("An exception should have been thrown while creating a binary
      // that
      // already exists but it didn't happened!");
    } catch (AlreadyExistsException e) {
      // do nothing
      Assert.fail("Fedora did not support this, change this test when it starts to support it");
    }

    // 4) delete binary
    getStorage().deleteResource(binaryStoragePath);

    // 5) test deleted binary
    try {
      getStorage().getBinary(binaryStoragePath);
      Assert
        .fail("An exception should have been thrown while getting a binary that was deleted but it didn't happened!");
    } catch (NotFoundException e) {
      // do nothing
    }

    // 6) Versionning
    final StoragePath binaryStoragePathVersionning = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload payloadV0 = new RandomMockContentPayload();
    getStorage().createBinary(binaryStoragePathVersionning, payloadV0, false);
    getStorage().createBinaryVersion(binaryStoragePathVersionning, "V0");
    final ContentPayload payloadV1 = new RandomMockContentPayload();
    getStorage().updateBinaryContent(binaryStoragePathVersionning, payloadV1, false, true);
    getStorage().createBinaryVersion(binaryStoragePathVersionning, "V1");
    final ContentPayload payloadV2 = new RandomMockContentPayload();
    getStorage().updateBinaryContent(binaryStoragePathVersionning, payloadV2, false, true);
    getStorage().createBinaryVersion(binaryStoragePathVersionning, "V2");
    final ContentPayload payloadV3 = new RandomMockContentPayload();
    getStorage().updateBinaryContent(binaryStoragePathVersionning, payloadV3, false, true);
    getStorage().createBinaryVersion(binaryStoragePathVersionning, "V3");

    int counter = 0;
    CloseableIterable<BinaryVersion> versions = getStorage().listBinaryVersions(binaryStoragePathVersionning);
    Iterator<BinaryVersion> it = versions.iterator();
    while (it.hasNext()) {
      BinaryVersion next = it.next();
      counter++;
    }
    Assert.assertEquals(4, counter);
    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Test(enabled = false)
  public void testCreateGetDeleteBinaryAsReference() throws RODAException, IOException {
    super.testCreateGetDeleteBinaryAsReference();
  }

  @Test(enabled = false)
  public void testUpdateBinaryContent() throws RODAException, IOException {
    super.testUpdateBinaryContent();
  }

  @Test(enabled = false)
  public void testUpdateBinaryThatDoesntExist() throws RODAException, IOException {
    super.testUpdateBinaryThatDoesntExist();
  }

  @Test(enabled = false)
  public void testGetBinaryThatDoesntExist() throws RODAException {
    super.testGetBinaryThatDoesntExist();
  }

  @Test(enabled = false)
  public void testGetBinaryThatIsActuallyADirectory() throws RODAException {
    super.testGetBinaryThatIsActuallyADirectory();
  }

  @Test(enabled = false)
  public void testGetBinaryThatIsActuallyAContainer() throws RODAException {
    super.testGetBinaryThatIsActuallyAContainer();
  }

  @Test(enabled = false)
  public void testDeleteNonEmptyDirectory() throws RODAException {
    super.testDeleteNonEmptyDirectory();
  }

  @Test(enabled = false)
  public void testCopyContainerToSameStorage() throws RODAException, IOException {
    super.testCopyContainerToSameStorage();
  }

  @Test(enabled = false)
  public void testCopyDirectoryToSameStorage() throws RODAException, IOException {
    super.testCopyDirectoryToSameStorage();
  }

  @Test(enabled = false)
  public void testCopyBinaryToSameStorage() throws RODAException, IOException {
    super.testCopyBinaryToSameStorage();
  }

  @Test(enabled = false)
  public void testMoveContainerToSameStorage() throws RODAException, IOException {
    super.testMoveContainerToSameStorage();
  }

  @Test(enabled = false)
  public void testMoveDirectoryToSameStorage() throws RODAException, IOException {
    super.testMoveDirectoryToSameStorage();
  }

  @Test(enabled = false)
  public void testMoveBinaryToSameStorage() throws RODAException, IOException {
    super.testMoveBinaryToSameStorage();
  }

  @Test(enabled = false)
  public void testBinaryVersions() throws RODAException, IOException {
    super.testBinaryVersions();
  }

}
