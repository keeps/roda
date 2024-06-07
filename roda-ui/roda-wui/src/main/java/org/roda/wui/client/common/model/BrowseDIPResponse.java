package org.roda.wui.client.common.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class BrowseDIPResponse implements Serializable {

  @Serial
  private static final long serialVersionUID = -1643255887537720719L;

  private IndexedDIP dip;
  private DIPFile dipFile;
  private IsIndexed referred;
  private Permissions permissions;
  private List<DIPFile> dipFileAncestors;
  private IndexedRepresentation indexedRepresentation;
  private IndexedAIP indexedAIP;
  private IndexedFile indexedFile;
  private boolean embeddedDIP;

  public BrowseDIPResponse() {
    this.dipFileAncestors = new ArrayList<>();
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

  public IsIndexed getReferred() {
    return referred;
  }

  public void setReferred(IsIndexed referred) {
    this.referred = referred;
  }

  public Permissions getPermissions() {
    return permissions;
  }

  public void setPermissions(Permissions permissions) {
    this.permissions = permissions;
  }

  public List<DIPFile> getDipFileAncestors() {
    return dipFileAncestors;
  }

  public void setDipFileAncestors(List<DIPFile> dipFileAncestors) {
    this.dipFileAncestors = dipFileAncestors;
  }

  public IndexedRepresentation getIndexedRepresentation() {
    return indexedRepresentation;
  }

  public void setIndexedRepresentation(IndexedRepresentation indexedRepresentation) {
    this.indexedRepresentation = indexedRepresentation;
  }

  public IndexedAIP getIndexedAIP() {
    return indexedAIP;
  }

  public void setIndexedAIP(IndexedAIP indexedAIP) {
    this.indexedAIP = indexedAIP;
  }

  public IndexedFile getIndexedFile() {
    return indexedFile;
  }

  public void setIndexedFile(IndexedFile indexedFile) {
    this.indexedFile = indexedFile;
  }

  public boolean isEmbeddedDIP() {
    return embeddedDIP;
  }

  public void setEmbeddedDIP(boolean embeddedDIP) {
    this.embeddedDIP = embeddedDIP;
  }
}
