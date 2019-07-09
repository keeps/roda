package org.roda.core.plugins.plugins.notifications;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.RodaCoreFactory;
import org.roda.core.common.notifications.EmailNotificationProcessor;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotificationException;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.index.IndexService;
import org.roda.core.model.ModelService;

import com.google.common.base.CaseFormat;

public class EmailGenericNotification extends AbstractJobNotification implements GenericJobNotification {
  public EmailGenericNotification(String to) {
    super(to);
  }

  public EmailGenericNotification(String to, boolean whenFailed) {
    super(to, whenFailed);
  }

  @Override
  public void notify(ModelService model, IndexService index, Job job, JobStats jobStats) throws NotificationException {
    try {
      if (StringUtils.isNotBlank(this.getTo())) {
        List<String> emailList = new ArrayList<>(Arrays.asList(this.getTo().split("\\s*,\\s*")));
        Notification notification = new Notification();
        String outcome = PluginState.SUCCESS.toString();

        if (jobStats.getSourceObjectsProcessedWithFailure() > 0) {
          outcome = PluginState.FAILURE.toString();
        }

        String subject = RodaCoreFactory.getRodaConfigurationAsString("core", "notification", "default_subject");
        if (StringUtils.isNotBlank(subject)) {
          subject = subject.replaceAll("\\{RESULT\\}", outcome);
        } else {
          subject = outcome;
        }

        notification.setSubject(subject);
        notification.setFromUser(this.getClass().getSimpleName());
        notification.setRecipientUsers(emailList);

        Map<String, Object> scopes = new HashMap<>();
        scopes.put("outcome", outcome);
        scopes.put("type", CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, job.getPluginType().toString()));
        scopes.put("sources", jobStats.getSourceObjectsCount());
        scopes.put("success", jobStats.getSourceObjectsProcessedWithSuccess());
        scopes.put("failed", jobStats.getSourceObjectsProcessedWithFailure());
        scopes.put("name", job.getName());
        scopes.put("creator", job.getUsername());

        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        scopes.put("start", parser.format(job.getStartDate()));

        long duration = (new Date().getTime() - job.getStartDate().getTime()) / 1000;
        scopes.put("duration", duration + " seconds");
        model.createNotification(notification,
          new EmailNotificationProcessor(RodaConstants.GENERIC_EMAIL_TEMPLATE, scopes));
      }
    } catch (GenericException | AuthorizationDeniedException e) {
      throw new NotificationException(e);
    }
  }
}
