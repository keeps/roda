/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.confirmations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.FocusPanel;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.wui.client.browse.tabs.DisposalConfirmationTabs;
import org.roda.wui.client.common.DisposalConfirmationActionsToolbar;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.disposal.DisposalConfirmations;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ShowDisposalConfirmation extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String confirmationId = historyTokens.get(0);
        Services services = new Services("Retrieve disposal hold", "get");
        services
          .disposalConfirmationResource(
            s -> s.findByUuid(confirmationId, LocaleInfo.getCurrentLocale().getLocaleName()))
          .whenComplete((confirmation, throwable) -> {
            if (throwable != null) {
              AsyncCallbackUtils.defaultFailureTreatment(throwable);
            } else {
              ShowDisposalConfirmation panel = new ShowDisposalConfirmation(confirmation);
              callback.onSuccess(panel);
            }
          });
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {DisposalConfirmations.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(DisposalConfirmations.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "confirmation";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField
  FocusPanel keyboardFocus;
  @UiField
  NavigationToolbar<DisposalConfirmation> navigationToolbar;
  @UiField
  DisposalConfirmationActionsToolbar actionsToolbar;
  @UiField
  TitlePanel title;
  @UiField
  DisposalConfirmationTabs browseTab;

  private Map<Actionable.ActionImpact, Runnable> handlers = new HashMap<>();
  private AsyncCallback<Actionable.ActionImpact> handler = new NoAsyncCallback<Actionable.ActionImpact>() {
    @Override
    public void onSuccess(Actionable.ActionImpact result) {
      if (handlers.containsKey(result)) {
        handlers.get(result).run();
      }
    }
  };

  private ShowDisposalConfirmation(DisposalConfirmation confirmation) {
    initWidget(uiBinder.createAndBindUi(this));

    handlers.put(Actionable.ActionImpact.DESTROYED, () -> HistoryUtils.newHistory(DisposalConfirmations.RESOLVER));

    navigationToolbar.withObject(confirmation);
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getDisposalConfirmationBreadcrumbs(confirmation));
    navigationToolbar.build();

    actionsToolbar.setLabel(messages.showDisposalConfirmationTitle());
    actionsToolbar.setObjectAndBuild(confirmation, null, handler);

    title.setText(confirmation.getTitle());
    title.setIconClass("DisposalConfirmations");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");

    browseTab.init(confirmation);
  }

  interface MyUiBinder extends UiBinder<Widget, ShowDisposalConfirmation> {
  }
}
