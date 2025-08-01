/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
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
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationState;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.fs.FSUtils;
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
public class DisposalConfirmationTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(DisposalConfirmationTest.class);

  private Path basePath;
  private ModelService model;
  private SolrClient solrClient;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(DisposalConfirmationTest.class, true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false, ldapUtilityTestHelper.getLdapUtility());
    model = RodaCoreFactory.getModelService();
    solrClient = RodaCoreFactory.getSolr();

    LOGGER.info("Running disposal confirmation tests under storage {}", basePath);
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
    TestsHelper.releaseAllLocks();
  }

  @Test
  public void testDisposalConfirmationCreation() throws AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException, AlreadyExistsException {

    DisposalConfirmation confirmation = model.createDisposalConfirmation(createDisposalConfirmation(), "admin");

    model.createDisposalConfirmation(createDisposalConfirmation(DisposalConfirmationState.PENDING), "mguimaraes");

    model.createDisposalConfirmation(createDisposalConfirmation(DisposalConfirmationState.RESTORED), "lfaria");

    DisposalConfirmation retrievedConfirmation = SolrUtils.retrieve(solrClient, DisposalConfirmation.class,
      confirmation.getId(), new ArrayList<>());

    assertEquals(confirmation, retrievedConfirmation);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void testDisposalConfirmationPendingDeletion() throws AlreadyExistsException, AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException, IllegalOperationException {
    DisposalConfirmation confirmation = model.createDisposalConfirmation(createDisposalConfirmation(), "admin");

    model.deleteDisposalConfirmation(confirmation.getId());

    SolrUtils.retrieve(solrClient, DisposalConfirmation.class, confirmation.getId(), Collections.emptyList());
  }

  @Test(expectedExceptions = IllegalOperationException.class)
  public void testDisposalConfirmationApprovedOrRecoveredDeletion()
    throws AlreadyExistsException, AuthorizationDeniedException, GenericException, NotFoundException,
    RequestNotValidException, IllegalOperationException {
    DisposalConfirmation confirmation = model
      .createDisposalConfirmation(createDisposalConfirmation(DisposalConfirmationState.APPROVED), "admin");

    model.deleteDisposalConfirmation(confirmation.getId());
  }

  private DisposalConfirmation createDisposalConfirmation() {
    return createDisposalConfirmation(DisposalConfirmationState.PENDING);
  }

  private DisposalConfirmation createDisposalConfirmation(DisposalConfirmationState state) {
    DisposalConfirmation confirmation = new DisposalConfirmation();
    confirmation.setTitle("Confirmation");
    confirmation.setNumberOfAIPs(100L);
    confirmation.setState(state);
    confirmation.setSize(12346234L);

    return confirmation;
  }
}
