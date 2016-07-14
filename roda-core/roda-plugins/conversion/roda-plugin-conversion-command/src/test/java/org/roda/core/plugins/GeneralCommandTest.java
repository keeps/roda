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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrServerException;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.monitor.TransferredResourcesScanner;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sublist.Sublist;
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
import org.roda.core.data.v2.index.SelectedItemsAll;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.plugins.conversion.GeneralCommandConvertPlugin;
import org.roda.core.plugins.plugins.ingest.TransferredResourceToAIPPlugin;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

import jersey.repackaged.com.google.common.collect.Lists;

@Test(groups = {"all", "travis"})
public class GeneralCommandTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(GeneralCommandTest.class);

  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static int numberOfConvertableFiles = 17;
  private static Path corporaPath;

  @BeforeMethod
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(this.getClass(), true);

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

    URL corporaURL = getClass().getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());

    LOGGER.info("Running internal convert plugins tests under storage {}", basePath);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  public List<TransferredResource> createCorpora() throws InterruptedException, IOException, FileAlreadyExistsException,
    NotFoundException, GenericException, AlreadyExistsException, SolrServerException, IsStillUpdatingException {
    TransferredResourcesScanner f = RodaCoreFactory.getTransferredResourcesScanner();

    List<TransferredResource> resources = new ArrayList<TransferredResource>();

    Path corpora = corporaPath.resolve(RodaConstants.STORAGE_CONTAINER_AIP)
      .resolve(CorporaConstants.SOURCE_AIP_CONVERTER_1).resolve(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS)
      .resolve(CorporaConstants.REPRESENTATION_CONVERTER_ID_1).resolve(RodaConstants.STORAGE_DIRECTORY_DATA);

    String transferredResourceId = "testt";
    FSUtils.copy(corpora, f.getBasePath().resolve(transferredResourceId), true);

    f.updateAllTransferredResources(null, true);
    index.commit(TransferredResource.class);

    resources.add(
      index.retrieve(TransferredResource.class, UUID.nameUUIDFromBytes(transferredResourceId.getBytes()).toString()));
    return resources;
  }

  public AIP ingestCorpora() throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, InvalidParameterException, InterruptedException, IOException,
    FileAlreadyExistsException, SolrServerException, IsStillUpdatingException {
    String parentId = null;
    String aipType = RodaConstants.AIP_TYPE_MIXED;
    AIP root = model.createAIP(parentId, aipType, new Permissions());

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());

    List<TransferredResource> transferredResources = new ArrayList<TransferredResource>();
    transferredResources = createCorpora();

    AssertJUnit.assertEquals(1, transferredResources.size());

    TestsHelper.executeJob(TransferredResourceToAIPPlugin.class, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class,
        transferredResources.stream().map(tr -> tr.getUUID()).collect(Collectors.toList())));

    index.commitAIPs();

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, root.getId())), null, new Sublist(0, 10));

    AssertJUnit.assertEquals(1L, find.getTotalCount());
    IndexedAIP indexedAIP = find.getResults().get(0);

    return model.retrieveAIP(indexedAIP.getId());
  }

  @Test
  public void testGeneralCommandPlugin() throws RODAException, FileAlreadyExistsException, InterruptedException,
    IOException, SolrServerException, IsStillUpdatingException {
    AIP aip = ingestCorpora();

    CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
      aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles,
      Lists.newArrayList(allFiles).stream().filter(f -> f.isPresent()).map(f -> f.get()).collect(Collectors.toList()));

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_INPUT_FORMAT, "png");
    parameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT, "tiff");
    parameters.put(RodaConstants.PLUGIN_PARAMS_COMMAND_ARGUMENTS,
      "/usr/bin/convert -regard-warnings {input_file} {output_file}");

    Job job = TestsHelper.executeJob(GeneralCommandConvertPlugin.class, parameters, PluginType.AIP_TO_AIP,
      SelectedItemsAll.create(Representation.class));

    TestsHelper.testJobReports(index, job);

    aip = model.retrieveAIP(aip.getId());
    AssertJUnit.assertEquals(2, aip.getRepresentations().size());

    CloseableIterable<OptionalWithCause<File>> newAllFiles = model.listFilesUnder(aip.getId(),
      aip.getRepresentations().get(1).getId(), true);
    List<File> newReusableAllFiles = new ArrayList<>();
    Iterables.addAll(newReusableAllFiles, Lists.newArrayList(newAllFiles).stream().filter(f -> f.isPresent())
      .map(f -> f.get()).collect(Collectors.toList()));

    AssertJUnit.assertEquals(numberOfConvertableFiles, newReusableAllFiles.size());

    int changedCounter = 0;

    for (File f : reusableAllFiles) {
      if (f.getId().matches(".*[.](png)$")) {
        changedCounter++;
        String filename = f.getId().substring(0, f.getId().lastIndexOf('.'));
        AssertJUnit.assertEquals(1,
          newReusableAllFiles.stream().filter(o -> o.getId().equals(filename + ".tiff")).count());
      }
    }

    AssertJUnit.assertEquals(changedCounter,
      newReusableAllFiles.stream().filter(o -> o.getId().matches(".*[.]tiff$")).count());
  }

}
