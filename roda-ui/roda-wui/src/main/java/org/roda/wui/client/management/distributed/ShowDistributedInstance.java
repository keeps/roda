package org.roda.wui.client.management.distributed;

import java.util.List;

import org.roda.core.data.v2.distributedInstance.DistributedInstance;
import org.roda.core.data.v2.user.User;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.UserLoginService;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.management.EditUser;
import org.roda.wui.client.management.UserManagementService;
import org.roda.wui.client.management.access.AccessTokenTablePanel;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.server.browse.BrowserServiceImpl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ShowDistributedInstance extends Composite {
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

  private static ShowDistributedInstance instance = null;
  private DistributedInstance distributedInstance;

  interface MyUiBinder extends UiBinder<Widget, ShowDistributedInstance> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static ShowDistributedInstance getInstance() {
    if (instance == null) {
      instance = new ShowDistributedInstance(new DistributedInstance());
    }
    return instance;
  }

  @UiField
  TitlePanel title;

  @UiField
  Label dateCreated;

  @UiField
  Label dateUpdated;

  @UiField
  HTML descriptionValue;

  @UiField
  HTML IDValue;

  @UiField
  HTML lastSyncDateValue;

  @UiField
  HTML statusValue;

  @UiField
  FlowPanel userNameValue;

  @UiField
  FlowPanel accessTokenTablePanel;

  public ShowDistributedInstance(DistributedInstance distributedInstance) {
    initWidget(uiBinder.createAndBindUi(this));
    this.distributedInstance = distributedInstance;

    initElements();
  }

  private void initElements() {
    title.setText(distributedInstance.getName());

    descriptionValue.setHTML(distributedInstance.getDescription());
    if (distributedInstance.getCreatedOn() != null && StringUtils.isNotBlank(distributedInstance.getCreatedBy())) {
      dateCreated.setText(messages.dateCreated(Humanize.formatDateTime(distributedInstance.getCreatedOn()),
        distributedInstance.getCreatedBy()));
    }

    if (distributedInstance.getUpdatedOn() != null && StringUtils.isNotBlank(distributedInstance.getUpdatedBy())) {
      dateUpdated.setText(messages.dateUpdated(Humanize.formatDateTime(distributedInstance.getUpdatedOn()),
        distributedInstance.getUpdatedBy()));
    }

    IDValue.setHTML(distributedInstance.getId());
    if (distributedInstance.getLastSyncDate() != null) {
      lastSyncDateValue.setHTML(Humanize.formatDateTime(distributedInstance.getLastSyncDate()));
    } else {
      lastSyncDateValue.setHTML(messages.permanentlyRetained());
    }

    statusValue.setHTML(HtmlSnippetUtils.getDistributedInstanceStateHtml(distributedInstance));

    if(StringUtils.isNotBlank(distributedInstance.getUsername())){
      UserManagementService.Util.getInstance().retrieveUser(distributedInstance.getUsername(), new AsyncCallback<User>() {
        @Override
        public void onFailure(Throwable throwable) {
          userNameValue.add(new Label("NONE"));
        }

        @Override
        public void onSuccess(User user) {
//          Anchor anchor = new Anchor(user.getName(),
//              HistoryUtils.createHistoryHashLink(EditUser.RESOLVER, user.getId()));
//          userNameValue.add(anchor);
          userNameValue.add(new Label(user.getId()));
        }
      });

      accessTokenTablePanel.add(new AccessTokenTablePanel(distributedInstance.getUsername()));
    }
  }

  private void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 1) {
      BrowserService.Util.getInstance().retrieveDistributedInstance(historyTokens.get(0),
        new NoAsyncCallback<DistributedInstance>() {
          @Override
          public void onSuccess(DistributedInstance result) {
            ShowDistributedInstance showDistributedInstance = new ShowDistributedInstance(result);
            callback.onSuccess(showDistributedInstance);
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
    BrowserServiceImpl.Util.getInstance().deleteDistributedInstance(distributedInstance.getId(),
      new NoAsyncCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
          HistoryUtils.newHistory(DistributedInstancesManagement.RESOLVER);
        }
      });
  }
}
