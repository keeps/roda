package org.roda.core.transactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.config.TestConfig;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.FailAcceptPlugin;
import org.roda.core.plugins.base.LongAcceptPlugin;
import org.roda.core.plugins.orchestrate.JobsHelper;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.transaction.RODATransactionException;
import org.roda.core.transaction.RODATransactionManager;
import org.roda.core.transaction.TransactionLogService;
import org.roda.core.transaction.TransactionalContext;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Alexandre Flores <aflores@keep.pt>
 */
@SpringBootTest(classes = TestConfig.class)
public class TransactionLockTimeoutDuringOptimisticOperationTest extends AbstractTestNGSpringContextTests {
  private static final Logger LOGGER = LoggerFactory
    .getLogger(TransactionLockTimeoutDuringOptimisticOperationTest.class);

  private static ModelService mainModelService;
  private static StorageService mainStorageService;
  private static IndexService index;

  @Autowired
  private RODATransactionManager transactionManager;

  @Autowired
  private TransactionLogService transactionLogService;

  @Autowired
  private Environment environment;

  @BeforeClass
  public void setup() throws GenericException {
    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false, new LdapUtilityTestHelper().getLdapUtility(),
      transactionManager);
    mainModelService = RodaCoreFactory.getModelService();
    mainStorageService = RodaCoreFactory.getStorageService();
    index = RodaCoreFactory.getIndexService();

