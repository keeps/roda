package org.roda.wui.client.common.utils;

import java.util.Date;

import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalPolicyUtils {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private DisposalPolicyUtils() {
    // do nothing
  }

  public static String getDisposalPolicySummaryText(IndexedAIP aip) {
    if (AIPState.ACTIVE.equals(aip.getState())) {
      return getDisposalPolicySummaryForActiveAIP(aip);
    } else if (AIPState.DESTROYED.equals(aip.getState())) {
      return getDisposalPolicySummaryForResidualAIP(aip);
    }

    return "";
  }

  private static String getDisposalPolicySummaryForResidualAIP(IndexedAIP aip) {
    return messages.disposalPolicyDestroyedAIPSummary(Humanize.formatDate(aip.getDestroyedOn()));
  }

  private static String getDisposalPolicySummaryForActiveAIP(IndexedAIP aip) {
    StringBuilder builder = new StringBuilder();

    if (aip.getDisposalConfirmationId() != null) {
      builder.append(messages.disposalPolicyConfirmationSummary());
    } else {
      if (aip.getDisposalScheduleId() != null) {
        if (DisposalActionCode.DESTROY.equals(aip.getDisposalAction())
          || DisposalActionCode.REVIEW.equals(aip.getDisposalAction())) {

          JavaScriptObject javaScriptObject = JavascriptUtils.durationInYMD(Humanize.formatDate(aip.getOverdueDate()));
          JSONObject jsonObject = new JSONObject(javaScriptObject);
          String duration = jsonObject.get("diff").toString();
          String timeUnit = jsonObject.get("unit").isString().stringValue();

          if (aip.getOverdueDate().after(new Date())) {
            switch (timeUnit) {
              case "years":
                builder.append(
                  messages.disposalPolicyScheduleSummary(messages.disposalPolicyActionSummary(aip.getDisposalAction().name()),
                    messages.disposalPolicyScheduleYearSummary(Integer.parseInt(duration))));
                break;
              case "months":
                builder.append(
                  messages.disposalPolicyScheduleSummary(messages.disposalPolicyActionSummary(aip.getDisposalAction().name()),
                    messages.disposalPolicyScheduleMonthSummary(Integer.parseInt(duration))));
                break;
              case "days":
                builder.append(
                  messages.disposalPolicyScheduleSummary(messages.disposalPolicyActionSummary(aip.getDisposalAction().name()),
                    messages.disposalPolicyScheduleDaySummary(Integer.parseInt(duration))));
                break;
            }
          } else {
            builder.append(
              messages.disposalPolicySummaryReady(messages.disposalPolicyActionSummary(aip.getDisposalAction().name())));
          }
        } else if (DisposalActionCode.RETAIN_PERMANENTLY.equals(aip.getDisposalAction())) {
          builder.append(messages.disposalPolicyRetainPermanently());
        }

      } else {
        builder.append(messages.disposalPolicyNoScheduleSummary());

        if (aip.isDisposalHoldStatus()) {
          builder.append(" ").append(messages.disposalPolicyHoldSummary());
        } else {
          return messages.disposalPolicyNone();
        }
      }
    }

    return builder.toString();
  }
}
