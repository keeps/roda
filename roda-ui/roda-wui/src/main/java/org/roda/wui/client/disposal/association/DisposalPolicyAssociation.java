package org.roda.wui.client.disposal.association;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.ui.HTML;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.common.DisposalPolicySummaryPanel;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.DisposalAssociationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.utils.DisposalPolicyUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.disposal.Disposal;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class DisposalPolicyAssociation extends Composite {
  private static final List<String> fieldsToReturn = new ArrayList<>(
    Arrays.asList(RodaConstants.INDEX_AIP, RodaConstants.AIP_TITLE, RodaConstants.AIP_DISPOSAL_SCHEDULE_NAME));

  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public String getHistoryToken() {
      return "association";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Disposal.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 2) {
        final String aipId = historyTokens.get(1);
        BrowserService.Util.getInstance().retrieveBrowseAIPBundle(aipId, LocaleInfo.getCurrentLocale().getLocaleName(),
          fieldsToReturn, new NoAsyncCallback<BrowseAIPBundle>() {
            @Override
            public void onSuccess(BrowseAIPBundle bundle) {
              callback.onSuccess(new DisposalPolicyAssociation(bundle));
            }
          });
      }
    }
  };

  private IndexedAIP aip;

  @UiField
  NavigationToolbar<IndexedAIP> navigationToolbar;

  @UiField
  TitlePanel titlePanel;

  @UiField
  FlowPanel content;

  @UiField
  DisposalPolicySummaryPanel disposalPolicySummaryPanel;

  @UiField(provided = true)
  DisposalConfirmationPanel disposalConfirmationPanel;

  @UiField(provided = true)
  RetentionPeriodPanel retentionPeriodPanel;

  @UiField(provided = true)
  DisposalHoldsPanel disposalHoldsPanel;

  @UiField
  SimplePanel actionsSidebar;

  ActionableWidgetBuilder<IndexedAIP> actionableWidgetBuilder;

  interface MyUiBinder extends UiBinder<Widget, DisposalPolicyAssociation> {
  }

  private static DisposalPolicyAssociation.MyUiBinder uiBinder = GWT.create(DisposalPolicyAssociation.MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public DisposalPolicyAssociation(BrowseAIPBundle bundle) {
    disposalConfirmationPanel = new DisposalConfirmationPanel(bundle.getAip().getDisposalConfirmationId());

    retentionPeriodPanel = new RetentionPeriodPanel(bundle.getAip());

    disposalHoldsPanel = new DisposalHoldsPanel(bundle.getAip());

    initWidget(uiBinder.createAndBindUi(this));

    actionableWidgetBuilder = new ActionableWidgetBuilder<>(DisposalAssociationActions.get());

    aip = bundle.getAip();
    titlePanel.setText(aip.getTitle());
    titlePanel.setIcon(DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), false));

    // NAVIGATION TOOLBAR
    navigationToolbar.withObject(aip);
    navigationToolbar.withoutButtons();
    navigationToolbar.withPermissions(aip.getPermissions());
    navigationToolbar.build();

    // DISPOSAL POLICY SUMMARY
    disposalPolicySummaryPanel.setIcon("fas fa-info-circle");
    disposalPolicySummaryPanel.setText(DisposalPolicyUtils.getDisposalPolicySummaryText(aip));

    BreadcrumbItem item = new BreadcrumbItem(messages.disposalPolicyTitle(),
      () -> HistoryUtils.newHistory(DisposalPolicyAssociation.RESOLVER, aip.getId()));
    List<BreadcrumbItem> aipBreadcrumbs = BreadcrumbUtils.getAipBreadcrumbs(bundle.getAIPAncestors(), bundle.getAip());
    aipBreadcrumbs.add(item);
    navigationToolbar.updateBreadcrumb(bundle);
    navigationToolbar.updateBreadcrumbPath(aipBreadcrumbs);

    actionsSidebar
      .setWidget(actionableWidgetBuilder.withBackButton().buildListWithObjects(new ActionableObject<>(aip)));
  }
}
