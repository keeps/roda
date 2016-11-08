/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.roda.core.data.v2.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import scala.concurrent.duration.Duration;

public class AkkaJobsManager extends AkkaBaseActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaJobsManager.class);

  private ActorRef jobsRouter;
  private int maxNumberOfJobsInParallel;
  private ConcurrentLinkedQueue<Job> jobsWaiting;
  private Map<String, ActorRef> jobsWaitingCreators;
  private AtomicInteger jobsBeingExecuted;
  private boolean tickSent;

  public AkkaJobsManager(int maxNumberOfJobsInParallel) {
    super();
    this.maxNumberOfJobsInParallel = maxNumberOfJobsInParallel;
    this.jobsWaiting = new ConcurrentLinkedQueue<>();
    this.jobsWaitingCreators = new HashMap<>();
    this.jobsBeingExecuted = new AtomicInteger(0);
    this.tickSent = false;

    Props jobsProps = new RoundRobinPool(maxNumberOfJobsInParallel).props(Props.create(AkkaJobActor.class, getSelf()));
    jobsRouter = getContext().actorOf(jobsProps, "JobsRouter");

    getContext().system().scheduler().schedule(Duration.create(0, TimeUnit.MILLISECONDS),
      Duration.create(2, TimeUnit.SECONDS), new Runnable() {
        @Override
        public void run() {
          if (!tickSent && !jobsWaiting.isEmpty()) {
            tickSent = true;
            getSelf().tell(new Messages.JobsManagerTick(), getSelf());
          }
        }
      }, getContext().system().dispatcher());
  }

  @Override
  public void onReceive(Object msg) throws Throwable {
    LOGGER.debug("start > maxNumberOfJobsInParallel:{}; #jobsBeingExecuted:{}; #jobsWaiting:{}",
      maxNumberOfJobsInParallel, jobsBeingExecuted.get(), jobsWaiting.size());
    if (msg instanceof Job) {
      if (jobsWaiting.isEmpty() && jobsBeingExecuted.get() < maxNumberOfJobsInParallel) {
        jobsBeingExecuted.incrementAndGet();
        jobsRouter.tell(msg, getSender());
      } else {
        Job job = (Job) msg;
        jobsWaiting.add(job);
        jobsWaitingCreators.put(job.getId(), getSender());
      }
    } else if (msg instanceof Messages.JobsManagerTick) {
      if (!jobsWaiting.isEmpty() && jobsBeingExecuted.get() < maxNumberOfJobsInParallel) {
        while (!jobsWaiting.isEmpty() && jobsBeingExecuted.get() < maxNumberOfJobsInParallel) {
          jobsBeingExecuted.incrementAndGet();
          Job job = jobsWaiting.remove();
          ActorRef jobCreator = jobsWaitingCreators.remove(job.getId());
          jobsRouter.tell(job, jobCreator);
        }
      }
      tickSent = false;
    } else if (msg instanceof Messages.JobsManagerJobEnded) {
      jobsBeingExecuted.decrementAndGet();
      tickSent = false;
    } else {
      LOGGER.error("Received a message that don't know how to process ({})...", msg.getClass().getName());
      unhandled(msg);
    }
    LOGGER.debug("end > maxNumberOfJobsInParallel:{}; #jobsBeingExecuted:{}; #jobsWaiting:{}",
      maxNumberOfJobsInParallel, jobsBeingExecuted.get(), jobsWaiting.size());
  }

}
