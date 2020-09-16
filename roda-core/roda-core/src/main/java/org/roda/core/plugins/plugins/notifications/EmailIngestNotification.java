/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.plugins.plugins.notifications;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.index.IndexService;
import org.roda.core.index.utils.IterableIndexResult;
import org.roda.core.model.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.base.CaseFormat;

public class EmailIngestNotification extends AbstractJobNotification {
  private static final Logger LOGGER = LoggerFactory.getLogger(EmailIngestNotification.class);

  public EmailIngestNotification(String emails) {
    super(emails);
  }

  public EmailIngestNotification(String emails, boolean whenFailed) {
    super(emails, whenFailed);
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

        String subject = RodaCoreFactory.getRodaConfigurationAsString("core", "notification", "ingest_subject");
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
        scopes.put("sips", jobStats.getSourceObjectsCount());
        scopes.put("success", jobStats.getSourceObjectsProcessedWithSuccess());
        scopes.put("failed", jobStats.getSourceObjectsProcessedWithFailure());
        scopes.put("name", job.getName());
        scopes.put("creator", job.getUsername());

        if (outcome.equals(PluginState.FAILURE.toString())) {
          Filter filter = new Filter();
          filter.add(new SimpleFilterParameter(RodaConstants.JOB_REPORT_JOB_ID, job.getId()));
          filter.add(new SimpleFilterParameter(RodaConstants.JOB_REPORT_PLUGIN_STATE, PluginState.FAILURE.toString()));
          try (IterableIndexResult<IndexedReport> reports = index.findAll(IndexedReport.class, filter,
            Collections.emptyList())) {
            StringBuilder builder = new StringBuilder();

            for (IndexedReport report : reports) {
              Report last = report.getReports().get(report.getReports().size() - 1);
              builder.append(last.getPluginDetails()).append("\n\n");
            }

            scopes.put("failures", new Handlebars.SafeString(builder.toString()));
          } catch (IOException e) {
            LOGGER.error("Error getting failed reports", e);
          }
        }

        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        scopes.put("start", parser.format(job.getStartDate()));

        long duration = (new Date().getTime() - job.getStartDate().getTime()) / 1000;
        scopes.put("duration", duration + " seconds");
        model.createNotification(notification,
          new EmailNotificationProcessor(RodaConstants.INGEST_EMAIL_TEMPLATE, scopes));
      }
    } catch (GenericException | AuthorizationDeniedException | RequestNotValidException e) {
      throw new NotificationException(e);
    }
  }
}
