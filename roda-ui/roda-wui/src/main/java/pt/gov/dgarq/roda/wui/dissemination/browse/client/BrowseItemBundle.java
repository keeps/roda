package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.io.Serializable;
import java.util.List;

import pt.gov.dgarq.roda.core.data.v2.Representation;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;

public class BrowseItemBundle implements Serializable {

  private static final long serialVersionUID = 7901536603462531124L;

  private SimpleDescriptionObject sdo;
  private List<SimpleDescriptionObject> sdoAncestors;
  private List<DescriptiveMetadataViewBundle> descriptiveMetadata;
  private PreservationMetadataBundle preservationMetadata;
  private List<Representation> representations;

  public BrowseItemBundle() {
    super();

  }

  public BrowseItemBundle(SimpleDescriptionObject sdo, List<SimpleDescriptionObject> sdoAncestors,
    List<DescriptiveMetadataViewBundle> descriptiveMetadata, PreservationMetadataBundle preservationMetadata,
    List<Representation> representations) {
    super();
    this.sdo = sdo;
    this.setSdoAncestors(sdoAncestors);
    this.descriptiveMetadata = descriptiveMetadata;
    this.preservationMetadata = preservationMetadata;
    this.representations = representations;
  }

  public SimpleDescriptionObject getSdo() {
    return sdo;
  }

  public void setSdo(SimpleDescriptionObject sdo) {
    this.sdo = sdo;
  }

  public List<DescriptiveMetadataViewBundle> getDescriptiveMetadata() {
    return descriptiveMetadata;
  }

  public void setDescriptiveMetadata(List<DescriptiveMetadataViewBundle> descriptiveMetadata) {
    this.descriptiveMetadata = descriptiveMetadata;
  }

  public PreservationMetadataBundle getPreservationMetadata() {
    return preservationMetadata;
  }

  public void setPreservationMetadata(PreservationMetadataBundle preservationMetadata) {
    this.preservationMetadata = preservationMetadata;
  }

  public List<Representation> getRepresentations() {
    return representations;
  }

  public void setRepresentations(List<Representation> representations) {
    this.representations = representations;
  }

  public List<SimpleDescriptionObject> getSdoAncestors() {
    return sdoAncestors;
  }

  public void setSdoAncestors(List<SimpleDescriptionObject> sdoAncestors) {
    this.sdoAncestors = sdoAncestors;
  }

}
