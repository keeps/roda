package org.roda.wui.api.v2.services;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.notifications.NotificationProcessor;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.notifications.NotificationAcknowledgeRequest;
import org.roda.wui.common.client.tools.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class NotificationsService {

  public Notification createNotification(Notification notification, NotificationProcessor processor)
    throws GenericException, AuthorizationDeniedException {
    return RodaCoreFactory.getModelService().createNotification(notification, processor);
  }

  public void acknowledgeNotification(String notificationId, String ackToken)
    throws GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().acknowledgeNotification(notificationId, ackToken);
  }

  public void validateAckRequest(NotificationAcknowledgeRequest ackRequest) throws RequestNotValidException {
    if (StringUtils.isBlank(ackRequest.getNotificationUUID())) {
      throw new RequestNotValidException("Missing required notification identifier");
    }

    if (StringUtils.isBlank(ackRequest.getToken())) {
      throw new RequestNotValidException("Token value is required");
    }
  }
}
