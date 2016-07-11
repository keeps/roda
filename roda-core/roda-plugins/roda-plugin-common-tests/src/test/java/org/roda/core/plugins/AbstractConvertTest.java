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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
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
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.plugins.ingest.TransferredResourceToAIPPlugin;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractConvertTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConvertTest.class);

  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static int numberOfConvertableFiles = 17;
  private static Path corporaPath;

  @Before
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(this.getClass(), true);

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager);
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = AbstractConvertTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());

    LOGGER.info("Running internal convert plugins tests under storage {}", basePath);
  }

  @After
  public void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  public List<TransferredResource> createCorpora(int corporaId)
    throws InterruptedException, IOException, FileAlreadyExistsException, NotFoundException, GenericException,
    AlreadyExistsException, SolrServerException, IsStillUpdatingException {
    TransferredResourcesScanner f = RodaCoreFactory.getTransferredResourcesScanner();

    List<TransferredResource> resources = new ArrayList<TransferredResource>();

    String[] aips = {CorporaConstants.SOURCE_AIP_CONVERTER_1, CorporaConstants.SOURCE_AIP_CONVERTER_2,
      CorporaConstants.SOURCE_AIP_CONVERTER_3};

    String[] reps = {CorporaConstants.REPRESENTATION_CONVERTER_ID_1, CorporaConstants.REPRESENTATION_CONVERTER_ID_2,
      CorporaConstants.REPRESENTATION_CONVERTER_ID_3};

    Path corpora = corporaPath.resolve(RodaConstants.STORAGE_CONTAINER_AIP).resolve(aips[corporaId])
      .resolve(RodaConstants.STORAGE_DIRECTORY_REPRESENTATIONS).resolve(reps[corporaId])
      .resolve(RodaConstants.STORAGE_DIRECTORY_DATA);

    String transferredResourceId = "testt";
    FSUtils.copy(corpora, f.getBasePath().resolve(transferredResourceId), true);

    f.updateAllTransferredResources(null, true);

    index.commit(TransferredResource.class);

    resources.add(
      index.retrieve(TransferredResource.class, UUID.nameUUIDFromBytes(transferredResourceId.getBytes()).toString()));
    return resources;
  }

  public AIP ingestCorpora(int corporaId) throws RequestNotValidException, NotFoundException, GenericException,
    AlreadyExistsException, AuthorizationDeniedException, InvalidParameterException, InterruptedException, IOException,
    FileAlreadyExistsException, SolrServerException, IsStillUpdatingException {
    String parentId = null;
    String aipType = RodaConstants.AIP_TYPE_MIXED;
    AIP root = model.createAIP(parentId, aipType, new Permissions());

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());

    List<TransferredResource> transferredResources = new ArrayList<TransferredResource>();
    transferredResources = createCorpora(corporaId);

    Assert.assertEquals(1, transferredResources.size());

    TestsHelper.executeJob(TransferredResourceToAIPPlugin.class, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class,
        transferredResources.stream().map(tr -> tr.getUUID()).collect(Collectors.toList())));

    index.commitAIPs();

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, root.getId())), null, new Sublist(0, 10));

    Assert.assertEquals(1L, find.getTotalCount());
    IndexedAIP indexedAIP = find.getResults().get(0);

    return model.retrieveAIP(indexedAIP.getId());
  }

  public ModelService getModel() {
    return model;
  }

  public int getNumberOfConvertableFiles() {
    return numberOfConvertableFiles;
  }

  public Path getCorporaPath() {
    return corporaPath;
  }

}
