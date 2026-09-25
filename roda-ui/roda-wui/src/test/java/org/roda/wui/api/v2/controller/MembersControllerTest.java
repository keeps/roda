/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.v2.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertThrows;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.mockito.MockedStatic;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.JwtUtils;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeyStatus;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.data.v2.accessKey.CreateAccessKeyRequest;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.core.data.v2.user.requests.UpdateUserRequest;
import org.roda.core.model.ModelService;
import org.roda.core.model.utils.UserUtility;
import org.roda.wui.api.v2.exceptions.RESTException;
import org.roda.wui.api.v2.services.MembersService;
import org.roda.wui.common.ControllerAssistantUtils;
import org.roda.wui.common.model.RequestContext;
import org.roda.wui.common.utils.RequestUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.servlet.http.HttpServletRequest;

@Test(groups = "travis")
public class MembersControllerTest {
  private static final String OWNER = "alice";
  private static final String OTHER = "bob";
  private static final String ACCESS_KEY_ID = "key-1";

  private MembersController controller;
  private MembersService membersService;
  private ModelService modelService;
  private RequestContext requestContext;

  private MockedStatic<RequestUtils> requestUtils;
  private MockedStatic<UserUtility> userUtility;
  private MockedStatic<ControllerAssistantUtils> controllerAssistantUtils;
  private MockedStatic<RodaCoreFactory> rodaCoreFactory;

  @BeforeMethod
  public void setUp() throws Exception {
    membersService = mock(MembersService.class);
    modelService = mock(ModelService.class);
    requestContext = new RequestContext();

    controller = new MembersController();
    controller.request = mock(HttpServletRequest.class);
    controller.membersService = membersService;

    requestUtils = mockStatic(RequestUtils.class);
    requestUtils.when(() -> RequestUtils.parseHTTPRequest(any())).thenReturn(requestContext);

    userUtility = mockStatic(UserUtility.class);
    userUtility.when(() -> UserUtility.checkRoles(any(User.class), any(Class.class)))
      .thenThrow(new AuthorizationDeniedException("denied"));

    controllerAssistantUtils = mockStatic(ControllerAssistantUtils.class);

    rodaCoreFactory = mockStatic(RodaCoreFactory.class);
    rodaCoreFactory.when(RodaCoreFactory::getModelService).thenReturn(modelService);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() {
    requestUtils.close();
    userUtility.close();
    controllerAssistantUtils.close();
    rodaCoreFactory.close();
  }

  private void loginAs(String id) {
    requestContext.setUser(new User(id, id, false));
  }

  private void loginAsGuest(String id) {
    requestContext.setUser(new User(id, id, true));
  }

  private void allowRoles() {
    userUtility.when(() -> UserUtility.checkRoles(any(User.class), any(Class.class))).then(invocation -> null);
  }

  private void verifyRolesChecked() {
    userUtility.verify(() -> UserUtility.checkRoles(any(User.class), any(Class.class)));
  }

  private void verifyRolesNotChecked() {
    userUtility.verify(() -> UserUtility.checkRoles(any(User.class), any(Class.class)), never());
  }

  private static User userWithTokens(String id) {
    User user = new User(id, id, false);
    user.setResetPasswordToken("reset-token");
    user.setResetPasswordTokenExpirationDate("reset-expiration");
    user.setEmailConfirmationToken("email-token");
    user.setEmailConfirmationTokenExpirationDate("email-expiration");
    return user;
  }

  private static void assertNoTokens(User user) {
    assertNotNull(user);
    assertNull(user.getResetPasswordToken());
    assertNull(user.getResetPasswordTokenExpirationDate());
    assertNull(user.getEmailConfirmationToken());
    assertNull(user.getEmailConfirmationTokenExpirationDate());
  }

  private static AccessKey accessKey(String owner) {
    AccessKey accessKey = new AccessKey();
    accessKey.setId(ACCESS_KEY_ID);
    accessKey.setUserName(owner);
    accessKey.setKey("secret");
    accessKey.setStatus(AccessKeyStatus.ACTIVE);
    return accessKey;
  }

  private static CreateAccessKeyRequest accessKeyRequest(Date expirationDate) {
    CreateAccessKeyRequest request = new CreateAccessKeyRequest();
    request.setName("my key");
    request.setExpirationDate(expirationDate);
    return request;
  }

  private static Date tomorrow() {
    return new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000L);
  }

