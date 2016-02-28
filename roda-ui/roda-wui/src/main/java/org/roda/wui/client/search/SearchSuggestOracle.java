/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.facet.SimpleFacetParameter;
import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.v2.index.FacetValue;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.browse.BrowserService;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

public class SearchSuggestOracle<T extends IsIndexed> extends SuggestOracle {

  private Class<T> classToRequest;
  private String facet;

  public SearchSuggestOracle(Class<T> classToRequest, String facet) {
    this.classToRequest = classToRequest;
    this.facet = facet;
  }

  @Override
  public void requestSuggestions(final Request request, final Callback callback) {
    Facets facets = new Facets(new SimpleFacetParameter(facet));

    GWT.log(request.getQuery());

    BrowserService.Util.getInstance().find(classToRequest.getName(),
      new Filter(new BasicSearchFilterParameter(facet, request.getQuery() + "*")), null, new Sublist(0, 0), facets,
      LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<IndexResult<T>>() {

        @Override
        public void onFailure(Throwable caught) {
          GWT.log("ERROR" + caught.getMessage());
        }

        @Override
        public void onSuccess(IndexResult<T> result) {
          List<SearchSuggestion> suggestions = new ArrayList<SearchSuggestion>();

          Iterator<FacetValue> it = result.getFacetResults().iterator().next().getValues().iterator();

          while (it.hasNext()) {
            FacetValue facetValue = (FacetValue) it.next();
            if (facetValue.getCount() > 0) {
              suggestions.add(new SearchSuggestion(facetValue.getValue(), facetValue.getValue(), suggestions.size()));
            }
          }

          Response response = new Response();
          response.setSuggestions(suggestions);

          callback.onSuggestionsReady(request, response);
        }
      });
  }
}
