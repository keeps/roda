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
import java.util.List;
import java.util.stream.Collectors;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IndexRunnable;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
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

import com.google.common.collect.Iterables;

import jersey.repackaged.com.google.common.collect.Lists;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class MinimalIngestPluginTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(MinimalIngestPluginTest.class);

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
      deployPluginManager, deployDefaultResources);
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = MinimalIngestPluginTest.class.getResource("/corpora");
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
  }

  @Test
  public void testMinimalIngestPluginWithEARKSIPAndNoMoveWhenAutoAccept() throws IOException, RODAException {
    RodaCoreFactory.getRodaConfiguration()
      .setProperty(RodaConstants.CORE_TRANSFERRED_RESOURCES_INGEST_MOVE_WHEN_AUTOACCEPT, false);

    AIP aip = EARKSIPPluginsTest.ingestCorpora(MinimalIngestPlugin.class, model, index, corporaPath);
    assessAIP(aip);
  }

  @Test
  public void testMinimalIngestPluginWithEARKSIPAndMoveWhenAutoAccept() throws IOException, RODAException {
    RodaCoreFactory.getRodaConfiguration()
      .setProperty(RodaConstants.CORE_TRANSFERRED_RESOURCES_INGEST_MOVE_WHEN_AUTOACCEPT, true);

    AIP aip = EARKSIPPluginsTest.ingestCorpora(MinimalIngestPlugin.class, model, index, corporaPath);
    assessAIP(aip);
  }

  private void assessAIP(AIP aip)
    throws GenericException, RequestNotValidException, NotFoundException, AuthorizationDeniedException {

    // check if all ingest events were created (for minimal execution)
    index.commit(IndexedPreservationEvent.class);
    IndexResult<IndexedPreservationEvent> preservationEventsFind = index.find(IndexedPreservationEvent.class,
      new Filter(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aip.getId()))
        .add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS, AIP.class.getSimpleName())),
      null, new Sublist(0, 20), Collections.emptyList());

    Assert.assertEquals(preservationEventsFind.getTotalCount(), 8L);

    ArrayList<String> expectedEvents = new ArrayList<>();
    expectedEvents.add(PreservationEventType.UNPACKING.getOriginalText());
    expectedEvents.add(PreservationEventType.WELLFORMEDNESS_CHECK.getOriginalText());
    expectedEvents.add(PreservationEventType.INGEST_START.getOriginalText());
    expectedEvents.add(PreservationEventType.WELLFORMEDNESS_CHECK.getOriginalText());
    expectedEvents.add(PreservationEventType.MESSAGE_DIGEST_CALCULATION.getOriginalText());
    expectedEvents.add(PreservationEventType.AUTHORIZATION_CHECK.getOriginalText());
    expectedEvents.add(PreservationEventType.ACCESSION.getOriginalText());
    expectedEvents.add(PreservationEventType.INGEST_END.getOriginalText());
    for (IndexedPreservationEvent indexedPreservationEvent : preservationEventsFind.getResults()) {
      expectedEvents.remove(indexedPreservationEvent.getEventType());
    }
    Assert.assertEquals(expectedEvents.size(), 0);

    Assert.assertEquals(aip.getRepresentations().size(), 1);

    CloseableIterable<OptionalWithCause<File>> allFiles = model.listFilesUnder(aip.getId(),
      aip.getRepresentations().get(0).getId(), true);
    List<File> reusableAllFiles = new ArrayList<>();
    Iterables.addAll(reusableAllFiles,
      Lists.newArrayList(allFiles).stream().filter(f -> f.isPresent()).map(f -> f.get()).collect(Collectors.toList()));

    // All folders and files
    Assert.assertEquals(reusableAllFiles.size(), CORPORA_FOLDERS_COUNT + CORPORA_FILES_COUNT);
  }

}
