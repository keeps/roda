package org.roda.wui.api.v2.controller;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotImplementedException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.StringResponse;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.notifications.NotificationAcknowledgeRequest;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.NotificationsService;
import org.roda.wui.api.v2.utils.ApiUtils;
import org.roda.wui.client.services.NotificationRestService;
import org.roda.wui.common.RequestControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/api/v2/notifications")
public class NotificationController implements NotificationRestService, Exportable {

  @Autowired
  private NotificationsService notificationsService;

  @Autowired
  private IndexService indexService;

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private RequestHandler requestHandler;

  @Override
  public Notification getNotification(String notificationId) {
    return requestHandler.processRequest(new RequestHandler.RequestProcessor<Notification>() {
      @Override
      public Notification process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(notificationId);

        return requestContext.getModelService().retrieveNotification(notificationId);
      }
    });
  }

  @Override
  public StringResponse acknowledgeNotification(@RequestBody NotificationAcknowledgeRequest ackRequest) {
    // 20170515 nvieira: decided to not check roles considering the ackToken
    // should be enough and it is not necessary nor usable to create a new role
    // only for this purpose
    return requestHandler.processRequestWithTransaction(new RequestHandler.RequestProcessor<StringResponse>() {
      @Override
      public StringResponse process(RequestContext requestContext, RequestControllerAssistant controllerAssistant)
        throws RODAException, RESTException {
        controllerAssistant.setRelatedObjectId(ackRequest.getNotificationUUID());
        controllerAssistant.setParameters(RodaConstants.CONTROLLER_NOTIFICATION_ID_PARAM,
          ackRequest.getNotificationUUID(), RodaConstants.CONTROLLER_NOTIFICATION_TOKEN_PARAM, ackRequest.getToken());
        notificationsService.validateAckRequest(ackRequest);

        notificationsService.acknowledgeNotification(requestContext.getModelService(), ackRequest.getNotificationUUID(),
          ackRequest.getToken());
        return new StringResponse("Notification acknowledged");
      }
    });
  }

  @Override
  public Notification findByUuid(String uuid, String localeString) {
    return indexService.retrieve(Notification.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<Notification> find(@RequestBody FindRequest findRequest, String localeString) {
    return indexService.find(Notification.class, findRequest, localeString);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_NOTIFICATION)) {
      return new LongResponse(indexService.count(Notification.class, countRequest));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  public List<String> suggest(SuggestRequest suggestRequest) {
    throw new RESTException(new NotImplementedException());
  }

  @Override
  public ResponseEntity<StreamingResponseBody> exportToCSV(String findRequestString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    // delegate
    return ApiUtils
      .okResponse(indexService.exportToCSV(requestContext.getUser(), findRequestString, Notification.class));
  }
}
