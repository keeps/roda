package org.roda.wui.api.v2.controller;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.crypto.SecretKey;

import org.apereo.cas.client.authentication.AttributePrincipal;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.JwtUtils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.InactiveUserException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeyStatus;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.data.v2.accessToken.AccessToken;
import org.roda.core.data.v2.generics.CreateUserRequest;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.RodaPrincipal;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.model.GenericOkResponse;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.MembersService;
import org.roda.wui.client.management.recaptcha.RecaptchaException;
import org.roda.wui.client.services.MembersRestService;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NamingException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@RestController
@RequestMapping(path = "/api/v2/members")
public class MembersController implements MembersRestService {

  @Autowired
  private HttpServletRequest request;

  @Autowired
  private MembersService membersService;

  @Autowired
  private IndexService indexService;
  private static final Logger LOGGER = LoggerFactory.getLogger(MembersController.class);

  @Override
  public User getUser(String name) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return membersService.retrieveUser(name);

    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_USERNAME_PARAM,
        name);
    }
  }

  @Override
  public User getAuthenticatedUser() {
    User user = UserUtility.getUser(request);
    LOGGER.debug("Serving user {}", user);
    return user;
  }

  @Override
  public Void deleteUser(String name) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      membersService.deleteUser(name);
      return deleteUserAccessKeys(name);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_USERNAME_PARAM,
        name);
    }
  }

  @Override
  public Void deleteUserAccessKeys(String name) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      RodaCoreFactory.getModelService().deleteUserAccessKeys(name, requestContext.getUser().getId());
      return null;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  @Override
  public Void deactivateUserAccessKeys(String name) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      return null;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  @Override
  public Void deleteAccessKey(String accessKeyId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      RodaCoreFactory.getModelService().deleteAccessKey(accessKeyId);
      return null;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  @Override
  public AccessKeys getAccessKeysByUser(String username) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      return RodaCoreFactory.getModelService().listAccessKeysByUser(username);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  @Override
  public AccessKey getAccessKey(String accessKeyId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      return RodaCoreFactory.getModelService().retrieveAccessKey(accessKeyId);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  @Override
  public AccessKey regenerateAccessKey(@RequestBody AccessKey accessKey) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      accessKey.setKey(JwtUtils.regenerateToken(accessKey.getKey()));
      return RodaCoreFactory.getModelService().updateAccessKey(accessKey, requestContext.getUser().getName());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  @Override
  public AccessKey createAccessKey(@RequestBody AccessKey accessKey) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      return RodaCoreFactory.getModelService().createAccessKey(accessKey, requestContext.getUser().getName());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM,
        accessKey);
    }
  }

  @Override
  public AccessKey revokeAccessKey(@RequestBody AccessKey accessKey) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    accessKey.setStatus(AccessKeyStatus.REVOKED);

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      return RodaCoreFactory.getModelService().updateAccessKey(accessKey, requestContext.getUser().getName());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  @Override
  public AccessToken authenticate(@RequestBody AccessKey accessKey)
    throws GenericException, AuthorizationDeniedException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LogEntryState state = LogEntryState.SUCCESS;

    Claims claims;
    try {
      SecretKey secretKey = Keys.hmacShaKeyFor(RodaCoreFactory.getApiSecretKey().getBytes(StandardCharsets.UTF_8));
      claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(accessKey.getKey()).getPayload();
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
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  @Override
  public Group getGroup(String name) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      return membersService.retrieveGroup(name);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_GROUPNAME_PARAM,
        name);
    }
  }

  @Override
  public Void deleteGroup(String name) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      membersService.deleteGroup(name);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_GROUPNAME_PARAM,
        name);
    }
    return null;
  }

  @Override
  public Void changeActive(@RequestBody SelectedItems<RODAMember> members, Boolean activate) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      membersService.changeActiveMembers(members, activate);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        members, RodaConstants.CONTROLLER_ACTIVATE_PARAM, activate);
    }
    return null;
  }

  @Override
  public Void deleteMultipleMembers(@RequestBody SelectedItems<RODAMember> members) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      membersService.deleteMembers(members);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        members);
    }
    return null;
  }

  @Override
  public Group createGroup(@RequestBody Group group) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      return membersService.createGroup(group);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_GROUP_PARAM, group);
    }
  }

  @Override
  public Void updateGroup(@RequestBody Group modifiedGroup) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      membersService.updateGroup(modifiedGroup);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_GROUP_PARAM,
        modifiedGroup);
    }
    return null;
  }

  @Override
  public User updateUser(@RequestBody CreateUserRequest userRequest) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      return membersService.updateUser(userRequest);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_USER_PARAM,
        requestContext.getUser());
    }
  }

  @Override
  public User updateMyUser(@RequestBody CreateUserRequest userOperations) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      return membersService.updateMyUser(requestContext.getUser(), userOperations.getUser(),
        userOperations.getPassword(), userOperations.getValues());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_USER_PARAM,
        userOperations.getUser());
    }
  }

  @Override
  public Void resetUserPassword(String username, String resetPasswordToken, @RequestBody SecureString password) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    User user = null;
    try {
      user = membersService.resetUserPassword(username, password, resetPasswordToken);
      // 20180112 hsilva: need to set ip address for registering the action
      user.setIpAddress(request.getRemoteAddr());
    } catch (RODAException e) {
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), LogEntryState.SUCCESS,
        RodaConstants.CONTROLLER_USER_PARAM, user);
    }
    return null;
  }

  @Override
  public String recoverLogin(String email, String localeString, String captcha) {

    try {
      membersService.recoverLoginCheckCaptcha(captcha);
      membersService.requestPasswordReset(request.getRequestURL().toString().split("/api")[0], email,
        localeString, request.getRemoteAddr(), true);
      return JsonUtils.getJsonFromObject(new GenericOkResponse("Recover email sent to " + email), GenericOkResponse.class);
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public Void confirmUserEmail(String username, String token) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    User user = null;
    try {
      user = membersService.confirmUserEmail(username, null, token);
      // 20180112 hsilva: need to set ip address for registering the action
      user.setIpAddress(request.getRemoteAddr());
      return null;
    } catch (RODAException e) {
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), LogEntryState.SUCCESS,
        RodaConstants.CONTROLLER_USER_PARAM, user);
    }
  }

  @Override
  public Set<MetadataValue> getUserExtra(String username) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    if (requestContext.getUser().getName().equals(username)) {
      return membersService.retrieveOwnUserExtra(requestContext.getUser());
    } else {
      return membersService.retrieveUserExtra(requestContext.getUser(), username);
    }
  }

  @Override
  public Set<MetadataValue> getDefaultUserExtra() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    // delegate
    Set<MetadataValue> userExtra = membersService.retrieveDefaultExtraBundle();

    // register action
    controllerAssistant.registerAction(requestContext.getUser(), LogEntryState.SUCCESS);

    return userExtra;
  }

  @Override
  public Notification sendEmailVerification(String username, boolean generateNewToken, String localeString) {
    try {
      return membersService.sendEmailVerification(request.getRequestURL().toString().split("/api")[0], username,
        generateNewToken, request.getRemoteAddr(), localeString);
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public User login(String username, @RequestBody SecureString password) throws AuthenticationDeniedException {
    if (RodaCoreFactory.getRodaConfiguration().getBoolean(RodaConstants.CORE_WEB_BASIC_AUTH_DISABLE, false)) {
      List<String> allowedUsers = RodaCoreFactory
        .getRodaConfigurationAsList(RodaConstants.CORE_WEB_BASIC_AUTH_WHITELIST);
      if (allowedUsers.isEmpty() || !allowedUsers.contains(username)) {
        throw new AuthenticationDeniedException("User is not authorized to login via basic authentication");
      }
    }

    User user;
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    try {
      // delegate
      user = membersService.login(username, password, request);

      // register action
      controllerAssistant.registerAction(user, LogEntryState.SUCCESS, RodaConstants.CONTROLLER_USERNAME_PARAM,
        username);

      LOGGER.debug("Logged user {}", user);
      return user;

    } catch (RODAException e) {
      user = UserUtility.getGuest(request.getRemoteAddr());
      // register action
      controllerAssistant.registerAction(user, LogEntryState.FAILURE, RodaConstants.CONTROLLER_USERNAME_PARAM,
        username);
      throw new RESTException(e);
    }

  }

  public static void logout(HttpServletRequest request, List<String> extraAttributesToBeRemovedFromSession) {
    User user = UserUtility.getUser(request);
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // delegate
    UserUtility.removeUserFromSession(request, extraAttributesToBeRemovedFromSession);

    // register action
    controllerAssistant.registerAction(user, LogEntryState.SUCCESS, RodaConstants.CONTROLLER_USERNAME_PARAM,
      user.getName());
  }

  @Override
  public User createUser(@RequestBody CreateUserRequest userOperations, String localeString) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    User user = userOperations.getUser();
    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      User createdUser = membersService.createUser(user, userOperations.getPassword(), userOperations.getValues());
      membersService.requestPasswordReset(request.getRequestURL().toString().split("/api")[0], user.getEmail(),
        localeString, request.getRemoteAddr(), false);
      return createdUser;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_USER_PARAM, user);
    }
  }

  @Override
  public User registerUser(@RequestBody CreateUserRequest createUserRequest, String localeString, String captcha) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // delegate
      return membersService.registerUser(request, createUserRequest.getUser(), createUserRequest.getPassword(), captcha,
        createUserRequest.getValues(), localeString, request.getRequestURL().toString().split("/api")[0]);
    } catch (EmailAlreadyExistsException | UserAlreadyExistsException e) {
      state = LogEntryState.FAILURE;
      return new User(createUserRequest.getUser());
    } catch (RODAException | RecaptchaException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_USER_PARAM,
        createUserRequest.getUser());
    }
  }

  @Override
  public RodaPrincipal findByUuid(String uuid, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.retrieve(requestContext, RodaPrincipal.class, uuid, new ArrayList<>());
  }

  @Override
  public IndexResult<RODAMember> find(@RequestBody FindRequest findRequest, String localeString) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.find(RODAMember.class, findRequest, localeString, requestContext);
  }

  @Override
  public LongResponse count(@RequestBody CountRequest countRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return new LongResponse(indexService.count(RODAMember.class, countRequest, requestContext));
  }

  public static void casLogin(String username, HttpServletRequest request) throws RODAException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    try {
      User requestUser = UserUtility.getUser(request, false);

      if (requestUser == null || !username.equals(requestUser.getName())) {
        // delegate
        User user = casLoginHelper(username, request);
        // register action
        controllerAssistant.registerAction(user, LogEntryState.SUCCESS, RodaConstants.CONTROLLER_USERNAME_PARAM,
          username);
      }
    } catch (AuthenticationDeniedException e) {
      User user = UserUtility.getGuest(request.getRemoteAddr());
      // register action
      controllerAssistant.registerAction(user, LogEntryState.FAILURE, RodaConstants.CONTROLLER_USERNAME_PARAM,
        username);
      throw e;
    }
  }

  public static User casLoginHelper(final String username, final HttpServletRequest request) throws RODAException {
    User user = null;

    try {
      user = RodaCoreFactory.getModelService().retrieveUser(username);
    } catch (GenericException e) {
      if (!(e.getCause() instanceof NamingException)) {
        throw e;
      }
    }

    if (user == null) {
      User newUser = new User(username);
      newUser = UserUtility.resetGroupsAndRoles(newUser);

      // try to set user email from cas principal attributes
      if (request.getUserPrincipal() instanceof AttributePrincipal attributePrincipal) {
        Map<String, Object> attributes = attributePrincipal.getAttributes();

        mapCasAttribute(newUser, attributes, "fullname", (u, a) -> u.setFullName(a));
        mapCasAttribute(newUser, attributes, "email", (u, a) -> u.setEmail(a));
      }

      // Try to find a user with email
      User retrievedUserByEmail = null;
      if (StringUtils.isNotBlank(newUser.getEmail())) {
        try {
          retrievedUserByEmail = RodaCoreFactory.getModelService().retrieveUserByEmail(newUser.getEmail());
        } catch (GenericException e) {
          if (!(e.getCause() instanceof NamingException)) {
            throw e;
          }
        }
      }

      if (retrievedUserByEmail != null) {
        user = retrievedUserByEmail;
      } else {
        // If no user was found with username or e-mail, a new one is created.
        user = RodaCoreFactory.getModelService().createUser(newUser, true);
      }
    }

    if (!user.isActive()) {
      throw new InactiveUserException("User is not active.");
    }

    UserUtility.setUser(request, user);
    return user;
  }

  private static void mapCasAttribute(User user, Map<String, Object> attributes, String attributeKey,
    BiConsumer<User, String> mapping) {
    Object attributeValue = attributes.get(attributeKey);
    if (attributeValue instanceof String value) {
      mapping.accept(user, value);
    }
  }
}
