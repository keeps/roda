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
package org.roda.wui.client.planning.agents;

import java.util.List;

import com.google.gwt.user.client.ui.FocusPanel;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.wui.client.browse.tabs.PreservationAgentTabs;
import org.roda.wui.client.common.NavigationToolbar;
import org.roda.wui.client.common.PreservationAgentActionsToolbar;
import org.roda.wui.client.common.TitlePanel;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.main.BreadcrumbUtils;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
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
        Services services = new Services("Retrieve preservation agent", "get");
        services.rodaEntityRestService(s -> s.findByUuid(historyTokens.get(0), LocaleInfo.getCurrentLocale().getLocaleName()),
                IndexedPreservationAgent.class).whenComplete((preservationAgent, throwable) -> {
          if (throwable != null) {
            if (throwable instanceof NotFoundException) {
              Toast.showError(messages.notFoundError(), messages.couldNotFindPreservationAgent());
              HistoryUtils.newHistory(PreservationAgents.RESOLVER);
            } else {
              AsyncCallbackUtils.defaultFailureTreatment(throwable);
            }
          } else {
            ShowPreservationAgent preservationAgents = new ShowPreservationAgent(preservationAgent);
            callback.onSuccess(preservationAgents);
          }
        });


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
  FocusPanel keyboardFocus;

  @UiField
  NavigationToolbar<IndexedPreservationAgent> navigationToolbar;

  @UiField
  PreservationAgentActionsToolbar actionsToolbar;

  @UiField
  TitlePanel title;

  @UiField
  PreservationAgentTabs browseTab;

  public ShowPreservationAgent(final IndexedPreservationAgent agent) {
    initWidget(uiBinder.createAndBindUi(this));

    navigationToolbar.withObject(agent).build();
    navigationToolbar.updateBreadcrumbPath(BreadcrumbUtils.getPreservationAgentBreadcrumbs(agent));

    actionsToolbar.setLabel(messages.preservationAgentTitle());
    actionsToolbar.setObjectAndBuild(agent, null, null);

    title.setText(agent.getName());
    title.setIconClass("IndexedPreservationAgent");

    keyboardFocus.setFocus(true);
    keyboardFocus.addStyleName("browse");

    browseTab.init(agent);
  }


  interface MyUiBinder extends UiBinder<Widget, ShowPreservationAgent> {
  }
}
