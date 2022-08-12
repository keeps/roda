/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.management.distributed;

import java.util.List;

import org.roda.core.data.v2.synchronization.central.DistributedInstance;
import org.roda.core.data.v2.synchronization.central.DistributedInstances;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.SidebarUtils;
import org.roda.wui.client.management.Management;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DistributedInstancesManagement extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {DistributedInstancesManagement.RESOLVER}, false,
        callback);
    }

    @Override
    public String getHistoryToken() {
      return "distributed_instances";
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Management.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };
  private static DistributedInstancesManagement instance = null;

  interface MyUiBinder extends UiBinder<Widget, DistributedInstancesManagement> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static DistributedInstancesManagement getInstance() {
    if (instance == null) {
      instance = new DistributedInstancesManagement();
    } else {
      instance.refresh();
    }
    return instance;
  }

  @UiField
  FlowPanel distributedInstancesManagementDescription;

  @UiField
  FlowPanel contentFlowPanel;

  @UiField
  ScrollPanel distributedInstancesManagementTablePanel;

  @UiField
  FlowPanel sidebarFlowPanel;

  @UiField
  FlowPanel sidebarButtonsPanel;

  public DistributedInstancesManagement() {
    initWidget(uiBinder.createAndBindUi(this));
    distributedInstancesManagementDescription.add(new HTMLWidgetWrapper(("DistributedInstancesDescription.html")));
    BrowserService.Util.getInstance().listDistributedInstances(new NoAsyncCallback<DistributedInstances>() {
      @Override
      public void onSuccess(DistributedInstances distributedInstances) {
        init(distributedInstances);
      }
    });
    initSidebar();
  }

  private void initSidebar() {
    SidebarUtils.showSidebar(contentFlowPanel, sidebarFlowPanel);
    Button createDistributedInstanceBtn = new Button();
    createDistributedInstanceBtn.addStyleName("btn btn-block btn-plus");
    createDistributedInstanceBtn.setText(messages.newButton());
    createDistributedInstanceBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        HistoryUtils.newHistory(CreateDistributedInstance.RESOLVER);
      }
    });

    sidebarButtonsPanel.add(createDistributedInstanceBtn);
  }

  private void init(DistributedInstances distributedInstances) {
    createDistributedInstanceListPanel(distributedInstances);
  }

  private void createDistributedInstanceListPanel(DistributedInstances distributedInstances) {
    distributedInstancesManagementTablePanel.clear();
    distributedInstancesManagementTablePanel.addStyleName("basicTable-border");
    distributedInstancesManagementTablePanel.addStyleName("basicTable");

    if (distributedInstances.getObjects().isEmpty()) {
      String someOfAObject = messages.someOfAObject(distributedInstances.getClass().getName());
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(someOfAObject)));
      label.addStyleName("basicTableEmpty");
      distributedInstancesManagementTablePanel.add(label);
    } else {
      FlowPanel DistributedInstancesPanel = new FlowPanel();
      BasicTablePanel<DistributedInstance> tableDistributedInstances = getBasicTableForDistributedInstances(
        distributedInstances);
      tableDistributedInstances.getSelectionModel().addSelectionChangeHandler(event -> {
        DistributedInstance selectedObject = tableDistributedInstances.getSelectionModel().getSelectedObject();
        if (selectedObject != null) {
          tableDistributedInstances.getSelectionModel().clear();
          List<String> path = HistoryUtils.getHistory(ShowDistributedInstance.RESOLVER.getHistoryPath(),
            selectedObject.getId());
          HistoryUtils.newHistory(path);
        }
      });

      DistributedInstancesPanel.add(tableDistributedInstances);
      distributedInstancesManagementTablePanel.add(DistributedInstancesPanel);
    }
  }

  private BasicTablePanel<DistributedInstance> getBasicTableForDistributedInstances(
    DistributedInstances distributedInstances) {
    if (distributedInstances.getObjects().isEmpty()) {
      return new BasicTablePanel<>(messages.noItemsToDisplay(messages.distributedInstancesLabel()));
    } else {
      return new BasicTablePanel<DistributedInstance>(distributedInstances.getObjects().iterator(),
        new BasicTablePanel.ColumnInfo<DistributedInstance>(messages.distributedInstanceNameLabel(), 15,
          new TextColumn<DistributedInstance>() {
            @Override
            public String getValue(DistributedInstance distributedInstance) {
              return distributedInstance.getName();
            }
          }),
        new BasicTablePanel.ColumnInfo<DistributedInstance>(messages.distributedInstanceStatusLabel(), 15,
          new Column<DistributedInstance, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(DistributedInstance distributedInstance) {
              return HtmlSnippetUtils.getDistributedInstanceStateHtml(distributedInstance, true);
            }
          }),
        new BasicTablePanel.ColumnInfo<DistributedInstance>(messages.distributedInstanceLastSyncDateLabel(), 15,
          new TextColumn<DistributedInstance>() {
            @Override
            public String getValue(DistributedInstance distributedInstance) {
              return distributedInstance.getLastSynchronizationDate() != null
                ? distributedInstance.getLastSynchronizationDate().toString()
                : "None";
            }
          }),
        new BasicTablePanel.ColumnInfo<DistributedInstance>(messages.loginUsername(), 15,
          new TextColumn<DistributedInstance>() {
            @Override
            public String getValue(DistributedInstance distributedInstance) {
              return distributedInstance.getUsername();
            }
          }));
    }
  }

  private void refresh() {
    BrowserService.Util.getInstance().listDistributedInstances(new NoAsyncCallback<DistributedInstances>() {
      @Override
      public void onSuccess(DistributedInstances distributedInstances) {
        init(distributedInstances);
      }
    });
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      callback.onSuccess(this);
    } else if (historyTokens.get(0).equals(CreateDistributedInstance.RESOLVER.getHistoryToken())) {
      CreateDistributedInstance.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(ShowDistributedInstance.RESOLVER.getHistoryToken())) {
      ShowDistributedInstance.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(EditDistributedInstance.RESOLVER.getHistoryToken())) {
      EditDistributedInstance.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }
}
