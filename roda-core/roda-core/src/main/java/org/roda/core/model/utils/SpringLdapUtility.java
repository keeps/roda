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
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.ldap.model.exception.LdapException;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringLdapUtility.class);

  /** Size of random passwords */
  private static final int RANDOM_PASSWORD_LENGTH = 12;

  private final LdapTemplate ldapTemplate;
  private final LdapContextSource ldapContextSource;
  private final LdapUserRepository ldapUserRepository;
  private final LdapGroupRepository ldapGroupRepository;
  private final LdapRoleRepository ldapRoleRepository;

  private static final String OBJECT_CLASS = "objectClass";
  private static final String OBJECT_CLASS_TOP = "top";
  private static final String OBJECT_CLASS_DOMAIN = "dcObject";
  private static final String OBJECT_CLASS_ORGANIZATION = "organization";
  private static final String OBJECT_CLASS_ORGANIZATIONAL_UNIT = "organizationalUnit";
  private static final String OBJECT_CLASS_ORGANIZATIONAL_ROLE = "organizationalRole";
  private static final String OBJECT_CLASS_EXTENSIBLE_OBJECT = "extensibleObject";

  private static final LdapName ldapRootDNName = LdapUtils.newLdapName("dc=roda,dc=org");
  private static final LdapName ldapRolesOU = LdapUtils.newLdapName("ou=roles");
  private static final LdapName ldapPeopleOU = LdapUtils.newLdapName("ou=users");
  private static final LdapName ldapGroupsOU = LdapUtils.newLdapName("ou=groups");
  private static final String RODA_DUMMY_USER = "cn=roda,ou=system,dc=roda,dc=org";
  private static final String DC = "dc";
  private static final String OU = "ou";
  private static final String O = "o";
  private static final String UID = "uid";
  private static final String CN = "cn";

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

  public SpringLdapUtility(LdapContextSource ldapContextSource, LdapTemplate ldapTemplate,
    LdapUserRepository ldapUserRepository, LdapGroupRepository ldapGroupRepository,
    LdapRoleRepository ldapRoleRepository) {
    this.ldapContextSource = ldapContextSource;
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

  @Override
  public void stopService() throws GenericException {

  }

  @Override
  public void initDirectoryService() throws Exception {
    initDirectoryService(null);
  }

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

  private void applyLdif(final String ldifPath) throws LdapException, IOException {
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

  private boolean dnExists(LdapName rdn) {
    try {
      ldapTemplate.lookup(rdn);
      return true;
    } catch (NamingException e) {
      return false;
    }
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
      return getUser(ldapUser);
    }
    return new User();
  }

  private User getUser(LdapUser ldapUser) {
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
    user.setGroups(getUserGroups(memberDN));

    // Add groups to which this user belongs
    for (String groupDN : getDNsOfGroupsContainingMember(memberDN)) {
      user.addGroup(getFirstNameFromDN(groupDN));
    }

    return user;
  }

  @Override
  public User getUserWithEmail(String email) throws GenericException {
    LdapUser ldapUser = ldapUserRepository.findFirstByEmail(email);
    if (ldapUser != null) {
      return getUser(ldapUser);
    }
    return null;
  }

  @Override
  public User addUser(User user) throws UserAlreadyExistsException, EmailAlreadyExistsException, GenericException {
    if (!user.isNameValid()) {
      LOGGER.debug("'{}' is not a valid user name.", user.getName());
      throw new GenericException("'" + user.getName() + "' is not a valid user name.");
    }

    if (getUserWithEmail(user.getEmail()) != null) {
      LOGGER.debug("The email address {} is already used.", user.getEmail());
      throw new EmailAlreadyExistsException("The email address " + user.getEmail() + " is already used.");
    }

    if (ldapUserRepository.findByUid(user.getId()) != null) {
      throw new UserAlreadyExistsException("User " + user.getName() + " already exists.");
    }

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

    return user;
  }

  private void setMemberDirectRoles(Name memberDN, Set<String> roles) {
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

  private void setMemberGroups(Name memberDN, Set<String> groups) {
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

  private void setUserPasswordUnchecked(final String username, SecureString password)
    throws NotFoundException, GenericException {
    try {
      modifyUserPassword(username, password);
    } catch (final LdapException e) {
      throw new GenericException("Error setting password for user " + username, e);
    } catch (final NoSuchAlgorithmException e) {
      throw new GenericException("Error encoding password for user " + username, e);
    }
  }

  private Set<String> getRoles() {
    final Set<String> roles = new HashSet<>();
    for (LdapRole ldapRole : ldapRoleRepository.findAll()) {
      roles.add(getFirstNameFromDN(ldapRole.getDn().toString()));
    }
    return roles;
  }

  @Override
  public User modifyUser(User modifiedUser)
    throws NotFoundException, IllegalOperationException, EmailAlreadyExistsException, GenericException {
    modifyUser(modifiedUser, null, true, false);
    return getUser(modifiedUser.getName());
  }

  public void modifyUser(final User modifiedUser, SecureString newPassword, final boolean modifyRolesAndGroups,
    final boolean force)
    throws NotFoundException, IllegalOperationException, EmailAlreadyExistsException, GenericException {

    if (!force && this.ldapProtectedUsers.contains(modifiedUser.getName())) {
      throw new IllegalOperationException("User (" + modifiedUser.getName() + ") is protected and cannot be modified.");
    }

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
      try {
        modifyUserPassword(modifiedUser.getName(), newPassword);
      } catch (final LdapException e) {
        throw new GenericException("Error modifying user " + modifiedUser.getName() + " - " + e.getMessage(), e);
      } catch (final NoSuchAlgorithmException e) {
        throw new GenericException("Error encoding password for user " + modifiedUser.getName(), e);
      }
    }

    if (modifyRolesAndGroups) {
      setMemberDirectRoles(userDN, modifiedUser.getGroups());
      setMemberDirectRoles(userDN, modifiedUser.getDirectRoles());
    }
  }

  private void modifyUserPassword(final String username, SecureString password)
    throws LdapException, NoSuchAlgorithmException {
    try {
      LdapUser ldapUser = ldapUserRepository.findByUid(username);
      ldapUser.setUserPassword(encodePassword(password));

      ldapUserRepository.save(ldapUser);
    } catch (InvalidKeySpecException e) {
      throw new LdapException(e.getMessage(), e);
    }
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

  @Override
  public void setUserPassword(String username, SecureString password)
    throws IllegalOperationException, NotFoundException, GenericException {
    LdapUser ldapUser = ldapUserRepository.findByUid(username);
    if (this.rodaGuestDN.equals(ldapUser.getDn().toString()) || this.ldapProtectedUsers.contains(username)) {
      throw new IllegalOperationException(String.format("User (%s) is protected and cannot be modified.", username));
    }

    setUserPasswordUnchecked(username, password);
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

    if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
      throw new AuthenticationDeniedException("Username and password cannot be blank!");
    }

    try {
      ldapTemplate.authenticate(LdapQueryBuilder.query().where(UID).is(username), password);
      return getUser(username);
    } catch (AuthenticationException e) {
      throw new AuthenticationDeniedException(e.getMessage(), e);
    } catch (Exception e) {
      throw new GenericException(e.getMessage(), e);
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
    LdapName roleDN = LdapNameBuilder.newInstance(ldapRolesOU).add(CN, roleName).build();
    LdapRole ldapRole = new LdapRole();
    ldapRole.setDn(roleDN);
    ldapRole.setCommonName(roleName);
    try {
      ldapRole.addRoleOccupant(new LdapName(rodaAdministratorsDN));
    } catch (InvalidNameException e) {
      throw new GenericException("Error adding RODA administrator user to role '" + roleName + "'", e);
    }

    if (!dnExists(roleDN)) {
      ldapRole.setNew(true);
    }

    ldapRoleRepository.save(ldapRole);
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

  private Group getGroupFromEntry(final LdapGroup ldapGroup) {
    Group group = new Group(ldapGroup.getCommonName());
    group.setActive("0".equalsIgnoreCase(ldapGroup.getShadowInactive()));
    group.setFullName(ldapGroup.getOu());

    for (Name name : ldapGroup.getUniqueMember()) {
      String memberDN = name.toString();
      if (memberDN.endsWith(ldapPeopleOU.toString())) {
        group.addMemberUser(LdapUtils.getStringValue(name, UID));
      } else if (memberDN.endsWith(ldapGroupsOU.toString())) {
        LOGGER.warn("Ignoring sub-group {} connection with group {}", memberDN, group.getId());
      } else if (!memberDN.equals(RODA_DUMMY_USER)) {
        LOGGER.warn("Member {} outside users and groups", memberDN);
      }
    }

    return group;
  }

  private Set<String> getDNsOfAllRolesForMember(final String memberDN) {
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

  private Set<String> getMemberRoles(final String memberDN) {
    final Set<String> allMemberRolesDN = getDNsOfAllRolesForMember(memberDN);
    final Set<String> roles = new HashSet<>();
    for (String roleDN : allMemberRolesDN) {
      roles.add(getFirstNameFromDN(roleDN));
    }
    return roles;
  }

  private Set<String> getMemberDirectRoles(final String memberDN) {
    Set<String> memberDirectRolesDN = getDNsOfDirectRolesForMember(memberDN);
    final Set<String> directRoles = new HashSet<>();
    for (String roleDN : memberDirectRolesDN) {
      directRoles.add(getFirstNameFromDN(roleDN));
    }
    return directRoles;
  }

  private Set<String> getUserGroups(final String memberDN) {
    Set<String> groups = new HashSet<>();
    for (String groupDN : getDNsOfGroupsContainingMember(memberDN)) {
      groups.add(getFirstNameFromDN(groupDN));
    }
    return groups;
  }

  private Set<String> getDNsOfActiveGroupsContainingMember(final String memberDN) {
    Set<LdapGroup> activeGroups = ldapGroupRepository.findAllByUniqueMemberAndShadowInactiveEquals(memberDN, 0);
    final Set<String> groupsDN = new HashSet<>();
    for (LdapGroup group : activeGroups) {
      groupsDN.add(group.getId().toString());
    }
    return groupsDN;
  }

  private Set<String> getDNsOfGroupsContainingMember(final String memberDN) {
    Set<LdapGroup> uniqueMembers = ldapGroupRepository.findAllByUniqueMember(memberDN);
    final Set<String> groupsDN = new HashSet<>();
    for (LdapGroup uniqueMember : uniqueMembers) {
      groupsDN.add(uniqueMember.getId().toString());
    }
    return groupsDN;
  }

  private Set<String> getDNsOfDirectRolesForMember(final String memberDN) {
    Set<LdapRole> ldapRoles = ldapRoleRepository.findAllByRoleOccupants(memberDN);
    final Set<String> rolesDN = new HashSet<>();
    for (LdapRole ldapRole : ldapRoles) {
      rolesDN.add(ldapRole.getDn().toString());
    }
    return rolesDN;
  }

  private String getFirstNameFromDN(final String dn) {
    LdapName ldapName = LdapNameBuilder.newInstance().add(dn).build();
    return getFirstNameFromDN(ldapName);
  }

  private String getFirstNameFromDN(LdapName ldapName) {
    return ldapName.getRdns().get(ldapName.size() - 1).getValue().toString();
  }
}
