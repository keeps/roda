package org.roda.wui.client.browse;

import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.user.client.Command;

public class DipFilePreview extends BitstreamPreview {

  private static final FileFormat NO_FORMAT = null;

  public DipFilePreview(Viewers viewers, DIPFile dipFile) {
    super(viewers, RestUtils.createDipFileDownloadUri(dipFile.getUUID()), NO_FORMAT, dipFile.getId(), dipFile.getSize(),
      dipFile.isDirectory());
  }

  public DipFilePreview(Viewers viewers, DIPFile dipFile, Command onPreviewFailure) {
    super(viewers, RestUtils.createDipFileDownloadUri(dipFile.getUUID()), NO_FORMAT, dipFile.getId(), dipFile.getSize(),
      dipFile.isDirectory(), onPreviewFailure);
  }

}
