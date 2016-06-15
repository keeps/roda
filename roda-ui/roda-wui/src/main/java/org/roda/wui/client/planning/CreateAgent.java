/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning;

import java.util.List;

import org.roda.core.data.v2.agents.Agent;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

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

public class CreateAgent extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      Agent agent = new Agent();
      CreateAgent createAgent = new CreateAgent(agent);
      callback.onSuccess(createAgent);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(AgentRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "create_agent";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, CreateAgent> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private Agent agent;
  private static ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField(provided = true)
  AgentDataPanel agentDataPanel;

  /**
   * Create a new panel to create a user
   *
   * @param user
   *          the user to create
   */
  public CreateAgent(Agent agent) {
    this.agent = agent;
    this.agentDataPanel = new AgentDataPanel(true, false, agent);
    initWidget(uiBinder.createAndBindUi(this));
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (agentDataPanel.isValid()) {
      agent = agentDataPanel.getAgent();
      BrowserService.Util.getInstance().addAgent(agent, new AsyncCallback<Agent>() {

        public void onFailure(Throwable caught) {
          errorMessage(caught);
        }

        @Override
        public void onSuccess(Agent result) {
          Tools.newHistory(AgentRegister.RESOLVER, result.getId());
        }

      });
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(AgentRegister.RESOLVER);
  }

  private void errorMessage(Throwable caught) {
    Toast.showError(messages.createAgentFailure(caught.getMessage()));
  }

}
