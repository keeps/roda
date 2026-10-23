/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.review;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.cloud.DocCollection;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Solr's own requests between shards deadlock over HTTP/2 under RODA's load.
 * <p>
 * When a shard leader receives an update for a document of another shard (or a
 * delete-by-query, which goes to every shard), it forwards it to that shard's
 * leader over Solr's internal client, HTTP/2 by default. Under concurrent
 * updates the forwarded request body stops arriving: the receiving request
 * threads park forever in {@code HttpInput.read}, the Jetty task that would
 * deliver the body waits for the {@code HttpInput} lock, and the update never
 * finishes (seen in production on Solr 10.0.0 with one node and four shards per
 * collection, until Solr was restarted). RODA's updates are forwarded often:
 * deletes by query fan out to every shard, and SolrJ routes documents by
 * {@code id} while the unique key is {@code uuid}, so documents whose
 * {@code id} differs from their {@code uuid} (files, representations, log
 * entries) and every atomic update go to the wrong leader first (#3792).
 * <p>
 * This test does not switch Solr to HTTP/1.1. It measures whether RODA's own
 * use of Solr still triggers the deadlock, so it can be run after each fix of
 * the Solr review (#3782) to see whether the problem went away. It starts its
 * own Solr (default settings, so HTTP/2 between shards), points RODA at it with
 * every collection on {@code solrreview.stall.shards} shards, and runs RODA
 * operations from several threads, each repeating what a busy repository does:
 * create an AIP (indexes the AIP, its representations, files and events), write
 * audit log entries, update the AIP (reindex in place) and delete it. An
 * operation running longer than {@code solrreview.stall.stallSeconds} is taken
 * as a deadlock: a thread dump and the Solr logs are saved to
 * {@code solrreview.outputDir} and the test fails. It passes if the load runs
 * for {@code solrreview.stall.durationSeconds} without one. The deadlock does
 * not happen at a fixed point, so a pass is a probability, not a proof: the
 * longer the run, the stronger it is.
 * <p>
 * It has a group of its own, so it runs alone and the operations it abandons
 * on a deadlock do not affect other tests:
 *
 * <pre>
 * mvn -pl roda-core/roda-core-tests test -Dtestng.groups=solr-review-stall
 * </pre>
 *
 * Parameters (system properties, defaults in brackets):
 * {@code solrreview.stall.image} [solr:10.0.0], {@code solrreview.stall.heap}
 * [2g], {@code solrreview.stall.shards} [4], {@code solrreview.stall.threads}
 * [16], {@code solrreview.stall.durationSeconds} [300],
 * {@code solrreview.stall.stallSeconds} [60],
 * {@code solrreview.stall.logEntriesPerCycle} [5], {@code solrreview.outputDir}
 * [target/solr-review].
 */
@Test(groups = {SolrReview.STALL_GROUP})
public class Http2InterShardStallTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(Http2InterShardStallTest.class);

  private static final String SOLR_CLOUD_URLS_PROPERTY = "RODA_CORE_SOLR_CLOUD_URLS";
  private static final String SOLR_NUM_SHARDS_PROPERTY = "SOLR_NUM_SHARDS";
  // Solr's embedded ZooKeeper listens on the Solr port + 1000
  private static final int ZOOKEEPER_PORT_OFFSET = 1000;

  private static GenericContainer<?> solr;
  private static String previousSolrCloudUrls;
  private static String previousNumShards;
  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;
  private static StorageService corporaService;
  private static int shards;

  @BeforeClass
  public static void setUp() throws Exception {
    shards = Integer.getInteger("solrreview.stall.shards", 4);
    int port = findFreePort();
    int zookeeperPort = port + ZOOKEEPER_PORT_OFFSET;

    // a Solr of its own, with Solr's defaults (HTTP/2 between shards) and the heap of the reference deploy
    solr = new GenericContainer<>(
      DockerImageName.parse(System.getProperty("solrreview.stall.image", "solr:10.0.0")))
      .withExposedPorts(port, zookeeperPort).withEnv("SOLR_PORT_LISTEN", String.valueOf(port))
      .withEnv("SOLR_HOST_ADVERTISE", "localhost")
      .withEnv("SOLR_HEAP", System.getProperty("solrreview.stall.heap", "2g"))
      .waitingFor(Wait.forHttp("/solr/admin/info/system").forPort(port).forStatusCode(200)
        .withStartupTimeout(Duration.ofMinutes(3)));
    solr.setPortBindings(List.of(port + ":" + port, zookeeperPort + ":" + zookeeperPort));
    solr.start();

    // point RODA at it, with every collection created on several shards
    previousSolrCloudUrls = System.getProperty(SOLR_CLOUD_URLS_PROPERTY);
    previousNumShards = System.getProperty(SOLR_NUM_SHARDS_PROPERTY);
    System.setProperty(SOLR_CLOUD_URLS_PROPERTY, "localhost:" + zookeeperPort);
    System.setProperty(SOLR_NUM_SHARDS_PROPERTY, String.valueOf(shards));

    basePath = TestsHelper.createBaseTempDir(Http2InterShardStallTest.class, true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();
    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = false;
    boolean deployPluginManager = false;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false, ldapUtilityTestHelper.getLdapUtility());

    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();
    URL corporaURL = Http2InterShardStallTest.class.getResource("/corpora");
    corporaService = new FileStorageService(Paths.get(corporaURL.toURI()));
  }

  @AfterClass(alwaysRun = true)
  public static void tearDown() throws Exception {
    try {
      if (ldapUtilityTestHelper != null) {
        ldapUtilityTestHelper.shutdown();
      }
      RodaCoreFactory.shutdown();
      if (basePath != null) {
        FSUtils.deletePath(basePath);
      }
    } finally {
      restore(SOLR_CLOUD_URLS_PROPERTY, previousSolrCloudUrls);
      restore(SOLR_NUM_SHARDS_PROPERTY, previousNumShards);
      if (solr != null) {
        solr.stop();
      }
    }
  }

  @Test
  public void rodaLoadDoesNotDeadlockSolrBetweenShards() throws Exception {
    int threads = Integer.getInteger("solrreview.stall.threads", 16);
    int durationSeconds = Integer.getInteger("solrreview.stall.durationSeconds", 300);
    int stallSeconds = Integer.getInteger("solrreview.stall.stallSeconds", 60);
    int logEntriesPerCycle = Integer.getInteger("solrreview.stall.logEntriesPerCycle", 5);
    Path outputDir = Paths.get(System.getProperty("solrreview.outputDir", "target/solr-review"))
      .resolve("http2-stall-" + Instant.now().toString().replace(':', '-'));

    assertCollectionsAreSharded();

    // what each thread is doing and since when, for the watchdog
    Map<Integer, Operation> running = new ConcurrentHashMap<>();
    AtomicBoolean stop = new AtomicBoolean(false);
    AtomicLong operations = new AtomicLong();
    List<String> errors = Collections.synchronizedList(new ArrayList<>());
    long start = System.nanoTime();
    long deadline = start + TimeUnit.SECONDS.toNanos(durationSeconds);

    ExecutorService executor = Executors.newFixedThreadPool(threads);
    for (int t = 0; t < threads; t++) {
      int thread = t;
      executor.submit(() -> {
        while (!stop.get() && System.nanoTime() < deadline) {
          try {
            String aipId = IdUtils.createUUID();
            run(running, thread, "create AIP " + aipId, () -> model.createAIP(aipId, corporaService,
              DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
              RodaConstants.ADMIN));
            for (int i = 0; i < logEntriesPerCycle; i++) {
              run(running, thread, "add log entry", () -> model.addLogEntry(logEntry(aipId),
                RodaCoreFactory.getLogPath()));
            }
            run(running, thread, "update AIP " + aipId, () -> model.notifyAipUpdated(aipId));
            run(running, thread, "delete AIP " + aipId, () -> model.deleteAIP(aipId));
            operations.addAndGet(3L + logEntriesPerCycle);
          } catch (Exception e) {
            if (!stop.get()) {
              errors.add(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
          }
        }
      });
    }
    executor.shutdown();

    // watchdog: an operation running for longer than stallSeconds is a deadlock
    Operation stalled = null;
    long lastProgress = System.nanoTime();
    while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
      for (Operation operation : running.values()) {
        if (operation.seconds() >= stallSeconds) {
          stalled = operation;
          break;
        }
      }
      if (stalled != null) {
        break;
      }
      if (System.nanoTime() - lastProgress > TimeUnit.SECONDS.toNanos(30)) {
        LOGGER.info("{} RODA operations so far, {} errors", operations.get(), errors.size());
        lastProgress = System.nanoTime();
      }
    }

    String summary = String.format(
      "%d shards per collection, %d threads, %d RODA operations in %d s, %d errors%s", shards, threads,
      operations.get(), TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start), errors.size(),
      stalled == null ? "" : ", deadlock: '" + stalled.name + "' running for " + stalled.seconds() + " s");
    LOGGER.info(summary);
    if (stalled != null) {
      stop.set(true);
      saveDiagnostics(outputDir, summary, errors);
      executor.shutdownNow();
      Assert.fail("Solr deadlocked between shards: " + summary + "; diagnostics in " + outputDir.toAbsolutePath());
    }
    Assert.assertTrue(errors.isEmpty(), "RODA operations failed: " + summary + ": " + errors);
  }

  private static void run(Map<Integer, Operation> running, int thread, String name, RodaCall call)
    throws Exception {
    running.put(thread, new Operation(name, System.nanoTime()));
    try {
      call.run();
    } finally {
      running.remove(thread);
    }
  }

  private static LogEntry logEntry(String aipId) {
    LogEntry entry = new LogEntry();
    entry.setUUID(IdUtils.createUUID());
    entry.setAddress("127.0.0.1");
    entry.setUsername(RodaConstants.ADMIN);
    entry.setActionComponent(Http2InterShardStallTest.class.getSimpleName());
    entry.setActionMethod("load");
    entry.setParameters(Collections.emptyList());
    entry.setDuration(1);
    entry.setDatetime(new Date());
    entry.setRelatedObjectID(aipId);
    entry.setState(LogEntryState.SUCCESS);
    return entry;
  }

  private void assertCollectionsAreSharded() throws Exception {
    CloudSolrClient client = (CloudSolrClient) RodaCoreFactory.getSolr();
    for (String collection : List.of(RodaConstants.INDEX_FILE, RodaConstants.INDEX_REPRESENTATION,
      RodaConstants.INDEX_ACTION_LOG)) {
      DocCollection state = client.getClusterStateProvider().getCollection(collection);
      Assert.assertEquals(state.getSlices().size(), shards, "shards of " + collection);
    }
  }

  private static void saveDiagnostics(Path outputDir, String summary, List<String> errors) {
    try {
      Files.createDirectories(outputDir);
      Files.writeString(outputDir.resolve("summary.txt"), summary + "\n" + String.join("\n", errors) + "\n",
        StandardCharsets.UTF_8);
      // SIGQUIT makes the JVM print a thread dump to stdout (the container log); it does not stop Solr
      solr.execInContainer("bash", "-c", "kill -3 $(pgrep -f start.jar)");
      Thread.sleep(3000);
      Files.writeString(outputDir.resolve("container.log"), solr.getLogs(), StandardCharsets.UTF_8);
      ExecResult log = solr.execInContainer("bash", "-c", "cat /var/solr/logs/solr.log");
      Files.writeString(outputDir.resolve("solr.log"), log.getStdout(), StandardCharsets.UTF_8);
      LOGGER.error("Thread dump and Solr logs saved to {}", outputDir.toAbsolutePath());
    } catch (IOException | UnsupportedOperationException e) {
      LOGGER.warn("Could not save the Solr diagnostics", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private static void restore(String property, String value) {
    if (value == null) {
      System.clearProperty(property);
    } else {
      System.setProperty(property, value);
    }
  }

  private static int findFreePort() throws IOException {
    // the ZooKeeper port (port + 1000) must be free too
    for (int attempt = 0; attempt < 20; attempt++) {
      int port;
      try (ServerSocket socket = new ServerSocket(0)) {
        port = socket.getLocalPort();
      }
      if (port + ZOOKEEPER_PORT_OFFSET < 65536) {
        try (ServerSocket zookeeper = new ServerSocket(port + ZOOKEEPER_PORT_OFFSET)) {
          return port;
        } catch (IOException e) {
          // taken, try another
        }
      }
    }
    throw new IOException("No free pair of ports for Solr and its ZooKeeper");
  }

  @FunctionalInterface
  private interface RodaCall {
    void run() throws Exception;
  }

  private record Operation(String name, long startNanos) {
    long seconds() {
      return TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startNanos);
    }
  }
}