    transactionManager.setMainModelService(mainModelService);
    transactionManager.setInitialized(true);
  }

  @AfterMethod
  public void cleanUp() throws RODAException {
    Consumer<RODAException> cleanupFailHandler = (e -> Assert.fail("Error cleaning up", e));

    // delete all AIPs
    index.execute(IndexedAIP.class, Filter.ALL, new ArrayList<>(), item -> {
      try {
        mainModelService.deleteAIP(item.getId());
      } catch (NotFoundException e) {
        // do nothing
      }
    }, cleanupFailHandler);

    // delete preservation agents
    index.execute(IndexedPreservationAgent.class, Filter.ALL, new ArrayList<>(), item -> {
      PreservationMetadata pm = mainModelService.retrievePreservationMetadata(item.getId(),
        PreservationMetadata.PreservationMetadataType.AGENT);
      try {
        mainModelService.deletePreservationMetadata(pm, true);
      } catch (NotFoundException e) {
        // do nuthin'
      }
    }, cleanupFailHandler);
  }

  private void createAIPsForAssessing(int total) throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, RODATransactionException {
    // Start transaction
    TransactionalContext context = transactionManager.beginTransaction();
    ModelService model = context.transactionalModelService();

    // Create AIPS
    for (int i = 0; i < total; i++) {
      model.createAIP(AIPState.UNDER_APPRAISAL, null, RodaConstants.AIP_TYPE_MIXED, new Permissions(),
        RodaConstants.ADMIN, null);
    }

    // End transaction
    transactionManager.endTransaction(context.transactionLog().getId());

    // Commit to index
    index.commitAIPs();
  }

  private void acceptAIPsWithLongBatchExecutionTime(int workers, int batchSize, int lockTimeout, String agentName)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    JobsHelper.setNumberOfJobsWorkers(workers);
    JobsHelper.setBlockSize(batchSize);
    JobsHelper.setLockRequestTimeout(lockTimeout);

    TestsHelper.executeJob(LongAcceptPlugin.class, Map.of(), PluginType.INTERNAL, SelectedItemsAll.create(AIP.class),
      agentName);
  }

  private void failAcceptingAIPs(int workers, int batchSize, int lockTimeout, String agentName)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {
    JobsHelper.setNumberOfJobsWorkers(workers);
    JobsHelper.setBlockSize(batchSize);
    JobsHelper.setLockRequestTimeout(lockTimeout);

    TestsHelper.executeJob(FailAcceptPlugin.class, Map.of(), PluginType.INTERNAL, SelectedItemsAll.create(AIP.class),
      Job.JOB_STATE.FAILED_TO_COMPLETE, agentName);
  }

  @Test
  private void testAssertLockTimeoutSequential() throws AuthorizationDeniedException, RequestNotValidException,
    AlreadyExistsException, RODATransactionException, NotFoundException, GenericException {
    // Create AIPs
    int totalAIPs = 100;
    createAIPsForAssessing(totalAIPs);
    IndexResult<IndexedAIP> findAppraisalAIPs = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_STATE, AIPState.UNDER_APPRAISAL.name())), new Sorter(),
      new Sublist(0, 0), List.of());
    Assert.assertEquals(findAppraisalAIPs.getTotalCount(), totalAIPs,
      "Total created AIPs is " + findAppraisalAIPs.getTotalCount() + " instead of expected " + totalAIPs);
    // Accept them
    String agentName = "dummy";
    acceptAIPsWithLongBatchExecutionTime(1, 100, 5, agentName);
    IndexResult<IndexedAIP> findAcceptedAIPs = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_STATE, AIPState.ACTIVE.name())), new Sorter(),
      new Sublist(0, 0), List.of());
    Assert.assertEquals(findAcceptedAIPs.getTotalCount(), totalAIPs,
      "Total accepted AIPs is " + findAcceptedAIPs.getTotalCount() + " instead of expected " + totalAIPs);
    IndexResult<IndexedPreservationAgent> findAgents = index.find(IndexedPreservationAgent.class,
      new Filter(new SimpleFilterParameter(RodaConstants.PRESERVATION_AGENT_ID, IdUtils.getUserAgentId("dummy", null))),
      new Sorter(), new Sublist(0, 0), List.of());
    Assert.assertEquals(findAgents.getTotalCount(), 1, "Job executing agent was not indexed.");
  }

  @Test
  private void testAssertParallelLockTimeout() throws AuthorizationDeniedException, RequestNotValidException,
    AlreadyExistsException, RODATransactionException, NotFoundException, GenericException {
    // Create AIPs
    int totalAIPs = 100;
    createAIPsForAssessing(totalAIPs);
    IndexResult<IndexedAIP> findAppraisalAIPs = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_STATE, AIPState.UNDER_APPRAISAL.name())), new Sorter(),
      new Sublist(0, 0), List.of());
    Assert.assertEquals(findAppraisalAIPs.getTotalCount(), totalAIPs,
      "Total created AIPs is " + findAppraisalAIPs.getTotalCount() + " instead of expected " + totalAIPs);
    // Accept them
    String agentName = "dummy";
    acceptAIPsWithLongBatchExecutionTime(10, 10, 5, agentName);
    IndexResult<IndexedAIP> findAcceptedAIPs = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_STATE, AIPState.ACTIVE.name())), new Sorter(),
      new Sublist(0, 0), List.of());
    Assert.assertEquals(findAcceptedAIPs.getTotalCount(), totalAIPs,
      "Total accepted AIPs is " + findAcceptedAIPs.getTotalCount() + " instead of expected " + totalAIPs);
    IndexResult<IndexedPreservationAgent> findAgents = index.find(IndexedPreservationAgent.class,
      new Filter(new SimpleFilterParameter(RodaConstants.PRESERVATION_AGENT_ID, IdUtils.getUserAgentId("dummy", null))),
      new Sorter(), new Sublist(0, 0), List.of());
    Assert.assertEquals(findAgents.getTotalCount(), 1, "Job executing agent was not indexed.");
  }

  @Test
  private void testOptimisticOperationRollback() throws AuthorizationDeniedException, RequestNotValidException,
    AlreadyExistsException, RODATransactionException, NotFoundException, GenericException, ValidationException {
    // Create AIPs
    int totalAIPs = 100;
    createAIPsForAssessing(totalAIPs);
    IndexResult<IndexedAIP> findAppraisalAIPs = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_STATE, AIPState.UNDER_APPRAISAL.name())), new Sorter(),
      new Sublist(0, 0), List.of());
    Assert.assertEquals(findAppraisalAIPs.getTotalCount(), totalAIPs,
      "Total created AIPs is " + findAppraisalAIPs.getTotalCount() + " instead of expected " + totalAIPs);
    // Create the test agent
    String preExistingAgentName = "dummyExisting";
    PremisV3Utils.createIfNotExistsPremisUserAgentBinary(preExistingAgentName, mainModelService, index, true,
      List.of());
    index.commit(IndexedPreservationAgent.class);
    IndexResult<IndexedPreservationAgent> initialFindPreExistingAgent = index.find(IndexedPreservationAgent.class,
      new Filter(new SimpleFilterParameter(RodaConstants.PRESERVATION_AGENT_ID,
        IdUtils.getUserAgentId(preExistingAgentName, null))),
      new Sorter(), new Sublist(0, 0), List.of());
    Assert.assertEquals(initialFindPreExistingAgent.getTotalCount(), 1,
      "Pre-existing job executing agent was not indexed.");
    // Fail accepting AIPs using a pre-existing agent
    failAcceptingAIPs(10, 10, 5, preExistingAgentName);
    IndexResult<IndexedPreservationAgent> finalFindPreExistingAgent = index.find(IndexedPreservationAgent.class,
      new Filter(new SimpleFilterParameter(RodaConstants.PRESERVATION_AGENT_ID,
        IdUtils.getUserAgentId(preExistingAgentName, null))),
      new Sorter(), new Sublist(0, 0), List.of());
    Assert.assertEquals(finalFindPreExistingAgent.getTotalCount(), 1,
      "Pre-existing job executing agent was deleted after failed plugin.");
    // Fail accepting AIPs using a new agent
    String newAgentName = "dummy2";
    failAcceptingAIPs(10, 10, 5, newAgentName);
    IndexResult<IndexedPreservationAgent> findNewAgent = index.find(IndexedPreservationAgent.class,
      new Filter(
        new SimpleFilterParameter(RodaConstants.PRESERVATION_AGENT_ID, IdUtils.getUserAgentId(newAgentName, null))),
      new Sorter(), new Sublist(0, 0), List.of());
    Assert.assertEquals(findNewAgent.getTotalCount(), 0,
      "New job executing agent was not deleted after failed plugin.");
  }
}
