package org.roda.core.model.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.joda.time.DateTime;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.RodaUtils;
import org.roda.core.data.common.RodaConstants;
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
import org.roda.core.util.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.InvalidNameException;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.DirContextOperations;
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
public class LdapUtility {

  /** Class logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(LdapUtility.class);

  /** Size of random passwords */
  private static final int RANDOM_PASSWORD_LENGTH = 12;

  /** Object class constant. */
  private static final String OBJECT_CLASS = "objectClass";

  /** Constant: top. */
  private static final String OBJECT_CLASS_TOP = "top";

  /** Constant: organization. */
  private static final String OBJECT_CLASS_ORGANIZATION = "organization";

  /** Constant: top. */
  private static final String OBJECT_CLASS_ORGANIZATIONAL_UNIT = "organizationalUnit";

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

  private static final String RODA_DUMMY_USER = "cn=roda,ou=system,dc=roda,dc=org";

  /**
   * LDAP administrator Distinguished Name (DN).
   */
  private String ldapAdminDN = null;

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

  private final LdapTemplate ldapTemplate;
  private final LdapUserRepository ldapUserRepository;
  private final LdapGroupRepository ldapGroupRepository;
  private final LdapRoleRepository ldapRoleRepository;

  public LdapUtility(LdapTemplate ldapTemplate, LdapUserRepository ldapUserRepository,
    LdapGroupRepository ldapGroupRepository, LdapRoleRepository ldapRoleRepository) {
    this.ldapTemplate = ldapTemplate;
    this.ldapUserRepository = ldapUserRepository;
    this.ldapGroupRepository = ldapGroupRepository;
    this.ldapRoleRepository = ldapRoleRepository;
  }

  public LdapTemplate getLdapTemplate() {
    return ldapTemplate;
  }

  public void initialize(RodaConstants.NodeType nodeType) throws Exception {
    Configuration configuration = RodaCoreFactory.getRodaConfiguration();
    this.ldapRootDN = configuration.getString("core.ldap.baseDN", "dc=roda,dc=org");

    this.ldapPeopleDN = configuration.getString("core.ldap.peopleDN", "ou=users,dc=roda,dc=org");
    this.ldapGroupsDN = configuration.getString("core.ldap.groupsDN", "ou=groups,dc=roda,dc=org");
    this.ldapRolesDN = configuration.getString("core.ldap.rolesDN", "ou=roles,dc=roda,dc=org");
    this.ldapAdminDN = configuration.getString("core.ldap.adminDN", "uid=admin,ou=system");

    this.ldapProtectedUsers.clear();
    if (ldapProtectedUsers != null) {
      this.ldapProtectedUsers.addAll(RodaUtils.copyList(configuration.getList("core.ldap.protectedUsers")));
      LOGGER.debug("Protected users: {}", this.ldapProtectedUsers);
    }

    this.ldapProtectedGroups.clear();
    if (ldapProtectedGroups != null) {
      this.ldapProtectedGroups.addAll(RodaUtils.copyList(configuration.getList("core.ldap.protectedGroups")));
      LOGGER.debug("Protected groups: {}", this.ldapProtectedGroups);
    }

    this.rodaGuestDN = configuration.getString("core.ldap.rodaGuestDN", "uid=guest,ou=users,dc=roda,dc=org");
    this.rodaAdminDN = configuration.getString("core.ldap.rodaAdminDN", "uid=admin,ou=users,dc=roda,dc=org");
    this.rodaAdministratorsDN = configuration.getString("core.ldap.rodaAdministratorsDN",
      "cn=administrators,ou=groups,dc=roda,dc=org");

    if (nodeType != RodaConstants.NodeType.TEST) {
      final String ldapUrl = configuration.getString("core.ldap.url", RodaConstants.CORE_LDAP_DEFAULT_URL);
      final int ldapPort = configuration.getInt("core.ldap.port", RodaConstants.CORE_LDAP_DEFAULT_PORT);
      final String ldapAdminPassword = configuration.getString("core.ldap.adminPassword", "roda");

      LdapContextSource contextSource = new LdapContextSource();
      contextSource.setUrl(ldapUrl + ":" + ldapPort);
      contextSource.setBase(ldapRootDN);
      contextSource.setUserDn(ldapAdminDN);
      contextSource.setPassword(ldapAdminPassword);
      contextSource.afterPropertiesSet();

      ldapTemplate.setContextSource(contextSource);
    }

    bootstrap();
    createRoles(configuration);
  }

