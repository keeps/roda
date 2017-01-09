package org.roda.wui.client.browse;

import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.user.client.Command;

public class IndexedFilePreview extends BitstreamPreview<IndexedFile> {

  public IndexedFilePreview(Viewers viewers, IndexedFile file) {
    super(viewers, RestUtils.createRepresentationFileDownloadUri(file.getUUID()), file.getFileFormat(),
      file.getOriginalName() != null ? file.getOriginalName() : file.getId(), file.getSize(), file.isDirectory(), file);
  }

  public IndexedFilePreview(Viewers viewers, IndexedFile file, Command onPreviewFailure) {
    super(viewers, RestUtils.createRepresentationFileDownloadUri(file.getUUID()), file.getFileFormat(),
      file.getOriginalName() != null ? file.getOriginalName() : file.getId(), file.getSize(), file.isDirectory(),
      onPreviewFailure, file);
  }

}
