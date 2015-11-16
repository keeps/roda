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
package org.roda.wui.client.ingest.transfer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.common.RodaConstants;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.filter.EmptyKeyFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.filter.SimpleFilterParameter;
import org.roda.core.data.v2.TransferredResource;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.TransferredResourceList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

/**
 * @author Luis Faria <lfaria@keep.pt>
 * 
 */
public class IngestTransfer extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public String getHistoryToken() {
      return "transfer";
    }

    @Override
    public List<String> getHistoryPath() {
      return Tools.concat(Ingest.RESOLVER.getHistoryPath(), getHistoryToken());
    }
  };

  private static IngestTransfer instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static IngestTransfer getInstance() {
    if (instance == null) {
      instance = new IngestTransfer();
    }
    return instance;
  }

  interface MyUiBinder extends UiBinder<Widget, IngestTransfer> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private boolean init = true;

  @UiField(provided = true)
  TransferredResourceList transferredResourceList;

  // FILTERS
  @UiField(provided = true)
  FlowPanel facetOwner;

  private IngestTransfer() {

    Filter filter = new Filter(new EmptyKeyFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENTPATH));

    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.TRANSFERRED_RESOURCE_OWNER));

    // TODO externalise strings
    transferredResourceList = new TransferredResourceList(filter, facets, "Transferred resources list");

    facetOwner = new FlowPanel();
    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.TRANSFERRED_RESOURCE_OWNER, facetOwner);
    FacetUtils.bindFacets(transferredResourceList, facetPanels);

    initWidget(uiBinder.createAndBindUi(this));

    transferredResourceList.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        TransferredResource r = transferredResourceList.getSelectionModel().getSelectedObject();
        view(r);
      }
    });

  }

  protected void view(TransferredResource r) {
    Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENTPATH, r.getRelativePath()));
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.SDO_LEVEL),
      new SimpleFacetParameter(RodaConstants.AIP_HAS_REPRESENTATIONS));
    transferredResourceList.setFilter(filter);
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      if (init) {
        init = false;
      } else {
        transferredResourceList.refresh();
      }

      callback.onSuccess(this);
    } else {
      Tools.newHistory(RESOLVER);
      callback.onSuccess(null);
    }
  }

  protected void updateVisibles() {

  }
}
