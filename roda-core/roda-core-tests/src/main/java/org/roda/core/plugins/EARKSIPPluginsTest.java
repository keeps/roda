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
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InvalidParameterException;
import org.roda.core.data.exceptions.IsStillUpdatingException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
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
import org.roda.core.plugins.plugins.base.FixAncestorsPlugin;
import org.roda.core.plugins.plugins.ingest.EARKSIPToAIPPlugin;
import org.roda.core.plugins.plugins.reindex.ReindexAIPPlugin;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

import jersey.repackaged.com.google.common.collect.Lists;

@Test(groups = {"all", "travis"})
public class EARKSIPPluginsTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(EARKSIPPluginsTest.class);

  private static final int CORPORA_FILES_COUNT = 4;
  private static final int CORPORA_FOLDERS_COUNT = 2;
  private static Path basePath;

  private static ModelService model;
  private static IndexService index;

  private static Path corporaPath;
  private static String aipCreator = "admin";

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

    URL corporaURL = EARKSIPPluginsTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());

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
    });
  }

  private TransferredResource createCorpora() throws InterruptedException, IOException, FileAlreadyExistsException,
    NotFoundException, GenericException, RequestNotValidException, IsStillUpdatingException, AlreadyExistsException {
    TransferredResourcesScanner f = RodaCoreFactory.getTransferredResourcesScanner();

    Path sip = corporaPath.resolve(CorporaConstants.SIP_FOLDER).resolve(CorporaConstants.EARK_SIP);

    f.createFile(null, CorporaConstants.EARK_SIP, Files.newInputStream(sip));

    f.updateTransferredResources(Optional.empty(), true);
    index.commit(TransferredResource.class);

    TransferredResource transferredResource = index.retrieve(TransferredResource.class,
      UUID.nameUUIDFromBytes(CorporaConstants.EARK_SIP.getBytes()).toString(), new ArrayList<>());
    return transferredResource;
  }

  private AIP ingestCorpora() throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, InvalidParameterException, InterruptedException, IOException,
    FileAlreadyExistsException, SolrServerException, IsStillUpdatingException {
    String parentId = null;
    String aipType = RodaConstants.AIP_TYPE_MIXED;
    AIP root = model.createAIP(parentId, aipType, new Permissions(), aipCreator);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    parameters.put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, "true");

    TransferredResource transferredResource = createCorpora();
    Assert.assertNotNull(transferredResource);

    Job job = TestsHelper.executeJob(EARKSIPToAIPPlugin.class, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));

    TestsHelper.getJobReports(index, job, true);

    index.commitAIPs();

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, root.getId())), null, new Sublist(0, 10),
      new ArrayList<>());

    Assert.assertEquals(find.getTotalCount(), 1L);
    IndexedAIP indexedAIP = find.getResults().get(0);

    AIP aip = model.retrieveAIP(indexedAIP.getId());
    return aip;
  }

  @Test
  public void testIngestEARKSIP()
    throws IOException, InterruptedException, RODAException, SolrServerException, IsStillUpdatingException {
    AIP aip = ingestCorpora();
    Assert.assertEquals(aip.getRepresentations().size(), 1);

    CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
      aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles,
      Lists.newArrayList(allFiles).stream().filter(f -> f.isPresent()).map(f -> f.get()).collect(Collectors.toList()));

    // All folders and files
    Assert.assertEquals(reusableAllFiles.size(), CORPORA_FOLDERS_COUNT + CORPORA_FILES_COUNT);
  }

  private List<String> createCorporaAncestors() throws InterruptedException, IOException, NotFoundException,
    GenericException, RequestNotValidException, IsStillUpdatingException, AlreadyExistsException {
    TransferredResourcesScanner f = RodaCoreFactory.getTransferredResourcesScanner();
    List<String> resultIDs = new ArrayList<>();

    Path sipFolder = corporaPath.resolve(CorporaConstants.SIP_FOLDER).resolve(CorporaConstants.ANCESTOR_SIP_FOLDER);
    Files.walk(sipFolder).forEach(filePath -> {
      if (FSUtils.isFile(filePath)) {
        try {
          TransferredResource tr = f.createFile(null, filePath.getFileName().toString(),
            Files.newInputStream(filePath));
          resultIDs.add(tr.getUUID());
        } catch (GenericException | RequestNotValidException | NotFoundException | AlreadyExistsException
          | IOException e) {
          LOGGER.error("Error creating file: " + filePath, e);
        }
      }
    });

    f.updateTransferredResources(Optional.empty(), true);
    index.commit(TransferredResource.class);

    return resultIDs;
  }

  private void ingestCorporaAncestors() throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, InvalidParameterException, InterruptedException, IOException,
    SolrServerException, IsStillUpdatingException {
    String parentId = null;
    String aipType = RodaConstants.AIP_TYPE_MIXED;
    AIP root = model.createAIP(parentId, aipType, new Permissions(), aipCreator);

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
    IndexedAIP specificAIP = findSpecificAIP.getResults().get(0);
    Assert.assertEquals(specificAIP.getAncestors().size(), 4);
  }

  @Test
  public void testIngestAncestors() throws IOException, InterruptedException, RODAException, SolrServerException {
    ingestCorporaAncestors();
  }

}
