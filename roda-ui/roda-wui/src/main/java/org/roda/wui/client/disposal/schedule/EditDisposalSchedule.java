/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.schedule;

import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.DisposalScheduleState;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoActionsToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.client.disposal.schedule.data.panels.DisposalScheduleDataPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.roda.wui.common.client.widgets.Toast;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class EditDisposalSchedule extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        Services services = new Services("Retrieve disposal schedule", "get");
        services.disposalScheduleResource(s -> s.retrieveDisposalSchedule(historyTokens.get(0)))
          .whenComplete((result, throwable) -> {
            if (throwable != null) {
              AsyncCallbackUtils.defaultFailureTreatment(throwable);
            } else {
              if (DisposalScheduleState.INACTIVE.equals(result.getState())) {
                HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
              } else {
                EditDisposalSchedule panel = new EditDisposalSchedule(result);
                callback.onSuccess(panel);
              }
            }
          });
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {DisposalPolicy.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(DisposalPolicy.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "edit_disposal_schedule";
    }
  };

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<DisposalRule> navigationToolbar;
  @UiField
  NoActionsToolbar actionsToolbar;
  @UiField
  TitlePanel title;
  @UiField
  FlowPanel disposalScheduleDataPanel;

  public EditDisposalSchedule(DisposalSchedule disposalSchedule) {
    initWidget(uiBinder.createAndBindUi(this));

    // 1. Create the panel and keep a reference
    DisposalScheduleDataPanel dataPanel = new DisposalScheduleDataPanel(disposalSchedule, true);
    disposalScheduleDataPanel.add(dataPanel);

    dataPanel.setSaveHandler(() -> {
      Services services = new Services("Update disposal schedule", "update");
      services.disposalScheduleResource(s -> s.updateDisposalSchedule(dataPanel.getValue()))
        .whenComplete((updated, throwable) -> {
          if (throwable != null) {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          } else {
            Toast.showInfo(messages.disposalScheduleTitle(), messages.disposalScheduleSuccessfullyUpdated());
            HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER, updated.getId());
          }
        });
    });

    dataPanel.setCancelHandler(() -> HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER, disposalSchedule.getId()));

    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getEditDisposalScheduleBreadcrumbs(disposalSchedule));

    actionsToolbar.setLabel(messages.showDisposalRuleTitle());

    // 3. Pass the shared object
    actionsToolbar.build();

    title.setText(disposalSchedule.getTitle());
    title.setIconClass("DisposalSchedule");
    title.addStyleName("mb-20");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  interface MyUiBinder extends UiBinder<Widget, EditDisposalSchedule> {
  }
}
