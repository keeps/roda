/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core_disabled.storage.fedora;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
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
import org.roda.core.storage.fedora.FedoraStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(enabled = false)
public class FedoraStorageServiceTestDelegate extends AbstractStorageServiceTest<FedoraStorageService> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FedoraStorageServiceTest.class);

  private static final String PROTOCOL = "http";
  private static final String HOSTNAME = "localhost";
  private static final int SERVER_PORT = determineOpenDoorAndSetFedoraProperty(9999);
  private static final String SERVER_ADDRESS = PROTOCOL + "://" + HOSTNAME + ":" + SERVER_PORT + "/";

  private final FedoraStorageService storage = new FedoraStorageService(SERVER_ADDRESS);

  @Override
  protected FedoraStorageService getStorage() {
    return storage;
  }

  public static int determineOpenDoorAndSetFedoraProperty(int defaultDoor) {
    int openDoor = defaultDoor;
    try {
      ServerSocket s = new ServerSocket(0);
      openDoor = s.getLocalPort();
      s.close();
    } catch (IOException e) {
      openDoor = defaultDoor;
    }
    System.setProperty("fcrepo.dynamic.test.port", Integer.toString(openDoor));
    return openDoor;
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
    final HttpGet request = new HttpGet(SERVER_ADDRESS);
    HttpResponse response;
    try {
      response = client.execute(request);
      assertEquals(RodaConstants.STATUS_OK, response.getStatusLine().getStatusCode());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Override
  @Test(enabled = false)
  public void testListContainer() throws RODAException {
    super.testListContainer();
  }

  @Override
  @Test(enabled = false)
  public void testCreateGetDeleteContainer() throws RODAException {
    super.testCreateGetDeleteContainer();
  }

  @Override
  @Test(enabled = false)
  public void testGetContainerThatDoesntExist() throws RODAException {
    super.testGetContainerThatDoesntExist();
  }

  @Override
  @Test(enabled = false)
  public void testGetContainerThatIsActuallyADirectory() throws RODAException {
    super.testGetContainerThatIsActuallyADirectory();
  }

  @Override
  @Test(enabled = false)
  public void testGetContainerThatIsActuallyABinary() throws RODAException {
    super.testGetContainerThatIsActuallyABinary();
  }

  @Override
  @Test(enabled = false)
  public void testDeleteContainerThatDoesntExist() throws RODAException {
    super.testDeleteContainerThatDoesntExist();
  }

  @Override
  @Test(enabled = false)
  public void testDeleteNonEmptyContainer() throws RODAException {
    super.testDeleteNonEmptyContainer();
  }

  @Override
  @Test(enabled = false)
  public void testListResourcesUnderContainer() throws RODAException {
    super.testListResourcesUnderContainer();
  }

  @Override
  @Test(enabled = false)
  public void testCreateGetDeleteDirectory() throws RODAException {
    super.testCreateGetDeleteDirectory();
  }

  @Override
  @Test(enabled = false)
  public void testGetDirectoryThatDoesntExist() throws RODAException {
    super.testGetDirectoryThatDoesntExist();
  }

  @Override
  @Test(enabled = false)
  public void testGetDirectoryThatIsActuallyABinary() throws RODAException {
    super.testGetDirectoryThatIsActuallyABinary();
  }

  @Override
  @Test(enabled = false)
  public void testGetDirectoryThatIsActuallyAContainer() throws RODAException {
    super.testGetDirectoryThatIsActuallyAContainer();
  }

  @Override
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
      // that already exists but it didn't happened!");
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
    Map<String, String> properties = new HashMap<>();
    properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());

    final StoragePath binaryStoragePathVersionning = StorageTestUtils
      .generateRandomResourceStoragePathUnder(containerStoragePath);
    final ContentPayload payloadV0 = new RandomMockContentPayload();
    getStorage().createBinary(binaryStoragePathVersionning, payloadV0, false);
    properties.put(RodaConstants.VERSION_MESSAGE, "V0");
    getStorage().createBinaryVersion(binaryStoragePathVersionning, properties);
    final ContentPayload payloadV1 = new RandomMockContentPayload();
    getStorage().updateBinaryContent(binaryStoragePathVersionning, payloadV1, false, true);
    properties.put(RodaConstants.VERSION_MESSAGE, "V1");
    getStorage().createBinaryVersion(binaryStoragePathVersionning, properties);
    final ContentPayload payloadV2 = new RandomMockContentPayload();
    getStorage().updateBinaryContent(binaryStoragePathVersionning, payloadV2, false, true);
    properties.put(RodaConstants.VERSION_MESSAGE, "V2");
    getStorage().createBinaryVersion(binaryStoragePathVersionning, properties);
    final ContentPayload payloadV3 = new RandomMockContentPayload();
    getStorage().updateBinaryContent(binaryStoragePathVersionning, payloadV3, false, true);
    properties.put(RodaConstants.VERSION_MESSAGE, "V3");
    getStorage().createBinaryVersion(binaryStoragePathVersionning, properties);

    int counter = 0;
    CloseableIterable<BinaryVersion> versions = getStorage().listBinaryVersions(binaryStoragePathVersionning);
    Iterator<BinaryVersion> it = versions.iterator();
    while (it.hasNext()) {
      it.next();
      counter++;
    }
    Assert.assertEquals(counter, 4);
    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Override
  @Test(enabled = false)
  public void testCreateGetDeleteBinaryAsReference() throws RODAException, IOException {
    super.testCreateGetDeleteBinaryAsReference();
  }

  @Override
  @Test(enabled = false)
  public void testUpdateBinaryContent() throws RODAException, IOException {
    super.testUpdateBinaryContent();
  }

  @Override
  @Test(enabled = false)
  public void testUpdateBinaryThatDoesntExist() throws RODAException, IOException {
    super.testUpdateBinaryThatDoesntExist();
  }

  @Override
  @Test(enabled = false)
  public void testGetBinaryThatDoesntExist() throws RODAException {
    super.testGetBinaryThatDoesntExist();
  }

  @Override
  @Test(enabled = false)
  public void testGetBinaryThatIsActuallyADirectory() throws RODAException {
    super.testGetBinaryThatIsActuallyADirectory();
  }

  @Override
  @Test(enabled = false)
  public void testGetBinaryThatIsActuallyAContainer() throws RODAException {
    super.testGetBinaryThatIsActuallyAContainer();
  }

  @Override
  @Test(enabled = false)
  public void testDeleteNonEmptyDirectory() throws RODAException {
    super.testDeleteNonEmptyDirectory();
  }

  @Override
  @Test(enabled = false)
  public void testCopyContainerToSameStorage() throws RODAException, IOException {
    super.testCopyContainerToSameStorage();
  }

  @Override
  @Test(enabled = false)
  public void testCopyDirectoryToSameStorage() throws RODAException, IOException {
    super.testCopyDirectoryToSameStorage();
  }

  @Override
  @Test(enabled = false)
  public void testCopyBinaryToSameStorage() throws RODAException, IOException {
    super.testCopyBinaryToSameStorage();
  }

  @Override
  @Test(enabled = false)
  public void testMoveContainerToSameStorage() throws RODAException, IOException {
    super.testMoveContainerToSameStorage();
  }

  @Override
  @Test(enabled = false)
  public void testMoveDirectoryToSameStorage() throws RODAException, IOException {
    super.testMoveDirectoryToSameStorage();
  }

  @Override
  @Test(enabled = false)
  public void testMoveBinaryToSameStorage() throws RODAException, IOException {
    super.testMoveBinaryToSameStorage();
  }

  @Override
  @Test(enabled = false)
  public void testBinaryVersions() throws RODAException, IOException {
    super.testBinaryVersions();
  }

}
