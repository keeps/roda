/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.pekko;

import org.apache.pekko.actor.DeadLetter;
import org.apache.pekko.actor.UntypedAbstractActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeadLetterActor extends UntypedAbstractActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeadLetterActor.class);

  @Override
  public void onReceive(Object message) {
    if (message instanceof DeadLetter letter
      && !(letter.message() instanceof String && "Done".equals(letter.message()))) {
        LOGGER.debug("Dead letter: {}", message);
      }

  }
}
