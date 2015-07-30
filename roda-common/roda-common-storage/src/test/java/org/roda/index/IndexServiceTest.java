package org.roda.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roda.CorporaConstants;
import org.roda.common.RodaUtils;
import org.roda.model.AIP;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.ModelServiceTest;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.StorageActionException;
import org.roda.storage.StoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.fs.FSUtils;
import org.roda.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.gov.dgarq.roda.core.common.RodaConstants;
import pt.gov.dgarq.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.filter.SimpleFilterParameter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.LogEntryParameter;
import pt.gov.dgarq.roda.core.data.v2.RODAObject;
import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.core.data.v2.SimpleEventPreservationMetadata;
import pt.gov.dgarq.roda.core.data.v2.SimpleRepresentationFilePreservationMetadata;

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
	public static void setUp() throws IOException, StorageActionException, URISyntaxException, ModelServiceException {

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
		System.setProperty("solr.data.dir.representations", indexPath.resolve("representation").toString());
		System.setProperty("solr.data.dir.preservationobject", indexPath.resolve("preservationobject").toString());
		System.setProperty("solr.data.dir.preservationevent", indexPath.resolve("preservationevent").toString());
		System.setProperty("solr.data.dir.actionlog", indexPath.resolve("actionlog").toString());
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
	public void testAIPIndexCreateDelete()
			throws ModelServiceException, StorageActionException, IndexActionException, ParseException {
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
		assertEquals(RodaUtils.parseDate("0001-01-01T00:00:00.000+0000"), sdo.getDateInitial());
		assertEquals(RodaUtils.parseDate("0002-01-01T00:00:00.000+0000"), sdo.getDateFinal());

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

		SimpleEventPreservationMetadata sepm = index.retrieveSimpleEventPreservationMetadata(aipId,
				CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.EVENT_RODA_398_PREMIS_XML);
		assertEquals(sepm.getType(), CorporaConstants.INGESTION);
		Filter filterType = new Filter();
		filterType.add(new SimpleFilterParameter(RodaConstants.SEPM_TYPE, CorporaConstants.INGESTION));
		assertThat(index.countSimpleEventPreservationMetadata(filterType), Matchers.equalTo(1L));
		assertThat(index.findSimpleEventPreservationMetadata(filterType, null, new Sublist(0, 10)).getTotalCount(),
				Matchers.equalTo(1L));

		SimpleRepresentationFilePreservationMetadata srfpm = index.retrieveSimpleRepresentationFilePreservationMetadata(
				aipId, CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.F0_PREMIS_XML);
		assertEquals(srfpm.getAipId(), aipId);
		Filter filterAIPID = new Filter();
		filterAIPID.add(new SimpleFilterParameter(RodaConstants.SRFM_AIP_ID, aipId));
		assertThat(index.countSimpleRepresentationFilePreservationMetadata(filterAIPID), Matchers.equalTo(4L));
		/*
		 * filterMimetype.add(new
		 * SimpleFilterParameter(RodaConstants.SRFM_MIMETYPE,
		 * CorporaConstants.TEXT_XML));
		 * assertEquals(index.findSimpleEventPreservationMetadata(
		 * filterMimetype, null, new Sublist(0, 10)).getTotalCount(),1L);
		 */

		/*
		 * SimpleRepresentationPreservationMetadata srpm =
		 * index.retrieveSimpleRepresentationPreservationMetadata(aipId,
		 * CorporaConstants.REPRESENTATION_1_ID,CorporaConstants.
		 * REPRESENTATION_PREMIS_XML); assertEquals(srpm.getAipId(), aipId);
		 * assertEquals(srpm.getFileId(),CorporaConstants.
		 * REPRESENTATION_PREMIS_XML); Filter filterFileId = new Filter();
		 * filterFileId.add(new
		 * SimpleFilterParameter(RodaConstants.SRPM_FILE_ID,
		 * CorporaConstants.REPRESENTATION_PREMIS_XML));
		 * assertEquals(""+index.countSimpleRepresentationPreservationMetadata(
		 * filterFileId),""+1L);
		 * assertEquals(index.findSimpleEventPreservationMetadata(filterFileId,
		 * null, new Sublist(0, 10)).getTotalCount(),1L);
		 */
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
	public void testAIPIndexCreateDelete2()
			throws ModelServiceException, StorageActionException, IndexActionException, ParseException {
		final String aipId = UUID.randomUUID().toString();

		final AIP aip = model.createAIP(aipId, corporaService,
				DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID_3));

		Filter filter = new Filter();
		filter.add(new SimpleFilterParameter(RodaConstants.AIP_ID, aipId));
		SimpleDescriptionObject sdo = index.findDescriptiveMetadata(filter, null, new Sublist(0, 10)).getResults()
				.get(0);
		Calendar calInitial = Calendar.getInstance();
		calInitial.setTime(sdo.getDateInitial());
		assertEquals(calInitial.get(Calendar.YEAR), CorporaConstants.YEAR_1213);
		Calendar calFinal = Calendar.getInstance();
		calFinal.setTime(sdo.getDateFinal());
		assertEquals(calFinal.get(Calendar.YEAR), CorporaConstants.YEAR_2003);

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

		List<SimpleDescriptionObject> ancestors = index.getAncestors(CorporaConstants.OTHER_AIP_ID);
		assertThat(ancestors, Matchers.hasItem(Matchers.<SimpleDescriptionObject> hasProperty("id",
				Matchers.equalTo(CorporaConstants.SOURCE_AIP_ID))));

		SimpleDescriptionObject sdo = index.retrieveDescriptiveMetadata(CorporaConstants.OTHER_AIP_ID);
		ancestors = index.getAncestors(sdo);
		assertThat(ancestors, Matchers.hasItem(Matchers.<SimpleDescriptionObject> hasProperty("id",
				Matchers.equalTo(CorporaConstants.SOURCE_AIP_ID))));
	}

	@Test
	public void testGetElementWithoutParentId()
			throws ModelServiceException, StorageActionException, IndexActionException {
		// generate AIP ID
		final String aipId = UUID.randomUUID().toString();

		// Create AIP
		model.createAIP(aipId, corporaService,
				DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

		Filter filter = new Filter();
		filter.add(new SimpleFilterParameter(RodaConstants.SDO_LEVEL, "fonds"));
		filter.add(new EmptyKeyFilterParameter(RodaConstants.AIP_PARENT_ID));
		IndexResult<SimpleDescriptionObject> findDescriptiveMetadata = index.findDescriptiveMetadata(filter, null,
				new Sublist());

		assertNotNull(findDescriptiveMetadata);
		assertThat(findDescriptiveMetadata.getResults(), Matchers.hasSize(1));

		// cleanup
		model.deleteAIP(aipId);
	}

	@Test
	public void testGetLogEntriesCount() throws StorageActionException, IndexActionException {
		LogEntry entry = new LogEntry();
		entry.setAction("Action");
		entry.setAddress("Address");
		entry.setDatetime("Datetime");
		entry.setDescription("Description");
		entry.setDuration(10L);
		entry.setId("ID");
		entry.setRelatedObjectID("Related");
		entry.setUsername("Username");
		LogEntryParameter[] parameters = new LogEntryParameter[2];
		LogEntryParameter p1 = new LogEntryParameter("NAME1", "VALUE1");
		LogEntryParameter p2 = new LogEntryParameter("NAME2", "VALUE2");
		parameters[0] = p1;
		parameters[1] = p2;
		entry.setParameters(parameters);
		model.addLogEntry(entry);

		Filter filterDescription = new Filter();
		filterDescription.add(new SimpleFilterParameter(RodaConstants.LOG_DESCRIPTION, "Description"));
		Long n = 1L;
		assertEquals(index.getLogEntriesCount(filterDescription), n);

		Filter filterDescription2 = new Filter();
		filterDescription2.add(new SimpleFilterParameter(RodaConstants.LOG_DESCRIPTION, "Description2"));
		Long n2 = 0L;
		assertEquals(index.getLogEntriesCount(filterDescription2), n2);
	}

	@Test
	public void testFindLogEntry() throws StorageActionException, IndexActionException {
		LogEntry entry = new LogEntry();
		entry.setAction("action");
		entry.setAddress("address");
		entry.setDatetime("datetime");
		entry.setDescription("description");
		entry.setDuration(10L);
		entry.setId("id");
		entry.setRelatedObjectID("related");
		entry.setUsername("username");
		LogEntryParameter[] parameters = new LogEntryParameter[2];
		LogEntryParameter p1 = new LogEntryParameter("NAME1", "VALUE1");
		LogEntryParameter p2 = new LogEntryParameter("NAME2", "VALUE2");
		parameters[0] = p1;
		parameters[1] = p2;
		entry.setParameters(parameters);
		model.addLogEntry(entry);

		Filter filterDescription = new Filter();
		filterDescription.add(new SimpleFilterParameter(RodaConstants.LOG_DESCRIPTION, "description"));

		IndexResult<LogEntry> entries = index.findLogEntry(filterDescription, null, new Sublist());
		assertEquals(entries.getTotalCount(), 1);
		assertEquals(entries.getResults().get(0).getAction(), CorporaConstants.LOG_ACTION);

		Filter filterDescription2 = new Filter();
		filterDescription2.add(new SimpleFilterParameter(RodaConstants.LOG_DESCRIPTION, "description2"));

		IndexResult<LogEntry> entries2 = index.findLogEntry(filterDescription2, null, new Sublist());
		assertEquals(entries2.getTotalCount(), 0);
	}

	@Test
	public void testReindexLogEntry() throws StorageActionException, ModelServiceException, IndexActionException {
		for (int i = 0; i < 100; i++) {
			LogEntry entry = new LogEntry();
			entry.setId("ID" + i);
			entry.setDescription("DESCRIPTION:" + i);
			entry.setAction("ACTION:" + i);
			entry.setAddress("ADDRESS");
			entry.setDatetime("DATETIME:" + i);
			entry.setDuration(i);
			entry.setRelatedObjectID("RELATED:" + i);
			entry.setUsername("USER:" + i);
			LogEntryParameter[] parameters = new LogEntryParameter[2];
			LogEntryParameter p1 = new LogEntryParameter("NAME1", "VALUE1");
			LogEntryParameter p2 = new LogEntryParameter("NAME2", "VALUE2");
			parameters[0] = p1;
			parameters[1] = p2;
			entry.setParameters(parameters);
			model.addLogEntry(entry, false);
		}
		model.reindexActionLogs();
		Filter f1 = new Filter();
		f1.add(new SimpleFilterParameter(CorporaConstants.LOG_ACTION, "ACTION:54"));
		IndexResult<LogEntry> entries1 = index.findLogEntry(f1, null, new Sublist(0, 10));
		assertEquals(entries1.getTotalCount(), 1L);
		Filter f2 = new Filter();
		f2.add(new SimpleFilterParameter(CorporaConstants.LOG_ADDRESS, "ADDRESS"));
		IndexResult<LogEntry> entries2 = index.findLogEntry(f2, null, new Sublist(0, 10));
		assertEquals(entries2.getTotalCount(), 100L);
	}

	@Test
	public void testReindexAIP()
			throws ModelServiceException, StorageActionException, IndexActionException, ParseException {
		for (int i = 0; i < 100; i++) {
			final String aipId = UUID.randomUUID().toString();
			final AIP aip = model.createAIP(aipId, corporaService,
					DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
					false);
		}

		model.reindexAIPs();
		long count = index.countAIP(new Filter());
		assertEquals(count, 100L);

	}
}
