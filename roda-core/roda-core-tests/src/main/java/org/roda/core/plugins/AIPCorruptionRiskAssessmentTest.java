/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.ingest.v2.DefaultIngestPlugin;
import org.roda.core.plugins.base.preservation.AIPCorruptionRiskAssessmentPlugin;
import org.roda.core.plugins.base.v2.FailureOnMandatoryStepIngestPlugin;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class AIPCorruptionRiskAssessmentTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(AIPCorruptionRiskAssessmentTest.class);
  private static Path basePath;

  private static ModelService model;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;
  private static StorageService corporaService;

  @BeforeMethod
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false, ldapUtilityTestHelper.getLdapUtility());
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = AIPCorruptionRiskAssessmentTest.class.getResource("/corpora");
    Path corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    LOGGER.info("Running AIP corruption risk assessment tests under storage {}", basePath);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testAIPCorruption() throws RODAException {
    String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_CORRUPTED),
      RodaConstants.ADMIN);

    Job job = TestsHelper.executeJob(AIPCorruptionRiskAssessmentPlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, Collections.singletonList(aipId)));

    List<Report> jobReports = TestsHelper.getJobReports(index, job, false);
    int count = StringUtils.countMatches(jobReports.get(0).getPluginDetails(), "<div class=\"entry level_error\">");
    index.commit(RiskIncidence.class);
    long incidences = index.count(RiskIncidence.class, Filter.ALL);

    // 3 errors: 1 checksum checking error, 1 file without premis, 1 premis
    // without file Assert.assertEquals(count, 3);
    Assert.assertEquals(incidences, 2);
    Assert.assertEquals(jobReports.get(0).getPluginState(), PluginState.FAILURE);
  }

  @Test
  public void testFileRemovedFromStorage() throws RequestNotValidException, AuthorizationDeniedException,
    ValidationException, AlreadyExistsException, NotFoundException, GenericException {
    String aipId = IdUtils.createUUID();
    AIP aip = model.createAIP(aipId, corporaService,
            DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, "AIP_4"),
            RodaConstants.ADMIN);

    File file = model.retrieveFile(aip.getId(), aip.getRepresentations().get(0).getId(), List.of(), "2012-roda-promo-en.pdf");
    Path path = model.getDirectAccess(file).getPath();

    Assert.assertTrue(path.toFile().exists());

    path.toFile().delete();
    Assert.assertFalse(path.toFile().exists());
  }
}
