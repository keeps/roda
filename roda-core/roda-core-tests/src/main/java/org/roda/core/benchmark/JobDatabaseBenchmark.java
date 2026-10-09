/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.benchmark;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.config.TestConfig;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.model.ModelService;
import org.roda.core.repository.job.JobFlushCleanupTask;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Database benchmark for the job and job report persistence ({@code jobs} and
 * {@code job_reports} tables).
 *
 * It drives the real {@link ModelService} code paths (the same ones used by
 * the plugin orchestrator) with a synthetic, ingest-like workload and, for each
 * phase, measures client-side latencies, Hibernate statement counts and
 * PostgreSQL-side activity (tuples written, dead tuples, table/TOAST/index
 * sizes, WAL generated and per-statement statistics via pg_stat_statements).
 *
 * Phases:
 * <ol>
 * <li><b>create-jobs</b>: create all jobs (state STARTED)</li>
 * <li><b>ingest</b>: for every SIP of every job, create its report and then,
 * for each plugin step, retrieve the report, append a step sub-report and save
 * it (at step {@code idChangeStep} the outcome object id changes, as when the
 * AIP gets created, which replaces the report row). Every
 * {@code jobUpdateEvery} report updates the job (stats) is also retrieved and
 * saved, as the orchestrator does</li>
 * <li><b>read</b>: list each job's reports and retrieve some of them and the
 * job, as the UI does while a job is running</li>
 * <li><b>finish</b>: move each job to COMPLETED, which flushes the job and its
 * reports to storage and marks the job row as flushed</li>
 * <li><b>cleanup</b>: run {@link JobFlushCleanupTask} once, removing all
 * flushed jobs and reports from the database</li>
 * </ol>
 *
 * The results are written as JSON to {@code benchmark.outputDir} (default
 * {@code target/benchmark}); compare two runs (e.g. before and after a code
 * change) with {@code scripts/benchmark/compare_job_db_benchmark.py}.
 *
 * Not part of any regular test group; run it with:
 *
 * <pre>
 * mvn -pl roda-core/roda-core-tests test -Dtestng.groups=benchmark -Dbenchmark.label=baseline
 * </pre>
 *
 * Workload parameters (system properties, defaults in brackets):
 * {@code benchmark.jobs} [10], {@code benchmark.sipsPerJob} [100],
 * {@code benchmark.steps} [12], {@code benchmark.threads} [8],
 * {@code benchmark.idChangeStep} [3], {@code benchmark.jobUpdateEvery} [10],
 * {@code benchmark.detailsBytes} [256], {@code benchmark.readsPerJob} [50],
 * {@code benchmark.warmupJobs} [2], {@code benchmark.warmupSipsPerJob} [20],
 * {@code benchmark.topStatements} [20], {@code benchmark.statsFlushWaitMillis}
 * [1500], {@code benchmark.seed} [42], {@code benchmark.label} [git branch],
 * {@code benchmark.outputDir} [target/benchmark].
 *
 * @author RODA Development Team
 */
@SpringBootTest(classes = TestConfig.class, properties = {"spring.jpa.properties.hibernate.generate_statistics=true",
  "spring.jpa.properties.hibernate.session.events.log=false"})
@Test(groups = {JobDatabaseBenchmark.BENCHMARK_GROUP})
public class JobDatabaseBenchmark extends AbstractDatabaseBenchmark {
  public static final String BENCHMARK_GROUP = "benchmark";

  private static final Logger LOGGER = LoggerFactory.getLogger(JobDatabaseBenchmark.class);
  private static final String OP_REPORT_RETRIEVE = "report.retrieve";
  private static final String OP_JOB_RETRIEVE = "job.retrieve";

  private ModelService model;
  private Config config;

  @BeforeClass
  public void init() throws Exception {
    initRoda("", false, false, false, false);
    model = RodaCoreFactory.getModelService();
    config = Config.fromSystemProperties();
  }

  @Test
  public void benchmarkJobAndReportPersistence() throws Exception {
    LOGGER.info("Job database benchmark configuration: {}", config.toMap());

    if (config.warmupJobs > 0) {
      LOGGER.info("Warming up with {} jobs x {} SIPs (not measured)", config.warmupJobs, config.warmupSipsPerJob);
      collector.resetTables();
      runWorkload(config.warmupJobs, config.warmupSipsPerJob);
    }

    collector.resetTables();
    List<Map<String, Object>> phases = runWorkload(config.jobs, config.sipsPerJob);

    Map<String, Object> result = resultHeader(config.toMap());
    result.put("phases", phases);

    Path output = writeResult(result);
    LOGGER.info("Job database benchmark results:\n{}", summary(phases));
    LOGGER.info("Job database benchmark results written to {}", output.toAbsolutePath());
  }

