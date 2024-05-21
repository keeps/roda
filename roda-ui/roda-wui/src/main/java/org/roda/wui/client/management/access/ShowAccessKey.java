/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.access;

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
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.AccessKeyDialogs;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ShowAccessKey extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        Services services = new Services("Get access key", "get");
        services.membersResource(s -> s.getAccessKey(historyTokens.get(0))).whenComplete((accessKey, error) -> {
          if (accessKey != null) {
            ShowAccessKey showAccessKey = new ShowAccessKey(accessKey);
            callback.onSuccess(showAccessKey);
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
      return "show_access_key";
    }
  };
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
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
  Button buttonRegenerate, buttonRevoke, buttonDelete, buttonCancel;
  private AccessKey accessKey;

  public ShowAccessKey(AccessKey accessKey) {
    initWidget(uiBinder.createAndBindUi(this));
    initElements(accessKey);
  }

  public void refresh() {
    reset();
    Services services = new Services("Get access key", "get");
    services.membersResource(s -> s.getAccessKey(accessKey.getId())).whenComplete((accessKey, error) -> {
      if (accessKey != null) {
        initElements(accessKey);
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

  private void initElements(AccessKey accessKey) {
    this.accessKey = accessKey;
    title.setText(accessKey.getName());

    if (accessKey.getCreatedOn() != null && StringUtils.isNotBlank(accessKey.getCreatedBy())) {
      dateCreated
        .setText(messages.dateCreated(Humanize.formatDateTime(accessKey.getCreatedOn()), accessKey.getCreatedBy()));
    }

    if (accessKey.getUpdatedOn() != null && StringUtils.isNotBlank(accessKey.getUpdatedBy())) {
      dateUpdated
        .setText(messages.dateUpdated(Humanize.formatDateTime(accessKey.getUpdatedOn()), accessKey.getUpdatedBy()));
    }

    if (accessKey.getExpirationDate() != null) {
      expirationDateValue.setText(Humanize.formatDateTime(accessKey.getExpirationDate()));
    }

    if (accessKey.getLastUsageDate() != null) {
      lastUsageValue.setText(Humanize.formatDateTime(accessKey.getLastUsageDate()));
    } else {
      lastUsageValue.setText("Never");
    }

    if (accessKey.getStatus() != null) {
      statusValue.setHTML(HtmlSnippetUtils.getAccessKeyStateHtml(accessKey));
    }

    nameValue.setHTML(accessKey.getName());

    initSidebarButtons(accessKey);
  }

  private void initSidebarButtons(AccessKey accessKey) {
    switch (accessKey.getStatus()) {
      case CREATED:
      case ACTIVE:
        enableButtons(buttonRegenerate, buttonRevoke);
        disableButtons(buttonDelete);
        break;
      case EXPIRED:
        enableButtons(buttonRevoke);
        disableButtons(buttonRegenerate, buttonDelete);
        break;
      case REVOKED:
        enableButtons(buttonDelete);
        disableButtons(buttonRegenerate, buttonRevoke);
        break;
      case INACTIVE:
        enableButtons(buttonRevoke, buttonDelete);
        disableButtons(buttonDelete, buttonRegenerate);
        break;
      default:
        enableButtons(buttonRegenerate, buttonRevoke, buttonDelete);
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

  @UiHandler("buttonDelete")
  void buttonDeleteHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.accessKeyLabel(), messages.accessKeyDeleteConfirmationMessage(),
      messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirm) {
          Services services = new Services("Delete access key", "delete");
          services.membersResource(s -> s.deleteAccessKey(accessKey.getId())).whenComplete((accessKey, error) -> {
            if (error == null) {
              cancel();
            }
          });
        }
      });
  }

  @UiHandler("buttonRegenerate")
  void buttonRegenerateHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.accessKeyLabel(), messages.accessKeyRegenerateConfirmationMessage(),
      messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirm) {
          if (confirm) {
            Services services = new Services("Regenerate access key", "regenerate");
            services.membersResource(s -> s.regenerateAccessKey(accessKey)).whenComplete((accessKey, error) -> {
              if (accessKey != null) {
                AccessKeyDialogs.showAccessKeyDialog(messages.accessKeyLabel(), accessKey,
                  new NoAsyncCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                      refresh();
                      Toast.showInfo(messages.accessKeyLabel(), messages.accessKeySuccessfullyRegenerated());
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
    Dialogs.showConfirmDialog(messages.accessKeyLabel(), messages.accessKeyRevokeConfirmationMessage(),
      messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirm) {
          if (confirm) {
            Services services = new Services("Revoke access key", "revoke");
            services.membersResource(s -> s.revokeAccessKey(accessKey)).whenComplete((accessKey, error) -> {
              if (accessKey != null) {
                refresh();
                Toast.showInfo(messages.accessKeyLabel(), messages.accessKeySuccessfullyRevoked());
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

  interface MyUiBinder extends UiBinder<Widget, ShowAccessKey> {
  }

}
