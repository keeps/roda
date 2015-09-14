package org.roda.common;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import pt.gov.dgarq.roda.core.common.AuthenticationDeniedException;
import pt.gov.dgarq.roda.core.common.EmailAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.GroupAlreadyExistsException;
import pt.gov.dgarq.roda.core.common.IllegalOperationException;
import pt.gov.dgarq.roda.core.common.InvalidTokenException;
import pt.gov.dgarq.roda.core.common.NoSuchGroupException;
import pt.gov.dgarq.roda.core.common.NoSuchUserException;
import pt.gov.dgarq.roda.core.common.UserAlreadyExistsException;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.core.data.v2.RodaUser;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.util.PasswordHandler;

/**
 * @author Rui Castro
 * 
 */
// FIXME this should be moved back to roda-common-servlet-security or any place
// more meaningful
public class LdapUtility {

  private static final Logger LOGGER = Logger.getLogger(LdapUtility.class);

  private static final String AUTHENTICATION_SIMPLE = "simple";
  private static final String SHADOW_INACTIVE = "shadowInactive";
  private static final String UNIQUE_MEMBER = "uniqueMember";

  /**
   * LDAP server host
   */
  private String ldapHost = "localhost";

  /**
   * LDAP server port
   */
  private int ldapPort = 10389;

  /**
   * LDAP administrator Distinguished Name (DN)
   */
  private String ldapAdminDN = null;

  /**
   * LDAP administrator password
   */
  private String ldapAdminPassword = null;

  /**
   * LDAP DN of the root
   */
  private String ldapRootDN = "";

  /**
   * LDAP OU of the people entry (default: null)
   */
  private String ldapPeopleDN = null;

  /**
   * LDAP OU of the groups entry (default: null)
   */
  private String ldapGroupsDN = null;

  /**
   * LDAP OU of the roles entry (default: null)
   */
  private String ldapRolesDN = null;

  /**
   * Password Digest Algorithm
   */
  private String ldapDigestAlgorithm = "MD5";

  /**
   * List of protected users. Users in the protected list cannot be modified.
   * 
   * The list of protected users can be set in roda-core.properties file.
   */
  private List<String> ldapProtectedUsers = new ArrayList<String>();

  /**
   * List of protected groups. Groups in the protected list cannot be modified.
   * 
   * The list of protected groups can be set in roda-core.properties file.
   */
  private List<String> ldapProtectedGroups = new ArrayList<String>();

  /**
   * Constructs a new LdapUtility class with the given parameters.
   * 
   * @param ldapHost
   *          LDAP server host
   * @param ldapPort
   *          LDAP server port
   * @param ldapPopleDN
   *          the DN for the people entry. Users should be located under this
   *          entry.
   * @param ldapGroupsDN
   *          the DN for the groups entry. Groups should be located under this
   *          entry.
   * @param ldapRolesDN
   *          the DN for the roles entry. Roles should be located under this
   *          entry.
   * @param ldapProtectedUsers
   *          list of protected users. Users in the protected list cannot be
   *          modified.
   * @param ldapProtectedGroups
   *          list of protected groups. Groups in the protected list cannot be
   *          modified.
   */
  public LdapUtility(String ldapHost, int ldapPort, String ldapPopleDN, String ldapGroupsDN, String ldapRolesDN,
    List<String> ldapProtectedUsers, List<String> ldapProtectedGroups) {
    this(ldapHost, ldapPort, ldapPopleDN, ldapGroupsDN, ldapRolesDN, null, null, null, ldapProtectedUsers,
      ldapProtectedGroups);
  }

