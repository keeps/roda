/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.review;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.model.ModelService;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * #3783: deleting AIPs blocks indexing.
 * <p>
 * Deleting an AIP removes its representations, files and events from the index
 * with a delete-by-query each. Every core that applies a delete-by-query blocks
 * all its updates until it is done ({@code DistributedUpdateProcessor
 * .versionDeleteByQuery} calls {@code blockUpdates()}), and while they are
 * blocked it opens a realtime searcher, which flushes the documents indexed
 * since the last one ({@code DirectUpdateHandler2.deleteByQuery} and
 * {@code UpdateLog.deleteByQuery}). A delete by id only locks its own
 * document. So while AIPs are being deleted, everything else that indexes into
 * the same collections (other jobs, ingest) slows to a crawl.
 * <p>
 * The test indexes files from several threads, as concurrent jobs do, and
 * measures how many it indexes per second, first alone and then while other
 * threads delete AIPs through {@code ModelService.deleteAIP}. It also checks
 * that the deleted AIPs' files are gone. It fails while deleting AIPs takes
 * concurrent indexing below {@code solrreview.minThroughputRatio} of what it
 * was alone, and passes once the deletes no longer block it (deleting by id).
 * <p>
 * Measured with this test (one shard, 8 writers, 4 deleters): concurrent
 * indexing fell to 2% of what it was alone while deleting by query, and stayed
 * at 54-70% while deleting by id after a soft commit.
 * <p>
 * Parameters (system properties, defaults in brackets):
 * {@code solrreview.aips} [300], {@code solrreview.writers} [8],
 * {@code solrreview.deleters} [4], {@code solrreview.baselineSeconds} [10],
 * {@code solrreview.minThroughputRatio} [0.3].
 */
@Test(groups = {SolrReview.GROUP})
public class DeleteBlocksIndexingTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteBlocksIndexingTest.class);

  private static final String LOAD_TEXT = "lorem ipsum dolor sit amet ".repeat(40);

  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;
  private static StorageService corporaService;

  @BeforeClass
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(DeleteBlocksIndexingTest.class, true);
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

    URL corporaURL = DeleteBlocksIndexingTest.class.getResource("/corpora");
    corporaService = new FileStorageService(Paths.get(corporaURL.toURI()));
  }

  @AfterClass
  public static void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void deletingAIPsDoesNotBlockConcurrentIndexing() throws Exception {
    int aips = Integer.getInteger("solrreview.aips", 300);
    int writers = Integer.getInteger("solrreview.writers", 8);
    int deleters = Integer.getInteger("solrreview.deleters", 4);
    int baselineSeconds = Integer.getInteger("solrreview.baselineSeconds", 10);
    double minRatio = Double.parseDouble(System.getProperty("solrreview.minThroughputRatio", "0.3"));

    ConcurrentLinkedQueue<String> toDelete = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < aips; i++) {
      toDelete.add(model.createAIP(IdUtils.createUUID(), corporaService,
        DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
        RodaConstants.ADMIN).getId());
    }
    index.commitAIPs();
    List<String> deleted = new ArrayList<>(toDelete);
    Assert.assertTrue(countFiles(deleted.get(0)) > 0, "the AIPs have indexed files");

    ExecutorService executor = Executors.newFixedThreadPool(writers + deleters);
    AtomicBoolean stop = new AtomicBoolean(false);
    AtomicLong indexed = new AtomicLong();
    List<Future<?>> load = new ArrayList<>();
    try {
      // concurrent indexing into the File collection, as other jobs do while AIPs are deleted
      SolrClient solr = RodaCoreFactory.getSolr();
      for (int w = 0; w < writers; w++) {
        load.add(executor.submit(() -> {
          while (!stop.get()) {
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField(RodaConstants.INDEX_UUID, UUID.randomUUID().toString());
            doc.addField(RodaConstants.FILE_AIP_ID, "load");
            doc.addField("content_txt", LOAD_TEXT);
            solr.add(RodaConstants.INDEX_FILE, doc);
            indexed.incrementAndGet();
          }
          return null;
        }));
      }

      // indexing alone
      long start = indexed.get();
      Thread.sleep(TimeUnit.SECONDS.toMillis(baselineSeconds));
      double alone = (indexed.get() - start) / (double) baselineSeconds;

      // indexing while AIPs are deleted
      start = indexed.get();
      long deleteStart = System.nanoTime();
      List<Future<?>> deletes = new ArrayList<>();
      for (int d = 0; d < deleters; d++) {
        deletes.add(executor.submit(() -> {
          for (String aipId = toDelete.poll(); aipId != null; aipId = toDelete.poll()) {
            model.deleteAIP(aipId);
          }
          return null;
        }));
      }
      for (Future<?> delete : deletes) {
        delete.get();
      }
      double deleteSeconds = (System.nanoTime() - deleteStart) / 1e9;
      double whileDeleting = (indexed.get() - start) / deleteSeconds;

      stop.set(true);
      for (Future<?> writer : load) {
        writer.get();
      }
      index.commitAIPs();

      for (String aipId : deleted) {
        Assert.assertEquals(countFiles(aipId), 0, "the files of the deleted AIP " + aipId + " are gone");
      }

      double ratio = whileDeleting / alone;
      String measured = String.format(
        "concurrent indexing: %.0f files/s alone, %.0f files/s while deleting %d AIPs in %.1f s (%.0f%%)", alone,
        whileDeleting, aips, deleteSeconds, ratio * 100);
      LOGGER.info(measured);
      Assert.assertTrue(ratio >= minRatio,
        "deleting AIPs blocked concurrent indexing: " + measured + ", expected at least " + minRatio * 100 + "%");
    } finally {
      stop.set(true);
      executor.shutdownNow();
    }
  }

  private long countFiles(String aipId) throws RODAException {
    return index.count(IndexedFile.class, new Filter(new SimpleFilterParameter(RodaConstants.FILE_AIP_ID, aipId)));
  }
}
