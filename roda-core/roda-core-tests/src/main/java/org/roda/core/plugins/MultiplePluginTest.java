package org.roda.core.plugins;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.plugins.DummyPlugin;
import org.roda.core.plugins.plugins.MultiplePlugin;
import org.roda.core.plugins.plugins.characterization.PremisSkeletonPlugin;
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

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class MultiplePluginTest {

  private static Path basePath;

  private static ModelService model;
  private static IndexService index;
  private static StorageService corporaService;
  private static Path corporaPath;

  private static final Logger LOGGER = LoggerFactory.getLogger(MultiplePluginTest.class);

  @BeforeMethod
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources);
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = MultiplePluginTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    LOGGER.info("Running AIP corruption risk assessment tests under storage {}", basePath);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testMutiplePlugin() throws RODAException {
    String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_CORRUPTED),
      RodaConstants.ADMIN);

    Job job = TestsHelper.executeJob(MultiplePlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, Arrays.asList(aipId)));

    TestsHelper.executeJob(PremisSkeletonPlugin.class, PluginType.AIP_TO_AIP, new SelectedItemsFilter<IndexedAIP>(Filter.ALL,IndexedAIP.class.getName(),null));
    List<Report> jobReports = TestsHelper.getJobReports(index, job, false);

    // 3 errors: 1 checksum checking error, 1 file without premis, 1 premis
    // without file
    Assert.assertEquals(jobReports.get(0).getReports().size(), 2);
    Assert.assertEquals(jobReports.get(0).getTotalSteps().intValue(), 2);
    Assert.assertEquals(jobReports.get(0).getCompletionPercentage().intValue(), 100);
  }
}
