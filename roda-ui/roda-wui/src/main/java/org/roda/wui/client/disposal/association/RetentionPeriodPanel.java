package org.roda.wui.client.disposal.association;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.disposal.schedule.ShowDisposalSchedule;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.roda.wui.common.client.tools.Humanize;

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
  Label disposalRetentionPeriod;

  @UiField
  HTML disposalDisposalAction;

  @UiField
  HTML disposalDisposalStatus;

  public RetentionPeriodPanel(IndexedAIP aip) {
    initWidget(uiBinder.createAndBindUi(this));
    if (aip.getDisposalScheduleId() == null) {
      retentionPeriodPanel.clear();
    } else {
      Anchor scheduleLink = new Anchor(aip.getDisposalScheduleName(),
        HistoryUtils.createHistoryHashLink(ShowDisposalSchedule.RESOLVER, aip.getDisposalScheduleId()));

      scheduleInfo.add(scheduleLink);

      disposalRetentionStartDate.setText(Humanize.formatDate(aip.getCreatedOn()));

      disposalRetentionDueDate.setText(Humanize.formatDate(aip.getOverdueDate()));

      disposalRetentionPeriod.setText(aip.getDisposalRetentionPeriod());

      disposalDisposalAction.setHTML(HtmlSnippetUtils.getDisposalScheduleActionHtml(aip.getDisposalAction()));

      disposalDisposalStatus.setHTML(HtmlSnippetUtils.getDisposalHoldStatusHTML(aip.isDisposalHoldStatus()));
    }
  }
}