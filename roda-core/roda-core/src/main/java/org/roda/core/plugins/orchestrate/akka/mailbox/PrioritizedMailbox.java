/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka.mailbox;

import org.roda.core.common.akka.Messages;

import com.typesafe.config.Config;

import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.dispatch.PriorityGenerator;
import org.apache.pekko.dispatch.UnboundedStablePriorityMailbox;

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
