/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.File;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Representation;
import org.roda.core.data.v2.jobs.Job;
import org.roda.core.data.v2.jobs.Job.JOB_STATE;
import org.roda.core.data.v2.jobs.Report.PluginState;
import org.roda.core.data.v2.risks.Risk.SEVERITY_LEVEL;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.Browse;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.ViewRepresentation;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;

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
        } else if (job.getJobStats().getSourceObjectsProcessedWithSuccess() > 0) {
          ret = SafeHtmlUtils
            .fromSafeConstant("<span class='label-warning'>" + messages.showJobStatusCompleted() + "</span>");
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
      } else if (JOB_STATE.STOPPING.equals(state)) {
        ret = SafeHtmlUtils
          .fromSafeConstant("<span class='label-default'>" + messages.showJobStatusStopping() + "</span>");
      } else if (JOB_STATE.STOPPED.equals(state)) {
        ret = SafeHtmlUtils
          .fromSafeConstant("<span class='label-default'>" + messages.showJobStatusStopped() + "</span>");
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

  public static void addRiskIncidenceObjectLinks(RiskIncidence incidence, final Label objectLabel,
    final Anchor objectLink) {
    if (AIP.class.getSimpleName().equals(incidence.getObjectClass())) {
      objectLabel.setText(messages.showAIPExtended());
      objectLink.setHref(Tools.createHistoryHashLink(Browse.RESOLVER, incidence.getAipId()));
      objectLink.setText(incidence.getAipId());

    } else if (Representation.class.getSimpleName().equals(incidence.getObjectClass())) {
      BrowserService.Util.getInstance().retrieveRepresentationById(incidence.getRepresentationId(),
        new AsyncCallback<IndexedRepresentation>() {

          @Override
          public void onFailure(Throwable caught) {
            // do nothing
          }

          @Override
          public void onSuccess(IndexedRepresentation result) {
            if (result != null) {
              objectLabel.setText(messages.showRepresentationExtended());
              objectLink.setHref(Tools.createHistoryHashLink(Browse.RESOLVER,
                ViewRepresentation.RESOLVER.getHistoryToken(), result.getAipId(), result.getUUID()));
              objectLink.setText(result.getUUID());
            }
          }
        });

    } else if (File.class.getSimpleName().equals(incidence.getObjectClass())) {
      BrowserService.Util.getInstance().retrieveFileById(incidence.getFileId(), new AsyncCallback<IndexedFile>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(IndexedFile result) {
          if (result != null) {
            objectLabel.setText(messages.showFileExtended());
            objectLink
              .setHref(Tools.createHistoryHashLink(Browse.RESOLVER, ViewRepresentation.RESOLVER.getHistoryToken(),
                result.getAipId(), result.getRepresentationUUID(), result.getUUID()));
            objectLink.setText(result.getUUID());
          }
        }
      });
    }
  }

}
