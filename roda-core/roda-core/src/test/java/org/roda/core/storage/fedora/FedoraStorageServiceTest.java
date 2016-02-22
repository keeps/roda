/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage.fedora;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Paths;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.storage.AbstractStorageServiceTest;
import org.roda.core.storage.Binary;
import org.roda.core.storage.Container;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.RandomMockContentPayload;
import org.roda.core.storage.StorageTestUtils;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
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
      fail(e.getMessage());
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
      fail("Fedora did not support this, change this test when it starts to support it");
    }

    // 4) delete binary
    getStorage().deleteResource(binaryStoragePath);

    // 5) test deleted binary
    try {
      getStorage().getBinary(binaryStoragePath);
      fail("An exception should have been thrown while getting a binary that was deleted but it didn't happened!");
    } catch (NotFoundException e) {
      // do nothing
    }

    // cleanup
    getStorage().deleteContainer(containerStoragePath);
  }

  @Ignore
  @Test
  public void testMoveBinaryToSameStorage() throws RODAException, IOException {
    super.testMoveBinaryToSameStorage();
  }

  @Ignore
  @Test
  public void testBinaryVersions() throws RODAException, IOException {
    super.testBinaryVersions();
  }

}
