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
import org.roda.core.data.v2.accessToken.AccessToken;
import org.roda.core.data.v2.accessToken.AccessTokenStatus;
import org.roda.core.data.v2.accessToken.AccessTokens;
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
    throws AuthorizationDeniedException, RequestNotValidException, GenericException, NotFoundException,
    AuthenticationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    controllerAssistant.checkRoles(user);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      accessToken.setAccessKey(JwtUtils.regenerateToken(accessToken.getAccessKey()));
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

    Claims claims;
    try {
      claims = Jwts.parser().setSigningKey(RodaCoreFactory.getApiSecretKey()).parseClaimsJws(accessToken.getAccessKey())
        .getBody();
    } catch (JwtException e) {
      throw new AuthorizationDeniedException("Expired token");
    }

    User user = RodaCoreFactory.getModelService().retrieveUser(claims.getSubject());

    try {
      AccessTokens accessTokens = RodaCoreFactory.getModelService().listAccessToken();
      AccessToken retAccessToken = accessTokens.getAccessTokenByKey(accessToken.getAccessKey());
      if (retAccessToken != null) {
        Date expirationDate = new Date(new Date().getTime() + RodaCoreFactory.getTokenValidity());
        String token = JwtUtils.generateToken(user.getId(), expirationDate, retAccessToken.getClaims());
        retAccessToken.setLastUsageDate(new Date());
        RodaCoreFactory.getModelService().updateAccessTokenLastUsageDate(retAccessToken);
        return token;
      } else {
        state = LogEntryState.FAILURE;
        throw new AuthorizationDeniedException("Access token not found");
      }
    } catch (RequestNotValidException | AuthorizationDeniedException | GenericException | NotFoundException e) {
      state = LogEntryState.FAILURE;
      throw e;
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_TOKEN_PARAM);
    }
  }
}
