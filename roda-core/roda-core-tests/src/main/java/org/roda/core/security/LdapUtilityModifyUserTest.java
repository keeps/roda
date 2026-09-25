/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertThrows;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.EmailAlreadyExistsException;
import org.roda.core.data.v2.user.User;
import org.roda.core.model.utils.LdapUtility;
import org.roda.core.repository.ldap.LdapGroupRepository;
import org.roda.core.repository.ldap.LdapRoleRepository;
import org.roda.core.repository.ldap.LdapUserRepository;
import org.springframework.ldap.core.LdapTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = {RodaConstants.TEST_GROUP_ALL, RodaConstants.TEST_GROUP_TRAVIS})
public class LdapUtilityModifyUserTest {
  private static final String SELF = "attacker";
  private static final String OTHER = "victim";
  private static final String OTHER_EMAIL = "victim@example.com";

  private LdapUserRepository ldapUserRepository;
  private LdapUtility ldapUtility;

  @BeforeMethod
  public void setUp() throws Exception {
    ldapUserRepository = mock(LdapUserRepository.class);
    ldapUtility = spy(new LdapUtility(mock(LdapTemplate.class), ldapUserRepository, mock(LdapGroupRepository.class),
      mock(LdapRoleRepository.class)));
    doReturn(new User(OTHER, OTHER, OTHER_EMAIL, false)).when(ldapUtility).getUserWithEmail(OTHER_EMAIL);
  }

  @Test
  public void modifySelfUserWithAnotherUsersEmailAndNameIsRejected() {
    User modified = new User(SELF, OTHER, OTHER_EMAIL, false);

    assertThrows(EmailAlreadyExistsException.class, () -> ldapUtility.modifySelfUser(modified, null));
    verify(ldapUserRepository, never()).save(any());
  }

  @Test
  public void modifyUserWithAnotherUsersEmailAndNameIsRejected() {
    User modified = new User(SELF, OTHER, OTHER_EMAIL, false);

    assertThrows(EmailAlreadyExistsException.class, () -> ldapUtility.modifyUser(modified, true));
    verify(ldapUserRepository, never()).save(any());
  }
}
