package org.roda.core.data.eadc;

import java.io.Serializable;
import java.util.Arrays;

public class Index implements EadCValue, Serializable {

  private static final long serialVersionUID = 8354829833834860455L;

  private Indexentry[] indexentries = null;

  public Index() {

  }

  public Index(Indexentry[] indexentries) {
    super();
    this.indexentries = indexentries;
  }

  public Indexentry[] getIndexes() {
    return indexentries;
  }

  public void setIndexes(Indexentry[] indexes) {
    this.indexentries = indexes;
  }

  @Override
  public String toString() {
    return "Index [indexentries=" + Arrays.toString(indexentries) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(indexentries);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Index other = (Index) obj;
    if (!Arrays.equals(indexentries, other.indexentries)) {
      return false;
    }
    return true;
  }

}
