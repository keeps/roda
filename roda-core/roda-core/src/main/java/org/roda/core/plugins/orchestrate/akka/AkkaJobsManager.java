/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.v2.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import scala.concurrent.duration.Duration;

public class AkkaJobsManager extends AkkaBaseActor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AkkaJobsManager.class);

  // state
  private int maxNumberOfJobsInParallel;
  private Queue<JobWaiting> jobsWaiting;
  private Map<String, ActorRef> jobsWaitingCreators;
  private ActorRef jobsRouter;

  // metrics
  private Counter jobsBeingExecuted;
  private Counter jobsWaitingToBeExecuted;
  private Counter ticksWaitingToBeProcessed;
  private Histogram jobsBeingExecutedHisto;
  private Histogram jobsWaitingToBeExecutedHisto;
  private Histogram jobsTimeInTheQueueInMilis;

  public AkkaJobsManager(int maxNumberOfJobsInParallel) {
    super();
    this.maxNumberOfJobsInParallel = maxNumberOfJobsInParallel;
    this.jobsWaiting = new LinkedList<>();
    this.jobsWaitingCreators = new HashMap<>();

    Props jobsProps = new RoundRobinPool(maxNumberOfJobsInParallel).props(Props.create(AkkaJobActor.class, getSelf()));
    jobsRouter = getContext().actorOf(jobsProps, "JobsRouter");

    initMetrics(maxNumberOfJobsInParallel);

    getContext().system().scheduler().schedule(Duration.create(0, TimeUnit.MILLISECONDS),
      Duration.create(2, TimeUnit.SECONDS), new Runnable() {
        @Override
        public void run() {
          if (jobsWaitingToBeExecuted.getCount() > 0) {
            sendTick();
          }
        }
      }, getContext().system().dispatcher());
  }

  @Override
  public void onReceive(Object msg) throws Throwable {
    if (msg instanceof Job) {
      Job job = (Job) msg;
      if (jobsWaitingToBeExecuted.getCount() == 0 && jobsBeingExecuted.getCount() < maxNumberOfJobsInParallel) {
        sendJobForExecution(msg, job);
      } else {
        queueJob(job, getSender());
      }
    } else if (msg instanceof Messages.JobsManagerTick) {
      if (jobsWaitingToBeExecuted.getCount() > 0 && jobsBeingExecuted.getCount() < maxNumberOfJobsInParallel) {
        while (jobsWaitingToBeExecuted.getCount() > 0 && jobsBeingExecuted.getCount() < maxNumberOfJobsInParallel) {
          dequeueJob();
        }
      }
      ticksWaitingToBeProcessed.dec();
    } else if (msg instanceof Messages.JobsManagerJobEnded) {
      updateJobsBeingExecuted(false);
      sendTick();
      log("The end for job", ((Messages.JobsManagerJobEnded) msg).getJobId());
    } else {
      LOGGER.error("Received a message that don't know how to process ({})...", msg.getClass().getName());
      unhandled(msg);
    }
  }

  private void sendTick() {
    if (ticksWaitingToBeProcessed.getCount() == 0) {
      self().tell(new Messages.JobsManagerTick(), self());
      ticksWaitingToBeProcessed.inc();
    }
  }

  private void sendJobForExecution(Object msg, Job job) {
    updateJobsBeingExecuted(true);
    jobsTimeInTheQueueInMilis.update(0);
    jobsRouter.tell(msg, getSender());
    log("Will execute job", job.getId());
  }

  private void updateJobsBeingExecuted(boolean increment) {
    if (increment) {
      jobsBeingExecuted.inc();
    } else {
      jobsBeingExecuted.dec();
    }
    jobsBeingExecutedHisto.update(jobsBeingExecuted.getCount());
  }

  private void updateJobsWaitingToBeExecuted(boolean increment) {
    if (increment) {
      jobsWaitingToBeExecuted.inc();
    } else {
      jobsWaitingToBeExecuted.dec();
    }
    jobsWaitingToBeExecutedHisto.update(jobsWaitingToBeExecuted.getCount());
  }

  private void queueJob(Job job, ActorRef sender) {
    jobsWaiting.offer(new JobWaiting(job));
    jobsWaitingCreators.put(job.getId(), sender);
    updateJobsWaitingToBeExecuted(true);
    log("Queued job", job.getId());
  }

  private Job dequeueJob() {
    JobWaiting jobWaiting = jobsWaiting.remove();
    jobsTimeInTheQueueInMilis.update(jobWaiting.timeInQueue());
    Job job = jobWaiting.job;
    ActorRef jobCreator = jobsWaitingCreators.remove(job.getId());
    jobsRouter.tell(job, jobCreator);
    updateJobsBeingExecuted(true);
    updateJobsWaitingToBeExecuted(false);
    log("Dequeued job", job.getId());
    return job;
  }

  private void log(String msg, String jobId) {
    LOGGER.info("{} '{}' (max: {}| exec: {}| wait: {})", msg, jobId, maxNumberOfJobsInParallel,
      jobsBeingExecuted.getCount(), jobsWaitingToBeExecuted.getCount());
  }

  private void initMetrics(int maxNumberOfJobsInParallel) {
    MetricRegistry metrics = RodaCoreFactory.getMetrics();
    Counter maxNumberOfJobsInParallelCounter = metrics
      .counter(MetricRegistry.name(AkkaJobsManager.class, "maxNumberOfJobsInParallel"));
    maxNumberOfJobsInParallelCounter.inc(maxNumberOfJobsInParallel);
    jobsBeingExecuted = metrics.counter(MetricRegistry.name(AkkaJobsManager.class, "jobsBeingExecuted"));
    jobsWaitingToBeExecuted = metrics.counter(MetricRegistry.name(AkkaJobsManager.class, "jobsWaitingToBeExecuted"));
    ticksWaitingToBeProcessed = metrics
      .counter(MetricRegistry.name(AkkaJobsManager.class, "ticksWaitingToBeProcessed"));
    jobsBeingExecutedHisto = metrics.histogram(MetricRegistry.name(AkkaJobsManager.class, "jobsBeingExecutedHisto"));
    jobsWaitingToBeExecutedHisto = metrics
      .histogram(MetricRegistry.name(AkkaJobsManager.class, "jobsWaitingToBeExecutedHisto"));
    jobsTimeInTheQueueInMilis = metrics
      .histogram(MetricRegistry.name(AkkaJobsManager.class, "jobsTimeInTheQueueInMilis"));
  }

  private class JobWaiting {
    public Job job;
    private long queuedIn;

    public JobWaiting(Job job) {
      this.job = job;
      this.queuedIn = new Date().getTime();
    }

    /** Time in miliseconds */
    public long timeInQueue() {
      return new Date().getTime() - queuedIn;
    }
  }

}
