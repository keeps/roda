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

import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.PreservationAgentActions;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 *
 */
public class ShowPreservationAgent extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        ShowPreservationAgent preservationAgents = new ShowPreservationAgent(historyTokens.get(0));
        callback.onSuccess(preservationAgents);
      } else {
        HistoryUtils.newHistory(PreservationAgents.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {PreservationAgents.RESOLVER}, false, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(PreservationAgents.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "agent";
    }
  };

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  @UiField
  Label agentId;
  @UiField
  Label agentName;
  @UiField
  Label agentType;
  @UiField
  Label agentVersion;
  @UiField
  Label agentNote;
  @UiField
  Label agentExtension;
  @UiField
  FlowPanel agentVersionPanel;
  @UiField
  FlowPanel agentNotePanel;
  @UiField
  FlowPanel agentExtensionPanel;
  @UiField
  SimplePanel actionsSidebar;
  ActionableWidgetBuilder<IndexedPreservationAgent> actionableWidgetBuilder;

  public ShowPreservationAgent(final String agentId) {
    initWidget(uiBinder.createAndBindUi(this));

    actionableWidgetBuilder = new ActionableWidgetBuilder<>(PreservationAgentActions.get());

    Services services = new Services("Retrieve preservation agent", "get");
    services.rodaEntityRestService(s -> s.findByUuid(agentId), IndexedPreservationAgent.class)
      .whenComplete((preservationAgent, throwable) -> {
        if (throwable != null) {
          if (throwable instanceof NotFoundException) {
            Toast.showError(messages.notFoundError(), messages.couldNotFindPreservationAgent());
            HistoryUtils.newHistory(ListUtils.concat(PreservationAgents.RESOLVER.getHistoryPath()));
          } else {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          }
        } else {
          viewAction(preservationAgent);
        }
      });
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  public void viewAction(IndexedPreservationAgent agent) {
    agentId.setText(agent.getId());
    agentName.setText(agent.getName());
    agentType.setText(agent.getType());

    agentVersionPanel.setVisible(StringUtils.isNotBlank(agent.getVersion()));
    agentVersion.setText(agent.getVersion());

    agentNotePanel.setVisible(StringUtils.isNotBlank(agent.getNote()));
    agentNote.setText(agent.getNote());

    agentExtensionPanel.setVisible(StringUtils.isNotBlank(agent.getExtension()));
    agentExtension.setText(agent.getExtension());

    actionsSidebar
      .setWidget(actionableWidgetBuilder.withBackButton().buildListWithObjects(new ActionableObject<>(agent)));
  }

  interface MyUiBinder extends UiBinder<Widget, ShowPreservationAgent> {
  }
}
