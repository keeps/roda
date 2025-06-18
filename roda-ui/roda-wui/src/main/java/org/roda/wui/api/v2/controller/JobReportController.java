package org.roda.wui.api.v2.controller;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.client.services.JobReportRestService;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/job-reports")
public class JobReportController implements JobReportRestService, Exportable {
  @Autowired
  private HttpServletRequest request;

  @Autowired
  private IndexService indexService;

  @Override
  public IndexedReport findByUuid(String uuid, String localeString) {
    return indexService.retrieve(IndexedReport.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<IndexedReport> find(@RequestBody FindRequest findRequest, String localeString) {
    return indexService.find(IndexedReport.class, findRequest, localeString);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_JOB_REPORT)) {
      return new LongResponse(indexService.count(IndexedReport.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(SuggestRequest suggestRequest) {
    return indexService.suggest(suggestRequest, IndexedReport.class);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    // delegate
    return ApiUtils.okResponse(
      indexService.exportToCSV(findRequestString, IndexedReport.class));
  }
}