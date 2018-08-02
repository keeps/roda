/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.browse;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.metadata.FileFormat;
import org.roda.wui.client.common.lists.DIPFileList;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.common.client.tools.RestUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class DipFilePreview extends BitstreamPreview<DIPFile> {

  private static final boolean CONTENT_DISPOSITION_INLINE = true;

  private static final FileFormat NO_FORMAT = null;
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public DipFilePreview(Viewers viewers, DIPFile dipFile) {
    super(viewers, RestUtils.createDipFileDownloadUri(dipFile.getUUID(), CONTENT_DISPOSITION_INLINE), NO_FORMAT,
      dipFile.getId(), dipFile.getSize(), dipFile.isDirectory(), dipFile);
  }

  public DipFilePreview(Viewers viewers, DIPFile dipFile, Command onPreviewFailure) {
    super(viewers, RestUtils.createDipFileDownloadUri(dipFile.getUUID(), CONTENT_DISPOSITION_INLINE), NO_FORMAT,
      dipFile.getId(), dipFile.getSize(), dipFile.isDirectory(), onPreviewFailure, dipFile);
  }

  @Override
  protected Widget directoryPreview() {
    final Filter filter = new Filter(
      new SimpleFilterParameter(RodaConstants.DIPFILE_PARENT_UUID, getObject().getUUID()));

    ListBuilder<DIPFile> dipFileListBuilder = new ListBuilder<>(DIPFileList::new,
      new AsyncTableCell.Options<>(DIPFile.class, "DipFilePreview_files").withFilter(filter)
        .withSummary(messages.allOfAObject(DIPFile.class.getName())).bindOpener());

    return new SearchWrapper(false).createListAndSearchPanel(dipFileListBuilder);
  }

}
