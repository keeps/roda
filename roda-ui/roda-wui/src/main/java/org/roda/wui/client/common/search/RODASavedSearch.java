/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import org.roda.core.data.v2.index.filter.Filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RODASavedSearch {

    private final String searchClassName;
    private final String title;
    private final Filter filter;

    @JsonCreator
    public RODASavedSearch(@JsonProperty("searchClassName") String searchClassName, @JsonProperty("title") String title, @JsonProperty("filter") Filter filter) {
        this.searchClassName = searchClassName;
        this.title = title;
        this.filter = filter;
    }

    public String getSearchClassName() {
        return searchClassName;
    }

    public Filter getFilter() {
        return filter;
    }

    public String getTitle() {
        return title;
    }
}
