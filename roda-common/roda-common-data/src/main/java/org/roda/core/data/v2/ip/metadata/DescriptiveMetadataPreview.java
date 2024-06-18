package org.roda.core.data.v2.ip.metadata;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Carlos Afonso <cafonso@keep.pt>
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class DescriptiveMetadataPreview implements Serializable {

  @Serial
  private static final long serialVersionUID = 1868606330058572708L;
  private String preview;

  public DescriptiveMetadataPreview(String preview) {
    this.preview = preview;
  }

  public DescriptiveMetadataPreview() {
  }

  public String getPreview() {
    return preview;
  }

  public void setPreview(String preview) {
    this.preview = preview;
  }

  @Override
  public String toString() {
    return "DescriptiveMetadataPreview{" + "preview='" + preview + '\'' + '}';
  }
}
