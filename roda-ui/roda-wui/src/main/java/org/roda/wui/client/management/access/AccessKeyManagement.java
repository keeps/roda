/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.access;

import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import org.roda.core.data.v2.accessKey.AccessKey;
import org.roda.core.data.v2.accessKey.AccessKeys;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.SidebarUtils;
import org.roda.wui.client.management.Management;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class AccessKeyManagement extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {AccessKeyManagement.RESOLVER}, false, callback);
    }

    @Override
    public String getHistoryToken() {
      return "access_keys";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  interface MyUiBinder extends UiBinder<Widget, AccessKeyManagement> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static AccessKeyManagement instance = null;

  public static AccessKeyManagement getInstance() {
    if (instance == null) {
      instance = new AccessKeyManagement();
    } else {
      instance.refresh();
    }
    return instance;
  }

  @UiField
  FlowPanel description;

  @UiField
  ScrollPanel accessKeyManagementTablePanel;

  @UiField
  FlowPanel contentFlowPanel;

  @UiField
  FlowPanel sidebarFlowPanel;

  @UiField
  FlowPanel sidebarButtonsPanel;

  public AccessKeyManagement() {
    initWidget(uiBinder.createAndBindUi(this));
    description.add(new HTMLWidgetWrapper(("DisposalPolicyDescription.html")));
    BrowserService.Util.getInstance().listAccessKey(new NoAsyncCallback<AccessKeys>() {
      @Override
      public void onSuccess(AccessKeys accessKeys) {
        createAccessKeyListPanel(accessKeys);
      }
    });
    initSidebar();
  }

  private void initSidebar() {
    SidebarUtils.showSidebar(contentFlowPanel, sidebarFlowPanel);
    Button createNewAccessKeyBtn = new Button();
    createNewAccessKeyBtn.addStyleName("btn btn-block btn-plus");
    createNewAccessKeyBtn.setText(messages.newButton());
    createNewAccessKeyBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        HistoryUtils.newHistory(CreateAccessKey.RESOLVER);
      }
    });

    sidebarButtonsPanel.add(createNewAccessKeyBtn);
  }

  private void createAccessKeyListPanel(AccessKeys accessKeys) {
    accessKeyManagementTablePanel.clear();
    accessKeyManagementTablePanel.addStyleName("basicTable-border");
    accessKeyManagementTablePanel.addStyleName("basicTable");

    if (accessKeys.getObjects().isEmpty()) {
      String someOfAObject = messages.someOfAObject(accessKeys.getClass().getName());
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(someOfAObject)));
      label.addStyleName("basicTableEmpty");
      accessKeyManagementTablePanel.add(label);
    } else {
      FlowPanel accessKeyPanel = new FlowPanel();
      BasicTablePanel<AccessKey> table = getBasicTableForAccessKey(accessKeys);
      table.getSelectionModel().addSelectionChangeHandler(event -> {
        AccessKey selectedObject = table.getSelectionModel().getSelectedObject();
        if (selectedObject != null) {
          table.getSelectionModel().clear();
          List<String> path = HistoryUtils.getHistory(ShowAccessKey.RESOLVER.getHistoryPath(), selectedObject.getId());
          HistoryUtils.newHistory(path);
        }
      });

      accessKeyPanel.add(table);
      accessKeyManagementTablePanel.add(accessKeyPanel);
    }
  }

  private BasicTablePanel<AccessKey> getBasicTableForAccessKey(AccessKeys accessKeys) {
    if (accessKeys.getObjects().isEmpty()) {
      return new BasicTablePanel<>(messages.noItemsToDisplay(messages.distributedInstancesLabel()));
    } else {
      return new BasicTablePanel<AccessKey>(accessKeys.getObjects().iterator(),
        new BasicTablePanel.ColumnInfo<AccessKey>(messages.accessKeyNameLabel(), 15, new TextColumn<AccessKey>() {
          @Override
          public String getValue(AccessKey accessKey) {
            return accessKey.getName();
          }
        }), new BasicTablePanel.ColumnInfo<AccessKey>(messages.accessKeyLastUsageDateLabel(), 15,
          new TextColumn<AccessKey>() {
            @Override
            public String getValue(AccessKey accessKey) {
              return accessKey.getLastUsageDate() != null ? accessKey.getLastUsageDate().toString() : "Never";
            }
          }));
    }
  }

  private void refresh() {
    BrowserService.Util.getInstance().listAccessKey(new NoAsyncCallback<AccessKeys>() {
      @Override
      public void onSuccess(AccessKeys result) {
        createAccessKeyListPanel(result);
      }
    });
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      callback.onSuccess(this);
    } else if (historyTokens.get(0).equals(CreateAccessKey.RESOLVER.getHistoryToken())) {
      CreateAccessKey.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(ShowAccessKey.RESOLVER.getHistoryToken())) {
      ShowAccessKey.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(EditAccessKey.RESOLVER.getHistoryToken())) {
      EditAccessKey.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }
}
