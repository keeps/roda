package org.roda.wui.common.utils;

import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.model.RequestHeaders;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtils {

  public static RequestContext parseHTTPRequest(HttpServletRequest request) {
    RequestContext requestContext = new RequestContext();

    // get user
    User user = UserUtility.getUser(request, true);

    // get headers
    RequestHeaders headers = new RequestHeaders();
    headers.setUuid(request.getHeader("x-request-uuid"));
    headers.setReason(request.getHeader("x-request-reason"));
    headers.setType(request.getHeader("x-request-type"));

    requestContext.setUser(user);
    requestContext.setRequestHeaders(headers);

    return requestContext;
  }

  private RequestUtils() {
    //empty constructor
  }
}
