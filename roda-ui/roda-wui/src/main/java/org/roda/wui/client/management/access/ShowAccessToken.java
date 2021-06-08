package org.roda.wui.client.management.access;

import java.util.List;

import org.roda.core.data.v2.AccessToken.AccessToken;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.AccessTokenDialogs;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;
import org.roda.wui.server.browse.BrowserServiceImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ShowAccessToken extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        BrowserService.Util.getInstance().retrieveAccessToken(historyTokens.get(0), new NoAsyncCallback<AccessToken>() {
          @Override
          public void onSuccess(AccessToken result) {
            ShowAccessToken showAccessToken = new ShowAccessToken(result);
            callback.onSuccess(showAccessToken);
          }
        });
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(MemberManagement.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "show_access_tokens";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, ShowAccessToken> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private AccessToken accessToken;

  @UiField
  TitlePanel title;

  @UiField
  Label dateCreated;

  @UiField
  Label dateUpdated;

  @UiField
  HTML nameValue;

  @UiField
  HTML expirationDateValue;

  @UiField
  HTML lastUsageValue;

  @UiField
  HTML statusValue;

  @UiField
  Button buttonEdit, buttonRegenerate, buttonRevoke, buttonDelete, buttonCancel;

  public ShowAccessToken(AccessToken accessToken) {
    initWidget(uiBinder.createAndBindUi(this));
    initElements(accessToken);
  }

  public void refresh() {
    reset();
    BrowserService.Util.getInstance().retrieveAccessToken(accessToken.getId(), new NoAsyncCallback<AccessToken>() {
      @Override
      public void onSuccess(AccessToken accessToken) {
        initElements(accessToken);
      }
    });
  }

  public void reset() {
    dateCreated.setText("");
    dateUpdated.setText("");
    expirationDateValue.setText("");
    lastUsageValue.setText("");
    statusValue.setHTML(SafeHtmlUtils.EMPTY_SAFE_HTML);
    nameValue.setHTML(SafeHtmlUtils.EMPTY_SAFE_HTML);
  }

  private void initElements(AccessToken accessToken) {
    this.accessToken = accessToken;
    title.setText(accessToken.getName());

    if (accessToken.getCreatedOn() != null && StringUtils.isNotBlank(accessToken.getCreatedBy())) {
      dateCreated
        .setText(messages.dateCreated(Humanize.formatDateTime(accessToken.getCreatedOn()), accessToken.getCreatedBy()));
    }

    if (accessToken.getUpdatedOn() != null && StringUtils.isNotBlank(accessToken.getUpdatedBy())) {
      dateUpdated
        .setText(messages.dateUpdated(Humanize.formatDateTime(accessToken.getUpdatedOn()), accessToken.getUpdatedBy()));
    }

    if (accessToken.getExpirationDate() != null) {
      expirationDateValue.setText(Humanize.formatDateTime(accessToken.getExpirationDate()));
    }

    if (accessToken.getLastUsageDate() != null) {
      lastUsageValue.setText(Humanize.formatDateTime(accessToken.getLastUsageDate()));
    } else {
      lastUsageValue.setText("Never");
    }

    if (accessToken.getStatus() != null) {
      statusValue.setHTML(HtmlSnippetUtils.getAccessTokenStateHtml(accessToken));
    }

    nameValue.setHTML(accessToken.getName());

    initSidebarButtons(accessToken);
  }

  private void initSidebarButtons(AccessToken accessToken) {
    switch (accessToken.getStatus()) {
      case CREATED:
      case ACTIVE:
        enableButtons(buttonEdit, buttonRegenerate, buttonRevoke);
        disableButtons(buttonDelete);
        break;
      case EXPIRED:
        enableButtons(buttonRevoke, buttonEdit);
        disableButtons(buttonRegenerate, buttonDelete);
        break;
      case REVOKED:
        enableButtons(buttonDelete);
        disableButtons(buttonEdit, buttonRegenerate, buttonRevoke);
        break;
      case INACTIVE:
        enableButtons(buttonRevoke, buttonDelete);
        disableButtons(buttonDelete, buttonRegenerate, buttonEdit);
        break;
      default:
        enableButtons(buttonEdit, buttonRegenerate, buttonRevoke, buttonDelete);
        break;
    }
  }

  private void disableButtons(Button... buttons) {
    for (Button button : buttons) {
      button.setEnabled(false);
    }
  }

  private void enableButtons(Button... buttons) {
    for (Button button : buttons) {
      button.setEnabled(true);
    }
  }

  @UiHandler("buttonEdit")
  void buttonApplyHandler(ClickEvent e) {
    HistoryUtils.newHistory(EditAccessToken.RESOLVER, accessToken.getId());
  }

  @UiHandler("buttonDelete")
  void buttonDeleteHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.accessTokenLabel(), messages.accessTokenDeleteConfirmationMessage(),
      messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirm) {
          BrowserServiceImpl.Util.getInstance().deleteAccessToken(accessToken.getId(), new NoAsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
              cancel();
            }
          });
        }
      });
  }

  @UiHandler("buttonRegenerate")
  void buttonRegenerateHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.accessTokenLabel(), messages.accessTokenRegenerateConfirmationMessage(),
      messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirm) {
          if (confirm) {
            BrowserService.Util.getInstance().regenerateAccessToken(accessToken, new NoAsyncCallback<AccessToken>() {
              @Override
              public void onSuccess(AccessToken result) {
                AccessTokenDialogs.showAccessTokenDialog(messages.accessTokenLabel(), accessToken,
                  new NoAsyncCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                      refresh();
                      Toast.showInfo(messages.accessTokenLabel(), messages.accessTokenSuccessfullyRegenerated());
                    }
                  });
              }
            });
          }
        }
      });
  }

  @UiHandler("buttonRevoke")
  void buttonRevokeHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.accessTokenLabel(), messages.accessTokenRevokeConfirmationMessage(),
      messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirm) {
          if (confirm) {
            BrowserService.Util.getInstance().revokeAccessToken(accessToken, new NoAsyncCallback<AccessToken>() {
              @Override
              public void onSuccess(AccessToken result) {
                refresh();
                Toast.showInfo(messages.accessTokenLabel(), messages.accessTokenSuccessfullyRevoked());
              }
            });
          }
        }
      });
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    History.back();
  }

}
