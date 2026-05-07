package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.jobs.IndexedReport;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public enum JobReportAction implements Actionable.Action<IndexedReport> {
  BROWSE_SOURCE(RodaConstants.PERMISSION_METHOD_FIND_JOB_REPORT),
  BROWSE_OUTCOME(RodaConstants.PERMISSION_METHOD_FIND_JOB_REPORT);

  private final List<String> methods;

  JobReportAction(String... methods) {
    this.methods = Arrays.asList(methods);
  }

  @Override
  public List<String> getMethods() {
    return this.methods;
  }
}
