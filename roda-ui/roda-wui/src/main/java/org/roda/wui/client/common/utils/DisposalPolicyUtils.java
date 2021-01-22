package org.roda.wui.client.common.utils;

import java.util.Date;

import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodCalculation;
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
    }

    return new DisposalPolicySummary();
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
