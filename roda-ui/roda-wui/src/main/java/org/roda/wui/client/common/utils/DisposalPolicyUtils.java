/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.utils;

import java.util.Date;
import java.util.List;

import org.roda.core.data.v2.disposal.schedule.DisposalActionCode;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodCalculation;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.labels.Tag;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalPolicyUtils {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private DisposalPolicyUtils() {
    // do nothing
  }

  public static boolean showDisposalPolicySummary(IndexedAIP aip) {
    return aip.getDisposalConfirmationId() != null || aip.isOnHold() || aip.getDisposalScheduleId() != null;
  }

  public static SafeHtml getDisposalPolicySummarySafeHTML(IndexedAIP aip) {
    if (AIPState.ACTIVE.equals(aip.getState())) {
      DisposalPolicySummary disposalPolicySummary = getDisposalPolicySummaryForActiveAIP(aip);
      return HtmlSnippetUtils.getDisposalPolicySummaryBadge(disposalPolicySummary);
    } else if (AIPState.DESTROYED.equals(aip.getState())) {
      String message = messages.disposalPolicyDestroyedAIPSummary(Humanize.formatDate(aip.getDestroyedOn()));
      return HtmlSnippetUtils.getDisposalPolicySummaryBadge(message, "");
    }

    return SafeHtmlUtils.EMPTY_SAFE_HTML;
  }

  public static Tag getDisposalPolicySummaryTag(IndexedAIP aip) {
    if (AIPState.ACTIVE.equals(aip.getState())) {
      DisposalPolicySummary summary = getDisposalPolicySummaryForActiveAIP(aip);
      switch (summary.getPolicyStatus()) {
        case DESTROY:
        case CONFIRMATION:
        case OVERDUE:
        case ERROR:
          return Tag.fromText(summary.getMessage(), List.of(Tag.TagStyle.DANGER_LIGHT, Tag.TagStyle.MONO));
        case REVIEW:
          return Tag.fromText(summary.getMessage(), List.of(Tag.TagStyle.MONO, Tag.TagStyle.WARNING_LIGHT));
        case HOLD:
          return Tag.fromText(summary.getMessage(), List.of(Tag.TagStyle.MONO, Tag.TagStyle.NEUTRAL));
        case RETAIN:
          return Tag.fromText(summary.getMessage(), List.of(Tag.TagStyle.MONO, Tag.TagStyle.SUCCESS));
        case NONE:
        default:
          return null;
      }
    } else if (AIPState.DESTROYED.equals(aip.getState())) {
      String message = messages.disposalPolicyDestroyedAIPSummary(Humanize.formatDate(aip.getDestroyedOn()));
      return Tag.fromText(message, List.of(Tag.TagStyle.DANGER_LIGHT, Tag.TagStyle.MONO));
    }

    return null;
  }

  public static String getDisposalPolicySummaryText(IndexedAIP aip) {
    if (AIPState.ACTIVE.equals(aip.getState())) {
      return getDisposalPolicySummaryForActiveAIP(aip).getMessage();
    } else if (AIPState.DESTROYED.equals(aip.getState())) {
      return getDisposalPolicySummaryForResidualAIP(aip);
    }

    return "";
  }

  private static String getDisposalPolicySummaryForResidualAIP(IndexedAIP aip) {
    return messages.disposalPolicyDestroyedAIPSummary(Humanize.formatDate(aip.getDestroyedOn()));
  }

  private static DisposalPolicySummary getDisposalPolicySummaryForActiveAIP(IndexedAIP aip) {

    if (RetentionPeriodCalculation.ERROR.equals(aip.getRetentionPeriodState())) {
      return new DisposalPolicySummary(DisposalPolicySummary.PolicyStatus.ERROR,
        messages.disposalPolicyRetentionPeriodCalculationError());
    }

    if (aip.getDisposalConfirmationId() != null) {
      return new DisposalPolicySummary(DisposalPolicySummary.PolicyStatus.CONFIRMATION,
        messages.disposalPolicyConfirmationSummary());
    } else {
      if (aip.isOnHold()) {
        return new DisposalPolicySummary(DisposalPolicySummary.PolicyStatus.HOLD, messages.disposalPolicyHoldSummary());
      } else if (aip.getDisposalScheduleId() != null) {
        return onSchedule(aip);
      }
      else {
        return new DisposalPolicySummary(DisposalPolicySummary.PolicyStatus.NONE, messages.disposalPolicyNoneSummary());
      }
    }
  }

  private static DisposalPolicySummary onSchedule(IndexedAIP aip) {
    if (DisposalActionCode.DESTROY.equals(aip.getDisposalAction())
      || DisposalActionCode.REVIEW.equals(aip.getDisposalAction())) {

      JavaScriptObject javaScriptObject = JavascriptUtils.durationInYMD(Humanize.formatDate(aip.getOverdueDate()));
      JSONObject jsonObject = new JSONObject(javaScriptObject);
      String duration = jsonObject.get("diff").toString();
      String timeUnit = jsonObject.get("unit").isString().stringValue();

      if (aip.getOverdueDate().after(new Date())) {
        switch (timeUnit) {
          case "years":
            return new DisposalPolicySummary(convertActionCodeToPolicyStatus(aip.getDisposalAction()),
              messages.disposalPolicyScheduleSummary(
                messages.disposalPolicyActionSummary(aip.getDisposalAction().name()),
                messages.disposalPolicyScheduleYearSummary(Integer.parseInt(duration))));
          case "months":
            return new DisposalPolicySummary(convertActionCodeToPolicyStatus(aip.getDisposalAction()),
              messages.disposalPolicyScheduleSummary(
                messages.disposalPolicyActionSummary(aip.getDisposalAction().name()),
                messages.disposalPolicyScheduleMonthSummary(Integer.parseInt(duration))));
          case "days":
            return new DisposalPolicySummary(convertActionCodeToPolicyStatus(aip.getDisposalAction()),
              messages.disposalPolicyScheduleSummary(
                messages.disposalPolicyActionSummary(aip.getDisposalAction().name()),
                messages.disposalPolicyScheduleDaySummary(Integer.parseInt(duration))));
          default:
            break;
        }
      } else {
        return new DisposalPolicySummary(convertActionCodeToPolicyStatus(aip.getDisposalAction()),
          messages.disposalPolicySummaryReady(messages.disposalPolicyActionSummary(aip.getDisposalAction().name())));
      }
    } else if (DisposalActionCode.RETAIN_PERMANENTLY.equals(aip.getDisposalAction())) {
      return new DisposalPolicySummary(DisposalPolicySummary.PolicyStatus.RETAIN,
        messages.disposalPolicyRetainPermanently());
    }

    return new DisposalPolicySummary();
  }

  private static DisposalPolicySummary.PolicyStatus convertActionCodeToPolicyStatus(DisposalActionCode code) {
    switch (code) {
      case RETAIN_PERMANENTLY:
        return DisposalPolicySummary.PolicyStatus.RETAIN;
      case REVIEW:
        return DisposalPolicySummary.PolicyStatus.REVIEW;
      case DESTROY:
      default:
        return DisposalPolicySummary.PolicyStatus.DESTROY;
    }
  }
}
