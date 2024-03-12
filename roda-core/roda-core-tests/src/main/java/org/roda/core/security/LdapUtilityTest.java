package org.roda.core.security;

import java.nio.file.Path;

import org.roda.core.config.LdapConfig;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
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
  public void setup() {
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
  public void authenticateUser() throws GenericException, NotFoundException, AuthenticationDeniedException {
    User authenticatedUser = springLdapUtility.getAuthenticatedUser("admin", "roda");
    if (authenticatedUser == null) {
      LOGGER.error("Not found");
    } else {
      LOGGER.info(authenticatedUser.toString());
    }
  }
}
