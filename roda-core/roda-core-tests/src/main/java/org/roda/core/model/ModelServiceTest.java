/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.log.LogEntryParameter;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageTestUtils;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

import gov.loc.premis.v3.AgentComplexType;
import gov.loc.premis.v3.EventComplexType;
import gov.loc.premis.v3.ObjectCharacteristicsComplexType;
import gov.loc.premis.v3.ObjectIdentifierComplexType;
import jersey.repackaged.com.google.common.collect.Lists;

/**
 * Unit tests for ModelService
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 * 
 * @see ModelService
 */
// @PrepareForTest({})
@Test(groups = {"all", "travis"})
public class ModelServiceTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(ModelServiceTest.class);
  private static final String ROLE1 = RodaConstants.REPOSITORY_PERMISSIONS_DESCRIPTIVE_METADATA_UPDATE;
  private static final String ROLE2 = RodaConstants.REPOSITORY_PERMISSIONS_LOG_ENTRY_READ;

  private static Path basePath;
  private static Path logPath;
  private static StorageService storage;
  private static ModelService model;

  private static Path corporaPath;
  private static StorageService corporaService;
  private static String aipCreator = "admin";

  @BeforeClass
  public static void setUp() throws IOException, URISyntaxException, GenericException {
    URL corporaURL = ModelServiceTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    LOGGER.debug("Running model test under storage: " + basePath);
  }

  @BeforeMethod
  public void init() throws IOException, GenericException {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);

    boolean deploySolr = false;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = false;
    boolean deployPluginManager = false;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources);

    logPath = RodaCoreFactory.getLogPath();
    storage = RodaCoreFactory.getStorageService();
    model = RodaCoreFactory.getModelService();
  }

  @AfterMethod
  public void cleanup() throws NotFoundException, GenericException, IOException {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testCreateAIP() throws RODAException, ParseException, IOException, XmlException {

    // generate AIP ID
    final String aipId = CorporaConstants.SOURCE_AIP_ID;

    // testing AIP
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    assertNotNull(aip);
    assertEquals(aipId, aip.getId());
    assertNull("AIP_1 should not have a parent", aip.getParentId());
    assertThat(aip.getState(), Is.is(AIPState.ACTIVE));

    List<String> descriptiveMetadataIds = aip.getDescriptiveMetadata().stream().map(dm -> dm.getId())
      .collect(Collectors.toList());
    assertThat(descriptiveMetadataIds, containsInAnyOrder(CorporaConstants.DESCRIPTIVE_METADATA_ID));

    List<String> representationIds = aip.getRepresentations().stream().map(rep -> rep.getId())
      .collect(Collectors.toList());

    assertThat(representationIds,
      containsInAnyOrder(CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_2_ID));

    // testing descriptive metadata
    final DescriptiveMetadata descMetadata = model.retrieveDescriptiveMetadata(aipId,
      CorporaConstants.DESCRIPTIVE_METADATA_ID);

    assertEquals(aipId, descMetadata.getAipId());
    assertEquals(CorporaConstants.DESCRIPTIVE_METADATA_ID, descMetadata.getId());
    assertEquals(CorporaConstants.DESCRIPTIVE_METADATA_TYPE, descMetadata.getType());

    StoragePath descriptiveMetadataPath = ModelUtils.getDescriptiveMetadataStoragePath(descMetadata.getAipId(),
      descMetadata.getId());
    final Binary descMetadataBinary = storage.getBinary(descriptiveMetadataPath);
    assertTrue(descMetadataBinary.getSizeInBytes() > 0);
    assertEquals(descMetadataBinary.getSizeInBytes().intValue(),
      IOUtils.toByteArray(descMetadataBinary.getContent().createInputStream()).length);

    // testing representations
    final Representation representation1 = model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);
    assertEquals(aipId, representation1.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_1_ID, representation1.getId());
    assertEquals(CorporaConstants.REPRESENTATION_1_ORIGINAL, representation1.isOriginal());

    CloseableIterable<OptionalWithCause<File>> allRep1Files = model.listFilesUnder(aipId,
      CorporaConstants.REPRESENTATION_1_ID, true);
    List<String> allRep1FileIds = Lists.newArrayList(allRep1Files).stream().filter(f -> f.isPresent())
      .map(f -> f.get().getId()).collect(Collectors.toList());
    allRep1Files.close();

    assertThat(allRep1FileIds,
      containsInAnyOrder(CorporaConstants.REPRESENTATION_1_FILE_1_ID, CorporaConstants.REPRESENTATION_1_FILE_2_ID));

    final Representation representation2 = model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_2_ID);
    assertEquals(aipId, representation2.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_2_ID, representation2.getId());
    assertEquals(CorporaConstants.REPRESENTATION_2_ORIGINAL, representation2.isOriginal());

    CloseableIterable<OptionalWithCause<File>> allRep2Files = model.listFilesUnder(aipId,
      CorporaConstants.REPRESENTATION_2_ID, true);
    List<String> allRep2FileIds = Lists.newArrayList(allRep2Files).stream().filter(f -> f.isPresent())
      .map(f -> f.get().getId()).collect(Collectors.toList());
    allRep2Files.close();

    assertThat(allRep2FileIds,
      containsInAnyOrder(CorporaConstants.REPRESENTATION_2_FILE_1_ID, CorporaConstants.REPRESENTATION_2_FILE_2_ID));

    // testing files
    final File file_1_1 = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_FILE_1_PATH, CorporaConstants.REPRESENTATION_1_FILE_1_ID);
    assertEquals(aipId, file_1_1.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_1_ID, file_1_1.getRepresentationId());
    assertEquals(CorporaConstants.REPRESENTATION_1_FILE_1_ID, file_1_1.getId());

    final Binary binary_1_1 = storage.getBinary(ModelUtils.getFileStoragePath(file_1_1));
    assertTrue(binary_1_1.getSizeInBytes() > 0);
    assertEquals(binary_1_1.getSizeInBytes().intValue(),
      IOUtils.toByteArray(binary_1_1.getContent().createInputStream()).length);

    final File file_1_2 = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_FILE_2_PATH, CorporaConstants.REPRESENTATION_1_FILE_2_ID);
    assertEquals(aipId, file_1_2.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_1_ID, file_1_2.getRepresentationId());
    assertEquals(CorporaConstants.REPRESENTATION_1_FILE_2_ID, file_1_2.getId());

    final Binary binary_1_2 = storage.getBinary(ModelUtils.getFileStoragePath(file_1_2));
    assertTrue(binary_1_2.getSizeInBytes() > 0);
    assertEquals(binary_1_2.getSizeInBytes().intValue(),
      IOUtils.toByteArray(binary_1_2.getContent().createInputStream()).length);

    final File file_2_1 = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_2_ID,
      CorporaConstants.REPRESENTATION_2_FILE_1_PATH, CorporaConstants.REPRESENTATION_2_FILE_1_ID);
    assertEquals(aipId, file_2_1.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_2_ID, file_2_1.getRepresentationId());
    assertEquals(CorporaConstants.REPRESENTATION_2_FILE_1_ID, file_2_1.getId());

    final Binary binary_2_1 = storage.getBinary(ModelUtils.getFileStoragePath(file_2_1));
    assertTrue(binary_2_1.getSizeInBytes() > 0);
    assertEquals(binary_2_1.getSizeInBytes().intValue(),
      IOUtils.toByteArray(binary_2_1.getContent().createInputStream()).length);

    final File file_2_2 = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_2_ID,
      CorporaConstants.REPRESENTATION_2_FILE_2_PATH, CorporaConstants.REPRESENTATION_2_FILE_2_ID);
    assertEquals(aipId, file_2_2.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_2_ID, file_2_2.getRepresentationId());
    assertEquals(CorporaConstants.REPRESENTATION_2_FILE_2_ID, file_2_2.getId());

    final Binary binary_2_2 = storage.getBinary(ModelUtils.getFileStoragePath(file_2_2));
    assertTrue(binary_2_2.getSizeInBytes() > 0);
    assertEquals(binary_2_2.getSizeInBytes().intValue(),
      IOUtils.toByteArray(binary_2_2.getContent().createInputStream()).length);

    // test preservation metadata

    Binary preservationObject = model.retrievePreservationRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);
    gov.loc.premis.v3.Representation rpo = PremisV3Utils.binaryToRepresentation(preservationObject.getContent(), true);

    ObjectIdentifierComplexType[] objectIdentifierArray = rpo.getObjectIdentifierArray();
    assertEquals(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN,
      objectIdentifierArray[0].getObjectIdentifierType().getStringValue());
    assertEquals(CorporaConstants.REPRESENTATION_1_URN, rpo.getObjectIdentifierArray()[0].getObjectIdentifierValue());
    assertEquals(CorporaConstants.PRESERVATION_LEVEL_FULL,
      rpo.getPreservationLevelArray(0).getPreservationLevelValue().getStringValue());

    Binary f0_premis_bin = model.retrievePreservationFile(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_FILE_1_PATH, CorporaConstants.REPRESENTATION_1_FILE_1_ID);
    gov.loc.premis.v3.File f0_premis_file = PremisV3Utils.binaryToFile(f0_premis_bin.getContent(), true);

    ObjectCharacteristicsComplexType f0_characteristics = f0_premis_file.getObjectCharacteristicsArray(0);
    assertEquals(0, f0_characteristics.getCompositionLevel().getBigIntegerValue().intValue());
    assertEquals(0, f0_characteristics.getCompositionLevel().getBigIntegerValue().intValue());

    assertEquals(f0_characteristics.getFormatArray(0).getFormatDesignation().getFormatName().getStringValue(),
      CorporaConstants.TEXT_XML);

    Binary event_premis_bin = model.retrievePreservationEvent(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_PREMIS_EVENT_ID);
    EventComplexType event_premis = PremisV3Utils.binaryToEvent(event_premis_bin.getContent(), true);
    assertEquals(CorporaConstants.INGESTION, event_premis.getEventType().getStringValue());
    assertEquals(CorporaConstants.SUCCESS,
      event_premis.getEventOutcomeInformationArray(0).getEventOutcome().getStringValue());
  }

  @Test
  public void testCreateAIPVersionEAD3() throws RODAException, ParseException, IOException, XmlException {

    // generate AIP ID
    final String aipId = UUID.randomUUID().toString();

    // testing AIP
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_VERSION_EAD_3),
      aipCreator);

    assertNotNull(aip);
    assertEquals(aipId, aip.getId());
    assertNull("AIP_1 should not have a parent", aip.getParentId());
    assertThat(aip.getState(), Is.is(AIPState.ACTIVE));

    List<String> descriptiveMetadataIds = aip.getDescriptiveMetadata().stream().map(dm -> dm.getId())
      .collect(Collectors.toList());
    assertThat(descriptiveMetadataIds, containsInAnyOrder(CorporaConstants.DESCRIPTIVE_METADATA_ID_EAD3));

    List<String> representationIds = aip.getRepresentations().stream().map(rep -> rep.getId())
      .collect(Collectors.toList());

    assertThat(representationIds,
      containsInAnyOrder(CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_2_ID));

    // testing descriptive metadata
    final DescriptiveMetadata descMetadata = model.retrieveDescriptiveMetadata(aipId,
      CorporaConstants.DESCRIPTIVE_METADATA_ID_EAD3);

    assertEquals(aipId, descMetadata.getAipId());
    assertEquals(CorporaConstants.DESCRIPTIVE_METADATA_ID_EAD3, descMetadata.getId());
    assertEquals(CorporaConstants.DESCRIPTIVE_METADATA_TYPE_EAD, descMetadata.getType());
    assertEquals(CorporaConstants.DESCRIPTIVE_METADATA_TYPE_EAD_VERSION3, descMetadata.getVersion());

    StoragePath descriptiveMetadataPath = ModelUtils.getDescriptiveMetadataStoragePath(descMetadata.getAipId(),
      descMetadata.getId());
    final Binary descMetadataBinary = storage.getBinary(descriptiveMetadataPath);
    assertTrue(descMetadataBinary.getSizeInBytes() > 0);
    assertEquals(descMetadataBinary.getSizeInBytes().intValue(),
      IOUtils.toByteArray(descMetadataBinary.getContent().createInputStream()).length);
  }

  @Test
  public void testCreateAIPVersionUnknown() throws RODAException, ParseException, IOException, XmlException {

    // generate AIP ID
    final String aipId = UUID.randomUUID().toString();

    // testing AIP
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_VERSION_EAD_UNKNOWN),
      aipCreator);

    assertNotNull(aip);
    assertEquals(aipId, aip.getId());
    assertNull("AIP_1 should not have a parent", aip.getParentId());
    assertThat(aip.getState(), Is.is(AIPState.ACTIVE));

    List<String> descriptiveMetadataIds = aip.getDescriptiveMetadata().stream().map(dm -> dm.getId())
      .collect(Collectors.toList());
    assertThat(descriptiveMetadataIds, containsInAnyOrder(CorporaConstants.DESCRIPTIVE_METADATA_ID_EADUNKNOWN));

    List<String> representationIds = aip.getRepresentations().stream().map(rep -> rep.getId())
      .collect(Collectors.toList());

    assertThat(representationIds,
      containsInAnyOrder(CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_2_ID));

    // testing descriptive metadata
    final DescriptiveMetadata descMetadata = model.retrieveDescriptiveMetadata(aipId,
      CorporaConstants.DESCRIPTIVE_METADATA_ID_EADUNKNOWN);

    assertEquals(aipId, descMetadata.getAipId());
    assertEquals(CorporaConstants.DESCRIPTIVE_METADATA_ID_EADUNKNOWN, descMetadata.getId());
    assertEquals(CorporaConstants.DESCRIPTIVE_METADATA_TYPE_EAD, descMetadata.getType());
    assertEquals(CorporaConstants.DESCRIPTIVE_METADATA_TYPE_EAD_VERSIONUNKNOWN, descMetadata.getVersion());

    StoragePath descriptiveMetadataPath = ModelUtils.getDescriptiveMetadataStoragePath(descMetadata.getAipId(),
      descMetadata.getId());
    final Binary descMetadataBinary = storage.getBinary(descriptiveMetadataPath);
    assertTrue(descMetadataBinary.getSizeInBytes() > 0);
    assertEquals(descMetadataBinary.getSizeInBytes().intValue(),
      IOUtils.toByteArray(descMetadataBinary.getContent().createInputStream()).length);
  }

  @Test
  public void testCreateAIPWithSubFolders() throws RODAException, ParseException, IOException {

    // generate AIP ID
    final String aipId = UUID.randomUUID().toString();

    // testing AIP
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_REP_WITH_SUBFOLDERS),
      aipCreator);

    CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aipId,
      CorporaConstants.REPRESENTATION_1_ID, true);

    List<File> reusableList = new ArrayList<>();
    Iterables.addAll(reusableList,
      Lists.newArrayList(allFiles).stream().filter(f -> f.isPresent()).map(f -> f.get()).collect(Collectors.toList()));
    allFiles.close();

    assertTrue(reusableList.contains(
      new File("2012-roda-promo-en.pdf", aipId, CorporaConstants.REPRESENTATION_1_ID, new ArrayList<>(), false)));
    assertTrue(
      reusableList.contains(new File("folder", aipId, CorporaConstants.REPRESENTATION_1_ID, new ArrayList<>(), true)));
    assertTrue(reusableList
      .contains(new File("subfolder", aipId, CorporaConstants.REPRESENTATION_1_ID, Arrays.asList("folder"), true)));
    assertTrue(reusableList.contains(new File("RODA 2 logo.svg", aipId, CorporaConstants.REPRESENTATION_1_ID,
      Arrays.asList("folder", "subfolder"), false)));

    assertTrue(reusableList.contains(new File("RODA 2 logo-circle-black.svg", aipId,
      CorporaConstants.REPRESENTATION_1_ID, Arrays.asList("folder"), false)));
    assertTrue(reusableList.contains(new File("RODA 2 logo-circle-white.svg", aipId,
      CorporaConstants.REPRESENTATION_1_ID, Arrays.asList("folder"), false)));

    // assertThat(allFiles, containsInAnyOrder());
  }

  @Test
  public void testDeleteAIP() throws RODAException {
    // generate AIP ID
    final String aipId = UUID.randomUUID().toString();

    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    model.deleteAIP(aipId);

    // check if AIP was indeed deleted
    try {
      model.retrieveAIP(aipId);
      Assert.fail("AIP should have been deleted, but yet was retrieved.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }

    // check if AIP sub-resources were recursively deleted
    try {
      model.retrieveDescriptiveMetadata(aipId, CorporaConstants.DESCRIPTIVE_METADATA_ID);
      Assert.fail("Descriptive metadata binary should have been deleted, but yet was retrieved.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }

    try {
      model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);
      Assert.fail("Descriptive metadata binary should have been deleted, but yet was retrieved.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }

    try {
      model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_2_ID);
      Assert.fail("Representation should have been deleted, but yet was retrieved.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }

    try {
      model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_1_FILE_1_PATH,
        CorporaConstants.REPRESENTATION_1_FILE_1_ID);
      Assert.fail("File should have been deleted, but yet was retrieved.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }
  }

  // TODO test if deleting AIP also deletes all sub-AIPs

  @Test
  public void testListAIPs() throws RODAException {

    // generate AIP ID
    final String aip1Id = UUID.randomUUID().toString();
    final String aip2Id = UUID.randomUUID().toString();
    final String aip3Id = UUID.randomUUID().toString();

    final AIP aip1 = model.createAIP(aip1Id, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);
    final AIP aip2 = model.createAIP(aip2Id, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);
    final AIP aip3 = model.createAIP(aip3Id, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    Iterable<OptionalWithCause<AIP>> listAIPs = model.listAIPs();
    List<AIP> reusableList = new ArrayList<>();
    Iterables.addAll(reusableList,
      Lists.newArrayList(listAIPs).stream().filter(f -> f.isPresent()).map(f -> f.get()).collect(Collectors.toList()));

    assertThat(reusableList, containsInAnyOrder(aip1, aip2, aip3));

  }

  @Test
  public void testCreateAIPWithExistingId() throws RODAException {
    // generate AIP ID
    final String aipId = UUID.randomUUID().toString();

    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    try {
      model.createAIP(aipId, corporaService,
        DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);
      Assert.fail("AIP shouldn't have been created and yet it was.");
    } catch (AlreadyExistsException e) {
      // do nothing as it was expected
    }
  }

  @Test
  public void testUpdateAIP() throws RODAException, IOException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    StoragePath otherAipPath = DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER,
      CorporaConstants.OTHER_AIP_ID);
    AIP updatedAIP = model.updateAIP(aipId, corporaService, otherAipPath, aipCreator);

    // check it is connected
    AIP retrievedAIP = model.retrieveAIP(aipId);
    assertEquals(updatedAIP, retrievedAIP);

    // check content is correct
    StorageTestUtils.testEntityEqualRecursively(corporaService, otherAipPath, storage,
      ModelUtils.getAIPStoragePath(aipId));
  }

  @Test
  public void testListDescriptiveMetadata() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    Iterable<DescriptiveMetadata> list = model.retrieveAIP(aipId).getDescriptiveMetadata();
    DescriptiveMetadata descriptiveMetadata1 = model.retrieveDescriptiveMetadata(aipId,
      CorporaConstants.DESCRIPTIVE_METADATA_ID);

    assertThat(list, containsInAnyOrder(descriptiveMetadata1));
  }

  @Test
  public void testCreateDescriptiveMetadata() throws RODAException, IOException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    final String newDescriptiveMetadataId = UUID.randomUUID().toString();
    final Binary binary = corporaService
      .getBinary(DefaultStoragePath.parse(CorporaConstants.OTHER_DESCRIPTIVE_METADATA_STORAGEPATH));

    final DescriptiveMetadata newDescriptiveMetadata = model.createDescriptiveMetadata(aipId, newDescriptiveMetadataId,
      binary.getContent(), CorporaConstants.OTHER_DESCRIPTIVE_METADATA_TYPE,
      CorporaConstants.OTHER_DESCRIPTIVE_METADATA_VERSION);

    // check if it is connected
    DescriptiveMetadata retrievedDescriptiveMetadata = model.retrieveDescriptiveMetadata(aipId,
      newDescriptiveMetadataId);
    assertEquals(newDescriptiveMetadata, retrievedDescriptiveMetadata);

    // check content
    Binary newDescriptiveMetadataBinary = storage
      .getBinary(ModelUtils.getDescriptiveMetadataStoragePath(newDescriptiveMetadata));
    assertTrue(IOUtils.contentEquals(binary.getContent().createInputStream(),
      newDescriptiveMetadataBinary.getContent().createInputStream()));

  }

  @Test
  public void testUpdateDescriptiveMetadata() throws RODAException, IOException, ValidationException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    final Binary binary = corporaService
      .getBinary(DefaultStoragePath.parse(CorporaConstants.OTHER_DESCRIPTIVE_METADATA_STORAGEPATH));

    Map<String, String> properties = new HashMap<String, String>();
    properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());

    final DescriptiveMetadata updatedDescriptiveMetadata = model.updateDescriptiveMetadata(aipId,
      CorporaConstants.DESCRIPTIVE_METADATA_ID, binary.getContent(), CorporaConstants.OTHER_DESCRIPTIVE_METADATA_TYPE,
      CorporaConstants.OTHER_DESCRIPTIVE_METADATA_VERSION, properties);

    // check if it is connected
    DescriptiveMetadata retrievedDescriptiveMetadata = model.retrieveDescriptiveMetadata(aipId,
      CorporaConstants.DESCRIPTIVE_METADATA_ID);
    assertEquals(updatedDescriptiveMetadata, retrievedDescriptiveMetadata);

    // check content
    StoragePath storagePath = ModelUtils.getDescriptiveMetadataStoragePath(updatedDescriptiveMetadata);
    Binary updatedDescriptiveMetadataBinary = storage.getBinary(storagePath);
    assertTrue(IOUtils.contentEquals(binary.getContent().createInputStream(),
      updatedDescriptiveMetadataBinary.getContent().createInputStream()));

    // check if binary version was created
    assertEquals(1, Iterables.size(storage.listBinaryVersions(storagePath)));

    // check if binary version message collisions are well treated
    model.updateDescriptiveMetadata(aipId, CorporaConstants.DESCRIPTIVE_METADATA_ID, binary.getContent(),
      CorporaConstants.OTHER_DESCRIPTIVE_METADATA_TYPE, CorporaConstants.OTHER_DESCRIPTIVE_METADATA_VERSION,
      properties);
    model.updateDescriptiveMetadata(aipId, CorporaConstants.DESCRIPTIVE_METADATA_ID, binary.getContent(),
      CorporaConstants.OTHER_DESCRIPTIVE_METADATA_TYPE, CorporaConstants.OTHER_DESCRIPTIVE_METADATA_VERSION,
      properties);
    model.updateDescriptiveMetadata(aipId, CorporaConstants.DESCRIPTIVE_METADATA_ID, binary.getContent(),
      CorporaConstants.OTHER_DESCRIPTIVE_METADATA_TYPE, CorporaConstants.OTHER_DESCRIPTIVE_METADATA_VERSION,
      properties);

    assertEquals(4, Iterables.size(storage.listBinaryVersions(storagePath)));
  }

  @Test
  public void testDeleteDescriptiveMetadata() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    model.deleteDescriptiveMetadata(aipId, CorporaConstants.DESCRIPTIVE_METADATA_ID);

    // check if it deleted
    try {
      model.retrieveDescriptiveMetadata(aipId, CorporaConstants.DESCRIPTIVE_METADATA_ID);
      Assert.fail("Descriptive metadata deleted and yet exists.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }
  }

  @Test
  public void testListRepresentations() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    final Representation representation1 = model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);
    final Representation representation2 = model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_2_ID);

    final Iterable<Representation> list = model.retrieveAIP(aipId).getRepresentations();
    assertThat(list, containsInAnyOrder(representation1, representation2));

    CloseableIterable<OptionalWithCause<Representation>> list2 = model.list(Representation.class);
    assertThat(Iterables.transform(list2, i -> i.get()), containsInAnyOrder(representation1, representation2));

  }

  @Test
  public void testCreateRepresentation() throws RODAException, IOException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    final String newRepresentationId = UUID.randomUUID().toString();
    final StoragePath corporaRepresentationPath = DefaultStoragePath
      .parse(CorporaConstants.OTHER_REPRESENTATION_STORAGEPATH);

    Representation createdRepresentation = model.createRepresentation(aipId, newRepresentationId,
      CorporaConstants.REPRESENTATION_1_ORIGINAL, CorporaConstants.REPRESENTATION_1_TYPE, corporaService,
      DefaultStoragePath.parse(CorporaConstants.OTHER_REPRESENTATION_STORAGEPATH));

    // check if it is connected
    Representation retrievedRepresentation = model.retrieveRepresentation(aipId, newRepresentationId);
    assertEquals(createdRepresentation, retrievedRepresentation);

    // check content
    StorageTestUtils.testEntityEqualRecursively(corporaService, corporaRepresentationPath, storage,
      ModelUtils.getRepresentationStoragePath(aipId, newRepresentationId));
  }

  @Test
  public void testUpdateRepresentation() throws RODAException, IOException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    final StoragePath corporaRepresentationPath = DefaultStoragePath
      .parse(CorporaConstants.OTHER_REPRESENTATION_STORAGEPATH);
    Representation updatedRepresentation = model.updateRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_ORIGINAL, CorporaConstants.REPRESENTATION_1_TYPE, corporaService,
      DefaultStoragePath.parse(CorporaConstants.OTHER_REPRESENTATION_STORAGEPATH));

    // check if it is connected
    Representation retrievedRepresentation = model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);
    assertEquals(updatedRepresentation, retrievedRepresentation);

    // check content
    StorageTestUtils.testEntityEqualRecursively(corporaService, corporaRepresentationPath, storage,
      ModelUtils.getRepresentationStoragePath(aipId, CorporaConstants.REPRESENTATION_1_ID));
  }

  @Test
  public void testDeleteRepresentation() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    model.deleteRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);

    // check if it deleted
    try {
      model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);
      Assert.fail("Representation deleted and yet exists.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }

    // check if file under representation was deleted
    try {
      model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_1_FILE_1_PATH,
        CorporaConstants.REPRESENTATION_1_FILE_1_ID);
      Assert.fail("Representation deleted and yet one of its files still exist.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }
  }

  @Test
  public void testCreateFile() throws RODAException, IOException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    final String newFileId = UUID.randomUUID().toString();
    final List<String> newFileDirectoryPath = new ArrayList<>();
    final StoragePath corporaFilePath = DefaultStoragePath.parse(CorporaConstants.OTHER_FILE_STORAGEPATH);
    final Binary binary = corporaService.getBinary(corporaFilePath);

    File createdFile = model.createFile(aipId, CorporaConstants.REPRESENTATION_1_ID, newFileDirectoryPath, newFileId,
      binary.getContent(), true);

    // check if it is connected
    File retrievedFile = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID, newFileDirectoryPath,
      newFileId);
    assertEquals(createdFile, retrievedFile);

    // check content
    Binary createdFileBinary = storage.getBinary(ModelUtils.getFileStoragePath(createdFile));
    assertTrue(IOUtils.contentEquals(binary.getContent().createInputStream(),
      createdFileBinary.getContent().createInputStream()));
  }

  @Test
  public void testUpdateFile() throws RODAException, IOException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    final StoragePath corporaFilePath = DefaultStoragePath.parse(CorporaConstants.OTHER_FILE_STORAGEPATH);
    final Binary binary = corporaService.getBinary(corporaFilePath);

    final boolean createIfNotExists = false;
    final boolean notify = false;
    File createdFile = model.updateFile(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_FILE_1_PATH, CorporaConstants.REPRESENTATION_1_FILE_1_ID, binary,
      createIfNotExists, notify);

    // check if it is connected
    File retrievedFile = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_FILE_1_PATH, CorporaConstants.REPRESENTATION_1_FILE_1_ID);
    assertEquals(createdFile, retrievedFile);

    // check content
    Binary createdFileBinary = storage.getBinary(ModelUtils.getFileStoragePath(createdFile));
    assertTrue(IOUtils.contentEquals(binary.getContent().createInputStream(),
      createdFileBinary.getContent().createInputStream()));
  }

  @Test
  public void testDeleteFile() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    model.deleteFile(aipId, CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_1_FILE_1_PATH,
      CorporaConstants.REPRESENTATION_1_FILE_1_ID, true);

    // check if it deleted
    try {
      model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_1_FILE_1_PATH,
        CorporaConstants.REPRESENTATION_1_FILE_1_ID);
      Assert.fail("File deleted and yet exists.");
    } catch (NotFoundException e) {
      // do nothing
    }
  }

  @Test
  public void testRetrieveEventPreservationObject() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    Binary event_bin = model.retrievePreservationEvent(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_PREMIS_EVENT_ID);
    EventComplexType event = PremisV3Utils.binaryToEvent(event_bin.getContent(), true);

    assertEquals(CorporaConstants.AGENT_RODA_8,
      event.getLinkingAgentIdentifierArray(0).getLinkingAgentIdentifierValue());
    assertEquals(CorporaConstants.INGESTION, event.getEventType().getStringValue());
  }

  @Test
  public void testRepresentationFileObject() throws RODAException {
    // set up
    final String aipId = CorporaConstants.SOURCE_AIP_ID;
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    Binary file_bin = model.retrievePreservationFile(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_FILE_1_PATH, CorporaConstants.REPRESENTATION_1_FILE_1_ID);
    gov.loc.premis.v3.File file = PremisV3Utils.binaryToFile(file_bin.getContent(), true);

    ObjectCharacteristicsComplexType file_characteristics = file.getObjectCharacteristicsArray(0);
    assertEquals(2, file_characteristics.getFixityArray().length);
    assertEquals(CorporaConstants.METS_XML, file.getOriginalName().getStringValue());
  }

  @Test
  public void testRepresentationPreservationObject() throws RODAException {
    // set up

    final String aipId = CorporaConstants.SOURCE_AIP_ID;
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), aipCreator);

    Binary representation_bin = model.retrievePreservationRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);

    gov.loc.premis.v3.Representation representation = PremisV3Utils
      .binaryToRepresentation(representation_bin.getContent(), true);

    assertEquals(representation.getPreservationLevelArray(0).getPreservationLevelValue().getStringValue(),
      CorporaConstants.PRESERVATION_LEVEL_FULL);
  }

  @Test
  public void getAgentPreservationObject() throws RODAException {
    // pre-load the preservation container data
    DefaultStoragePath preservationContainerPath = DefaultStoragePath
      .parse(CorporaConstants.SOURCE_PRESERVATION_CONTAINER);

    storage.deleteContainer(preservationContainerPath);
    storage.copy(corporaService, preservationContainerPath,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_PRESERVATION_CONTAINER));

    Binary agent_bin = model.retrievePreservationAgent(CorporaConstants.AGENT_RODA_8);
    AgentComplexType agent = PremisV3Utils.binaryToAgent(agent_bin.getContent(), true);

    assertEquals(CorporaConstants.AGENT_RODA_8, agent.getAgentIdentifierArray(0).getAgentIdentifierValue());
    assertEquals(CorporaConstants.SOFTWARE_INGEST_TASK, agent.getAgentType().getStringValue());
    assertEquals(CorporaConstants.INGEST_CREATE_AIP, agent.getAgentNameArray(0).getStringValue());

  }

  @Test
  public void createLogEntry() throws RODAException {
    // setup
    createLogActionDirectory();

    LogEntry entry = new LogEntry();
    entry.setActionComponent("Action");
    entry.setActionMethod("Method");
    entry.setAddress("Address");
    entry.setId("ID");
    entry.setDatetime(new Date());
    entry.setState(LOG_ENTRY_STATE.SUCCESS);
    List<LogEntryParameter> parameters = new ArrayList<LogEntryParameter>();
    parameters.add(new LogEntryParameter("NAME1", "VALUE1"));
    parameters.add(new LogEntryParameter("NAME2", "VALUE2"));
    entry.setParameters(parameters);
    model.addLogEntry(entry, logPath);
  }

  private void createLogActionDirectory() {
    try {
      Files.createDirectories(logPath);
    } catch (IOException e) {
    }
  }

  @Test
  public void testMemberInheritance() throws RODAException {
    // create group 1
    Group group1 = new Group("group1");
    group1.setActive(true);
    group1.setFullName("NAMEGROUP1");
    group1.setDirectRoles(new HashSet<>(Arrays.asList(ROLE1)));
    model.createGroup(group1, true);

    // gen. asserts for group 1
    Group retrievedGroup1 = model.retrieveGroup(group1.getId());
    Assert.assertNotNull(retrievedGroup1);

    // create group 2
    Group group2 = new Group("group2");
    group2.setActive(true);
    group2.setFullName("NAMEGROUP2");
    group2.setDirectRoles(new HashSet<>(Arrays.asList(ROLE2)));
    model.createGroup(group2, true);

    // gen. asserts for group 2
    Group retrievedGroup2 = model.retrieveGroup(group2.getId());
    Assert.assertNotNull(retrievedGroup2);
    MatcherAssert.assertThat(retrievedGroup2.getAllRoles(), Matchers.containsInAnyOrder(ROLE2));

    // create user 1
    User user = new User("user1");
    user.setActive(true);
    user.setEmail("user1@example.com");
    user.setGuest(false);
    user.setFullName("user1");
    user.addGroup(group1.getId());
    user.addGroup(group2.getId());
    model.createUser(user, true);

    // gen. asserts for user 1
    User retrievedUser = model.retrieveUserByName(user.getId());
    Assert.assertNotNull(retrievedUser);
    MatcherAssert.assertThat(retrievedUser.getGroups(), Matchers.containsInAnyOrder(group1.getId(), group2.getId()));
    MatcherAssert.assertThat(retrievedUser.getAllRoles(), Matchers.containsInAnyOrder(ROLE1, ROLE2));

    // modify group 2 groups
    model.deleteGroup(group1.getId(), false);

    try {
      model.retrieveGroup(group1.getId());
      Assert.fail("should have not found exception");
    } catch (NotFoundException e) {
      // expected
    }

    User retrievedUser2 = model.retrieveUserByName(user.getId());
    Assert.assertNotNull(retrievedUser2);
    MatcherAssert.assertThat(retrievedUser2.getGroups(), Matchers.containsInAnyOrder(group2.getId()));
    MatcherAssert.assertThat(retrievedUser2.getGroups(), Matchers.not(Matchers.containsInAnyOrder(group1.getId())));
    MatcherAssert.assertThat(retrievedUser2.getAllRoles(), Matchers.containsInAnyOrder(ROLE2));
    MatcherAssert.assertThat(retrievedUser2.getAllRoles(), Matchers.not(Matchers.containsInAnyOrder(ROLE1)));

  }

}
