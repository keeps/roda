package org.roda.wui.client.management;

import java.util.List;

import com.google.gwt.event.dom.client.ClickHandler;
import org.roda.core.data.v2.institution.Institution;
import org.roda.core.data.v2.institution.InstitutionStatus;
import org.roda.wui.client.browse.BrowserService;
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
public class EditInstitution extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        BrowserService.Util.getInstance().retrieveInstitution(historyTokens.get(0), new NoAsyncCallback<Institution>() {
          @Override
          public void onSuccess(Institution result) {
            EditInstitution editInstitution = new EditInstitution(result);
            callback.onSuccess(editInstitution);
          }
        });
      }
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
      return "edit_institution";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, EditInstitution> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private Institution institution;

  @UiField
  Button buttonSave;

  @UiField
  Button buttonCancel;

  @UiField
  Button buttonChangeStatus;

  @UiField(provided = true)
  InstitutionDataPanel institutionDataPanel;

  public EditInstitution(final Institution institution) {
    this.institution = institution;
    this.institutionDataPanel = new InstitutionDataPanel(institution, false);
    this.institutionDataPanel.setInstitution(institution);


    initWidget(uiBinder.createAndBindUi(this));
    initStatusButton(institution);
  }

  private void initStatusButton(Institution institution) {
    switch (institution.getStatus()) {
      case ACTIVE:
      case CREATED:
        buttonChangeStatus.setText(messages.institutionStatusButtonDeactivateLabel());
        buttonChangeStatus.addStyleName("btn-default btn-times-circle");
        buttonChangeStatus.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent clickEvent) {
            institution.setStatus(InstitutionStatus.INACTIVE);
            BrowserServiceImpl.Util.getInstance().updateInstitution(institution, new NoAsyncCallback<Institution>() {
              @Override
              public void onSuccess(Institution institution) {
                HistoryUtils.newHistory(InstitutionManagement.RESOLVER);
              }
            });
          }
        });
        break;
      case INACTIVE:
        buttonChangeStatus.setText(messages.institutionStatusButtonActivateLabel());
        buttonChangeStatus.addStyleName("btn-success btn-check");
        buttonChangeStatus.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent clickEvent) {
            institution.setStatus(InstitutionStatus.ACTIVE);
            BrowserServiceImpl.Util.getInstance().updateInstitution(institution, new NoAsyncCallback<Institution>() {
              @Override
              public void onSuccess(Institution institution) {
                HistoryUtils.newHistory(InstitutionManagement.RESOLVER);
              }
            });
          }
        });
        break;
      default:
        buttonChangeStatus.setVisible(false);
    }
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
      BrowserServiceImpl.Util.getInstance().updateInstitution(institution, new NoAsyncCallback<Institution>() {
        @Override
        public void onSuccess(Institution institution) {
          HistoryUtils.newHistory(InstitutionManagement.RESOLVER);
        }
      });
    }
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    BrowserServiceImpl.Util.getInstance().deleteInstitution(institution.getId(), new NoAsyncCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        HistoryUtils.newHistory(InstitutionManagement.RESOLVER, institution.getId());
      }
    });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    HistoryUtils.newHistory(ShowInstitution.RESOLVER, institution.getId());
  }
}
