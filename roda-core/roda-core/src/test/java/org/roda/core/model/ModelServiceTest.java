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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.metadata.AgentPreservationObject;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.ip.metadata.EventPreservationObject;
import org.roda.core.data.v2.ip.metadata.RepresentationFilePreservationObject;
import org.roda.core.data.v2.ip.metadata.RepresentationPreservationObject;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryParameter;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.Binary;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageTestUtils;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    basePath = Files.createTempDirectory("modelTests");
    System.setProperty("roda.home", basePath.toString());
    RodaCoreFactory.instantiateTest(true, true);
    logPath = RodaCoreFactory.getLogPath();
    storage = RodaCoreFactory.getStorageService();
    model = RodaCoreFactory.getModelService();
  }

  @After
  public void cleanup() throws NotFoundException, GenericException {
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testCreateAIP() throws RODAException, ParseException, IOException {

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
    assertThat(aip.getRepresentationIds(),
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
    assertThat(representation1.getFilesDirectlyUnder(),
      containsInAnyOrder(CorporaConstants.REPRESENTATION_1_FILE_1_ID, CorporaConstants.REPRESENTATION_1_FILE_2_ID));

    final Representation representation2 = model.retrieveRepresentation(aipId, CorporaConstants.REPRESENTATION_2_ID);
    assertEquals(aipId, representation2.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_2_ID, representation2.getId());
    assertEquals(CorporaConstants.REPRESENTATION_2_ORIGINAL, representation2.isOriginal());
    assertThat(representation2.getFilesDirectlyUnder(),
      containsInAnyOrder(CorporaConstants.REPRESENTATION_2_FILE_1_ID, CorporaConstants.REPRESENTATION_2_FILE_2_ID));

    // testing files
    final File file_1_1 = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_FILE_1_ID);
    assertEquals(aipId, file_1_1.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_1_ID, file_1_1.getRepresentationId());
    assertEquals(CorporaConstants.REPRESENTATION_1_FILE_1_ID, file_1_1.getId());

    // TODO test directories

    final Binary binary_1_1 = storage.getBinary(ModelUtils.getRepresentationFilePath(file_1_1));
    assertTrue(binary_1_1.getSizeInBytes() > 0);
    assertEquals(binary_1_1.getSizeInBytes().intValue(),
      IOUtils.toByteArray(binary_1_1.getContent().createInputStream()).length);

    final File file_1_2 = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_FILE_2_ID);
    assertEquals(aipId, file_1_2.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_1_ID, file_1_2.getRepresentationId());
    assertEquals(CorporaConstants.REPRESENTATION_1_FILE_2_ID, file_1_2.getId());

    final Binary binary_1_2 = storage.getBinary(ModelUtils.getRepresentationFilePath(file_1_2));
    assertTrue(binary_1_2.getSizeInBytes() > 0);
    assertEquals(binary_1_2.getSizeInBytes().intValue(),
      IOUtils.toByteArray(binary_1_2.getContent().createInputStream()).length);

    final File file_2_1 = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_2_ID,
      CorporaConstants.REPRESENTATION_2_FILE_1_ID);
    assertEquals(aipId, file_2_1.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_2_ID, file_2_1.getRepresentationId());
    assertEquals(CorporaConstants.REPRESENTATION_2_FILE_1_ID, file_2_1.getId());

    final Binary binary_2_1 = storage.getBinary(ModelUtils.getRepresentationFilePath(file_2_1));
    assertTrue(binary_2_1.getSizeInBytes() > 0);
    assertEquals(binary_2_1.getSizeInBytes().intValue(),
      IOUtils.toByteArray(binary_2_1.getContent().createInputStream()).length);

    final File file_2_2 = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_2_ID,
      CorporaConstants.REPRESENTATION_2_FILE_2_ID);
    assertEquals(aipId, file_2_2.getAipId());
    assertEquals(CorporaConstants.REPRESENTATION_2_ID, file_2_2.getRepresentationId());
    assertEquals(CorporaConstants.REPRESENTATION_2_FILE_2_ID, file_2_2.getId());

    final Binary binary_2_2 = storage.getBinary(ModelUtils.getRepresentationFilePath(file_2_2));
    assertTrue(binary_2_2.getSizeInBytes() > 0);
    assertEquals(binary_2_2.getSizeInBytes().intValue(),
      IOUtils.toByteArray(binary_2_2.getContent().createInputStream()).length);

    // test preservation metadata
    RepresentationFilePreservationObject rfpo = model.retrieveRepresentationFileObject(aipId,
      CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.F0_PREMIS_XML);
    assertEquals(rfpo.getCompositionLevel(), 0);
    assertEquals(rfpo.getFormatDesignationName(), CorporaConstants.TEXT_XML);

    EventPreservationObject epo = model.retrieveEventPreservationObject(aipId, CorporaConstants.REPRESENTATION_1_ID,
      null,CorporaConstants.EVENT_RODA_398_PREMIS_XML);
    assertEquals(epo.getEventType(), CorporaConstants.INGESTION);
    assertEquals(epo.getOutcome(), CorporaConstants.SUCCESS);

    RepresentationPreservationObject rpo = model.retrieveRepresentationPreservationObject(aipId,CorporaConstants.REPRESENTATION_1_ID);
    assertEquals(rpo.getPreservationLevel(), CorporaConstants.PRESERVATION_LEVEL_FULL);
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
      model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_1_FILE_1_ID);
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
    assertThat(listAIPs, containsInAnyOrder(aip1, aip2, aip3));

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

    Iterable<DescriptiveMetadata> list = model.listDescriptiveMetadataBinaries(aipId);
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
      binary, CorporaConstants.OTHER_DESCRIPTIVE_METADATA_TYPE);

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
      CorporaConstants.DESCRIPTIVE_METADATA_ID, binary, CorporaConstants.OTHER_DESCRIPTIVE_METADATA_TYPE);

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

    final Iterable<Representation> list = model.listRepresentations(aipId);
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
    Representation createdRepresentation = model.createRepresentation(aipId, newRepresentationId, corporaService,
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
      corporaService, DefaultStoragePath.parse(CorporaConstants.OTHER_REPRESENTATION_STORAGEPATH));

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
      model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_1_FILE_1_ID);
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
    final StoragePath corporaFilePath = DefaultStoragePath.parse(CorporaConstants.OTHER_FILE_STORAGEPATH);
    final Binary binary = corporaService.getBinary(corporaFilePath);

    File createdFile = model.createFile(aipId, CorporaConstants.REPRESENTATION_1_ID, newFileId, binary);

    // check if it is connected
    File retrievedFile = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID, newFileId);
    assertEquals(createdFile, retrievedFile);

    // check content
    Binary createdFileBinary = storage.getBinary(ModelUtils.getRepresentationFilePath(createdFile));
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
      CorporaConstants.REPRESENTATION_1_FILE_1_ID, binary, createIfNotExists, notify);

    // check if it is connected
    File retrievedFile = model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID,
      CorporaConstants.REPRESENTATION_1_FILE_1_ID);
    assertEquals(createdFile, retrievedFile);

    // check content
    Binary createdFileBinary = storage.getBinary(ModelUtils.getRepresentationFilePath(createdFile));
    assertTrue(IOUtils.contentEquals(binary.getContent().createInputStream(),
      createdFileBinary.getContent().createInputStream()));
  }

  @Test
  public void testDeleteFile() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    model.deleteFile(aipId, CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_1_FILE_1_ID);

    // check if it deleted
    try {
      model.retrieveFile(aipId, CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.REPRESENTATION_1_FILE_1_ID);
      fail("File deleted and yet exists.");
    } catch (NotFoundException e) {
      // do nothing
    }
  }

  @Test
  public void testRetrieveEventPreservationObject() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    EventPreservationObject epo = model.retrieveEventPreservationObject(aipId, CorporaConstants.REPRESENTATION_1_ID,null,
      CorporaConstants.EVENT_RODA_398_PREMIS_XML);
    assertEquals(CorporaConstants.AGENT_RODA_8, epo.getAgentID());
    assertEquals(CorporaConstants.INGESTION, epo.getType());
  }

  @Test
  public void testRepresentationFileObject() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
    RepresentationFilePreservationObject rfpo = model.retrieveRepresentationFileObject(aipId,
      CorporaConstants.REPRESENTATION_1_ID, CorporaConstants.F0_PREMIS_XML);
    assertEquals(rfpo.getFixities().length, 2);
    assertEquals(rfpo.getOriginalName(), CorporaConstants.METS_XML);
  }

  @Test
  public void testRepresentationPreservationObject() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
    RepresentationPreservationObject rpo = model.getRepresentationPreservationObject(aipId, CorporaConstants.REPRESENTATION_1_ID);
    assertEquals(rpo.getPreservationLevel(), CorporaConstants.PRESERVATION_LEVEL_FULL);
  }

  @Test
  public void testGetAipPreservationObjects() throws RODAException {
    // set up
    final String aipId = UUID.randomUUID().toString();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    Iterable<RepresentationPreservationObject> representationPreservationObject = model
      .getAipPreservationObjects(aipId);
    List<String> fileIDs = new ArrayList<String>();
    Iterator<RepresentationPreservationObject> it = representationPreservationObject.iterator();
    while (it.hasNext()) {
      RepresentationPreservationObject rpo = it.next();
      fileIDs.add(rpo.getId());
    }
    assertThat(fileIDs,
      containsInAnyOrder(CorporaConstants.REPRESENTATION_PREMIS_XML, CorporaConstants.REPRESENTATION_PREMIS_XML));
  }

  @Test
  public void getAgentPreservationObject() throws RODAException {
    // pre-load the preservation container data
    DefaultStoragePath preservationContainerPath = DefaultStoragePath
      .parse(CorporaConstants.SOURCE_PRESERVATION_CONTAINER);
    // 2016-01-11 hsilva commented out (why to delete if it doesn't exist?)
    // storage.deleteContainer(preservationContainerPath);
    storage.copy(corporaService, preservationContainerPath,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_PRESERVATION_CONTAINER));
    AgentPreservationObject apo = model.getAgentPreservationObject(CorporaConstants.AGENT_RODA_8_PREMIS_XML);
    assertEquals(apo.getAgentType(), CorporaConstants.SOFTWARE_INGEST_TASK);
    assertEquals(apo.getAgentName(), CorporaConstants.INGEST_CREATE_AIP);
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
