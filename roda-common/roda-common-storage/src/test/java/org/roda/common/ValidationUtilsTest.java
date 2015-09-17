package org.roda.common;

import static org.junit.Assert.assertEquals;

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
import org.roda.index.IndexServiceTest;
import org.roda.model.AIP;
import org.roda.model.DescriptiveMetadata;
import org.roda.model.ModelService;
import org.roda.model.ModelServiceException;
import org.roda.model.ModelServiceTest;
import org.roda.model.ValidationException;
import org.roda.storage.DefaultStoragePath;
import org.roda.storage.StorageService;
import org.roda.storage.StorageServiceException;
import org.roda.storage.fs.FSUtils;
import org.roda.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationUtilsTest {
  private static Path basePath;
  private static Path indexPath;
  private static StorageService storage;
  private static ModelService model;

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
  public void testValidateDescriptiveMetadata()
    throws ModelServiceException, StorageServiceException, IndexServiceException, ValidationException {
    final String aipId = UUID.randomUUID().toString();
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
    final DescriptiveMetadata descMetadata = model.retrieveDescriptiveMetadata(aipId,
      CorporaConstants.DESCRIPTIVE_METADATA_ID);
    assertEquals(ValidationUtils.isDescriptiveMetadataValid(model, descMetadata, true), true);
  }

  @Test
  public void testValidateDescriptiveMetadataBuggy()
    throws ModelServiceException, StorageServiceException, IndexServiceException, ValidationException {
    // buggy aip have acqinfo2 instead of acqinfo in ead-c.xml
    final String aipId = UUID.randomUUID().toString();
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_BUGGY_ID));
    final DescriptiveMetadata descMetadata = model.retrieveDescriptiveMetadata(aipId,
      CorporaConstants.DESCRIPTIVE_METADATA_ID);
    assertEquals(ValidationUtils.isDescriptiveMetadataValid(model, descMetadata, true), false);
  }

  @Test
  public void testValidateAIP()
    throws ModelServiceException, StorageServiceException, IndexServiceException, ValidationException {
    final String aipId = UUID.randomUUID().toString();
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
    assertEquals(ValidationUtils.isAIPDescriptiveMetadataValid(model, aip.getId(), true), true);
  }

  @Test
  public void testValidateAIPBuggy()
    throws ModelServiceException, StorageServiceException, IndexServiceException, ValidationException {
    // buggy aip have acqinfo2 instead of acqinfo in ead-c.xml
    final String aipId = UUID.randomUUID().toString();
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_BUGGY_ID));
    assertEquals(ValidationUtils.isAIPDescriptiveMetadataValid(model, aip.getId(), true), false);
  }
}
