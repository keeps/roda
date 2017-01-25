/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.ldap.model.cursor.Cursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapAuthenticationException;
import org.apache.directory.api.ldap.model.exception.LdapEntryAlreadyExistsException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidSearchFilterException;
import org.apache.directory.api.ldap.model.exception.LdapNoSuchObjectException;
import org.apache.directory.api.ldap.model.filter.FilterParser;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.api.ldap.model.message.AliasDerefMode;
import org.apache.directory.api.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.api.ldap.schema.extractor.SchemaLdifExtractor;
import org.apache.directory.api.ldap.schema.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.api.ldap.schema.loader.LdifSchemaLoader;
import org.apache.directory.api.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.api.CacheService;
import org.apache.directory.server.core.api.CoreSession;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.DnFactory;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.api.schema.SchemaPartition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.xdbm.Index;
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
import org.roda.core.util.PasswordHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.util.DateParser;

/**
 * @author Rui Castro
 *
 */
// FIXME this should be moved back to roda-common-servlet-security or any place
// more meaningful
public class LdapUtility {

  /** Class logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(LdapUtility.class);

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

  /** Constant: groupOfUniqueNames. */
  private static final String GROUP_OF_UNIQUE_NAMES = "groupOfUniqueNames";

  /** Constant: domain. */
  private static final String OBJECT_CLASS_DOMAIN = "domain";

  /** Constant: extensibleObject. */
  private static final String OBJECT_CLASS_EXTENSIBLE_OBJECT = "extensibleObject";

  /** Constant: userPassword. */
  private static final String USER_PASSWORD = "userPassword";

  /** Constant: uid. */
  private static final String UID = "uid";

  /** Constant: cn. */
  private static final String CN = "cn";

  /** Constant: ou. */
  private static final String OU = "ou";

  /** Constant: email. */
  private static final String EMAIL = "email";

  private static final String RODA_DUMMY_USER = "cn=roda,ou=system,dc=roda,dc=org";

  /** Start the LDAP server? */
  private boolean ldapStartServer = false;

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
  private Path dataDirectory = null;

  /** The directory service. */
  private DirectoryService service;

  /** The LDAP server. */
  private LdapServer server;

  /**
   * Constructs a new LdapUtility class with the given parameters.
   *
   * @param ldapStartServer
   *          start the LDAP server?
   * @param ldapPort
   *          the port where LDAP server should bind.
   * @param ldapRootDN
   *          the root DN.
   * @param ldapPeopleDN
   *          the DN for the people entry. Users should be located under this
   *          entry.
   * @param ldapGroupsDN
   *          the DN for the groups entry. Groups should be located under this
   *          entry.
   * @param ldapRolesDN
   *          the DN for the roles entry. Roles should be located under this
   *          entry.
   * @param ldapAdminDN
   *          the DN (Distinguished Name) of the LDAP administrator.
   * @param ldapAdminPassword
   *          the password of the LDAP administrator.
   * @param ldapPasswordDigestAlgorithm
   *          the algorithm to use for password encryption (crypt, sha, md5).
   *          The default is MD5.
   * @param ldapProtectedUsers
   *          list of protected users. Users in the protected list cannot be
   *          modified.
   * @param ldapProtectedGroups
   *          list of protected groups. Groups in the protected list cannot be
   *          modified.
   * @param rodaGuestDN
   *          the DN (Distinguished Name) of the RODA guest.
   * @param rodaAdminDN
   *          the DN (Distinguished Name) of the RODA administrator.
   * @param dataDirectory
   *          Directory where ApacheDS data will be stored.
   */
  public LdapUtility(final boolean ldapStartServer, final int ldapPort, final String ldapRootDN,
    final String ldapPeopleDN, final String ldapGroupsDN, final String ldapRolesDN, final String ldapAdminDN,
    final String ldapAdminPassword, final String ldapPasswordDigestAlgorithm, final List<String> ldapProtectedUsers,
    final List<String> ldapProtectedGroups, final String rodaGuestDN, final String rodaAdminDN,
    final Path dataDirectory) {
    this.ldapStartServer = ldapStartServer;
    this.ldapPort = ldapPort;
    this.ldapRootDN = ldapRootDN;
    this.ldapPeopleDN = ldapPeopleDN;
    this.ldapGroupsDN = ldapGroupsDN;
    this.ldapRolesDN = ldapRolesDN;
    this.ldapAdminDN = ldapAdminDN;
    this.ldapAdminPassword = ldapAdminPassword;

    if (ldapPasswordDigestAlgorithm != null) {
      this.ldapDigestAlgorithm = ldapPasswordDigestAlgorithm;
    }
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
    this.dataDirectory = dataDirectory;
  }

  public void setRODAAdministratorsDN(String rodaAdministratorsDN) {
    this.rodaAdministratorsDN = rodaAdministratorsDN;
  }

  /**
   * Stop the directory service and LDAP server if it is running.
   *
   * @throws GenericException
   *           is some error occurred during shutdown.
   */
  public void stopService() throws GenericException {
    if (this.server != null && this.server.isStarted()) {
      this.server.stop();
    }
    try {
      this.service.shutdown();
    } catch (final Exception e) {
      throw new GenericException(e.getMessage(), e);
    }
  }

  /**
   * Initialize the server. It creates the partition and adds the index.
   *
   * @throws Exception
   *           if there were some problems while initializing the system
   */
  public void initDirectoryService() throws Exception {
    initDirectoryService(null);
  }

