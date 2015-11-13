/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

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
