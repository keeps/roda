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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
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
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.MetadataValue;
import org.roda.wui.client.browse.UserExtraBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

public class UserManagementHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementHelper.class);

  protected static Long countLogEntries(Filter filter) throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().count(LogEntry.class, filter);
  }

  protected static IndexResult<LogEntry> findLogEntries(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().find(LogEntry.class, filter, sorter, sublist, facets);
  }

  public static LogEntry retrieveLogEntry(String logEntryId) throws GenericException, NotFoundException {
    return RodaCoreFactory.getIndexService().retrieve(LogEntry.class, logEntryId);
  }

  protected static Long countMembers(Filter filter) throws GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().count(RODAMember.class, filter);
  }

  protected static IndexResult<RODAMember> findMembers(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    return RodaCoreFactory.getIndexService().find(RODAMember.class, filter, sorter, sublist, facets);
  }

  protected static IndexResult<RODAMember> findMembers(boolean isUser)
    throws AuthorizationDeniedException, GenericException, RequestNotValidException {
    Filter filter = new Filter();
    filter.add(new SimpleFilterParameter(RodaConstants.MEMBERS_IS_USER, Boolean.toString(isUser)));
    int memberCounter = RodaCoreFactory.getIndexService().count(RODAMember.class, filter).intValue();
    return RodaCoreFactory.getIndexService().find(RODAMember.class, filter, Sorter.NONE, new Sublist(0, memberCounter));
  }

  protected static User retrieveUser(String username) throws GenericException {
    return RodaCoreFactory.getModelService().retrieveUserByName(username);
  }

  protected static Group retrieveGroup(String groupname) throws GenericException, NotFoundException {
    return RodaCoreFactory.getModelService().retrieveGroup(groupname);

  }

  protected static List<Group> listAllGroups() throws GenericException {
    return RodaCoreFactory.getModelService().listGroups();
  }

  public static User registerUser(User user, String password, UserExtraBundle extra)
    throws GenericException, UserAlreadyExistsException, EmailAlreadyExistsException {
    user.setExtra(getUserExtra(user, extra));
    User registeredUser = RodaCoreFactory.getModelService().registerUser(user, password, true);
    RodaCoreFactory.getIndexService().commit(RODAMember.class);
    return registeredUser;
  }

  public static User createUser(User user, String password, UserExtraBundle extra) throws GenericException,
    EmailAlreadyExistsException, UserAlreadyExistsException, IllegalOperationException, NotFoundException {
    user.setExtra(getUserExtra(user, extra));
    User addedUser = RodaCoreFactory.getModelService().createUser(user, password, true);
    RodaCoreFactory.getIndexService().commit(RODAMember.class);
    return addedUser;
  }

  public static User updateUser(User user, String password, UserExtraBundle extra)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    user.setExtra(getUserExtra(user, extra));
    User modifiedUser = RodaCoreFactory.getModelService().updateUser(user, password, true);
    RodaCoreFactory.getIndexService().commit(RODAMember.class);
    return modifiedUser;
  }

  public static User updateMyUser(User user, String password, UserExtraBundle extra)
    throws GenericException, AlreadyExistsException, NotFoundException, AuthorizationDeniedException {
    user.setExtra(getUserExtra(user, extra));
    User modifiedUser = RodaCoreFactory.getModelService().updateMyUser(user, password, true);
    RodaCoreFactory.getIndexService().commit(RODAMember.class);
    return modifiedUser;
  }

  private static String getUserExtra(User user, UserExtraBundle extra) throws GenericException {
    Handlebars handlebars = new Handlebars();
    Map<String, String> data = new HashMap<>();
    handlebars.registerHelper("field", (o, options) -> {
      return options.fn();
    });

    InputStream templateStream = RodaCoreFactory
      .getConfigurationFileAsStream(RodaConstants.USERS_TEMPLATE_FOLDER + "/user_extra.xml.hbs");
    try {
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

      String result = tmpl.apply(data);
      // result = RodaUtils.indentXML(result);
      return result;
    } catch (IOException e1) {
      LOGGER.error("Error getting template from stream");
    }

    return "";
  }

  public static void deleteUser(String username) throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteUser(username, true);
    RodaCoreFactory.getIndexService().commit(RODAMember.class);
  }

  public static void createGroup(Group group) throws GenericException, AlreadyExistsException {
    RodaCoreFactory.getModelService().createGroup(group, true);
    RodaCoreFactory.getIndexService().commit(RODAMember.class);
  }

  public static void updateGroup(Group group) throws GenericException, NotFoundException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().updateGroup(group, true);
    RodaCoreFactory.getIndexService().commit(RODAMember.class);
  }

  public static void deleteGroup(String groupname) throws GenericException, AuthorizationDeniedException {
    RodaCoreFactory.getModelService().deleteGroup(groupname, true);
    RodaCoreFactory.getIndexService().commit(RODAMember.class);
  }

  public static User confirmUserEmail(String username, String email, String emailConfirmationToken)
    throws InvalidTokenException, NotFoundException, GenericException {
    return RodaCoreFactory.getModelService().confirmUserEmail(username, email, emailConfirmationToken, true, true);
  }

  public static User requestPasswordReset(String username, String email)
    throws IllegalOperationException, NotFoundException, GenericException {
    return RodaCoreFactory.getModelService().requestPasswordReset(username, email, true, true);
  }

  public static User resetUserPassword(String username, String password, String resetPasswordToken)
    throws InvalidTokenException, IllegalOperationException, NotFoundException, GenericException {
    return RodaCoreFactory.getModelService().resetUserPassword(username, password, resetPasswordToken, true, true);
  }

}
