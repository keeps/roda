package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.io.Serializable;
import java.util.List;

/**
 * @author sleroux
 *
 */
public class PreservationMetadataBundle implements Serializable {

  private static final long serialVersionUID = 515251862250083594L;

  private List<RepresentationPreservationMetadataBundle> representationsMetadata;
  

  public PreservationMetadataBundle() {
    super();
  }


  public PreservationMetadataBundle(List<RepresentationPreservationMetadataBundle> representationsMetadata) {
    super();
    this.representationsMetadata = representationsMetadata;
  }


  public List<RepresentationPreservationMetadataBundle> getRepresentationsMetadata() {
    return representationsMetadata;
  }


  public void setRepresentationsMetadata(List<RepresentationPreservationMetadataBundle> representationsMetadata) {
    this.representationsMetadata = representationsMetadata;
  }
  
}
