package org.roda.wui.client.disposal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHoldAssociation;
import org.roda.core.data.v2.ip.disposal.DisposalHolds;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.bundle.BrowseAIPBundle;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.DisposalAssociationActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.dialogs.DisposalDialogs;
import org.roda.wui.client.common.dialogs.utils.DisposalHoldDialogResult;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.disposal.hold.ShowDisposalHold;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class DisposalPolicyAssociation extends Composite {
  private static final List<String> aipFieldsToReturn = new ArrayList<>(
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
          aipFieldsToReturn, new NoAsyncCallback<BrowseAIPBundle>() {
            @Override
            public void onSuccess(BrowseAIPBundle bundle) {
              BrowserService.Util.getInstance().listDisposalHoldsAssociation(bundle.getAip().getId(),
                new NoAsyncCallback<List<DisposalHoldAssociation>>() {
                  @Override
                  public void onSuccess(List<DisposalHoldAssociation> disposalHoldAssociations) {
                    callback.onSuccess(new DisposalPolicyAssociation(bundle, disposalHoldAssociations));
                  }
                });

            }
          });
      }
    }
  };

  private static DisposalPolicyAssociation instance = null;
  private List<DisposalHold> disposalHoldList = new ArrayList<>();
  private IndexedAIP aip;

  @UiField
  NavigationToolbar<IndexedAIP> navigationToolbar;

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
  HTML disposalDisposalStatus;

  @UiField
  FlowPanel disposalHoldsPanel;

  @UiField
  Button addDisposalHoldBtn;

  @UiField
  SimplePanel actionsSidebar;
  ActionableWidgetBuilder<IndexedAIP> actionableWidgetBuilder;

  interface MyUiBinder extends UiBinder<Widget, DisposalPolicyAssociation> {
  }

  private static DisposalPolicyAssociation.MyUiBinder uiBinder = GWT.create(DisposalPolicyAssociation.MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public DisposalPolicyAssociation(BrowseAIPBundle bundle, List<DisposalHoldAssociation> disposalHoldAssociations) {
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

    BreadcrumbItem item = new BreadcrumbItem(messages.disposalPolicyTitle(),
      () -> HistoryUtils.newHistory(DisposalPolicyAssociation.RESOLVER, aip.getId()));
    List<BreadcrumbItem> aipBreadcrumbs = BreadcrumbUtils.getAipBreadcrumbs(bundle.getAIPAncestors(), bundle.getAip());
    aipBreadcrumbs.add(item);
    navigationToolbar.updateBreadcrumb(bundle);
    navigationToolbar.updateBreadcrumbPath(aipBreadcrumbs);

    if (aip.getDisposalScheduleId() == null || aip.getDisposalScheduleId().isEmpty()) {
      Label label = new HTML(
        SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(messages.showDisposalScheduleTitle())));
      label.addStyleName("disposalSchedulesEmpty");
      disposalSchedule.clear();
      disposalSchedule.add(label);
    } else {
      disposalName.setText(aip.getDisposalScheduleName());
      disposalRetentionStartDate.setText(aip.getCreatedOn().toString());
      if (aip.getOverdueDate() != null) {
        disposalRetentionDueDate.setText(aip.getOverdueDate().toString());
      }
      disposalRetentionPeriod.setText(aip.getDisposalRetentionPeriod());
      disposalDisposalAction.setText(aip.getDisposalAction());

      if (aip.isDisposalHoldStatus()) {
        disposalDisposalStatus.setText("On Hold");
      } else {
        disposalDisposalStatus.setText("CLEAR");
      }
    }

    actionsSidebar
      .setWidget(actionableWidgetBuilder.withBackButton().buildListWithObjects(new ActionableObject<>(aip)));

    fetchDisposalHolds(disposalHoldAssociations);
  }

  private void fetchDisposalHolds(List<DisposalHoldAssociation> disposalHoldAssociations) {
    for (DisposalHoldAssociation association : disposalHoldAssociations) {
      BrowserService.Util.getInstance().retrieveDisposalHold(association.getId(), new NoAsyncCallback<DisposalHold>() {
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
    BasicTablePanel<DisposalHoldAssociation> tableHolds = getBasicTablePanelForDisposalHolds(disposalHolds,
      disposalHoldAssociations);
    tableHolds.getSelectionModel().addSelectionChangeHandler(event -> {
      DisposalHoldAssociation selectedObject = tableHolds.getSelectionModel().getSelectedObject();
      if (selectedObject != null) {
        List<String> path = HistoryUtils.getHistory(ShowDisposalHold.RESOLVER.getHistoryPath(), selectedObject.getId());
        HistoryUtils.newHistory(path);
      }
    });
    disposalHoldsPanel.clear();
    disposalHoldsPanel.add(tableHolds);
    disposalHoldsPanel.add(associateDisposalHoldButton());
  }

  private Button associateDisposalHoldButton() {
    Button associateDisposalHoldButton = new Button();
    associateDisposalHoldButton.addStyleName("btn btn-plus");
    associateDisposalHoldButton.setText(messages.associateDisposalHoldButton());
    associateDisposalHoldButton.addClickHandler(clickEvent -> {
      BrowserService.Util.getInstance().listDisposalHolds(new NoAsyncCallback<DisposalHolds>() {
        @Override
        public void onSuccess(DisposalHolds holds) {
          DisposalDialogs.showDisposalHoldSelection("", holds, new NoAsyncCallback<DisposalHoldDialogResult>() {
            @Override
            public void onSuccess(DisposalHoldDialogResult result) {

            }
          });
        }
      });

    });
    return associateDisposalHoldButton;
  }

  private BasicTablePanel<DisposalHoldAssociation> getBasicTablePanelForDisposalHolds(DisposalHolds disposalHolds,
    List<DisposalHoldAssociation> disposalHoldAssociations) {
    Label headerHolds = new Label();
    HTMLPanel info = new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML);

    disposalHoldAssociations.sort(Comparator.comparing(DisposalHoldAssociation::getAssociatedOn));

    if (disposalHoldAssociations.isEmpty()) {
      return new BasicTablePanel<>(headerHolds, messages.noItemsToDisplayPreFilters(messages.disposalHoldsTitle()));
    } else {
      return new BasicTablePanel<DisposalHoldAssociation>(headerHolds, info, disposalHoldAssociations.iterator(),

        new BasicTablePanel.ColumnInfo<>(messages.disposalHoldTitle(), 15, new TextColumn<DisposalHoldAssociation>() {
          @Override
          public String getValue(DisposalHoldAssociation association) {
            DisposalHold hold = disposalHolds.findDisposalHold(association.getId());
            if (hold != null && hold.getTitle() != null) {
              return hold.getTitle();
            } else {
              return "";
            }
          }
        }),

        new BasicTablePanel.ColumnInfo<DisposalHoldAssociation>(messages.disposalHoldAssociatedOn(), 15,
          new TextColumn<DisposalHoldAssociation>() {
            @Override
            public String getValue(DisposalHoldAssociation association) {
              if (association != null && association.getAssociatedOn() != null) {
                return Humanize.formatDate(association.getAssociatedOn());
              } else {
                return "";
              }
            }
          }),

        new BasicTablePanel.ColumnInfo<DisposalHoldAssociation>(messages.disposalHoldLiftedOn(), 15,
          new TextColumn<DisposalHoldAssociation>() {
            @Override
            public String getValue(DisposalHoldAssociation association) {
              if (association != null && association.getLiftedOn() != null) {
                return Humanize.formatDate(association.getLiftedOn());
              } else {
                return "";
              }
            }
          }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalHoldStateCol(), 8,
          new Column<DisposalHoldAssociation, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(DisposalHoldAssociation association) {
              return HtmlSnippetUtils.getDisposalHoldStateHtml(association);
            }
          }));
    }
  }
}
