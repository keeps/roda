/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.benchmark;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.config.TestConfig;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.ingest.v2.ConfigurableIngestPlugin;
import org.roda.core.plugins.base.ingest.v2.MinimalIngestPlugin;
import org.roda.core.plugins.orchestrate.JobsHelper;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Database benchmark driven by real SIP ingests: the same E-ARK SIP, copied N
 * times as transferred resources, is ingested by the real plugin orchestrator
 * with a real ingest workflow, so jobs and reports reach the database exactly as
 * in production. Each ingest step appends its report item as RUNNING
 * ({@code PluginHelper.updatePartialJobReport(..., false, ...)}) and then
 * replaces it with its outcome ({@code ..., true, ...}); the SIP-to-AIP step
 * changes the report's outcome (and so its id); the job counters are updated as
 * the orchestrator reports progress; and the job is flushed to storage when it
 * ends.
 *
 * Phases:
 * <ol>
 * <li><b>ingest</b>: run all ingest jobs (concurrently when
 * {@code benchmark.jobs} &gt; 1), each until it ends, including its flush to
 * storage. SIP copies and the parent AIP are prepared before, unmeasured</li>
 * <li><b>cleanup</b>: run {@code JobFlushCleanupTask} once</li>
 * </ol>
 *
 * Besides the database it also measures everything else an ingest does
 * (storage, index, ClamAV, Siegfried), so wall times are only comparable
 * between runs on the same machine; the database counters (statements, tuples
 * written, WAL, table sizes) are what isolates the database layer. The result
 * also records each job's final state and counters, to check that the runs
 * being compared ingested the same way.
 *
 * Runs in its own group; with Docker available:
 *
 * <pre>
 * mvn -pl roda-core/roda-core-tests test -Dtestng.groups=benchmark-ingest -Dbenchmark.label=ingest-baseline
 * </pre>
 *
 * Parameters (system properties, defaults in brackets): {@code benchmark.sips}
 * [50], {@code benchmark.jobs} [2], {@code benchmark.ingestPlugin}
 * [configurable] (the default workflow; {@code minimal} skips the virus check,
 * format identification and disposal rules), {@code benchmark.virusCheck}
 * [true], {@code benchmark.formatIdentification} [true],
 * {@code benchmark.blockSize} [10] (SIPs per orchestrator worker block),
 * {@code benchmark.sipFile} [e-ark-sip-2.1.0-with-custom-representation-type.zip,
 * from the test corpora], {@code benchmark.warmupSips} [2],
 * {@code benchmark.jobTimeoutSeconds} [1800], plus the common
 * {@code benchmark.label} [ingest-&lt;git branch&gt;], {@code benchmark.outputDir},
 * {@code benchmark.topStatements} and {@code benchmark.statsFlushWaitMillis}.
 *
 * @author RODA Development Team
 */
@SpringBootTest(classes = TestConfig.class, properties = {"spring.jpa.properties.hibernate.generate_statistics=true",
  "spring.jpa.properties.hibernate.session.events.log=false"})
@Test(groups = {IngestDatabaseBenchmark.BENCHMARK_GROUP})
public class IngestDatabaseBenchmark extends AbstractDatabaseBenchmark {
  public static final String BENCHMARK_GROUP = "benchmark-ingest";

  private static final Logger LOGGER = LoggerFactory.getLogger(IngestDatabaseBenchmark.class);

  private ModelService model;
  private IndexService index;
  private Config config;

  @BeforeClass
  public void init() throws Exception {
    initRoda("ingest-", true, true, true, true);
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();
    config = Config.fromSystemProperties();

    // keep the transferred resources in place after ingest (both are already false by default, but other
    // tests change them on the shared configuration)
    RodaCoreFactory.getRodaConfiguration()
      .setProperty(RodaConstants.CORE_TRANSFERRED_RESOURCES_INGEST_MOVE_WHEN_AUTOACCEPT, false);
    RodaCoreFactory.getRodaConfiguration()
      .setProperty(RodaConstants.CORE_TRANSFERRED_RESOURCES_DELETE_WHEN_SUCCESSFULLY_INGESTED, false);
    JobsHelper.setBlockSize(config.blockSize);
    JobsHelper.setSyncTimeout(config.jobTimeoutSeconds);
  }

