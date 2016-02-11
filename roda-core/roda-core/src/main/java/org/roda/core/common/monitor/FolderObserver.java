/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common.monitor;

import java.nio.file.Path;

import org.roda.core.data.v2.ip.TransferredResource;

public interface FolderObserver {

  public void transferredResourceAdded(TransferredResource resource);

  public void transferredResourceModified(TransferredResource resource);

  public void transferredResourceDeleted(TransferredResource resource, boolean forceCommit);

  public void pathDeleted(Path deleted);

}
