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
import java.nio.file.Paths;
import java.util.Iterator;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
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
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

// @RunWith(SpringJUnit4ClassRunner.class)
@Test(groups = {"all", "travis"})
@ContextConfiguration("/fcrepo/spring-test/test-container.xml")
public class FedoraStorageServiceTest extends AbstractStorageServiceTest<FedoraStorageService> {
  private static final Logger LOGGER = LoggerFactory.getLogger(FedoraStorageServiceTest.class);

  private static final String PROTOCOL = "http";
  private static final String HOSTNAME = "localhost";
  private static final int SERVER_PORT = determineOpenDoorAndSetFedoraProperty(9999);
  private static final String serverAddress = PROTOCOL + "://" + HOSTNAME + ":" + SERVER_PORT + "/";

  private final FedoraStorageService storage = new FedoraStorageService(serverAddress);

  @AfterClass
  public static void tearDown() throws NotFoundException, GenericException {
    FSUtils.deletePath(Paths.get("fcrepo4-data"));
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
    System.setProperty("fcrepo.dynamic.test.port", openDoor + "");
    return openDoor;
  }

  @Test
  @Override
  public void testClassInstantiation() {
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

  @Test
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
      // fail("An exception should have been thrown while creating a binary that
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
  public void testMoveBinaryToSameStorage() throws RODAException, IOException {
    super.testMoveBinaryToSameStorage();
  }

  @Test(enabled = false)
  public void testBinaryVersions() throws RODAException, IOException {
    // TODO re-introduce this test once workaround to last version delete
    // constraint is done
    super.testBinaryVersions();
  }

}
