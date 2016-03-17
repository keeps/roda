/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.core.model.ModelService;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.storage.fs.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PermissionsTest {

  private static Path basePath;
  private static Path logPath;
  private static ModelService model;
  private static IndexService index;

  private static Path corporaPath;
  private static StorageService corporaService;

  private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsTest.class);

  @BeforeClass
  public static void setUp() throws Exception {

    basePath = Files.createTempDirectory("indexTests");
    System.setProperty("roda.home", basePath.toString());

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator);

    logPath = RodaCoreFactory.getLogPath();
    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = PermissionsTest.class.getResource("/corpora");
    corporaPath = Paths.get(corporaURL.toURI());
    corporaService = new FileStorageService(corporaPath);

    LOGGER.debug("Running index tests under storage {}", basePath);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  @Test
  public void testAIPIndexCreateDelete() throws RODAException, ParseException {
    // generate AIP ID
    final String aipId = UUID.randomUUID().toString();

    // Create AIP
    // TODO move aip id to constants
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_PERMISSIONS));

    RodaUser user = null;
    boolean showInactive = false;
    IndexResult<IndexedAIP> find1 = index.find(IndexedAIP.class, null, null, new Sublist(0, 10), null, user,
      showInactive);
    assertEquals(0, find1.getTotalCount());

    showInactive = true;
    IndexResult<IndexedAIP> find2 = index.find(IndexedAIP.class, null, null, new Sublist(0, 10), null, user,
      showInactive);
    assertEquals(1, find2.getTotalCount());

    user = new RodaUser("testuser", "User with access", "", false);
    showInactive = false;
    IndexResult<IndexedAIP> find3 = index.find(IndexedAIP.class, null, null, new Sublist(0, 10), null, user,
      showInactive);
    assertEquals(0, find3.getTotalCount());

    showInactive = true;
    IndexResult<IndexedAIP> find4 = index.find(IndexedAIP.class, null, null, new Sublist(0, 10), null, user,
      showInactive);
    assertEquals(1, find4.getTotalCount());

    user = new RodaUser("guest", "User with access", "", true);
    showInactive = false;
    IndexResult<IndexedAIP> find5 = index.find(IndexedAIP.class, null, null, new Sublist(0, 10), null, user,
      showInactive);
    assertEquals(0, find5.getTotalCount());

    showInactive = true;
    IndexResult<IndexedAIP> find6 = index.find(IndexedAIP.class, null, null, new Sublist(0, 10), null, user,
      showInactive);
    assertEquals(0, find6.getTotalCount());

    user.addGroup("testgroup");
    IndexResult<IndexedAIP> find7 = index.find(IndexedAIP.class, null, null, new Sublist(0, 10), null, user,
      showInactive);
    assertEquals(1, find7.getTotalCount());

  }

  @Test
  public void testRepresentationIndexCreateDelete() throws RODAException, ParseException {
    // generate AIP ID
    final String aipId = UUID.randomUUID().toString();

    // Create AIP
    // TODO move aip id to constants
    final AIP aip = model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_PERMISSIONS));

    RodaUser user = null;
    boolean showInactive = false;
    IndexResult<IndexedRepresentation> find1 = index.find(IndexedRepresentation.class, null, null, new Sublist(0, 10),
      null, user, showInactive);
    assertEquals(0, find1.getTotalCount());

    showInactive = true;
    IndexResult<IndexedRepresentation> find2 = index.find(IndexedRepresentation.class, null, null, new Sublist(0, 10),
      null, user, showInactive);
    assertEquals(2, find2.getTotalCount());

    user = new RodaUser("testuser", "User with access", "", false);
    showInactive = false;
    IndexResult<IndexedRepresentation> find3 = index.find(IndexedRepresentation.class, null, null, new Sublist(0, 10),
      null, user, showInactive);
    assertEquals(0, find3.getTotalCount());

    showInactive = true;
    IndexResult<IndexedRepresentation> find4 = index.find(IndexedRepresentation.class, null, null, new Sublist(0, 10),
      null, user, showInactive);
    assertEquals(2, find4.getTotalCount());

    user = new RodaUser("guest", "User with access", "", true);
    showInactive = false;
    IndexResult<IndexedRepresentation> find5 = index.find(IndexedRepresentation.class, null, null, new Sublist(0, 10),
      null, user, showInactive);
    assertEquals(0, find5.getTotalCount());

    showInactive = true;
    IndexResult<IndexedRepresentation> find6 = index.find(IndexedRepresentation.class, null, null, new Sublist(0, 10),
      null, user, showInactive);
    assertEquals(0, find6.getTotalCount());

    user.addGroup("testgroup");
    IndexResult<IndexedRepresentation> find7 = index.find(IndexedRepresentation.class, null, null, new Sublist(0, 10),
      null, user, showInactive);
    assertEquals(2, find7.getTotalCount());

  }

}
