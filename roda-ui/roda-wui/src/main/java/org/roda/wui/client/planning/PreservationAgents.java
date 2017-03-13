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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.facet.SimpleFacetParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.metadata.IndexedPreservationAgent;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.lists.PreservationAgentList;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.ListUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class PreservationAgents extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.isEmpty()) {
        PreservationAgents preservationAgents = new PreservationAgents();
        callback.onSuccess(preservationAgents);
      } else if (!historyTokens.isEmpty()
        && historyTokens.get(0).equals(ShowPreservationAgent.RESOLVER.getHistoryToken())) {
        ShowPreservationAgent.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else {
        HistoryUtils.newHistory(RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    public List<String> getHistoryPath() {
      return ListUtils.concat(Planning.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "agents";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, PreservationAgents> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static ClientMessages messages = (ClientMessages) GWT.create(ClientMessages.class);

  @UiField
  SimplePanel itemIcon;

  @UiField
  Label itemTitle;

  @UiField(provided = true)
  SearchPanel agentSearch;

  @UiField(provided = true)
  PreservationAgentList agentList;

  @UiField(provided = true)
  FlowPanel facetClasses;

  public PreservationAgents() {
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.PRESERVATION_AGENT_TYPE));
    agentList = new PreservationAgentList(Filter.ALL, facets, messages.preservationAgentsTitle(), false);

    facetClasses = new FlowPanel();
    Map<String, FlowPanel> facetPanels = new HashMap<>();
    facetPanels.put(RodaConstants.PRESERVATION_AGENT_TYPE, facetClasses);
    FacetUtils.bindFacets(agentList, facetPanels);

    agentList.getSelectionModel().addSelectionChangeHandler(new Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedPreservationAgent selected = agentList.getSelectionModel().getSelectedObject();
        if (selected != null) {
          HistoryUtils.newHistory(ShowPreservationAgent.RESOLVER, selected.getId());
        }
      }
    });

    agentSearch = new SearchPanel(Filter.ALL, RodaConstants.PRESERVATION_AGENT_SEARCH, true,
      messages.searchPlaceHolder(), false, false, true);
    agentSearch.setList(agentList);

    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    JavascriptUtils.stickSidebar();
  }
}
