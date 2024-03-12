package org.roda.core.model.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.naming.Name;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapName;

import org.apache.logging.log4j.util.Strings;
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
import org.roda.core.repository.LdapGroupRepository;
import org.roda.core.repository.LdapUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class SpringLdapUtility implements LdapUtility {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringLdapUtility.class);

  private final LdapContextSource contextSource;
  private final LdapUserRepository ldapUserRepository;
  private final LdapGroupRepository ldapGroupRepository;

  private String ldapBaseDN = "dc=roda,dc=org";
  private String peopleDN = "ou=users,dc=roda,dc=org";
  private String ldapGroupsDN = "ou=groups,dc=roda,dc=org";
  private String ldapRolesDN = "ou=roles,dc=roda,dc=org";
  private String ldapAdminDN = "uid=admin,ou=system";
  private String ldapPasswordDigestAlgorithm = "PKCS5S2";
  private String rodaGuestDN = "uid=guest,ou=users,dc=roda,dc=org";
  private String rodaAdminDN = "uid=admin,ou=users,dc=roda,dc=org";
  private String rodaAdministratorsDN = "cn=administrators,ou=groups,dc=roda,dc=org";

  private static final String RODA_DUMMY_USER = "cn=roda,ou=system,dc=roda,dc=org";

  public SpringLdapUtility(LdapContextSource contextSource, LdapUserRepository ldapUserRepository,
    LdapGroupRepository ldapGroupRepository) {
    this.contextSource = contextSource;
    this.ldapUserRepository = ldapUserRepository;
    this.ldapGroupRepository = ldapGroupRepository;
  }

  @Override
  public void setRODAAdministratorsDN(String rodaAdministratorsDN) {

  }

  @Override
  public void stopService() throws GenericException {

  }

  @Override
  public void initDirectoryService() throws Exception {

  }

  @Override
  public void initDirectoryService(List<String> ldifs) throws Exception {

  }

  @Override
  public List<User> getUsers() throws GenericException {
    final List<LdapUser> ldapUsers = ldapUserRepository.findAll();
    final List<User> users = new ArrayList<>();
    for (LdapUser ldapUser : ldapUsers) {
      final User user = getUserFromEntry(ldapUser);

      users.add(user);
    }
    return users;
  }

  @Override
  public User getUser(final String uid) throws GenericException {
    LdapUser ldapUser = ldapUserRepository.findByUid(uid);
    if (ldapUser != null) {
      User user = getUserFromEntry(ldapUser);

      // Add all roles assigned to this user
      final Set<String> memberRoles = getMemberRoles();
      user.setAllRoles(memberRoles);

      return user;
    }
    return null;
  }

  private Set<String> getMemberRoles() {

    return null;
  }

  @Override
  public User getUserWithEmail(String email) throws GenericException {
    return null;
  }

  @Override
  public User addUser(User user) throws UserAlreadyExistsException, EmailAlreadyExistsException, GenericException {
    return null;
  }

  @Override
  public User modifyUser(User modifiedUser)
    throws NotFoundException, IllegalOperationException, EmailAlreadyExistsException, GenericException {
    return null;
  }

  @Override
  public void setUserPassword(String username, SecureString password)
    throws IllegalOperationException, NotFoundException, GenericException {

  }

  @Override
  public User modifySelfUser(User modifiedUser, SecureString newPassword)
    throws NotFoundException, EmailAlreadyExistsException, IllegalOperationException, GenericException {
    return null;
  }

  @Override
  public void removeUser(String username) throws IllegalOperationException, GenericException {

  }

  @Override
  public List<Group> getGroups() throws GenericException {
    final List<LdapGroup> ldapGroups = ldapGroupRepository.findAll();
    final List<Group> groups = new ArrayList<>();
    for (LdapGroup ldapGroup : ldapGroups) {
      final Group group = getGroupFromEntry(ldapGroup);
      groups.add(group);
    }
    return groups;
  }

  @Override
  public Group getGroup(String name) throws GenericException, NotFoundException {
    LdapGroup ldapGroup = ldapGroupRepository.findByCommonName(name);
    if (ldapGroup != null) {
      return getGroupFromEntry(ldapGroup);
    }
    return null;
  }

  @Override
  public Group addGroup(Group group) throws GroupAlreadyExistsException, GenericException {
    return null;
  }

  @Override
  public Group modifyGroup(Group modifiedGroup) throws NotFoundException, IllegalOperationException, GenericException {
    return null;
  }

  @Override
  public void removeGroup(String groupname) throws GenericException, IllegalOperationException {

  }

  @Override
  public User getAuthenticatedUser(String username, String password)
    throws AuthenticationDeniedException, GenericException {
    LdapUser ldapUser = ldapUserRepository.findByUid(username);
    LdapName dName = LdapUtils.prepend(LdapUtils.removeFirst(ldapUser.getDn(), contextSource.getBaseLdapName()),
      contextSource.getBaseLdapName());

    DirContext ctx = null;
    try {
      ctx = contextSource.getContext(dName.toString(), password);
      return getUser(username);
    } finally {
      LdapUtils.closeContext(ctx);
    }
  }

  @Override
  public User registerUser(User user, SecureString password)
    throws UserAlreadyExistsException, EmailAlreadyExistsException, GenericException {
    return null;
  }

  @Override
  public User confirmUserEmail(String username, String email, String emailConfirmationToken)
    throws NotFoundException, InvalidTokenException, GenericException {
    return null;
  }

  @Override
  public User requestPasswordReset(String username, String email)
    throws NotFoundException, IllegalOperationException, GenericException {
    return null;
  }

  @Override
  public User resetUserPassword(String username, SecureString password, String resetPasswordToken)
    throws NotFoundException, InvalidTokenException, IllegalOperationException, GenericException {
    return null;
  }

  @Override
  public void addRole(String roleName) throws RoleAlreadyExistsException, GenericException {

  }

  @Override
  public void resetAdminAccess(SecureString password) throws GenericException {

  }

  @Override
  public boolean isInternal(String username) throws GenericException, NotFoundException {
    return false;
  }

  private User getUserFromEntry(final LdapUser ldapUser) {
    User user = new User(ldapUser.getUid());
    user.setFullName(ldapUser.getCommonName());
    user.setActive("0".equalsIgnoreCase(ldapUser.getShadowInactive()));
    user.setEmail(ldapUser.getEmail());
    user.setGuest(false);
    user.setExtra(ldapUser.getDescription());
    if (Strings.isNotBlank(ldapUser.getInfo())) {
      final String infoStr = ldapUser.getInfo();

      final String[] parts = infoStr.split(";");

      if (parts.length >= 1 && parts[0].trim().length() > 0) {
        user.setEmailConfirmationToken(parts[0].trim());
      }
      if (parts.length >= 2 && parts[1].trim().length() > 0) {
        user.setEmailConfirmationTokenExpirationDate(parts[1].trim());
      }
      if (parts.length >= 3 && parts[2].trim().length() > 0) {
        user.setResetPasswordToken(parts[2].trim());
      }
      if (parts.length >= 4 && parts[3].trim().length() > 0) {
        user.setResetPasswordTokenExpirationDate(parts[3].trim());
      }
    }
    return user;
  }

  private Group getGroupFromEntry(final LdapGroup ldapGroup) {
    Group group = new Group(ldapGroup.getCommonName());
    group.setActive("0".equalsIgnoreCase(ldapGroup.getShadowInactive()));
    group.setFullName(ldapGroup.getOu());

    for (Name name : ldapGroup.getUniqueMember()) {
      String memberDN = name.toString();
      if (memberDN.endsWith(peopleDN)) {
        group.addMemberUser(LdapUtils.getStringValue(name, "uid"));
      } else if (memberDN.endsWith(ldapGroupsDN)) {
        LOGGER.warn("Ignoring sub-group {} connection with group {}", memberDN, group.getId());
      } else if (!memberDN.equals(RODA_DUMMY_USER)) {
        LOGGER.warn("Member {} outside users and groups", memberDN);
      }
    }

    return group;
  }
}
