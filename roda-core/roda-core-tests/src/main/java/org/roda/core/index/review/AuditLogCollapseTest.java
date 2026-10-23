/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.index.review;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;

import org.roda.core.RodaCoreFactory;
import org.roda.core.TestsHelper;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.collapse.Collapse;
import org.roda.core.data.v2.index.collapse.NullPolicyEnum;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.log.AuditLogRequestHeaders;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.security.LdapUtilityTestHelper;
import org.roda.core.storage.fs.FSUtils;
import org.roda.core.util.IdUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * #3784: the audit log collapse bug on a sharded audit log.
 * <p>
 * The audit log list collapses entries by request ({@code requestHeaderUUID}),
 * but {@code {!collapse}} only groups the documents of each shard, and log
 * entries got random ids, so the entries of one request spread over the shards
 * and a request showed up once per shard. Entries now take the request id as a
 * prefix of their id ({@code <request>!<random>}), which routes the entries of
 * a request to one shard.
 * <p>
 * It runs against a Solr of its own ({@link ShardedSolr}) with
 * {@code SOLR_NUM_SHARDS=4}, so the audit log has 4 shards with or without the
 * per-collection shard settings.
 */
@Test(groups = {SolrReview.GROUP})
public class AuditLogCollapseTest {
  private static final int SHARDS = 4;

  private static ShardedSolr solr;
  private static Path basePath;
  private static ModelService model;
  private static IndexService index;
  private static LdapUtilityTestHelper ldapUtilityTestHelper;

  @BeforeClass
  public static void setUp() throws Exception {
    solr = ShardedSolr.start(SHARDS);
    basePath = TestsHelper.createBaseTempDir(AuditLogCollapseTest.class, true);
    ldapUtilityTestHelper = new LdapUtilityTestHelper();

    boolean deploySolr = true;
    boolean deployLdap = true;
    boolean deployFolderMonitor = false;
    boolean deployOrchestrator = false;
    boolean deployPluginManager = false;
    boolean deployDefaultResources = false;
    RodaCoreFactory.instantiateTest(deploySolr, deployLdap, deployFolderMonitor, deployOrchestrator,
      deployPluginManager, deployDefaultResources, false, ldapUtilityTestHelper.getLdapUtility());

    model = RodaCoreFactory.getModelService();
    index = RodaCoreFactory.getIndexService();
  }

  @AfterClass(alwaysRun = true)
  public static void tearDown() throws Exception {
    try {
      if (ldapUtilityTestHelper != null) {
        ldapUtilityTestHelper.shutdown();
      }
      RodaCoreFactory.shutdown();
      if (basePath != null) {
        FSUtils.deletePath(basePath);
      }
    } finally {
      if (solr != null) {
        solr.close();
      }
    }
  }


  @Test
  public void auditLogCollapseShowsEachRequestOnce() throws Exception {
    int requests = 100;
    int entriesPerRequest = 5;
    // the entries of this test only, whatever else is in the audit log
    String component = AuditLogCollapseTest.class.getSimpleName() + "-" + IdUtils.createUUID();
    for (int r = 0; r < requests; r++) {
      String requestUuid = IdUtils.createUUID();
      for (int e = 0; e < entriesPerRequest; e++) {
        model.addLogEntry(logEntry(component, requestUuid), RodaCoreFactory.getLogPath());
      }
    }
    index.commit(LogEntry.class);

    Filter entries = new Filter(new SimpleFilterParameter(RodaConstants.LOG_ACTION_COMPONENT, component));
    Assert.assertEquals(index.count(LogEntry.class, entries), (long) requests * entriesPerRequest,
      "every entry is indexed");

    // as the audit log list (LogEntryList) asks for it
    Collapse byRequest = new Collapse();
    byRequest.setField(RodaConstants.LOG_REQUEST_HEADER_UUID);
    byRequest.setNullPolicy(NullPolicyEnum.EXPAND);
    FindRequest findRequest = FindRequest.getBuilder(entries, false).withSublist(new Sublist(0, 1000))
      .withCollapse(byRequest).build();
    IndexResult<LogEntry> collapsed = index.find(LogEntry.class, findRequest, null);
    Assert.assertEquals(collapsed.getTotalCount(), requests,
      "the collapsed audit log shows each of the " + requests + " requests once");

    // an entry is still found by its id
    LogEntry entry = collapsed.getResults().get(0);
    Assert.assertEquals(index.retrieve(LogEntry.class, entry.getUUID(), Collections.emptyList()).getUUID(),
      entry.getUUID());
  }

  /** An entry as ControllerAssistantUtils creates it for a REST call made in a request. */
  private static LogEntry logEntry(String component, String requestUuid) {
    LogEntry entry = new LogEntry();
    entry.setUUID(IdUtils.createUUID());
    entry.setAddress("127.0.0.1");
    entry.setUsername(RodaConstants.ADMIN);
    entry.setActionComponent(component);
    entry.setActionMethod("call");
    entry.setParameters(Collections.emptyList());
    entry.setDuration(1);
    entry.setDatetime(new Date());
    entry.setState(LogEntryState.SUCCESS);
    entry.setAuditLogRequestHeaders(new AuditLogRequestHeaders(requestUuid, "reason", "type"));
    return entry;
  }
}
