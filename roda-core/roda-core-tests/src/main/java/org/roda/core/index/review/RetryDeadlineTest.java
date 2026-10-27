/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.review;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.apache.commons.configuration2.Configuration;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IndexResultIterator;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.index.utils.RetryPolicyBuilder;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.testcontainers.DockerClientFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * #3785: a failed Solr call must give up within a deadline instead of holding
 * its worker (and the locks and transaction of the object it indexes) for
 * minutes; an outage shorter than the deadline must not fail the calls, and
 * while Solr is down the workers must not each hammer it with their retries;
 * requests that cannot succeed (a client error, an invalid query) must not be
 * retried, and a Solr internal error, that may be caused by the request itself,
 * only a few times.
 *
 * Solr is made to hang by pausing its container, as a Solr that stopped
 * answering: connections are accepted and requests never answered. Solr is made
 * to be down by a client that fails as the cloud client of RODA does when Solr
 * refuses connections (a {@link CloudSolrClient.RouteException}). The retry
 * settings are those of production, with a shorter deadline, not the short ones
 * of the test node. Each call runs with a time limit, so a test fails at the
 * limit instead of hanging.
 */
@Test(groups = {SolrReview.GROUP})
public class RetryDeadlineTest {
  // how long a single call may take while Solr hangs: the overall deadline below,
  // plus one attempt
  private static final long CALL_LIMIT_SECONDS = 45;
  private static final int CONCURRENT_WRITERS = 8;
  private static final int CIRCUIT_BREAKER_FAILURES = 10;
  // the production retry settings (3 retries of an internal error), with a
  // shorter deadline and circuit breaker delay
  private static final Map<String, Object> RETRY_SETTINGS = Map.of(RodaConstants.SOLR_RETRY_DELAY, 1,
    RodaConstants.SOLR_RETRY_MAX_DELAY, 15, RodaConstants.SOLR_RETRY_DELAY_FACTOR, 2.0,
    RodaConstants.SOLR_RETRY_MAX_DURATION, 20, RodaConstants.SOLR_CIRCUIT_BREAKER_FAILURES, CIRCUIT_BREAKER_FAILURES,
    RodaConstants.SOLR_CIRCUIT_BREAKER_DELAY, 5);
  private static final int SERVER_ERROR_MAX_RETRIES = 3;

  private static ShardedSolr solr;
  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;
  private static ExecutorService executor;

  @BeforeClass
  public static void setUp() throws Exception {
    // a Solr of its own, as it gets paused; requests idle for 10 s are given up
    // (system properties RODA reads)
    solr = ShardedSolr.start(Map.of("SOLR_NUM_SHARDS", "1", "RODA_CORE_SOLR_CLIENT_IDLE_TIMEOUT_MS", "10000",
      "RODA_CORE_SOLR_CLIENT_CONNECTION_TIMEOUT_MS", "5000"));
    basePath = TestsHelper.createBaseTempDir(RetryDeadlineTest.class, true);
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

    Configuration configuration = RodaCoreFactory.getRodaConfiguration();
    RETRY_SETTINGS.forEach(configuration::setProperty);
    resetRetryPolicy();
    executor = Executors.newCachedThreadPool();
  }

  @AfterClass(alwaysRun = true)
  public static void tearDown() throws Exception {
    try {
      if (executor != null) {
        executor.shutdownNow();
      }
      if (RodaCoreFactory.getRodaConfiguration() != null) {
        RETRY_SETTINGS.keySet().forEach(RodaCoreFactory.getRodaConfiguration()::clearProperty);
      }
      resetRetryPolicy();
      if (ldapUtilityTestHelper != null) {
        ldapUtilityTestHelper.shutdown();
      }
      RodaCoreFactory.shutdown();
      if (basePath != null) {
        FSUtils.deletePath(basePath);
      }
    } finally {
      if (solr != null) {
        solr.close();
      }
    }
  }

  /** The retry policy is built once, from the configuration at that time. */
  private static void resetRetryPolicy() throws ReflectiveOperationException {
    Field instance = RetryPolicyBuilder.class.getDeclaredField("instance");
    instance.setAccessible(true);
    instance.set(null, null);
  }

