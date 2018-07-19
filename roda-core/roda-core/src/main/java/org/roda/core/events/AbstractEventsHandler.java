package org.roda.core.events;

import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEventsHandler implements EventsHandler {
  private static final long serialVersionUID = -1284727831525932207L;
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEventsHandler.class);

  @Override
  public void handleUserCreated(ModelService model, User user, String password) {
    LOGGER.debug("handleUserCreated '{}' with password '{}'", user, password != null ? "******" : "NULL");
    try {
      model.createUser(user, password, true, true);
    } catch (IllegalOperationException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling user created event", e);
    } catch (EmailAlreadyExistsException | UserAlreadyExistsException e) {
      try {
        model.updateUser(user, password, true, true);
      } catch (GenericException | AlreadyExistsException | NotFoundException | AuthorizationDeniedException e1) {
        LOGGER.error(
          "Error handling user created event (but user already exists & we were trying to update it, but exception occurred)",
          e1);
      }
    }
  }

  @Override
  public void handleUserUpdated(ModelService model, User user, String password) {
    LOGGER.debug("handleUserUpdated '{}' with password '{}'", user, password != null ? "******" : "NULL");
    try {
      model.updateUser(user, password, true, true);
    } catch (GenericException | AlreadyExistsException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling user updated event", e);
    } catch (NotFoundException e) {
      try {
        model.createUser(user, password, true, true);
      } catch (EmailAlreadyExistsException | UserAlreadyExistsException | IllegalOperationException | GenericException
        | NotFoundException | AuthorizationDeniedException e1) {
        LOGGER.error(
          "Error handling user updated event (but user was not found & we were trying to create it, but exception occurred)",
          e1);
      }
    }
  }

  @Override
  public void handleMyUserUpdated(ModelService model, User user, String password) {
    LOGGER.debug("handleMyUserUpdated '{}' with password '{}'", user, password != null ? "******" : "NULL");
    try {
      model.updateMyUser(user, password, true, true);
    } catch (GenericException | AlreadyExistsException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling user updated event", e);
    } catch (NotFoundException e) {
      try {
        model.createUser(user, password, true, true);
      } catch (EmailAlreadyExistsException | UserAlreadyExistsException | IllegalOperationException | GenericException
        | NotFoundException | AuthorizationDeniedException e1) {
        LOGGER.error(
          "Error handling user updated event (but user was not found & we were trying to create it, but exception occurred)",
          e1);
      }
    }
  }

  @Override
  public void handleUserDeleted(ModelService model, String id) {
    LOGGER.debug("handleUserDeleted '{}'", id);
    try {
      User user = null;
      try {
        // 20180814 hsilva: the following is to avoid deleting already deleted
        // user
        user = model.retrieveUser(id);
        model.deleteUser(id, true, true);
      } catch (GenericException e) {
        if (user != null) {
          throw e;
        }
      }
    } catch (GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling user deleted event", e);
    }
  }

  public void handleGroupCreated(ModelService model, Group group) {
    LOGGER.debug("handleGroupCreated '{}'", group);
    try {
      model.createGroup(group, true, true);
    } catch (GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling create group event", e);
    } catch (AlreadyExistsException e) {
      try {
        model.updateGroup(group, true, true);
      } catch (GenericException | NotFoundException | AuthorizationDeniedException e1) {
        LOGGER.error(
          "Error handling create group event (but group already exists & we were trying to update it, but exception occurred)",
          e1);
      }
    }
  }

  public void handleGroupUpdated(ModelService model, Group group) {
    LOGGER.debug("handleGroupUpdated '{}'", group);
    try {
      model.updateGroup(group, true, true);
    } catch (GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling update group event", e);
    } catch (NotFoundException e) {
      try {
        model.createGroup(group, true, true);
      } catch (GenericException | AlreadyExistsException | AuthorizationDeniedException e1) {
        LOGGER.error(
          "Error handling update group event (but group was not found & we were trying to create it, but exception occurred)",
          e1);
      }
    }
  }

  public void handleGroupDeleted(ModelService model, String id) {
    LOGGER.debug("handleGroupDeleted '{}'", id);
    try {
      try {
        model.retrieveGroup(id);
        model.deleteGroup(id, true, true);
      } catch (NotFoundException e) {
        // do nothing as it is already deleted
      }
    } catch (GenericException | AuthorizationDeniedException e) {
      LOGGER.error("Error handling delete group event", e);
    }
  }

}
