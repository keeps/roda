/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import static org.testng.AssertJUnit.assertEquals;

import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.CorporaConstants;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.AndFiltersParameters;
import org.roda.core.data.v2.index.filter.ChildOfFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.DefaultStoragePath;
import org.roda.core.storage.StorageService;
import org.roda.core.storage.fs.FileStorageService;
import org.roda.core.util.IdUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Regression tests for nested/block-join document search (helpdesk #123903, GitHub #3737):
 * ChildOfFilterParameter queries against IndexedAIP must enforce permissions correctly for
 * non-admin users, while admin keeps bypassing permission checks (as it does everywhere else).
 *
 * The fixture AIP grants READ only to "testgroup" — admin has NO explicit grant, so any test
 * where admin still gets results is specifically exercising the admin bypass, not a fixture
 * permission it happens to hold.
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class NestedDocumentsPermissionsTest {

  private static ModelService model;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;
  private static StorageService corporaService;

  @BeforeClass
  public static void setUp() throws Exception {
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    RodaCoreFactory.instantiateTest(true, true, false, false, false, false, false,
      ldapUtilityTestHelper.getLdapUtility());

    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();

    URL corporaURL = NestedDocumentsPermissionsTest.class.getResource("/corpora");
    corporaService = new FileStorageService(Paths.get(corporaURL.toURI()));
  }

  @BeforeMethod
  public static void resetIndex() {
    IndexTestUtils.resetIndex();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
  }

  private String createNestedAIP() throws RODAException {
    String aipId = IdUtils.createUUID();
    model.createAIP(aipId, corporaService,
      DefaultStoragePath.parse(CorporaConstants.SOURCE_AIP_CONTAINER, CorporaConstants.SOURCE_AIP_NESTED_PERMISSIONS),
      RodaConstants.ADMIN);
    index.commitAIPs();
    return aipId;
  }

  private Filter wrappedChildOfFilter(String perIdentityNo1) {
    ChildOfFilterParameter childOfFilter = new ChildOfFilterParameter(null,
      new SimpleFilterParameter("content_type", "BOB"));

    List<FilterParameter> filterParams = new ArrayList<>();
    filterParams.add(childOfFilter);
    filterParams.add(new SimpleFilterParameter("content_type", "prelDecision"));
    filterParams.add(new SimpleFilterParameter("perIdentityNo1_txt", perIdentityNo1));

    return new Filter(new AndFiltersParameters(filterParams));
  }

  /**
   * Core regression test: non-admin users only. Reproduces the exact query shape from helpdesk
   * #123903 (ChildOfFilterParameter wrapped in AndFiltersParameters alongside child-level search
   * conditions). A user in the granted group must get the matching child; a user outside it must
   * get nothing.
   */
  @Test
  public void testNestedDocumentsPermissionEnforcement() throws RODAException {
    String aipId = createNestedAIP();

    User testgroupUser = new User("testuser1", "Test User 1", "", false);
    testgroupUser.addGroup("testgroup");
    IndexResult<IndexedAIP> testgroupResult = index.find(IndexedAIP.class,
      FindRequest.getBuilder(wrappedChildOfFilter("197901022397"), false).build(), testgroupUser);
    assertEquals("User in the granted group should get the matching prelDecision child", 1,
      testgroupResult.getTotalCount());

    User unrelatedUser = new User("nogroup", "User with no group", "", false);
    unrelatedUser.addGroup("unrelatedgroup");
    IndexResult<IndexedAIP> unrelatedResult = index.find(IndexedAIP.class,
      FindRequest.getBuilder(wrappedChildOfFilter("197901022397"), false).build(), unrelatedUser);
    assertEquals("User not in the granted group should get no results", 0, unrelatedResult.getTotalCount());

    model.deleteAIP(aipId);
  }

  /**
   * Admin has no explicit permission grant on the fixture AIP. This proves the nested-document
   * permission-injection path (applyNestedDocumentsPermissions) bypasses admin, mirroring the
   * existing top-level bypass in getFilterQueryPermissions.
   */
  @Test
  public void testAdminBypassNestedSearch() throws RODAException {
    String aipId = createNestedAIP();

    User admin = new User(RodaConstants.ADMIN, RodaConstants.ADMIN, "", false);
    IndexResult<IndexedAIP> adminResult = index.find(IndexedAIP.class,
      FindRequest.getBuilder(wrappedChildOfFilter("197901022397"), false).build(), admin);
    assertEquals("Admin should get the matching prelDecision child despite no explicit permission grant", 1,
      adminResult.getTotalCount());

    model.deleteAIP(aipId);
  }

  /**
   * Same fixture, but a plain top-level AIP search (no ChildOfFilterParameter at all). Regression
   * guard for the pre-existing top-level permission bypass, unrelated to this fix — and, since an
   * unscoped Filter.ALL is used, also a regression guard for the nested-child-document exclusion
   * (NOT_NESTED_DOCUMENT_FILTER_QUERY): without it, this would match 3 documents (the parent AIP
   * plus its 2 prelDecision children), since nested child documents are still flat Lucene
   * documents in the same Solr collection.
   */
  @Test
  public void testAdminBypassNonNestedSearch() throws RODAException {
    String aipId = createNestedAIP();

    User admin = new User(RodaConstants.ADMIN, RodaConstants.ADMIN, "", false);
    IndexResult<IndexedAIP> adminResult = index.find(IndexedAIP.class,
      FindRequest.getBuilder(Filter.ALL, false).build(), admin);
    assertEquals("Admin should find only the parent AIP via plain top-level search, not its nested children", 1,
      adminResult.getTotalCount());

    model.deleteAIP(aipId);
  }

  /**
   * Regression guard: a bare (non-AND-wrapped) ChildOfFilterParameter — the shape that already
   * worked before this fix — must keep working.
   */
  @Test
  public void testNestedDocumentsWithBareChildOfFilter() throws RODAException {
    String aipId = createNestedAIP();

    ChildOfFilterParameter childOfFilter = new ChildOfFilterParameter(null,
      new SimpleFilterParameter("content_type", "BOB"));
    Filter bareFilter = new Filter(childOfFilter);

    User testgroupUser = new User("testuser2", "Test User 2", "", false);
    testgroupUser.addGroup("testgroup");
    IndexResult<IndexedAIP> result = index.find(IndexedAIP.class, FindRequest.getBuilder(bareFilter, false).build(),
      testgroupUser);
    assertEquals("User in testgroup should get both prelDecision children from a bare ChildOfFilterParameter", 2,
      result.getTotalCount());

    model.deleteAIP(aipId);
  }

  /**
   * ChildOfFilterParameter wrapped in AND with a single sibling condition (no admin, no
   * perIdentityNo1_txt scoping) — exercises the recursive detection at a different filter shape,
   * for both a permitted and a non-permitted user.
   */
  @Test
  public void testNestedDocumentsWithDeeplyNestedChildOfFilter() throws RODAException {
    String aipId = createNestedAIP();

    ChildOfFilterParameter childOfFilter = new ChildOfFilterParameter(null,
      new SimpleFilterParameter("content_type", "BOB"));
    List<FilterParameter> innerParams = List.of(childOfFilter, new SimpleFilterParameter("content_type", "prelDecision"));
    Filter nestedFilter = new Filter(new AndFiltersParameters(innerParams));

    User testgroupUser = new User("testuser3", "Test User 3", "", false);
    testgroupUser.addGroup("testgroup");
    IndexResult<IndexedAIP> result = index.find(IndexedAIP.class, FindRequest.getBuilder(nestedFilter, false).build(),
      testgroupUser);
    assertEquals("User in testgroup should get both prelDecision children", 2, result.getTotalCount());

    User unrelatedUser = new User("nogroup2", "User with no group", "", false);
    unrelatedUser.addGroup("othergroup");
    IndexResult<IndexedAIP> unrelatedResult = index.find(IndexedAIP.class,
      FindRequest.getBuilder(nestedFilter, false).build(), unrelatedUser);
    assertEquals("User not in granted group should get no results", 0, unrelatedResult.getTotalCount());

    model.deleteAIP(aipId);
  }
}
