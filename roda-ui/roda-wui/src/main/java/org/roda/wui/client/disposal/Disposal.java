/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal;

import java.util.Collections;
import java.util.List;

import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.disposal.confirmations.OverdueRecords;
import org.roda.wui.client.disposal.policy.DisposalPolicy;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */
public class Disposal {
  private static Disposal instance = null;
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {DisposalPolicy.RESOLVER, DisposalConfirmations.RESOLVER,
        DisposalDestroyedRecords.RESOLVER}, false, callback);

    }

    @Override
    public List<String> getHistoryPath() {
      return Collections.singletonList(getHistoryToken());
    }

    @Override
    public String getHistoryToken() {
      return "disposal";
    }
  };
  private boolean initialized;
  private HTMLWidgetWrapper page;

  private Disposal() {
    initialized = false;
  }

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static Disposal getInstance() {
    if (instance == null) {
      instance = new Disposal();
    }
    return instance;
  }

  public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
    if (historyTokens.isEmpty()) {
      callback.onSuccess(page);
    } else if (historyTokens.get(0).equals(DisposalPolicy.RESOLVER.getHistoryToken())) {
      DisposalPolicy.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(OverdueRecords.RESOLVER.getHistoryToken())) {
      OverdueRecords.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(DisposalConfirmations.RESOLVER.getHistoryToken())) {
      DisposalConfirmations.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    } else if (historyTokens.get(0).equals(DisposalDestroyedRecords.RESOLVER.getHistoryToken())) {
      DisposalDestroyedRecords.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
    }
  }
}
