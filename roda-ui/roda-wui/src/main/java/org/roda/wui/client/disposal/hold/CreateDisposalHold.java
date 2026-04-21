/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.hold;

import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.wui.client.common.CreateOrUpdateDisposalHoldActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.common.client.HistoryResolver;
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
public class CreateDisposalHold extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      CreateDisposalHold createDisposalHold = new CreateDisposalHold(new DisposalHold());
      callback.onSuccess(createDisposalHold);
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
      return "create_disposal_hold";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<DisposalHold> navigationToolbar;
  @UiField
  CreateOrUpdateDisposalHoldActionsToolbar actionsToolbar;
  @UiField
  TitlePanel title;
  @UiField
  FlowPanel disposalHoldDataPanel;

  public CreateDisposalHold(DisposalHold disposalHold) {
    initWidget(uiBinder.createAndBindUi(this));

    // 1. Create the panel and keep a reference
    DisposalHoldDataPanel dataPanel = new DisposalHoldDataPanel(disposalHold, false);
    disposalHoldDataPanel.add(dataPanel);

    navigationToolbar.withoutButtons().build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getCreateDisposalHoldBreadcrumbs());

    actionsToolbar.setLabel(messages.showDisposalHoldTitle());

    // 2. Give the toolbar the panel so it can check validity
    actionsToolbar.setDataPanel(dataPanel);
    actionsToolbar.setIsCreate(true);

    // 3. Pass the shared object
    actionsToolbar.setObjectAndBuild(disposalHold, null, new AsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(Actionable.ActionImpact result) {
        // Redirect user or show success message if result == ActionImpact.UPDATED
      }
    });

    title.setText(messages.newDisposalHoldTitle());
    title.setIconClass("DisposalHold");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");
  }

  interface MyUiBinder extends UiBinder<Widget, CreateDisposalHold> {
  }
}
