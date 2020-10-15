package org.roda.core.disposal;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

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
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.storage.fs.FSUtils;
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

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(DisposalHoldTest.class, true);

    boolean deploySolr = false;
    boolean deployLdap = false;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources);
    model = RodaCoreFactory.getModelService();
    storagePath = RodaCoreFactory.getStoragePath();

    LOGGER.info("Running Disposal hold tests under storage {}", basePath);
  }

  @AfterClass
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    //FSUtils.deletePath(basePath);
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
  public void testDisposalHoldLiftAll() throws AuthorizationDeniedException, AlreadyExistsException, NotFoundException, GenericException, RequestNotValidException {
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
