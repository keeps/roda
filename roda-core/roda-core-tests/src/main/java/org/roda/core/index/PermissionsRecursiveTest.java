/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import static org.testng.AssertJUnit.assertEquals;

import java.nio.file.Path;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.plugins.base.internal.UpdateAIPPermissionsPlugin;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {"all", "travis"})
public class PermissionsRecursiveTest {

  private static Path basePath;
  private static ModelService model;
  private static IndexService index;

  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsRecursiveTest.class);

  @BeforeClass
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(PermissionsRecursiveTest.class, true);

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

    LOGGER.debug("Running index tests under storage {}", basePath);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testUpdatePermissionsRecursively() throws RODAException, ParseException {
    // INFO this test may fail if the default objects change
    String parentAIPId = "8faeaf8c-9a1f-49cc-a239-f3410d8bc13b";
    String firstAIPChild = "ee1b2aa0-d336-418b-ad1d-1aa659fb97a8";
    String otherAIPChild = "d743a569-945a-49d9-844f-39af67992f05";

    User user = new User();
    user.setName("rodauser");
    user.setFullName("Roda User");
    user.setEmail("rodauser@example.com");
    model.registerUser(user, "rodapassword", false);

    // change parent permissions
    AIP parent = model.retrieveAIP(parentAIPId);
    Set<PermissionType> userPermissions = new HashSet<>();
    userPermissions.add(PermissionType.READ);
    parent.getPermissions().setUserPermissions(user.getName(), userPermissions);
    model.updateAIPPermissions(parent, RodaConstants.ADMIN);

    // change a child permissions as well
    AIP child = model.retrieveAIP(firstAIPChild);
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

    Set<PermissionType> permissions = model.retrieveAIP(otherAIPChild).getPermissions()
      .getUserPermissions(user.getName());
    assertEquals(permissions.contains(PermissionType.READ), true);
  }
}
