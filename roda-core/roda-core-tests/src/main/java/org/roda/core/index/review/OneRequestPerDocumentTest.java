/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.review;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.util.NamedList;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.index.IndexModelObserver;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * #3773: every document is sent to Solr in a request of its own.
 * <p>
 * Indexing an AIP adds its representations, files and preservation events one
 * at a time ({@code SolrUtils.create2} calls {@code SolrClient.add} with a single
 * document), so an AIP with thousands of files takes thousands of round-trips,
 * and indexing or reindexing a repository of large AIPs is dominated by them
 * rather than by the work of Solr.
 * <p>
 * The same goes for the atomic updates that copy a change of the AIP (state,
 * permissions, instance id, ancestors) into each of its files and events.
 * <p>
 * The test indexes, reindexes and updates an AIP of many files and events
 * through a client that counts the update requests and the documents they
 * carry, and lets them through to the Solr of the tests. It fails while the
 * files or the events take more than one request for every
 * {@value #MIN_DOCUMENTS_PER_REQUEST} of them, and passes once they are sent in
 * batches. The time each one takes is logged, to compare versions.
 * <p>
 * Parameters (system properties, defaults in brackets): {@code solrreview.files}
 * [1000], {@code solrreview.events} [200].
 */
@Test(groups = {SolrReview.GROUP})
public class OneRequestPerDocumentTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(OneRequestPerDocumentTest.class);

  // far fewer requests than documents, whatever the size of a batch
  private static final int MIN_DOCUMENTS_PER_REQUEST = 10;

  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;

  @BeforeClass
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(OneRequestPerDocumentTest.class, true);
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
  }

  @AfterClass
  public static void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @AfterMethod
  public void cleanUp() {
    IndexTestUtils.resetIndex();
  }

  private static int files() {
    return Integer.getInteger("solrreview.files", 1000);
  }

  private static int events() {
    return Integer.getInteger("solrreview.events", 200);
  }

  /** An AIP of {@link #files()} files and {@link #events()} events, indexed. */
  private static AIP indexedAIP() throws RODAException {
    AIP aip = IndexTestUtils.createAIP(model, files(), events());
    model.notifyAipCreated(aip.getId());
    index.commitAIPs();
    return aip;
  }

  @Test
  public void indexingAnAIPSendsItsFilesAndEventsInFewRequests() throws RODAException {
    AIP aip = IndexTestUtils.createAIP(model, files(), events());
    CountingSolrClient solr = new CountingSolrClient();

    long start = System.nanoTime();
    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(solr, model).aipCreated(aip);
    log("Indexing", start, solr);

    assertFilesAndEventsSentInFewRequests(ret, solr);
    assertFilesAndEventsIndexed(aip);
  }

  @Test
  public void reindexingAnAIPSendsItsFilesAndEventsInFewRequests() throws RODAException {
    AIP aip = indexedAIP();
    CountingSolrClient solr = new CountingSolrClient();

    long start = System.nanoTime();
    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(solr, model).aipUpdated(aip);
    log("Reindexing", start, solr);

    assertFilesAndEventsSentInFewRequests(ret, solr);
    assertFilesAndEventsIndexed(aip);
  }

  @Test
  public void updatingTheStateOfAnAIPSendsItsFilesAndEventsInFewRequests() throws RODAException {
    AIP aip = indexedAIP();
    aip.setState(AIPState.UNDER_APPRAISAL);
    CountingSolrClient solr = new CountingSolrClient();

    long start = System.nanoTime();
    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(solr, model).aipStateUpdated(aip);
    log("Updating the state of", start, solr);

    assertFilesAndEventsSentInFewRequests(ret, solr);
    SimpleFilterParameter state = new SimpleFilterParameter(RodaConstants.INDEX_STATE,
      SolrUtils.formatEnum(AIPState.UNDER_APPRAISAL));
    assertUpdated(IndexedFile.class, RodaConstants.FILE_AIP_ID, aip, state, files());
    assertUpdated(IndexedPreservationEvent.class, RodaConstants.PRESERVATION_EVENT_AIP_ID, aip, state, events());
  }

  @Test
  public void updatingThePermissionsOfAnAIPSendsItsFilesAndEventsInFewRequests() throws RODAException {
    AIP aip = indexedAIP();
    aip.getPermissions().setGroupPermissions("readers", Collections.singleton(PermissionType.READ));
    CountingSolrClient solr = new CountingSolrClient();

    long start = System.nanoTime();
    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(solr, model).aipPermissionsUpdated(aip);
    log("Updating the permissions of", start, solr);

    assertFilesAndEventsSentInFewRequests(ret, solr);
    SimpleFilterParameter readers = new SimpleFilterParameter(
      RodaConstants.INDEX_PERMISSION_GROUPS_PREFIX + PermissionType.READ, "readers");
    assertUpdated(IndexedFile.class, RodaConstants.FILE_AIP_ID, aip, readers, files());
    assertUpdated(IndexedPreservationEvent.class, RodaConstants.PRESERVATION_EVENT_AIP_ID, aip, readers, events());
  }

  @Test
  public void updatingTheInstanceIdOfAnAIPSendsItsFilesInFewRequests() throws RODAException {
    AIP aip = indexedAIP();
    aip.setInstanceId(IdUtils.createUUID());
    CountingSolrClient solr = new CountingSolrClient();

    long start = System.nanoTime();
    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(solr, model).aipInstanceIdUpdated(aip);
    log("Updating the instance id of", start, solr);

    assertSentInFewRequests(ret, solr, IndexedFile.class, files());
    assertUpdated(IndexedFile.class, RodaConstants.FILE_AIP_ID, aip,
      new SimpleFilterParameter(RodaConstants.INDEX_INSTANCE_ID, aip.getInstanceId()), files());
  }

  @Test
  public void movingAnAIPSendsItsFilesInFewRequests() throws RODAException {
    AIP parent = IndexTestUtils.createAIP(model, 0, 0);
    model.notifyAipCreated(parent.getId());
    AIP aip = indexedAIP();
    CountingSolrClient solr = new CountingSolrClient();

    long start = System.nanoTime();
    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(solr, model).aipMoved(aip, null,
      parent.getId());
    log("Moving", start, solr);

    assertSentInFewRequests(ret, solr, IndexedFile.class, files());
    assertUpdated(IndexedFile.class, RodaConstants.FILE_AIP_ID, aip,
      new SimpleFilterParameter(RodaConstants.FILE_ANCESTORS, parent.getId()), files());
  }

  private static void log(String what, long start, CountingSolrClient solr) {
    LOGGER.info("{} an AIP of {} files and {} events took {} ms, in {} requests of files and {} of events", what,
      files(), events(), TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start), solr.requests(IndexedFile.class),
      solr.requests(IndexedPreservationEvent.class));
  }

  private static void assertFilesAndEventsSentInFewRequests(ReturnWithExceptions<Void, ModelObserver> ret,
    CountingSolrClient solr) {
    assertSentInFewRequests(ret, solr, IndexedFile.class, files());
    assertSentInFewRequests(ret, solr, IndexedPreservationEvent.class, events());
  }

  private static void assertSentInFewRequests(ReturnWithExceptions<Void, ModelObserver> ret,
    CountingSolrClient solr, Class<? extends IsIndexed> indexClass, int documents) {
    Assert.assertTrue(ret.isEmpty(), "indexing failed: " + ret.getExceptions());
    String what = indexClass.getSimpleName();
    Assert.assertEquals(solr.documents(indexClass), documents, what + " documents sent");
    Assert.assertTrue(solr.requests(indexClass) <= documents / MIN_DOCUMENTS_PER_REQUEST,
      documents + " " + what + " documents sent in " + solr.requests(indexClass) + " requests");
  }

  private static void assertFilesAndEventsIndexed(AIP aip) throws RODAException {
    index.commitAIPs();
    Assert.assertEquals(
      index.count(IndexedFile.class, new Filter(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, aip.getId()))),
      files(), "files indexed");
    Assert.assertEquals(index.count(IndexedPreservationEvent.class,
      new Filter(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()))), events(),
      "events indexed");
  }

  /** Asserts the children of the AIP of the class that have the value updated. */
  private static void assertUpdated(Class<? extends IsIndexed> indexClass, String aipField, AIP aip,
    SimpleFilterParameter updated, int expected) throws RODAException {
    index.commitAIPs();
    Assert.assertEquals(index.count(indexClass, new Filter(new SimpleFilterParameter(aipField, aip.getId()), updated)),
      expected, indexClass.getSimpleName() + " documents updated");
  }

  private static String collection(Class<? extends IsIndexed> indexClass) {
    try {
      return SolrCollectionRegistry.getIndexName(indexClass);
    } catch (RODAException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Counts, by collection, the update requests that add documents and the
   * documents they add, and lets every request through to the Solr of the tests.
   */
  private static class CountingSolrClient extends SolrClient {
    private final transient Map<String, AtomicInteger> requests = new ConcurrentHashMap<>();
    private final transient Map<String, AtomicInteger> documents = new ConcurrentHashMap<>();

    @Override
    public NamedList<Object> request(SolrRequest<?> request, String collection)
      throws SolrServerException, IOException {
      if (request instanceof UpdateRequest update && update.getDocuments() != null
        && !update.getDocuments().isEmpty()) {
        requests.computeIfAbsent(collection, c -> new AtomicInteger()).incrementAndGet();
        documents.computeIfAbsent(collection, c -> new AtomicInteger()).addAndGet(update.getDocuments().size());
      }
      return index.getSolrClient().request(request, collection);
    }

    int requests(Class<? extends IsIndexed> indexClass) {
      AtomicInteger count = requests.get(collection(indexClass));
      return count == null ? 0 : count.get();
    }

    int documents(Class<? extends IsIndexed> indexClass) {
      AtomicInteger count = documents.get(collection(indexClass));
      return count == null ? 0 : count.get();
    }

    @Override
    public void close() {
      // the Solr of the tests is closed with them
    }
  }
}
