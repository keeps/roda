/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.akka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.DeadLetter;
import akka.actor.UntypedAbstractActor;

public class DeadLetterActor extends UntypedAbstractActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeadLetterActor.class);

  @Override
  public void onReceive(Object message) {
    if (message instanceof DeadLetter) {
      DeadLetter letter = (DeadLetter) message;
      if (!(letter.message() instanceof String && "Done".equals(String.class.cast(letter.message())))) {
        LOGGER.debug("Dead letter: {}", message);
      }
    }
  }
}