  @Test
  public void getMemberOfOwnUserSkipsRoleCheckAndStripsTokens() throws Exception {
    loginAs(OWNER);
    User stored = userWithTokens(OWNER);
    when(membersService.retrieveUser(OWNER)).thenReturn(stored);

    RODAMember member = controller.getMember("user-" + OWNER);

    verifyRolesNotChecked();
    assertNoTokens((User) member);
    assertEquals(stored.getResetPasswordToken(), "reset-token");
  }

  @Test
  public void getMemberOfAnotherUserRequiresRoles() throws Exception {
    loginAs(OWNER);

    assertThrows(RESTException.class, () -> controller.getMember("user-" + OTHER));
    verify(membersService, never()).retrieveUser(anyString());
  }

  @Test
  public void getMemberAsGuestRequiresRoles() throws Exception {
    loginAsGuest(OWNER);

    assertThrows(RESTException.class, () -> controller.getMember("user-" + OWNER));
    verify(membersService, never()).retrieveUser(anyString());
  }

  @Test
  public void getMemberOfGroupRequiresRoles() throws Exception {
    loginAs(OWNER);

    assertThrows(RESTException.class, () -> controller.getMember("group-" + OWNER));
    verify(membersService, never()).retrieveGroup(anyString());
  }

  @Test
  public void getUserStripsTokens() throws Exception {
    loginAs(OWNER);
    allowRoles();
    when(membersService.retrieveUser(OTHER)).thenReturn(userWithTokens(OTHER));

    assertNoTokens(controller.getUser(OTHER));
    verifyRolesChecked();
  }

  @Test
  public void getAuthenticatedUserStripsTokensWithoutChangingSessionUser() {
    User sessionUser = userWithTokens(OWNER);
    userUtility.when(() -> UserUtility.getUser(any(HttpServletRequest.class))).thenReturn(sessionUser);

    User result = controller.getAuthenticatedUser();

    assertNoTokens(result);
    assertEquals(result.getId(), OWNER);
    assertEquals(sessionUser.getEmailConfirmationToken(), "email-token");
  }

  @Test
  public void getGroupMembersStripsTokens() throws Exception {
    loginAs(OWNER);
    allowRoles();
    when(membersService.getGroupMembers("admins")).thenReturn(Set.of(userWithTokens(OWNER), userWithTokens(OTHER)));

    Set<User> members = controller.getGroupMembers("admins");

    assertEquals(members.size(), 2);
    members.forEach(MembersControllerTest::assertNoTokens);
  }

  @Test
  public void getAccessKeysOfOwnUserSkipsRoleCheckAndHidesKeys() throws Exception {
    loginAs(OWNER);
    when(membersService.retrieveUser(OWNER)).thenReturn(new User(OWNER, OWNER, false));
    when(modelService.listAccessKeysByUser(OWNER)).thenReturn(new AccessKeys(List.of(accessKey(OWNER))));

    AccessKeys result = controller.getAccessKeysByUser(OWNER);

    verifyRolesNotChecked();
    assertEquals(result.getObjects().size(), 1);
    assertNull(result.getObjects().get(0).getKey());
  }

  @Test
  public void getAccessKeysOfAnotherUserRequiresRoles() throws Exception {
    loginAs(OWNER);

    assertThrows(RESTException.class, () -> controller.getAccessKeysByUser(OTHER));
    verify(modelService, never()).listAccessKeysByUser(anyString());
  }

  @Test
  public void getOwnAccessKeySkipsRoleCheckAndHidesKey() throws Exception {
    loginAs(OWNER);
    when(modelService.retrieveAccessKey(ACCESS_KEY_ID)).thenReturn(accessKey(OWNER));

    AccessKey result = controller.getAccessKey(ACCESS_KEY_ID);

    verifyRolesNotChecked();
    assertNull(result.getKey());
  }

