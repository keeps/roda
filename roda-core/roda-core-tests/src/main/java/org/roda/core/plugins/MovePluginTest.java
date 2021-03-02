/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.nio.file.Path;
import java.util.Collections;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.plugins.internal.MovePlugin;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MovePluginTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(MovePluginTest.class);

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
      deployPluginManager, deployDefaultResources, false);
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
  public void testMoveFileToRootAndBackToFolder() throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException {

    // create AIP and rep
    AIP aip = model.createAIP(null, "", new Permissions(), RodaConstants.ADMIN);
    Representation representation = model.createRepresentation(aip.getId(), IdUtils.createUUID(), true, "", true,
      RodaConstants.ADMIN);

    // create 'folderA' and 'folderA/fileAA'
    File folderA = model.createFile(aip.getId(), representation.getId(), Collections.emptyList(), "", "folderA", true);
    File fileAA = model.createFile(aip.getId(), representation.getId(), Collections.singletonList("folderA"), "fileAA",
      new StringContentPayload("fileAA"), true);
    index.commit(IndexedFile.class);

    // move fileAA to root
    Job jobFileAAtoRoot = TestsHelper.executeJob(MovePlugin.class, Collections.EMPTY_MAP, PluginType.INTERNAL,
      SelectedItemsList.create(IndexedFile.class, IdUtils.getFileId(fileAA)));
    TestsHelper.getJobReports(index, jobFileAAtoRoot, true);
    index.commit(IndexedFile.class);

    // test if fileA is now in the root
    File fileAAinRoot = model.retrieveFile(aip.getId(), representation.getId(), Collections.emptyList(), "fileAA");
    AssertJUnit.assertNotNull("Failed move fileAA to root", fileAAinRoot);
    AssertJUnit.assertNotNull("Failed to move fileAA to root in index",
      index.retrieve(IndexedFile.class, IdUtils.getFileId(fileAAinRoot), Collections.emptyList()));

    // move fileAA back to folderA/fileAA
    Job jobFileAAtoFolderA = TestsHelper.executeJob(MovePlugin.class,
      Collections.singletonMap(RodaConstants.PLUGIN_PARAMS_ID, IdUtils.getFileId(folderA)), PluginType.INTERNAL,
      SelectedItemsList.create(IndexedFile.class, IdUtils.getFileId(fileAAinRoot)));
    TestsHelper.getJobReports(index, jobFileAAtoFolderA, true);
    index.commit(IndexedFile.class);

    // test if fileAA is now in folderA
    File fileAAinFolderA = model.retrieveFile(aip.getId(), representation.getId(), Collections.singletonList("folderA"),
      "fileAA");
    AssertJUnit.assertNotNull("Failed move fileAA back to folderA", fileAAinFolderA);
    AssertJUnit.assertNotNull("Failed to move fileAA back to folderA in index",
      index.retrieve(IndexedFile.class, IdUtils.getFileId(fileAAinFolderA), Collections.emptyList()));
  }
}
