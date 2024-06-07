package org.roda.core.data.v2.generics.select;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonTypeName("SelectedItemsListRequest")
public class SelectedItemsListRequest implements SelectedItemsRequest {

  @Serial
  private static final long serialVersionUID = -6736732967710193663L;

  private List<String> ids;

  public SelectedItemsListRequest() {
    // empty constructor
    this.ids = new ArrayList<>();
  }

  public SelectedItemsListRequest(List<String> ids) {
    this.ids = ids;
  }

  public List<String> getIds() {
    return ids;
  }

  public void setIds(List<String> ids) {
    this.ids = ids;
  }
}
