package org.roda.wui.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.common.model.RequestHeaders;
import org.roda.wui.common.model.RequestContext;
import org.springframework.web.context.request.WebRequest;

public class RequestUtils {

  public static RequestContext parseWebRequest(WebRequest request) {
    RequestContext requestContext = new RequestContext();

    // get user


    return requestContext;
  }

  public static RequestContext parseHTTPRequest(HttpServletRequest request) {
    RequestContext requestContext = new RequestContext();

    // get user
    User user = UserUtility.getApiUser(request);

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