  private List<Map<String, Object>> runWorkload(int numberOfJobs, int sipsPerJob) throws Exception {
    Random random = new Random(config.seed);
    List<Job> jobs = new ArrayList<>();
    Map<String, List<String>> sipIdsByJob = new LinkedHashMap<>();
    for (int i = 0; i < numberOfJobs; i++) {
      List<String> sipIds = new ArrayList<>();
      for (int s = 0; s < sipsPerJob; s++) {
        sipIds.add(IdUtils.createUUID());
      }
      Job job = createJobObject(i, sipIds);
      jobs.add(job);
      sipIdsByJob.put(job.getId(), sipIds);
    }
    Map<String, List<String>> reportIdsByJob = new ConcurrentHashMap<>();
    Map<String, AtomicInteger> reportUpdatesByJob = new ConcurrentHashMap<>();

    List<Map<String, Object>> phases = new ArrayList<>();

    phases.add(runPhase("create-jobs", recorder -> runParallel(config.threads,
      jobs.stream().map(job -> (Callable<Void>) () -> {
      recorder.time("job.create", () -> model.createJob(job));
      return null;
    }).toList())));

    List<Job> sipJobs = new ArrayList<>();
    List<String> sipIds = new ArrayList<>();
    for (Job job : jobs) {
      reportIdsByJob.put(job.getId(), Collections.synchronizedList(new ArrayList<>()));
      reportUpdatesByJob.put(job.getId(), new AtomicInteger());
      for (String sipId : sipIdsByJob.get(job.getId())) {
        sipJobs.add(job);
        sipIds.add(sipId);
      }
    }
    // interleave the SIPs of the different jobs, as with concurrent ingests
    List<Integer> sipOrder = new ArrayList<>();
    for (int i = 0; i < sipIds.size(); i++) {
      sipOrder.add(i);
    }
    Collections.shuffle(sipOrder, random);
    phases.add(runPhase("ingest", recorder -> runParallel(config.threads,
      sipOrder.stream().map(i -> (Callable<Void>) () -> {
      Job job = sipJobs.get(i);
      ingestSip(recorder, job, sipIds.get(i), reportIdsByJob.get(job.getId()), reportUpdatesByJob.get(job.getId()));
      return null;
    }).toList())));

    phases.add(runPhase("read", recorder -> runParallel(config.threads,
      jobs.stream().map(job -> (Callable<Void>) () -> {
      readJob(recorder, job, reportIdsByJob.get(job.getId()), new Random(config.seed + job.getId().hashCode()));
      return null;
    }).toList())));

    phases.add(runPhase("finish", recorder -> runParallel(config.threads,
      jobs.stream().map(job -> (Callable<Void>) () -> {
      Job finalJob = recorder.time(OP_JOB_RETRIEVE, () -> model.retrieveJob(job.getId()));
      finalJob.setState(JOB_STATE.COMPLETED);
      finalJob.setEndDate(new Date());
      finalJob.getJobStats().setCompletionPercentage(100);
      recorder.time("job.finish", () -> model.createOrUpdateJob(finalJob));
      return null;
    }).toList())));

    phases.add(runPhase("cleanup", recorder -> recorder.time("cleanup.run", jobFlushCleanupTask::cleanFlushedJobs)));

    return phases;
  }

  private void ingestSip(LatencyRecorder recorder, Job job, String sipId, List<String> reportIds,
    AtomicInteger reportUpdates) throws Exception {
    String jobId = job.getId();
    String outcomeId = Report.NO_OUTCOME_OBJECT_ID;
    int sipCount = Math.max(1, job.getJobStats().getSourceObjectsCount());

    Report report = new Report();
    report.setJobId(jobId);
    report.setId(IdUtils.getJobReportId(jobId, sipId, outcomeId));
    report.setSourceAndOutcomeObjectId(sipId, outcomeId);
    report.setSourceObjectClass(TransferredResource.class.getName());
    report.setSourceObjectOriginalName("sip-" + sipId + ".zip");
    report.setSourceObjectOriginalIds(List.of("original-" + sipId));
    report.setTitle(job.getName());
    report.setPlugin(job.getPlugin());
    report.setPluginName("Benchmark ingest");
    report.setPluginVersion("1.0");
    report.setTotalSteps(config.steps);
    report.setDateCreated(new Date());
    report.setDateUpdated(new Date());
    recorder.time("report.create", () -> model.createOrUpdateJobReport(report, job));

    Random random = new Random(config.seed + sipId.hashCode());
    for (int step = 1; step <= config.steps; step++) {
      String currentOutcomeId = outcomeId;
      Report current = recorder.time(OP_REPORT_RETRIEVE,
        () -> model.retrieveJobReport(jobId, sipId, currentOutcomeId));

      boolean idChange = step == config.idChangeStep;
      if (idChange) {
        outcomeId = IdUtils.createUUID();
        current.setOutcomeObjectId(outcomeId);
        current.setOutcomeObjectClass(AIP.class.getName());
      }
      current.addReport(createStepReport(step, random));
      recorder.time(idChange ? "report.update.id-change" : "report.update",
        () -> model.createOrUpdateJobReport(current, job));

      if (reportUpdates.incrementAndGet() % config.jobUpdateEvery == 0) {
        Job jobToUpdate = recorder.time(OP_JOB_RETRIEVE, () -> model.retrieveJob(jobId));
        jobToUpdate.getJobStats().setSourceObjectsProcessedWithSuccess(reportUpdates.get() / config.steps);
        jobToUpdate.getJobStats()
          .setCompletionPercentage(Math.min(100, reportUpdates.get() * 100 / (config.steps * sipCount)));
        recorder.time("job.update", () -> model.createOrUpdateJob(jobToUpdate));
      }
    }
    reportIds.add(IdUtils.getJobReportId(jobId, sipId, outcomeId));
  }

