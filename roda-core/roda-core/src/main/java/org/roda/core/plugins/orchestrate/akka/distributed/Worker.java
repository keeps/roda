/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka.distributed;

import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.stop;

import java.io.Serializable;

import org.roda.core.data.utils.IdUtils;
import org.roda.core.plugins.orchestrate.akka.distributed.Master.Ack;
import org.roda.core.plugins.orchestrate.akka.distributed.Master.Work;
import org.roda.core.plugins.orchestrate.akka.distributed.MasterWorkerProtocol.RegisterWorker;
import org.roda.core.plugins.orchestrate.akka.distributed.MasterWorkerProtocol.WorkFailed;
import org.roda.core.plugins.orchestrate.akka.distributed.MasterWorkerProtocol.WorkIsDone;
import org.roda.core.plugins.orchestrate.akka.distributed.MasterWorkerProtocol.WorkIsReady;
import org.roda.core.plugins.orchestrate.akka.distributed.MasterWorkerProtocol.WorkerRequestsWork;

import akka.actor.ActorInitializationException;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.DeathPactException;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.actor.SupervisorStrategy;
import akka.actor.SupervisorStrategy.Directive;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.cluster.client.ClusterClient.SendToAll;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import akka.japi.Procedure;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class Worker extends UntypedActor {

  private final ActorRef clusterClient;
  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private final String workerId = IdUtils.createUUID();
  private final ActorRef workExecutor;
  private final Cancellable registerTask;
  private String currentWorkId = null;

  public Worker(ActorRef clusterClient, Props workExecutorProps, FiniteDuration registerInterval) {
    this.clusterClient = clusterClient;
    this.workExecutor = getContext().watch(getContext().actorOf(workExecutorProps, "exec"));
    this.registerTask = getContext().system().scheduler().schedule(Duration.Zero(), registerInterval, clusterClient,
      new SendToAll("/user/master/singleton", new RegisterWorker(workerId)), getContext().dispatcher(), getSelf());
  }

  public static Props props(ActorRef clusterClient, Props workExecutorProps, FiniteDuration registerInterval) {
    return Props.create(Worker.class, clusterClient, workExecutorProps, registerInterval);
  }

  public static Props props(ActorRef clusterClient, Props workExecutorProps) {
    return props(clusterClient, workExecutorProps, Duration.create(10, "seconds"));
  }

  private String workId() {
    if (currentWorkId != null)
      return currentWorkId;
    else
      throw new IllegalStateException("Not working");
  }

  @Override
  public SupervisorStrategy supervisorStrategy() {
    return new OneForOneStrategy(-1, Duration.Inf(), new Function<Throwable, Directive>() {
      @Override
      public Directive apply(Throwable t) {
        if (t instanceof ActorInitializationException || t instanceof DeathPactException) {
          return stop();
        } else if (t instanceof Exception) {
          if (currentWorkId != null) {
            sendToMaster(new WorkFailed(workerId, workId()));
          }
          getContext().become(idle);
          return restart();
        } else {
          return escalate();
        }
      }
    });
  }

  @Override
  public void postStop() {
    registerTask.cancel();
  }

  @Override
  public void onReceive(Object message) {
    unhandled(message);
  }

  private final Procedure<Object> idle = new Procedure<Object>() {
    @Override
    public void apply(Object message) {
      if (message instanceof MasterWorkerProtocol.WorkIsReady) {
        sendToMaster(new MasterWorkerProtocol.WorkerRequestsWork(workerId));
      } else if (message instanceof Work) {
        Work work = (Work) message;
        log.info("Got work: {}", work.job);
        currentWorkId = work.workId;
        workExecutor.tell(work.job, getSelf());
        getContext().become(working);
      } else {
        unhandled(message);
      }
    }
  };

  private final Procedure<Object> working = new Procedure<Object>() {
    @Override
    public void apply(Object message) {
      if (message instanceof WorkComplete) {
        Object result = ((WorkComplete) message).result;
        log.info("Work is complete. Result {}.", result);
        sendToMaster(new WorkIsDone(workerId, workId(), result));
        getContext().setReceiveTimeout(Duration.create(5, "seconds"));
        getContext().become(waitForWorkIsDoneAck(result));
      } else if (message instanceof Work) {
        log.info("Yikes. Master told me to do work, while I'm working.");
      } else {
        unhandled(message);
      }
    }
  };

  private Procedure<Object> waitForWorkIsDoneAck(final Object result) {
    return new Procedure<Object>() {
      @Override
      public void apply(Object message) {
        if (message instanceof Ack && ((Ack) message).workId.equals(workId())) {
          sendToMaster(new WorkerRequestsWork(workerId));
          getContext().setReceiveTimeout(Duration.Undefined());
          getContext().become(idle);
        } else if (message instanceof ReceiveTimeout) {
          log.info("No ack from master, retrying (" + workerId + " -> " + workId() + ")");
          sendToMaster(new WorkIsDone(workerId, workId(), result));
        } else {
          unhandled(message);
        }
      }
    };
  }

  {
    getContext().become(idle);
  }

  @Override
  public void unhandled(Object message) {
    if (message instanceof Terminated && ((Terminated) message).getActor().equals(workExecutor)) {
      getContext().stop(getSelf());
    } else if (message instanceof WorkIsReady) {
      // do nothing
    } else {
      super.unhandled(message);
    }
  }

  private void sendToMaster(Object msg) {
    clusterClient.tell(new SendToAll("/user/master/singleton", msg), getSelf());
  }

  public static final class WorkComplete implements Serializable {
    private static final long serialVersionUID = -3884968649942671879L;
    public final Object result;

    public WorkComplete(Object result) {
      this.result = result;
    }

    @Override
    public String toString() {
      return "WorkComplete{" + "result=" + result + '}';
    }
  }
}
