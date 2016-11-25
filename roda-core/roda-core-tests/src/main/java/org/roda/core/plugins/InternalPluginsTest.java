/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
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
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.xmlbeans.XmlException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.IdUtils;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sublist.Sublist;
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
import org.roda.core.storage.Binary;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.util.InvalidDateException;

import com.google.common.collect.Iterables;

import gov.loc.premis.v3.EventComplexType;
import gov.loc.premis.v3.FormatComplexType;
import gov.loc.premis.v3.FormatRegistryComplexType;
import gov.loc.premis.v3.LinkingAgentIdentifierComplexType;
import gov.loc.premis.v3.ObjectCharacteristicsComplexType;
import gov.loc.premis.v3.Representation;
import jersey.repackaged.com.google.common.collect.Lists;

@Test(groups = {"all", "travis"})
public class InternalPluginsTest {
  private static final String FAKE_REPORTING_CLASS = "NONE";

  private static final Logger LOGGER = LoggerFactory.getLogger(InternalPluginsTest.class);

  private static final int CORPORA_FILES_COUNT = 13;
  private static final int CORPORA_FOLDERS_COUNT = 3;
  private static final String CORPORA_PDF = "test.docx";
  private static final String CORPORA_TEST1 = "test1";
  private static final String CORPORA_TEST1_TXT = "test1.txt";
  private static final int GENERATED_FILE_SIZE = 100;

  private static Path basePath;

