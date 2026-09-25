/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.MockedStatic;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.user.requests.UpdateUserRequest;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "travis")
public class MembersServiceTest {
  private static final String USERNAME = "alice";

  private MembersService membersService;
  private ModelService modelService;

  private MockedStatic<RodaCoreFactory> rodaCoreFactory;
  private MockedStatic<PremisV3Utils> premisV3Utils;

  @BeforeMethod
  public void setUp() {
    membersService = new MembersService();
    modelService = mock(ModelService.class);

    rodaCoreFactory = mockStatic(RodaCoreFactory.class);
    rodaCoreFactory.when(RodaCoreFactory::getModelService).thenReturn(modelService);
    rodaCoreFactory.when(RodaCoreFactory::getIndexService).thenReturn(mock(IndexService.class));

    premisV3Utils = mockStatic(PremisV3Utils.class);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() {
    rodaCoreFactory.close();
    premisV3Utils.close();
  }

  private static User storedUserWithTokens() {
    User user = new User(USERNAME, USERNAME, false);
    user.setResetPasswordToken("reset-token");
    user.setResetPasswordTokenExpirationDate("reset-expiration");
    user.setEmailConfirmationToken("email-token");
    user.setEmailConfirmationTokenExpirationDate("email-expiration");
    return user;
  }

  private static void assertStoredTokens(User user) {
    assertEquals(user.getResetPasswordToken(), "reset-token");
    assertEquals(user.getResetPasswordTokenExpirationDate(), "reset-expiration");
    assertEquals(user.getEmailConfirmationToken(), "email-token");
    assertEquals(user.getEmailConfirmationTokenExpirationDate(), "email-expiration");
  }

  @Test
  public void updateUserKeepsStoredTokens() throws Exception {
    when(modelService.retrieveUser(USERNAME)).thenReturn(storedUserWithTokens());
    when(modelService.updateUser(any(User.class), any(), anyBoolean())).then(invocation -> invocation.getArgument(0));

    UpdateUserRequest request = new UpdateUserRequest();
    request.setUser(new User(USERNAME, USERNAME, false));

    assertStoredTokens(membersService.updateUser(request));
  }

  @Test
  public void updateUserOverridesTokensSentByClient() throws Exception {
    when(modelService.retrieveUser(USERNAME)).thenReturn(storedUserWithTokens());
    when(modelService.updateUser(any(User.class), any(), anyBoolean())).then(invocation -> invocation.getArgument(0));

    User sent = new User(USERNAME, USERNAME, false);
    sent.setResetPasswordToken("forged");
    sent.setEmailConfirmationToken("forged");
    UpdateUserRequest request = new UpdateUserRequest();
    request.setUser(sent);

    assertStoredTokens(membersService.updateUser(request));
  }

  @Test
  public void updateMyUserKeepsStoredTokens() throws Exception {
    User authenticated = new User(USERNAME, USERNAME, false);
    when(modelService.retrieveUser(USERNAME)).thenReturn(storedUserWithTokens());
    when(modelService.updateMyUser(any(User.class), any(), eq(true))).then(invocation -> invocation.getArgument(0));

    User result = membersService.updateMyUser(authenticated, new User(USERNAME, USERNAME, false), null, null);

    assertStoredTokens(result);
  }
}
