/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.base.notifications;

import org.roda.core.data.exceptions.NotificationException;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;

public interface JobNotification {
  boolean whenFailed();

  void notify(ModelService model, IndexService index, Job job, JobStats jobStats) throws NotificationException;
}
