/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.review;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.ToIntFunction;

import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.fs.FSUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * #3784: four shards per collection on one node.
 * <p>
 * RODA creates every collection with the same number of shards
 * ({@code SOLR_NUM_SHARDS}), so the reference deploy (4) turns 20 collections,
 * most of them small, into 80 cores on one node. The shard count is now set in
 * the RODA configuration: per collection
 * ({@code core.solr.shards.collection.<Collection>}), then per group, large
 * ({@code core.solr.shards.large}) or small ({@code core.solr.shards.small}),
 * then {@code SOLR_NUM_SHARDS} for every collection as before, then 1.
 * <p>
 * Each test creates the collections in a Solr of its own ({@link ShardedSolr}),
 * as the shard count only applies when a collection is created, and sets the
 * configuration through the system properties RODA reads it from
 * ({@code RODA_CORE_SOLR_SHARDS_LARGE} and so on).
 */
@Test(groups = {SolrReview.GROUP})
public class ShardCountsTest {
  // the collections that grow with the repository (measured on an installation of ~600 GB); the others are small
  private static final Set<String> LARGE_COLLECTIONS = Set.of(RodaConstants.INDEX_FILE,
    RodaConstants.INDEX_PRESERVATION_EVENTS, RodaConstants.INDEX_AIP, RodaConstants.INDEX_REPRESENTATION,
    RodaConstants.INDEX_ACTION_LOG, RodaConstants.INDEX_JOB_REPORT);

  @Test
  public void solrNumShardsAloneKeepsTheCurrentBehaviour() throws Exception {
    Map<String, Integer> shards = shardsOfCollectionsCreatedWith(Map.of("SOLR_NUM_SHARDS", "4"));

    assertShards(shards, collection -> 4, "SOLR_NUM_SHARDS=4 alone applies to every collection, as before");
  }

  @Test
  public void largeAndSmallCollectionsGetTheirGroupShardCount() throws Exception {
    Map<String, Integer> shards = shardsOfCollectionsCreatedWith(
      Map.of("RODA_CORE_SOLR_SHARDS_LARGE", "4", "RODA_CORE_SOLR_SHARDS_SMALL", "2"));

    assertShards(shards, collection -> LARGE_COLLECTIONS.contains(collection) ? 4 : 2,
      "core.solr.shards.large=4 for " + LARGE_COLLECTIONS + " and core.solr.shards.small=2 for the others");
  }

  @Test
  public void aCollectionShardCountWinsOverItsGroup() throws Exception {
    Map<String, Integer> shards = shardsOfCollectionsCreatedWith(Map.of("SOLR_NUM_SHARDS", "5",
      "RODA_CORE_SOLR_SHARDS_LARGE", "4", "RODA_CORE_SOLR_SHARDS_SMALL", "2",
      "RODA_CORE_SOLR_SHARDS_COLLECTION_FILE", "3", "RODA_CORE_SOLR_SHARDS_COLLECTION_JOB", "6"));

    assertShards(shards, collection -> switch (collection) {
      case RodaConstants.INDEX_FILE -> 3;
      case RodaConstants.INDEX_JOB -> 6;
      default -> LARGE_COLLECTIONS.contains(collection) ? 4 : 2;
    }, "core.solr.shards.collection.File=3 (large) and core.solr.shards.collection.Job=6 (small) win over their "
      + "groups, which win over SOLR_NUM_SHARDS");
  }

  /** Starts RODA on a new Solr with the given configuration and returns the shard count of each collection. */
  private static Map<String, Integer> shardsOfCollectionsCreatedWith(Map<String, String> properties)
    throws Exception {
    Path basePath = TestsHelper.createBaseTempDir(ShardCountsTest.class, true);
    LdapUtilityTestHelper ldapUtilityTestHelper = new LdapUtilityTestHelper();
    try (ShardedSolr solr = ShardedSolr.start(properties)) {
      try {
        boolean deploySolr = true;
        boolean deployLdap = true;
        boolean deployFolderMonitor = false;
        boolean deployOrchestrator = false;
        boolean deployPluginManager = false;
        boolean deployDefaultResources = false;
        RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
          deployPluginManager, deployDefaultResources, false, ldapUtilityTestHelper.getLdapUtility());

        CloudSolrClient client = (CloudSolrClient) RodaCoreFactory.getSolr();
        Map<String, Integer> shards = new TreeMap<>();
        for (String collection : SolrCollectionRegistry.registryIndexNames()) {
          shards.put(collection, client.getClusterStateProvider().getCollection(collection).getSlices().size());
        }
        return shards;
      } finally {
        ldapUtilityTestHelper.shutdown();
        RodaCoreFactory.shutdown();
        FSUtils.deletePath(basePath);
      }
    }
  }

  private static void assertShards(Map<String, Integer> shards,
    ToIntFunction<String> expected, String rule) {
    Map<String, String> wrong = new TreeMap<>();
    shards.forEach((collection, count) -> {
      if (count != expected.applyAsInt(collection)) {
        wrong.put(collection, count + " (expected " + expected.applyAsInt(collection) + ")");
      }
    });
    Assert.assertTrue(wrong.isEmpty(), rule + "; collections with other shard counts: " + wrong);
  }
}
