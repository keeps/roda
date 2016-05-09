package org.roda.wui.client.common.utils;

import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import config.i18n.client.BrowseMessages;

public class HtmlSnippetUtils {

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  public static SafeHtml getJobStateHtml(Job job) {
    SafeHtml ret = null;
    if (job != null) {
      JOB_STATE state = job.getState();
      if (JOB_STATE.COMPLETED.equals(state)) {
        if (job.getObjectsCount() == job.getObjectsProcessedWithSuccess()) {
          ret = SafeHtmlUtils
            .fromSafeConstant("<span class='label-success'>" + messages.showJobStatusCompleted() + "</span>");
        } else {
          ret = SafeHtmlUtils
            .fromSafeConstant("<span class='label-danger'>" + messages.showJobStatusCompleted() + "</span>");
        }
      } else if (JOB_STATE.FAILED_DURING_CREATION.equals(state)) {
        ret = SafeHtmlUtils
          .fromSafeConstant("<span class='label-default'>" + messages.showJobStatusFailedDuringCreation() + "</span>");
      } else if (JOB_STATE.FAILED_TO_COMPLETE.equals(state)) {
        ret = SafeHtmlUtils
          .fromSafeConstant("<span class='label-default'>" + messages.showJobStatusFailedToComplete() + "</span>");
      } else if (JOB_STATE.CREATED.equals(state)) {
        ret = SafeHtmlUtils.fromSafeConstant("<span class='label-info'>" + messages.showJobStatusCreated() + "</span>");
      } else if (JOB_STATE.STARTED.equals(state)) {
        ret = SafeHtmlUtils.fromSafeConstant("<span class='label-info'>" + messages.showJobStatusStarted() + "</span>");
      } else {
        ret = SafeHtmlUtils.fromSafeConstant("<span class='label-warning'>" + state + "</span>");
      }
    }
    return ret;
  }

}
