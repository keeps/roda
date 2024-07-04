package org.roda.core.common.pekko.messages.events;

import java.io.Serial;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public final class EventGroupDeleted extends AbstractEventMessage {
  @Serial
  private static final long serialVersionUID = -7862917122791858311L;

  private final String id;

  public EventGroupDeleted(String id, String senderId) {
    super(senderId);
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return "EventGroupDeleted [id=" + id + ", getSenderId()=" + getSenderId() + "]";
  }
}
