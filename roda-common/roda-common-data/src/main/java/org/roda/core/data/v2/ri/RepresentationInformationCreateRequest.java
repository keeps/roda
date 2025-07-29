/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.ri;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RepresentationInformationCreateRequest implements Serializable {
  @Serial
  private static final long serialVersionUID = -3160826080550756292L;

  private RepresentationInformation representationInformation;
  private RepresentationInformationCustomForm form;

  public RepresentationInformationCreateRequest() {
    // empty constructor
  }

  public RepresentationInformation getRepresentationInformation() {
    return representationInformation;
  }

  public void setRepresentationInformation(RepresentationInformation representationInformation) {
    this.representationInformation = representationInformation;
  }

  public RepresentationInformationCustomForm getForm() {
    return form;
  }

  public void setForm(RepresentationInformationCustomForm form) {
    this.form = form;
  }

  @Override
  public String toString() {
    return "RepresentationInformationCreateRequest{" +
        "representationInformation=" + representationInformation +
        ", form=" + form +
        '}';
  }
}
