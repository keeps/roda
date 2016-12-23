/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.bundle;

import java.io.Serializable;
import java.util.List;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;

public class BrowseFileBundle implements Serializable {

  private static final long serialVersionUID = 7901536603462531124L;

  private List<IndexedAIP> aipAncestors;
  private IndexedAIP aip;
  private IndexedRepresentation representation;
  private IndexedFile file;

  public BrowseFileBundle() {
    super();
  }

  public BrowseFileBundle(List<IndexedAIP> aipAncestors, IndexedAIP aip, IndexedRepresentation representation,
    IndexedFile file) {
    super();
    this.aipAncestors = aipAncestors;
    this.aip = aip;
    this.representation = representation;
    this.file = file;
  }

  public List<IndexedAIP> getAipAncestors() {
    return aipAncestors;
  }

  public void setAipAncestors(List<IndexedAIP> aipAncestors) {
    this.aipAncestors = aipAncestors;
  }

  public IndexedAIP getAip() {
    return aip;
  }

  public void setAip(IndexedAIP aip) {
    this.aip = aip;
  }

  public IndexedRepresentation getRepresentation() {
    return representation;
  }

  public void setRepresentation(IndexedRepresentation representation) {
    this.representation = representation;
  }

  public IndexedFile getFile() {
    return file;
  }

  public void setFile(IndexedFile file) {
    this.file = file;
  }

}
