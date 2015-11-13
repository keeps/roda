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

import java.util.List;

import org.roda.core.common.RodaConstants;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.ingest.Ingest;
import org.roda.wui.common.client.ClientLogger;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.TransferredResourceList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

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

  @UiField(provided=true)
  TransferredResourceList transferredResourceList;

  private IngestTransfer() {

    Filter filter = null;

    Facets facets = null;
    
    // TODO externalise strings
    transferredResourceList = new TransferredResourceList(filter, facets, "Transferred resources list");

    initWidget(uiBinder.createAndBindUi(this));

  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      if (init) {
        init = false;
      } else {
        // sipList.refresh();
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
