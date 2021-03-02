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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.common.iterables.CloseableIterables;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.ip.metadata.PreservationMetadata;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.user.User;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.plugins.ingest.MinimalIngestPlugin;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class UserPermissionsPluginTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserPermissionsPluginTest.class);

  private static final int CORPORA_FILES_COUNT = 4;
  private static final int CORPORA_FOLDERS_COUNT = 2;
  private Path basePath;

  private ModelService model;
  private IndexService index;

  private Path corporaPath;

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

    URL corporaURL = UserPermissionsPluginTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());

    LOGGER.info("Running MinimalIngestPlugin tests under storage {}", basePath);
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
    }, e -> Assert.fail("Error cleaning up", e));

    TestsHelper.releaseAllLocks();
  }

  @Test
  public void testIfEventsAreIndexedAfterIngest() throws IOException, RODAException {
    TransferredResource transferredResource = EARKSIPPluginsTest.createIngestCorpora(corporaPath, index);
    Assert.assertNotNull(transferredResource);

    User user = new User("random_user", "random_user", "random@test.com", false);
    user.addGroup(RodaConstants.ADMINISTRATORS);
    user = model.createUser(user, "teste123", true);

    Job job = TestsHelper.executeJob(MinimalIngestPlugin.class, new HashMap<>(), PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()), user.getName());
    TestsHelper.getJobReports(index, job, true);

    IndexResult<IndexedAIP> aipsOnIndex = index.find(IndexedAIP.class, Filter.ALL, Sorter.NONE, Sublist.ALL,
      Facets.NONE, user, false, Collections.emptyList());
    Assert.assertFalse(aipsOnIndex.getResults().isEmpty(), "No AIP is indexed");

    IndexedAIP aip = aipsOnIndex.getResults().get(0);
    Filter eventFilter = new Filter();
    eventFilter.add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()));
    IndexResult<IndexedPreservationEvent> eventsOnIndex = index.find(IndexedPreservationEvent.class, eventFilter,
      Sorter.NONE, Sublist.ALL, Facets.NONE, user, false, Collections.emptyList());

    CloseableIterable<OptionalWithCause<PreservationMetadata>> eventsOnAIP = model.listPreservationMetadata(aip.getId(),
      false);
    Assert.assertEquals(eventsOnIndex.getResults().size(), CloseableIterables.size(eventsOnAIP));
  }
}
