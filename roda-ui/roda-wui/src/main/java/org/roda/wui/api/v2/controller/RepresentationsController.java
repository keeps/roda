package org.roda.wui.api.v2.controller;

import java.util.ArrayList;

import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.client.services.RepresentationRestService;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/representations")
public class RepresentationsController implements RepresentationRestService {

  @Autowired
  HttpServletRequest request;

  @Autowired
  IndexService indexService;

  @Override
  public IndexedRepresentation findByUuid(String uuid, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.retrieve(requestContext, IndexedRepresentation.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<IndexedRepresentation> find(FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.find(IndexedRepresentation.class, findRequest, localeString, requestContext);
  }

  @Override
  public LongResponse count(CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return new LongResponse(indexService.count(IndexedRepresentation.class, countRequest, requestContext));
  }
}
