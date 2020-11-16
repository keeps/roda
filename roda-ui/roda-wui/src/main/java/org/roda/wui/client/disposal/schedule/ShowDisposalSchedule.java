package org.roda.wui.client.disposal.schedule;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalRules;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalScheduleState;
import org.roda.core.data.v2.ip.disposal.RetentionPeriodIntervalCode;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.server.browse.BrowserServiceImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
public class ShowDisposalSchedule extends Composite {
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
      return "disposal_schedule";
    }
  };

  private static ShowDisposalSchedule instance = null;

  interface MyUiBinder extends UiBinder<Widget, ShowDisposalSchedule> {
  }

  private static ShowDisposalSchedule.MyUiBinder uiBinder = GWT.create(ShowDisposalSchedule.MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private DisposalSchedule disposalSchedule;
  private DisposalRules disposalRules;

  public static ShowDisposalSchedule getInstance() {
    if (instance == null) {
      instance = new ShowDisposalSchedule();
    }
    return instance;
  }

  @UiField
  TitlePanel title;

  @UiField
  Label disposalScheduleId;

  @UiField
  Label dateCreated, dateUpdated;

  @UiField
  Label descriptionLabel;

  @UiField
  HTML descriptionValue;

  @UiField
  Label mandateLabel;

  @UiField
  HTML mandateValue;

  @UiField
  Label notesLabel;

  @UiField
  HTML notesValue;

  // disposal actions

  @UiField
  Label disposalActionsLabel;

  @UiField
  HTML disposalActionsValue;

  // retention triggers

  @UiField
  Label retentionTriggersLabel;

  @UiField
  HTML retentionTriggersValue;

  // retention period

  @UiField
  Label retentionPeriodLabel;

  @UiField
  HTML retentionPeriodValue;

  @UiField
  Label stateLabel;

  @UiField
  HTML stateValue;

  @UiField
  FlowPanel buttonsPanel;

  @UiField
  FlowPanel aipListTitle;

  @UiField
  SimplePanel aipsListCard;

  public ShowDisposalSchedule() {
    this.disposalSchedule = new DisposalSchedule();
  }

  public ShowDisposalSchedule(final DisposalSchedule disposalSchedule, final DisposalRules disposalRules) {
    instance = this;
    this.disposalSchedule = disposalSchedule;
    this.disposalRules = disposalRules;

    initWidget(uiBinder.createAndBindUi(this));
    initElements();
    initButtons();
  }

  public void initElements() {
    title.setText(disposalSchedule.getTitle());

    disposalScheduleId.setText(messages.disposalScheduleIdentifier() + ": " + disposalSchedule.getId());

    if (disposalSchedule.getCreatedOn() != null && StringUtils.isNotBlank(disposalSchedule.getCreatedBy())) {
      dateCreated.setText(messages.dateCreated(Humanize.formatDateTime(disposalSchedule.getCreatedOn()),
        disposalSchedule.getCreatedBy()));
    }

    if (disposalSchedule.getUpdatedOn() != null && StringUtils.isNotBlank(disposalSchedule.getUpdatedBy())) {
      dateUpdated.setText(messages.dateUpdated(Humanize.formatDateTime(disposalSchedule.getUpdatedOn()),
        disposalSchedule.getUpdatedBy()));
    }

    descriptionValue.setHTML(disposalSchedule.getDescription());
    descriptionLabel.setVisible(StringUtils.isNotBlank(disposalSchedule.getDescription()));

    mandateValue.setHTML(disposalSchedule.getMandate());
    mandateLabel.setVisible(StringUtils.isNotBlank(disposalSchedule.getMandate()));

    notesValue.setHTML(disposalSchedule.getScopeNotes());
    notesLabel.setVisible(StringUtils.isNotBlank(disposalSchedule.getScopeNotes()));

    disposalActionsValue.setHTML(messages.disposalScheduleAction(disposalSchedule.getActionCode().toString()));
    disposalActionsLabel.setVisible(StringUtils.isNotBlank(disposalSchedule.getActionCode().toString()));

    if (disposalSchedule.getRetentionTriggerElementId() == null) {
      retentionTriggersValue.setHTML("");
      retentionTriggersLabel.setVisible(false);
    } else {
      retentionTriggersValue.setHTML(
        messages.disposalScheduleRetentionTriggerElementId());
      retentionTriggersLabel.setVisible(StringUtils.isNotBlank(disposalSchedule.getRetentionTriggerElementId().toString()));
    }

    if (disposalSchedule.getRetentionPeriodIntervalCode() == null) {
      retentionPeriodValue.setHTML("");
      retentionPeriodLabel.setVisible(false);
    } else if (disposalSchedule.getRetentionPeriodIntervalCode()
      .equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD)) {
      retentionPeriodValue.setHTML(disposalSchedule.getRetentionPeriodIntervalCode().toString());
      retentionPeriodLabel.setVisible(true);
    } else {
      String retentionPeriod = messages.retentionPeriod(disposalSchedule.getRetentionPeriodDuration(),
        disposalSchedule.getRetentionPeriodIntervalCode().toString());
      retentionPeriodValue.setHTML(retentionPeriod);
      retentionPeriodLabel.setVisible(true);
    }

    stateValue.setHTML(HtmlSnippetUtils.getDisposalScheduleStateHtml(disposalSchedule));

    // Records with this schedule

    if (disposalSchedule.getState().equals(DisposalScheduleState.ACTIVE)
      && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_FIND_AIP)) {
      Label aipTitle = new Label();
      aipTitle.addStyleName("h5");
      aipTitle.setText(messages.disposalScheduleListAips());
      aipListTitle.add(aipTitle);

      ListBuilder<IndexedAIP> aipsListBuilder = new ListBuilder<>(() -> new ConfigurableAsyncTableCell<>(),
        new AsyncTableCellOptions<>(IndexedAIP.class, "ShowDisposalSchedule_aips")
          .withFilter(
            new Filter(new SimpleFilterParameter(RodaConstants.AIP_DISPOSAL_SCHEDULE_ID, disposalSchedule.getId())))
          .withSummary(messages.listOfAIPs()).bindOpener());

      SearchWrapper aipsSearchWrapper = new SearchWrapper(false).createListAndSearchPanel(aipsListBuilder);
      aipsListCard.setWidget(aipsSearchWrapper);
      aipsListCard.setVisible(true);
    } else {
      aipsListCard.setVisible(false);
    }
  }

  public void initButtons() {

    if (disposalSchedule.getState().equals(DisposalScheduleState.ACTIVE)) {
      Button editScheduleBtn = new Button();
      editScheduleBtn.addStyleName("btn btn-block btn-edit");
      editScheduleBtn.setText(messages.editButton());
      if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_UPDATE_DISPOSAL_SCHEDULE)) {
        editScheduleBtn.addClickHandler(
          clickEvent -> HistoryUtils.newHistory(EditDisposalSchedule.RESOLVER, disposalSchedule.getId()));
      }
      buttonsPanel.add(editScheduleBtn);

      if (!isScheduleInRule() && disposalSchedule.getApiCounter() == 0
        && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_UPDATE_DISPOSAL_SCHEDULE)) {
        Button deactivateScheduleButton = new Button();
        deactivateScheduleButton.addStyleName("btn btn-block btn-danger btn-ban");
        deactivateScheduleButton.setText(messages.deactivateButton());
        deactivateScheduleButton.addClickHandler(clickEvent -> {
          disposalSchedule.setState(DisposalScheduleState.INACTIVE);
          BrowserServiceImpl.Util.getInstance().updateDisposalSchedule(disposalSchedule,
            new NoAsyncCallback<DisposalSchedule>() {
              @Override
              public void onSuccess(DisposalSchedule disposalSchedule) {
                HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
              }
            });
        });
        buttonsPanel.add(deactivateScheduleButton);
      }
    }

    Button backBtn = new Button();
    backBtn.setText(messages.backButton());
    backBtn.addStyleName("btn btn-block btn-default btn-times-circle");
    backBtn.addClickHandler(clickEvent -> HistoryUtils.newHistory(DisposalPolicy.RESOLVER));
    buttonsPanel.add(backBtn);

  }

  private boolean isScheduleInRule() {
    boolean ret = false;
    for(DisposalRule rule: disposalRules.getObjects()){
      if(rule.getDisposalScheduleId().equals(disposalSchedule.getId())){
        ret = true;
      }
    }
    return ret;
  }

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      BrowserService.Util.getInstance().retrieveDisposalSchedule(historyTokens.get(0),
        new NoAsyncCallback<DisposalSchedule>() {
          @Override
          public void onSuccess(DisposalSchedule disposalSchedule) {
            BrowserService.Util.getInstance().listDisposalRules(new NoAsyncCallback<DisposalRules>() {
              @Override
              public void onSuccess(DisposalRules disposalRules) {
                ShowDisposalSchedule panel = new ShowDisposalSchedule(disposalSchedule, disposalRules);
                callback.onSuccess(panel);
              }
            });
          }
        });
    }
  }

}
