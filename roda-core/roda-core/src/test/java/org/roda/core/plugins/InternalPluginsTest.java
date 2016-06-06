/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.xmlbeans.XmlException;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SelectedItemsNone;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata.PreservationMetadataType;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.plugins.antivirus.AntivirusPlugin;
import org.roda.core.plugins.plugins.ingest.AutoAcceptSIPPlugin;
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

import gov.loc.premis.v3.CreatingApplicationComplexType;
import gov.loc.premis.v3.EventComplexType;
import gov.loc.premis.v3.FormatComplexType;
import gov.loc.premis.v3.FormatRegistryComplexType;
import gov.loc.premis.v3.LinkingAgentIdentifierComplexType;
import gov.loc.premis.v3.ObjectCharacteristicsComplexType;
import gov.loc.premis.v3.Representation;
import jersey.repackaged.com.google.common.collect.Lists;

public class InternalPluginsTest {
  private static final String FAKE_JOB_ID = "NONE";
  private static final String FAKE_REPORTING_CLASS = "NONE";

  private static final Logger LOGGER = LoggerFactory.getLogger(InternalPluginsTest.class);

  private static final int CORPORA_FILES_COUNT = 13;
  private static final int CORPORA_FOLDERS_COUNT = 3;
  private static final String CORPORA_PDF = "test.docx";
  private static final String CORPORA_TEST1 = "test1";
  private static final String CORPORA_TEST1_TXT = "test1.txt";
  private static final int GENERATED_FILE_SIZE = 100;

  private static Path basePath;
  private static Path logPath;

  private static ModelService model;
  private static IndexService index;

  private static Path corporaPath;
  private static StorageService corporaService;

  @Before
  public void setUp() throws Exception {

    basePath = Files.createTempDirectory("indexTests",
      PosixFilePermissions
        .asFileAttribute(new HashSet<>(Arrays.asList(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
          PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE))));
    System.setProperty("roda.home", basePath.toString());

    boolean deploySolr = true;
    boolean deployLdap = false;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager);
    logPath = RodaCoreFactory.getLogPath();
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = InternalPluginsTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    LOGGER.info("Running internal plugins tests under storage {}", basePath);

