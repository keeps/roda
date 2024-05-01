package org.roda.wui.api.v2.controller;

import java.util.ArrayList;

import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.client.services.JobReportRestService;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/job-report")
@Tag(name = JobReportController.SWAGGER_ENDPOINT)
public class JobReportController implements JobReportRestService {
  public static final String SWAGGER_ENDPOINT = "v2 job-reports";

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private IndexService indexService;

  @Override
  public IndexedReport findByUuid(String uuid) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.retrieve(requestContext, IndexedReport.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<IndexedReport> find(@RequestBody FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.find(IndexedReport.class, findRequest, localeString, requestContext);
  }

  @Override
  public Long count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.count(IndexedReport.class, countRequest, requestContext.getUser());
  }
}