  /**
   * Initialize the server. It creates the partition, and injects the context
   * entries for the created partitions.
   *
   * @throws Exception
   *           if there were some problems while initializing the system
   */
  private void bootstrap() throws Exception {
    if (!dnExists(LdapUtils.emptyLdapName())) {
      // Add root DN
      addRootEntry();

      // Add roles DN
      addOrganizationUnitIfNotExists(ldapRolesDN);

      // Add people DN
      addOrganizationUnitIfNotExists(ldapPeopleDN);

      // Add groups DN
      addOrganizationUnitIfNotExists(ldapGroupsDN);

      applyLdif();
    }
  }

  /**
   * For each role in roda-roles.properties create the role in LDAP if it don't
   * exist already.
   *
   * @param configuration
   *          roda configuration
   * @throws GenericException
   *           if something unexpected happens creating roles.
   */
  private void createRoles(final Configuration configuration) throws GenericException {
    final Iterator<String> keys = configuration.getKeys("core.roles");
    final Set<String> roles = new HashSet<>();

    while (keys.hasNext()) {
      roles.addAll(Arrays.asList(configuration.getStringArray(keys.next())));
    }

    for (final String role : roles) {
      try {
        if (StringUtils.isNotBlank(role)) {
          addRole(role);
          LOGGER.debug("Created LDAP role {}", role);
        }
      } catch (final RoleAlreadyExistsException e) {
        LOGGER.trace("Role {} already exists.", role, e);
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
  public User addUser(final User user)
    throws UserAlreadyExistsException, EmailAlreadyExistsException, GenericException {
    if (!user.isNameValid()) {
      LOGGER.warn("'{}' is not a valid user name.", user.getName());
      throw new GenericException("'" + user.getName() + "' is not a valid user name.");
    }

    if (!getUser(user.getId()).equals(new User())) {
      LOGGER.warn("The username {} is already used.", user.getName());
      throw new UserAlreadyExistsException("The username " + user.getName() + " is already used.");
    }

    if (getUserWithEmail(user.getEmail()) != null) {
      LOGGER.warn("The email address {} is already used.", user.getEmail());
      throw new EmailAlreadyExistsException("The email address " + user.getEmail() + " is already used.");
    }

    try {
      LdapUser ldapUser = getLdapUserFromUser(user);
      ldapUser.setNew(true);
      ldapUserRepository.save(ldapUser);
      setMemberDirectRoles(getUserDN(user.getName()), user.getDirectRoles());
      setMemberGroups(getUserDN(user.getName()), user.getGroups());

      if (user.isActive()) {
        try (SecureString randomPassword = new SecureString(
          RandomStringUtils.random(RANDOM_PASSWORD_LENGTH).toCharArray())) {
          setUserPasswordUnchecked(user.getId(), randomPassword);
        } catch (final NotFoundException e) {
          LOGGER.error("Created user doesn't exist! Notify developers!!!", e);
        }
      }
    } catch (final NameAlreadyBoundException e) {
      LOGGER.debug(e.getMessage(), e);
      throw new UserAlreadyExistsException(userMessage(user.getId(), " already exists."), e);
    } catch (final NamingException e) {
      LOGGER.debug(e.getMessage(), e);
      throw new GenericException("Error adding user " + user.getId(), e);
    }

    final User newUser = getUser(user.getId());
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
  public void removeUser(final String username) throws IllegalOperationException, GenericException {
    final String userDN = getUserDN(username);
    if (this.rodaAdminDN.equals(userDN) || this.rodaGuestDN.equals(userDN)
      || this.ldapProtectedUsers.contains(username)) {
      throw new IllegalOperationException(userMessage(username, " is protected and cannot be removed."));
    }
    try {
      removeMember(userDN);
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
  public Group getGroup(final String name) throws GenericException, NotFoundException {
    try {
      LdapGroup ldapGroup = ldapGroupRepository.findByCommonName(name);
      if (ldapGroup != null) {
        final Group group = getGroupFromEntry(ldapGroup);

        // Add all roles assigned to this group
        final Set<String> memberRoles = getMemberRoles(getGroupDN(group.getName()));
        group.setAllRoles(memberRoles);

        // Add direct roles assigned to this group
        for (String role : getMemberDirectRoles(getGroupDN(group.getName()))) {
          group.addDirectRole(role);
        }
        return group;
      } else {
        throw new NotFoundException();
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
  public Group addGroup(final Group group) throws GroupAlreadyExistsException, GenericException {
    if (!group.isNameValid()) {
      throw new GenericException("'" + group.getName() + "' is not a valid group name.");
    }

    try {
      LdapGroup ldapGroup = getLdapGroupFromGroup(group);
      ldapGroup.setNew(true);
      ldapGroupRepository.save(ldapGroup);

      setMemberDirectRoles(getGroupDN(group.getName()), group.getDirectRoles());
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
  public User getAuthenticatedUser(final String username, final String password)
    throws AuthenticationDeniedException, GenericException {

    if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
      throw new AuthenticationDeniedException("Username and password cannot be blank!");
    }

    try {
      ldapTemplate.authenticate(LdapQueryBuilder.query().where(UID).is(username), password);
      return getUser(username);
    } catch (AuthenticationException | EmptyResultDataAccessException e) {
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
  public User registerUser(final User user, SecureString password)
    throws UserAlreadyExistsException, EmailAlreadyExistsException, GenericException {
    // Generate an email verification token with 1 day expiration date.
    final String isoDateNoMillis = DateTime.now().plusDays(1).toDateTimeISO().toInstant().toString();

    user.setEmailConfirmationToken(IdUtils.createUUID());
    user.setEmailConfirmationTokenExpirationDate(isoDateNoMillis);
    user.setResetPasswordToken(IdUtils.createUUID());
    user.setResetPasswordTokenExpirationDate(isoDateNoMillis);

    final User newUser = addUser(user);
    try {

      setUserPassword(newUser.getId(), password);

    } catch (final IllegalOperationException | NotFoundException e) {
      throw new GenericException("Error setting user password - " + e.getMessage(), e);
    }

    return newUser;
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
  public User confirmUserEmail(final String username, final String email, final String emailConfirmationToken)
    throws NotFoundException, InvalidTokenException, GenericException {

    final User user = getUserByNameOrEmail(username, email);

    if (user == null) {
      final String message;
      if (username != null) {
        message = userMessage(username, " doesn't exist");
      } else {
        message = "Email " + email + " is not registered by any user";
      }
      throw new NotFoundException(message);
    } else {
      if (user.getEmailConfirmationToken() == null) {
        throw new InvalidTokenException("There's no active email confirmation token.");
      } else if (!user.getEmailConfirmationToken().equals(emailConfirmationToken)
        || user.getEmailConfirmationTokenExpirationDate() == null) {
        // Token argument is not equal to stored token, or
        // No expiration date
        throw new InvalidTokenException("Email confirmation token is invalid.");

      } else {
        boolean after = DateTime.now().isAfter(DateTime.parse(user.getEmailConfirmationTokenExpirationDate()));
        if (after) {
          throw new InvalidTokenException(
            "Email confirmation token expired in " + user.getEmailConfirmationTokenExpirationDate());
        }
      }

      user.setActive(true);
      user.setEmailConfirmationToken(null);
      user.setEmailConfirmationTokenExpirationDate(null);

      try {
        return modifyUser(user);
      } catch (final IllegalOperationException | EmailAlreadyExistsException e) {
        throw new GenericException("Error confirming user email - " + e.getMessage(), e);
      }
    }
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
  public User requestPasswordReset(final String username, final String email)
    throws NotFoundException, IllegalOperationException, GenericException {
    User user = null;
    try {
      user = getUserByNameOrEmail(username, email);
    } catch (GenericException e) {
      LOGGER.debug("Error getting user with given credentials");
    }

    if (user == null) {
      LOGGER.debug("Could not find any user with given credentials");
      return null;
    } else {
      // Generate a password reset token with 1 day expiration date.
      user.setResetPasswordToken(IdUtils.createUUID());
      user.setResetPasswordTokenExpirationDate(DateTime.now().plusDays(1).toDateTimeISO().toInstant().toString());
      try {
        return modifyUser(user);
      } catch (final EmailAlreadyExistsException e) {
        throw new GenericException("Error setting password reset token - " + e.getMessage(), e);
      }
    }
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
  public User resetUserPassword(final String username, SecureString password, final String resetPasswordToken)
    throws NotFoundException, InvalidTokenException, IllegalOperationException, GenericException {
    final User user = getUser(username);

    if (user == null) {

      throw new NotFoundException(userMessage(username, " doesn't exist"));

    } else {

      if (user.getResetPasswordToken() == null) {

        throw new InvalidTokenException("There's no active password reset token.");

      } else if (!user.getResetPasswordToken().equals(resetPasswordToken)
        || user.getResetPasswordTokenExpirationDate() == null) {

        // Token argument is not equal to stored token, or
        // expiration date is null.
        throw new InvalidTokenException("Password reset token is invalid.");

      } else {
        if (DateTime.now().isAfter(DateTime.parse(user.getResetPasswordTokenExpirationDate()))) {
          throw new InvalidTokenException(
            "Password reset token expired in " + user.getResetPasswordTokenExpirationDate());
        }
      }

      try {
        setUserPassword(username, password);
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpirationDate(null);
        return modifyUser(user);
      } catch (final IllegalOperationException | EmailAlreadyExistsException e) {
        throw new GenericException("Error reseting user password - " + e.getMessage(), e);
      }
    }
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
  public void addRole(final String roleName) throws RoleAlreadyExistsException, GenericException {
    LdapName roleDN = LdapNameBuilder.newInstance(removeBaseDN(ldapRolesDN)).add(CN, roleName).build();
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
    LdapName rootDN = LdapUtils.newLdapName(ldapRootDN);
    if (!dnExists(LdapUtils.emptyLdapName())) {
      BasicAttributes rootAttributes = new BasicAttributes();
      addAttribute(OBJECT_CLASS, rootAttributes, OBJECT_CLASS_TOP, OBJECT_CLASS_DOMAIN, OBJECT_CLASS_ORGANIZATION,
        OBJECT_CLASS_EXTENSIBLE_OBJECT);
      addAttribute(DC, rootAttributes, getFirstNameFromDN(rootDN));
      addAttribute(O, rootAttributes, getFirstNameFromDN(rootDN));
      // Empty name because of the base search
      ldapTemplate.bind(LdapUtils.emptyLdapName(), null, rootAttributes);
    }
  }

  private void addOrganizationUnitIfNotExists(String entry) {
    Name name = removeBaseDN(entry);
    if (!dnExists(name)) {
      BasicAttributes rolesAttributes = new BasicAttributes();
      addAttribute(OBJECT_CLASS, rolesAttributes, OBJECT_CLASS_TOP, OBJECT_CLASS_ORGANIZATIONAL_UNIT);
      addAttribute(OU, rolesAttributes, LdapUtils.getStringValue(name, OU));
      ldapTemplate.bind(name, null, rolesAttributes);
    }
  }

  private void addAttribute(String attributeName, Attributes attributes, String... attributeValues) {
    BasicAttribute basicAttribute = new BasicAttribute(attributeName);
    for (String attributeValue : attributeValues) {
      basicAttribute.add(attributeValue);
    }
    attributes.put(basicAttribute);
  }

  private User setUserRolesAndGroups(final User user) throws NamingException {
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
    String memberDN = LdapNameBuilder.newInstance(ldapRootDN).add(ldapUser.getDn()).build().toString();

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

    user.setExtraLDAP(ldapUser.getDescription());

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
    LdapName userDN = LdapNameBuilder.newInstance(removeBaseDN(ldapPeopleDN)).add(UID, user.getId()).build();
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

    if (user.getExtraLDAP() != null) {
      ldapUser.setDescription(user.getExtraLDAP());
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
    LdapName groupDN = LdapNameBuilder.newInstance(removeBaseDN(ldapGroupsDN)).add(CN, group.getName()).build();
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
      uniqueMembers.add(getFullDN(ldapUser.getDn()));
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

        if (memberDN.endsWith(ldapPeopleDN)) {
          group.addMemberUser(LdapUtils.getStringValue(name, UID));
        } else if (memberDN.endsWith(ldapGroupsDN)) {
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
          // convert from ASCII
          String encodedPass = Stream.of(currentUserPassword.split(","))
            .map(ch -> (char) Integer.valueOf(ch).intValue())
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
          modifiedLdapUser.setUserPassword(encodedPass);
        }
      }
      ldapUserRepository.save(modifiedLdapUser);

      if (newPassword != null) {
        modifyUserPassword(modifiedUser.getId(), newPassword);
      }

      if (modifyRolesAndGroups) {
        setMemberGroups(getUserDN(modifiedUser.getId()), modifiedUser.getGroups());
        setMemberDirectRoles(getUserDN(modifiedUser.getId()), modifiedUser.getDirectRoles());
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
   * @throws NamingException
   *           if some error occurs.
   * @throws NoSuchAlgorithmException
   *           the the algorithm doesn't exist.
   * @throws InvalidKeySpecException
   *           Unable to generate a secret for encoding.
   */
  private void modifyUserPassword(final String username, SecureString password)
    throws NamingException, NoSuchAlgorithmException, InvalidKeySpecException {

    if (password != null) {
      LdapUser ldapUser = ldapUserRepository.findByUid(username);
      Pbkdf2PasswordEncoderImpl encoder = new Pbkdf2PasswordEncoderImpl();
      ldapUser.setUserPassword(encoder.encode(password));

      ldapUserRepository.save(ldapUser);
    }
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
    ldapUserRepository.deleteById(removeBaseDN(memberDN));
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
        getDNsOfAllRolesForMember(LdapNameBuilder.newInstance(ldapRootDN).add(memberGroupDN).build().toString()));
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

  private void setMemberDirectRoles(final String memberDN, Set<String> roles) throws NamingException {

    final Set<String> oldRoles = getMemberDirectRoles(memberDN);
    final Set<String> newRoles;
    if (this.rodaAdministratorsDN.equals(memberDN)) {
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
        .findById(LdapNameBuilder.newInstance(removeBaseDN(ldapRolesDN)).add(CN, role).build());
      if (oLdapRole.isPresent()) {
        LdapRole oldLdapRole = oLdapRole.get();
        oldLdapRole.getRoleOccupants().remove(LdapUtils.newLdapName(memberDN));
        ldapRoleRepository.save(oldLdapRole);
      }
    }

    // removing from newRoles all the roles in oldRoles, newRoles
    // becomes the Set of the new roles that the user wants to own.
    newRoles.removeAll(oldRoles);

    // add member to the roles in newRoles
    for (String role : newRoles) {
      Optional<LdapRole> oLdapRole = ldapRoleRepository
        .findById(LdapNameBuilder.newInstance(removeBaseDN(ldapRolesDN)).add(CN, role).build());
      if (oLdapRole.isPresent()) {
        LdapRole newLdapRole = oLdapRole.get();
        newLdapRole.getRoleOccupants().add(LdapUtils.newLdapName(memberDN));
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
  private void setMemberGroups(final String memberDN, final Set<String> groups) throws NamingException {
    final Set<String> newGroups = (groups == null) ? new HashSet<>() : new HashSet<>(groups);
    final Set<String> oldgroupDNs = getDNsOfGroupsContainingMember(memberDN);
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
      // TODO: review LdapNameBuilder.newInstance() should be empty?
      Optional<LdapGroup> oLdapGroup = ldapGroupRepository.findById(LdapNameBuilder.newInstance().add(groupDN).build());
      if (oLdapGroup.isPresent()) {
        LdapGroup ldapGroup = oLdapGroup.get();
        ldapGroup.getUniqueMember().remove(LdapUtils.newLdapName(memberDN));
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
      // TODO: same above
      Optional<LdapGroup> oLdapGroup = ldapGroupRepository.findById(LdapNameBuilder.newInstance().add(groupDN).build());
      if (oLdapGroup.isPresent()) {
        LdapGroup ldapGroup = oLdapGroup.get();
        ldapGroup.getUniqueMember().add(LdapUtils.newLdapName(memberDN));
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
   * @throws InvalidNameException
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
   * @throws NamingException
   *           if some LDAP related error occurs.
   * @throws IOException
   *           if stream could not be closed.
   */
  private void applyLdif() throws NamingException, IOException {
    final List<String> ldifFileNames = Arrays.asList("users.ldif", "groups.ldif", "roles.ldif");
    for (String ldifFileName : ldifFileNames) {
      InputStream inputStream = RodaCoreFactory
        .getConfigurationFileAsStream(RodaConstants.CORE_LDAP_FOLDER + "/" + ldifFileName);
      if (inputStream != null) {
        Resource resource = new InputStreamResource(inputStream);
        if (resource.exists()) {
          LdifParser parser = new LdifParser(resource);
          parser.open();
          while (parser.hasMoreRecords()) {
            LdapAttributes record = parser.getRecord();
            if (!dnExists(record.getName())) {
              ldapTemplate.bind(removeBaseDN(record.getName()), null, record);
            }
          }
          parser.close();
        }
      }
    }
  }

  private User getUserByNameOrEmail(final String username, final String email) throws GenericException {
    final User user;
    if (username != null) {
      user = getUser(username);
    } else if (email != null) {
      user = getUserWithEmail(email);
    } else {
      throw new IllegalArgumentException("username and email can not both be null");
    }
    return user;
  }

  public void resetAdminAccess(SecureString password) throws GenericException {
    try {
      final String adminName = getFirstNameFromDN(this.rodaAdminDN);
      final String administratorsName = getFirstNameFromDN(this.rodaAdministratorsDN);

      User admin;

      try {
        admin = getUser(adminName);
      } catch (final NameNotFoundException e) {
        admin = new User(adminName);
        admin = addUser(admin);
      }
      admin.setActive(true);
      modifyUser(admin, password, false, true);

      Group administrators;
      try {
        administrators = getGroup(administratorsName);
      } catch (final NameNotFoundException e) {
        administrators = addGroup(new Group(administratorsName));
        administrators.setActive(true);
      }
      administrators.setDirectRoles(getRoles());
      administrators.addMemberUser(adminName);
      modifyGroup(administrators, true);
    } catch (final UserAlreadyExistsException | EmailAlreadyExistsException | NotFoundException
      | IllegalOperationException | GroupAlreadyExistsException | NamingException e) {
      throw new GenericException(e.getMessage(), e);
    }
  }

  public boolean isInternal(String username) throws GenericException, NotFoundException {
    try {
      Object entry = ldapTemplate.lookup(removeBaseDN(getUserDN(username)));
      if (entry instanceof DirContextOperations dirContextOperations) {
        return dirContextOperations.getObjectAttribute(USER_PASSWORD) != null;
      }
    } catch (NameNotFoundException e) {
      throw new NotFoundException(username);
    } catch (NamingException e) {
      throw new GenericException(e);
    }
    return false;
  }

  private boolean dnExists(Name dn) {
    try {
      ldapTemplate.lookup(dn);
      return true;
    } catch (NamingException e) {
      return false;
    }
  }

  private Name getFullDN(Name dn) {
    return LdapNameBuilder.newInstance(ldapRootDN).add(dn).build();
  }

  private Name removeBaseDN(String dn) {
    return removeBaseDN(LdapUtils.newLdapName(dn));
  }

  private Name removeBaseDN(Name dn) {
    return LdapUtils.removeFirst(dn, LdapUtils.newLdapName(ldapRootDN));
  }
}
