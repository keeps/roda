/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;

public class SearchSuggestOracle<T extends IsIndexed> extends SuggestOracle {

  private Class<T> classToRequest;
  private String field;
  private boolean allowPartial;

  public SearchSuggestOracle(Class<T> classToRequest, String field, boolean allowPartial) {
    this.classToRequest = classToRequest;
    this.field = field;
    this.allowPartial = allowPartial;
  }

  @Override
  public void requestSuggestions(final Request request, final Callback callback) {
    BrowserService.Util.getInstance().suggest(classToRequest.getName(), field, request.getQuery(), allowPartial,
      new AsyncCallback<List<String>>() {

        @Override
        public void onFailure(Throwable caught) {
          AsyncCallbackUtils.defaultFailureTreatment(caught);
        }

        @Override
        public void onSuccess(List<String> suggestionList) {
          List<SearchSuggest> suggestions = new ArrayList<>();
          for (String suggestion : suggestionList) {
            suggestions.add(new SearchSuggest(suggestion, suggestion, suggestions.size()));
          }

          Response response = new Response();
          response.setSuggestions(suggestions);
          callback.onSuggestionsReady(request, response);
        }
      });
  }
}
