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

import org.roda.core.RodaCoreFactory;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.AlreadyExistsException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.notifications.Notification;
import org.roda.core.data.v2.notifications.Notification.NOTIFICATION_STATE;
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
      if (dropPath != null && dropPath.startsWith("file:///")) {
        LOGGER.debug("Sending notification via drop folder ...");
        Path trimmedDropPath = Paths.get(dropPath.substring(7));

        if (FSUtils.isDirectory(trimmedDropPath)) {
          try {
            Path jobPath = FSUtils.createDirectory(trimmedDropPath, job.getId());

            Path jobFilePath = RodaCoreFactory.getStoragePath().resolve(RodaConstants.STORAGE_CONTAINER_JOB)
              .resolve(job.getId() + RodaConstants.JOB_FILE_EXTENSION);
            FSUtils.copy(jobFilePath, jobPath.resolve(job.getId() + RodaConstants.JOB_FILE_EXTENSION), true);

            Path jobReportPath = RodaCoreFactory.getStoragePath().resolve(RodaConstants.STORAGE_CONTAINER_JOB_REPORT)
              .resolve(job.getId());
            FSUtils.copy(jobReportPath, jobPath.resolve(RodaConstants.RODA_OBJECT_REPORTS), true);

            FSUtils.createFile(jobPath, ".ready");

            LOGGER.debug("Notification sent");
            notification.setState(NOTIFICATION_STATE.COMPLETED);
          } catch (AlreadyExistsException | GenericException | IOException e) {
            LOGGER.warn("Notification not sent", e);
            notification.setState(NOTIFICATION_STATE.FAILED);
          }
        } else {
          LOGGER.warn("Drop path is not a folder, cannot send notification.");
          notification.setState(NOTIFICATION_STATE.FAILED);
        }
      } else {
        LOGGER.warn("No drop path, cannot send notification.");
        notification.setState(NOTIFICATION_STATE.FAILED);
      }
    }

    return notification;
  }

  private String createNotificationContent(Job job) {
    return JsonUtils.getJsonFromObject(job);
  }
}
