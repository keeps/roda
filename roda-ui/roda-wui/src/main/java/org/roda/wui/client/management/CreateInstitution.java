package org.roda.wui.client.management;

import java.util.List;

import org.roda.core.data.v2.institution.Institution;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.server.browse.BrowserServiceImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CreateInstitution extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      CreateInstitution createInstitution = new CreateInstitution();
      callback.onSuccess(createInstitution);
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
      return "create_institution";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, CreateInstitution> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Institution institution;

  @UiField
  Button buttonSave;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  InstitutionDataPanel institutionDataPanel;

  public CreateInstitution() {
    this.institution = new Institution();
    this.institutionDataPanel = new InstitutionDataPanel(institution, false);
    this.institutionDataPanel.setInstitution(institution);

    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  @UiHandler("buttonSave")
  void buttonApplyHandler(ClickEvent e) {
    if (institutionDataPanel.isValid()) {
      institution = institutionDataPanel.getInstitution();
      BrowserServiceImpl.Util.getInstance().createInstitution(institution, new NoAsyncCallback<Institution>() {
        @Override
        public void onSuccess(Institution institution) {
          HistoryUtils.newHistory(InstitutionManagement.RESOLVER);
        }
      });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(InstitutionManagement.RESOLVER);
  }
}
