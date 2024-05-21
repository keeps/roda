package org.roda.wui.client.services;

import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.data.v2.accessToken.AccessToken;
import org.roda.core.data.v2.generics.CreateUserRequest;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author António Lindo <alindo@keep.pt>
 */
@Tag(name = "v2 members")
@RequestMapping(path = "../api/v2/members")
public interface MembersRestService extends RODAEntityRestService<RODAMember> {

  @RequestMapping(path = "/users/{" + RodaConstants.API_PATH_PARAM_NAME
    + "}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get user", description = "Gets a particular user", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = User.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  User getUser(
    @Parameter(description = "The user name") @PathVariable(name = RodaConstants.API_PATH_PARAM_NAME) String name);

  @RequestMapping(path = "/users/authenticated/{" + RodaConstants.API_PATH_PARAM_NAME
    + "}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get authenticated user", description = "Gets the authenticated user", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = User.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  User getAuthenticatedUser();

  @RequestMapping(path = "/users/{" + RodaConstants.API_PATH_PARAM_NAME
    + "}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete user", description = "Deletes an existing user", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteUser(
    @Parameter(description = "The user name") @PathVariable(name = RodaConstants.API_PATH_PARAM_NAME) String name);

  @RequestMapping(path = "/groups/{" + RodaConstants.API_PATH_PARAM_NAME
    + "}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get group", description = "Gets a particular group", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = User.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Group getGroup(
    @Parameter(description = "The group name") @PathVariable(name = RodaConstants.API_PATH_PARAM_NAME) String name);

  @RequestMapping(path = "/groups/{" + RodaConstants.API_PATH_PARAM_NAME
    + "}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete group", description = "Deletes an existing group", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteGroup(
    @Parameter(description = "The group name") @PathVariable(name = RodaConstants.API_PATH_PARAM_NAME) String name);

  @RequestMapping(path = "/change-active", method = RequestMethod.POST)
  @Operation(summary = "Activate a RODA member", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), description = "Activates RODA members", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void changeActive(
    @Parameter(name = "selectedItems", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<RODAMember> members,
    @Parameter(description = "Is Active") @RequestParam(name = "active") Boolean active);

  @RequestMapping(path = "/delete", method = RequestMethod.POST)
  @Operation(summary = "Delete multiple RODA members via search query", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), description = "Deletes one or more RODA members", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteMultipleMembers(
    @Parameter(name = "selectedItems", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<RODAMember> members);

  @RequestMapping(path = "/users/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create user", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = User.class))), description = "Creates a new user", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = User.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  User createUser(
    @Parameter(name = "user", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) CreateUserRequest userOperations,
    @Parameter(description = "localeString") @RequestParam(name = "locale") String localeString);

  @RequestMapping(path = "/users/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Register user", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = User.class))), description = "Registers a new user", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = User.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  User registerUser(
    @Parameter(name = "user", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) CreateUserRequest userOperations,
    @Parameter(description = "localeString") @RequestParam(name = "locale") String localeString,
    @Parameter(description = "captcha") @RequestParam(required = false, name = "captcha") String captcha);

  @RequestMapping(path = "/groups/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create group", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Group.class))), description = "Creates a new group", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Group.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Group createGroup(
    @Parameter(name = "group", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) Group group);

  @RequestMapping(path = "/groups/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update group", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Group.class))), description = "Updates a group", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void updateGroup(
    @Parameter(name = "group", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) Group modifiedGroup);

  @RequestMapping(path = "/users", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update user", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateUserRequest.class))), description = "Updates a user", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = User.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  User updateUser(
    @Parameter(name = "user", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) CreateUserRequest userOperations);

  @RequestMapping(path = "/users/update-my-user", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update my user", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateUserRequest.class))), description = "Updates my user", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = User.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  User updateMyUser(
    @Parameter(name = "user", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) CreateUserRequest userOperations);

  @RequestMapping(path = "/users/reset-password", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Reset user password", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SecureString.class))), description = "Resets a user password", responses = {
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void resetUserPassword(
    @Parameter(description = "username") @RequestParam(name = "username") String username,
    @Parameter(description = "token") @RequestParam(name = "token") String resetPasswordToken,
    @Parameter(name = "password", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SecureString password);

  @RequestMapping(path = "/users/recover", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Recover login", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = User.class))), description = "Registers a new user", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void recoverLogin(
    @Parameter(description = "usernameOrEmail") @RequestParam(name = "usernameOrEmail") String usernameOrEmail,
    @Parameter(description = "localeString") @RequestParam(name = "locale") String localeString,
    @Parameter(description = "captcha") @RequestParam(required = false, name = "captcha") String captcha);

  @RequestMapping(path = "/users/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Confirm user email", description = "Confirms a user email", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void confirmUserEmail(
    @Parameter(description = "username") @RequestParam(name = "username") String username,
    @Parameter(description = "token") @RequestParam(required = false, name = "token") String token);

  @RequestMapping(path = "/users/extra", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get user extra", description = "Gets user extra", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Set.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Set<MetadataValue> getUserExtra(
    @Parameter(description = "username") @RequestParam(name = "username") String username);

  @RequestMapping(path = "/users/extra/default", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get default user extra", description = "Gets default user extra", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Set.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Set<MetadataValue> getDefaultUserExtra();

  @RequestMapping(path = "/users/send-verification", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Send verification email", description = "Sends verification email", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Notification.class)))})
  Notification sendEmailVerification(
    @Parameter(description = "Username") @RequestParam(name = "username") String username,
    @Parameter(description = "Generate new token") @RequestParam(required = false, name = "generate-new-token", defaultValue = "false") boolean generateNewToken,
    @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/users/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Login", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SecureString.class))), description = "Logs in a user", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = User.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  User login(
    @Parameter(description = "username") @RequestParam(name = "username") String username,
    @Parameter(name = "password", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SecureString password) throws AuthenticationDeniedException;

  @RequestMapping(path = "/users/accesskey/{" + RodaConstants.API_PATH_PARAM_NAME
    + "}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete user access key", description = "Deletes user access keys", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteUserAccessKeys(
    @Parameter(description = "The user name") @PathVariable(name = RodaConstants.API_PATH_PARAM_NAME) String name);

  @RequestMapping(path = "/users/accesskey/deactivate/{" + RodaConstants.API_PATH_PARAM_NAME
    + "}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Deactivate user access key", description = "Deactivates user access keys", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deactivateUserAccessKeys(
    @Parameter(description = "The user name") @PathVariable(name = RodaConstants.API_PATH_PARAM_NAME) String name);

  @RequestMapping(path = "/accesskey/{" + RodaConstants.API_PATH_PARAM_NAME
    + "}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete access key", description = "Deletes an access key", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteAccessKey(
    @Parameter(description = "The access key id ") @PathVariable(name = RodaConstants.API_PATH_PARAM_NAME) String accessKeyId);

  @RequestMapping(path = "/users/accesskey/list/{" + RodaConstants.API_PATH_PARAM_NAME
    + "}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get user access keys list", description = "Gets a particular user access keys", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccessKeys.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  AccessKeys getAccessKeysByUser(
    @Parameter(description = "username") @PathVariable(name = RodaConstants.API_PATH_PARAM_NAME) String username);

  @RequestMapping(path = "/users/accesskey/{" + RodaConstants.API_PATH_PARAM_NAME
    + "}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get access key", description = "Gets a particular access key", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccessKey.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  AccessKey getAccessKey(
    @Parameter(description = "The access key id ") @PathVariable(name = RodaConstants.API_PATH_PARAM_NAME) String accessKeyId);

  @RequestMapping(path = "/accesskey/regenerate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Regenerate access key", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccessKey.class))), description = "Regenerate a access key", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccessKey.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  AccessKey regenerateAccessKey(
    @Parameter(name = "accesskey", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) AccessKey accessKey);

  @RequestMapping(path = "/accesskey/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Regenerate access key", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccessKey.class))), description = "Regenerate a access key", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccessKey.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  AccessKey createAccessKey(
    @Parameter(name = "accesskey", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) AccessKey accessKey);

  @RequestMapping(path = "/accesskey/revoke", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Revoke access key", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccessKey.class))), description = "Revokes a access key", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccessKey.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  AccessKey revokeAccessKey(
    @Parameter(name = "accesskey", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) AccessKey accessKey);

  @RequestMapping(path = "/token", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Authenticate using access key", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AccessKey.class))), description = "Authenticates using access key", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccessToken.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  AccessToken authenticate(
    @Parameter(name = "accesskey", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) AccessKey accessKey) throws GenericException, AuthorizationDeniedException;
}
