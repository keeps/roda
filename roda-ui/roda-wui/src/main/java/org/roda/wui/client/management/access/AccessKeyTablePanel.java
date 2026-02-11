/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.access;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeyStatus;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.core.data.v2.accessKey.CreateAccessKeyRequest;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.dialogs.AccessKeyDialogs;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.utils.ActionMenuCell;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.roda.wui.common.client.widgets.Toast;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class AccessKeyTablePanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  FlowPanel contentFlowPanel;

  private BasicTablePanel<AccessKey> table;
  private String username;

  public AccessKeyTablePanel(String username) {
    initWidget(uiBinder.createAndBindUi(this));
    this.username = username;

    // Call the new unified refresh method
    refresh();
  }

  public void refresh() {
    Services services = new Services("Get user access keys", "get");
    services.membersResource(s -> s.getAccessKeysByUser(username)).whenComplete((accessKeys, error) -> {
      if (accessKeys != null) {
        // Clear the panel and rebuild the table/empty state from scratch
        contentFlowPanel.clear();
        contentFlowPanel.add(createTable(accessKeys));
      } else if (error != null) {
        Toast.showError(error.getMessage());
      }
    });
  }

  public ScrollPanel createTable(AccessKeys accessKeys) {
    ScrollPanel scrollPanel = new ScrollPanel();

    if (accessKeys.getObjects().isEmpty()) {
      String someOfAObject = messages.someOfAObject(accessKeys.getClass().getName());
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(someOfAObject)));
      label.addStyleName("basicTableEmpty");
      scrollPanel.add(label);
    } else {
      FlowPanel accessKeyPanel = new FlowPanel();
      table = getBasicTableForAccessKey(accessKeys);
      table.removeSelectionModel();

      accessKeyPanel.add(table);
      scrollPanel.add(accessKeyPanel);
    }

    return scrollPanel;
  }

  private void showActionsMenu(AccessKey key, int left, int top) {
    // 1. Create the Popup
    PopupPanel popup = new PopupPanel(true); // true = auto-hide when clicking away

    // 2. Create your FlowPanel and add your action items
    FlowPanel menuPanel = new FlowPanel();
    menuPanel.addStyleName("groupedActionableDropdown");

    Button revokeBtn = new Button(messages.accessKeyRevokeButton());
    revokeBtn.addStyleName("actionable-button actionable-button-updated actionable-button-label btn-edit");
    revokeBtn.addClickHandler(e -> {
      popup.hide();
      Dialogs.showConfirmDialog(messages.accessKeyLabel(), messages.accessKeyRevokeConfirmationMessage(),
        messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean confirm) {
            if (confirm) {
              Services services = new Services("Revoke access key", "revoke");
              services.membersResource(s -> s.revokeAccessKey(key.getId())).whenComplete((response, error) -> {
                if (response != null) {
                  Toast.showInfo(messages.accessKeyLabel(), messages.accessKeySuccessfullyRevoked());
                  refresh();
                } else {
                  Toast.showError(error.getMessage());
                }
              });
            }
          }
        });
    });

    Button regenBtn = new Button(messages.accessKeyRegenerateButton());
    regenBtn.addStyleName("actionable-button actionable-button-updated actionable-button-label btn-edit");
    regenBtn.addClickHandler(e -> {
      popup.hide();
      Dialogs.showConfirmDialog(messages.accessKeyLabel(), messages.accessKeyRegenerateConfirmationMessage(),
        messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean confirm) {
            if (confirm) {
              AccessKeyDialogs.createAccessKeyDialog(messages.regenerateAccessKeyTitle(), key.getName(), false,
                new AsyncCallback<CreateAccessKeyRequest>() {
                  @Override
                  public void onFailure(Throwable caught) {
                    // do nothing
                  }

                  @Override
                  public void onSuccess(CreateAccessKeyRequest request) {
                    Services services = new Services("Regenerate access key", "regenerate");
                    CreateAccessKeyRequest regenerateAccessKeyRequest = new CreateAccessKeyRequest();
                    regenerateAccessKeyRequest.setExpirationDate(request.getExpirationDate());
                    services.membersResource(s -> s.regenerateAccessKey(key.getId(), regenerateAccessKeyRequest))
                      .whenComplete((response, error) -> {
                        if (response != null) {
                          AccessKeyDialogs.showAccessKeyDialog(messages.accessKeyLabel(), response,
                            new NoAsyncCallback<Boolean>() {
                              @Override
                              public void onSuccess(Boolean result) {
                                Toast.showInfo(messages.accessKeyLabel(), messages.accessKeySuccessfullyRegenerated());
                                refresh();
                              }
                            });
                        } else {
                          Toast.showError(error.getMessage());
                        }
                      });
                  }
                });
            }
          }
        });
    });

    Button deleteKeyBtn = new Button(messages.accessKeyDeleteButton());
    deleteKeyBtn.addStyleName("actionable-button actionable-button-updated actionable-button-label btn-edit");
    deleteKeyBtn.addClickHandler(e -> {
      popup.hide();
      Dialogs.showConfirmDialog(messages.accessKeyLabel(), messages.accessKeyDeleteConfirmationMessage(),
        messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean confirm) {
            if (confirm) {
              Services services = new Services("Delete access key", "delete");
              services.membersResource(s -> s.deleteAccessKey(key.getId())).whenComplete((accessKey, error) -> {
                if (error != null) {
                  Toast.showError(error.getMessage());
                } else {
                  Toast.showInfo(messages.accessKeyLabel(), messages.accessKeySuccessfullyDeleted());
                  refresh();
                }
              });
            }
          }
        });
    });

    if (showRevokeAccessKey(key)) {
      menuPanel.add(revokeBtn);
    }

    if (showRegenerateAccessKey(key)) {
      menuPanel.add(regenBtn);
    }

    if (showDeleteAccessKey(key)) {
      menuPanel.add(deleteKeyBtn);
    }

    // 3. Show the popup at the calculated coordinates
    popup.setWidget(menuPanel);
    popup.setPopupPosition(left, top);
    popup.show();
  }

  private TextColumn<AccessKey> getLastUsageDateColumn() {
    return new TextColumn<AccessKey>() {
      @Override
      public String getValue(AccessKey accessKey) {
        return accessKey.getLastUsageDate() != null ? Humanize.formatDate(accessKey.getLastUsageDate())
          : messages.accessKeyNeverUsedLabel();
      }
    };
  }

  private TextColumn<AccessKey> getNameColumn() {
    return new TextColumn<AccessKey>() {
      @Override
      public String getValue(AccessKey accessKey) {
        return accessKey.getName();
      }
    };
  }

  private TextColumn<AccessKey> getExpirationDateColumn() {
    return new TextColumn<AccessKey>() {
      @Override
      public String getValue(AccessKey accessKey) {
        return accessKey.getExpirationDate() != null ? Humanize.formatDate(accessKey.getExpirationDate())
          : messages.accessKeyNotFoundLabel();
      }
    };
  }

  private Column<AccessKey, SafeHtml> getStatusColumn() {
    return new Column<AccessKey, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(AccessKey accessKey) {
        return HtmlSnippetUtils.getAccessKeyStateHtml(accessKey);
      }
    };
  }

  private Column<AccessKey, AccessKey> getActionsColumn() {
    ActionMenuCell<AccessKey> actionCell = new ActionMenuCell<AccessKey>(this::showActionsMenu);

    return new Column<AccessKey, AccessKey>(actionCell) {
      @Override
      public AccessKey getValue(AccessKey object) {
        return object;
      }
    };
  }

  private boolean showActionsColumn() {
    return PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_REGENERATE_ACCESS_TOKEN,
      RodaConstants.PERMISSION_METHOD_DELETE_ACCESS_TOKEN, RodaConstants.PERMISSION_METHOD_REVOKE_ACCESS_TOKEN);
  }

  private BasicTablePanel<AccessKey> getBasicTableForAccessKey(AccessKeys accessKeys) {
    if (accessKeys.getObjects().isEmpty()) {
      return new BasicTablePanel<>(messages.noItemsToDisplay(messages.distributedInstancesLabel()));
    } else {
      return new BasicTablePanel<>(accessKeys.getObjects().iterator(),
        new BasicTablePanel.ColumnInfo<AccessKey>(messages.accessKeyNameLabel(), 15, getNameColumn()),
        new BasicTablePanel.ColumnInfo<AccessKey>(messages.accessKeyLastUsageDateLabel(), 15, getLastUsageDateColumn()),
        new BasicTablePanel.ColumnInfo<AccessKey>(messages.accessKeyExpirationDateLabel(), 15,
          getExpirationDateColumn()),
        new BasicTablePanel.ColumnInfo<AccessKey>(messages.accessKeyStatusLabel(), 15, getStatusColumn()),
        new BasicTablePanel.ColumnInfo<AccessKey>(messages.actions(), !showActionsColumn(), 15, getActionsColumn()));
    }
  }

  private boolean showRevokeAccessKey(AccessKey accessKey) {
    switch (accessKey.getStatus()) {
      case CREATED:
      case ACTIVE:
      case INACTIVE:
        return true;
      default:
        return false;
    }
  }

  private boolean showRegenerateAccessKey(AccessKey accessKey) {
    switch (accessKey.getStatus()) {
      case CREATED:
      case ACTIVE:
        return true;
      default:
        return false;
    }
  }

  private boolean showDeleteAccessKey(AccessKey accessKey) {
    return AccessKeyStatus.REVOKED.equals(accessKey.getStatus())
      || AccessKeyStatus.EXPIRED.equals(accessKey.getStatus());
  }

  interface MyUiBinder extends UiBinder<Widget, AccessKeyTablePanel> {
  }
}
