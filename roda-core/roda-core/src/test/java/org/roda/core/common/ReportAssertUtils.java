package org.roda.core.common;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.jobs.Report.PluginState;


public class ReportAssertUtils {
  public static void assertReports(List<Report> reports, List<String> outcomeObjectIds) {
    assertReports(reports, outcomeObjectIds, null);
  }

  public static void assertReports(List<Report> reports, List<String> outcomeObjectIds, List<String> sourceObjectIds) {
    if (outcomeObjectIds != null) {
      Assert.assertEquals(outcomeObjectIds.size(), reports.size());
    } else if (sourceObjectIds != null) {
      Assert.assertEquals(sourceObjectIds.size(), reports.size());
    }

    for (Report report : reports) {
      if (!PluginState.SUCCESS.equals(report.getPluginState())) {
        Assert.fail("Report failure: " + report);
      }

      if (outcomeObjectIds != null && StringUtils.isNotBlank(report.getOutcomeObjectId())) {
        Assert.assertThat(report.getOutcomeObjectId(), Matchers.isIn(outcomeObjectIds));
      }

      if (sourceObjectIds != null && StringUtils.isNotBlank(report.getSourceObjectId())) {
        Assert.assertThat(report.getSourceObjectId(), Matchers.isIn(sourceObjectIds));
      }

      // assert sub-reports
      if (report.getReports().size() > 0) {
        assertReports(report.getReports(), outcomeObjectIds, sourceObjectIds);
      }
    }
  }
}
