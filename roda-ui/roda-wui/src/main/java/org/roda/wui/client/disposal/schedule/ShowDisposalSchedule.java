/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.schedule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.FocusPanel;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.wui.client.browse.tabs.DisposalScheduleTabs;
import org.roda.wui.client.common.DisposalScheduleActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class ShowDisposalSchedule extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        Services services = new Services("Retrieve disposal schedule", "get");
        services.disposalScheduleResource(s -> s.retrieveDisposalSchedule(historyTokens.get(0)))
            .whenComplete((schedule, throwable) -> {
              if (throwable != null) {
                AsyncCallbackUtils.defaultFailureTreatment(throwable);
              } else {
                ShowDisposalSchedule panel = new ShowDisposalSchedule(schedule);
                callback.onSuccess(panel);
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
      return "disposal_schedule";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;

  @UiField
  NavigationToolbar<DisposalSchedule> navigationToolbar;

  @UiField
  DisposalScheduleActionsToolbar actionsToolbar;

  @UiField
  TitlePanel title;

  @UiField
  DisposalScheduleTabs browseTab;

  private Map<Actionable.ActionImpact, Runnable> handlers = new HashMap<>();
  private AsyncCallback<Actionable.ActionImpact> handler = new NoAsyncCallback<Actionable.ActionImpact>() {
    @Override
    public void onSuccess(Actionable.ActionImpact result) {
      if (handlers.containsKey(result)) {
        handlers.get(result).run();
      }
    }
  };
  private DisposalSchedule disposalSchedule;

  public ShowDisposalSchedule(final DisposalSchedule schedule) {
    this.disposalSchedule = schedule;

    initWidget(uiBinder.createAndBindUi(this));

    initHandlers(schedule);

    navigationToolbar.withObject(schedule).build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getDisposalScheduleBreadcrumbs(schedule));

    actionsToolbar.setLabel(messages.showDisposalScheduleTitle());
    actionsToolbar.setObjectAndBuild(schedule, null, handler);

    title.setText(schedule.getTitle());
    title.setIconClass("DisposalSchedule");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");

    browseTab.init(schedule, handler);
  }

  private void initHandlers(DisposalSchedule schedule) {
    handlers.put(Actionable.ActionImpact.DESTROYED,
      () -> HistoryUtils.newHistory(DisposalPolicy.RESOLVER.getHistoryPath()));

    // Use the DOM Swap refresh method instead of HistoryUtils
    handlers.put(Actionable.ActionImpact.UPDATED, () -> {
      Services services = new Services("Retrieve updated disposal schedule", "get");
      services.disposalScheduleResource(s -> s.retrieveDisposalSchedule(disposalSchedule.getId()))
        .whenComplete((updatedSchedule, throwable) -> {
          if (throwable != null) {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          } else {
            // 1. Update local reference
            this.disposalSchedule = updatedSchedule;

            // 2. Update the main TitlePanel
            title.setText(updatedSchedule.getTitle());

            navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getDisposalScheduleBreadcrumbs(updatedSchedule));

            // 4. Re-initialize tabs (this recreates the details panel with the new
            // schedule)
            browseTab.init(updatedSchedule, handler);
          }
        });
    });
  }

  interface MyUiBinder extends UiBinder<Widget, ShowDisposalSchedule> {
  }

}
