package org.roda.wui.client.disposal.hold;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.DisposalHoldAlreadyExistsException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHoldState;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.server.browse.BrowserServiceImpl;

import com.google.gwt.core.client.GWT;
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

  private static ShowDisposalHold instance = null;

  interface MyUiBinder extends UiBinder<Widget, ShowDisposalHold> {
  }

  private static ShowDisposalHold.MyUiBinder uiBinder = GWT.create(ShowDisposalHold.MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private DisposalHold disposalHold;

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

  public ShowDisposalHold() {
    this.disposalHold = new DisposalHold();
  }

  public ShowDisposalHold(final DisposalHold disposalHold) {
    instance = this;
    this.disposalHold = disposalHold;

    initWidget(uiBinder.createAndBindUi(this));
    initElements();
    initButtons();
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

    disposalHoldDescriptionValue.setHTML(disposalHold.getDescription());
    disposalHoldDescriptionKey.setVisible(StringUtils.isNotBlank(disposalHold.getDescription()));

    disposalHoldMandateValue.setHTML(disposalHold.getMandate());
    disposalHoldMandateKey.setVisible(StringUtils.isNotBlank(disposalHold.getMandate()));

    disposalHoldNotesValue.setHTML(disposalHold.getScopeNotes());
    disposalHoldNotesKey.setVisible(StringUtils.isNotBlank(disposalHold.getScopeNotes()));

    disposalHoldStateValue.setHTML(HtmlSnippetUtils.getDisposalHoldStateHtml(disposalHold));

    // Records with this schedule

    if (disposalHold.getState().equals(DisposalHoldState.ACTIVE)
      && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_AIP)) {
      Label aipTitle = new Label();
      aipTitle.addStyleName("h5");
      aipTitle.setText(messages.disposalHoldListAips());
      aipListTitle.add(aipTitle);

      ListBuilder<IndexedAIP> aipsListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
        new AsyncTableCellOptions<>(IndexedAIP.class, "ShowDisposalHold_aips")
          .withFilter(new Filter(new SimpleFilterParameter(RodaConstants.AIP_DISPOSAL_HOLDS_ID, disposalHold.getId())))
          .withSummary(messages.listOfAIPs()).bindOpener());

      SearchWrapper aipsSearchWrapper = new SearchWrapper(false).createListAndSearchPanel(aipsListBuilder);
      aipsListCard.setWidget(aipsSearchWrapper);
      aipsListCard.setVisible(true);
    } else {
      aipsListCard.setVisible(false);
    }

  }

  public void initButtons() {

    if (disposalHold.getState().equals(DisposalHoldState.ACTIVE)
      && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_UPDATE_DISPOSAL_HOLD)) {
      Button editHoldBtn = new Button();
      editHoldBtn.addStyleName("btn btn-block btn-edit");
      editHoldBtn.setText(messages.editButton());
      editHoldBtn
        .addClickHandler(clickEvent -> HistoryUtils.newHistory(EditDisposalHold.RESOLVER, disposalHold.getId()));

      buttonsPanel.add(editHoldBtn);

      Button liftHoldBtn = new Button();
      liftHoldBtn.addStyleName("btn btn-block btn-danger btn-ban");
      liftHoldBtn.setText(messages.liftButton());

      liftHoldBtn.addClickHandler(clickEvent -> {
        if (disposalHold.getFirstTimeUsed() == null) {
          disposalHold.setState(DisposalHoldState.LIFTED);
          BrowserServiceImpl.Util.getInstance().updateDisposalHold(disposalHold, new NoAsyncCallback<DisposalHold>() {
            @Override
            public void onSuccess(DisposalHold disposalHold) {
              HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
            }
          });
        } else {
          Filter filter = new Filter(
            new SimpleFilterParameter(RodaConstants.AIP_DISPOSAL_HOLDS_ID, disposalHold.getId()));
          SelectedItemsFilter<IndexedAIP> selectedItemsFilter = new SelectedItemsFilter<>(filter,
            IndexedAIP.class.getName(), true);
          BrowserService.Util.getInstance().liftDisposalHold(selectedItemsFilter, disposalHold.getId(),
            new AsyncCallback<Job>() {
              @Override
              public void onFailure(Throwable throwable) {
                HistoryUtils.newHistory(InternalProcess.RESOLVER);
              }

              @Override
              public void onSuccess(Job job) {
                Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                    Timer timer = new Timer() {
                      @Override
                      public void run() {
                        refresh();
                      }
                    };

                    timer.schedule(RodaConstants.ACTION_TIMEOUT);
                  }

                  @Override
                  public void onSuccess(final Void nothing) {
                    HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                  }
                });
              }
            });
        }
      });
      buttonsPanel.add(liftHoldBtn);
    }

    Button backBtn = new Button();
    backBtn.setText(messages.backButton());
    backBtn.addStyleName("btn btn-block btn-default btn-times-circle");
    backBtn.addClickHandler(clickEvent -> HistoryUtils.newHistory(DisposalPolicy.RESOLVER));
    buttonsPanel.add(backBtn);
  }

  public static ShowDisposalHold getInstance() {
    if (instance == null) {
      instance = new ShowDisposalHold();
    }
    return instance;
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof DisposalHoldAlreadyExistsException) {
      Toast.showError(messages.createDisposalHoldAlreadyExists(disposalHold.getTitle()));
    } else {
      Toast.showError(messages.createDisposalHoldFailure(caught.getMessage()));
    }
  }

  private void refresh() {
    BrowserService.Util.getInstance().retrieveDisposalHold(disposalHold.getId(), new NoAsyncCallback<DisposalHold>() {
      @Override
      public void onSuccess(DisposalHold result) {
        disposalHold = result;
      }
    });
  }

  private void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      BrowserService.Util.getInstance().retrieveDisposalHold(historyTokens.get(0), new NoAsyncCallback<DisposalHold>() {
        @Override
        public void onSuccess(DisposalHold result) {
          ShowDisposalHold panel = new ShowDisposalHold(result);
          callback.onSuccess(panel);
        }
      });
    }
  }
}
