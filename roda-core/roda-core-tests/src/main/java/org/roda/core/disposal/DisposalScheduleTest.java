package org.roda.core.disposal;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodIntervalCode;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexServiceTest;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.plugins.internal.disposal.schedule.AssociateDisposalScheduleToAIPPlugin;
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
public class DisposalScheduleTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(DisposalScheduleTest.class);

  private Path basePath;
  private Path storagePath;

  private ModelService model;
  private static StorageService corporaService;
  private static IndexService index;

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(DisposalScheduleTest.class, true);

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

    LOGGER.info("Running Disposal schedule tests under storage {}", basePath);
  }

  @AfterClass
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    // FSUtils.deletePath(basePath);
  }

  @AfterMethod
  public void cleanUp() throws RODAException {
    TestsHelper.releaseAllLocks();
  }

  @Test
  public void testDisposalScheduleCreation() throws AlreadyExistsException, AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException {
    DisposalSchedule disposalSchedule = createDisposalSchedule();
    DisposalSchedule retrievedDisposalSchedule = model.retrieveDisposalSchedule(disposalSchedule.getId());

    assertEquals(disposalSchedule, retrievedDisposalSchedule);
  }

  @Test
  public void testDisposalScheduleDeletionWhenNeverUsed() throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, IllegalOperationException {
    DisposalSchedule disposalSchedule = createDisposalSchedule();
    StoragePath disposalScheduleStoragePath = ModelUtils.getDisposalScheduleStoragePath(disposalSchedule.getId());

    model.deleteDisposalSchedule(disposalSchedule.getId());

    assertFalse(Files.exists(FSUtils.getEntityPath(storagePath, disposalScheduleStoragePath)));
  }

  @Test
  public void testDisposalScheduleDeletionWhenUsed() throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, IllegalOperationException {
    DisposalSchedule disposalSchedule = createDisposalSchedule();
    disposalSchedule.setFirstTimeUsed(new Date());

    model.updateDisposalSchedule(disposalSchedule, "admin");
    StoragePath disposalScheduleStoragePath = ModelUtils.getDisposalScheduleStoragePath(disposalSchedule.getId());
    try {
      model.deleteDisposalSchedule(disposalSchedule.getId());
    } catch (IllegalOperationException e) {
      // do nothing
    }

    assertTrue(Files.exists(FSUtils.getEntityPath(storagePath, disposalScheduleStoragePath)));
  }

  @Test
  public void testDisposalScheduleAipAssociation() throws AuthorizationDeniedException, RequestNotValidException,
    NotFoundException, GenericException, AlreadyExistsException, ValidationException {

    // generate AIP ID
    final String aipCorporaId = CorporaConstants.SOURCE_AIP_ID;
    final String aipId = IdUtils.createUUID();
    final DefaultStoragePath aipPath = DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, aipCorporaId);
    final AIP aip = model.createAIP(aipId, corporaService, aipPath, RodaConstants.ADMIN);
    // Generate Disposal
    DisposalSchedule disposalSchedule = createDisposalSchedule();

    // Associate AIP with Disposal schedule
    aip.setDisposalScheduleId(disposalSchedule.getId());
    aip.setDestroyedOn(new Date());
    aip.setDestroyedBy(RodaConstants.ADMIN);
    AIP updatedAIP = model.updateAIP(aip, RodaConstants.ADMIN);

    // check it is connected
    AIP retrievedAIP = model.retrieveAIP(aipId);
    assertEquals(updatedAIP, retrievedAIP);
  }

  @Test
  public void testDisposalScheduleAipIndexAssociation() throws GenericException, AuthorizationDeniedException,
    ValidationException, AlreadyExistsException, RequestNotValidException, NotFoundException {
    // generate AIP ID
    final String aipId = IdUtils.createUUID();

    // Create AIP
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    // Generate Disposal
    DisposalSchedule disposalSchedule = createDisposalSchedule();

    // Associate AIP with Disposal schedule
    aip.setDisposalScheduleId(disposalSchedule.getId());
    // aip.setDestructionOn(new Date());
    aip.setDestroyedBy(RodaConstants.ADMIN);
    model.updateAIP(aip, RodaConstants.ADMIN);
    index.commitAIPs();

    // Retrieve AIP
    final IndexedAIP indexedAip = index.retrieve(IndexedAIP.class, aipId, new ArrayList<>());
    assertEquals(aip.getDisposalScheduleId(), indexedAip.getDisposalScheduleId());
    assertEquals(aip.getDestroyedOn(), indexedAip.getDestroyedOn());
    assertEquals(aip.getDestroyedBy(), indexedAip.getDestroyedBy());
  }

  @Test
  public void testDisposalScheduleRecursiveAssociation() throws RequestNotValidException, AuthorizationDeniedException,
    ValidationException, NotFoundException, GenericException, AlreadyExistsException {
    // generate AIP ID
    final String parentId = IdUtils.createUUID();
    final String childId = IdUtils.createUUID();
    final String grandsonId = IdUtils.createUUID();

    // Create AIPs
    AIP parentAIP = model.createAIP(parentId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);
    AIP childAIP = model.createAIP(childId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);
    AIP grandsonAIP = model.createAIP(grandsonId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    model.updateAIP(parentAIP, RodaConstants.ADMIN);
    model.updateAIP(childAIP, RodaConstants.ADMIN);
    model.updateAIP(grandsonAIP, RodaConstants.ADMIN);
    // Create hierarchy
    model.moveAIP(childId, parentId, RodaConstants.ADMIN);
    model.moveAIP(grandsonId, parentId, RodaConstants.ADMIN);
    index.commitAIPs();

    // Generate Disposal
    DisposalSchedule disposalSchedule = createDisposalSchedule();

    // Associate disposal schedule to parentAip
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.AIP_ID, parentId));
    SelectedItemsFilter<IndexedAIP> selectedItems = new SelectedItemsFilter<>(filter, IndexedAIP.class.getName(),
      Boolean.FALSE);
    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_DISPOSAL_SCHEDULE_ID, disposalSchedule.getId());
    Job job = TestsHelper.executeJob(AssociateDisposalScheduleToAIPPlugin.class, pluginParameters, PluginType.INTERNAL,
      (SelectedItems) selectedItems);
    index.commitAIPs();
    // assertEquals(3, job.getJobStats().getSourceObjectsCount());

    AIP retrieveChildAIP = model.retrieveAIP(childId);
    AIP retrieveGrandSonAIP = model.retrieveAIP(grandsonId);
    assertEquals(disposalSchedule.getId(), retrieveChildAIP.getDisposalScheduleId());
    assertEquals(disposalSchedule.getId(), retrieveGrandSonAIP.getDisposalScheduleId());
  }

  private DisposalSchedule createDisposalSchedule() throws AlreadyExistsException, AuthorizationDeniedException,
    GenericException, NotFoundException, RequestNotValidException {
    String title = "Normal";
    String description = "Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Sed diam.";
    String mandate = "Pellentesque imperdiet nunc dolor.";
    String scopeNotes = "Praesent laoreet lacus leo, vitae consectetur nulla maximus non. Ut.";
    DisposalActionCode actionCode = DisposalActionCode.DESTROY;
    String retentionTriggerElementId = "dateFinal";
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
