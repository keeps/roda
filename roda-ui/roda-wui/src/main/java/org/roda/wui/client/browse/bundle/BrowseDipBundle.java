/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse.bundle;

import java.util.List;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.Permissions;

public class BrowseDipBundle implements Bundle {

  private static final long serialVersionUID = 1L;

  private IsIndexed referrer;
  private Bundle referrerBundle;

  private IndexedDIP dip;

  private DIPFile dipFile;
  private List<DIPFile> dipFileAncestors;

  private Permissions referrerPermissions;

  public BrowseDipBundle() {
    super();
  }

  public IsIndexed getReferrer() {
    return referrer;
  }

  public void setReferrer(IsIndexed referrer) {
    this.referrer = referrer;
  }

  public Bundle getReferrerBundle() {
    return referrerBundle;
  }

  public void setReferrerBundle(Bundle referrerBundle) {
    this.referrerBundle = referrerBundle;
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

  public List<DIPFile> getDipFileAncestors() {
    return dipFileAncestors;
  }

  public void setDipFileAncestors(List<DIPFile> dipFileAncestors) {
    this.dipFileAncestors = dipFileAncestors;
  }

  public Permissions getReferrerPermissions() {
    return referrerPermissions;
  }

  public void setReferrerPermissions(Permissions referrerPermissions) {
    this.referrerPermissions = referrerPermissions;
  }
}
