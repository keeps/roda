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

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.common.RodaConstants;
import org.roda.wui.client.common.BasicSearch;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.AgentList;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

import config.i18n.client.BrowseMessages;

/**
 * @author Luis Faria
 *
 */
public class AgentRegister extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {AgentRegister.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "agentregister";
    }
  };

  private static AgentRegister instance = null;

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static AgentRegister getInstance() {
    if (instance == null) {
      instance = new AgentRegister();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, AgentRegister> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @SuppressWarnings("unused")
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  @UiField
  FlowPanel agentRegisterDescription;

  @UiField(provided = true)
  BasicSearch basicSearch;

  @UiField(provided = true)
  AgentList agentList;

  @UiField
  Button buttonAdd;

  @UiField
  Button buttonEdit;

  @UiField
  Button buttonRemove;

  @UiField
  Button startProcess;

  private static final Filter DEFAULT_FILTER = new Filter(
    new BasicSearchFilterParameter(RodaConstants.AGENT_SEARCH, "*"));

  /**
   * Create a agent register page
   *
   * @param user
   */
  public AgentRegister() {
    Filter filter = null;
    Facets facets = null;

    agentList = new AgentList(filter, facets, "Agents", true);

    basicSearch = new BasicSearch(DEFAULT_FILTER, RodaConstants.AGENT_SEARCH, messages.agentRegisterSearchPlaceHolder(),
      false, false);
    basicSearch.setList(agentList);

    agentList.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {

      }
    });

    initWidget(uiBinder.createAndBindUi(this));

    agentRegisterDescription.add(new HTMLWidgetWrapper("AgentRegisterDescription.html"));

    buttonEdit.setEnabled(false);
    buttonRemove.setEnabled(false);
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      agentList.refresh();
      callback.onSuccess(this);
    } else {
      Tools.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  protected void updateVisibles() {
    // TODO selection control
    buttonEdit.setEnabled(true);
    buttonRemove.setEnabled(true);
  }
}
