package org.roda.wui.api.v2.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.client.services.AIPRestService;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/aips")
public class AIPController implements AIPRestService {

  @Autowired
  HttpServletRequest request;

  @Autowired
  IndexService indexService;

  @Override
  public IndexedAIP findByUuid(String uuid) {
    return null;
  }

  @Override
  public IndexResult<IndexedAIP> find(@RequestBody FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.find(IndexedAIP.class, findRequest, localeString, requestContext.getUser());
  }

  @Override
  public String count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return String.valueOf(
      indexService.count(IndexedAIP.class, countRequest.filter, countRequest.onlyActive, requestContext.getUser()));
  }
}
