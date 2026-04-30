/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.schedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoActionsToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.disposal.schedule.data.panels.DisposalScheduleDataPanel;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import java.util.List;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class CreateDisposalSchedule extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      CreateDisposalSchedule createDisposalSchedule = new CreateDisposalSchedule();
      callback.onSuccess(createDisposalSchedule);
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
      return "create_disposal_schedule";
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

  public CreateDisposalSchedule() {
    initWidget(uiBinder.createAndBindUi(this));

    DisposalScheduleDataPanel dataPanel = new DisposalScheduleDataPanel(new DisposalSchedule(), false);
    disposalScheduleDataPanel.add(dataPanel);

    dataPanel.setSaveHandler(() -> {
      Services services = new Services("Create disposal schedule", "create");
      services.disposalScheduleResource(s -> s.createDisposalSchedule(dataPanel.getValue()))
        .whenComplete((created, throwable) -> {
          if (throwable != null) {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          } else {
            Toast.showInfo(messages.showDisposalScheduleTitle(), messages.disposalScheduleSuccessfullyCreated());
            HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER, created.getId());
          }
        });
    });

    dataPanel.setCancelHandler(() -> HistoryUtils.newHistory(DisposalPolicy.RESOLVER));

    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getCreateDisposalScheduleBreadcrumbs());

    actionsToolbar.setLabel(messages.showDisposalScheduleTitle());

    // 3. Pass the shared object
    actionsToolbar.build();

    title.setText(messages.newDisposalScheduleTitle());
    title.setIconClass("DisposalSchedule");
    title.addStyleName("mb-20");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  interface MyUiBinder extends UiBinder<Widget, CreateDisposalSchedule> {
  }

}
