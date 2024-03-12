package org.roda.core.security;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashSet;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.config.LdapConfig;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.GroupAlreadyExistsException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.UserAlreadyExistsException;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.LdapUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@SpringBootTest(classes = {LdapConfig.class, LdapUtility.class})
public class LdapUtilityTest extends AbstractTestNGSpringContextTests {
  private static final Logger LOGGER = LoggerFactory.getLogger(LdapUtilityTest.class);

  @Autowired
  LdapUtility ldapUtility;

  private GenericContainer<?> openldap;

  @BeforeClass
  public void setup() throws Exception {
    RodaCoreFactory.instantiateTest(false, false, false, false, false, false, false);
    RodaCoreFactory.addConfiguration("roda-test.properties");
    openldap = createLdapContainer();
    openldap.start();

    RodaCoreFactory.getRodaConfiguration().setProperty("core.ldap.port", openldap.getMappedPort(1389));
    ldapUtility.initialize();
  }

  private GenericContainer<?> createLdapContainer() {
    final String ldapBaseDN = RodaCoreFactory.getRodaConfiguration().getString("core.ldap.baseDN", "dc=roda,dc=org");
    DockerImageName OPENLDAP_IMAGE = DockerImageName.parse("docker.io/bitnami/openldap:2.6");

    return new GenericContainer<>(OPENLDAP_IMAGE).withExposedPorts(1389).withEnv("BITNAMI_DEBUG", "true")
      .withEnv("LDAP_ROOT", ldapBaseDN).withEnv("LDAP_SKIP_DEFAULT_TREE", "yes").withEnv("LDAP_ADMIN_USERNAME", "admin")
      .withEnv("LDAP_ADMIN_PASSWORD", "roda").withEnv("LDAP_EXTRA_SCHEMAS", "cosine,inetorgperson,nis,pbkdf2")
      .withCopyFileToContainer(MountableFile.forClasspathResource("/config/ldap/schema/pbkdf2.ldif"),
        "/opt/bitnami/openldap/etc/schema/pbkdf2.ldif")
      .waitingFor(Wait.forLogMessage(".* Starting slapd .*", 1));
  }

  @AfterClass
  public void tearDown() throws Exception {
    openldap.stop();
    RodaCoreFactory.shutdown();
  }

  @Test
  public void listLdapUsers() throws GenericException {
    List<User> users = ldapUtility.getUsers();
    assertEquals(2, users.size());
  }

  @Test
  public void addUser() throws GenericException, NotFoundException, IllegalOperationException {
    User user = addTestUser();
    ldapUtility.setUserPassword(user.getName(), new SecureString("123456".toCharArray()));

    User retrieveUser = ldapUtility.getUser(user.getName());

    assertEquals(user, retrieveUser);
    removeTestUser();
  }

  @Test
  public void modifyUser()
    throws EmailAlreadyExistsException, GenericException, NotFoundException, IllegalOperationException {
    User user = addTestUser();

    user.setFullName("Test user full name changed");
    ldapUtility.modifyUser(user);

    User retrieveUser = ldapUtility.getUser(user.getName());

    assertEquals(user, retrieveUser);
    removeTestUser();
  }

  @Test
  public void removeUser() throws GenericException, IllegalOperationException {
    User user = addTestUser();

    ldapUtility.removeUser(user.getName());
    User retrieveUser = ldapUtility.getUser(user.getName());

    assertEquals(null, retrieveUser.getName());
  }

  @Test
  public void authenticateUser()
    throws GenericException, NotFoundException, IllegalOperationException, AuthenticationDeniedException {
    User user = addTestUser();
    SecureString pass = new SecureString("123456".toCharArray());
    ldapUtility.setUserPassword(user.getName(), pass);

    User authenticatedUser = ldapUtility.getAuthenticatedUser(user.getName(), pass.toString());
    assertNotNull(authenticatedUser);
    removeTestUser();
  }

