package org.roda.wui.client.disposal.schedule.tabs;

import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import org.roda.core.data.v2.disposal.schedule.DisposalActionCode;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodIntervalCode;
import org.roda.wui.client.common.actions.DisposalScheduleAction;
import org.roda.wui.client.common.actions.DisposalScheduleToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.disposal.schedule.DisposalScheduleUtils;

import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalScheduleDetailsPanel extends GenericMetadataCardPanel<DisposalSchedule> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public DisposalScheduleDetailsPanel(DisposalSchedule schedule) {
    super(createConfiguredToolbar(schedule));
    setData(schedule);
  }

  private static FlowPanel createConfiguredToolbar(DisposalSchedule schedule) {
    if (schedule == null) {
      return null;
    }

    return new ActionableWidgetBuilder<DisposalSchedule>(DisposalScheduleToolbarActions.get())
      .buildGroupedListWithObjects(new ActionableObject<>(schedule), List.of(DisposalScheduleAction.EDIT),
        List.of(DisposalScheduleAction.EDIT));
  }

  @Override
  public void setData(DisposalSchedule schedule) {
    // 1. Clear any existing fields in case setData is called multiple times
    metadataContainer.clear();

    if (schedule == null) {
      return;
    }

    addFieldIfNotNull(messages.disposalScheduleTitle(), DisposalSchedule::getTitle, schedule);
    addFieldIfNotNull(messages.disposalScheduleDescription(), DisposalSchedule::getDescription, schedule);
    addFieldIfNotNull(messages.disposalScheduleMandate(), DisposalSchedule::getMandate, schedule);
    addFieldIfNotNull(messages.disposalScheduleNotes(), DisposalSchedule::getScopeNotes, schedule);
    addFieldIfNotNull(messages.disposalScheduleActionCol(),
      messages.disposalScheduleActionCode(schedule.getActionCode().toString()));
    addFieldIfNotNull(messages.disposalScheduleRetentionTriggerElementId(),
      DisposalScheduleUtils.getI18nRetentionTriggerIdentifier(schedule.getRetentionTriggerElementId()));
    if (!DisposalActionCode.RETAIN_PERMANENTLY.equals(schedule.getActionCode())) {
      String retentionPeriod;

      if (schedule.getRetentionPeriodIntervalCode().equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD)) {
        retentionPeriod = messages.retentionPeriod(0, schedule.getRetentionPeriodIntervalCode().toString());
      } else {
        retentionPeriod = messages.retentionPeriod(schedule.getRetentionPeriodDuration(),
          schedule.getRetentionPeriodIntervalCode().toString());
      }
      addFieldIfNotNull(messages.disposalScheduleRetentionPeriodDuration(), retentionPeriod);
    }

    addFieldIfNotNull(messages.showUserStatusLabel(), HtmlSnippetUtils.getDisposalScheduleStateHtml(schedule));
  }
}
