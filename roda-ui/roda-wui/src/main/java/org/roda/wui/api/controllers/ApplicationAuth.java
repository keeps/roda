package org.roda.wui.api.controllers;

import java.util.Date;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.JwtUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeyStatus;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.data.v2.accessToken.AccessToken;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ApplicationAuth extends RodaWuiController {
  private ApplicationAuth() {
    super();
  }

  public static AccessKey createAccessKey(User user, AccessKey accessKey) throws AuthorizationDeniedException,
    AlreadyExistsException, NotFoundException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return RodaCoreFactory.getModelService().createAccessKey(accessKey, user.getName());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM, accessKey);
    }
  }

  public static AccessKeys listAccessKey(User user)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return RodaCoreFactory.getModelService().listAccessKeys();
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  public static AccessKey retrieveAccessKey(User user, String accessKeyId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return RodaCoreFactory.getModelService().retrieveAccessKey(accessKeyId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  public static AccessKey updateAccessKey(User user, AccessKey accessKey)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return RodaCoreFactory.getModelService().updateAccessKey(accessKey, user.getName());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  public static void deleteAccessKey(User user, String accessKeyId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      RodaCoreFactory.getModelService().deleteAccessKey(accessKeyId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  public static AccessKeys listAccessKeyByUser(User user, String userId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return RodaCoreFactory.getModelService().listAccessKeysByUser(userId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  public static void deactivateUserAccessKeys(User user, String userId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      RodaCoreFactory.getModelService().deactivateUserAccessKeys(userId, user.getId());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  public static void deleteUserAccessKeys(User user, String userId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      RodaCoreFactory.getModelService().deleteUserAccessKeys(userId, user.getId());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  public static AccessKey regenerateAccessKey(User user, AccessKey accessKey) throws AuthorizationDeniedException,
    RequestNotValidException, GenericException, NotFoundException, AuthenticationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      accessKey.setKey(JwtUtils.regenerateToken(accessKey.getKey()));
      return RodaCoreFactory.getModelService().updateAccessKey(accessKey, user.getName());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  public static AccessKey revokeAccessKey(User user, AccessKey accessKey)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    accessKey.setStatus(AccessKeyStatus.REVOKED);

    try {
      return RodaCoreFactory.getModelService().updateAccessKey(accessKey, user.getName());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  public static AccessToken authenticate(AccessKey accessKey)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LogEntryState state = LogEntryState.SUCCESS;

    Claims claims;
    try {
      claims = Jwts.parser().setSigningKey(RodaCoreFactory.getApiSecretKey()).parseClaimsJws(accessKey.getKey())
        .getBody();
    } catch (JwtException e) {
      throw new AuthorizationDeniedException("Expired token");
    }

    User user = RodaCoreFactory.getModelService().retrieveUser(claims.getSubject());

    try {
      AccessKeys accessKeys = RodaCoreFactory.getModelService().listAccessKeys();
      AccessKey retAccessKey = accessKeys.getAccessKeyByKey(accessKey.getKey());
      if (retAccessKey != null) {
        AccessToken accessToken = new AccessToken();
        Date expirationDate = new Date(new Date().getTime() + RodaCoreFactory.getAccessTokenValidity());
        accessToken.setToken(JwtUtils.generateToken(user.getId(), expirationDate, retAccessKey.getClaims()));
        accessToken.setExpiresIn(RodaCoreFactory.getAccessTokenValidity());
        retAccessKey.setLastUsageDate(new Date());
        RodaCoreFactory.getModelService().updateAccessKeyLastUsageDate(retAccessKey);
        return accessToken;
      } else {
        state = LogEntryState.FAILURE;
        throw new AuthorizationDeniedException("Access token not found");
      }
    } catch (RequestNotValidException | AuthorizationDeniedException | GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }
}
