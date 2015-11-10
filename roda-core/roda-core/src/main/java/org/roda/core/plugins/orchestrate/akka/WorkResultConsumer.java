package org.roda.core.plugins.orchestrate.akka;

import org.roda.core.plugins.orchestrate.akka.Master.WorkResult;

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
    mediator.tell(new DistributedPubSubMediator.Subscribe(Master.ResultsTopic, getSelf()), getSelf());
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
