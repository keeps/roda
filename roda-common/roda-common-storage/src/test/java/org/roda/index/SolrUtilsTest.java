package org.roda.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roda.CorporaConstants;
import org.roda.index.utils.SolrUtils;
import org.roda.storage.Binary;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fs.FileStorageService;

import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.ClassificationSchemeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.DateIntervalFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.DateRangeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.LikeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.LongRangeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.ProducerFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.SortParameter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;

public class SolrUtilsTest {

  private static StorageService corporaService;

  @BeforeClass
  public static void setUp() throws StorageServiceException, URISyntaxException {
    URL corporaURL = IndexServiceTest.class.getResource("/corpora");
    Path corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);
  }

  @Test
  public void testGetDescriptiveMetataFields() throws StorageServiceException, IndexServiceException {
    final DefaultStoragePath strangeMetadataPath = DefaultStoragePath
      .parse(CorporaConstants.SOURCE_DESC_METADATA_CONTAINER, CorporaConstants.STRANGE_DESC_METADATA_FILE);
    Binary strangeMetadata = corporaService.getBinary(strangeMetadataPath);

    SolrInputDocument descriptiveMetataFields = SolrUtils.getDescriptiveMetataFields(strangeMetadata, null);

    assertNotNull(descriptiveMetataFields);
    assertEquals(5, descriptiveMetataFields.size());
    SolrInputField field1 = descriptiveMetataFields
      .getField(RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX + ".note.to_txt");
    assertNotNull(field1);
    assertEquals(RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX + ".note.to_txt", field1.getName());
    assertEquals("Tove", field1.getValue());
  }

  @Test
  public void testParseFilter() {
    Filter filter = null;
    String stringFilter = null;
    String fonds = "fonds", series = "series", fondsOrSeries = fonds + " " + series;
    List<String> oneOfManyValues = Arrays.asList(fonds, series);
    Long from = 1L, to = 5L;

    // 1) null filter
    try {
      stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertEquals("*:*", stringFilter);
    } catch (IndexServiceException e) {
      fail("An exception was not expected!");
    }

    // 2) empty filter
    try {
      filter = new Filter();
      stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertEquals("*:*", stringFilter);
    } catch (IndexServiceException e) {
      fail("An exception was not expected!");
    }

    // 3) filter with one SimpleFilterParameter (uses exact match)
    try {
      filter = new Filter();
      filter.add(new SimpleFilterParameter(RodaConstants.SDO__ALL, fonds));
      stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertEquals(String.format("(%s: \"%s\")", RodaConstants.SDO__ALL, fonds), stringFilter);
    } catch (IndexServiceException e) {
      fail("An exception was not expected!");
    }

    // 4) filter with two SimpleFilterParameter (uses exact match, will be
    // combined with AND operator)
    try {
      filter = new Filter();
      filter.add(new SimpleFilterParameter(RodaConstants.SDO__ALL, fonds));
      filter.add(new SimpleFilterParameter(RodaConstants.SDO__ALL, series));
      stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertEquals(
        String.format("(%s: \"%s\") AND (%s: \"%s\")", RodaConstants.SDO__ALL, fonds, RodaConstants.SDO__ALL, series),
        stringFilter);
    } catch (IndexServiceException e) {
      fail("An exception was not expected!");
    }

    // 5) filter with one OneOfManyFilterParameter (uses exact match for
    // each of the values, and they will be combined using OR operator)
    try {
      filter = new Filter();
      filter.add(new OneOfManyFilterParameter(RodaConstants.SDO__ALL, oneOfManyValues));
      stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertEquals(
        String.format("((%s: \"%s\") OR (%s: \"%s\"))", RodaConstants.SDO__ALL, fonds, RodaConstants.SDO__ALL, series),
        stringFilter);
    } catch (IndexServiceException e) {
      fail("An exception was not expected!");
    }

    // 6) filter with one ClassificationSchemeFilterParameter
    try {
      filter = new Filter();
      filter.add(new ClassificationSchemeFilterParameter());
      stringFilter = SolrUtils.parseFilter(filter);
      fail("An exception should have been thrown but it wasn't!");
    } catch (IndexServiceException e) {
      assertEquals(IndexServiceException.BAD_REQUEST, e.getCode());
    }

    // 7) filter with one LikeFilterParameter
    try {
      filter = new Filter();
      filter.add(new LikeFilterParameter());
      stringFilter = SolrUtils.parseFilter(filter);
      fail("An exception should have been thrown but it wasn't!");
    } catch (IndexServiceException e) {
      assertEquals(IndexServiceException.BAD_REQUEST, e.getCode());
    }

    // 8) filter with one ProducerFilterParameter
    try {
      filter = new Filter();
      filter.add(new ProducerFilterParameter());
      stringFilter = SolrUtils.parseFilter(filter);
      fail("An exception should have been thrown but it wasn't!");
    } catch (IndexServiceException e) {
      assertEquals(IndexServiceException.BAD_REQUEST, e.getCode());
    }

    // 9) filter with one empty DateRangeFilterParameter
    try {
      filter = new Filter();
      filter.add(new DateRangeFilterParameter());
      stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertThat(stringFilter, Matchers.is("*:*"));
    } catch (IndexServiceException e) {
      assertEquals(IndexServiceException.BAD_REQUEST, e.getCode());
    }

    // 10) filter with one RegexFilterParameter
    try {
      filter = new Filter();
      filter.add(new RegexFilterParameter());
      stringFilter = SolrUtils.parseFilter(filter);
      fail("An exception should have been thrown but it wasn't!");
    } catch (IndexServiceException e) {
      assertEquals(IndexServiceException.BAD_REQUEST, e.getCode());
    }

    // 11) filter with one BasicSearchFilterParameter
    try {
      filter = new Filter();
      filter.add(new BasicSearchFilterParameter(RodaConstants.SDO__ALL, fondsOrSeries));
      stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertEquals(String.format("(%s: %s AND %s: %s)", RodaConstants.SDO__ALL, fonds, RodaConstants.SDO__ALL, series),
        stringFilter);
    } catch (IndexServiceException e) {
      fail("An exception was not expected!");
    }

    // 12) filter with one EmptyKeyFilterParameter
    try {
      filter = new Filter();
      filter.add(new EmptyKeyFilterParameter(RodaConstants.SDO__ALL));
      stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertEquals(String.format("(*:* NOT %s:*)", RodaConstants.SDO__ALL), stringFilter);
    } catch (IndexServiceException e) {
      fail("An exception was not expected!");
    }

    // 13) filter with one empty LongRangeFilterParameter
    try {
      filter = new Filter();
      filter.add(new LongRangeFilterParameter());
      stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertThat(stringFilter, Matchers.is("*:*"));
    } catch (IndexServiceException e) {
      assertEquals(IndexServiceException.BAD_REQUEST, e.getCode());
    }

    // 13.1) filter with one LongRangeFilterParameter
    try {
      filter = new Filter();
      filter.add(new LongRangeFilterParameter(RodaConstants.LOG_DURATION, from, to));
      stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertThat(stringFilter, Matchers.is(String.format("(%s:[%s TO %s])", RodaConstants.LOG_DURATION, from, to)));
    } catch (IndexServiceException e) {
      assertEquals(IndexServiceException.BAD_REQUEST, e.getCode());
    }

    // 14) filter with one DateIntervalFilterParameter
    try {
      filter = new Filter();
      filter.add(new DateIntervalFilterParameter());
      stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertThat(stringFilter, Matchers.is("*:*"));
    } catch (IndexServiceException e) {
      assertEquals(IndexServiceException.BAD_REQUEST, e.getCode());
    }

  }

  @Test
  public void testParseSorter() throws IndexServiceException {
    Sorter sorter = null;
    List<SortClause> sortList = null;
    String field1 = "field1", field2 = "field2";
    boolean descending = true;
    boolean ascending = false;

    // 1) null sorter
    sortList = SolrUtils.parseSorter(sorter);
    assertNotNull(sortList);
    assertThat(sortList, Matchers.hasSize(0));

    // 2) empty sorter
    sorter = new Sorter();
    sortList = SolrUtils.parseSorter(sorter);
    assertNotNull(sortList);
    assertThat(sortList, Matchers.hasSize(0));

    // 3) sorter with 1 sorter parameter
    sorter = new Sorter();
    sorter.add(new SortParameter(field1, descending));
    sortList = SolrUtils.parseSorter(sorter);
    assertNotNull(sortList);
    assertThat(sortList, Matchers.hasSize(1));
    assertThat(sortList.get(0).getItem(), Matchers.equalTo(field1));
    assertThat(sortList.get(0).getOrder(), Matchers.equalTo(ORDER.desc));

    // 4) sorter with 2 sorter parameters
    sorter = new Sorter();
    sorter.add(new SortParameter(field1, descending));
    sorter.add(new SortParameter(field2, ascending));
    sortList = SolrUtils.parseSorter(sorter);
    assertNotNull(sortList);
    assertThat(sortList, Matchers.hasSize(2));
    assertThat(sortList.get(0).getItem(), Matchers.equalTo(field1));
    assertThat(sortList.get(0).getOrder(), Matchers.equalTo(ORDER.desc));
    assertThat(sortList.get(1).getItem(), Matchers.equalTo(field2));
    assertThat(sortList.get(1).getOrder(), Matchers.equalTo(ORDER.asc));

  }

}
