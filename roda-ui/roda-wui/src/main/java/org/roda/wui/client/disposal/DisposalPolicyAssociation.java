package org.roda.wui.client.disposal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.DisposalAssociationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class DisposalPolicyAssociation extends Composite {
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

        List<String> fieldsToReturn = new ArrayList<>();
        fieldsToReturn.addAll(
          Arrays.asList(RodaConstants.INDEX_AIP, RodaConstants.AIP_TITLE, RodaConstants.AIP_DISPOSAL_SCHEDULE_NAME));
        BrowserService.Util.getInstance().retrieveBrowseAIPBundle(aipId, LocaleInfo.getCurrentLocale().getLocaleName(),
          fieldsToReturn, new AsyncCallback<BrowseAIPBundle>() {
            @Override
            public void onFailure(Throwable throwable) {

            }

            @Override
            public void onSuccess(BrowseAIPBundle bundle) {
              callback.onSuccess(new DisposalPolicyAssociation(bundle));
            }
          });
      }
    }
  };

  private static DisposalPolicyAssociation instance = null;

  @UiField
  FlowPanel disposalPolicyAIPDescription;

  @UiField
  TitlePanel titlePanel;

  @UiField
  FlowPanel content;

  @UiField
  Label disposalName;

  @UiField
  Label disposalRetentionStartDate;

  @UiField
  Label disposalRetentionDueDate;

  @UiField
  Label disposalRetentionPeriod;

  @UiField
  Label disposalDisposalAction;

  @UiField
  Label disposalDisposalStatus;

  @UiField
  FlowPanel disposalHoldsList;

  @UiField
  SimplePanel actionsSidebar;
  ActionableWidgetBuilder<IndexedAIP> actionableWidgetBuilder;

  interface MyUiBinder extends UiBinder<Widget, DisposalPolicyAssociation> {
  }

  private static DisposalPolicyAssociation.MyUiBinder uiBinder = GWT.create(DisposalPolicyAssociation.MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public DisposalPolicyAssociation(BrowseAIPBundle bundle) {
    initWidget(uiBinder.createAndBindUi(this));

    actionableWidgetBuilder = new ActionableWidgetBuilder<>(DisposalAssociationActions.get());

    IndexedAIP aip = bundle.getAip();
    titlePanel.setText(aip.getTitle());

    if (aip.getDisposalScheduleId() == null) {
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters("disposal schedules")));
      label.addStyleName("disposalSchedulesEmpty");
      content.add(label);
    } else {
      disposalName.setText(aip.getDisposalScheduleName());
      disposalRetentionStartDate.setText(aip.getCreatedOn().toString());
      disposalRetentionDueDate.setText(aip.getOverdueDate().toString());
      disposalRetentionPeriod
        .setText(aip.getDisposalRetentionPeriodDuration() + " " + aip.getDisposalRetentionPeriodCode());
      disposalDisposalAction.setText(aip.getDisposalAction());
      if (aip.isDisposalHoldStatus()) {
        disposalDisposalStatus.setText("On Hold");
      } else {
        disposalDisposalStatus.setText("CLEAR");
      }
    }

    actionsSidebar
      .setWidget(actionableWidgetBuilder.withBackButton().buildListWithObjects(new ActionableObject<>(aip)));

    if(!aip.getDisposalHoldsId().isEmpty()){
     fetchDisposalHolds(aip.getDisposalHoldsId());
    }
  }

  private void fetchDisposalHolds(List<String> disposalHoldsId) {
    for (String disposalHoldId : disposalHoldsId) {
      BrowserService.Util.getInstance().retrieveDisposalHold(disposalHoldId, new AsyncCallback<DisposalHold>() {
        @Override
        public void onFailure(Throwable throwable) {

        }

        @Override
        public void onSuccess(DisposalHold disposalHold) {
          addDisposalHold(disposalHold);
        }
      });
    }
  }

  private void addDisposalHold(DisposalHold disposalHold) {
    disposalHoldsList.add(new Label(disposalHold.getTitle()));
  }
}
