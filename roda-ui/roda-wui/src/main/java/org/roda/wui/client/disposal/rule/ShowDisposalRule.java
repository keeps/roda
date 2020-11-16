package org.roda.wui.client.disposal.rule;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.disposal.ConditionType;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.DisposalDialogs;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.client.disposal.schedule.ShowDisposalSchedule;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.server.browse.BrowserServiceImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

  private static ShowDisposalRule instance = null;

  interface MyUiBinder extends UiBinder<Widget, ShowDisposalRule> {
  }

  private static ShowDisposalRule.MyUiBinder uiBinder = GWT.create(ShowDisposalRule.MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private DisposalRule disposalRule;

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

  // Conditions

  @UiField
  Label conditionsLabel;

  @UiField
  FlowPanel conditionsPanel;

  // Sidebar

  @UiField
  FlowPanel buttonsPanel;

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

    disposalRuleDescription.setHTML(disposalRule.getDescription());
    disposalRuleDescriptionLabel.setVisible(StringUtils.isNotBlank(disposalRule.getDescription()));

    disposalRuleScheduleName.setHTML(disposalRule.getDisposalScheduleName());
    disposalRuleScheduleLabel.setVisible(StringUtils.isNotBlank(disposalRule.getDisposalScheduleName()));
    disposalRuleScheduleName.addStyleName("btn-link addCursorPointer");
    disposalRuleScheduleName.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER, disposalRule.getDisposalScheduleId());
      }
    });

    disposalRuleType.setHTML(messages.disposalRuleTypeValue(disposalRule.getType().toString()));
    disposalRuleTypeLabel.setVisible(StringUtils.isNotBlank(disposalRule.getType().toString()));

    conditionsLabel.setVisible(true);
    HTML condition = new HTML();
    if (disposalRule.getType().equals(ConditionType.IS_CHILD_OF)) {
      String conditionTxt = messages.disposalRuleTypeValue(disposalRule.getType().toString()) + " "
        + disposalRule.getConditionValue() + " (" + disposalRule.getConditionKey() + ")";
      condition.setHTML(conditionTxt);
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
      condition.setHTML(conditionTxt);
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
      removeRuleBtn.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          Dialogs.showConfirmDialog(messages.removeButton(), messages.confirmDeleteRule(disposalRule.getTitle()),
            messages.dialogNo(), messages.dialogYes(), new NoAsyncCallback<Boolean>() {
              @Override
              public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                  BrowserServiceImpl.Util.getInstance().deleteDisposalRule(disposalRule.getId(),
                    new NoAsyncCallback<Void>() {
                      @Override
                      public void onSuccess(Void unused) {
                        Dialogs.showConfirmDialog(messages.applyRules(), messages.applyRulesAfterAction(),
                          messages.dialogNo(), messages.dialogYes(), new NoAsyncCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                              HistoryUtils.newHistory(DisposalPolicy.RESOLVER);
                              if (aBoolean) {
                                DisposalDialogs.showApplyRules(messages.applyRules(), new NoAsyncCallback<Void>() {
                                  @Override
                                  public void onSuccess(Void unused) {
                                    // TODO
                                  }
                                });
                              }
                            }
                          });
                      }
                    });
                }
              }
            });
        }
      });

      buttonsPanel.add(removeRuleBtn);
    }

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

  public static ShowDisposalRule getInstance() {
    if (instance == null) {
      instance = new ShowDisposalRule();
    }
    return instance;
  }

  public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      BrowserService.Util.getInstance().retrieveDisposalRule(historyTokens.get(0), new NoAsyncCallback<DisposalRule>() {
        @Override
        public void onSuccess(DisposalRule result) {
          ShowDisposalRule panel = new ShowDisposalRule(result);
          callback.onSuccess(panel);
        }
      });
    }
  }
}
