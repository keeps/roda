/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.common.UserUtility;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidTokenException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.notifications.Notification.NOTIFICATION_STATE;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.RodaPrincipal;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.wui.client.browse.MetadataValue;
import org.roda.wui.client.browse.bundle.UserExtraBundle;
import org.roda.wui.common.server.ServerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

public class UserManagementHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementHelper.class);

  private UserManagementHelper() {
    // do nothing
  }

  protected static User retrieveUser(String username) throws GenericException {
    return RodaCoreFactory.getModelService().retrieveUser(username);
  }

  protected static Group retrieveGroup(String groupname) throws GenericException, NotFoundException {
    return RodaCoreFactory.getModelService().retrieveGroup(groupname);
  }

  protected static List<Group> listAllGroups() throws GenericException {
    return RodaCoreFactory.getModelService().listGroups();
  }

  public static User registerUser(User user, String password, UserExtraBundle extra, String localeString,
    String servletPath)
    throws GenericException, UserAlreadyExistsException, EmailAlreadyExistsException, AuthorizationDeniedException {
    user.setExtra(getUserExtra(extra));
    User updatedUser = UserUtility.resetGroupsAndRoles(user);

    User registeredUser = RodaCoreFactory.getModelService().registerUser(updatedUser, password, true);
    RodaCoreFactory.getIndexService().commit(true, RODAMember.class);

    if (!user.isActive()) {
      try {
        boolean generateNewToken = false;
        Notification notification = UserManagement.sendEmailVerification(servletPath, updatedUser.getName(),
          generateNewToken, user.getIpAddress(), localeString);

        if (notification.getState() == NOTIFICATION_STATE.FAILED) {
          registeredUser.setActive(true);
          boolean notify = true;
          RodaCoreFactory.getModelService().updateUser(registeredUser, password, notify);
        }
      } catch (NotFoundException | AlreadyExistsException | AuthorizationDeniedException e) {
        LOGGER.error("Error updating user", e);
        throw new GenericException(e);
      }
    }

    return registeredUser;
  }

  public static User createUser(User user, String password, UserExtraBundle extra)
    throws GenericException, AlreadyExistsException, IllegalOperationException, NotFoundException,
    AuthorizationDeniedException, RequestNotValidException, ValidationException {
    user.setExtra(getUserExtra(extra));
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    User addedUser = model.createUser(user, password, true);
    PremisV3Utils.createOrUpdatePremisUserAgentBinary(user.getName(), model, index, false);
    index.commit(true, RODAMember.class);
    return addedUser;
  }

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

  public static void changeActiveMembers(SelectedItems<RODAMember> members, boolean active) throws GenericException,
    RequestNotValidException, AuthorizationDeniedException, NotFoundException, AlreadyExistsException {
    List<String> uuids = getMemberUuidFromSelectedItems(members);
    for (String uuid : uuids) {
      if (RodaPrincipal.isUser(uuid)) {
        RodaCoreFactory.getModelService().deActivateUser(RodaPrincipal.getId(uuid), active, true);
      }
    }
    RodaCoreFactory.getIndexService().commit(true, RODAMember.class);
  }

  public static User updateUser(User user, String password, UserExtraBundle extra)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException,
    ValidationException, RequestNotValidException {
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    user.setExtra(getUserExtra(extra));
    User modifiedUser = RodaCoreFactory.getModelService().updateUser(user, password, true);
    PremisV3Utils.createOrUpdatePremisUserAgentBinary(user.getName(), model, index, false);
    RodaCoreFactory.getIndexService().commit(true, RODAMember.class);
    return modifiedUser;
  }

  public static User updateMyUser(User modifiedUser, String password, UserExtraBundle extra)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException,
    ValidationException, RequestNotValidException {
    ModelService model = RodaCoreFactory.getModelService();
    IndexService index = RodaCoreFactory.getIndexService();
    modifiedUser.setExtra(getUserExtra(extra));

    User currentUser = model.retrieveUser(modifiedUser.getName());
    User resetUser = resetUser(modifiedUser, currentUser);

    User finalModifiedUser = model.updateMyUser(resetUser, password, true);
    PremisV3Utils.createOrUpdatePremisUserAgentBinary(resetUser.getName(), model, index, false);
    index.commit(true, RODAMember.class);
    return finalModifiedUser;
  }

  private static User resetUser(User newUser, User oldUser) {
    newUser.setActive(oldUser.isActive());
    newUser.setDirectRoles(oldUser.getDirectRoles());
    newUser.setAllRoles(oldUser.getAllRoles());
    newUser.setGroups(oldUser.getGroups());
    return newUser;
  }

  private static String getUserExtra(UserExtraBundle extra) {
    Handlebars handlebars = new Handlebars();
    Map<String, String> data = new HashMap<>();
    handlebars.registerHelper("field", (o, options) -> {
      return options.fn();
    });

    try (InputStream templateStream = RodaCoreFactory.getConfigurationFileAsStream(
      RodaConstants.USERS_TEMPLATE_FOLDER + "/" + RodaConstants.USER_EXTRA_METADATA_FILE)) {
      String rawTemplate = IOUtils.toString(templateStream, RodaConstants.DEFAULT_ENCODING);
      Template tmpl = handlebars.compileInline(rawTemplate);

      if (extra != null) {
        Set<MetadataValue> values = extra.getValues();
        if (values != null) {
          values.forEach(metadataValue -> {
            String val = metadataValue.get("value");
            if (val != null) {
              val = val.replaceAll("\\s", "");
              if (!"".equals(val)) {
                data.put(metadataValue.get("name"), metadataValue.get("value"));
              }
            }
          });
        }
      }

      // result = RodaUtils.indentXML(result);
      return tmpl.apply(data);
    } catch (IOException e) {
      LOGGER.error("Error getting template from stream");
    }

    return "";
  }

  public static void deleteMembers(SelectedItems<RODAMember> members)
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

  public static void deleteUser(String username) throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteUser(username, true);
    RodaCoreFactory.getIndexService().commit(true, RODAMember.class);
  }

  public static void createGroup(Group group)
    throws GenericException, AlreadyExistsException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().createGroup(group, true);
    RodaCoreFactory.getIndexService().commit(true, RODAMember.class);
  }

  public static void updateGroup(Group group) throws GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().updateGroup(group, true);
    RodaCoreFactory.getIndexService().commit(true, RODAMember.class);
  }

  public static void deleteGroup(String groupname) throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteGroup(groupname, true);
    RodaCoreFactory.getIndexService().commit(true, RODAMember.class);
  }

  public static User confirmUserEmail(String username, String email, String emailConfirmationToken)
    throws InvalidTokenException, NotFoundException, GenericException {
    return RodaCoreFactory.getModelService().confirmUserEmail(username, email, emailConfirmationToken, true, true);
  }

  public static User requestPasswordReset(String username, String email)
    throws IllegalOperationException, NotFoundException, GenericException, AuthorizationDeniedException {
    return RodaCoreFactory.getModelService().requestPasswordReset(username, email, true, true);
  }

  public static User resetUserPassword(String username, String password, String resetPasswordToken)
    throws InvalidTokenException, IllegalOperationException, NotFoundException, GenericException,
    AuthorizationDeniedException {
    return RodaCoreFactory.getModelService().resetUserPassword(username, password, resetPasswordToken, true, true);
  }

  public static UserExtraBundle retrieveUserExtraBundle(String name) {
    if (!RodaConstants.SYSTEM_USERS.contains(name)) {
      String template = null;

      try (InputStream templateStream = RodaCoreFactory.getConfigurationFileAsStream(
        RodaConstants.USERS_TEMPLATE_FOLDER + "/" + RodaConstants.USER_EXTRA_METADATA_FILE)) {
        template = IOUtils.toString(templateStream, RodaConstants.DEFAULT_ENCODING);
      } catch (IOException e) {
        LOGGER.error("Error getting template from stream", e);
      }

      Set<MetadataValue> values = ServerTools.transform(template);

      try {
        User user = RodaCoreFactory.getModelService().retrieveUser(name);
        String userExtra = user.getExtra();

        if (userExtra != null && !values.isEmpty()) {
          for (MetadataValue mv : values) {
            // clear the auto-generated values
            // mv.set("value", null);
            String xpathRaw = mv.get("xpath");
            if (xpathRaw != null && xpathRaw.length() > 0) {
              String[] xpaths = xpathRaw.split("##%##");
              String value;
              List<String> allValues = new ArrayList<>();
              for (String xpath : xpaths) {
                allValues.addAll(ServerTools.applyXpath(userExtra, xpath));
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

      } catch (GenericException e) {
        // do nothing
      }

      return new UserExtraBundle(name, values);
    } else {
      return new UserExtraBundle(name);
    }
  }

  public static UserExtraBundle retrieveDefaultExtraBundle() {
    String template = null;

    try (InputStream templateStream = RodaCoreFactory.getConfigurationFileAsStream(
      RodaConstants.USERS_TEMPLATE_FOLDER + "/" + RodaConstants.USER_EXTRA_METADATA_FILE)) {
      template = IOUtils.toString(templateStream, RodaConstants.DEFAULT_ENCODING);
    } catch (IOException e) {
      LOGGER.error("Error getting template from stream", e);
    }

    return new UserExtraBundle("", ServerTools.transform(template));
  }
}
