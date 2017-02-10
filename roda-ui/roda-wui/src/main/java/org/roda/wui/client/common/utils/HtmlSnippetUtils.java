/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import java.util.List;

import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntry.LOG_ENTRY_STATE;
import org.roda.core.data.v2.notifications.Notification.NOTIFICATION_STATE;
import org.roda.core.data.v2.risks.Risk.SEVERITY_LEVEL;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.RiskIncidence.INCIDENCE_STATUS;
import org.roda.wui.client.browse.BrowseAIP;
import org.roda.wui.client.browse.BrowseRepresentation;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.UIObject;

import config.i18n.client.ClientMessages;

public class HtmlSnippetUtils {

  private static final String OPEN_SPAN_CLASS_LABEL_INFO = "<span class='label-info'>";

  private static final String OPEN_SPAN_CLASS_LABEL_DEFAULT = "<span class='label-default'>";

  private static final String OPEN_SPAN_CLASS_LABEL_DANGER = "<span class='label-danger'>";

  private static final String OPEN_SPAN_CLASS_LABEL_WARNING = "<span class='label-warning'>";

  private static final String OPEN_SPAN_CLASS_LABEL_SUCCESS = "<span class='label-success'>";

  private static final String CLOSE_SPAN = "</span>";

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
            .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS + messages.showJobStatusCompleted() + CLOSE_SPAN);
        } else if (job.getJobStats().getSourceObjectsProcessedWithSuccess() > 0) {
          ret = SafeHtmlUtils
            .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_WARNING + messages.showJobStatusCompleted() + CLOSE_SPAN);
        } else {
          ret = SafeHtmlUtils
            .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DANGER + messages.showJobStatusCompleted() + CLOSE_SPAN);
        }
      } else if (JOB_STATE.FAILED_DURING_CREATION.equals(state)) {
        ret = SafeHtmlUtils
          .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DEFAULT + messages.showJobStatusFailedDuringCreation() + CLOSE_SPAN);
      } else if (JOB_STATE.FAILED_TO_COMPLETE.equals(state)) {
        ret = SafeHtmlUtils
          .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DEFAULT + messages.showJobStatusFailedToComplete() + CLOSE_SPAN);
      } else if (JOB_STATE.STOPPING.equals(state)) {
        ret = SafeHtmlUtils
          .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DEFAULT + messages.showJobStatusStopping() + CLOSE_SPAN);
      } else if (JOB_STATE.STOPPED.equals(state)) {
        ret = SafeHtmlUtils
          .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DEFAULT + messages.showJobStatusStopped() + CLOSE_SPAN);
      } else if (JOB_STATE.CREATED.equals(state)) {
        ret = SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_INFO + messages.showJobStatusCreated() + CLOSE_SPAN);
      } else if (JOB_STATE.STARTED.equals(state)) {
        ret = SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_INFO + messages.showJobStatusStarted() + CLOSE_SPAN);
      } else {
        ret = SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_WARNING + state + CLOSE_SPAN);
      }
    }
    return ret;
  }

  public static SafeHtml getAIPStateHTML(AIPState aipState) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();

    switch (aipState) {
      case ACTIVE:
        b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS));
        break;
      case UNDER_APPRAISAL:
        b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_WARNING));
        break;
      default:
        b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DANGER));
        break;
    }

    b.append(SafeHtmlUtils.fromString(messages.aipState(aipState)));
    b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_SPAN));

    return b.toSafeHtml();
  }

  public static SafeHtml getPluginStateHTML(PluginState pluginState) {
    SafeHtml pluginStateHTML;
    switch (pluginState) {
      case SUCCESS:
        pluginStateHTML = SafeHtmlUtils
          .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS + messages.pluginStateMessage(pluginState) + CLOSE_SPAN);
        break;
      case RUNNING:
        pluginStateHTML = SafeHtmlUtils
          .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DEFAULT + messages.pluginStateMessage(pluginState) + CLOSE_SPAN);
        break;
      case FAILURE:
      default:
        pluginStateHTML = SafeHtmlUtils
          .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DANGER + messages.pluginStateMessage(pluginState) + CLOSE_SPAN);
        break;
    }
    return pluginStateHTML;
  }

  public static SafeHtml getNotificationStateHTML(NOTIFICATION_STATE state) {
    GWT.log("STATE1: " + state);
    String label = messages.notificationStateValue(state);
    GWT.log("STATE2: " + label);
    return getNotificationStateHTML(state, label);
  }

  public static SafeHtml getNotificationStateHTML(NOTIFICATION_STATE state, String label) {
    SafeHtml notificationStateHTML;
    switch (state) {
      case COMPLETED:
        notificationStateHTML = SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS + label + CLOSE_SPAN);
        break;
      case FAILED:
        notificationStateHTML = SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DANGER + label + CLOSE_SPAN);
        break;
      case CREATED:
      default:
        notificationStateHTML = SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DEFAULT + label + CLOSE_SPAN);
        break;
    }
    return notificationStateHTML;
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

  public static SafeHtml getSeverityDefinition(int severity, int lowLimit, int highLimit) {
    return getSeverityDefinition(getSeverityLevel(severity, lowLimit, highLimit));
  }

  public static SafeHtml getSeverityDefinition(SEVERITY_LEVEL level) {
    SafeHtml ret;
    switch (level) {
      case LOW:
        ret = SafeHtmlUtils
          .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS + messages.severityLevel(level) + CLOSE_SPAN);
        break;
      case MODERATE:
        ret = SafeHtmlUtils
          .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_WARNING + messages.severityLevel(level) + CLOSE_SPAN);
        break;
      case HIGH:
      default:
        ret = SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DANGER + messages.severityLevel(level) + CLOSE_SPAN);
        break;
    }
    return ret;
  }

  public static SafeHtml getStatusDefinition(INCIDENCE_STATUS status) {
    if (status.equals(INCIDENCE_STATUS.UNMITIGATED)) {
      return SafeHtmlUtils
        .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DANGER + messages.riskIncidenceStatusValue(status) + CLOSE_SPAN);
    } else {
      return SafeHtmlUtils
        .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS + messages.riskIncidenceStatusValue(status) + CLOSE_SPAN);
    }
  }

  public static SafeHtml getLogEntryStateHtml(LOG_ENTRY_STATE state) {
    String labelClass;

    switch (state) {
      case SUCCESS:
        labelClass = "label-success";
        break;

      case FAILURE:
        labelClass = "label-danger";
        break;

      case UNAUTHORIZED:
        labelClass = "label-warning";
        break;

      default:
        labelClass = "label-default";
        break;
    }

    return SafeHtmlUtils
      .fromSafeConstant("<span class='" + labelClass + "'>" + messages.logEntryStateValue(state) + CLOSE_SPAN);
  }

  public static void addRiskIncidenceObjectLinks(RiskIncidence incidence, final Label objectLabel,
    final Anchor objectLink) {
    if (AIP.class.getSimpleName().equals(incidence.getObjectClass())) {
      objectLabel.setText(messages.showAIPExtended());
      objectLink.setHref(HistoryUtils.createHistoryHashLink(BrowseAIP.RESOLVER, incidence.getAipId()));
      objectLink.setText(incidence.getAipId());

    } else if (Representation.class.getSimpleName().equals(incidence.getObjectClass())) {
      objectLabel.setText(messages.showRepresentationExtended());
      objectLink.setHref(HistoryUtils.createHistoryHashLink(BrowseRepresentation.RESOLVER, incidence.getAipId(),
        incidence.getRepresentationId()));
      objectLink.setText(incidence.getRepresentationId());

    } else if (File.class.getSimpleName().equals(incidence.getObjectClass())) {
      objectLabel.setText(messages.showFileExtended());
      objectLink.setHref(HistoryUtils.createHistoryHashLink(HistoryUtils.getHistoryBrowse(incidence.getAipId(),
        incidence.getRepresentationId(), incidence.getFilePath(), incidence.getFileId())));
      objectLink.setText(incidence.getFileId());
    }
  }

  public static SafeHtml getLogEntryComponent(LogEntry entry, List<FacetFieldResult> facets) {
    String html = null;
    if (facets != null) {
      for (FacetFieldResult ffr : facets) {
        if ("actionComponent".equalsIgnoreCase(ffr.getField()) && ffr.getValues() != null) {
          for (FacetValue fv : ffr.getValues()) {
            if (fv.getValue().equalsIgnoreCase(entry.getActionComponent())) {
              html = fv.getLabel();
              break;
            }

          }
        }
        if (html != null) {
          break;
        }
      }
    }
    if (html == null) {
      html = entry.getActionComponent();
    }
    return SafeHtmlUtils.fromSafeConstant(html);
  }

  public static final void setCssClassDisabled(UIObject uiobject, boolean disabled) {
    if (disabled) {
      uiobject.addStyleName("disabled");
    } else {
      uiobject.removeStyleName("disabled");
    }
  }

}