  @Test
  public void getAccessKeyOfAnotherUserRequiresRoles() throws Exception {
    loginAs(OWNER);
    when(modelService.retrieveAccessKey(ACCESS_KEY_ID)).thenReturn(accessKey(OTHER));

    assertThrows(RESTException.class, () -> controller.getAccessKey(ACCESS_KEY_ID));
  }

  @Test
  public void createOwnAccessKeySkipsRoleCheck() throws Exception {
    loginAs(OWNER);
    AccessKey created = accessKey(OWNER);
    when(modelService.createAccessKey(any(AccessKey.class), eq(OWNER))).thenReturn(created);

    AccessKey result = controller.createAccessKey(OWNER, accessKeyRequest(tomorrow()));

    verifyRolesNotChecked();
    assertSame(result, created);
  }

  @Test
  public void createAccessKeyForAnotherUserRequiresRoles() throws Exception {
    loginAs(OWNER);

    assertThrows(RESTException.class, () -> controller.createAccessKey(OTHER, accessKeyRequest(tomorrow())));
    verify(modelService, never()).createAccessKey(any(AccessKey.class), anyString());
  }

  @Test
  public void createAccessKeyAsGuestRequiresRoles() throws Exception {
    loginAsGuest(OWNER);

    assertThrows(RESTException.class, () -> controller.createAccessKey(OWNER, accessKeyRequest(tomorrow())));
    verify(modelService, never()).createAccessKey(any(AccessKey.class), anyString());
  }

  @Test
  public void createOwnAccessKeyStillValidatesExpirationDate() throws Exception {
    loginAs(OWNER);

    assertThrows(RESTException.class, () -> controller.createAccessKey(OWNER, accessKeyRequest(new Date(0))));
    verify(modelService, never()).createAccessKey(any(AccessKey.class), anyString());
  }

  @Test
  public void deleteOwnAccessKeySkipsRoleCheck() throws Exception {
    loginAs(OWNER);
    when(modelService.retrieveAccessKey(ACCESS_KEY_ID)).thenReturn(accessKey(OWNER));

    controller.deleteAccessKey(ACCESS_KEY_ID);

    verifyRolesNotChecked();
    verify(modelService).deleteAccessKey(ACCESS_KEY_ID);
  }

  @Test
  public void deleteAccessKeyOfAnotherUserRequiresRoles() throws Exception {
    loginAs(OWNER);
    when(modelService.retrieveAccessKey(ACCESS_KEY_ID)).thenReturn(accessKey(OTHER));

    assertThrows(RESTException.class, () -> controller.deleteAccessKey(ACCESS_KEY_ID));
    verify(modelService, never()).deleteAccessKey(anyString());
  }

  @Test
  public void deleteUnknownAccessKeyRequiresRoles() throws Exception {
    loginAs(OWNER);
    when(modelService.retrieveAccessKey(ACCESS_KEY_ID)).thenThrow(new NotFoundException("not found"));

    assertThrows(RESTException.class, () -> controller.deleteAccessKey(ACCESS_KEY_ID));
    verifyRolesChecked();
    verify(modelService, never()).deleteAccessKey(anyString());
  }

  @Test
  public void deleteAccessKeyAsGuestRequiresRoles() throws Exception {
    loginAsGuest(OWNER);
    when(modelService.retrieveAccessKey(ACCESS_KEY_ID)).thenReturn(accessKey(OWNER));

    assertThrows(RESTException.class, () -> controller.deleteAccessKey(ACCESS_KEY_ID));
    verify(modelService, never()).deleteAccessKey(anyString());
  }

