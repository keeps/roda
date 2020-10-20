package org.roda.wui.client.disposal.schedule;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.disposal.DisposalPolicy;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
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

  /*@UiField
  FlowPanel removeSchedulePanel;

  @UiField
  FlowPanel backPanel;*/

  public ShowDisposalSchedule() {
    this.disposalSchedule = new DisposalSchedule();
  }

  public ShowDisposalSchedule(final DisposalSchedule disposalSchedule) {
    instance = this;
    this.disposalSchedule = disposalSchedule;

    /*
     * if (PermissionClientUtils.hasPermissions(RodaConstants.
     * PERMISSION_METHOD_UPDATE_DISPOSAL_HOLD)) { Button editBtn = new Button();
     * editBtn.addStyleName("btn"); editBtn.setText(messages.editButton());
     * editBtn.addClickHandler(new ClickHandler() {
     *
     * @Override public void onClick(ClickEvent event) {
     * HistoryUtils.newHistory(CreateDisposalSchedule.RESOLVER); } });
     * editHold.add(editBtn); }
     */

    /*
     * if (PermissionClientUtils.hasPermissions(RodaConstants.
     * PERMISSION_METHOD_DELETE_DISPOSAL_HOLD)) { Button liftBtn = new Button();
     * liftBtn.addStyleName("btn btn-danger");
     * liftBtn.setText(messages.liftButton()); liftBtn.addClickHandler(new
     * ClickHandler() {
     *
     * @Override public void onClick(ClickEvent event) {
     * HistoryUtils.newHistory(CreateDisposalSchedule.RESOLVER); } });
     * liftHold.add(liftBtn); }
     */
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

    disposalActionsValue.setHTML(disposalSchedule.getActionCode().toString());
    disposalActionsLabel.setVisible(StringUtils.isNotBlank(disposalSchedule.getActionCode().toString()));

    retentionTriggersValue.setHTML(disposalSchedule.getRetentionTriggerCode().toString());
    retentionTriggersLabel.setVisible(StringUtils.isNotBlank(disposalSchedule.getRetentionTriggerCode().toString()));


    String retentionPeriod = disposalSchedule.getRetentionPeriodDuration().toString() + " " + disposalSchedule.getRetentionPeriodIntervalCode().toString();
    retentionPeriodValue.setHTML(retentionPeriod);
    retentionPeriodLabel
      .setVisible(StringUtils.isNotBlank(retentionPeriod));

    stateValue.setHTML(HtmlSnippetUtils.getDisposalScheduleStateHtml(disposalSchedule));
  }

  public void initButtons(){

    Button editScheduleBtn = new Button();
    editScheduleBtn.addStyleName("btn btn-block btn-edit");
    editScheduleBtn.setText(messages.editButton());
    buttonsPanel.add(editScheduleBtn);


    Button removeScheduleBtn = new Button();
    removeScheduleBtn.addStyleName("btn btn-block btn-danger btn-ban");
    removeScheduleBtn.setText(messages.discontinueButton());
    buttonsPanel.add(removeScheduleBtn);

    Button backBtn = new Button();
    backBtn.setText(messages.backButton());
    backBtn.addStyleName("btn btn-block btn-default btn-times-circle");
    backBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
      }
    });
    buttonsPanel.add(backBtn);

  }

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      BrowserService.Util.getInstance().retrieveDisposalSchedule(historyTokens.get(0),
        new AsyncCallback<DisposalSchedule>() {
          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(DisposalSchedule result) {
            ShowDisposalSchedule panel = new ShowDisposalSchedule(result);
            callback.onSuccess(panel);
          }
        });
    }
  }

}
