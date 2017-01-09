package org.roda.wui.client.browse;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.client.common.lists.DIPFileList;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

public class DipFilePreview extends BitstreamPreview<DIPFile> {

  private static final FileFormat NO_FORMAT = null;

  public DipFilePreview(Viewers viewers, DIPFile dipFile) {
    super(viewers, RestUtils.createDipFileDownloadUri(dipFile.getUUID()), NO_FORMAT, dipFile.getId(), dipFile.getSize(),
      dipFile.isDirectory(), dipFile);
  }

  public DipFilePreview(Viewers viewers, DIPFile dipFile, Command onPreviewFailure) {
    super(viewers, RestUtils.createDipFileDownloadUri(dipFile.getUUID()), NO_FORMAT, dipFile.getId(), dipFile.getSize(),
      dipFile.isDirectory(), onPreviewFailure, dipFile);
  }

  @Override
  protected Widget directoryPreview() {
    final Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.DIPFILE_PARENT_UUID, getObject().getUUID()));
    // TODO add summary
    final DIPFileList folderList = new DIPFileList(filter, Facets.NONE, "", false);

    folderList.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        DIPFile selectedDipFile = folderList.getSelectionModel().getSelectedObject();
        if (selectedDipFile != null) {
          HistoryUtils.openBrowse(selectedDipFile);
        }
      }
    });

    return folderList;
  }

}
