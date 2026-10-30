/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.apache.commons.configuration2.Configuration;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.index.utils.RetryPolicyBuilder;
import org.roda.core.index.utils.SolrDocumentBuffer;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * #3773: documents are sent to Solr a batch per collection at a time, one
 * request for every {@code core.solr.index.batch.size} of them, or about
 * {@code core.solr.index.batch.maxBytes} of their text, and one for the rest
 * when the buffer is flushed; a batch size of one sends each document on its
 * own, as before. A failed request stops the buffer; when Solr rejects one
 * document of a request, the others are still added.
 *
 * The documents are log entries, sent through a client that records the number
 * of documents of each request that adds them, and lets the requests through to
 * the Solr of the tests or fails them. Internal errors are not retried here.
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class SolrDocumentBufferTest {
  private static final Map<String, Object> RETRY_SETTINGS = Map.of(RodaConstants.SOLR_RETRY_SERVER_ERROR_MAX_RETRIES, 0,
    RodaConstants.SOLR_CIRCUIT_BREAKER_FAILURES, 0);

  private static Path basePath;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;

  @BeforeClass
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(SolrDocumentBufferTest.class, true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = false;
    boolean deployPluginManager = false;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false, ldapUtilityTestHelper.getLdapUtility());

    index = RodaCoreFactory.getIndexService();

    Configuration configuration = RodaCoreFactory.getRodaConfiguration();
    RETRY_SETTINGS.forEach(configuration::setProperty);
    resetRetryPolicy();
  }

  @AfterClass(alwaysRun = true)
  public static void tearDown() throws Exception {
    if (RodaCoreFactory.getRodaConfiguration() != null) {
      RETRY_SETTINGS.keySet().forEach(RodaCoreFactory.getRodaConfiguration()::clearProperty);
    }
    resetRetryPolicy();
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  /** The retry policy is built once, from the configuration at that time. */
  private static void resetRetryPolicy() throws ReflectiveOperationException {
    Field instance = RetryPolicyBuilder.class.getDeclaredField("instance");
    instance.setAccessible(true);
    instance.set(null, null);
  }

  @AfterMethod
  public void cleanUp() {
    RodaCoreFactory.getRodaConfiguration().clearProperty(RodaConstants.SOLR_INDEX_BATCH_SIZE);
    RodaCoreFactory.getRodaConfiguration().clearProperty(RodaConstants.SOLR_INDEX_BATCH_MAX_BYTES);
    IndexTestUtils.resetIndex();
  }

  private static String collection() throws RODAException {
    return SolrCollectionRegistry.getIndexName(LogEntry.class);
  }

  private static List<SolrInputDocument> logEntries(String component, int count) throws RODAException {
    return logEntries(component, count, "add");
  }

  private static List<SolrInputDocument> logEntries(String component, int count, String method)
    throws RODAException {
    List<SolrInputDocument> documents = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      LogEntry entry = new LogEntry();
      entry.setUUID(IdUtils.createUUID());
      entry.setAddress("127.0.0.1");
      entry.setUsername(RodaConstants.ADMIN);
      entry.setActionComponent(component);
      entry.setActionMethod(method);
      entry.setParameters(Collections.emptyList());
      entry.setDuration(1);
      entry.setDatetime(new Date());
      entry.setState(LogEntryState.SUCCESS);
      documents.add(SolrCollectionRegistry.toSolrDocument(LogEntry.class, RodaCoreFactory.getModelService(), entry));
    }
    return documents;
  }

  private static ReturnWithExceptions<Void, Object> add(SolrDocumentBuffer<Object> buffer,
    List<SolrInputDocument> documents) throws RODAException {
    ReturnWithExceptions<Void, Object> ret = new ReturnWithExceptions<>();
    for (SolrInputDocument document : documents) {
      buffer.add(collection(), document).addTo(ret);
    }
    return ret;
  }

  private static long indexed(String component) throws RODAException {
    SolrUtils.commit(index.getSolrClient(), LogEntry.class);
    return index.count(LogEntry.class,
      new Filter(new SimpleFilterParameter(RodaConstants.LOG_ACTION_COMPONENT, component)));
  }

  @Test
  public void documentsAreSentWhenABatchIsFullAndTheRestWhenFlushed() throws RODAException {
    RodaCoreFactory.getRodaConfiguration().setProperty(RodaConstants.SOLR_INDEX_BATCH_SIZE, 10);
    RecordingSolrClient solr = new RecordingSolrClient(request -> false, null);
    AtomicInteger requests = new AtomicInteger();
    SolrDocumentBuffer<Object> buffer = new SolrDocumentBuffer<>(solr, new Object(), requests::incrementAndGet);
    String component = IdUtils.createUUID();

    Assert.assertTrue(add(buffer, logEntries(component, 25)).isEmpty());
    Assert.assertEquals(solr.documentsByRequest(), List.of(10, 10), "full batches");

    Assert.assertTrue(buffer.flush().isEmpty());
    Assert.assertEquals(solr.documentsByRequest(), List.of(10, 10, 5), "the rest, when flushed");
    Assert.assertEquals(requests.get(), 3, "the callback runs after each request");
    Assert.assertEquals(indexed(component), 25L);
  }

  @Test
  public void aBatchSizeOfOneSendsEachDocumentOnItsOwn() throws RODAException {
    RodaCoreFactory.getRodaConfiguration().setProperty(RodaConstants.SOLR_INDEX_BATCH_SIZE, 1);
    RecordingSolrClient solr = new RecordingSolrClient(request -> false, null);
    SolrDocumentBuffer<Object> buffer = new SolrDocumentBuffer<>(solr, new Object());
    String component = IdUtils.createUUID();

    for (SolrInputDocument document : logEntries(component, 3)) {
      Assert.assertTrue(buffer.add(collection(), document).isEmpty());
    }
    Assert.assertEquals(solr.documentsByRequest(), List.of(1, 1, 1), "sent as soon as added");
    Assert.assertTrue(buffer.flush().isEmpty());

    Assert.assertEquals(solr.documentsByRequest(), List.of(1, 1, 1));
    Assert.assertEquals(indexed(component), 3L);
  }

  @Test
  public void documentsLargerThanTheMaximumSizeAreSentOnTheirOwn() throws RODAException {
    RodaCoreFactory.getRodaConfiguration().setProperty(RodaConstants.SOLR_INDEX_BATCH_MAX_BYTES, 1000);
    RecordingSolrClient solr = new RecordingSolrClient(request -> false, null);
    SolrDocumentBuffer<Object> buffer = new SolrDocumentBuffer<>(solr, new Object());
    String component = IdUtils.createUUID();

    Assert.assertTrue(add(buffer, logEntries(component, 3, "a".repeat(2000))).isEmpty());
    Assert.assertTrue(buffer.flush().isEmpty());

    Assert.assertEquals(solr.documentsByRequest(), List.of(1, 1, 1));
    Assert.assertEquals(indexed(component), 3L);
  }

  @Test
  public void documentsAreSentWhenTheirSizeReachesTheMaximum() throws RODAException {
    RodaCoreFactory.getRodaConfiguration().setProperty(RodaConstants.SOLR_INDEX_BATCH_MAX_BYTES, 4000);
    RecordingSolrClient solr = new RecordingSolrClient(request -> false, null);
    SolrDocumentBuffer<Object> buffer = new SolrDocumentBuffer<>(solr, new Object());
    String component = IdUtils.createUUID();

    // over 1000 characters each, with their other fields: the fourth reaches the
    // maximum, whatever the size of the other fields
    Assert.assertTrue(add(buffer, logEntries(component, 7, "a".repeat(1000))).isEmpty());
    Assert.assertEquals(solr.documentsByRequest(), List.of(4), "sent when the maximum size is reached");
    Assert.assertTrue(buffer.flush().isEmpty());

    Assert.assertEquals(solr.documentsByRequest(), List.of(4, 3));
    Assert.assertEquals(indexed(component), 7L);
  }

  @Test
  public void aFailedRequestStopsTheBuffer() throws RODAException {
    RodaCoreFactory.getRodaConfiguration().setProperty(RodaConstants.SOLR_INDEX_BATCH_SIZE, 10);
    RecordingSolrClient solr = new RecordingSolrClient(request -> true, SolrException.ErrorCode.SERVER_ERROR);
    SolrDocumentBuffer<Object> buffer = new SolrDocumentBuffer<>(solr, new Object());
    String component = IdUtils.createUUID();

    Assert.assertFalse(add(buffer, logEntries(component, 15)).isEmpty(), "the first batch fails");
    Assert.assertTrue(buffer.flush().isEmpty(), "the rest is not sent");

    Assert.assertEquals(solr.documentsByRequest(), List.of(10));
    Assert.assertEquals(indexed(component), 0L);
  }

  @Test
  public void aDocumentSolrRejectsIsIsolatedFromTheOthersOfItsRequest() throws RODAException {
    String component = IdUtils.createUUID();
    List<SolrInputDocument> documents = logEntries(component, 5);
    Object rejected = documents.get(2).getFieldValue(RodaConstants.INDEX_UUID);
    RecordingSolrClient solr = new RecordingSolrClient(
      request -> request.getDocuments().stream()
        .anyMatch(document -> rejected.equals(document.getFieldValue(RodaConstants.INDEX_UUID))),
      SolrException.ErrorCode.BAD_REQUEST);

    ReturnWithExceptions<Void, Object> ret = SolrUtils.create(solr, collection(), documents, new Object());

    Assert.assertEquals(ret.getExceptions().size(), 1, "only the rejected document fails");
    Assert.assertEquals(solr.documentsByRequest(), List.of(5, 1, 1, 1, 1, 1), "then sent one by one");
    Assert.assertEquals(indexed(component), 4L);
  }

  /**
   * Records the number of documents of each request that adds documents, and
   * fails, with a Solr error, those the predicate selects; lets the others
   * through to the Solr of the tests.
   */
  private static class RecordingSolrClient extends SolrClient {
    private final transient Predicate<UpdateRequest> fails;
    private final SolrException.ErrorCode errorCode;
    private final transient List<Integer> documentsByRequest = Collections.synchronizedList(new ArrayList<>());

    RecordingSolrClient(Predicate<UpdateRequest> fails, SolrException.ErrorCode errorCode) {
      this.fails = fails;
      this.errorCode = errorCode;
    }

    @Override
    public NamedList<Object> request(SolrRequest<?> request, String collection)
      throws SolrServerException, IOException {
      if (request instanceof UpdateRequest update && update.getDocuments() != null) {
        documentsByRequest.add(update.getDocuments().size());
        if (fails.test(update)) {
          throw new SolrException(errorCode, "Solr failed");
        }
      }
      return index.getSolrClient().request(request, collection);
    }

    List<Integer> documentsByRequest() {
      return new ArrayList<>(documentsByRequest);
    }

    @Override
    public void close() {
      // the Solr of the tests is closed with them
    }
  }
}
