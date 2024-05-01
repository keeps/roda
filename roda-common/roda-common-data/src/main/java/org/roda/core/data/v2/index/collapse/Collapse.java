package org.roda.core.data.v2.index.collapse;

import java.io.Serial;
import java.io.Serializable;

import org.roda.core.data.v2.index.sort.Sorter;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */

// https://solr.apache.org/guide/solr/latest/query-guide/collapse-and-expand-results.html
public class Collapse implements Serializable {

  @Serial
  private static final long serialVersionUID = 5682757525166931995L;

  private String field;
  private MinMax minMax = MinMax.NONE;
  private Sorter sorter = Sorter.NONE;
  private NullPolicyEnum nullPolicy = NullPolicyEnum.IGNORE;
  private HintEnum hint = HintEnum.NONE;
  private long size = 100000;
  private boolean collectElevatedDocsWhenCollapsing = true;

  public Collapse() {
    // empty constructor
  }

  public Collapse(String field, MinMax minMax, Sorter sorter, NullPolicyEnum nullPolicy, HintEnum hint, long size,
    boolean collectElevatedDocsWhenCollapsing) {
    this.field = field;
    this.minMax = minMax;
    this.sorter = sorter;
    this.nullPolicy = nullPolicy;
    this.hint = hint;
    this.size = size;
    this.collectElevatedDocsWhenCollapsing = collectElevatedDocsWhenCollapsing;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public MinMax getMinMax() {
    return minMax;
  }

  public void setMinMax(MinMax minMax) {
    this.minMax = minMax;
  }

  public Sorter getSorter() {
    return sorter;
  }

  public void setSorter(Sorter sorter) {
    this.sorter = sorter;
  }

  public NullPolicyEnum getNullPolicy() {
    return nullPolicy;
  }

  public void setNullPolicy(NullPolicyEnum nullPolicy) {
    this.nullPolicy = nullPolicy;
  }

  public HintEnum getHint() {
    return hint;
  }

  public void setHint(HintEnum hint) {
    this.hint = hint;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public boolean isCollectElevatedDocsWhenCollapsing() {
    return collectElevatedDocsWhenCollapsing;
  }

  public void setCollectElevatedDocsWhenCollapsing(boolean collectElevatedDocsWhenCollapsing) {
    this.collectElevatedDocsWhenCollapsing = collectElevatedDocsWhenCollapsing;
  }
}
