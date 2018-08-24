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

import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.PreservationAgentActions;
import org.roda.wui.client.common.lists.PreservationAgentList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
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
public class PreservationAgents extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public List<String> getHistoryPath() {
      return ListUtils.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "agents";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, PreservationAgents> {
  }

  private static PreservationAgents instance = null;

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField(provided = true)
  SearchWrapper searchWrapper;

  public PreservationAgents() {
    ListBuilder<IndexedPreservationAgent> preservationAgentListBuilder = new ListBuilder<>(PreservationAgentList::new,
      new AsyncTableCell.Options<>(IndexedPreservationAgent.class, "PreservationAgents_agents").bindOpener());

    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(preservationAgentListBuilder,
      PreservationAgentActions.get());

    initWidget(uiBinder.createAndBindUi(this));
  }

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static PreservationAgents getInstance() {
    if (instance == null) {
      instance = new PreservationAgents();
    }
    return instance;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }

  private void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      searchWrapper.refreshCurrentList();
      callback.onSuccess(this);
    } else if (!historyTokens.isEmpty()
      && historyTokens.get(0).equals(ShowPreservationAgent.RESOLVER.getHistoryToken())) {
      ShowPreservationAgent.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else {
      HistoryUtils.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }
}
