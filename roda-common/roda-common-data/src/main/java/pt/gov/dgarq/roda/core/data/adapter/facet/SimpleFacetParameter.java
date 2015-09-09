package pt.gov.dgarq.roda.core.data.adapter.facet;

import java.util.List;

public class SimpleFacetParameter extends FacetParameter {

  private static final long serialVersionUID = -5377147008170114648L;

  public static final int DEFAULT_LIMIT = 100;

  private int limit = DEFAULT_LIMIT;

  public SimpleFacetParameter() {
    super();
  }

  public SimpleFacetParameter(String name) {
    super(name);
  }

  public SimpleFacetParameter(String name, int limit) {
    super(name);
    this.limit = limit;
  }

  public SimpleFacetParameter(String name, List<String> values) {
    super(name, values);
  }

  public SimpleFacetParameter(String name, List<String> values, int minCount) {
    super(name, values, minCount);
  }

  public SimpleFacetParameter(String name, List<String> values, int minCount, int limit) {
    super(name, values, minCount);
    this.limit = limit;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }
}
