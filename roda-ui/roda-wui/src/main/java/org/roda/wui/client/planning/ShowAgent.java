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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.AgentMessages;

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

  private static AgentMessages messages = GWT.create(AgentMessages.class);

  @UiField
  Label agentId;

  @UiField
  Label agentName;

  @UiField
  Label agentTypeKey, agentTypeValue;

  @UiField
  Label agentDescriptionKey, agentDescriptionValue;

  @UiField
  Label agentCategory;

  @UiField
  Label agentVersionKey, agentVersionValue;

  @UiField
  Label agentLicenseKey, agentLicenseValue;

  @UiField
  Label agentPopularityKey, agentPopularityValue;

  @UiField
  Label agentInitialRelease;

  @UiField
  Label agentWebsiteKey, agentWebsiteValue;

  @UiField
  Label agentDownloadKey, agentDownloadValue;

  @UiField
  Label agentProvenanceInformationKey, agentProvenanceInformationValue;

  @UiField
  Label platformsKey;

  @UiField
  FlowPanel platformsValue;

  @UiField
  Label extensionsKey;

  @UiField
  FlowPanel extensionsValue;

  @UiField
  Label mimetypesKey;

  @UiField
  FlowPanel mimetypesValue;

  @UiField
  Label pronomsKey;

  @UiField
  FlowPanel pronomsValue;

  @UiField
  Label utisKey;

  @UiField
  FlowPanel utisValue;

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

    agentTypeValue.setText(agent.getType());
    agentTypeKey.setVisible(agent.getType().length() > 0);

    agentDescriptionValue.setText(agent.getDescription());
    agentDescriptionKey.setVisible(agent.getDescription().length() > 0);

    agentCategory.setText(agent.getCategory());

    agentVersionValue.setText(agent.getVersion());
    agentVersionKey.setVisible(agent.getVersion().length() > 0);

    agentLicenseValue.setText(agent.getLicense());
    agentLicenseKey.setVisible(agent.getLicense().length() > 0);

    agentPopularityValue.setText(Integer.toString(agent.getPopularity()));
    agentPopularityKey.setVisible(Integer.toString(agent.getPopularity()).length() > 0);

    agentInitialRelease
      .setText(DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL).format(agent.getInitialRelease()));

    agentWebsiteValue.setText(agent.getWebsite());
    agentWebsiteKey.setVisible(agent.getWebsite().length() > 0);

    agentDownloadValue.setText(agent.getDownload());
    agentDownloadKey.setVisible(agent.getDownload().length() > 0);

    agentProvenanceInformationValue.setText(agent.getProvenanceInformation());
    agentProvenanceInformationKey.setVisible(agent.getProvenanceInformation().length() > 0);

    List<String> platformsList = agent.getPlatforms();
    platformsValue.setVisible(platformsList != null && !platformsList.isEmpty());
    platformsKey.setVisible(platformsList != null && !platformsList.isEmpty());

    if (platformsList != null) {
      for (String platform : platformsList) {
        HTML parPanel = new HTML();
        parPanel.setHTML(messages.agentListItems(platform));
        platformsValue.add(parPanel);
      }
    }

    List<String> extensionsList = agent.getExtensions();
    extensionsValue.setVisible(extensionsList != null && !extensionsList.isEmpty());
    extensionsKey.setVisible(extensionsList != null && !extensionsList.isEmpty());

    if (extensionsList != null) {
      for (String extension : extensionsList) {
        HTML parPanel = new HTML();
        parPanel.setHTML(messages.agentListItems(extension));
        extensionsValue.add(parPanel);
      }
    }

    List<String> mimetypesList = agent.getMimetypes();
    mimetypesValue.setVisible(mimetypesList != null && !mimetypesList.isEmpty());
    mimetypesKey.setVisible(mimetypesList != null && !mimetypesList.isEmpty());

    if (mimetypesList != null) {
      for (String mimetype : mimetypesList) {
        HTML parPanel = new HTML();
        parPanel.setHTML(messages.agentListItems(mimetype));
        mimetypesValue.add(parPanel);
      }
    }

    List<String> pronomsList = agent.getPronoms();
    pronomsValue.setVisible(pronomsList != null && !pronomsList.isEmpty());
    pronomsKey.setVisible(pronomsList != null && !pronomsList.isEmpty());

    if (pronomsList != null) {
      for (String pronom : pronomsList) {
        HTML parPanel = new HTML();
        parPanel.setHTML(messages.agentListItems(pronom));
        pronomsValue.add(parPanel);
      }
    }

    List<String> utisList = agent.getUtis();
    utisValue.setVisible(utisList != null && !utisList.isEmpty());
    utisKey.setVisible(utisList != null && !utisList.isEmpty());

    if (utisList != null) {
      for (String uti : utisList) {
        HTML parPanel = new HTML();
        parPanel.setHTML(messages.agentListItems(uti));
        utisValue.add(parPanel);
      }
    }
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
