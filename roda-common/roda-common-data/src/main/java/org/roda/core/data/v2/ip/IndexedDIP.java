/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ip;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@XmlRootElement(name = "dip")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndexedDIP extends DIP implements IsIndexed, HasPermissions {

  private static final long serialVersionUID = 4188689893034771594L;
  private String openExternalURL = "";

  public IndexedDIP() {
    super();
  }

  public IndexedDIP(IndexedDIP dip) {
    super(dip);
    this.openExternalURL = dip.getOpenExternalURL();
  }

  public String getOpenExternalURL() {
    return openExternalURL;
  }

  public void setOpenExternalURL(String openExternalURL) {
    this.openExternalURL = openExternalURL;
  }

  @JsonIgnore
  @Override
  public String getUUID() {
    return getId();
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return "IndexedDIP [super=" + super.toString() + ", openExternalURL=" + openExternalURL + "]";
  }

  @Override
  public List<String> toCsvHeaders() {
    return Arrays.asList("id", "title", "description", "type", "dateCreated", "lastModified", "isPermanent",
      "properties", "aipIds", "representationIds", "fileIds", "permissions", "openExternalURL");
  }

  @Override
  public List<Object> toCsvValues() {
    return Arrays.asList(super.getId(), super.getTitle(), super.getDescription(), super.getType(),
      super.getDateCreated(), super.getLastModified(), super.getIsPermanent(), super.getProperties(), super.getAipIds(),
      super.getRepresentationIds(), super.getFileIds(), super.getPermissions(), openExternalURL);
  }

  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.INDEX_UUID);
  }

}
