/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.util.Arrays;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.FileActions;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class IndexedFilePreview extends BitstreamPreview<IndexedFile> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final boolean CONTENT_DISPOSITION_INLINE = true;

  private SearchFileList folderList = null;
  private boolean justActive = true;

  public IndexedFilePreview(Viewers viewers, IndexedFile file, boolean justActive, Command onPreviewFailure) {
    super(viewers, RestUtils.createRepresentationFileDownloadUri(file.getUUID(), CONTENT_DISPOSITION_INLINE), file.getFileFormat(),
      file.getOriginalName() != null ? file.getOriginalName() : file.getId(), file.getSize(), file.isDirectory(),
      onPreviewFailure, file);
    this.justActive = justActive;
  }

  @Override
  protected Widget directoryPreview() {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_PARENT_UUID, getObject().getUUID()));

    final FlowPanel layout = new FlowPanel();

    final SearchPanel fileSearch = new SearchPanel(filter, RodaConstants.FILE_SEARCH, true,
      messages.searchPlaceHolder(), false, false, true);

    boolean selectable = true;
    boolean showFilesPath = false;

    this.folderList = new SearchFileList(filter, justActive, Facets.NONE, messages.representationListOfFiles(),
      selectable, showFilesPath);

    LastSelectedItemsSingleton.getInstance().setSelectedJustActive(justActive);
    this.folderList.setActionable(FileActions.get(getObject().getAipId(), getObject().getRepresentationId()));

    fileSearch.setList(folderList);

    layout.add(fileSearch);
    layout.add(folderList);

    ListSelectionUtils.bindBrowseOpener(folderList);

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
