package org.roda.core.data.v2.index;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SuggestRequest implements Serializable {

  @Serial
  private static final long serialVersionUID = -4752301854207083738L;

  private String field;
  private String query;
  private boolean allowPartial;

  public SuggestRequest() {
    // empty constructor
  }

  public SuggestRequest(String field, String query, boolean allowPartial) {
    this.field = field;
    this.query = query;
    this.allowPartial = allowPartial;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public boolean isAllowPartial() {
    return allowPartial;
  }

  public void setAllowPartial(boolean allowPartial) {
    this.allowPartial = allowPartial;
  }
}