  /**
   * Constructs a new LdapUtility class with the given parameters.
   * 
   * @param ldapHost
   *          LDAP server host
   * @param ldapPort
   *          LDAP server port
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
   */
  public LdapUtility(String ldapHost, int ldapPort, String ldapPeopleDN, String ldapGroupsDN, String ldapRolesDN,
    String ldapAdminDN, String ldapAdminPassword, String ldapPasswordDigestAlgorithm, List<String> ldapProtectedUsers,
    List<String> ldapProtectedGroups) {
    this.ldapHost = ldapHost;
    this.ldapPort = ldapPort;
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
      LOGGER.debug("protected users: " + this.ldapProtectedUsers);
    }
    this.ldapProtectedGroups.clear();
    if (ldapProtectedGroups != null) {
      this.ldapProtectedGroups.addAll(ldapProtectedGroups);
      LOGGER.debug("protected groups: " + this.ldapProtectedGroups);
    }
  }

  /*
   * Users
   */

  /**
   * Returns the number of registered users.
   * 
   * @param contentAdapterFilter
   * 
   * @return an <code>int</code> with the number of users in the repository.
   * @throws LdapUtilityException
   */
  // public int getUserCount(Filter contentAdapterFilter) throws
  // LdapUtilityException {
  //
  // JndiContentAdapterEngine<UserAdapter, User> jndiAdapterEngine = new
  // JndiContentAdapterEngine<UserAdapter, User>(
  // new UserAdapter(), new ContentAdapter(contentAdapterFilter, null, null));
  //
  // try {
  //
  // DirContext ctxRoot = getLDAPDirContext(ldapRootDN);
  //
  // List<Attributes> attributesList = searchAttributes(ctxRoot, ldapPeopleDN,
  // "uid", jndiAdapterEngine);
  //
  // ctxRoot.close();
  //
  // return attributesList.size();
  //
  // } catch (NamingException e) {
  // logger.debug("Error counting users - " + e.getMessage(), e);
  // throw new LdapUtilityException("Error counting users - " +
  // e.getMessage(), e);
  // }
  // }

  /**
   * Return the users that match the given {@link ContentAdapter}.
   * 
   * @param contentAdapter
   *          the {@link ContentAdapter}.
   * 
   * @return an array of {@link User}'s.
   * 
   * @throws LdapUtilityException
   */
  public List<User> getUsers(Filter filter) throws LdapUtilityException {
    return getUsers(filter, null);
  }

  public List<User> getUsers(Filter filter, Sorter sorter) throws LdapUtilityException {

    try {

      DirContext ctxRoot = getLDAPDirContext(ldapRootDN);

      List<Attributes> attributesList = searchAttributes(ctxRoot, ldapPeopleDN, "uid", filter);

      List<User> users = new ArrayList<User>();
      for (Attributes attributes : attributesList) {

        User user = getUserFromAttributes(attributes);

        // Add all roles assigned to this user
        Set<String> memberRoles = getMemberRoles(ctxRoot, getUserDN(user.getName()));
        user.setAllRoles(memberRoles);

        // Add direct roles assigned to this user
        for (String role : getMemberDirectRoles(ctxRoot, getUserDN(user.getName()))) {
          user.addDirectRole(role);
        }

        // Add all groups to which this user belongs
        Set<String> memberGroups = getMemberGroups(ctxRoot, getUserDN(user.getName()));
        user.setAllGroups(memberGroups);

        // Add groups to which this user belongs
        for (String groupDN : getDNsOfGroupsContainingMember(ctxRoot, getUserDN(user.getName()))) {
          user.addGroup(getGroupCNFromDN(groupDN));
        }

        users.add(user);
      }

      ctxRoot.close();

      return users;

    } catch (NamingException e) {
      LOGGER.debug("Error getting users - " + e.getMessage(), e);
      throw new LdapUtilityException("Error getting users - " + e.getMessage(), e);
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
   * @throws LdapUtilityException
   *           if the user information could not be retrieved from the LDAP
   *           server.
   */
  public User getUser(String name) throws LdapUtilityException {

    User user = null;

    try {
      // Create initial context
      DirContext ctxRoot = getLDAPDirContext(ldapRootDN);

      user = getUser(ctxRoot, name);

      // Close the context when we're done
      ctxRoot.close();

    } catch (NameNotFoundException e) {
      LOGGER.debug(userMessage(name, " doesn't exist"), e);
      user = null;

    } catch (NamingException e) {
      LOGGER.debug("Error getting user " + name, e);
      throw new LdapUtilityException("Error getting user " + name, e);
    }

    return user;
  }

  public RodaUser getRodaUser(String name) throws LdapUtilityException {

    RodaUser user = null;

    try {
      // Create initial context
      DirContext ctxRoot = getLDAPDirContext(ldapRootDN);

      user = getRodaUser(ctxRoot, name);

      // Close the context when we're done
      ctxRoot.close();

    } catch (NameNotFoundException e) {
      LOGGER.debug(userMessage(name, " doesn't exist"), e);
      user = null;

    } catch (NamingException e) {
      LOGGER.debug("Error getting user " + name, e);
      throw new LdapUtilityException("Error getting user " + name, e);
    }

    return user;
  }

  /**
   * Returns the {@link User} with name <code>email</code> or <code>null</code>
   * if it doesn't exist.
   * 
   * @param email
   *          the email of the desired {@link User}.
   * 
   * @return the {@link User} with email <code>email</code> or <code>null</code>
   *         if it doesn't exist.
   * 
   * @throws LdapUtilityException
   *           if the user information could not be retrieved from the LDAP
   *           server.
   */
  public User getUserWithEmail(String email) throws LdapUtilityException {

    User user = null;

    try {
      // Create initial context
      DirContext ctxRoot = getLDAPDirContext(ldapRootDN);

      user = getUserWithEmail(ctxRoot, email);

      // Close the context when we're done
      ctxRoot.close();

    } catch (NamingException e) {
      LOGGER.debug("Error getting user with email " + email, e);
      throw new LdapUtilityException("Error getting user with email " + email, e);
    }

    return user;
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
   * @throws LdapUtilityException
   *           if something goes wrong with the creation of the new user.
   */
  public User addUser(User user) throws UserAlreadyExistsException, EmailAlreadyExistsException, LdapUtilityException {

    if (!user.isNameValid()) {
      LOGGER.debug("'" + user.getName() + "' is not a valid user name.");
      throw new LdapUtilityException("'" + user.getName() + "' is not a valid user name.");
    }

    if (getUserWithEmail(user.getEmail()) != null) {
      LOGGER.debug("The email address " + user.getEmail() + " is already used.");
      throw new EmailAlreadyExistsException("The email address " + user.getEmail() + " is already used.");
    }

    try {
      // Create initial context
      DirContext ctxRoot = getLDAPAdminDirContext(ldapRootDN);

      // Get the attributes for the user
      Attributes attributes = getAttributesFromUser(user, false);

      ctxRoot.bind(getUserDN(user.getName()), null, attributes);

      setMemberDirectRoles(ctxRoot, getUserDN(user.getName()), user.getDirectRoles());
      setMemberGroups(ctxRoot, getUserDN(user.getName()), user.getDirectGroups());

      // Close the context when we're done
      ctxRoot.close();

      if (!user.isActive()) {
        try {
          setUserPasswordUnchecked(user.getName(), PasswordHandler.generateRandomPassword(12));
        } catch (NoSuchUserException e) {
          LOGGER.error("Created user doesn't exist! Notify developers!!!", e);
        }
      }

    } catch (NameAlreadyBoundException e) {

      LOGGER.debug(userMessage(user.getName(), " already exists."), e);
      throw new UserAlreadyExistsException(userMessage(user.getName(), " already exists."), e);

    } catch (NamingException e) {

      LOGGER.debug("Error adding user " + user.getName(), e);
      throw new LdapUtilityException("Error adding user " + user.getName(), e);

    }

    User newUser = getUser(user.getName());
    if (newUser == null) {
      throw new LdapUtilityException("The user was not created!");
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
   * @throws NoSuchUserException
   *           if the {@link User} being modified doesn't exist.
   * @throws EmailAlreadyExistsException
   *           if the specified email is already used by another user.
   * @throws IllegalOperationException
   *           if the user is one of the protected users.
   * @throws LdapUtilityException
   */
  public User modifyUser(User modifiedUser)
    throws NoSuchUserException, IllegalOperationException, EmailAlreadyExistsException, LdapUtilityException {

    LOGGER.trace("modifyUser() - " + modifiedUser.getName());

    // Create initial context
    DirContext ctxRoot;
    try {

      ctxRoot = getLDAPAdminDirContext(ldapRootDN);

    } catch (NamingException e) {

      LOGGER.debug("Error creating LDAP context with user - " + modifiedUser.getName(), e);
      throw new LdapUtilityException("Error creating LDAP context with user - " + modifiedUser.getName(), e);
    }

    modifyUser(ctxRoot, modifiedUser, null, true);

    try {

      ctxRoot.close();

    } catch (NamingException e) {
      // TODO Should I ignore this?
      LOGGER.warn("Ignoring error closing LDAP context - " + modifiedUser.getName(), e);
    }

    return getUser(modifiedUser.getName());
  }

  /**
   * Sets the user's password.
   * 
   * @param username
   * @param password
   * 
   * @throws NoSuchUserException
   *           if specified {@link User} doesn't exist.
   * @throws IllegalOperationException
   *           if the user is one of the protected users.
   * @throws LdapUtilityException
   */
  public void setUserPassword(String username, String password)
    throws IllegalOperationException, NoSuchUserException, LdapUtilityException {

    if (this.ldapProtectedUsers.contains(username)) {
      throw new IllegalOperationException("User (" + username + ") is protected and cannot be modified.");
    }

    setUserPasswordUnchecked(username, password);
  }

  /**
   * Modify the {@link User}'s information.
   * 
   * @param modifiedUser
   *          the {@link User} to modify.
   * 
   * @param currentPassword
   *          the current {@link User}'s password.
   * @param newPassword
   *          the new {@link User}'s password. To maintain the current password,
   *          use <code>null</code>.
   * 
   * @return the modified {@link User}.
   * 
   * @throws NoSuchUserException
   *           if the Use being modified doesn't exist.
   * @throws EmailAlreadyExistsException
   *           if the specified email is already used by another user.
   * @throws IllegalOperationException
   *           if the user is one of the protected users.
   * @throws LdapUtilityException
   */
  public User modifySelfUser(User modifiedUser, String currentPassword, String newPassword)
    throws NoSuchUserException, EmailAlreadyExistsException, IllegalOperationException, LdapUtilityException {

    LOGGER.trace("modifySelfUser() - " + modifiedUser.getName());

    // Create initial context
    DirContext ctxRoot;
    try {

      ctxRoot = getLDAPDirContext(ldapRootDN, getUserDN(modifiedUser.getName()), currentPassword);

    } catch (NamingException e) {

      LOGGER.debug("Error creating LDAP context with user - " + modifiedUser.getName(), e);
      throw new LdapUtilityException("Error creating LDAP context with user - " + modifiedUser.getName(), e);
    }

    modifyUser(ctxRoot, modifiedUser, newPassword, false);

    try {

      ctxRoot.close();

    } catch (NamingException e) {
      // TODO Should I ignore this?
      LOGGER.warn("Ignoring error closing LDAP context - " + modifiedUser.getName(), e);
    }

    return getUser(modifiedUser.getName());
  }

  /**
   * Removes a {@link User}.
   * 
   * @param username
   *          the name of the user to remove.
   * 
   * @throws IllegalOperationException
   * @throws LdapUtilityException
   */
  public void removeUser(String username) throws IllegalOperationException, LdapUtilityException {

    if (this.ldapProtectedUsers.contains(username)) {
      throw new IllegalOperationException(userMessage(username, " is protected and cannot be removed."));
    }

    try {
      // Create initial context
      DirContext ctxRoot = getLDAPAdminDirContext(ldapRootDN);

      removeMember(ctxRoot, getUserDN(username));

      ctxRoot.close();

    } catch (NamingException e) {
      LOGGER.debug("Error removing user " + username, e);
      throw new LdapUtilityException("Error removing user " + username, e);
    }

  }

  /**
   * Marks a {@link User} as inactive.
   * 
   * @param username
   *          the name of the user to deactivate.
   * 
   * 
   * @throws NoSuchUserException
   * @throws IllegalOperationException
   * @throws LdapUtilityException
   * 
   */
  public void deactivateUser(String username)
    throws NoSuchUserException, IllegalOperationException, LdapUtilityException {

    User user = getUser(username);

    if (user != null) {

      user.setActive(false);

      try {
        modifyUser(user);
      } catch (EmailAlreadyExistsException e) {
        LOGGER.error("EmailAlreadyExistsException should not occcur here!!! This is problably a bug!", e);
      }

    } else {
      throw new NoSuchUserException(userMessage(username, " doesn't exist."));

    }

  }

  /**
   * Returns the number of registered groups.
   * 
   * @param contentAdapterFilter
   * 
   * @return an <code>int</code> with the number of groups in the repository.
   * @throws LdapUtilityException
   */
  // public int getGroupCount(Filter contentAdapterFilter) throws
  // LdapUtilityException {
  //
  // JndiContentAdapterEngine<GroupAdapter, Group> jndiAdapterEngine = new
  // JndiContentAdapterEngine<GroupAdapter, Group>(
  // new GroupAdapter(), new ContentAdapter(contentAdapterFilter, null,
  // null));
  //
  // try {
  //
  // DirContext ctxRoot = getLDAPDirContext(ldapRootDN);
  //
  // List<Attributes> attributesList = searchAttributes(ctxRoot, ldapGroupsDN,
  // "cn", jndiAdapterEngine);
  //
  // ctxRoot.close();
  //
  // return attributesList.size();
  //
  // } catch (NamingException e) {
  // logger.debug("Error counting groups - " + e.getMessage(), e);
  // throw new LdapUtilityException("Error counting groups - " +
  // e.getMessage(), e);
  // }
  // }

  /**
   * Return groups that match the given {@link ContentAdapter}.
   * 
   * @param contentAdapter
   * 
   * @return an array of {@link Group}'s.
   * 
   * @throws LdapUtilityException
   */
  public List<Group> getGroups(Filter filter) throws LdapUtilityException {

    try {

      DirContext ctxRoot = getLDAPDirContext(ldapRootDN);

      List<Attributes> attributesList = searchAttributes(ctxRoot, ldapGroupsDN, "cn", filter);

      List<Group> groups = new ArrayList<Group>();
      for (Attributes attributes : attributesList) {
        Group group = getGroupFromAttributes(attributes);

        // Add all roles assigned to this group
        Set<String> memberRoles = getMemberRoles(ctxRoot, getGroupDN(group.getName()));
        group.setAllRoles(memberRoles);

        // Add direct roles assigned to this group
        for (String role : getMemberDirectRoles(ctxRoot, getGroupDN(group.getName()))) {
          group.addDirectRole(role);
        }

        // Add all groups to which this group belongs
        Set<String> memberGroups = getMemberGroups(ctxRoot, getGroupDN(group.getName()));
        group.setAllGroups(memberGroups);

        // Add groups to which this group belongs
        for (String groupDN : getDNsOfGroupsContainingMember(ctxRoot, getGroupDN(group.getName()))) {
          group.addDirectGroup(getGroupCNFromDN(groupDN));
        }

        groups.add(group);
      }

      ctxRoot.close();

      return groups;

    } catch (NamingException e) {
      LOGGER.debug("Error getting groups - " + e.getMessage(), e);
      throw new LdapUtilityException("Error getting groups - " + e.getMessage(), e);
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
   * @throws LdapUtilityException
   *           if the group information could not be retrieved from the LDAP
   *           server.
   */
  public Group getGroup(String name) throws LdapUtilityException {

    Group group = null;

    try {
      // Create initial context
      DirContext ctxRoot = getLDAPDirContext(ldapRootDN);

      group = getGroup(ctxRoot, name);

      // Close the context when we're done
      ctxRoot.close();

    } catch (NameNotFoundException e) {

      group = null;
      LOGGER.debug("Group " + name + " doesn't exist.", e);

    } catch (NamingException e) {
      LOGGER.debug("Error searching for group " + name, e);
      throw new LdapUtilityException("Error searching for group " + name, e);
    }

    return group;
  }

  /**
   * Adds a new {@link Group}.
   * 
   * @param group
   *          the {@link Group} to add.
   * @return the newly created {@link Group}.
   * @throws GroupAlreadyExistsException
   *           if a Group with the same name already exists.
   * @throws LdapUtilityException
   *           if something goes wrong with the creation of the new group.
   */
  public Group addGroup(Group group) throws GroupAlreadyExistsException, LdapUtilityException {

    if (!group.isNameValid()) {
      LOGGER.debug("'" + group.getName() + "' is not a valid group name.");
      throw new LdapUtilityException("'" + group.getName() + "' is not a valid group name.");
    }

    try {
      // Create initial context
      DirContext ctxRoot = getLDAPAdminDirContext(ldapRootDN);

      Attributes attributes = new BasicAttributes();

      Attribute objectClass = new BasicAttribute("objectclass");
      objectClass.add("groupOfUniqueNames");
      objectClass.add("top");
      objectClass.add("extensibleObject");

      attributes.put(objectClass);
      attributes.put("cn", group.getName());
      attributes.put("ou", group.getFullName());
      attributes.put(SHADOW_INACTIVE, group.isActive() ? "0" : "1");

      Attribute attributeUniqueMember = new BasicAttribute(UNIQUE_MEMBER);

      // Add admin to all groups
      attributeUniqueMember.add(ldapAdminDN);

      for (String memberName : group.getMemberUserNames()) {
        attributeUniqueMember.add(getUserDN(memberName));
      }

      attributes.put(attributeUniqueMember);

      ctxRoot.bind(getGroupDN(group.getName()), null, attributes);

      setMemberDirectRoles(ctxRoot, getGroupDN(group.getName()), group.getDirectRoles());
      setMemberGroups(ctxRoot, getGroupDN(group.getName()), group.getDirectGroups());

      // Close the context when we're done
      ctxRoot.close();

    } catch (NameAlreadyBoundException e) {

      LOGGER.debug("Group " + group.getName() + " already exists.", e);
      throw new GroupAlreadyExistsException("Group " + group.getName() + " already exists.", e);

    } catch (NamingException e) {
      LOGGER.debug("Error adding group " + group.getName(), e);
      throw new LdapUtilityException("Error adding group " + group.getName(), e);
    }

    Group newGroup = getGroup(group.getName());
    if (newGroup == null) {
      throw new LdapUtilityException("The group was not created!");
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
   * @throws NoSuchGroupException
   *           if the group with being modified doesn't exist.
   * @throws IllegalOperationException
   * @throws LdapUtilityException
   */
  public Group modifyGroup(Group modifiedGroup)
    throws NoSuchGroupException, IllegalOperationException, LdapUtilityException {

    LOGGER.trace("modifyGroup() - " + modifiedGroup.getName());

    if (this.ldapProtectedGroups.contains(modifiedGroup.getName())) {
      throw new IllegalOperationException(
        "Group (" + modifiedGroup.getName() + ") is protected and cannot be modified.");
    }

    try {
      // Create initial context
      DirContext ctxRoot = getLDAPAdminDirContext(ldapRootDN);

      Attributes attributes = new BasicAttributes();

      attributes.put(SHADOW_INACTIVE, modifiedGroup.isActive() ? "0" : "1");

      attributes.put("ou", modifiedGroup.getFullName());

      Attribute attributeUniqueMember = new BasicAttribute(UNIQUE_MEMBER);

      // Add admin to all groups
      attributeUniqueMember.add(ldapAdminDN);

      if (modifiedGroup.getMemberUserNames() != null) {
        for (String memberName : modifiedGroup.getMemberUserNames()) {
          attributeUniqueMember.add(getUserDN(memberName));
        }
      }
      if (modifiedGroup.getMemberGroupNames() != null) {
        for (String memberName : modifiedGroup.getMemberGroupNames()) {
          attributeUniqueMember.add(getGroupDN(memberName));
        }
      }
      attributes.put(attributeUniqueMember);

      ctxRoot.modifyAttributes(getGroupDN(modifiedGroup.getName()), DirContext.REPLACE_ATTRIBUTE, attributes);

      setMemberGroups(ctxRoot, getGroupDN(modifiedGroup.getName()), modifiedGroup.getDirectGroups());
      setMemberDirectRoles(ctxRoot, getGroupDN(modifiedGroup.getName()), modifiedGroup.getDirectRoles());

      // Close the context when we're done
      ctxRoot.close();

    } catch (NameNotFoundException e) {
      LOGGER.debug("Group " + modifiedGroup.getName() + " doesn't exist.", e);
      throw new NoSuchGroupException("Group " + modifiedGroup.getName() + " doesn't exist.", e);
    } catch (NamingException e) {
      LOGGER.debug("Error modifying group " + modifiedGroup.getName(), e);
      throw new LdapUtilityException("Error modifying group " + modifiedGroup.getName(), e);
    }

    return getGroup(modifiedGroup.getName());
  }

  /**
   * Removes a group.
   * 
   * @param groupname
   *          the name of the group to remove.
   * @throws IllegalOperationException
   * @throws LdapUtilityException
   */
  public void removeGroup(String groupname) throws LdapUtilityException, IllegalOperationException {

    if (this.ldapProtectedGroups.contains(groupname)) {
      throw new IllegalOperationException("Group (" + groupname + ") is protected and cannot be removed.");
    }

    try {

      // Create initial context
      DirContext ctxRoot = getLDAPAdminDirContext(ldapRootDN);

      removeMember(ctxRoot, getGroupDN(groupname));

      ctxRoot.close();

    } catch (NamingException e) {

      LOGGER.debug("Error removing group " + groupname, e);

      throw new LdapUtilityException("Error removing group " + groupname, e);
    }

  }

  /**
   * Returns list of {@link User}s that belong to a group named
   * <code>groupName</code>.
   * 
   * @param groupName
   *          the name of the group.
   * 
   * @return an array of {@link User}s with all users that belong to a group
   *         named <code>groupName</code>.
   * 
   * @throws LdapUtilityException
   *           if the group or user information could not be retrieved from the
   *           LDAP server.
   */
  public User[] getUsersInGroup(String groupName) throws LdapUtilityException {
    try {
      List<User> users = new ArrayList<User>();

      DirContext ctxRoot = getLDAPDirContext(ldapRootDN);

      Set<String> directGroupMembersDN = getDNsOfDirectGroupMembers(ctxRoot, getGroupDN(groupName));

      for (String memberDN : directGroupMembersDN) {

        if (memberDN.endsWith(getPeopleDN())) {

          try {

            users.add(getUser(ctxRoot, getUserUIDFromDN(memberDN)));

          } catch (NamingException e) {
            LOGGER.error("Error getting user " + memberDN + " - " + e.getMessage() + " - IGNORING!", e);
          }

        } else {
          // It's not a user
        }

      }

      ctxRoot.close();

      return users.toArray(new User[users.size()]);

    } catch (NamingException e) {
      LOGGER.debug("Error getting users in group - " + e.getMessage(), e);
      throw new LdapUtilityException("Error getting users in group - " + e.getMessage(), e);
    }
  }

  /**
   * Returns the roles assigned to a given user.
   * 
   * @param userName
   *          the name of the user.
   * @return a Set of roles assigned to a user.
   * @throws LdapUtilityException
   */
  public Set<String> getUserRoles(String userName) throws LdapUtilityException {

    try {

      DirContext ctxRoot = getLDAPDirContext(ldapRootDN);

      Set<String> userRoleNames = getMemberRoles(ctxRoot, getUserDN(userName));

      ctxRoot.close();

      return userRoleNames;

    } catch (NamingException e) {
      LOGGER.debug("Error getting user group names", e);
      throw new LdapUtilityException("Error getting user group names", e);
    }
  }

  /**
   * Returns the roles directly assigned to a given user.
   * 
   * @param userName
   *          the name of the user.
   * 
   * @return an array of roles directly assigned to a user.
   * 
   * @throws LdapUtilityException
   */
  public Set<String> getUserDirectRoles(String userName) throws LdapUtilityException {

    try {

      DirContext ctxRoot = getLDAPDirContext(ldapRootDN);

      Set<String> directMemberRolesDN = getDNsOfDirectRolesForMember(ctxRoot, getUserDN(userName));

      ctxRoot.close();

      Set<String> directRoles = new HashSet<String>();
      for (String roleDN : directMemberRolesDN) {
        directRoles.add(getRoleCNFromDN(roleDN));
      }

      return directRoles;

    } catch (NamingException e) {
      LOGGER.debug("Error getting user group names", e);
      throw new LdapUtilityException("Error getting user group names", e);
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
   * @throws ServiceException
   */
  public RodaUser getAuthenticatedUser(String username, String password)
    throws AuthenticationDeniedException, ServiceException {

    try {
      // Create initial context
      DirContext ctxRoot = getLDAPDirContext(ldapRootDN, getUserDN(username), password);

      RodaUser user = getRodaUser(ctxRoot, username);
      ctxRoot.close();
      return user;

    } catch (AuthenticationException e) {

      if (isInvalidCredentials(e.getMessage())) {
        throw new AuthenticationDeniedException(e.getMessage());
      } else {
        LOGGER.debug("Error searching for user " + username, e);
        throw new ServiceException(e.getMessage(), ServiceException.INTERNAL_SERVER_ERROR);
      }
    } catch (NamingException e) {
      LOGGER.debug("Error searching for user " + username, e);
      throw new ServiceException(e.getMessage(), ServiceException.INTERNAL_SERVER_ERROR);
    }

  }

  private boolean isInvalidCredentials(String errorMessage) {
    return errorMessage.contains("LDAP: error code 49 - INVALID_CREDENTIALS");
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
   * @throws LdapUtilityException
   *           if something goes wrong with the register process.
   */
  public User registerUser(User user, String password)
    throws UserAlreadyExistsException, EmailAlreadyExistsException, LdapUtilityException {

    // A new registered user, is always inactive.
    user.setActive(false);

    // Generate an email verification token with 1 day expiration date.
    UUID uuidToken = UUID.randomUUID();
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, 1);
    String isoDateNoMillis = DateParser.getIsoDateNoMillis(calendar.getTime());

    user.setEmailConfirmationToken(uuidToken.toString());
    user.setEmailConfirmationTokenExpirationDate(isoDateNoMillis);

    User newUser = addUser(user);
    try {

      setUserPassword(newUser.getName(), password);

    } catch (IllegalOperationException e) {
      throw new LdapUtilityException("Error setting user password - " + e.getMessage(), e);
    } catch (NoSuchUserException e) {
      throw new LdapUtilityException("Error setting user password - " + e.getMessage(), e);
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
   * 
   * @return the {@link User} whose email has been confirmed.
   * 
   * @throws NoSuchUserException
   *           if the username and email don't exist.
   * @throws IllegalArgumentException
   *           if username and email are <code>null</code>.
   * @throws InvalidTokenException
   *           if the specified token doesn't exist, has already expired or it
   *           doesn't correspond to the stored token.
   * @throws LdapUtilityException
   *           if something goes wrong with the operation.
   */
  public User confirmUserEmail(String username, String email, String emailConfirmationToken)
    throws NoSuchUserException, IllegalArgumentException, InvalidTokenException, LdapUtilityException {

    User user = null;
    if (username != null) {
      user = getUser(username);
    } else if (email != null) {
      user = getUserWithEmail(email);
    } else {
      throw new IllegalArgumentException("username and email can not both be null");
    }

    if (user == null) {

      String message;
      if (username != null) {
        message = userMessage(username, " doesn't exist");
      } else {
        message = "Email " + email + " is not registered by any user";
      }

      throw new NoSuchUserException(message);

    } else {

      if (user.getEmailConfirmationToken() == null) {

        throw new InvalidTokenException("There's no active email confirmation token.");

      } else if (!user.getEmailConfirmationToken().equals(emailConfirmationToken)) {

        // Token argument is not equal to stored token.
        throw new InvalidTokenException("Email confirmation token is invalid.");

      } else if (user.getEmailConfirmationTokenExpirationDate() == null) {

        // No expiration date

      } else {

        String currentIsoDate = DateParser.getIsoDateNoMillis(Calendar.getInstance().getTime());

        if (currentIsoDate.compareToIgnoreCase(user.getEmailConfirmationTokenExpirationDate()) > 0) {

          throw new InvalidTokenException(
            "Email confirmation token expired in " + user.getEmailConfirmationTokenExpirationDate());

        } else {
          // Good, token didn't expired yet.
        }

      }

      user.setActive(true);
      user.setEmailConfirmationToken(null);
      user.setEmailConfirmationTokenExpirationDate(null);

      try {

        return modifyUser(user);

      } catch (IllegalOperationException e) {
        throw new LdapUtilityException("Error confirming user email - " + e.getMessage(), e);
      } catch (EmailAlreadyExistsException e) {
        throw new LdapUtilityException("Error confirming user email - " + e.getMessage(), e);
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
   * @throws NoSuchUserException
   *           if username or email doesn't correspond to any registered
   *           {@link User}.
   * @throws IllegalOperationException
   *           if email corresponds to a protected {@link User}.
   * @throws LdapUtilityException
   *           if something goes wrong with the operation.
   */
  public User requestPasswordReset(String username, String email)
    throws NoSuchUserException, IllegalOperationException, LdapUtilityException {

    User user = null;
    if (username != null) {
      user = getUser(username);
    } else if (email != null) {
      user = getUserWithEmail(email);
    } else {
      throw new IllegalArgumentException("username and email can not both be null");
    }

    if (user == null) {

      String message;
      if (username != null) {
        message = userMessage(username, " doesn't exist");
      } else {
        message = "Email " + email + " is not registered by any user";
      }

      throw new NoSuchUserException(message);

    } else {

      // Generate a password reset token with 1 day expiration date.
      UUID uuidToken = UUID.randomUUID();
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_MONTH, 1);
      String isoDateNoMillis = DateParser.getIsoDateNoMillis(calendar.getTime());

      user.setResetPasswordToken(uuidToken.toString());
      user.setResetPasswordTokenExpirationDate(isoDateNoMillis);

      try {

        return modifyUser(user);

      } catch (EmailAlreadyExistsException e) {
        throw new LdapUtilityException("Error setting password reset token - " + e.getMessage(), e);
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
   * @throws NoSuchUserException
   *           if a {@link User} with the same name already exists.
   * @throws InvalidTokenException
   *           if the specified token doesn't exist, has already expired or it
   *           doesn't correspond to the stored token.
   * @throws IllegalOperationException
   *           if the username corresponds to a protected {@link User}.
   * @throws LdapUtilityException
   *           if something goes wrong with the operation.
   */
  public User resetUserPassword(String username, String password, String resetPasswordToken)
    throws NoSuchUserException, InvalidTokenException, IllegalOperationException, LdapUtilityException {

    User user = getUser(username);

    if (user == null) {

      throw new NoSuchUserException(userMessage(username, " doesn't exist"));

    } else {

      if (user.getResetPasswordToken() == null) {

        throw new InvalidTokenException("There's no active password reset token.");

      } else if (!user.getResetPasswordToken().equals(resetPasswordToken)) {

        // Token argument is not equal to stored token.
        throw new InvalidTokenException("Password reset token is invalid.");

      } else if (user.getResetPasswordTokenExpirationDate() == null) {

        // No expiration date

      } else {

        String currentIsoDate = DateParser.getIsoDateNoMillis(Calendar.getInstance().getTime());

        if (currentIsoDate.compareToIgnoreCase(user.getResetPasswordTokenExpirationDate()) > 0) {

          throw new InvalidTokenException(
            "Password reset token expired in " + user.getResetPasswordTokenExpirationDate());

        } else {
          // Good, token didn't expired yet.
        }

      }

      try {

        setUserPassword(username, password);

        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpirationDate(null);

        return modifyUser(user);

      } catch (IllegalOperationException e) {
        throw new LdapUtilityException("Error reseting user password - " + e.getMessage(), e);
      } catch (EmailAlreadyExistsException e) {
        throw new LdapUtilityException("Error reseting user password - " + e.getMessage(), e);
      }
    }
  }

  /**
   * Gets the list of role names.
   * 
   * @return and array {@link String} with all the roles names.
   * @throws LdapUtilityException
   */
  public String[] getRoles() throws LdapUtilityException {
    try {

      DirContext ctxRoot = getLDAPDirContext(ldapRootDN);

      List<String> rolesDN = getRolesDN(ctxRoot);

      String[] roleNames = new String[rolesDN.size()];

      int index = 0;
      for (String roleDN : rolesDN) {

        roleNames[index] = getRoleCNFromDN(roleDN);

        index++;
      }

      ctxRoot.close();

      return roleNames;

    } catch (NamingException e) {
      LOGGER.debug("Error getting role names", e);
      throw new LdapUtilityException("Error getting role names", e);
    }
  }

  /**
   * Returns the roles assigned to a given group.
   * 
   * @param groupName
   *          the name of the group.
   * 
   * @return a Set of roles assigned to a group.
   * @throws LdapUtilityException
   */
  public Set<String> getGroupRoles(String groupName) throws LdapUtilityException {
    try {

      DirContext ctxRoot = getLDAPDirContext(ldapRootDN);

      Set<String> roles = getMemberRoles(ctxRoot, getGroupDN(groupName));

      ctxRoot.close();

      return roles;

    } catch (NamingException e) {
      LOGGER.debug("Error getting group names", e);
      throw new LdapUtilityException("Error getting group names", e);
    }
  }

  /**
   * Returns the roles directly assigned to a given group.
   * 
   * @param groupName
   *          the name of the group.
   * 
   * @return an array of roles directly assigned to a user.
   * 
   * @throws LdapUtilityException
   */
  public String[] getGroupDirectRoles(String groupName) throws LdapUtilityException {

    try {

      DirContext ctxRoot = getLDAPDirContext(ldapRootDN);

      Set<String> directMemberRolesDN = getDNsOfDirectRolesForMember(ctxRoot, getGroupDN(groupName));

      ctxRoot.close();

      String[] directRoles = new String[directMemberRolesDN.size()];

      int count = 0;
      for (String roleDN : directMemberRolesDN) {
        directRoles[count] = getRoleCNFromDN(roleDN);
        count++;
      }

      return directRoles;

    } catch (NamingException e) {
      LOGGER.debug("Error getting group group names", e);
      throw new LdapUtilityException("Error getting group group names", e);
    }
  }

  /**
   * Sets the groups to which a group belongs.
   * 
   * @param groupName
   *          the name of the group to change the groups.
   * @param groups
   *          a list of groups that this group should belongs.
   * @throws NoSuchGroupException
   *           if the specified Group doesn't exist.
   * @throws LdapUtilityException
   */
  public void setSuperGroups(String groupName, Set<String> groups) throws LdapUtilityException, NoSuchGroupException {
    try {

      DirContext ctxRoot = getLDAPAdminDirContext(ldapRootDN);
      setMemberGroups(ctxRoot, getGroupDN(groupName), groups);
      ctxRoot.close();

    } catch (NameNotFoundException e) {
      LOGGER.debug("Group " + groupName + " doesn't exist.", e);
      throw new NoSuchGroupException("Group " + groupName + " doesn't exist.", e);
    } catch (NamingException e) {
      LOGGER.debug("Error setting user groups", e);
      throw new LdapUtilityException("Error setting user groups", e);
    }
  }

  private RodaUser getRodaUser(DirContext ctxRoot, String username) throws NamingException {

    Attributes attributes = ctxRoot.getAttributes(getUserDN(username));

    RodaUser user = getRodaUserFromAttributes(attributes);

    // Add all roles assigned to this user
    Set<String> memberRoles = getMemberRoles(ctxRoot, getUserDN(username));
    user.setAllRoles(memberRoles);

    // Add direct roles assigned to this user
    for (String role : getMemberDirectRoles(ctxRoot, getUserDN(username))) {
      user.addDirectRole(role);
    }

    // Add all groups to which this user belongs
    Set<String> memberGroups = getMemberGroups(ctxRoot, getUserDN(username));
    user.setAllGroups(memberGroups);

    // Add groups to which this user belongs
    for (String groupDN : getDNsOfGroupsContainingMember(ctxRoot, getUserDN(username))) {
      user.addGroup(getGroupCNFromDN(groupDN));
    }

    return user;
  }

  private User getUser(DirContext ctxRoot, String username) throws NamingException {

    Attributes attributes = ctxRoot.getAttributes(getUserDN(username));

    User user = getUserFromAttributes(attributes);

    // Add all roles assigned to this user
    Set<String> memberRoles = getMemberRoles(ctxRoot, getUserDN(username));
    user.setAllRoles(memberRoles);

    // Add direct roles assigned to this user
    for (String role : getMemberDirectRoles(ctxRoot, getUserDN(username))) {
      user.addDirectRole(role);
    }

    // Add all groups to which this user belongs
    Set<String> memberGroups = getMemberGroups(ctxRoot, getUserDN(username));
    user.setAllGroups(memberGroups);

    // Add groups to which this user belongs
    for (String groupDN : getDNsOfGroupsContainingMember(ctxRoot, getUserDN(username))) {
      user.addGroup(getGroupCNFromDN(groupDN));
    }

    return user;
  }

  private RodaUser getRodaUserFromAttributes(Attributes userAttributes) throws NamingException {
    RodaUser rodaUser = new RodaUser();
    rodaUser.setId(userAttributes.get("uid").get().toString());
    rodaUser.setName(rodaUser.getId());
    if (userAttributes.get("email") != null) {
      rodaUser.setEmail(userAttributes.get("email").get().toString());
    }
    if (userAttributes.get("cn") != null) {
      rodaUser.setFullName(userAttributes.get("cn").get().toString());
    }
    if (userAttributes.get(SHADOW_INACTIVE) != null) {
      String zeroOrOne = userAttributes.get(SHADOW_INACTIVE).get().toString();
      rodaUser.setActive("0".equalsIgnoreCase(zeroOrOne));
    } else {
      rodaUser.setActive(true);
    }
    rodaUser.setGuest(false);
    return rodaUser;
  }

  private User getUserFromAttributes(Attributes userAttributes) throws NamingException {

    User user = new User(userAttributes.get("uid").get().toString());

    if (userAttributes.get(SHADOW_INACTIVE) != null) {
      String zeroOrOne = userAttributes.get(SHADOW_INACTIVE).get().toString();
      user.setActive("0".equalsIgnoreCase(zeroOrOne));
    } else {
      user.setActive(true);
    }

    if (userAttributes.get("documentTitle") != null) {
      user.setIdDocumentType(userAttributes.get("documentTitle").get().toString());
    }
    if (userAttributes.get("documentIdentifier") != null) {
      user.setIdDocument(userAttributes.get("documentIdentifier").get().toString());
    }
    if (userAttributes.get("documentLocation") != null) {
      user.setIdDocumentLocation(userAttributes.get("documentLocation").get().toString());
    }
    if (userAttributes.get("documentVersion") != null) {
      try {
        user.setIdDocumentDate(DateParser.parse(userAttributes.get("documentVersion").get().toString()));
      } catch (InvalidDateException e) {
        LOGGER.warn("Error parsing ID document date (documentVersion) - " + e.getMessage(), e);
      }
    }

    if (userAttributes.get("serialNumber") != null) {
      user.setFinanceIdentificationNumber(userAttributes.get("serialNumber").get().toString());
    }

    if (userAttributes.get("co") != null) {
      user.setBirthCountry(userAttributes.get("co").get().toString());
    }

    if (userAttributes.get("cn") != null) {
      user.setFullName(userAttributes.get("cn").get().toString());
    }
    if (userAttributes.get("postalAddress") != null) {
      user.setPostalAddress(userAttributes.get("postalAddress").get().toString());
    }
    if (userAttributes.get("postalCode") != null) {
      user.setPostalCode(userAttributes.get("postalCode").get().toString());
    }
    if (userAttributes.get("l") != null) {
      user.setLocalityName(userAttributes.get("l").get().toString());
    }
    if (userAttributes.get("c") != null) {
      user.setCountryName(userAttributes.get("c").get().toString());
    }
    if (userAttributes.get("telephoneNumber") != null) {
      user.setTelephoneNumber(userAttributes.get("telephoneNumber").get().toString());
    }
    if (userAttributes.get("facsimileTelephoneNumber") != null) {
      user.setFax(userAttributes.get("facsimileTelephoneNumber").get().toString());
    }

    if (userAttributes.get("email") != null) {
      user.setEmail(userAttributes.get("email").get().toString());
    }

    if (userAttributes.get("businessCategory") != null) {
      user.setBusinessCategory(userAttributes.get("businessCategory").get().toString());
    }

    if (userAttributes.get("info") != null) {
      String infoStr = userAttributes.get("info").get().toString();

      // emailValidationToken;emailValidationTokenValidity;resetPasswordToken;resetPasswordTokenValidity

      String[] parts = infoStr.split(";");

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

  private Attributes getAttributesFromUser(User user, boolean removeUnsetValues) {

    Attributes attributes = new BasicAttributes();

    Attribute objectClass = new BasicAttribute("objectclass");
    objectClass.add("inetOrgPerson");
    objectClass.add("organizationalPerson");
    objectClass.add("person");
    objectClass.add("top");
    objectClass.add("extensibleObject");

    attributes.put(objectClass);
    attributes.put("uid", user.getName());
    attributes.put("cn", user.getFullName());
    attributes.put(SHADOW_INACTIVE, user.isActive() ? "0" : "1");

    if (!StringUtils.isBlank(user.getFullName())) {
      String[] names = user.getFullName().split(" ");
      if (names.length > 0) {
        attributes.put("givenName", names[0]);
        attributes.put("sn", names[names.length - 1]);
      } else {
        attributes.put("sn", user.getName());
      }
    }

    if (!StringUtils.isBlank(user.getIdDocumentType())) {
      attributes.put("documentTitle", user.getIdDocumentType());
    } else if (removeUnsetValues) {
      attributes.remove("documentTitle");
    }
    if (!StringUtils.isBlank(user.getIdDocument())) {
      attributes.put("documentIdentifier", user.getIdDocument());
    } else if (removeUnsetValues) {
      attributes.remove("documentIdentifier");
    }
    if (!StringUtils.isBlank(user.getIdDocumentLocation())) {
      attributes.put("documentLocation", user.getIdDocumentLocation());
    } else if (removeUnsetValues) {
      attributes.remove("documentLocation");
    }
    if (user.getIdDocumentDate() != null) {
      attributes.put("documentVersion", DateParser.getIsoDate(user.getIdDocumentDate()));
    } else if (removeUnsetValues) {
      attributes.remove("documentVersion");
    }

    if (!StringUtils.isBlank(user.getFinanceIdentificationNumber())) {
      attributes.put("serialNumber", user.getFinanceIdentificationNumber());
    } else if (removeUnsetValues) {
      attributes.remove("serialNumber");
    }

    if (!StringUtils.isBlank(user.getBirthCountry())) {
      attributes.put("friendlyCountryName", user.getBirthCountry());
    } else if (removeUnsetValues) {
      attributes.remove("friendlyCountryName");
    }

    if (!StringUtils.isBlank(user.getPostalAddress())) {
      attributes.put("postalAddress", user.getPostalAddress());
    } else if (removeUnsetValues) {
      attributes.remove("postalAddress");
    }
    if (!StringUtils.isBlank(user.getPostalCode())) {
      attributes.put("postalCode", user.getPostalCode());
    } else if (removeUnsetValues) {
      attributes.remove("postalCode");
    }
    if (!StringUtils.isBlank(user.getLocalityName())) {
      attributes.put("localityName", user.getLocalityName());
    } else if (removeUnsetValues) {
      attributes.remove("localityName");
    }
    if (!StringUtils.isBlank(user.getCountryName())) {
      attributes.put("countryName", user.getCountryName());
    } else if (removeUnsetValues)
      if (removeUnsetValues) {
        attributes.remove("countryName");
      }
    if (!StringUtils.isBlank(user.getTelephoneNumber())) {
      attributes.put("telephoneNumber", user.getTelephoneNumber());
    } else if (removeUnsetValues) {
      attributes.remove("telephoneNumber");
    }
    if (!StringUtils.isBlank(user.getFax())) {
      attributes.put("fax", user.getFax());
    } else if (removeUnsetValues) {
      attributes.remove("fax");
    }
    if (!StringUtils.isBlank(user.getEmail())) {
      attributes.put("email", user.getEmail());
    } else if (removeUnsetValues) {
      attributes.remove("email");
    }

    if (!StringUtils.isBlank(user.getBusinessCategory())) {
      attributes.put("businessCategory", user.getBusinessCategory());
    } else if (removeUnsetValues) {
      attributes.remove("businessCategory");
    }

    String infoStr = "";
    if (user.getEmailConfirmationToken() != null && user.getEmailConfirmationToken().trim().length() > 0) {
      infoStr = user.getEmailConfirmationToken();
    }
    infoStr += ";";
    if (user.getEmailConfirmationTokenExpirationDate() != null
      && user.getEmailConfirmationTokenExpirationDate().trim().length() > 0) {
      infoStr += user.getEmailConfirmationTokenExpirationDate();
    }
    infoStr += ";";
    if (user.getResetPasswordToken() != null && user.getResetPasswordToken().trim().length() > 0) {
      infoStr += user.getResetPasswordToken();
    }
    infoStr += ";";
    if (user.getResetPasswordTokenExpirationDate() != null
      && user.getResetPasswordTokenExpirationDate().trim().length() > 0) {
      infoStr += user.getResetPasswordTokenExpirationDate();
    }
    attributes.put("info", infoStr);

    return attributes;
  }

  private Group getGroup(DirContext ctxRoot, String name) throws InvalidNameException, NamingException {

    Attributes attributes = ctxRoot.getAttributes(getGroupDN(name));

    Group group = getGroupFromAttributes(attributes);

    // Add all roles assigned to this group
    Set<String> memberRoles = getMemberRoles(ctxRoot, getGroupDN(name));
    group.setDirectRoles(memberRoles);

    // Add direct roles assigned to this group
    for (String role : getMemberDirectRoles(ctxRoot, getGroupDN(name))) {
      group.addDirectRole(role);
    }

    // Add all groups to which this group belongs
    Set<String> memberGroups = getMemberGroups(ctxRoot, getGroupDN(name));
    group.setAllGroups(memberGroups);

    // Add groups to which this group belongs
    for (String groupDN : getDNsOfGroupsContainingMember(ctxRoot, getGroupDN(name))) {
      group.addDirectGroup(getGroupCNFromDN(groupDN));
    }

    return group;
  }

  private Group getGroupFromAttributes(Attributes attributes) throws NamingException {

    Group group = new Group(attributes.get("cn").get().toString());

    if (attributes.get(SHADOW_INACTIVE) != null) {
      String zeroOrOne = attributes.get(SHADOW_INACTIVE).get().toString();
      group.setActive("0".equalsIgnoreCase(zeroOrOne));
    } else {
      group.setActive(true);
    }

    if (attributes.get("ou") != null) {
      group.setFullName(attributes.get("ou").get().toString());
    }

    Attribute attributeUniqueMember = attributes.get(UNIQUE_MEMBER);

    if (attributeUniqueMember != null) {

      for (int i = 0; i < attributeUniqueMember.size(); i++) {

        String memberDN = attributeUniqueMember.get(i).toString();

        if (memberDN.endsWith(getPeopleDN())) {

          group.addMemberUser(getUserUIDFromDN(memberDN));

        } else if (memberDN.endsWith(getGroupsDN())) {

          group.addMemberGroup(getGroupCNFromDN(memberDN));

        } else {
          // admin should be here!
        }

      }

    } else {
      // This group has no members
    }

    return group;
  }

  @SuppressWarnings("unchecked")
  // private List<Attributes> searchAttributes(DirContext ctxRoot, String
  // ctxDN, String keyAttribute,
  // JndiContentAdapterEngine<? extends JndiEntityAdapter, ? extends
  // RODAMember> jndiAdapter)
  // throws NamingException {
  //
  // // Create the default search controls
  // SearchControls searchControls = new SearchControls();
  //
  // String filter = jndiAdapter.getJndiFilter(keyAttribute);
  //
  // logger.trace("searchAttributes() JNDI filter: " + filter);
  //
  // // Search for objects using the filter
  // NamingEnumeration<SearchResult> answer = ctxRoot.search(ctxDN, filter,
  // searchControls);
  //
  // List<Attributes> filteredAttributesList = new ArrayList<Attributes>();
  //
  // while (answer.hasMore()) {
  // SearchResult sr = answer.next();
  // filteredAttributesList.add(sr.getAttributes());
  // }
  //
  // filteredAttributesList = (List<Attributes>)
  // jndiAdapter.filterValues(filteredAttributesList);
  //
  // List<Attributes> sortedAttributesList =
  // jndiAdapter.sortAttributes(filteredAttributesList);
  //
  // List<Attributes> attributesList =
  // jndiAdapter.getSublist(sortedAttributesList);
  //
  // return attributesList;
  // }
  // FIXME filter is not being used: see if it is needed
  private List<Attributes> searchAttributes(DirContext ctxRoot, String ctxDN, String keyAttribute, Filter filter)
    throws NamingException {
    // Create the default search controls
    SearchControls searchControls = new SearchControls();

    String jndiFilter = "(" + keyAttribute + "=*)";

    LOGGER.trace("searchAttributes() JNDI filter: " + jndiFilter);

    // Search for objects using the filter
    NamingEnumeration<SearchResult> answer = ctxRoot.search(ctxDN, jndiFilter, searchControls);

    List<Attributes> filteredAttributesList = new ArrayList<Attributes>();

    while (answer.hasMore()) {
      SearchResult sr = answer.next();
      filteredAttributesList.add(sr.getAttributes());
    }

    // filteredAttributesList = (List<Attributes>) jndiAdapter
    // .filterValues(filteredAttributesList);
    //
    // List<Attributes> sortedAttributesList = jndiAdapter
    // .sortAttributes(filteredAttributesList);
    //
    // List<Attributes> attributesList = jndiAdapter
    // .getSublist(sortedAttributesList);

    return filteredAttributesList;
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
  private String getUserDN(String username) {
    return String.format("uid=%1$s,%2$s", username, getPeopleDN());
  }

  /**
   * Returns the DN of a group given is groupName.
   * 
   * @param groupName
   *          the name of the group.
   * @return the DN of a group given is groupName.
   */
  private String getGroupDN(String groupName) {
    return String.format("cn=%1$s,%2$s", groupName, getGroupsDN());
  }

  /**
   * Returns the DN of a role given is roleName.
   * 
   * @param roleName
   *          the name of the role.
   * @return the DN of a role given is roleName.
   */
  private String getRoleDN(String roleName) {
    return String.format("cn=%1$s,%2$s", roleName, getRolesDN());
  }

  /**
   * Returns a role CN (Common Name) from it's DN (Distinguished Name). Ex: for
   * <i>DN=cn=administrator,ou=roles,dc=roda,dc=dgarq,dc=gov,dc=pt</i> returns
   * <i>administrator</i>.
   * 
   * @param roleDN
   * @return a {@link java.lang.String} with the CN.
   * @throws InvalidNameException
   */
  private String getRoleCNFromDN(String roleDN) throws InvalidNameException {
    Name name = new LdapName(roleDN);
    String roleCNWithCN = name.getSuffix(name.size() - 1).toString();
    String[] nameComps = roleCNWithCN.split("=");
    String roleCN = nameComps[1];
    return roleCN;
  }

  /**
   * Modify the {@link User}'s information.
   * 
   * @param ctxRoot
   * @param modifiedUser
   *          the {@link User} to modify.
   * 
   * @param newPassword
   *          the new {@link User}'s password. To maintain the current password,
   *          use <code>null</code>.
   * @param modifyRolesAndGroups
   * 
   * @throws NoSuchUserException
   *           if the {@link User} being modified doesn't exist.
   * @throws EmailAlreadyExistsException
   *           if the specified email is already used by another user.
   * @throws IllegalOperationException
   *           if the user is one of the protected users.
   * @throws LdapUtilityException
   */
  private void modifyUser(DirContext ctxRoot, User modifiedUser, String newPassword, boolean modifyRolesAndGroups)
    throws NoSuchUserException, IllegalOperationException, EmailAlreadyExistsException, LdapUtilityException {

    LOGGER.trace("modifyUser() - " + modifiedUser.getName());

    if (this.ldapProtectedUsers.contains(modifiedUser.getName())) {
      throw new IllegalOperationException("User (" + modifiedUser.getName() + ") is protected and cannot be modified.");
    }

    try {

      User currentEmailOwner = getUserWithEmail(ctxRoot, modifiedUser.getEmail());
      if (currentEmailOwner != null && !modifiedUser.getName().equals(currentEmailOwner.getName())) {

        LOGGER.debug("The email address " + modifiedUser.getEmail() + " is already used by another user.");

        throw new EmailAlreadyExistsException(
          "The email address " + modifiedUser.getEmail() + " is already used by another user.");
      }

      Attributes attributes = getAttributesFromUser(modifiedUser, true);

      ctxRoot.modifyAttributes(getUserDN(modifiedUser.getName()), DirContext.REPLACE_ATTRIBUTE, attributes);

      if (newPassword != null) {
        modifyUserPassword(ctxRoot, modifiedUser.getName(), newPassword);
      }

      if (modifyRolesAndGroups) {
        setMemberGroups(ctxRoot, getUserDN(modifiedUser.getName()), modifiedUser.getDirectGroups());
        setMemberDirectRoles(ctxRoot, getUserDN(modifiedUser.getName()), modifiedUser.getDirectRoles());
      }

    } catch (NameNotFoundException e) {

      LOGGER.debug(userMessage(modifiedUser.getName(), " doesn't exist."), e);
      throw new NoSuchUserException(userMessage(modifiedUser.getName(), " doesn't exist."), e);

    } catch (NamingException e) {

      LOGGER.debug("Error modifying user " + modifiedUser.getName(), e);
      throw new LdapUtilityException("Error modifying user " + modifiedUser.getName() + " - " + e.getMessage(), e);

    } catch (NoSuchAlgorithmException e) {

      LOGGER.debug("Error encoding password for user " + modifiedUser.getName(), e);
      throw new LdapUtilityException("Error encoding password for user " + modifiedUser.getName(), e);
    }

  }

  /**
   * Modifies user password.
   * 
   * @param ctxRoot
   * @param username
   * @param password
   * @throws NamingException
   * @throws UnsupportedEncodingException
   * @throws NoSuchAlgorithmException
   */
  private void modifyUserPassword(DirContext ctxRoot, String username, String password)
    throws NamingException, NoSuchAlgorithmException {

    PasswordHandler passwordHandler = PasswordHandler.getInstance();
    String passwordDigest = passwordHandler.generateDigest(password, null, ldapDigestAlgorithm);

    Attribute attributePassword = new BasicAttribute("userPassword", passwordDigest);

    ModificationItem[] modificationItem = new ModificationItem[1];
    modificationItem[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attributePassword);

    ctxRoot.modifyAttributes(getUserDN(username), modificationItem);
  }

  private void addMemberToGroup(DirContext ctxRoot, String groupDN, String memberDN) throws NamingException {

    Attributes attributes = ctxRoot.getAttributes(groupDN, new String[] {UNIQUE_MEMBER});

    Attribute attribute = attributes.get(UNIQUE_MEMBER);
    attribute.add(memberDN);

    ctxRoot.modifyAttributes(groupDN, DirContext.REPLACE_ATTRIBUTE, attributes);
  }

  private void addMemberToRole(DirContext ctxRoot, String roleDN, String memberDN) throws NamingException {

    Attributes attributes = ctxRoot.getAttributes(roleDN, new String[] {"roleOccupant"});

    Attribute attribute = attributes.get("roleOccupant");
    if (attribute == null) {
      attribute = new BasicAttribute("roleOccupant");
    }
    attribute.add(memberDN);

    ctxRoot.modifyAttributes(roleDN, DirContext.REPLACE_ATTRIBUTE, attributes);
  }

  private void removeMemberFromGroup(DirContext ctxRoot, String groupDN, String memberDN) throws NamingException {

    Attributes attributes = ctxRoot.getAttributes(groupDN, new String[] {UNIQUE_MEMBER});
    Attribute attribute = attributes.get(UNIQUE_MEMBER);
    attribute.remove(memberDN);

    ctxRoot.modifyAttributes(groupDN, DirContext.REPLACE_ATTRIBUTE, attributes);
  }

  private void removeMemberFromRole(DirContext ctxRoot, String roleDN, String memberDN) throws NamingException {

    Attributes attributes = ctxRoot.getAttributes(roleDN, new String[] {"roleOccupant"});

    Attribute attribute = attributes.get("roleOccupant");
    attribute.remove(memberDN);

    ctxRoot.modifyAttributes(roleDN, DirContext.REPLACE_ATTRIBUTE, attributes);
  }

  private void removeMember(DirContext ctxRoot, String memberDN) throws NamingException {

    // For each group the member is in, remove that member from the group
    Set<String> directMemberGroupsDN = getDNsOfGroupsContainingMember(ctxRoot, memberDN);
    for (String groupDN : directMemberGroupsDN) {
      removeMemberFromGroup(ctxRoot, groupDN, memberDN);
    }

    // For each role the member owns, remove that member from the
    // roleOccupant
    Set<String> directMemberRolesDN = getDNsOfDirectRolesForMember(ctxRoot, memberDN);
    for (String roleDN : directMemberRolesDN) {
      removeMemberFromRole(ctxRoot, roleDN, memberDN);
    }

    ctxRoot.unbind(memberDN);
  }

  /**
   * Returns the DirContext through the JNDI API using anonymous login to access
   * the LDAP server.
   * 
   * @param contextName
   *          the name of the context.
   * 
   * @return DirContext
   * @throws NamingException
   */
  private DirContext getLDAPDirContext(String contextName) throws NamingException {
    // FIXME
    // return getLDAPDirContext(contextName, null, null);
    return getLDAPDirContext(contextName, ldapAdminDN, ldapAdminPassword);
  }

  /**
   * Returns the DirContext through the JNDI API using admin's username and
   * password. to access the LDAP server.
   * 
   * @param contextName
   *          the name of the context.
   * 
   * @return DirContext
   * @throws NamingException
   */
  private DirContext getLDAPAdminDirContext(String contextName) throws NamingException {
    return getLDAPDirContext(contextName, ldapAdminDN, ldapAdminPassword);
  }

  /**
   * Returns the DirContext through the JNDI API using <code>accessUser</code>
   * and <code>accessPassword</code> to access the LDAP server.
   * 
   * @param contextDN
   *          the name of the context.
   * @param accessUserDN
   *          the username to access the LDAP server.
   * @param accessPassword
   *          the password to access the LDAP server.
   * @return the DirContext.
   * @throws NamingException
   */
  private DirContext getLDAPDirContext(String contextDN, String accessUserDN, String accessPassword)
    throws NamingException {

    // Set up the environment for creating the initial context
    final Hashtable<String, Object> env = new Hashtable<String, Object>(11);

    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, String.format("ldap://%1$s:%2$d/%3$s", ldapHost, ldapPort, contextDN));

    env.put(Context.SECURITY_AUTHENTICATION, AUTHENTICATION_SIMPLE);

    if (accessUserDN != null) {
      env.put(Context.SECURITY_PRINCIPAL, accessUserDN);
      env.put(Context.SECURITY_CREDENTIALS, accessPassword);
    }

    // env.put(Context.AUTHORITATIVE, "true"); // dont use cache

    return new InitialDirContext(env);
  }

  private Set<String> getDNsOfDirectGroupMembers(DirContext ctxRoot, String groupDN) throws NamingException {

    Set<String> membersDN = new HashSet<String>();

    Attributes attributes = ctxRoot.getAttributes(groupDN, new String[] {UNIQUE_MEMBER});

    Attribute attrUniqueMember = attributes.get(UNIQUE_MEMBER);

    NamingEnumeration<?> e = attrUniqueMember.getAll();

    while (e.hasMore()) {
      Object next = e.next();
      if (next.toString().equals(ldapAdminDN)) {
        // It's the admin user. don't add it to the list.
      } else {
        membersDN.add(next.toString());
      }
    }

    return membersDN;
  }

  /**
   * Returns the DN of groups that contain the given member.
   * 
   * @param ctxRoot
   * @param memberDN
   *          the DN of the member.
   * @return the DNs of the groups that has memberDN as member.
   * @throws NamingException
   */
  private Set<String> getDNsOfGroupsContainingMember(DirContext ctxRoot, String memberDN) throws NamingException {
    Set<String> groupsDN = new HashSet<String>();

    // Specify the attributes to match
    Attributes matchAttrs = new BasicAttributes(true); // ignore case
    matchAttrs.put(new BasicAttribute("cn"));
    matchAttrs.put(new BasicAttribute(UNIQUE_MEMBER, memberDN));

    // Search for objects that have those matching attributes
    NamingEnumeration<SearchResult> answer = ctxRoot.search(getGroupsDN(), matchAttrs, new String[] {});

    while (answer.hasMore()) {

      SearchResult sr = answer.next();
      groupsDN.add(sr.getNameInNamespace());
    }

    return groupsDN;
  }

  /**
   * Returns the DN of active groups that contain the given member.
   * 
   * @param ctxRoot
   * @param memberDN
   *          the DN of the member.
   * @return the DNs of the groups that has memberDN as member.
   * @throws NamingException
   */
  private Set<String> getDNsOfActiveGroupsContainingMember(DirContext ctxRoot, String memberDN) throws NamingException {

    Set<String> groupsDN = new HashSet<String>();

    // Specify the attributes to match
    Attributes matchAttrs = new BasicAttributes(true); // ignore case
    matchAttrs.put(new BasicAttribute("cn"));
    // matchAttrs.put(new BasicAttribute("olcReadOnly", "FALSE"));
    // matchAttrs.put(new BasicAttribute(SHADOW_INACTIVE, "FALSE"));
    matchAttrs.put(new BasicAttribute(UNIQUE_MEMBER, memberDN));

    // Search for objects that have those matching attributes
    NamingEnumeration<SearchResult> answer = ctxRoot.search(getGroupsDN(), matchAttrs, new String[] {SHADOW_INACTIVE});

    while (answer.hasMore()) {

      SearchResult sr = answer.next();

      String shadowInactive = "0";
      if (sr.getAttributes().get(SHADOW_INACTIVE) != null) {
        shadowInactive = sr.getAttributes().get(SHADOW_INACTIVE).get().toString();
      }

      if ("0".equalsIgnoreCase(shadowInactive)) {
        // its active
        groupsDN.add(sr.getNameInNamespace());
      } else {
        // it's read only (inactive)
      }
    }

    return groupsDN;
  }

  private Set<String> getDNsOfAllActiveGroupsForMember(DirContext ctxRoot, String memberDN) throws NamingException {

    Set<String> allMemberActiveGroupsDN = new HashSet<String>();

    Set<String> directMemberGroupsDN = getDNsOfActiveGroupsContainingMember(ctxRoot, memberDN);

    // add the groups that the member directly belongs to
    allMemberActiveGroupsDN.addAll(directMemberGroupsDN);

    // For each group, get the groups to which it belongs
    for (String memberGroupDN : directMemberGroupsDN) {
      allMemberActiveGroupsDN.addAll(getDNsOfAllActiveGroupsForMember(ctxRoot, memberGroupDN));
    }

    return allMemberActiveGroupsDN;
  }

  private Set<String> getDNsOfDirectRolesForMember(DirContext ctxRoot, String memberDN) throws NamingException {
    Set<String> rolesDN = new HashSet<String>();

    // Specify the attributes to match
    Attributes matchAttrs = new BasicAttributes(true); // ignore case
    matchAttrs.put(new BasicAttribute("cn"));
    matchAttrs.put(new BasicAttribute("roleOccupant", memberDN));

    // Search for objects that have those matching attributes
    NamingEnumeration<SearchResult> answer = ctxRoot.search(getRolesDN(), matchAttrs, new String[] {});

    while (answer.hasMore()) {

      SearchResult sr = answer.next();
      rolesDN.add(sr.getNameInNamespace());
    }

    return rolesDN;
  }

  private Set<String> getDNsOfAllRolesForMember(DirContext ctxRoot, String memberDN) throws NamingException {
    Set<String> allMemberRolesDN = new HashSet<String>();

    Set<String> directMemberRolesDN = getDNsOfDirectRolesForMember(ctxRoot, memberDN);

    // add the roles that the member directly owns
    allMemberRolesDN.addAll(directMemberRolesDN);

    // for each group that the member belongs to, get it's roles
    // too..
    Set<String> directMemberGroupsDN = getDNsOfActiveGroupsContainingMember(ctxRoot, memberDN);

    for (String memberGroupDN : directMemberGroupsDN) {
      allMemberRolesDN.addAll(getDNsOfAllRolesForMember(ctxRoot, memberGroupDN));
    }

    return allMemberRolesDN;
  }

  private Set<String> getMemberRoles(DirContext ctxRoot, String memberDN) throws NamingException {
    Set<String> roles = new HashSet<String>();

    Set<String> allMemberRolesDN = getDNsOfAllRolesForMember(ctxRoot, memberDN);
    for (String roleDN : allMemberRolesDN) {
      roles.add(getRoleCNFromDN(roleDN));
    }

    return roles;
  }

  private Set<String> getMemberDirectRoles(DirContext ctxRoot, String memberDN) throws NamingException {
    Set<String> directRoles = new HashSet<String>();

    Set<String> memberDirectRolesDN = getDNsOfDirectRolesForMember(ctxRoot, memberDN);

    for (String roleDN : memberDirectRolesDN) {
      directRoles.add(getRoleCNFromDN(roleDN));
    }

    return directRoles;
  }

  private Set<String> getMemberGroups(DirContext ctxRoot, String memberDN) throws NamingException {
    Set<String> groups = new HashSet<String>();

    Set<String> allMemberGroupsDN = getDNsOfAllActiveGroupsForMember(ctxRoot, memberDN);
    for (String groupDN : allMemberGroupsDN) {
      groups.add(getGroupCNFromDN(groupDN));
    }

    return groups;
  }

  private User getUserWithEmail(DirContext ctxRoot, String email) throws NamingException {

    // Specify the attributes to match
    Attributes matchAttrs = new BasicAttributes(true); // ignore case
    matchAttrs.put(new BasicAttribute("email", email));

    // Search for objects that have those matching attributes
    NamingEnumeration<SearchResult> answer = ctxRoot.search(getPeopleDN(), matchAttrs);

    User user = null;
    while (answer.hasMore() && user != null) {
      SearchResult sr = answer.next();
      user = getUserFromAttributes(sr.getAttributes());
    }

    return user;
  }

  /**
   * Sets the roles that a member owns.
   * 
   * @param ctxRoot
   *          the root context of LDAP
   * @param memberDN
   *          the DN of the member to change the roles for.
   * @param roles
   *          a list of roles that this member should own.
   * @throws NamingException
   */
  private void setMemberDirectRoles(DirContext ctxRoot, String memberDN, final Set<String> roles)
    throws NamingException {

    Set<String> properRoles = roles;
    if (properRoles == null) {
      LOGGER.warn("setMemberRoles() - roles is null. no roles");
      properRoles = new HashSet<String>();
    }

    Set<String> oldroles = getMemberDirectRoles(ctxRoot, memberDN);
    Set<String> newroles = new HashSet<String>(properRoles);

    // removing from oldroles all the roles in newroles, oldroles
    // becomes the Set of roles that the user doesn't want to own
    // anymore.
    Set<String> tempOldroles = new HashSet<String>(oldroles);
    tempOldroles.removeAll(newroles);

    // remove user from the roles in oldroles
    for (String role : tempOldroles) {
      removeMemberFromRole(ctxRoot, getRoleDN(role), memberDN);
    }

    // removing from newroles all the roles in oldroles, newroles
    // becomes the Set of the new roles that the user wants to own.
    newroles.removeAll(oldroles);

    // add member to the roles in newroles
    for (String role : newroles) {
      addMemberToRole(ctxRoot, getRoleDN(role), memberDN);
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
   */
  private void setMemberGroups(DirContext ctxRoot, String memberDN, Set<String> groups) throws NamingException {

    if (groups == null) {
      LOGGER.warn("setMemberGroups() - groups is null. no groups");
      groups = new HashSet<String>();
    }

    Set<String> oldgroupDNs = getDNsOfGroupsContainingMember(ctxRoot, memberDN);
    Set<String> newgroupDNs = new HashSet<String>();
    Iterator<String> it = groups.iterator();
    while (it.hasNext()) {
      newgroupDNs.add(getGroupDN(it.next()));
    }

    // removing all the groups in newgroups, oldgroups becomes the Set
    // of groups that the user doesn't want to belong to anymore.
    Set<String> tempOldgroupDNs = new HashSet<String>(oldgroupDNs);
    tempOldgroupDNs.removeAll(newgroupDNs);

    // remove user from the groups in oldgroups
    for (String groupDN : tempOldgroupDNs) {
      removeMemberFromGroup(ctxRoot, groupDN, memberDN);
    }

    // removing all the groups in oldgroups, newgroups becomes the Set
    // of the new groups that the user wants to bellong to.
    newgroupDNs.removeAll(oldgroupDNs);

    // add user to the groups in newgroups
    for (String groupDN : newgroupDNs) {
      addMemberToGroup(ctxRoot, groupDN, memberDN);
    }

  }

  /**
   * Sets the user's password without checking admin and guest users.
   * 
   * @param username
   * @param password
   * 
   * @throws NoSuchUserException
   *           if specified {@link User} doesn't exist.
   * @throws LdapUtilityException
   */
  private void setUserPasswordUnchecked(String username, String password)
    throws NoSuchUserException, LdapUtilityException {

    try {

      // Create initial context
      DirContext ctxRoot = getLDAPAdminDirContext(ldapRootDN);

      modifyUserPassword(ctxRoot, username, password);

      ctxRoot.close();

    } catch (NameNotFoundException e) {
      LOGGER.debug(userMessage(username, " doesn't exist."), e);
      throw new NoSuchUserException(userMessage(username, " doesn't exist."), e);
    } catch (NamingException e) {
      LOGGER.debug("Error setting password for user " + username, e);
      throw new LdapUtilityException("Error setting password for user " + username, e);
    } catch (NoSuchAlgorithmException e) {
      LOGGER.debug("Error encoding password for user " + username, e);
      throw new LdapUtilityException("Error encoding password for user " + username, e);
    }

  }

  /**
   * Returns a user UID (Unique ID) from it's DN (Distinguished Name). Ex: for
   * <i>DN=uid=rcastro,ou=people,dc=roda,dc=dgarq,dc=gov,dc=pt</i> returns
   * <i>rcastro</i>.
   * 
   * @param roleDN
   * @return a {@link java.lang.String} with the UID.
   * @throws InvalidNameException
   */
  private String getUserUIDFromDN(String userDN) throws InvalidNameException {
    Name name = new LdapName(userDN);
    String userCNWithCN = name.getSuffix(name.size() - 1).toString();
    String[] nameComps = userCNWithCN.split("=");
    String userCN = nameComps[1];
    return userCN;
  }

  /**
   * Returns a group CN (Common Name) from it's DN (Distinguished Name). Ex: for
   * <i>DN=cn=administrators,ou=groups,dc=roda,dc=dgarq,dc=gov,dc=pt</i> returns
   * <i>administrators</i>.
   * 
   * @param roleDN
   * @return a {@link java.lang.String} with the CN.
   * @throws InvalidNameException
   */
  private String getGroupCNFromDN(String groupDN) throws InvalidNameException {
    Name name = new LdapName(groupDN);
    String groupCNWithCN = name.getSuffix(name.size() - 1).toString();
    String[] nameComps = groupCNWithCN.split("=");
    String groupCN = nameComps[1];
    return groupCN;
  }

  private List<String> getRolesDN(DirContext ctxRoot) throws NamingException {
    return getDNsOfUsersGroupsRoles(ctxRoot, ldapRolesDN, "cn");
  }

  private List<String> getDNsOfUsersGroupsRoles(DirContext ctxRoot, String ctxDN, String keyAttribute)
    throws NamingException {

    List<String> dnList = new ArrayList<String>();

    // Specify the attributes to match
    Attributes matchAttrs = new BasicAttributes(true); // ignore case
    matchAttrs.put(new BasicAttribute(keyAttribute));

    // Search for objects that have those matching attributes
    NamingEnumeration<SearchResult> answer = ctxRoot.search(ctxDN, matchAttrs, new String[] {});

    while (answer.hasMore()) {
      SearchResult sr = answer.next();

      // Adds a new entry with the username
      dnList.add(sr.getNameInNamespace());
    }

    return dnList;
  }

  private String userMessage(String user, String message) {
    return "User " + user + message;
  }

}
