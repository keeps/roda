/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.hold;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.FocusPanel;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.wui.client.browse.tabs.DisposalHoldTabs;
import org.roda.wui.client.common.DisposalHoldActionsToolbar;
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
public class ShowDisposalHold extends Composite {
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
              ShowDisposalHold panel = new ShowDisposalHold(hold);
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
      return "disposal_hold";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<DisposalHold> navigationToolbar;
  @UiField
  DisposalHoldActionsToolbar actionsToolbar;
  @UiField
  TitlePanel title;
  @UiField
  DisposalHoldTabs browseTab;

  private Map<Actionable.ActionImpact, Runnable> handlers = new HashMap<>();
  private AsyncCallback<Actionable.ActionImpact> handler = new NoAsyncCallback<Actionable.ActionImpact>() {
    @Override
    public void onSuccess(Actionable.ActionImpact result) {
      if (handlers.containsKey(result)) {
        handlers.get(result).run();
      }
    }
  };
  private DisposalHold disposalHold;

  public ShowDisposalHold(final DisposalHold hold) {
    this.disposalHold = hold;

    initWidget(uiBinder.createAndBindUi(this));

    initHandlers(hold);

    navigationToolbar.withObject(disposalHold);
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getDisposalHoldBreadcrumbs(hold));
    navigationToolbar.build();

    actionsToolbar.setLabel(messages.showDisposalHoldTitle());
    actionsToolbar.setObjectAndBuild(hold, null, handler);

    title.setText(hold.getTitle());
    title.setIconClass("DisposalHold");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");

    browseTab.init(hold, handler);
  }

  private void initHandlers(DisposalHold hold) {
    handlers.put(Actionable.ActionImpact.DESTROYED,
      () -> HistoryUtils.newHistory(DisposalPolicy.RESOLVER.getHistoryPath()));

    // Use the DOM Swap refresh method instead of HistoryUtils
    handlers.put(Actionable.ActionImpact.UPDATED, () -> {
      Services services = new Services("Retrieve updated disposal hold", "get");
      services.disposalHoldResource(s -> s.retrieveDisposalHold(hold.getId()))
        .whenComplete((updatedHold, throwable) -> {
          if (throwable != null) {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          } else {
            // 1. Update local reference
            this.disposalHold = updatedHold;

            // 2. Update the main TitlePanel
            title.setText(updatedHold.getTitle());

            navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getDisposalHoldBreadcrumbs(updatedHold));

            // 4. Re-initialize tabs (this recreates the details panel with the new
            // schedule)
            browseTab.init(updatedHold, handler);
          }
        });
    });
  }

  interface MyUiBinder extends UiBinder<Widget, ShowDisposalHold> {
  }
}
