/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.request.GenericSolrRequest;
import org.apache.solr.client.solrj.response.InputStreamResponseParser;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.IsRODAObject;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationSupport;
import org.roda.core.data.v2.risks.IncidenceStatus;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.SeverityLevel;
import org.roda.core.index.schema.SolrCollectionRegistry;
import org.roda.core.model.ModelService;
import org.roda.core.plugins.base.characterization.PremisSkeletonPlugin;
import org.roda.core.plugins.base.characterization.SiegfriedPlugin;
import org.roda.core.plugins.base.maintenance.DeleteRODAObjectPlugin;
import org.roda.core.plugins.base.preservation.EditFileFormatPlugin;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.StringContentPayload;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * #3786: a job does not commit Solr once per item it writes, which opens a new
 * searcher each time, but at most once per block; what it wrote is still
 * visible as soon as the job ends. Deleting AIPs commits the collections of
 * their children only when children were written after the last such commit:
 * once for AIPs written together, never for the ghosts of an ingest, which
 * have none.
 *
 * The explicit commits are counted from the metrics of the Solr of the tests,
 * which count every commit and, apart, those Solr makes on its own.
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class ExplicitCommitsTest {
  // files Siegfried identifies by extension only, which it warns about, in a
  // folder, as the incidences of the files at
  // the root of a representation are not found to be mitigated
  private static final int FILES = 5;
  private static final List<String> FOLDER = List.of("folder");
  private static final int OBJECTS = 5;
  private static final Pattern COMMIT_METRIC = Pattern
    .compile("solr_core_update_(commit_ops_total|auto_commits_total)\\{([^}]*)\\} (\\S+)");

  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;

  @BeforeClass
  public static void setUp() throws Exception {
    // readable by others, as Siegfried, in its own container, reads the files
    basePath = TestsHelper.createBaseTempDir(ExplicitCommitsTest.class, true,
      PosixFilePermissions
        .asFileAttribute(new HashSet<>(Arrays.asList(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
          PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE))));
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
  }

  @AfterClass(alwaysRun = true)
  public static void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @AfterMethod
  public void cleanUp() {
    // each test counts the documents in the index it created
    IndexTestUtils.resetIndex();
  }

  /**
   * The commits Solr was asked for in the collection, without those it made on
   * its own.
   */
  private static long explicitCommits(Class<? extends IsIndexed> indexClass) throws Exception {
    String collection = SolrCollectionRegistry.getIndexName(indexClass);
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("wt", "prometheus");
    GenericSolrRequest request = new GenericSolrRequest(SolrRequest.METHOD.GET, "/admin/metrics", params);
    request.setResponseParser(new InputStreamResponseParser("prometheus"));
    NamedList<Object> response = index.getSolrClient().request(request);
    String metrics;
    try (InputStream stream = (InputStream) response.get(InputStreamResponseParser.STREAM_KEY)) {
      metrics = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }

    double commits = 0;
    Matcher matcher = COMMIT_METRIC.matcher(metrics);
    while (matcher.find()) {
      String labels = matcher.group(2);
      if (labels.contains("collection=\"" + collection + "\"")) {
        double value = Double.parseDouble(matcher.group(3));
        if (matcher.group(1).equals("commit_ops_total") && labels.contains("ops=\"commits\"")) {
          commits += value;
        } else if (matcher.group(1).equals("auto_commits_total")) {
          commits -= value;
        }
      }
    }
    return Math.round(commits);
  }

  private static AIP createAIPWithWarnedFiles() throws Exception {
    AIP aip = model.createAIP(null, RodaConstants.AIP_TYPE_MIXED, new Permissions(), RodaConstants.ADMIN, null);
    String representationId = IdUtils.createUUID();
    model.createRepresentation(aip.getId(), representationId, true, RodaConstants.REPRESENTATION_TYPE_MIXED, true,
      RodaConstants.ADMIN);
    for (int i = 0; i < FILES; i++) {
      model.createFile(aip.getId(), representationId, FOLDER, "file" + i + ".json",
        new StringContentPayload("{\"file\": " + i + "}"), RodaConstants.ADMIN);
    }
    TestsHelper.executeJob(PremisSkeletonPlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, aip.getId()));
    return model.retrieveAIP(aip.getId());
  }

  private static void runSiegfried(AIP aip) throws Exception {
    TestsHelper.executeJob(SiegfriedPlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, aip.getId()));
  }

  private static long incidences(AIP aip, IncidenceStatus status) throws RODAException {
    return index.count(RiskIncidence.class,
      new Filter(new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_AIP_ID, aip.getId()),
        new SimpleFilterParameter(RodaConstants.RISK_INCIDENCE_STATUS, status.name())));
  }

  private static List<RiskIncidence> createIncidences() throws RODAException {
    List<RiskIncidence> incidences = new ArrayList<>();
    for (int i = 0; i < OBJECTS; i++) {
      RiskIncidence incidence = new RiskIncidence();
      incidence.setRiskId(RodaConstants.RISK_ID_SIEGFRIED_IDENTIFICATION_WARNING);
      incidence.setAipId(IdUtils.createUUID());
      incidence.setStatus(IncidenceStatus.UNMITIGATED);
      incidence.setSeverity(SeverityLevel.LOW);
      incidence.setDescription("incidence " + i);
      incidence.setDetectedBy(RodaConstants.ADMIN);
      incidence.setObjectClass(AIP.class.getSimpleName());
      incidence.setByPlugin(false);
      incidences.add(model.createRiskIncidence(incidence, false));
    }
    index.commit(RiskIncidence.class);
    return incidences;
  }

  private static List<Risk> createRisks() throws RODAException {
    List<Risk> risks = new ArrayList<>();
    for (int i = 0; i < OBJECTS; i++) {
      Risk risk = new Risk();
      risk.setName("risk " + i);
      risk.setIdentifiedOn(new Date());
      risk.setIdentifiedBy(RodaConstants.ADMIN);
      risk.setCategories(List.of("category"));
      risk.setPreMitigationProbability(1);
      risk.setPreMitigationImpact(1);
      risk.setPreMitigationSeverity(1);
      risk.setPreMitigationSeverityLevel(SeverityLevel.LOW);
      risk.setCreatedOn(new Date());
      risk.setCreatedBy(RodaConstants.ADMIN);
      risks.add(model.createRisk(risk, false));
    }
    index.commit(IndexedRisk.class);
    return risks;
  }

  @Test
  public void siegfriedCommitsTheIncidencesOfABlockOnce() throws Exception {
    AIP aip = createAIPWithWarnedFiles();

    long before = explicitCommits(RiskIncidence.class);
    runSiegfried(aip);

    Assert.assertTrue(explicitCommits(RiskIncidence.class) - before <= 1,
      "commits of " + FILES + " incidences: " + (explicitCommits(RiskIncidence.class) - before));
    // visible as soon as the job ends
    Assert.assertEquals(incidences(aip, IncidenceStatus.UNMITIGATED), FILES);
  }

  @Test
  public void siegfriedRunAgainMitigatesTheIncidencesOfThePreviousRun() throws Exception {
    AIP aip = createAIPWithWarnedFiles();

    runSiegfried(aip);
    runSiegfried(aip);

    Assert.assertEquals(incidences(aip, IncidenceStatus.MITIGATED), FILES);
    Assert.assertEquals(incidences(aip, IncidenceStatus.UNMITIGATED), FILES);
  }

  @Test
  public void editFileFormatCommitsTheMitigatedIncidencesOnce() throws Exception {
    AIP aip = createAIPWithWarnedFiles();
    runSiegfried(aip);
    List<String> files = filesOf(aip);
    Assert.assertEquals(files.size(), FILES);
    Map<String, String> parameters = new HashMap<>();
    parameters.put(RodaConstants.PLUGIN_PARAMS_MIMETYPE, "application/json");
    parameters.put(RodaConstants.PLUGIN_PARAMS_FORMAT, "JSON");
    parameters.put(RodaConstants.PLUGIN_PARAMS_CLEAR_INCIDENCES, "true");

    long before = explicitCommits(RiskIncidence.class);
    TestsHelper.executeJob(EditFileFormatPlugin.class, parameters, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(File.class, files));

    Assert.assertTrue(explicitCommits(RiskIncidence.class) - before <= 1,
      "commits of " + FILES + " mitigated incidences: " + (explicitCommits(RiskIncidence.class) - before));
    Assert.assertEquals(incidences(aip, IncidenceStatus.MITIGATED), FILES);
    Assert.assertEquals(incidences(aip, IncidenceStatus.UNMITIGATED), 0);
  }

  private static List<String> filesOf(AIP aip) throws Exception {
    List<String> uuids = new ArrayList<>();
    try (CloseableIterable<OptionalWithCause<File>> files = model.listFilesUnder(aip.getId(),
      aip.getRepresentations().get(0).getId(), true)) {
      for (OptionalWithCause<File> file : files) {
        if (!file.get().isDirectory()) {
          uuids.add(IdUtils.getFileId(file.get()));
        }
      }
    }
    return uuids;
  }

  @Test
  public void deletingIncidencesRisksAndRepresentationInformationCommitsOnce() throws Exception {
    List<String> incidenceIds = new ArrayList<>();
    for (RiskIncidence incidence : createIncidences()) {
      incidenceIds.add(incidence.getId());
    }
    List<String> riskIds = new ArrayList<>();
    for (Risk risk : createRisks()) {
      riskIds.add(risk.getId());
    }
    List<String> representationInformationIds = new ArrayList<>();
    for (int i = 0; i < OBJECTS; i++) {
      RepresentationInformation ri = new RepresentationInformation();
      ri.setName("representation information " + i);
      ri.setSupport(RepresentationInformationSupport.SUPPORTED);
      ri.setExtras("<extras></extras>");
      representationInformationIds.add(model.createRepresentationInformation(ri, RodaConstants.ADMIN, false).getId());
    }
    index.commit(RepresentationInformation.class);

    assertDeletedWithOneCommit(RiskIncidence.class, RiskIncidence.class, incidenceIds);
    assertDeletedWithOneCommit(IndexedRisk.class, Risk.class, riskIds);
    assertDeletedWithOneCommit(RepresentationInformation.class, RepresentationInformation.class,
      representationInformationIds);
  }

  @Test
  public void deletingGhostAIPsDoesNotCommitTheCollectionsOfChildren() throws Exception {
    // the ghosts an ingest creates as placeholders of parents not yet ingested,
    // deleted when it ends, have no
    // representations, files, events or incidences
    List<String> ghosts = new ArrayList<>();
    for (int i = 0; i < OBJECTS; i++) {
      ghosts.add(model.createAIP(null, "", new Permissions(), List.of(IdUtils.createUUID()), null, true,
        RodaConstants.ADMIN, true, null).getId());
    }
    List<Class<? extends IsIndexed>> children = List.of(IndexedRepresentation.class, IndexedFile.class,
      IndexedPreservationEvent.class, RiskIncidence.class);
    Map<Class<? extends IsIndexed>, Long> before = new HashMap<>();
    for (Class<? extends IsIndexed> child : children) {
      before.put(child, explicitCommits(child));
    }

    for (String ghost : ghosts) {
      model.deleteAIP(ghost);
    }

    for (Class<? extends IsIndexed> child : children) {
      Assert.assertEquals(explicitCommits(child) - before.get(child), 0L,
        "commits of " + child.getSimpleName() + " deleting " + OBJECTS + " ghosts");
    }
    index.commit(IndexedAIP.class);
    Assert.assertEquals(index.count(IndexedAIP.class, Filter.ALL), 0L, "ghosts left");
  }

  @Test
  public void deletingAIPsWrittenTogetherCommitsTheCollectionsOfChildrenOnce() throws Exception {
    List<String> aipIds = new ArrayList<>();
    for (int i = 0; i < OBJECTS; i++) {
      AIP aip = model.createAIP(null, RodaConstants.AIP_TYPE_MIXED, new Permissions(), RodaConstants.ADMIN, null);
      String representationId = IdUtils.createUUID();
      model.createRepresentation(aip.getId(), representationId, true, RodaConstants.REPRESENTATION_TYPE_MIXED, true,
        RodaConstants.ADMIN);
      model.createFile(aip.getId(), representationId, FOLDER, "file.json", new StringContentPayload("{}"),
        RodaConstants.ADMIN);
      aipIds.add(aip.getId());
    }
    TestsHelper.executeJob(PremisSkeletonPlugin.class, PluginType.AIP_TO_AIP,
      SelectedItemsList.create(AIP.class, aipIds));
    List<Class<? extends IsIndexed>> children = List.of(IndexedRepresentation.class, IndexedFile.class,
      IndexedPreservationEvent.class, RiskIncidence.class);
    Map<Class<? extends IsIndexed>, Long> before = new HashMap<>();
    for (Class<? extends IsIndexed> child : children) {
      before.put(child, explicitCommits(child));
    }

    TestsHelper.executeJob(DeleteRODAObjectPlugin.class, Collections.emptyMap(), PluginType.INTERNAL,
      SelectedItemsList.create(AIP.class, aipIds));

    for (Class<? extends IsIndexed> child : children) {
      Assert.assertTrue(explicitCommits(child) - before.get(child) <= 1, "commits of " + child.getSimpleName()
        + " deleting " + OBJECTS + " AIPs: " + (explicitCommits(child) - before.get(child)));
    }
    index.commit(IndexedRepresentation.class, IndexedFile.class, IndexedPreservationEvent.class);
    Assert.assertEquals(index.count(IndexedRepresentation.class, Filter.ALL), 0L, "representations left");
    Assert.assertEquals(index.count(IndexedFile.class, Filter.ALL), 0L, "files left");
    Assert.assertEquals(index.count(IndexedPreservationEvent.class,
      new Filter(new SimpleFilterParameter(RodaConstants.PRESERVATION_EVENT_AIP_ID, aipIds.get(0)))), 0L,
      "events left");
  }

  private static <T extends IsRODAObject> void assertDeletedWithOneCommit(Class<? extends IsIndexed> indexClass,
    Class<T> objectClass, List<String> ids) throws Exception {
    long before = explicitCommits(indexClass);
    TestsHelper.executeJob(DeleteRODAObjectPlugin.class, Collections.emptyMap(), PluginType.INTERNAL,
      SelectedItemsList.create(objectClass, ids));

    Assert.assertTrue(explicitCommits(indexClass) - before <= 1, "commits deleting " + ids.size() + " "
      + objectClass.getSimpleName() + ": " + (explicitCommits(indexClass) - before));
    // gone as soon as the job ends
    Assert.assertEquals(index.count(indexClass, Filter.ALL), 0L, objectClass.getSimpleName() + " left");
  }
}
