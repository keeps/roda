package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.io.Serializable;

public class DescriptiveMetadataBundle implements Serializable {

  private static final long serialVersionUID = 515251862250083594L;

  private String id;
  private String html;
  private Long sizeInBytes;

  public DescriptiveMetadataBundle() {
    super();
  }

  public DescriptiveMetadataBundle(String id, String html, Long sizeInBytes) {
    super();
    this.id = id;
    this.html = html;
    this.sizeInBytes = sizeInBytes;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getHtml() {
    return html;
  }

  public void setHtml(String html) {
    this.html = html;
  }

  public Long getSizeInBytes() {
    return sizeInBytes;
  }

  public void setSizeInBytes(Long sizeInBytes) {
    this.sizeInBytes = sizeInBytes;
  }

}
