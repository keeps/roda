/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.services;

import org.roda.core.data.v2.generics.StringResponse;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.notifications.NotificationAcknowledgeRequest;
import org.roda.wui.api.v2.exceptions.model.ErrorResponseMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notifications")
@RequestMapping(path = "../api/v2/notifications")
public interface NotificationRestService extends RODAEntityRestService<Notification> {

  @RequestMapping(method = RequestMethod.GET, path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Get notification", description = "Gets a notification", responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Notification.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  Notification getNotification(@PathVariable(name = "id") String notificationId);

  @RequestMapping(method = RequestMethod.POST, path = "/acknowledge", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Acknowledge notification", description = "Acknowledges a notification", requestBody = @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NotificationAcknowledgeRequest.class))), responses = {
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StringResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ErrorResponseMessage.class)))})
  StringResponse acknowledgeNotification(NotificationAcknowledgeRequest notificationAcknowledgeRequest);
}