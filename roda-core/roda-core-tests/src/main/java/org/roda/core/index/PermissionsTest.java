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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.plugins.base.UpdateAIPPermissionsPlugin;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {"all", "travis"})
public class PermissionsTest {

  private static Path basePath;
  private static ModelService model;
  private static IndexService index;

  private static Path corporaPath;
  private static StorageService corporaService;
  private static String aipCreator = "admin";

  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsTest.class);

  @BeforeClass
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(PermissionsTest.class, true);

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = true;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources);

    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = PermissionsTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    LOGGER.debug("Running index tests under storage {}", basePath);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testAIP() throws RODAException, ParseException {
    // Generate AIP Id
    final String aipId = UUID.randomUUID().toString();

    // Create AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_PERMISSIONS),
      aipCreator);

    index.commitAIPs();

    User user = null;
    boolean justActive = true;
    IndexResult<IndexedAIP> find1 = index.find(IndexedAIP.class, null, null, new Sublist(0, 10), null, user, justActive,
      new ArrayList<>());
    assertEquals(0, find1.getTotalCount());

    justActive = false;
    IndexResult<IndexedAIP> find2 = index.find(IndexedAIP.class, null, null, new Sublist(0, 10), null, user, justActive,
      new ArrayList<>());
    assertEquals(1, find2.getTotalCount());

    user = new User("testuser", "User with access", "", false);
    justActive = true;
    IndexResult<IndexedAIP> find3 = index.find(IndexedAIP.class, null, null, new Sublist(0, 10), null, user, justActive,
      new ArrayList<>());
    assertEquals(0, find3.getTotalCount());

    justActive = false;
    IndexResult<IndexedAIP> find4 = index.find(IndexedAIP.class, null, null, new Sublist(0, 10), null, user, justActive,
      new ArrayList<>());
    assertEquals(1, find4.getTotalCount());

    user = new User("guest", "User with access", "", true);
    justActive = true;
    IndexResult<IndexedAIP> find5 = index.find(IndexedAIP.class, null, null, new Sublist(0, 10), null, user, justActive,
      new ArrayList<>());
    assertEquals(0, find5.getTotalCount());

    justActive = false;
    IndexResult<IndexedAIP> find6 = index.find(IndexedAIP.class, null, null, new Sublist(0, 10), null, user, justActive,
      new ArrayList<>());
    assertEquals(0, find6.getTotalCount());

    user.addGroup("testgroup");
    IndexResult<IndexedAIP> find7 = index.find(IndexedAIP.class, null, null, new Sublist(0, 10), null, user, justActive,
      new ArrayList<>());
    assertEquals(1, find7.getTotalCount());

    model.deleteAIP(aipId);

  }

  @Test
  public void testRepresentation() throws RODAException, ParseException {
    // Generate AIP Id
    final String aipId = UUID.randomUUID().toString();

    // Create AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_PERMISSIONS),
      aipCreator);

    index.commitAIPs();

    User user = null;
    boolean justActive = true;
    IndexResult<IndexedRepresentation> find1 = index.find(IndexedRepresentation.class, null, null, new Sublist(0, 10),
      null, user, justActive, new ArrayList<>());
    assertEquals(0, find1.getTotalCount());

    justActive = false;
    IndexResult<IndexedRepresentation> find2 = index.find(IndexedRepresentation.class, null, null, new Sublist(0, 10),
      null, user, justActive, new ArrayList<>());
    assertEquals(2, find2.getTotalCount());

    user = new User("testuser", "User with access", "", false);
    justActive = true;
    IndexResult<IndexedRepresentation> find3 = index.find(IndexedRepresentation.class, null, null, new Sublist(0, 10),
      null, user, justActive, new ArrayList<>());
    assertEquals(0, find3.getTotalCount());

    justActive = false;
    IndexResult<IndexedRepresentation> find4 = index.find(IndexedRepresentation.class, null, null, new Sublist(0, 10),
      null, user, justActive, new ArrayList<>());
    assertEquals(2, find4.getTotalCount());

    user = new User("guest", "User with access", "", true);
    justActive = true;
    IndexResult<IndexedRepresentation> find5 = index.find(IndexedRepresentation.class, null, null, new Sublist(0, 10),
      null, user, justActive, new ArrayList<>());
    assertEquals(0, find5.getTotalCount());

    justActive = false;
    IndexResult<IndexedRepresentation> find6 = index.find(IndexedRepresentation.class, null, null, new Sublist(0, 10),
      null, user, justActive, new ArrayList<>());
    assertEquals(0, find6.getTotalCount());

    user.addGroup("testgroup");
    IndexResult<IndexedRepresentation> find7 = index.find(IndexedRepresentation.class, null, null, new Sublist(0, 10),
      null, user, justActive, new ArrayList<>());
    assertEquals(2, find7.getTotalCount());

    model.deleteAIP(aipId);
  }

  @Test
  public void testFiles() throws RODAException, ParseException {
    // Generate AIP Id
    final String aipId = UUID.randomUUID().toString();

    // Create AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_PERMISSIONS),
      aipCreator);

    index.commitAIPs();

    User user = null;
    boolean justActive = true;
    IndexResult<IndexedFile> find1 = index.find(IndexedFile.class, null, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(0, find1.getTotalCount());

    justActive = false;
    IndexResult<IndexedFile> find2 = index.find(IndexedFile.class, null, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(4, find2.getTotalCount());

    user = new User("testuser", "User with access", "", false);
    justActive = true;
    IndexResult<IndexedFile> find3 = index.find(IndexedFile.class, null, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(0, find3.getTotalCount());

    justActive = false;
    IndexResult<IndexedFile> find4 = index.find(IndexedFile.class, null, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(4, find4.getTotalCount());

    user = new User("guest", "User with access", "", true);
    justActive = true;
    IndexResult<IndexedFile> find5 = index.find(IndexedFile.class, null, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(0, find5.getTotalCount());

    justActive = false;
    IndexResult<IndexedFile> find6 = index.find(IndexedFile.class, null, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(0, find6.getTotalCount());

    user.addGroup("testgroup");
    IndexResult<IndexedFile> find7 = index.find(IndexedFile.class, null, null, new Sublist(0, 10), null, user,
      justActive, new ArrayList<>());
    assertEquals(4, find7.getTotalCount());

    model.deleteAIP(aipId);
  }

  @Test
  public void testPreservationEvents() throws RODAException, ParseException {
    // Generate AIP Id
    final String aipId = UUID.randomUUID().toString();

    // Create AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_PERMISSIONS),
      aipCreator);

    index.commitAIPs();

    User user = null;
    boolean justActive = true;
    IndexResult<IndexedPreservationEvent> find1 = index.find(IndexedPreservationEvent.class, null, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(0, find1.getTotalCount());

    justActive = false;
    IndexResult<IndexedPreservationEvent> find2 = index.find(IndexedPreservationEvent.class, null, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(2, find2.getTotalCount());

    user = new User("testuser", "User with access", "", false);
    justActive = true;
    IndexResult<IndexedPreservationEvent> find3 = index.find(IndexedPreservationEvent.class, null, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(0, find3.getTotalCount());

    justActive = false;
    IndexResult<IndexedPreservationEvent> find4 = index.find(IndexedPreservationEvent.class, null, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(2, find4.getTotalCount());

    user = new User("guest", "User with access", "", true);
    justActive = true;
    IndexResult<IndexedPreservationEvent> find5 = index.find(IndexedPreservationEvent.class, null, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(0, find5.getTotalCount());

    justActive = false;
    IndexResult<IndexedPreservationEvent> find6 = index.find(IndexedPreservationEvent.class, null, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(0, find6.getTotalCount());

    user.addGroup("testgroup");
    IndexResult<IndexedPreservationEvent> find7 = index.find(IndexedPreservationEvent.class, null, null,
      new Sublist(0, 10), null, user, justActive, new ArrayList<>());
    assertEquals(2, find7.getTotalCount());

    model.deleteAIP(aipId);
  }

  @Test
  public void testUpdatePermissionsRecursively() throws RODAException, ParseException {
    // INFO this test may fail if the default objects change
    User user = new User();
    user.setName("rodauser");
    user.setFullName("Roda User");
    user.setEmail("rodauser@example.com");
    model.registerUser(user, "rodapassword", false);

    // change parent permissions
    AIP parent = model.retrieveAIP("8faeaf8c-9a1f-49cc-a239-f3410d8bc13b");
    Set<PermissionType> userPermissions = new HashSet<>();
    userPermissions.add(PermissionType.READ);
    parent.getPermissions().setUserPermissions(user.getName(), userPermissions);
    model.updateAIPPermissions(parent, RodaConstants.ADMIN);

    // change a child permissions as well
    AIP child = model.retrieveAIP("ee1b2aa0-d336-418b-ad1d-1aa659fb97a8");
    child.getPermissions().setUserPermissions(user.getName(), userPermissions);
    model.updateAIPPermissions(child, RodaConstants.ADMIN);
    index.commitAIPs();

    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, parent.getId()),
      new NotSimpleFilterParameter("permission_users_READ", user.getName()));
    SelectedItemsFilter<IndexedAIP> selectedItems = new SelectedItemsFilter<IndexedAIP>(filter,
      IndexedAIP.class.getName(), Boolean.FALSE);

    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_AIP_ID, parent.getId());
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, "Update descendant AIPs");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION,
      "The process of updating an object of the repository.");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OUTCOME_TEXT,
      "Parent permissions were updated and all sublevels will be too");

    Job job = TestsHelper.executeJob(UpdateAIPPermissionsPlugin.class, pluginParameters, PluginType.INTERNAL,
      selectedItems);
    assertEquals(6, job.getJobStats().getSourceObjectsCount());

    Set<PermissionType> permissions = model.retrieveAIP("d743a569-945a-49d9-844f-39af67992f05").getPermissions()
      .getUserPermissions(user.getName());
    assertEquals(permissions.contains(PermissionType.READ), true);
  }

  private AIP addCorporaDefautAIP(String aipId) throws RequestNotValidException, GenericException,
    AuthorizationDeniedException, AlreadyExistsException, NotFoundException, ValidationException {
    return model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, aipId), aipCreator);
  }

}
