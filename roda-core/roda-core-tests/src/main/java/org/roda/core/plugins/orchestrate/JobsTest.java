/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.plugins.DummyPlugin;
import org.roda.core.plugins.plugins.PluginThatFailsDuringInit;
import org.roda.core.plugins.plugins.PluginThatFailsDuringXMethod;
import org.roda.core.plugins.plugins.PluginThatStopsItself;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_TRAVIS})
public class JobsTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobsTest.class);

  private static Path basePath;

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources);

    LOGGER.info("Running Jobs tests under storage {}", basePath);
  }

  @AfterClass
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testJobExecutingDummyPlugin()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    TestsHelper.executeJob(DummyPlugin.class, PluginType.MISC, SelectedItemsNone.create(), JOB_STATE.COMPLETED);
  }

  @Test
  public void testJobExecutingPluginThatFailsDuringInit()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    try {
      RodaCoreFactory.getPluginManager().registerPlugin(new PluginThatFailsDuringInit());
      Assert.fail("Plugin should not load & therefore an exception was expected!");
    } catch (PluginException e) {
      // do nothing as it is expected
    }
    Job job = TestsHelper.executeJob(PluginThatFailsDuringInit.class, PluginType.MISC, SelectedItemsNone.create(),
      JOB_STATE.FAILED_TO_COMPLETE);
    Assert.assertEquals(job.getStateDetails(), "Plugin is NULL");
  }

  @Test
  public void testJobExecutingPluginThatFailsDuringBeforeAllExecute()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put(PluginThatFailsDuringXMethod.BEFORE_ALL_EXECUTE, "");
    TestsHelper.executeJob(PluginThatFailsDuringXMethod.class, parameters, PluginType.MISC, SelectedItemsNone.create(),
      JOB_STATE.FAILED_TO_COMPLETE);
  }

  @Test
  public void testJobExecutingPluginThatFailsDuringExecute()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put(PluginThatFailsDuringXMethod.ON_EXECUTE, "");
    TestsHelper.executeJob(PluginThatFailsDuringXMethod.class, parameters, PluginType.MISC, SelectedItemsNone.create(),
      JOB_STATE.COMPLETED);
  }

  @Test
  public void testJobExecutingPluginThatFailsDuringAfterAllExecute()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put(PluginThatFailsDuringXMethod.AFTER_ALL_EXECUTE, "");
    TestsHelper.executeJob(PluginThatFailsDuringXMethod.class, parameters, PluginType.MISC, SelectedItemsNone.create(),
      JOB_STATE.FAILED_TO_COMPLETE);
  }

  @Test
  public void testJobExecutingPluginThatStopsItselfUsingOrchestratorStop()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    ModelService modelService = RodaCoreFactory.getModelService();
    List<String> aips = new ArrayList<>();
    try {
      for (int i = 0; i < 30; i++) {
        aips.add(modelService
          .createAIP(null, RodaConstants.REPRESENTATION_TYPE_MIXED, new Permissions(), RodaConstants.ADMIN).getId());
      }
    } catch (AlreadyExistsException e) {
      // do nothing
    }

    int originalNumberOfJobWorkers = JobsHelper.getNumberOfJobsWorkers();
    int originalBlockSize = JobsHelper.getBlockSize();

    // setting new/test value for the number of job workers & block size
    JobsHelper.setNumberOfJobsWorkers(1);
    JobsHelper.setBlockSize(1);

    TestsHelper.executeJob(PluginThatStopsItself.class, PluginType.MISC, SelectedItemsList.create(AIP.class, aips),
      JOB_STATE.STOPPED);

    // resetting number of job workers & block size
    JobsHelper.setNumberOfJobsWorkers(originalNumberOfJobWorkers);
    JobsHelper.setBlockSize(originalBlockSize);
  }

  /**
   * 20160914 hsilva: this method tests orchestration to ensure that, even if
   * there are no objects to pass to the plugin, the job comes to an end (i.e.
   * complete state)
   */
  @Test
  public void testJobRunningInContainerWithNoObjectsInIt()
    throws RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {

    int originalSyncTimeout = JobsHelper.getSyncTimeout();
    // set sync timeout as the method execute() will do nothing as well as
    // receive nothing. and if some problem exists with job state transitions 5
    // seconds will be enough as a timeout will be thrown
    JobsHelper.setSyncTimeout(5);
    TestsHelper.executeJob(DummyPlugin.class, PluginType.MISC, SelectedItemsNone.create(), JOB_STATE.COMPLETED);

    JobsHelper.setSyncTimeout(originalSyncTimeout);
  }

}
