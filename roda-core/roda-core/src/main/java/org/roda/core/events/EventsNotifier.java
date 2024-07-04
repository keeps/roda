/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.events;

import java.io.Serializable;

import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;

public interface EventsNotifier extends Serializable {
  void notifyUserCreated(ModelService model, User user);

  void notifyUserUpdated(ModelService model, User user, User updatedUser);

  void notifyMyUserUpdated(ModelService model, User user, User updatedUser);

  void notifyUserDeleted(ModelService model, String id);

  void notifyGroupCreated(ModelService model, Group group);

  void notifyGroupUpdated(ModelService model, Group group, Group updatedGroup);

  void notifyGroupDeleted(ModelService model, String id);

  void shutdown();

}
