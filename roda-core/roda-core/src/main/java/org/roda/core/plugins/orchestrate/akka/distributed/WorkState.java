/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka.distributed;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.roda.core.plugins.orchestrate.akka.distributed.Master.Work;

public final class WorkState {

  private final Map<String, Work> workInProgress;
  private final Set<String> acceptedWorkIds;
  private final Set<String> doneWorkIds;
  private final ConcurrentLinkedDeque<Work> pendingWork;

  public WorkState updated(WorkDomainEvent event) {
    WorkState newState = null;
    if (event instanceof WorkAccepted) {
      return new WorkState(this, (WorkAccepted) event);
    } else if (event instanceof WorkStarted) {
      return new WorkState(this, (WorkStarted) event);
    } else if (event instanceof WorkCompleted) {
      return new WorkState(this, (WorkCompleted) event);
    } else if (event instanceof WorkerFailed) {
      return new WorkState(this, (WorkerFailed) event);
    } else if (event instanceof WorkerTimedOut) {
      return new WorkState(this, (WorkerTimedOut) event);
    }
    return newState;
  }

  public WorkState() {
    workInProgress = new HashMap<>();
    acceptedWorkIds = new HashSet<>();
    doneWorkIds = new HashSet<>();
    pendingWork = new ConcurrentLinkedDeque<>();
  }

  private WorkState(WorkState workState, WorkAccepted workAccepted) {
    ConcurrentLinkedDeque<Work> tmp_pendingWork = new ConcurrentLinkedDeque<>(workState.pendingWork);
    Set<String> tmp_acceptedWorkIds = new HashSet<>(workState.acceptedWorkIds);
    tmp_pendingWork.addLast(workAccepted.work);
    tmp_acceptedWorkIds.add(workAccepted.work.workId);
    workInProgress = new HashMap<>(workState.workInProgress);
    acceptedWorkIds = tmp_acceptedWorkIds;
    doneWorkIds = new HashSet<>(workState.doneWorkIds);
    pendingWork = tmp_pendingWork;

  }

  public WorkState(WorkState workState, WorkStarted workStarted) {
    ConcurrentLinkedDeque<Work> tmp_pendingWork = new ConcurrentLinkedDeque<>(workState.pendingWork);
    Map<String, Work> tmp_workInProgress = new HashMap<>(workState.workInProgress);

    Work work = tmp_pendingWork.removeFirst();
    if (!work.workId.equals(workStarted.workId)) {
      throw new IllegalArgumentException("WorkStarted expected workId " + work.workId + "==" + workStarted.workId);
    }
    tmp_workInProgress.put(work.workId, work);

    workInProgress = tmp_workInProgress;
    acceptedWorkIds = new HashSet<>(workState.acceptedWorkIds);
    doneWorkIds = new HashSet<>(workState.doneWorkIds);
    pendingWork = tmp_pendingWork;
  }

  public WorkState(WorkState workState, WorkCompleted workCompleted) {
    Map<String, Work> tmp_workInProgress = new HashMap<>(workState.workInProgress);
    Set<String> tmp_doneWorkIds = new HashSet<>(workState.doneWorkIds);
    tmp_workInProgress.remove(workCompleted.workId);
    tmp_doneWorkIds.add(workCompleted.workId);
    workInProgress = tmp_workInProgress;
    acceptedWorkIds = new HashSet<>(workState.acceptedWorkIds);
    doneWorkIds = tmp_doneWorkIds;
    pendingWork = new ConcurrentLinkedDeque<>(workState.pendingWork);
  }

  public WorkState(WorkState workState, WorkerFailed workerFailed) {
    Map<String, Work> tmp_workInProgress = new HashMap<>(workState.workInProgress);
    ConcurrentLinkedDeque<Work> tmp_pendingWork = new ConcurrentLinkedDeque<>(workState.pendingWork);
    tmp_pendingWork.addLast(workState.workInProgress.get(workerFailed.workId));
    tmp_workInProgress.remove(workerFailed.workId);
    workInProgress = tmp_workInProgress;
    acceptedWorkIds = new HashSet<>(workState.acceptedWorkIds);
    doneWorkIds = new HashSet<>(workState.doneWorkIds);
    pendingWork = tmp_pendingWork;
  }

  public WorkState(WorkState workState, WorkerTimedOut workerTimedOut) {
    Map<String, Work> tmp_workInProgress = new HashMap<>(workState.workInProgress);
    ConcurrentLinkedDeque<Work> tmp_pendingWork = new ConcurrentLinkedDeque<>(workState.pendingWork);
    tmp_pendingWork.addLast(workState.workInProgress.get(workerTimedOut.workId));
    tmp_workInProgress.remove(workerTimedOut.workId);
    workInProgress = tmp_workInProgress;
    acceptedWorkIds = new HashSet<>(workState.acceptedWorkIds);
    doneWorkIds = new HashSet<>(workState.doneWorkIds);
    pendingWork = tmp_pendingWork;
  }

  public String toString() {
    return Integer.toString(acceptedWorkIds.size());
  }

  public Work nextWork() {
    return pendingWork.getFirst();
  }

  public boolean hasWork() {
    return !pendingWork.isEmpty();
  }

  public boolean isAccepted(String workId) {
    return acceptedWorkIds.contains(workId);
  }

  public boolean isInProgress(String workId) {
    return workInProgress.containsKey(workId);
  }

  public boolean isDone(String workId) {
    return doneWorkIds.contains(workId);
  }

  public interface WorkDomainEvent {

  }

  public static final class WorkAccepted implements WorkDomainEvent, Serializable {
    private static final long serialVersionUID = -3030209712968228650L;

    final Work work;

    public WorkAccepted(Work work) {
      this.work = work;
    }

  }

  public static final class WorkStarted implements WorkDomainEvent, Serializable {
    private static final long serialVersionUID = 5143608113160395981L;

    final String workId;

    public WorkStarted(String workId) {
      this.workId = workId;
    }

  }

  public static final class WorkCompleted implements WorkDomainEvent, Serializable {
    private static final long serialVersionUID = -551011155695164520L;

    final String workId;
    final Object result;

    public WorkCompleted(String workId, Object result) {
      this.workId = workId;
      this.result = result;
    }

  }

  public static final class WorkerFailed implements WorkDomainEvent, Serializable {
    private static final long serialVersionUID = 6671821443961398568L;

    final String workId;

    public WorkerFailed(String workId) {
      this.workId = workId;
    }

  }

  public static final class WorkerTimedOut implements WorkDomainEvent, Serializable {
    private static final long serialVersionUID = -1080174696275300604L;

    final String workId;

    public WorkerTimedOut(String workId) {
      this.workId = workId;
    }

  }

}
