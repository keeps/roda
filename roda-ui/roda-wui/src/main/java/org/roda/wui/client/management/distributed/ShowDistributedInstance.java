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
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.management.access.AccessKeyTablePanel;
import org.roda.wui.client.services.Services;
import org.roda.wui.client.welcome.Welcome;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
public class ShowDistributedInstance extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static ShowDistributedInstance instance = null;
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
    public List<String> getHistoryPath() {
      return ListUtils.concat(DistributedInstancesManagement.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "show_distributed_instance";
    }
  };
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  TitlePanel title;
  @UiField
  Label dateCreated;
  @UiField
  Label dateUpdated;
  @UiField
  Label descriptionLabel;
  @UiField
  HTML descriptionValue;
  @UiField
  HTML IDValue;
  @UiField
  HTML lastSyncValue;
  @UiField
  HTML statusValue;
  @UiField
  FlowPanel userNameValue;
  @UiField
  ScrollPanel accessKeyTablePanel;
  @UiField
  FlowPanel statisticsPanel;
  private DistributedInstance distributedInstance;

  public ShowDistributedInstance(DistributedInstance distributedInstance) {
    initWidget(uiBinder.createAndBindUi(this));
    this.distributedInstance = distributedInstance;

    initElements();
  }

  public static ShowDistributedInstance getInstance() {
    if (instance == null) {
      instance = new ShowDistributedInstance(new DistributedInstance());
    }
    return instance;
  }

  private void initElements() {
    title.setText(distributedInstance.getName());

    if (distributedInstance.getDescription() != null && !distributedInstance.getDescription().isEmpty()) {
      descriptionValue.setHTML(distributedInstance.getDescription());
    } else {
      descriptionLabel.setVisible(false);
    }

    if (distributedInstance.getCreatedOn() != null && StringUtils.isNotBlank(distributedInstance.getCreatedBy())) {
      dateCreated.setText(messages.dateCreated(Humanize.formatDateTime(distributedInstance.getCreatedOn()),
        distributedInstance.getCreatedBy()));
    }

    if (distributedInstance.getUpdatedOn() != null && StringUtils.isNotBlank(distributedInstance.getUpdatedBy())) {
      dateUpdated.setText(messages.dateUpdated(Humanize.formatDateTime(distributedInstance.getUpdatedOn()),
        distributedInstance.getUpdatedBy()));
    }

    IDValue.setHTML(distributedInstance.getId());
    lastSyncValue.setHTML(HtmlSnippetUtils.getLastSyncHtml(distributedInstance, true));

    statusValue.setHTML(HtmlSnippetUtils.getDistributedInstanceStateHtml(distributedInstance, false));
    if (StringUtils.isNotBlank(distributedInstance.getUsername())) {
      Services services = new Services("Get User", "get");
      services.membersResource(s -> s.getUser(distributedInstance.getUsername())).whenComplete((user, error) -> {
        if (user != null) {
          userNameValue.add(new Label(user.getId()));
        } else if (error != null) {
          userNameValue.add(new Label("NONE"));
        }
      });

      accessKeyTablePanel.add(new AccessKeyTablePanel(distributedInstance.getUsername()));
    }

    if (distributedInstance != null && distributedInstance.getId() != null) {
      statisticsPanel.add(new HTMLWidgetWrapper("DistributedInstanceStatistics.html", distributedInstance.getId()));
    }

  }

  private void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      Services services = new Services("Get distributed instance", "get");
      services.distributedInstanceResource(s -> s.getDistributedInstance(historyTokens.get(0)))
        .whenComplete((distributedInstance, error) -> {
          if (distributedInstance != null) {
            ShowDistributedInstance showDistributedInstance = new ShowDistributedInstance(distributedInstance);
            callback.onSuccess(showDistributedInstance);
          } else if (error != null) {
            callback.onFailure(error);
            HistoryUtils.newHistory(Welcome.RESOLVER);
          }
        });
    }
  }

  @UiHandler("buttonEdit")
  void buttonApplyHandler(ClickEvent e) {
    HistoryUtils.newHistory(EditDistributedInstance.RESOLVER, distributedInstance.getId());
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    Dialogs.showConfirmDialog(messages.removeDistributedInstanceTitle(), messages.removeDistributedInstanceLabel(),
      messages.cancelButton(), messages.confirmButton(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirm) {
          if (confirm) {
            Services services = new Services("Delete distributed instance", "delete");
            services.distributedInstanceResource(s -> s.deleteDistributedInstance(distributedInstance.getId()))
              .whenComplete((result, error) -> {
                if (error == null) {
                  HistoryUtils.newHistory(DistributedInstancesManagement.RESOLVER);
                }
              });
          }
        }
      });
  }

  interface MyUiBinder extends UiBinder<Widget, ShowDistributedInstance> {
  }

}
