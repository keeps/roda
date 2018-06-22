/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.wui.client.common.lists.SimpleFileList;
import org.roda.wui.client.common.search.SearchFilters;

import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

public class SelectFileDialog extends DefaultSelectDialog<IndexedFile, Void> {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Filter DEFAULT_FILTER_FILE = SearchFilters.defaultFilter(IndexedFile.class.getName());

  private static final Boolean DEFAULT_JUST_ACTIVE = Boolean.TRUE;
  private static final Boolean SELECTABLE = Boolean.FALSE;

  public SelectFileDialog(String title, boolean hidePreFilters) {
    this(title, DEFAULT_FILTER_FILE, DEFAULT_JUST_ACTIVE, hidePreFilters);
  }

  public SelectFileDialog(String title, Filter filter, boolean justActive, boolean hidePreFilters) {
    this(title, filter, justActive, hidePreFilters, SELECTABLE);
  }

  public SelectFileDialog(String title, Filter filter, boolean justActive, boolean hidePreFilters, boolean selectable) {
    super(title, filter, RodaConstants.FILE_SEARCH,
      new SimpleFileList("SelectFileDialog_simpleFiles", filter, justActive, messages.selectFileSearchResults(),
        selectable),
      hidePreFilters);
  }
}
