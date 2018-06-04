/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.nio.file.Path;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.plugins.reindex.ReindexFilePlugin;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ReindexFilePluginTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReindexFilePluginTest.class);

  private static Path basePath;

  private static ModelService model;
  private static IndexService index;

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
      deployPluginManager, deployDefaultResources);
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    LOGGER.info("Running '{}' tests under storage {}", getClass().getName(), basePath);
  }

  @AfterClass
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testReindexWithEmptyRepresentation() throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException {
    AIP aip = model.createAIP(null, "", new Permissions(), RodaConstants.ADMIN);
    model.createRepresentation(aip.getId(), IdUtils.createUUID(), true, "", true, RodaConstants.ADMIN);
    Job job = TestsHelper.executeJob(ReindexFilePlugin.class, PluginType.MISC, SelectedItemsAll.create(File.class));
    TestsHelper.getJobReports(index, job, true);
  }

}
