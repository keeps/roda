package org.roda.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roda.CorporaConstants;
import org.roda.common.RodaConstants;
import org.roda.index.filter.Filter;
import org.roda.index.filter.SimpleFilterParameter;
import org.roda.index.sublist.Sublist;
import org.roda.legacy.aip.metadata.RODAObject;
import org.roda.legacy.aip.metadata.descriptive.SimpleDescriptionObject;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.ModelServiceTest;
import org.roda.model.Representation;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.StorageActionException;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.fs.FSUtils;
import org.roda.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexServiceTest {

	private static Path basePath;
	private static Path indexPath;
	private static StorageService storage;
	private static ModelService model;
	private static IndexService index;

	private static Path corporaPath;
	private static StorageService corporaService;

	private static final Logger logger = LoggerFactory.getLogger(ModelServiceTest.class);

	@BeforeClass
	public static void setUp() throws IOException, StorageActionException, URISyntaxException {

		basePath = Files.createTempDirectory("modelTests");
		indexPath = Files.createTempDirectory("indexTests");
		storage = new FileStorageService(basePath);
		model = new ModelService(storage);

		// Configure Solr
		URL solrConfigURL = IndexServiceTest.class.getResource("/index/solr.xml");
		Path solrConfigPath = Paths.get(solrConfigURL.toURI());
		Files.copy(solrConfigPath, indexPath.resolve("solr.xml"));
		Path aipSchema = indexPath.resolve("aip");
		Files.createDirectories(aipSchema);
		Files.createFile(aipSchema.resolve("core.properties"));

		Path solrHome = Paths.get(IndexServiceTest.class.getResource("/index/").toURI());
		System.setProperty("solr.data.dir", indexPath.toString());
		System.setProperty("solr.data.dir.aip", indexPath.resolve("aip").toString());
		System.setProperty("solr.data.dir.sdo", indexPath.resolve("sdo").toString());
		System.setProperty("solr.data.dir.representation", indexPath.resolve("representation").toString());

		// start embedded solr
		final EmbeddedSolrServer solr = new EmbeddedSolrServer(solrHome, "test");

		index = new IndexService(solr, model);

		URL corporaURL = IndexServiceTest.class.getResource("/corpora");
		corporaPath = Paths.get(corporaURL.toURI());
		corporaService = new FileStorageService(corporaPath);

		logger.debug("Running model test under storage: " + basePath);
	}

	@AfterClass
	public static void tearDown() throws StorageActionException {
		FSUtils.deletePath(basePath);
		FSUtils.deletePath(indexPath);
	}

	@Test
	public void testAIPIndexCreateDelete() throws ModelServiceException, StorageActionException, IndexActionException {
		// generate AIP ID
		final String aipId = UUID.randomUUID().toString();

		// Create AIP
		final AIP aip = model.createAIP(aipId, corporaService,
				DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

		// Retrieve, count and list AIP
		final AIP indexedAIP = index.retrieveAIP(aipId);
		assertEquals(aip, indexedAIP);

		final long countAIP = index.countAIP(null);
		assertEquals(1, countAIP);

		final IndexResult<AIP> aipList = index.findAIP(null, null, new Sublist(0, 10));
		assertEquals(1, aipList.getTotalCount());
		assertEquals(1, aipList.getLimit());
		assertEquals(0, aipList.getOffset());
		assertEquals(1, aipList.getResults().size());
		assertEquals(aip, aipList.getResults().get(0));

		// Retrieve, count and list SDO
		final SimpleDescriptionObject sdo = index.retrieveDescriptiveMetadata(aipId);
		assertEquals(aip.getId(), sdo.getId());
		assertEquals(aip.isActive(), RODAObject.STATE_ACTIVE.equals(sdo.getState()));
		assertEquals(aip.getParentId(), sdo.getParentID());
		assertEquals(aip.getDateCreated(), sdo.getCreatedDate());
		assertEquals(aip.getDateModified(), sdo.getLastModifiedDate());

		final IndexResult<SimpleDescriptionObject> sdos = index.findDescriptiveMetadata(null, null, new Sublist(0, 10));
		assertEquals(1, sdos.getTotalCount());
		assertEquals(1, sdos.getLimit());
		assertEquals(0, sdos.getOffset());
		assertEquals(1, sdos.getResults().size());

		final SimpleDescriptionObject sdoFromList = sdos.getResults().get(0);
		assertEquals(aip.getId(), sdoFromList.getId());
		assertEquals(aip.isActive(), RODAObject.STATE_ACTIVE.equals(sdoFromList.getState()));
		assertEquals(aip.getParentId(), sdoFromList.getParentID());
		assertEquals(aip.getDateCreated(), sdoFromList.getCreatedDate());
		assertEquals(aip.getDateModified(), sdoFromList.getLastModifiedDate());

		assertEquals(sdo, sdoFromList);
		assertEquals("fonds", sdo.getLevel());
		assertEquals("My example", sdo.getTitle());
		assertEquals("This is a very nice example", sdo.getDescription());
		// assertEquals("0001-01-01", sdo.getDateInitial());
		// assertEquals("0002-01-01", sdo.getDateFinal());

		// Retrieve, count and list SRO
		String rep1Id = aip.getRepresentationIds().get(0);
		Representation rep1 = index.retrieveRepresentation(aipId, rep1Id);
		assertEquals(rep1Id, rep1.getId());

		Filter filterParentTheAIP = new Filter();
		filterParentTheAIP.add(new SimpleFilterParameter(RodaConstants.SRO_AIP_ID, aipId));
		IndexResult<Representation> sros = index.findRepresentation(filterParentTheAIP, null, new Sublist(0, 10));
		assertEquals(aip.getRepresentationIds().size(), sros.getTotalCount());

		List<String> sro_IDs = new ArrayList<>();
		for (Representation sro : sros.getResults()) {
			sro_IDs.add(sro.getId());
		}

		assertThat(sro_IDs, Matchers.contains(aip.getRepresentationIds().toArray()));

		model.deleteAIP(aipId);
		try {
			index.retrieveAIP(aipId);
			fail("AIP deleted but yet it was retrieved");
		} catch (IndexActionException e) {
			assertEquals(IndexActionException.NOT_FOUND, e.getCode());
		}

		try {
			index.retrieveDescriptiveMetadata(aipId);
			fail("AIP was deleted but yet its descriptive metadata was retrieved");
		} catch (IndexActionException e) {
			assertEquals(IndexActionException.NOT_FOUND, e.getCode());
		}
	}

	@Test
	public void testAIPUpdate() throws ModelServiceException, StorageActionException, IndexActionException {
		// generate AIP ID
		final String aipId = UUID.randomUUID().toString();

		// testing AIP
		model.createAIP(aipId, corporaService,
				DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

		final StoragePath otherAipPath = DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER,
				CorporaConstants.OTHER_AIP_ID);
		final AIP updatedAIP = model.updateAIP(aipId, corporaService, otherAipPath);

		final AIP indexedAIP = index.retrieveAIP(aipId);
		assertEquals(updatedAIP, indexedAIP);

		model.deleteAIP(aipId);
	}

	@Test
	public void testListCollections() throws ModelServiceException, StorageActionException, IndexActionException {
		// set up
		model.createAIP(CorporaConstants.SOURCE_AIP_ID, corporaService,
				DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
		model.createAIP(CorporaConstants.OTHER_AIP_ID, corporaService,
				DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.OTHER_AIP_ID));

		long sdoCount = index.countDescriptiveMetadata(SimpleDescriptionObject.FONDS_FILTER);
		assertEquals(1, sdoCount);

		final IndexResult<SimpleDescriptionObject> sdos = index
				.findDescriptiveMetadata(SimpleDescriptionObject.FONDS_FILTER, null, new Sublist(0, 10));

		assertEquals(1, sdos.getLimit());
		assertEquals(CorporaConstants.SOURCE_AIP_ID, sdos.getResults().get(0).getId());

		model.deleteAIP(CorporaConstants.SOURCE_AIP_ID);
		model.deleteAIP(CorporaConstants.OTHER_AIP_ID);
	}

	@Test
	public void testSubElements() throws ModelServiceException, StorageActionException, IndexActionException {
		// set up
		model.createAIP(CorporaConstants.SOURCE_AIP_ID, corporaService,
				DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
		model.createAIP(CorporaConstants.OTHER_AIP_ID, corporaService,
				DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.OTHER_AIP_ID));

		Filter filter = new Filter();
		filter.add(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, CorporaConstants.SOURCE_AIP_ID));

		long sdoCount = index.countDescriptiveMetadata(filter);
		assertEquals(1, sdoCount);

		final IndexResult<SimpleDescriptionObject> sdos = index.findDescriptiveMetadata(filter, null,
				new Sublist(0, 10));

		assertEquals(1, sdos.getLimit());
		assertEquals(CorporaConstants.OTHER_AIP_ID, sdos.getResults().get(0).getId());

		model.deleteAIP(CorporaConstants.SOURCE_AIP_ID);
		model.deleteAIP(CorporaConstants.OTHER_AIP_ID);
	}

	@Test
	public void testGetAncestors() throws ModelServiceException, StorageActionException, IndexActionException {
		// set up
		model.createAIP(CorporaConstants.SOURCE_AIP_ID, corporaService,
				DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
		model.createAIP(CorporaConstants.OTHER_AIP_ID, corporaService,
				DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.OTHER_AIP_ID));

		List<String> ancestors = index.getAncestors(CorporaConstants.OTHER_AIP_ID);
		assertEquals(Arrays.asList(CorporaConstants.SOURCE_AIP_ID), ancestors);
	}

}
