/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
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
import org.roda.core.index.IndexTestUtils;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.antivirus.AntivirusPlugin;
import org.roda.core.plugins.base.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.base.characterization.SiegfriedPlugin;
import org.roda.core.plugins.base.ingest.AutoAcceptSIPPlugin;
import org.roda.core.plugins.base.ingest.EARKSIP2ToAIPPlugin;
import org.roda.core.plugins.base.ingest.TransferredResourceToAIPPlugin;
import org.roda.core.plugins.base.ingest.v2.MinimalIngestPlugin;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

import gov.loc.premis.v3.EventComplexType;
import gov.loc.premis.v3.FormatComplexType;
import gov.loc.premis.v3.FormatRegistryComplexType;
import gov.loc.premis.v3.LinkingAgentIdentifierComplexType;
import gov.loc.premis.v3.ObjectCharacteristicsComplexType;
import gov.loc.premis.v3.Representation;
import jersey.repackaged.com.google.common.collect.Lists;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
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
  private static LdapUtilityTestHelper ldapUtilityTestHelper;

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(getClass(), true,
      PosixFilePermissions
        .asFileAttribute(new HashSet<>(Arrays.asList(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
          PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE))));
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false, ldapUtilityTestHelper.getLdapUtility());
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    LOGGER.info("Running internal plugins tests under storage {}", basePath);
  }

  @AfterClass
  public void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @AfterMethod
  public void cleanUp() throws RODAException {

    // delete all AIPs
    index.execute(IndexedAIP.class, Filter.ALL, new ArrayList<>(), item -> {
      try {
        model.deleteAIP(item.getId());
      } catch (NotFoundException e) {
        // do nothing
      }
    }, e -> Assert.fail("Error cleaning up", e));

    // delete all Transferred Resources
    index.execute(TransferredResource.class, Filter.ALL, new ArrayList<>(),
      item -> model.deleteTransferredResource(item), e -> Assert.fail("Error cleaning up", e));
  }

  private ByteArrayInputStream generateContentData() {
    return new ByteArrayInputStream(RandomStringUtils.randomAscii(GENERATED_FILE_SIZE)
      .replaceAll("\\{", RandomStringUtils.randomAlphabetic(1)).getBytes());
  }

  private TransferredResource createCorpora()
    throws NotFoundException, GenericException, AlreadyExistsException, AuthorizationDeniedException {
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

    index.commit(TransferredResource.class);

    return index.retrieve(TransferredResource.class, IdUtils.createUUID("test"), new ArrayList<>());
  }

  private AIP ingestCorpora() throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException {
    String aipType = RodaConstants.AIP_TYPE_MIXED;
    AIP root = model.createAIP(null, aipType, new Permissions(), RodaConstants.ADMIN);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    TransferredResource transferredResource = createCorpora();
    AssertJUnit.assertNotNull(transferredResource);

    Job job = TestsHelper.executeJob(TransferredResourceToAIPPlugin.class, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));

    TestsHelper.getJobReports(index, job, true);

    index.commitAIPs();

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, root.getId())), null, new Sublist(0, 10),
      new ArrayList<>());

    AssertJUnit.assertEquals(1L, find.getTotalCount());
    IndexedAIP indexedAIP = find.getResults().get(0);

    return model.retrieveAIP(indexedAIP.getId());
  }

  @Test
  public void testIngestTransferredResource() throws RODAException {
    AIP aip = ingestCorpora();
    AssertJUnit.assertEquals(1, aip.getRepresentations().size());

    CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
      aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles, Lists.newArrayList(allFiles).stream().filter(OptionalWithCause::isPresent)
      .map(OptionalWithCause::get).collect(Collectors.toList()));

    // All folders and files
    AssertJUnit.assertEquals(CORPORA_FOLDERS_COUNT + CORPORA_FILES_COUNT, reusableAllFiles.size());
  }

  @Test
  public void testVirusCheck() throws RODAException, IOException {
    AIP aip = ingestCorpora();

    Job job = TestsHelper.executeJob(AntivirusPlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, aip.getId()));

    TestsHelper.getJobReports(index, job, true);

    aip = model.retrieveAIP(aip.getId());

    Plugin<? extends IsRODAObject> plugin = RodaCoreFactory.getPluginManager()
      .getPlugin(AntivirusPlugin.class.getName());
    String agentID = PluginHelper.getPluginAgentId(plugin);
    boolean found = false;

    try (CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadataList = model
      .listPreservationMetadata(aip.getId(), true)) {
      for (OptionalWithCause<PreservationMetadata> opm : preservationMetadataList) {
        if (opm.isPresent()) {
          PreservationMetadata pm = opm.get();
          if (pm.getType().equals(PreservationMetadataType.EVENT)) {
            EventComplexType event = PremisV3Utils
              .binaryToEvent(model.retrievePreservationEvent(pm.getAipId(), pm.getRepresentationId(),
                pm.getFileDirectoryPath(), pm.getFileId(), pm.getId()).getContent().createInputStream());
            if (event.getLinkingAgentIdentifier() != null && !event.getLinkingAgentIdentifier().isEmpty()) {
              for (LinkingAgentIdentifierComplexType laict : event.getLinkingAgentIdentifier()) {
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
    }

    AssertJUnit.assertTrue(found);
    index.commitAIPs();

    Filter filter = new Filter();
    filter.add(
      new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_TYPE, PreservationEventType.VIRUS_CHECK.toString()));
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()));
    IndexResult<IndexedPreservationEvent> events = index.find(IndexedPreservationEvent.class, filter, null,
      new Sublist(0, 10), new ArrayList<>());
    AssertJUnit.assertEquals(1, events.getTotalCount());
  }

  @Test
  public void testPremisSkeleton() throws RODAException, IOException {
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
    AssertJUnit.assertEquals(CORPORA_FILES_COUNT, rpo.getRelationship().size());

    Binary fpo_bin = model.retrievePreservationFile(aip.getId(), aip.getRepresentations().get(0).getId(),
      List.of(CORPORA_TEST1), CORPORA_TEST1_TXT);

    gov.loc.premis.v3.File fpo = PremisV3Utils.binaryToFile(fpo_bin.getContent(), true);

    ObjectCharacteristicsComplexType fileCharacteristics = fpo.getObjectCharacteristics().get(0);

    // check a fixity was generated
    AssertJUnit.assertFalse("No fixity checks", fileCharacteristics.getFixity().isEmpty());

    // check file size
    long size = fileCharacteristics.getSize();
    AssertJUnit.assertTrue("File size is zero", size > 0);

    // check file original name
    String originalName = fpo.getOriginalName().getValue();
    AssertJUnit.assertEquals(CORPORA_TEST1_TXT, originalName);
  }

  @Test
  public void testSiegfried() throws RODAException, IOException {
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
      List.of(CORPORA_TEST1), CORPORA_TEST1_TXT, SiegfriedPlugin.FILE_SUFFIX,
      RodaConstants.OTHER_METADATA_TYPE_SIEGFRIED);

    AssertJUnit.assertNotNull(om);

    Binary fpo_bin = model.retrievePreservationFile(aip.getId(), aip.getRepresentations().get(0).getId(),
      List.of(CORPORA_TEST1), CORPORA_TEST1_TXT);

    gov.loc.premis.v3.File fpo = PremisV3Utils.binaryToFile(fpo_bin.getContent(), true);

    FormatComplexType format = fpo.getObjectCharacteristics().get(0).getFormat().get(0);
    AssertJUnit.assertEquals("Plain Text File", format.getFormatDesignation().get(0).getFormatName().getValue());
    FormatRegistryComplexType pronomRegistry = PremisV3Utils.getFormatRegistry(fpo,
      RodaConstants.PRESERVATION_REGISTRY_PRONOM);
    AssertJUnit.assertEquals(RodaConstants.PRESERVATION_REGISTRY_PRONOM,
      pronomRegistry.getFormatRegistryName().getValue());
    AssertJUnit.assertEquals("x-fmt/111", pronomRegistry.getFormatRegistryKey().getValue());

    FormatRegistryComplexType mimeRegistry = PremisV3Utils.getFormatRegistry(fpo,
      RodaConstants.PRESERVATION_REGISTRY_MIME);
    String mimetype = "text/plain";
    AssertJUnit.assertEquals(mimetype, mimeRegistry.getFormatRegistryKey().getValue());

    index.commitAIPs();

    IndexedFile indFile = index.retrieve(IndexedFile.class, IdUtils.getFileId(aip.getId(),
      aip.getRepresentations().get(0).getId(), Collections.singletonList(CORPORA_TEST1), CORPORA_TEST1_TXT),
      new ArrayList<>());

    AssertJUnit.assertEquals(mimetype, indFile.getFileFormat().getMimeType());
    AssertJUnit.assertEquals("x-fmt/111", indFile.getFileFormat().getPronom());
    AssertJUnit.assertEquals("Plain Text File", indFile.getFileFormat().getFormatDesignationName());

    List<String> suggest = index.suggest(IndexedFile.class, RodaConstants.FILE_FORMAT_MIMETYPE,
      mimetype.substring(0, 1), null, false, false);
    MatcherAssert.assertThat(suggest, contains(mimetype));

    Plugin<? extends IsRODAObject> plugin = RodaCoreFactory.getPluginManager()
      .getPlugin(SiegfriedPlugin.class.getName());
    String agentID = PluginHelper.getPluginAgentId(plugin);

    boolean found = false;

    try (CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadataList = model
      .listPreservationMetadata(aip.getId(), true)) {
      for (OptionalWithCause<PreservationMetadata> opm : preservationMetadataList) {
        if (opm.isPresent()) {
          PreservationMetadata pm = opm.get();
          if (pm.getType().equals(PreservationMetadataType.EVENT)) {
            EventComplexType event = PremisV3Utils
              .binaryToEvent(model.retrievePreservationEvent(pm.getAipId(), pm.getRepresentationId(),
                pm.getFileDirectoryPath(), pm.getFileId(), pm.getId()).getContent().createInputStream());
            if (event.getLinkingAgentIdentifier() != null && !event.getLinkingAgentIdentifier().isEmpty()) {
              for (LinkingAgentIdentifierComplexType laict : event.getLinkingAgentIdentifier()) {
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
    }

    AssertJUnit.assertTrue(found);

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_TYPE,
      PreservationEventType.FORMAT_IDENTIFICATION.toString()));
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()));
    IndexResult<IndexedPreservationEvent> events = index.find(IndexedPreservationEvent.class, filter, null,
      new Sublist(0, 10), new ArrayList<>());
    AssertJUnit.assertEquals(1, events.getTotalCount());
  }

  @Test
  public void testSiegfriedUsingRepresentation() throws RODAException, IOException {
    AIP aip = ingestCorpora();

    // ensure PREMIS objects are created
    TestsHelper.executeJob(PremisSkeletonPlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, aip.getId()));

    // run siegfried
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_REPORTING_CLASS, FAKE_REPORTING_CLASS);
    Job job = TestsHelper.executeJob(SiegfriedPlugin.class, parameters, PluginType.AIP_TO_AIP,
      SelectedItemsAll.create(org.roda.core.data.v2.ip.Representation.class));
    TestsHelper.getJobReports(index, job, true);

    aip = model.retrieveAIP(aip.getId());

    // Files with Siegfried output
    AssertJUnit.assertEquals(CORPORA_FILES_COUNT,
      Iterables.size(model.listOtherMetadata(aip.getId(), RodaConstants.OTHER_METADATA_TYPE_SIEGFRIED, true)));

    Binary om = model.retrieveOtherMetadataBinary(aip.getId(), aip.getRepresentations().get(0).getId(),
      List.of(CORPORA_TEST1), CORPORA_TEST1_TXT, SiegfriedPlugin.FILE_SUFFIX,
      RodaConstants.OTHER_METADATA_TYPE_SIEGFRIED);

    AssertJUnit.assertNotNull(om);

    Binary fpo_bin = model.retrievePreservationFile(aip.getId(), aip.getRepresentations().get(0).getId(),
      List.of(CORPORA_TEST1), CORPORA_TEST1_TXT);

    gov.loc.premis.v3.File fpo = PremisV3Utils.binaryToFile(fpo_bin.getContent(), true);

    FormatComplexType format = fpo.getObjectCharacteristics().get(0).getFormat().get(0);
    AssertJUnit.assertEquals("Plain Text File", format.getFormatDesignation().get(0).getFormatName().getValue());
    FormatRegistryComplexType pronomRegistry = PremisV3Utils.getFormatRegistry(fpo,
      RodaConstants.PRESERVATION_REGISTRY_PRONOM);
    AssertJUnit.assertEquals(RodaConstants.PRESERVATION_REGISTRY_PRONOM,
      pronomRegistry.getFormatRegistryName().getValue());
    AssertJUnit.assertEquals("x-fmt/111", pronomRegistry.getFormatRegistryKey().getValue());

    FormatRegistryComplexType mimeRegistry = PremisV3Utils.getFormatRegistry(fpo,
      RodaConstants.PRESERVATION_REGISTRY_MIME);
    String mimetype = "text/plain";
    AssertJUnit.assertEquals(mimetype, mimeRegistry.getFormatRegistryKey().getValue());

    index.commitAIPs();

    IndexedFile indFile = index.retrieve(IndexedFile.class, IdUtils.getFileId(aip.getId(),
      aip.getRepresentations().get(0).getId(), List.of(CORPORA_TEST1), CORPORA_TEST1_TXT), new ArrayList<>());

    AssertJUnit.assertEquals(mimetype, indFile.getFileFormat().getMimeType());
    AssertJUnit.assertEquals("x-fmt/111", indFile.getFileFormat().getPronom());
    AssertJUnit.assertEquals("Plain Text File", indFile.getFileFormat().getFormatDesignationName());

    List<String> suggest = index.suggest(IndexedFile.class, RodaConstants.FILE_FORMAT_MIMETYPE,
      mimetype.substring(0, 1), null, false, false);
    MatcherAssert.assertThat(suggest, contains(mimetype));

    Plugin<? extends IsRODAObject> plugin = RodaCoreFactory.getPluginManager()
      .getPlugin(SiegfriedPlugin.class.getName());
    String agentID = PluginHelper.getPluginAgentId(plugin);

    boolean found = false;

    try (CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadataList = model
      .listPreservationMetadata(aip.getId(), true)) {
      for (OptionalWithCause<PreservationMetadata> opm : preservationMetadataList) {
        if (opm.isPresent()) {
          PreservationMetadata pm = opm.get();
          if (pm.getType().equals(PreservationMetadataType.EVENT)) {
            EventComplexType event = PremisV3Utils
              .binaryToEvent(model.retrievePreservationEvent(pm.getAipId(), pm.getRepresentationId(),
                pm.getFileDirectoryPath(), pm.getFileId(), pm.getId()).getContent().createInputStream());
            if (event.getLinkingAgentIdentifier() != null && !event.getLinkingAgentIdentifier().isEmpty()) {
              for (LinkingAgentIdentifierComplexType laict : event.getLinkingAgentIdentifier()) {
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
    }

    AssertJUnit.assertTrue(found);

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_TYPE,
      PreservationEventType.FORMAT_IDENTIFICATION.toString()));
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()));
    IndexResult<IndexedPreservationEvent> events = index.find(IndexedPreservationEvent.class, filter, null,
      new Sublist(0, 10), new ArrayList<>());
    AssertJUnit.assertEquals(1, events.getTotalCount());
  }

  @Test
  public void testSiegfriedUsingFile() throws RODAException, IOException {
    AIP aip = ingestCorpora();

    // ensure PREMIS objects are created
    TestsHelper.executeJob(PremisSkeletonPlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, aip.getId()));

    // run siegfried
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_REPORTING_CLASS, FAKE_REPORTING_CLASS);
    Job job = TestsHelper.executeJob(SiegfriedPlugin.class, parameters, PluginType.AIP_TO_AIP,
      SelectedItemsAll.create(org.roda.core.data.v2.ip.File.class));
    TestsHelper.getJobReports(index, job, true);

    aip = model.retrieveAIP(aip.getId());

    // Files with Siegfried output
    AssertJUnit.assertEquals(CORPORA_FILES_COUNT,
      Iterables.size(model.listOtherMetadata(aip.getId(), RodaConstants.OTHER_METADATA_TYPE_SIEGFRIED, true)));

    Binary om = model.retrieveOtherMetadataBinary(aip.getId(), aip.getRepresentations().get(0).getId(),
      List.of(CORPORA_TEST1), CORPORA_TEST1_TXT, SiegfriedPlugin.FILE_SUFFIX,
      RodaConstants.OTHER_METADATA_TYPE_SIEGFRIED);

    AssertJUnit.assertNotNull(om);

    Binary fpo_bin = model.retrievePreservationFile(aip.getId(), aip.getRepresentations().get(0).getId(),
      List.of(CORPORA_TEST1), CORPORA_TEST1_TXT);

    gov.loc.premis.v3.File fpo = PremisV3Utils.binaryToFile(fpo_bin.getContent(), true);

    FormatComplexType format = fpo.getObjectCharacteristics().get(0).getFormat().get(0);
    AssertJUnit.assertEquals("Plain Text File", format.getFormatDesignation().get(0).getFormatName().getValue());
    FormatRegistryComplexType pronomRegistry = PremisV3Utils.getFormatRegistry(fpo,
      RodaConstants.PRESERVATION_REGISTRY_PRONOM);
    AssertJUnit.assertEquals(RodaConstants.PRESERVATION_REGISTRY_PRONOM,
      pronomRegistry.getFormatRegistryName().getValue());
    AssertJUnit.assertEquals("x-fmt/111", pronomRegistry.getFormatRegistryKey().getValue());

    FormatRegistryComplexType mimeRegistry = PremisV3Utils.getFormatRegistry(fpo,
      RodaConstants.PRESERVATION_REGISTRY_MIME);
    String mimetype = "text/plain";
    AssertJUnit.assertEquals(mimetype, mimeRegistry.getFormatRegistryKey().getValue());

    index.commitAIPs();

    IndexedFile indFile = index.retrieve(IndexedFile.class, IdUtils.getFileId(aip.getId(),
      aip.getRepresentations().get(0).getId(), List.of(CORPORA_TEST1), CORPORA_TEST1_TXT), new ArrayList<>());

    AssertJUnit.assertEquals(mimetype, indFile.getFileFormat().getMimeType());
    AssertJUnit.assertEquals("x-fmt/111", indFile.getFileFormat().getPronom());
    AssertJUnit.assertEquals("Plain Text File", indFile.getFileFormat().getFormatDesignationName());

    List<String> suggest = index.suggest(IndexedFile.class, RodaConstants.FILE_FORMAT_MIMETYPE,
      mimetype.substring(0, 1), null, false, false);
    MatcherAssert.assertThat(suggest, contains(mimetype));

    Plugin<? extends IsRODAObject> plugin = RodaCoreFactory.getPluginManager()
      .getPlugin(SiegfriedPlugin.class.getName());
    String agentID = PluginHelper.getPluginAgentId(plugin);

    boolean found = false;

    try (CloseableIterable<OptionalWithCause<PreservationMetadata>> preservationMetadataList = model
      .listPreservationMetadata(aip.getId(), true)) {
      for (OptionalWithCause<PreservationMetadata> opm : preservationMetadataList) {
        if (opm.isPresent()) {
          PreservationMetadata pm = opm.get();
          if (pm.getType().equals(PreservationMetadataType.EVENT)) {
            EventComplexType event = PremisV3Utils
              .binaryToEvent(model.retrievePreservationEvent(pm.getAipId(), pm.getRepresentationId(),
                pm.getFileDirectoryPath(), pm.getFileId(), pm.getId()).getContent().createInputStream());
            if (event.getLinkingAgentIdentifier() != null && !event.getLinkingAgentIdentifier().isEmpty()) {
              for (LinkingAgentIdentifierComplexType laict : event.getLinkingAgentIdentifier()) {
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
    }

    AssertJUnit.assertTrue(found);

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_TYPE,
      PreservationEventType.FORMAT_IDENTIFICATION.toString()));
    filter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()));
    IndexResult<IndexedPreservationEvent> events = index.find(IndexedPreservationEvent.class, filter, null,
      new Sublist(0, 10), new ArrayList<>());
    AssertJUnit.assertEquals(16, events.getTotalCount());
  }

  @Test
  public void testAutoAccept() throws RODAException {
    AIP aip = ingestCorpora();

    TestsHelper.executeJob(AutoAcceptSIPPlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, aip.getId()));

    aip = model.retrieveAIP(aip.getId());
    MatcherAssert.assertThat(aip.getState(), Is.is(AIPState.ACTIVE));
  }

  @Test
  public void testPremisURN() throws IOException, RODAException, URISyntaxException {
    final URL corporaURL = EARKSIPPluginsTest.class.getResource("/corpora");
    Assert.assertNotNull(corporaURL);
    final Path corporaPath = Paths.get(corporaURL.toURI());

    final TransferredResource transferredResource = EARKSIPPluginsTest.createIngestCorpora(corporaPath, index,
      "earkSip_twoFiles_with_same_name.zip");

    final Map<String, String> parameters = new HashMap<>();
    parameters.put("parameter.sip_to_aip_class", EARKSIP2ToAIPPlugin.class.getName());

    final Job job = TestsHelper.executeJob(MinimalIngestPlugin.class, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));

    TestsHelper.getJobReports(index, job, true);

    index.commitAIPs();
    final IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.INGEST_JOB_ID, job.getId())), null, new Sublist(0, 10),
      new ArrayList<>());
    final IndexedAIP indexedAIP = find.getResults().get(0);

    final AIP aip = model.retrieveAIP(indexedAIP.getId());

    final Binary binary = model.retrievePreservationRepresentation(aip.getId(),
      aip.getRepresentations().get(0).getId());
    final Representation representation = PremisV3Utils.binaryToRepresentation(binary.getContent(), false);
    List<String> collect = representation.getRelationship().stream()
      .map(p -> p.getRelatedObjectIdentifier().get(0).getRelatedObjectIdentifierValue()).collect(Collectors.toList());

    assertThat(collect, hasSize(2));

    assertThat(collect,
      containsInAnyOrder("urn:roda:premis:file:f2-image (1).png", "urn:roda:premis:file:f1-image (1).png"));
  }
}