  @AfterClass(alwaysRun = true)
  public void resetIndex() {
    IndexTestUtils.resetIndex();
  }

  @Test
  public void benchmarkRealIngest() throws Exception {
    LOGGER.info("Ingest database benchmark configuration: {}", config.toMap());

    if (config.warmupSips > 0) {
      LOGGER.info("Warming up with {} SIPs in one job (not measured)", config.warmupSips);
      collector.resetTables();
      runIngest("warmup", config.warmupSips, 1);
    }

    collector.resetTables();
    IngestRun run = runIngest("run", config.sips, config.jobs);

    Map<String, Object> result = resultHeader(config.toMap());
    result.put("phases", run.phases);
    result.put("ingestOutcome", run.outcome);
    Path output = writeResult(result);
    LOGGER.info("Ingest database benchmark results:\n{}", summary(run.phases));
    LOGGER.info("Ingest database benchmark job outcomes: {}", run.outcome);
    LOGGER.info("Ingest database benchmark results written to {}", output.toAbsolutePath());
  }

  private record IngestRun(List<Map<String, Object>> phases, List<Map<String, Object>> outcome) {
  }

  private IngestRun runIngest(String name, int sips, int numberOfJobs) throws Exception {
    // unmeasured preparation: SIP copies (unique names, as transferred resources are identified by path) and
    // the AIP every SIP is ingested under
    List<String> transferredResources = copySips(name, sips);
    AIP parent = model.createAIP(null, RodaConstants.AIP_TYPE_MIXED, new Permissions(), RodaConstants.ADMIN, null);

    List<Job> jobs = new ArrayList<>();
    int perJob = (int) Math.ceil((double) transferredResources.size() / numberOfJobs);
    for (int i = 0; i < transferredResources.size(); i += perJob) {
      // a real list (a subList view is not Serializable, which the job's source objects must be)
      List<String> jobSips = new ArrayList<>(
        transferredResources.subList(i, Math.min(transferredResources.size(), i + perJob)));
      jobs.add(createJobObject(name + " " + (jobs.size() + 1), parent.getId(), jobSips));
    }

    List<Map<String, Object>> phases = new ArrayList<>();
    phases.add(runPhase("ingest", recorder -> runParallel(jobs.size(), jobs.stream().map(job -> (Callable<Void>) () -> {
      // synchronous: returns when the job ended (and was flushed to storage)
      recorder.time("job.ingest",
        () -> RodaCoreFactory.getPluginOrchestrator().createAndExecuteJobs(job, false));
      return null;
    }).toList())));

    List<Map<String, Object>> outcome = new ArrayList<>();
    for (Job job : jobs) {
      outcome.add(outcomeOf(model.retrieveJob(job.getId()), job.getSourceObjects()));
    }

    phases.add(runPhase("cleanup", recorder -> recorder.time("cleanup.run", jobFlushCleanupTask::cleanFlushedJobs)));
    return new IngestRun(phases, outcome);
  }

  private List<String> copySips(String prefix, int sips) throws Exception {
    Path sip = Paths.get(getClass().getResource("/corpora").toURI())
      .resolve(CorporaConstants.SIP_FOLDER).resolve(config.sipFile);
    TransferredResourcesScanner scanner = RodaCoreFactory.getTransferredResourcesScanner();
    String runId = IdUtils.createUUID().substring(0, 8);
    List<String> uuids = new ArrayList<>();
    for (int i = 1; i <= sips; i++) {
      try (InputStream in = Files.newInputStream(sip)) {
        String fileName = String.format("benchmark-%s-%s-%04d.zip", prefix, runId, i);
        uuids.add(scanner.createFile(null, fileName, in).getUUID());
      }
    }
    scanner.updateTransferredResources(Optional.empty(), true);
    index.commit(TransferredResource.class);
    return uuids;
  }

