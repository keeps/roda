package org.roda.core.plugins;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPLink;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.FileLink;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.RepresentationLink;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DeleteRodaObjectPluginTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteRodaObjectPluginTest.class);
  private static Path basePath;
  private static ModelService model;
  private static IndexService index;

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false);
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    LOGGER.info("Running '{}' tests under storage {}", getClass().getName(), basePath);
  }

  @AfterClass
  public void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  private DIP createAIPDIP(List<AIP> aipList) throws AuthorizationDeniedException, GenericException {
    List<AIPLink> links = new ArrayList<>();
    for (AIP aip : aipList) {
      AIPLink aipLink = new AIPLink(aip.getId());
      links.add(aipLink);
    }

    DIP dip = new DIP();
    dip.setId(IdUtils.createUUID());
    dip.setAipIds(links);
    dip.setTitle("EARK-DIP");
    dip.setDescription("EARK-DIP generated and filtered based on an AIP");
    dip.setType(RodaConstants.DIP_TYPE_CONVERSION);
    dip = model.createDIP(dip, true);

    index.commit(IndexedDIP.class);
    return dip;
  }

  private DIP createRepresentationDIP(List<Representation> repList)
    throws AuthorizationDeniedException, GenericException {
    List<RepresentationLink> links = new ArrayList<>();
    for (Representation representation : repList) {
      RepresentationLink representationLink = new RepresentationLink(representation.getAipId(), representation.getId());
      links.add(representationLink);
    }

    DIP dip = new DIP();
    dip.setId(IdUtils.createUUID());
    dip.setRepresentationIds(links);
    dip.setTitle("EARK-DIP");
    dip.setDescription("EARK-DIP generated and filtered based on an Representations");
    dip.setType(RodaConstants.DIP_TYPE_CONVERSION);
    dip = model.createDIP(dip, true);

    index.commit(IndexedDIP.class);
    return dip;
  }

  private DIP createFileDIP(List<File> fileList) throws AuthorizationDeniedException, GenericException {
    List<FileLink> links = new ArrayList<>();
    for (File file : fileList) {
      FileLink fileLink = new FileLink(file.getAipId(), file.getRepresentationId(), file.getPath(), file.getId());
      links.add(fileLink);
    }

    DIP dip = new DIP();
    dip.setId(IdUtils.createUUID());
    dip.setFileIds(links);
    dip.setTitle("EARK-DIP");
    dip.setDescription("EARK-DIP generated and filtered based on an File");
    dip.setType(RodaConstants.DIP_TYPE_CONVERSION);
    dip = model.createDIP(dip, true);

    index.commit(IndexedDIP.class);
    return dip;
  }

  /*
   * AIP related
   */
  @Test
  public void testDeleteAIPWithASingleLinkInDIP() throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    // create AIP and rep
    AIP aip = model.createAIP(null, "", new Permissions(), RodaConstants.ADMIN);

    // create DIP
    DIP dip = createAIPDIP(Collections.singletonList(aip));

    Job jobDeleteAIP = TestsHelper.executeJob(DeleteRODAObjectPlugin.class, Collections.EMPTY_MAP, PluginType.INTERNAL,
      SelectedItemsList.create(IndexedAIP.class, aip.getId()));
    TestsHelper.getJobReports(index, jobDeleteAIP, true);

    try {
      DIP retrievedDIP = model.retrieveDIP(dip.getId());
      AssertJUnit.assertNull("Failed to delete DIP linked to an AIP", retrievedDIP);
    } catch (NotFoundException e) {
      // pass
    }
  }

  @Test
  public void testDeleteAIPWithAMultiplesLinkInDIP() throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    // create AIP and rep
    AIP aip = model.createAIP(null, "", new Permissions(), RodaConstants.ADMIN);
    AIP aip2 = model.createAIP(null, "", new Permissions(), RodaConstants.ADMIN);

    // create DIP
    DIP dip = createAIPDIP(Arrays.asList(aip, aip2));

    Job jobDeleteAIP = TestsHelper.executeJob(DeleteRODAObjectPlugin.class, Collections.EMPTY_MAP, PluginType.INTERNAL,
      SelectedItemsList.create(IndexedAIP.class, aip.getId()));
    TestsHelper.getJobReports(index, jobDeleteAIP, true);

    DIP retrievedDIP = model.retrieveDIP(dip.getId());
    AssertJUnit.assertNotNull("DIP associated with AIP 2 has been removed improperly", retrievedDIP);

    List<String> aipIdList = retrievedDIP.getAipIds().stream().map(AIPLink::getAipId).collect(Collectors.toList());
    AssertJUnit.assertEquals("DIP should have only one link", 1, aipIdList.size());

    if (aipIdList.contains(aip.getId())) {
      AssertJUnit.fail("AIP 1 should have been removed from the DIP");
    }

    if (!aipIdList.contains(aip2.getId())) {
      AssertJUnit.fail("AIP 2 should be on the DIP");
    }
  }

  @Test
  public void testDeleteAIPWithALinkedRepresentationInDIP() throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    // create AIP and rep
    AIP aip = model.createAIP(null, "", new Permissions(), RodaConstants.ADMIN);
    Representation representation = model.createRepresentation(aip.getId(), IdUtils.createUUID(), true, "", true,
      RodaConstants.ADMIN);

    // create DIP
    DIP dip = createRepresentationDIP(Collections.singletonList(representation));

    Job jobDeleteAIP = TestsHelper.executeJob(DeleteRODAObjectPlugin.class, Collections.EMPTY_MAP, PluginType.INTERNAL,
      SelectedItemsList.create(IndexedAIP.class, aip.getId()));
    TestsHelper.getJobReports(index, jobDeleteAIP, true);

    try {
      DIP retrievedDIP = model.retrieveDIP(dip.getId());
      AssertJUnit.assertNull("File was removed with AIP, but DIP still exists", retrievedDIP);
    } catch (NotFoundException e) {
      // pass
    }
  }

  @Test
  public void testDeleteAIPWithALinkedFileInDIP() throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    // create AIP and rep
    AIP aip = model.createAIP(null, "", new Permissions(), RodaConstants.ADMIN);
    Representation representation = model.createRepresentation(aip.getId(), IdUtils.createUUID(), true, "", true,
      RodaConstants.ADMIN);
    File file = model.createFile(aip.getId(), representation.getId(), Collections.emptyList(), "file",
      new StringContentPayload("file"), RodaConstants.ADMIN, true);

    // create DIP
    DIP dip = createFileDIP(Collections.singletonList(file));

    Job jobDeleteAIP = TestsHelper.executeJob(DeleteRODAObjectPlugin.class, Collections.EMPTY_MAP, PluginType.INTERNAL,
      SelectedItemsList.create(IndexedAIP.class, aip.getId()));
    TestsHelper.getJobReports(index, jobDeleteAIP, true);

    try {
      DIP retrievedDIP = model.retrieveDIP(dip.getId());
      AssertJUnit.assertNull("File was removed with AIP, but DIP still exists", retrievedDIP);
    } catch (NotFoundException e) {
      // pass
    }
  }

  @Test
  public void testDeleteAIPWithAIPChildAndALinkInDIP() throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    // create AIP
    AIP aip = model.createAIP(null, "", new Permissions(), RodaConstants.ADMIN);
    DIP dip = createAIPDIP(Collections.singletonList(aip));

    // create AIP child
    AIP aipChild = model.createAIP(aip.getId(), "", new Permissions(), RodaConstants.ADMIN);
    DIP dipChild = createAIPDIP(Collections.singletonList(aipChild));

    AIP aipChild2 = model.createAIP(aip.getId(), "", new Permissions(), RodaConstants.ADMIN);
    DIP dipChild2 = createAIPDIP(Collections.singletonList(aipChild2));

    Job jobDeleteAIP = TestsHelper.executeJob(DeleteRODAObjectPlugin.class, Collections.EMPTY_MAP, PluginType.INTERNAL,
      SelectedItemsList.create(IndexedAIP.class, aip.getId()));
    TestsHelper.getJobReports(index, jobDeleteAIP, true);

    try {
      DIP retrievedDIP = model.retrieveDIP(dip.getId());
      AssertJUnit.assertNull("Failed to delete DIP linked to an AIP", retrievedDIP);

      DIP retrievedChildDIP = model.retrieveDIP(dipChild.getId());
      AssertJUnit.assertNull("Failed to delete DIP linked to child AIP 1", retrievedChildDIP);

      DIP retrievedChildDIP2 = model.retrieveDIP(dipChild2.getId());
      AssertJUnit.assertNull("Failed to delete DIP linked to child AIP 2", retrievedChildDIP2);
    } catch (NotFoundException e) {
      // pass
    }
  }

  /*
   * Representation related
   */
  @Test
  public void testDeleteRepresentationWithASingleLinkInDIP() throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    // create AIP and rep
    AIP aip = model.createAIP(null, "", new Permissions(), RodaConstants.ADMIN);
    Representation representation = model.createRepresentation(aip.getId(), IdUtils.createUUID(), true, "", true,
      RodaConstants.ADMIN);

    // create DIP
    DIP dip = createRepresentationDIP(Collections.singletonList(representation));

    Job jobDeleteRep = TestsHelper.executeJob(DeleteRODAObjectPlugin.class, Collections.EMPTY_MAP, PluginType.INTERNAL,
      SelectedItemsList.create(IndexedRepresentation.class, IdUtils.getRepresentationId(representation)));
    TestsHelper.getJobReports(index, jobDeleteRep, true);

    try {
      DIP retrievedDIP = model.retrieveDIP(dip.getId());
      AssertJUnit.assertNull("Failed to delete DIP linked to a representation", retrievedDIP);
    } catch (NotFoundException e) {
      // pass
    }
  }

  @Test
  public void testDeleteRepresentationWithALinkedFileInDIP() throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    // create AIP and rep
    AIP aip = model.createAIP(null, "", new Permissions(), RodaConstants.ADMIN);
    Representation representation = model.createRepresentation(aip.getId(), IdUtils.createUUID(), true, "", true,
      RodaConstants.ADMIN);
    File file = model.createFile(aip.getId(), representation.getId(), Collections.emptyList(), "file",
      new StringContentPayload("file"), RodaConstants.ADMIN, true);

    // create DIP
    DIP dip = createFileDIP(Collections.singletonList(file));

    Job jobDeleteRep = TestsHelper.executeJob(DeleteRODAObjectPlugin.class, Collections.EMPTY_MAP, PluginType.INTERNAL,
      SelectedItemsList.create(IndexedRepresentation.class, IdUtils.getRepresentationId(representation)));
    TestsHelper.getJobReports(index, jobDeleteRep, true);

    try {
      DIP retrievedDIP = model.retrieveDIP(dip.getId());
      AssertJUnit.assertNull("File was removed with AIP, but DIP still exists", retrievedDIP);
    } catch (NotFoundException e) {
      // pass
    }
  }

  @Test
  public void testDeleteRepresentationWithAMultiplesLinkInDIP() throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    // create AIP and rep
    AIP aip = model.createAIP(null, "", new Permissions(), RodaConstants.ADMIN);
    Representation representation = model.createRepresentation(aip.getId(), IdUtils.createUUID(), true, "", true,
      RodaConstants.ADMIN);
    Representation representation2 = model.createRepresentation(aip.getId(), IdUtils.createUUID(), true, "", true,
      RodaConstants.ADMIN);

    // create DIP
    DIP dip = createRepresentationDIP(Arrays.asList(representation, representation2));

    Job jobDeleteRep = TestsHelper.executeJob(DeleteRODAObjectPlugin.class, Collections.EMPTY_MAP, PluginType.INTERNAL,
      SelectedItemsList.create(IndexedRepresentation.class, IdUtils.getRepresentationId(representation)));
    TestsHelper.getJobReports(index, jobDeleteRep, true);

    DIP retrievedDIP = model.retrieveDIP(dip.getId());
    AssertJUnit.assertNotNull("DIP associated with Representation 2 has been removed improperly", retrievedDIP);

    List<String> representationIdList = retrievedDIP.getRepresentationIds().stream()
      .map(RepresentationLink::getRepresentationId).collect(Collectors.toList());
    AssertJUnit.assertEquals("DIP should have only one link", 1, representationIdList.size());

    if (representationIdList.contains(representation.getId())) {
      AssertJUnit.fail("Representation 1 should have been removed from the DIP");
    }

    if (!representationIdList.contains(representation2.getId())) {
      AssertJUnit.fail("Representation 2 should be on the DIP");
    }
  }

  /*
   * File related
   */
  @Test
  public void testDeleteFileWithASingleLinkInDIP() throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    // create AIP and rep
    AIP aip = model.createAIP(null, "", new Permissions(), RodaConstants.ADMIN);
    Representation representation = model.createRepresentation(aip.getId(), IdUtils.createUUID(), true, "", true,
      RodaConstants.ADMIN);
    File file = model.createFile(aip.getId(), representation.getId(), Collections.emptyList(), "file",
      new StringContentPayload("file"), RodaConstants.ADMIN, true);

    // create DIP
    DIP dip = createFileDIP(Collections.singletonList(file));

    Job jobDeleteFile = TestsHelper.executeJob(DeleteRODAObjectPlugin.class, Collections.EMPTY_MAP, PluginType.INTERNAL,
      SelectedItemsList.create(IndexedFile.class, IdUtils.getFileId(file)));
    TestsHelper.getJobReports(index, jobDeleteFile, true);

    try {
      DIP retrievedDIP = model.retrieveDIP(dip.getId());
      AssertJUnit.assertNull("Failed to delete DIP linked to a file", retrievedDIP);
    } catch (NotFoundException e) {
      // pass
    }
  }

  @Test
  public void testDeleteFileWithAMultiplesLinkInDIP() throws AuthorizationDeniedException, GenericException,
    RequestNotValidException, NotFoundException, AlreadyExistsException {
    // create AIP and rep
    AIP aip = model.createAIP(null, "", new Permissions(), RodaConstants.ADMIN);
    Representation representation = model.createRepresentation(aip.getId(), IdUtils.createUUID(), true, "", true,
      RodaConstants.ADMIN);
    File file = model.createFile(aip.getId(), representation.getId(), Collections.emptyList(), "file",
      new StringContentPayload("file"), RodaConstants.ADMIN, true);
    File file2 = model.createFile(aip.getId(), representation.getId(), Collections.emptyList(), "file2",
      new StringContentPayload("file2"), RodaConstants.ADMIN, true);

    // create DIP
    DIP dip = createFileDIP(Arrays.asList(file, file2));

    Job jobDeleteFile = TestsHelper.executeJob(DeleteRODAObjectPlugin.class, Collections.EMPTY_MAP, PluginType.INTERNAL,
      SelectedItemsList.create(IndexedFile.class, IdUtils.getFileId(file)));
    TestsHelper.getJobReports(index, jobDeleteFile, true);

    DIP retrievedDIP = model.retrieveDIP(dip.getId());
    AssertJUnit.assertNotNull("DIP associated with File 2 has been removed improperly", retrievedDIP);

    List<String> fileIdList = retrievedDIP.getFileIds().stream().map(FileLink::getFileId).collect(Collectors.toList());
    AssertJUnit.assertEquals("DIP should have only one link", 1, fileIdList.size());

    if (fileIdList.contains(file.getId())) {
      AssertJUnit.fail("File 1 should have been removed from the DIP");
    }

    if (!fileIdList.contains(file2.getId())) {
      AssertJUnit.fail("File 2 should be on the DIP");
    }
  }

}
