/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.plugins.multiple.MultiplePlugin;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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

    RodaCoreFactory.getPluginManager().registerPlugin(new MultiplePlugin());

    URL corporaURL = MultiplePluginTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    LOGGER.info("Running AIP corruption risk assessment tests under storage {}", basePath);
  }

  @AfterClass
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testMultiplePlugin() throws RODAException {
    // generate AIP ID
    final String aipCorporaId = CorporaConstants.SOURCE_AIP_ID_3;
    final String aipId = IdUtils.createUUID();
    final DefaultStoragePath aipPath = DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, aipCorporaId);
    model.createAIP(aipId, corporaService, aipPath, RodaConstants.ADMIN);

    Job job = TestsHelper.executeJob(MultiplePlugin.class, PluginType.MULTI,
      SelectedItemsList.create(AIP.class, Collections.singletonList(aipId)));

    List<Report> jobReports = TestsHelper.getJobReports(index, job, false);

    Assert.assertEquals(job.getJobStats().getSourceObjectsBeingProcessed(), 0);
    Assert.assertEquals(jobReports.get(0).getReports().size(), 3);
    Assert.assertEquals(jobReports.get(0).getTotalSteps().intValue(), 3);
    Assert.assertEquals(jobReports.get(0).getCompletionPercentage().intValue(), 100);
  }
}
