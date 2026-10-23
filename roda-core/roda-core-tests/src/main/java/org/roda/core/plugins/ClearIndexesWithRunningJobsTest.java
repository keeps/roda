/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.ingest.v2.MinimalIngestPlugin;
import org.roda.core.plugins.base.maintenance.reindex.ReindexAIPPlugin;
import org.roda.core.plugins.base.maintenance.reindex.ReindexAllRodaEntitiesPlugin;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A reindex with "clear indexes" of a whole entity clears its indexes with a
 * delete by query, which blocks every update of each core until it is applied
 * (#3783), so it refuses to start while a job other than a reindex is running,
 * without deleting anything. Reindex jobs do not block each other: "Rebuild all
 * indexes" starts one reindex job per entity while it runs.
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class ClearIndexesWithRunningJobsTest {
  private static final Map<String, String> CLEAR_INDEXES = Map.of(RodaConstants.PLUGIN_PARAMS_CLEAR_INDEXES, "true");

  private Path basePath;
  private ModelService model;
  private IndexService index;
  private LdapUtilityTestHelper ldapUtilityTestHelper;
  private AIP aip;
  // the files of the AIP, counted once after it is created, which every reindex must keep
  private long files;

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

    URL corporaURL = getClass().getResource("/corpora");
    StorageService corporaService = new FileStorageService(Paths.get(corporaURL.toURI()));
    aip = model.createAIP(IdUtils.createUUID(), corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);
    index.commitAIPs();
    files = countFiles();
    Assert.assertTrue(files > 0, "the AIP has indexed files");
  }

  @AfterClass
  public void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void clearingIndexesFailsWithoutDeletingWhileAnotherJobRuns() throws RODAException {
    Job ingest = createRunningJob(MinimalIngestPlugin.class, PluginType.INGEST);
    try {
      Job reindex = TestsHelper.executeJob(ReindexAIPPlugin.class, CLEAR_INDEXES, PluginType.MISC,
        SelectedItemsAll.create(AIP.class), JOB_STATE.FAILED_TO_COMPLETE);

      Assert.assertTrue(reindex.getStateDetails().contains("Cannot clear the index while other jobs are running"),
        reindex.getStateDetails());
      Assert.assertTrue(reindex.getStateDetails().contains(ingest.getId()),
        "the message names the running job: " + reindex.getStateDetails());
    } finally {
      deleteJob(ingest);
    }

    index.commitAIPs();
    Assert.assertEquals(index.count(IndexedAIP.class, Filter.ALL), 1L, "the AIP is still indexed");
    Assert.assertEquals(countFiles(), files, "its files are still indexed");
  }

  @Test
  public void clearingIndexesRunsWhileOnlyReindexJobsRun() throws RODAException {
    // the parent "Rebuild all indexes" job, which runs while it starts the reindex job of each entity
    Job rebuildAll = createRunningJob(ReindexAllRodaEntitiesPlugin.class, PluginType.MISC);
    try {
      TestsHelper.executeJob(ReindexAIPPlugin.class, CLEAR_INDEXES, PluginType.MISC,
        SelectedItemsAll.create(AIP.class));
    } finally {
      deleteJob(rebuildAll);
    }

    index.commitAIPs();
    Assert.assertEquals(index.count(IndexedAIP.class, Filter.ALL), 1L, "the AIP was reindexed");
    Assert.assertEquals(countFiles(), files, "its files were reindexed");
  }

  @Test
  public void clearingIndexesRunsWithoutOtherJobs() throws RODAException {

    TestsHelper.executeJob(ReindexAIPPlugin.class, CLEAR_INDEXES, PluginType.MISC, SelectedItemsAll.create(AIP.class));

    index.commitAIPs();
    Assert.assertEquals(index.count(IndexedAIP.class, Filter.ALL), 1L, "the AIP was reindexed");
    Assert.assertEquals(countFiles(), files, "its files were reindexed");
  }

  /** A job that the index shows as running, without running it. */
  private Job createRunningJob(Class<?> plugin, PluginType pluginType) throws RODAException {
    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName("Running " + plugin.getSimpleName());
    job.setPlugin(plugin.getName());
    job.setPluginType(pluginType);
    job.setSourceObjects(SelectedItemsNone.create());
    job.setUsername(RodaConstants.ADMIN);
    job.setState(JOB_STATE.STARTED);
    model.createJob(job);
    index.commit(IndexedJob.class);
    return job;
  }

  private void deleteJob(Job job) throws RODAException {
    model.deleteJob(job.getId());
    index.commit(IndexedJob.class);
  }

  private long countFiles() throws RODAException {
    return index.count(IndexedFile.class,
      new Filter(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, aip.getId())));
  }
}
