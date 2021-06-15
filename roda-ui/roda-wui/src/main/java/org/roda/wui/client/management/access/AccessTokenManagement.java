package org.roda.wui.client.management.access;

import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import org.roda.core.data.v2.accessToken.AccessToken;
import org.roda.core.data.v2.accessToken.AccessTokens;
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
public class AccessTokenManagement extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {AccessTokenManagement.RESOLVER}, false, callback);
    }

    @Override
    public String getHistoryToken() {
      return "access_tokens";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  interface MyUiBinder extends UiBinder<Widget, AccessTokenManagement> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static AccessTokenManagement instance = null;

  public static AccessTokenManagement getInstance() {
    if (instance == null) {
      instance = new AccessTokenManagement();
    } else {
      instance.refresh();
    }
    return instance;
  }

  @UiField
  FlowPanel description;

  @UiField
  ScrollPanel accessTokenManagementTablePanel;

  @UiField
  FlowPanel contentFlowPanel;

  @UiField
  FlowPanel sidebarFlowPanel;

  @UiField
  FlowPanel sidebarButtonsPanel;

  public AccessTokenManagement() {
    initWidget(uiBinder.createAndBindUi(this));
    description.add(new HTMLWidgetWrapper(("DisposalPolicyDescription.html")));
    BrowserService.Util.getInstance().listAccessToken(new NoAsyncCallback<AccessTokens>() {
      @Override
      public void onSuccess(AccessTokens accessTokens) {
        createAccessTokenListPanel(accessTokens);
      }
    });
    initSidebar();
  }

  private void initSidebar() {
    SidebarUtils.showSidebar(contentFlowPanel, sidebarFlowPanel);
    Button createNewAccessTokenBtn = new Button();
    createNewAccessTokenBtn.addStyleName("btn btn-block btn-plus");
    createNewAccessTokenBtn.setText(messages.newButton());
    createNewAccessTokenBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        HistoryUtils.newHistory(CreateAccessToken.RESOLVER);
      }
    });

    sidebarButtonsPanel.add(createNewAccessTokenBtn);
  }

  private void createAccessTokenListPanel(AccessTokens accessTokens) {
    accessTokenManagementTablePanel.clear();
    accessTokenManagementTablePanel.addStyleName("basicTable-border");
    accessTokenManagementTablePanel.addStyleName("basicTable");

    if (accessTokens.getObjects().isEmpty()) {
      String someOfAObject = messages.someOfAObject(accessTokens.getClass().getName());
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(someOfAObject)));
      label.addStyleName("basicTableEmpty");
      accessTokenManagementTablePanel.add(label);
    } else {
      FlowPanel accessTokenPanel = new FlowPanel();
      BasicTablePanel<AccessToken> table = getBasicTableForAccessToken(accessTokens);
      table.getSelectionModel().addSelectionChangeHandler(event -> {
        AccessToken selectedObject = table.getSelectionModel().getSelectedObject();
        if (selectedObject != null) {
          table.getSelectionModel().clear();
          List<String> path = HistoryUtils.getHistory(ShowAccessToken.RESOLVER.getHistoryPath(),
            selectedObject.getId());
          HistoryUtils.newHistory(path);
        }
      });

      accessTokenPanel.add(table);
      accessTokenManagementTablePanel.add(accessTokenPanel);
    }
  }

  private BasicTablePanel<AccessToken> getBasicTableForAccessToken(AccessTokens accessTokens) {
    if (accessTokens.getObjects().isEmpty()) {
      return new BasicTablePanel<>(messages.noItemsToDisplay(messages.distributedInstancesLabel()));
    } else {
      return new BasicTablePanel<AccessToken>(accessTokens.getObjects().iterator(),
        new BasicTablePanel.ColumnInfo<AccessToken>(messages.accessTokenNameLabel(), 15, new TextColumn<AccessToken>() {
          @Override
          public String getValue(AccessToken accessToken) {
            return accessToken.getName();
          }
        }), new BasicTablePanel.ColumnInfo<AccessToken>(messages.accessTokenLastUsageDateLabel(), 15,
          new TextColumn<AccessToken>() {
            @Override
            public String getValue(AccessToken accessToken) {
              return accessToken.getLastUsageDate() != null ? accessToken.getLastUsageDate().toString() : "Never";
            }
          }));
    }
  }

  private void refresh() {
    BrowserService.Util.getInstance().listAccessToken(new NoAsyncCallback<AccessTokens>() {
      @Override
      public void onSuccess(AccessTokens result) {
        createAccessTokenListPanel(result);
      }
    });
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      callback.onSuccess(this);
    } else if (historyTokens.get(0).equals(CreateAccessToken.RESOLVER.getHistoryToken())) {
      CreateAccessToken.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(ShowAccessToken.RESOLVER.getHistoryToken())) {
      ShowAccessToken.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(EditAccessToken.RESOLVER.getHistoryToken())) {
      EditAccessToken.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }
}
