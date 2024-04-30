package org.roda.wui.client.common.model;

import org.roda.core.data.v2.generics.LongResponse;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class CountRIResponse {

  private LongResponse aipsCount;
  private LongResponse representationsCount;
  private LongResponse filesCount;

  public CountRIResponse() {
  }

  public LongResponse getAipsCount() {
    return aipsCount;
  }

  public void setAipsCount(LongResponse aipsCount) {
    this.aipsCount = aipsCount;
  }

  public LongResponse getRepresentationsCount() {
    return representationsCount;
  }

  public void setRepresentationsCount(LongResponse representationsCount) {
    this.representationsCount = representationsCount;
  }

  public LongResponse getFilesCount() {
    return filesCount;
  }

  public void setFilesCount(LongResponse filesCount) {
    this.filesCount = filesCount;
  }
}
