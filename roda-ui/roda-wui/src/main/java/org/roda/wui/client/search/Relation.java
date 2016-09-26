/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.browse.Browse;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Luis Faria
 * 
 */
public class Relation {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      getInstance().resolve(historyTokens, callback);
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      callback.onSuccess(Boolean.TRUE);
    }

    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }

    public String getHistoryToken() {
      return "relation";
    }
  };

  private static Relation instance = null;

  /**
   * Get the singleton instance
   * 
   * @return the instance
   */
  public static Relation getInstance() {
    if (instance == null) {
      instance = new Relation();
    }
    return instance;
  }

  public void resolve(final List<String> historyTokens, final AsyncCallback<Widget> callback) {
    if (historyTokens.size() == 0) {
      Tools.newHistory(Search.RESOLVER);
    } else {
      // #relation/TYPE/key/value/key/value
      List<FilterParameter> params = new ArrayList<>();
      for (int i = 1; i < historyTokens.size() - 1; i += 2) {
        String key = historyTokens.get(i);
        String value = historyTokens.get(i + 1);

        params.add(new SimpleFilterParameter(key, value));
      }

      Filter filter = new Filter(params);

      BrowserService.Util.getInstance().find(IndexedAIP.class.getName(), filter, Sorter.NONE, new Sublist(0, 1),
        Facets.NONE, LocaleInfo.getCurrentLocale().getLocaleName(), false,
        new AsyncCallback<IndexResult<IndexedAIP>>() {

          @Override
          public void onFailure(Throwable caught) {
            Tools.newHistory(Search.RESOLVER);
          }

          @Override
          public void onSuccess(IndexResult<IndexedAIP> result) {
            if (result.getTotalCount() == 1) {
              Tools.newHistory(Browse.RESOLVER, result.getResults().get(0).getUUID());
            } else {
              Tools.newHistory(Search.RESOLVER, historyTokens);
            }
          }
        });
    }
  }
}
