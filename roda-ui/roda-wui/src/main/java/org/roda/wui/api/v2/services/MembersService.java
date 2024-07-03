package org.roda.wui.api.v2.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.notifications.EmailNotificationProcessor;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.EmailUnverifiedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InactiveUserException;
import org.roda.core.data.exceptions.InvalidTokenException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.notifications.NotificationState;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.RodaPrincipal;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.user.requests.CreateUserRequest;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.UserUtility;
import org.roda.core.plugins.base.maintenance.ChangeRodaMemberStatusPlugin;
import org.roda.core.util.IdUtils;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.utils.CommonServicesUtils;
import org.roda.wui.client.management.recaptcha.RecaptchaException;
import org.roda.wui.common.ControllerAssistant;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.server.ServerTools;
import org.roda.wui.server.management.RecaptchaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Service
public class MembersService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MembersService.class);
  private static final String RECAPTCHA_CODE_SECRET_PROPERTY = "ui.google.recaptcha.code.secret";

  private static List<String> getMemberUuidFromSelectedItems(SelectedItems<RODAMember> members)
    throws GenericException, RequestNotValidException {
    List<String> uuids = new ArrayList<>();

    if (members instanceof SelectedItemsFilter) {
      Filter filter = ((SelectedItemsFilter) members).getFilter();
      Iterable<RODAMember> all = RodaCoreFactory.getIndexService().findAll(RODAMember.class, filter,
        Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.INDEX_ID, RodaConstants.MEMBERS_IS_USER));
      for (RODAMember rodaMember : all) {
        uuids.add(rodaMember.getUUID());
      }
    } else if (members instanceof SelectedItemsList) {
      uuids.addAll(((SelectedItemsList<RODAMember>) members).getIds());
    }
    return uuids;
  }


  public static Set<MetadataValue> retrieveUserExtra(String extraLDAP) {
      String template = null;

      try (InputStream templateStream = RodaCoreFactory.getConfigurationFileAsStream(
        RodaConstants.USERS_TEMPLATE_FOLDER + "/" + RodaConstants.USER_EXTRA_METADATA_FILE)) {
        template = IOUtils.toString(templateStream, StandardCharsets.UTF_8);
      } catch (IOException e) {
        LOGGER.error("Error getting template from stream", e);
      }

      Set<MetadataValue> values = ServerTools.transform(template);

      if (extraLDAP != null && !values.isEmpty()) {
        for (MetadataValue mv : values) {
          // clear the auto-generated values
          // mv.set("value", null);
          String xpathRaw = mv.get("xpath");
          if (xpathRaw != null && xpathRaw.length() > 0) {
            String[] xpaths = xpathRaw.split("##%##");
            String value;
            List<String> allValues = new ArrayList<>();
            for (String xpath : xpaths) {
              allValues.addAll(ServerTools.applyXpath(extraLDAP, xpath));
            }
            // if any of the values is different, concatenate all values in a
            // string, otherwise return the value
            boolean allEqual = allValues.stream().allMatch(s -> s.trim().equals(allValues.get(0).trim()));
            if (allEqual && !allValues.isEmpty()) {
              value = allValues.get(0);
            } else {
              value = String.join(" / ", allValues);
            }
            mv.set("value", value.trim());
          }
        }
      }

      return values;

  }

  private static void sendSetPasswordEmail(String servletPath, User user, String localeString, boolean isRecover)
    throws GenericException {
    try {
      Messages messages = RodaCoreFactory.getI18NMessages(ServerTools.parseLocale(localeString));

      Notification notification = new Notification();

      notification.setFromUser(messages.getTranslation(RodaConstants.RECOVER_LOGIN_EMAIL_TEMPLATE_FROM_TRANSLATION));
      notification.setRecipientUsers(Arrays.asList(user.getEmail()));

      String token = user.getResetPasswordToken();
      String username = user.getName();

      Map<String, Object> scopes = new HashMap<>();
      scopes.put("username", username);
      scopes.put("token", token);

      if (isRecover) {
        String recoverLoginURL = servletPath + "/#resetpassword";
        String recoverLoginCompleteURL = recoverLoginURL + "/"
          + URLEncoder.encode(username, RodaConstants.DEFAULT_ENCODING) + "/" + token;
        scopes.put("recoverLoginURL", recoverLoginURL);
        scopes.put("recoverLoginCompleteURL", recoverLoginCompleteURL);
        notification
          .setSubject(messages.getTranslation(RodaConstants.RECOVER_LOGIN_EMAIL_TEMPLATE_SUBJECT_TRANSLATION));
        RodaCoreFactory.getModelService().createNotification(notification,
          new EmailNotificationProcessor(RodaConstants.RECOVER_LOGIN_EMAIL_TEMPLATE, scopes, localeString));
      } else {
        String recoverLoginURL = servletPath + "/#setpassword";
        String recoverLoginCompleteURL = recoverLoginURL + "/"
          + URLEncoder.encode(username, RodaConstants.DEFAULT_ENCODING) + "/" + token;
        scopes.put("recoverLoginURL", recoverLoginURL);
        scopes.put("recoverLoginCompleteURL", recoverLoginCompleteURL);
        notification.setSubject(messages.getTranslation(RodaConstants.SET_PASSWORD_EMAIL_TEMPLATE_SUBJECT_TRANSLATION));
        RodaCoreFactory.getModelService().createNotification(notification,
          new EmailNotificationProcessor(RodaConstants.SET_PASSWORD_TEMPLATE, scopes, localeString));
      }

    } catch (Exception e) {
      throw new GenericException("Problem sending email", e);
    }
  }

  private static User resetUser(User newUser, User oldUser) {
    newUser.setActive(oldUser.isActive());
    newUser.setDirectRoles(oldUser.getDirectRoles());
    newUser.setAllRoles(oldUser.getAllRoles());
    newUser.setGroups(oldUser.getGroups());
    return newUser;
  }

  public User retrieveUser(String username) throws GenericException {
    User user = RodaCoreFactory.getModelService().retrieveUser(username);
    user.setExtra(retrieveUserExtra(RodaCoreFactory.getModelService().retrieveExtraLdap(username)));
    return user;
  }

  public void deleteUser(String username) throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteUser(username, true);
    RodaCoreFactory.getIndexService().commit(true, RODAMember.class);
  }

  public Group retrieveGroup(String groupname) throws GenericException, NotFoundException {
    return RodaCoreFactory.getModelService().retrieveGroup(groupname);
  }

  public Job changeActiveMembers(SelectedItems<RODAMember> selectedItems, boolean active, User user)
    throws AuthorizationDeniedException, RequestNotValidException, NotFoundException, GenericException {

    Map<String, String> pluginParameters = new HashMap<>();
    pluginParameters.put(RodaConstants.PLUGIN_PARAMS_RODA_MEMBER_ACTIVATE, Boolean.toString(active));

    return CommonServicesUtils.createAndExecuteInternalJob("Change RODA users status", selectedItems,
      ChangeRodaMemberStatusPlugin.class, user, pluginParameters, "Could not change RODA useres status");
  }

  public void deactivateUserAccessKeys(SelectedItems<RODAMember> members, String userId) throws GenericException,
    RequestNotValidException, AuthorizationDeniedException, NotFoundException, AlreadyExistsException {
    List<String> uuids = getMemberUuidFromSelectedItems(members);
    for (String uuid : uuids) {
      if (RodaPrincipal.isUser(uuid)) {
        RodaCoreFactory.getModelService().deactivateUserAccessKeys(RodaPrincipal.getId(uuid), userId);
      }
    }
  }

  public void deleteMembers(SelectedItems<RODAMember> members)
    throws GenericException, RequestNotValidException, AuthorizationDeniedException {
    List<String> uuids = getMemberUuidFromSelectedItems(members);
    for (String uuid : uuids) {
      String id = RodaPrincipal.getId(uuid);
      if (RodaPrincipal.isUser(uuid)) {
        RodaCoreFactory.getModelService().deleteUser(id, true);
      } else {
        RodaCoreFactory.getModelService().deleteGroup(id, true);
      }
    }
    RodaCoreFactory.getIndexService().commit(true, RODAMember.class);
  }

  public User createUser(User user, SecureString password, Set<MetadataValue> extra)
    throws GenericException, AlreadyExistsException, IllegalOperationException, NotFoundException,
    AuthorizationDeniedException, RequestNotValidException, ValidationException {
    user.setExtra(extra);
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    User addedUser = model.createUser(user, password, true);
    PremisV3Utils.createOrUpdatePremisUserAgentBinary(user.getName(), model, index, false);
    index.commit(true, RODAMember.class);
    return addedUser;
  }

  public Group createGroup(Group group) throws GenericException, AlreadyExistsException, AuthorizationDeniedException {
    Group newGroup = RodaCoreFactory.getModelService().createGroup(group, true);
    RodaCoreFactory.getIndexService().commit(true, RODAMember.class);
    return newGroup;
  }

  public void updateGroup(Group group) throws GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().updateGroup(group, true);
    RodaCoreFactory.getIndexService().commit(true, RODAMember.class);
  }

  public void deleteGroup(String groupname) throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteGroup(groupname, true);
    RodaCoreFactory.getIndexService().commit(true, RODAMember.class);
  }

  public void requestPasswordReset(String servletPath, String email, String localeString, String ipAddress,
    boolean isRecover)
    throws GenericException, NotFoundException, IllegalOperationException, AuthorizationDeniedException {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    User user = requestPasswordReset(email);
    if (user != null) {
      sendSetPasswordEmail(servletPath, user, localeString, isRecover);
    } else {
      user = new User(email);
    }

    // 20180112 hsilva: need to set ip address for registering the action
    user.setIpAddress(ipAddress);

    // register action
    controllerAssistant.registerAction(user, LogEntryState.SUCCESS, RodaConstants.CONTROLLER_USER_PARAM, user);
  }

  public User requestPasswordReset(String email)
    throws IllegalOperationException, NotFoundException, GenericException, AuthorizationDeniedException {
    return RodaCoreFactory.getModelService().requestPasswordReset(null, email, true, true);
  }

  public User confirmUserEmail(String username, String email, String emailConfirmationToken)
    throws InvalidTokenException, NotFoundException, GenericException {
    return RodaCoreFactory.getModelService().confirmUserEmail(username, email, emailConfirmationToken, true, true);
  }

  public User updateUser(CreateUserRequest request) throws GenericException, AlreadyExistsException, NotFoundException,
    AuthorizationDeniedException, ValidationException, RequestNotValidException {
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    request.getUser().setExtra(request.getValues());
    User modifiedUser = RodaCoreFactory.getModelService().updateUser(request.getUser(), request.getPassword(), true);
    PremisV3Utils.createOrUpdatePremisUserAgentBinary(request.getUser().getName(), model, index, false);
    RodaCoreFactory.getIndexService().commit(true, RODAMember.class);
    return modifiedUser;
  }

  public User updateMyUser(User user, User modifiedUser, SecureString password, Set<MetadataValue> extra)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException,
    ValidationException, RequestNotValidException, IllegalOperationException {

    if (!user.getId().equals(modifiedUser.getId())) {
      throw new IllegalOperationException("Trying to modify user information for another user");
    }

    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    modifiedUser.setExtra(extra);
    User currentUser = model.retrieveUser(modifiedUser.getName());
    User resetUser = resetUser(modifiedUser, currentUser);

    User finalModifiedUser = model.updateMyUser(resetUser, password, true);
    PremisV3Utils.createOrUpdatePremisUserAgentBinary(resetUser.getName(), model, index, false);
    index.commit(true, RODAMember.class);
    return finalModifiedUser;
  }

  public User resetUserPassword(String username, SecureString password, String resetPasswordToken)
    throws InvalidTokenException, IllegalOperationException, NotFoundException, GenericException,
    AuthorizationDeniedException {
    return RodaCoreFactory.getModelService().resetUserPassword(username, password, resetPasswordToken, true, true);
  }

  public User registerUser(HttpServletRequest request, User user, SecureString password, String captcha,
    Set<MetadataValue> extra, String localeString, String servletPath) throws GenericException,
    UserAlreadyExistsException, EmailAlreadyExistsException, AuthorizationDeniedException, RecaptchaException {
    boolean userRegistrationDisabled = RodaCoreFactory.getRodaConfiguration()
      .getBoolean(RodaConstants.USER_REGISTRATION_DISABLED, false);
    if (userRegistrationDisabled) {
      throw new GenericException("User registration is disabled");
    }

    String recaptchakey = RodaCoreFactory.getRodaConfiguration()
      .getString(RodaConstants.UI_GOOGLE_RECAPTCHA_CODE_PROPERTY);

    if (captcha != null) {
      RecaptchaUtils
        .recaptchaVerify(RodaCoreFactory.getRodaConfiguration().getString(RECAPTCHA_CODE_SECRET_PROPERTY, ""), captcha);
    } else if (StringUtils.isNotBlank(recaptchakey)) {
      throw new RecaptchaException("The Captcha can not be null.");
    }

    String ipAddress = request.getRemoteAddr();

    user.setIpAddress(ipAddress);

    User updatedUser = UserUtility.resetGroupsAndRoles(user);

    User registeredUser = RodaCoreFactory.getModelService().registerUser(updatedUser, password, true);
    RodaCoreFactory.getIndexService().commit(true, RODAMember.class);

    if (!user.isActive()) {
      try {
        boolean generateNewToken = false;
        Notification notification = sendEmailVerification(servletPath, updatedUser.getName(), generateNewToken,
          user.getIpAddress(), localeString);

        if (notification.getState() == NotificationState.FAILED) {
          registeredUser.setActive(false);
          boolean notify = true;
          RodaCoreFactory.getModelService().updateUser(registeredUser, password, notify);
        }
      } catch (NotFoundException | AlreadyExistsException | AuthorizationDeniedException e) {
        LOGGER.error("Error updating user", e);
        throw new GenericException(e);
      }
    }
    registeredUser.setResetPasswordToken(null);
    registeredUser.setResetPasswordTokenExpirationDate(null);
    registeredUser.setEmailConfirmationToken(null);
    registeredUser.setEmailConfirmationTokenExpirationDate(null);

    return registeredUser;
  }

  public Notification sendEmailVerification(final String servletPath, final String username,
    final boolean generateNewToken, String ipAddress, String localeString) throws GenericException, NotFoundException {
    final ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = retrieveUser(username);
    // 20170523 hsilva: need to set ip address for registering the action
    user.setIpAddress(ipAddress);

    LogEntryState state = LogEntryState.SUCCESS;

    try {
      if (generateNewToken) {
        user.setEmailConfirmationToken(IdUtils.createUUID());
        user.setEmailConfirmationTokenExpirationDate(DateTime.now().plusDays(1).toDateTimeISO().toInstant().toString());

        try {
          user = updateUser(new CreateUserRequest(user, null, null));
          // 20170523 hsilva: need to set ip address for registering the action
          // as the above method returns a new instante of the modified user
          user.setIpAddress(ipAddress);
        } catch (final RODAException e) {
          throw new GenericException("Error updating email confirmation token - " + e.getMessage(), e);
        }
      }

      if (user.isActive() || user.getEmailConfirmationToken() == null) {
        throw new GenericException(
          "User " + username + " is already active or email confirmation token doesn't exist.");
      }

      return sendEmailVerification(servletPath, user, localeString);
    } catch (RODAException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, RodaConstants.CONTROLLER_USER_PARAM, user);
    }
  }

  public void recoverLoginCheckCaptcha(String captcha) {
    boolean userRegistrationDisabled = RodaCoreFactory.getRodaConfiguration()
      .getBoolean(RodaConstants.USER_REGISTRATION_DISABLED, false);

    try {
      if (userRegistrationDisabled) {
        throw new GenericException("User registration is disabled");
      }

      if (captcha != null) {
        RecaptchaUtils.recaptchaVerify(
          RodaCoreFactory.getRodaConfiguration().getString(RECAPTCHA_CODE_SECRET_PROPERTY, ""), captcha);
      }
    } catch (RecaptchaException | GenericException e) {
      throw new RESTException(e);
    }
  }

  private Notification sendEmailVerification(String servletPath, User user, String localeString)
    throws GenericException {
    try {
      Messages messages = RodaCoreFactory.getI18NMessages(ServerTools.parseLocale(localeString));

      Notification notification = new Notification();
      notification.setSubject(messages.getTranslation(RodaConstants.VERIFICATION_EMAIL_TEMPLATE_SUBJECT_TRANSLATION));
      notification.setFromUser(messages.getTranslation(RodaConstants.VERIFICATION_EMAIL_TEMPLATE_FROM_TRANSLATION));
      notification.setRecipientUsers(Arrays.asList(user.getEmail()));

      String tokenVerifyEmail = user.getEmailConfirmationToken();
      String username = user.getName();
      String verificationEmailURL = servletPath + "/#verifyemail";
      String verificationCompleteURL = verificationEmailURL + "/"
        + URLEncoder.encode(username, RodaConstants.DEFAULT_ENCODING) + "/" + tokenVerifyEmail;

      Map<String, Object> scopes = new HashMap<>();
      scopes.put("username", username);
      scopes.put("token", tokenVerifyEmail);
      scopes.put("verificationURL", verificationEmailURL);
      scopes.put("verificationCompleteURL", verificationCompleteURL);

      Notification res = RodaCoreFactory.getModelService().createNotification(notification,
        new EmailNotificationProcessor(RodaConstants.VERIFICATION_EMAIL_TEMPLATE, scopes, localeString));
      LOGGER.info("The email verification was sent to user {}", user.getName());

      return res;
    } catch (UnsupportedEncodingException | AuthorizationDeniedException e) {
      throw new GenericException("Error sending email verification", e);
    }
  }

  public User login(final String username, final SecureString password, final HttpServletRequest request)
    throws GenericException, AuthenticationDeniedException {
    final User user = RodaCoreFactory.getModelService().retrieveAuthenticatedUser(username, password.toString());
    if (!user.isActive()) {
      if (StringUtils.isNotBlank(user.getEmailConfirmationToken())) {
        throw new EmailUnverifiedException("Email is not verified.");
      }
      throw new InactiveUserException("User is not active.");
    }
    UserUtility.setUser(request, user);
    return user;
  }

  public Set<MetadataValue> retrieveDefaultExtraFormFields() {
    String template = null;

    try (InputStream templateStream = RodaCoreFactory.getConfigurationFileAsStream(
      RodaConstants.USERS_TEMPLATE_FOLDER + "/" + RodaConstants.USER_EXTRA_METADATA_FILE)) {
      template = IOUtils.toString(templateStream, RodaConstants.DEFAULT_ENCODING);
    } catch (IOException e) {
      LOGGER.error("Error getting template from stream", e);
    }

    return ServerTools.transform(template);
  }

}
