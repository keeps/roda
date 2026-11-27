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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;

import java.io.ByteArrayInputStream;
import java.util.Locale;

import org.apache.commons.configuration2.Configuration;
import org.mockito.ArgumentCaptor;

import org.mockito.MockedStatic;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.Messages;
import org.roda.core.common.PremisV3Utils;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.IllegalOperationException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.user.requests.UpdateUserRequest;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;
import org.roda.wui.common.ControllerAssistantUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "travis")
public class MembersServiceTest {
  private static final String USERNAME = "alice";
  private static final String OTHER_USERNAME = "bob";

  private MembersService membersService;
  private ModelService modelService;

  private MockedStatic<RodaCoreFactory> rodaCoreFactory;
  private MockedStatic<PremisV3Utils> premisV3Utils;
  private MockedStatic<ControllerAssistantUtils> controllerAssistantUtils;

  @BeforeMethod
  public void setUp() {
    membersService = new MembersService();
    modelService = mock(ModelService.class);

    rodaCoreFactory = mockStatic(RodaCoreFactory.class);
    rodaCoreFactory.when(RodaCoreFactory::getModelService).thenReturn(modelService);
    rodaCoreFactory.when(RodaCoreFactory::getIndexService).thenReturn(mock(IndexService.class));

    premisV3Utils = mockStatic(PremisV3Utils.class);
    controllerAssistantUtils = mockStatic(ControllerAssistantUtils.class);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() {
    rodaCoreFactory.close();
    premisV3Utils.close();
    controllerAssistantUtils.close();
  }

  private static User storedUserWithTokens() {
    User user = new User(USERNAME, USERNAME, false);
    user.setResetPasswordToken("reset-token");
    user.setResetPasswordTokenExpirationDate("reset-expiration");
    user.setEmailConfirmationToken("email-token");
    user.setEmailConfirmationTokenExpirationDate("email-expiration");
    return user;
  }

  private static User otherUserWithTokens() {
    User user = new User(OTHER_USERNAME, OTHER_USERNAME, false);
    user.setResetPasswordToken("other-reset-token");
    user.setResetPasswordTokenExpirationDate("other-reset-expiration");
    user.setEmailConfirmationToken("other-email-token");
    user.setEmailConfirmationTokenExpirationDate("other-email-expiration");
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

    User result = membersService.updateMyUser(authenticated, new User(USERNAME, USERNAME, false), null, null, null);

    assertStoredTokens(result);
  }

  @Test
  public void updateMyUserIgnoresNameFromRequest() throws Exception {
    User authenticated = new User(USERNAME, USERNAME, false);
    when(modelService.retrieveUser(USERNAME)).thenReturn(storedUserWithTokens());
    when(modelService.updateMyUser(any(User.class), any(), eq(true))).then(invocation -> invocation.getArgument(0));

    User sent = new User(USERNAME, OTHER_USERNAME, false);
    User result = membersService.updateMyUser(authenticated, sent, null, null, null);

    verify(modelService, never()).retrieveUser(OTHER_USERNAME);
    ArgumentCaptor<User> updated = ArgumentCaptor.forClass(User.class);
    verify(modelService).updateMyUser(updated.capture(), any(), eq(true));
    assertEquals(updated.getValue().getId(), USERNAME);
    assertEquals(updated.getValue().getName(), USERNAME);
    assertEquals(result.getName(), USERNAME);
    assertStoredTokens(result);
  }

  @Test
  public void updateMyUserForAnotherIdIsRejected() throws Exception {
    User authenticated = new User(USERNAME, USERNAME, false);
    User sent = new User(OTHER_USERNAME, OTHER_USERNAME, false);

    assertThrows(IllegalOperationException.class,
      () -> membersService.updateMyUser(authenticated, sent, null, null, null));
    verify(modelService, never()).retrieveUser(anyString());
    verify(modelService, never()).updateMyUser(any(User.class), any(), anyBoolean());
  }

  @Test
  public void updateMyUserNeverTakesTokensFromAnotherUser() throws Exception {
    User authenticated = new User(USERNAME, USERNAME, false);
    when(modelService.retrieveUser(USERNAME)).thenReturn(storedUserWithTokens());
    when(modelService.retrieveUser(OTHER_USERNAME)).thenReturn(otherUserWithTokens());
    when(modelService.updateMyUser(any(User.class), any(), eq(true))).then(invocation -> invocation.getArgument(0));

    User result = membersService.updateMyUser(authenticated, new User(USERNAME, OTHER_USERNAME, false), null, null,
      null);

    assertStoredTokens(result);
  }

  @Test
  public void updateUserTakesTokensFromUserWithSameId() throws Exception {
    when(modelService.retrieveUser(USERNAME)).thenReturn(storedUserWithTokens());
    when(modelService.retrieveUser(OTHER_USERNAME)).thenReturn(otherUserWithTokens());
    when(modelService.updateUser(any(User.class), any(), anyBoolean())).then(invocation -> invocation.getArgument(0));

    UpdateUserRequest request = new UpdateUserRequest();
    request.setUser(new User(USERNAME, OTHER_USERNAME, false));

    assertStoredTokens(membersService.updateUser(request));
    verify(modelService, never()).retrieveUser(OTHER_USERNAME);
  }

  @Test
  public void sendEmailVerificationStoresNewlyGeneratedToken() throws Exception {
    when(modelService.retrieveUser(USERNAME)).thenAnswer(invocation -> {
      User stored = storedUserWithTokens();
      stored.setActive(false);
      stored.setEmail("alice@example.com");
      return stored;
    });
    when(modelService.updateUser(any(User.class), any(), anyBoolean())).then(invocation -> invocation.getArgument(0));
    rodaCoreFactory.when(() -> RodaCoreFactory.getConfigurationFileAsStream(anyString()))
      .thenAnswer(invocation -> new ByteArrayInputStream(new byte[0]));
    rodaCoreFactory.when(() -> RodaCoreFactory.getI18NMessages(any(Locale.class))).thenReturn(mock(Messages.class));

    membersService.sendEmailVerification("http://localhost", USERNAME, true, "127.0.0.1", "en");

    ArgumentCaptor<User> updated = ArgumentCaptor.forClass(User.class);
    verify(modelService).updateUser(updated.capture(), any(), eq(true));
    assertNotNull(updated.getValue().getEmailConfirmationToken());
    assertNotEquals(updated.getValue().getEmailConfirmationToken(), "email-token");
    assertNotEquals(updated.getValue().getEmailConfirmationTokenExpirationDate(), "email-expiration");
  }

  private void casEnabled(boolean enabled) {
    Configuration configuration = mock(Configuration.class);
    when(configuration.getBoolean(RodaConstants.UI_FILTER_CAS_ENABLED, false)).thenReturn(enabled);
    rodaCoreFactory.when(RodaCoreFactory::getRodaConfiguration).thenReturn(configuration);
  }

  private void allowUpdateMyUser() throws Exception {
    when(modelService.retrieveUser(USERNAME)).thenReturn(storedUserWithTokens());
    when(modelService.updateMyUser(any(User.class), any(), eq(true))).then(invocation -> invocation.getArgument(0));
  }

  @Test
  public void updateMyUserWithPasswordIsRejectedWhenCasIsEnabled() throws Exception {
    casEnabled(true);
    User authenticated = new User(USERNAME, USERNAME, false);
    SecureString password = new SecureString("Local#2026".toCharArray());

    assertThrows(IllegalOperationException.class,
      () -> membersService.updateMyUser(authenticated, new User(USERNAME, USERNAME, false), password, null, null));
    verify(modelService, never()).updateMyUser(any(User.class), any(), anyBoolean());
  }

  @Test
  public void updateMyUserWithoutPasswordIsAllowedWhenCasIsEnabled() throws Exception {
    casEnabled(true);
    allowUpdateMyUser();
    User authenticated = new User(USERNAME, USERNAME, false);

    membersService.updateMyUser(authenticated, new User(USERNAME, USERNAME, false), null, null, null);
    membersService.updateMyUser(authenticated, new User(USERNAME, USERNAME, false), new SecureString(), null, null);

    verify(modelService, times(2)).updateMyUser(any(User.class), any(), eq(true));
  }

  @Test
  public void updateMyUserWithPasswordIsAllowedWhenCasIsDisabled() throws Exception {
    casEnabled(false);
    allowUpdateMyUser();
    User authenticated = new User(USERNAME, USERNAME, false);
    SecureString password = new SecureString("Local#2026".toCharArray());
    SecureString currentPassword = new SecureString("Current#2026".toCharArray());

    membersService.updateMyUser(authenticated, new User(USERNAME, USERNAME, false), password, currentPassword, null);

    verify(modelService).retrieveAuthenticatedUser(USERNAME, "Current#2026");
    verify(modelService).updateMyUser(any(User.class), eq(password), eq(true));
  }

  @Test
  public void updateMyUserWithPasswordAndWithoutCurrentPasswordIsRejected() throws Exception {
    casEnabled(false);
    allowUpdateMyUser();
    User authenticated = new User(USERNAME, USERNAME, false);
    SecureString password = new SecureString("Local#2026".toCharArray());

    assertThrows(RequestNotValidException.class,
      () -> membersService.updateMyUser(authenticated, new User(USERNAME, USERNAME, false), password, null, null));
    assertThrows(RequestNotValidException.class, () -> membersService.updateMyUser(authenticated,
      new User(USERNAME, USERNAME, false), password, new SecureString(), null));

    verify(modelService, never()).updateMyUser(any(User.class), any(), anyBoolean());
  }

  @Test
  public void updateMyUserWithPasswordAndWrongCurrentPasswordIsRejected() throws Exception {
    casEnabled(false);
    allowUpdateMyUser();
    when(modelService.retrieveAuthenticatedUser(USERNAME, "Wrong#2026"))
      .thenThrow(new AuthenticationDeniedException("denied"));
    User authenticated = new User(USERNAME, USERNAME, false);
    SecureString password = new SecureString("Local#2026".toCharArray());
    SecureString currentPassword = new SecureString("Wrong#2026".toCharArray());

    assertThrows(AuthorizationDeniedException.class, () -> membersService.updateMyUser(authenticated,
      new User(USERNAME, USERNAME, false), password, currentPassword, null));

    verify(modelService, never()).updateMyUser(any(User.class), any(), anyBoolean());
  }

  @Test
  public void updateMyUserWithoutPasswordDoesNotCheckCurrentPassword() throws Exception {
    casEnabled(false);
    allowUpdateMyUser();
    User authenticated = new User(USERNAME, USERNAME, false);

    membersService.updateMyUser(authenticated, new User(USERNAME, USERNAME, false), null, null, null);
    membersService.updateMyUser(authenticated, new User(USERNAME, USERNAME, false), new SecureString(), null, null);

    verify(modelService, never()).retrieveAuthenticatedUser(anyString(), anyString());
    verify(modelService, times(2)).updateMyUser(any(User.class), any(), eq(true));
  }
}
