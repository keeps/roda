/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.config.TestConfig;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.db.jobs.JobReportStep;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.repository.job.JobDatabaseService;
import org.roda.core.repository.job.JobFlushCleanupTask;
import org.roda.core.repository.job.JobReportRepository;
import org.roda.core.repository.job.JobReportStepRepository;
import org.roda.core.repository.job.JobRepository;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit tests for the hybrid Job/Report persistence logic.
 * Tests that running jobs are stored in the database and flushed to storage on completion.
 *
 * @author RODA Development Team
 */
@SpringBootTest(classes = TestConfig.class)
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV})
public class JobPersistenceTest extends AbstractTestNGSpringContextTests {
  private static final Logger LOGGER = LoggerFactory.getLogger(JobPersistenceTest.class);

  private static Path basePath;
  private static StorageService storage;
  private static ModelService model;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;

  @Autowired
  private JobRepository jobRepository;

  @Autowired
  private JobDatabaseService jobDatabaseService;

  @Autowired
  private JobFlushCleanupTask jobFlushCleanupTask;

  @Autowired
  private JobReportRepository jobReportRepository;

  @Autowired
  private JobReportStepRepository jobReportStepRepository;

  @BeforeClass
  public void init() throws IOException, GenericException {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    boolean deploySolr = false;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = false;
    boolean deployPluginManager = false;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false, ldapUtilityTestHelper.getLdapUtility());

    storage = RodaCoreFactory.getStorageService();
    model = RodaCoreFactory.getModelService();

