package org.roda.wui.client.disposal;

import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class DisposalPolicy extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      BrowserService.Util.getInstance().listDisposalSchedules(new AsyncCallback<DisposalSchedules>() {
        @Override
        public void onFailure(Throwable throwable) {

        }

        @Override
        public void onSuccess(DisposalSchedules disposalSchedules) {
          callback.onSuccess(new DisposalPolicy(disposalSchedules));
        }
      });
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Disposal.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "policy";
    }
  };

  private static DisposalPolicy instance = null;

  interface MyUiBinder extends UiBinder<Widget, DisposalPolicy> {
  }

  private static DisposalPolicy.MyUiBinder uiBinder = GWT.create(DisposalPolicy.MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel disposalPolicyDescription;

  @UiField
  FlowPanel content;

  private DisposalSchedules disposalSchedules;

  /**
   * Create a representation information page
   */
  private DisposalPolicy(DisposalSchedules disposalSchedules) {
    initWidget(uiBinder.createAndBindUi(this));

    this.disposalSchedules =disposalSchedules;

    GWT.log("" + disposalSchedules.getObjects().size());

    if (disposalSchedules.getObjects().isEmpty()) {
      content.add(new HTML(SafeHtmlUtils.fromSafeConstant("VAZIO!!!")));
    }

    disposalPolicyDescription.add(new HTMLWidgetWrapper("DisposalPolicyDescription.html"));
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      callback.onSuccess(this);
    } /*
       * else { String basePage = historyTokens.remove(0); if
       * (ShowRepresentationInformation.RESOLVER.getHistoryToken().equals(basePage)) {
       * ShowRepresentationInformation.RESOLVER.resolve(historyTokens, callback); }
       * else if
       * (CreateRepresentationInformation.RESOLVER.getHistoryToken().equals(basePage))
       * { CreateRepresentationInformation.RESOLVER.resolve(historyTokens, callback);
       * } else if
       * (EditRepresentationInformation.RESOLVER.getHistoryToken().equals(basePage)) {
       * EditRepresentationInformation.RESOLVER.resolve(historyTokens, callback); }
       * else if
       * (RepresentationInformationAssociations.RESOLVER.getHistoryToken().equals(
       * basePage)) {
       * RepresentationInformationAssociations.RESOLVER.resolve(historyTokens,
       * callback); } else if (Search.RESOLVER.getHistoryToken().equals(basePage)) {
       * searchPanel.setFilter(RepresentationInformation.class,
       * SearchFilters.createFilterFromHistoryTokens(historyTokens));
       * callback.onSuccess(this); } else { HistoryUtils.newHistory(RESOLVER);
       * callback.onSuccess(null); } }
       */
  }
}
