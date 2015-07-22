package org.roda.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roda.CorporaConstants;
import org.roda.index.utils.SolrUtils;
import org.roda.storage.Binary;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.StorageActionException;
import org.roda.storage.StorageService;
import org.roda.storage.fs.FileStorageService;

import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.ClassificationSchemeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.LikeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.OneOfManyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.ProducerFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RangeFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.RegexFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;


public class SolrUtilsTest {

	private static StorageService corporaService;

	@BeforeClass
	public static void setUp() throws StorageActionException, URISyntaxException {
		URL corporaURL = IndexServiceTest.class.getResource("/corpora");
		Path corporaPath = Paths.get(corporaURL.toURI());
		corporaService = new FileStorageService(corporaPath);
	}

	@Test
	public void testGetDescriptiveMetataFields() throws StorageActionException, IndexActionException {
		final DefaultStoragePath strangeMetadataPath = DefaultStoragePath
				.parse(CorporaConstants.SOURCE_DESC_METADATA_CONTAINER, CorporaConstants.STRANGE_DESC_METADATA_FILE);
		Binary strangeMetadata = corporaService.getBinary(strangeMetadataPath);

		SolrInputDocument descriptiveMetataFields = SolrUtils.getDescriptiveMetataFields(strangeMetadata);

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
		String[] array = new String[] { fonds, series };

		// 1) null filter
		try {
			stringFilter = SolrUtils.parseFilter(filter);
			assertNotNull(stringFilter);
			assertEquals("*:*", stringFilter);
		} catch (IndexActionException e) {
			fail("An exception was not expected!");
		}

		// 2) empty filter
		try {
			filter = new Filter();
			stringFilter = SolrUtils.parseFilter(filter);
			assertNotNull(stringFilter);
			assertEquals("*:*", stringFilter);
		} catch (IndexActionException e) {
			fail("An exception was not expected!");
		}

		// 3) filter with one SimpleFilterParameter (uses exact match)
		try {
			filter = new Filter();
			filter.add(new SimpleFilterParameter(RodaConstants.SDO__ALL, fonds));
			stringFilter = SolrUtils.parseFilter(filter);
			assertNotNull(stringFilter);
			assertEquals(String.format("(%s: \"%s\")", RodaConstants.SDO__ALL, fonds), stringFilter);
		} catch (IndexActionException e) {
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
			assertEquals(String.format("(%s: \"%s\") AND (%s: \"%s\")", RodaConstants.SDO__ALL, fonds,
					RodaConstants.SDO__ALL, series), stringFilter);
		} catch (IndexActionException e) {
			fail("An exception was not expected!");
		}

		// 5) filter with one OneOfManyFilterParameter (uses exact match for
		// each of the values, and they will be combined using OR operator)
		try {
			filter = new Filter();
			filter.add(new OneOfManyFilterParameter(RodaConstants.SDO__ALL, array));
			stringFilter = SolrUtils.parseFilter(filter);
			assertNotNull(stringFilter);
			assertEquals(String.format("((%s: \"%s\") OR (%s: \"%s\"))", RodaConstants.SDO__ALL, fonds,
					RodaConstants.SDO__ALL, series), stringFilter);
		} catch (IndexActionException e) {
			fail("An exception was not expected!");
		}

		// 6) filter with one ClassificationSchemeFilterParameter
		try {
			filter = new Filter();
			filter.add(new ClassificationSchemeFilterParameter());
			stringFilter = SolrUtils.parseFilter(filter);
			fail("An exception should have been thrown but it wasn't!");
		} catch (IndexActionException e) {
			assertEquals(IndexActionException.BAD_REQUEST, e.getCode());
		}

		// 7) filter with one LikeFilterParameter
		try {
			filter = new Filter();
			filter.add(new LikeFilterParameter());
			stringFilter = SolrUtils.parseFilter(filter);
			fail("An exception should have been thrown but it wasn't!");
		} catch (IndexActionException e) {
			assertEquals(IndexActionException.BAD_REQUEST, e.getCode());
		}

		// 8) filter with one ProducerFilterParameter
		try {
			filter = new Filter();
			filter.add(new ProducerFilterParameter());
			stringFilter = SolrUtils.parseFilter(filter);
			fail("An exception should have been thrown but it wasn't!");
		} catch (IndexActionException e) {
			assertEquals(IndexActionException.BAD_REQUEST, e.getCode());
		}

		// 9) filter with one RangeFilterParameter
		try {
			filter = new Filter();
			filter.add(new RangeFilterParameter());
			stringFilter = SolrUtils.parseFilter(filter);
			fail("An exception should have been thrown but it wasn't!");
		} catch (IndexActionException e) {
			assertEquals(IndexActionException.BAD_REQUEST, e.getCode());
		}

		// 10) filter with one RegexFilterParameter
		try {
			filter = new Filter();
			filter.add(new RegexFilterParameter());
			stringFilter = SolrUtils.parseFilter(filter);
			fail("An exception should have been thrown but it wasn't!");
		} catch (IndexActionException e) {
			assertEquals(IndexActionException.BAD_REQUEST, e.getCode());
		}

		// 11) filter with one BasicSearchFilterParameter
		try {
			filter = new Filter();
			filter.add(new BasicSearchFilterParameter(RodaConstants.SDO__ALL, fondsOrSeries));
			stringFilter = SolrUtils.parseFilter(filter);
			assertNotNull(stringFilter);
			assertEquals(
					String.format("(%s: %s OR %s: %s)", RodaConstants.SDO__ALL, fonds, RodaConstants.SDO__ALL, series),
					stringFilter);
		} catch (IndexActionException e) {
			fail("An exception was not expected!");
		}

		// 12) filter with one EmptyKeyFilterParameter
		try {
			filter = new Filter();
			filter.add(new EmptyKeyFilterParameter(RodaConstants.SDO__ALL));
			stringFilter = SolrUtils.parseFilter(filter);
			assertNotNull(stringFilter);
			assertEquals(String.format("(%s: \"\")", RodaConstants.SDO__ALL), stringFilter);
		} catch (IndexActionException e) {
			fail("An exception was not expected!");
		}

	}

}
