/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.xml.validation.Schema;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.validation.ValidationUtils;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexServiceTest;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceTest;
import org.roda.core.plugins.plugins.base.DescriptiveMetadataValidationPlugin;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {"all", "travis"})
public class ValidationUtilsTest {
  private static Path basePath;
  private static ModelService model;
  private static IndexService index;

  private static Path corporaPath;
  private static StorageService corporaService;
  private static String aipCreator = "admin";

  private static final Logger logger = LoggerFactory.getLogger(ModelServiceTest.class);

  @BeforeClass
  public static void setUp() throws IOException, URISyntaxException, GenericException {
    basePath = TestsHelper.createBaseTempDir(ValidationUtilsTest.class, true);

    boolean deploySolr = true;
    boolean deployLdap = false;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources);

    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    // Configure Solr
    // URL solrConfigURL =
    // IndexServiceTest.class.getResource("/index/solr.xml");
    // Path solrConfigPath = Paths.get(solrConfigURL.toURI());
    // Files.copy(solrConfigPath, indexPath.resolve("solr.xml"));
    // Path aipSchema = indexPath.resolve("aip");
    // Files.createDirectories(aipSchema);
    // Files.createFile(aipSchema.resolve("core.properties"));

    // Path solrHome =
    // Paths.get(IndexServiceTest.class.getResource("/index/").toURI());
    // System.setProperty("solr.data.dir", indexPath.toString());
    // System.setProperty("solr.data.dir.aip",
    // indexPath.resolve("aip").toString());
    // System.setProperty("solr.data.dir.sdo",
    // indexPath.resolve("sdo").toString());
    // System.setProperty("solr.data.dir.representation",
    // indexPath.resolve("representation").toString());
    // System.setProperty("solr.data.dir.preservationobject",
    // indexPath.resolve("preservationobject").toString());
    // System.setProperty("solr.data.dir.preservationevent",
    // indexPath.resolve("preservationevent").toString());
    // start embedded solr
    // final EmbeddedSolrServer solr = new EmbeddedSolrServer(solrHome, "test");

    URL corporaURL = IndexServiceTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    logger.debug("Running model test under storage: " + basePath);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testValidateDescriptiveMetadata() throws ValidationException, RequestNotValidException, GenericException,
    AuthorizationDeniedException, AlreadyExistsException, NotFoundException {
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);
    final DescriptiveMetadata descMetadata = model.retrieveDescriptiveMetadata(aipId,
      CorporaConstants.DESCRIPTIVE_METADATA_ID);
    assertEquals(ValidationUtils.isDescriptiveMetadataValid(model, descMetadata, true).isValid(), true);
  }

  @Test(enabled = false)
  public void testValidateDescriptiveMetadataBuggy() throws RODAException {
    // buggy aip have acqinfo2 instead of acqinfo in ead.xml
    final String aipId = UUID.randomUUID().toString();
    try {
      model.createAIP(aipId, corporaService,
        DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_BUGGY_ID),
        aipCreator);
      final DescriptiveMetadata descMetadata = model.retrieveDescriptiveMetadata(aipId,
        CorporaConstants.DESCRIPTIVE_METADATA_ID);
      assertEquals(ValidationUtils.isDescriptiveMetadataValid(model, descMetadata, true), false);
    } catch (NotFoundException e) {
      // expected exception (for now)
    }
  }

  @Test
  public void testValidateAIP() throws ValidationException, RequestNotValidException, GenericException,
    AuthorizationDeniedException, AlreadyExistsException, NotFoundException {
    final String aipId = UUID.randomUUID().toString();
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);
    assertEquals(ValidationUtils.isAIPDescriptiveMetadataValid(model, aip.getId(), true).isValid(), true);
  }

  @Test(enabled = false)
  public void testValidateAIPBuggy() throws ValidationException, RequestNotValidException, GenericException,
    AuthorizationDeniedException, AlreadyExistsException, NotFoundException {
    // TODO AIP changed, so the corpora also needs to be changed
    // buggy aip have acqinfo2 instead of acqinfo in ead.xml
    final String aipId = UUID.randomUUID().toString();
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_BUGGY_ID),
      aipCreator);
    assertEquals(ValidationUtils.isAIPDescriptiveMetadataValid(model, aip.getId(), true), false);
  }

  @Test
  public void testValidationOfDescriptiveMetadata() throws ValidationException, RequestNotValidException,
    GenericException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException, IOException {
    // AIP 1 (wrong one)
    final AIP aip = model.createAIP(UUID.randomUUID().toString(), corporaService, DefaultStoragePath
      .parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_WITH_INVALID_METADATA), aipCreator);

    DefaultStoragePath path = DefaultStoragePath.parse(CorporaConstants.SOURCE_PRESERVATION_CONTAINER,
      CorporaConstants.SOURCE_INVALID_FOLDER, "ead.xml");
    model.createDescriptiveMetadata(aip.getId(), "ead", corporaService.getBinary(path).getContent(), "ead", "2002");

    Optional<Schema> xmlSchema = RodaCoreFactory.getRodaSchema("ead", "2002");
    assertEquals(xmlSchema.isPresent(), true);

    // AIP 2 (correct one)
    final AIP aip2 = model.createAIP(UUID.randomUUID().toString(), corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    Job job = TestsHelper.executeJob(DescriptiveMetadataValidationPlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, Arrays.asList(aip.getId())));

    List<Report> jobReports = TestsHelper.getJobReports(index, job, false);
    assertEquals(jobReports.get(0).getPluginState(), PluginState.FAILURE);

    Job job2 = TestsHelper.executeJob(DescriptiveMetadataValidationPlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, Arrays.asList(aip2.getId())));
    TestsHelper.getJobReports(index, job2, true);
  }
}
