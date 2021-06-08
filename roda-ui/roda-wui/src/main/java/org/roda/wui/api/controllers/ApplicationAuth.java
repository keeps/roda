package org.roda.wui.api.controllers;

import java.util.Date;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.AccessToken.AccessToken;
import org.roda.core.data.v2.AccessToken.AccessTokenStatus;
import org.roda.core.data.v2.AccessToken.AccessTokens;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.user.User;
import org.roda.core.util.IdUtils;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.RodaWuiController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ApplicationAuth extends RodaWuiController {
  private ApplicationAuth() {
    super();
  }

  public static AccessToken createAccessToken(User user, AccessToken accessToken) throws AuthorizationDeniedException,
    AlreadyExistsException, NotFoundException, GenericException, RequestNotValidException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return RodaCoreFactory.getModelService().createAccessToken(accessToken, user.getName());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_TOKEN_PARAM, accessToken);
    }
  }

  public static AccessTokens listAccessToken(User user)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return RodaCoreFactory.getModelService().listAccessToken();
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_TOKEN_PARAM);
    }
  }

  public static AccessToken retrieveAccessToken(User user, String accessTokenId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return RodaCoreFactory.getModelService().retrieveAccessToken(accessTokenId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_TOKEN_PARAM);
    }
  }

  public static AccessToken updateAccessToken(User user, AccessToken accessToken)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return RodaCoreFactory.getModelService().updateAccessToken(accessToken, user.getName());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_TOKEN_PARAM);
    }
  }

  public static void deleteAccessToken(User user, String accessTokenId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      RodaCoreFactory.getModelService().deleteAccessToken(accessTokenId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_TOKEN_PARAM);
    }
  }

  public static AccessTokens listAccessTokenByUser(User user, String userId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      return RodaCoreFactory.getModelService().listAccessTokenByUser(userId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_TOKEN_PARAM);
    }
  }

  public static void deactivateUserAccessTokens(User user, String userId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      RodaCoreFactory.getModelService().deactivateUserAccessTokens(userId, user.getId());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_TOKEN_PARAM);
    }
  }

  public static void deleteUserAccessTokens(User user, String userId)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      RodaCoreFactory.getModelService().deleteUserAccessTokens(userId, user.getId());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_TOKEN_PARAM);
    }
  }

  public static AccessToken regenerateAccessToken(User user, AccessToken accessToken)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    accessToken.setAccessKey(IdUtils.createUUID());

    try {
      return RodaCoreFactory.getModelService().updateAccessToken(accessToken, user.getName());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_TOKEN_PARAM);
    }
  }

  public static AccessToken revokeAccessToken(User user, AccessToken accessToken)
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    accessToken.setStatus(AccessTokenStatus.REVOKED);

    try {
      return RodaCoreFactory.getModelService().updateAccessToken(accessToken, user.getName());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_TOKEN_PARAM);
    }
  }

  public static String authenticate(AccessToken accessToken)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      AccessTokens accessTokens = RodaCoreFactory.getModelService().listAccessToken();
      AccessToken retAccessToken = accessTokens.getAccessTokenByKey(accessToken.getAccessKey());
      if (retAccessToken != null) {
        String token = generateJWTToken(retAccessToken);
        retAccessToken.setLastUsageDate(new Date());
        RodaCoreFactory.getModelService().updateAccessTokenLastUsageDate(retAccessToken);
        return token;
      }
      state = LogEntryState.FAILURE;
      throw new AuthorizationDeniedException("Access token not found");
    } catch (RequestNotValidException | AuthorizationDeniedException | GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw e;
    }
  }

  public static String generateJWTToken(AccessToken accessToken) {
    long timestamp = System.currentTimeMillis();
    String token = Jwts.builder().signWith(SignatureAlgorithm.HS256, RodaCoreFactory.getApiSecretKey())
      .setIssuedAt(new Date(timestamp)).setExpiration(new Date(timestamp + RodaCoreFactory.getTokenValidity()))
      .claim("accessTokenId", accessToken.getId()).claim("accessTokenName", accessToken.getName())
      .claim("accessTokenLastUsage",
        accessToken.getLastUsageDate() != null ? accessToken.getLastUsageDate().toString() : "Never")
      .compact();

    return token;
  }
}
