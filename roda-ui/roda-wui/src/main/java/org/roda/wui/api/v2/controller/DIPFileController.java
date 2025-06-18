package org.roda.wui.api.v2.controller;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.client.services.DIPFileRestService;
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
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/dip-files")
public class DIPFileController implements DIPFileRestService, Exportable {
  @Autowired
  HttpServletRequest request;

  @Autowired
  IndexService indexService;

  @Override
  public DIPFile findByUuid(String uuid, String localeString) {
    return indexService.retrieve(DIPFile.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<DIPFile> find(@RequestBody FindRequest findRequest, String localeString) {
    return indexService.find(DIPFile.class, findRequest, localeString);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_AIP)) {
      return new LongResponse(indexService.count(DIPFile.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(SuggestRequest suggestRequest) {
    return indexService.suggest(suggestRequest, DIPFile.class);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    // delegate
    return ApiUtils.okResponse(indexService.exportToCSV(requestContext.getUser(), findRequestString, DIPFile.class));
  }
}
