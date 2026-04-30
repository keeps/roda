/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.confirmations;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.roda.wui.client.browse.tabs.DisposalOverdueRecordsTabs;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.disposal.Disposal;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.ListUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class OverdueRecords extends Composite {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private static OverdueRecords instance;
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
    public List<String> getHistoryPath() {
      return ListUtils.concat(Disposal.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "overdue";
    }
  };
  @UiField
  FlowPanel createDisposalConfirmationDescription;

  @UiField
  DisposalOverdueRecordsTabs browseTab;

  /**
   * Create a new panel to create a disposal confirmation
   */
  private OverdueRecords() {
    initWidget(uiBinder.createAndBindUi(this));
    createDisposalConfirmationDescription.add(new HTMLWidgetWrapper("CreateDisposalConfirmationDescription.html"));

    browseTab.init();
  }

  public static OverdueRecords getInstance() {
    if (instance == null) {
      instance = new OverdueRecords();
    }

    return instance;
  }

  private void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      callback.onSuccess(this);
    }
  }

  interface MyUiBinder extends UiBinder<Widget, OverdueRecords> {
  }
}
