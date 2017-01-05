package org.roda.core.plugins.orchestrate.akka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.DeadLetter;
import akka.actor.UntypedActor;

public class DeadLetterActor extends UntypedActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeadLetterActor.class);

  public void onReceive(Object message) {
    if (message instanceof DeadLetter) {
      DeadLetter letter = (DeadLetter) message;
      if (!(letter.message() instanceof String && "Done".equals(String.class.cast(letter.message())))) {
        LOGGER.error("Dead letter: {}", message);
      }
    }
  }
}
