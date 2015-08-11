package org.roda.common;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roda.CorporaConstants;
import org.roda.index.IndexServiceException;
import org.roda.index.IndexService;
import org.roda.index.IndexServiceTest;
import org.roda.model.AIP;
import org.roda.model.DescriptiveMetadata;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.ModelServiceTest;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.StorageServiceException;
import org.roda.storage.StorageService;
import org.roda.storage.fs.FSUtils;
import org.roda.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.gov.dgarq.roda.core.data.v2.EventPreservationObject;
import pt.gov.dgarq.roda.core.data.v2.SimpleEventPreservationMetadata;
import pt.gov.dgarq.roda.core.data.v2.SimpleRepresentationFilePreservationMetadata;

public class HtmlUtilsTest {
	private static Path basePath;
	private static Path indexPath;
	private static StorageService storage;
	private static ModelService model;
	private static IndexService index;

	private static Path corporaPath;
	private static StorageService corporaService;

	private static final Logger logger = LoggerFactory.getLogger(ModelServiceTest.class);

	@BeforeClass
	public static void setUp() throws IOException, StorageServiceException, URISyntaxException {

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
		System.setProperty("solr.data.dir.preservationobject", indexPath.resolve("preservationobject").toString());
		System.setProperty("solr.data.dir.preservationevent", indexPath.resolve("preservationevent").toString());
		// start embedded solr
		final EmbeddedSolrServer solr = new EmbeddedSolrServer(solrHome, "test");

		index = new IndexService(solr, model);

		URL corporaURL = IndexServiceTest.class.getResource("/corpora");
		corporaPath = Paths.get(corporaURL.toURI());
		corporaService = new FileStorageService(corporaPath);

		logger.debug("Running model test under storage: " + basePath);
	}

	@AfterClass
	public static void tearDown() throws StorageServiceException {
		FSUtils.deletePath(basePath);
		FSUtils.deletePath(indexPath);
	}

