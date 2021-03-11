/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.association;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalActionCode;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodCalculation;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodIntervalCode;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.disposal.schedule.ShowDisposalSchedule;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RetentionPeriodPanel extends Composite {
  interface MyUiBinder extends UiBinder<Widget, RetentionPeriodPanel> {
  }

  private static RetentionPeriodPanel.MyUiBinder uiBinder = GWT.create(RetentionPeriodPanel.MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel retentionPeriodPanel;

  @UiField
  FlowPanel scheduleInfo;

  @UiField
  Label disposalRetentionStartDate;

  @UiField
  Label disposalRetentionDueDate;

  @UiField
  Label retentionPeriodLabel;

  @UiField
  Label disposalRetentionPeriod;

  @UiField
  Label retentionOverdueDateLabel;

  @UiField
  Label disposalAssociationType;

  @UiField
  HTML disposalDisposalAction;

  @UiField
  HTML disposalDisposalStatus;

  public RetentionPeriodPanel(IndexedAIP aip) {
    initWidget(uiBinder.createAndBindUi(this));

    if (aip.getDisposalScheduleId() == null) {
      retentionPeriodPanel.clear();
    } else {
      if (RetentionPeriodCalculation.SUCCESS.equals(aip.getRetentionPeriodState())) {
        handleRetentionCorrectCalculation(aip);
      } else {
        handleRetentionFailedCalculation(aip);
      }

      handleRetentionPeriodCommonInformation(aip);
    }
  }

  private void handleRetentionPeriodCommonInformation(final IndexedAIP aip) {
    Anchor scheduleLink = new Anchor(aip.getDisposalScheduleName(),
      HistoryUtils.createHistoryHashLink(ShowDisposalSchedule.RESOLVER, aip.getDisposalScheduleId()));

    scheduleInfo.add(scheduleLink);

    disposalAssociationType.setText(messages.disposalScheduleAssociationType(aip.getScheduleAssociationType().name()));

    disposalDisposalAction.setHTML(HtmlSnippetUtils.getDisposalScheduleActionHtml(aip.getDisposalAction()));

    disposalDisposalStatus.setHTML(HtmlSnippetUtils.getDisposalHoldStatusHTML(aip.isOnHold()));
  }

  private void handleRetentionFailedCalculation(final IndexedAIP aip) {
    if (DisposalActionCode.RETAIN_PERMANENTLY.equals(aip.getDisposalAction())) {
      disposalRetentionDueDate.setText(messages.permanentlyRetained());
      retentionPeriodLabel.setVisible(false);
      disposalRetentionPeriod.setVisible(false);
    } else {
      retentionPeriodLabel.setVisible(false);
      retentionOverdueDateLabel.setVisible(false);
      disposalRetentionDueDate.setVisible(false);
      disposalRetentionPeriod.setVisible(false);
      disposalRetentionStartDate.setText(aip.getRetentionPeriodDetails());
    }
  }

  private void handleRetentionCorrectCalculation(final IndexedAIP aip) {
    disposalRetentionStartDate.setText(Humanize.formatDate(aip.getRetentionPeriodStartDate()));

    if (DisposalActionCode.RETAIN_PERMANENTLY.equals(aip.getDisposalAction())) {
      disposalRetentionDueDate.setText(messages.permanentlyRetained());
      retentionPeriodLabel.setVisible(false);
      disposalRetentionPeriod.setVisible(false);
    } else {
      disposalRetentionDueDate.setText(Humanize.formatDate(aip.getOverdueDate()));

      if (aip.getRetentionPeriodInterval().equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD)) {
        disposalRetentionPeriod.setText(messages.retentionPeriod(0, aip.getRetentionPeriodInterval().name()));
      } else {
        disposalRetentionPeriod
          .setText(messages.retentionPeriod(aip.getRetentionPeriodDuration(), aip.getRetentionPeriodInterval().name()));
      }

    }
  }

}