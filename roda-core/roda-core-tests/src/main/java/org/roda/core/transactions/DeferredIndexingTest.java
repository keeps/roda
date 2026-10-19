/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.transactions;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.TransactionalTestSupport;
import org.roda.core.catalog.CatalogAip;
import org.roda.core.catalog.CatalogService;
import org.roda.core.catalog.Catalogs;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.disposal.rule.ConditionType;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.schedule.DisposalActionCode;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodCalculation;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodIntervalCode;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.EARKSIPPluginsTest;
import org.roda.core.plugins.base.TransactionProbePlugin;
import org.roda.core.plugins.base.ingest.v2.ConfigurableIngestPlugin;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.transaction.RODATransactionManager;
import org.roda.core.util.IdUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * With RODA transactions on, nothing a transaction writes reaches the index
 * before it commits, the catalog shows the transaction its own writes, and a
 * rollback leaves nothing behind.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class DeferredIndexingTest {
  private Path basePath;
  private Path corporaPath;
  private ModelService model;
  private IndexService index;
  private CatalogService catalog;
  private LdapUtilityTestHelper ldapUtilityTestHelper;

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();
    RODATransactionManager transactionManager = TransactionalTestSupport.transactionManager();
    RodaCoreFactory.instantiateTest(true, true, true, true, true, false, false, ldapUtilityTestHelper.getLdapUtility(),
      transactionManager);
    TransactionalTestSupport.activate(transactionManager);
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();
    catalog = Catalogs.service().orElseThrow();

    URL corporaURL = DeferredIndexingTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
  }

  @AfterClass
  public void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  private void runProbe(String aipId, String sipId, boolean fail) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put(TransactionProbePlugin.PARAM_AIP_ID, aipId);
    parameters.put(TransactionProbePlugin.PARAM_SIP_ID, sipId);
    parameters.put(TransactionProbePlugin.PARAM_FAIL, Boolean.toString(fail));
    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName(TransactionProbePlugin.class.getName());
    job.setPlugin(TransactionProbePlugin.class.getName());
    job.setPluginParameters(parameters);
    job.setPluginType(PluginType.MISC);
    job.setSourceObjects(SelectedItemsNone.create());
    job.setUsername(RodaConstants.ADMIN);
    try {
      RodaCoreFactory.getPluginOrchestrator().createAndExecuteJobs(job, false);
    } catch (Exception e) {
      Assert.fail("Unable to execute the probe job", e);
    }
  }

  @Test
  public void testChangesArePublishedOnlyAfterCommit() throws Exception {
    String aipId = IdUtils.createUUID();
    String sipId = "probe-sip-" + aipId;

    runProbe(aipId, sipId, false);

    TransactionProbePlugin.Observation observation = TransactionProbePlugin.OBSERVATIONS.get(aipId);
    Assert.assertNotNull(observation, "the probe did not run");
    Assert.assertTrue(observation.transactional(), "the probe should run inside a transaction");
    Assert.assertFalse(observation.visibleInIndex(), "the index must not see uncommitted data");
    Assert.assertTrue(observation.visibleInCatalog(), "the transaction must see its own writes in the catalog");

    IndexedAIP indexedAIP = index.retrieve(IndexedAIP.class, aipId, Collections.emptyList());
    Assert.assertTrue(indexedAIP.getIngestSIPIds().contains(sipId));

    CatalogAip catalogAip = catalog.get(aipId).orElseThrow();
    Assert.assertEquals(catalogAip, CatalogAip.fromAIP(model.retrieveAIP(aipId)));
    Assert.assertEquals(catalog.findByIngestSIPId(sipId).size(), 1);
  }

  @Test
  public void testRollbackLeavesNothingBehind() throws Exception {
    String aipId = IdUtils.createUUID();
    String sipId = "probe-sip-" + aipId;

    runProbe(aipId, sipId, true);

    TransactionProbePlugin.Observation observation = TransactionProbePlugin.OBSERVATIONS.get(aipId);
    Assert.assertNotNull(observation, "the probe did not run");
    Assert.assertFalse(observation.visibleInIndex());

    Assert.expectThrows(NotFoundException.class, () -> model.retrieveAIP(aipId));
    Assert.expectThrows(NotFoundException.class,
      () -> index.retrieve(IndexedAIP.class, aipId, Collections.emptyList()));
    Assert.assertTrue(catalog.get(aipId).isEmpty());
    Assert.assertTrue(catalog.findByIngestSIPId(sipId).isEmpty());
  }

  @Test
  public void testIngestAppliesDisposalRuleOnMetadataFieldOfNewAIP() throws Exception {
    DisposalSchedule schedule = new DisposalSchedule();
    schedule.setTitle("Deferred indexing schedule");
    schedule.setDescription("Schedule applied through a metadata field rule during ingest");
    schedule.setActionCode(DisposalActionCode.DESTROY);
    // computed from the descriptive metadata by the EAD crosswalk (unitdate 2023-04-05)
    schedule.setRetentionTriggerElementId(RodaConstants.AIP_DATE_INITIAL);
    schedule.setRetentionPeriodIntervalCode(RetentionPeriodIntervalCode.YEARS);
    schedule.setRetentionPeriodDuration(10);
    schedule = model.createDisposalSchedule(schedule, RodaConstants.ADMIN);

    // "level" is produced by the EAD crosswalk from the SIP's descriptive
    // metadata, so the rule needs data written in the ingest transaction
    DisposalRule rule = new DisposalRule();
    rule.setTitle("Items");
    rule.setOrder(1);
    rule.setType(ConditionType.METADATA_FIELD);
    rule.setConditionKey("level");
    rule.setConditionValue("item");
    rule.setDisposalScheduleId(schedule.getId());
    rule.setDisposalScheduleName(schedule.getTitle());
    rule = model.createDisposalRule(rule, RodaConstants.ADMIN);

    try {
      AIP root = model.createAIP(null, RodaConstants.AIP_TYPE_MIXED, new Permissions(), RodaConstants.ADMIN, null);
      TransferredResource transferredResource = EARKSIPPluginsTest.createIngestCorpora(corporaPath, index);

      Map<String, String> parameters = new HashMap<>();
      parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
      parameters.put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, "true");
      parameters.put(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK, "false");
      parameters.put(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION, "false");
      Job job = TestsHelper.executeJob(ConfigurableIngestPlugin.class, parameters, PluginType.SIP_TO_AIP,
        SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));
      TestsHelper.getJobReports(index, job, true);

      List<CatalogAip> children = catalog.findChildren(root.getId());
      Assert.assertEquals(children.size(), 1);
      AIP aip = model.retrieveAIP(children.get(0).id());
      Assert.assertEquals(aip.getState(), AIPState.ACTIVE);
      Assert.assertNotNull(aip.getDisposal(), "the disposal rule was not applied");
      Assert.assertEquals(aip.getDisposal().getSchedule().getId(), schedule.getId());

      IndexedAIP indexedAIP = index.retrieve(IndexedAIP.class, aip.getId(), Collections.emptyList());
      Assert.assertEquals(indexedAIP.getState(), AIPState.ACTIVE);
      Assert.assertEquals(indexedAIP.getDisposalScheduleId(), schedule.getId());
      Assert.assertEquals(indexedAIP.getRetentionPeriodState(), RetentionPeriodCalculation.SUCCESS,
        indexedAIP.getRetentionPeriodDetails());
      Assert.assertNotNull(indexedAIP.getOverdueDate(), "the retention period was not calculated");

      Assert.assertEquals(catalog.verify(model), List.of(), "the catalog must match the main storage");
    } finally {
      model.deleteDisposalRule(rule.getId(), RodaConstants.ADMIN);
      TestsHelper.releaseAllLocks();
    }
  }
}
