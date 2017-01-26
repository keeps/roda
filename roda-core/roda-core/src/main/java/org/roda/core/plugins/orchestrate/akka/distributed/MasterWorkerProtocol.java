/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate.akka.distributed;

import java.io.Serializable;

public abstract class MasterWorkerProtocol {

  // Messages from/to Workers

  public static final class RegisterWorker implements Serializable {
    private static final long serialVersionUID = 1958015449276995915L;
    public final String workerId;

    public RegisterWorker(String workerId) {
      this.workerId = workerId;
    }

    @Override
    public String toString() {
      return "RegisterWorker{" + "workerId='" + workerId + '\'' + '}';
    }
  }

  public static final class WorkerRequestsWork implements Serializable {
    private static final long serialVersionUID = 5975172522395596054L;
    public final String workerId;

    public WorkerRequestsWork(String workerId) {
      this.workerId = workerId;
    }

    @Override
    public String toString() {
      return "WorkerRequestsWork{" + "workerId='" + workerId + '\'' + '}';
    }
  }

  public static final class WorkIsDone implements Serializable {
    private static final long serialVersionUID = -2988546494525969435L;
    public final String workerId;
    public final String workId;
    public final Object result;

    public WorkIsDone(String workerId, String workId, Object result) {
      this.workerId = workerId;
      this.workId = workId;
      this.result = result;
    }

    @Override
    public String toString() {
      return "WorkIsDone{" + "workerId='" + workerId + '\'' + ", workId='" + workId + '\'' + ", result=" + result + '}';
    }
  }

  public static final class WorkFailed implements Serializable {
    private static final long serialVersionUID = -3521696564547324113L;

    public final String workerId;
    public final String workId;

    public WorkFailed(String workerId, String workId) {
      this.workerId = workerId;
      this.workId = workId;
    }

    @Override
    public String toString() {
      return "WorkFailed{" + "workerId='" + workerId + '\'' + ", workId='" + workId + '\'' + '}';
    }
  }

  // Messages to Workers

  public static final class WorkIsReady implements Serializable {
    private static final long serialVersionUID = 1819335575064977572L;
    private static final WorkIsReady instance = new WorkIsReady();

    public static WorkIsReady getInstance() {
      return instance;
    }
  }
}