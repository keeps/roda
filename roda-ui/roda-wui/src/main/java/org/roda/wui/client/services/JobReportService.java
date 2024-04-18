package org.roda.wui.client.services;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Path;
import org.roda.core.data.v2.jobs.IndexedReport;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Path("../api/v2/job-report/")
@Tag(name = "v2")
public interface JobReportService extends RODAEntityService<IndexedReport> {
}
