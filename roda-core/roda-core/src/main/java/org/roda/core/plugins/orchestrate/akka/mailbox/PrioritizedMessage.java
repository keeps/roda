package org.roda.core.plugins.orchestrate.akka.mailbox;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public interface PrioritizedMessage {
  int URGENT = 100, HIGH = 200, MEDIUM = 300, LOW = 400;

  int getPriority();

  interface Urgent extends PrioritizedMessage {
    default int getPriority() { return URGENT; }
  }

  interface High extends PrioritizedMessage {
    default int getPriority() { return HIGH; }
  }

  interface Medium extends PrioritizedMessage {
    default int getPriority() { return MEDIUM; }
  }

  interface Low extends PrioritizedMessage {
    default int getPriority() { return LOW; }
  }
}
