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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrServerException;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
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
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.ModelServiceTest;
import org.roda.core.plugins.plugins.ingest.BagitToAIPPlugin;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

import jersey.repackaged.com.google.common.collect.Lists;

public class BagitSIPPluginsTest {

  private static final int CORPORA_FILES_COUNT = 4;
  private static final int CORPORA_FOLDERS_COUNT = 2;
  private static final String CORPORA_PDF = "test.docx";
  private static final String CORPORA_TEST1 = "test1";
  private static final String CORPORA_TEST1_TXT = "test1.txt";
  private static final int GENERATED_FILE_SIZE = 100;
  private static final int AUTO_COMMIT_TIMEOUT = 2000;
  private static Path basePath;
  private static Path logPath;

  private static ModelService model;
  private static IndexService index;

  private static Path corporaPath;
  private static StorageService corporaService;

  private static final Logger logger = LoggerFactory.getLogger(ModelServiceTest.class);

  @Before
  public void setUp() throws Exception {

    basePath = Files.createTempDirectory("indexTests");
    System.setProperty("roda.home", basePath.toString());

    boolean deploySolr = true;
    boolean deployLdap = false;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager);
    logPath = RodaCoreFactory.getLogPath();
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = BagitSIPPluginsTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    logger.info("Running internal plugins tests under storage {}", basePath);
  }

  @After
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  private TransferredResource createCorpora() throws InterruptedException, IOException, FileAlreadyExistsException,
    NotFoundException, GenericException, RequestNotValidException {
    TransferredResourcesScanner f = RodaCoreFactory.getTransferredResourcesScanner();

    Path sip = corporaPath.resolve(CorporaConstants.SIP_FOLDER).resolve(CorporaConstants.BAGIT_SIP);

    TransferredResource transferredResourceCreated = f.createFile(null, CorporaConstants.BAGIT_SIP,
      Files.newInputStream(sip));

    // TODO check if 4 times is the expected
    // Mockito.verify(observer, Mockito.times(4));

    index.commit(TransferredResource.class);

    TransferredResource transferredResource = index.retrieve(TransferredResource.class,
      transferredResourceCreated.getUUID());
    return transferredResource;
  }

  private void assertReports(List<Report> reports) {
    for (Report report : reports) {
      Assert.assertThat(report.getReports().get(0).getPluginState(), Matchers.is(PluginState.SUCCESS));
    }
  }

  private AIP ingestCorpora() throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, InvalidParameterException, InterruptedException, IOException,
    FileAlreadyExistsException, SolrServerException {
    String parentId = null;
    String aipType = RodaConstants.AIP_TYPE_MIXED;

    AIP root = model.createAIP(parentId, aipType, new Permissions());

    Plugin<TransferredResource> plugin = new BagitToAIPPlugin();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    plugin.setParameterValues(parameters);

    TransferredResource transferredResource = createCorpora();
    Assert.assertNotNull(transferredResource);

    // FIXME 20160623 hsilva: passing by null just to make code compiling
    RodaCoreFactory.getPluginOrchestrator().runPluginOnTransferredResources(null, plugin,
      Arrays.asList(transferredResource.getUUID()));
    // assertReports(reports);

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, root.getId())), null, new Sublist(0, 10));

    Assert.assertEquals(1L, find.getTotalCount());
    IndexedAIP indexedAIP = find.getResults().get(0);

    AIP aip = model.retrieveAIP(indexedAIP.getId());
    return aip;
  }

  @Test
  public void testIngestBagitSIP() throws IOException, InterruptedException, RODAException, SolrServerException {
    AIP aip = ingestCorpora();
    Assert.assertEquals(1, aip.getRepresentations().size());

    CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
      aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles,
      Lists.newArrayList(allFiles).stream().filter(f -> f.isPresent()).map(f -> f.get()).collect(Collectors.toList()));

    // All folders and files
    Assert.assertEquals(CORPORA_FOLDERS_COUNT + CORPORA_FILES_COUNT, reusableAllFiles.size());
  }

}
