/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import java.util.Collections;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.FileActions;
import org.roda.wui.client.common.lists.SearchFileList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class IndexedFilePreview extends BitstreamPreview<IndexedFile> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final boolean CONTENT_DISPOSITION_INLINE = true;

  private SearchWrapper searchWrapper = null;
  private boolean justActive;
  private Permissions permissions;

  public IndexedFilePreview(Viewers viewers, IndexedFile file, boolean justActive, Permissions permissions, Command onPreviewFailure) {
    super(viewers, RestUtils.createRepresentationFileDownloadUri(file.getUUID(), CONTENT_DISPOSITION_INLINE),
      file.getFileFormat(), file.getOriginalName() != null ? file.getOriginalName() : file.getId(), file.getSize(),
      file.isDirectory(), onPreviewFailure, file);
    this.justActive = justActive;
    this.permissions = permissions;
  }

  @Override
  protected Widget directoryPreview() {
    Filter filter = new Filter(new SimpleFilterParameter(RodaConstants.FILE_PARENT_UUID, getObject().getUUID()));

    ListBuilder<IndexedFile> folderListBuilder = new ListBuilder<>(SearchFileList::new,
      new AsyncTableCellOptions<>(IndexedFile.class, "IndexedFilePreview_files").withFilter(filter)
        .withSummary(messages.representationListOfFiles()).withJustActive(justActive).bindOpener());

    LastSelectedItemsSingleton.getInstance().setSelectedJustActive(justActive);

    searchWrapper = new SearchWrapper(false).createListAndSearchPanel(folderListBuilder,
      FileActions.get(getObject().getAipId(), getObject().getRepresentationId(), permissions), messages.searchPlaceHolder());
    return searchWrapper;
  }

  public SelectedItems<IndexedFile> getSelected() {

    SelectedItems<IndexedFile> ret = SelectedItemsList.create(IndexedFile.class,
      Collections.singletonList(getObject().getUUID()));
    if (searchWrapper != null) {
      SelectedItems<IndexedFile> listSelected = searchWrapper.getSelectedItems(IndexedFile.class);

      if (!ClientSelectedItemsUtils.isEmpty(listSelected)) {
        ret = listSelected;
      }
    }
    return ret;
  }

  public void refresh() {
    if (searchWrapper != null) {
      searchWrapper.refreshCurrentList();
    }
  }

}
