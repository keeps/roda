package org.roda.core.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsAll;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.AbstractConvertPluginDummy;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class AbstractConvertPluginTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(MinimalIngestPluginTest.class);

  private static final int CORPORA_FILES_COUNT = 4;
  private static final int CORPORA_FOLDERS_COUNT = 2;
  private Path basePath;

  private ModelService model;
  private IndexService index;

  private static StorageService corporaService;

  private Path corporaPath;

  private Path tmpDir;

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
      deployPluginManager, deployDefaultResources, true);
    IndexTestUtils.resetIndex();
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();
    RodaCoreFactory.addConfiguration("roda-test.properties");
    URL corporaURL = AbstractConvertPluginTest.class.getResource("/corpora");
    corporaService = new FileStorageService(Paths.get(corporaURL.toURI()));
    corporaPath = Paths.get(corporaURL.toURI());
    FileUtils.deleteDirectory(new File("/tmp/test"));
    tmpDir = Files.createDirectory(Paths.get("/tmp/test"));
    Files.copy(corporaPath.resolve("Media").resolve("example.tiff"), tmpDir.resolve("example.tiff"));
    LOGGER.info("Running Abstract Convert Plugin tests under storage {}", basePath);

  }

  @AfterClass
  public void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @AfterMethod
  public void cleanUp() throws RODAException {
    try {
      Files.deleteIfExists(tmpDir);
    } catch (IOException e) {
      // do nothing.
    }
  }

  /**
   * Test the abstract convert plugin from files in shallow AIP.
   * 
   * @throws RODAException
   *           if some error occurs.
   */
  @Test
  public void testAbstractConvertPluginOnFile() throws RODAException {
    final String aipId = IdUtils.createUUID();

    // create the AIP in Resources
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID_EARK2S),
      RodaConstants.ADMIN);

    index.commitAIPs();
    final Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP, "false");
    parameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT, "txt");

    final Job job = TestsHelper.executeJob(AbstractConvertPluginDummy.class, parameters, PluginType.AIP_TO_AIP,
      SelectedItemsAll.create(org.roda.core.data.v2.ip.File.class));

    index.commitAIPs();

    final Filter filterParentTheAIP = new Filter();
    filterParentTheAIP.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aipId));
    final IndexResult<IndexedRepresentation> indexResult = index.find(IndexedRepresentation.class, filterParentTheAIP, null,
      new Sublist(0, 10), Collections.emptyList());

    Assert.assertEquals(indexResult.getResults().size(), 2);

  }

  /**
   * Test the abstract convert plugin from Representations in shallow AIP.
   *
   * @throws RODAException
   *           if some error occurs.
   */
  @Test
  public void testAbstractConvertPluginOnRepresentation() throws RODAException {
    final String aipId = IdUtils.createUUID();

    // create the AIP in Resources
    model.createAIP(aipId, corporaService,
            DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID_EARK2S),
            RodaConstants.ADMIN);

    index.commitAIPs();
    final Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP, "false");
    parameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT, "txt");

    final Job job = TestsHelper.executeJob(AbstractConvertPluginDummy.class, parameters, PluginType.AIP_TO_AIP,
            SelectedItemsAll.create(Representation.class));

    index.commitAIPs();

    final Filter filterParentTheAIP = new Filter();
    filterParentTheAIP.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aipId));
    final IndexResult<IndexedRepresentation> indexResult = index.find(IndexedRepresentation.class, filterParentTheAIP, null,
            new Sublist(0, 10), Collections.emptyList());

    Assert.assertEquals(indexResult.getResults().size(), 2);

  }

  /**
   * Test the abstract convert plugin from AIP class in shallow AIP.
   *
   * @throws RODAException
   *           if some error occurs.
   */
  @Test
  public void testAbstractConvertPluginOnAIP() throws RODAException {
    final String aipId = IdUtils.createUUID();

    // create the AIP in Resources
    model.createAIP(aipId, corporaService,
            DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID_EARK2S),
            RodaConstants.ADMIN);

    index.commitAIPs();
    final Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_REPRESENTATION_OR_DIP, "false");
    parameters.put(RodaConstants.PLUGIN_PARAMS_OUTPUT_FORMAT, "txt");

    final Job job = TestsHelper.executeJob(AbstractConvertPluginDummy.class, parameters, PluginType.AIP_TO_AIP,
            SelectedItemsAll.create(AIP.class));

    index.commitAIPs();

    final Filter filterParentTheAIP = new Filter();
    filterParentTheAIP.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aipId));
    final IndexResult<IndexedRepresentation> indexResult = index.find(IndexedRepresentation.class, filterParentTheAIP, null,
            new Sublist(0, 10), Collections.emptyList());

    Assert.assertEquals(indexResult.getResults().size(), 2);

  }
}
