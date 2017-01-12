package org.roda.wui.client.browse;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.wui.client.common.lists.SearchFileList;
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

public class IndexedFilePreview extends BitstreamPreview<IndexedFile> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public IndexedFilePreview(Viewers viewers, IndexedFile file) {
    super(viewers, RestUtils.createRepresentationFileDownloadUri(file.getUUID()), file.getFileFormat(),
      file.getOriginalName() != null ? file.getOriginalName() : file.getId(), file.getSize(), file.isDirectory(), file);
  }

  public IndexedFilePreview(Viewers viewers, IndexedFile file, Command onPreviewFailure) {
    super(viewers, RestUtils.createRepresentationFileDownloadUri(file.getUUID()), file.getFileFormat(),
      file.getOriginalName() != null ? file.getOriginalName() : file.getId(), file.getSize(), file.isDirectory(),
      onPreviewFailure, file);
  }

  @Override
  protected Widget directoryPreview() {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_PARENT_UUID, getObject().getUUID()));

    final FlowPanel layout = new FlowPanel();

    final SearchPanel fileSearch = new SearchPanel(filter, RodaConstants.FILE_SEARCH, messages.searchPlaceHolder(),
      false, false, true);

    boolean justActive = true;
    boolean selectable = false;
    boolean showFilesPath = false;
    final SearchFileList folderList = new SearchFileList(filter, justActive, Facets.NONE,
      messages.representationListOfFiles(), selectable, showFilesPath);

    fileSearch.setList(folderList);
    fileSearch.setDefaultFilter(filter);
    fileSearch.setDefaultFilterIncremental(true);

    layout.add(fileSearch);
    layout.add(folderList);

    folderList.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedFile selectedFile = folderList.getSelectionModel().getSelectedObject();
        int selectedFileIndex = folderList.getIndexOfVisibleObject(selectedFile);

        if (selectedFile != null) {
          HistoryUtils.openBrowse(selectedFile, folderList.getSorter(), selectedFileIndex);
        }
      }
    });

    return layout;
  }

}
