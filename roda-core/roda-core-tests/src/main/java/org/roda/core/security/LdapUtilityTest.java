package org.roda.core.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.config.LdapConfig;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.ApacheLdapUtility;
import org.roda.core.model.utils.SpringLdapUtility;
import org.roda.core.model.utils.UserUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
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
  public void bootstrapLDAP() throws Exception {
    final List<String> ldifFileNames = Arrays.asList("users.ldif", "groups.ldif", "roles.ldif");

    springLdapUtility.initDirectoryService(ldifFileNames);
    springLdapUtility.setRODAAdministratorsDN("cn=administrators,ou=groups,dc=roda,dc=org");
    springLdapUtility.addRole("aip.read");
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

  @Test
  public void apacheDS() throws Exception {
    RodaCoreFactory.instantiateTest(false, true, false, false, false, false);
    final int ldapPort = RodaConstants.CORE_LDAP_DEFAULT_PORT;
    final String ldapBaseDN = "dc=roda,dc=org";
    final String ldapPeopleDN = "ou=users,dc=roda,dc=org";
    final String ldapGroupsDN = "ou=groups,dc=roda,dc=org";
    final String ldapRolesDN = "ou=roles,dc=roda,dc=org";
    final String ldapAdminDN = "uid=admin,ou=system";
    final String ldapAdminPassword = "roda";
    final String ldapPasswordDigestAlgorithm = "PKCS5S2";
    final List<String> ldapProtectedUsers = new ArrayList<>();
    final List<String> ldapProtectedGroups = new ArrayList<>();
    final String rodaGuestDN = "uid=guest,ou=users,dc=roda,dc=org";
    final String rodaAdminDN = "uid=admin,ou=users,dc=roda,dc=org";
    final String rodaAdministratorsDN = "cn=administrators,ou=groups,dc=roda,dc=org";

//    ApacheLdapUtility apacheLdapUtility = new ApacheLdapUtility(true, ldapPort, ldapBaseDN, ldapPeopleDN, ldapGroupsDN,
//      ldapRolesDN, ldapAdminDN, ldapAdminPassword, ldapPasswordDigestAlgorithm, ldapProtectedUsers, ldapProtectedGroups,
//      rodaGuestDN, rodaAdminDN, Paths.get("/tmp/ldap"));
//
//    apacheLdapUtility.initDirectoryService();

    User admin = UserUtility.getLdapUtility().getUser("admin");
    System.out.println(admin.toString());

  }
}
