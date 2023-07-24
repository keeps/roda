/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.hamcrest.Matchers;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.DateIntervalFilterParameter;
import org.roda.core.data.v2.index.filter.DateRangeFilterParameter;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.LikeFilterParameter;
import org.roda.core.data.v2.index.filter.LongRangeFilterParameter;
import org.roda.core.data.v2.index.filter.OneOfManyFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.SortParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FileStorageService;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class SolrUtilsTest {

  private static final String FONDS = "fonds";
  private static final String SERIES = "series";
  private static StorageService corporaService;

  @BeforeClass
  public static void setUp() throws URISyntaxException, GenericException {
    URL corporaURL = IndexServiceTest.class.getResource("/corpora");
    Path corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);
  }

  @BeforeMethod
  public void init() {
    boolean deploySolr = false;
    boolean deployLdap = false;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = false;
    boolean deployPluginManager = false;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false);
  }

  @AfterMethod
  public void cleanup() throws NotFoundException, GenericException, IOException {
    RodaCoreFactory.shutdown();
  }

  @Test
  public void testGetDescriptiveMetadataFields() throws RODAException {
    final DefaultStoragePath strangeMetadataPath = DefaultStoragePath
      .parse(CorporaConstants.SOURCE_DESC_METADATA_CONTAINER, CorporaConstants.STRANGE_DESC_METADATA_FILE);
    Binary strangeMetadata = corporaService.getBinary(strangeMetadataPath);

    SolrInputDocument descriptiveMetataFields = SolrUtils.getDescriptiveMetadataFields(strangeMetadata, null, null);

    assertNotNull(descriptiveMetataFields);
    assertEquals(5, descriptiveMetataFields.size());
    SolrInputField field1 = descriptiveMetataFields
      .getField(RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX + ".note.to_txt");
    assertNotNull(field1);
    assertEquals(RodaConstants.INDEX_OTHER_DESCRIPTIVE_DATA_PREFIX + ".note.to_txt", field1.getName());
    assertEquals("Tove", field1.getValue());
  }

  @Test
  public void testParserWithNullFilter() {
    try {
      String stringFilter = SolrUtils.parseFilter(null);
      assertNotNull(stringFilter);
      assertTrue(stringFilter.isEmpty());
    } catch (RODAException e) {
      Assert.fail("An exception was not expected!");
    }
  }

  @Test
  public void testParserWithEmptyFilter() {
    try {
      String stringFilter = SolrUtils.parseFilter(new Filter());
      assertNotNull(stringFilter);
      assertTrue(stringFilter.isEmpty());
    } catch (RODAException e) {
      Assert.fail("An exception was not expected!");
    }
  }

  @Test
  public void testParserWithOneSimpleFilterParameter() {
    try {
      Filter filter = new Filter();
      filter.add(new SimpleFilterParameter(RodaConstants.INDEX_SEARCH, FONDS));
      String stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertEquals(String.format("(%s: \"%s\")", RodaConstants.INDEX_SEARCH, FONDS), stringFilter);
    } catch (RODAException e) {
      Assert.fail("An exception was not expected!");
    }
  }

  @Test
  public void testParserWithTwoSimpleFilterParameter() {
    try {
      Filter filter = new Filter();
      filter.add(new SimpleFilterParameter(RodaConstants.INDEX_SEARCH, FONDS));
      filter.add(new SimpleFilterParameter(RodaConstants.INDEX_SEARCH, SERIES));
      String stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertEquals(String.format("(%s: \"%s\") AND (%s: \"%s\")", RodaConstants.INDEX_SEARCH, FONDS,
        RodaConstants.INDEX_SEARCH, SERIES), stringFilter);
    } catch (RODAException e) {
      Assert.fail("An exception was not expected!");
    }
  }

  @Test
  private void testParserWithOneOneOfManyFilterParameter() {
    try {
      Filter filter = new Filter();
      filter.add(new OneOfManyFilterParameter(RodaConstants.INDEX_SEARCH, Arrays.asList(FONDS, SERIES)));
      String stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertEquals(String.format("((%s: \"%s\") OR (%s: \"%s\"))", RodaConstants.INDEX_SEARCH, FONDS,
        RodaConstants.INDEX_SEARCH, SERIES), stringFilter);
    } catch (RODAException e) {
      Assert.fail("An exception was not expected!");
    }
  }

  @Test
  private void testParserWithOneLikeFilterParameter() {
    try {
      Filter filter = new Filter();
      filter.add(new LikeFilterParameter());
      SolrUtils.parseFilter(filter);
      Assert.fail("An exception should have been thrown but it wasn't!");
    } catch (RequestNotValidException e) {
      // do nothing as it was expected
    }
  }

  @Test
  private void testParserWithOneEmptyDateRangeFilterParameter() {
    try {
      Filter filter = new Filter();
      filter.add(new DateRangeFilterParameter());
      String stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertTrue(stringFilter.isEmpty());
    } catch (RequestNotValidException e) {
      // do nothing as it was expected
    }
  }

  @Test
  private void testParserWithOneBasicSearchFilterParameter() {
    try {
      Filter filter = new Filter();
      filter.add(new BasicSearchFilterParameter(RodaConstants.INDEX_SEARCH, FONDS + " " + SERIES));
      String stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertEquals(
        String.format("(%s: (%s) AND %s: (%s))", RodaConstants.INDEX_SEARCH, FONDS, RodaConstants.INDEX_SEARCH, SERIES),
        stringFilter);
    } catch (RODAException e) {
      Assert.fail("An exception was not expected!");
    }
  }

  @Test
  private void testParserWithOneEmptyKeyFilterParameter() {
    try {
      Filter filter = new Filter();
      filter.add(new EmptyKeyFilterParameter(RodaConstants.INDEX_SEARCH));
      String stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertEquals(String.format("(*:* NOT %s:*)", RodaConstants.INDEX_SEARCH), stringFilter);
    } catch (RODAException e) {
      Assert.fail("An exception was not expected!");
    }
  }

  @Test
  private void testParserWithOneEmptyLongRangeFilterParameter() {
    try {
      Filter filter = new Filter();
      filter.add(new LongRangeFilterParameter());
      String stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertTrue(stringFilter.isEmpty());
    } catch (RequestNotValidException e) {
      // do nothing as it was expected
    }
  }

  @Test
  private void testParserWithOneLongRangeFilterParameter() {
    try {
      Filter filter = new Filter();
      filter.add(new LongRangeFilterParameter(RodaConstants.LOG_DURATION, 1L, 5L));
      String stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertThat(stringFilter, Matchers.is(String.format("(%s:[%s TO %s])", RodaConstants.LOG_DURATION, 1L, 5L)));
    } catch (RequestNotValidException e) {
      // do nothing as it was expected
    }
  }

  @Test
  public void testParseWithOneDateIntervalFilterParameter() {
    try {
      Filter filter = new Filter();
      filter.add(new DateIntervalFilterParameter());
      String stringFilter = SolrUtils.parseFilter(filter);
      assertNotNull(stringFilter);
      assertTrue(stringFilter.isEmpty());
    } catch (RequestNotValidException e) {
      // do nothing as it was expected
    }
  }

  @Test
  public void testParseSorter() {
    Sorter sorter = null;
    List<SortClause> sortList;
    String field1 = "field1";
    String field2 = "field2";
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

  @Test
  public void testDateParser() throws ParseException {
    String test1 = "2018-06-22T00:00:00Z";

    Date parsedDate = SolrUtils.parseDate(test1);
    String formatedDate = SolrUtils.formatDate(parsedDate);

    Assert.assertEquals(formatedDate, test1);
  }

  @Test
  public void testDateParserOldDates() throws ParseException {
    String test1 = "1213-01-01T00:00:00Z";

    Date parsedDate = SolrUtils.parseDate(test1);

    Instant dateInitial = parsedDate.toInstant();
    LocalDateTime ldt = LocalDateTime.ofInstant(dateInitial, ZoneOffset.UTC);
    ;

    Assert.assertEquals(1213, ldt.getYear());

    Calendar calInitial = Calendar.getInstance();
    calInitial.setTime(parsedDate);
    Assert.assertEquals(1213, calInitial.get(Calendar.YEAR));

    String formatedDate = SolrUtils.formatDate(parsedDate);

    Assert.assertEquals(formatedDate, test1);

  }

}
