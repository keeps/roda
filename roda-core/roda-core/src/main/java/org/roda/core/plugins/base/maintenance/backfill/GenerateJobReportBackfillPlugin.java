package org.roda.core.plugins.base.maintenance.backfill;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.plugins.Plugin;
import org.roda.core.util.IdUtils;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class GenerateJobReportBackfillPlugin extends GenerateRODAEntityBackfillPlugin<Report> {

  @Override
  protected <I extends IsIndexed> Class<I> getIndexClass() {
    return (Class<I>) IndexedReport.class;
  }

  @Override
  protected String getObjectId(Report object) {
    return IdUtils.getJobReportId(object.getJobId(), object.getSourceObjectId(), object.getOutcomeObjectId());
  }

  @Override
  public Plugin<Report> cloneMe() {
    return new GenerateJobReportBackfillPlugin();
  }

  @Override
  public List<Class<Report>> getObjectClasses() {
    return List.of(Report.class);
  }
}
