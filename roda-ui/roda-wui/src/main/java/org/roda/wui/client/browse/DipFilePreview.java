/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.client.common.lists.DIPFileList;
import org.roda.wui.client.common.lists.pagination.ListSelectionUtils;
import org.roda.wui.client.common.search.SearchPanel;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class DipFilePreview extends BitstreamPreview<DIPFile> {

  private static final FileFormat NO_FORMAT = null;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

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

    final FlowPanel layout = new FlowPanel();

    final SearchPanel dipFileSearch = new SearchPanel(filter, RodaConstants.DIPFILE_SEARCH, true,
      messages.searchPlaceHolder(), false, false, true);

    final DIPFileList folderList = new DIPFileList(filter, Facets.NONE, messages.allOfAObject(DIPFile.class.getName()),
      false);
    dipFileSearch.setList(folderList);

    layout.add(dipFileSearch);
    layout.add(folderList);

    ListSelectionUtils.bindBrowseOpener(folderList);

    return layout;
  }

}
