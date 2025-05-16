/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.notifications;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.roda.core.common.iterables.CloseableIterable;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.common.OptionalWithCause;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Report;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.notifications.NotificationState;
import org.roda.core.model.ModelService;
import org.roda.core.storage.fs.FSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileNotificationProcessor implements NotificationProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileNotificationProcessor.class);
  public static final String JOB_KEY = "job";

  private String dropPath;
  private Map<String, Object> scope;

  public FileNotificationProcessor(String dropPath, Map<String, Object> scope) {
    this.dropPath = dropPath;
    this.scope = scope;
  }

  @Override
  public Notification processNotification(ModelService model, Notification notification) {
    if (scope.containsKey(JOB_KEY)) {
      Job job = (Job) scope.get(JOB_KEY);
      String content = createNotificationContent(job);
      notification.setBody(content);
      notification.setState(NotificationState.FAILED);

      if (dropPath != null) {
        LOGGER.debug("Sending notification via drop folder ...");
        Path trimmedDropPath = Paths.get(FilenameUtils.normalize(dropPath.substring(7)));

        if (FSUtils.isDirectory(trimmedDropPath)) {
          try {
            Path jobPath = FSUtils.createDirectory(trimmedDropPath, job.getId());
            model.exportToPath(job, jobPath.resolve(job.getId() + RodaConstants.JOB_FILE_EXTENSION), true);
            try (CloseableIterable<OptionalWithCause<Report>> reports = model.listJobReports(job.getId())) {
              for (OptionalWithCause<Report> report : reports) {
                if (report.isPresent()) {
                  Path targetPath = jobPath.resolve(RodaConstants.RODA_OBJECT_REPORTS).resolve(report.get().getId());
                  model.exportToPath(report.get(), targetPath, true);
                }
              }
            }

            FSUtils.createFile(jobPath, ".ready");

            LOGGER.debug("Notification sent");
            notification.setState(NotificationState.COMPLETED);
          } catch (RODAException | IOException e) {
            LOGGER.warn("Notification not sent", e);
          }
        } else {
          LOGGER.warn("Drop path is not a folder, cannot send notification.");
        }
      } else {
        LOGGER.warn("No drop path, cannot send notification.");
      }
    }

    return notification;
  }

  private String createNotificationContent(Job job) {
    return JsonUtils.getJsonFromObject(job);
  }
}
