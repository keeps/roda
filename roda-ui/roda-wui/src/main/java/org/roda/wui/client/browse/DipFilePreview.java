package org.roda.wui.client.browse;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.client.common.lists.DIPFileList;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import config.i18n.client.ClientMessages;

public class DipFilePreview extends BitstreamPreview<DIPFile> {

  private static final FileFormat NO_FORMAT = null;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private final IndexedAIP refererAIP;
  private final IndexedRepresentation refererRepresentation;
  private final IndexedFile refererFile;

  public DipFilePreview(Viewers viewers, DIPFile dipFile, IndexedAIP refererAIP,
    IndexedRepresentation refererRepresentation, IndexedFile refererFile) {
    super(viewers, RestUtils.createDipFileDownloadUri(dipFile.getUUID()), NO_FORMAT, dipFile.getId(), dipFile.getSize(),
      dipFile.isDirectory(), dipFile);
    this.refererAIP = refererAIP;
    this.refererRepresentation = refererRepresentation;
    this.refererFile = refererFile;
  }

  public DipFilePreview(Viewers viewers, DIPFile dipFile, IndexedAIP refererAIP,
    IndexedRepresentation refererRepresentation, IndexedFile refererFile, Command onPreviewFailure) {
    super(viewers, RestUtils.createDipFileDownloadUri(dipFile.getUUID()), NO_FORMAT, dipFile.getId(), dipFile.getSize(),
      dipFile.isDirectory(), onPreviewFailure, dipFile);
    this.refererAIP = refererAIP;
    this.refererRepresentation = refererRepresentation;
    this.refererFile = refererFile;
  }

  @Override
  protected Widget directoryPreview() {
    final Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.DIPFILE_PARENT_UUID, getObject().getUUID()));

    final FlowPanel layout = new FlowPanel();

    final SearchPanel dipFileSearch = new SearchPanel(filter, RodaConstants.DIPFILE_SEARCH, true,
      messages.searchPlaceHolder(), false, false, true);

    // TODO add summary
    final DIPFileList folderList = new DIPFileList(filter, Facets.NONE, "", false);
    dipFileSearch.setList(folderList);

    layout.add(dipFileSearch);
    layout.add(folderList);

    folderList.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        DIPFile selectedDipFile = folderList.getSelectionModel().getSelectedObject();
        int selectedDipFileIndex = folderList.getIndexOfVisibleObject(selectedDipFile);

        if (selectedDipFile != null) {
          HistoryUtils.openBrowse(selectedDipFile, folderList.getSorter(), selectedDipFileIndex, refererAIP,
            refererRepresentation, refererFile);
        }
      }
    });

    return layout;
  }

}
