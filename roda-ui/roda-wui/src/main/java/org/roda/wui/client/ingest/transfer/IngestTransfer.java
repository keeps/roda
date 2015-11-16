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

import java.util.ArrayList;
import java.util.Arrays;
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
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.TransferredResourceList;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.client.main.BreadcrumbItem;
import org.roda.wui.client.main.BreadcrumbPanel;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.FacetUtils;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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

  private static final String TOP_ICON = "<i class='fa fa-circle-o'></i>";

  private static final Filter DEFAULT_FILTER = new Filter(
    new EmptyKeyFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENTPATH));

  interface MyUiBinder extends UiBinder<Widget, IngestTransfer> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  // private ClientLogger logger = new ClientLogger(getClass().getName());

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  TransferredResourceList transferredResourceList;

  // FILTERS
  @UiField(provided = true)
  FlowPanel facetOwner;

  private IngestTransfer() {
    Facets facets = new Facets(new SimpleFacetParameter(RodaConstants.TRANSFERRED_RESOURCE_OWNER));

    // TODO externalise strings
    transferredResourceList = new TransferredResourceList(DEFAULT_FILTER, facets, "Transferred resources list");

    facetOwner = new FlowPanel();
    Map<String, FlowPanel> facetPanels = new HashMap<String, FlowPanel>();
    facetPanels.put(RodaConstants.TRANSFERRED_RESOURCE_OWNER, facetOwner);
    FacetUtils.bindFacets(transferredResourceList, facetPanels);

    initWidget(uiBinder.createAndBindUi(this));

    transferredResourceList.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        TransferredResource r = transferredResourceList.getSelectionModel().getSelectedObject();
        if (r != null) {
          Tools.newHistory(RESOLVER, getPathFromTransferredResourceId(r.getId()));
        }
      }
    });

  }

  protected void view(TransferredResource r) {

    Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_PARENTPATH, r.getRelativePath()),
      new SimpleFilterParameter(RodaConstants.TRANSFERRED_RESOURCE_OWNER, r.getOwner()));
    transferredResourceList.setFilter(filter);

    breadcrumb.updatePath(getBreadcrumbs(r));
    breadcrumb.setVisible(true);
  }

  protected void view() {
    transferredResourceList.setFilter(DEFAULT_FILTER);
    breadcrumb.setVisible(false);
  }

  private List<BreadcrumbItem> getBreadcrumbs(TransferredResource r) {
    List<BreadcrumbItem> ret = new ArrayList<BreadcrumbItem>();

    ret.add(new BreadcrumbItem(SafeHtmlUtils.fromSafeConstant(TOP_ICON), RESOLVER.getHistoryPath()));
    if (r != null) {
      List<String> pathBuilder = new ArrayList<String>();
      pathBuilder.addAll(RESOLVER.getHistoryPath());

      String[] parts = r.getId().split("/");
      for (String part : parts) {
        SafeHtml breadcrumbLabel = SafeHtmlUtils.fromString(part);
        pathBuilder.add(part);
        List<String> path = new ArrayList<>(pathBuilder);
        ret.add(new BreadcrumbItem(breadcrumbLabel, path));
      }
    }

    return ret;
  }

  public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
    GWT.log("tokens: " + historyTokens);
    if (historyTokens.size() == 0) {
      view();
      callback.onSuccess(this);
    } else {
      String transferredResourceId = getTransferredResourceIdFromPath(historyTokens);
      if (transferredResourceId != null) {
        BrowserService.Util.getInstance().retrieveTransferredResource(transferredResourceId,
          new AsyncCallback<TransferredResource>() {

            @Override
            public void onFailure(Throwable caught) {
              callback.onFailure(caught);
            }

            @Override
            public void onSuccess(TransferredResource r) {
              view(r);
              callback.onSuccess(IngestTransfer.this);
            }
          });
      } else {
        view();
        callback.onSuccess(this);
      }

    }

  }

  private String getTransferredResourceIdFromPath(List<String> historyTokens) {
    String ret;
    if (historyTokens.size() > 1) {
      ret = Tools.join(historyTokens, "/");
    } else {
      ret = null;
    }

    return ret;
  }

  private List<String> getPathFromTransferredResourceId(String transferredResourceId) {
    return Arrays.asList(transferredResourceId.split("/"));
  }

  protected void updateVisibles() {

  }
}
