/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.storage;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageServiceTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eduardo Teixeira <eteixeira@keep.pt>
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL})
public class DeleteAIPPermissionTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteAIPPermissionTest.class);
  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;

  @BeforeMethod
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(FileStorageServiceTest.class, true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    RodaCoreFactory.instantiateTest(true, true, true, true, true, false, false, ldapUtilityTestHelper.getLdapUtility());
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();
  }

  @AfterMethod
  public void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testDeleteParentBlockedByChildPermission()
    throws GenericException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException,
    IllegalArgumentException, IllegalOperationException, RequestNotValidException {
    String username = "user1st";
    User testUser = createUser(username, "user1st@test.pt", "roda");
    Assert.assertFalse(UserUtility.isAdministrator(testUser), "user should not have admin permissions");

    AIP parent = model.createAIP(null, "Parent AIP", new Permissions(), RodaConstants.ADMIN, null);
    AIP child = model.createAIP(parent.getId(), "Child AIP", new Permissions(), RodaConstants.ADMIN, null);

    List<Permissions.PermissionType> userPermissionsParent = Arrays.asList(Permissions.PermissionType.READ,
      Permissions.PermissionType.DELETE);
    List<Permissions.PermissionType> userPermissionsChild = List.of(Permissions.PermissionType.READ);
    setAIPPermissions(parent.getId(), username, userPermissionsParent);
    setAIPPermissions(child.getId(), username, userPermissionsChild);
    index.commit(IndexedAIP.class);

    Assert.assertTrue(hasPermissionToDelete(parent, username), "has permission to delete parent AIP");
    Assert.assertFalse(hasPermissionToDelete(child, username), "does not have permission to delete child AIP");

    SelectedItemsList<IndexedAIP> selectedItems = SelectedItemsList.create(IndexedAIP.class, parent.getId());
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, "testDeleteParentBLockedByChildPermission");
    parameters.put(RodaConstants.PLUGIN_PARAMS_DONT_CHECK_RELATIVES, "false");
    Job job = TestsHelper.executeJob(DeleteRODAObjectPlugin.class, parameters, PluginType.INTERNAL, selectedItems,
      username);

    try (CloseableIterable<OptionalWithCause<Report>> jobReport = model.listJobReports(job.getId())) {
      for (OptionalWithCause<Report> report : jobReport) {
        if (report.isPresent()) {
          Report r = report.get();
          LOGGER.info(
            "JOB_REPORT jobId={} reportId={} pluginState={} sourceId={} outcomeId={} plugin={} txId={} details={}",
            r.getJobId(), r.getId(), r.getPluginState(), r.getSourceObjectId(), r.getOutcomeObjectId(), r.getPlugin(),
            r.getTransactionId(), r.getPluginDetails() == null ? "" : r.getPluginDetails().replace("\n", " | "));
        } else
          LOGGER.warn("job missing");
      }
    } catch (IOException e) {
      LOGGER.warn(e.getMessage());
    }

    // after jobb
    Assert.assertEquals(job.getState(), Job.JOB_STATE.COMPLETED);
    Assert.assertTrue(aipExists(parent.getId()), "parent AIP should continue to exist");
    Assert.assertTrue(aipExists(child.getId()), "son should continue existing");

  }

  @Test
  public void testDeleteParentChildPermission()
    throws GenericException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException,
    IllegalArgumentException, IllegalOperationException, RequestNotValidException {

    String username = "user2";
    User testUser2 = createUser(username, "user2@test.pt", "roda");
    Assert.assertFalse(UserUtility.isAdministrator(testUser2), "user should not have admin permissions.");

    AIP parent = model.createAIP(null, "Parent AIP", new Permissions(), RodaConstants.ADMIN, null);
    AIP child = model.createAIP(parent.getId(), "Child AIP", new Permissions(), RodaConstants.ADMIN, null);

    List<Permissions.PermissionType> userPermissionsParent = Arrays.asList(Permissions.PermissionType.READ,
      Permissions.PermissionType.DELETE);
    List<Permissions.PermissionType> userPermissionsChild = Arrays.asList(Permissions.PermissionType.READ,
      Permissions.PermissionType.DELETE);
    setAIPPermissions(parent.getId(), username, userPermissionsParent);
    setAIPPermissions(child.getId(), username, userPermissionsChild);
    index.commit(IndexedAIP.class);

    Assert.assertTrue(hasPermissionToDelete(parent, username), "has permission to delete parent AIP.");
    Assert.assertTrue(hasPermissionToDelete(child, username), "has permission to delete child AIP.");

    SelectedItemsList<IndexedAIP> selectedItems = SelectedItemsList.create(IndexedAIP.class, parent.getId(),
      child.getId());
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, "testDeleteParentChildPermission");
    parameters.put(RodaConstants.PLUGIN_PARAMS_DONT_CHECK_RELATIVES, "false");
    Job job = TestsHelper.executeJob(DeleteRODAObjectPlugin.class, parameters, PluginType.INTERNAL, selectedItems,
      username);

    // after job
    Assert.assertEquals(job.getState(), Job.JOB_STATE.COMPLETED);
    Assert.assertFalse(aipExists(parent.getId()), "parent AIP should be removed");
    Assert.assertFalse(aipExists(child.getId()), "son should be removed");
  }

  @Test
  public void testDeleteChild() throws GenericException, AuthorizationDeniedException, NotFoundException,
    IllegalOperationException, RequestNotValidException, AlreadyExistsException {
    String username = "user3";
    User testUser3 = createUser(username, "user3@test.pt", "roda");
    Assert.assertFalse(UserUtility.isAdministrator(testUser3), "user should not have admin permissions.");

    AIP parent = model.createAIP(null, "Parent AIP", new Permissions(), RodaConstants.ADMIN, null);
    AIP child = model.createAIP(parent.getId(), "Child AIP", new Permissions(), RodaConstants.ADMIN, null);

    List<Permissions.PermissionType> userPermissionsParent = List.of(Permissions.PermissionType.READ);
    List<Permissions.PermissionType> userPermissionsChild = Arrays.asList(Permissions.PermissionType.READ,
      Permissions.PermissionType.DELETE);
    setAIPPermissions(parent.getId(), username, userPermissionsParent);
    setAIPPermissions(child.getId(), username, userPermissionsChild);
    index.commit(IndexedAIP.class);

    Assert.assertFalse(hasPermissionToDelete(parent, username), "does not have permission to delete parent AIP.");
    Assert.assertTrue(hasPermissionToDelete(child, username), "has permission to delete child AIP.");

    SelectedItemsList<IndexedAIP> selectedItems = SelectedItemsList.create(IndexedAIP.class, child.getId());
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, "testDeleteChild");
    parameters.put(RodaConstants.PLUGIN_PARAMS_DONT_CHECK_RELATIVES, "false");
    Job job = TestsHelper.executeJob(DeleteRODAObjectPlugin.class, parameters, PluginType.INTERNAL, selectedItems,
      username);

    // after job
    Assert.assertEquals(job.getState(), Job.JOB_STATE.COMPLETED);
    Assert.assertFalse(aipExists(child.getId()), "son should be removed");
    Assert.assertTrue(aipExists(parent.getId()), "parent AIP should NOT be removed");

  }

  @Test
  public void testDeleteParentButDontCheckRelatives() throws AuthorizationDeniedException, AlreadyExistsException,
    NotFoundException, IllegalOperationException, GenericException, RequestNotValidException {
    String username = "user4";
    User testUser4 = createUser(username, "user4@test.pt", "roda");
    Assert.assertFalse(UserUtility.isAdministrator(testUser4), "user should not have admin permissions.");

    AIP parent = model.createAIP(null, "Parent AIP", new Permissions(), RodaConstants.ADMIN, null);
    AIP child = model.createAIP(parent.getId(), "Child AIP", new Permissions(), RodaConstants.ADMIN, null);

    List<Permissions.PermissionType> userPermissionsParent = Arrays.asList(Permissions.PermissionType.READ,
      Permissions.PermissionType.DELETE);
    List<Permissions.PermissionType> userPermissionsChild = List.of(Permissions.PermissionType.READ);
    setAIPPermissions(parent.getId(), username, userPermissionsParent);
    setAIPPermissions(child.getId(), username, userPermissionsChild);
    index.commit(IndexedAIP.class);

    Assert.assertTrue(hasPermissionToDelete(parent, username), "has permission to delete parent AIP.");
    Assert.assertFalse(hasPermissionToDelete(child, username), "does not have permission to delete child AIP.");

    SelectedItemsList<IndexedAIP> selectedItems = SelectedItemsList.create(IndexedAIP.class, parent.getId());
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, "testDeleteParentButDontCheckRelatives");
    parameters.put(RodaConstants.PLUGIN_PARAMS_DONT_CHECK_RELATIVES, "true");
    Job job = TestsHelper.executeJob(DeleteRODAObjectPlugin.class, parameters, PluginType.INTERNAL, selectedItems,
      username);

    // after job
    Assert.assertEquals(job.getState(), Job.JOB_STATE.COMPLETED);
    Assert.assertFalse(aipExists(parent.getId()), "parent AIP should be removed");
    Assert.assertTrue(aipExists(child.getId()), "son should be orphan");

    AIP orphan = model.retrieveAIP(child.getId());
    Assert.assertEquals(orphan.getParentId(), parent.getId(), "child keeps AIP parent id");
  }

  private User createUser(String username, String email, String password)
    throws AuthorizationDeniedException, EmailAlreadyExistsException, NotFoundException, UserAlreadyExistsException,
    IllegalOperationException, GenericException {
    User testUser = new User(username, username, email, true);
    try (SecureString securePassword = new SecureString(password.toCharArray())) {
      model.createUser(testUser, securePassword, true);
      return testUser;
    }
  }

  private boolean aipExists(String aipId) {
    try {
      model.retrieveAIP(aipId);
      return true;
    } catch (NotFoundException | RequestNotValidException | GenericException | AuthorizationDeniedException e) {
      return false;
    }
  }

  private boolean hasPermissionToDelete(AIP aip, String username) {
    try {
      return model.checkObjectPermission(username, Permissions.PermissionType.DELETE.name(), AIP.class.getName(),
        aip.getId());
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException | NotFoundException e) {
      return false;
    }
  }

  private void setAIPPermissions(String aipId, String username, List<Permissions.PermissionType> permissionsTypeList)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    Permissions permissions = new Permissions();
    permissions.setUserPermissions(username, new HashSet<>(permissionsTypeList));
    model.updateAIPPermissions(aipId, permissions, RodaConstants.ADMIN);
  }
}
