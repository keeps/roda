/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.plugins.ingest.BagitToAIPPlugin;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

import jersey.repackaged.com.google.common.collect.Lists;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class BagitSIPPluginsTest {

  private static final int CORPORA_FILES_COUNT = 4;
  private static final int CORPORA_FOLDERS_COUNT = 2;
  private static Path basePath;

  private static ModelService model;
  private static IndexService index;

  private static Path corporaPath;

  private static final Logger LOGGER = LoggerFactory.getLogger(BagitSIPPluginsTest.class);

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
      deployPluginManager, deployDefaultResources, false);
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = BagitSIPPluginsTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());

    LOGGER.info("Running internal plugins tests under storage {}", basePath);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  private TransferredResource createCorpora()
    throws IOException, NotFoundException, GenericException, RequestNotValidException, AlreadyExistsException,
    AuthorizationDeniedException {
    TransferredResourcesScanner f = RodaCoreFactory.getTransferredResourcesScanner();

    Path sip = corporaPath.resolve(CorporaConstants.SIP_FOLDER).resolve(CorporaConstants.BAGIT_SIP);

    TransferredResource transferredResourceCreated = f.createFile(null, CorporaConstants.BAGIT_SIP,
      Files.newInputStream(sip));

    index.commit(TransferredResource.class);
    return index.retrieve(TransferredResource.class, transferredResourceCreated.getUUID(), new ArrayList<>());
  }

  private AIP ingestCorpora() throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, IOException {
    String parentId = null;
    String aipType = RodaConstants.AIP_TYPE_MIXED;

    AIP root = model.createAIP(parentId, aipType, new Permissions(), RodaConstants.ADMIN);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    parameters.put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, "true");

    TransferredResource transferredResource = createCorpora();
    AssertJUnit.assertNotNull(transferredResource);

    Job job = TestsHelper.executeJob(BagitToAIPPlugin.class, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));

    TestsHelper.getJobReports(index, job, true);

    index.commitAIPs();

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, root.getId())), null, new Sublist(0, 10),
      new ArrayList<>());

    AssertJUnit.assertEquals(1L, find.getTotalCount());
    IndexedAIP indexedAIP = find.getResults().get(0);

    return model.retrieveAIP(indexedAIP.getId());
  }

  @Test
  public void testIngestBagitSIP() throws IOException, RODAException {
    AIP aip = ingestCorpora();
    AssertJUnit.assertEquals(1, aip.getRepresentations().size());

    CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
      aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles,
      Lists.newArrayList(allFiles).stream().filter(f -> f.isPresent()).map(f -> f.get()).collect(Collectors.toList()));

    // All folders and files
    AssertJUnit.assertEquals(CORPORA_FOLDERS_COUNT + CORPORA_FILES_COUNT, reusableAllFiles.size());
  }

}
