/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.hold;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.DisposalHoldAlreadyExistsException;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.core.data.v2.generics.select.SelectedItemsFilterRequest;
import org.roda.core.data.v2.generics.select.SelectedItemsRequest;
import org.roda.core.data.v2.index.CountRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposalhold.DisassociateDisposalHoldRequest;
import org.roda.core.data.v2.ip.disposalhold.UpdateDisposalHoldRequest;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.DisposalHoldActions;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class ShowDisposalHold extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static ShowDisposalHold instance = null;
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
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
  private static ShowDisposalHold.MyUiBinder uiBinder = GWT.create(ShowDisposalHold.MyUiBinder.class);
  @UiField
  Label disposalHoldId;
  @UiField
  Label dateCreated, dateUpdated;
  @UiField
  TitlePanel title;
  @UiField
  Label disposalHoldMandateKey;
  @UiField
  HTML disposalHoldMandateValue;
  @UiField
  Label disposalHoldDescriptionKey;
  @UiField
  HTML disposalHoldDescriptionValue;
  @UiField
  Label disposalHoldNotesKey;
  @UiField
  HTML disposalHoldNotesValue;
  @UiField
  Label disposalHoldStateKey;
  @UiField
  HTML disposalHoldStateValue;
  @UiField
  FlowPanel buttonsPanel;
  @UiField
  FlowPanel aipListTitle;
  @UiField
  SimplePanel aipsListCard;
  private DisposalHold disposalHold;
  private SearchWrapper aipsSearchWrapper;

  public ShowDisposalHold() {
    this.disposalHold = new DisposalHold();
  }

  public ShowDisposalHold(final DisposalHold disposalHold) {
    instance = this;
    this.disposalHold = disposalHold;
    initAipsList();

    initWidget(uiBinder.createAndBindUi(this));
    initElements();
    initButtons();
  }

  public static ShowDisposalHold getInstance() {
    if (instance == null) {
      instance = new ShowDisposalHold();
    }
    return instance;
  }

  private void initAipsList() {
    DisposalHoldActions disposalHoldActions = new DisposalHoldActions(disposalHold);

    AsyncCallback<Actionable.ActionImpact> listActionableCallback = new NoAsyncCallback<Actionable.ActionImpact>() {
      @Override
      public void onSuccess(Actionable.ActionImpact impact) {
        if (!Actionable.ActionImpact.NONE.equals(impact)) {
          refresh();
        }
      }
    };

    ListBuilder<IndexedAIP> aipsListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
      new AsyncTableCellOptions<>(IndexedAIP.class, "ShowDisposalHold_aips")
        .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.AIP_DISPOSAL_HOLDS_ID, disposalHold.getId())))
        .withSummary(messages.listOfAIPs()).bindOpener().withActionable(disposalHoldActions)
        .withActionableCallback(listActionableCallback));

    aipsSearchWrapper = new SearchWrapper(false).createListAndSearchPanel(aipsListBuilder);
  }

  public void initElements() {
    title.setText(disposalHold.getTitle());

    disposalHoldId.setText(messages.disposalHoldIdentifier() + ": " + disposalHold.getId());

    if (disposalHold.getCreatedOn() != null && StringUtils.isNotBlank(disposalHold.getCreatedBy())) {
      dateCreated.setText(
        messages.dateCreated(Humanize.formatDateTime(disposalHold.getCreatedOn()), disposalHold.getCreatedBy()));
    }

    if (disposalHold.getUpdatedOn() != null && StringUtils.isNotBlank(disposalHold.getUpdatedBy())) {
      dateUpdated.setText(
        messages.dateUpdated(Humanize.formatDateTime(disposalHold.getUpdatedOn()), disposalHold.getUpdatedBy()));
    }

    disposalHoldDescriptionValue.setHTML(SafeHtmlUtils.fromString(disposalHold.getDescription()));
    disposalHoldDescriptionKey.setVisible(StringUtils.isNotBlank(disposalHold.getDescription()));

    disposalHoldMandateValue.setHTML(SafeHtmlUtils.fromString(disposalHold.getMandate()));
    disposalHoldMandateKey.setVisible(StringUtils.isNotBlank(disposalHold.getMandate()));

    disposalHoldNotesValue.setHTML(SafeHtmlUtils.fromString(disposalHold.getScopeNotes()));
    disposalHoldNotesKey.setVisible(StringUtils.isNotBlank(disposalHold.getScopeNotes()));

    disposalHoldStateValue.setHTML(HtmlSnippetUtils.getDisposalHoldStateHtml(disposalHold));

    // Records with this hold

    if (disposalHold.getState().equals(DisposalHoldState.ACTIVE)
      && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_AIP)) {
      Label aipTitle = new Label();
      aipTitle.addStyleName("h5");
      aipTitle.setText(messages.disposalHoldListAips());
      aipListTitle.clear();
      aipListTitle.add(aipTitle);

      aipsListCard.setWidget(aipsSearchWrapper);
      aipsListCard.setVisible(true);
    } else {
      aipListTitle.setVisible(false);
      aipsListCard.setVisible(false);
    }

  }

  public void initButtons() {
    buttonsPanel.clear();

    if (disposalHold.getState().equals(DisposalHoldState.ACTIVE)
      && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_UPDATE_DISPOSAL_HOLD)) {
      Button editHoldBtn = new Button();
      editHoldBtn.addStyleName("btn btn-block btn-edit");
      editHoldBtn.setText(messages.editButton());
      editHoldBtn
        .addClickHandler(clickEvent -> HistoryUtils.newHistory(EditDisposalHold.RESOLVER, disposalHold.getId()));

      buttonsPanel.add(editHoldBtn);

      Button liftHoldBtn = new Button();
      liftHoldBtn.addStyleName("btn btn-block btn-danger btn-lift-hold");
      liftHoldBtn.setText(messages.liftButton());

      liftHoldBtn.addClickHandler(clickEvent -> {
        Dialogs.showConfirmDialog(messages.liftDisposalHoldDialogTitle(), messages.liftDisposalHoldDialogMessage(1),
          messages.cancelButton(), messages.confirmButton(), new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable throwable) {
              // do nothing
            }

            @Override
            public void onSuccess(Boolean confirm) {
              if (confirm) {
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, true,
                  new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                      // do nothing
                    }

                    @Override
                    public void onSuccess(String details) {
                      Services services = new Services("Lift disposal hold", "job");

                      services.disposalHoldResource(s -> s.liftDisposalHold(disposalHold.getId(), details))
                        .whenComplete((job, throwable) -> {
                          if (throwable != null) {
                            AsyncCallbackUtils.defaultFailureTreatment(throwable);
                          } else {
                            Toast.showInfo(messages.runningInBackgroundTitle(), messages.updateDisposalHoldMessage());
                            Timer timer = new Timer() {
                              @Override
                              public void run() {
                                refresh();
                              }
                            };

                            timer.schedule(RodaConstants.ACTION_TIMEOUT);
                          }
                        });
                    }
                  });
              }
            }
          });
      });
      buttonsPanel.add(liftHoldBtn);
    }

    Button backBtn = new Button();
    backBtn.setText(messages.backButton());
    backBtn.addStyleName("btn btn-block btn-default btn-times-circle");
    backBtn.addClickHandler(clickEvent -> HistoryUtils.newHistory(DisposalPolicy.RESOLVER));
    buttonsPanel.add(backBtn);
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof DisposalHoldAlreadyExistsException) {
      Toast.showError(messages.createDisposalHoldAlreadyExists(disposalHold.getTitle()));
    } else {
      Toast.showError(messages.createDisposalHoldFailure(caught.getMessage()));
    }
  }

  private void refresh() {
    Services services = new Services("Retrieve disposal hold", "get");
    services.disposalHoldResource(s -> s.retrieveDisposalHold(disposalHold.getId())).whenComplete((hold, throwable) -> {
      if (throwable != null) {
        AsyncCallbackUtils.defaultFailureTreatment(throwable);
      } else {
        disposalHold = hold;
        initElements();
        initAipsList();
        initButtons();
      }
    });
  }

  private void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
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

  interface MyUiBinder extends UiBinder<Widget, ShowDisposalHold> {
  }
}
