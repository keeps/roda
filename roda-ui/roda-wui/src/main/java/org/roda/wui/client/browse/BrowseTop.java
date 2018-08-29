/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.client.browse;

import java.util.Arrays;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.EmptyKeyFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.lists.AIPList;
import org.roda.wui.client.common.lists.utils.AsyncTableCellOptions;
import org.roda.wui.client.common.lists.utils.ListBuilder;
import org.roda.wui.client.common.search.SearchWrapper;
import org.roda.wui.client.ingest.transfer.TransferUpload;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.DescriptionLevelUtils;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Luis Faria
 * 
 */
public class BrowseTop extends Composite {
  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, AsyncCallback<Widget> callback) {
      if (historyTokens.isEmpty()) {
        if (instance == null) {
          instance = new BrowseTop();
        }
        callback.onSuccess(instance);
      } else if (historyTokens.size() == 1
        && !historyTokens.get(0).equals(EditPermissions.AIP_RESOLVER.getHistoryToken())) {
        BrowseAIP.RESOLVER.resolve(historyTokens, callback);
      } else if (historyTokens.size() > 1
        && historyTokens.get(0).equals(EditDescriptiveMetadata.RESOLVER.getHistoryToken())) {
        EditDescriptiveMetadata.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.size() > 1
        && historyTokens.get(0).equals(CreateDescriptiveMetadata.RESOLVER.getHistoryToken())) {
        CreateDescriptiveMetadata.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.size() > 1 && historyTokens.get(0).equals(BrowseFile.RESOLVER.getHistoryToken())) {
        BrowseFile.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.size() > 1 && historyTokens.get(0).equals(BrowseDIP.RESOLVER.getHistoryToken())) {
        BrowseDIP.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.size() > 1
        && historyTokens.get(0).equals(PreservationEvents.BROWSE_RESOLVER.getHistoryToken())) {
        PreservationEvents.BROWSE_RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.size() > 1
        && historyTokens.get(0).equals(DescriptiveMetadataHistory.RESOLVER.getHistoryToken())) {
        DescriptiveMetadataHistory.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(EditPermissions.AIP_RESOLVER.getHistoryToken())) {
        EditPermissions.AIP_RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(EditPermissions.DIP_RESOLVER.getHistoryToken())) {
        EditPermissions.DIP_RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.size() > 1
        && historyTokens.get(0).equals(BrowseRepresentation.RESOLVER.getHistoryToken())) {
        BrowseRepresentation.RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else if (historyTokens.get(0).equals(TransferUpload.BROWSE_FILE_RESOLVER.getHistoryToken())) {
        TransferUpload.BROWSE_FILE_RESOLVER.resolve(HistoryUtils.tail(historyTokens), callback);
      } else {
        HistoryUtils.newHistory(RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRole(this, callback);
    }

    @Override
    public String getHistoryToken() {
      return "browse";
    }

    @Override
    public List<String> getHistoryPath() {
      return Arrays.asList(getHistoryToken());
    }
  };

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  interface MyUiBinder extends UiBinder<Widget, BrowseTop> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static BrowseTop instance = null;


  @UiField
  FlowPanel browseDescription;

  @UiField(provided = true)
  SearchWrapper search;
  @UiField
  HTML itemIcon;
  @UiField
  Label itemTitle;

  private BrowseTop() {
    // AIP LIST, it has the same id as the AIP children list because facets should
    // be the same

    ListBuilder<IndexedAIP> listBuilder = new ListBuilder<>(AIPList::new,
      new AsyncTableCellOptions<>(IndexedAIP.class, "BrowseTop_aip")
        .withFilter(new Filter(new EmptyKeyFilterParameter(RodaConstants.AIP_PARENT_ID))).withJustActive(true)
        .withSummary(messages.listOfAIPs()).bindOpener());

    search = new SearchWrapper(false).createListAndSearchPanel(listBuilder, AipActions.get());

    // INIT
    initWidget(uiBinder.createAndBindUi(this));

    // HEADER
    itemIcon.setHTML(DescriptionLevelUtils.getTopIconSafeHtml());
    itemTitle.setText(messages.allCollectionsTitle());

    browseDescription.add(new HTMLWidgetWrapper("BrowseDescription.html"));

    // CSS
    this.addStyleName("browse browse_top");

    // make FocusPanel comply with WCAG
    Element firstElement = this.getElement().getFirstChildElement();
    if ("input".equalsIgnoreCase(firstElement.getTagName())) {
      firstElement.setAttribute("title", "browse input");
    }

  }
}
