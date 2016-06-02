/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka.distributed;

import org.roda.core.plugins.orchestrate.akka.distributed.Master.WorkResult;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;

// FIXME to delete
public class WorkResultConsumer extends UntypedActor {

  private ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  {
    mediator.tell(new DistributedPubSubMediator.Subscribe(Master.RESULTS_TOPIC, getSelf()), getSelf());
  }

  @Override
  public void onReceive(Object message) {
    if (message instanceof DistributedPubSubMediator.SubscribeAck) {
      // do nothing
    } else if (message instanceof WorkResult) {
      WorkResult workResult = (WorkResult) message;
      log.info("Consumed result: {}", workResult.result);
    } else {
      unhandled(message);
    }
  }
}
