/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.config.TestConfig;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.AllFilterParameter;
import org.roda.core.data.v2.index.filter.AndFiltersParameters;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.ParentWhichFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SpringBootTest(classes = TestConfig.class)
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class NestedDocumentSearchTest extends AbstractTestNGSpringContextTests {

  private static final Logger LOGGER = LoggerFactory.getLogger(NestedDocumentSearchTest.class);

  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;
  private static StorageService corporaService;

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    RodaCoreFactory.instantiateTest(true, true, false, false, false, false, false,
      ldapUtilityTestHelper.getLdapUtility());

    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = getClass().getResource("/corpora");
    corporaService = new FileStorageService(Paths.get(corporaURL.toURI()));

    LOGGER.debug("Running nested document search tests under {}", basePath);
  }

  @AfterClass
  public void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @AfterMethod
  public void cleanUp() throws RODAException {
    try (IterableIndexResult<IndexedAIP> result = index.findAll(IndexedAIP.class, Filter.ALL, Collections.emptyList())) {
      for (IndexedAIP aip : result) {
        try {
          model.deleteAIP(aip.getId());
        } catch (Exception e) {
          // ignore
        }
      }
      index.clearAIPs();
    } catch (IOException e) {
      LOGGER.error("Error cleaning up AIPs", e);
    } finally {
      index.commitAIPs();
    }
  }

  @Test
  public void testParentWhichFilterFindsEmailArchiveAIPByChildSender() throws Exception {
    // Create AIP and attach emailarchive descriptive metadata
    String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_EMPTY),
      RodaConstants.ADMIN);

    org.roda.core.storage.Binary metaBinary = corporaService
      .getBinary(DefaultStoragePath.parse(CorporaConstants.SOURCE_DESC_METADATA_CONTAINER,
        CorporaConstants.EMAIL_ARCHIVE_FULL_FILE));

    model.createDescriptiveMetadata(aipId, "emailarchive.xml", metaBinary.getContent(),
      CorporaConstants.EMAIL_ARCHIVE_METADATA_TYPE, null, RodaConstants.ADMIN);

    // Reindex and commit
    AIP aip = model.retrieveAIP(aipId);
    index.reindexAIP(aip);
    index.commitAIPs();

    // Layer 1: parent AIP is indexed
    long parentCount = index.count(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.INDEX_UUID, aipId)));
    assertEquals("Parent AIP should be indexed", 1L, parentCount);

    // Layer 2: content_type field is populated on the parent document
    long contentTypeCount = index.count(IndexedAIP.class,
      new Filter(new SimpleFilterParameter("content_type", "emailarchive")));
    assertEquals("AIP should be findable by content_type=emailarchive", 1L, contentTypeCount);

    // Layer 3: child email documents are indexed in Solr
    QueryResponse childResp = index.getSolrClient().query(RodaConstants.INDEX_AIP,
      new SolrQuery("content_type:email").setRows(0));
    long childCount = childResp.getResults().getNumFound();
    assertTrue("Child email documents should be indexed in Solr (found " + childCount + ")", childCount > 0);

    // Layer 4: block-join ParentWhichFilterParameter returns the parent AIP
    Filter nestedFilter = new Filter(
      new ParentWhichFilterParameter(
        new SimpleFilterParameter("content_type", "emailarchive"),
        new AndFiltersParameters(Arrays.asList(
          new SimpleFilterParameter("sender_s", "joao.silva@empresa.pt"),
          new SimpleFilterParameter("content_type", "email")))));

    IndexResult<IndexedAIP> result = index.find(IndexedAIP.class, nestedFilter, Sorter.NONE,
      new Sublist(0, 10), Collections.emptyList());

    assertEquals("ParentWhichFilterParameter should return exactly 1 AIP", 1L, result.getTotalCount());
    assertEquals("Returned AIP id should match", aipId, result.getResults().get(0).getId());

    // Layer 5: same block-join combined with AllFilterParameter (*:* AND {!parent ...})
    // This reproduces the production scenario where q.op=AND was breaking the query
    Filter combinedFilter = new Filter(
      new AllFilterParameter(),
      new ParentWhichFilterParameter(
        new SimpleFilterParameter("content_type", "emailarchive"),
        new AndFiltersParameters(Arrays.asList(
          new SimpleFilterParameter("sender_s", "joao.silva@empresa.pt"),
          new SimpleFilterParameter("content_type", "email")))));

    IndexResult<IndexedAIP> combinedResult = index.find(IndexedAIP.class, combinedFilter, Sorter.NONE,
      new Sublist(0, 10), Collections.emptyList());

    assertEquals("AllFilter + ParentWhichFilterParameter should return exactly 1 AIP", 1L,
      combinedResult.getTotalCount());
    assertEquals("Returned AIP id should match", aipId, combinedResult.getResults().get(0).getId());
  }
}
