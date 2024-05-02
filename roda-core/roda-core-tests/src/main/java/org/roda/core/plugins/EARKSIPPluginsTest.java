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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.ingest.EARKSIP2ToAIPPlugin;
import org.roda.core.plugins.base.ingest.EARKSIPToAIPPlugin;
import org.roda.core.plugins.base.maintenance.FixAncestorsPlugin;
import org.roda.core.plugins.base.maintenance.reindex.ReindexAIPPlugin;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

import jersey.repackaged.com.google.common.collect.Lists;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class EARKSIPPluginsTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(EARKSIPPluginsTest.class);

  private static final int CORPORA_FILES_COUNT = 4;
  private static final int CORPORA_FOLDERS_COUNT = 2;
  private Path basePath;

  private ModelService model;
  private IndexService index;

  private Path corporaPath;

  private static TransferredResource createIngestCorpora(Path corporaPath, IndexService index, String sipFileInCorpora,
    String renameSipFileTo) throws IOException, NotFoundException, GenericException, IsStillUpdatingException,
    AlreadyExistsException, AuthorizationDeniedException {
    TransferredResourcesScanner f = RodaCoreFactory.getTransferredResourcesScanner();
    Path sip = corporaPath.resolve(CorporaConstants.SIP_FOLDER).resolve(sipFileInCorpora);
    String filename = renameSipFileTo == null ? sipFileInCorpora : renameSipFileTo;
    if (!f.fileExists(filename)) {
      f.createFile(null, filename, Files.newInputStream(sip));
    }
    f.updateTransferredResources(Optional.empty(), true);
    index.commit(TransferredResource.class);
    return index.retrieve(TransferredResource.class, IdUtils.createUUID(filename), new ArrayList<>());
  }

  public static TransferredResource createIngestCorpora(Path corporaPath, IndexService index)
    throws IOException, NotFoundException, GenericException, IsStillUpdatingException, AlreadyExistsException,
    AuthorizationDeniedException {
    return createIngestCorpora(corporaPath, index, CorporaConstants.EARK_SIP_204, null);
  }

  public static TransferredResource createIngestUpdateCorpora(Path corporaPath, IndexService index,
    String renameSipFileTo) throws IOException, NotFoundException, GenericException, IsStillUpdatingException,
    AlreadyExistsException, AuthorizationDeniedException {
    return createIngestCorpora(corporaPath, index, CorporaConstants.EARK_SIP_204_UPDATE, renameSipFileTo);
  }

  public static TransferredResource createIngestCorpora(Path corporaPath, IndexService index, String sipName)
    throws AuthorizationDeniedException, AlreadyExistsException, NotFoundException, IOException,
    IsStillUpdatingException, GenericException {
    return createIngestCorpora(corporaPath, index, sipName, null);
  }

  protected static <T extends Plugin<TransferredResource>> AIP ingestCorpora(Class<T> pluginClass, ModelService model,
    IndexService index, TransferredResource transferredResource, boolean failIfReportNotSucceeded)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException {
    String aipType = RodaConstants.AIP_TYPE_MIXED;
    AIP root = model.createAIP(null, aipType, new Permissions(), RodaConstants.ADMIN);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    parameters.put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, "true");

    Assert.assertNotNull(transferredResource);

    Job job = TestsHelper.executeJob(pluginClass, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));

    TestsHelper.getJobReports(index, job, failIfReportNotSucceeded);
    index.commitAIPs();

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, root.getId())), null, new Sublist(0, 10),
      new ArrayList<>());

    Assert.assertEquals(find.getTotalCount(), 1L);
    IndexedAIP indexedAIP = find.getResults().getFirst();

    return model.retrieveAIP(indexedAIP.getId());
  }

  protected static <T extends Plugin<TransferredResource>> AIP ingestCorpora(Class<T> pluginClass, ModelService model,
    IndexService index, Path corporaPath, boolean failIfReportNotSucceeded)
    throws RequestNotValidException, NotFoundException, GenericException, AlreadyExistsException,
    AuthorizationDeniedException, IOException, IsStillUpdatingException {
    String aipType = RodaConstants.AIP_TYPE_MIXED;
    AIP root = model.createAIP(null, aipType, new Permissions(), RodaConstants.ADMIN);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    parameters.put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, "true");

    TransferredResource transferredResource = createIngestCorpora(corporaPath, index);
    Assert.assertNotNull(transferredResource);

    Job job = TestsHelper.executeJob(pluginClass, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));

    TestsHelper.getJobReports(index, job, failIfReportNotSucceeded);
    index.commitAIPs();

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, root.getId())), null, new Sublist(0, 10),
      new ArrayList<>());

    Assert.assertEquals(find.getTotalCount(), 1L);
    IndexedAIP indexedAIP = find.getResults().getFirst();

    return model.retrieveAIP(indexedAIP.getId());
  }

  protected static <T extends Plugin<TransferredResource>> AIP ingestCorpora(Class<T> pluginClass, ModelService model,
    IndexService index, TransferredResource transferredResource) throws AuthorizationDeniedException,
    RequestNotValidException, AlreadyExistsException, NotFoundException, GenericException {
    return ingestCorpora(pluginClass, model, index, transferredResource, false);
  }

  protected static <T extends Plugin<TransferredResource>> AIP ingestCorpora(Class<T> pluginClass, ModelService model,
    IndexService index, Path corporaPath) throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, IOException, IsStillUpdatingException {
    TransferredResource ingestCorpora = createIngestCorpora(corporaPath, index);
    return ingestCorpora(pluginClass, model, index, ingestCorpora, false);
  }

  @BeforeClass
  public void setUp() throws IOException, URISyntaxException {
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

    URL corporaURL = EARKSIPPluginsTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());

    LOGGER.info("Running E-ARK SIP plugins tests under storage {}", basePath);
  }

  @AfterClass
  public void tearDown() throws IOException, NotFoundException, GenericException {
    IndexTestUtils.resetIndex();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @AfterMethod
  public void cleanUp() throws RODAException {
    index.execute(IndexedAIP.class, Filter.ALL, new ArrayList<>(), item -> {
      try {
        model.deleteAIP(item.getId());
      } catch (NotFoundException e) {
        // do nothing
      }
    }, e -> Assert.fail("Error cleaning up", e));

    TestsHelper.releaseAllLocks();
  }

  private AIP ingestUpdateCorpora(AIP aip) throws RequestNotValidException, NotFoundException, GenericException,
    AuthorizationDeniedException, IOException, IsStillUpdatingException, AlreadyExistsException {

    TransferredResource transferredResource = createIngestUpdateCorpora(corporaPath, index, null);
    Assert.assertNotNull(transferredResource);

    Job job = TestsHelper.executeJob(EARKSIP2ToAIPPlugin.class, new HashMap<>(), PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));

    TestsHelper.getJobReports(index, job, true);
    index.commitAIPs();

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_ID, aip.getId())), null, new Sublist(0, 10),
      new ArrayList<>());

    Assert.assertEquals(find.getTotalCount(), 1L);
    IndexedAIP indexedAIP = find.getResults().getFirst();

    return model.retrieveAIP(indexedAIP.getId());
  }

  @Test
  public void testIngestEARKSIP() throws IOException, RODAException {
    AIP aip = ingestCorpora(EARKSIP2ToAIPPlugin.class, model, index, corporaPath);
    Assert.assertEquals(aip.getRepresentations().size(), 1);

    CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
      aip.getRepresentations().getFirst().getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles,
      Lists.newArrayList(allFiles).stream().filter(OptionalWithCause::isPresent).map(OptionalWithCause::get).toList());

    // All folders and files
    Assert.assertEquals(reusableAllFiles.size(), CORPORA_FOLDERS_COUNT + CORPORA_FILES_COUNT);
  }

  @Test
  public void testIngestAndUpdateEARKSIP() throws IOException, RODAException {
    AIP aip = ingestCorpora(EARKSIP2ToAIPPlugin.class, model, index, corporaPath);
    Assert.assertEquals(aip.getRepresentations().size(), 1);
    aip.setState(AIPState.ACTIVE);
    model.updateAIP(aip, CorporaConstants.EARK_SIP_UPDATE_USER);
    index.commitAIPs();

    TestsHelper.releaseAllLocks();

    AIP aipUpdated = ingestUpdateCorpora(aip);
    Assert.assertEquals(aipUpdated.getRepresentations().size(), 2);
    Assert.assertEquals(aipUpdated.getIngestSIPIds().size(), 2);
  }

  @Test
  public void testIngestEARKSIP2WithOtherRepresentationType()
    throws AuthorizationDeniedException, RequestNotValidException, AlreadyExistsException, NotFoundException,
    IOException, IsStillUpdatingException, GenericException {
    String expectedRepresentationType = "SVG";
    TransferredResource ingestCorpora = createIngestCorpora(corporaPath, index,
      "e-ark-sip-2.1.0-with-custom-representation-type.zip");

    AIP aip = ingestCorpora(EARKSIP2ToAIPPlugin.class, model, index, ingestCorpora);
    Assert.assertEquals(aip.getRepresentations().size(), 1);

    Representation representation = aip.getRepresentations().getFirst();
    String type = representation.getType();
    Assert.assertEquals(type, expectedRepresentationType);
  }

  private List<String> createCorporaAncestors()
    throws IOException, GenericException, IsStillUpdatingException, AuthorizationDeniedException {
    TransferredResourcesScanner f = RodaCoreFactory.getTransferredResourcesScanner();
    List<String> resultIDs = new ArrayList<>();

    Path sipFolder = corporaPath.resolve(CorporaConstants.SIP_FOLDER).resolve(CorporaConstants.ANCESTOR_SIP_FOLDER);
    try (Stream<Path> stream = Files.walk(sipFolder)) {
      stream.forEach(filePath -> {
        if (FSUtils.isFile(filePath)) {
          try {
            TransferredResource tr = f.createFile(null, filePath.getFileName().toString(),
              Files.newInputStream(filePath));
            resultIDs.add(tr.getUUID());
          } catch (GenericException | NotFoundException | AlreadyExistsException | IOException
            | AuthorizationDeniedException e) {
            LOGGER.error("Error creating file: {}", filePath, e);
          }
        }
      });
    }

    f.updateTransferredResources(Optional.empty(), true);
    index.commit(TransferredResource.class);

    return resultIDs;
  }

  private void ingestCorporaAncestors() throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, IOException, IsStillUpdatingException {
    String aipType = RodaConstants.AIP_TYPE_MIXED;
    AIP root = model.createAIP(null, aipType, new Permissions(), RodaConstants.ADMIN);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());

    List<String> transferredResourcesIDs = createCorporaAncestors();
    Assert.assertNotNull(transferredResourcesIDs);

    Job ingestJob = TestsHelper.executeJob(EARKSIPToAIPPlugin.class, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResourcesIDs));

    TestsHelper.getJobReports(index, ingestJob, true);
    index.commitAIPs();

    Map<String, String> fixAncestorsParameters = new HashMap<>();
    fixAncestorsParameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    fixAncestorsParameters.put(RodaConstants.PLUGIN_PARAMS_OTHER_JOB_ID, ingestJob.getId());

    Job fixAncestorsJob = TestsHelper.executeJob(FixAncestorsPlugin.class, fixAncestorsParameters, PluginType.MISC,
      new SelectedItemsNone<>());

    TestsHelper.getJobReports(index, fixAncestorsJob, true);
    index.commitAIPs();

    TestsHelper.releaseAllLocks();

    // 20161202 hsilva: somehow a reindex is needed for getting ancestors
    // correctly calculated
    TestsHelper.executeJob(ReindexAIPPlugin.class, fixAncestorsParameters, PluginType.MISC,
      SelectedItemsAll.create(AIP.class));
    index.commitAIPs();

    IndexResult<IndexedAIP> findAllAIP = index.find(IndexedAIP.class, Filter.ALL, null, new Sublist(0, 12),
      new ArrayList<>());
    Assert.assertEquals(findAllAIP.getTotalCount(), 12L);

    IndexResult<IndexedAIP> findRootChildren = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, root.getId())), null, new Sublist(0, 2),
      new ArrayList<>());
    Assert.assertEquals(findRootChildren.getTotalCount(), 2L);

    IndexResult<IndexedAIP> findSpecificAIP = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.INGEST_SIP_IDS, "026106")), null, new Sublist(0, 1),
      new ArrayList<>());
    Assert.assertEquals(findSpecificAIP.getTotalCount(), 1L);
    IndexedAIP specificAIP = findSpecificAIP.getResults().getFirst();
    Assert.assertEquals(specificAIP.getAncestors().size(), 4);
  }

  @Test
  public void testIngestAncestors() throws IOException, RODAException {
    ingestCorporaAncestors();
  }
}
