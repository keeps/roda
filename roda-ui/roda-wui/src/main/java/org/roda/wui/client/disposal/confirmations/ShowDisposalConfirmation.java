/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.confirmations;

import java.util.List;

import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.DisposalConfirmationReportActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.common.utils.SidebarUtils;
import org.roda.wui.client.disposal.DisposalConfirmations;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ShowDisposalConfirmation extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static ShowDisposalConfirmation instance = null;
  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
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
  private static ShowDisposalConfirmation.MyUiBinder uiBinder = GWT.create(ShowDisposalConfirmation.MyUiBinder.class);
  @UiField
  SimplePanel actionsSidebar;
  @UiField
  FlowPanel contentFlowPanel;
  @UiField
  FlowPanel sidebarFlowPanel;

  private ShowDisposalConfirmation() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public static ShowDisposalConfirmation getInstance() {
    if (instance == null) {
      instance = new ShowDisposalConfirmation();
    }
    return instance;
  }

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      String confirmationId = historyTokens.get(0);
      Services services = new Services("Retrieve the disposal confirmation", "get");
      services.rodaEntityRestService(s -> s.findByUuid(confirmationId), DisposalConfirmation.class)
        .thenCompose(disposalConfirmation -> services
          .disposalConfirmationResource(s -> s.retrieveDisposalConfirmationReport(disposalConfirmation.getId(), false))
          .whenComplete((report, throwable) -> {
            if (throwable != null) {
              AsyncCallbackUtils.defaultFailureTreatment(throwable);
            } else {
              final DisposalConfirmationReportActions confirmationActions = DisposalConfirmationReportActions.get();
              instance = new ShowDisposalConfirmation();
              SidebarUtils.toggleSidebar(contentFlowPanel, sidebarFlowPanel, confirmationActions.hasAnyRoles());
              instance.actionsSidebar.setWidget(new ActionableWidgetBuilder<>(confirmationActions).withBackButton()
                .buildListWithObjects(new ActionableObject<>(disposalConfirmation)));
              HTML reportHtml = new HTML(SafeHtmlUtils.fromSafeConstant(report));
              instance.contentFlowPanel.add(reportHtml);
              callback.onSuccess(instance);
            }

          }));
    } else {
      HistoryUtils.newHistory(DisposalConfirmations.RESOLVER);
      callback.onSuccess(null);
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  interface MyUiBinder extends UiBinder<Widget, ShowDisposalConfirmation> {
  }
}