    Job fakeJob = new Job();
    fakeJob.setId(FAKE_JOB_ID);
    fakeJob.setPluginType(PluginType.MISC);
    fakeJob.setSourceObjects(SelectedItemsNone.create());
    model.createJob(fakeJob);
    index.commit(Job.class);
  }

  @After
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  private ByteArrayInputStream generateContentData() {
    return new ByteArrayInputStream(RandomStringUtils.randomAscii(GENERATED_FILE_SIZE).getBytes());
  }

  private TransferredResource createCorpora() throws InterruptedException, IOException, FileAlreadyExistsException,
    NotFoundException, GenericException, RequestNotValidException {
    TransferredResourcesScanner f = RodaCoreFactory.getTransferredResourcesScanner();

    // Path corpora = corporaPath.resolve(RodaConstants.STORAGE_CONTAINER_AIP)
    // .resolve(CorporaConstants.SOURCE_AIP_REP_WITH_SUBFOLDERS).resolve(RodaConstants.STORAGE_DIRECTORY_DATA)
    // .resolve(CorporaConstants.REPRESENTATION_1_ID);
    //
    // FSUtils.copy(corpora, f.getBasePath().resolve("test"), true);

    String parentUUID = f.createFolder(null, "test").getUUID();
    String test1UUID = f.createFolder(parentUUID, CORPORA_TEST1).getUUID();
    String test2UUID = f.createFolder(parentUUID, "test2").getUUID();
    String test3UUID = f.createFolder(parentUUID, "test3").getUUID();

    f.createFile(parentUUID, CORPORA_TEST1_TXT, generateContentData());
    f.createFile(parentUUID, "test2.txt", generateContentData());
    f.createFile(parentUUID, "test3.txt", generateContentData());
    f.createFile(test1UUID, CORPORA_TEST1_TXT, generateContentData());
    f.createFile(test1UUID, "test2.txt", generateContentData());
    f.createFile(test1UUID, "test3.txt", generateContentData());
    f.createFile(test2UUID, CORPORA_TEST1_TXT, generateContentData());
    f.createFile(test2UUID, "test2.txt", generateContentData());
    f.createFile(test2UUID, "test3.txt", generateContentData());
    f.createFile(test3UUID, CORPORA_TEST1_TXT, generateContentData());
    f.createFile(test3UUID, "test2.txt", generateContentData());
    f.createFile(test3UUID, "test3.txt", generateContentData());

    f.createFile(parentUUID, CORPORA_PDF, getClass().getResourceAsStream("/corpora/Media/" + CORPORA_PDF));

    // TODO check if 4 times is the expected
    // Mockito.verify(observer, Mockito.times(4));

    index.commit(TransferredResource.class);

    TransferredResource transferredResource = index.retrieve(TransferredResource.class,
      UUID.nameUUIDFromBytes("test".getBytes()).toString());
    return transferredResource;
  }

  private AIP ingestCorpora() throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, InvalidParameterException, InterruptedException, IOException,
    FileAlreadyExistsException, SolrServerException {
    String parentId = null;
    String aipType = RodaConstants.AIP_TYPE_MIXED;
    AIP root = model.createAIP(parentId, aipType, new Permissions());

    Plugin<TransferredResource> plugin = new TransferredResourceToAIPPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, FAKE_JOB_ID);
    plugin.setParameterValues(parameters);

    TransferredResource transferredResource = createCorpora();
    Assert.assertNotNull(transferredResource);

    List<String> transferredResourcesUUIDs = Arrays.asList(transferredResource.getUUID());
    RodaCoreFactory.getPluginOrchestrator().runPluginOnTransferredResources(plugin, transferredResourcesUUIDs);
    // ReportAssertUtils.assertReports(reports, null,
    // transferredResourcesUUIDs);

    // TODO wait for job to finish
    Thread.sleep(1000);

    index.commitAIPs();

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, root.getId())), null, new Sublist(0, 10));

    Assert.assertEquals(1L, find.getTotalCount());
    IndexedAIP indexedAIP = find.getResults().get(0);

    AIP aip = model.retrieveAIP(indexedAIP.getId());
    return aip;
  }

  @Test
  public void testIngestTransferredResource()
    throws IOException, InterruptedException, RODAException, SolrServerException {
    AIP aip = ingestCorpora();
    Assert.assertEquals(1, aip.getRepresentations().size());

    CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
      aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles,
      Lists.newArrayList(allFiles).stream().filter(f -> f.isPresent()).map(f -> f.get()).collect(Collectors.toList()));

    // All folders and files
    Assert.assertEquals(CORPORA_FOLDERS_COUNT + CORPORA_FILES_COUNT, reusableAllFiles.size());
  }

  @Test
  public void testVirusCheck()
    throws RODAException, FileAlreadyExistsException, InterruptedException, IOException, SolrServerException {
    AIP aip = ingestCorpora();

    // 20160426 hsilva: we have to get the plugin from PluginManager to obtain
    // the correct version of the concrete antivirus being used
    Plugin<AIP> plugin = (Plugin<AIP>) RodaCoreFactory.getPluginManager()
      .getPlugin(AntivirusPlugin.class.getCanonicalName());
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, FAKE_JOB_ID);
    plugin.setParameterValues(parameters);

    List<String> aipIdList = Arrays.asList(aip.getId());
    RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs(plugin, aipIdList);
    // ReportAssertUtils.assertReports(reports, aipIdList);

    // TODO wait for job to finish
    Thread.sleep(1000);

    aip = model.retrieveAIP(aip.getId());

    String agentID = plugin.getClass().getName() + "@" + plugin.getVersion();
    boolean found = false;
    CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadataList = model
      .listPreservationMetadata(aip.getId(), true);
    for (OptionalWithCause<PreservationMetadata> opm : preservationMetadataList) {
      if (opm.isPresent()) {
        PreservationMetadata pm = opm.get();
        if (pm.getType().equals(PreservationMetadataType.EVENT)) {
          try {
            EventComplexType event = PremisV3Utils
              .binaryToEvent(model.retrievePreservationEvent(pm.getAipId(), pm.getRepresentationId(), pm.getId())
                .getContent().createInputStream());
            if (event.getLinkingAgentIdentifierArray() != null && event.getLinkingAgentIdentifierArray().length > 0) {
              for (LinkingAgentIdentifierComplexType laict : event.getLinkingAgentIdentifierArray()) {
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
    }
    IOUtils.closeQuietly(preservationMetadataList);
    Assert.assertTrue(found);

    index.commitAIPs();

    Filter filter = new Filter();
    filter.add(
      new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_TYPE, PreservationEventType.VIRUS_CHECK.toString()));
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()));
    IndexResult<IndexedPreservationEvent> events = index.find(IndexedPreservationEvent.class, filter, null,
      new Sublist(0, 10));
    Assert.assertEquals(1, events.getTotalCount());
  }

  @Test
  public void testPremisSkeleton()
    throws RODAException, FileAlreadyExistsException, InterruptedException, IOException, SolrServerException {
    AIP aip = ingestCorpora();

    Plugin<AIP> plugin = new PremisSkeletonPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, FAKE_JOB_ID);
    plugin.setParameterValues(parameters);

    List<String> aipIdList = Arrays.asList(aip.getId());
    RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs(plugin, aipIdList);
    // ReportAssertUtils.assertReports(reports, aipIdList);

    // TODO wait for job to finish
    Thread.sleep(1000);

    aip = model.retrieveAIP(aip.getId());

    CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadata = model
      .listPreservationMetadata(aip.getId(), true);

    // Files plus one representation + 2 SIP To AIP Event + 1 Premis Skeleton
    // Event
    Assert.assertEquals(CORPORA_FILES_COUNT + 4, Iterables.size(preservationMetadata));
    preservationMetadata.close();

    Binary rpo_bin = model.retrievePreservationRepresentation(aip.getId(), aip.getRepresentations().get(0).getId());
    Representation rpo = PremisV3Utils.binaryToRepresentation(rpo_bin.getContent(), true);

    // Relates to files
    Assert.assertEquals(CORPORA_FILES_COUNT, rpo.getRelationshipArray().length);

    Binary fpo_bin = model.retrievePreservationFile(aip.getId(), aip.getRepresentations().get(0).getId(),
      Arrays.asList(CORPORA_TEST1), CORPORA_TEST1_TXT);

    gov.loc.premis.v3.File fpo = PremisV3Utils.binaryToFile(fpo_bin.getContent(), true);

    ObjectCharacteristicsComplexType fileCharacteristics = fpo.getObjectCharacteristicsArray(0);

    // check a fixity was generated
    Assert.assertTrue("No fixity checks", fileCharacteristics.getFixityArray().length > 0);

    // check file size
    long size = fileCharacteristics.getSize();
    Assert.assertTrue("File size is zero", size > 0);

    // check file original name
    String originalName = fpo.getOriginalName().getStringValue();
    Assert.assertEquals(CORPORA_TEST1_TXT, originalName);

  }

  @Test
  public void testSiegfried()
    throws RODAException, FileAlreadyExistsException, InterruptedException, IOException, SolrServerException {
    AIP aip = ingestCorpora();

    Plugin<AIP> premisSkeletonPlugin = new PremisSkeletonPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, FAKE_JOB_ID);
    premisSkeletonPlugin.setParameterValues(parameters);

    List<String> aipIdList = Arrays.asList(aip.getId());
    RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs(premisSkeletonPlugin, aipIdList);

    Plugin<AIP> plugin = new SiegfriedPlugin();
    Map<String, String> parameters2 = new HashMap<>();
    parameters2.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, FAKE_JOB_ID);
    parameters2.put(RodaConstants.PLUGIN_PARAMS_REPORTING_CLASS, FAKE_REPORTING_CLASS);
    plugin.setParameterValues(parameters2);

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs(plugin, aipIdList);
    // ReportAssertUtils.assertReports(reports, aipIdList);
    // TODO wait for job to finish
    Thread.sleep(1000);

    aip = model.retrieveAIP(aip.getId());

    // Files with Siegfried output
    Assert.assertEquals(CORPORA_FILES_COUNT,
      Iterables.size(model.listOtherMetadata(aip.getId(), RodaConstants.OTHER_METADATA_TYPE_SIEGFRIED, true)));

    Binary om = model.retrieveOtherMetadataBinary(aip.getId(), aip.getRepresentations().get(0).getId(),
      Arrays.asList(CORPORA_TEST1), CORPORA_TEST1_TXT, SiegfriedPlugin.FILE_SUFFIX,
      RodaConstants.OTHER_METADATA_TYPE_SIEGFRIED);

    Assert.assertNotNull(om);

    Binary fpo_bin = model.retrievePreservationFile(aip.getId(), aip.getRepresentations().get(0).getId(),
      Arrays.asList(CORPORA_TEST1), CORPORA_TEST1_TXT);

    gov.loc.premis.v3.File fpo = PremisV3Utils.binaryToFile(fpo_bin.getContent(), true);

    FormatComplexType format = fpo.getObjectCharacteristicsArray(0).getFormatArray(0);
    Assert.assertEquals("Plain Text File", format.getFormatDesignation().getFormatName().getStringValue());
    FormatRegistryComplexType pronomRegistry = PremisV3Utils.getFormatRegistry(fpo,
      RodaConstants.PRESERVATION_REGISTRY_PRONOM);
    Assert.assertEquals(RodaConstants.PRESERVATION_REGISTRY_PRONOM,
      pronomRegistry.getFormatRegistryName().getStringValue());
    Assert.assertEquals("x-fmt/111", pronomRegistry.getFormatRegistryKey().getStringValue());

    FormatRegistryComplexType mimeRegistry = PremisV3Utils.getFormatRegistry(fpo,
      RodaConstants.PRESERVATION_REGISTRY_MIME);
    String mimetype = "text/plain";
    Assert.assertEquals(mimetype, mimeRegistry.getFormatRegistryKey().getStringValue());

    index.commitAIPs();

    IndexedFile indFile = index.retrieve(IndexedFile.class, IdUtils.getFileId(aip.getId(),
      aip.getRepresentations().get(0).getId(), Arrays.asList(CORPORA_TEST1), CORPORA_TEST1_TXT));

    Assert.assertEquals(mimetype, indFile.getFileFormat().getMimeType());
    Assert.assertEquals("x-fmt/111", indFile.getFileFormat().getPronom());
    Assert.assertEquals("Plain Text File", indFile.getFileFormat().getFormatDesignationName());

    List<String> suggest = index.suggest(IndexedFile.class, RodaConstants.FILE_FORMAT_MIMETYPE,
      mimetype.substring(0, 1));
    Assert.assertThat(suggest, Matchers.contains(mimetype));

    String agentID = plugin.getClass().getName() + "@" + plugin.getVersion();
    boolean found = false;
    CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadataList = model
      .listPreservationMetadata(aip.getId(), true);
    for (OptionalWithCause<PreservationMetadata> opm : preservationMetadataList) {
      if (opm.isPresent()) {
        PreservationMetadata pm = opm.get();
        if (pm.getType().equals(PreservationMetadataType.EVENT)) {
          try {
            EventComplexType event = PremisV3Utils
              .binaryToEvent(model.retrievePreservationEvent(pm.getAipId(), pm.getRepresentationId(), pm.getId())
                .getContent().createInputStream());
            if (event.getLinkingAgentIdentifierArray() != null && event.getLinkingAgentIdentifierArray().length > 0) {
              for (LinkingAgentIdentifierComplexType laict : event.getLinkingAgentIdentifierArray()) {
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
    }
    IOUtils.closeQuietly(preservationMetadataList);
    Assert.assertTrue(found);

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_TYPE,
      PreservationEventType.FORMAT_IDENTIFICATION.toString()));
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()));
    IndexResult<IndexedPreservationEvent> events = index.find(IndexedPreservationEvent.class, filter, null,
      new Sublist(0, 10));
    Assert.assertEquals(1, events.getTotalCount());

  }

  @Test
  public void testApacheTika() throws RODAException, FileAlreadyExistsException, InterruptedException, IOException,
    InvalidDateException, SolrServerException {
    AIP aip = ingestCorpora();

    Plugin<AIP> premisSkeletonPlugin = new PremisSkeletonPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, FAKE_JOB_ID);
    premisSkeletonPlugin.setParameterValues(parameters);

    List<String> aipIdList = Arrays.asList(aip.getId());
    RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs(premisSkeletonPlugin, aipIdList);

    Plugin<AIP> plugin = new TikaFullTextPlugin();
    Map<String, String> parameters2 = new HashMap<>();
    parameters2.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, FAKE_JOB_ID);
    parameters2.put(RodaConstants.PLUGIN_PARAMS_REPORTING_CLASS, FAKE_REPORTING_CLASS);
    parameters2.put(RodaConstants.PLUGIN_PARAMS_DO_FULLTEXT_EXTRACTION, Boolean.TRUE.toString());
    parameters2.put(RodaConstants.PLUGIN_PARAMS_DO_FEATURE_EXTRACTION, Boolean.TRUE.toString());
    plugin.setParameterValues(parameters2);

    
    RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs(plugin, aipIdList);
    // ReportAssertUtils.assertReports(reports, aipIdList);
    
    Job job = new Job();
    RodaCoreFactory.getPluginOrchestrator().executeJob(job);

    aip = model.retrieveAIP(aip.getId());

    // Files with Apache Tika output each tika run creates 2 files
    Assert.assertEquals(2 * CORPORA_FILES_COUNT,
      Iterables.size(model.listOtherMetadata(aip.getId(), RodaConstants.OTHER_METADATA_TYPE_APACHE_TIKA, true)));

    Binary om = model.retrieveOtherMetadataBinary(aip.getId(), aip.getRepresentations().get(0).getId(),
      Arrays.asList(CORPORA_TEST1), CORPORA_TEST1_TXT, TikaFullTextPlugin.FILE_SUFFIX_FULLTEXT,
      RodaConstants.OTHER_METADATA_TYPE_APACHE_TIKA);

    Assert.assertNotNull(om);

    Binary fpo_bin = model.retrievePreservationFile(aip.getId(), aip.getRepresentations().get(0).getId(),
      new ArrayList<>(), CORPORA_PDF);

    gov.loc.premis.v3.File fpo = PremisV3Utils.binaryToFile(fpo_bin.getContent(), true);

    ObjectCharacteristicsComplexType characteristics = fpo.getObjectCharacteristicsArray(0);

    Assert.assertEquals(1, characteristics.getCreatingApplicationArray().length);

    CreatingApplicationComplexType creatingApplication = characteristics.getCreatingApplicationArray(0);
    Assert.assertEquals("Microsoft Office Word", creatingApplication.getCreatingApplicationName().getStringValue());
    Assert.assertEquals("15.0000", creatingApplication.getCreatingApplicationVersion());
    Assert.assertEquals(DateParser.parse("2016-02-10T15:52:00Z"),
      DateParser.parse(creatingApplication.getDateCreatedByApplication().toString()));

    index.commit(IndexedFile.class);

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.FILE_FULLTEXT, "Test"));
    filter.add(new SimpleFilterParameter(RodaConstants.FILE_UUID,
      IdUtils.getFileId(aip.getId(), aip.getRepresentations().get(0).getId(), new ArrayList<>(), CORPORA_PDF)));

    IndexResult<IndexedFile> files = index.find(IndexedFile.class, filter, null, new Sublist(0, 10));
    Assert.assertEquals(1, files.getTotalCount());
  }

  @Test
  public void testAutoAccept() throws RODAException, FileAlreadyExistsException, InterruptedException, IOException,
    InvalidDateException, SolrServerException {
    AIP aip = ingestCorpora();

    Plugin<AIP> autoAcceptPlugin = new AutoAcceptSIPPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_JOB_ID, FAKE_JOB_ID);
    autoAcceptPlugin.setParameterValues(parameters);

    RodaCoreFactory.getPluginOrchestrator().runPluginOnAIPs(autoAcceptPlugin, Arrays.asList(aip.getId()));
    // TODO wait for job to finish
    Thread.sleep(1000);

    aip = model.retrieveAIP(aip.getId());

    assertThat(aip.getState(), Is.is(AIPState.ACTIVE));
  }

}
