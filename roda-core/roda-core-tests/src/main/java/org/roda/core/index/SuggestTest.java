/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index;

import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.select.SelectedItemsNone;
import org.roda.core.data.v2.jobs.IndexedJob;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.PluginType;
import org.roda.core.index.utils.SolrUtils;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * #3787: the text a user types in a suggestion box is searched as text, however
 * it is written (punctuation, query syntax, wildcards), a single character is
 * not expanded to every term starting with it, and only the fields the
 * collection defines can be asked for.
 *
 * The suggestions are those of the names of jobs (only the job documents are
 * indexed), the name being a field with a text copy, as the fields the user
 * interface asks suggestions for.
 */
@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_DEV, RodaConstants.TEST_GROUP_TRAVIS})
public class SuggestTest {
  private static final String ALPHA = "Ingest alpha";
  private static final String BETA = "Ingest beta";
  private static final String GAMMA = "Reindex (gamma)";
  private static final String VERSION = "Version 1";

  private static Path basePath;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;

  @BeforeClass
  public static void setUp() throws Exception {
    basePath = TestsHelper.createBaseTempDir(SuggestTest.class, true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = false;
    boolean deployPluginManager = false;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false, ldapUtilityTestHelper.getLdapUtility());
    index = RodaCoreFactory.getIndexService();

    for (String name : List.of(ALPHA, BETA, GAMMA, VERSION)) {
      Job job = new Job();
      job.setId(IdUtils.createUUID());
      job.setName(name);
      job.setUsername(RodaConstants.ADMIN);
      job.setStartDate(new Date());
      job.setPlugin("org.roda.core.plugins.test.TestPlugin");
      job.setPluginType(PluginType.MISC);
      job.setPluginParameters(new HashMap<>());
      job.setSourceObjects(new SelectedItemsNone<>());
      Assert.assertTrue(SolrUtils.create2(index.getSolrClient(), RodaCoreFactory.getModelService(), job,
        IndexedJob.class, job).isEmpty(), name);
    }
    index.commit(IndexedJob.class);
  }

  @AfterClass(alwaysRun = true)
  public static void tearDown() throws Exception {
    IndexTestUtils.resetIndex();
    ldapUtilityTestHelper.shutdown();
    RodaCoreFactory.shutdown();
    FSUtils.deletePath(basePath);
  }

  private static Set<String> suggest(String field, String query) throws RODAException {
    return new HashSet<>(index.suggest(IndexedJob.class, field, query, null, false, true));
  }

  private static Set<String> suggest(String query) throws RODAException {
    return suggest(RodaConstants.JOB_NAME, query);
  }

  @Test
  public void prefixesAreSuggested() throws RODAException {
    Assert.assertEquals(suggest("ing"), Set.of(ALPHA, BETA));
    Assert.assertEquals(suggest("Ingest al"), Set.of(ALPHA));
    Assert.assertEquals(suggest("gam"), Set.of(GAMMA));
  }

  @Test
  public void punctuationIsSearchedAsText() throws RODAException {
    for (String query : List.of("a AND (", "x) OR (y", "\"", "gamma)", "re:", "[", "alpha^2", "!", "{!")) {
      // no syntax error, whatever it suggests
      suggest(query);
    }
  }

  @Test
  public void querySyntaxAndWildcardsAreSearchedAsText() throws RODAException {
    Assert.assertEquals(suggest("*:*"), Set.of(), "*:*");
    Assert.assertEquals(suggest("*lpha"), Set.of(), "*lpha");
    Assert.assertEquals(suggest("al?ha"), Set.of(), "al?ha");
    Assert.assertEquals(suggest("x OR ingest"), Set.of(), "x OR ingest");
  }

  @Test
  public void aSingleCharacterIsNotExpanded() throws RODAException {
    // "a" would expand to every term starting with it: it only matches the term "a"
    Assert.assertEquals(suggest("a"), Set.of());
    Assert.assertEquals(suggest("al"), Set.of(ALPHA));
    // a single character term is still found
    Assert.assertEquals(suggest("1"), Set.of(VERSION));
  }

  @Test
  public void aSingleCharacterAfterOtherWordsIsExpanded() throws RODAException {
    // the other words already narrow the search, as when the user starts typing the next word
    Assert.assertEquals(suggest("Ingest a"), Set.of(ALPHA));
  }

  @Test
  public void onlyFieldsOfTheCollectionAreAccepted() {
    for (String field : List.of("nonexistent", "name_txt", "{!terms f=name}x", "name}")) {
      Assert.expectThrows(RequestNotValidException.class, () -> suggest(field, "ing"));
    }
  }
}