  /** Runs the call with these settings on top of those of the class. */
  private static <T> T withSettings(Map<String, Object> settings, Callable<T> call) throws Exception {
    Configuration configuration = RodaCoreFactory.getRodaConfiguration();
    settings.forEach(configuration::setProperty);
    resetRetryPolicy();
    try {
      return call.call();
    } finally {
      settings.keySet().forEach(configuration::clearProperty);
      RETRY_SETTINGS.forEach(configuration::setProperty);
      resetRetryPolicy();
    }
  }

  private static void pauseSolr() {
    DockerClientFactory.instance().client().pauseContainerCmd(solr.container().getContainerId()).exec();
  }

  private static void unpauseSolr() {
    DockerClientFactory.instance().client().unpauseContainerCmd(solr.container().getContainerId()).exec();
  }

  private static ReturnWithExceptions<Void, Object> write() {
    return SolrUtils.create(index.getSolrClient(), model, LogEntry.class, logEntry(), new Object());
  }

  private static <T> T within(Callable<T> call, long seconds, String what) throws Exception {
    Future<T> future = executor.submit(call);
    try {
      return future.get(seconds, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      future.cancel(true);
      Assert.fail(what + " did not end within " + seconds + " s");
      return null;
    }
  }

  @Test(priority = 1)
  public void writeGivesUpWithinDeadlineWhenSolrHangs() throws Exception {
    pauseSolr();
    try {
      ReturnWithExceptions<Void, Object> ret = within(RetryDeadlineTest::write, CALL_LIMIT_SECONDS,
        "a write while Solr hangs");
      Assert.assertFalse(ret.isEmpty(), "the write fails");
    } finally {
      unpauseSolr();
    }
  }

  @Test(priority = 2)
  public void concurrentWritesEndWithinDeadlineWhileSolrHangs() throws Exception {
    pauseSolr();
    try {
      List<Callable<ReturnWithExceptions<Void, Object>>> writers = new ArrayList<>();
      for (int i = 0; i < CONCURRENT_WRITERS; i++) {
        writers.add(RetryDeadlineTest::write);
      }
      // all of them together within the time of a single call
      List<ReturnWithExceptions<Void, Object>> results = within(() -> {
        List<ReturnWithExceptions<Void, Object>> all = new ArrayList<>();
        for (Future<ReturnWithExceptions<Void, Object>> future : executor.invokeAll(writers)) {
          all.add(future.get());
        }
        return all;
      }, CALL_LIMIT_SECONDS, CONCURRENT_WRITERS + " concurrent writes while Solr hangs");
      for (ReturnWithExceptions<Void, Object> ret : results) {
        Assert.assertFalse(ret.isEmpty(), "every write fails");
      }
    } finally {
      unpauseSolr();
    }
  }

  @Test(priority = 3)
  public void writesRecoverAfterSolrIsBack() throws Exception {
    // once Solr answers again (and a circuit breaker, if open, lets a call
    // through), writes succeed
    long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(90);
    ReturnWithExceptions<Void, Object> ret;
    do {
      ret = within(RetryDeadlineTest::write, CALL_LIMIT_SECONDS, "a write after Solr is back");
      if (!ret.isEmpty()) {
        Thread.sleep(2000);
      }
    } while (!ret.isEmpty() && System.currentTimeMillis() < deadline);
    Assert.assertTrue(ret.isEmpty(), "a write succeeds once Solr is back: " + ret.getExceptions());
  }

  @Test(priority = 4)
  public void clientErrorIsNotRetried() throws Exception {
    // a request Solr rejects (4xx) fails the same way every time, however the
    // client reports it
    SolrException rejected = new SolrException(SolrException.ErrorCode.BAD_REQUEST, "rejected");
    for (Exception failure : List.of(new SolrServerException(rejected), routeException(rejected))) {
      resetRetryPolicy();
      FailingSolrClient rejecting = new FailingSolrClient(() -> failure);
      SolrInputDocument document = new SolrInputDocument();
      document.addField(RodaConstants.INDEX_UUID, IdUtils.createUUID());

      ReturnWithExceptions<Void, Object> ret = within(
        () -> SolrUtils.create(rejecting, RodaConstants.INDEX_ACTION_LOG, document, new Object()), 30,
        "a write Solr rejects");
      Assert.assertFalse(ret.isEmpty(), "the write fails");
      Assert.assertEquals(rejecting.failed(), 1, "a rejected request is sent once: " + failure);
    }
  }

  @Test(priority = 7)
  public void writeIsRetriedWhenSolrRefusesConnections() throws Exception {
    resetRetryPolicy();
    // the cloud client reports a Solr that refuses connections as a route exception
    AtomicInteger attempts = new AtomicInteger();
    FailingSolrClient down = new FailingSolrClient(() -> attempts.incrementAndGet() <= 2 ? refused() : null);

    ReturnWithExceptions<Void, Object> ret = within(
      () -> SolrUtils.create(down, model, LogEntry.class, logEntry(), new Object()), CALL_LIMIT_SECONDS,
      "a write while Solr refuses connections");

    Assert.assertTrue(ret.isEmpty(), "the write succeeds once Solr is back: " + ret.getExceptions());
    Assert.assertEquals(down.failed(), 2);
  }

  @Test(priority = 8)
  public void outageShorterThanDeadlineDoesNotFailWrites() throws Exception {
    resetRetryPolicy();
    // Solr refuses connections for 4 s, less than the deadline: the writes wait for
    // it instead of failing, and the
    // circuit breaker keeps them from each retrying against it
    long back = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(4);
    FailingSolrClient down = new FailingSolrClient(() -> System.currentTimeMillis() < back ? refused() : null);
    List<Callable<ReturnWithExceptions<Void, Object>>> writers = new ArrayList<>();
    for (int i = 0; i < CONCURRENT_WRITERS; i++) {
      writers.add(() -> SolrUtils.create(down, model, LogEntry.class, logEntry(), new Object()));
    }

    List<ReturnWithExceptions<Void, Object>> results = within(() -> {
      List<ReturnWithExceptions<Void, Object>> all = new ArrayList<>();
      for (Future<ReturnWithExceptions<Void, Object>> future : executor.invokeAll(writers)) {
        all.add(future.get());
      }
      return all;
    }, CALL_LIMIT_SECONDS, CONCURRENT_WRITERS + " concurrent writes during a short outage");

    for (ReturnWithExceptions<Void, Object> ret : results) {
      Assert.assertTrue(ret.isEmpty(), "every write succeeds once Solr is back: " + ret.getExceptions());
    }
    // the failures that open the circuit breaker, the requests the other writers
    // had sent by then, and one request
    // each time it lets a call through (5 s apart); without it each writer would
    // try 3 times
    Assert.assertTrue(down.failed() <= CIRCUIT_BREAKER_FAILURES + CONCURRENT_WRITERS + 2,
      "requests sent to Solr while it is down: " + down.failed());
  }

  @Test(priority = 9)
  public void halfOpenCircuitBreakerLetsOneCallThrough() throws Exception {
    // internal errors without retries, so each call is a single request
    withSettings(Map.of(RodaConstants.SOLR_RETRY_SERVER_ERROR_MAX_RETRIES, 0), () -> {
      FailingSolrClient failing = new FailingSolrClient(RetryDeadlineTest::internalError);
      Callable<ReturnWithExceptions<Void, Object>> write = () -> SolrUtils.create(failing, model, LogEntry.class,
        logEntry(), new Object());
      for (int i = 0; i < CIRCUIT_BREAKER_FAILURES; i++) {
        write.call();
      }
      // once its delay has passed, of the writes that start together one is sent to
      // Solr; the others wait for the
      // circuit breaker, within their deadline
      Thread.sleep(TimeUnit.SECONDS.toMillis(6));
      List<Callable<ReturnWithExceptions<Void, Object>>> writers = Collections.nCopies(CONCURRENT_WRITERS, write);
      Future<?> writes = executor.submit(() -> executor.invokeAll(writers));
      Thread.sleep(TimeUnit.SECONDS.toMillis(2));
      Assert.assertEquals(failing.failed(), CIRCUIT_BREAKER_FAILURES + 1);
      writes.get(CALL_LIMIT_SECONDS, TimeUnit.SECONDS);
      return null;
    });
  }

  @Test(priority = 10)
  public void internalErrorIsRetriedAFewTimes() throws Exception {
    // an internal error may be caused by the request itself, so it is not retried
    // until the deadline
    SolrException internal = internalError();
    for (Exception failure : List.of(internal, routeException(internal))) {
      resetRetryPolicy();
      FailingSolrClient failing = new FailingSolrClient(() -> failure);

      ReturnWithExceptions<Void, Object> ret = within(
        () -> SolrUtils.create(failing, model, LogEntry.class, logEntry(), new Object()), CALL_LIMIT_SECONDS,
        "a write Solr fails with an internal error");

      Assert.assertFalse(ret.isEmpty(), "the write fails");
      Assert.assertEquals(failing.failed(), 1 + SERVER_ERROR_MAX_RETRIES, "requests sent: " + failure);
    }
  }

  @Test(priority = 11)
  public void outageShorterThanDeadlineIsWaitedForHoweverManyAttempts() throws Exception {
    // the number of retries (here set by the old setting) limits internal errors
    // only: a Solr that is unavailable is
    // waited for until the deadline, also while the circuit breaker turns the
    // attempts away
    withSettings(Map.of(RodaConstants.SOLR_RETRY_MAX_RETRIES, 1), () -> {
      long back = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(4);
      FailingSolrClient down = new FailingSolrClient(() -> System.currentTimeMillis() < back ? refused() : null);
      List<Callable<ReturnWithExceptions<Void, Object>>> writers = Collections.nCopies(CONCURRENT_WRITERS,
        () -> SolrUtils.create(down, model, LogEntry.class, logEntry(), new Object()));

      List<ReturnWithExceptions<Void, Object>> results = within(() -> {
        List<ReturnWithExceptions<Void, Object>> all = new ArrayList<>();
        for (Future<ReturnWithExceptions<Void, Object>> future : executor.invokeAll(writers)) {
          all.add(future.get());
        }
        return all;
      }, CALL_LIMIT_SECONDS, CONCURRENT_WRITERS + " concurrent writes during a short outage");

      for (ReturnWithExceptions<Void, Object> ret : results) {
        Assert.assertTrue(ret.isEmpty(), "every write succeeds once Solr is back: " + ret.getExceptions());
      }
      return null;
    });
  }

  @Test(priority = 12)
  public void tooManyRequestsIsRetried() throws Exception {
    // Solr limiting its requests is a matter of load, not of the request, however
    // the client reports it
    SolrException tooMany = new SolrException(SolrException.ErrorCode.TOO_MANY_REQUESTS, "too many requests");
    for (Exception failure : List.of(tooMany, routeException(tooMany))) {
      resetRetryPolicy();
      AtomicInteger attempts = new AtomicInteger();
      FailingSolrClient limiting = new FailingSolrClient(() -> attempts.incrementAndGet() <= 2 ? failure : null);

      ReturnWithExceptions<Void, Object> ret = within(
        () -> SolrUtils.create(limiting, model, LogEntry.class, logEntry(), new Object()), CALL_LIMIT_SECONDS,
        "a write while Solr limits its requests");

      Assert.assertTrue(ret.isEmpty(), "the write succeeds once Solr accepts it: " + ret.getExceptions());
      Assert.assertEquals(limiting.failed(), 2);
    }
  }

  @Test(priority = 13)
  public void oldMaxRetriesSettingLimitsInternalErrorsUnlessTheNewOneIsSet() throws Exception {
    withSettings(Map.of(RodaConstants.SOLR_RETRY_MAX_RETRIES, 1), () -> {
      Assert.assertEquals(internalErrorRequests(), 2, "requests sent with the old setting");
      return null;
    });
    withSettings(Map.of(RodaConstants.SOLR_RETRY_MAX_RETRIES, 1, RodaConstants.SOLR_RETRY_SERVER_ERROR_MAX_RETRIES, 2),
      () -> {
        Assert.assertEquals(internalErrorRequests(), 3, "requests sent with both settings");
        return null;
      });
  }

  /**
   * The requests a write sends to a Solr that always fails with an internal
   * error.
   */
  private static int internalErrorRequests() throws Exception {
    FailingSolrClient failing = new FailingSolrClient(RetryDeadlineTest::internalError);
    within(() -> SolrUtils.create(failing, model, LogEntry.class, logEntry(), new Object()), CALL_LIMIT_SECONDS,
      "a write Solr fails with an internal error");
    return failing.failed();
  }

  /**
   * Fails the requests for which the supplier gives an exception, counting them,
   * and lets the others through to the Solr of the test.
   */
  private static class FailingSolrClient extends SolrClient {
    private final transient Supplier<Exception> failure;
    private final AtomicInteger failed = new AtomicInteger();

    FailingSolrClient(Supplier<Exception> failure) {
      this.failure = failure;
    }

    @Override
    public NamedList<Object> request(SolrRequest<?> request, String collection)
      throws SolrServerException, IOException {
      Exception e = failure.get();
      if (e instanceof SolrServerException solrServerException) {
        failed.incrementAndGet();
        throw solrServerException;
      } else if (e instanceof RuntimeException runtimeException) {
        failed.incrementAndGet();
        throw runtimeException;
      }
      return index.getSolrClient().request(request, collection);
    }

    int failed() {
      return failed.get();
    }

    @Override
    public void close() {
      // the Solr of the test is closed with it
    }
  }

  private static CloudSolrClient.RouteException routeException(Throwable cause) {
    NamedList<Throwable> throwables = new NamedList<>();
    throwables.add("http://solr:8983/solr/" + RodaConstants.INDEX_ACTION_LOG, cause);
    SolrException.ErrorCode code = cause instanceof SolrException solrException
      ? SolrException.ErrorCode.getErrorCode(solrException.code())
      : SolrException.ErrorCode.SERVER_ERROR;
    return new CloudSolrClient.RouteException(code, throwables, Collections.emptyMap());
  }

  private static SolrException internalError() {
    return new SolrException(SolrException.ErrorCode.SERVER_ERROR, "internal error");
  }

  /** As the cloud client of RODA reports a Solr that refuses connections. */
  private static CloudSolrClient.RouteException refused() {
    return routeException(new SolrServerException("No live SolrServers available to handle this request",
      new ConnectException("Connection refused")));
  }

  @Test(priority = 5)
  public void invalidPageRequestIsNotRetried() throws Exception {
    // a filter that can never be turned into a query (a field without a name)
    Filter invalid = new Filter(new SimpleFilterParameter("", "x"));
    Exception failure = within(() -> {
      try (IterableIndexResult<IndexedAIP> result = index.findAll(IndexedAIP.class, invalid,
        Collections.singletonList(RodaConstants.INDEX_UUID))) {
        result.forEach(aip -> {
          // nothing to do
        });
        return null;
      } catch (Exception e) {
        return e;
      }
    }, 10, "listing with an invalid filter");
    Assert.assertNotNull(failure, "listing with an invalid filter fails");
  }

  @Test(priority = 6)
  public void listingGivesUpWithinDeadlineWhenSolrHangs() throws Exception {
    // a page is retried for at most retries x sleep, however long each attempt
    // hangs. Solr is not paused,
    // as the ZooKeeper of the tests runs in its container: a client that cannot
    // reach ZooKeeper hangs refreshing the
    // cluster state, which is not what is tested here
    SolrClient hanging = new SolrClient() {
      @Override
      public NamedList<Object> request(SolrRequest<?> request, String collection) throws SolrServerException {
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(15));
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        throw new SolrServerException(new IOException("Idle timeout expired"));
      }

      @Override
      public void close() {
        // nothing to close
      }
    };
    Exception failure = within(() -> {
      try {
        new IndexResultIterator<>(hanging, IndexedAIP.class, Filter.ALL, null, false,
          Collections.singletonList(RodaConstants.INDEX_UUID));
        return null;
      } catch (NoSuchElementException e) {
        return e;
      }
    }, TimeUnit.MILLISECONDS
      .toSeconds((long) IndexResultIterator.DEFAULT_RETRIES * IndexResultIterator.DEFAULT_SLEEP_BETWEEN_RETRIES) + 30,
      "a listing while Solr hangs");
    Assert.assertNotNull(failure, "the listing fails");
  }

  private static LogEntry logEntry() {
    LogEntry entry = new LogEntry();
    entry.setUUID(IdUtils.createUUID());
    entry.setAddress("127.0.0.1");
    entry.setUsername(RodaConstants.ADMIN);
    entry.setActionComponent(RetryDeadlineTest.class.getSimpleName());
    entry.setActionMethod("write");
    entry.setParameters(Collections.emptyList());
    entry.setDuration(1);
    entry.setDatetime(new Date());
    entry.setState(LogEntryState.SUCCESS);
    return entry;
  }
}
