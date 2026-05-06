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
    setData(schedule);
  }

  @Override
  protected FlowPanel createHeaderWidget(DisposalSchedule data) {
    if (data == null) {
      return null;
    }

    return new ActionableWidgetBuilder<DisposalSchedule>(DisposalScheduleToolbarActions.get())
      .buildGroupedListWithObjects(new ActionableObject<>(data), List.of(DisposalScheduleAction.EDIT),
        List.of(DisposalScheduleAction.EDIT));
  }

  @Override
  protected void buildFields(DisposalSchedule schedule) {
    buildField(messages.disposalScheduleTitle()).withValue(schedule.getTitle()).build();

    buildField(messages.disposalScheduleDescription()).withValue(schedule.getDescription()).build();

    buildField(messages.disposalScheduleMandate()).withValue(schedule.getMandate()).build();

    buildField(messages.disposalScheduleNotes()).withValue(schedule.getScopeNotes()).build();

    buildField(messages.disposalScheduleActionCol())
      .withValue(messages.disposalScheduleActionCode(schedule.getActionCode().toString())).build();

    buildField(messages.disposalScheduleRetentionTriggerElementId())
      .withValue(DisposalScheduleUtils.getI18nRetentionTriggerIdentifier(schedule.getRetentionTriggerElementId()))
      .build();

    if (!DisposalActionCode.RETAIN_PERMANENTLY.equals(schedule.getActionCode())) {
      String retentionPeriod;

      if (schedule.getRetentionPeriodIntervalCode().equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD)) {
        retentionPeriod = messages.retentionPeriod(0, schedule.getRetentionPeriodIntervalCode().toString());
      } else {
        retentionPeriod = messages.retentionPeriod(schedule.getRetentionPeriodDuration(),
          schedule.getRetentionPeriodIntervalCode().toString());
      }
      buildField(messages.disposalScheduleRetentionPeriodDuration()).withValue(retentionPeriod).build();
    }

    buildField(messages.showUserStatusLabel()).withHtml(HtmlSnippetUtils.getDisposalScheduleStateHtml(schedule))
      .build();
  }
}