	@Test
	public void testRepresentationFilePreservationObjectToHtml()
			throws ModelServiceException, StorageServiceException, IndexServiceException {
		final String aipId = UUID.randomUUID().toString();
		final AIP aip = model.createAIP(aipId, corporaService,
				DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
		SimpleRepresentationFilePreservationMetadata srfm = index.retrieve(
				SimpleRepresentationFilePreservationMetadata.class, aipId, CorporaConstants.REPRESENTATION_1_ID,
				CorporaConstants.F0_PREMIS_XML);
		// Element html = HTMLUtils.preservationObjectFromStorageToHtml(srfm,
		// model, new Locale("pt", "PT"));
		// logger.debug("HTML: " + html);
		// Element representationFilePreservationElement = html
		// .getElementsByAttributeValueMatching(CorporaConstants.HTML_TYPE,
		// CorporaConstants.HTML_PREMIS).get(0);
		// Element fieldElement =
		// representationFilePreservationElement.getElementsByAttributeValueMatching(
		// CorporaConstants.HTML_FIELD,
		// CorporaConstants.HTML_PRESERVATION_LEVEL).get(0);
		// Element fieldValueElement = fieldElement
		// .getElementsByAttributeValue(CorporaConstants.HTML_TYPE,
		// CorporaConstants.HTML_VALUE).get(0);
		// assertEquals(fieldValueElement.text(), CorporaConstants.HTML_FULL);
	}

	@Test
	public void testDescriptiveMetadataToHtml()
			throws ModelServiceException, StorageServiceException, IndexServiceException {
		final String aipId = UUID.randomUUID().toString();
		final AIP aip = model.createAIP(aipId, corporaService,
				DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
		final DescriptiveMetadata descMetadata = model.retrieveDescriptiveMetadata(aipId,
				CorporaConstants.DESCRIPTIVE_METADATA_ID);

		// Element html2 = HTMLUtils.descriptiveMetadataToHtml(descMetadata,
		// model, new Locale("pt", "PT"));
		// Element descriptiveMetadataElement2 =
		// html2.getElementsByAttributeValueMatching(CorporaConstants.HTML_TYPE,
		// CorporaConstants.HTML_DESCRIPTIVE_METADATA).get(0);
		// Element fieldElement2 =
		// descriptiveMetadataElement2.getElementsByAttributeValueMatching(CorporaConstants.HTML_FIELD,
		// CorporaConstants.HTML_LEVEL).get(0);
		// Element fieldValueElement2 =
		// fieldElement2.getElementsByAttributeValue(CorporaConstants.HTML_TYPE,
		// CorporaConstants.HTML_VALUE).get(0);
		// assertEquals(fieldValueElement2.text(),CorporaConstants.HTML_FONDS);
		//
		//
		// final DescriptiveMetadata descMetadata2 =
		// model.retrieveDescriptiveMetadata(aipId,
		// CorporaConstants.DESCRIPTIVE_METADATA_ID);
		// Element html3 = HTMLUtils.descriptiveMetadataToHtml(descMetadata2,
		// model,new Locale("pt", "PT"));
		// Element descriptiveMetadataElement3 =
		// html3.getElementsByAttributeValueMatching(CorporaConstants.HTML_TYPE,
		// CorporaConstants.HTML_DESCRIPTIVE_METADATA).get(0);
		// Element fieldElement3 =
		// descriptiveMetadataElement3.getElementsByAttributeValueMatching(CorporaConstants.HTML_FIELD,
		// CorporaConstants.HTML_TITLE).get(0);
		// Element fieldValueElement3 =
		// fieldElement3.getElementsByAttributeValue(CorporaConstants.HTML_TYPE,
		// CorporaConstants.HTML_VALUE).get(0);
		// assertEquals(fieldValueElement3.text(),CorporaConstants.HTML_MY_EXAMPLE);
	}
	/*
	 * @Test public void testRepresentationPreservationObjectToHtml() throws
	 * ModelServiceException, StorageActionException, IndexActionException {
	 * final String aipId = UUID.randomUUID().toString(); final AIP aip =
	 * model.createAIP(aipId, corporaService,
	 * DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER,
	 * CorporaConstants.SOURCE_AIP_ID));
	 * SimpleRepresentationPreservationMetadata srpm =
	 * index.retrieveSimpleRepresentationPreservationMetadata(aipId,
	 * CorporaConstants.REPRESENTATION_1_ID,
	 * CorporaConstants.REPRESENTATION_PREMIS_XML); Element html =
	 * HTMLUtils.representationPreservationObjectToHtml(new
	 * RepresentationPreservationObject(srpm)); System.out.println(html);
	 * 
	 * Element representationFilePreservationElement =
	 * html.getElementsByAttributeValueMatching("type",
	 * "representationPreservationObject").get(0); Element fieldElement =
	 * representationFilePreservationElement.getElementsByAttributeValueMatching
	 * ("field", "aipID").get(0); Element fieldValueElement =
	 * fieldElement.getElementsByAttributeValue("type", "value").get(0);
	 * assertEquals(fieldValueElement.text(),aipId); }
	 */

	@Test
	public void testRepresentationPreservationObjectToHtml()
			throws ModelServiceException, StorageServiceException, IndexServiceException {
		final String aipId = UUID.randomUUID().toString();
		final AIP aip = model.createAIP(aipId, corporaService,
				DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
		SimpleEventPreservationMetadata sepm = index.retrieve(SimpleEventPreservationMetadata.class, aipId,
				CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.EVENT_RODA_398_PREMIS_XML);
		// Element html =
		// HTMLUtils.preservationObjectFromStorageToHtml(sepm,model,new
		// Locale("pt","PT"));
		// logger.debug("HTML: "+html);
		// Element representationFilePreservationElement =
		// html.getElementsByAttributeValueMatching(CorporaConstants.HTML_TYPE,
		// CorporaConstants.HTML_PREMIS).get(0);
		// Element fieldElement =
		// representationFilePreservationElement.getElementsByAttributeValueMatching(CorporaConstants.HTML_FIELD,
		// CorporaConstants.HTML_EVENT_TYPE).get(0);
		// Element fieldValueElement =
		// fieldElement.getElementsByAttributeValue(CorporaConstants.HTML_TYPE,
		// CorporaConstants.HTML_VALUE).get(0);
		// assertEquals(fieldValueElement.text(),CorporaConstants.HTML_INGESTION);
	}

	@Test
	public void testRepresentationFilePreservationObjectFromStorageToHtml()
			throws ModelServiceException, StorageServiceException, IndexServiceException {
		final String aipId = UUID.randomUUID().toString();
		final AIP aip = model.createAIP(aipId, corporaService,
				DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
		SimpleRepresentationFilePreservationMetadata srfm = index.retrieve(
				SimpleRepresentationFilePreservationMetadata.class, aipId, CorporaConstants.REPRESENTATION_1_ID,
				CorporaConstants.F0_PREMIS_XML);
		// Element html = HTMLUtils.preservationObjectFromStorageToHtml(new
		// RepresentationFilePreservationObject(srfm),model,new Locale("pt",
		// "PT"));
		// Element premisElement =
		// html.getElementsByAttributeValueMatching(CorporaConstants.HTML_TYPE,
		// CorporaConstants.HTML_PREMIS).get(0);
		// Element fieldElement =
		// premisElement.getElementsByAttributeValueMatching(CorporaConstants.HTML_FIELD,
		// CorporaConstants.HTML_PRESERVATION_LEVEL).get(0);
		// Element fieldValueElement =
		// fieldElement.getElementsByAttributeValue(CorporaConstants.HTML_TYPE,
		// CorporaConstants.HTML_VALUE).get(0);
		// assertEquals(fieldValueElement.text(),CorporaConstants.HTML_FULL);
	}

	@Test
	public void testEventPreservationObjectFromStorageToHtml()
			throws ModelServiceException, StorageServiceException, IndexServiceException {
		final String aipId = UUID.randomUUID().toString();
		final AIP aip = model.createAIP(aipId, corporaService,
				DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
		EventPreservationObject epo = model.retrieveEventPreservationObject(aipId, CorporaConstants.REPRESENTATION_1_ID,
				CorporaConstants.EVENT_RODA_398_PREMIS_XML);
		// Element html =
		// HTMLUtils.preservationObjectFromStorageToHtml(epo,model,new
		// Locale("pt", "PT"));
		// logger.debug("HTML: "+html);
		// Element premisElement =
		// html.getElementsByAttributeValueMatching(CorporaConstants.HTML_TYPE,
		// CorporaConstants.HTML_PREMIS).get(0);
		// Element fieldElement =
		// premisElement.getElementsByAttributeValueMatching(CorporaConstants.HTML_FIELD,
		// CorporaConstants.HTML_EVENT_TYPE).get(0);
		// Element fieldValueElement =
		// fieldElement.getElementsByAttributeValue(CorporaConstants.HTML_TYPE,
		// CorporaConstants.HTML_VALUE).get(0);
		// assertEquals(fieldValueElement.text(),CorporaConstants.HTML_INGESTION);
	}

}
