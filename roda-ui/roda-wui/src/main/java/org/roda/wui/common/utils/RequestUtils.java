package org.roda.wui.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.common.model.RequestHeaders;
import org.roda.wui.common.model.RequestController;

public class RequestUtils {

  public static RequestController parseHTTPRequest(HttpServletRequest request) {
    RequestController requestController = new RequestController();

    // get user
    User user = UserUtility.getApiUser(request);

    // get headers
    RequestHeaders headers = new RequestHeaders();
    headers.setUuid(request.getHeader("x-request-uuid"));
    headers.setReason(request.getHeader("x-request-reason"));
    headers.setType(request.getHeader("x-request-type"));

    requestController.setUser(user);
    requestController.setRequestHeaders(headers);

    return requestController;
  }

  private RequestUtils() {
    //empty constructor
  }
}