  @Test
  public void modifyUserWithSamePassword() throws EmailAlreadyExistsException, GenericException, NotFoundException,
    IllegalOperationException, AuthenticationDeniedException {
    User user = addTestUser();
    SecureString pass = new SecureString("123456".toCharArray());
    ldapUtility.setUserPassword(user.getName(), pass);
    user.setFullName("Other user");
    ldapUtility.modifyUser(user);

    User authenticatedUser = ldapUtility.getAuthenticatedUser(user.getName(), pass.toString());
    assertNotNull(authenticatedUser);
    removeTestUser();
  }

  @Test
  public void listLdapGroups() throws GenericException {
    List<Group> groups = ldapUtility.getGroups();
    assertEquals(3, groups.size());
  }

  @Test
  public void addGroup() throws GenericException, NotFoundException {
    Group group = addTestGroup();

    Group retrieveGroup = ldapUtility.getGroup(group.getName());
    assertEquals(group, retrieveGroup);
    removeTestGroup();
  }

  @Test
  public void modifyGroup() throws GenericException, NotFoundException, IllegalOperationException {
    Group group = addTestGroup();
    group.addMemberUser("admin");

    ldapUtility.modifyGroup(group);
    Group retrieveGroup = ldapUtility.getGroup(group.getName());
    assertEquals(retrieveGroup, group);
    removeTestGroup();
  }

  @Test
  public void removeGroup() throws GenericException, IllegalOperationException, NotFoundException {
    Group group = addTestGroup();

    ldapUtility.removeGroup(group.getName());
    Group retrieveGroup = ldapUtility.getGroup(group.getName());

    assertNull(retrieveGroup);
  }

  @Test
  public void resetAdminAccess() throws GenericException, AuthenticationDeniedException {
    SecureString pass = new SecureString("123456".toCharArray());
    ldapUtility.resetAdminAccess(pass);

    User admin = ldapUtility.getAuthenticatedUser("admin", pass.toString());
    Assert.assertNotNull(admin);
  }

  @Test
  public void isInternal() throws GenericException, NotFoundException {
    User user = addTestUser();
    assertTrue(ldapUtility.isInternal(user.getId()));

    Assert.assertThrows(NotFoundException.class, () -> {
      ldapUtility.isInternal("externalUser");
    });

    removeTestUser();
  }

  private User addTestUser() throws GenericException {
    User user = getTestUser();
    try {
      ldapUtility.addUser(user);
    } catch (UserAlreadyExistsException | EmailAlreadyExistsException e) {
      // do nothing
    }

    return user;
  }

  private void removeTestUser() throws GenericException {
    User user = getTestUser();
    try {
      ldapUtility.removeUser(user.getName());
    } catch (IllegalOperationException e) {
      // do nothing
    }
  }

  private static User getTestUser() {
    User user = new User("testUser", "testUser", "test@roda-org", false);
    HashSet<String> groups = new HashSet<>();
    groups.add("users");

    HashSet<String> roles = new HashSet<>();
    roles.add("access_key.manage");

    user.setGroups(groups);
    user.setDirectRoles(roles);
    user.setAllRoles(roles);
    return user;
  }

  private Group addTestGroup() throws GenericException {
    Group group = getTestGroup();

    HashSet<String> roles = new HashSet<>();
    roles.add("access_key.manage");
    group.setDirectRoles(roles);
    group.setAllRoles(roles);
    try {
      ldapUtility.addGroup(group);
    } catch (GroupAlreadyExistsException e) {
      // do nothing
    }

    return group;
  }

  private void removeTestGroup() throws GenericException {
    Group group = getTestGroup();
    try {
      ldapUtility.removeGroup(group.getName());
    } catch (IllegalOperationException e) {
      // do nothing
    }
  }

  private static Group getTestGroup() {
    Group testGroup = new Group("test group");

    return testGroup;
  }
}
