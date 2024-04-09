package org.roda.wui.client.services;

import org.roda.core.data.v2.jobs.IndexedReport;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Tag(name = "v2 job-reports")
@RequestMapping(path = "../api/v2/job-reports")
public interface JobReportRestService extends RODAEntityRestService<IndexedReport> {
}
