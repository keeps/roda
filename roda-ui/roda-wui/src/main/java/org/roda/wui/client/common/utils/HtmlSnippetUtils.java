/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.RepresentationInformationUtils;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationState;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHoldAssociation;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.core.data.v2.disposal.schedule.DisposalActionCode;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.generics.MetadataValue;
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.DIP;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationEvent;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.JobParallelism;
import org.roda.core.data.v2.jobs.JobPriority;
import org.roda.core.data.v2.jobs.JobStats;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.notifications.NotificationState;
import org.roda.core.data.v2.risks.IncidenceStatus;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.SeverityLevel;
import org.roda.core.data.v2.synchronization.EntitySummary;
import org.roda.core.data.v2.synchronization.RODAInstance;
import org.roda.core.data.v2.synchronization.SynchronizingStatus;
import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.local.LocalInstance;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.BrowseRepresentation;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.browse.RepresentationInformationHelper;
import org.roda.wui.client.planning.RepresentationInformationAssociations;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;

import config.i18n.client.ClientMessages;

public class HtmlSnippetUtils {

  public static final SafeHtml LOADING = SafeHtmlUtils.fromSafeConstant(
    "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>");
  private static final String OPEN_SPAN_CLASS_LABEL_INFO = "<span class='label-info'>";
  private static final String OPEN_SPAN_CLASS_LABEL_DEFAULT = "<span class='label-default'>";
  private static final String OPEN_SPAN_CLASS_LABEL_DANGER = "<span class='label-danger'>";
  private static final String OPEN_SPAN_CLASS_LABEL_WARNING = "<span class='label-warning'>";
  private static final String OPEN_SPAN_CLASS_LABEL_SUCCESS = "<span class='label-success'>";
  private static final String OPEN_SPAN_ORIGINAL_LABEL_SUCCESS = "<span class='label-success browseRepresentationOriginalIcon'>";
  private static final String OPEN_H2_CLASS_LABEL_SUCCESS = "<span class='h2'>";
  private static final String OPEN_SPAN = "<span>";
  private static final String CLOSE_SPAN = "</span>";
  private static final String OPEN_DIV_FONT_STYLE_1_REM = "<div style='font-size: 1rem; padding-top:0.5rem;'>";
  private static final String OPEN_DIV = "<div>";
  private static final String ClOSE_DIV = "</div>";
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private HtmlSnippetUtils() {
    // do nothing
  }

  public static SafeHtml getDisposalPolicySummaryBadge(DisposalPolicySummary summary) {
    switch (summary.getPolicyStatus()) {
      case DESTROY:
      case CONFIRMATION:
      case OVERDUE:
      case ERROR:
        return getDisposalPolicySummaryBadge(summary.getMessage(), "");
      case REVIEW:
        return getDisposalPolicySummaryBadge(summary.getMessage(), "disposal-policy-summary-review");
      case HOLD:
        return getDisposalPolicySummaryBadge(summary.getMessage(), "disposal-policy-summary-hold");
      case RETAIN:
        return getDisposalPolicySummaryBadge(summary.getMessage(), "disposal-policy-summary-retain");
      case NONE:
      default:
        return getDisposalPolicySummaryBadge(summary.getMessage(), "disposal-policy-summary-none");
    }
  }

  public static SafeHtml getDisposalPolicySummaryBadge(String label, String extraCSS) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();

    b.append(SafeHtmlUtils.fromSafeConstant("<span class='badge-card "))
      .append(SafeHtmlUtils.fromSafeConstant(extraCSS)).append(SafeHtmlUtils.fromSafeConstant("'>"));

    b.append(SafeHtmlUtils.fromSafeConstant("<i class=\"fas fa-stopwatch\" style=\"color: white\"></i>"));

    b.append(SafeHtmlUtils.fromSafeConstant("<span> ")).append(SafeHtmlUtils.fromSafeConstant(label))
      .append(SafeHtmlUtils.fromSafeConstant(CLOSE_SPAN));

