package org.roda.wui.client.disposal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.ui.Button;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHoldAssociation;
import org.roda.core.data.v2.ip.disposal.DisposalHolds;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.DisposalAssociationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.disposal.hold.CreateDisposalHold;
import org.roda.wui.client.disposal.hold.ShowDisposalHold;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

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
  private List<DisposalHold> disposalHoldList = new ArrayList<>();
  private IndexedAIP aip;

  @UiField
  FlowPanel disposalPolicyAIPDescription;

  @UiField
  TitlePanel titlePanel;

  @UiField
  FlowPanel content, disposalSchedule, disposalHolds;

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
  FlowPanel disposalHoldsPanel;

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

    aip = bundle.getAip();
    titlePanel.setText(aip.getTitle());
    titlePanel.setIcon(DescriptionLevelUtils.getElementLevelIconSafeHtml(aip.getLevel(), false));


    if (aip.getDisposalScheduleId() == null || aip.getDisposalScheduleId().isEmpty()) {
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(messages.showDisposalScheduleTitle())));
      label.addStyleName("disposalSchedulesEmpty");
      disposalSchedule.clear();
      disposalSchedule.add(label);
    } else {
      disposalName.setText(aip.getDisposalScheduleName());
      disposalRetentionStartDate.setText(aip.getCreatedOn().toString());
      if (aip.getOverdueDate() != null) {
        disposalRetentionDueDate.setText(aip.getOverdueDate().toString());
      }
      disposalRetentionPeriod
        .setText(aip.getDisposalRetentionPeriod());
      disposalDisposalAction.setText(aip.getDisposalAction());
      GWT.log("status " + aip.isDisposalHoldStatus());
      if (aip.isDisposalHoldStatus()) {
        disposalDisposalStatus.setText("On Hold");
      } else {
        disposalDisposalStatus.setText("CLEAR");
      }
    }

    actionsSidebar
      .setWidget(actionableWidgetBuilder.withBackButton().buildListWithObjects(new ActionableObject<>(aip)));

    if (aip.getDisposalHoldsId().isEmpty()) {
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(messages.showDisposalHoldTitle())));
      label.addStyleName("disposalSchedulesEmpty");
      disposalHoldsPanel.clear();
      disposalHoldsPanel.add(label);
      disposalHoldsPanel.add(getAddDisposalHoldBtn());
    } else {
      BrowserService.Util.getInstance().listDisposalHoldsAssociation(aip.getId(),
        new AsyncCallback<List<DisposalHoldAssociation>>() {
          @Override
          public void onFailure(Throwable throwable) {
            GWT.log("Fail");
          }

          @Override
          public void onSuccess(List<DisposalHoldAssociation> disposalHoldAssociations) {
            fetchDisposalHolds(aip.getDisposalHoldsId(), disposalHoldAssociations);
          }
        });
    }
  }

  private void fetchDisposalHolds(List<String> disposalHoldsId,
    List<DisposalHoldAssociation> disposalHoldAssociations) {
    for (String disposalHoldId : disposalHoldsId) {
      BrowserService.Util.getInstance().retrieveDisposalHold(disposalHoldId, new AsyncCallback<DisposalHold>() {
        @Override
        public void onFailure(Throwable throwable) {

        }

        @Override
        public void onSuccess(DisposalHold disposalHold) {
          disposalHoldList.add(disposalHold);
          refreshDisposalHoldList(disposalHoldAssociations);
        }
      });
    }
  }

  private void refreshDisposalHoldList(List<DisposalHoldAssociation> disposalHoldAssociations) {
    DisposalHolds disposalHolds = new DisposalHolds(disposalHoldList);
    BasicTablePanel<DisposalHold> tableHolds = getBasicTablePanelForDisposalHolds(disposalHolds,
      disposalHoldAssociations);
    tableHolds.getSelectionModel().addSelectionChangeHandler(event -> {
      DisposalHold selectedHold = tableHolds.getSelectionModel().getSelectedObject();
      if (selectedHold != null) {
        tableHolds.getSelectionModel().clear();
        List<String> path = HistoryUtils.getHistory(ShowDisposalHold.RESOLVER.getHistoryPath(), selectedHold.getId());
        HistoryUtils.newHistory(path);
      }
    });
    disposalHoldsPanel.clear();
    disposalHoldsPanel.add(tableHolds);
    disposalHoldsPanel.add(getAddDisposalHoldBtn());
  }

  private Button getAddDisposalHoldBtn() {
    Button newDisposalHoldBtn = new Button();
    newDisposalHoldBtn.addStyleName("btn btn-plus");
    newDisposalHoldBtn.setText(messages.newDisposalHoldTitle());
    newDisposalHoldBtn.addClickHandler(event -> CreateDisposalHold.RESOLVER.getHistoryToken());
    return newDisposalHoldBtn;
  }

  private BasicTablePanel<DisposalHold> getBasicTablePanelForDisposalHolds(DisposalHolds disposalHolds,
    List<DisposalHoldAssociation> disposalHoldAssociations) {
    Label headerHolds = new Label();
    HTMLPanel info = new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML);

    if (disposalHolds.getObjects().isEmpty()) {
      return new BasicTablePanel<>(headerHolds, messages.noItemsToDisplayPreFilters("disposal holds"));
    } else {
      return new BasicTablePanel<DisposalHold>(headerHolds, info, disposalHolds.getObjects().iterator(),

        new BasicTablePanel.ColumnInfo<>(messages.disposalHoldTitle(), 15, new TextColumn<DisposalHold>() {
          @Override
          public String getValue(DisposalHold hold) {
            return hold.getTitle();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.aipCreated(), 15, new TextColumn<DisposalHold>() {
          @Override
          public String getValue(DisposalHold hold) {
            DisposalHoldAssociation association = disposalHoldAssociations.stream()
              .filter(disposalHoldAssociation -> hold.getId().equals(disposalHoldAssociation.getId())).findAny()
              .orElse(null);
            if (association != null && association.getAssociatedOn() != null) {
              return association.getAssociatedOn().toString();
            } else {
              return null;
            }
          }
        }),

        new BasicTablePanel.ColumnInfo<>("Lifted on", 15, new TextColumn<DisposalHold>() {
          @Override
          public String getValue(DisposalHold hold) {
            DisposalHoldAssociation association = disposalHoldAssociations.stream()
              .filter(disposalHoldAssociation -> hold.getId().equals(disposalHoldAssociation.getId())).findAny()
              .orElse(null);
            if (association != null && association.getLiftedOn() != null) {
              return association.getLiftedOn().toString();
            } else {
              return null;
            }
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalHoldStateCol(), 8,
          new Column<DisposalHold, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(DisposalHold hold) {
              return HtmlSnippetUtils.getDisposalHoldStateHtml(hold);
            }
          }));
    }
  }
}
