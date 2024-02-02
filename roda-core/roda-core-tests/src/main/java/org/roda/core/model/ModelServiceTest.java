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
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.LiteOptionalWithCause;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryParameter;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageTestUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class ModelServiceTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(ModelServiceTest.class);
  private static final String ROLE1 = RodaConstants.REPOSITORY_PERMISSIONS_DESCRIPTIVE_METADATA_UPDATE;
  private static final String ROLE2 = RodaConstants.REPOSITORY_PERMISSIONS_LOG_ENTRY_READ;

  private static Path basePath;
  private static Path logPath;
  private static StorageService storage;
  private static ModelService model;
  private static StorageService corporaService;
  private static int fileCounter = 0;

  @BeforeClass
  public static void setUp() throws IOException, URISyntaxException, GenericException {
    URL corporaURL = ModelServiceTest.class.getResource("/corpora");
    Path corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    LOGGER.debug("Running model test under storage: {}", basePath);
  }

  // @BeforeMethod
  @BeforeClass
  public void init() throws IOException, GenericException {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);

    boolean deploySolr = false;
    boolean deployLdap = true;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = false;
    boolean deployPluginManager = false;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false);

    logPath = RodaCoreFactory.getLogPath();
    storage = RodaCoreFactory.getStorageService();
    model = RodaCoreFactory.getModelService();
  }

  // @AfterMethod
  @AfterClass
  public void cleanup() throws NotFoundException, GenericException, IOException {
    RodaCoreFactory.shutdown();
    // FSUtils.deletePath(basePath);
  }

  @Test
  public void testCreateAIP() throws RODAException, IOException {

    // generate AIP ID
    final String aipId = CorporaConstants.SOURCE_AIP_ID;

    // testing AIP
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    assertNotNull(aip);
    assertEquals(aipId, aip.getId());
    assertNull("AIP_1 should not have a parent", aip.getParentId());
    assertThat(aip.getState(), Is.is(AIPState.ACTIVE));

    List<String> descriptiveMetadataIds = aip.getDescriptiveMetadata().stream().map(DescriptiveMetadata::getId)
      .collect(Collectors.toList());
    assertThat(descriptiveMetadataIds, containsInAnyOrder(CorporaConstants.DESCRIPTIVE_METADATA_ID));

    List<String> representationIds = aip.getRepresentations().stream().map(Representation::getId)
      .collect(Collectors.toList());

    assertThat(representationIds,
      containsInAnyOrder(CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_2_ID));

    // testing descriptive metadata
    final DescriptiveMetadata descMetadata = model.retrieveDescriptiveMetadata(aipId,
      CorporaConstants.DESCRIPTIVE_METADATA_ID);

    assertEquals(aipId, descMetadata.getAipId());
    assertEquals(CorporaConstants.DESCRIPTIVE_METADATA_ID, descMetadata.getId());
    assertEquals(CorporaConstants.DESCRIPTIVE_METADATA_TYPE, descMetadata.getType());
    assertEquals(CorporaConstants.DESCRIPTIVE_METADATA_VERSION, descMetadata.getVersion());

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
    List<String> allRep1FileIds = Lists.newArrayList(allRep1Files).stream().filter(OptionalWithCause::isPresent)
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
    List<String> allRep2FileIds = Lists.newArrayList(allRep2Files).stream().filter(OptionalWithCause::isPresent)
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

    List<ObjectIdentifierComplexType> objectIdentifier = rpo.getObjectIdentifier();
    assertEquals(RodaConstants.PREMIS_IDENTIFIER_TYPE_URN,
      objectIdentifier.get(0).getObjectIdentifierType().getValue());
    assertEquals(CorporaConstants.REPRESENTATION_1_URN, rpo.getObjectIdentifier().get(0).getObjectIdentifierValue());
    assertEquals(CorporaConstants.PRESERVATION_LEVEL_FULL,
      rpo.getPreservationLevel().get(0).getPreservationLevelValue().getValue());

    Binary f0_premis_bin = model.retrievePreservationFile(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_FILE_1_PATH, CorporaConstants.REPRESENTATION_1_FILE_1_ID);
    gov.loc.premis.v3.File f0_premis_file = PremisV3Utils.binaryToFile(f0_premis_bin.getContent(), true);

    ObjectCharacteristicsComplexType f0_characteristics = f0_premis_file.getObjectCharacteristics().get(0);
    assertEquals(0, f0_characteristics.getCompositionLevel().getValue().intValue());
    assertEquals(0, f0_characteristics.getCompositionLevel().getValue().intValue());

    assertEquals(f0_characteristics.getFormat().get(0).getFormatDesignation().get(0).getFormatName().getValue(),
      CorporaConstants.TEXT_XML);

    Binary event_premis_bin = model.retrievePreservationEvent(aipId, CorporaConstants.REPRESENTATION_1_ID, null, null,
      CorporaConstants.REPRESENTATION_1_PREMIS_EVENT_ID);
    EventComplexType event_premis = PremisV3Utils.binaryToEvent(event_premis_bin.getContent(), true);
    assertEquals(CorporaConstants.INGESTION, event_premis.getEventType().getValue());
    assertEquals(CorporaConstants.SUCCESS,
      event_premis.getEventOutcomeInformation().get(0).getEventOutcome().get(0).getValue());

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testCreateAIPVersionEAD3() throws RODAException, IOException {

    // generate AIP ID
    final String aipId = IdUtils.createUUID();

    // testing AIP
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_VERSION_EAD_3),
      RodaConstants.ADMIN);

    assertNotNull(aip);
    assertEquals(aipId, aip.getId());
    assertNull("AIP_1 should not have a parent", aip.getParentId());
    assertThat(aip.getState(), Is.is(AIPState.ACTIVE));

    List<String> descriptiveMetadataIds = aip.getDescriptiveMetadata().stream().map(DescriptiveMetadata::getId)
      .collect(Collectors.toList());
    assertThat(descriptiveMetadataIds, containsInAnyOrder(CorporaConstants.DESCRIPTIVE_METADATA_ID_EAD3));

    List<String> representationIds = aip.getRepresentations().stream().map(Representation::getId)
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

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testCreateAIPVersionUnknown() throws RODAException, IOException {

    // generate AIP ID
    final String aipId = IdUtils.createUUID();

    // testing AIP
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_VERSION_EAD_UNKNOWN),
      RodaConstants.ADMIN);

    assertNotNull(aip);
    assertEquals(aipId, aip.getId());
    assertNull("AIP_1 should not have a parent", aip.getParentId());
    assertThat(aip.getState(), Is.is(AIPState.ACTIVE));

    List<String> descriptiveMetadataIds = aip.getDescriptiveMetadata().stream().map(DescriptiveMetadata::getId)
      .collect(Collectors.toList());
    assertThat(descriptiveMetadataIds, containsInAnyOrder(CorporaConstants.DESCRIPTIVE_METADATA_ID_EADUNKNOWN));

    List<String> representationIds = aip.getRepresentations().stream().map(Representation::getId)
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

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testCreateAIPWithSubFolders() throws RODAException, IOException {

    // generate AIP ID
    final String aipId = IdUtils.createUUID();

    // testing AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_REP_WITH_SUBFOLDERS),
      RodaConstants.ADMIN);

    CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aipId,
      CorporaConstants.REPRESENTATION_1_ID, true);

    List<File> reusableList = new ArrayList<>();
    Iterables.addAll(reusableList, Lists.newArrayList(allFiles).stream().filter(OptionalWithCause::isPresent)
      .map(OptionalWithCause::get).collect(Collectors.toList()));
    allFiles.close();

    assertTrue(reusableList.contains(
      new File("2012-roda-promo-en.pdf", aipId, CorporaConstants.REPRESENTATION_1_ID, new ArrayList<>(), false)));
    assertTrue(
      reusableList.contains(new File("folder", aipId, CorporaConstants.REPRESENTATION_1_ID, new ArrayList<>(), true)));
    assertTrue(reusableList
      .contains(new File("subfolder", aipId, CorporaConstants.REPRESENTATION_1_ID, List.of("folder"), true)));
    assertTrue(reusableList.contains(new File("RODA 2 logo.svg", aipId, CorporaConstants.REPRESENTATION_1_ID,
      Arrays.asList("folder", "subfolder"), false)));

    assertTrue(reusableList.contains(
      new File("RODA 2 logo-circle-black.svg", aipId, CorporaConstants.REPRESENTATION_1_ID, List.of("folder"), false)));
    assertTrue(reusableList.contains(
      new File("RODA 2 logo-circle-white.svg", aipId, CorporaConstants.REPRESENTATION_1_ID, List.of("folder"), false)));

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testDeleteAIP() throws RODAException {
    // generate AIP ID
    final String aipId = IdUtils.createUUID();

    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

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
    final String aip1Id = IdUtils.createUUID();
    final String aip2Id = IdUtils.createUUID();
    final String aip3Id = IdUtils.createUUID();

    final AIP aip1 = model.createAIP(aip1Id, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);
    final AIP aip2 = model.createAIP(aip2Id, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);
    final AIP aip3 = model.createAIP(aip3Id, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    Iterable<OptionalWithCause<AIP>> listAIPs = model.listAIPs();
    List<AIP> reusableList = new ArrayList<>();
    Iterables.addAll(reusableList, Lists.newArrayList(listAIPs).stream().filter(OptionalWithCause::isPresent)
      .map(OptionalWithCause::get).collect(Collectors.toList()));

    assertThat(reusableList, containsInAnyOrder(aip1, aip2, aip3));

    // cleanup
    model.deleteAIP(aip1Id);
    model.deleteAIP(aip2Id);
    model.deleteAIP(aip3Id);
  }

  @Test
  public void testCreateAIPWithExistingId() throws RODAException {
    // generate AIP ID
    final String aipId = IdUtils.createUUID();

    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    try {
      model.createAIP(aipId, corporaService,
        DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
        RodaConstants.ADMIN);
      Assert.fail("AIP shouldn't have been created and yet it was.");

    } catch (AlreadyExistsException e) {
      // do nothing as it was expected
    }

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testUpdateAIP() throws RODAException, IOException {
    // set up
    final String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    StoragePath otherAipPath = DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER,
      CorporaConstants.OTHER_AIP_ID);
    AIP updatedAIP = model.updateAIP(aipId, corporaService, otherAipPath, RodaConstants.ADMIN);

    // check it is connected
    AIP retrievedAIP = model.retrieveAIP(aipId);
    assertEquals(updatedAIP, retrievedAIP);

    // check content is correct
    StorageTestUtils.testEntityEqualRecursively(corporaService, otherAipPath, storage,
      ModelUtils.getAIPStoragePath(aipId));

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testListDescriptiveMetadata() throws RODAException {
    // set up
    final String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    Iterable<DescriptiveMetadata> list = model.retrieveAIP(aipId).getDescriptiveMetadata();
    DescriptiveMetadata descriptiveMetadata1 = model.retrieveDescriptiveMetadata(aipId,
      CorporaConstants.DESCRIPTIVE_METADATA_ID);

    assertThat(list, containsInAnyOrder(descriptiveMetadata1));

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testCreateDescriptiveMetadata() throws RODAException, IOException {
    // set up
    final String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    final String newDescriptiveMetadataId = IdUtils.createUUID();
    final Binary binary = corporaService
      .getBinary(DefaultStoragePath.parse(CorporaConstants.OTHER_DESCRIPTIVE_METADATA_STORAGEPATH));

    final DescriptiveMetadata newDescriptiveMetadata = model.createDescriptiveMetadata(aipId, newDescriptiveMetadataId,
      binary.getContent(), CorporaConstants.OTHER_DESCRIPTIVE_METADATA_TYPE,
      CorporaConstants.OTHER_DESCRIPTIVE_METADATA_VERSION, RodaConstants.ADMIN);

    // check if it is connected
    DescriptiveMetadata retrievedDescriptiveMetadata = model.retrieveDescriptiveMetadata(aipId,
      newDescriptiveMetadataId);
    assertEquals(newDescriptiveMetadata, retrievedDescriptiveMetadata);

    // check content
    Binary newDescriptiveMetadataBinary = storage
      .getBinary(ModelUtils.getDescriptiveMetadataStoragePath(newDescriptiveMetadata));
    assertTrue(IOUtils.contentEquals(binary.getContent().createInputStream(),
      newDescriptiveMetadataBinary.getContent().createInputStream()));

    // cleanup
    model.deleteAIP(aipId);

  }

  @Test
  public void testUpdateDescriptiveMetadata() throws RODAException, IOException {
    // set up
    final String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    final Binary binary = corporaService
      .getBinary(DefaultStoragePath.parse(CorporaConstants.OTHER_DESCRIPTIVE_METADATA_STORAGEPATH));

    Map<String, String> properties = new HashMap<>();
    properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());

    final DescriptiveMetadata updatedDescriptiveMetadata = model.updateDescriptiveMetadata(aipId,
      CorporaConstants.DESCRIPTIVE_METADATA_ID, binary.getContent(), CorporaConstants.OTHER_DESCRIPTIVE_METADATA_TYPE,
      CorporaConstants.OTHER_DESCRIPTIVE_METADATA_VERSION, properties, RodaConstants.ADMIN);

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
      CorporaConstants.OTHER_DESCRIPTIVE_METADATA_TYPE, CorporaConstants.OTHER_DESCRIPTIVE_METADATA_VERSION, properties,
      RodaConstants.ADMIN);
    model.updateDescriptiveMetadata(aipId, CorporaConstants.DESCRIPTIVE_METADATA_ID, binary.getContent(),
      CorporaConstants.OTHER_DESCRIPTIVE_METADATA_TYPE, CorporaConstants.OTHER_DESCRIPTIVE_METADATA_VERSION, properties,
      RodaConstants.ADMIN);
    model.updateDescriptiveMetadata(aipId, CorporaConstants.DESCRIPTIVE_METADATA_ID, binary.getContent(),
      CorporaConstants.OTHER_DESCRIPTIVE_METADATA_TYPE, CorporaConstants.OTHER_DESCRIPTIVE_METADATA_VERSION, properties,
      RodaConstants.ADMIN);

    assertEquals(4, Iterables.size(storage.listBinaryVersions(storagePath)));

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testDeleteDescriptiveMetadata() throws RODAException {
    // set up
    final String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    model.deleteDescriptiveMetadata(aipId, CorporaConstants.DESCRIPTIVE_METADATA_ID, RodaConstants.ADMIN);

    // check if it deleted
    try {
      model.retrieveDescriptiveMetadata(aipId, CorporaConstants.DESCRIPTIVE_METADATA_ID);
      Assert.fail("Descriptive metadata deleted and yet exists.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testListRepresentations() throws RODAException {
    // set up
    final String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    final Representation representation1 = model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);
    final Representation representation2 = model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_2_ID);

    final Iterable<Representation> list = model.retrieveAIP(aipId).getRepresentations();
    assertThat(list, containsInAnyOrder(representation1, representation2));

    CloseableIterable<OptionalWithCause<Representation>> list2 = model.list(Representation.class);
    assertThat(
      StreamSupport.stream(list2.spliterator(), false).map(OptionalWithCause::get).collect(Collectors.toList()),
      containsInAnyOrder(representation1, representation2));

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testCreateRepresentation() throws RODAException, IOException {
    // set up
    final String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    final String newRepresentationId = IdUtils.createUUID();
    final StoragePath corporaRepresentationPath = DefaultStoragePath
      .parse(CorporaConstants.OTHER_REPRESENTATION_STORAGEPATH);

    Representation createdRepresentation = model.createRepresentation(aipId, newRepresentationId,
      CorporaConstants.REPRESENTATION_1_ORIGINAL, CorporaConstants.REPRESENTATION_1_TYPE, corporaService,
      DefaultStoragePath.parse(CorporaConstants.OTHER_REPRESENTATION_STORAGEPATH), false, RodaConstants.ADMIN);

    // check if it is connected
    Representation retrievedRepresentation = model.retrieveRepresentation(aipId, newRepresentationId);
    assertEquals(createdRepresentation, retrievedRepresentation);

    // check content
    StorageTestUtils.testEntityEqualRecursively(corporaService, corporaRepresentationPath, storage,
      ModelUtils.getRepresentationStoragePath(aipId, newRepresentationId));

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testUpdateRepresentation() throws RODAException, IOException {
    // set up
    final String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    final StoragePath corporaRepresentationPath = DefaultStoragePath
      .parse(CorporaConstants.OTHER_REPRESENTATION_STORAGEPATH);
    Representation updatedRepresentation = model.updateRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_ORIGINAL, CorporaConstants.REPRESENTATION_1_TYPE, corporaService,
      DefaultStoragePath.parse(CorporaConstants.OTHER_REPRESENTATION_STORAGEPATH), RodaConstants.ADMIN);

    // check if it is connected
    Representation retrievedRepresentation = model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);
    assertEquals(updatedRepresentation, retrievedRepresentation);

    // check content
    StorageTestUtils.testEntityEqualRecursively(corporaService, corporaRepresentationPath, storage,
      ModelUtils.getRepresentationStoragePath(aipId, CorporaConstants.REPRESENTATION_1_ID));

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testDeleteRepresentation() throws RODAException {
    // set up
    final String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    model.deleteRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID, RodaConstants.ADMIN);

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

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testCreateFile() throws RODAException, IOException {
    // set up
    final String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    final String newFileId = IdUtils.createUUID();
    final List<String> newFileDirectoryPath = new ArrayList<>();
    final StoragePath corporaFilePath = DefaultStoragePath.parse(CorporaConstants.OTHER_FILE_STORAGEPATH);
    final Binary binary = corporaService.getBinary(corporaFilePath);

    File createdFile = model.createFile(aipId, CorporaConstants.REPRESENTATION_1_ID, newFileDirectoryPath, newFileId,
      binary.getContent(), RodaConstants.ADMIN, true);

    // check if it is connected
    File retrievedFile = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID, newFileDirectoryPath,
      newFileId);
    assertEquals(createdFile, retrievedFile);

    // check content
    Binary createdFileBinary = storage.getBinary(ModelUtils.getFileStoragePath(createdFile));
    assertTrue(IOUtils.contentEquals(binary.getContent().createInputStream(),
      createdFileBinary.getContent().createInputStream()));

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testUpdateFile() throws RODAException, IOException {
    // set up
    final String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    final StoragePath corporaFilePath = DefaultStoragePath.parse(CorporaConstants.OTHER_FILE_STORAGEPATH);
    final Binary binary = corporaService.getBinary(corporaFilePath);

    final boolean createIfNotExists = false;
    final boolean notify = false;
    File createdFile = model.updateFile(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_FILE_1_PATH, CorporaConstants.REPRESENTATION_1_FILE_1_ID, binary.getContent(),
      createIfNotExists, RodaConstants.ADMIN, notify);

    // check if it is connected
    File retrievedFile = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_FILE_1_PATH, CorporaConstants.REPRESENTATION_1_FILE_1_ID);
    assertEquals(createdFile, retrievedFile);

    // check content
    Binary createdFileBinary = storage.getBinary(ModelUtils.getFileStoragePath(createdFile));
    assertTrue(IOUtils.contentEquals(binary.getContent().createInputStream(),
      createdFileBinary.getContent().createInputStream()));

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testDeleteFile() throws RODAException {
    // set up
    final String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    model.deleteFile(aipId, CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_1_FILE_1_PATH,
      CorporaConstants.REPRESENTATION_1_FILE_1_ID, RodaConstants.ADMIN, true);

    // check if it deleted
    try {
      model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_1_FILE_1_PATH,
        CorporaConstants.REPRESENTATION_1_FILE_1_ID);
      Assert.fail("File deleted and yet exists.");
    } catch (NotFoundException e) {
      // do nothing
    }

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testRetrieveEventPreservationObject() throws RODAException {
    // set up
    final String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    Binary event_bin = model.retrievePreservationEvent(aipId, CorporaConstants.REPRESENTATION_1_ID, null, null,
      CorporaConstants.REPRESENTATION_1_PREMIS_EVENT_ID);
    EventComplexType event = PremisV3Utils.binaryToEvent(event_bin.getContent(), true);

    assertEquals(CorporaConstants.AGENT_RODA_8,
      event.getLinkingAgentIdentifier().get(0).getLinkingAgentIdentifierValue());
    assertEquals(CorporaConstants.INGESTION, event.getEventType().getValue());
  }

  @Test
  public void testRepresentationFileObject() throws RODAException {
    // set up
    final String aipId = CorporaConstants.SOURCE_AIP_ID;
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    Binary file_bin = model.retrievePreservationFile(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_FILE_1_PATH, CorporaConstants.REPRESENTATION_1_FILE_1_ID);
    gov.loc.premis.v3.File file = PremisV3Utils.binaryToFile(file_bin.getContent(), true);

    ObjectCharacteristicsComplexType file_characteristics = file.getObjectCharacteristics().get(0);
    assertEquals(2, file_characteristics.getFixity().size());
    assertEquals(CorporaConstants.METS_XML, file.getOriginalName().getValue());

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testRepresentationPreservationObject() throws RODAException {
    // set up

    final String aipId = CorporaConstants.SOURCE_AIP_ID;
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    Binary representation_bin = model.retrievePreservationRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);

    gov.loc.premis.v3.Representation representation = PremisV3Utils
      .binaryToRepresentation(representation_bin.getContent(), true);

    assertEquals(representation.getPreservationLevel().get(0).getPreservationLevelValue().getValue(),
      CorporaConstants.PRESERVATION_LEVEL_FULL);

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void getAgentPreservationObject() throws RODAException {
    // pre-load the preservation container data
    DefaultStoragePath preservationContainerPath = DefaultStoragePath
      .parse(CorporaConstants.SOURCE_PRESERVATION_CONTAINER);

    storage.deleteContainer(preservationContainerPath);
    storage.copy(corporaService, preservationContainerPath,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_PRESERVATION_CONTAINER));

    Binary agentBinary = model.retrievePreservationAgent(CorporaConstants.AGENT_RODA_8);
    AgentComplexType agent = PremisV3Utils.binaryToAgent(agentBinary.getContent(), true);

    assertEquals(CorporaConstants.AGENT_RODA_8, agent.getAgentIdentifier().getFirst().getAgentIdentifierValue());
    assertEquals(CorporaConstants.SOFTWARE_INGEST_TASK, agent.getAgentType().getValue());
    assertEquals(CorporaConstants.INGEST_CREATE_AIP, agent.getAgentName().getFirst().getValue());
  }

  /*
   * @Test public void testImportLog() throws RODAException, IOException { Path
   * dummyLogFile = corporaPath.resolve("logs").resolve("dummy.log"); int response
   * = RESTClientUtility.sendPostRequestWithFile("http://localhost:8080",
   * "/api/v1/log_entries", "admin", "roda", dummyLogFile);
   *
   * assertEquals(response, RodaConstants.HTTP_RESPONSE_CODE_SUCCESS); }
   */

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
    entry.setState(LogEntryState.SUCCESS);
    List<LogEntryParameter> parameters = new ArrayList<>();
    parameters.add(new LogEntryParameter("NAME1", "VALUE1"));
    parameters.add(new LogEntryParameter("NAME2", "VALUE2"));
    entry.setParameters(parameters);
    model.addLogEntry(entry, logPath);
  }

  private void createLogActionDirectory() {
    try {
      Files.createDirectories(logPath);
    } catch (IOException e) {
      // do nothing
    }
  }

  @Test
  public void testMemberInheritance() throws RODAException {
    // create group 1
    Group group1 = new Group("group-1");
    group1.setActive(true);
    group1.setFullName("NAMEGROUP1");
    group1.setDirectRoles(new HashSet<>(List.of(ROLE1)));
    model.createGroup(group1, true);

    // gen. asserts for group 1
    Group retrievedGroup1 = model.retrieveGroup(group1.getId());
    Assert.assertNotNull(retrievedGroup1);

    // create group 2
    Group group2 = new Group("group2");
    group2.setActive(true);
    group2.setFullName("NAMEGROUP2");
    group2.setDirectRoles(new HashSet<>(List.of(ROLE2)));
    model.createGroup(group2, true);

    // gen. asserts for group 2
    Group retrievedGroup2 = model.retrieveGroup(group2.getId());
    Assert.assertNotNull(retrievedGroup2);
    MatcherAssert.assertThat(retrievedGroup2.getAllRoles(), Matchers.containsInAnyOrder(ROLE2));

    // create user 1
    User user = new User("user_1");
    user.setActive(true);
    user.setEmail("user1@example.com");
    user.setGuest(false);
    user.setFullName("user1");
    user.addGroup(group1.getId());
    user.addGroup(group2.getId());
    model.createUser(user, true);

    // gen. asserts for user 1
    User retrievedUser = model.retrieveUser(user.getId());
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

    User retrievedUser2 = model.retrieveUser(user.getId());
    Assert.assertNotNull(retrievedUser2);
    MatcherAssert.assertThat(retrievedUser2.getGroups(), Matchers.containsInAnyOrder(group2.getId()));
    MatcherAssert.assertThat(retrievedUser2.getGroups(), Matchers.not(Matchers.containsInAnyOrder(group1.getId())));
    MatcherAssert.assertThat(retrievedUser2.getAllRoles(), Matchers.containsInAnyOrder(ROLE2));
    MatcherAssert.assertThat(retrievedUser2.getAllRoles(), Matchers.not(Matchers.containsInAnyOrder(ROLE1)));

    // cleanup
    model.deleteUser(user.getId(), true);
  }

  @Test
  public void testUserUpdate() throws RODAException {

    // create user 1
    User user = new User("user_1");
    user.setActive(true);
    user.setEmail("user1@example.com");
    user.setGuest(false);
    user.setFullName("user1");
    model.createUser(user, true);

    // create group 1
    Group group1 = new Group("group_1");
    group1.setActive(true);
    group1.setFullName("NAMEGROUP1");
    group1.setDirectRoles(new HashSet<>(List.of(ROLE1)));
    model.createGroup(group1, true);

    // update user
    User user2 = model.retrieveUser(user.getId());
    user2.addGroup(group1.getId());
    User user3 = model.updateUser(user2, null, true);

    MatcherAssert.assertThat(user3.getGroups(), Matchers.containsInAnyOrder(group1.getId()));

    // cleanup
    model.deleteUser(user.getId(), true);
    model.deleteGroup(group1.getId(), true);

  }

  @Test
  public void testListing() throws RODAException, IOException {
    populate(RodaCoreFactory.getTransferredResourcesScanner().getBasePath());
    CloseableIterable<OptionalWithCause<LiteRODAObject>> list = model.listLite(TransferredResource.class);
    int size = CloseableIterables.size(list);
    assertEquals(fileCounter, size);
  }

  @Test
  public void testLiteOptionalWithCauseSerialization() throws RODAException, IOException, ClassNotFoundException {
    final String aipId = CorporaConstants.SOURCE_AIP_ID;
    AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);
    Optional<LiteRODAObject> lightAIP = model.retrieveLiteFromObject(aip);

    // cleanup
    model.deleteAIP(aipId);

    if (lightAIP.isPresent()) {
      LiteOptionalWithCause test = LiteOptionalWithCause.of(lightAIP.get());
      Path tempFile = Files.createTempFile(basePath, "test", ".tmp");

      try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(tempFile))) {
        oos.writeObject(test);
      }

      try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(tempFile))) {
        LiteOptionalWithCause test2 = (LiteOptionalWithCause) ois.readObject();
        assertEquals(test, test2);
      }
    } else {
      fail("Should be present");
    }
  }

  private static void populate(Path basePath) throws IOException {
    Random randomno = new Random();
    int numberOfItemsByLevel = nextIntInRange(2, 3, randomno);
    int numberOfLevels = nextIntInRange(2, 3, randomno);
    populate(basePath, numberOfItemsByLevel, numberOfLevels, 0, randomno);
  }

  private static void populate(Path path, int numberOfItemsByLevel, int numberOfLevels, int currentLevel,
    Random randomno) throws IOException {
    currentLevel++;
    for (int i = 0; i < numberOfItemsByLevel; i++) {
      Path p;
      if (i % 2 == 0) {
        if (currentLevel > 1) {
          p = Files.createFile(path.resolve(IdUtils.createUUID() + ".txt"));
          Files.write(p, "NUNCAMAISACABA".getBytes());
          fileCounter++;
        }
      } else {
        p = Files.createDirectory(path.resolve(IdUtils.createUUID()));
        fileCounter++;
        if (currentLevel <= numberOfLevels) {
          populate(p, numberOfItemsByLevel, numberOfLevels, currentLevel, randomno);
        } else {
          if (currentLevel > 1) {
            for (int j = 0; j < numberOfItemsByLevel; j++) {
              Path temp = Files.createFile(p.resolve(IdUtils.createUUID() + ".txt"));
              Files.write(temp, "NUNCAMAISACABA".getBytes());
              fileCounter++;
            }
          }
        }
      }
    }
  }

  static int nextIntInRange(int min, int max, Random rng) {
    if (min > max) {
      throw new IllegalArgumentException("Cannot draw random int from invalid range [" + min + ", " + max + "].");
    }
    int diff = max - min;
    if (diff >= 0 && diff != Integer.MAX_VALUE) {
      return (min + rng.nextInt(diff + 1));
    }
    int i;
    do {
      i = rng.nextInt();
    } while (i < min || i > max);
    return i;
  }
}
