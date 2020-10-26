package org.roda.wui.client.disposal.hold;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
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
  }

  public void initButtons(){
    Button editHoldBtn = new Button();
    editHoldBtn.addStyleName("btn btn-block btn-edit");
    editHoldBtn.setText(messages.editButton());
    buttonsPanel.add(editHoldBtn);


    Button liftHoldBtn = new Button();
    liftHoldBtn.addStyleName("btn btn-block btn-danger btn-ban");
    liftHoldBtn.setText(messages.liftButton());
    buttonsPanel.add(liftHoldBtn);

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

  public static ShowDisposalHold getInstance() {
    if (instance == null) {
      instance = new ShowDisposalHold();
    }
    return instance;
  }

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {

      BrowserService.Util.getInstance().retrieveDisposalHold(historyTokens.get(0), new AsyncCallback<DisposalHold>() {
        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(DisposalHold result) {
          ShowDisposalHold panel = new ShowDisposalHold(result);
          callback.onSuccess(panel);
        }
      });
    }
  }
}
