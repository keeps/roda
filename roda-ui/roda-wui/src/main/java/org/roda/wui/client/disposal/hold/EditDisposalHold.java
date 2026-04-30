/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.hold;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.core.data.v2.ip.disposalhold.UpdateDisposalHoldRequest;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoActionsToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.disposal.hold.data.panels.DisposalHoldDataPanel;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
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
public class EditDisposalHold extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        Services services = new Services("Retrieve disposal hold", "get");
        services.disposalHoldResource(s -> s.retrieveDisposalHold(historyTokens.get(0)))
          .whenComplete((hold, throwable) -> {
            if (throwable != null) {
              AsyncCallbackUtils.defaultFailureTreatment(throwable);
            } else {
              if (DisposalHoldState.LIFTED.equals(hold.getState())) {
                HistoryUtils.newHistory(ShowDisposalHold.RESOLVER, hold.getId());
              } else {
                EditDisposalHold panel = new EditDisposalHold(hold);
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
      return "edit_disposal_hold";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<DisposalHold> navigationToolbar;
  @UiField
  NoActionsToolbar actionsToolbar;
  @UiField
  TitlePanel title;
  @UiField
  FlowPanel disposalHoldDataPanel;

  public EditDisposalHold(DisposalHold disposalHold) {
    initWidget(uiBinder.createAndBindUi(this));

    // 1. Create the panel and keep a reference
    DisposalHoldDataPanel dataPanel = getDisposalHoldDataPanel(disposalHold);
    disposalHoldDataPanel.add(dataPanel);

    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getEditDisposalHoldBreadcrumbs(disposalHold));

    actionsToolbar.setLabel(messages.showDisposalHoldTitle());

    // 3. Pass the shared object
    actionsToolbar.build();

    title.setText(disposalHold.getTitle());
    title.setIconClass("DisposalHold");
    title.addStyleName("mb-20");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  private static DisposalHoldDataPanel getDisposalHoldDataPanel(DisposalHold disposalHold) {
    DisposalHoldDataPanel dataPanel = new DisposalHoldDataPanel();
    dataPanel.setDisposalHold(disposalHold);

    dataPanel.setSaveHandler(() -> {
      // This will trigger the validation and, if valid, execute the save logic
      Services services = new Services("Update Disposal Hold", "update");
      UpdateDisposalHoldRequest request = new UpdateDisposalHoldRequest();
      request.setDisposalHold(dataPanel.getValue());
      services.disposalHoldResource(s -> s.updateDisposalHold(request)).whenComplete((result, error) -> {
        if (error != null) {
          AsyncCallbackUtils.defaultFailureTreatment(error);
        } else {
          Toast.showInfo(messages.showDisposalHoldTitle(), messages.disposalHoldSuccessfullyUpdated());
          HistoryUtils.newHistory(ShowDisposalHold.RESOLVER, result.getId());
        }
      });
    });

    // Bind the Cancel Action logic
    dataPanel.setCancelHandler(() -> HistoryUtils.newHistory(ShowDisposalHold.RESOLVER, disposalHold.getId()));
    return dataPanel;
  }

  interface MyUiBinder extends UiBinder<Widget, EditDisposalHold> {
  }
}
