/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.model;

import static org.junit.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.LiteRODAObject;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.DescriptiveMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.plugins.EARKSIPPluginsTest;
import org.roda.core.plugins.plugins.characterization.PremisSkeletonPlugin;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LiteRODAObjectsTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(LiteRODAObjectsTest.class);

  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static StorageService storage;
  private static StorageService corporaService;
  private static Path corporaPath;

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
    storage = RodaCoreFactory.getStorageService();

    URL corporaURL = EARKSIPPluginsTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    LOGGER.info("Running E-ARK SIP plugins tests under storage {}", basePath);
  }

  @AfterClass
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @AfterMethod
  public void cleanUp() throws RODAException {
    index.execute(IndexedAIP.class, Filter.ALL, new ArrayList<>(), new IndexRunnable<IndexedAIP>() {
      @Override
      public void run(IndexedAIP item) throws GenericException, RequestNotValidException, AuthorizationDeniedException {
        try {
          model.deleteAIP(item.getId());
        } catch (NotFoundException e) {
          // do nothing
        }
      }
    }, e -> Assert.fail("Error cleaning up", e));
  }

  @Test
  public void testLitesWhenOnIndexButNotOnModel() throws RequestNotValidException, GenericException, AuthorizationDeniedException, AlreadyExistsException, NotFoundException, ValidationException {
    AIP aip = model.createAIP(IdUtils.createUUID(), corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_EMPTY),
      RodaConstants.ADMIN);
    
    File file = model.createFile(aip.getId(), CorporaConstants.REPRESENTATION_1_ID, Collections.emptyList(), "file1", new StringContentPayload(""), true);
    model.createFile(aip.getId(), CorporaConstants.REPRESENTATION_1_ID, Collections.emptyList(), "file2", new StringContentPayload(""), true);
    model.createFile(aip.getId(), CorporaConstants.REPRESENTATION_1_ID, Collections.emptyList(), "file3", new StringContentPayload(""), true);
    index.commit(IndexedFile.class);
    
    RodaCoreFactory.getStorageService().deleteResource(ModelUtils.getFileStoragePath(file));
    Filter filter = new Filter(new EmptyKeyFilterParameter(RodaConstants.FILE_HASH));

    try {   
      SelectedItemsFilter selectedItems = new SelectedItemsFilter(filter, IndexedFile.class.getName(), false);
      TestsHelper.executeJob(PremisSkeletonPlugin.class, PluginType.AIP_TO_AIP, selectedItems);
    } catch (NotFoundException e) {
      // exception can not stop the plugin execution
      fail();
    }
    
    index.commit(IndexedFile.class);
    long countWithoutHash1 = index.count(IndexedFile.class, filter);
    assertEquals(1, countWithoutHash1);
    
    index.reindexAIPs();
    long countWithoutHash2 = index.count(IndexedFile.class, filter);
    assertEquals(0, countWithoutHash2);
  }

  @Test
  public void createLites() {
    // 20170110 hsilva: ids have the LiteRODAObjectFactory separator in order to
    // test the encoding/decoding
    String id1 = "01|23";
    String id2 = "45|67";
    String id3 = "89|ab";

    // AIP
    AIP aip = new AIP();
    aip.setId(id1);
    Optional<LiteRODAObject> lite = LiteRODAObjectFactory.get(aip);
    assertTrue(lite.isPresent());
    if (lite.isPresent()) {
      assertEquals(getExpected(AIP.class, id1), lite.get().getInfo());
    }

    // DIP
    DIP dip = new DIP();
    dip.setId(id1);
    lite = LiteRODAObjectFactory.get(dip);
    assertTrue(lite.isPresent());
    if (lite.isPresent()) {
      assertEquals(getExpected(DIP.class, id1), lite.get().getInfo());
    }

    // Format
    RepresentationInformation ri = new RepresentationInformation();
    ri.setId(id1);
    lite = LiteRODAObjectFactory.get(ri);
    assertTrue(lite.isPresent());
    if (lite.isPresent()) {
      assertEquals(getExpected(RepresentationInformation.class, id1), lite.get().getInfo());
    }

    // Job
    Job job = new Job();
    job.setId(id1);
    lite = LiteRODAObjectFactory.get(job);
    assertTrue(lite.isPresent());
    if (lite.isPresent()) {
      assertEquals(getExpected(Job.class, id1), lite.get().getInfo());
    }

    // Report
    Report report = new Report();
    report.setJobId(id1);
    report.setId(id2);
    lite = LiteRODAObjectFactory.get(report);
    assertTrue(lite.isPresent());
    if (lite.isPresent()) {
      assertEquals(getExpected(Report.class, id1, id2), lite.get().getInfo());
    }

    // Notification
    Notification notification = new Notification();
    notification.setId(id1);
    lite = LiteRODAObjectFactory.get(notification);
    assertTrue(lite.isPresent());
    if (lite.isPresent()) {
      assertEquals(getExpected(Notification.class, id1), lite.get().getInfo());
    }

    // Risk
    Risk risk = new Risk();
    risk.setId(id1);
    lite = LiteRODAObjectFactory.get(risk);
    assertTrue(lite.isPresent());
    if (lite.isPresent()) {
      assertEquals(getExpected(Risk.class, id1), lite.get().getInfo());
    }

    // RiskIncidence
    RiskIncidence riskIncidence = new RiskIncidence();
    riskIncidence.setId(id1);
    lite = LiteRODAObjectFactory.get(riskIncidence);
    assertTrue(lite.isPresent());
    if (lite.isPresent()) {
      assertEquals(getExpected(RiskIncidence.class, id1), lite.get().getInfo());
    }

    // Representation
    Representation representation = new Representation();
    representation.setAipId(id1);
    representation.setId(id2);
    lite = LiteRODAObjectFactory.get(representation);
    assertTrue(lite.isPresent());
    if (lite.isPresent()) {
      assertEquals(getExpected(Representation.class, id1, id2), lite.get().getInfo());
    }

    // TransferredResource
    TransferredResource transferredResource = new TransferredResource();
    transferredResource.setFullPath(id1);
    lite = LiteRODAObjectFactory.get(transferredResource);
    assertTrue(lite.isPresent());
    if (lite.isPresent()) {
      assertEquals(getExpected(TransferredResource.class, id1), lite.get().getInfo());
    }

    // DescriptiveMetadata - aip level
    DescriptiveMetadata descriptiveMetadataAip = new DescriptiveMetadata();
    descriptiveMetadataAip.setAipId(id1);
    descriptiveMetadataAip.setId(id2);
    lite = LiteRODAObjectFactory.get(descriptiveMetadataAip);
    assertTrue(lite.isPresent());
    if (lite.isPresent()) {
      assertEquals(getExpected(DescriptiveMetadata.class, id1, id2), lite.get().getInfo());
    }

    // DescriptiveMetadata - representation level
    DescriptiveMetadata descriptiveMetadataRep = new DescriptiveMetadata();
    descriptiveMetadataRep.setAipId(id1);
    descriptiveMetadataRep.setRepresentationId(id2);
    descriptiveMetadataRep.setId(id3);
    lite = LiteRODAObjectFactory.get(descriptiveMetadataRep);
    assertTrue(lite.isPresent());
    if (lite.isPresent()) {
      assertEquals(getExpected(DescriptiveMetadata.class, id1, id2, id3), lite.get().getInfo());
    }

    // LogEntry
    LogEntry logEntry = new LogEntry();
    logEntry.setId(id1);
    lite = LiteRODAObjectFactory.get(logEntry);
    assertTrue(lite.isPresent());
    if (lite.isPresent()) {
      assertEquals(getExpected(LogEntry.class, id1), lite.get().getInfo());
    }

    // RODAMember - user
    User user = new User();
    user.setName(id1);
    lite = LiteRODAObjectFactory.get(user);
    assertTrue(lite.isPresent());
    if (lite.isPresent()) {
      assertEquals(getExpected(User.class, id1), lite.get().getInfo());
    }

    // RODAMember - group
    Group group = new Group();
    group.setName(id1);
    lite = LiteRODAObjectFactory.get(group);
    assertTrue(lite.isPresent());
    if (lite.isPresent()) {
      assertEquals(getExpected(Group.class, id1), lite.get().getInfo());
    }
  }

  private <T extends IsRODAObject> String getExpected(Class<T> objectClass, String... ids) {
    StringBuilder sb = new StringBuilder();
    sb.append(objectClass.getName());
    for (String id : ids) {
      sb.append(LiteRODAObjectFactory.SEPARATOR)
        .append(id.replaceAll(LiteRODAObjectFactory.SEPARATOR_REGEX, LiteRODAObjectFactory.SEPARATOR_URL_ENCODED));
    }

    return sb.toString();
  }
}
