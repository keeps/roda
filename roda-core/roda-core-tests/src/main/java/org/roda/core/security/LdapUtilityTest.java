package org.roda.core.security;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
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

  private LdapUtility ldapUtility;
  private LdapUtilityTestHelper ldapUtilityTestHelper;

  @BeforeClass
  public void setup() throws Exception {
    ldapUtilityTestHelper = new LdapUtilityTestHelper();
    ldapUtility = ldapUtilityTestHelper.getLdapUtility();
    RodaCoreFactory.instantiateTest(false, true, false, false, false, false, false, ldapUtility);
  }

  @AfterClass
  public void tearDown() throws Exception {
    ldapUtilityTestHelper.shutdown();
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
  public void removeGroup() throws GenericException, IllegalOperationException {
    Group group = addTestGroup();

    ldapUtility.removeGroup(group.getName());
    try {
      ldapUtility.getGroup(group.getName());
      Assert.fail("should have not found exception");
    } catch (NotFoundException e) {
      // expected
    }
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