  private void readJob(LatencyRecorder recorder, Job job, List<String> reportIds, Random random) throws Exception {
    String jobId = job.getId();
    recorder.time("report.list", () -> {
      int count = 0;
      try (CloseableIterable<OptionalWithCause<Report>> reports = model.listJobReports(jobId)) {
        for (OptionalWithCause<Report> ignored : reports) {
          count++;
        }
      }
      return count;
    });
    for (int i = 0; i < config.readsPerJob && !reportIds.isEmpty(); i++) {
      String reportId = reportIds.get(random.nextInt(reportIds.size()));
      recorder.time(OP_REPORT_RETRIEVE, () -> model.retrieveJobReport(jobId, reportId));
      if (i % 10 == 0) {
        recorder.time(OP_JOB_RETRIEVE, () -> model.retrieveJob(jobId));
      }
    }
  }

  private Job createJobObject(int index, List<String> sipIds) {
    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName("Benchmark ingest job " + index);
    job.setUsername(RodaConstants.ADMIN);
    job.setState(JOB_STATE.STARTED);
    job.setStartDate(new Date());
    job.setPlugin("org.roda.core.plugins.base.ingest.EARKSIP2ToAIPPlugin");
    job.setPluginType(PluginType.SIP_TO_AIP);
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, "benchmark-parent");
    parameters.put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, "false");
    job.setPluginParameters(parameters);
    job.setSourceObjects(SelectedItemsList.create(TransferredResource.class, sipIds));
    job.getJobStats().setSourceObjectsCount(sipIds.size());
    job.getJobStats().setSourceObjectsWaitingToBeProcessed(sipIds.size());
    return job;
  }

  private Report createStepReport(int step, Random random) {
    Report stepReport = new Report();
    stepReport.setPlugin("org.roda.core.plugins.benchmark.Step" + step + "Plugin");
    stepReport.setPluginName("Benchmark step " + step);
    stepReport.setPluginVersion("1.0");
    stepReport.setPluginState(PluginState.SUCCESS);
    stepReport.setPluginDetails(randomText(config.detailsBytes, random));
    stepReport.setDateCreated(new Date());
    return stepReport;
  }

  private static String randomText(int length, Random random) {
    String[] words = {"file", "format", "identified", "as", "fmt/", "checksum", "verified", "virus", "check",
      "passed", "metadata", "valid", "representation", "created", "preservation", "event", "agent", "pdf", "tiff"};
    StringBuilder sb = new StringBuilder(length + 16);
    while (sb.length() < length) {
      sb.append(words[random.nextInt(words.length)]);
      if (random.nextInt(4) == 0) {
        sb.append(random.nextInt(1000));
      }
      sb.append(' ');
    }
    return sb.substring(0, length);
  }

  /**
   * Benchmark parameters, read from {@code benchmark.*} system properties.
   */
  private static final class Config {
    private int jobs;
    private int sipsPerJob;
    private int steps;
    private int threads;
    private int idChangeStep;
    private int jobUpdateEvery;
    private int detailsBytes;
    private int readsPerJob;
    private int warmupJobs;
    private int warmupSipsPerJob;
    private long seed;

    static Config fromSystemProperties() {
      Config config = new Config();
      config.jobs = Integer.getInteger("benchmark.jobs", 10);
      config.sipsPerJob = Integer.getInteger("benchmark.sipsPerJob", 100);
      config.steps = Integer.getInteger("benchmark.steps", 12);
      config.threads = Integer.getInteger("benchmark.threads", 8);
      config.idChangeStep = Integer.getInteger("benchmark.idChangeStep", 3);
      config.jobUpdateEvery = Math.max(1, Integer.getInteger("benchmark.jobUpdateEvery", 10));
      config.detailsBytes = Integer.getInteger("benchmark.detailsBytes", 256);
      config.readsPerJob = Integer.getInteger("benchmark.readsPerJob", 50);
      config.warmupJobs = Integer.getInteger("benchmark.warmupJobs", 2);
      config.warmupSipsPerJob = Integer.getInteger("benchmark.warmupSipsPerJob", 20);
      config.seed = Long.getLong("benchmark.seed", 42L);
      return config;
    }

    Map<String, Object> toMap() {
      Map<String, Object> ret = new LinkedHashMap<>();
      ret.put("jobs", jobs);
      ret.put("sipsPerJob", sipsPerJob);
      ret.put("steps", steps);
      ret.put("threads", threads);
      ret.put("idChangeStep", idChangeStep);
      ret.put("jobUpdateEvery", jobUpdateEvery);
      ret.put("detailsBytes", detailsBytes);
      ret.put("readsPerJob", readsPerJob);
      ret.put("warmupJobs", warmupJobs);
      ret.put("warmupSipsPerJob", warmupSipsPerJob);
      ret.put("seed", seed);
      return ret;
    }
  }

}