  @Test
  public void revokeOwnAccessKeySkipsRoleCheck() throws Exception {
    loginAs(OWNER);
    when(modelService.retrieveAccessKey(ACCESS_KEY_ID)).thenReturn(accessKey(OWNER));
    when(modelService.updateAccessKey(any(AccessKey.class), eq(OWNER))).then(invocation -> invocation.getArgument(0));

    AccessKey result = controller.revokeAccessKey(ACCESS_KEY_ID);

    verifyRolesNotChecked();
    assertEquals(result.getStatus(), AccessKeyStatus.REVOKED);
  }

  @Test
  public void revokeAccessKeyOfAnotherUserRequiresRoles() throws Exception {
    loginAs(OWNER);
    when(modelService.retrieveAccessKey(ACCESS_KEY_ID)).thenReturn(accessKey(OTHER));

    assertThrows(RESTException.class, () -> controller.revokeAccessKey(ACCESS_KEY_ID));
    verify(modelService, never()).updateAccessKey(any(AccessKey.class), anyString());
  }

  @Test
  public void regenerateOwnAccessKeySkipsRoleCheck() throws Exception {
    loginAs(OWNER);
    Date expirationDate = tomorrow();
    when(modelService.retrieveAccessKey(ACCESS_KEY_ID)).thenReturn(accessKey(OWNER));
    when(modelService.updateAccessKey(any(AccessKey.class), eq(OWNER))).then(invocation -> invocation.getArgument(0));

    try (MockedStatic<JwtUtils> jwtUtils = mockStatic(JwtUtils.class)) {
      jwtUtils.when(() -> JwtUtils.generateToken(OWNER, expirationDate)).thenReturn("new-token");

      AccessKey result = controller.regenerateAccessKey(ACCESS_KEY_ID, accessKeyRequest(expirationDate));

      verifyRolesNotChecked();
      assertEquals(result.getKey(), "new-token");
    }
  }

  @Test
  public void regenerateAccessKeyOfAnotherUserRequiresRoles() throws Exception {
    loginAs(OWNER);
    when(modelService.retrieveAccessKey(ACCESS_KEY_ID)).thenReturn(accessKey(OTHER));

    assertThrows(RESTException.class, () -> controller.regenerateAccessKey(ACCESS_KEY_ID, accessKeyRequest(tomorrow())));
    verify(modelService, never()).updateAccessKey(any(AccessKey.class), anyString());
  }

  @Test
  public void getOwnUserGroupsSkipsRoleCheck() throws Exception {
    loginAs(OWNER);
    Set<Group> groups = Collections.singleton(new Group("users"));
    when(membersService.getGroupFromUser(OWNER)).thenReturn(groups);

    assertEquals(controller.getUserGroups(OWNER), groups);
    verifyRolesNotChecked();
  }

  @Test
  public void getUserGroupsOfAnotherUserRequiresRoles() throws Exception {
    loginAs(OWNER);

    assertThrows(RESTException.class, () -> controller.getUserGroups(OTHER));
    verify(membersService, never()).getGroupFromUser(anyString());
  }

  @Test
  public void updateMyUserSkipsRoleCheckAndStripsTokens() throws Exception {
    loginAs(OWNER);
    UpdateUserRequest request = new UpdateUserRequest();
    request.setUser(new User(OWNER, OWNER, false));
    when(membersService.updateMyUser(any(User.class), any(User.class), any(), any()))
      .thenReturn(userWithTokens(OWNER));

    User result = controller.updateMyUser(request);

    verifyRolesNotChecked();
    assertNoTokens(result);
  }

  @Test
  public void updateMyUserForAnotherUserRequiresRoles() throws Exception {
    loginAs(OWNER);
    UpdateUserRequest request = new UpdateUserRequest();
    request.setUser(new User(OTHER, OTHER, false));

    assertThrows(RESTException.class, () -> controller.updateMyUser(request));
    verify(membersService, never()).updateMyUser(any(User.class), any(User.class), any(), any());
  }

  @Test
  public void updateMyUserWithoutUserRequiresRoles() throws Exception {
    loginAs(OWNER);

    assertThrows(RESTException.class, () -> controller.updateMyUser(new UpdateUserRequest()));
    verify(membersService, never()).updateMyUser(any(), any(), any(), any());
  }
}
