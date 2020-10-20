package org.roda.core.disposal;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexServiceTest;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class DisposalHoldTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(DisposalHoldTest.class);

  private Path basePath;
  private Path storagePath;

  private ModelService model;
  private static StorageService corporaService;
  private static IndexService index;

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(DisposalHoldTest.class, true);

    boolean deploySolr = true;
    boolean deployLdap = false;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources);
    model = RodaCoreFactory.getModelService();
    storagePath = RodaCoreFactory.getStoragePath();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = IndexServiceTest.class.getResource("/corpora");
    corporaService = new FileStorageService(Paths.get(corporaURL.toURI()));

    LOGGER.info("Running Disposal hold tests under storage {}", basePath);
  }

  @AfterClass
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @AfterMethod
  public void cleanUp() throws RODAException {
    TestsHelper.releaseAllLocks();
  }

  @Test
  public void testDisposalHoldCreation() throws AuthorizationDeniedException, GenericException, NotFoundException,
    RequestNotValidException, AlreadyExistsException {

    DisposalHold hold = model.createDisposalHold(createDisposalHold(), "admin");

    DisposalHold retrievedDisposalHold = model.retrieveDisposalHold(hold.getId());

    assertEquals(hold, retrievedDisposalHold);
  }

  @Test
  public void testDisposalHoldDeletionWhenNeverUsed() throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, IllegalOperationException, AlreadyExistsException {
    DisposalHold disposalHold = createDisposalHold();
    model.createDisposalHold(disposalHold, "admin");
    StoragePath disposalHoldStoragePath = ModelUtils.getDisposalHoldStoragePath(disposalHold.getId());

    model.deleteDisposalHold(disposalHold.getId());

    assertFalse(Files.exists(FSUtils.getEntityPath(storagePath, disposalHoldStoragePath)));
  }

  @Test
  public void testDisposalHoldDeletionWhenUsed() throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException {
    DisposalHold disposalHold = createDisposalHold();
    disposalHold = model.createDisposalHold(disposalHold, "admin");

    disposalHold.addAIPtoActiveAIPs(IdUtils.createUUID());

    model.updateDisposalHold(disposalHold, "admin");
    StoragePath disposalScheduleStoragePath = ModelUtils.getDisposalHoldStoragePath(disposalHold.getId());
    try {
      model.deleteDisposalHold(disposalHold.getId());
    } catch (IllegalOperationException e) {
      // do nothing
    }
    assertTrue(Files.exists(FSUtils.getEntityPath(storagePath, disposalScheduleStoragePath)));
  }

  @Test
  public void testDisposalHoldLiftAll() throws AuthorizationDeniedException, AlreadyExistsException, NotFoundException,
    GenericException, RequestNotValidException {
    DisposalHold disposalHold = createDisposalHold();
    disposalHold = model.createDisposalHold(disposalHold, "admin");

    disposalHold.addAIPtoActiveAIPs(IdUtils.createUUID());

    model.updateDisposalHold(disposalHold, "admin");

    disposalHold.liftAllAIPs();
    model.updateDisposalHold(disposalHold, "admin");

    DisposalHold retrievedDisposalHold = model.retrieveDisposalHold(disposalHold.getId());
    assertTrue(retrievedDisposalHold.getActiveAIPs().isEmpty());
    assertFalse(retrievedDisposalHold.getInactiveAIPs().isEmpty());
    assertEquals(retrievedDisposalHold.getInactiveAIPs().size(), 1);
  }

  public void testDisposalHoldAIPAssociation() throws AuthorizationDeniedException, AlreadyExistsException,
          NotFoundException, GenericException, RequestNotValidException, ValidationException {

    // generate AIP ID
    final String aipId = CorporaConstants.SOURCE_AIP_ID;
    final DefaultStoragePath aipPath = DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, aipId);
    final AIP aip = model.createAIP(aipId, corporaService, aipPath, RodaConstants.ADMIN);

    // generate disposal hold
    DisposalHold disposalHold = model.createDisposalHold(createDisposalHold(), RodaConstants.ADMIN);
    DisposalHold disposalHold2 = model.createDisposalHold(createDisposalHold(), RodaConstants.ADMIN);

    aip.addDisposalHoldsId(disposalHold.getId());
    aip.addDisposalHoldsId(disposalHold2.getId());
    AIP updatedAIP = model.updateAIP(aip, RodaConstants.ADMIN);

    // check it is connected
    AIP retrievedAIP = model.retrieveAIP(aipId);
    assertEquals(updatedAIP, retrievedAIP);
  }

    @Test
  public void testDisposalHoldsAipIndexAssociation() throws GenericException, AuthorizationDeniedException,
    ValidationException, AlreadyExistsException, RequestNotValidException, NotFoundException {
    // generate AIP ID
    final String aipId = IdUtils.createUUID();

    // Create AIP
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    // generate disposal hold
    DisposalHold disposalHold = model.createDisposalHold(createDisposalHold(), RodaConstants.ADMIN);
    DisposalHold disposalHold2 = model.createDisposalHold(createDisposalHold(), RodaConstants.ADMIN);

    // Associate AIP with Disposal schedule
    aip.addDisposalHoldsId(disposalHold.getId());
    aip.addDisposalHoldsId(disposalHold2.getId());
    model.updateAIP(aip, RodaConstants.ADMIN);
    index.commitAIPs();

    // Retrieve AIP
    final IndexedAIP indexedAip = index.retrieve(IndexedAIP.class, aipId, new ArrayList<>());
    assertEquals(aip.getDisposalHoldsId(), indexedAip.getDisposalHoldsId());
  }

  private DisposalHold createDisposalHold() {
    String title = "Process in court of law";
    String description = "Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Sed diam.";
    String mandate = "Pellentesque imperdiet nunc dolor.";
    String scopeNotes = "Praesent laoreet lacus leo, vitae consectetur nulla maximus non. Ut.";

    DisposalHold hold = new DisposalHold();
    hold.setTitle(title);
    hold.setDescription(description);
    hold.setMandate(mandate);
    hold.setScopeNotes(scopeNotes);

    return hold;
  }
}
