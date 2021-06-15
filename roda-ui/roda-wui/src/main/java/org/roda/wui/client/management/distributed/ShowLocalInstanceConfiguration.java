package org.roda.wui.client.management.distributed;

import java.util.List;

import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import org.roda.core.data.v2.distributedInstance.LocalInstance;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.server.browse.BrowserServiceImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ShowLocalInstanceConfiguration extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      BrowserService.Util.getInstance().retrieveLocalInstance(new NoAsyncCallback<LocalInstance>() {
        @Override
        public void onSuccess(LocalInstance localInstance) {
          if (localInstance != null) {
            ShowLocalInstanceConfiguration showLocalInstanceConfiguration = new ShowLocalInstanceConfiguration(
              localInstance);
            callback.onSuccess(showLocalInstanceConfiguration);
          } else {
            HistoryUtils.newHistory(CreateLocalInstanceConfiguration.RESOLVER);
          }
        }
      });
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {ShowLocalInstanceConfiguration.RESOLVER}, false,
        callback);
    }

    @Override
    public String getHistoryToken() {
      return "show_instance_management";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(LocalInstanceManagement.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  interface MyUiBinder extends UiBinder<Widget, ShowLocalInstanceConfiguration> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private LocalInstance localInstance;

  @UiField
  HTML IDValue;

  @UiField
  HTML centralInstanceURLValue;

  public ShowLocalInstanceConfiguration(LocalInstance localInstance) {
    initWidget(uiBinder.createAndBindUi(this));
    this.localInstance = localInstance;

    initElements(localInstance);
  }

  private void initElements(LocalInstance localInstance) {
    IDValue.setText(localInstance.getId());
    centralInstanceURLValue.setText(localInstance.getCentralInstanceURL());
  }

  @UiHandler("buttonEdit")
  void buttonApplyHandler(ClickEvent e) {
    HistoryUtils.newHistory(EditLocalInstanceConfiguration.RESOLVER);
  }

  @UiHandler("buttonTest")
  void buttonTestHandler(ClickEvent e) {
    BrowserService.Util.getInstance().testLocalInstanceConfiguration(localInstance, new NoAsyncCallback<List<String>>() {
      @Override
      public void onSuccess(List<String> result) {
        if (result.isEmpty()) {
          Toast.showInfo("Test instance", "Success");
        } else {
          Toast.showError("Test instance", "Error: " + result.toString());
        }
      }
    });
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    BrowserServiceImpl.Util.getInstance().deleteLocalInstanceConfiguration(new NoAsyncCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        HistoryUtils.newHistory(LocalInstanceManagement.RESOLVER);
      }
    });
  }
}