  /**
   * Initialize the server. It creates the partition, adds the index, and
   * injects the context entries for the created partitions.
   *
   * @param ldifs
   *          LDIF files to apply to Directory Service.
   * @throws Exception
   *           if there were some problems while initializing the system
   */
  public void initDirectoryService(final List<String> ldifs) throws Exception {

    // Initialize the LDAP service
    final JdbmPartition rodaPartition = instantiateDirectoryService();
    final CoreSession session = service.getAdminSession();

    // Inject the context entry for dc=roda,dc=org partition
    if (!session.exists(rodaPartition.getSuffixDn())) {
      final Dn dnRoot = new Dn(this.ldapRootDN);
      final Entry entryRoda = service.newEntry(dnRoot);
      entryRoda.add(OBJECT_CLASS, OBJECT_CLASS_TOP, OBJECT_CLASS_DOMAIN, OBJECT_CLASS_EXTENSIBLE_OBJECT);
      entryRoda.add("dc", getFirstNameFromDN(dnRoot));
      session.add(entryRoda);
    }

    if (ldifs != null) {
      for (String ldif : ldifs) {
        applyLdif(ldif);
      }
    }

    if (this.ldapStartServer) {
      this.server = new LdapServer();
      this.server.setTransports(new TcpTransport(this.ldapPort));
      this.server.setDirectoryService(this.service);
      this.server.start();
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

      final CoreSession session = service.getAdminSession();
      final List<Entry> entries = searchEntries(session, ldapPeopleDN, UID);
      final List<User> users = new ArrayList<>();
      for (Entry entry : entries) {

        final User user = getUserFromEntry(entry);

        // Add all roles assigned to this user
        final Set<String> memberRoles = getMemberRoles(session, getUserDN(user.getName()));
        user.setAllRoles(memberRoles);

        // Add direct roles assigned to this user
        for (String role : getMemberDirectRoles(session, getUserDN(user.getName()))) {
          user.addDirectRole(role);
        }

        // Add groups to which this user belongs
        user.setGroups(getUserGroups(session, user.getName()));

        users.add(user);
      }

      return users;

    } catch (final LdapException e) {
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
      return getUser(service.getAdminSession(), name);
    } catch (final LdapException e) {
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
  public User getUserWithEmail(final String email) throws GenericException {
    try {
      return getUserWithEmail(service.getAdminSession(), email);
    } catch (final LdapException e) {
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
      LOGGER.debug("'{}' is not a valid user name.", user.getName());
      throw new GenericException("'" + user.getName() + "' is not a valid user name.");
    }

    if (getUserWithEmail(user.getEmail()) != null) {
      LOGGER.debug("The email address {} is already used.", user.getEmail());
      throw new EmailAlreadyExistsException("The email address " + user.getEmail() + " is already used.");
    }

    try {
      final CoreSession session = service.getAdminSession();
      session.add(getEntryFromUser(user));
      setMemberDirectRoles(session, getUserDN(user.getName()), user.getDirectRoles());
      setMemberGroups(session, getUserDN(user.getName()), user.getGroups());

      if (!user.isActive()) {
        try {
          setUserPasswordUnchecked(user.getName(), PasswordHandler.generateRandomPassword(RANDOM_PASSWORD_LENGTH));
        } catch (final NotFoundException e) {
          LOGGER.error("Created user doesn't exist! Notify developers!!!", e);
        }
      }

    } catch (final LdapEntryAlreadyExistsException e) {
      LOGGER.debug(e.getMessage(), e);
      throw new UserAlreadyExistsException(userMessage(user.getName(), " already exists."), e);
    } catch (final LdapException e) {
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
  public User modifyUser(final User modifiedUser)
    throws NotFoundException, IllegalOperationException, EmailAlreadyExistsException, GenericException {
    modifyUser(service.getAdminSession(), modifiedUser, null, true, false);
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
  public void setUserPassword(final String username, final String password)
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
  public User modifySelfUser(final User modifiedUser, final String newPassword)
    throws NotFoundException, EmailAlreadyExistsException, IllegalOperationException, GenericException {
    modifyUser(service.getAdminSession(), modifiedUser, newPassword, false, false);
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
      removeMember(service.getAdminSession(), getUserDN(username));
    } catch (final LdapException e) {
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

      final CoreSession session = service.getAdminSession();
      final List<Entry> entries = searchEntries(session, ldapGroupsDN, CN);
      final List<Group> groups = new ArrayList<>();
      for (Entry entry : entries) {
        final Group group = getGroupFromEntry(entry);

        // Add all roles assigned to this group
        final Set<String> memberRoles = getMemberRoles(session, getGroupDN(group.getName()));
        group.setAllRoles(memberRoles);

        // Add direct roles assigned to this group
        for (String role : getMemberDirectRoles(session, getGroupDN(group.getName()))) {
          group.addDirectRole(role);
        }

        groups.add(group);
      }

      return groups;

    } catch (final LdapException e) {
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
      return getGroup(service.getAdminSession(), name);
    } catch (final LdapNoSuchObjectException e) {
      throw new NotFoundException(name);
    } catch (final LdapException e) {
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
      final Dn dn = new Dn(getGroupDN(group.getName()));
      final Entry entry = service.newEntry(dn);
      entry.add(OBJECT_CLASS, GROUP_OF_UNIQUE_NAMES, OBJECT_CLASS_TOP, OBJECT_CLASS_EXTENSIBLE_OBJECT);
      entry.add(CN, group.getName());
      entry.add(OU, group.getFullName());
      entry.add(SHADOW_INACTIVE, group.isActive() ? "0" : "1");
      // 20160906 hsilva: this is needed because at least one UNIQUE_MEMBER must
      // be added to the entry
      entry.add(UNIQUE_MEMBER, RODA_DUMMY_USER);

      // 20160907 lfaria: commenting members as this should be removed if
      // possible
      // TODO 20160907 lfaria: remove commented code
      // Add user members
      // for (String memberName : group.getMemberUserNames()) {
      // entry.add(UNIQUE_MEMBER, getUserDN(memberName));
      // }

      final CoreSession session = service.getAdminSession();
      session.add(entry);

      setMemberDirectRoles(session, getGroupDN(group.getName()), group.getDirectRoles());

    } catch (final LdapEntryAlreadyExistsException e) {
      throw new GroupAlreadyExistsException("Group " + group.getName() + " already exists.", e);
    } catch (final LdapException e) {
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
    throws NotFoundException, IllegalOperationException, GenericException, GenericException {
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
      removeMember(service.getAdminSession(), getGroupDN(groupname));
    } catch (final LdapException e) {
      throw new GenericException("Error removing group " + groupname, e);
    }

  }

  /**
   * Gets {@link User} with <code>username</code> and <code>password</code> from
   * LDAP server using this username and password as login for LDAP to verify
   * that the parameters are valid.
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

      // Try to get a session using username and password.
      // Use this session to retrieve user's direct attributes.
      final CoreSession userSession = service.getSession(new Dn(getUserDN(username)), password.getBytes());
      final Entry entry = userSession.lookup(new Dn(getUserDN(username)));
      final User user = getUserFromEntry(entry);
      // Use the admin session to get the user roles and groups
      return setUserRolesAndGroups(service.getAdminSession(), user);

    } catch (final LdapAuthenticationException e) {
      throw new AuthenticationDeniedException(e.getMessage(), e);
    } catch (final LdapException e) {
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
  public User registerUser(final User user, final String password)
    throws UserAlreadyExistsException, EmailAlreadyExistsException, GenericException {

    // Generate an email verification token with 1 day expiration date.
    final UUID uuidToken = UUID.randomUUID();
    final Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, 1);
    final String isoDateNoMillis = DateParser.getIsoDateNoMillis(calendar.getTime());

    user.setEmailConfirmationToken(uuidToken.toString());
    user.setEmailConfirmationTokenExpirationDate(isoDateNoMillis);

    final User newUser = addUser(user);
    try {

      setUserPassword(newUser.getName(), password);

    } catch (final IllegalOperationException | NotFoundException e) {
      throw new GenericException("Error setting user password - " + e.getMessage(), e);
    }

    return newUser;
  }

  /**
   * Confirms the {@link User} email using the token supplied at register time
   * and activate the {@link User}.
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

        final String currentIsoDate = DateParser.getIsoDateNoMillis(Calendar.getInstance().getTime());

        if (currentIsoDate.compareToIgnoreCase(user.getEmailConfirmationTokenExpirationDate()) > 0) {

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
   * Generate a password reset token for the {@link User} with the given
   * username or email.
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

      // Generate a password reset token with 1 day expiration date.
      final UUID uuidToken = UUID.randomUUID();
      final Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_MONTH, 1);
      final String isoDateNoMillis = DateParser.getIsoDateNoMillis(calendar.getTime());

      user.setResetPasswordToken(uuidToken.toString());
      user.setResetPasswordTokenExpirationDate(isoDateNoMillis);

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
  public User resetUserPassword(final String username, final String password, final String resetPasswordToken)
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
        final String currentIsoDate = DateParser.getIsoDateNoMillis(Calendar.getInstance().getTime());
        if (currentIsoDate.compareToIgnoreCase(user.getResetPasswordTokenExpirationDate()) > 0) {
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
    try {
      final CoreSession session = service.getAdminSession();
      final String roleDN = getRoleDN(roleName);
      final Entry entryRole = service.newEntry(new Dn(roleDN));
      entryRole.add(OBJECT_CLASS, "organizationalRole", OBJECT_CLASS_TOP);
      entryRole.add(CN, roleName);
      entryRole.add(ROLE_OCCUPANT, rodaAdministratorsDN);
      try {
        session.add(entryRole);
      } catch (final LdapEntryAlreadyExistsException e) {
        // Assign role to RODA administrators group
        final Set<String> roles = getMemberDirectRoles(session, this.rodaAdministratorsDN);
        if (!roles.contains(roleName)) {
          addMemberToRoleOrGroup(service.getAdminSession(), roleDN, this.rodaAdministratorsDN, ROLE_OCCUPANT);
        }
        throw new RoleAlreadyExistsException("Role " + roleName + " already exists.", e);
      }
    } catch (final LdapException e) {
      throw new GenericException("Error adding role '" + roleName + "'", e);
    }
  }

  /**
   * Instantiate Directory Service.
   *
   * @return RODA {@link JdbmPartition}
   * @throws Exception
   *           if some error occurs.
   */
  private JdbmPartition instantiateDirectoryService() throws Exception {
    this.service = new DefaultDirectoryService();
    this.service.setInstanceId(INSTANCE_NAME);
    this.service.setInstanceLayout(new InstanceLayout(this.dataDirectory.toFile()));

    final CacheService cacheService = new CacheService();
    cacheService.initialize(this.service.getInstanceLayout());

    this.service.setCacheService(cacheService);

    // first load the schema
    initSchemaPartition();

    final File systemPartitionPath = new File(this.service.getInstanceLayout().getPartitionsDirectory(), "system");

    // If the system partition directory exists, delete it, to avoid "ou=system
    // already exists!" error at startup.
    // It will be recreated again.
    // TODO: this is a workaround for this issue
    // https://issues.apache.org/jira/browse/DIRSERVER-1954
    if (systemPartitionPath.exists() && !FileUtils.deleteQuietly(systemPartitionPath)) {
      LOGGER.warn("Could not delete ApacheDS system partition directory: {}", systemPartitionPath);
    }

    // then the system partition
    // this is a MANDATORY partition
    // DO NOT add this via addPartition() method, trunk code complains about
    // duplicate partition while initializing
    final JdbmPartition systemPartition = new JdbmPartition(this.service.getSchemaManager(),
      this.service.getDnFactory());
    systemPartition.setId("system");
    systemPartition.setPartitionPath(systemPartitionPath.toURI());
    systemPartition.setSuffixDn(new Dn(ServerDNConstants.SYSTEM_DN));
    systemPartition.setSchemaManager(service.getSchemaManager());

    // mandatory to call this method to set the system partition
    // Note: this system partition might be removed from trunk
    this.service.setSystemPartition(systemPartition);

    // Disable the ChangeLog system
    // this.service.getChangeLog().setEnabled(false);
    // this.service.setDenormalizeOpAttrsEnabled(true);

    // Now we can create as many partitions as we need
    final JdbmPartition rodaPartition = addPartition(INSTANCE_NAME, this.ldapRootDN, this.service.getDnFactory());

    // Index some attributes on the apache partition
    addIndex(rodaPartition, OBJECT_CLASS, OU, UID);

    // And start the service
    this.service.startup();

    final CoreSession session = this.service.getAdminSession();

    // change nis attribute in order to make things like
    // "shadowinactive" work
    ModifyRequestImpl modifyRequestImpl = new ModifyRequestImpl();
    modifyRequestImpl.setName(new Dn("cn=nis,ou=schema"));
    modifyRequestImpl.replace("m-disabled", "FALSE");
    session.modify(modifyRequestImpl);

    // change admin password
    modifyRequestImpl = new ModifyRequestImpl();
    modifyRequestImpl.setName(new Dn(this.ldapAdminDN));
    modifyRequestImpl.replace(USER_PASSWORD, this.ldapAdminPassword);
    session.modify(modifyRequestImpl);

    return rodaPartition;
  }

  private User setUserRolesAndGroups(final CoreSession session, final User user) throws LdapException {
    // Add all roles assigned to this user
    final Set<String> memberRoles = getMemberRoles(session, getUserDN(user.getName()));
    user.setAllRoles(memberRoles);

    // Add direct roles assigned to this user
    for (String role : getMemberDirectRoles(session, getUserDN(user.getName()))) {
      user.addDirectRole(role);
    }

    // Add all groups to which this user belongs
    user.setGroups(getUserGroups(session, user.getName()));

    return user;
  }

  private User getUser(final CoreSession session, final String username) throws LdapException {

    final Entry entry = session.lookup(new Dn(getUserDN(username)));
    final User user = getUserFromEntry(entry);

    // Add all roles assigned to this user
    final Set<String> memberRoles = getMemberRoles(session, getUserDN(username));
    user.setAllRoles(memberRoles);

    // Add direct roles assigned to this user
    for (String role : getMemberDirectRoles(session, getUserDN(username))) {
      user.addDirectRole(role);
    }

    // Add all groups to which this user belongs
    user.setGroups(getUserGroups(session, username));

    // Add groups to which this user belongs
    for (String groupDN : getDNsOfGroupsContainingMember(session, getUserDN(username))) {
      user.addGroup(getFirstNameFromDN(groupDN));
    }

    return user;
  }

  private User getUserFromEntry(final Entry entry) throws LdapException {

    final User user = new User(getEntryAttributeAsString(entry, UID));
    // id and name set in the constructor
    user.setFullName(getEntryAttributeAsString(entry, CN));

    user.setActive("0".equalsIgnoreCase(getEntryAttributeAsString(entry, SHADOW_INACTIVE)));

    user.setEmail(getEntryAttributeAsString(entry, EMAIL));
    user.setGuest(false);

    user.setExtra(getEntryAttributeAsString(entry, "description"));

    if (entry.get("info") != null) {
      final String infoStr = entry.get("info").getString();

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

  private Entry getEntryFromUser(final User user) throws LdapException {
    final String userDN = getUserDN(user.getName());
    final Entry entry = service.newEntry(new Dn(userDN));
    entry.add(OBJECT_CLASS, "inetOrgPerson", "organizationalPerson", "person", OBJECT_CLASS_TOP,
      OBJECT_CLASS_EXTENSIBLE_OBJECT);
    entry.add(UID, user.getName());
    entry.add(CN, user.getFullName());
    if (this.rodaAdminDN.equals(userDN) || this.rodaGuestDN.equals(userDN)) {
      entry.add(SHADOW_INACTIVE, "0");
    } else {
      entry.add(SHADOW_INACTIVE, user.isActive() ? "0" : "1");
    }
    if (StringUtils.isNotBlank(user.getFullName())) {
      final String[] names = user.getFullName().split(" ");
      if (names.length > 0) {
        entry.add("givenName", names[0]);
        entry.add("sn", names[names.length - 1]);
      } else {
        entry.add("sn", user.getName());
      }
    }
    if (StringUtils.isNotBlank(user.getEmail())) {
      entry.add(EMAIL, user.getEmail());
    }
    if (StringUtils.isNotBlank(user.getExtra())) {
      entry.add("description", user.getExtra());
    }

    final String[] infoParts = new String[] {user.getEmailConfirmationToken(),
      user.getEmailConfirmationTokenExpirationDate(), user.getResetPasswordToken(),
      user.getResetPasswordTokenExpirationDate()};
    for (int i = 0; i < infoParts.length; i++) {
      if (StringUtils.isBlank(infoParts[i])) {
        infoParts[i] = "";
      }
    }
    entry.add("info", String.join(";", infoParts));

    return entry;
  }

  private Group getGroup(final CoreSession session, final String name) throws LdapException {

    final Entry entry = session.lookup(new Dn(getGroupDN(name)));

    final Group group = getGroupFromEntry(entry);

    // Add all roles assigned to this group
    final Set<String> memberRoles = getMemberRoles(session, getGroupDN(name));
    group.setAllRoles(memberRoles);

    // Add direct roles assigned to this group
    for (String role : getMemberDirectRoles(session, getGroupDN(name))) {
      group.addDirectRole(role);
    }

    return group;
  }

  private Group getGroupFromEntry(final Entry entry) throws LdapException {

    final Group group = new Group(getEntryAttributeAsString(entry, CN));

    group.setActive("0".equalsIgnoreCase(getEntryAttributeAsString(entry, SHADOW_INACTIVE)));
    group.setFullName(getEntryAttributeAsString(entry, OU));

    final Attribute attributeUniqueMember = entry.get(UNIQUE_MEMBER);

    if (attributeUniqueMember != null) {

      for (Value<?> value : attributeUniqueMember) {
        final String memberDN = value.toString();

        if (memberDN.endsWith(getPeopleDN())) {
          group.addMemberUser(getFirstNameFromDN(memberDN));
        } else if (memberDN.endsWith(getGroupsDN())) {
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
    throws NotFoundException, IllegalOperationException, GenericException, GenericException {

    if (!force && this.ldapProtectedGroups.contains(modifiedGroup.getName())) {
      throw new IllegalOperationException(
        String.format("Group (%s) is protected and cannot be modified.", modifiedGroup.getName()));
    }

    try {
      final CoreSession session = service.getAdminSession();
      final String groupDN = getGroupDN(modifiedGroup.getName());
      final Entry entry = session.lookup(new Dn(groupDN));
      // 20160906 hsilva: cannot change CN as it is used as id (as well as the
      // name)
      entry.removeAttributes(OU);
      entry.add(OU, modifiedGroup.getFullName());
      entry.removeAttributes(SHADOW_INACTIVE);
      entry.add(SHADOW_INACTIVE, modifiedGroup.isActive() ? "0" : "1");
      // Remove all members
      entry.removeAttributes(UNIQUE_MEMBER);
      // 20160906 hsilva: this is needed because at least one UNIQUE_MEMBER must
      // be added to the entry
      entry.add(UNIQUE_MEMBER, RODA_DUMMY_USER);
      // Add user members
      for (String memberName : modifiedGroup.getUsers()) {
        entry.add(UNIQUE_MEMBER, getUserDN(memberName));
      }
      session.delete(entry.getDn());
      session.add(entry);

      setMemberDirectRoles(session, groupDN, modifiedGroup.getDirectRoles());

    } catch (final LdapNoSuchObjectException e) {
      throw new NotFoundException("Group " + modifiedGroup.getName() + " doesn't exist.", e);
    } catch (final LdapException e) {
      throw new GenericException("Error modifying group " + modifiedGroup.getName(), e);
    }

    return getGroup(modifiedGroup.getName());
  }

  private List<Entry> searchEntries(final CoreSession session, final String ctxDN, final String keyAttribute)
    throws LdapException {
    final Cursor<Entry> cursor = search(session, ctxDN, String.format("(%s=*)", keyAttribute));
    final List<Entry> entries = new ArrayList<>();
    for (Entry entry : cursor) {
      entries.add(entry);
    }
    return entries;
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
   * @param session
   *          the session.
   * @param modifiedUser
   *          the {@link User} to modify.
   * @param newPassword
   *          the new {@link User}'s password. To maintain the current password,
   *          use <code>null</code>.
   * @param modifyRolesAndGroups
   *          <code>true</code> if User's groups and roles should be updated
   *          also.
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
  private void modifyUser(final CoreSession session, final User modifiedUser, final String newPassword,
    final boolean modifyRolesAndGroups, final boolean force)
    throws NotFoundException, IllegalOperationException, EmailAlreadyExistsException, GenericException {

    if (!force && this.ldapProtectedUsers.contains(modifiedUser.getName())) {
      throw new IllegalOperationException("User (" + modifiedUser.getName() + ") is protected and cannot be modified.");
    }

    try {

      final User currentEmailOwner = getUserWithEmail(session, modifiedUser.getEmail());
      if (currentEmailOwner != null && !modifiedUser.getName().equals(currentEmailOwner.getName())) {
        throw new EmailAlreadyExistsException(
          "The email address " + modifiedUser.getEmail() + " is already used by another user.");
      }

      final Entry modifiedUserEntry = getEntryFromUser(modifiedUser);
      final String userDN = getUserDN(modifiedUser.getName());
      if (newPassword == null) {
        // Copy password from old entry
        final Entry oldEntry = session.lookup(new Dn(userDN));
        final Object oldPassword = oldEntry.get(USER_PASSWORD);
        if (oldPassword != null) {
          modifiedUserEntry.add(oldEntry.get(USER_PASSWORD));
        }
      }
      session.delete(modifiedUserEntry.getDn());
      session.add(modifiedUserEntry);

      if (newPassword != null) {
        modifyUserPassword(session, modifiedUser.getName(), newPassword);
      }

      if (modifyRolesAndGroups) {
        setMemberGroups(session, userDN, modifiedUser.getGroups());
        setMemberDirectRoles(session, userDN, modifiedUser.getDirectRoles());
      }

    } catch (final LdapException e) {
      throw new GenericException("Error modifying user " + modifiedUser.getName() + " - " + e.getMessage(), e);
    } catch (final NoSuchAlgorithmException e) {
      throw new GenericException("Error encoding password for user " + modifiedUser.getName(), e);
    }

  }

  /**
   * Modifies user password.
   *
   * @param session
   *          the session.
   * @param username
   *          the username.
   * @param password
   *          the password.
   * @throws LdapException
   *           if some error occurs.
   * @throws NoSuchAlgorithmException
   *           the the algorithm doesn't exist.
   */
  private void modifyUserPassword(final CoreSession session, final String username, final String password)
    throws LdapException, NoSuchAlgorithmException {
    final PasswordHandler passwordHandler = PasswordHandler.getInstance();
    final String passwordDigest = passwordHandler.generateDigest(password, null, ldapDigestAlgorithm);
    session.modify(new Dn(getUserDN(username)),
      new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, USER_PASSWORD, passwordDigest));
  }

  private void addMemberToRoleOrGroup(final CoreSession session, final String dn, final String memberDN,
    final String attributeName) throws LdapException {
    final Entry entry = session.lookup(new Dn(dn), attributeName);
    Attribute attribute = entry.get(attributeName);
    if (attribute == null) {
      entry.add(attributeName, memberDN);
      attribute = entry.get(attributeName);
    } else {
      attribute.add(memberDN);
    }
    final ModifyRequestImpl modifyRequestImpl = new ModifyRequestImpl();
    modifyRequestImpl.setName(entry.getDn());
    modifyRequestImpl.replace(attribute);
    session.modify(modifyRequestImpl);
  }

  private void removeMemberFromRoleOrGroup(final CoreSession session, final String dn, final String memberDN,
    final String attributeName) throws LdapException {
    final Entry entry = session.lookup(new Dn(dn), attributeName);
    final Attribute attribute = entry.get(attributeName);
    if (attribute != null) {
      attribute.remove(memberDN);
      final ModifyRequestImpl modifyRequestImpl = new ModifyRequestImpl();
      modifyRequestImpl.setName(entry.getDn());
      modifyRequestImpl.replace(attribute);
      session.modify(modifyRequestImpl);
    }
  }

  private void removeMember(final CoreSession session, final String memberDN) throws LdapException {
    // For each group the member is in, remove that member from the group
    final Set<String> directMemberGroupsDN = getDNsOfGroupsContainingMember(session, memberDN);
    for (String groupDN : directMemberGroupsDN) {
      removeMemberFromRoleOrGroup(session, groupDN, memberDN, UNIQUE_MEMBER);
    }
    // For each role the member owns, remove that member from the
    // roleOccupant
    final Set<String> directMemberRolesDN = getDNsOfDirectRolesForMember(session, memberDN);
    for (String roleDN : directMemberRolesDN) {
      removeMemberFromRoleOrGroup(session, roleDN, memberDN, ROLE_OCCUPANT);
    }
    session.delete(new Dn(memberDN));
  }

  /**
   * Returns the DN of groups that contain the given member.
   *
   * @param session
   *          the session.
   * @param memberDN
   *          the DN of the member.
   * @return the DNs of the groups that has memberDN as member.
   * @throws LdapException
   *           if some error occurs.
   */
  private Set<String> getDNsOfGroupsContainingMember(final CoreSession session, final String memberDN)
    throws LdapException {
    final Cursor<Entry> cursor = search(session, getGroupsDN(),
      String.format("(&(%s=*)(%s=%s))", CN, UNIQUE_MEMBER, memberDN));
    final Set<String> groupsDN = new HashSet<>();
    for (Entry entry : cursor) {
      groupsDN.add(entry.getDn().getName());
    }
    return groupsDN;
  }

  /**
   * Returns the DN of active groups that contain the given member.
   *
   * @param session
   *          the session.
   * @param memberDN
   *          the DN of the member.
   * @return the DNs of the groups that has memberDN as member.
   * @throws LdapException
   *           if some error occurs.
   */
  private Set<String> getDNsOfActiveGroupsContainingMember(final CoreSession session, final String memberDN)
    throws LdapException {
    final Cursor<Entry> cursor = search(session, getGroupsDN(),
      String.format("(&(%s=%s)(%s=%s))", UNIQUE_MEMBER, memberDN, SHADOW_INACTIVE, 0));
    final Set<String> groupsDN = new HashSet<>();
    for (Entry entry : cursor) {
      groupsDN.add(entry.getDn().getName());
    }
    return groupsDN;
  }

  private Set<String> getDNsOfDirectRolesForMember(final CoreSession session, final String memberDN)
    throws LdapException {
    final Set<String> rolesDN = new HashSet<>();
    final Cursor<Entry> cursor = search(session, getRolesDN(),
      String.format("(&(cn=*)(%s=%s))", ROLE_OCCUPANT, memberDN));
    for (Entry entry : cursor) {
      rolesDN.add(entry.getDn().getName());
    }
    return rolesDN;
  }

  /**
   * Get all roles.
   * 
   * @param session
   *          the session.
   * @return a {@link Set} with all role names.
   * @throws LdapException
   *           if some error occurs.
   */
  private Set<String> getRoles(final CoreSession session) throws LdapException {
    final Set<String> roles = new HashSet<>();
    for (Entry entry : searchEntries(session, getRolesDN(), CN)) {
      roles.add(getFirstNameFromDN(entry.getDn()));
    }
    return roles;
  }

  private Set<String> getDNsOfAllRolesForMember(final CoreSession session, final String memberDN) throws LdapException {
    final Set<String> directMemberRolesDN = getDNsOfDirectRolesForMember(session, memberDN);
    final Set<String> allMemberRolesDN = new HashSet<>();
    // add the roles that the member directly owns
    allMemberRolesDN.addAll(directMemberRolesDN);
    // for each group that the member belongs to, get it's roles
    // too..
    final Set<String> directMemberGroupsDN = getDNsOfActiveGroupsContainingMember(session, memberDN);
    for (String memberGroupDN : directMemberGroupsDN) {
      allMemberRolesDN.addAll(getDNsOfAllRolesForMember(session, memberGroupDN));
    }
    return allMemberRolesDN;
  }

  private Set<String> getMemberRoles(final CoreSession session, final String memberDN) throws LdapException {
    final Set<String> allMemberRolesDN = getDNsOfAllRolesForMember(session, memberDN);
    final Set<String> roles = new HashSet<>();
    for (String roleDN : allMemberRolesDN) {
      roles.add(getFirstNameFromDN(roleDN));
    }
    return roles;
  }

  private Set<String> getMemberDirectRoles(final CoreSession session, final String memberDN) throws LdapException {
    final Set<String> memberDirectRolesDN = getDNsOfDirectRolesForMember(session, memberDN);
    final Set<String> directRoles = new HashSet<>();
    for (String roleDN : memberDirectRolesDN) {
      directRoles.add(getFirstNameFromDN(roleDN));
    }
    return directRoles;
  }

  private Set<String> getUserGroups(final CoreSession session, final String username) throws LdapException {
    Set<String> groups = new HashSet<>();
    for (String groupDN : getDNsOfGroupsContainingMember(session, getUserDN(username))) {
      groups.add(getFirstNameFromDN(groupDN));
    }
    return groups;
  }

  private User getUserWithEmail(final CoreSession session, final String email) throws LdapException {
    final Cursor<Entry> cursor = search(session, getPeopleDN(), String.format("(email=%s)", email));
    final Iterator<Entry> it = cursor.iterator();
    User user = null;
    while (it.hasNext() && user == null) {
      user = getUserFromEntry(it.next());
    }
    return user;
  }

  /**
   * Sets the roles that a member owns.
   *
   * @param session
   *          the session
   * @param memberDN
   *          the DN of the member to change the roles for.
   * @param roles
   *          a list of roles that this member should own.
   * @throws LdapException
   *           if some error occurs.
   */
  private void setMemberDirectRoles(final CoreSession session, final String memberDN, final Set<String> roles)
    throws LdapException {

    final Set<String> oldRoles = getMemberDirectRoles(session, memberDN);
    final Set<String> newRoles;
    if (this.rodaAdministratorsDN.equals(memberDN)) {
      newRoles = getRoles(session);
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
      removeMemberFromRoleOrGroup(session, getRoleDN(role), memberDN, ROLE_OCCUPANT);
    }

    // removing from newRoles all the roles in oldRoles, newRoles
    // becomes the Set of the new roles that the user wants to own.
    newRoles.removeAll(oldRoles);

    // add member to the roles in newRoles
    for (String role : newRoles) {
      addMemberToRoleOrGroup(session, getRoleDN(role), memberDN, ROLE_OCCUPANT);
    }
  }

  /**
   * Sets the groups to which a member belongs to.
   *
   * @param session
   *          the session.
   * @param memberDN
   *          the DN of the member to change the groups for.
   * @param groups
   *          a list of groups that this member should belong to.
   * @throws LdapException
   *           if some error occurs.
   */
  private void setMemberGroups(final CoreSession session, final String memberDN, final Set<String> groups)
    throws LdapException {

    final Set<String> newGroups = (groups == null) ? new HashSet<>() : new HashSet<>(groups);
    final Set<String> oldgroupDNs = getDNsOfGroupsContainingMember(session, memberDN);
    final Set<String> newgroupDNs = new HashSet<>();
    for (String groupName : newGroups) {
      newgroupDNs.add(getGroupDN(groupName));
    }

    // removing all the groups in newgroups, oldgroups becomes the Set
    // of groups that the user doesn't want to belong to anymore.
    final Set<String> tempOldgroupDNs = new HashSet<>(oldgroupDNs);
    tempOldgroupDNs.removeAll(newgroupDNs);

    // remove user from the groups in oldgroups
    for (String groupDN : tempOldgroupDNs) {
      removeMemberFromRoleOrGroup(session, groupDN, memberDN, UNIQUE_MEMBER);
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
      try {
        addMemberToRoleOrGroup(session, groupDN, memberDN, UNIQUE_MEMBER);
      } catch (final LdapNoSuchObjectException e) {
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
  private void setUserPasswordUnchecked(final String username, final String password)
    throws NotFoundException, GenericException {
    try {
      modifyUserPassword(service.getAdminSession(), username, password);
    } catch (final LdapException e) {
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
  private String getFirstNameFromDN(final String dn) throws LdapInvalidDnException {
    return getFirstNameFromDN(new Dn(dn));
  }

  /**
   * Returns the first name from a DN (Distinguished Name). Ex: for
   * <i>DN=cn=administrators,ou=groups,dc=roda,dc=org</i> returns
   * <i>administrators</i>.
   *
   * @param dn
   *          the Distinguished Name.
   * @return a {@link String} with the first name.
   */
  private String getFirstNameFromDN(final Dn dn) {
    return dn.getRdn().getValue();
  }

  private String userMessage(final String user, final String message) {
    return "User " + user + message;
  }

  /**
   * Initialize the schema manager and add the schema partition to directory
   * service.
   *
   * @throws Exception
   *           if the schema LDIF files are not found on the classpath
   */
  private void initSchemaPartition() throws Exception {
    final InstanceLayout instanceLayout = this.service.getInstanceLayout();

    final File schemaPartitionDirectory = new File(instanceLayout.getPartitionsDirectory(), "schema");

    // Extract the schema on disk (a brand new one) and load the registries
    if (!schemaPartitionDirectory.exists()) {
      final SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor(instanceLayout.getPartitionsDirectory());
      extractor.extractOrCopy();
    }

    final SchemaLoader loader = new LdifSchemaLoader(schemaPartitionDirectory);
    final SchemaManager schemaManager = new DefaultSchemaManager(loader);

    // We have to load the schema now, otherwise we won't be able
    // to initialize the Partitions, as we won't be able to parse
    // and normalize their suffix Dn
    schemaManager.loadAllEnabled();

    final List<Throwable> errors = schemaManager.getErrors();

    if (!errors.isEmpty()) {
      throw new GenericException("Error while loading ApacheDS schemas");
    }

    this.service.setSchemaManager(schemaManager);

    // Init the LdifPartition with schema
    final LdifPartition schemaLdifPartition = new LdifPartition(schemaManager, this.service.getDnFactory());
    schemaLdifPartition.setPartitionPath(schemaPartitionDirectory.toURI());

    // The schema partition
    final SchemaPartition schemaPartition = new SchemaPartition(schemaManager);
    schemaPartition.setWrappedPartition(schemaLdifPartition);
    this.service.setSchemaPartition(schemaPartition);
  }

  /**
   * Add a new partition to the server.
   *
   * @param partitionId
   *          The partition Id
   * @param partitionDn
   *          The partition DN
   * @param dnFactory
   *          the DN factory
   * @return The newly added partition
   * @throws Exception
   *           If the partition can't be added
   */
  private JdbmPartition addPartition(final String partitionId, final String partitionDn, final DnFactory dnFactory)
    throws Exception {
    // Create a new partition with the given partition id
    final JdbmPartition partition = new JdbmPartition(service.getSchemaManager(), dnFactory);
    partition.setId(partitionId);
    partition.setPartitionPath(new File(service.getInstanceLayout().getPartitionsDirectory(), partitionId).toURI());
    partition.setSuffixDn(new Dn(partitionDn));
    service.addPartition(partition);
    return partition;
  }

  /**
   * Apply LDIF text.
   *
   * @param ldif
   *          LDIF text.
   * @throws LdapException
   *           if some LDAP related error occurs.
   * @throws IOException
   *           if stream could not be closed.
   */
  private void applyLdif(final String ldif) throws LdapException, IOException {
    try (LdifReader entries = new LdifReader(new StringReader(ldif))) {
      for (LdifEntry ldifEntry : entries) {
        final DefaultEntry newEntry = new DefaultEntry(this.service.getSchemaManager(), ldifEntry.getEntry());
        LOGGER.debug("LDIF entry: {}", newEntry);
        this.service.getAdminSession().add(newEntry);
      }
    }
  }

  /**
   * Add a new set of index on the given attributes.
   *
   * @param partition
   *          The partition on which we want to add index
   * @param attrs
   *          The list of attributes to index
   */
  private void addIndex(final JdbmPartition partition, final String... attrs) {
    // Index some attributes on the apache partition
    final Set<Index<?, String>> indexedAttributes = new HashSet<>();

    for (String attribute : attrs) {
      indexedAttributes.add(new JdbmIndex<String>(attribute, false));
    }

    partition.setIndexedAttributes(indexedAttributes);
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

  private Cursor<Entry> search(final CoreSession session, final String dn, final String filter) throws LdapException {
    try {
      return session.search(new Dn(dn), SearchScope.SUBTREE, FilterParser.parse(service.getSchemaManager(), filter),
        AliasDerefMode.NEVER_DEREF_ALIASES);
    } catch (final ParseException e) {
      throw new LdapInvalidSearchFilterException(e.getMessage());
    }
  }

  private String getEntryAttributeAsString(final Entry entry, final String attributeName)
    throws LdapInvalidAttributeValueException {
    final Attribute attribute = entry.get(attributeName);
    String value = null;
    if (attribute != null) {
      value = attribute.getString();
    }
    return value;
  }

  public void resetAdminAccess(final String password) throws GenericException, GenericException {
    try {

      final CoreSession session = this.service.getAdminSession();

      final String adminName = getFirstNameFromDN(this.rodaAdminDN);
      final String administratorsName = getFirstNameFromDN(this.rodaAdministratorsDN);

      User admin;
      try {
        admin = getUser(session, adminName);
      } catch (final LdapNoSuchObjectException e) {
        admin = new User(adminName);
        admin = addUser(admin);
      }
      admin.setActive(true);
      modifyUser(session, admin, password, false, true);

      Group administrators;
      try {
        administrators = getGroup(session, administratorsName);
      } catch (final LdapNoSuchObjectException e) {
        administrators = addGroup(new Group(administratorsName));
        administrators.setActive(true);
      }
      administrators.setDirectRoles(getRoles(session));
      administrators.addMemberUser(adminName);
      modifyGroup(administrators, true);

    } catch (final UserAlreadyExistsException | EmailAlreadyExistsException | NotFoundException
      | IllegalOperationException | GroupAlreadyExistsException | LdapException e) {
      throw new GenericException(e.getMessage(), e);
    }
  }

}
