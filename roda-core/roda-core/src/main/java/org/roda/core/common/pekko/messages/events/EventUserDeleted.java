/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.pekko.messages.events;

import java.io.Serial;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public final class EventUserDeleted extends AbstractEventMessage {
  @Serial
  private static final long serialVersionUID = -7862917122791858311L;

  private final String id;

  public EventUserDeleted(String id, String senderId) {
    super(senderId);
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return "EventUserDeleted [id=" + id + ", getSenderId()=" + getSenderId() + "]";
  }
}
