/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.notifications;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.notifications.FileNotificationProcessor;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotificationException;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;

public class FileGenericNotification extends AbstractJobNotification implements GenericJobNotification {
  public FileGenericNotification(String to) {
    super(to);
  }

  public FileGenericNotification(String to, boolean whenFailed) {
    super(to, whenFailed);
  }

  @Override
  public void notify(ModelService model, IndexService index, Job job, JobStats jobStats) throws NotificationException {
    try {
      if (StringUtils.isNotBlank(this.getTo())) {
        Notification notification = new Notification();
        String outcome = PluginState.SUCCESS.toString();

        if (jobStats.getSourceObjectsProcessedWithFailure() > 0) {
          outcome = PluginState.FAILURE.toString();
        }

        String subject = RodaCoreFactory.getRodaConfigurationAsString("core", "notification", "generic_subject");
        if (StringUtils.isNotBlank(subject)) {
          subject = subject.replaceAll("\\{RESULT\\}", outcome);
        } else {
          subject = outcome;
        }

        notification.setSubject(subject);
        notification.setFromUser(this.getClass().getSimpleName());
        notification.setRecipientUsers(Collections.singletonList(this.getTo()));
        Map<String, Object> scope = new HashMap<>();
        scope.put(FileNotificationProcessor.JOB_KEY, job);
        model.createNotification(notification, new FileNotificationProcessor(this.getTo(), scope));
      }
    } catch (GenericException | AuthorizationDeniedException e) {
      throw new NotificationException(e);
    }
  }
}
