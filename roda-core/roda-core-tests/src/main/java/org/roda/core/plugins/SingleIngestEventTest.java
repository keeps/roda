/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.RodaConstants.PreservationEventType;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.TransferredResource;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.IndexService;
import org.roda.core.index.IndexTestUtils;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.base.ingest.v2.MinimalIngestPlugin;
import org.roda.core.plugins.base.v2.FailureOnMandatoryStepIngestPlugin;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.Binary;
import org.roda.core.storage.fs.FSUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import gov.loc.premis.v3.EventComplexType;
import gov.loc.premis.v3.EventOutcomeInformationComplexType;

/**
 * Ingest with the option to write a single preservation event per AIP
 * ({@link RodaConstants#PLUGIN_PARAMS_CONSOLIDATE_INGEST_EVENTS}).
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class SingleIngestEventTest {
  // steps of the minimal ingest that write an AIP event: unpacking, wellformedness check (SIP), wellformedness
  // check (descriptive metadata), message digest calculation, authorization check and accession
  private static final int MINIMAL_INGEST_STEP_EVENTS = 6;

  private Path basePath;
  private ModelService model;
  private IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;
  private Path corporaPath;

  @BeforeClass
  public void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(getClass(), true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = true;
    boolean deployOrchestrator = true;
    boolean deployPluginManager = true;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false, ldapUtilityTestHelper.getLdapUtility());
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = SingleIngestEventTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());

    RodaCoreFactory.getRodaConfiguration()
      .setProperty(RodaConstants.CORE_TRANSFERRED_RESOURCES_INGEST_MOVE_WHEN_AUTOACCEPT, false);
    RodaCoreFactory.getRodaConfiguration()
      .setProperty(RodaConstants.CORE_TRANSFERRED_RESOURCES_DELETE_WHEN_SUCCESSFULLY_INGESTED, false);
  }

  @AfterClass
  public void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
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

    index.execute(TransferredResource.class, Filter.ALL, new ArrayList<>(),
      item -> model.deleteTransferredResource(item), e -> Assert.fail("Error removing resources", e));

    TestsHelper.releaseAllLocks();
  }

  @Test
  public void testOptionOffWritesOneEventPerStep() throws Exception {
    AIP aip = ingest(MinimalIngestPlugin.class, false, true);

    List<IndexedPreservationEvent> events = getAIPEvents(aip.getId());
    // ingest start + steps + ingest end, as always
    Assert.assertEquals(events.size(), MINIMAL_INGEST_STEP_EVENTS + 2);
    Assert.assertTrue(events.stream()
      .noneMatch(e -> PreservationEventType.INGESTION.getOriginalText().equals(e.getEventType())));
  }

  @Test
  public void testOptionOnWritesASingleEvent() throws Exception {
    AIP aip = ingest(MinimalIngestPlugin.class, true, true);

    List<IndexedPreservationEvent> events = getAIPEvents(aip.getId());
    Assert.assertEquals(events.size(), 1);
    IndexedPreservationEvent indexedEvent = events.get(0);
    Assert.assertEquals(indexedEvent.getEventType(), PreservationEventType.INGESTION.getOriginalText());
    Assert.assertEquals(indexedEvent.getEventOutcome(), PluginState.SUCCESS.name());

    EventComplexType event = getEvent(aip.getId(), indexedEvent.getId());
    // ingest outcome followed by the outcome of each step
    List<EventOutcomeInformationComplexType> outcomes = event.getEventOutcomeInformation();
    Assert.assertEquals(outcomes.size(), MINIMAL_INGEST_STEP_EVENTS + 1);
    Assert.assertEquals(event.getEventDetailInformation().size(), MINIMAL_INGEST_STEP_EVENTS + 1);
    for (EventOutcomeInformationComplexType stepOutcome : outcomes.subList(1, outcomes.size())) {
      Assert.assertEquals(stepOutcome.getEventOutcome().get(0).getValue(), PluginState.SUCCESS.name());
      Assert.assertTrue(getNote(stepOutcome).startsWith("[" + PluginState.SUCCESS.name() + "] "), getNote(stepOutcome));
    }

    // each step plugin is an agent of the event
    Assert.assertTrue(event.getLinkingAgentIdentifier().stream().anyMatch(
      agent -> agent.getLinkingAgentIdentifierValue().contains(PremisSkeletonPlugin.class.getName())));

    // the step events are kept in files that are deleted at the end
    Path jobIngestEvents = RodaCoreFactory.getWorkingDirectory().resolve(aip.getIngestJobId())
      .resolve("ingest-events");
    Assert.assertFalse(Files.exists(jobIngestEvents), jobIngestEvents.toString());
  }

  @Test
  public void testOptionOnWritesASingleEventWhenAMandatoryStepFails() throws Exception {
    AIP aip = ingest(FailureOnMandatoryStepIngestPlugin.class, true, false);

    List<IndexedPreservationEvent> events = getAIPEvents(aip.getId());
    Assert.assertEquals(events.size(), 1);
    IndexedPreservationEvent indexedEvent = events.get(0);
    Assert.assertEquals(indexedEvent.getEventType(), PreservationEventType.INGESTION.getOriginalText());
    Assert.assertEquals(indexedEvent.getEventOutcome(), PluginState.FAILURE.name());
  }

  @Test
  public void testOptionOnWritesASingleEventOnSIPUpdate() throws Exception {
    AIP aip = ingest(MinimalIngestPlugin.class, true, true);

    TransferredResource update = EARKSIPPluginsTest.createIngestUpdateCorpora(corporaPath, index, null);
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_CONSOLIDATE_INGEST_EVENTS, "true");
    Job job = TestsHelper.executeJob(MinimalIngestPlugin.class, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, update.getUUID()));
    TestsHelper.getJobReports(index, job, true);

    // one event for the ingest and one for the update
    List<IndexedPreservationEvent> events = getAIPEvents(aip.getId());
    Assert.assertEquals(events.size(), 2);
    Assert.assertTrue(events.stream()
      .allMatch(e -> PreservationEventType.INGESTION.getOriginalText().equals(e.getEventType())));
  }

  @Test
  public void testPluginRunOnItsOwnWritesItsOwnEvent() throws Exception {
    AIP aip = ingest(MinimalIngestPlugin.class, true, true);

    // the option is set, but no ingest is running in this job
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_CONSOLIDATE_INGEST_EVENTS, "true");
    TestsHelper.executeJob(PremisSkeletonPlugin.class, parameters, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, aip.getId()));

    List<IndexedPreservationEvent> events = getAIPEvents(aip.getId());
    Assert.assertEquals(events.size(), 2);
    Assert.assertTrue(events.stream().anyMatch(
      e -> PreservationEventType.MESSAGE_DIGEST_CALCULATION.getOriginalText().equals(e.getEventType())));
  }

  private <T extends Plugin<TransferredResource>> AIP ingest(Class<T> ingestPlugin, boolean singleEvent,
    boolean failIfReportNotSucceeded) throws Exception {
    AIP root = model.createAIP(null, RodaConstants.AIP_TYPE_MIXED, new Permissions(), RodaConstants.ADMIN, null);
    TransferredResource transferredResource = EARKSIPPluginsTest.createIngestCorpora(corporaPath, index);

    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_PARENT_ID, root.getId());
    parameters.put(RodaConstants.PLUGIN_PARAMS_FORCE_PARENT_ID, "true");
    parameters.put(RodaConstants.PLUGIN_PARAMS_CONSOLIDATE_INGEST_EVENTS, Boolean.toString(singleEvent));
    Job job = TestsHelper.executeJob(ingestPlugin, parameters, PluginType.SIP_TO_AIP,
      SelectedItemsList.create(TransferredResource.class, transferredResource.getUUID()));
    TestsHelper.getJobReports(index, job, failIfReportNotSucceeded);
    index.commitAIPs();

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class,
      new Filter(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, root.getId())), null, new Sublist(0, 10),
      new ArrayList<>());
    Assert.assertEquals(find.getTotalCount(), 1L);
    return model.retrieveAIP(find.getResults().get(0).getId());
  }

  private List<IndexedPreservationEvent> getAIPEvents(String aipId) throws RODAException {
    index.commit(IndexedPreservationEvent.class);
    return index.find(IndexedPreservationEvent.class,
      new Filter(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aipId))
        .add(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_OBJECT_CLASS, AIP.class.getSimpleName())),
      null, new Sublist(0, 50), Collections.emptyList()).getResults();
  }

  private EventComplexType getEvent(String aipId, String eventId) throws Exception {
    Binary binary = model.retrievePreservationEvent(aipId, null, null, null, eventId);
    return PremisV3Utils.binaryToEvent(binary.getContent(), true);
  }

  private static String getNote(EventOutcomeInformationComplexType outcome) {
    return outcome.getEventOutcomeDetail().get(0).getEventOutcomeDetailNote().get(0);
  }
}
