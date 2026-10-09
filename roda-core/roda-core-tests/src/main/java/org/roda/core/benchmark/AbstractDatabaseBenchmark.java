/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.benchmark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.config.ConfigurationManager;
import org.roda.core.repository.job.JobFlushCleanupTask;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

/**
 * Shared machinery of the database benchmarks: a RODA instance in a temporary
 * home, per-phase measurement (client latencies, Hibernate statistics and
 * PostgreSQL statistics through {@link PostgresStatsCollector}) and the JSON
 * result format read by {@code scripts/benchmark}.
 *
 * Subclasses carry {@code @SpringBootTest(classes = TestConfig.class,
 * properties = {"spring.jpa.properties.hibernate.generate_statistics=true",
 * ...})} and call {@link #initRoda} from their {@code @BeforeClass}.
 *
 * @author RODA Development Team
 */
public abstract class AbstractDatabaseBenchmark extends AbstractTestNGSpringContextTests {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatabaseBenchmark.class);

  // "jobs" first: resetting the tables deletes in this order, and the other rows cascade from it ("plugins" is
  // shared reference data, cached by JobDatabaseService, so it is never reset)
  protected static final List<String> TABLES = List.of("jobs", "job_stats", "job_plugin_parameters",
    "job_source_objects", "job_users", "job_attachments", "job_reports", "job_report_source_original_ids",
    "job_report_steps");

  @Autowired
  private DataSource dataSource;

  @Autowired
  private EntityManagerFactory entityManagerFactory;

  @Autowired
  protected JobFlushCleanupTask jobFlushCleanupTask;

  protected Path basePath;
  protected CommonConfig common;
  private LdapUtilityTestHelper ldapUtilityTestHelper;
  private HikariDataSource hikari;
  private Statistics hibernateStatistics;
  protected PostgresStatsCollector collector;

  /**
   * Starts RODA in a temporary home and prepares the measurements.
   *
   * The configuration manager is a singleton that fixes the RODA home the first
   * time it is used, which the Spring context already does while starting (before
   * any {@code @BeforeClass} could point {@code roda.home} elsewhere). It is
   * therefore reset here, and the benchmark refuses to run unless RODA really
   * uses the temporary home, so that it never writes into a real installation
   * (e.g. {@code ~/.roda}).
   */
  protected void initRoda(String defaultLabelPrefix, boolean deploySolr, boolean deployFolderMonitor,
    boolean deployOrchestrator, boolean deployPluginManager) throws Exception {
    common = CommonConfig.fromSystemProperties(defaultLabelPrefix);
    basePath = TestsHelper.createBaseTempDir(getClass(), true);
    ConfigurationManager.resetInstanceAfterTest();
    ldapUtilityTestHelper = new LdapUtilityTestHelper();
    RodaCoreFactory.instantiateTest(deploySolr, true, deployFolderMonitor, deployOrchestrator, deployPluginManager,
      false, false, ldapUtilityTestHelper.getLdapUtility());
    Path rodaHome = RodaCoreFactory.getRodaHomePath();
    if (!rodaHome.toAbsolutePath().normalize().startsWith(basePath.toAbsolutePath().normalize())) {
      throw new IllegalStateException(
        "RODA home is " + rodaHome + ", not the benchmark's temporary " + basePath + ": refusing to run");
    }

    hikari = dataSource.unwrap(HikariDataSource.class);
    hibernateStatistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    hibernateStatistics.setStatisticsEnabled(true);
    collector = new PostgresStatsCollector(hikari.getJdbcUrl(), hikari.getUsername(), hikari.getPassword(), TABLES);
  }

  @AfterClass(alwaysRun = true)
  public void cleanup() throws Exception {
    if (collector != null) {
      collector.resetTables();
      collector.close();
    }
    if (ldapUtilityTestHelper != null) {
      ldapUtilityTestHelper.shutdown();
    }
    RodaCoreFactory.shutdown();
    if (basePath != null) {
      FSUtils.deletePath(basePath);
    }
  }

  /**
   * @return the result document (to which the subclass adds its "phases")
   */
  protected Map<String, Object> resultHeader(Map<String, Object> workloadConfig) throws SQLException {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("label", common.label);
    result.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date()));
    result.put("git", gitInfo());
    result.put("config", workloadConfig);
    result.put("environment", environment());
    return result;
  }

  @FunctionalInterface
  protected interface PhaseBody {
    void run(LatencyRecorder recorder) throws Exception;
  }

  protected Map<String, Object> runPhase(String name, PhaseBody body) throws Exception {
    LOGGER.info("Benchmark phase '{}' starting", name);
    LatencyRecorder recorder = new LatencyRecorder();

    flushPoolStatistics();
    PostgresStatsCollector.Snapshot before = collector.snapshot();
    collector.resetStatements();
    hibernateStatistics.clear();

    long start = System.nanoTime();
    body.run(recorder);
    long wallNanos = System.nanoTime() - start;

    Map<String, Object> hibernate = hibernateStatistics();
    Map<String, Object> statementTotals = collector.statementTotals();
    List<Map<String, Object>> topStatements = collector.topStatements(common.topStatements);
    flushPoolStatistics();
    PostgresStatsCollector.Snapshot after = collector.snapshot();

    double wallMillis = wallNanos / 1_000_000d;
    long operations = recorder.totalCount();
    Map<String, Object> phase = new LinkedHashMap<>();
    phase.put("name", name);
    phase.put("wallMs", Math.round(wallMillis * 1000d) / 1000d);
    phase.put("operations", operations);
    phase.put("opsPerSec", wallMillis == 0 ? 0 : Math.round(operations / (wallMillis / 1000d) * 100d) / 100d);
    phase.put("latency", recorder.summarize());
    phase.put("hibernate", hibernate);
    phase.put("walBytes", after.getWalBytes() - before.getWalBytes());
    Map<String, Object> tables = new LinkedHashMap<>();
    for (String table : after.getTables().keySet()) {
      tables.put(table, after.getTables().get(table).diff(before.getTables().get(table)));
    }
    phase.put("tables", tables);
    phase.put("statementTotals", statementTotals);
    phase.put("topStatements", topStatements);
    LOGGER.info("Benchmark phase '{}' finished in {} ms ({} operations)", name, phase.get("wallMs"), operations);
    return phase;
  }

  /**
   * Closes the (idle) pool connections so that their backends publish their
   * pending table statistics, then waits for that to happen.
   */
  protected void flushPoolStatistics() throws InterruptedException {
    hikari.getHikariPoolMXBean().softEvictConnections();
    Thread.sleep(common.statsFlushWaitMillis);
  }

  protected Map<String, Object> hibernateStatistics() {
    Map<String, Object> ret = new LinkedHashMap<>();
    ret.put("preparedStatements", hibernateStatistics.getPrepareStatementCount());
    ret.put("queries", hibernateStatistics.getQueryExecutionCount());
    ret.put("entityLoads", hibernateStatistics.getEntityLoadCount());
    ret.put("entityInserts", hibernateStatistics.getEntityInsertCount());
    ret.put("entityUpdates", hibernateStatistics.getEntityUpdateCount());
    ret.put("entityDeletes", hibernateStatistics.getEntityDeleteCount());
    ret.put("transactions", hibernateStatistics.getTransactionCount());
    ret.put("flushes", hibernateStatistics.getFlushCount());
    ret.put("sessions", hibernateStatistics.getSessionOpenCount());
    return ret;
  }

  protected static void runParallel(int threads, List<Callable<Void>> tasks) throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(threads);
    try {
      for (Future<Void> future : executor.invokeAll(tasks)) {
        future.get();
      }
    } finally {
      executor.shutdownNow();
    }
  }

  protected Map<String, Object> environment() throws SQLException {
    Map<String, Object> ret = new LinkedHashMap<>();
    ret.put("java", System.getProperty("java.version"));
    ret.put("availableProcessors", Runtime.getRuntime().availableProcessors());
    ret.put("connectionPoolMaxSize", hikari.getMaximumPoolSize());
    ret.put("pgStatStatements", collector.isStatementsAvailable());
    ret.put("postgres", collector.environment());
    return ret;
  }

  protected static Map<String, Object> gitInfo() {
    Map<String, Object> ret = new LinkedHashMap<>();
    ret.put("branch", git("rev-parse", "--abbrev-ref", "HEAD"));
    ret.put("commit", git("rev-parse", "--short", "HEAD"));
    String status = git("status", "--porcelain", "--untracked-files=no");
    ret.put("dirty", status != null && !status.isEmpty());
    return ret;
  }

  protected static String git(String... args) {
    List<String> command = new ArrayList<>();
    command.add("git");
    Collections.addAll(command, args);
    try {
      Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
      String output;
      try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        output = String.join("\n", reader.lines().toList()).trim();
      }
      if (process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0) {
        return output;
      }
    } catch (IOException e) {
      LOGGER.debug("Could not run git", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return null;
  }

  protected Path writeResult(Map<String, Object> result) throws IOException {
    Path outputDir = Paths.get(common.outputDir);
    Files.createDirectories(outputDir);
    String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
    String label = common.label.replaceAll("[^A-Za-z0-9._-]", "_");
    Path output = outputDir.resolve("job-db-benchmark-" + label + "-" + timestamp + ".json");
    new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValue(output.toFile(), result);
    return output;
  }

  @SuppressWarnings("unchecked")
  protected static String summary(List<Map<String, Object>> phases) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%-12s %10s %8s %10s %10s %12s %14s %10s %10s%n", "phase", "wall ms", "ops", "ops/s",
      "stmts", "pg exec ms", "WAL bytes", "upd rows", "dead rows"));
    for (Map<String, Object> phase : phases) {
      Map<String, Object> hibernate = (Map<String, Object>) phase.get("hibernate");
      Map<String, Object> totals = (Map<String, Object>) phase.get("statementTotals");
      Object pgExec = totals.isEmpty() ? "n/a"
        : ((Map<String, Object>) totals.get("all")).get("total_exec_ms");
      long updated = 0;
      long dead = 0;
      for (Object table : ((Map<String, Object>) phase.get("tables")).values()) {
        updated += (Long) ((Map<String, Object>) table).get("tuplesUpdated");
        dead += (Long) ((Map<String, Object>) table).get("deadTuplesAfter");
      }
      sb.append(String.format("%-12s %10s %8s %10s %10s %12s %14s %10s %10s%n", phase.get("name"),
        phase.get("wallMs"), phase.get("operations"), phase.get("opsPerSec"), hibernate.get("preparedStatements"),
        pgExec, phase.get("walBytes"), updated, dead));
    }
    return sb.toString();
  }

  /**
   * Parameters shared by all benchmarks, read from {@code benchmark.*} system
   * properties.
   */
  protected static final class CommonConfig {
    protected int topStatements;
    protected long statsFlushWaitMillis;
    protected String label;
    protected String outputDir;

    static CommonConfig fromSystemProperties(String defaultLabelPrefix) {
      CommonConfig config = new CommonConfig();
      config.topStatements = Integer.getInteger("benchmark.topStatements", 20);
      config.statsFlushWaitMillis = Long.getLong("benchmark.statsFlushWaitMillis", 1500L);
      String branch = git("rev-parse", "--abbrev-ref", "HEAD");
      String defaultLabel = defaultLabelPrefix + (branch == null ? "run" : branch);
      config.label = System.getProperty("benchmark.label", defaultLabel);
      config.outputDir = System.getProperty("benchmark.outputDir", "target/benchmark");
      return config;
    }
  }
}
