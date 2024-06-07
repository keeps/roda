package org.roda.core.data.v2.generics.select;

import java.io.Serial;

import org.roda.core.data.v2.index.filter.Filter;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonTypeName("SelectedItemsFilterRequest")
public class SelectedItemsFilterRequest implements SelectedItemsRequest {

  @Serial
  private static final long serialVersionUID = 6578005945791757611L;

  private Filter filter;
  private boolean justActive;

  public SelectedItemsFilterRequest() {
    justActive = true;
  }

  public SelectedItemsFilterRequest(Filter filter, boolean justActive) {
    this.filter = filter;
    this.justActive = justActive;
  }

  public Filter getFilter() {
    return filter;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public boolean getJustActive() {
    return justActive;
  }

  public void setJustActive(boolean justActive) {
    this.justActive = justActive;
  }
}
