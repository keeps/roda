/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.dialogs;

import org.roda.core.data.adapter.filter.BasicSearchFilterParameter;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.lists.AIPList;

import com.google.gwt.core.client.GWT;

import config.i18n.client.BrowseMessages;

public class SelectAipDialog extends DefaultSelectDialog<IndexedAIP, Void> {

  private static final BrowseMessages messages = GWT.create(BrowseMessages.class);

  private static final Filter DEFAULT_FILTER_AIP = new Filter(
    new BasicSearchFilterParameter(RodaConstants.AIP_SEARCH, "*"));

  private static final Boolean SHOW_INACTIVE = Boolean.FALSE;

  public SelectAipDialog(String title) {
    this(title, DEFAULT_FILTER_AIP, SHOW_INACTIVE);
  }

  public SelectAipDialog(String title, Filter filter, boolean justActive) {
    super(title, filter, RodaConstants.AIP_SEARCH,
      new AIPList(filter, justActive, null, messages.selectAipSearchResults(), false));

  }
}
