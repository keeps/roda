package org.roda.core.security;

import java.nio.file.Path;
import java.util.HashSet;

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
import org.roda.core.model.utils.SpringLdapUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@SpringBootTest(classes = {LdapConfig.class, SpringLdapUtility.class})
public class LdapUtilityTest extends AbstractTestNGSpringContextTests {
  private static final Logger LOGGER = LoggerFactory.getLogger(LdapUtilityTest.class);
  private static Path basePath;

  @Autowired
  SpringLdapUtility springLdapUtility;

  @BeforeClass
  public void setup() throws Exception {
    RodaCoreFactory.instantiateTest(springLdapUtility);
  }

  @Test
  public void listLdapUsers() throws GenericException {
    for (User user : springLdapUtility.getUsers()) {
      LOGGER.info(user.toString());
    }
  }

  @Test
  public void fetchLdapUser() throws GenericException {
    User admin = springLdapUtility.getUser("admin");
    if (admin == null) {
      LOGGER.error("Not found");
    } else {
      LOGGER.info(admin.toString());
    }
  }

  @Test
  public void addUser() throws EmailAlreadyExistsException, UserAlreadyExistsException, GenericException,
    NotFoundException, IllegalOperationException {
    User user = getTestUser();
    springLdapUtility.addUser(user);
    springLdapUtility.setUserPassword(user.getName(), new SecureString("123456".toCharArray()));
  }

  @Test
  public void modifyUser() throws EmailAlreadyExistsException, UserAlreadyExistsException, GenericException,
    NotFoundException, IllegalOperationException {
    User user = getTestUser();
    springLdapUtility.addUser(user);

    user.addGroup("administrators");
    springLdapUtility.modifyUser(user);
  }

  @Test
  public void removeUser()
    throws EmailAlreadyExistsException, UserAlreadyExistsException, GenericException, IllegalOperationException {
    User user = getTestUser();
    springLdapUtility.addUser(user);

    springLdapUtility.removeUser(user.getName());
  }

  @Test
  public void listLdapGroups() throws GenericException {
    for (Group group : springLdapUtility.getGroups()) {
      LOGGER.info(group.toString());
    }
  }

  @Test
  public void fetchLdapGroup() throws GenericException, NotFoundException {
    Group admin = springLdapUtility.getGroup("administrators");
    if (admin == null) {
      LOGGER.error("Not found");
    } else {
      LOGGER.info(admin.toString());
    }
  }

  @Test
  public void addGroup() throws GenericException, GroupAlreadyExistsException {
    Group group = getTestGroup();
    springLdapUtility.addGroup(group);
  }

  @Test
  public void modifyGroup()
    throws GenericException, GroupAlreadyExistsException, NotFoundException, IllegalOperationException {
    Group group = getTestGroup();
    springLdapUtility.addGroup(group);

    group.addMemberUser("guest");
    springLdapUtility.modifyGroup(group);
  }

  @Test
  public void authenticateUser() throws GenericException, AuthenticationDeniedException {
    User authenticatedUser = springLdapUtility.getAuthenticatedUser("testUser", "123456");
    if (authenticatedUser == null) {
      LOGGER.error("Not found");
    } else {
      LOGGER.info(authenticatedUser.toString());
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
    return user;
  }

  private static Group getTestGroup() {
    Group testGroup = new Group("test group");
    testGroup.addMemberUser("admin");

    return testGroup;
  }
}
