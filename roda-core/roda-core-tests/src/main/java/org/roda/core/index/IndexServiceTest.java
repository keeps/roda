/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.solr.client.solrj.SolrServerException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.common.RodaUtils;
import org.roda.core.common.notifications.EmailNotificationProcessor;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.log.LogEntryParameter;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.notifications.Notification.NOTIFICATION_STATE;
import org.roda.core.data.v2.ri.RepresentationInformation;
import org.roda.core.data.v2.ri.RepresentationInformationSupport;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.Risk.SEVERITY_LEVEL;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.model.ModelService;
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

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_TRAVIS})
public class IndexServiceTest {

  private static Path basePath;
  private static Path logPath;
  private static ModelService model;
  private static IndexService index;

  private static StorageService corporaService;

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexServiceTest.class);

  @BeforeClass
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(IndexServiceTest.class, true);

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = false;
    boolean deployPluginManager = false;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources);

    logPath = RodaCoreFactory.getLogPath();
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = IndexServiceTest.class.getResource("/corpora");
    corporaService = new FileStorageService(Paths.get(corporaURL.toURI()));

    LOGGER.debug("Running index tests under storage {}", basePath);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @AfterMethod
  public void cleanUp() throws RODAException {
    List<String> aipsToDelete = new ArrayList<>();
    index.findAll(IndexedAIP.class, Filter.ALL, new ArrayList<>()).forEach(e -> aipsToDelete.add(e.getId()));
    for (String id : aipsToDelete) {
      try {
        model.deleteAIP(id);
      } catch (NotFoundException e) {
        // do nothing
      }
    }
    // last attempt to delete everything (for model/index inconsistencies)
    index.clearAIPs();
  }

  private void compareAIPWithIndexedAIP(final AIP aip, final IndexedAIP indexedAIP) {
    assertEquals(aip.getId(), indexedAIP.getId());
    assertEquals(aip.getParentId(), indexedAIP.getParentID());
    for (PermissionType permissionType : PermissionType.values()) {
      // users
      Set<String> usersSet1 = aip.getPermissions().getUsers().get(permissionType);
      Set<String> usersSet2 = indexedAIP.getPermissions().getUsers().get(permissionType);

      usersSet1 = usersSet1 == null ? new HashSet<>() : usersSet1;
      usersSet2 = usersSet2 == null ? new HashSet<>() : usersSet2;

      assertEquals(usersSet1, usersSet2);

      // groups
      Set<String> groupsSet1 = aip.getPermissions().getGroups().get(permissionType);
      Set<String> groupsSet2 = indexedAIP.getPermissions().getGroups().get(permissionType);

      groupsSet1 = groupsSet1 == null ? new HashSet<>() : groupsSet1;
      groupsSet2 = groupsSet2 == null ? new HashSet<>() : groupsSet2;

      assertEquals(groupsSet1, groupsSet2);
    }

    assertEquals(AIPState.ACTIVE, aip.getState());
  }

  @Test
  public void testAIPIndexCreateDelete() throws RODAException, ParseException {
    // generate AIP ID
    final String aipId = IdUtils.createUUID();

    // Create AIP
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    index.commitAIPs();

    // Retrieve, count and list AIP
    final IndexedAIP indexedAip = index.retrieve(IndexedAIP.class, aipId, new ArrayList<>());
    compareAIPWithIndexedAIP(aip, indexedAip);

    final IndexResult<IndexedAIP> indexAips = index.find(IndexedAIP.class, null, null, new Sublist(0, 10),
      Collections.emptyList());
    assertEquals(1, indexAips.getTotalCount());
    assertEquals(1, indexAips.getLimit());
    assertEquals(0, indexAips.getOffset());
    assertEquals(1, indexAips.getResults().size());

    final IndexedAIP aipFromList = indexAips.getResults().get(0);
    assertEquals(aip.getId(), aipFromList.getId());
    assertEquals(aip.getState(), aipFromList.getState());
    assertEquals(aip.getParentId(), aipFromList.getParentID());

    assertEquals(indexedAip, aipFromList);
    assertEquals("fonds", indexedAip.getLevel());
    assertEquals("My example", indexedAip.getTitle());
    assertEquals("This is a very nice example", indexedAip.getDescription());
    assertEquals(RodaUtils.parseDate("0001-01-01T00:00:00.000+0000"), indexedAip.getDateInitial());
    assertEquals(RodaUtils.parseDate("0002-01-01T00:00:00.000+0000"), indexedAip.getDateFinal());

    // Retrieve, count and list SRO
    String rep1Id = aip.getRepresentations().get(0).getId();
    IndexedRepresentation rep1 = index.retrieve(IndexedRepresentation.class, IdUtils.getRepresentationId(aipId, rep1Id),
      new ArrayList<>());
    assertEquals(rep1Id, rep1.getId());

    Filter filterParentTheAIP = new Filter();
    filterParentTheAIP.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aipId));
    IndexResult<IndexedRepresentation> sros = index.find(IndexedRepresentation.class, filterParentTheAIP, null,
      new Sublist(0, 10), Collections.emptyList());
    assertEquals(aip.getRepresentations().size(), sros.getTotalCount());

    List<String> sroIDs = new ArrayList<>();
    for (Representation sro : sros.getResults()) {
      sroIDs.add(sro.getId());
    }

    List<String> representationIds = aip.getRepresentations().stream().map(r -> r.getId()).collect(Collectors.toList());
    MatcherAssert.assertThat(sroIDs, Matchers.contains(representationIds.toArray()));

    model.deleteAIP(aipId);
    try {
      index.retrieve(IndexedAIP.class, aipId, new ArrayList<>());
      Assert.fail("AIP deleted but yet it was retrieved");
    } catch (NotFoundException e) {
      // do nothing as it was the expected exception
    } catch (RODAException e) {
      Assert.fail("AIP was deleted and therefore a " + NotFoundException.class.getName()
        + " should have been thrown instead of a " + e.getClass().getName());
    }
  }

  @Test
  public void testAIPIndexCreateDelete2() throws RODAException {
    final String aipId = IdUtils.createUUID();

    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID_3),
      RodaConstants.ADMIN);

    index.commitAIPs();

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.INDEX_UUID, aipId));
    IndexedAIP indexedAip = index.find(IndexedAIP.class, filter, null, new Sublist(0, 10), Collections.emptyList())
      .getResults().get(0);
    Calendar calInitial = Calendar.getInstance();
    calInitial.setTime(indexedAip.getDateInitial());
    assertEquals(calInitial.get(Calendar.YEAR), CorporaConstants.YEAR_1213);
    Calendar calFinal = Calendar.getInstance();
    calFinal.setTime(indexedAip.getDateFinal());
    assertEquals(calFinal.get(Calendar.YEAR), CorporaConstants.YEAR_2003);

  }

  @Test
  public void testAIPUpdate() throws RODAException {
    // generate AIP ID
    final String aipId = IdUtils.createUUID();

    // testing AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    final StoragePath otherAipPath = DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER,
      CorporaConstants.OTHER_AIP_ID);
    final AIP updatedAIP = model.updateAIP(aipId, corporaService, otherAipPath, RodaConstants.ADMIN);

    final IndexedAIP indexedAIP = index.retrieve(IndexedAIP.class, aipId, new ArrayList<>());

    compareAIPWithIndexedAIP(updatedAIP, indexedAIP);

    model.deleteAIP(aipId);
  }

  @Test
  public void testListCollections() throws RODAException {
    // set up
    model.createAIP(CorporaConstants.SOURCE_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);
    model.createAIP(CorporaConstants.OTHER_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.OTHER_AIP_ID),
      RodaConstants.ADMIN);

    index.commitAIPs();

    // TODO following filter should be in corpora constants or removed
    Filter fondsFilter = new Filter(new SimpleFilterParameter("level", "fonds"));

    long aipCount = index.count(IndexedAIP.class, fondsFilter);
    assertEquals(1, aipCount);

    final IndexResult<IndexedAIP> aips = index.find(IndexedAIP.class, fondsFilter, null, new Sublist(0, 10),
      Collections.emptyList());

    assertEquals(1, aips.getLimit());
    assertEquals(CorporaConstants.SOURCE_AIP_ID, aips.getResults().get(0).getId());

    model.deleteAIP(CorporaConstants.SOURCE_AIP_ID);
    model.deleteAIP(CorporaConstants.OTHER_AIP_ID);
  }

  @Test
  public void testSubElements() throws RODAException {
    // set up
    model.createAIP(CorporaConstants.SOURCE_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);
    model.createAIP(CorporaConstants.OTHER_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.OTHER_AIP_ID),
      RodaConstants.ADMIN);

    index.commitAIPs();

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, CorporaConstants.SOURCE_AIP_ID));

    long aipCount = index.count(IndexedAIP.class, filter);
    assertEquals(1, aipCount);

    final IndexResult<IndexedAIP> aips = index.find(IndexedAIP.class, filter, null, new Sublist(0, 10),
      Collections.emptyList());

    assertEquals(1, aips.getLimit());
    assertEquals(CorporaConstants.OTHER_AIP_ID, aips.getResults().get(0).getId());

    model.deleteAIP(CorporaConstants.SOURCE_AIP_ID);
    model.deleteAIP(CorporaConstants.OTHER_AIP_ID);
  }

  @Test
  public void testGetAncestors() throws RODAException {
    // set up
    model.createAIP(CorporaConstants.SOURCE_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);
    model.createAIP(CorporaConstants.OTHER_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.OTHER_AIP_ID),
      RodaConstants.ADMIN);

    index.commitAIPs();

    IndexedAIP aip = index.retrieve(IndexedAIP.class, CorporaConstants.OTHER_AIP_ID, new ArrayList<>());
    List<IndexedAIP> ancestors = index.retrieveAncestors(aip, new ArrayList<>());
    MatcherAssert.assertThat(ancestors,
      Matchers.hasItem(Matchers.<IndexedAIP> hasProperty("id", Matchers.equalTo(CorporaConstants.SOURCE_AIP_ID))));
  }

  @Test
  public void testGetElementWithoutParentId() throws RODAException {
    // generate AIP ID
    final String aipId = IdUtils.createUUID();

    // Create AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    index.commitAIPs();

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.AIP_LEVEL, "fonds"));
    filter.add(new EmptyKeyFilterParameter(RodaConstants.AIP_PARENT_ID));
    IndexResult<IndexedAIP> findDescriptiveMetadata = index.find(IndexedAIP.class, filter, null, new Sublist(),
      Collections.emptyList());

    assertNotNull(findDescriptiveMetadata);
    MatcherAssert.assertThat(findDescriptiveMetadata.getResults(), IsCollectionWithSize.hasSize(1));

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testGetLogEntriesCount() throws GenericException, RequestNotValidException, AuthorizationDeniedException,
    NotFoundException, InterruptedException {
    // cleaning up action log entries on index (if any)
    index.deleteAllActionLog();

    LogEntry entry = new LogEntry();
    entry.setActionComponent("Action");
    entry.setActionMethod("Method");
    entry.setAddress("Address");
    entry.setDatetime(new Date());
    entry.setDuration(10L);
    entry.setId("ID");
    entry.setRelatedObjectID("Related");
    entry.setUsername("Username");
    entry.setState(LOG_ENTRY_STATE.SUCCESS);
    List<LogEntryParameter> parameters = new ArrayList<>();
    parameters.add(new LogEntryParameter("NAME1", "VALUE1"));
    parameters.add(new LogEntryParameter("NAME2", "VALUE2"));
    entry.setParameters(parameters);
    model.addLogEntry(entry, logPath);

    index.commit(LogEntry.class);

    Filter filterDescription = new Filter();
    filterDescription.add(new SimpleFilterParameter(RodaConstants.LOG_ID, "ID"));
    MatcherAssert.assertThat(index.count(LogEntry.class, filterDescription), Matchers.is(1L));

    Filter filterDescription2 = new Filter();
    filterDescription2.add(new SimpleFilterParameter(RodaConstants.LOG_ID, "ID2"));
    MatcherAssert.assertThat(index.count(LogEntry.class, filterDescription2), Matchers.is(0L));
  }

  @Test
  public void testFindLogEntry() throws GenericException, RequestNotValidException, AuthorizationDeniedException,
    NotFoundException, InterruptedException {
    LogEntry entry = new LogEntry();
    entry.setActionComponent(RodaConstants.LOG_ACTION_COMPONENT);
    entry.setActionMethod("Method");
    entry.setAddress("address");
    entry.setDatetime(new Date());
    entry.setDuration(10L);
    entry.setId("id");
    entry.setRelatedObjectID("related");
    entry.setUsername("username");
    entry.setState(LOG_ENTRY_STATE.SUCCESS);
    List<LogEntryParameter> parameters = new ArrayList<>();
    parameters.add(new LogEntryParameter("NAME1", "VALUE1"));
    parameters.add(new LogEntryParameter("NAME2", "VALUE2"));
    entry.setParameters(parameters);
    model.addLogEntry(entry, logPath);

    index.commit(LogEntry.class);

    Filter filterDescription = new Filter();
    filterDescription.add(new SimpleFilterParameter(RodaConstants.LOG_ID, "id"));

    IndexResult<LogEntry> entries = index.find(LogEntry.class, filterDescription, null, new Sublist(),
      Collections.emptyList());
    assertEquals(entries.getTotalCount(), 1);
    assertEquals(entries.getResults().get(0).getActionComponent(), RodaConstants.LOG_ACTION_COMPONENT);

    Filter filterDescription2 = new Filter();
    filterDescription2.add(new SimpleFilterParameter(RodaConstants.LOG_ID, "id2"));

    IndexResult<LogEntry> entries2 = index.find(LogEntry.class, filterDescription2, null, new Sublist(),
      Collections.emptyList());
    assertEquals(entries2.getTotalCount(), 0);
  }

  @Test
  public void testReindexLogEntry() throws GenericException, RequestNotValidException, AuthorizationDeniedException,
    NotFoundException, InterruptedException {
    Long number = 10L;

    for (int i = 0; i < number; i++) {
      LogEntry entry = new LogEntry();
      entry.setId("ID" + i);
      entry.setActionComponent("ACTION:" + i);
      entry.setActionMethod("Method:" + i);
      entry.setAddress("ADDRESS");
      entry.setDatetime(new Date());
      entry.setDuration(i);
      entry.setRelatedObjectID("RELATED:" + i);
      entry.setUsername("USER:" + i);
      entry.setState(LOG_ENTRY_STATE.SUCCESS);
      List<LogEntryParameter> parameters = new ArrayList<>();
      parameters.add(new LogEntryParameter("NAME1", "VALUE1"));
      parameters.add(new LogEntryParameter("NAME2", "VALUE2"));
      entry.setParameters(parameters);
      model.addLogEntry(entry, logPath, false);
    }

    index.commit(LogEntry.class);

    model.findOldLogsAndMoveThemToStorage(logPath, null);
    index.reindexActionLogs();

    index.commit(LogEntry.class);

    Filter f1 = new Filter();
    f1.add(new SimpleFilterParameter(RodaConstants.LOG_ACTION_COMPONENT, "ACTION:0"));
    IndexResult<LogEntry> entries1 = index.find(LogEntry.class, f1, null, new Sublist(0, 10), Collections.emptyList());
    MatcherAssert.assertThat(entries1.getTotalCount(), Matchers.is(1L));
    Filter f2 = new Filter();
    f2.add(new SimpleFilterParameter(RodaConstants.LOG_ADDRESS, "ADDRESS"));
    IndexResult<LogEntry> entries2 = index.find(LogEntry.class, f2, null, new Sublist(0, 10), Collections.emptyList());
    MatcherAssert.assertThat(entries2.getTotalCount(), Matchers.is(number));
  }

  @Test
  public void testReindexAIP() throws ParseException, RequestNotValidException, GenericException,
    AuthorizationDeniedException, AlreadyExistsException, NotFoundException, ValidationException {
    index.clearIndex(RodaConstants.INDEX_AIP);

    for (int i = 0; i < 10; i++) {
      final String aipId = IdUtils.createUUID();
      model.createAIP(aipId, corporaService,
        DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), false,
        RodaConstants.ADMIN);
    }

    index.reindexAIPs();
    long count = index.count(IndexedAIP.class, new Filter());
    assertEquals(10L, count);

  }

  @Test
  public void indexMembers() throws AlreadyExistsException, GenericException, RequestNotValidException,
    IllegalOperationException, NotFoundException {
    Set<String> groups = new HashSet<>();
    groups.add("administrators");

    Set<String> users = new HashSet<>();
    users.add("admin");
    Set<String> roles = new HashSet<>();
    roles.add(RodaConstants.REPOSITORY_PERMISSIONS_DESCRIPTIVE_METADATA_UPDATE);

    for (int i = 0; i < 5; i++) {
      if (i % 2 == 0) {
        User user = new User();
        user.setActive(true);
        user.setAllRoles(roles);
        user.setDirectRoles(roles);
        user.setGuest(false);
        user.setId("USER" + i);
        user.setName("NAMEUSER" + i);
        user.setFullName("NAMEUSER" + i);

        user.setEmail("mail_" + i + "@example.com");
        user.setGroups(groups);
        model.createUser(user, true);
      } else {
        Group group = new Group();
        group.setActive(true);
        group.setAllRoles(roles);
        group.setDirectRoles(roles);
        group.setId("GROUP" + i);
        group.setName("NAMEGROUP" + i);
        group.setFullName("NAMEGROUP" + i);

        group.setUsers(users);
        model.createGroup(group, true);
      }
    }

    index.commit(RODAMember.class);

    Filter filterUSER1 = new Filter();
    filterUSER1.add(new SimpleFilterParameter(RodaConstants.MEMBERS_NAME, "NAMEUSER0"));
    filterUSER1.add(new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, "true"));
    MatcherAssert.assertThat(index.count(RODAMember.class, filterUSER1), Matchers.is(1L));

    Filter filterGroup = new Filter();
    filterGroup.add(new SimpleFilterParameter(RodaConstants.MEMBERS_NAME, "NAMEGROUP1"));
    filterGroup.add(new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, "false"));
    MatcherAssert.assertThat(index.count(RODAMember.class, filterGroup), Matchers.is(1L));

  }

  @Test
  public void testIdWithComma() throws RODAException {
    // generate AIP ID
    final String origAipId = "id, with, comma";
    final String aipId = IdUtils.createUUID(origAipId);

    // Create AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID),
      RodaConstants.ADMIN);

    index.commitAIPs();

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class, null, null, new Sublist(0, 10),
      Collections.emptyList());
    assertEquals(1, find.getTotalCount());

    IndexedAIP aip = index.retrieve(IndexedAIP.class, aipId, new ArrayList<>());
    assertNotNull(aip);

    // cleanup
    model.deleteAIP(aipId);
  }

  @Test
  public void testRiskIndex() {
    try {
      Risk risk = new Risk();
      risk.setName("Risk name");
      risk.setDescription("Risk description");
      risk.setIdentifiedOn(new Date());
      risk.setIdentifiedBy("Risk identifier");
      risk.setCategory("Risk category");
      risk.setNotes("Risk notes");

      risk.setPreMitigationProbability(4);
      risk.setPreMitigationImpact(4);
      risk.setPreMitigationSeverity(16);
      risk.setPreMitigationSeverityLevel(SEVERITY_LEVEL.HIGH);
      risk.setPreMitigationNotes("Pre Notes");

      risk.setPostMitigationProbability(3);
      risk.setPostMitigationImpact(2);
      risk.setPostMitigationSeverity(6);
      risk.setPostMitigationSeverityLevel(SEVERITY_LEVEL.MODERATE);
      risk.setPostMitigationNotes("Pos Notes");

      risk.setMitigationStrategy("Mitigation Strategy");
      risk.setMitigationOwnerType("Owner type");
      risk.setMitigationOwner("Owner");
      risk.setMitigationRelatedEventIdentifierType("Mitigation REI type");
      risk.setMitigationRelatedEventIdentifierValue("Mitigation REI value");

      risk.setCreatedOn(new Date());
      risk.setCreatedBy("admin");
      risk.setUpdatedOn(new Date());
      risk.setUpdatedBy("admin");

      model.createRisk(risk, false);
      index.commit(IndexedRisk.class);

      Risk risk2 = model.retrieveRisk(risk.getId());
      assertNotNull(risk2);
      assertEquals(risk.getId(), risk2.getId());
      assertEquals(risk.getName(), risk2.getName());

      IndexResult<IndexedRisk> find = index.find(IndexedRisk.class, null, null, new Sublist(0, 10),
        Collections.emptyList());
      assertEquals(1, find.getTotalCount());

      Risk risk3 = index.retrieve(IndexedRisk.class, risk.getId(), new ArrayList<>());
      assertNotNull(risk3);
      assertEquals(risk.getId(), risk3.getId());
      assertEquals(risk.getName(), risk3.getName());

      risk3.setName("Risk New Name");
      Map<String, String> properties = new HashMap<>();
      properties.put(RodaConstants.VERSION_ACTION, RodaConstants.VersionAction.UPDATED.toString());
      model.updateRisk(risk3, properties, false, 0);

      Risk risk4 = index.retrieve(IndexedRisk.class, risk.getId(), new ArrayList<>());
      assertNotNull(risk4);
      assertEquals(risk.getId(), risk4.getId());
      assertEquals(risk4.getName(), "Risk New Name");

      model.deleteRisk(risk.getId(), false);

    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      e.printStackTrace(System.err);
      assertTrue(false);
    }
  }

  @Test
  public void testRepresentationInformationIndex() throws RODAException {
    RepresentationInformation ri = new RepresentationInformation();
    ri.setName("Portable Document Format");
    ri.setDescription("PDF definition");
    ri.setTags(Arrays.asList("Page Layout Files"));
    ri.setExtras("");
    ri.setSupport(RepresentationInformationSupport.SUPPORTED);
    model.createRepresentationInformation(ri, RodaConstants.ADMIN, false);
    index.commit(RepresentationInformation.class);

    RepresentationInformation ri2 = model.retrieveRepresentationInformation(ri.getId());
    assertNotNull(ri2);
    assertEquals(ri.getId(), ri2.getId());
    assertEquals(ri.getName(), ri2.getName());

    IndexResult<RepresentationInformation> find = index.find(RepresentationInformation.class, null, null,
      new Sublist(0, 10), Collections.emptyList());
    assertEquals(1, find.getTotalCount());

    RepresentationInformation ri3 = index.retrieve(RepresentationInformation.class, ri.getId(), new ArrayList<>());
    assertNotNull(ri3);
    assertEquals(ri.getId(), ri3.getId());
    assertEquals(ri.getName(), ri3.getName());

    ri3.setName("RepresentationInformation New Name");
    model.updateRepresentationInformation(ri3, RodaConstants.ADMIN, false);

    RepresentationInformation ri4 = index.retrieve(RepresentationInformation.class, ri.getId(), new ArrayList<>());
    assertNotNull(ri4);
    assertEquals(ri.getId(), ri4.getId());
    assertEquals(ri4.getName(), "RepresentationInformation New Name");

    model.deleteRepresentationInformation(ri.getId(), false);
  }

  @Test
  public void testMessageIndex() throws ConfigurationException, RODAException {
    Notification notification = new Notification();
    notification.setSubject("Message subject");
    notification.setBody("Message body");
    notification.setSentOn(new Date());
    notification.setFromUser("Test Message Index");
    notification.setRecipientUsers(Arrays.asList("recipientuser@example.com"));
    Notification n = model.createNotification(notification, new EmailNotificationProcessor("test-email-template.vm"));
    // notification state must be FAILED because SMTP is not configured on test
    // environment
    Assert.assertEquals(n.getState(), NOTIFICATION_STATE.FAILED);
    index.commit(Notification.class);

    Notification message2 = model.retrieveNotification(notification.getId());
    assertNotNull(message2);
    assertEquals(notification.getId(), message2.getId());
    assertEquals(notification.getSubject(), message2.getSubject());

    IndexResult<Notification> find = index.find(Notification.class, null, null, new Sublist(0, 10),
      Collections.emptyList());
    assertEquals(1, find.getTotalCount());

    Notification message3 = index.retrieve(Notification.class, notification.getId(), new ArrayList<>());
    assertNotNull(message3);
    assertEquals(notification.getId(), message3.getId());
    assertEquals(message3.getSubject(), message3.getSubject());

    message3.setSubject("Message New Subject");
    model.updateNotification(message3);

    Notification message4 = index.retrieve(Notification.class, notification.getId(), new ArrayList<>());
    assertNotNull(message4);
    assertEquals(notification.getId(), message4.getId());
    assertEquals(message4.getSubject(), "Message New Subject");

    model.deleteNotification(notification.getId());

  }

  @Test
  public void testIteration() throws RODAException, SolrServerException, IOException {

    // populate index
    for (int i = 0; i < 10000; i++) {
      String id = "id_" + i;
      AIP aip = new AIP();
      aip.setId(id);
      aip.setState(AIPState.ACTIVE);
      aip.setDescriptiveMetadata(new ArrayList<>());
      aip.setRepresentations(new ArrayList<>());
      aip.setPermissions(new Permissions());

      index.getSolrClient().add(SolrUtils.getIndexName(AIP.class).get(0),
        SolrUtils.aipToSolrInputDocument(aip, new ArrayList<>(), model, true));

      index.commit(IndexedAIP.class);

    }

    List<String> results = new ArrayList<>();

    IndexResult<IndexedAIP> find;
    int offset = 0;
    int blockSize = 100;
    do {
      find = RodaCoreFactory.getIndexService().find(IndexedAIP.class, Filter.ALL, Sorter.NONE,
        new Sublist(offset, blockSize), Collections.emptyList());
      offset += find.getLimit();

      // Add all ids to result list
      results.addAll(find.getResults().stream().map(aip -> aip.getId()).collect(Collectors.toList()));

    } while (find.getTotalCount() > find.getOffset() + find.getLimit());

    // check if they are all there
    for (int i = 0; i < 10000; i++) {
      String id = "id_" + i;
      Assert.assertTrue(results.contains(id), "Could not find expected id: " + id);
    }

    // check if none is repeated
    Set<String> set = new HashSet<>();
    set.addAll(results);
    Assert.assertEquals(results.size(), set.size());

  }
}
