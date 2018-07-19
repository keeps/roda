package org.roda.core.events;

import org.roda.core.data.common.RodaConstants.NodeType;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;

public class EventsManager implements EventsNotifier {
  private static final long serialVersionUID = 3733394744862836327L;

  private EventsNotifier eventsNotifier;
  private EventsHandler eventsHandler;
  private NodeType nodeType;
  private boolean enabled;

  public EventsManager(EventsNotifier eventsNotifier, EventsHandler eventsHandler, NodeType nodeType, boolean enabled) {
    this.eventsNotifier = eventsNotifier;
    this.eventsHandler = eventsHandler;
    this.nodeType = nodeType;
    this.enabled = enabled;
  }

  @Override
  public void notifyUserCreated(ModelService model, User user, String password) {
    if (enabled) {
      eventsNotifier.notifyUserCreated(model, user, password);
    }
  }

  @Override
  public void notifyUserUpdated(ModelService model, User user, User updatedUser, String password) {
    if (enabled) {
      eventsNotifier.notifyUserUpdated(model, user, updatedUser, password);
    }
  }

  @Override
  public void notifyMyUserUpdated(ModelService model, User user, User updatedUser, String password) {
    if (enabled) {
      eventsNotifier.notifyMyUserUpdated(model, user, updatedUser, password);
    }
  }

  @Override
  public void notifyUserDeleted(ModelService model, String userID) {
    if (enabled) {
      eventsNotifier.notifyUserDeleted(model, userID);
    }
  }

  @Override
  public void notifyGroupCreated(ModelService model, Group group) {
    if (enabled) {
      eventsNotifier.notifyGroupCreated(model, group);
    }
  }

  @Override
  public void notifyGroupUpdated(ModelService model, Group group, Group updatedGroup) {
    if (enabled) {
      eventsNotifier.notifyGroupUpdated(model, group, updatedGroup);
    }
  }

  @Override
  public void notifyGroupDeleted(ModelService model, String id) {
    if (enabled) {
      eventsNotifier.notifyGroupDeleted(model, id);
    }
  }

  @Override
  public void shutdown() {
    if (enabled) {
      eventsHandler.shutdown();
      eventsNotifier.shutdown();
    }
  }

}
