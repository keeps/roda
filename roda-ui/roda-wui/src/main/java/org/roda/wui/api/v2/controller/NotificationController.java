package org.roda.wui.api.v2.controller;

import java.util.ArrayList;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.notifications.NotificationAcknowledgeRequest;
import org.roda.core.data.v2.notifications.Notifications;
import org.roda.wui.api.v1.utils.ApiUtils;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.model.GenericOkResponse;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.NotificationsService;
import org.roda.wui.client.services.NotificationRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/api/v2/notifications")
@Tag(name = NotificationController.SWAGGER_ENDPOINT)
public class NotificationController implements NotificationRestService {
  public static final String SWAGGER_ENDPOINT = "v2 notifications";

  @Autowired
  private NotificationsService notificationsService;

  @Autowired
  private IndexService indexService;

  @Autowired
  private HttpServletRequest request;

  @Override
  public Notifications listNotifications(String start, String limit) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    // delegate action to controller
    boolean justActive = false;
    Pair<Integer, Integer> pagingParams = ApiUtils.processPagingParams(start, limit);

    FindRequest findRequest = new FindRequest.FindRequestBuilder(Notification.class.getName(), Filter.ALL, justActive).withSubList(new Sublist(pagingParams.getFirst(), pagingParams.getSecond()))
        .build();

    IndexResult<Notification> result = indexService.find(Notification.class, findRequest, requestContext.getUser());

    return new Notifications(result.getResults());
  }

  @Override
  public Notification getNotification(String notificationId) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    return indexService.retrieve(requestContext.getUser(), Notification.class, notificationId, new ArrayList<>());
  }

  @Override
  public String acknowledgeNotification(@RequestBody NotificationAcknowledgeRequest ackRequest) {
    // 20170515 nvieira: decided to not check roles considering the ackToken
    // should be enough and it is not necessary nor usable to create a new role
    // only for this purpose

    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      notificationsService.validateAckRequest(ackRequest);

      notificationsService.acknowledgeNotification(ackRequest.getNotificationUUID(), ackRequest.getToken());
      return JsonUtils.getJsonFromObject(new GenericOkResponse("Notification acknowledged"), GenericOkResponse.class);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), ackRequest.getNotificationUUID(), state,
        RodaConstants.CONTROLLER_NOTIFICATION_ID_PARAM, ackRequest.getNotificationUUID(),
        RodaConstants.CONTROLLER_NOTIFICATION_TOKEN_PARAM, ackRequest.getToken());
    }
  }

  @Override
  public Notification findByUuid(String uuid) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.retrieve(requestContext.getUser(), Notification.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<Notification> find(FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    return indexService.find(Notification.class, findRequest, localeString, requestContext.getUser());
  }

  @Override
  public String count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return String.valueOf(indexService.count(Notification.class, countRequest.filter, countRequest.onlyActive,
      requestContext.getUser()));
  }
}
