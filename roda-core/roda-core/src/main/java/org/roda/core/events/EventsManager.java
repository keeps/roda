/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.events;

import org.roda.core.data.common.RodaConstants.NodeType;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;

import java.io.Serial;

public class EventsManager implements EventsNotifier {
  @Serial
  private static final long serialVersionUID = 3733394744862836327L;

  private final EventsNotifier eventsNotifier;
  private final EventsHandler eventsHandler;
  private final boolean enabled;

  public EventsManager(EventsNotifier eventsNotifier, EventsHandler eventsHandler, NodeType nodeType, boolean enabled) {
    this.eventsNotifier = eventsNotifier;
    this.eventsHandler = eventsHandler;
    this.enabled = enabled;
  }

  @Override
  public void notifyUserCreated(ModelService model, User user) {
    if (enabled) {
      eventsNotifier.notifyUserCreated(model, user);
    }
  }

  @Override
  public void notifyUserUpdated(ModelService model, User user, User updatedUser) {
    if (enabled) {
      eventsNotifier.notifyUserUpdated(model, user, updatedUser);
    }
  }

  @Override
  public void notifyMyUserUpdated(ModelService model, User user, User updatedUser) {
    if (enabled) {
      eventsNotifier.notifyMyUserUpdated(model, user, updatedUser);
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
