package org.roda.wui.common;

import java.util.ArrayList;
import java.util.Date;

import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RequestControllerAssistant extends ControllerAssistant {
  private final Object requester;
  private String relatedObjectId;
  private Object[] parameters;

  public RequestControllerAssistant(Object requester) {
    this.startDate = new Date();
    this.enclosingMethod = requester.getClass().getEnclosingMethod();
    this.requester = requester;
  }

  @Override
  public void checkRoles(User user) throws AuthorizationDeniedException {
    try {
      UserUtility.checkRoles(user, requester.getClass());
    } catch (final AuthorizationDeniedException e) {
      registerAction(user, LogEntryState.UNAUTHORIZED);
      throw e;
    }
  }

  @Override
  public void checkRoles(User user, Class<?> classToReturn) throws AuthorizationDeniedException {
    try {
      UserUtility.checkRoles(user, requester.getClass(), classToReturn);
    } catch (final AuthorizationDeniedException e) {
      registerAction(user, LogEntryState.UNAUTHORIZED);
      throw e;
    }
  }

  @Override
  public <T extends IsIndexed> void checkObjectPermissions(User user, T obj) throws AuthorizationDeniedException {
    checkObjectPermissions(user, obj, null);
  }

  @Override
  public <T extends IsIndexed> void checkObjectPermissions(User user, T obj, Class<?> classToReturn)
    throws AuthorizationDeniedException {
    try {
      UserUtility.checkObjectPermissions(user, obj, requester.getClass(), classToReturn);
    } catch (final AuthorizationDeniedException e) {
      registerAction(user, LogEntryState.UNAUTHORIZED);
      throw e;
    }
  }

  @Override
  public <T extends IsIndexed> void checkObjectPermissions(User user, SelectedItems<T> objs)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    checkObjectPermissions(user, objs, null);
  }

  @Override
  public <T extends IsIndexed> void checkObjectPermissions(User user, SelectedItems<T> objs, Class<T> classToReturn)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    try {
      UserUtility.checkObjectPermissions(user, objs, requester.getClass(), classToReturn);
    } catch (final AuthorizationDeniedException e) {
      registerAction(user, LogEntryState.UNAUTHORIZED);
      throw e;
    }
  }

  public void setRelatedObjectId(String relatedObjectId) {
    this.relatedObjectId = relatedObjectId;
  }

  public String getRelatedObjectId() {
    return relatedObjectId;
  }

  public void setParameters(Object... parameters) {
    this.parameters = parameters;
  }

  public Object[] getParameters() {
    return parameters;
  }
}
