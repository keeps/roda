package org.roda.wui.api.v2.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.client.services.DIPRestService;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 * @author António Lindo <alindo@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/dips")
public class DIPController implements DIPRestService {

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private IndexService indexService;

  @Override
  public IndexedDIP findByUuid(String uuid, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.retrieve(requestContext, IndexedDIP.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<IndexedDIP> find(@RequestBody FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.find(IndexedDIP.class, findRequest, localeString, requestContext);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return new LongResponse(indexService.count(IndexedDIP.class, countRequest, requestContext));
  }
}
