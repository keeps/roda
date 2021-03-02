/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.disposal;

import static org.testng.AssertJUnit.assertEquals;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodIntervalCode;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexServiceTest;
import org.roda.core.model.ModelService;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class DisposalRuleTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(DisposalRuleTest.class);

  private Path basePath;
  private Path storagePath;

  private ModelService model;
  private static StorageService corporaService;
  private static IndexService index;

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(DisposalRuleTest.class, true);

    boolean deploySolr = true;
    boolean deployLdap = false;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false);
    model = RodaCoreFactory.getModelService();
    storagePath = RodaCoreFactory.getStoragePath();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = IndexServiceTest.class.getResource("/corpora");
    corporaService = new FileStorageService(Paths.get(corporaURL.toURI()));

    LOGGER.info("Running Disposal rule tests under storage {}", basePath);
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
  public void testDisposalRuleCreation() throws AlreadyExistsException, AuthorizationDeniedException, GenericException,
    NotFoundException, RequestNotValidException {
    DisposalRule disposalRule = createDisposalRule();
    DisposalRule retrievedDisposalRule = model.retrieveDisposalRule(disposalRule.getId());

    assertEquals(disposalRule, retrievedDisposalRule);
  }

  @Test
  public void testDisposalRuleDisposalScheduleAssociation() throws AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException, ValidationException {

    // generate Disposal Schedule ID
    DisposalSchedule disposalSchedule = createDisposalSchedule();

    // Generate Disposal Rule
    DisposalRule disposalRule = createDisposalRule();

    // Associate Disposal Schedule with Disposal Rule
    disposalRule.setDisposalScheduleId(disposalSchedule.getId());
    DisposalRule updatedDisposalRule = model.updateDisposalRule(disposalRule, RodaConstants.ADMIN);

    // check it is connected
    DisposalRule retrievedDisposalRule = model.retrieveDisposalRule(disposalRule.getId());
    assertEquals(updatedDisposalRule, retrievedDisposalRule);
  }

  private DisposalRule createDisposalRule() throws AlreadyExistsException, AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException {
    String title = "Normal";
    String key = "metadataKey";
    String value = "metadataValue";
    String createdBy = "admin";
    Integer order = 1;

    DisposalRule rule = new DisposalRule();
    rule.setTitle(title);
    rule.setOrder(order);

    return model.createDisposalRule(rule, createdBy);
  }

  private DisposalSchedule createDisposalSchedule() throws AlreadyExistsException, AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException {
    String title = "Normal";
    String description = "Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Sed diam.";
    String mandate = "Pellentesque imperdiet nunc dolor.";
    String scopeNotes = "Praesent laoreet lacus leo, vitae consectetur nulla maximus non. Ut.";
    DisposalActionCode actionCode = DisposalActionCode.DESTROY;
    String retentionTriggerElementId = "";
    RetentionPeriodIntervalCode periodIntervalCode = RetentionPeriodIntervalCode.YEARS;
    int retentionPeriodDuration = 10;
    String createdBy = "admin";

    DisposalSchedule schedule = new DisposalSchedule();
    schedule.setTitle(title);
    schedule.setDescription(description);
    schedule.setMandate(mandate);
    schedule.setScopeNotes(scopeNotes);
    schedule.setActionCode(actionCode);
    schedule.setRetentionTriggerElementId(retentionTriggerElementId);
    schedule.setRetentionPeriodIntervalCode(periodIntervalCode);
    schedule.setRetentionPeriodDuration(retentionPeriodDuration);

    return model.createDisposalSchedule(schedule, createdBy);
  }
}
