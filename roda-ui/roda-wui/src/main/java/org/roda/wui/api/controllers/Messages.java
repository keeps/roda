/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.api.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.roda.core.RodaCoreFactory;
import org.roda.core.common.UserUtility;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.messages.Message;
import org.roda.core.data.v2.user.RodaUser;
import org.roda.wui.common.RodaCoreService;

/**
 * FIXME 1) verify all checkObject*Permissions (because now also a permission
 * for insert is available)
 */
public class Messages extends RodaCoreService {

  private static final String MESSAGES_COMPONENT = "Messages";
  private static final String INGEST_SUBMIT_ROLE = "ingest.submit";

  private Messages() {
    super();
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - start -----------------------------
   * ---------------------------------------------------------------------------
   */
  public static Message createMessage(RodaUser user, Message message) throws AuthorizationDeniedException,
    RequestNotValidException, NotFoundException, GenericException {
    Date startDate = new Date();

    // FIXME check user permissions
    UserUtility.checkRoles(user, INGEST_SUBMIT_ROLE);

    RodaCoreFactory.getModelService().createMessage(message);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, MESSAGES_COMPONENT, "createMessage", null, duration, "message", message);

    return message;
  }

  public static void deleteMessage(RodaUser user, String messageId) throws RequestNotValidException, GenericException,
    NotFoundException, AuthorizationDeniedException {
    Date startDate = new Date();

    // check user permissions
    // FIXME

    // delegate
    RodaCoreFactory.getModelService().deleteMessage(messageId);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, MESSAGES_COMPONENT, "deleteMessage", null, duration, "messageId", messageId);
  }

  public static List<Message> retrieveMessages(IndexResult<Message> listMessagesIndexResult) {
    List<Message> messages = new ArrayList<Message>();
    for (Message message : listMessagesIndexResult.getResults()) {
      messages.add(message);
    }
    return messages;
  }

  public static void acknowledgeMessage(RodaUser user, String messageId, String token) throws RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException {
    Date startDate = new Date();

    // check user permissions
    // FIXME

    // delegate
    RodaCoreFactory.getModelService().acknowledgeMessage(messageId, token);

    // register action
    long duration = new Date().getTime() - startDate.getTime();
    registerAction(user, MESSAGES_COMPONENT, "acknowledgeMessage", null, duration, "messageId", messageId, "token",
      token);
  }

  /*
   * ---------------------------------------------------------------------------
   * ---------------- REST related methods - end -------------------------------
   * ---------------------------------------------------------------------------
   */
}
