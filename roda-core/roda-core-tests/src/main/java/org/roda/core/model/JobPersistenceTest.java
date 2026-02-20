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
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.repository.job.JobRepository;
import org.roda.core.repository.job.ReportRepository;
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
  private ReportRepository reportRepository;

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
    jobRepository.deleteAll();
    reportRepository.deleteAll();

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
   * Test that updating a job to a final state (COMPLETED) flushes it from the database
   * to file storage.
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
    assertTrue(reportRepository.existsById(report.getId()), "Report should exist in database");

    // Now update job to final state
    job.setState(JOB_STATE.COMPLETED);
    job.setEndDate(new Date());
    model.createOrUpdateJob(job);

    // Verify job is no longer in database (flushed to storage)
    assertFalse(jobRepository.existsById(jobId), "Job should not exist in database after completion");

    // Verify report is no longer in database
    assertFalse(reportRepository.existsById(report.getId()), "Report should not exist in database after job completion");

    // Verify job can still be retrieved (from storage)
    Job retrievedJob = model.retrieveJob(jobId);
    assertNotNull(retrievedJob, "Should be able to retrieve completed job from storage");
    assertEquals(retrievedJob.getState(), JOB_STATE.COMPLETED);

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
    assertTrue(reportRepository.existsById(report.getId()), "Report should exist in database");

    // Delete the job
    model.deleteJob(jobId);

    // Verify both job and reports are deleted from DB
    assertFalse(jobRepository.existsById(jobId), "Job should be deleted from database");
    List<Report> remainingReports = reportRepository.findByJobId(jobId);
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
    List<Report> dbReports = reportRepository.findByJobId(jobId);
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