    return b.toSafeHtml();
  }

  public static SafeHtml getDisposalScheduleStateHtml(DisposalSchedule disposalSchedule) {
    SafeHtml ret = null;
    if (disposalSchedule != null && disposalSchedule.getState() != null) {
      SafeHtmlBuilder b = new SafeHtmlBuilder();
      switch (disposalSchedule.getState()) {
        case ACTIVE:
          b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS));
          break;
        case INACTIVE:
        default:
          b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DEFAULT));
          break;
      }

      b.append(SafeHtmlUtils.fromString(messages.disposalScheduleState(disposalSchedule.getState().toString())));
      b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_SPAN));
      ret = b.toSafeHtml();
    }
    return ret;
  }

  public static SafeHtml getDisposalHoldStateHtml(DisposalHoldAssociation disposalHoldAssociation) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    if (disposalHoldAssociation.getLiftedOn() != null) {
      b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DEFAULT));
      b.append(SafeHtmlUtils.fromString(messages.disposalHoldState(DisposalHoldState.LIFTED.toString())));
    } else {
      b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS));
      b.append(SafeHtmlUtils.fromString(messages.disposalHoldState(DisposalHoldState.ACTIVE.toString())));
    }
    b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_SPAN));

    return b.toSafeHtml();
  }

  public static SafeHtml getDisposalHoldStateHtml(DisposalHold disposalHold) {
    SafeHtml ret = null;
    if (disposalHold != null && disposalHold.getState() != null) {
      SafeHtmlBuilder b = new SafeHtmlBuilder();
      switch (disposalHold.getState()) {
        case ACTIVE:
          b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS));
          break;
        case LIFTED:
        default:
          b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DEFAULT));
          break;
      }

      b.append(SafeHtmlUtils.fromString(messages.disposalHoldState(disposalHold.getState().toString())));
      b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_SPAN));
      ret = b.toSafeHtml();
    }
    return ret;
  }

  public static SafeHtml getDisposalHoldStatusHTML(Boolean onHold) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    if (onHold) {
      b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_WARNING));
      b.append(SafeHtmlUtils.fromString(messages.disposalOnHoldStatusLabel()));
    } else {
      b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS));
      b.append(SafeHtmlUtils.fromString(messages.disposalClearStatusLabel()));
    }
    b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_SPAN));
    return b.toSafeHtml();
  }

  public static SafeHtml getJobParallelismTypeHtml(JobParallelism parallelism) {
    return getJobParallelismTypeHtml(parallelism, false);
  }

  public static SafeHtml getJobParallelismTypeHtml(JobParallelism parallelism, boolean appendDefinition) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();
    b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_INFO));

    if (appendDefinition) {
      b.append(SafeHtmlUtils.fromString(messages.jobParallelismLongBadge(parallelism)));
    } else {
      b.append(SafeHtmlUtils.fromString(messages.jobParallelismShortBadge(parallelism)));
    }

    b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_SPAN));

    return b.toSafeHtml();
  }

  public static SafeHtml getJobPriorityHtml(JobPriority priority, boolean appendDefinition) {
    SafeHtmlBuilder b = new SafeHtmlBuilder();

    switch (priority) {
      case URGENT:
        b.append(SafeHtmlUtils.fromSafeConstant("<span class='label-priority-urgent'>"));
        break;
      case HIGH:
        b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DANGER));
        break;
      case LOW:
        b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS));
        break;
      case MEDIUM:
      default:
        b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_WARNING));
        break;
    }

    if (appendDefinition) {
      b.append(SafeHtmlUtils.fromString(messages.jobPriorityLongBadge(priority)));
    } else {
      b.append(SafeHtmlUtils.fromString(messages.jobPriorityShortBadge(priority)));
    }

    b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_SPAN));

    return b.toSafeHtml();
  }

  public static SafeHtml getJobPriorityHtml(JobPriority priority) {
    return getJobPriorityHtml(priority, false);
  }

  public static SafeHtml getJobStateHtml(JOB_STATE jobState, JobStats jobStats) {
    SafeHtml ret = null;
    if (jobState != null && jobStats != null) {
      JOB_STATE state = jobState;
      if (JOB_STATE.COMPLETED.equals(state)) {
        if (jobStats.getSourceObjectsCount() == jobStats.getSourceObjectsProcessedWithSuccess()) {
          ret = SafeHtmlUtils
            .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS + messages.showJobStatusCompleted() + CLOSE_SPAN);
        } else if (jobStats.getSourceObjectsProcessedWithPartialSuccess() > 0) {
          ret = SafeHtmlUtils
            .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_WARNING + messages.showJobStatusCompleted() + CLOSE_SPAN);
        } else if (jobStats.getSourceObjectsProcessedWithSuccess() > 0) {
          ret = SafeHtmlUtils
            .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_WARNING + messages.showJobStatusCompleted() + CLOSE_SPAN);
        } else if (jobStats.getSourceObjectsProcessedWithSuccess() == 0
          && jobStats.getSourceObjectsProcessedWithSkipped() > 0) {
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
      } else if (JOB_STATE.PENDING_APPROVAL.equals(state)) {
        ret = SafeHtmlUtils
          .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_WARNING + messages.showJobStatusPendingApproval() + CLOSE_SPAN);
      } else if (JOB_STATE.SCHEDULED.equals(state)) {
        ret = SafeHtmlUtils
          .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_WARNING + messages.showJobStatusScheduled() + CLOSE_SPAN);
      } else if (JOB_STATE.REJECTED.equals(state)) {
        ret = SafeHtmlUtils
          .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DANGER + messages.showJobStatusApprovalRejected() + CLOSE_SPAN);
      } else if (JOB_STATE.TO_BE_CLEANED.equals(state)) {
        ret = SafeHtmlUtils
          .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_WARNING + messages.showJobStatusToBeCleaned() + CLOSE_SPAN);
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

  public static void getRepresentationTypeHTML(FlowPanel panel, String title, List<String> representationStates) {
    RepresentationInformationHelper.addFieldWithRepresentationInformationIcon(
      SafeHtmlUtils.fromSafeConstant(OPEN_H2_CLASS_LABEL_SUCCESS + title + CLOSE_SPAN), null, panel, false);

    for (String state : representationStates) {
      SafeHtmlBuilder b = new SafeHtmlBuilder();
      b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_ORIGINAL_LABEL_SUCCESS));
      b.append(SafeHtmlUtils.fromString(messages.statusLabel(state)));
      b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_SPAN));

      Anchor icon = new Anchor();
      icon.setHTML(b.toSafeHtml());
      icon.addStyleName("h6 representation-information-badge");

      final String filter = RepresentationInformationUtils.createRepresentationInformationFilter(
        RodaConstants.INDEX_REPRESENTATION, RodaConstants.REPRESENTATION_STATES, state);
      icon.setHref(HistoryUtils.createHistoryHashLink(RepresentationInformationAssociations.RESOLVER,
        RodaConstants.REPRESENTATION_INFORMATION_FILTERS, filter));

      panel.add(icon);
    }
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
      case PARTIAL_SUCCESS:
      case SKIPPED:
        pluginStateHTML = SafeHtmlUtils
          .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_WARNING + messages.pluginStateMessage(pluginState) + CLOSE_SPAN);
        break;
      case FAILURE:
      default:
        pluginStateHTML = SafeHtmlUtils
          .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DANGER + messages.pluginStateMessage(pluginState) + CLOSE_SPAN);
        break;
    }
    return pluginStateHTML;
  }

  public static SafeHtml getPluginMandatoryHTML(Boolean value) {
    SafeHtml pluginMandatoryHTML;

    if (value == null) {
      return SafeHtmlUtils.EMPTY_SAFE_HTML;
    }

    if (value) {
      pluginMandatoryHTML = SafeHtmlUtils
        .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_INFO + messages.mandatoryPlugin() + CLOSE_SPAN);
    } else {
      pluginMandatoryHTML = SafeHtmlUtils
        .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_INFO + messages.optionalPlugin() + CLOSE_SPAN);
    }

    return pluginMandatoryHTML;
  }

  public static SafeHtml getNotificationStateHTML(NotificationState state) {
    String label = messages.notificationStateValue(state);
    return getNotificationStateHTML(state, label);
  }

  public static SafeHtml getNotificationStateHTML(NotificationState state, String label) {
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

  public static SeverityLevel getSeverityLevel(int severity, int lowLimit, int highLimit) {
    if (severity < lowLimit) {
      return SeverityLevel.LOW;
    } else if (severity < highLimit) {
      return SeverityLevel.MODERATE;
    } else {
      return SeverityLevel.HIGH;
    }
  }

  public static SafeHtml getSeverityDefinition(int severity, int lowLimit, int highLimit) {
    return getSeverityDefinition(getSeverityLevel(severity, lowLimit, highLimit));
  }

  public static SafeHtml getSeverityDefinition(SeverityLevel level) {
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

  public static SafeHtml getStatusDefinition(IncidenceStatus status) {
    if (status.equals(IncidenceStatus.UNMITIGATED)) {
      return SafeHtmlUtils
        .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DANGER + messages.riskIncidenceStatusValue(status) + CLOSE_SPAN);
    } else {
      return SafeHtmlUtils
        .fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS + messages.riskIncidenceStatusValue(status) + CLOSE_SPAN);
    }
  }

  public static SafeHtml getLogEntryStateHtml(LogEntryState state) {
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
      objectLink.setHref(HistoryUtils.createHistoryHashLink(BrowseTop.RESOLVER, incidence.getAipId()));
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

  public static void createExtraShow(FlowPanel panel, Set<MetadataValue> bundle, boolean addStyle) {
    FlowPanel lastSeparator = null;
    boolean hasFields = false;

    for (MetadataValue mv : bundle) {
      boolean mandatory = (mv.get("mandatory") != null && "true".equalsIgnoreCase(mv.get("mandatory"))) ? true : false;

      if (mv.get("hidden") != null && "true".equals(mv.get("hidden"))) {
        continue;
      }

      FlowPanel layout = new FlowPanel();
      String controlType = mv.get("type");

      if (controlType == null) {
        addField(panel, layout, mv, mandatory);
      } else if ("separator".equals(controlType)) {
        if (lastSeparator != null && !hasFields) {
          lastSeparator.setVisible(false);
        }

        addSeparator(panel, layout, mv);
        lastSeparator = layout;
        hasFields = false;
      } else if ("text-area".equals(controlType) || "rich-text-area".equals(controlType)) {
        boolean addedField = addHTML(panel, layout, mv, mandatory);
        hasFields = hasFields || addedField;
      } else {
        boolean addedField = addField(panel, layout, mv, mandatory);
        hasFields = hasFields || addedField;
      }
    }

    if (lastSeparator != null && !hasFields) {
      lastSeparator.setVisible(false);
    }
  }

  private static String getFieldLabel(MetadataValue mv) {
    String result = mv.getId();
    String rawLabel = mv.get("label");
    if (rawLabel != null && rawLabel.length() > 0) {
      String loc = LocaleInfo.getCurrentLocale().getLocaleName();
      try {
        JSONObject jsonObject = JSONParser.parseLenient(rawLabel).isObject();
        JSONValue jsonValue = jsonObject.get(loc);
        if (jsonValue != null) {
          JSONString jsonString = jsonValue.isString();
          if (jsonString != null) {
            result = jsonString.stringValue();
          }
        } else {
          if (loc.contains("_")) {
            jsonValue = jsonObject.get(loc.split("_")[0]);
            if (jsonValue != null) {
              JSONString jsonString = jsonValue.isString();
              if (jsonString != null) {
                result = jsonString.stringValue();
              }
            }
          }
          // label for the desired language doesn't exist
          // do nothing
        }
      } catch (JSONException e) {
        // The JSON was malformed
        // do nothing
      }
    }
    mv.set("l", result);
    return result;
  }

  private static boolean addField(FlowPanel panel, final FlowPanel layout, final MetadataValue mv,
    final boolean mandatory) {
    layout.addStyleName("field");

    if (StringUtils.isNotBlank(mv.get("value"))) {
      // Top label
      Label mvLabel = new Label(getFieldLabel(mv));
      mvLabel.addStyleName("label");

      // Field
      final Label mvText = new Label();
      mvText.setTitle(mvLabel.getText());
      mvText.addStyleName("value");
      mvText.setText(mv.get("value"));

      layout.add(mvLabel);
      layout.add(mvText);
      panel.add(layout);
      return true;
    } else {
      return false;
    }
  }

  private static boolean addHTML(FlowPanel panel, final FlowPanel layout, final MetadataValue mv,
    final boolean mandatory) {
    layout.addStyleName("field");

    if (StringUtils.isNotBlank(mv.get("value"))) {
      // Top label
      Label mvLabel = new Label(getFieldLabel(mv));
      mvLabel.addStyleName("label");

      // Field
      final HTML mvText = new HTML();
      mvText.addStyleName("value ri-html-content rich-text-value");
      mvText.setHTML(SafeHtmlUtils.fromString(mv.get("value")));

      layout.add(mvLabel);
      layout.add(mvText);
      panel.add(layout);
      return true;
    } else {
      return false;
    }
  }

  private static void addSeparator(FlowPanel panel, final FlowPanel layout, final MetadataValue mv) {
    layout.addStyleName("form-separator");
    Label mvLabel = new Label(getFieldLabel(mv));
    layout.add(mvLabel);
    panel.add(layout);
  }

  public static SafeHtml getIngestProcessJobListIcon(String innerIcon, String outerIcon, String tooltip) {
    String beginSpan = "<span style=\"font-size: 0.5em\" class=\"fa-stack fa-xs\" title=\"" + tooltip + "\">";
    String innerIconTag = "<i class=\"" + innerIcon + " fa-stack-1x\"></i>";
    String outerIconTag = "<i class=\"" + outerIcon + " fa-stack-2x\"></i>";
    String endSpan = "</span>";

    return SafeHtmlUtils.fromSafeConstant(beginSpan + innerIconTag + outerIconTag + endSpan);
  }

  public static SafeHtml getIngestProcessJobListIcon(String icon, String tooltip) {
    String beginSpan = "<span style=\"font-size: 0.5em\" class=\"fa-stack fa-xs\" title=\"" + tooltip + "\">";
    String innerIcon = "<i class=\"" + icon + " fa-stack-1x\"></i>";
    String outerIcon = "<i class=\"far fa-circle fa-stack-2x\"></i>";
    String endSpan = "</span>";

    return SafeHtmlUtils.fromSafeConstant(beginSpan + innerIcon + outerIcon + endSpan);
  }

  public static SafeHtml getStackIcon(String innerIcon, String outerIcon) {
    String beginSpan = "<span class=\"fa-stack\">";
    String innerIconTag = "<i class=\"" + innerIcon + " fa-stack-1x\"></i>";
    String outerIconTag = "<i class=\"" + outerIcon + " fa-stack-1x\"></i>";
    String endSpan = "</span>";

    return SafeHtmlUtils.fromSafeConstant(beginSpan + innerIconTag + outerIconTag + endSpan);
  }

  public static SafeHtml getDisposalConfirmationStateHTML(DisposalConfirmationState state) {
    String labelClass;

    switch (state) {
      case PENDING:
        labelClass = "label-warning";
        break;
      case APPROVED:
        labelClass = "label-success";
        break;
      case RESTORED:
        labelClass = "label-info";
        break;
      case PERMANENTLY_DELETED:
        labelClass = "label-danger";
        break;
      case EXECUTION_FAILED:
        labelClass = "label-danger";
      default:
        labelClass = "label-default";
        break;
    }

    return SafeHtmlUtils
      .fromSafeConstant("<span class='" + labelClass + "'>" + messages.disposalConfirmationState(state) + CLOSE_SPAN);
  }

  public static String getTransferredResourceStateHTML(String state) {
    String response = "";

    switch (state) {
      case "Deleted":
       response = OPEN_SPAN_CLASS_LABEL_DANGER + messages.sipDeleted() + CLOSE_SPAN;
        break;
      }

    return response;
  }

  public static SafeHtml getDisposalScheduleActionHtml(DisposalActionCode disposalAction) {
    String labelClass;

    switch (disposalAction) {
      case DESTROY:
        labelClass = "label-danger";
        break;
      case RETAIN_PERMANENTLY:
        labelClass = "label-info";
        break;
      case REVIEW:
        labelClass = "label-warning";
        break;
      default:
        labelClass = "label-default";
        break;
    }

    return SafeHtmlUtils.fromSafeConstant(
      "<span class='" + labelClass + "'>" + messages.disposalScheduleActionCode(disposalAction.name()) + CLOSE_SPAN);
  }

  /**
   * Create a {@link SafeHtml} with the status and the issues in synchronization.
   *
   * @param distributedInstance
   *          {@link DistributedInstance}.
   * @param togetherWithStatus
   *          if this flag is true put the issues together with the status
   *
   * @return {@link SafeHtml}.
   */
  public static SafeHtml getDistributedInstanceStateHtml(final DistributedInstance distributedInstance,
    final boolean togetherWithStatus) {
    SafeHtml ret = null;
    if (distributedInstance != null && distributedInstance.getStatus() != null) {
      SafeHtmlBuilder b = new SafeHtmlBuilder();
      switch (distributedInstance.getStatus()) {
        case CREATED:
          b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_WARNING));
          break;
        case ACTIVE:
          b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS));
          break;
        case INACTIVE:
        default:
          b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DEFAULT));
          break;
      }

      b.append(SafeHtmlUtils.fromString(messages.distributedInstanceStatusValue(distributedInstance.getStatus())));
      b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_SPAN));
      b.append(SafeHtmlUtils.fromString(" "));
      final SafeHtmlBuilder syncErrorsBuilder = new SafeHtmlBuilder();
      if (togetherWithStatus) {
        if (distributedInstance.getSyncErrors() == 0) {
          syncErrorsBuilder.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS));
        } else {
          syncErrorsBuilder.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DANGER));
        }
        syncErrorsBuilder.append(SafeHtmlUtils.fromString(String.valueOf(distributedInstance.getSyncErrors()) + " "));
        syncErrorsBuilder.append(SafeHtmlUtils.fromString(messages.distributedInstanceSyncErrorsLabel()));
        syncErrorsBuilder.append(SafeHtmlUtils.fromSafeConstant(CLOSE_SPAN));
      }
      b.append(syncErrorsBuilder.toSafeHtml());
      ret = b.toSafeHtml();
    }
    return ret;
  }

  public static SafeHtml getLastSyncHtml(final RODAInstance rodaInstance, final boolean showIssues) {
    SafeHtml ret = null;
    final SafeHtmlBuilder lastSyncBuilder = new SafeHtmlBuilder();
    // div to last sync date
    lastSyncBuilder.append(SafeHtmlUtils.fromSafeConstant("<div>"));
    if (rodaInstance.getLastSynchronizationDate() != null) {
      lastSyncBuilder
        .append(SafeHtmlUtils.fromString(Humanize.formatDateTime(rodaInstance.getLastSynchronizationDate())));
    } else {
      lastSyncBuilder.append(SafeHtmlUtils.fromString(messages.permanentlyRetained()));
    }
    lastSyncBuilder.append(SafeHtmlUtils.fromSafeConstant("</div>"));

    // div to entities
    List<EntitySummary> entitySummaries = rodaInstance.getEntitySummaryList();
    if (entitySummaries != null && !entitySummaries.isEmpty()) {
      lastSyncBuilder.append(SafeHtmlUtils.fromSafeConstant(OPEN_DIV));
      for (final EntitySummary entitySummary : entitySummaries) {
        String entityClassUiName = getNameByEntityClassName(entitySummary.getEntityClass());
        String entityClass = getEntityClassNameSplit(entitySummary.getEntityClass());
        SafeUri downloadUriRemoved = RestUtils.createLastSynchronizationDownloadUri(rodaInstance.getId(), entityClass,
          RodaConstants.SYNCHRONIZATION_REPORT_KEY_REMOVED);
        SafeUri downloadUriIssue = RestUtils.createLastSynchronizationDownloadUri(rodaInstance.getId(), entityClass,
          RodaConstants.SYNCHRONIZATION_REPORT_KEY_ISSUES);

        lastSyncBuilder.append(SafeHtmlUtils.fromSafeConstant(OPEN_DIV_FONT_STYLE_1_REM));
        lastSyncBuilder.append(SafeHtmlUtils.fromString(entityClassUiName + ": "));
        // Added/ Updated
        createAddedUpdatedSpan(lastSyncBuilder, entitySummary, entitySummary.getCountAddedUpdated());
        // Removed
        createRemovedAndIssuesLink(lastSyncBuilder, entitySummary.getCountRemoved(),
          messages.distributedInstanceRemovedEntitiesLabel(), downloadUriRemoved, "label-warning");
        // Issues
        if (showIssues) {
          createRemovedAndIssuesLink(lastSyncBuilder, entitySummary.getCountIssues(),
            messages.distributedInstanceSyncErrorsLabel(), downloadUriIssue, "label-danger");
        }
        lastSyncBuilder.append(SafeHtmlUtils.fromSafeConstant(ClOSE_DIV));
      }
      lastSyncBuilder.append(SafeHtmlUtils.fromSafeConstant(ClOSE_DIV));
    }
    ret = lastSyncBuilder.toSafeHtml();
    return ret;
  }

  private static void createAddedUpdatedSpan(SafeHtmlBuilder safeHtmlBuilder, EntitySummary entitySummary, int count) {
    if (count > 0) {
      safeHtmlBuilder.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS));
      safeHtmlBuilder.append(SafeHtmlUtils.fromString(String.valueOf(entitySummary.getCountAddedUpdated()) + " "
        + messages.distributedInstanceUpdatedEntitiesLabel()));
      safeHtmlBuilder.append(SafeHtmlUtils.fromSafeConstant(CLOSE_SPAN));
    }
  }

  private static void createRemovedAndIssuesLink(SafeHtmlBuilder safeHtmlBuilder, int count, String message,
    SafeUri safeUri, String labelStyle) {
    if (count > 0) {
      safeHtmlBuilder.append(SafeHtmlUtils.fromSafeConstant(" <a href='"));
      safeHtmlBuilder.append(SafeHtmlUtils.fromString(safeUri.asString()));
      safeHtmlBuilder.append(SafeHtmlUtils.fromSafeConstant("' class='" + labelStyle + "'>"));
      safeHtmlBuilder.append(SafeHtmlUtils.fromString(String.valueOf(count) + " " + message));
      safeHtmlBuilder.append(SafeHtmlUtils.fromSafeConstant("</a>"));
    }
  }

  private static String getNameByEntityClassName(String entityClass) {
    if (AIP.class.getName().equals(entityClass)) {
      return messages.intellectualEntity();
    } else if (DIP.class.getName().equals(entityClass)) {
      return messages.catalogueDIPTitle();
    } else if (IndexedPreservationEvent.class.getName().equals(entityClass)) {
      return messages.preservationEventsTitle();
    } else if (IndexedPreservationAgent.class.getName().equals(entityClass)) {
      return messages.preservationAgentsTitle();
    } else if (Job.class.getName().equals(entityClass)) {
      return messages.processTitle();
    } else {
      return getEntityClassNameSplit(entityClass);
    }
  }

  private static String getEntityClassNameSplit(String entityClass) {
    String[] splitEntityClass = entityClass.split("\\.");
    String entityClassName = splitEntityClass[splitEntityClass.length - 1];
    return entityClassName;
  }

  public static SafeHtml getAccessKeyStateHtml(AccessKey accessKey) {
    SafeHtml ret = null;
    if (accessKey != null && accessKey.getStatus() != null) {
      SafeHtmlBuilder b = new SafeHtmlBuilder();
      switch (accessKey.getStatus()) {
        case CREATED:
          b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_WARNING));
          break;
        case ACTIVE:
          b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS));
          break;
        case REVOKED:
          b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DANGER));
          break;
        case INACTIVE:
        default:
          b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DEFAULT));
          break;
      }

      b.append(SafeHtmlUtils.fromString(messages.accessKeyStatusValue(accessKey.getStatus())));
      b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_SPAN));
      ret = b.toSafeHtml();
    }
    return ret;
  }

  public static SafeHtml getInstanceIdStateHtml(LocalInstance localInstance) {
    SafeHtml ret = null;
    if (localInstance != null) {
      SafeHtmlBuilder b = new SafeHtmlBuilder();
      if (SynchronizingStatus.ACTIVE.equals(localInstance.getStatus())) {
        b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS));
        b.append(SafeHtmlUtils.fromString(messages.synchronizingStatus(localInstance.getStatus())));
      } else if (SynchronizingStatus.APPLYINGIDENTIFIER.equals(localInstance.getStatus())) {
        b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_INFO));
        b.append(SafeHtmlUtils.fromString(messages.synchronizingStatus(localInstance.getStatus())));
      } else if (SynchronizingStatus.SYNCHRONIZING.equals(localInstance.getStatus())) {
        b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_WARNING));
        b.append(SafeHtmlUtils.fromString(messages.synchronizingStatus(localInstance.getStatus())));
      } else {
        b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DEFAULT));
        b.append(SafeHtmlUtils.fromString(messages.synchronizingStatus(localInstance.getStatus())));
      }

      b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_SPAN));
      ret = b.toSafeHtml();
    }
    return ret;
  }

  public static SafeHtml getUserStateHtml(User user) {
    SafeHtml ret = null;
    if (user != null) {
      SafeHtmlBuilder b = new SafeHtmlBuilder();
      if (user.isActive()) {
        b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_SUCCESS));
        b.append(SafeHtmlUtils.fromString(messages.showUserActivated()));
      } else {
        b.append(SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_DEFAULT));
        b.append(SafeHtmlUtils.fromString(messages.showUserDeactivated()));
      }
      b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_SPAN));
      ret = b.toSafeHtml();
    }
    return ret;
  }
}
