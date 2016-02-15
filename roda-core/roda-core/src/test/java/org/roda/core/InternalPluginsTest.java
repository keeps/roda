/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.xmlbeans.XmlException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.roda.core.common.PremisUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.monitor.FolderMonitorNIO;
import org.roda.core.common.monitor.FolderObserver;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IdUtils;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Attribute;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceTest;
import org.roda.core.plugins.Plugin;
import org.roda.core.plugins.plugins.antivirus.AntivirusPlugin;
import org.roda.core.plugins.plugins.ingest.TransferredResourceToAIPPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.SiegfriedPlugin;
import org.roda.core.plugins.plugins.ingest.characterization.TikaFullTextPlugin;
import org.roda.core.storage.Binary;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import lc.xmlns.premisV2.CreatingApplicationComplexType;
import lc.xmlns.premisV2.EventComplexType;
import lc.xmlns.premisV2.FormatComplexType;
import lc.xmlns.premisV2.FormatRegistryComplexType;
import lc.xmlns.premisV2.LinkingAgentIdentifierComplexType;
import lc.xmlns.premisV2.ObjectCharacteristicsComplexType;
import lc.xmlns.premisV2.Representation;

public class InternalPluginsTest {

  private static final int CORPORA_FILES_COUNT = 13;
  private static final int CORPORA_FOLDERS_COUNT = 3;
  private static final String CORPORA_PDF = "test.docx";
  private static final String CORPORA_TEST1 = "test1";
  private static final String CORPORA_TEST1_TXT = "test1.txt";
  private static final int GENERATED_FILE_SIZE = 100;
  private static final int AUTO_COMMIT_TIMEOUT = 2000;
  private static Path basePath;
  private static Path logPath;

  private static ModelService model;
  private static IndexService index;

  private static Path corporaPath;
  private static StorageService corporaService;

  private static final Logger logger = LoggerFactory.getLogger(ModelServiceTest.class);

