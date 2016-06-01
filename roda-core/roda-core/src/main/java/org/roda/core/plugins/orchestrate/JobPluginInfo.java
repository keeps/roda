/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.orchestrate;

import org.roda.core.data.v2.jobs.JobStats;

public abstract class JobPluginInfo extends JobStats implements JobPluginInfoInterface {
  private static final long serialVersionUID = 2106710222456788707L;

  private boolean done = false;

  public JobPluginInfo() {
    super();
  }

  public boolean isDone() {
    return done;
  }

  public void setDone(boolean done) {
    this.done = done;
  }

  public void finalizeInfo() {
    setDone(true);
    setSourceObjectsBeingProcessed(0);
    setSourceObjectsWaitingToBeProcessed(0);
  }

}
