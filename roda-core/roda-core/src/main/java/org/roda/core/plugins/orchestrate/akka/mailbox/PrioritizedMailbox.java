package org.roda.core.plugins.orchestrate.akka.mailbox;

import org.roda.core.common.akka.Messages;

import com.typesafe.config.Config;

import akka.actor.ActorSystem;
import akka.dispatch.PriorityGenerator;
import akka.dispatch.UnboundedStablePriorityMailbox;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PrioritizedMailbox extends UnboundedStablePriorityMailbox {

  public PrioritizedMailbox(ActorSystem.Settings settings, Config config) {
    super(new PriorityGenerator() {
      @Override
      public int gen(Object message) {
        if (message instanceof Messages.AbstractMessage) {
          switch (((Messages.AbstractMessage) message).getJobPriority()) {
            case LOW:
              return PrioritizedMessage.LOW;
            case HIGH:
              return PrioritizedMessage.HIGH;
            case MEDIUM:
            default:
              return PrioritizedMessage.MEDIUM;

          }
        } else {
          return PrioritizedMessage.MEDIUM;
        }
      }
    });
  }
}
