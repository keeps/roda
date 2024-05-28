package org.roda.wui.client.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Date;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import jakarta.websocket.OnError;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.common.SecureString;
import org.roda.core.data.exceptions.AuthenticationDeniedException;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.data.v2.generics.CreateGroupRequest;
import org.roda.core.data.v2.accessToken.AccessToken;
import org.roda.core.data.v2.generics.CreateAccessKeyRequest;
import org.roda.core.data.v2.generics.CreateUserExtraFormFields;
import org.roda.core.data.v2.generics.CreateUserRequest;
import org.roda.core.data.v2.generics.LoginRequest;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.generics.RegenerateAccessKeyRequest;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.user.Group;
import org.roda.core.data.v2.user.RODAMember;
import org.roda.core.data.v2.user.User;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.roda.wui.api.v2.model.GenericOkResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Date;
import java.util.Set;

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

  @RequestMapping(path = "/users/authenticated", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get authenticated user", description = "Gets the authenticated user", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = User.class)))})
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
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete group", description = "Deletes an existing group", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteGroup(
    @Parameter(description = "The group name") @PathVariable(name = RodaConstants.API_PATH_PARAM_NAME) String name);

  @RequestMapping(path = "/users/status", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Activate or deactivate a RODA user", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), description = "Activates or deactivates RODA users", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void changeActive(
    @Parameter(name = "selectedItems", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<RODAMember> members,
    @Parameter(description = "True to change their status to active and False to change their status to deactivate") @RequestParam(name = "activate") Boolean activate);

  @RequestMapping(path = "/delete", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete multiple RODA members via search query", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SelectedItems.class))), description = "Deletes one or more RODA members", responses = {
    @ApiResponse(responseCode = "204", description = "No Content")})
  Void deleteMultipleMembers(
    @Parameter(name = "selectedItems", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) SelectedItems<RODAMember> members);

  @RequestMapping(path = "/users", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create user", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = User.class))), description = "Creates a new user", responses = {
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = User.class))),
    @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  User createUser(
    @Parameter(name = "user", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) CreateUserRequest userOperations,
    @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/users/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Register user", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = User.class))), description = "Registers a new user", responses = {
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = User.class)))})
  User registerUser(
    @Parameter(name = "user", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) CreateUserRequest userOperations,
    @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString,
    @Parameter(description = "captcha") @RequestParam(required = false, name = "captcha") String captcha);

  @RequestMapping(path = "/groups", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create group", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateGroupRequest.class))), description = "Creates a new group", responses = {
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = Group.class))),
    @ApiResponse(responseCode = "409", description = "Conflict", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Group createGroup(
    @Parameter(name = "group", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) CreateGroupRequest group);

  @RequestMapping(path = "/groups", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Update group", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Group.class))), description = "Updates a group", responses = {
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
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
  @Operation(summary = "Recover login", description = "Sends an email to recover login", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GenericOkResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  String recoverLogin(
    @Parameter(description = "email") @RequestParam(name = "email") String email,
    @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString,
    @Parameter(description = "captcha") @RequestParam(required = false, name = "captcha") String captcha);

  @RequestMapping(path = "/users/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Confirm user email", description = "Confirms a user email", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = GenericOkResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  String confirmUserEmail(
    @Parameter(description = "username") @RequestParam(name = "username") String username,
    @Parameter(description = "token") @RequestParam(required = false, name = "token") String token);

  @RequestMapping(path = "/configuration/custom-form", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get default user extra", description = "Gets the default extra form fields to create an user", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Set.class))),
    @ApiResponse(responseCode = "409", description = "Already exists", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  CreateUserExtraFormFields getDefaultUserExtra();

  @RequestMapping(path = "/users/send-verification", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Send verification email", description = "Sends verification email", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Notification.class)))})
  Notification sendEmailVerification(
    @Parameter(description = "Username") @RequestParam(name = "username") String username,
    @Parameter(description = "Generate new token") @RequestParam(required = false, name = "generate-new-token", defaultValue = "false") boolean generateNewToken,
    @Parameter(description = "The language to be used for internationalization") @RequestParam(name = "lang", defaultValue = "en", required = false) String localeString);

  @RequestMapping(path = "/users/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Login", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LoginRequest.class))), description = "Logs in a user", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = User.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  User login(
    @Parameter(name = "login-request", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) LoginRequest loginRequest) throws AuthenticationDeniedException;

  @RequestMapping(path = "/users/accesskey/{" + RodaConstants.API_PATH_PARAM_NAME
    + "}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete user access key", description = "Deletes user access keys", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteUserAccessKeys(
    @Parameter(description = "The user name") @PathVariable(name = RodaConstants.API_PATH_PARAM_NAME) String name);

  @RequestMapping(path = "/accesskey/{" + RodaConstants.API_PATH_PARAM_NAME
    + "}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Delete access key", description = "Deletes an access key", responses = {
    @ApiResponse(responseCode = "204", description = "No Content"),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Void deleteAccessKey(
    @Parameter(description = "The access key id ") @PathVariable(name = RodaConstants.API_PATH_PARAM_NAME) String accessKeyId);

  @RequestMapping(path = "/users/{" + RodaConstants.API_PATH_PARAM_NAME
    + "}/access-keys", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get user access keys list", description = "Gets a particular user access keys", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccessKeys.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  AccessKeys getAccessKeysByUser(
    @Parameter(description = "username") @PathVariable(name = RodaConstants.API_PATH_PARAM_NAME) String username);

  @RequestMapping(path = "/users/access-keys/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get access key", description = "Gets an access key by its id", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccessKey.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  AccessKey getAccessKey(
    @Parameter(description = "The access key id ") @PathVariable(name = "id") String accessKeyId);

  @RequestMapping(path = "/users/access-keys/regenerate/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Regenerate access key", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RegenerateAccessKeyRequest.class))), description = "Regenerate a access key", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccessKey.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  AccessKey regenerateAccessKey(
    @Parameter(description = "The access key id") @PathVariable(name = "id") String id,
    @Parameter(name = "expirationDate", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) RegenerateAccessKeyRequest regenerateAccessKeyRequest);

  @RequestMapping(path = "/users/access-keys", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create access key", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateAccessKeyRequest.class))), description = "Creates an access key", responses = {
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = AccessKey.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  AccessKey createAccessKey(
    @Parameter(name = "accessKeyRequest", required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)) CreateAccessKeyRequest accessKey);

  @RequestMapping(path = "/users/access-keys/revoke/{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Revoke access key", description = "Revokes a access key", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccessKey.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  AccessKey revokeAccessKey(
    @Parameter(description = "The access key id") @PathVariable(name = "id") String id);
}
