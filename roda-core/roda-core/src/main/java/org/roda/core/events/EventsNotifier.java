package org.roda.core.events;

import java.io.Serializable;

import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;

public interface EventsNotifier extends Serializable {
  public void notifyUserCreated(ModelService model, User user, String password);

  public void notifyUserUpdated(ModelService model, User user, User updatedUser, String password);

  public void notifyMyUserUpdated(ModelService model, User user, User updatedUser, String password);

  public void notifyUserDeleted(ModelService model, String id);

  public void notifyGroupCreated(ModelService model, Group group);

  public void notifyGroupUpdated(ModelService model, Group group, Group updatedGroup);

  public void notifyGroupDeleted(ModelService model, String id);

  public void shutdown();

}
