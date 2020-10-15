package org.roda.core.disposal;

import static org.testng.AssertJUnit.assertEquals;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.solr.client.solrj.SolrClient;
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
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationMetadata;
import org.roda.core.data.v2.ip.disposal.DisposalConfirmationState;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
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
public class DisposalConfirmationMetadataTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(DisposalConfirmationMetadataTest.class);

  private Path basePath;
  private Path storagePath;

  private ModelService model;
  private SolrClient solrClient;

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(DisposalConfirmationMetadataTest.class, true);

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources);
    model = RodaCoreFactory.getModelService();
    storagePath = RodaCoreFactory.getStoragePath();
    solrClient = RodaCoreFactory.getSolr();

    LOGGER.info("Running disposal confirmation tests under storage {}", basePath);
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
  public void testDisposalConfirmationCreation() throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException {

    DisposalConfirmationMetadata confirmation = model.createDisposalConfirmationMetadata(createDisposalConfirmation(),
      "admin");

    DisposalConfirmationMetadata retrievedConfirmation = SolrUtils.retrieve(solrClient,
      DisposalConfirmationMetadata.class, confirmation.getId(), new ArrayList<>());

    assertEquals(confirmation, retrievedConfirmation);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void testDisposalConfirmationPendingDeletion() throws AlreadyExistsException, AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException, IllegalOperationException {
    DisposalConfirmationMetadata confirmation = model.createDisposalConfirmationMetadata(createDisposalConfirmation(),
      "admin");

    model.deleteDisposalConfirmation(confirmation.getId());

    SolrUtils.retrieve(solrClient, DisposalConfirmationMetadata.class, confirmation.getId(), Collections.emptyList());
  }

  @Test(expectedExceptions = IllegalOperationException.class)
  public void testDisposalConfirmationApprovedOrRecoveredDeletion()
    throws AlreadyExistsException, AuthorizationDeniedException, GenericException, NotFoundException,
    RequestNotValidException, IllegalOperationException {
    DisposalConfirmationMetadata confirmation = model
      .createDisposalConfirmationMetadata(createDisposalConfirmation(DisposalConfirmationState.APPROVED), "admin");

    model.deleteDisposalConfirmation(confirmation.getId());
  }

  private DisposalConfirmationMetadata createDisposalConfirmation() {
    return createDisposalConfirmation(DisposalConfirmationState.PENDING);
  }

  private DisposalConfirmationMetadata createDisposalConfirmation(DisposalConfirmationState state) {
    DisposalConfirmationMetadata confirmation = new DisposalConfirmationMetadata();
    confirmation.setTitle("Confirmation");
    confirmation.setNumberOfAIPs(100L);
    confirmation.setNumberOfCollections(1L);
    confirmation.setState(state);
    confirmation.setSize(12346234L);

    return confirmation;
  }
}
