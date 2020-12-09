/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.common;

import java.lang.reflect.Method;
import java.util.Date;

import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.client.tools.StringUtils;

public class ControllerAssistant {
  private final Date startDate;
  private final Method enclosingMethod;

  public ControllerAssistant() {
    this.startDate = new Date();
    this.enclosingMethod = this.getClass().getEnclosingMethod();
  }

  public void checkGroup(final User user, final String group) throws AuthorizationDeniedException {
    try {
      UserUtility.checkGroup(user, group);
    } catch (final AuthorizationDeniedException e) {
      registerAction(user, LogEntryState.UNAUTHORIZED);
      throw e;
    }
  }

  public void checkRoles(final User user) throws AuthorizationDeniedException {
    try {
      UserUtility.checkRoles(user, this.getClass());
    } catch (final AuthorizationDeniedException e) {
      registerAction(user, LogEntryState.UNAUTHORIZED);
      throw e;
    }
  }

  public void checkRoles(final User user, final Class<?> classToReturn) throws AuthorizationDeniedException {
    try {
      UserUtility.checkRoles(user, this.getClass(), classToReturn);
    } catch (final AuthorizationDeniedException e) {
      registerAction(user, LogEntryState.UNAUTHORIZED);
      throw e;
    }
  }

  public <T extends IsIndexed> void checkObjectPermissions(final User user, T obj) throws AuthorizationDeniedException {
    checkObjectPermissions(user, obj, null);
  }

  public <T extends IsIndexed> void checkObjectPermissions(final User user, T obj, Class<?> classToReturn)
    throws AuthorizationDeniedException {
    try {
      UserUtility.checkObjectPermissions(user, obj, this.getClass(), classToReturn);
    } catch (final AuthorizationDeniedException e) {
      registerAction(user, LogEntryState.UNAUTHORIZED);
      throw e;
    }
  }

  public <T extends IsIndexed> void checkObjectPermissions(final User user, SelectedItems<T> objs)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    checkObjectPermissions(user, objs, null);
  }

  public <T extends IsIndexed> void checkObjectPermissions(final User user, SelectedItems<T> objs,
    Class<T> classToReturn) throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    try {
      UserUtility.checkObjectPermissions(user, objs, this.getClass(), classToReturn);
    } catch (final AuthorizationDeniedException e) {
      registerAction(user, LogEntryState.UNAUTHORIZED);
      throw e;
    }
  }

  public void registerAction(final User user, final String relatedObjectId, final LogEntryState state,
    final Object... parameters) {
    final long duration = new Date().getTime() - startDate.getTime();
    ControllerAssistantUtils.registerAction(user, this.enclosingMethod.getDeclaringClass().getName(),
      this.enclosingMethod.getName(), relatedObjectId, duration, state, parameters);
  }

  public void registerAction(final User user, final LogEntryState state, final Object... parameters) {
    registerAction(user, null, state, parameters);
  }

  public void registerAction(final User user, final LogEntryState state) {
    registerAction(user, (String) null, state);
  }

  public void checkAIPstate(IndexedAIP aip) throws RequestNotValidException {
    if(aip.getState().equals(AIPState.DESTROYED)){
      throw new RequestNotValidException("The AIP [id: " + aip.getId() + "] is destroyed, therefore the request is not valid.");
    }
  }

    public void checkIfAIPInConfirmation(IndexedAIP indexedAip) throws RequestNotValidException {
      if(StringUtils.isNotBlank(indexedAip.getDisposalConfirmationId())){
        throw new RequestNotValidException("The AIP [id: " + indexedAip.getId() + "] is under a disposal confirmation [id: " + indexedAip.getDisposalConfirmationId() + "]  , therefore the request is not valid.");
      }
    }
}