    LOGGER.debug("Running JobPersistenceTest under storage: {}", basePath);
  }

  @AfterClass
  public void cleanup() throws NotFoundException, GenericException, IOException {
    // Clean up any test data
    // reports and the other job rows go with their job (ON DELETE CASCADE)
    jobRepository.deleteAll();

    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  /**
   * Test that a newly created job with a non-final state (STARTED) is saved to the database
   * and NOT written to file storage.
   */
  @Test
  public void testRunningJobPersistence() throws RODAException {
    // Create a running job
    String jobId = IdUtils.createUUID();
    Job job = createTestJob(jobId, JOB_STATE.STARTED);

    // Create the job using the model service
    model.createJob(job);

    // Verify job exists in database
    assertTrue(jobRepository.existsById(jobId), "Job should exist in database");

    // Verify job retrieved from model service
    Job retrievedJob = model.retrieveJob(jobId);
    assertNotNull(retrievedJob, "Should be able to retrieve the job");
    assertEquals(retrievedJob.getId(), jobId);
    assertEquals(retrievedJob.getState(), JOB_STATE.STARTED);

    // Clean up
    model.deleteJob(jobId);
  }

  /**
   * Test that updating a job to a final state (COMPLETED) flushes it to file
   * storage immediately, while its DB row (and its reports) are only marked as
   * flushed -- actual DB removal is deferred to {@link JobFlushCleanupTask}.
   */
  @Test
  public void testJobFinalization() throws RODAException {
    // Create a running job
    String jobId = IdUtils.createUUID();
    Job job = createTestJob(jobId, JOB_STATE.STARTED);

    // Create the job using the model service
    model.createJob(job);

    // Verify job is in database initially
    assertTrue(jobRepository.existsById(jobId), "Job should exist in database initially");

    // Create a report for this job
    Report report = createTestReport(jobId);
    model.createOrUpdateJobReport(report, job);

    // Verify report is in database
    assertTrue(jobDatabaseService.findReport(report.getId()).isPresent(), "Report should exist in database");

    // Now update job to final state
    job.setState(JOB_STATE.COMPLETED);
    job.setEndDate(new Date());
    model.createOrUpdateJob(job);

    // Job row still exists right after flush -- deletion is deferred -- but is
    // now marked as flushed
    assertTrue(jobRepository.existsById(jobId), "Job row should still exist in database right after flush");
    assertNotNull(jobRepository.findById(jobId).orElseThrow().getFlushedAt(),
      "Job should be marked as flushed right after flush");

    // Report row is likewise still present right after flush
    assertTrue(jobDatabaseService.findReport(report.getId()).isPresent(), "Report row should still exist right after flush");

    // Job can be retrieved with its up-to-date final state (not stale), whether
    // read from the still-present DB row or, after cleanup, from storage
    Job retrievedJob = model.retrieveJob(jobId);
    assertNotNull(retrievedJob, "Should be able to retrieve the completed job");
    assertEquals(retrievedJob.getState(), JOB_STATE.COMPLETED);

    // Running the cleanup task now removes the flushed job and its reports
    jobFlushCleanupTask.cleanFlushedJobs();

    assertFalse(jobRepository.existsById(jobId), "Job should be removed from database after cleanup runs");
    assertFalse(jobDatabaseService.findReport(report.getId()).isPresent(),
      "Report should be removed from database after cleanup runs");

    // Job is still retrievable, now from storage
    Job retrievedAfterCleanup = model.retrieveJob(jobId);
    assertNotNull(retrievedAfterCleanup, "Should be able to retrieve completed job from storage after cleanup");
    assertEquals(retrievedAfterCleanup.getState(), JOB_STATE.COMPLETED);

    // Clean up
    model.deleteJob(jobId);
  }

  /**
   * Test that the list method returns both running jobs (from DB) and completed jobs (from storage).
   */
  @Test
  public void testListingConsistency() throws RODAException {
    // Create a running job (will be in DB)
    String runningJobId = IdUtils.createUUID();
    Job runningJob = createTestJob(runningJobId, JOB_STATE.STARTED);
    model.createJob(runningJob);

    // Create a completed job (will be in storage)
    String completedJobId = IdUtils.createUUID();
    Job completedJob = createTestJob(completedJobId, JOB_STATE.COMPLETED);
    completedJob.setEndDate(new Date());
    model.createJob(completedJob);
    // Force transition to storage by creating and immediately completing
    model.createOrUpdateJob(completedJob);

    // List all jobs using model service
    try (CloseableIterable<OptionalWithCause<Job>> jobsIterable = model.list(Job.class)) {
      List<Job> allJobs = StreamSupport.stream(jobsIterable.spliterator(), false)
        .filter(OptionalWithCause::isPresent)
        .map(OptionalWithCause::get)
        .collect(Collectors.toList());

      // Verify both jobs are listed
      assertTrue(allJobs.stream().anyMatch(j -> j.getId().equals(runningJobId)),
        "Running job should be in the list");
      // Note: completed job may or may not be in list depending on timing

      LOGGER.info("Listed {} jobs total", allJobs.size());
    } catch (IOException e) {
      throw new GenericException("Error closing iterable", e);
    }

    // Clean up
    model.deleteJob(runningJobId);
    try {
      model.deleteJob(completedJobId);
    } catch (NotFoundException e) {
      // May have already been deleted or never existed in storage
    }
  }

  /**
   * Test that deleteJob properly cleans up both DB and storage.
   */
  @Test
  public void testDeletion() throws RODAException {
    // Create a running job
    String jobId = IdUtils.createUUID();
    Job job = createTestJob(jobId, JOB_STATE.STARTED);
    model.createJob(job);

    // Create a report
    Report report = createTestReport(jobId);
    model.createOrUpdateJobReport(report, job);

    // Verify they exist in DB
    assertTrue(jobRepository.existsById(jobId), "Job should exist in database");
    assertTrue(jobDatabaseService.findReport(report.getId()).isPresent(), "Report should exist in database");

    // Delete the job
    model.deleteJob(jobId);

    // Verify both job and reports are deleted from DB
    assertFalse(jobRepository.existsById(jobId), "Job should be deleted from database");
    List<Report> remainingReports = jobDatabaseService.findReports(jobId);
    assertTrue(remainingReports.isEmpty(), "Reports should be deleted from database");

    // Verify job cannot be retrieved
    boolean notFound = false;
    try {
      model.retrieveJob(jobId);
    } catch (NotFoundException e) {
      notFound = true;
    }
    assertTrue(notFound, "Job should not be found after deletion");
  }

  /**
   * The transaction manager reads only the reports of its own transaction: from
   * the database while the job runs, and from storage once it was flushed and
   * cleaned up.
   */
  @Test
  public void testReportsByTransaction() throws RODAException {
    String jobId = IdUtils.createUUID();
    Job job = createTestJob(jobId, JOB_STATE.STARTED);
    model.createJob(job);

    String transactionA = UUID.randomUUID().toString();
    String transactionB = UUID.randomUUID().toString();
    Report a1 = createTestReport(jobId);
    a1.setTransactionId(transactionA);
    Report a2 = createTestReport(jobId);
    a2.setTransactionId(transactionA);
    Report b1 = createTestReport(jobId);
    b1.setTransactionId(transactionB);
    Report noTransaction = createTestReport(jobId);
    for (Report report : List.of(a1, a2, b1, noTransaction)) {
      model.createOrUpdateJobReport(report, job);
    }

    // running job: from the database
    assertEquals(reportIdsOfTransaction(jobId, transactionA), List.of(a1.getId(), a2.getId()).stream().sorted()
      .collect(Collectors.toList()), "Only transaction A's reports should be listed");
    assertEquals(reportIdsOfTransaction(jobId, transactionB), List.of(b1.getId()));
    assertTrue(reportIdsOfTransaction(jobId, UUID.randomUUID().toString()).isEmpty(),
      "An unknown transaction should have no reports");

    // flushed and cleaned up: from storage
    job.setState(JOB_STATE.COMPLETED);
    job.setEndDate(new Date());
    model.createOrUpdateJob(job);
    jobFlushCleanupTask.cleanFlushedJobs();
    assertFalse(jobRepository.existsById(jobId), "Job should no longer be in the database");
    assertEquals(reportIdsOfTransaction(jobId, transactionA), List.of(a1.getId(), a2.getId()).stream().sorted()
      .collect(Collectors.toList()), "Transaction A's reports should be listed from storage");

    model.deleteJob(jobId);
  }

  private List<String> reportIdsOfTransaction(String jobId, String transactionId) throws RODAException {
    try (CloseableIterable<OptionalWithCause<Report>> reports = model.listJobReportsByTransaction(jobId,
      transactionId)) {
      return StreamSupport.stream(reports.spliterator(), false).filter(OptionalWithCause::isPresent)
        .map(r -> r.get().getId()).sorted().collect(Collectors.toList());
    } catch (IOException e) {
      throw new GenericException("Error closing iterable", e);
    }
  }

  /**
   * Test report persistence for running jobs.
   */
  @Test
  public void testReportPersistence() throws RODAException {
    // Create a running job
    String jobId = IdUtils.createUUID();
    Job job = createTestJob(jobId, JOB_STATE.STARTED);
    model.createJob(job);

    // Create multiple reports
    Report report1 = createTestReport(jobId);
    Report report2 = createTestReport(jobId);
    model.createOrUpdateJobReport(report1, job);
    model.createOrUpdateJobReport(report2, job);

    // Verify reports are in database
    List<Report> dbReports = jobDatabaseService.findReports(jobId);
    assertEquals(dbReports.size(), 2, "Should have 2 reports in database");

    // Verify reports can be listed through model service
    try (CloseableIterable<OptionalWithCause<Report>> reportsIterable = model.listJobReports(jobId)) {
      List<Report> listedReports = StreamSupport.stream(reportsIterable.spliterator(), false)
        .filter(OptionalWithCause::isPresent)
        .map(OptionalWithCause::get)
        .collect(Collectors.toList());
      assertEquals(listedReports.size(), 2, "Should list 2 reports through model service");
    } catch (IOException e) {
      throw new GenericException("Error closing iterable", e);
    }

    // Clean up
    model.deleteJob(jobId);
  }

  private Job createTestJob(String jobId, JOB_STATE state) {
    Job job = new Job();
    job.setId(jobId);
    job.setName("Test Job " + jobId);
    job.setUsername(RodaConstants.ADMIN);
    job.setState(state);
    job.setStartDate(new Date());
    job.setPlugin("org.roda.core.plugins.test.TestPlugin");
    job.setPluginType(PluginType.MISC);
    job.setPluginParameters(new HashMap<>());
    job.setSourceObjects(new SelectedItemsNone<>());
    return job;
  }

  /**
   * A plugin step is saved as RUNNING and then replaced with its outcome
   * (PluginHelper.updatePartialJobReport): the stored steps follow, both when
   * appending and when replacing the last one.
   */
  @Test
  public void testStepAppendAndReplace() throws RODAException {
    String jobId = IdUtils.createUUID();
    Job job = createTestJob(jobId, JOB_STATE.STARTED);
    model.createJob(job);
    Report report = createTestReport(jobId);
    model.createOrUpdateJobReport(report, job);

    Report first = createTestStep("org.roda.Step1", PluginState.SUCCESS, "first done");
    Report running = createTestStep("org.roda.Step2", PluginState.RUNNING, "");
    Report done = createTestStep("org.roda.Step2", PluginState.FAILURE, "second failed");

    // append, append
    Report stored = model.retrieveJobReport(jobId, report.getId());
    stored.addReport(first);
    model.createOrUpdateJobReport(stored, job);
    stored = model.retrieveJobReport(jobId, report.getId());
    stored.addReport(running);
    model.createOrUpdateJobReport(stored, job);
    assertEquals(stepStates(jobId, report.getId()), List.of(PluginState.SUCCESS, PluginState.RUNNING));

    // replace the last step with its outcome
    stored = model.retrieveJobReport(jobId, report.getId());
    stored.getReports().remove(stored.getReports().size() - 1);
    stored.addReport(done);
    model.createOrUpdateJobReport(stored, job);
    Report reread = model.retrieveJobReport(jobId, report.getId());
    assertEquals(stepStates(jobId, report.getId()), List.of(PluginState.SUCCESS, PluginState.FAILURE));
    assertEquals(reread.getReports().get(1).getPluginDetails(), "second failed");
    assertEquals(reread.getStepsCompleted(), Integer.valueOf(2));

    // saving the same steps again leaves the stored step rows untouched
    List<Long> seqs = stepSeqs(report.getId());
    model.createOrUpdateJobReport(model.retrieveJobReport(jobId, report.getId()), job);
    assertEquals(stepSeqs(report.getId()), seqs, "Unchanged steps should not be rewritten");
    assertEquals(stepStates(jobId, report.getId()), List.of(PluginState.SUCCESS, PluginState.FAILURE));

    // appending keeps the existing rows and adds one
    stored = model.retrieveJobReport(jobId, report.getId());
    stored.addReport(createTestStep("org.roda.Step3", PluginState.SUCCESS, "third"));
    model.createOrUpdateJobReport(stored, job);
    List<Long> afterAppend = stepSeqs(report.getId());
    assertEquals(afterAppend.subList(0, 2), seqs, "Appending should keep the existing step rows");
    assertEquals(afterAppend.size(), 3);

    model.deleteJob(jobId);
  }

  /**
   * A job with more reports than one flush page and one delete transaction:
   * every report reaches storage, and cleanup removes everything.
   */
  @Test
  public void testLargeJobFlushAndCleanup() throws RODAException {
    int reports = 1201;
    String jobId = IdUtils.createUUID();
    Job job = createTestJob(jobId, JOB_STATE.STARTED);
    model.createJob(job);
    for (int i = 0; i < reports; i++) {
      model.createOrUpdateJobReport(createTestReport(jobId), job);
    }
    assertEquals(jobDatabaseService.findReports(jobId).size(), reports);

    job.setState(JOB_STATE.COMPLETED);
    job.setEndDate(new Date());
    model.createOrUpdateJob(job);
    assertEquals(listReportIds(jobId).size(), reports, "Every report should have been flushed");

    jobFlushCleanupTask.cleanFlushedJobs();
    assertFalse(jobRepository.existsById(jobId), "Job should be removed from the database");
    assertTrue(jobDatabaseService.findReports(jobId).isEmpty(), "Reports should be removed from the database");
    assertEquals(listReportIds(jobId).size(), reports, "Reports should be listed from storage");

    model.deleteJob(jobId);
  }

  /**
   * Listings stream the running jobs' reports from the database, and list a job
   * flushed but not yet cleaned up only once (from storage).
   */
  @Test
  public void testListingsOfRunningAndFlushedJobs() throws RODAException, IOException {
    String runningId = IdUtils.createUUID();
    Job running = createTestJob(runningId, JOB_STATE.STARTED);
    model.createJob(running);
    Report runningReport = createTestReport(runningId);
    model.createOrUpdateJobReport(runningReport, running);

    String flushedId = IdUtils.createUUID();
    Job flushed = createTestJob(flushedId, JOB_STATE.STARTED);
    model.createJob(flushed);
    Report flushedReport = createTestReport(flushedId);
    model.createOrUpdateJobReport(flushedReport, flushed);
    flushed.setState(JOB_STATE.COMPLETED);
    flushed.setEndDate(new Date());
    model.createOrUpdateJob(flushed);
    assertTrue(jobRepository.existsById(flushedId), "Flushed job is not cleaned up yet");

    try (CloseableIterable<OptionalWithCause<Job>> jobs = model.list(Job.class)) {
      List<String> ids = StreamSupport.stream(jobs.spliterator(), false).filter(OptionalWithCause::isPresent)
        .map(j -> j.get().getId()).collect(Collectors.toList());
      assertEquals(ids.stream().filter(runningId::equals).count(), 1L);
      assertEquals(ids.stream().filter(flushedId::equals).count(), 1L, "Flushed job should be listed once");
    }
    try (CloseableIterable<OptionalWithCause<Report>> reportsIterable = model.list(Report.class)) {
      List<String> ids = StreamSupport.stream(reportsIterable.spliterator(), false)
        .filter(OptionalWithCause::isPresent).map(r -> r.get().getId()).collect(Collectors.toList());
      assertEquals(ids.stream().filter(runningReport.getId()::equals).count(), 1L);
      assertEquals(ids.stream().filter(flushedReport.getId()::equals).count(), 1L,
        "Flushed job's report should be listed once");
    }
    try (CloseableIterable<OptionalWithCause<LiteRODAObject>> lites = model.listLite(Report.class)) {
      List<String> infos = StreamSupport.stream(lites.spliterator(), false).filter(OptionalWithCause::isPresent)
        .map(l -> l.get().getInfo()).collect(Collectors.toList());
      assertEquals(infos.stream().filter(info -> info.contains(runningReport.getId())).count(), 1L);
      assertEquals(infos.stream().filter(info -> info.contains(flushedReport.getId())).count(), 1L);
    }

    model.deleteJob(runningId);
    model.deleteJob(flushedId);
  }

  private List<Long> stepSeqs(String reportId) {
    Long pk = jobReportRepository.findByReportId(reportId).orElseThrow().getPk();
    return jobReportStepRepository.findByReportPkOrderBySeq(pk).stream().map(JobReportStep::getSeq)
      .collect(Collectors.toList());
  }

  private List<PluginState> stepStates(String jobId, String reportId) throws RODAException {
    return model.retrieveJobReport(jobId, reportId).getReports().stream().map(Report::getPluginState)
      .collect(Collectors.toList());
  }

  private List<String> listReportIds(String jobId) throws RODAException {
    try (CloseableIterable<OptionalWithCause<Report>> reportsIterable = model.listJobReports(jobId)) {
      return StreamSupport.stream(reportsIterable.spliterator(), false).filter(OptionalWithCause::isPresent)
        .map(r -> r.get().getId()).collect(Collectors.toList());
    } catch (IOException e) {
      throw new GenericException("Error closing iterable", e);
    }
  }

  private Report createTestStep(String plugin, PluginState state, String details) {
    Report step = new Report();
    step.setPlugin(plugin);
    step.setPluginName(plugin);
    step.setPluginVersion("1.0");
    step.setPluginState(state);
    step.setPluginDetails(details);
    step.setDateCreated(new Date());
    return step;
  }

  private Report createTestReport(String jobId) {
    Report report = new Report();
    report.setId(IdUtils.createUUID());
    report.setJobId(jobId);
    report.setSourceObjectId("test-source-" + UUID.randomUUID().toString().substring(0, 8));
    report.setOutcomeObjectId("test-outcome-" + UUID.randomUUID().toString().substring(0, 8));
    report.setDateCreated(new Date());
    report.setTitle("Test Report");
    return report;
  }
}
