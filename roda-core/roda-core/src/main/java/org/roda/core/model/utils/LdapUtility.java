package org.roda.core.model.utils;

import java.nio.file.Path;
import java.util.List;

import org.roda.core.data.common.SecureString;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.GroupAlreadyExistsException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.InvalidTokenException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RoleAlreadyExistsException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface LdapUtility {
  void setRODAAdministratorsDN(String rodaAdministratorsDN);

  void stopService() throws GenericException;

  void initDirectoryService() throws Exception;

  void initDirectoryService(List<String> ldifs) throws Exception;

  List<User> getUsers() throws GenericException;

  User getUser(String name) throws GenericException;

  User getUserWithEmail(String email) throws GenericException;

  User addUser(User user) throws UserAlreadyExistsException, EmailAlreadyExistsException, GenericException;

  User modifyUser(User modifiedUser)
    throws NotFoundException, IllegalOperationException, EmailAlreadyExistsException, GenericException;

  void setUserPassword(String username, SecureString password)
    throws IllegalOperationException, NotFoundException, GenericException;

  User modifySelfUser(User modifiedUser, SecureString newPassword)
    throws NotFoundException, EmailAlreadyExistsException, IllegalOperationException, GenericException;

  void removeUser(String username) throws IllegalOperationException, GenericException;

  List<Group> getGroups() throws GenericException;

  Group getGroup(String name) throws GenericException, NotFoundException;

  Group addGroup(Group group) throws GroupAlreadyExistsException, GenericException;

  Group modifyGroup(Group modifiedGroup) throws NotFoundException, IllegalOperationException, GenericException;

  void removeGroup(String groupname) throws GenericException, IllegalOperationException;

  User getAuthenticatedUser(String username, String password) throws AuthenticationDeniedException, GenericException;

  User registerUser(User user, SecureString password)
    throws UserAlreadyExistsException, EmailAlreadyExistsException, GenericException;

  User confirmUserEmail(String username, String email, String emailConfirmationToken)
    throws NotFoundException, InvalidTokenException, GenericException;

  User requestPasswordReset(String username, String email)
    throws NotFoundException, IllegalOperationException, GenericException;

  User resetUserPassword(String username, SecureString password, String resetPasswordToken)
    throws NotFoundException, InvalidTokenException, IllegalOperationException, GenericException;

  void addRole(String roleName) throws RoleAlreadyExistsException, GenericException;

  void resetAdminAccess(SecureString password) throws GenericException;

  boolean isInternal(String username) throws GenericException, NotFoundException;
}
