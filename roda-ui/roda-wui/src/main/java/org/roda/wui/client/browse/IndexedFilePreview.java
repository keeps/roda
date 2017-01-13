package org.roda.wui.client.browse;

import java.util.Arrays;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
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
  private SearchFileList folderList = null;

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
    boolean selectable = true;
    boolean showFilesPath = false;

    final SearchFileList list = new SearchFileList(filter, justActive, Facets.NONE,
      messages.representationListOfFiles(), selectable, showFilesPath);
    this.folderList = list;

    fileSearch.setList(list);
    fileSearch.setDefaultFilter(filter);
    fileSearch.setDefaultFilterIncremental(true);

    layout.add(fileSearch);
    layout.add(list);

    folderList.getSelectionModel().addSelectionChangeHandler(new Handler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        IndexedFile selectedFile = list.getSelectionModel().getSelectedObject();
        int selectedFileIndex = list.getIndexOfVisibleObject(selectedFile);

        if (selectedFile != null) {
          HistoryUtils.openBrowse(selectedFile, list.getSorter(), selectedFileIndex);
        }
      }
    });

    return layout;
  }

  public SelectedItems<IndexedFile> getSelected() {

    SelectedItems<IndexedFile> ret = SelectedItemsList.create(IndexedFile.class, Arrays.asList(getObject().getUUID()));
    if (folderList != null) {
      SelectedItems<IndexedFile> listSelected = folderList.getSelected();

      if (!ClientSelectedItemsUtils.isEmpty(listSelected)) {
        ret = listSelected;
      }
    }
    return ret;
  }

  public void refresh() {
    if (folderList != null) {
      folderList.refresh();
    }
  }

}
