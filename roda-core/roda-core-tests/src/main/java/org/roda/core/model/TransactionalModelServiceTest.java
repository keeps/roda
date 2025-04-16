package org.roda.core.model;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.transaction.RODATransactionManager;
import org.roda.core.config.TestConfig;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.model.utils.ModelUtils;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.StorageTestUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@ContextConfiguration(classes = TestConfig.class)
@SpringBootTest(classes = TestConfig.class)
public class TransactionalModelServiceTest extends AbstractTestNGSpringContextTests {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalModelServiceTest.class);

  private static Path basePath;
  private static Path logPath;
  private static StorageService storage;
  private static ModelService model;
  private static StorageService corporaService;
  private static int fileCounter = 0;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;

  @Autowired
  private RODATransactionManager transactionManager;

  @BeforeClass
  public static void setUp() throws IOException, URISyntaxException, GenericException {
    URL corporaURL = ModelServiceTest.class.getResource("/corpora");
    Path corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    LOGGER.debug("Running model test under storage: {}", basePath);
  }

  @BeforeClass
  public void init() throws IOException, GenericException {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    boolean deploySolr = false;
    boolean deployLdap = true;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = false;
    boolean deployPluginManager = false;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false, ldapUtilityTestHelper.getLdapUtility());

    logPath = RodaCoreFactory.getLogPath();
    storage = RodaCoreFactory.getStorageService();
    model = RodaCoreFactory.getModelService();
  }

  @AfterClass
  public void cleanup() throws NotFoundException, GenericException, IOException {
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
  }

  @Test
  public void testUpdateAIP() throws RODAException, IOException {
    // set up
    final String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    StoragePath otherAipPath = DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER,
      CorporaConstants.OTHER_AIP_ID);
    AIP updatedAIP = model.updateAIP(aipId, corporaService, otherAipPath, RodaConstants.ADMIN);

    // check it is connected
    AIP retrievedAIP = model.retrieveAIP(aipId);
    assertEquals(updatedAIP, retrievedAIP);

    // check content is correct
    StorageTestUtils.testEntityEqualRecursively(corporaService, otherAipPath, storage,
      ModelUtils.getAIPStoragePath(aipId));

    // cleanup
    model.deleteAIP(aipId);
  }

}
