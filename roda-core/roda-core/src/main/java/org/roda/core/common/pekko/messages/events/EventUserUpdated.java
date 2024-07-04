package org.roda.core.common.pekko.messages.events;

import org.roda.core.data.v2.user.User;

import java.io.Serial;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public final class EventUserUpdated extends AbstractEventMessage {
  @Serial
  private static final long serialVersionUID = -2517455273875624115L;

  private final User user;
  private String password;
  private final boolean myUser;

  public EventUserUpdated(User user, boolean myUser, String senderId) {
    super(senderId);
    this.user = user;
    this.myUser = myUser;
  }

  public User getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public boolean isMyUser() {
    return myUser;
  }

  @Override
  public String toString() {
    return "EventUserUpdated [user=" + user + ", password=" + password + ", myUser=" + myUser + ", getSenderId()="
        + getSenderId() + "]";
  }
}
