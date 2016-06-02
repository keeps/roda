/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka.distributed;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class WorkExecutor extends UntypedActor {

  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  @Override
  public void onReceive(Object message) {
    if (message instanceof Integer) {
      Integer n = (Integer) message;
      int n2 = n.intValue() * n.intValue();
      String result = n + " * " + n + " = " + n2;
      log.info("Produced result {}", result);
      getSender().tell(new Worker.WorkComplete(result), getSelf());
    }else if(message instanceof Object){
      // TODO
    }
  }
}
