/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.schedule;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.core.data.v2.disposal.rule.DisposalRules;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.DisposalScheduleState;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodIntervalCode;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.DisposalPolicySummaryPanel;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.DisposalScheduleActions;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ConfigurableAsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.services.DisposalRuleRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static ShowDisposalSchedule instance = null;
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
  private static ShowDisposalSchedule.MyUiBinder uiBinder = GWT.create(ShowDisposalSchedule.MyUiBinder.class);
  @UiField
  TitlePanel title;
  @UiField
  DisposalPolicySummaryPanel usedInRulePanel;
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
  @UiField
  Label disposalActionsLabel;
  @UiField
  HTML disposalActionsValue;
  @UiField
  Label retentionTriggersLabel;
  @UiField
  HTML retentionTriggersValue;

  // disposal actions
  @UiField
  Label retentionPeriodLabel;
  @UiField
  HTML retentionPeriodValue;

  // retention triggers
  @UiField
  Label stateLabel;
  @UiField
  HTML stateValue;

  // retention period
  @UiField
  FlowPanel buttonsPanel;
  @UiField
  FlowPanel aipListTitle;
  @UiField
  SimplePanel aipsListCard;
  private DisposalSchedule disposalSchedule;
  private DisposalRules disposalRules;

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

  public static ShowDisposalSchedule getInstance() {
    if (instance == null) {
      instance = new ShowDisposalSchedule();
    }
    return instance;
  }

  public void initElements() {
    title.setText(disposalSchedule.getTitle());

    if (isScheduleInRule()) {
      usedInRulePanel.setIcon("fas fa-info-circle");
      usedInRulePanel.setText(messages.disposalScheduleUsedInRule());
    } else {
      usedInRulePanel.setVisible(false);
    }

    disposalScheduleId.setText(messages.disposalScheduleIdentifier() + ": " + disposalSchedule.getId());

    if (disposalSchedule.getCreatedOn() != null && StringUtils.isNotBlank(disposalSchedule.getCreatedBy())) {
      dateCreated.setText(messages.dateCreated(Humanize.formatDateTime(disposalSchedule.getCreatedOn()),
        disposalSchedule.getCreatedBy()));
    }

    if (disposalSchedule.getUpdatedOn() != null && StringUtils.isNotBlank(disposalSchedule.getUpdatedBy())) {
      dateUpdated.setText(messages.dateUpdated(Humanize.formatDateTime(disposalSchedule.getUpdatedOn()),
        disposalSchedule.getUpdatedBy()));
    }

    descriptionValue.setHTML(SafeHtmlUtils.fromString(disposalSchedule.getDescription()));
    descriptionLabel.setVisible(StringUtils.isNotBlank(disposalSchedule.getDescription()));

    mandateValue.setHTML(SafeHtmlUtils.fromString(disposalSchedule.getMandate()));
    mandateLabel.setVisible(StringUtils.isNotBlank(disposalSchedule.getMandate()));

    notesValue.setHTML(SafeHtmlUtils.fromString(disposalSchedule.getScopeNotes()));
    notesLabel.setVisible(StringUtils.isNotBlank(disposalSchedule.getScopeNotes()));

    disposalActionsValue.setHTML(messages.disposalScheduleAction(disposalSchedule.getActionCode().toString()));
    disposalActionsLabel.setVisible(StringUtils.isNotBlank(disposalSchedule.getActionCode().toString()));

    retentionTriggersValue.setHTML(
      DisposalScheduleUtils.getI18nRetentionTriggerIdentifier(disposalSchedule.getRetentionTriggerElementId()));
    retentionTriggersLabel.setVisible(StringUtils.isNotBlank(disposalSchedule.getRetentionTriggerElementId()));

    if (disposalSchedule.getRetentionPeriodIntervalCode() == null) {
      retentionPeriodValue.setHTML("");
      retentionPeriodLabel.setVisible(false);
    } else if (disposalSchedule.getRetentionPeriodIntervalCode()
      .equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD)) {
      String retentionPeriod = messages.retentionPeriod(0,
        disposalSchedule.getRetentionPeriodIntervalCode().toString());
      retentionPeriodValue.setHTML(retentionPeriod);
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
            new Filter(new SimpleFilterParameter(RodaConstants.AIP_DISPOSAL_SCHEDULE_ID, disposalSchedule.getId()),
              new SimpleFilterParameter(RodaConstants.AIP_STATE, AIPState.ACTIVE.name())))
          .withActionable(DisposalScheduleActions.get(disposalSchedule.getId())).withSummary(messages.listOfAIPs())
          .bindOpener());

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

      if (!isScheduleInRule() && disposalSchedule.getFirstTimeUsed() != null
        && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_DELETE_DISPOSAL_SCHEDULE)) {
        // Change the state to inactive
        Button deactivateScheduleButton = new Button();
        deactivateScheduleButton.addStyleName("btn btn-block btn-danger btn-ban");
        deactivateScheduleButton.setText(messages.deactivateButton());
        deactivateScheduleButton.addClickHandler(clickEvent -> {
          disposalSchedule.setState(DisposalScheduleState.INACTIVE);
          Services services = new Services("Update disposal schedule", "update");
          services.disposalScheduleResource(s -> s.updateDisposalSchedule(disposalSchedule))
            .whenComplete((disposalSchedule1, throwable) -> {
              if (throwable != null) {
                AsyncCallbackUtils.defaultRestErrorTreatment(throwable);
              } else {
                HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
              }
            });
        });
        buttonsPanel.add(deactivateScheduleButton);
      }

      if (!isScheduleInRule() && disposalSchedule.getFirstTimeUsed() == null
        && PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_DELETE_DISPOSAL_SCHEDULE)) {
        // Delete the disposal schedule
        Button deleteDisposalSchedule = new Button();
        deleteDisposalSchedule.addStyleName("btn btn-block btn-danger btn-delete");
        deleteDisposalSchedule.setText(messages.removeButton());
        deleteDisposalSchedule.addClickHandler(clickEvent -> {
          Services services = new Services("Delete disposal schedule", "delete");
          services.disposalScheduleResource(s -> s.deleteDisposalSchedule(disposalSchedule.getId()))
            .whenComplete((unused, throwable) -> {
              if (throwable != null) {
                AsyncCallbackUtils.defaultRestErrorTreatment(throwable);
              } else {
                Toast.showInfo(messages.disposalSchedulesTitle(),
                  messages.deleteDisposalSchedule(disposalSchedule.getTitle()));
                HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
              }
            });
        });
        buttonsPanel.add(deleteDisposalSchedule);
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
    for (DisposalRule rule : disposalRules.getObjects()) {
      if (rule.getDisposalScheduleId().equals(disposalSchedule.getId())) {
        ret = true;
        break;
      }
    }
    return ret;
  }

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      Services services = new Services("Retrieve disposal schedule", "get");
      services.disposalScheduleResource(s -> s.retrieveDisposalSchedule(historyTokens.get(0)))
        .thenCompose(schedule -> services.disposalRuleResource(DisposalRuleRestService::listDisposalRules)
          .whenComplete((rules, throwable) -> {
            if (throwable != null) {
              AsyncCallbackUtils.defaultFailureTreatment(throwable);
            } else {
              ShowDisposalSchedule panel = new ShowDisposalSchedule(schedule, rules);
              callback.onSuccess(panel);
            }
          }));
    }
  }

  interface MyUiBinder extends UiBinder<Widget, ShowDisposalSchedule> {
  }

}
