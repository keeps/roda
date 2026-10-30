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
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;

import org.apache.commons.configuration2.Configuration;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.ReturnWithExceptions;
import org.roda.core.data.v2.disposal.metadata.DisposalAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalScheduleAIPMetadata;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPDisposalScheduleAssociationType;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.index.utils.IndexResultIterator;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.index.utils.RetryPolicyBuilder;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelObserver;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * #3785: when Solr fails (after the retries of a call), indexing an object
 * stops instead of trying each of its remaining documents, each one with its
 * own retries; failures that are not of Solr keep the behaviour they had.
 *
 * Solr is made to fail by a client that fails the requests to some collections
 * with a Solr internal error, and lets the others through to the Solr of the
 * tests. Internal errors are not retried and the circuit breaker is disabled,
 * so the failed requests counted are the calls made.
 *
 * With AIPs of more files than are sent to Solr at once (#3773), the request
 * failed is the one with a given file: nothing is written after it and the
 * documents of files that exist are not taken as stale; when Solr rejects that
 * file, the other files sent are still indexed. The same goes for the atomic
 * updates of the files of an AIP.
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class SolrFailureStopsIndexingTest {
  // more files than are sent to Solr at once, and not a multiple of it, and the
  // one failed in the middle
  private static final int MANY_FILES = 1250;
  private static final int FAILED_FILE = 700;
  private static final Map<String, Object> RETRY_SETTINGS = Map.of(RodaConstants.SOLR_RETRY_SERVER_ERROR_MAX_RETRIES, 0,
    RodaConstants.SOLR_CIRCUIT_BREAKER_FAILURES, 0);

  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;
  private static StorageService corporaService;

  @BeforeClass
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(SolrFailureStopsIndexingTest.class, true);
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

    URL corporaURL = SolrFailureStopsIndexingTest.class.getResource("/corpora");
    corporaService = new FileStorageService(Paths.get(corporaURL.toURI()));

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
  public void cleanUp() throws RODAException {
    try (
      IterableIndexResult<IndexedAIP> result = index.findAll(IndexedAIP.class, Filter.ALL, Collections.emptyList())) {
      for (IndexedAIP aip : result) {
        try {
          model.deleteAIP(aip.getId());
        } catch (NotFoundException e) {
          // do nothing
        }
      }
      index.clearAIPs();
    } catch (IOException e) {
      throw new RODAException(e);
    } finally {
      index.commitAIPs();
    }
  }

  /** An AIP with 2 representations, 2 files in each, and PREMIS events. */
  private AIP createAIP() throws RODAException {
    AIP aip = model.createAIP(IdUtils.createUUID(), corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);
    index.commitAIPs();
    Assert.assertEquals(aip.getRepresentations().size(), 2);
    Assert.assertTrue(countChildren(IndexedFile.class, RodaConstants.FILE_AIP_ID, aip.getId()) >= 4);
    Assert.assertTrue(
      countChildren(IndexedPreservationEvent.class, RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()) >= 2);
    return aip;
  }

  private long countChildren(Class<? extends IsIndexed> indexClass, String aipField, String aipId)
    throws RODAException {
    return index.count(indexClass, new Filter(new SimpleFilterParameter(aipField, aipId)));
  }

  private static String fileUUID(AIP aip, int file) {
    return IdUtils.getFileId(aip.getId(), aip.getRepresentations().getFirst().getId(), Collections.emptyList(),
      IndexTestUtils.fileId(file));
  }

  /** The files of the AIP indexed, whatever their state, that match the parameters. */
  private Set<String> indexedFiles(AIP aip, SimpleFilterParameter... more) throws RODAException {
    Set<String> uuids = new HashSet<>();
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, aip.getId()));
    for (SimpleFilterParameter parameter : more) {
      filter.add(parameter);
    }
    try (IterableIndexResult<IndexedFile> result = index.findAll(IndexedFile.class, filter, false,
      Collections.singletonList(RodaConstants.INDEX_UUID))) {
      for (IndexedFile file : result) {
        uuids.add(file.getUUID());
      }
    } catch (IOException e) {
      throw new RODAException(e);
    }
    return uuids;
  }

  private static String collection(Class<? extends IsIndexed> indexClass) {
    try {
      return SolrCollectionRegistry.getIndexName(indexClass);
    } catch (RODAException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Fails, with a Solr error, the requests the predicate selects, and counts them
   * by collection; lets the others through to the Solr of the tests. Records the
   * documents sent to be added, and the writes sent after the first failure.
   */
  private static class FailingSolrClient extends SolrClient {
    private final transient SolrClient solr;
    private final transient BiPredicate<SolrRequest<?>, String> fails;
    private final SolrException.ErrorCode errorCode;
    private final transient Map<String, AtomicInteger> failed = new ConcurrentHashMap<>();
    // by collection, the documents sent to be added, in the order first sent
    private final transient Map<String, Set<String>> sent = new ConcurrentHashMap<>();
    private final AtomicInteger writesAfterFailure = new AtomicInteger();

    FailingSolrClient(BiPredicate<SolrRequest<?>, String> fails) {
      this(fails, SolrException.ErrorCode.SERVER_ERROR);
    }

    FailingSolrClient(BiPredicate<SolrRequest<?>, String> fails, SolrException.ErrorCode errorCode) {
      this.solr = index.getSolrClient();
      this.fails = fails;
      this.errorCode = errorCode;
    }

    /** Fails the writes that add the document with the given uuid. */
    static FailingSolrClient failingWritesOf(String uuid, SolrException.ErrorCode errorCode) {
      return new FailingSolrClient((request, collection) -> request instanceof UpdateRequest update
        && update.getDocuments() != null
        && update.getDocuments().stream().anyMatch(doc -> uuid.equals(doc.getFieldValue(RodaConstants.INDEX_UUID))),
        errorCode);
    }

    /** Fails the writes to the collections of the given classes. */
    @SafeVarargs
    static FailingSolrClient failingWritesTo(Class<? extends IsIndexed>... indexClasses) {
      List<String> collections = new ArrayList<>();
      for (Class<? extends IsIndexed> indexClass : indexClasses) {
        collections.add(collection(indexClass));
      }
      return new FailingSolrClient(
        (request, collection) -> request instanceof UpdateRequest && collections.contains(collection));
    }

    @Override
    public NamedList<Object> request(SolrRequest<?> request, String collection)
      throws SolrServerException, IOException {
      if (request instanceof UpdateRequest update) {
        if (!failed.isEmpty()) {
          writesAfterFailure.incrementAndGet();
        }
        if (update.getDocuments() != null) {
          Set<String> documents = sent.computeIfAbsent(collection,
            c -> Collections.synchronizedSet(new LinkedHashSet<>()));
          update.getDocuments().forEach(doc -> documents.add((String) doc.getFieldValue(RodaConstants.INDEX_UUID)));
        }
      }
      if (fails.test(request, collection)) {
        failed.computeIfAbsent(collection, c -> new AtomicInteger()).incrementAndGet();
        throw new SolrException(errorCode, "Solr failed");
      }
      return solr.request(request, collection);
    }

    int failed(Class<? extends IsIndexed> indexClass) {
      AtomicInteger count = failed.get(collection(indexClass));
      return count == null ? 0 : count.get();
    }

    /** The documents sent to be added to the collection. */
    Set<String> sent(Class<? extends IsIndexed> indexClass) {
      Set<String> documents = sent.get(collection(indexClass));
      if (documents == null) {
        return Collections.emptySet();
      }
      synchronized (documents) {
        return new HashSet<>(documents);
      }
    }

    /** The writes (adds, updates or deletes) sent after the first failure. */
    int writesAfterFailure() {
      return writesAfterFailure.get();
    }

    @Override
    public void close() {
      // the Solr of the tests is closed with them
    }
  }

  @Test
  public void testAIPIsNotIndexedInSafeModeWhenSolrFails() throws RODAException {
    AIP aip = createAIP();
    FailingSolrClient solr = new FailingSolrClient((request, collection) -> true);

    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(solr, model).aipCreated(aip);

    Assert.assertFalse(ret.isEmpty());
    // the safe mode is for AIPs whose document cannot be built, not for a Solr that
    // fails
    Assert.assertEquals(solr.failed(IndexedAIP.class), 1, "the AIP document is sent once");
  }

  @Test
  public void testAIPStateUpdateStopsAtFirstSolrFailure() throws RODAException {
    AIP aip = createAIP();
    FailingSolrClient solr = FailingSolrClient.failingWritesTo(IndexedFile.class);

    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(solr, model).aipStateUpdated(aip);

    Assert.assertFalse(ret.isEmpty());
    Assert.assertEquals(solr.failed(IndexedFile.class), 1, "no file is updated after the first fails");
  }

  @Test
  public void testAIPInstanceIdUpdateStopsAtFirstSolrFailure() throws RODAException {
    AIP aip = createAIP();
    FailingSolrClient solr = FailingSolrClient.failingWritesTo(IndexedFile.class);

    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(solr, model).aipInstanceIdUpdated(aip);

    Assert.assertFalse(ret.isEmpty());
    Assert.assertEquals(solr.failed(IndexedFile.class), 1, "no file is updated after the first fails");
  }

  @Test
  public void testAIPPermissionsUpdateStopsAtFirstSolrFailure() throws RODAException {
    AIP aip = createAIP();
    FailingSolrClient solr = FailingSolrClient.failingWritesTo(IndexedRepresentation.class,
      IndexedPreservationEvent.class);

    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(solr, model).aipPermissionsUpdated(aip);

    Assert.assertFalse(ret.isEmpty());
    Assert.assertEquals(solr.failed(IndexedRepresentation.class), 1,
      "no representation is updated after the first fails");
    Assert.assertEquals(solr.failed(IndexedPreservationEvent.class), 0,
      "the events are not updated after a representation fails");
  }

  @Test
  public void testAIPMoveStopsAtFirstSolrFailure() throws RODAException {
    AIP aip = createAIP();
    FailingSolrClient solr = FailingSolrClient.failingWritesTo(IndexedFile.class);

    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(solr, model).aipMoved(aip, null, null);

    Assert.assertFalse(ret.isEmpty());
    Assert.assertEquals(solr.failed(IndexedFile.class), 1, "no file is updated after the first fails");
  }

  @Test
  public void testDeleteMatchingStopsAtFirstFailedBatch() throws Exception {
    // more than two batches of deletes
    String component = IdUtils.createUUID();
    List<SolrInputDocument> documents = new ArrayList<>();
    for (int i = 0; i < 2500; i++) {
      LogEntry entry = new LogEntry();
      entry.setUUID(IdUtils.createUUID());
      entry.setAddress("127.0.0.1");
      entry.setUsername(RodaConstants.ADMIN);
      entry.setActionComponent(component);
      entry.setActionMethod("delete");
      entry.setParameters(Collections.emptyList());
      entry.setDuration(1);
      entry.setDatetime(new Date());
      entry.setState(LogEntryState.SUCCESS);
      documents.add(SolrCollectionRegistry.toSolrDocument(LogEntry.class, model, entry));
    }
    index.getSolrClient().add(collection(LogEntry.class), documents);
    SolrUtils.commit(index.getSolrClient(), LogEntry.class);

    FailingSolrClient solr = FailingSolrClient.failingWritesTo(LogEntry.class);
    ReturnWithExceptions<Void, Object> ret = SolrUtils.deleteMatchingById(solr, LogEntry.class,
      new Filter(new SimpleFilterParameter(RodaConstants.LOG_ACTION_COMPONENT, component)), Collections.emptySet(),
      new Object());

    Assert.assertFalse(ret.isEmpty());
    Assert.assertEquals(solr.failed(LogEntry.class), 1, "no batch is deleted after the first fails");
  }

  @Test
  public void testAIPUpdateWithErrorsNotOfSolrStillRemovesStaleDocuments() throws RODAException {
    AIP aip = createAIP();
    String repId = CorporaConstants.REPRESENTATION_1_ID;
    RodaCoreFactory.getStorageService().deleteResource(ModelUtils.getFileStoragePath(aip.getId(), repId,
      CorporaConstants.REPRESENTATION_1_FILE_2_PATH, CorporaConstants.REPRESENTATION_1_FILE_2_ID));
    // an error that is not of Solr: its disposal schedule cannot be found
    DisposalScheduleAIPMetadata schedule = new DisposalScheduleAIPMetadata();
    schedule.setId(IdUtils.createUUID());
    schedule.setAssociationType(AIPDisposalScheduleAssociationType.MANUAL);
    DisposalAIPMetadata disposal = new DisposalAIPMetadata();
    disposal.setSchedule(schedule);
    aip.setDisposal(disposal);

    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(index.getSolrClient(), model)
      .aipUpdated(aip);
    index.commitAIPs();

    Assert.assertFalse(ret.isEmpty(), "the update has an error");
    // as when it was deleted and indexed again: the file removed from storage is no
    // longer indexed
    Assert.assertEquals(index.count(IndexedFile.class,
      new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, IdUtils.getFileId(aip.getId(), repId,
        CorporaConstants.REPRESENTATION_1_FILE_2_PATH, CorporaConstants.REPRESENTATION_1_FILE_2_ID)))),
      0L);
    Assert.assertEquals(countChildren(IndexedFile.class, RodaConstants.FILE_AIP_ID, aip.getId()), 3L);
  }

  @Test
  public void testAIPUpdateStopsAtSolrFailureWithoutRemovingDocuments() throws RODAException {
    AIP aip = IndexTestUtils.createAIP(model, MANY_FILES, 0);
    model.notifyAipCreated(aip.getId());
    index.commitAIPs();
    FailingSolrClient solr = FailingSolrClient.failingWritesOf(fileUUID(aip, FAILED_FILE),
      SolrException.ErrorCode.SERVER_ERROR);

    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(solr, model).aipUpdated(aip);
    index.commitAIPs();

    Assert.assertFalse(ret.isEmpty());
    Assert.assertEquals(solr.writesAfterFailure(), 0, "nothing is written or deleted after Solr fails");
    // the files not written again exist: their documents are not stale
    Assert.assertEquals(countChildren(IndexedFile.class, RodaConstants.FILE_AIP_ID, aip.getId()), MANY_FILES);
  }

  @Test
  public void testFileRejectedBySolrIsTheOnlyFileSentNotIndexed() throws RODAException {
    AIP aip = IndexTestUtils.createAIP(model, MANY_FILES, 0);
    String rejected = fileUUID(aip, FAILED_FILE);
    FailingSolrClient solr = FailingSolrClient.failingWritesOf(rejected, SolrException.ErrorCode.BAD_REQUEST);

    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(solr, model).aipCreated(aip);
    index.commitAIPs();

    Assert.assertFalse(ret.isEmpty(), "the rejection is reported");
    Set<String> expected = solr.sent(IndexedFile.class);
    Assert.assertTrue(expected.remove(rejected), "the rejected file is sent");
    Assert.assertEquals(indexedFiles(aip), expected, "every file sent but the rejected one is indexed");
  }

  @Test
  public void testAIPPermissionsUpdateStopsAtSolrFailureOfAFile() throws RODAException {
    AIP aip = IndexTestUtils.createAIP(model, MANY_FILES, 0);
    model.notifyAipCreated(aip.getId());
    index.commitAIPs();
    aip.getPermissions().setGroupPermissions("readers", Collections.singleton(PermissionType.READ));
    FailingSolrClient solr = FailingSolrClient.failingWritesOf(fileUUID(aip, FAILED_FILE),
      SolrException.ErrorCode.SERVER_ERROR);

    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(solr, model).aipPermissionsUpdated(aip);

    Assert.assertFalse(ret.isEmpty());
    Assert.assertEquals(solr.writesAfterFailure(), 0, "nothing is written after Solr fails");
  }

  @Test
  public void testFileUpdateRejectedBySolrIsTheOnlyFileSentNotUpdated() throws RODAException {
    AIP aip = IndexTestUtils.createAIP(model, MANY_FILES, 0);
    model.notifyAipCreated(aip.getId());
    index.commitAIPs();
    aip.setState(AIPState.UNDER_APPRAISAL);
    String rejected = fileUUID(aip, FAILED_FILE);
    FailingSolrClient solr = FailingSolrClient.failingWritesOf(rejected, SolrException.ErrorCode.BAD_REQUEST);

    ReturnWithExceptions<Void, ModelObserver> ret = new IndexModelObserver(solr, model).aipStateUpdated(aip);
    index.commitAIPs();

    Assert.assertFalse(ret.isEmpty(), "the rejection is reported");
    Set<String> expected = solr.sent(IndexedFile.class);
    Assert.assertTrue(expected.remove(rejected), "the rejected file is sent");
    Assert.assertEquals(
      indexedFiles(aip,
        new SimpleFilterParameter(RodaConstants.INDEX_STATE, SolrUtils.formatEnum(AIPState.UNDER_APPRAISAL))),
      expected, "every file sent but the rejected one is updated");
  }

  @Test
  public void testListingRetriesSolrServerErrors() throws RODAException {
    createAIP();
    // a server error, as while a shard has no leader, is transient
    AtomicInteger failed = new AtomicInteger();
    SolrClient solr = new FailingSolrClient((request, collection) -> false) {
      @Override
      public NamedList<Object> request(SolrRequest<?> request, String collection)
        throws SolrServerException, IOException {
        if (failed.getAndIncrement() == 0) {
          throw new SolrException(SolrException.ErrorCode.SERVICE_UNAVAILABLE, "No active replicas");
        }
        return super.request(request, collection);
      }
    };

    IndexResultIterator<IndexedAIP> iterator = new IndexResultIterator<>(solr, IndexedAIP.class, Filter.ALL, null,
      false, Collections.singletonList(RodaConstants.INDEX_UUID));

    Assert.assertTrue(iterator.hasNext(), "the page is loaded after the server error");
    Assert.assertEquals(failed.get(), 2);
  }
}
