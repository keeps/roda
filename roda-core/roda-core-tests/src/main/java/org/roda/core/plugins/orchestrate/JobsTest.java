/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.LockingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.filter.AllFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.PluginException;
import org.roda.core.plugins.PluginHelper;
import org.roda.core.plugins.base.DummyPlugin;
import org.roda.core.plugins.base.PluginThatFailsDuringExecuteMethod;
import org.roda.core.plugins.base.PluginThatFailsDuringInit;
import org.roda.core.plugins.base.PluginThatFailsDuringXMethod;
import org.roda.core.plugins.base.PluginThatStopsItself;
import org.roda.core.plugins.base.PluginThatTestsLocking;
import org.roda.core.plugins.base.antivirus.AntivirusPlugin;
import org.roda.core.plugins.base.maintenance.reindex.ReindexAIPPlugin;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class JobsTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobsTest.class);

  private static ModelService model;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;
  private static Path basePath;

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false, ldapUtilityTestHelper.getLdapUtility());

    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    LOGGER.info("Running Jobs tests under storage {}", basePath);
  }

  @AfterClass
  public void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @AfterMethod
  public void cleanUp() throws RODAException, IOException {
    try (
      IterableIndexResult<IndexedAIP> result = index.findAll(IndexedAIP.class, Filter.ALL, Collections.emptyList())) {
      for (IndexedAIP aip : result) {
        try {
          model.deleteAIP(aip.getId());
        } catch (NotFoundException e) {
          // do nothing
        }
      }

      // last attempt to delete everything (for model/index inconsistencies)
      index.clearAIPs();
    } catch (IOException e) {
      LOGGER.error("Error getting AIPs when cleaning up", e);
    } finally {
      index.commitAIPs();
    }

    try (CloseableIterable<OptionalWithCause<AIP>> iterable = model.listAIPs()) {
      iterable.forEach(o -> {
        try {
          model.deleteAIP(o.get().getId());
        } catch (RequestNotValidException | NotFoundException | GenericException | AuthorizationDeniedException e) {
          throw new RuntimeException(e);
        }
      });
    }
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
      JOB_STATE.FAILED_TO_COMPLETE);
  }

  @Test
  public void testJobExecutingPluginThatFailsDuringExecuteInParallel() throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    JobsHelper.setBlockSize(1);

    ModelService modelService = RodaCoreFactory.getModelService();
    String aip1 = modelService.createAIP(null, "misc", new Permissions(), RodaConstants.ADMIN).getId();
    String aip2 = modelService.createAIP(null, "misc", new Permissions(), RodaConstants.ADMIN).getId();

    Map<String, String> parameters = new HashMap<>();
    Job job;
    JobStats jobStats;

    // 1) don't fail at all
    parameters.put(aip1, "false");
    parameters.put(aip2, "false");
    job = TestsHelper.executeJob(PluginThatFailsDuringExecuteMethod.class, parameters, PluginType.MISC,
      SelectedItemsList.create(AIP.class, aip1, aip2), JOB_STATE.COMPLETED);
    Assert.assertNotNull(job.getStateDetails());
    Assert.assertEquals(job.getStateDetails(), "");
    jobStats = job.getJobStats();
    assertJobStats(jobStats, 100, 0, 2, 0, 2, 0);

    // 2) fail in one (order doesn't matter as orchestration is asynchronous)
    parameters.put(aip1, "false");
    parameters.put(aip2, "true");
    job = TestsHelper.executeJob(PluginThatFailsDuringExecuteMethod.class, parameters, PluginType.MISC,
      SelectedItemsList.create(AIP.class, aip1, aip2), JOB_STATE.FAILED_TO_COMPLETE);
    Assert.assertNotNull(job.getStateDetails());
    Assert.assertTrue(StringUtils.containsIgnoreCase(job.getStateDetails(), "exception"));
    jobStats = job.getJobStats();
    assertJobStats(jobStats, 100, 0, 2, 1, 1, 0);

    // 3) fail in both
    parameters.put(aip1, "true");
    parameters.put(aip2, "true");
    job = TestsHelper.executeJob(PluginThatFailsDuringExecuteMethod.class, parameters, PluginType.MISC,
      SelectedItemsList.create(AIP.class, aip1, aip2), JOB_STATE.FAILED_TO_COMPLETE);
    Assert.assertNotNull(job.getStateDetails());
    Assert.assertTrue(StringUtils.containsIgnoreCase(job.getStateDetails(), "exception"));
    jobStats = job.getJobStats();
    assertJobStats(jobStats, 100, 0, 2, 2, 0, 0);
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

  @Test
  public void testPluginProcessingTheSameObjectInSeveralThreadsWithAutoLocking() throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    // set block size to 1 in order to have several "threads" when using list of
    // objects of size 2 or greater
    JobsHelper.setBlockSize(1);

    ModelService modelService = RodaCoreFactory.getModelService();
    IndexService indexService = RodaCoreFactory.getIndexService();
    AIP aip = modelService.createAIP(null, RodaConstants.REPRESENTATION_TYPE_MIXED, new Permissions(),
      RodaConstants.ADMIN);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(PluginThatTestsLocking.PLUGIN_PARAM_AUTO_LOCKING, "true");
    // execute plugin via list in the same object (two ids, the same object)
    Job job = TestsHelper.executeJob(PluginThatTestsLocking.class, parameters, PluginType.MISC,
      SelectedItemsList.create(AIP.class, aip.getId(), aip.getId()), JOB_STATE.COMPLETED);

    // asserts
    List<Report> jobReports = TestsHelper.getJobReports(indexService, job);
    Assert.assertEquals(jobReports.size(), 1);
    String pluginDetails = jobReports.get(0).getPluginDetails();
    String[] pluginDetailsSplitted = pluginDetails.split(System.lineSeparator());
    Assert.assertEquals(pluginDetailsSplitted.length, 5);
    Assert.assertTrue(pluginDetails.contains(PluginThatTestsLocking.PLUGIN_DETAILS_AT_LEAST_ONE_LOCK_REQUEST_WAITING));
    int dates = 0;
    String year = Calendar.getInstance().get(Calendar.YEAR) + "";
    for (String details : pluginDetailsSplitted) {
      if (details.contains(year)) {
        dates++;
      }
    }
    // details are not overwritten
    Assert.assertEquals(dates, 4);
  }

  @Test
  public void testPluginProcessingTheSameObjectInSeveralThreadsWithoutAutoLocking() throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException, AlreadyExistsException {
    // set block size to 1 in order to have several "threads" when using list of
    // objects of size 2 or greater
    JobsHelper.setBlockSize(1);

    ModelService modelService = RodaCoreFactory.getModelService();
    IndexService indexService = RodaCoreFactory.getIndexService();
    AIP aip = modelService.createAIP(null, RodaConstants.REPRESENTATION_TYPE_MIXED, new Permissions(),
      RodaConstants.ADMIN);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(PluginThatTestsLocking.PLUGIN_PARAM_AUTO_LOCKING, "false");
    // execute plugin via list in the same object (two ids, the same object)
    Job job = TestsHelper.executeJob(PluginThatTestsLocking.class, parameters, PluginType.MISC,
      SelectedItemsList.create(AIP.class, aip.getId(), aip.getId()), JOB_STATE.COMPLETED);

    // asserts
    List<Report> jobReports = TestsHelper.getJobReports(indexService, job);
    Assert.assertEquals(jobReports.size(), 1);
    String pluginDetails = jobReports.get(0).getPluginDetails();
    String[] pluginDetailsSplitted = pluginDetails.split(System.lineSeparator());
    Assert.assertEquals(pluginDetailsSplitted.length, 2);
    Assert.assertFalse(pluginDetails.contains(PluginThatTestsLocking.PLUGIN_DETAILS_AT_LEAST_ONE_LOCK_REQUEST_WAITING));
    int dates = 0;
    String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    for (String details : pluginDetailsSplitted) {
      if (details.contains(year)) {
        dates++;
      }
    }
    // details are overwritten
    Assert.assertEquals(dates, 2);
  }

  @Test
  public void testRunOnAllPlugin() throws AuthorizationDeniedException, RequestNotValidException,
    AlreadyExistsException, NotFoundException, GenericException {
    for (int i = 0; i < 20; i++) {
      createSampleAIP(false);
    }

    index.commitAIPs();

    Job job = TestsHelper.executeJob(AntivirusPlugin.class, Collections.emptyMap(), PluginType.MISC,
      SelectedItemsAll.create(AIP.class));

    assertJobStats(job.getJobStats(), 100, 0, 20, 0, 20, 0);
  }

  @Test
  public void testRunPluginWithFilter() throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException {
    for (int i = 0; i < 20; i++) {
      createSampleAIP(false);
    }

    index.commitAIPs();

    SelectedItemsFilter<IndexedAIP> itemsFilter = new SelectedItemsFilter<>(
      new Filter(new OneOfManyFilterParameter(RodaConstants.INDEX_ID, Collections.emptyList())),
      IndexedAIP.class.getName(), true);

    Job job = TestsHelper.executeJob(ReindexAIPPlugin.class, Collections.emptyMap(), PluginType.MISC, itemsFilter);

    assertJobStats(job.getJobStats(), 100, 0, 0, 0, 0, 0);

    Assert.assertEquals(
      index.findAll(IndexedAIP.class, new Filter(new AllFilterParameter()), Collections.emptyList()).getTotalCount(),
      20L);
  }

  @Test
  public void testWhenLockTimeoutDeletesSolrIndex() throws AuthorizationDeniedException, RequestNotValidException,
    AlreadyExistsException, NotFoundException, GenericException, LockingException {

    JobsHelper.setLockRequestTimeout(10);
    JobsHelper.setBlockSize(1);

    AIP aip = createSampleAIP();
    AIP aip2 = createSampleAIP();

    index.commitAIPs();

    String requestUUID = IdUtils.createUUID();
    PluginHelper.acquireObjectLock(Collections.singletonList("org.roda.core.data.v2.ip.AIP|" + aip.getId()),
      requestUUID);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");

    Job job = TestsHelper.executeJob(ReindexAIPPlugin.class, parameters, PluginType.MISC,
      SelectedItemsList.create(AIP.class, Arrays.asList(aip.getId(), aip2.getId())));

    index.commitAIPs();

    Assert.assertEquals(index.findAll(IndexedRepresentation.class,
      new Filter(
        new OneOfManyFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, Arrays.asList(aip.getId(), aip2.getId()))),
      Collections.emptyList()).getTotalCount(), 2L);

    Assert.assertEquals(index.findAll(IndexedFile.class,
      new Filter(new OneOfManyFilterParameter(RodaConstants.FILE_AIP_ID, Arrays.asList(aip.getId(), aip2.getId()))),
      Collections.emptyList()).getTotalCount(), 8L);

    assertJobStats(job.getJobStats(), 100, 0, 2, 1, 1, 0);
  }

  private AIP createSampleAIP() throws AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException,
    NotFoundException, GenericException {
    return createSampleAIP(true, true, 4);
  }

  private AIP createSampleAIP(boolean createRepresentation) throws AuthorizationDeniedException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, GenericException {
    return createSampleAIP(createRepresentation, false, 0);
  }

  private AIP createSampleAIP(boolean createRepresentation, boolean createFiles, int numberOfFiles)
    throws AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, NotFoundException,
    GenericException {
    AIP aip = model.createAIP(null, "", new Permissions(), RodaConstants.ADMIN);
    if (createRepresentation) {
      Representation representation = model.createRepresentation(aip.getId(), IdUtils.createUUID(), true, "", true,
        RodaConstants.ADMIN);
      if (createFiles) {
        for (int i = 0; i < numberOfFiles; i++) {
          model.createFile(aip.getId(), representation.getId(), Collections.emptyList(),
            String.format("test%d.txt", i + 1), new StringContentPayload(String.format("test %d", i + 1)),
            RodaConstants.ADMIN);
        }
      }
    }
    return aip;
  }

  private void assertJobStats(JobStats jobStats, int expectedCompletionPercentage,
    int expectedSourceObjectsBeingProcessed, int expectedSourceObjectsCount,
    int expectedSourceObjectsProcessedWithFailure, int expectedSourceObjectsProcessedWithSuccess,
    int expectedSourceObjectsWaitingToBeProcessed) {

    Assert.assertEquals(jobStats.getSourceObjectsCount(), expectedSourceObjectsCount);
    Assert.assertEquals(jobStats.getSourceObjectsProcessedWithSuccess(), expectedSourceObjectsProcessedWithSuccess);
    Assert.assertEquals(jobStats.getSourceObjectsProcessedWithFailure(), expectedSourceObjectsProcessedWithFailure);
    Assert.assertEquals(jobStats.getSourceObjectsBeingProcessed(), expectedSourceObjectsBeingProcessed);
    Assert.assertEquals(jobStats.getSourceObjectsWaitingToBeProcessed(), expectedSourceObjectsWaitingToBeProcessed);
    Assert.assertEquals(jobStats.getCompletionPercentage(), expectedCompletionPercentage);
  }
}
