/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.maintenance.reindex.ReindexAIPPlugin;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.fs.FSUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * #3773: a job still reports the objects that could not be indexed when their
 * documents are sent to Solr in batches.
 *
 * Solr rejects every write to the File collection, made read-only, while a
 * reindex job runs on an AIP of more files than are sent to Solr at once.
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class IndexingFailureReportTest {
  // more files than are sent to Solr at once
  private static final int FILES = 250;

  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;

  @BeforeClass
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(IndexingFailureReportTest.class, true);
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
  }

  @AfterClass(alwaysRun = true)
  public static void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @AfterMethod(alwaysRun = true)
  public void cleanUp() throws Exception {
    setFilesReadOnly(false);
    IndexTestUtils.resetIndex();
  }

  private static void setFilesReadOnly(boolean readOnly) throws Exception {
    CollectionAdminRequest
      .modifyCollection(SolrCollectionRegistry.getIndexName(IndexedFile.class), Map.of("readOnly", readOnly))
      .process(index.getSolrClient());
  }

  @Test
  public void aReindexJobReportsTheAIPWhoseFilesSolrRejects() throws Exception {
    AIP aip = IndexTestUtils.createAIP(model, FILES, 0);
    setFilesReadOnly(true);

    Job job = TestsHelper.executeJob(ReindexAIPPlugin.class, Collections.emptyMap(), PluginType.MISC,
      SelectedItemsList.create(AIP.class, aip.getId()));

    Assert.assertEquals(job.getJobStats().getSourceObjectsProcessedWithFailure(), 1, "the AIP failed");
    Assert.assertEquals(job.getJobStats().getSourceObjectsProcessedWithSuccess(), 0);
    List<Report> reports = TestsHelper.getJobReports(index, job, false);
    Assert.assertTrue(
      reports.stream().flatMap(report -> report.getReports().stream())
        .anyMatch(item -> aip.getId().equals(item.getSourceObjectId())
          && PluginState.FAILURE.equals(item.getPluginState())),
      "the report of the AIP is a failure: " + reports);
  }
}
