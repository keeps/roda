/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import static org.testng.AssertJUnit.assertEquals;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class PermissionsTest {

  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;

  private static StorageService corporaService;

  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsTest.class);

  @BeforeClass
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(PermissionsTest.class, true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = false;
    boolean deployPluginManager = false;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false, ldapUtilityTestHelper.getLdapUtility());

    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = PermissionsTest.class.getResource("/corpora");
    corporaService = new FileStorageService(Paths.get(corporaURL.toURI()));

    LOGGER.debug("Running index tests under storage {}", basePath);
  }

  @BeforeMethod
  public static void resetIndex() {
    IndexTestUtils.resetIndex();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testAIP() throws RODAException {
    // Generate AIP Id
    final String aipId = IdUtils.createUUID();

    // Create AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_PERMISSIONS),
      RodaConstants.ADMIN);

    index.commitAIPs();

    User user = null;
    boolean justActive = true;
    IndexResult<IndexedAIP> find1 = index.find(IndexedAIP.class, Filter.ALL, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(0, find1.getTotalCount());

    justActive = false;
    IndexResult<IndexedAIP> find2 = index.find(IndexedAIP.class, Filter.ALL, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(1, find2.getTotalCount());

    user = new User("testuser", "User with access", "", false);
    justActive = true;
    IndexResult<IndexedAIP> find3 = index.find(IndexedAIP.class, Filter.ALL, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(0, find3.getTotalCount());

    justActive = false;
    IndexResult<IndexedAIP> find4 = index.find(IndexedAIP.class, Filter.ALL, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(1, find4.getTotalCount());

    user = new User("guest", "User with access", "", true);
    justActive = true;
    IndexResult<IndexedAIP> find5 = index.find(IndexedAIP.class, Filter.ALL, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(0, find5.getTotalCount());

    justActive = false;
    IndexResult<IndexedAIP> find6 = index.find(IndexedAIP.class, Filter.ALL, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(0, find6.getTotalCount());

    user.addGroup("testgroup");
    IndexResult<IndexedAIP> find7 = index.find(IndexedAIP.class, Filter.ALL, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(1, find7.getTotalCount());

    model.deleteAIP(aipId);

  }

  @Test
  public void testRepresentation() throws RODAException {
    // Generate AIP Id
    final String aipId = IdUtils.createUUID();

    // Create AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_PERMISSIONS),
      RodaConstants.ADMIN);

    index.commitAIPs();

    User user = null;
    boolean justActive = true;
    IndexResult<IndexedRepresentation> find1 = index.find(IndexedRepresentation.class, Filter.ALL, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(0, find1.getTotalCount());

    justActive = false;
    IndexResult<IndexedRepresentation> find2 = index.find(IndexedRepresentation.class, Filter.ALL, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(2, find2.getTotalCount());

    user = new User("testuser", "User with access", "", false);
    justActive = true;
    IndexResult<IndexedRepresentation> find3 = index.find(IndexedRepresentation.class, Filter.ALL, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(0, find3.getTotalCount());

    justActive = false;
    IndexResult<IndexedRepresentation> find4 = index.find(IndexedRepresentation.class, Filter.ALL, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(2, find4.getTotalCount());

    user = new User("guest", "User with access", "", true);
    justActive = true;
    IndexResult<IndexedRepresentation> find5 = index.find(IndexedRepresentation.class, Filter.ALL, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(0, find5.getTotalCount());

    justActive = false;
    IndexResult<IndexedRepresentation> find6 = index.find(IndexedRepresentation.class, Filter.ALL, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(0, find6.getTotalCount());

    user.addGroup("testgroup");
    IndexResult<IndexedRepresentation> find7 = index.find(IndexedRepresentation.class, Filter.ALL, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(2, find7.getTotalCount());

    model.deleteAIP(aipId);
  }

  @Test
  public void testFiles() throws RODAException {
    // Generate AIP Id
    final String aipId = IdUtils.createUUID();

    // Create AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_PERMISSIONS),
      RodaConstants.ADMIN);

    index.commitAIPs();

    User user = null;
    boolean justActive = true;
    IndexResult<IndexedFile> find1 = index.find(IndexedFile.class, Filter.ALL, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(0, find1.getTotalCount());

    justActive = false;
    IndexResult<IndexedFile> find2 = index.find(IndexedFile.class, Filter.ALL, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(4, find2.getTotalCount());

    user = new User("testuser", "User with access", "", false);
    justActive = true;
    IndexResult<IndexedFile> find3 = index.find(IndexedFile.class, Filter.ALL, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(0, find3.getTotalCount());

    justActive = false;
    IndexResult<IndexedFile> find4 = index.find(IndexedFile.class, Filter.ALL, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(4, find4.getTotalCount());

    user = new User("guest", "User with access", "", true);
    justActive = true;
    IndexResult<IndexedFile> find5 = index.find(IndexedFile.class, Filter.ALL, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(0, find5.getTotalCount());

    justActive = false;
    IndexResult<IndexedFile> find6 = index.find(IndexedFile.class, Filter.ALL, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(0, find6.getTotalCount());

    user.addGroup("testgroup");
    IndexResult<IndexedFile> find7 = index.find(IndexedFile.class, Filter.ALL, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(4, find7.getTotalCount());

    model.deleteAIP(aipId);
  }

  @Test
  public void testPreservationEvents() throws RODAException {
    // Generate AIP Id
    final String aipId = IdUtils.createUUID();

    // Create AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_PERMISSIONS),
      RodaConstants.ADMIN);

    index.commitAIPs();

    User user = null;
    boolean justActive = true;
    IndexResult<IndexedPreservationEvent> find1 = index.find(IndexedPreservationEvent.class, Filter.ALL, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(0, find1.getTotalCount());

    justActive = false;
    IndexResult<IndexedPreservationEvent> find2 = index.find(IndexedPreservationEvent.class, Filter.ALL, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(2, find2.getTotalCount());

    user = new User("testuser", "User with access", "", false);
    justActive = true;
    IndexResult<IndexedPreservationEvent> find3 = index.find(IndexedPreservationEvent.class, Filter.ALL, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(0, find3.getTotalCount());

    justActive = false;
    IndexResult<IndexedPreservationEvent> find4 = index.find(IndexedPreservationEvent.class, Filter.ALL, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(2, find4.getTotalCount());

    user = new User("guest", "User with access", "", true);
    justActive = true;
    IndexResult<IndexedPreservationEvent> find5 = index.find(IndexedPreservationEvent.class, Filter.ALL, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(0, find5.getTotalCount());

    justActive = false;
    IndexResult<IndexedPreservationEvent> find6 = index.find(IndexedPreservationEvent.class, Filter.ALL, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(0, find6.getTotalCount());

    user.addGroup("testgroup");
    IndexResult<IndexedPreservationEvent> find7 = index.find(IndexedPreservationEvent.class, Filter.ALL, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(2, find7.getTotalCount());

    model.deleteAIP(aipId);
  }

}
