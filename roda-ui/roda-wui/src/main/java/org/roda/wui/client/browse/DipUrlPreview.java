/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.user.client.Command;

public class DipUrlPreview extends BitstreamPreview<IndexedDIP> {

  private static final FileFormat NO_FORMAT = null;

  public DipUrlPreview(Viewers viewers, IndexedDIP dip) {
    super(viewers, RestUtils.createDipDownloadUri(dip.getId()), NO_FORMAT, dip.getId(), 0L, false, dip);
  }

  public DipUrlPreview(Viewers viewers, IndexedDIP dip, Command onPreviewFailure) {
    super(viewers, RestUtils.createDipDownloadUri(dip.getId()), NO_FORMAT, dip.getId(), 0L, false, onPreviewFailure,
      dip);
  }
}
