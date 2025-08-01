/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.rule;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.rule.ConditionType;
import org.roda.core.data.v2.disposal.rule.DisposalRule;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.DisposalRuleActions;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.disposal.schedule.ShowDisposalSchedule;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class ShowDisposalRule extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static ShowDisposalRule instance = null;
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
      return "disposal_rule";
    }
  };
  private static ShowDisposalRule.MyUiBinder uiBinder = GWT.create(ShowDisposalRule.MyUiBinder.class);
  @UiField
  Label disposalRuleId;
  @UiField
  Label dateCreated, dateUpdated;
  @UiField
  TitlePanel title;
  @UiField
  Label disposalRuleDescriptionLabel;
  @UiField
  HTML disposalRuleDescription;
  @UiField
  Label disposalRuleScheduleLabel;
  @UiField
  HTML disposalRuleScheduleName;
  @UiField
  Label disposalRuleTypeLabel;
  @UiField
  HTML disposalRuleType;
  @UiField
  Label conditionsLabel;
  @UiField
  FlowPanel conditionsPanel;

  // Conditions
  @UiField
  FlowPanel buttonsPanel;
  private DisposalRule disposalRule;

  // Sidebar

  public ShowDisposalRule() {
    this.disposalRule = new DisposalRule();
  }

  public ShowDisposalRule(final DisposalRule disposalRule) {
    instance = this;
    this.disposalRule = disposalRule;

    initWidget(uiBinder.createAndBindUi(this));
    initElements();
    initButtons();
  }

  public static ShowDisposalRule getInstance() {
    if (instance == null) {
      instance = new ShowDisposalRule();
    }
    return instance;
  }

  public void initElements() {
    title.setText(disposalRule.getTitle());

    disposalRuleId.setText(messages.disposalRuleIdentifier() + ": " + disposalRule.getId());

    if (disposalRule.getCreatedOn() != null && StringUtils.isNotBlank(disposalRule.getCreatedBy())) {
      dateCreated.setText(
        messages.dateCreated(Humanize.formatDateTime(disposalRule.getCreatedOn()), disposalRule.getCreatedBy()));
    }

    if (disposalRule.getUpdatedOn() != null && StringUtils.isNotBlank(disposalRule.getUpdatedBy())) {
      dateUpdated.setText(
        messages.dateUpdated(Humanize.formatDateTime(disposalRule.getUpdatedOn()), disposalRule.getUpdatedBy()));
    }

    disposalRuleDescription.setHTML(SafeHtmlUtils.fromString(disposalRule.getDescription()));
    disposalRuleDescriptionLabel.setVisible(StringUtils.isNotBlank(disposalRule.getDescription()));

    disposalRuleScheduleName.setHTML(SafeHtmlUtils.fromString(disposalRule.getDisposalScheduleName()));
    disposalRuleScheduleLabel.setVisible(StringUtils.isNotBlank(disposalRule.getDisposalScheduleName()));
    disposalRuleScheduleName.addStyleName("btn-link addCursorPointer");
    disposalRuleScheduleName.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER, disposalRule.getDisposalScheduleId());
      }
    });

    disposalRuleType
      .setHTML(SafeHtmlUtils.fromString(messages.disposalRuleTypeValue(disposalRule.getType().toString())));
    disposalRuleTypeLabel.setVisible(StringUtils.isNotBlank(disposalRule.getType().toString()));

    conditionsLabel.setVisible(true);
    HTML condition = new HTML();
    if (disposalRule.getType().equals(ConditionType.IS_CHILD_OF)) {
      String conditionTxt = messages.disposalRuleTypeValue(disposalRule.getType().toString()) + " "
        + disposalRule.getConditionValue() + " (" + disposalRule.getConditionKey() + ")";
      condition.setHTML(SafeHtmlUtils.fromString(conditionTxt));
      condition.addStyleName("btn-link addCursorPointer");
      condition.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          HistoryUtils.newHistory(BrowseTop.RESOLVER, disposalRule.getConditionKey());
        }
      });
      conditionsPanel.add(condition);
    } else if (disposalRule.getType().equals(ConditionType.METADATA_FIELD)) {
      String conditionTxt = disposalRule.getConditionKey() + " " + messages.disposalRuleConditionOperator() + " "
        + disposalRule.getConditionValue();
      condition.setHTML(SafeHtmlUtils.fromString(conditionTxt));
      conditionsPanel.add(condition);
    }
  }

  public void initButtons() {

    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_UPDATE_DISPOSAL_RULE)) {
      Button editRuleBtn = new Button();
      editRuleBtn.addStyleName("btn btn-block btn-edit");
      editRuleBtn.setText(messages.editButton());
      editRuleBtn.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          HistoryUtils.newHistory(EditDisposalRule.RESOLVER, disposalRule.getId());
        }
      });

      buttonsPanel.add(editRuleBtn);

      Button removeRuleBtn = new Button();
      removeRuleBtn.addStyleName("btn btn-block btn-danger btn-ban");
      removeRuleBtn.setText(messages.removeButton());
      removeRuleBtn.addClickHandler(clickEvent -> Dialogs.showConfirmDialog(messages.deleteDisposalRuleDialogTitle(),
        messages.deleteDisposalRuleDialogMessage(disposalRule.getTitle()), messages.dialogNo(), messages.dialogYes(),
        new NoAsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean confirm) {
            if (confirm) {
              Services services = new Services("Delete disposal rule", "deletion");
              services.disposalRuleResource(s -> s.deleteDisposalRule(disposalRule.getId()))
                .whenComplete((unused, throwable) -> {
                  if (throwable != null) {
                    AsyncCallbackUtils.defaultFailureTreatment(throwable);
                  } else {
                    Toast.showInfo(messages.deleteDisposalRuleSuccessTitle(),
                      messages.deleteDisposalRuleSuccessMessage(disposalRule.getTitle()));
                    DisposalRuleActions.applyDisposalRulesAction();
                  }
                });
            }
          }
        }));

      buttonsPanel.add(removeRuleBtn);
    }

    Button backBtn = new Button();
    backBtn.setText(messages.backButton());
    backBtn.addStyleName("btn btn-block btn-default btn-times-circle");
    backBtn.addClickHandler(event -> HistoryUtils.newHistory(DisposalPolicy.RESOLVER));
    buttonsPanel.add(backBtn);
  }

  public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      Services services = new Services("Retrieve disposal rule", "get");
      services.disposalRuleResource(s -> s.retrieveDisposalRule(historyTokens.get(0)))
        .whenComplete((result, throwable) -> {
          if (throwable != null) {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          } else {
            ShowDisposalRule panel = new ShowDisposalRule(result);
            callback.onSuccess(panel);
          }
        });
    }
  }

  interface MyUiBinder extends UiBinder<Widget, ShowDisposalRule> {
  }
}
