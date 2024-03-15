package org.roda.core.model.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapName;

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
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.LdapAttributes;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.ldif.parser.LdifParser;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class SpringLdapUtility implements LdapUtility {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringLdapUtility.class);

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

  private static final LdapName ldapRootDN = LdapUtils.newLdapName("dc=roda,dc=org");
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

  @Override
  public void setup(final String rodaGuestDN, final String rodaAdminDN) {
    this.rodaGuestDN = rodaGuestDN;
    this.rodaAdminDN = rodaAdminDN;
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
        LdapName dn = LdapUtils.removeFirst(record.getName(), ldapRootDN);
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
    String memberDN = LdapNameBuilder.newInstance(ldapRootDN).add(ldapUser.getDn()).build().toString();

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

    return user;
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
    LdapName dName = LdapUtils.prepend(LdapUtils.removeFirst(ldapUser.getDn(), ldapContextSource.getBaseLdapName()),
      ldapContextSource.getBaseLdapName());

    DirContext ctx = null;
    try {
      ctx = ldapContextSource.getContext(dName.toString(), password);
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
    LdapName roleDN = LdapNameBuilder.newInstance().add(ldapRolesOU).add(CN, roleName).build();
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
    LdapName userDN = LdapNameBuilder.newInstance().add(UID, user.getId()).build();
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
        getDNsOfAllRolesForMember(LdapNameBuilder.newInstance(ldapRootDN).add(memberGroupDN).build().toString()));
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