  @Before
  public void setUp() throws Exception {

    basePath = Files.createTempDirectory("indexTests");
    System.setProperty("roda.home", basePath.toString());

    boolean deploySolr = true;
    boolean deployLdap = false;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator);
    logPath = RodaCoreFactory.getLogPath();
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = InternalPluginsTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    logger.info("Running internal plugins tests under storage {}", basePath);
  }

  @After
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  private ByteArrayInputStream generateContentData() {
    return new ByteArrayInputStream(RandomStringUtils.randomAscii(GENERATED_FILE_SIZE).getBytes());
  }

  private TransferredResource createCorpora()
    throws InterruptedException, IOException, FileAlreadyExistsException, NotFoundException, GenericException {
    FolderMonitorNIO f = RodaCoreFactory.getFolderMonitor();

    FolderObserver observer = Mockito.mock(FolderObserver.class);
    f.addFolderObserver(observer);

    while (!f.isFullyInitialized()) {
      logger.info("Waiting for folder monitor to initialize...");
      Thread.sleep(1000);
    }

    Assert.assertTrue(f.isFullyInitialized());

    // Path corpora = corporaPath.resolve(RodaConstants.STORAGE_CONTAINER_AIP)
    // .resolve(CorporaConstants.SOURCE_AIP_REP_WITH_SUBFOLDERS).resolve(RodaConstants.STORAGE_DIRECTORY_DATA)
    // .resolve(CorporaConstants.REPRESENTATION_1_ID);
    //
    // FSUtils.copy(corpora, f.getBasePath().resolve("test"), true);

    f.createFolder(null, "test");
    f.createFolder("test", CORPORA_TEST1);
    f.createFolder("test", "test2");
    f.createFolder("test", "test3");

    f.createFile("test", CORPORA_TEST1_TXT, generateContentData());
    f.createFile("test", "test2.txt", generateContentData());
    f.createFile("test", "test3.txt", generateContentData());
    f.createFile("test/test1", CORPORA_TEST1_TXT, generateContentData());
    f.createFile("test/test1", "test2.txt", generateContentData());
    f.createFile("test/test1", "test3.txt", generateContentData());
    f.createFile("test/test2", CORPORA_TEST1_TXT, generateContentData());
    f.createFile("test/test2", "test2.txt", generateContentData());
    f.createFile("test/test2", "test3.txt", generateContentData());
    f.createFile("test/test3", CORPORA_TEST1_TXT, generateContentData());
    f.createFile("test/test3", "test2.txt", generateContentData());
    f.createFile("test/test3", "test3.txt", generateContentData());

    f.createFile("test", CORPORA_PDF, getClass().getResourceAsStream("/corpora/Media/" + CORPORA_PDF));

    // TODO check if 4 times is the expected
    // Mockito.verify(observer, Mockito.times(4));

    logger.info("Waiting for soft-commit");
    Thread.sleep(AUTO_COMMIT_TIMEOUT);

    TransferredResource transferredResource = index.retrieve(TransferredResource.class, "test");
    return transferredResource;
  }

  private void assertReports(List<Report> reports) {
    for (Report report : reports) {
      boolean outcome = getReportOutcome(report);
      String outcomeDetails = getReportOutcomeDetails(report);
      Assert.assertTrue(outcomeDetails, outcome);
    }
  }

  private String getReportOutcomeDetails(Report report) {
    for (Attribute attribute : report.getItems().get(0).getAttributes()) {
      if (attribute.getName().equals(RodaConstants.REPORT_ATTR_OUTCOME_DETAILS)) {
        return attribute.getValue();
      }
    }
    return "";
  }

  private boolean getReportOutcome(Report report) {
    for (Attribute attribute : report.getItems().get(0).getAttributes()) {
      if (attribute.getName().equals(RodaConstants.REPORT_ATTR_OUTCOME)) {
        return attribute.getValue().equalsIgnoreCase(RodaConstants.REPORT_ATTR_OUTCOME_SUCCESS);
      }
    }
    return false;
  }

  private AIP ingestCorpora() throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, InvalidParameterException, InterruptedException, IOException,
    FileAlreadyExistsException {
    AIP root = model.createAIP(null);

    Plugin<TransferredResource> plugin = new TransferredResourceToAIPPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    plugin.setParameterValues(parameters);

    TransferredResource transferredResource = createCorpora();
    Assert.assertNotNull(transferredResource);

    List<Report> reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnTransferredResources(plugin,
      Arrays.asList(transferredResource));
    assertReports(reports);

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, root.getId())), null, new Sublist(0, 10));

    Assert.assertEquals(1L, find.getTotalCount());
    IndexedAIP indexedAIP = find.getResults().get(0);

    AIP aip = model.retrieveAIP(indexedAIP.getId());
    return aip;
  }

  @Test
  public void testIngestTransferredResource() throws IOException, InterruptedException, RODAException {
    AIP aip = ingestCorpora();
    Assert.assertEquals(1, aip.getRepresentations().size());

    CloseableIterable<File> allFiles = model.listFilesUnder(aip.getId(), aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles, allFiles);

    // All folders and files
    Assert.assertEquals(CORPORA_FOLDERS_COUNT + CORPORA_FILES_COUNT, reusableAllFiles.size());
  }

  @Test
  public void testVirusCheck() throws RODAException, FileAlreadyExistsException, InterruptedException, IOException {
    AIP aip = ingestCorpora();

    Plugin<AIP> plugin = new AntivirusPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    plugin.setParameterValues(parameters);

    List<Report> reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs(plugin, Arrays.asList(aip.getId()));
    assertReports(reports);

    aip = model.retrieveAIP(aip.getId());

    String agentID = plugin.getClass().getName() + "@" + plugin.getVersion();
    boolean found = false;
    CloseableIterable<PreservationMetadata> preservationMetadataList = model.listPreservationMetadata(aip.getId(),
      true);
    for (PreservationMetadata pm : preservationMetadataList) {
      if (pm.getType().equals(PreservationMetadataType.EVENT)) {
        try {
          EventComplexType event = PremisUtils
            .binaryToEvent(model.retrievePreservationEvent(pm.getAipId(), pm.getRepresentationId(), pm.getId())
              .getContent().createInputStream());
          if (event.getLinkingAgentIdentifierList() != null && event.getLinkingAgentIdentifierList().size() > 0) {
            for (LinkingAgentIdentifierComplexType laict : event.getLinkingAgentIdentifierList()) {
              if (laict.getLinkingAgentIdentifierValue() != null
                && laict.getLinkingAgentIdentifierValue().equalsIgnoreCase(agentID)) {
                found = true;
                break;
              }
            }
            if (found) {
              break;
            }
          }
        } catch (XmlException | IOException e) {

        }
      }
    }
    IOUtils.closeQuietly(preservationMetadataList);
    Assert.assertTrue(found);

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_TYPE,
      RodaConstants.PRESERVATION_EVENT_TYPE_ANTIVIRUS_CHECK));
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()));
    IndexResult<IndexedPreservationEvent> events = index.find(IndexedPreservationEvent.class, filter, null,
      new Sublist(0, 10));
    Assert.assertEquals(1, events.getTotalCount());
  }

  @Test
  public void testPremisSkeleton() throws RODAException, FileAlreadyExistsException, InterruptedException, IOException {
    AIP aip = ingestCorpora();

    Plugin<AIP> plugin = new PremisSkeletonPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    plugin.setParameterValues(parameters);

    List<Report> reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs(plugin, Arrays.asList(aip.getId()));
    assertReports(reports);

    aip = model.retrieveAIP(aip.getId());

    CloseableIterable<PreservationMetadata> preservationMetadata = model.listPreservationMetadata(aip.getId(), true);

    // Files plus one representation
    Assert.assertEquals(CORPORA_FILES_COUNT + 1, Iterables.size(preservationMetadata));
    preservationMetadata.close();

    Binary rpo_bin = model.retrievePreservationRepresentation(aip.getId(), aip.getRepresentations().get(0).getId());
    Representation rpo = PremisUtils.binaryToRepresentation(rpo_bin.getContent(), true);

    // Relates to files
    Assert.assertEquals(CORPORA_FILES_COUNT, rpo.getRelationshipList().size());

    Binary fpo_bin = model.retrievePreservationFile(aip.getId(), aip.getRepresentations().get(0).getId(),
      Arrays.asList(CORPORA_TEST1), CORPORA_TEST1_TXT);

    lc.xmlns.premisV2.File fpo = PremisUtils.binaryToFile(fpo_bin.getContent(), true);

    ObjectCharacteristicsComplexType fileCharacteristics = fpo.getObjectCharacteristicsArray(0);

    // check a fixity was generated
    Assert.assertTrue("No fixity checks", fileCharacteristics.getFixityList().size() > 0);

    // check file size
    long size = fileCharacteristics.getSize();
    Assert.assertTrue("File size is zero", size > 0);

    // check file original name
    String originalName = fpo.getOriginalName().getStringValue();
    Assert.assertEquals(CORPORA_TEST1_TXT, originalName);

  }

  @Test
  public void testSiegfried() throws RODAException, FileAlreadyExistsException, InterruptedException, IOException {
    AIP aip = ingestCorpora();

    Plugin<AIP> premisSkeletonPlugin = new PremisSkeletonPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    premisSkeletonPlugin.setParameterValues(parameters);

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs(premisSkeletonPlugin, Arrays.asList(aip.getId()));

    Plugin<AIP> plugin = new SiegfriedPlugin();
    Map<String, String> parameters2 = new HashMap<>();
    parameters2.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    plugin.setParameterValues(parameters2);

    List<Report> reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs(plugin, Arrays.asList(aip.getId()));
    assertReports(reports);

    aip = model.retrieveAIP(aip.getId());

    // Files with Siegfried output
    Assert.assertEquals(CORPORA_FILES_COUNT,
      Iterables.size(model.listOtherMetadata(aip.getId(), SiegfriedPlugin.OTHER_METADATA_TYPE, true)));

    Binary om = model.retrieveOtherMetadataBinary(aip.getId(), aip.getRepresentations().get(0).getId(),
      Arrays.asList(CORPORA_TEST1), CORPORA_TEST1_TXT, SiegfriedPlugin.FILE_SUFFIX,
      SiegfriedPlugin.OTHER_METADATA_TYPE);

    Assert.assertNotNull(om);

    Binary fpo_bin = model.retrievePreservationFile(aip.getId(), aip.getRepresentations().get(0).getId(),
      Arrays.asList(CORPORA_TEST1), CORPORA_TEST1_TXT);

    lc.xmlns.premisV2.File fpo = PremisUtils.binaryToFile(fpo_bin.getContent(), true);

    FormatComplexType format = fpo.getObjectCharacteristicsArray(0).getFormatArray(0);
    Assert.assertEquals("Plain Text File", format.getFormatDesignation().getFormatName());
    FormatRegistryComplexType pronomRegistry = PremisUtils.getFormatRegistry(fpo,
      RodaConstants.PRESERVATION_REGISTRY_PRONOM);
    Assert.assertEquals(RodaConstants.PRESERVATION_REGISTRY_PRONOM, pronomRegistry.getFormatRegistryName());
    Assert.assertEquals("x-fmt/111", pronomRegistry.getFormatRegistryKey());

    FormatRegistryComplexType mimeRegistry = PremisUtils.getFormatRegistry(fpo,
      RodaConstants.PRESERVATION_REGISTRY_MIME);
    Assert.assertEquals("text/plain", mimeRegistry.getFormatRegistryKey());

    IndexedFile indFile = index.retrieve(IndexedFile.class, IdUtils.getFileId(aip.getId(),
      aip.getRepresentations().get(0).getId(), Arrays.asList(CORPORA_TEST1), CORPORA_TEST1_TXT));

    Assert.assertEquals("text/plain", indFile.getFileFormat().getMimeType());
    Assert.assertEquals("x-fmt/111", indFile.getFileFormat().getPronom());
    Assert.assertEquals("Plain Text File", indFile.getFileFormat().getFormatDesignationName());

    String agentID = plugin.getClass().getName() + "@" + plugin.getVersion();
    boolean found = false;
    CloseableIterable<PreservationMetadata> preservationMetadataList = model.listPreservationMetadata(aip.getId(),
      true);
    for (PreservationMetadata pm : preservationMetadataList) {
      if (pm.getType().equals(PreservationMetadataType.EVENT)) {
        try {
          EventComplexType event = PremisUtils
            .binaryToEvent(model.retrievePreservationEvent(pm.getAipId(), pm.getRepresentationId(), pm.getId())
              .getContent().createInputStream());
          if (event.getLinkingAgentIdentifierList() != null && event.getLinkingAgentIdentifierList().size() > 0) {
            for (LinkingAgentIdentifierComplexType laict : event.getLinkingAgentIdentifierList()) {
              if (laict.getLinkingAgentIdentifierValue() != null
                && laict.getLinkingAgentIdentifierValue().equalsIgnoreCase(agentID)) {
                found = true;
                break;
              }
            }
            if (found) {
              break;
            }
          }
        } catch (XmlException | IOException e) {

        }
      }
    }
    IOUtils.closeQuietly(preservationMetadataList);
    Assert.assertTrue(found);

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_TYPE,
      RodaConstants.PRESERVATION_EVENT_TYPE_FORMAT_IDENTIFICATION));
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()));
    IndexResult<IndexedPreservationEvent> events = index.find(IndexedPreservationEvent.class, filter, null,
      new Sublist(0, 10));
    Assert.assertEquals(1, events.getTotalCount());

  }

  @Test
  public void testApacheTika()
    throws RODAException, FileAlreadyExistsException, InterruptedException, IOException, InvalidDateException {
    AIP aip = ingestCorpora();

    Plugin<AIP> premisSkeletonPlugin = new PremisSkeletonPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    premisSkeletonPlugin.setParameterValues(parameters);

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs(premisSkeletonPlugin, Arrays.asList(aip.getId()));

    Plugin<AIP> plugin = new TikaFullTextPlugin();
    Map<String, String> parameters2 = new HashMap<>();
    parameters2.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, "NONE");
    plugin.setParameterValues(parameters2);

    List<Report> reports = RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs(plugin, Arrays.asList(aip.getId()));
    assertReports(reports);

    aip = model.retrieveAIP(aip.getId());

    // Files with Apache Tika output
    Assert.assertEquals(CORPORA_FILES_COUNT,
      Iterables.size(model.listOtherMetadata(aip.getId(), TikaFullTextPlugin.OTHER_METADATA_TYPE, true)));

    Binary om = model.retrieveOtherMetadataBinary(aip.getId(), aip.getRepresentations().get(0).getId(),
      Arrays.asList(CORPORA_TEST1), CORPORA_TEST1_TXT, TikaFullTextPlugin.FILE_SUFFIX,
      TikaFullTextPlugin.OTHER_METADATA_TYPE);

    Assert.assertNotNull(om);

    Binary fpo_bin = model.retrievePreservationFile(aip.getId(), aip.getRepresentations().get(0).getId(),
      new ArrayList<>(), CORPORA_PDF);

    lc.xmlns.premisV2.File fpo = PremisUtils.binaryToFile(fpo_bin.getContent(), true);

    ObjectCharacteristicsComplexType characteristics = fpo.getObjectCharacteristicsArray(0);

    Assert.assertEquals(1, characteristics.getCreatingApplicationList().size());

    CreatingApplicationComplexType creatingApplication = characteristics.getCreatingApplicationArray(0);
    Assert.assertEquals("Microsoft Office Word", creatingApplication.getCreatingApplicationName());
    Assert.assertEquals("15.0000", creatingApplication.getCreatingApplicationVersion());
    Assert.assertEquals(DateParser.parse("2016-02-10T15:52:00Z"),
      DateParser.parse(creatingApplication.getDateCreatedByApplication().toString()));

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.FILE_FULLTEXT, "Test"));
    filter.add(new SimpleFilterParameter(RodaConstants.FILE_UUID,
      IdUtils.getFileId(aip.getId(), aip.getRepresentations().get(0).getId(), new ArrayList<>(), CORPORA_PDF)));

    IndexResult<IndexedFile> files = index.find(IndexedFile.class, filter, null, new Sublist(0, 10));
    Assert.assertEquals(1, files.getTotalCount());

  }

}
