/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryParameter;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.ClosableIterable;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageTestUtils;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

import jersey.repackaged.com.google.common.collect.Lists;
import lc.xmlns.premisV2.EventComplexType;
import lc.xmlns.premisV2.ObjectCharacteristicsComplexType;
import lc.xmlns.premisV2.ObjectIdentifierComplexType;

/**
 * Unit tests for ModelService
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 * @author Luis Faria <lfaria@keep.pt>
 * 
 * @see ModelService
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({})
public class ModelServiceTest {

  private static Path basePath;
  private static Path logPath;
  private static StorageService storage;
  private static ModelService model;

  private static Path corporaPath;
  private static StorageService corporaService;

  private static final Logger logger = LoggerFactory.getLogger(ModelServiceTest.class);

  @BeforeClass
  public static void setUp() throws IOException, URISyntaxException, GenericException {
    URL corporaURL = ModelServiceTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    logger.debug("Running model test under storage: " + basePath);
  }

  @Before
  public void init() throws IOException, GenericException {
    logger.info("init");
    basePath = Files.createTempDirectory("modelTests");
    System.setProperty("roda.home", basePath.toString());
    RodaCoreFactory.instantiateTest(true, true);
    logPath = RodaCoreFactory.getLogPath();
    storage = RodaCoreFactory.getStorageService();
    model = RodaCoreFactory.getModelService();
  }

  @After
  public void cleanup() throws NotFoundException, GenericException {
    logger.info("cleanup");
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testCreateAIP() throws RODAException, ParseException, IOException, XmlException {

    // generate AIP ID
    final String aipId = UUID.randomUUID().toString();

    // testing AIP
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    assertNotNull(aip);
    assertEquals(aipId, aip.getId());
    assertNull("AIP_1 should not have a parent", aip.getParentId());
    assertTrue(aip.isActive());

    List<String> descriptiveMetadataIds = aip.getMetadata().getDescriptiveMetadata().stream().map(dm -> dm.getId())
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

    StoragePath descriptiveMetadataPath = ModelUtils.getDescriptiveMetadataPath(descMetadata.getAipId(),
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

    ClosableIterable<File> allRep1Files = model.listAllFiles(aipId, CorporaConstants.REPRESENTATION_1_ID);
    List<String> allRep1FileIds = Lists.newArrayList(allRep1Files).stream().map(f -> f.getId())
      .collect(Collectors.toList());
    allRep1Files.close();

    assertThat(allRep1FileIds,
      containsInAnyOrder(CorporaConstants.REPRESENTATION_1_FILE_1_ID, CorporaConstants.REPRESENTATION_1_FILE_2_ID));

    final Representation representation2 = model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_2_ID);
    assertEquals(aipId, representation2.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_2_ID, representation2.getId());
    assertEquals(CorporaConstants.REPRESENTATION_2_ORIGINAL, representation2.isOriginal());

    ClosableIterable<File> allRep2Files = model.listAllFiles(aipId, CorporaConstants.REPRESENTATION_2_ID);
    List<String> allRep2FileIds = Lists.newArrayList(allRep2Files).stream().map(f -> f.getId())
      .collect(Collectors.toList());
    allRep2Files.close();

    assertThat(allRep2FileIds,
      containsInAnyOrder(CorporaConstants.REPRESENTATION_2_FILE_1_ID, CorporaConstants.REPRESENTATION_2_FILE_2_ID));

    // testing files
    final File file_1_1 = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_FILE_1_PATH, CorporaConstants.REPRESENTATION_1_FILE_1_ID);
    assertEquals(aipId, file_1_1.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_1_ID, file_1_1.getRepresentationId());
    assertEquals(CorporaConstants.REPRESENTATION_1_FILE_1_ID, file_1_1.getId());

    final Binary binary_1_1 = storage.getBinary(ModelUtils.getRepresentationFileStoragePath(file_1_1));
    assertTrue(binary_1_1.getSizeInBytes() > 0);
    assertEquals(binary_1_1.getSizeInBytes().intValue(),
      IOUtils.toByteArray(binary_1_1.getContent().createInputStream()).length);

    final File file_1_2 = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_FILE_2_PATH, CorporaConstants.REPRESENTATION_1_FILE_2_ID);
    assertEquals(aipId, file_1_2.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_1_ID, file_1_2.getRepresentationId());
    assertEquals(CorporaConstants.REPRESENTATION_1_FILE_2_ID, file_1_2.getId());

    final Binary binary_1_2 = storage.getBinary(ModelUtils.getRepresentationFileStoragePath(file_1_2));
    assertTrue(binary_1_2.getSizeInBytes() > 0);
    assertEquals(binary_1_2.getSizeInBytes().intValue(),
      IOUtils.toByteArray(binary_1_2.getContent().createInputStream()).length);

    final File file_2_1 = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_2_ID,
      CorporaConstants.REPRESENTATION_2_FILE_1_PATH, CorporaConstants.REPRESENTATION_2_FILE_1_ID);
    assertEquals(aipId, file_2_1.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_2_ID, file_2_1.getRepresentationId());
    assertEquals(CorporaConstants.REPRESENTATION_2_FILE_1_ID, file_2_1.getId());

    final Binary binary_2_1 = storage.getBinary(ModelUtils.getRepresentationFileStoragePath(file_2_1));
    assertTrue(binary_2_1.getSizeInBytes() > 0);
    assertEquals(binary_2_1.getSizeInBytes().intValue(),
      IOUtils.toByteArray(binary_2_1.getContent().createInputStream()).length);

    final File file_2_2 = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_2_ID,
      CorporaConstants.REPRESENTATION_2_FILE_2_PATH, CorporaConstants.REPRESENTATION_2_FILE_2_ID);
    assertEquals(aipId, file_2_2.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_2_ID, file_2_2.getRepresentationId());
    assertEquals(CorporaConstants.REPRESENTATION_2_FILE_2_ID, file_2_2.getId());

    final Binary binary_2_2 = storage.getBinary(ModelUtils.getRepresentationFileStoragePath(file_2_2));
    assertTrue(binary_2_2.getSizeInBytes() > 0);
    assertEquals(binary_2_2.getSizeInBytes().intValue(),
      IOUtils.toByteArray(binary_2_2.getContent().createInputStream()).length);

    // test preservation metadata

    Binary preservationObject = model.retrieveRepresentationPreservationObject(aipId,
      CorporaConstants.REPRESENTATION_1_ID);
    lc.xmlns.premisV2.Representation rpo = PremisUtils.binaryToRepresentation(preservationObject.getContent(), true);

    List<ObjectIdentifierComplexType> objectIdentifierList = rpo.getObjectIdentifierList();
    assertEquals(RodaConstants.PREMIS_LOCAL_IDENTIFIER_TYPE, objectIdentifierList.get(0).getObjectIdentifierType());
    assertEquals(CorporaConstants.REPRESENTATION_1_ID, rpo.getObjectIdentifierList().get(0).getObjectIdentifierValue());
    assertEquals(CorporaConstants.PRESERVATION_LEVEL_FULL,
      rpo.getPreservationLevelList().get(0).getPreservationLevelValue());

    Binary f0_premis_bin = model.retrieveRepresentationFileObject(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.F0_PREMIS_XML);
    lc.xmlns.premisV2.File f0_premis_file = PremisUtils.binaryToFile(f0_premis_bin.getContent().createInputStream());

    ObjectCharacteristicsComplexType f0_characteristics = f0_premis_file.getObjectCharacteristicsList().get(0);
    assertEquals(0, f0_characteristics.getCompositionLevel().intValue());
    assertEquals(0, f0_characteristics.getCompositionLevel().intValue());

    assertEquals(f0_characteristics.getFormatList().get(0).getFormatDesignation().getFormatName(),
      CorporaConstants.TEXT_XML);

    Binary event_premis_bin = model.retrieveEventPreservationObject(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_PREMIS_EVENT_ID);
    EventComplexType event_premis = PremisUtils.binaryToEvent(event_premis_bin.getContent().createInputStream());
    assertEquals(CorporaConstants.INGESTION, event_premis.getEventType());
    assertEquals(CorporaConstants.SUCCESS, event_premis.getEventOutcomeInformationList().get(0).getEventOutcome());
  }

  @Test
  public void testCreateAIPWithSubFolders() throws RODAException, ParseException, IOException {

    // generate AIP ID
    final String aipId = UUID.randomUUID().toString();

    // testing AIP
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_REP_WITH_SUBFOLDERS));

    ClosableIterable<File> allFiles = model.listAllFiles(aipId, CorporaConstants.REPRESENTATION_1_ID);

    List<File> reusableList = new ArrayList<>();
    Iterables.addAll(reusableList, allFiles);
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
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    model.deleteAIP(aipId);

    // check if AIP was indeed deleted
    try {
      model.retrieveAIP(aipId);
      fail("AIP should have been deleted, but yet was retrieved.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }

    // check if AIP sub-resources were recursively deleted
    try {
      model.retrieveDescriptiveMetadata(aipId, CorporaConstants.DESCRIPTIVE_METADATA_ID);
      fail("Descriptive metadata binary should have been deleted, but yet was retrieved.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }

    try {
      model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);
      fail("Descriptive metadata binary should have been deleted, but yet was retrieved.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }

    try {
      model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_2_ID);
      fail("Representation should have been deleted, but yet was retrieved.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }

    try {
      model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_1_FILE_1_PATH,
        CorporaConstants.REPRESENTATION_1_FILE_1_ID);
      fail("File should have been deleted, but yet was retrieved.");
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
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
    final AIP aip2 = model.createAIP(aip2Id, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
    final AIP aip3 = model.createAIP(aip3Id, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    Iterable<AIP> listAIPs = model.listAIPs();
    List<AIP> reusableList = new ArrayList<>();
    Iterables.addAll(reusableList, listAIPs);

    assertThat(reusableList, containsInAnyOrder(aip1, aip2, aip3));

  }

  @Test
  public void testCreateAIPWithExistingId() throws RODAException {
    // generate AIP ID
    final String aipId = UUID.randomUUID().toString();

    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    try {
      model.createAIP(aipId, corporaService,
        DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
      fail("AIP shouldn't have been created and yet it was.");
    } catch (AlreadyExistsException e) {
      // do nothing as it was expected
    }
  }

  @Test
  public void testUpdateAIP() throws RODAException, IOException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    StoragePath otherAipPath = DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER,
      CorporaConstants.OTHER_AIP_ID);
    AIP updatedAIP = model.updateAIP(aipId, corporaService, otherAipPath);

    // check it is connected
    AIP retrievedAIP = model.retrieveAIP(aipId);
    assertEquals(updatedAIP, retrievedAIP);

    // check content is correct
    StorageTestUtils.testEntityEqualRecursively(corporaService, otherAipPath, storage, ModelUtils.getAIPpath(aipId));
  }

  @Test
  public void testListDescriptiveMetadata() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    Iterable<DescriptiveMetadata> list = model.retrieveAIP(aipId).getMetadata().getDescriptiveMetadata();
    DescriptiveMetadata descriptiveMetadata1 = model.retrieveDescriptiveMetadata(aipId,
      CorporaConstants.DESCRIPTIVE_METADATA_ID);

    assertThat(list, containsInAnyOrder(descriptiveMetadata1));
  }

  @Test
  public void testCreateDescriptiveMetadata() throws RODAException, IOException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    final String newDescriptiveMetadataId = UUID.randomUUID().toString();
    final Binary binary = corporaService
      .getBinary(DefaultStoragePath.parse(CorporaConstants.OTHER_DESCRIPTIVE_METADATA_STORAGEPATH));

    final DescriptiveMetadata newDescriptiveMetadata = model.createDescriptiveMetadata(aipId, newDescriptiveMetadataId,
      binary.getContent(), CorporaConstants.OTHER_DESCRIPTIVE_METADATA_TYPE);

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
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    final Binary binary = corporaService
      .getBinary(DefaultStoragePath.parse(CorporaConstants.OTHER_DESCRIPTIVE_METADATA_STORAGEPATH));

    final DescriptiveMetadata updatedDescriptiveMetadata = model.updateDescriptiveMetadata(aipId,
      CorporaConstants.DESCRIPTIVE_METADATA_ID, binary.getContent(), CorporaConstants.OTHER_DESCRIPTIVE_METADATA_TYPE);

    // check if it is connected
    DescriptiveMetadata retrievedDescriptiveMetadata = model.retrieveDescriptiveMetadata(aipId,
      CorporaConstants.DESCRIPTIVE_METADATA_ID);
    assertEquals(updatedDescriptiveMetadata, retrievedDescriptiveMetadata);

    // check content
    Binary updatedDescriptiveMetadataBinary = storage
      .getBinary(ModelUtils.getDescriptiveMetadataStoragePath(updatedDescriptiveMetadata));
    assertTrue(IOUtils.contentEquals(binary.getContent().createInputStream(),
      updatedDescriptiveMetadataBinary.getContent().createInputStream()));

  }

  @Test
  public void testDeleteDescriptiveMetadata() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    model.deleteDescriptiveMetadata(aipId, CorporaConstants.DESCRIPTIVE_METADATA_ID);

    // check if it deleted
    try {
      model.retrieveDescriptiveMetadata(aipId, CorporaConstants.DESCRIPTIVE_METADATA_ID);
      fail("Descriptive metadata deleted and yet exists.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }
  }

  @Test
  public void testListRepresentations() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    final Representation representation1 = model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);
    final Representation representation2 = model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_2_ID);

    final Iterable<Representation> list = model.retrieveAIP(aipId).getRepresentations();
    assertThat(list, containsInAnyOrder(representation1, representation2));
  }

  @Test
  public void testCreateRepresentation() throws RODAException, IOException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    final String newRepresentationId = UUID.randomUUID().toString();
    final StoragePath corporaRepresentationPath = DefaultStoragePath
      .parse(CorporaConstants.OTHER_REPRESENTATION_STORAGEPATH);

    Representation createdRepresentation = model.createRepresentation(aipId, newRepresentationId,
      CorporaConstants.REPRESENTATION_1_ORIGINAL, corporaService,
      DefaultStoragePath.parse(CorporaConstants.OTHER_REPRESENTATION_STORAGEPATH));

    // check if it is connected
    Representation retrievedRepresentation = model.retrieveRepresentation(aipId, newRepresentationId);
    assertEquals(createdRepresentation, retrievedRepresentation);

    // check content
    StorageTestUtils.testEntityEqualRecursively(corporaService, corporaRepresentationPath, storage,
      ModelUtils.getRepresentationPath(aipId, newRepresentationId));
  }

  @Test
  public void testUpdateRepresentation() throws RODAException, IOException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    final StoragePath corporaRepresentationPath = DefaultStoragePath
      .parse(CorporaConstants.OTHER_REPRESENTATION_STORAGEPATH);
    Representation updatedRepresentation = model.updateRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_ORIGINAL, corporaService,
      DefaultStoragePath.parse(CorporaConstants.OTHER_REPRESENTATION_STORAGEPATH));

    // check if it is connected
    Representation retrievedRepresentation = model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);
    assertEquals(updatedRepresentation, retrievedRepresentation);

    // check content
    StorageTestUtils.testEntityEqualRecursively(corporaService, corporaRepresentationPath, storage,
      ModelUtils.getRepresentationPath(aipId, CorporaConstants.REPRESENTATION_1_ID));
  }

  @Test
  public void testDeleteRepresentation() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    model.deleteRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);

    // check if it deleted
    try {
      model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_1_ID);
      fail("Representation deleted and yet exists.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }

    // check if file under representation was deleted
    try {
      model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_1_FILE_1_PATH,
        CorporaConstants.REPRESENTATION_1_FILE_1_ID);
      fail("Representation deleted and yet one of its files still exist.");
    } catch (NotFoundException e) {
      // do nothing as it was expected
    }
  }

  @Test
  public void testCreateFile() throws RODAException, IOException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

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
    Binary createdFileBinary = storage.getBinary(ModelUtils.getRepresentationFileStoragePath(createdFile));
    assertTrue(IOUtils.contentEquals(binary.getContent().createInputStream(),
      createdFileBinary.getContent().createInputStream()));
  }

  @Test
  public void testUpdateFile() throws RODAException, IOException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

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
    Binary createdFileBinary = storage.getBinary(ModelUtils.getRepresentationFileStoragePath(createdFile));
    assertTrue(IOUtils.contentEquals(binary.getContent().createInputStream(),
      createdFileBinary.getContent().createInputStream()));
  }

  @Test
  public void testDeleteFile() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    model.deleteFile(aipId, CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_1_FILE_1_PATH,
      CorporaConstants.REPRESENTATION_1_FILE_1_ID, true);

    // check if it deleted
    try {
      model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_1_FILE_1_PATH,
        CorporaConstants.REPRESENTATION_1_FILE_1_ID);
      fail("File deleted and yet exists.");
    } catch (NotFoundException e) {
      // do nothing
    }
  }

  /*
   * @Test public void testRetrieveEventPreservationObject() throws
   * RODAException { // set up final String aipId =
   * UUID.randomUUID().toString(); model.createAIP(aipId, corporaService,
   * DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER,
   * CorporaConstants.SOURCE_AIP_ID));
   * 
   * EventPreservationObject epo = model.retrieveEventPreservationObject(aipId,
   * CorporaConstants.REPRESENTATION_1_ID, null,
   * CorporaConstants.REPRESENTATION_1_PREMIS_EVENT_ID);
   * assertEquals(CorporaConstants.AGENT_RODA_8, epo.getAgentID());
   * assertEquals(CorporaConstants.INGESTION, epo.getEventType()); }
   * 
   * @Test public void testRepresentationFileObject() throws RODAException { //
   * set up final String aipId = UUID.randomUUID().toString();
   * model.createAIP(aipId, corporaService,
   * DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER,
   * CorporaConstants.SOURCE_AIP_ID)); RepresentationFilePreservationObject rfpo
   * = model.retrieveRepresentationFileObject(aipId,
   * CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.F0_PREMIS_XML);
   * assertEquals(rfpo.getFixities().length, 2);
   * assertEquals(rfpo.getOriginalName(), CorporaConstants.METS_XML); }
   * 
   * @Test public void testRepresentationPreservationObject() throws
   * RODAException { // set up final String aipId =
   * UUID.randomUUID().toString(); model.createAIP(aipId, corporaService,
   * DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER,
   * CorporaConstants.SOURCE_AIP_ID)); RepresentationPreservationObject rpo =
   * model.getRepresentationPreservationObject(aipId,
   * CorporaConstants.REPRESENTATION_1_ID);
   * assertEquals(rpo.getPreservationLevel(),
   * CorporaConstants.PRESERVATION_LEVEL_FULL); }
   * 
   * @Test public void testGetAipPreservationObjects() throws RODAException { //
   * set up final String aipId = UUID.randomUUID().toString();
   * model.createAIP(aipId, corporaService,
   * DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER,
   * CorporaConstants.SOURCE_AIP_ID));
   * 
   * Iterable<RepresentationPreservationObject> representationPreservationObject
   * = model .getAipPreservationObjects(aipId); List<String> fileIDs = new
   * ArrayList<String>(); Iterator<RepresentationPreservationObject> it =
   * representationPreservationObject.iterator(); while (it.hasNext()) {
   * RepresentationPreservationObject rpo = it.next(); fileIDs.add(rpo.getId());
   * }
   * 
   * assertThat(fileIDs,
   * containsInAnyOrder(CorporaConstants.REPRESENTATION_1_ID,
   * CorporaConstants.REPRESENTATION_2_ID)); }
   * 
   * @Test public void getAgentPreservationObject() throws RODAException { //
   * pre-load the preservation container data DefaultStoragePath
   * preservationContainerPath = DefaultStoragePath
   * .parse(CorporaConstants.SOURCE_PRESERVATION_CONTAINER); // 2016-01-11
   * hsilva commented out (why to delete if it doesn't exist?) //
   * storage.deleteContainer(preservationContainerPath);
   * storage.copy(corporaService, preservationContainerPath,
   * DefaultStoragePath.parse(CorporaConstants.SOURCE_PRESERVATION_CONTAINER));
   * AgentPreservationObject apo =
   * model.getAgentPreservationObject(CorporaConstants.AGENT_RODA_8);
   * assertEquals(apo.getAgentType(), CorporaConstants.SOFTWARE_INGEST_TASK);
   * assertEquals(apo.getAgentName(), CorporaConstants.INGEST_CREATE_AIP); }
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
}
