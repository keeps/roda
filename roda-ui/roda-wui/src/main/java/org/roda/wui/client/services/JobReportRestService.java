/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.services;

import org.roda.core.data.v2.jobs.IndexedReport;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Tag(name = "Job reports")
@RequestMapping(path = "../api/v2/job-reports")
public interface JobReportRestService extends RODAEntityRestService<IndexedReport> {
}
