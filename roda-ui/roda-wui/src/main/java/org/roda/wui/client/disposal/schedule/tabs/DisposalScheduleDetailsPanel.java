package org.roda.wui.client.disposal.schedule.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.disposal.schedule.DisposalActionCode;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodIntervalCode;
import org.roda.wui.client.common.ActionsToolbar;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.DisposalScheduleAction;
import org.roda.wui.client.common.actions.DisposalScheduleToolbarActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.utils.FormUtilities;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.disposal.schedule.DisposalScheduleUtils;

import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

public class DisposalScheduleDetailsPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  ActionsToolbar actionsToolbar;

  @UiField
  FlowPanel detailsPanel;

  private DisposalSchedule schedule;
  private AsyncCallback<Actionable.ActionImpact> localCallback;

  public DisposalScheduleDetailsPanel(DisposalSchedule disposalSchedule,
    AsyncCallback<Actionable.ActionImpact> actionCallback) {
    initWidget(uiBinder.createAndBindUi(this));

    // Promote localCallback to an instance variable so refresh() can use it
    this.localCallback = new AsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onFailure(Throwable caught) {
        actionCallback.onFailure(caught);
      }

      @Override
      public void onSuccess(Actionable.ActionImpact result) {
        if (Actionable.ActionImpact.UPDATED.equals(result)) {
          actionCallback.onSuccess(result);
        }
      }
    };

    actionsToolbar.setLabelVisible(false);
    actionsToolbar.setTagsVisible(false);

    // Initial load
    refresh(disposalSchedule);
  }

  // Update the method signature to accept the new schedule
  public void refresh(DisposalSchedule newSchedule) {
    this.schedule = newSchedule;

    // 1. Clear out the old details
    clear();

    // 2. Re-populate text fields with the new data
    init(this.schedule);

    // 3. Re-bind the actions toolbar with the new schedule object
    actionsToolbar.setActionableMenu(new ActionableWidgetBuilder<DisposalSchedule>(DisposalScheduleToolbarActions.get())
      .withActionCallback(localCallback).buildGroupedListWithObjects(new ActionableObject<>(schedule),
        List.of(DisposalScheduleAction.EDIT), List.of(DisposalScheduleAction.EDIT)),
      true);
  }

  private void init(DisposalSchedule schedule) {
    FormUtilities.addIfNotBlank(detailsPanel, messages.disposalScheduleTitle(), schedule.getTitle());
    FormUtilities.addIfNotBlank(detailsPanel, messages.disposalScheduleDescription(), schedule.getDescription());
    FormUtilities.addIfNotBlank(detailsPanel, messages.disposalScheduleMandate(), schedule.getMandate());
    FormUtilities.addIfNotBlank(detailsPanel, messages.disposalScheduleNotes(), schedule.getScopeNotes());
    FormUtilities.addIfNotBlank(detailsPanel, messages.disposalScheduleActionCol(),
      messages.disposalScheduleActionCode(schedule.getActionCode().toString()));
    FormUtilities.addIfNotBlank(detailsPanel, messages.disposalScheduleRetentionTriggerElementId(),
      DisposalScheduleUtils.getI18nRetentionTriggerIdentifier(schedule.getRetentionTriggerElementId()));

    if (!DisposalActionCode.RETAIN_PERMANENTLY.equals(schedule.getActionCode())) {
      String retentionPeriod;
      if (schedule.getRetentionPeriodIntervalCode().equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD)) {
        retentionPeriod = messages.retentionPeriod(0, schedule.getRetentionPeriodIntervalCode().toString());
      } else {
        retentionPeriod = messages.retentionPeriod(schedule.getRetentionPeriodDuration(),
          schedule.getRetentionPeriodIntervalCode().toString());
      }
      FormUtilities.addIfNotBlank(detailsPanel, messages.disposalScheduleRetentionPeriodDuration(), retentionPeriod);
    }

    FormUtilities.addIfNotBlank(detailsPanel, messages.showUserStatusLabel(), HtmlSnippetUtils.getDisposalScheduleStateHtml(schedule));
  }

  public void clear() {
    detailsPanel.clear();
  }

  interface MyUiBinder extends UiBinder<Widget, DisposalScheduleDetailsPanel> {
    Widget createAndBindUi(DisposalScheduleDetailsPanel detailsPanel);
  }

}
