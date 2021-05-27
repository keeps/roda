package org.roda.wui.client.management;

import java.util.List;

import org.roda.core.data.v2.institution.Institution;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.server.browse.BrowserServiceImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ShowInstitution extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {
    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {InstitutionManagement.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(InstitutionManagement.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "show_institution";
    }
  };

  private static ShowInstitution instance = null;
  private Institution institution;

  interface MyUiBinder extends UiBinder<Widget, ShowInstitution> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static ShowInstitution getInstance() {
    if (instance == null) {
      instance = new ShowInstitution(new Institution());
    }
    return instance;
  }

  @UiField
  TitlePanel title;

  @UiField
  Label institutionUUID;

  @UiField
  Label dateCreated;

  @UiField
  Label dateUpdated;

  @UiField
  Label descriptionLabel;

  @UiField
  HTML descriptionValue;

  @UiField
  Label IDLabel;

  @UiField
  HTML IDValue;

  @UiField
  Label lastSyncDateLabel;

  @UiField
  HTML lastSyncDateValue;

  @UiField
  Label accessKeyLabel;

  @UiField
  HTML accessKeyValue;

  @UiField
  Label statusLabel;

  @UiField
  HTML statusValue;

  public ShowInstitution(Institution institution) {
    initWidget(uiBinder.createAndBindUi(this));
    this.institution = institution;

    initElements();
  }

  private void initElements() {
    title.setText(institution.getName());

    descriptionValue.setHTML(institution.getDescription());
    if (institution.getCreatedOn() != null && StringUtils.isNotBlank(institution.getCreatedBy())) {
      dateCreated
        .setText(messages.dateCreated(Humanize.formatDateTime(institution.getCreatedOn()), institution.getCreatedBy()));
    }

    if (institution.getUpdatedOn() != null && StringUtils.isNotBlank(institution.getUpdatedBy())) {
      dateCreated
        .setText(messages.dateUpdated(Humanize.formatDateTime(institution.getUpdatedOn()), institution.getUpdatedBy()));
    }

    IDValue.setHTML(institution.getId());
    if (institution.getLastSyncDate() != null) {
      lastSyncDateValue.setHTML(Humanize.formatDateTime(institution.getLastSyncDate()));
    } else {
      lastSyncDateValue.setHTML(messages.permanentlyRetained());
    }

    accessKeyValue.setHTML(institution.getAccessKey());
    statusValue.setHTML(HtmlSnippetUtils.getInstitutionStateHtml(institution));
  }

  private void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      BrowserService.Util.getInstance().retrieveInstitution(historyTokens.get(0), new NoAsyncCallback<Institution>() {
        @Override
        public void onSuccess(Institution result) {
          ShowInstitution showInstitution = new ShowInstitution(result);
          callback.onSuccess(showInstitution);
        }
      });
    }
  }

  @UiHandler("buttonEdit")
  void buttonApplyHandler(ClickEvent e) {
    HistoryUtils.newHistory(EditInstitution.RESOLVER, institution.getId());
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    BrowserServiceImpl.Util.getInstance().deleteInstitution(institution.getId(), new NoAsyncCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        HistoryUtils.newHistory(InstitutionManagement.RESOLVER);
      }
    });
  }
}
