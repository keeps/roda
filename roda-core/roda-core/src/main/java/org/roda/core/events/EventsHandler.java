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
import org.roda.core.data.common.SecureString;

public interface EventsHandler extends Serializable {
  public void handleUserCreated(ModelService model, User user, SecureString password);

  public void handleUserUpdated(ModelService model, User user, SecureString password);

  public void handleMyUserUpdated(ModelService model, User user, SecureString password);

  public void handleUserDeleted(ModelService model, String id);

  public void handleGroupCreated(ModelService model, Group group);

  public void handleGroupUpdated(ModelService model, Group group);

  public void handleGroupDeleted(ModelService model, String id);
  
  public void shutdown();
}
