package org.roda.wui.client.browse.bundle;

import java.io.Serializable;

import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;

public class DipBundle implements Serializable {

  private static final long serialVersionUID = 1L;

  private IndexedAIP aip;
  private IndexedRepresentation representation;
  private IndexedFile file;

  private IndexedDIP dip;
  private DIPFile dipFile;

  public DipBundle() {
    super();
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

  public IndexedDIP getDip() {
    return dip;
  }

  public void setDip(IndexedDIP dip) {
    this.dip = dip;
  }

  public DIPFile getDipFile() {
    return dipFile;
  }

  public void setDipFile(DIPFile dipFile) {
    this.dipFile = dipFile;
  }

}
