/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.RepresentationState;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.index.utils.FulltextIndexUtils.FulltextAccumulator;
import org.roda.core.index.utils.FulltextIndexUtils.RepresentationFulltext;
import org.roda.core.util.IdUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class FulltextIndexUtilsTest {

  private static final String AIP_ID = "aip";

  private static String representationCollection;
  private static String fileCollection;

  @BeforeClass
  public static void setUp() throws Exception {
    RodaCoreFactory.instantiateTest(false, false, false, false, false, false, false);
    representationCollection = SolrCollectionRegistry.getIndexName(IndexedRepresentation.class);
    fileCollection = SolrCollectionRegistry.getIndexName(IndexedFile.class);
  }

  @AfterClass
  public static void tearDown() throws NotFoundException, GenericException {
    RodaCoreFactory.shutdown();
  }

  @Test
  public void testAccumulatorKeepsChunksUpToLimit() {
    FulltextAccumulator accumulator = new FulltextAccumulator(10);
    accumulator.add("abcde");
    accumulator.add("fghij");

    Assert.assertEquals(accumulator.getFulltext(), Arrays.asList("abcde", "fghij"));
    Assert.assertFalse(accumulator.isTruncated());
    Assert.assertEquals(accumulator.getMaxBytes(), 10);
  }

  @Test
  public void testAccumulatorTruncatesFirstChunkLargerThanLimit() {
    FulltextAccumulator accumulator = new FulltextAccumulator(3);
    accumulator.add("abcd");

    Assert.assertTrue(accumulator.getFulltext().isEmpty());
    Assert.assertTrue(accumulator.isTruncated());
  }

  @Test
  public void testAccumulatorAddAll() {
    FulltextAccumulator accumulator = new FulltextAccumulator(4);
    accumulator.addAll(Arrays.asList("ab", "cd", "e"));

    Assert.assertEquals(accumulator.getFulltext(), Arrays.asList("ab", "cd"));
    Assert.assertTrue(accumulator.isTruncated());
  }

  @Test
  public void testAccumulatorCountsLoneSurrogateAsThreeBytes() {
    assertFitsExactly("\uD83D", 3);
  }

  private void assertFitsExactly(String chunk, long bytes) {
    FulltextAccumulator exact = new FulltextAccumulator(bytes);
    exact.add(chunk);
    Assert.assertFalse(exact.isTruncated(), "'" + chunk + "' should fit in " + bytes + " bytes");

    FulltextAccumulator smaller = new FulltextAccumulator(bytes - 1);
    smaller.add(chunk);
    Assert.assertTrue(smaller.isTruncated(), "'" + chunk + "' should not fit in " + (bytes - 1) + " bytes");
  }

  @Test
  public void testAccumulatorDefaultLimit() {
    Assert.assertEquals(new FulltextAccumulator().getMaxBytes(), 52428800L);
    Assert.assertEquals(FulltextIndexUtils.getAggregateThresholdInBytes(), 52428800L);
  }


  @Test
  public void testRepresentationFulltextReplacesNullsWithEmptyLists() {
    RepresentationFulltext representation = new RepresentationFulltext(true, null, null);

    Assert.assertTrue(representation.states().isEmpty());
    Assert.assertTrue(representation.fulltext().isEmpty());
  }

  @Test
  public void testSelectAipFulltextWithoutRepresentations() {
    Assert.assertTrue(FulltextIndexUtils.selectAipFulltext(Collections.emptyList()).isEmpty());
  }

  @Test
  public void testSelectAipFulltextWithoutAnyFulltext() {
    List<RepresentationFulltext> representations = Arrays.asList(
      new RepresentationFulltext(true, List.of(RepresentationState.ORIGINAL), Collections.emptyList()),
      new RepresentationFulltext(false, List.of(RepresentationState.PRESERVATION), null));

    Assert.assertTrue(FulltextIndexUtils.selectAipFulltext(representations).isEmpty());
  }

  @Test
  public void testSelectAipFulltextPrefersPreservation() {
    List<RepresentationFulltext> representations = Arrays.asList(
      new RepresentationFulltext(false, List.of(RepresentationState.ACCESS), List.of("access")),
      new RepresentationFulltext(true, List.of(RepresentationState.ORIGINAL), List.of("original")),
      new RepresentationFulltext(false, List.of(RepresentationState.PRESERVATION), List.of("preservation")));

    Assert.assertEquals(FulltextIndexUtils.selectAipFulltext(representations), List.of("preservation"));
  }

  @Test
  public void testSelectAipFulltextPrefersOriginalFlagOverOther() {
    List<RepresentationFulltext> representations = Arrays.asList(
      new RepresentationFulltext(false, List.of(RepresentationState.ACCESS), List.of("access")),
      new RepresentationFulltext(true, Collections.emptyList(), List.of("original")));

    Assert.assertEquals(FulltextIndexUtils.selectAipFulltext(representations), List.of("original"));
  }

  @Test
  public void testSelectAipFulltextPrefersOriginalStateOverOther() {
    List<RepresentationFulltext> representations = Arrays.asList(
      new RepresentationFulltext(false, Collections.emptyList(), List.of("other")),
      new RepresentationFulltext(false, List.of(RepresentationState.ORIGINAL), List.of("original")));

    Assert.assertEquals(FulltextIndexUtils.selectAipFulltext(representations), List.of("original"));
  }

  @Test
  public void testSelectAipFulltextSkipsRepresentationsWithoutFulltext() {
    List<RepresentationFulltext> representations = Arrays.asList(
      new RepresentationFulltext(false, List.of(RepresentationState.PRESERVATION), Collections.emptyList()),
      new RepresentationFulltext(true, List.of(RepresentationState.ORIGINAL), List.of("original")));

    Assert.assertEquals(FulltextIndexUtils.selectAipFulltext(representations), List.of("original"));
  }

  @Test
  public void testSelectAipFulltextResolvesTiesByOrder() {
    List<RepresentationFulltext> representations = Arrays.asList(
      new RepresentationFulltext(false, List.of(RepresentationState.PRESERVATION), List.of("first")),
      new RepresentationFulltext(false, List.of(RepresentationState.PRESERVATION), List.of("second")));

    Assert.assertEquals(FulltextIndexUtils.selectAipFulltext(representations), List.of("first"));
  }

  @Test
  public void testCollectAipFulltextOfDestroyedAIP() throws GenericException {
    SolrClient index = Mockito.mock(SolrClient.class);
    AIP aip = createAIP(AIPState.DESTROYED, "rep1");

    Assert.assertTrue(FulltextIndexUtils.collectAipFulltext(index, aip).isEmpty());
    Mockito.verifyNoInteractions(index);
  }

  @Test
  public void testCollectAipFulltextOfAIPWithoutRepresentations() throws GenericException {
    SolrClient index = Mockito.mock(SolrClient.class);
    AIP aip = createAIP(AIPState.ACTIVE);

    Assert.assertTrue(FulltextIndexUtils.collectAipFulltext(index, aip).isEmpty());
    Mockito.verifyNoInteractions(index);
  }

  @Test
  public void testCollectAipFulltextSelectsFromIndexedRepresentations()
    throws GenericException, SolrServerException, IOException {
    SolrClient index = Mockito.mock(SolrClient.class);
    AIP aip = createAIP(AIPState.ACTIVE, "rep1", "rep2", "rep3");

    // rep3 is not indexed and rep2 states come as a single value
    SolrDocumentList docs = new SolrDocumentList();
    docs.add(representationDoc("rep2", false, RepresentationState.PRESERVATION, List.of("preservation text")));
    docs.add(representationDoc("rep1", true, List.of(RepresentationState.ORIGINAL), List.of("original text")));
    Mockito.when(index.getById(eq(representationCollection), anyCollection(), any(SolrParams.class)))
      .thenReturn(docs);

    Assert.assertEquals(FulltextIndexUtils.collectAipFulltext(index, aip), List.of("preservation text"));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Collection<String>> ids = ArgumentCaptor.forClass(Collection.class);
    ArgumentCaptor<SolrParams> params = ArgumentCaptor.forClass(SolrParams.class);
    Mockito.verify(index).getById(eq(representationCollection), ids.capture(), params.capture());
    Assert.assertEquals(new ArrayList<>(ids.getValue()), Arrays.asList(IdUtils.getRepresentationId(AIP_ID, "rep1"),
      IdUtils.getRepresentationId(AIP_ID, "rep2"), IdUtils.getRepresentationId(AIP_ID, "rep3")));
    List<String> fields = Arrays.asList(params.getValue().get(CommonParams.FL).split(","));
    Assert.assertTrue(fields.contains(RodaConstants.REPRESENTATION_FULLTEXT));
    Assert.assertTrue(fields.contains(RodaConstants.REPRESENTATION_STATES));
    Assert.assertTrue(fields.contains(RodaConstants.REPRESENTATION_ORIGINAL));
  }

  @Test
  public void testCollectAipFulltextWhenNoRepresentationIsIndexed()
    throws GenericException, SolrServerException, IOException {
    SolrClient index = Mockito.mock(SolrClient.class);
    AIP aip = createAIP(AIPState.ACTIVE, "rep1");
    Mockito.when(index.getById(anyString(), anyCollection(), any(SolrParams.class)))
      .thenReturn(new SolrDocumentList());

    Assert.assertTrue(FulltextIndexUtils.collectAipFulltext(index, aip).isEmpty());
  }

  @Test(expectedExceptions = GenericException.class)
  public void testCollectAipFulltextWrapsSolrErrors() throws GenericException, SolrServerException, IOException {
    SolrClient index = Mockito.mock(SolrClient.class);
    AIP aip = createAIP(AIPState.ACTIVE, "rep1");
    Mockito.when(index.getById(anyString(), anyCollection(), any(SolrParams.class)))
      .thenThrow(new SolrServerException("error"));

    FulltextIndexUtils.collectAipFulltext(index, aip);
  }

  private static AIP createAIP(AIPState state, String... representationIds) {
    AIP aip = new AIP(AIP_ID, null, "type", state, new Permissions());
    for (String representationId : representationIds) {
      aip.getRepresentations().add(new Representation(representationId, AIP_ID, false, "type"));
    }
    return aip;
  }

  private static SolrDocument representationDoc(String representationId, boolean original, Object states,
    List<String> fulltext) {
    SolrDocument doc = new SolrDocument();
    doc.setField(RodaConstants.INDEX_UUID, IdUtils.getRepresentationId(AIP_ID, representationId));
    doc.setField(RodaConstants.REPRESENTATION_ORIGINAL, original);
    doc.setField(RodaConstants.REPRESENTATION_STATES, states);
    doc.setField(RodaConstants.REPRESENTATION_FULLTEXT, fulltext);
    return doc;
  }

  private static SolrDocument fileDoc(String uuid, String fulltext) {
    SolrDocument doc = new SolrDocument();
    doc.setField(RodaConstants.INDEX_UUID, uuid);
    if (fulltext != null) {
      doc.setField(RodaConstants.FILE_FULLTEXT, fulltext);
    }
    return doc;
  }
}