  private Job createJobObject(String name, String parentId, List<String> transferredResources) {
    Class<?> plugin = config.ingestPlugin.equals("minimal") ? MinimalIngestPlugin.class
      : ConfigurableIngestPlugin.class;
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, parentId);
    parameters.put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, "true");
    parameters.put(RodaConstants.PLUGIN_PARAMS_DO_AUTO_ACCEPT, "true");
    parameters.put(RodaConstants.PLUGIN_PARAMS_EMAIL_NOTIFICATION, "");
    if (plugin == ConfigurableIngestPlugin.class) {
      parameters.put(RodaConstants.PLUGIN_PARAMS_DO_VIRUS_CHECK, Boolean.toString(config.virusCheck));
      parameters.put(RodaConstants.PLUGIN_PARAMS_DO_FILE_FORMAT_IDENTIFICATION,
        Boolean.toString(config.formatIdentification));
    }

    Job job = new Job();
    job.setId(IdUtils.createUUID());
    job.setName("Benchmark " + name);
    job.setPlugin(plugin.getName());
    job.setPluginType(PluginType.INGEST);
    job.setPluginParameters(parameters);
    job.setSourceObjects(SelectedItemsList.create(TransferredResource.class, transferredResources));
    job.setUsername(RodaConstants.ADMIN);
    return job;
  }

  private static Map<String, Object> outcomeOf(Job job, Object sourceObjects) {
    JobStats stats = job.getJobStats();
    Map<String, Object> ret = new LinkedHashMap<>();
    ret.put("state", job.getState());
    ret.put("sips", ((SelectedItemsList<?>) sourceObjects).getIds().size());
    ret.put("success", stats.getSourceObjectsProcessedWithSuccess());
    ret.put("partialSuccess", stats.getSourceObjectsProcessedWithPartialSuccess());
    ret.put("failure", stats.getSourceObjectsProcessedWithFailure());
    ret.put("skipped", stats.getSourceObjectsProcessedWithSkipped());
    ret.put("manualIntervention", stats.getOutcomeObjectsWithManualIntervention());
    return ret;
  }

  /**
   * Workload parameters, read from {@code benchmark.*} system properties.
   */
  private static final class Config {
    private int sips;
    private int jobs;
    private String ingestPlugin;
    private boolean virusCheck;
    private boolean formatIdentification;
    private int blockSize;
    private String sipFile;
    private int warmupSips;
    private int jobTimeoutSeconds;

    static Config fromSystemProperties() {
      Config config = new Config();
      config.sips = Integer.getInteger("benchmark.sips", 50);
      config.jobs = Math.max(1, Integer.getInteger("benchmark.jobs", 2));
      config.ingestPlugin = System.getProperty("benchmark.ingestPlugin", "configurable");
      config.virusCheck = Boolean.parseBoolean(System.getProperty("benchmark.virusCheck", "true"));
      config.formatIdentification = Boolean
        .parseBoolean(System.getProperty("benchmark.formatIdentification", "true"));
      config.blockSize = Math.max(1, Integer.getInteger("benchmark.blockSize", 10));
      config.sipFile = System.getProperty("benchmark.sipFile", "e-ark-sip-2.1.0-with-custom-representation-type.zip");
      config.warmupSips = Integer.getInteger("benchmark.warmupSips", 2);
      config.jobTimeoutSeconds = Integer.getInteger("benchmark.jobTimeoutSeconds", 1800);
      return config;
    }

    Map<String, Object> toMap() {
      Map<String, Object> ret = new LinkedHashMap<>();
      ret.put("workload", "real-ingest");
      ret.put("sips", sips);
      ret.put("jobs", jobs);
      ret.put("ingestPlugin", ingestPlugin);
      ret.put("virusCheck", virusCheck);
      ret.put("formatIdentification", formatIdentification);
      ret.put("blockSize", blockSize);
      ret.put("sipFile", sipFile);
      ret.put("warmupSips", warmupSips);
      return ret;
    }
  }
}
