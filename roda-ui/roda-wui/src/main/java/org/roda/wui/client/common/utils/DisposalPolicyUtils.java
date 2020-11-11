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
    } else if (AIPState.RESIDUAL.equals(aip.getState())) {
      return getDisposalPolicySummaryForResidualAIP(aip);
    }

    return "";
  }

  private static String getDisposalPolicySummaryForResidualAIP(IndexedAIP aip) {
    return "";
  }

  private static String getDisposalPolicySummaryForActiveAIP(IndexedAIP aip) {
    StringBuilder builder = new StringBuilder();

    if (aip.getDisposalConfirmationId() != null) {
      builder.append(messages.disposalPolicyConfirmationSummary());
    } else {

      JavaScriptObject javaScriptObject = JavascriptUtils.durationInYMD(Humanize.formatDate(aip.getOverdueDate()));
      JSONObject jsonObject = new JSONObject(javaScriptObject);
      String duration = jsonObject.get("diff").toString();
      String timeUnit = jsonObject.get("unit").isString().stringValue();

      if (aip.getDisposalScheduleId() != null) {
        if (DisposalActionCode.DESTROY.name().equals(aip.getDisposalAction())
          || DisposalActionCode.REVIEW.name().equals(aip.getDisposalAction())) {
          if (aip.getOverdueDate().after(new Date())) {
            switch (timeUnit) {
              case "years":
                builder.append(
                  messages.disposalPolicyScheduleSummary(messages.disposalPolicyActionSummary(aip.getDisposalAction()),
                    messages.disposalPolicyScheduleYearSummary(Integer.parseInt(duration))));
                break;
              case "months":
                builder.append(
                  messages.disposalPolicyScheduleSummary(messages.disposalPolicyActionSummary(aip.getDisposalAction()),
                    messages.disposalPolicyScheduleMonthSummary(Integer.parseInt(duration))));
                break;
              case "days":
                builder.append(
                  messages.disposalPolicyScheduleSummary(messages.disposalPolicyActionSummary(aip.getDisposalAction()),
                    messages.disposalPolicyScheduleDaySummary(Integer.parseInt(duration))));
                break;
            }
          } else {
            builder.append(
              messages.disposalPolicySummaryReady(messages.disposalPolicyActionSummary(aip.getDisposalAction())));
          }
        } else if (DisposalActionCode.RETAIN_PERMANENTLY.name().equals(aip.getDisposalAction())) {

        }
        builder.append(" ");
      }

      if (aip.isDisposalHoldStatus()) {
        builder.append(messages.disposalPolicyHoldSummary());
      }
    }

    return builder.toString();
  }
}
