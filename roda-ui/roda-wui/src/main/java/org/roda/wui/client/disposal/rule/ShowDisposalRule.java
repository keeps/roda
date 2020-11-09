package org.roda.wui.client.disposal.rule;

import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.UListElement;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.ibm.icu.text.LocaleDisplayNames;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.DisposalHoldAlreadyExistsException;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.ip.disposal.DisposalRule;
import org.roda.core.data.v2.ip.disposal.DisposalRuleType;
import org.roda.wui.client.browse.BrowseTop;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.DisposalPolicy;
import org.roda.wui.client.disposal.schedule.ShowDisposalSchedule;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

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
  FlowPanel disposalRuleType;

  // Metadata Values (in case of type == METADATA_FIELD)

  @UiField
  FlowPanel metadataValuesPanel;

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

    if (disposalRule.getType().equals(DisposalRuleType.IS_CHILD_OF)) {
      disposalRuleTypeLabel.setVisible(StringUtils.isNotBlank(disposalRule.getType().toString()));
      HTML type = new HTML();
      String ruleType = messages.disposalRuleTypeValue(disposalRule.getType().toString()) + " "
        + disposalRule.getIsChildOf();
      type.setHTML(ruleType);
      type.addStyleName("btn-link addCursorPointer");
      type.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          HistoryUtils.newHistory(BrowseTop.RESOLVER, disposalRule.getIsChildOf());
        }
      });
      disposalRuleType.insert(type, 0);
      metadataValuesPanel.setVisible(false);
    } else if (disposalRule.getType().equals(DisposalRuleType.METADATA_FIELD)) {
      disposalRuleTypeLabel.setVisible(StringUtils.isNotBlank(disposalRule.getType().toString()));
      HTML typeValue = new HTML();
      typeValue.setText(messages.disposalRuleTypeValue(disposalRule.getType().toString()));
      disposalRuleType.add(typeValue);


      Label metadataLabel = new Label();
      metadataLabel.setText(messages.disposalRuleMetadataField());
      metadataLabel.addStyleName("label");
      metadataValuesPanel.add(metadataLabel);

      FlowPanel metadataPanel = new FlowPanel();
      metadataPanel.addStyleName("value");
      String list = "<ul>";
      for (Map.Entry<String, FilterParameter> entry : disposalRule.getMetadataFields().entrySet()) {
        String parameterValue = getParameterValue(entry.getValue());
        String text = entry.getKey() + " is " + parameterValue;
        list += "<li>" + text + "</li>";
      }
      list += "</ul>";
      HTMLPanel listPanel = new HTMLPanel(list);
      metadataPanel.add(listPanel);
      metadataValuesPanel.add(metadataPanel);
    }
  }

  private String getParameterValue(FilterParameter value) {
    String ret = "";

    if(value instanceof BasicSearchFilterParameter){
      BasicSearchFilterParameter basicSearchFilterParameter = (BasicSearchFilterParameter) value;
      ret = basicSearchFilterParameter.getValue();
    }

    return ret;
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
          /*
           * BrowserServiceImpl.Util.getInstance().updateDisposalHold(disposalHold, new
           * AsyncCallback<DisposalHold>() {
           *
           * @Override public void onFailure(Throwable caught) { errorMessage(caught); }
           *
           * @Override public void onSuccess(DisposalHold disposalHold) {
           * HistoryUtils.newHistory(DisposalPolicy.RESOLVER); } });
           */
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

  private void errorMessage(Throwable caught) {
    if (caught instanceof DisposalHoldAlreadyExistsException) {
      Toast.showError(messages.createDisposalRuleAlreadyExists(disposalRule.getTitle()));
    } else {
      Toast.showError(messages.createDisposalRuleFailure(caught.getMessage()));
    }
  }

  public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      BrowserService.Util.getInstance().retrieveDisposalRule(historyTokens.get(0), new AsyncCallback<DisposalRule>() {
        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(DisposalRule result) {
          ShowDisposalRule panel = new ShowDisposalRule(result);
          callback.onSuccess(panel);
        }
      });
    }
  }
}
