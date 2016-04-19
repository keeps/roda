/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.IdUtils;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.agents.Agent;
import org.roda.core.data.v2.formats.Format;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions.PermissionType;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.StoragePath;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryParameter;
import org.roda.core.data.v2.messages.Message;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.Risk.SEVERITY_LEVEL;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.model.ModelService;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexServiceTest {

  private static Path basePath;
  private static Path logPath;
  private static ModelService model;
  private static IndexService index;

  private static Path corporaPath;
  private static StorageService corporaService;

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexServiceTest.class);

  @BeforeClass
  public static void setUp() throws Exception {

    basePath = Files.createTempDirectory("indexTests");
    System.setProperty("roda.home", basePath.toString());

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = false;
    boolean deployPluginManager = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager);

    logPath = RodaCoreFactory.getLogPath();
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = IndexServiceTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    LOGGER.debug("Running index tests under storage {}", basePath);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
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

    assertEquals(aip.isActive(), indexedAIP.getState().equals(AIPState.ACTIVE));
  }

  @Test
  public void testAIPIndexCreateDelete() throws RODAException, ParseException {
    // generate AIP ID
    final String aipId = UUID.randomUUID().toString();

    // Create AIP
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    index.commitAIPs();

    // Retrieve, count and list AIP
    final IndexedAIP indexedAip = index.retrieve(IndexedAIP.class, aipId);
    compareAIPWithIndexedAIP(aip, indexedAip);

    final IndexResult<IndexedAIP> indexAips = index.find(IndexedAIP.class, null, null, new Sublist(0, 10), null);
    assertEquals(1, indexAips.getTotalCount());
    assertEquals(1, indexAips.getLimit());
    assertEquals(0, indexAips.getOffset());
    assertEquals(1, indexAips.getResults().size());

    final IndexedAIP aipFromList = indexAips.getResults().get(0);
    assertEquals(aip.getId(), aipFromList.getId());
    assertEquals(aip.isActive(), AIPState.ACTIVE.equals(aipFromList.getState()));
    assertEquals(aip.getParentId(), aipFromList.getParentID());

    assertEquals(indexedAip, aipFromList);
    assertEquals("fonds", indexedAip.getLevel());
    assertEquals("My example", indexedAip.getTitle());
    assertEquals("This is a very nice example", indexedAip.getDescription());
    assertEquals(RodaUtils.parseDate("0001-01-01T00:00:00.000+0000"), indexedAip.getDateInitial());
    assertEquals(RodaUtils.parseDate("0002-01-01T00:00:00.000+0000"), indexedAip.getDateFinal());

    // Retrieve, count and list SRO
    String rep1Id = aip.getRepresentations().get(0).getId();
    IndexedRepresentation rep1 = index.retrieve(IndexedRepresentation.class,
      IdUtils.getRepresentationId(aipId, rep1Id));
    assertEquals(rep1Id, rep1.getId());

    Filter filterParentTheAIP = new Filter();
    filterParentTheAIP.add(new SimpleFilterParameter(RodaConstants.REPRESENTATION_AIP_ID, aipId));
    IndexResult<IndexedRepresentation> sros = index.find(IndexedRepresentation.class, filterParentTheAIP, null,
      new Sublist(0, 10), null);
    assertEquals(aip.getRepresentations().size(), sros.getTotalCount());

    List<String> sro_IDs = new ArrayList<>();
    for (Representation sro : sros.getResults()) {
      sro_IDs.add(sro.getId());
    }

    List<String> representationIds = aip.getRepresentations().stream().map(r -> r.getId()).collect(Collectors.toList());
    assertThat(sro_IDs, Matchers.contains(representationIds.toArray()));

    /*
     * filterMimetype.add(new SimpleFilterParameter(RodaConstants.SRFM_MIMETYPE,
     * CorporaConstants.TEXT_XML));
     * assertEquals(index.find(SimpleEventPreservationMetadata.class,
     * filterMimetype, null, new Sublist(0, 10)).getTotalCount(),1L);
     */

    /*
     * SimpleRepresentationPreservationMetadata srpm =
     * index.retrieveSimpleRepresentationPreservationMetadata(aipId,
     * CorporaConstants.REPRESENTATION_1_ID,CorporaConstants.
     * REPRESENTATION_PREMIS_XML); assertEquals(srpm.getAipId(), aipId);
     * assertEquals(srpm.getFileId(),CorporaConstants.
     * REPRESENTATION_PREMIS_XML); Filter filterFileId = new Filter();
     * filterFileId.add(new SimpleFilterParameter(RodaConstants.SRPM_FILE_ID,
     * CorporaConstants.REPRESENTATION_PREMIS_XML));
     * assertEquals(""+index.countSimpleRepresentationPreservationMetadata(
     * filterFileId),""+1L);
     * assertEquals(index.find(SimpleEventPreservationMetadata.class,
     * filterFileId, null, new Sublist(0, 10)).getTotalCount(),1L);
     */
    model.deleteAIP(aipId);
    try {
      index.retrieve(IndexedAIP.class, aipId);
      fail("AIP deleted but yet it was retrieved");
    } catch (NotFoundException e) {
      // do nothing as it was the expected exception
    } catch (RODAException e) {
      fail("AIP was deleted and therefore a " + NotFoundException.class.getName()
        + " should have been thrown instead of a " + e.getClass().getName());
    }
  }

  @Test
  public void testAIPIndexCreateDelete2() throws RODAException {
    final String aipId = UUID.randomUUID().toString();

    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID_3));

    index.commitAIPs();

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.AIP_ID, aipId));
    IndexedAIP indexedAip = index.find(IndexedAIP.class, filter, null, new Sublist(0, 10), null).getResults().get(0);
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
    final String aipId = UUID.randomUUID().toString();

    // testing AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    final StoragePath otherAipPath = DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER,
      CorporaConstants.OTHER_AIP_ID);
    final AIP updatedAIP = model.updateAIP(aipId, corporaService, otherAipPath);

    final IndexedAIP indexedAIP = index.retrieve(IndexedAIP.class, aipId);

    compareAIPWithIndexedAIP(updatedAIP, indexedAIP);

    model.deleteAIP(aipId);
  }

  @Test
  public void testListCollections() throws RODAException {
    // set up
    model.createAIP(CorporaConstants.SOURCE_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
    model.createAIP(CorporaConstants.OTHER_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.OTHER_AIP_ID));

    index.commitAIPs();

    long aipCount = index.count(IndexedAIP.class, IndexedAIP.FONDS_FILTER);
    assertEquals(1, aipCount);

    final IndexResult<IndexedAIP> aips = index.find(IndexedAIP.class, IndexedAIP.FONDS_FILTER, null, new Sublist(0, 10),
      null);

    assertEquals(1, aips.getLimit());
    assertEquals(CorporaConstants.SOURCE_AIP_ID, aips.getResults().get(0).getId());

    model.deleteAIP(CorporaConstants.SOURCE_AIP_ID);
    model.deleteAIP(CorporaConstants.OTHER_AIP_ID);
  }

  @Test
  public void testSubElements() throws RODAException {
    // set up
    model.createAIP(CorporaConstants.SOURCE_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
    model.createAIP(CorporaConstants.OTHER_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.OTHER_AIP_ID));

    index.commitAIPs();

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.AIP_PARENT_ID, CorporaConstants.SOURCE_AIP_ID));

    long aipCount = index.count(IndexedAIP.class, filter);
    assertEquals(1, aipCount);

    final IndexResult<IndexedAIP> aips = index.find(IndexedAIP.class, filter, null, new Sublist(0, 10), null);

    assertEquals(1, aips.getLimit());
    assertEquals(CorporaConstants.OTHER_AIP_ID, aips.getResults().get(0).getId());

    model.deleteAIP(CorporaConstants.SOURCE_AIP_ID);
    model.deleteAIP(CorporaConstants.OTHER_AIP_ID);
  }

  @Test
  public void testGetAncestors() throws RODAException {
    // set up
    model.createAIP(CorporaConstants.SOURCE_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));
    model.createAIP(CorporaConstants.OTHER_AIP_ID, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.OTHER_AIP_ID));

    index.commitAIPs();

    IndexedAIP aip = index.retrieve(IndexedAIP.class, CorporaConstants.OTHER_AIP_ID);
    List<IndexedAIP> ancestors = index.getAncestors(aip);
    assertThat(ancestors,
      Matchers.hasItem(Matchers.<IndexedAIP> hasProperty("id", Matchers.equalTo(CorporaConstants.SOURCE_AIP_ID))));
  }

  @Test
  public void testGetElementWithoutParentId() throws RODAException {
    // generate AIP ID
    final String aipId = UUID.randomUUID().toString();

    // Create AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.AIP_LEVEL, "fonds"));
    filter.add(new EmptyKeyFilterParameter(RodaConstants.AIP_PARENT_ID));
    IndexResult<IndexedAIP> findDescriptiveMetadata = index.find(IndexedAIP.class, filter, null, new Sublist(), null);

    assertNotNull(findDescriptiveMetadata);
    assertThat(findDescriptiveMetadata.getResults(), Matchers.hasSize(1));

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
    List<LogEntryParameter> parameters = new ArrayList<LogEntryParameter>();
    parameters.add(new LogEntryParameter("NAME1", "VALUE1"));
    parameters.add(new LogEntryParameter("NAME2", "VALUE2"));
    entry.setParameters(parameters);
    model.addLogEntry(entry, logPath);

    index.commit(LogEntry.class);

    Filter filterDescription = new Filter();
    filterDescription.add(new SimpleFilterParameter(RodaConstants.LOG_ID, "ID"));
    assertThat(index.count(LogEntry.class, filterDescription), Matchers.is(1L));

    Filter filterDescription2 = new Filter();
    filterDescription2.add(new SimpleFilterParameter(RodaConstants.LOG_ID, "ID2"));
    assertThat(index.count(LogEntry.class, filterDescription2), Matchers.is(0L));
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
    List<LogEntryParameter> parameters = new ArrayList<LogEntryParameter>();
    parameters.add(new LogEntryParameter("NAME1", "VALUE1"));
    parameters.add(new LogEntryParameter("NAME2", "VALUE2"));
    entry.setParameters(parameters);
    model.addLogEntry(entry, logPath);

    index.commit(LogEntry.class);

    Filter filterDescription = new Filter();
    filterDescription.add(new SimpleFilterParameter(RodaConstants.LOG_ID, "id"));

    IndexResult<LogEntry> entries = index.find(LogEntry.class, filterDescription, null, new Sublist(), null);
    assertEquals(entries.getTotalCount(), 1);
    assertEquals(entries.getResults().get(0).getActionComponent(), RodaConstants.LOG_ACTION_COMPONENT);

    Filter filterDescription2 = new Filter();
    filterDescription2.add(new SimpleFilterParameter(RodaConstants.LOG_ID, "id2"));

    IndexResult<LogEntry> entries2 = index.find(LogEntry.class, filterDescription2, null, new Sublist(), null);
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
      List<LogEntryParameter> parameters = new ArrayList<LogEntryParameter>();
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
    IndexResult<LogEntry> entries1 = index.find(LogEntry.class, f1, null, new Sublist(0, 10), null);
    assertThat(entries1.getTotalCount(), Matchers.is(1L));
    Filter f2 = new Filter();
    f2.add(new SimpleFilterParameter(RodaConstants.LOG_ADDRESS, "ADDRESS"));
    IndexResult<LogEntry> entries2 = index.find(LogEntry.class, f2, null, new Sublist(0, 10), null);
    assertThat(entries2.getTotalCount(), Matchers.is(number));
  }

  @Test
  public void testReindexAIP() throws ParseException, RequestNotValidException, GenericException,
    AuthorizationDeniedException, AlreadyExistsException, NotFoundException, ValidationException {
    for (int i = 0; i < 10; i++) {
      final String aipId = UUID.randomUUID().toString();
      model.createAIP(aipId, corporaService,
        DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID), false);
    }

    index.reindexAIPs();

    long count = index.count(IndexedAIP.class, new Filter());
    assertEquals(10L, count);

  }

  @Test
  public void indexMembers() throws AlreadyExistsException, GenericException, RequestNotValidException,
    EmailAlreadyExistsException, UserAlreadyExistsException, IllegalOperationException, NotFoundException {
    Set<String> groups = new HashSet<String>();
    groups.add("administrators");
    Set<String> roles = new HashSet<String>();
    roles.add("browse");

    for (int i = 0; i < 5; i++) {
      if (i % 2 == 0) {
        User user = new User();
        user.setActive(true);
        user.setAllGroups(groups);
        user.setAllRoles(roles);
        user.setDirectGroups(groups);
        user.setDirectRoles(roles);
        user.setEmail("mail@example.com");
        user.setGuest(false);
        user.setId("USER" + i);
        user.setName("NAMEUSER" + i);
        user.setFullName("NAMEUSER" + i);
        model.addUser(user, true, true);
      } else {
        Group group = new Group();
        group.setActive(true);
        group.setAllGroups(groups);
        group.setAllRoles(roles);
        group.setDirectGroups(groups);
        group.setDirectRoles(roles);
        group.setId("GROUP" + i);
        group.setName("NAMEGROUP" + i);
        group.setFullName("NAMEGROUP" + i);
        model.addGroup(group, true, true);
      }
    }

    index.commit(RODAMember.class);

    Filter filterUSER1 = new Filter();
    filterUSER1.add(new SimpleFilterParameter(RodaConstants.MEMBERS_NAME, "NAMEUSER0"));
    filterUSER1.add(new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, "true"));
    assertThat(index.count(RODAMember.class, filterUSER1), Matchers.is(1L));

    Filter filterGroup = new Filter();
    filterGroup.add(new SimpleFilterParameter(RodaConstants.MEMBERS_NAME, "NAMEGROUP1"));
    filterGroup.add(new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, "false"));
    assertThat(index.count(RODAMember.class, filterGroup), Matchers.is(1L));

  }

  @Test
  public void testIdWithComma() throws RODAException {
    // generate AIP ID
    final String origAipId = "id, with, comma";
    final String aipId = UUID.nameUUIDFromBytes(origAipId.getBytes()).toString();

    // Create AIP
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_ID));

    index.commitAIPs();

    IndexResult<IndexedAIP> find = index.find(IndexedAIP.class, null, null, new Sublist(0, 10));
    assertEquals(1, find.getTotalCount());

    IndexedAIP aip = index.retrieve(IndexedAIP.class, UUID.nameUUIDFromBytes(origAipId.getBytes()).toString());
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
      risk.setPreMitigationSeverityLevel(SEVERITY_LEVEL.High);
      risk.setPreMitigationNotes("Pre Notes");

      risk.setPosMitigationProbability(3);
      risk.setPosMitigationImpact(2);
      risk.setPosMitigationSeverity(6);
      risk.setPosMitigationSeverityLevel(SEVERITY_LEVEL.Moderate);
      risk.setPosMitigationNotes("Pos Notes");

      risk.setMitigationStrategy("Mitigation Strategy");
      risk.setMitigationOwnerType("Owner type");
      risk.setMitigationOwner("Owner");
      risk.setMitigationRelatedEventIdentifierType("Mitigation REI type");
      risk.setMitigationRelatedEventIdentifierValue("Mitigation REI value");

      HashMap<String, String> affectedObjects = new HashMap<String, String>();
      affectedObjects.put("Affected related type", "Affected related value");
      risk.setAffectedObjects(affectedObjects);
      model.createRisk(risk);

      index.commit(Risk.class);

      Risk risk2 = model.retrieveRisk(risk.getId());
      assertNotNull(risk2);
      assertEquals(risk.getId(), risk2.getId());
      assertEquals(risk.getName(), risk2.getName());

      IndexResult<Risk> find = index.find(Risk.class, null, null, new Sublist(0, 10));
      assertEquals(1, find.getTotalCount());

      Risk risk3 = index.retrieve(Risk.class, risk.getId());
      assertNotNull(risk3);
      assertEquals(risk.getId(), risk3.getId());
      assertEquals(risk.getName(), risk3.getName());

      risk3.setName("Risk New Name");
      model.updateRisk(risk3, "Risk updated");

      Risk risk4 = index.retrieve(Risk.class, risk.getId());
      assertNotNull(risk4);
      assertEquals(risk.getId(), risk4.getId());
      assertEquals(risk4.getName(), "Risk New Name");

      model.deleteRisk(risk.getId());

    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  @Test
  public void testAgentIndex() {
    try {
      Agent agent = new Agent();
      agent.setName("Acrobat reader");
      agent.setType("Software");
      agent.setDescription("Agent description");
      agent.setCategory("Desktop publishing");
      agent.setVersion("1.7");
      agent.setLicense("Proprietary");
      agent.setPopularity(5);
      agent.setDeveloper("Adobe Systems");
      agent.setInitialRelease(new Date());
      agent.setWebsite("acrobat.adobe.com");
      agent.setDownload("https://get.adobe.com/br/reader/");
      agent.setProvenanceInformation("https://en.wikipedia.org/wiki/Adobe_Acrobat");

      List<String> platforms = new ArrayList<String>();
      platforms.add("Windows");
      platforms.add("MAC OS X");
      platforms.add("Linux");
      agent.setPlatforms(platforms);

      List<String> extensions = new ArrayList<String>();
      extensions.add(".pdf");
      agent.setExtensions(extensions);

      List<String> mimetypes = new ArrayList<String>();
      mimetypes.add("application/pdf");
      agent.setMimetypes(mimetypes);

      List<String> pronoms = new ArrayList<String>();
      pronoms.add("fmt/100");
      agent.setPronoms(pronoms);

      List<String> utis = new ArrayList<String>();
      utis.add("com.adobe.pdf");
      agent.setUtis(utis);

      List<String> formatIds = new ArrayList<String>();
      formatIds.add("format1");
      agent.setFormatIds(formatIds);

      model.createAgent(agent);

      Agent agent2 = model.retrieveAgent(agent.getId());
      assertNotNull(agent2);
      assertEquals(agent.getId(), agent2.getId());
      assertEquals(agent.getName(), agent2.getName());

      index.commit(Agent.class);

      IndexResult<Agent> find = index.find(Agent.class, null, null, new Sublist(0, 10));
      assertEquals(1, find.getTotalCount());

      Agent agent3 = index.retrieve(Agent.class, agent.getId());
      assertNotNull(agent3);
      assertEquals(agent.getId(), agent3.getId());
      assertEquals(agent.getName(), agent3.getName());

      agent3.setName("Agent New Name");
      model.updateAgent(agent3);

      Agent agent4 = index.retrieve(Agent.class, agent.getId());
      assertNotNull(agent4);
      assertEquals(agent.getId(), agent4.getId());
      assertEquals(agent4.getName(), "Agent New Name");

      model.deleteAgent(agent.getId());

    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      assertTrue(false);
    }
  }

  @Test
  public void testFormatIndex() {
    try {
      Format format = new Format();
      format.setName("Portable Document Format");
      format.setDefinition("PDF definition");
      format.setCategory("Page Layout Files");
      format.setLatestVersion("1.7");
      format.setPopularity(4);
      format.setDeveloper("Adobe Systems");
      format.setInitialRelease(new Date());
      format.setStandard("ISO 32000-1");
      format.setOpenFormat(true);
      format.setWebsite("https://www.adobe.com/devnet/pdf/pdf_reference_archive.html");
      format.setProvenanceInformation("https://en.wikipedia.org/wiki/Portable_Document_Format");

      List<String> extensions = new ArrayList<String>();
      extensions.add(".pdf");
      format.setExtensions(extensions);

      List<String> mimetypes = new ArrayList<String>();
      mimetypes.add("application/pdf");
      format.setMimetypes(mimetypes);

      List<String> pronoms = new ArrayList<String>();
      pronoms.add("fmt/100");
      format.setPronoms(pronoms);

      List<String> utis = new ArrayList<String>();
      utis.add("com.adobe.pdf");
      format.setUtis(utis);

      model.createFormat(format);

      index.commit(Format.class);

      Format format2 = model.retrieveFormat(format.getId());
      assertNotNull(format2);
      assertEquals(format.getId(), format2.getId());
      assertEquals(format.getName(), format2.getName());

      IndexResult<Format> find = index.find(Format.class, null, null, new Sublist(0, 10));
      assertEquals(1, find.getTotalCount());

      Format format3 = index.retrieve(Format.class, format.getId());
      assertNotNull(format3);
      assertEquals(format.getId(), format3.getId());
      assertEquals(format.getName(), format3.getName());

      format3.setName("Format New Name");
      model.updateFormat(format3);

      Format format4 = index.retrieve(Format.class, format.getId());
      assertNotNull(format4);
      assertEquals(format.getId(), format4.getId());
      assertEquals(format4.getName(), "Format New Name");

      model.deleteFormat(format.getId());

    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      assertTrue(false);
    }
  }

  @Test
  public void testMessageIndex() throws ConfigurationException {
    try {
      Message message = new Message();
      message.setSubject("Message subject");
      message.setBody("Message body");
      message.setSentOn(new Date());
      message.setFromUser("Test Message Index");
      message.setRecipientUsers(Arrays.asList("recipientuser@example.com"));

      model.createMessage(message, "test-email-template", new HashMap<String, Object>());
      index.commit(Message.class);

      Message message2 = model.retrieveMessage(message.getId());
      assertNotNull(message2);
      assertEquals(message.getId(), message2.getId());
      assertEquals(message.getSubject(), message2.getSubject());

      IndexResult<Message> find = index.find(Message.class, null, null, new Sublist(0, 10));
      assertEquals(1, find.getTotalCount());

      Message message3 = index.retrieve(Message.class, message.getId());
      assertNotNull(message3);
      assertEquals(message.getId(), message3.getId());
      assertEquals(message3.getSubject(), message3.getSubject());

      message3.setSubject("Message New Subject");
      model.updateMessage(message3);

      Message message4 = index.retrieve(Message.class, message.getId());
      assertNotNull(message4);
      assertEquals(message.getId(), message4.getId());
      assertEquals(message4.getSubject(), "Message New Subject");

      model.deleteMessage(message.getId());

    } catch (GenericException | RequestNotValidException | NotFoundException | AuthorizationDeniedException e) {
      assertTrue(false);
    }
  }
}
