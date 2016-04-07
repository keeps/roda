/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 *
 */
package org.roda.wui.client.planning;

import java.util.List;

import org.roda.core.data.v2.agents.Agent;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.client.management.UserManagementService;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 *
 */
public class ShowAgent extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(AgentRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "agent";
    }
  };

  private static ShowAgent instance = null;

  public static ShowAgent getInstance() {
    if (instance == null) {
      instance = new ShowAgent();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, ShowAgent> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  // private static CommonMessages messages = GWT.create(CommonMessages.class);

  @UiField
  Label agentId;

  @UiField
  Label agentName;

  @UiField
  Label agentType;

  @UiField
  Label agentDescription;

  @UiField
  Label agentCategory;

  @UiField
  Label agentVersion;

  @UiField
  Label agentLicense;

  @UiField
  Label agentPopularity;

  @UiField
  Label agentInitialRelease;

  @UiField
  Label agentWebsite;

  @UiField
  Label agentDownload;

  @UiField
  Label agentProvenanceInformation;

  @UiField
  Button buttonEdit;

  @UiField
  Button buttonCancel;

  private Agent agent;

  public ShowAgent() {
    this.agent = new Agent();
    initWidget(uiBinder.createAndBindUi(this));
  }

  public ShowAgent(Agent agent) {
    initWidget(uiBinder.createAndBindUi(this));
    this.agent = agent;

    agentId.setText(agent.getId());
    agentName.setText(agent.getName());
    agentType.setText(agent.getType());
    agentDescription.setText(agent.getDescription());
    agentCategory.setText(agent.getCategory());
    agentVersion.setText(agent.getVersion());
    agentLicense.setText(agent.getLicense());
    agentPopularity.setText(Integer.toString(agent.getPopularity()));
    agentInitialRelease.setText(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(
      agent.getInitialRelease()));
    agentWebsite.setText(agent.getWebsite());
    agentDownload.setText(agent.getDownload());
    agentProvenanceInformation.setText(agent.getProvenanceInformation());
  }

  void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {

    if (historyTokens.size() == 1) {
      String agentId = historyTokens.get(0);
      UserManagementService.Util.getInstance().retrieveAgent(agentId, new AsyncCallback<Agent>() {

        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }

        @Override
        public void onSuccess(Agent result) {
          ShowAgent agentPanel = new ShowAgent(result);
          callback.onSuccess(agentPanel);
        }
      });
    } else {
      Tools.newHistory(AgentRegister.RESOLVER);
      callback.onSuccess(null);
    }
  }

  @UiHandler("buttonEdit")
  void handleButtonEdit(ClickEvent e) {
    Tools.newHistory(AgentRegister.RESOLVER, EditAgent.RESOLVER.getHistoryToken(), agent.getId());
  }

  @UiHandler("buttonCancel")
  void handleButtonCancel(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(AgentRegister.RESOLVER);
  }

}
