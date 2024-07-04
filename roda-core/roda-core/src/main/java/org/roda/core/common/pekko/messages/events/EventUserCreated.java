package org.roda.core.common.pekko.messages.events;

import org.roda.core.data.v2.user.User;

import java.io.Serial;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public final class EventUserCreated extends AbstractEventMessage {
  @Serial
  private static final long serialVersionUID = -2517455273875624115L;

  private User user;
  private String password;

  public EventUserCreated(User user, String senderId) {
    super(senderId);
    this.user = user;
  }

  public User getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  @Override
  public String toString() {
    return "EventUserCreated [user=" + user + ", getSenderId()=" + getSenderId() + "]";
  }
}
