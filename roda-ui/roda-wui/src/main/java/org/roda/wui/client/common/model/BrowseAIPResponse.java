package org.roda.wui.client.common.model;

import org.roda.core.data.v2.ip.IndexedAIP;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class BrowseAIPResponse implements Serializable {

  @Serial
  private static final long serialVersionUID = 7702752004859207610L;

  private IndexedAIP indexedAIP;
  private List<String> representationInformationFields;

  public BrowseAIPResponse() {
    // empty constructor
  }

  public IndexedAIP getIndexedAIP() {
    return indexedAIP;
  }

  public void setIndexedAIP(IndexedAIP indexedAIP) {
    this.indexedAIP = indexedAIP;
  }

  public List<String> getRepresentationInformationFields() {
    return representationInformationFields;
  }

  public void setRepresentationInformationFields(List<String> representationInformationFields) {
    this.representationInformationFields = representationInformationFields;
  }
}
