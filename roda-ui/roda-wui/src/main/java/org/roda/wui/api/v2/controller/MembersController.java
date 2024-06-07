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
import org.roda.core.data.v2.accessKey.CreateAccessKeyRequest;
import org.roda.core.data.v2.accessToken.AccessToken;
import org.roda.core.data.v2.generics.LongResponse;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.FindRequest;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.SuggestRequest;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.RodaPrincipal;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.user.requests.ChangeUserStatusRequest;
import org.roda.core.data.v2.user.requests.CreateGroupRequest;
import org.roda.core.data.v2.user.requests.CreateUserExtraFormFields;
import org.roda.core.data.v2.user.requests.CreateUserRequest;
import org.roda.core.data.v2.user.requests.LoginRequest;
import org.roda.core.data.v2.user.requests.ResetPasswordRequest;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.model.GenericOkResponse;
import org.roda.wui.api.v2.services.IndexService;
import org.roda.wui.api.v2.services.MembersService;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(MembersController.class);
  @Autowired
  private HttpServletRequest request;

  @Autowired
  private MembersService membersService;

  @Autowired
  private IndexService indexService;

  public static void logout(HttpServletRequest request, List<String> extraAttributesToBeRemovedFromSession) {
    User user = UserUtility.getUser(request);
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    // delegate
    UserUtility.removeUserFromSession(request, extraAttributesToBeRemovedFromSession);

    // register action
    controllerAssistant.registerAction(user, LogEntryState.SUCCESS, RodaConstants.CONTROLLER_USERNAME_PARAM,
      user.getName());
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
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_USERNAME_PARAM, name);
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
      RodaCoreFactory.getModelService().deleteUserAccessKeys(name, requestContext.getUser().getId());
      membersService.deleteUser(name);
      return null;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_USERNAME_PARAM, name);
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
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  @Override
  public AccessKeys getAccessKeysByUser(String username) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      if (membersService.retrieveUser(username).getId() == null) {
        throw new NotFoundException("User not found");
      }
      AccessKeys accessKeys = RodaCoreFactory.getModelService().listAccessKeysByUser(username);
      for (AccessKey accessKey : accessKeys.getObjects()) {
        accessKey.setKey(null);
      }
      return accessKeys;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  @Override
  public AccessKey getAccessKey(String accessKeyId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      AccessKey accessKey = RodaCoreFactory.getModelService().retrieveAccessKey(accessKeyId);
      accessKey.setKey(null);
      return accessKey;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  @Override
  public AccessToken authenticate(@RequestBody String token) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    LogEntryState state = LogEntryState.SUCCESS;
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    Claims claims;

    try {
      SecretKey secretKey = Keys.hmacShaKeyFor(RodaCoreFactory.getApiSecretKey().getBytes(StandardCharsets.UTF_8));
      claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
      User user = RodaCoreFactory.getModelService().retrieveUser(claims.getSubject());
      AccessKeys accessKeys = RodaCoreFactory.getModelService().listAccessKeys();
      AccessKey retAccessKey = accessKeys.getAccessKeyByKey(token);
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
    } catch (RODAException | JwtException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext.getUser(), state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
    }
  }

  @Override
  public AccessKey regenerateAccessKey(String id, @RequestBody CreateAccessKeyRequest regenerateAccessKeyRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());
      if (regenerateAccessKeyRequest.getExpirationDate().before(new Date())) {
        throw new RequestNotValidException("Expiration date must be after current date");
      }

      AccessKey accessKey = RodaCoreFactory.getModelService().retrieveAccessKey(id);
      accessKey.setKey(JwtUtils.generateToken(accessKey.getName(), regenerateAccessKeyRequest.getExpirationDate()));
      return RodaCoreFactory.getModelService().updateAccessKey(accessKey, requestContext.getUser().getName());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM,
        regenerateAccessKeyRequest);
    }
  }

  @Override
  public AccessKey createAccessKey(String id, @RequestBody CreateAccessKeyRequest accessKeyRequest) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());

      if (accessKeyRequest.getExpirationDate() == null || accessKeyRequest.getExpirationDate().before(new Date())) {
        throw new RequestNotValidException("Expiration date must be after current date");
      }

      AccessKey accessKey = new AccessKey();
      accessKey.setName(accessKeyRequest.getName());
      accessKey.setExpirationDate(accessKeyRequest.getExpirationDate());
      accessKey.setUserName(id);

      return RodaCoreFactory.getModelService().createAccessKey(accessKey, requestContext.getUser().getName());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM,
        accessKeyRequest);
    }
  }

  @Override
  public AccessKey revokeAccessKey(String accessKeyId) {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      controllerAssistant.checkRoles(requestContext.getUser());

      AccessKey accessKey = RodaCoreFactory.getModelService().retrieveAccessKey(accessKeyId);
      accessKey.setStatus(AccessKeyStatus.REVOKED);

      return RodaCoreFactory.getModelService().updateAccessKey(accessKey, requestContext.getUser().getName());
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_ACCESS_KEY_PARAM);
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
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_GROUPNAME_PARAM, name);
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
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_GROUPNAME_PARAM, name);
    }
    return null;
  }

  @Override
  public Job changeActive(@RequestBody ChangeUserStatusRequest changeUserStatusRequest) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());

      return membersService.changeActiveMembers(
        CommonServicesUtils.convertSelectedItems(changeUserStatusRequest.getItems(), RODAMember.class),
        changeUserStatusRequest.isActivate(), requestContext.getUser());
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM,
        changeUserStatusRequest.getItems(), RodaConstants.CONTROLLER_ACTIVATE_PARAM,
        changeUserStatusRequest.isActivate());
    }
  }

  @Override
  public Void deleteMultipleMembers(@RequestBody SelectedItemsRequest members) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;

    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      // delegate
      membersService.deleteMembers(CommonServicesUtils.convertSelectedItems(members, RODAMember.class));
    } catch (AuthorizationDeniedException e) {
      state = LogEntryState.UNAUTHORIZED;
      throw new RESTException(e);
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_SELECTED_PARAM, members);
    }
    return null;
  }

  @Override
  public Group createGroup(@RequestBody CreateGroupRequest groupRequest) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    LogEntryState state = LogEntryState.SUCCESS;
    Group group = new Group();
    try {
      // check user permissions
      controllerAssistant.checkRoles(requestContext.getUser());
      group.setName(groupRequest.getName());
      group.setFullName(groupRequest.getFullName());
      group.setDirectRoles(groupRequest.getDirectRoles());
      // delegate
      return membersService.createGroup(group);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_GROUP_PARAM, group);
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
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_GROUP_PARAM, modifiedGroup);
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
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_USER_PARAM,
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
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_USER_PARAM,
        userOperations.getUser());
    }
  }

  @Override
  public Void resetUserPassword(String username, @RequestBody ResetPasswordRequest resetPasswordRequest) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);

    User user = null;
    try {
      user = membersService.resetUserPassword(username, resetPasswordRequest.getNewPassword(),
        resetPasswordRequest.getToken());
      // 20180112 hsilva: need to set ip address for registering the action
      user.setIpAddress(request.getRemoteAddr());
    } catch (RODAException e) {
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, LogEntryState.SUCCESS, RodaConstants.CONTROLLER_USER_PARAM,
        user);
    }
    return null;
  }

  @Override
  public String recoverLogin(String email, String localeString, String captcha) {

    try {
      membersService.recoverLoginCheckCaptcha(captcha);
      membersService.requestPasswordReset(request.getRequestURL().toString().split("/api")[0], email, localeString,
        request.getRemoteAddr(), true);
      return JsonUtils.getJsonFromObject(new GenericOkResponse("Recover email sent to " + email),
        GenericOkResponse.class);
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public String confirmUserEmail(String username, String token) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    User user = null;
    try {
      user = membersService.confirmUserEmail(username, null, token);
      // 20180112 hsilva: need to set ip address for registering the action
      user.setIpAddress(request.getRemoteAddr());
      return JsonUtils.getJsonFromObject(new GenericOkResponse("User " + username + " email confirmed"),
        GenericOkResponse.class);
    } catch (RODAException e) {
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, LogEntryState.SUCCESS, RodaConstants.CONTROLLER_USER_PARAM,
        user);
    }
  }

  @Override
  public CreateUserExtraFormFields getDefaultUserExtra() {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    // delegate
    Set<MetadataValue> userExtra = membersService.retrieveDefaultExtraFormFields();
    CreateUserExtraFormFields createUserExtraFormFields = new CreateUserExtraFormFields(userExtra);
    // register action
    controllerAssistant.registerAction(requestContext, LogEntryState.SUCCESS);

    return createUserExtraFormFields;
  }

  @Override
  public Notification sendEmailVerification(String username, String localeString) {
    try {
      return membersService.sendEmailVerification(request.getRequestURL().toString().split("/api")[0], username, true,
        request.getRemoteAddr(), localeString);
    } catch (RODAException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public User login(@RequestBody LoginRequest loginRequest) throws AuthenticationDeniedException {
    String username = loginRequest.getUsername();
    SecureString password = loginRequest.getPassword();

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
      createdUser.setExtra(userOperations.getValues());
      return createdUser;
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_USER_PARAM, user);
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
      controllerAssistant.registerAction(requestContext, state, RodaConstants.CONTROLLER_USER_PARAM,
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
    if (UserUtility.hasPermissions(requestContext.getUser(), RodaConstants.PERMISSION_METHOD_FIND_RODA_MEMBER)) {
      return new LongResponse(indexService.count(RODAMember.class, countRequest, requestContext));
    } else {
      return new LongResponse(-1L);
    }
  }

  @Override
  public List<String> suggest(SuggestRequest suggestRequest) {
    RequestContext requestContext = RequestUtils.parseHTTPRequest(request);
    return indexService.suggest(suggestRequest, RODAMember.class, requestContext);
  }
}