  private static ModelService model;
  private static IndexService index;
  private static String aipCreator = "admin";

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(getClass(), true,
      PosixFilePermissions
        .asFileAttribute(new HashSet<>(Arrays.asList(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
          PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE))));

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources);
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    LOGGER.info("Running internal plugins tests under storage {}", basePath);
  }

  @AfterClass
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @AfterMethod
  public void cleanUp() throws RODAException {

    // delete all AIPs
    index.execute(IndexedAIP.class, Filter.ALL, new IndexRunnable<IndexedAIP>() {
      @Override
      public void run(IndexedAIP item) throws GenericException, RequestNotValidException, AuthorizationDeniedException {
        try {
          model.deleteAIP(item.getId());
        } catch (NotFoundException e) {
          // do nothing
        }
      }
    });

    // delete all Transferred Resources
    index.execute(TransferredResource.class, Filter.ALL, new IndexRunnable<TransferredResource>() {

      @Override
      public void run(TransferredResource item)
        throws GenericException, RequestNotValidException, AuthorizationDeniedException {
        model.deleteTransferredResource(item);
      }
    });
  }

  private ByteArrayInputStream generateContentData() {
    return new ByteArrayInputStream(RandomStringUtils.randomAscii(GENERATED_FILE_SIZE).getBytes());
  }

  private TransferredResource createCorpora() throws InterruptedException, IOException, FileAlreadyExistsException,
    NotFoundException, GenericException, RequestNotValidException, AlreadyExistsException {
    TransferredResourcesScanner f = RodaCoreFactory.getTransferredResourcesScanner();

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
    AIP root = model.createAIP(parentId, aipType, new Permissions(), aipCreator);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    TransferredResource transferredResource = createCorpora();
    AssertJUnit.assertNotNull(transferredResource);

    Job job = TestsHelper.executeJob(TransferredResourceToAIPPlugin.class, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));

    TestsHelper.getJobReports(index, job, true);

    index.commitAIPs();

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, root.getId())), null, new Sublist(0, 10));

    AssertJUnit.assertEquals(1L, find.getTotalCount());
    IndexedAIP indexedAIP = find.getResults().get(0);

    AIP aip = model.retrieveAIP(indexedAIP.getId());
    return aip;
  }

  @Test
  public void testIngestTransferredResource()
    throws IOException, InterruptedException, RODAException, SolrServerException {
    AIP aip = ingestCorpora();
    AssertJUnit.assertEquals(1, aip.getRepresentations().size());

    CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
      aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles,
      Lists.newArrayList(allFiles).stream().filter(f -> f.isPresent()).map(f -> f.get()).collect(Collectors.toList()));

    // All folders and files
    AssertJUnit.assertEquals(CORPORA_FOLDERS_COUNT + CORPORA_FILES_COUNT, reusableAllFiles.size());
  }

  @Test
  public void testVirusCheck() throws RODAException, FileAlreadyExistsException, InterruptedException, IOException,
    SolrServerException, XmlException {
    AIP aip = ingestCorpora();

    Job job = TestsHelper.executeJob(AntivirusPlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, aip.getId()));

    TestsHelper.getJobReports(index, job, true);

    aip = model.retrieveAIP(aip.getId());

    Plugin<? extends IsRODAObject> plugin = RodaCoreFactory.getPluginManager()
      .getPlugin(AntivirusPlugin.class.getName());
    String agentID = IdUtils.getPluginAgentId(plugin);
    boolean found = false;
    CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadataList = model
      .listPreservationMetadata(aip.getId(), true);
    for (OptionalWithCause<PreservationMetadata> opm : preservationMetadataList) {
      if (opm.isPresent()) {
        PreservationMetadata pm = opm.get();
        if (pm.getType().equals(PreservationMetadataType.EVENT)) {
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
        }
      }
    }
    IOUtils.closeQuietly(preservationMetadataList);
    AssertJUnit.assertTrue(found);

    index.commitAIPs();

    Filter filter = new Filter();
    filter.add(
      new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_TYPE, PreservationEventType.VIRUS_CHECK.toString()));
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()));
    IndexResult<IndexedPreservationEvent> events = index.find(IndexedPreservationEvent.class, filter, null,
      new Sublist(0, 10));
    AssertJUnit.assertEquals(1, events.getTotalCount());
  }

  @Test
  public void testPremisSkeleton()
    throws RODAException, FileAlreadyExistsException, InterruptedException, IOException, SolrServerException {
    AIP aip = ingestCorpora();

    TestsHelper.executeJob(PremisSkeletonPlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, aip.getId()));

    aip = model.retrieveAIP(aip.getId());

    CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadata = model
      .listPreservationMetadata(aip.getId(), true);

    // Files plus one representation + 2 SIP To AIP Event + 1 Premis Skeleton
    // Event
    AssertJUnit.assertEquals(CORPORA_FILES_COUNT + 4, Iterables.size(preservationMetadata));
    preservationMetadata.close();

    Binary rpo_bin = model.retrievePreservationRepresentation(aip.getId(), aip.getRepresentations().get(0).getId());
    Representation rpo = PremisV3Utils.binaryToRepresentation(rpo_bin.getContent(), true);

    // Relates to files
    AssertJUnit.assertEquals(CORPORA_FILES_COUNT, rpo.getRelationshipArray().length);

    Binary fpo_bin = model.retrievePreservationFile(aip.getId(), aip.getRepresentations().get(0).getId(),
      Arrays.asList(CORPORA_TEST1), CORPORA_TEST1_TXT);

    gov.loc.premis.v3.File fpo = PremisV3Utils.binaryToFile(fpo_bin.getContent(), true);

    ObjectCharacteristicsComplexType fileCharacteristics = fpo.getObjectCharacteristicsArray(0);

    // check a fixity was generated
    AssertJUnit.assertTrue("No fixity checks", fileCharacteristics.getFixityArray().length > 0);

    // check file size
    long size = fileCharacteristics.getSize();
    AssertJUnit.assertTrue("File size is zero", size > 0);

    // check file original name
    String originalName = fpo.getOriginalName().getStringValue();
    AssertJUnit.assertEquals(CORPORA_TEST1_TXT, originalName);

  }

  @Test
  public void testSiegfried() throws RODAException, FileAlreadyExistsException, InterruptedException, IOException,
    SolrServerException, XmlException {
    AIP aip = ingestCorpora();

    // ensure PREMIS objects are created
    TestsHelper.executeJob(PremisSkeletonPlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, aip.getId()));

    // run siegfried
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_REPORTING_CLASS, FAKE_REPORTING_CLASS);
    Job job = TestsHelper.executeJob(SiegfriedPlugin.class, parameters, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, aip.getId()));
    TestsHelper.getJobReports(index, job, true);

    aip = model.retrieveAIP(aip.getId());

    // Files with Siegfried output
    AssertJUnit.assertEquals(CORPORA_FILES_COUNT,
      Iterables.size(model.listOtherMetadata(aip.getId(), RodaConstants.OTHER_METADATA_TYPE_SIEGFRIED, true)));

    Binary om = model.retrieveOtherMetadataBinary(aip.getId(), aip.getRepresentations().get(0).getId(),
      Arrays.asList(CORPORA_TEST1), CORPORA_TEST1_TXT, SiegfriedPlugin.FILE_SUFFIX,
      RodaConstants.OTHER_METADATA_TYPE_SIEGFRIED);

    AssertJUnit.assertNotNull(om);

    Binary fpo_bin = model.retrievePreservationFile(aip.getId(), aip.getRepresentations().get(0).getId(),
      Arrays.asList(CORPORA_TEST1), CORPORA_TEST1_TXT);

    gov.loc.premis.v3.File fpo = PremisV3Utils.binaryToFile(fpo_bin.getContent(), true);

    FormatComplexType format = fpo.getObjectCharacteristicsArray(0).getFormatArray(0);
    AssertJUnit.assertEquals("Plain Text File", format.getFormatDesignation().getFormatName().getStringValue());
    FormatRegistryComplexType pronomRegistry = PremisV3Utils.getFormatRegistry(fpo,
      RodaConstants.PRESERVATION_REGISTRY_PRONOM);
    AssertJUnit.assertEquals(RodaConstants.PRESERVATION_REGISTRY_PRONOM,
      pronomRegistry.getFormatRegistryName().getStringValue());
    AssertJUnit.assertEquals("x-fmt/111", pronomRegistry.getFormatRegistryKey().getStringValue());

    FormatRegistryComplexType mimeRegistry = PremisV3Utils.getFormatRegistry(fpo,
      RodaConstants.PRESERVATION_REGISTRY_MIME);
    String mimetype = "text/plain";
    AssertJUnit.assertEquals(mimetype, mimeRegistry.getFormatRegistryKey().getStringValue());

    index.commitAIPs();

    IndexedFile indFile = index.retrieve(IndexedFile.class, IdUtils.getFileId(aip.getId(),
      aip.getRepresentations().get(0).getId(), Arrays.asList(CORPORA_TEST1), CORPORA_TEST1_TXT));

    AssertJUnit.assertEquals(mimetype, indFile.getFileFormat().getMimeType());
    AssertJUnit.assertEquals("x-fmt/111", indFile.getFileFormat().getPronom());
    AssertJUnit.assertEquals("Plain Text File", indFile.getFileFormat().getFormatDesignationName());

    List<String> suggest = index.suggest(IndexedFile.class, RodaConstants.FILE_FORMAT_MIMETYPE,
      mimetype.substring(0, 1), null, false, false);
    MatcherAssert.assertThat(suggest, Matchers.contains(mimetype));

    Plugin<? extends IsRODAObject> plugin = RodaCoreFactory.getPluginManager()
      .getPlugin(SiegfriedPlugin.class.getName());
    String agentID = IdUtils.getPluginAgentId(plugin);

    boolean found = false;
    CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadataList = model
      .listPreservationMetadata(aip.getId(), true);
    for (OptionalWithCause<PreservationMetadata> opm : preservationMetadataList) {
      if (opm.isPresent()) {
        PreservationMetadata pm = opm.get();
        if (pm.getType().equals(PreservationMetadataType.EVENT)) {
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
        }
      }
    }
    IOUtils.closeQuietly(preservationMetadataList);
    AssertJUnit.assertTrue(found);

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_TYPE,
      PreservationEventType.FORMAT_IDENTIFICATION.toString()));
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()));
    IndexResult<IndexedPreservationEvent> events = index.find(IndexedPreservationEvent.class, filter, null,
      new Sublist(0, 10));
    AssertJUnit.assertEquals(1, events.getTotalCount());

  }

  @Test
  public void testAutoAccept() throws RODAException, FileAlreadyExistsException, InterruptedException, IOException,
    InvalidDateException, SolrServerException {
    AIP aip = ingestCorpora();

    TestsHelper.executeJob(AutoAcceptSIPPlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, aip.getId()));

    aip = model.retrieveAIP(aip.getId());

    MatcherAssert.assertThat(aip.getState(), Is.is(AIPState.ACTIVE));
  }

}
