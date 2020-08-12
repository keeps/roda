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
import org.roda.core.data.v2.index.facet.FacetFieldResult;
import org.roda.core.data.v2.index.facet.FacetValue;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.PluginState;
import org.roda.core.data.v2.log.LogEntryState;
import org.roda.core.data.v2.notifications.NotificationState;
import org.roda.core.data.v2.log.LogEntry;
import org.roda.core.data.v2.risks.IncidenceStatus;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.core.data.v2.risks.SeverityLevel;
import org.roda.wui.client.browse.BrowseRepresentation;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.browse.MetadataValue;
import org.roda.wui.client.browse.RepresentationInformationHelper;
import org.roda.wui.client.planning.RepresentationInformationAssociations;
import org.roda.wui.common.client.tools.HistoryUtils;
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
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.UIObject;

import config.i18n.client.ClientMessages;

public class HtmlSnippetUtils {

  private static final String OPEN_SPAN_CLASS_LABEL_INFO = "<span class='label-info'>";
  private static final String OPEN_SPAN_CLASS_LABEL_DEFAULT = "<span class='label-default'>";
  private static final String OPEN_SPAN_CLASS_LABEL_DANGER = "<span class='label-danger'>";
  private static final String OPEN_SPAN_CLASS_LABEL_WARNING = "<span class='label-warning'>";
  private static final String OPEN_SPAN_CLASS_LABEL_SUCCESS = "<span class='label-success'>";

  private static final String OPEN_SPAN_ORIGINAL_LABEL_SUCCESS = "<span class='label-success browseRepresentationOriginalIcon'>";
  private static final String OPEN_H2_CLASS_LABEL_SUCCESS = "<span class='h2'>";
  private static final String CLOSE_SPAN = "</span>";

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static final SafeHtml LOADING = SafeHtmlUtils.fromSafeConstant(
    "<div class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>");

  private HtmlSnippetUtils() {
    // do nothing
  }

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
      pluginMandatoryHTML = SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_INFO + "Mandatory" + CLOSE_SPAN);
    } else {
      pluginMandatoryHTML = SafeHtmlUtils.fromSafeConstant(OPEN_SPAN_CLASS_LABEL_INFO + "Optional" + CLOSE_SPAN);
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
      mvText.setHTML(mv.get("value"));

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
}
