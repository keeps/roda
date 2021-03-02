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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.plugins.internal.UpdatePermissionsPlugin;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_TRAVIS})
public class PermissionsRecursiveTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsRecursiveTest.class);

  private static Path basePath;
  private static ModelService model;
  private static IndexService index;

  @BeforeClass
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(PermissionsRecursiveTest.class, true);

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false);

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
  public void testUpdatePermissionsRecursively() throws RODAException {
    AIP parent = model.createAIP(null, "", new Permissions(), RodaConstants.ADMIN);
    AIP firstChild = model.createAIP(parent.getId(), "", new Permissions(), RodaConstants.ADMIN);
    AIP otherChild = model.createAIP(parent.getId(), "", new Permissions(), RodaConstants.ADMIN);
    model.createAIP(otherChild.getId(), "", new Permissions(), RodaConstants.ADMIN);

    User user = new User();
    user.setName("rodauser");
    user.setFullName("Roda User");
    user.setEmail("rodauser@example.com");
    model.registerUser(user, "rodapassword", false);

    // change parent permissions
    Set<PermissionType> userPermissions = new HashSet<>();
    userPermissions.add(PermissionType.READ);
    parent.getPermissions().setUserPermissions(user.getName(), userPermissions);
    model.updateAIPPermissions(parent, RodaConstants.ADMIN);

    // change a child permissions as well
    firstChild.getPermissions().setUserPermissions(user.getName(), userPermissions);
    model.updateAIPPermissions(firstChild, RodaConstants.ADMIN);
    index.commitAIPs();

    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ANCESTORS, parent.getId()),
      new NotSimpleFilterParameter("permission_users_READ", user.getName()));
    SelectedItemsFilter<IndexedAIP> selectedItems = new SelectedItemsFilter<>(filter, IndexedAIP.class.getName(),
      Boolean.FALSE);

    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_PERMISSIONS_JSON,
      JsonUtils.getJsonFromObject(parent.getPermissions(), Permissions.class));
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DETAILS, "Update descendant AIPs");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_EVENT_DESCRIPTION,
      "The process of updating an object of the repository.");
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_OUTCOME_TEXT,
      "Parent permissions were updated and all sublevels will be too");

    Job job = TestsHelper.executeJob(UpdatePermissionsPlugin.class, pluginParameters, PluginType.INTERNAL,
      (SelectedItems) selectedItems);
    assertEquals(2, job.getJobStats().getSourceObjectsCount());

    Set<PermissionType> permissions = model.retrieveAIP(otherChild.getId()).getPermissions()
      .getUserPermissions(user.getName());
    assertEquals(permissions.contains(PermissionType.READ), true);
  }
}
