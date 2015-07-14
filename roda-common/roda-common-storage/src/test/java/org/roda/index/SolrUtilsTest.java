package org.roda.index;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roda.CorporaConstants;
import org.roda.common.RodaConstants;
import org.roda.index.utils.SolrUtils;
import org.roda.storage.Binary;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.StorageActionException;
import org.roda.storage.StorageService;
import org.roda.storage.fs.FileStorageService;

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

}
