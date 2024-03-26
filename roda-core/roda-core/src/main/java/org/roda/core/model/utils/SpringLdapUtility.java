package org.roda.core.model.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
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
import org.roda.core.repository.LdapRoleRepository;
import org.roda.core.repository.LdapUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.InvalidNameException;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.LdapAttributes;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.ldif.parser.LdifParser;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class SpringLdapUtility implements LdapUtility {

  /** Class logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(SpringLdapUtility.class);

  /** RODA instance name. */
  private static final String INSTANCE_NAME = "RODA";

  /** Size of random passwords */
  private static final int RANDOM_PASSWORD_LENGTH = 12;

  /** Shadow inactive constant. */
  private static final String SHADOW_INACTIVE = "shadowInactive";

  /** Unique member constant. */
  private static final String UNIQUE_MEMBER = "uniqueMember";

  /** Role occupant constant. */
  private static final String ROLE_OCCUPANT = "roleOccupant";

  /** Object class constant. */
  private static final String OBJECT_CLASS = "objectClass";

  /** Constant: top. */
  private static final String OBJECT_CLASS_TOP = "top";

  /** Constant: organization. */
  private static final String OBJECT_CLASS_ORGANIZATION = "organization";

  /** Constant: top. */
  private static final String OBJECT_CLASS_ORGANIZATIONAL_UNIT = "organizationalUnit";

  /** Constant: groupOfUniqueNames. */
  private static final String GROUP_OF_UNIQUE_NAMES = "groupOfUniqueNames";

  /** Constant: domain. */
  private static final String OBJECT_CLASS_DOMAIN = "dcObject";

  /** Constant: extensibleObject. */
  private static final String OBJECT_CLASS_EXTENSIBLE_OBJECT = "extensibleObject";

  /** Constant: userPassword. */
  private static final String USER_PASSWORD = "userPassword";

  /** Constant: dc. */
  private static final String DC = "dc";

  /** Constant: o. */
  private static final String O = "o";

  /** Constant: uid. */
  private static final String UID = "uid";

  /** Constant: cn. */
  private static final String CN = "cn";

  /** Constant: ou. */
  private static final String OU = "ou";

  /** Constant: email. */
  private static final String EMAIL = "email";

  private static final String RODA_DUMMY_USER = "cn=roda,ou=system,dc=roda,dc=org";

  /** The port where LDAP server should bind. */
  private int ldapPort = 10389;

  /**
   * LDAP administrator Distinguished Name (DN).
   */
  private String ldapAdminDN = null;

  /**
   * LDAP administrator password.
   */
  private String ldapAdminPassword = null;

  /**
   * LDAP DN of the root.
   */
  private String ldapRootDN = "";

  /**
   * LDAP OU of the people entry (default: null).
   */
  private String ldapPeopleDN = null;

  /**
   * LDAP OU of the groups entry (default: null).
   */
  private String ldapGroupsDN = null;

  /**
   * LDAP OU of the roles entry (default: null).
   */
  private String ldapRolesDN = null;

  /**
   * Password Digest Algorithm.
   */
  private String ldapDigestAlgorithm = "MD5";

  /**
   * List of protected users. Users in the protected list cannot be modified.
   *
   * The list of protected users can be set in roda-core.properties file.
   */
  private List<String> ldapProtectedUsers = new ArrayList<>();

  /**
   * List of protected groups. Groups in the protected list cannot be modified.
   *
   * The list of protected groups can be set in roda-core.properties file.
   */
  private List<String> ldapProtectedGroups = new ArrayList<>();

  /**
   * RODA guest user Distinguished Name (DN).
   */
  private String rodaGuestDN = null;

  /**
   * RODA administrator user Distinguished Name (DN).
   */
  private String rodaAdminDN = null;

  /**
   * RODA administrator group Distinguished Name (DN).
   */
  private String rodaAdministratorsDN = null;

  /**
   * Directory where ApacheDS data will be stored.
   */

  private final LdapTemplate ldapTemplate;
  private final LdapUserRepository ldapUserRepository;
  private final LdapGroupRepository ldapGroupRepository;
  private final LdapRoleRepository ldapRoleRepository;
  private static final LdapName ldapRootDNName = LdapUtils.newLdapName("dc=roda,dc=org");
  private static final LdapName ldapRolesOU = LdapUtils.newLdapName("ou=roles");
  private static final LdapName ldapPeopleOU = LdapUtils.newLdapName("ou=users");
  private static final LdapName ldapGroupsOU = LdapUtils.newLdapName("ou=groups");

  public SpringLdapUtility(LdapTemplate ldapTemplate, LdapUserRepository ldapUserRepository,
    LdapGroupRepository ldapGroupRepository, LdapRoleRepository ldapRoleRepository) {
    this.ldapTemplate = ldapTemplate;
    this.ldapUserRepository = ldapUserRepository;
    this.ldapGroupRepository = ldapGroupRepository;
    this.ldapRoleRepository = ldapRoleRepository;
  }

  public void setup(final String ldapUrl, final int ldapPort, final String ldapRootDN, final String ldapPeopleDN,
    final String ldapGroupsDN, final String ldapRolesDN, final String ldapAdminDN, final String ldapAdminPassword,
    final String ldapPasswordDigestAlgorithm, final List<String> ldapProtectedUsers,
    final List<String> ldapProtectedGroups, final String rodaGuestDN, final String rodaAdminDN) {
    this.ldapRootDN = ldapRootDN;
    this.ldapPeopleDN = ldapPeopleDN;
    this.ldapGroupsDN = ldapGroupsDN;
    this.ldapRolesDN = ldapRolesDN;

    this.ldapProtectedUsers.clear();
    if (ldapProtectedUsers != null) {
      this.ldapProtectedUsers.addAll(ldapProtectedUsers);
      LOGGER.debug("Protected users: {}", this.ldapProtectedUsers);
    }

    this.ldapProtectedGroups.clear();
    if (ldapProtectedGroups != null) {
      this.ldapProtectedGroups.addAll(ldapProtectedGroups);
      LOGGER.debug("Protected groups: {}", this.ldapProtectedGroups);
    }
    this.rodaGuestDN = rodaGuestDN;
    this.rodaAdminDN = rodaAdminDN;

    LdapContextSource contextSource = new LdapContextSource();
    contextSource.setUrl(ldapUrl + ":" + ldapPort);
    contextSource.setBase(ldapRootDN);
    contextSource.setUserDn(ldapAdminDN);
    contextSource.setPassword(ldapAdminPassword);
    contextSource.afterPropertiesSet();

    ldapTemplate.setContextSource(contextSource);
  }

  @Override
  public void setRODAAdministratorsDN(String rodaAdministratorsDN) {
    this.rodaAdministratorsDN = rodaAdministratorsDN;
  }

  /**
   * Stop the directory service and LDAP server if it is running.
   *
   * @throws GenericException
   *           is some error occurred during shutdown.
   */
  @Override
  public void stopService() throws GenericException {

  }

  /**
   * Initialize the server. It creates the partition and adds the index.
   *
   * @throws Exception
   *           if there were some problems while initializing the system
   */
  @Override
  public void initDirectoryService() throws Exception {
    initDirectoryService(null);
  }

  /**
   * Initialize the server. It creates the partition, adds the index, and injects
   * the context entries for the created partitions.
   *
   * @param ldifs
   *          LDIF files to apply to Directory Service.
   * @throws Exception
   *           if there were some problems while initializing the system
   */
  @Override
  public void initDirectoryService(List<String> ldifs) throws Exception {
    // Add root DN
    addRootEntry();

    // Add roles DN
    addOrganizationUnitIfNotExists(ldapRolesOU);

    // Add people DN
    addOrganizationUnitIfNotExists(ldapPeopleOU);

    // Add groups DN
    addOrganizationUnitIfNotExists(ldapGroupsOU);

    if (ldifs != null) {
      for (String ldif : ldifs) {
        applyLdif(ldif);
      }
    }
  }

  /**
   * Return all users
   *
   * @return a list of {@link User}'s.
   *
   * @throws GenericException
   *           if some error occurs.
   */
  @Override
  public List<User> getUsers() throws GenericException {
    try {
      final List<LdapUser> ldapUsers = ldapUserRepository.findAll();
      final List<User> users = new ArrayList<>();
      for (LdapUser ldapUser : ldapUsers) {
        final User user = getUserFromEntry(ldapUser);

        // Add all roles assigned to this user
        final Set<String> memberRoles = getMemberRoles(getUserDN(user.getName()));
        user.setAllRoles(memberRoles);

        // Add direct roles assigned to this user
        for (String role : getMemberDirectRoles(getUserDN(user.getName()))) {
          user.addDirectRole(role);
        }

        // Add groups to which this user belongs
        user.setGroups(getUserGroups(user.getName()));

        users.add(user);
      }
      return users;
    } catch (NamingException e) {
      throw new GenericException("Error getting users", e);
    }
  }

  /**
   * Returns the User with name <code>uid</code> or <code>null</code> if it
   * doesn't exist.
   *
   * @param name
   *          the name of the desired User.
   *
   * @return the User with name <code>name</code> or <code>null</code> if it
   *         doesn't exist.
   *
   * @throws GenericException
   *           if the user information could not be retrieved from the LDAP
   *           server.
   */
  @Override
  public User getUser(final String name) throws GenericException {
    try {
      LdapUser ldapUser = ldapUserRepository.findByUid(name);
      if (ldapUser != null) {
        return getUser(ldapUser);
      }
      return new User();
    } catch (NamingException e) {
      throw new GenericException("Error getting user " + name, e);
    }
  }

  /**
   * Returns the {@link User} with email <code>email</code> or <code>null</code>
   * if it doesn't exist.
   *
   * @param email
   *          the email of the desired {@link User}.
   *
   * @return the {@link User} with email <code>email</code> or <code>null</code>
   *         if it doesn't exist.
   *
   * @throws GenericException
   *           if the user information could not be retrieved from the LDAP
   *           server.
   */
  @Override
  public User getUserWithEmail(String email) throws GenericException {
    try {
      LdapUser ldapUser = ldapUserRepository.findFirstByEmail(email);
      if (ldapUser != null) {
        return getUser(ldapUser);
      }
      return null;
    } catch (NamingException e) {
      throw new GenericException("Error getting user with email " + email, e);
    }
  }

  /**
   * Adds a new {@link User}.
   *
   * @param user
   *          the {@link User} to add.
   *
   * @return the newly created {@link User}.
   *
   * @throws UserAlreadyExistsException
   *           if a User with the same name already exists.
   * @throws EmailAlreadyExistsException
   *           if the {@link User}'s email is already used.
   * @throws GenericException
   *           if something goes wrong with the creation of the new user.
   */
  @Override
  public User addUser(final User user)
    throws UserAlreadyExistsException, EmailAlreadyExistsException, GenericException {
    if (!user.isNameValid()) {
      LOGGER.debug("'{}' is not a valid user name.", user.getName());
      throw new GenericException("'" + user.getName() + "' is not a valid user name.");
    }

    if (getUserWithEmail(user.getEmail()) != null) {
      LOGGER.debug("The email address {} is already used.", user.getEmail());
      throw new EmailAlreadyExistsException("The email address " + user.getEmail() + " is already used.");
    }

    try {
      LdapUser ldapUser = getLdapUserFromUser(user);
      ldapUser.setNew(true);
      ldapUserRepository.save(ldapUser);
      setMemberDirectRoles(ldapUser.getDn(), user.getDirectRoles());
      setMemberGroups(ldapUser.getDn(), user.getGroups());

      if (user.isActive()) {
        try (SecureString randomPassword = new SecureString(
          RandomStringUtils.random(RANDOM_PASSWORD_LENGTH).toCharArray())) {
          setUserPasswordUnchecked(user.getName(), randomPassword);
        } catch (final NotFoundException e) {
          LOGGER.error("Created user doesn't exist! Notify developers!!!", e);
        }
      }
    } catch (final NameAlreadyBoundException e) {
      LOGGER.debug(e.getMessage(), e);
      throw new UserAlreadyExistsException(userMessage(user.getName(), " already exists."), e);
    } catch (final NamingException e) {
      LOGGER.debug(e.getMessage(), e);
      throw new GenericException("Error adding user " + user.getName(), e);
    }

    final User newUser = getUser(user.getName());
    if (newUser == null) {
      throw new GenericException("The user was not created!");
    } else {
      return newUser;
    }
  }

  /**
   * Modify the {@link User}'s information.
   *
   * @param modifiedUser
   *          the {@link User} to modify.
   *
   * @return the modified {@link User}.
   *
   * @throws NotFoundException
   *           if the {@link User} being modified doesn't exist.
   * @throws EmailAlreadyExistsException
   *           if the specified email is already used by another user.
   * @throws IllegalOperationException
   *           if the user is one of the protected users.
   * @throws GenericException
   *           if some error occurred.
   */
  @Override
  public User modifyUser(final User modifiedUser)
    throws NotFoundException, IllegalOperationException, EmailAlreadyExistsException, GenericException {
    modifyUser(modifiedUser, null, true, false);
    return getUser(modifiedUser.getName());
  }

  /**
   * Sets the user's password.
   *
   * @param username
   *          the username.
   * @param password
   *          the password.
   *
   * @throws NotFoundException
   *           if specified {@link User} doesn't exist.
   * @throws IllegalOperationException
   *           if the user is one of the protected users.
   * @throws GenericException
   *           if some error occurs.
   */
  @Override
  public void setUserPassword(final String username, SecureString password)
    throws IllegalOperationException, NotFoundException, GenericException {

    final String userDN = getUserDN(username);
    if (this.rodaGuestDN.equals(userDN) || this.ldapProtectedUsers.contains(username)) {
      throw new IllegalOperationException(String.format("User (%s) is protected and cannot be modified.", username));
    }

    setUserPasswordUnchecked(username, password);
  }

  /**
   * Modify the {@link User}'s information.
   *
   * @param modifiedUser
   *          the {@link User} to modify.
   *
   * @param newPassword
   *          the new {@link User}'s password. To maintain the current password,
   *          use <code>null</code>.
   *
   * @return the modified {@link User}.
   *
   * @throws NotFoundException
   *           if the Use being modified doesn't exist.
   * @throws EmailAlreadyExistsException
   *           if the specified email is already used by another user.
   * @throws IllegalOperationException
   *           if the user is one of the protected users.
   * @throws GenericException
   *           if some error occurred.
   */
  @Override
  public User modifySelfUser(final User modifiedUser, SecureString newPassword)
    throws NotFoundException, EmailAlreadyExistsException, IllegalOperationException, GenericException {
    modifyUser(modifiedUser, newPassword, false, false);
    return getUser(modifiedUser.getName());
  }

  /**
   * Removes a {@link User}.
   *
   * @param username
   *          the name of the user to remove.
   *
   * @throws IllegalOperationException
   *           if the user is one of the protected users.
   * @throws GenericException
   *           if some error occurred.
   */
  @Override
  public void removeUser(final String username) throws IllegalOperationException, GenericException {
    final String userDN = getUserDN(username);
    if (this.rodaAdminDN.equals(userDN) || this.rodaGuestDN.equals(userDN)
      || this.ldapProtectedUsers.contains(username)) {
      throw new IllegalOperationException(userMessage(username, " is protected and cannot be removed."));
    }
    try {
      removeMember(getUserDN(username));
    } catch (NamingException e) {
      throw new GenericException("Error removing user " + username, e);
    }
  }

  /**
   * Return all groups
   *
   * @return an array of {@link Group}'s.
   *
   * @throws GenericException
   *           if some error occurred.
   */
  @Override
  public List<Group> getGroups() throws GenericException {
    try {
      final List<LdapGroup> ldapGroups = ldapGroupRepository.findAll();
      final List<Group> groups = new ArrayList<>();
      for (LdapGroup ldapGroup : ldapGroups) {
        final Group group = getGroupFromEntry(ldapGroup);

        // Add all roles assigned to this group
        final Set<String> memberRoles = getMemberRoles(getGroupDN(group.getName()));
        group.setAllRoles(memberRoles);

        // Add direct roles assigned to this group
        for (String role : getMemberDirectRoles(getGroupDN(group.getName()))) {
          group.addDirectRole(role);
        }

        groups.add(group);
      }
      return groups;
    } catch (NamingException e) {
      throw new GenericException("Error getting groups - " + e.getMessage(), e);
    }
  }

  /**
   * Returns the group named <code>grpName</code>.
   *
   * @param name
   *          the name of the group.
   *
   * @return a Group if the group exists, otherwise <code>null</code>.
   *
   * @throws GenericException
   *           if the group information could not be retrieved from the LDAP
   *           server.
   * @throws NotFoundException
   *           if the group doesn't exist.
   */
  @Override
  public Group getGroup(final String name) throws GenericException, NotFoundException {
    try {
      LdapGroup ldapGroup = ldapGroupRepository.findByCommonName(name);
      if (ldapGroup != null) {
        return getGroupFromEntry(ldapGroup);
      } else {
        return null;
      }
    } catch (NamingException e) {
      throw new GenericException("Error searching for group " + name, e);
    }
  }

  /**
   * Add a new {@link Group}.
   *
   * @param group
   *          the {@link Group} to add.
   * @return the newly created {@link Group}.
   * @throws GroupAlreadyExistsException
   *           if a Group with the same name already exists.
   * @throws GenericException
   *           if something goes wrong with the creation of the new group.
   */
  @Override
  public Group addGroup(final Group group) throws GroupAlreadyExistsException, GenericException {
    if (!group.isNameValid()) {
      throw new GenericException("'" + group.getName() + "' is not a valid group name.");
    }

    try {
      LdapGroup ldapGroup = getLdapGroupFromGroup(group);
      ldapGroup.setNew(true);
      ldapGroupRepository.save(ldapGroup);

      setMemberDirectRoles(ldapGroup.getId(), group.getDirectRoles());
    } catch (NameAlreadyBoundException e) {
      throw new GroupAlreadyExistsException("Group " + group.getName() + " already exists.", e);
    } catch (NamingException e) {
      throw new GenericException("Error adding group " + group.getName(), e);
    }

    final Group newGroup;
    try {
      newGroup = getGroup(group.getName());
    } catch (NotFoundException e) {
      throw new GenericException("The group was not created! " + e.getMessage());
    }
    if (newGroup == null) {
      throw new GenericException("The group was not created!");
    } else {
      return newGroup;
    }

  }

  /**
   * Modify the {@link Group}'s information.
   *
   * @param modifiedGroup
   *          the {@link Group} to modify.
   *
   * @return the modified {@link Group}.
   *
   * @throws NotFoundException
   *           if the group with being modified doesn't exist.
   * @throws IllegalOperationException
   *           if the user is one of the protected users.
   * @throws GenericException
   *           if some error occurred.
   * @throws GenericException
   *           if some error occurred.
   */
  @Override
  public Group modifyGroup(final Group modifiedGroup)
    throws NotFoundException, IllegalOperationException, GenericException {
    return modifyGroup(modifiedGroup, false);
  }

  /**
   * Removes a group.
   *
   * @param groupname
   *          the name of the group to remove.
   * @throws IllegalOperationException
   *           if the user is one of the protected users.
   * @throws GenericException
   *           if some error occurred.
   */
  @Override
  public void removeGroup(final String groupname) throws GenericException, IllegalOperationException {
    if (this.rodaAdministratorsDN.equals(getGroupDN(groupname)) || this.ldapProtectedGroups.contains(groupname)) {
      throw new IllegalOperationException("Group (" + groupname + ") is protected and cannot be removed.");
    }
    try {
      removeMember(getGroupDN(groupname));
    } catch (final NamingException e) {
      throw new GenericException("Error removing group " + groupname, e);
    }
  }

  /**
   * Gets {@link User} with <code>username</code> and <code>password</code> from
   * LDAP server using this username and password as login for LDAP to verify that
   * the parameters are valid.
   *
   * @param username
   *          the user's username.
   * @param password
   *          the user's password.
   *
   * @return the {@link User} registered in LDAP.
   *
   * @throws AuthenticationDeniedException
   *           if the provided credentials are not valid.
   * @throws GenericException
   *           if some error occurred.
   */
  @Override
  public User getAuthenticatedUser(final String username, final String password)
    throws AuthenticationDeniedException, GenericException {

    if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
      throw new AuthenticationDeniedException("Username and password cannot be blank!");
    }

    try {
      ldapTemplate.authenticate(LdapQueryBuilder.query().where(UID).is(username), password);
      return getUser(username);
    } catch (AuthenticationException e) {
      throw new AuthenticationDeniedException(e.getMessage(), e);
    } catch (NamingException e) {
      throw new GenericException(e.getMessage(), e);
    }
  }

  /**
   * Register a new {@link User}. The new {@link User} will be inactive and a
   * email validation token will be generated.
   *
   * @param user
   *          the new {@link User} to create.
   * @param password
   *          the new {@link User} password.
   *
   * @return the newly created {@link User}.
   *
   * @throws UserAlreadyExistsException
   *           if a {@link User} with the same name already exists.
   * @throws EmailAlreadyExistsException
   *           if the {@link User}'s email is already used.
   * @throws GenericException
   *           if something goes wrong with the register process.
   */
  @Override
  public User registerUser(final User user, SecureString password)
    throws UserAlreadyExistsException, EmailAlreadyExistsException, GenericException {
    // TODO
    return null;
  }

  /**
   * Confirms the {@link User} email using the token supplied at register time and
   * activate the {@link User}.
   * <p>
   * The <code>username</code> and <code>email</code> are used to identify the
   * user. One of them can be <code>null</code>, but not both at the same time.
   * </p>
   *
   * @param username
   *          the name of the {@link User}.
   * @param email
   *          the email address of the {@link User}.
   * @param emailConfirmationToken
   *          the email confirmation token.
   *
   * @return the {@link User} whose email has been confirmed.
   *
   * @throws NotFoundException
   *           if the username and email don't exist.
   * @throws IllegalArgumentException
   *           if username and email are <code>null</code>.
   * @throws InvalidTokenException
   *           if the specified token doesn't exist, has already expired or it
   *           doesn't correspond to the stored token.
   * @throws GenericException
   *           if something goes wrong with the operation.
   */
  @Override
  public User confirmUserEmail(final String username, final String email, final String emailConfirmationToken)
    throws NotFoundException, InvalidTokenException, GenericException {
    // TODO
    return null;
  }

  /**
   * Generate a password reset token for the {@link User} with the given username
   * or email.
   * <p>
   * The <code>username</code> and <code>email</code> are used to identify the
   * user. One of them can be <code>null</code>, but not both at the same time.
   * </p>
   *
   * @param username
   *          the username of the {@link User} for whom the password needs to be
   *          reset.
   *
   * @param email
   *          the email of the {@link User} for whom the password needs to be
   *          reset.
   *
   * @return the {@link User} with the password reset token and expiration date.
   *
   * @throws NotFoundException
   *           if username or email doesn't correspond to any registered
   *           {@link User}.
   * @throws IllegalOperationException
   *           if email corresponds to a protected {@link User}.
   * @throws GenericException
   *           if something goes wrong with the operation.
   */
  @Override
  public User requestPasswordReset(final String username, final String email)
    throws NotFoundException, IllegalOperationException, GenericException {
    // TODO
    return null;
  }

  /**
   * Reset {@link User}'s password given a previously generated token.
   *
   * @param username
   *          the {@link User}'s username.
   * @param password
   *          the {@link User}'s password.
   * @param resetPasswordToken
   *          the token to reset {@link User}'s password.
   *
   * @return the modified {@link User}.
   *
   * @throws NotFoundException
   *           if a {@link User} with the same name already exists.
   * @throws InvalidTokenException
   *           if the specified token doesn't exist, has already expired or it
   *           doesn't correspond to the stored token.
   * @throws IllegalOperationException
   *           if the username corresponds to a protected {@link User}.
   * @throws GenericException
   *           if something goes wrong with the operation.
   */
  @Override
  public User resetUserPassword(final String username, SecureString password, final String resetPasswordToken)
    throws NotFoundException, InvalidTokenException, IllegalOperationException, GenericException {
    // TODO
    return null;
  }

  /**
   * Add a new role with the specified name.
   *
   * @param roleName
   *          the role to add.
   * @throws RoleAlreadyExistsException
   *           if a role with the same name already exists.
   * @throws GenericException
   *           if something goes wrong with the creation of the new role.
   */

  @Override
  public void addRole(final String roleName) throws RoleAlreadyExistsException, GenericException {
    // TODO: Review
    LdapName roleDN = LdapNameBuilder.newInstance(ldapRolesOU).add(CN, roleName).build();
    LdapRole ldapRole = new LdapRole();
    ldapRole.setDn(roleDN);
    ldapRole.setCommonName(roleName);
    try {
      ldapRole.addRoleOccupant(LdapUtils.newLdapName(rodaAdministratorsDN));
    } catch (InvalidNameException e) {
      throw new GenericException("Error adding RODA administrator user to role '" + roleName + "'", e);
    }

    if (!dnExists(roleDN)) {
      ldapRole.setNew(true);
    }

    ldapRoleRepository.save(ldapRole);
  }

  private void addRootEntry() {
    // Empty name because of the base search
    if (!dnExists(LdapUtils.emptyLdapName())) {
      BasicAttributes rootAttributes = new BasicAttributes();
      addAttribute(OBJECT_CLASS, rootAttributes, OBJECT_CLASS_TOP, OBJECT_CLASS_DOMAIN, OBJECT_CLASS_ORGANIZATION,
        OBJECT_CLASS_EXTENSIBLE_OBJECT);
      addAttribute(DC, rootAttributes, "roda");
      addAttribute(O, rootAttributes, "roda");
      ldapTemplate.bind(LdapUtils.emptyLdapName(), null, rootAttributes);
    }
  }

  private void addOrganizationUnitIfNotExists(LdapName entry) {
    if (!dnExists(entry)) {
      BasicAttributes rolesAttributes = new BasicAttributes();
      addAttribute(OBJECT_CLASS, rolesAttributes, OBJECT_CLASS_TOP, OBJECT_CLASS_ORGANIZATIONAL_UNIT);
      addAttribute(OU, rolesAttributes, LdapUtils.getStringValue(entry, OU));
      ldapTemplate.bind(entry, null, rolesAttributes);
    }
  }

  private void addAttribute(String attributeName, Attributes attributes, String... attributeValues) {
    BasicAttribute basicAttribute = new BasicAttribute(attributeName);
    for (String attributeValue : attributeValues) {
      basicAttribute.add(attributeValue);
    }
    attributes.put(basicAttribute);
  }

  private boolean dnExists(LdapName rdn) {
    try {
      ldapTemplate.lookup(rdn);
      return true;
    } catch (NamingException e) {
      return false;
    }
  }

  private User setUserRolesAndGroups(final User user) throws LdapException {
    // Add all roles assigned to this user
    final Set<String> memberRoles = getMemberRoles(getUserDN(user.getName()));
    user.setAllRoles(memberRoles);

    // Add direct roles assigned to this user
    for (String role : getMemberDirectRoles(getUserDN(user.getName()))) {
      user.addDirectRole(role);
    }

    // Add all groups to which this user belongs
    user.setGroups(getUserGroups(user.getName()));

    return user;
  }

  private User getUser(final LdapUser ldapUser) {
    User user = getUserFromEntry(ldapUser);
    String memberDN = LdapNameBuilder.newInstance(ldapRootDNName).add(ldapUser.getDn()).build().toString();

    // Add all roles assigned to this user
    final Set<String> memberRoles = getMemberRoles(memberDN);
    user.setAllRoles(memberRoles);

    // Add direct roles assigned to this user
    for (String role : getMemberDirectRoles(memberDN)) {
      user.addDirectRole(role);
    }

    // Add all groups to which this user belongs
    user.setGroups(getUserGroups(user.getName()));

    // Add groups to which this user belongs
    for (String groupDN : getDNsOfGroupsContainingMember(memberDN)) {
      user.addGroup(getFirstNameFromDN(groupDN));
    }

    return user;
  }

  private User getUserFromEntry(final LdapUser ldapUser) {

    final User user = new User(ldapUser.getUid());
    // id and name set in the constructor
    user.setFullName(ldapUser.getCommonName());

    user.setActive("0".equalsIgnoreCase(ldapUser.getShadowInactive()));

    user.setEmail(ldapUser.getEmail());
    user.setGuest(false);

    user.setExtra(ldapUser.getDescription());

    if (Strings.isNotBlank(ldapUser.getInfo())) {
      final String infoStr = ldapUser.getInfo();

      // emailValidationToken;emailValidationTokenValidity;resetPasswordToken;resetPasswordTokenValidity

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

  private LdapUser getLdapUserFromUser(final User user) {
    LdapUser ldapUser = new LdapUser();
    ldapUser.setUid(user.getId());
    LdapName userDN = LdapNameBuilder.newInstance(ldapPeopleOU).add(UID, user.getId()).build();
    ldapUser.setDn(userDN);
    ldapUser.setCommonName(user.getFullName());

    if (this.rodaAdminDN.equals(userDN.toString()) || this.rodaGuestDN.equals(userDN.toString())) {
      ldapUser.setShadowInactive("0");
    } else {
      ldapUser.setShadowInactive(user.isActive() ? "0" : "1");
    }
    if (StringUtils.isNotBlank(user.getFullName())) {
      final String[] names = user.getFullName().split(" ");
      if (names.length > 0) {
        ldapUser.setGivenName(names[0]);
        ldapUser.setSurname(names[names.length - 1]);
      } else {
        ldapUser.setSurname(user.getName());
      }
    }

    if (StringUtils.isNotBlank(user.getEmail())) {
      ldapUser.setEmail(user.getEmail());
    }

    if (StringUtils.isNotBlank(user.getExtra())) {
      ldapUser.setDescription(user.getExtra());
    }

    final String[] infoParts = new String[] {user.getEmailConfirmationToken(),
      user.getEmailConfirmationTokenExpirationDate(), user.getResetPasswordToken(),
      user.getResetPasswordTokenExpirationDate()};
    for (int i = 0; i < infoParts.length; i++) {
      if (StringUtils.isBlank(infoParts[i])) {
        infoParts[i] = "";
      }
    }

    ldapUser.setInfo(String.join(";", infoParts));

    return ldapUser;
  }

  private LdapGroup getLdapGroupFromGroup(Group group) {
    LdapGroup ldapGroup = new LdapGroup();
    LdapName groupDN = LdapNameBuilder.newInstance(ldapGroupsOU).add(CN, group.getName()).build();
    ldapGroup.setId(groupDN);
    ldapGroup.setCommonName(group.getName());
    ldapGroup.setOu(group.getFullName());
    ldapGroup.setShadowInactive(group.isActive() ? "0" : "1");

    // 20160906 hsilva: this is needed because at least one UNIQUE_MEMBER must
    // be added to the entry
    HashSet<Name> uniqueMembers = new HashSet<>();
    uniqueMembers.add(LdapUtils.newLdapName(RODA_DUMMY_USER));

    for (String memberName : group.getUsers()) {
      LdapUser ldapUser = ldapUserRepository.findByUid(memberName);
      uniqueMembers.add(ldapUser.getDn());
    }

    ldapGroup.setUniqueMember(uniqueMembers);

    return ldapGroup;
  }

  private Group getGroupFromEntry(final LdapGroup ldapGroup) {

    final Group group = new Group(ldapGroup.getCommonName());

    group.setActive("0".equalsIgnoreCase(ldapGroup.getShadowInactive()));
    group.setFullName(ldapGroup.getOu());

    if (!ldapGroup.getUniqueMember().isEmpty()) {

      for (Name name : ldapGroup.getUniqueMember()) {
        String memberDN = name.toString();

        if (memberDN.endsWith(ldapPeopleOU.toString())) {
          group.addMemberUser(LdapUtils.getStringValue(name, UID));
        } else if (memberDN.endsWith(ldapGroupsOU.toString())) {
          // 20160907 lfaria: ignoring sub-groups
          // group.addMemberGroup(getFirstNameFromDN(memberDN));
          LOGGER.warn("Ignoring sub-group {} connection with group {}", memberDN, group.getId());
        } else if (!memberDN.equals(RODA_DUMMY_USER)) {
          LOGGER.warn("Member {} outside users and groups", memberDN);
        }
      }
    } else {
      LOGGER.debug("Group {} is empty", group.getName());
    }

    return group;
  }

  /**
   * Modify the {@link Group}'s information.
   *
   * @param modifiedGroup
   *          the {@link Group} to modify.
   * @param force
   *          ignore protected groups configuration.
   *
   * @return the modified {@link Group}.
   *
   * @throws NotFoundException
   *           if the group with being modified doesn't exist.
   * @throws IllegalOperationException
   *           if the user is one of the protected users.
   * @throws GenericException
   *           if some error occurred.
   * @throws GenericException
   *           if some error occurred.
   */
  private Group modifyGroup(final Group modifiedGroup, final boolean force)
    throws NotFoundException, IllegalOperationException, GenericException {

    if (!force && this.ldapProtectedGroups.contains(modifiedGroup.getName())) {
      throw new IllegalOperationException(
        String.format("Group (%s) is protected and cannot be modified.", modifiedGroup.getName()));
    }

    try {
      LdapGroup ldapGroup = ldapGroupRepository.findByCommonName(modifiedGroup.getName());
      LdapGroup modifiedLdapGroup = getLdapGroupFromGroup(modifiedGroup);

      // 20160906 hsilva: cannot change CN as it is used as id (as well as the name)
      modifiedLdapGroup.setId(ldapGroup.getId());
      modifiedLdapGroup.setNew(false);
      ldapGroupRepository.save(modifiedLdapGroup);
    } catch (NamingException e) {
      throw new GenericException("Error modifying group " + modifiedGroup.getName(), e);
    }

    return getGroup(modifiedGroup.getName());
  }

  /**
   * Returns the LDAP DN of the people entry.
   *
   * @return the LDAP DN of the people entry.
   */
  private String getPeopleDN() {
    return ldapPeopleDN;
  }

  /**
   * Returns the LDAP DN of the groups entry.
   *
   * @return the LDAP DN of the groups entry.
   */
  private String getGroupsDN() {
    return ldapGroupsDN;
  }

  /**
   * Returns the LDAP DN of the roles entry.
   *
   * @return the LDAP DN of the roles entry.
   */
  private String getRolesDN() {
    return ldapRolesDN;
  }

  /**
   * Returns the DN of a user given is username.
   *
   * @param username
   *          the username of the user.
   * @return the DN of a user given is username.
   */
  private String getUserDN(final String username) {
    return String.format("uid=%s,%s", username, getPeopleDN());
  }

  /**
   * Returns the DN of a group given is groupName.
   *
   * @param groupName
   *          the name of the group.
   * @return the DN of a group given is groupName.
   */
  private String getGroupDN(final String groupName) {
    return String.format("cn=%s,%s", groupName, getGroupsDN());
  }

  /**
   * Returns the DN of a role given is roleName.
   *
   * @param roleName
   *          the name of the role.
   * @return the DN of a role given is roleName.
   */
  private String getRoleDN(final String roleName) {
    return String.format("cn=%s,%s", roleName, getRolesDN());
  }

  /**
   * Modify the {@link User}'s information.
   *
   * @param modifiedUser
   *          the {@link User} to modify.
   * @param newPassword
   *          the new {@link User}'s password. To maintain the current password,
   *          use <code>null</code>.
   * @param modifyRolesAndGroups
   *          <code>true</code> if User's groups and roles should be updated also.
   * @param force
   *          ignore protected users configuration.
   *
   * @throws NotFoundException
   *           if the {@link User} being modified doesn't exist.
   * @throws EmailAlreadyExistsException
   *           if the specified email is already used by another user.
   * @throws IllegalOperationException
   *           if the user is one of the protected users.
   * @throws GenericException
   *           if some error occurred.
   */
  private void modifyUser(final User modifiedUser, SecureString newPassword, final boolean modifyRolesAndGroups,
    final boolean force)
    throws NotFoundException, IllegalOperationException, EmailAlreadyExistsException, GenericException {

    if (!force && this.ldapProtectedUsers.contains(modifiedUser.getName())) {
      throw new IllegalOperationException("User (" + modifiedUser.getName() + ") is protected and cannot be modified.");
    }

    try {
      final User currentEmailOwner = getUserWithEmail(modifiedUser.getEmail());
      if (currentEmailOwner != null && !modifiedUser.getName().equals(currentEmailOwner.getName())) {
        throw new EmailAlreadyExistsException(
          "The email address " + modifiedUser.getEmail() + " is already used by another user.");
      }

      final LdapUser modifiedLdapUser = getLdapUserFromUser(modifiedUser);
      Name userDN = modifiedLdapUser.getDn();

      Optional<LdapUser> oCurrentLdapUser = ldapUserRepository.findById(userDN);
      if (oCurrentLdapUser.isEmpty()) {
        throw new NotFoundException("Error modifying user " + modifiedUser.getName() + " - user not found");
      }

      LdapUser currentLdapUser = oCurrentLdapUser.get();

      if (newPassword == null) {
        String currentUserPassword = currentLdapUser.getUserPassword();
        if (currentUserPassword != null) {
          modifiedLdapUser.setUserPassword(currentUserPassword);
        }
      }

      ldapUserRepository.deleteById(userDN);
      modifiedLdapUser.setNew(true);
      ldapUserRepository.save(modifiedLdapUser);

      if (newPassword != null) {
        modifyUserPassword(modifiedUser.getName(), newPassword);
      }

      if (modifyRolesAndGroups) {
        setMemberDirectRoles(userDN, modifiedUser.getGroups());
        setMemberDirectRoles(userDN, modifiedUser.getDirectRoles());
      }
    } catch (final NameNotFoundException e) {
      throw new NotFoundException("Error modifying user " + modifiedUser.getName() + " - " + e.getMessage(), e);
    } catch (NamingException | InvalidKeySpecException e) {
      throw new GenericException("Error modifying user " + modifiedUser.getName() + " - " + e.getMessage(), e);
    } catch (final NoSuchAlgorithmException e) {
      throw new GenericException("Error encoding password for user " + modifiedUser.getName(), e);
    }
  }

  /**
   * Modifies user password.
   *
   * @param username
   *          the username.
   * @param password
   *          the password.
   * @throws LdapException
   *           if some error occurs.
   * @throws NoSuchAlgorithmException
   *           the the algorithm doesn't exist.
   * @throws InvalidKeySpecException
   *           Unable to generate a secret for encoding.
   */
  private void modifyUserPassword(final String username, SecureString password)
    throws NoSuchAlgorithmException, InvalidKeySpecException {

    LdapUser ldapUser = ldapUserRepository.findByUid(username);
    ldapUser.setUserPassword(encodePassword(password));

    ldapUserRepository.save(ldapUser);

  }

  private String encodePassword(SecureString password) throws NoSuchAlgorithmException, InvalidKeySpecException {
    final int hashBitSize = 512;
    final int saltByteSize = 16;
    final int pbkdf2Iterations = 10000;
    final String algorithm = "PBKDF2WithHmacSHA512";
    final String prefix = "{PBKDF2-SHA512}";

    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[saltByteSize];
    random.nextBytes(salt);

    PBEKeySpec spec = new PBEKeySpec(password.getChars(), salt, pbkdf2Iterations, hashBitSize);
    SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
    byte[] hash = skf.generateSecret(spec).getEncoded();

    String salt64 = Base64.getEncoder().encodeToString(salt).replace("+", ".").replace("=", "");
    String hash64 = Base64.getEncoder().encodeToString(hash).replace("+", ".").replace("=", "");

    return prefix + pbkdf2Iterations + "$" + salt64 + "$" + hash64;
  }

  private void removeMember(final String memberDN) {
    // For each group the member is in, remove that member from the group
    for (LdapGroup ldapGroup : ldapGroupRepository.findAllByUniqueMember(memberDN)) {
      ldapGroup.getUniqueMember().remove(LdapUtils.newLdapName(memberDN));
      ldapGroupRepository.save(ldapGroup);
    }

    // For each role the member owns, remove that member from the
    // roleOccupant
    for (LdapRole ldapRole : ldapRoleRepository.findAllByRoleOccupants(memberDN)) {
      ldapRole.getRoleOccupants().remove(LdapUtils.newLdapName(memberDN));
      ldapRoleRepository.save(ldapRole);
    }
    ldapUserRepository.deleteById(LdapUtils.newLdapName(memberDN));
  }

  /**
   * Returns the DN of groups that contain the given member.
   *
   * @param memberDN
   *          the DN of the member.
   * @return the DNs of the groups that has memberDN as member.
   * @throws NamingException
   *           if some error occurs.
   */
  private Set<String> getDNsOfGroupsContainingMember(final String memberDN) throws NamingException {
    Set<LdapGroup> uniqueMembers = ldapGroupRepository.findAllByUniqueMember(memberDN);
    final Set<String> groupsDN = new HashSet<>();
    for (LdapGroup uniqueMember : uniqueMembers) {
      groupsDN.add(uniqueMember.getId().toString());
    }
    return groupsDN;
  }

  /**
   * Returns the DN of active groups that contain the given member.
   *
   * @param memberDN
   *          the DN of the member.
   * @return the DNs of the groups that has memberDN as member.
   * @throws NamingException
   *           if some error occurs.
   */
  private Set<String> getDNsOfActiveGroupsContainingMember(final String memberDN) throws NamingException {
    Set<LdapGroup> activeGroups = ldapGroupRepository.findAllByUniqueMemberAndShadowInactiveEquals(memberDN, 0);
    final Set<String> groupsDN = new HashSet<>();
    for (LdapGroup group : activeGroups) {
      groupsDN.add(group.getId().toString());
    }
    return groupsDN;
  }

  private Set<String> getDNsOfDirectRolesForMember(final String memberDN) throws NamingException {
    Set<LdapRole> ldapRoles = ldapRoleRepository.findAllByRoleOccupants(memberDN);
    final Set<String> rolesDN = new HashSet<>();
    for (LdapRole ldapRole : ldapRoles) {
      rolesDN.add(ldapRole.getDn().toString());
    }
    return rolesDN;
  }

  /**
   * Get all roles.
   *
   * @return a {@link Set} with all role names.
   * @throws NamingException
   *           if some error occurs.
   */
  private Set<String> getRoles() throws NamingException {
    final Set<String> roles = new HashSet<>();
    for (LdapRole ldapRole : ldapRoleRepository.findAll()) {
      roles.add(getFirstNameFromDN(ldapRole.getDn().toString()));
    }
    return roles;
  }

  private Set<String> getDNsOfAllRolesForMember(final String memberDN) throws NamingException {
    final Set<String> directMemberRolesDN = getDNsOfDirectRolesForMember(memberDN);
    final Set<String> allMemberRolesDN = new HashSet<>();
    // add the roles that the member directly owns
    allMemberRolesDN.addAll(directMemberRolesDN);
    // for each group that the member belongs to, get it's roles
    // too..
    final Set<String> directMemberGroupsDN = getDNsOfActiveGroupsContainingMember(memberDN);
    for (String memberGroupDN : directMemberGroupsDN) {
      allMemberRolesDN.addAll(
        getDNsOfAllRolesForMember(LdapNameBuilder.newInstance(ldapRootDNName).add(memberGroupDN).build().toString()));
    }
    return allMemberRolesDN;
  }

  private Set<String> getMemberRoles(final String memberDN) throws NamingException {
    final Set<String> allMemberRolesDN = getDNsOfAllRolesForMember(memberDN);
    final Set<String> roles = new HashSet<>();
    for (String roleDN : allMemberRolesDN) {
      roles.add(getFirstNameFromDN(roleDN));
    }
    return roles;
  }

  private Set<String> getMemberDirectRoles(final String memberDN) throws NamingException {
    Set<String> memberDirectRolesDN = getDNsOfDirectRolesForMember(memberDN);
    final Set<String> directRoles = new HashSet<>();
    for (String roleDN : memberDirectRolesDN) {
      directRoles.add(getFirstNameFromDN(roleDN));
    }
    return directRoles;
  }

  private Set<String> getUserGroups(final String username) throws NamingException {
    Set<String> groups = new HashSet<>();
    for (String groupDN : getDNsOfGroupsContainingMember(getUserDN(username))) {
      groups.add(getFirstNameFromDN(groupDN));
    }
    return groups;
  }

  private void setMemberDirectRoles(Name memberDN, Set<String> roles) throws NamingException {

    final Set<String> oldRoles = getMemberDirectRoles(memberDN.toString());
    final Set<String> newRoles;
    if (this.rodaAdministratorsDN.equals(memberDN.toString())) {
      newRoles = getRoles();
    } else {
      newRoles = (roles == null) ? new HashSet<>() : new HashSet<>(roles);
    }

    // removing from oldRoles all the roles in newRoles, oldRoles
    // becomes the Set of roles that the user doesn't want to own
    // anymore.
    final Set<String> tempOldRoles = new HashSet<>(oldRoles);
    tempOldRoles.removeAll(newRoles);

    // remove user from the roles in oldRoles
    for (String role : tempOldRoles) {
      Optional<LdapRole> oLdapRole = ldapRoleRepository
        .findById(LdapNameBuilder.newInstance(ldapRolesOU).add(CN, role).build());
      if (oLdapRole.isPresent()) {
        LdapRole oldLdapRole = oLdapRole.get();
        oldLdapRole.getRoleOccupants().remove(memberDN);
        ldapRoleRepository.save(oldLdapRole);
      }
    }

    // removing from newRoles all the roles in oldRoles, newRoles
    // becomes the Set of the new roles that the user wants to own.
    newRoles.removeAll(oldRoles);

    // add member to the roles in newRoles
    for (String role : newRoles) {
      Optional<LdapRole> oLdapRole = ldapRoleRepository
        .findById(LdapNameBuilder.newInstance(ldapRolesOU).add(CN, role).build());
      if (oLdapRole.isPresent()) {
        LdapRole newLdapRole = oLdapRole.get();
        newLdapRole.getRoleOccupants().add(memberDN);
        ldapRoleRepository.save(newLdapRole);
      }
    }
  }

  /**
   * Sets the groups to which a member belongs to.
   *
   * @param memberDN
   *          the DN of the member to change the groups for.
   * @param groups
   *          a list of groups that this member should belong to.
   * @throws NamingException
   *           if some error occurs.
   */
  private void setMemberGroups(final Name memberDN, final Set<String> groups) throws NamingException {
    final Set<String> newGroups = (groups == null) ? new HashSet<>() : new HashSet<>(groups);
    final Set<String> oldgroupDNs = getDNsOfGroupsContainingMember(memberDN.toString());
    final Set<String> newgroupDNs = new HashSet<>();
    for (String groupName : newGroups) {
      LdapGroup ldapGroup = ldapGroupRepository.findByCommonName(groupName);
      newgroupDNs.add(ldapGroup.getId().toString());
    }

    // removing all the groups in newgroups, oldgroups becomes the Set
    // of groups that the user doesn't want to belong to anymore.
    final Set<String> tempOldgroupDNs = new HashSet<>(oldgroupDNs);
    tempOldgroupDNs.removeAll(newgroupDNs);

    // remove user from the groups in oldgroups
    for (String groupDN : tempOldgroupDNs) {
      Optional<LdapGroup> oLdapGroup = ldapGroupRepository
        .findById(LdapNameBuilder.newInstance(ldapGroupsDN).add(groupDN).build());
      if (oLdapGroup.isPresent()) {
        LdapGroup ldapGroup = oLdapGroup.get();
        ldapGroup.getUniqueMember().remove(memberDN);
        ldapGroupRepository.save(ldapGroup);
      }
    }

    // removing all the groups in oldgroups, newgroups becomes the Set
    // of the new groups that the user wants to bellong to.
    newgroupDNs.removeAll(oldgroupDNs);

    // RODA admin MUST belong to administrators group.
    if (this.rodaAdminDN.equals(memberDN)) {
      newgroupDNs.add(this.rodaAdministratorsDN);
    }

    // add user to the groups in newgroups
    for (String groupDN : newgroupDNs) {
      Optional<LdapGroup> oLdapGroup = ldapGroupRepository
        .findById(LdapNameBuilder.newInstance(ldapGroupsDN).add(groupDN).build());
      if (oLdapGroup.isPresent()) {
        LdapGroup ldapGroup = oLdapGroup.get();
        ldapGroup.getUniqueMember().add(memberDN);
        ldapGroupRepository.save(ldapGroup);
      } else {
        LOGGER.debug("Group {} doesn't exist", groupDN);
      }
    }
  }

  /**
   * Sets the user's password without checking admin and guest users.
   *
   * @param username
   *          the username.
   * @param password
   *          the password.
   *
   * @throws NotFoundException
   *           if specified {@link User} doesn't exist.
   * @throws GenericException
   *           if some error occurs.
   */
  private void setUserPasswordUnchecked(final String username, SecureString password)
    throws NotFoundException, GenericException {
    try {
      modifyUserPassword(username, password);
    } catch (final InvalidKeySpecException e) {
      throw new GenericException("Error setting password for user " + username, e);
    } catch (final NoSuchAlgorithmException e) {
      throw new GenericException("Error encoding password for user " + username, e);
    }
  }

  /**
   * Returns the first name from a DN (Distinguished Name). Ex: for
   * <i>DN=cn=administrators,ou=groups,dc=roda,dc=org</i> returns
   * <i>administrators</i>.
   *
   * @param dn
   *          the Distinguished Name.
   * @return a {@link String} with the first name.
   * @throws LdapInvalidDnException
   *           if the DN is not valid.
   */
  private String getFirstNameFromDN(final String dn) throws InvalidNameException {
    LdapName ldapName = LdapNameBuilder.newInstance().add(dn).build();
    return getFirstNameFromDN(ldapName);
  }

  /**
   * Returns the first name from a DN (Distinguished Name). Ex: for
   * <i>DN=cn=administrators,ou=groups,dc=roda,dc=org</i> returns
   * <i>administrators</i>.
   *
   * @param ldapName
   *          the Distinguished Name.
   * @return a {@link String} with the first name.
   */
  private String getFirstNameFromDN(LdapName ldapName) {
    return ldapName.getRdns().get(ldapName.size() - 1).getValue().toString();
  }

  private String userMessage(final String user, final String message) {
    return "User " + user + message;
  }

  /**
   * Apply LDIF text.
   *
   * @param ldifPath
   *          LDIF file path .
   * @throws NamingException
   *           if some LDAP related error occurs.
   * @throws IOException
   *           if stream could not be closed.
   */
  private void applyLdif(final String ldifPath) throws NamingException, IOException {
    File ldifFile = Paths.get(ldifPath).toFile();
    if (ldifFile.exists()) {
      LdifParser parser = new LdifParser(ldifFile);
      parser.open();
      while (parser.hasMoreRecords()) {
        LdapAttributes record = parser.getRecord();
        LdapName dn = LdapUtils.removeFirst(record.getName(), ldapRootDNName);
        if (!dnExists(dn)) {
          ldapTemplate.bind(dn, null, record);
        }
      }
      parser.close();
    }
  }

  @Override
  public void resetAdminAccess(SecureString password) throws GenericException {

  }

  @Override
  public boolean isInternal(String username) throws GenericException, NotFoundException {
    return false;
  }
}
