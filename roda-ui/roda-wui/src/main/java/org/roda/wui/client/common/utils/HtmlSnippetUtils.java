/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.risks.Risk.SEVERITY_LEVEL;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import config.i18n.client.ClientMessages;

public class HtmlSnippetUtils {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static final SafeHtml LOADING = SafeHtmlUtils.fromSafeConstant(
    "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>");

  public static SafeHtml getJobStateHtml(Job job) {
    SafeHtml ret = null;
    if (job != null) {
      JOB_STATE state = job.getState();
      if (JOB_STATE.COMPLETED.equals(state)) {
        if (job.getJobStats().getSourceObjectsCount() == job.getJobStats().getSourceObjectsProcessedWithSuccess()) {
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

  public static SafeHtml getAIPStateHTML(AIPState aipState) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();

    switch (aipState) {
      case ACTIVE:
        b.append(SafeHtmlUtils.fromSafeConstant("<span class='label-success'>"));
        break;
      case UNDER_APPRAISAL:
        b.append(SafeHtmlUtils.fromSafeConstant("<span class='label-warning'>"));
        break;
      default:
        b.append(SafeHtmlUtils.fromSafeConstant("<span class='label-danger'>"));
        break;
    }

    b.append(SafeHtmlUtils.fromString(messages.aipState(aipState)));
    b.append(SafeHtmlUtils.fromSafeConstant("</span>"));

    return b.toSafeHtml();
  }

  public static SafeHtml getPluginStateHTML(PluginState pluginState) {
    SafeHtml pluginStateHTML;
    switch (pluginState) {
      case SUCCESS:
        pluginStateHTML = SafeHtmlUtils
          .fromSafeConstant("<span class='label-success'>" + messages.pluginStateMessage(pluginState) + "</span>");
        break;
      case RUNNING:
        pluginStateHTML = SafeHtmlUtils
          .fromSafeConstant("<span class='label-default'>" + messages.pluginStateMessage(pluginState) + "</span>");
        break;
      case FAILURE:
      default:
        pluginStateHTML = SafeHtmlUtils
          .fromSafeConstant("<span class='label-danger'>" + messages.pluginStateMessage(pluginState) + "</span>");
        break;
    }
    return pluginStateHTML;
  }

  public static SEVERITY_LEVEL getSeverityLevel(int severity, int lowLimit, int highLimit) {
    if (severity < lowLimit) {
      return SEVERITY_LEVEL.LOW;
    } else if (severity < highLimit) {
      return SEVERITY_LEVEL.MODERATE;
    } else {
      return SEVERITY_LEVEL.HIGH;
    }
  }

  public static SafeHtml getSeverityDefinition(SEVERITY_LEVEL level) {
    SafeHtml ret;
    switch (level) {
      case LOW:
        ret = SafeHtmlUtils
          .fromSafeConstant("<span class='label-success'>" + messages.severityLevel(level) + "</span>");
        break;
      case MODERATE:
        ret = SafeHtmlUtils
          .fromSafeConstant("<span class='label-warning'>" + messages.severityLevel(level) + "</span>");
        break;
      case HIGH:
      default:
        ret = SafeHtmlUtils.fromSafeConstant("<span class='label-danger'>" + messages.severityLevel(level) + "</span>");
        break;
    }
    return ret;
  }

  public static SafeHtml getSeverityDefinition(int severity, int lowLimit, int highLimit) {
    return getSeverityDefinition(getSeverityLevel(severity, lowLimit, highLimit));
  }

}
