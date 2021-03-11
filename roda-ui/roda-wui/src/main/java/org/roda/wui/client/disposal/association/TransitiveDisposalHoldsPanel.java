/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.association;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHolds;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalTransitiveHoldAIPMetadata;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.search.Search;
import org.roda.wui.common.client.tools.HistoryUtils;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TransitiveDisposalHoldsPanel extends Composite {
  interface MyUiBinder extends UiBinder<Widget, TransitiveDisposalHoldsPanel> {
  }

  private static TransitiveDisposalHoldsPanel.MyUiBinder uiBinder = GWT
    .create(TransitiveDisposalHoldsPanel.MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel transitiveDisposalHoldsPanel;

  @UiField
  FlowPanel panelBody;

  @UiField
  FlowPanel panel;

  private final IndexedAIP indexedAip;
  private final DisposalHolds disposalHoldList = new DisposalHolds();

  public TransitiveDisposalHoldsPanel(IndexedAIP indexedAip) {
    initWidget(uiBinder.createAndBindUi(this));
    this.indexedAip = indexedAip;

    BrowserService.Util.getInstance().listTransitiveDisposalHolds(indexedAip.getId(),
      new NoAsyncCallback<List<DisposalTransitiveHoldAIPMetadata>>() {
        @Override
        public void onSuccess(List<DisposalTransitiveHoldAIPMetadata> transitiveDisposalHolds) {
          init(indexedAip.getDisposalConfirmationId() != null, transitiveDisposalHolds);
        }
      });
  }

  private void init(boolean onDisposalConfirmation, List<DisposalTransitiveHoldAIPMetadata> transitiveDisposalHolds) {
    if (transitiveDisposalHolds.isEmpty()) {
      transitiveDisposalHoldsPanel.remove(panel);
    } else {
      createTransitiveDisposalHoldsTable(transitiveDisposalHolds);
    }
  }

  private void createTransitiveDisposalHoldsTable(List<DisposalTransitiveHoldAIPMetadata> transitiveDisposalHolds) {
    for (DisposalTransitiveHoldAIPMetadata transitiveHold : transitiveDisposalHolds) {
      BrowserService.Util.getInstance().retrieveDisposalHold(transitiveHold.getId(),
        new NoAsyncCallback<DisposalHold>() {
          @Override
          public void onSuccess(DisposalHold disposalHold) {
            disposalHoldList.addObject(disposalHold);
            getTransitiveDisposalHoldList(transitiveDisposalHolds);
          }
        });
    }
  }

  private void getTransitiveDisposalHoldList(List<DisposalTransitiveHoldAIPMetadata> transitiveDisposalHolds) {
    panelBody.clear();
    BasicTablePanel<DisposalTransitiveHoldAIPMetadata> tableTransitiveHolds = getBasicTablePanelForTransitiveDisposalHolds(
      transitiveDisposalHolds);
    tableTransitiveHolds.getSelectionModel().addSelectionChangeHandler(event -> {
      DisposalTransitiveHoldAIPMetadata selectedObject = tableTransitiveHolds.getSelectionModel().getSelectedObject();
      if (selectedObject != null) {
        List<String> history = new ArrayList<>();
        history.add("@" + IndexedAIP.class.getSimpleName());
        history.add(RodaConstants.OPERATOR_OR);

        for (String aipId : selectedObject.getFromAIPs()) {
          history.add(RodaConstants.AIP_ID);
          history.add(aipId);
        }

        HistoryUtils.newHistory(Search.RESOLVER, history);
      }
    });
    panelBody.add(tableTransitiveHolds);
  }

  private BasicTablePanel<DisposalTransitiveHoldAIPMetadata> getBasicTablePanelForTransitiveDisposalHolds(
    List<DisposalTransitiveHoldAIPMetadata> transitiveDisposalHolds) {
    Label headerHolds = new Label();
    HTMLPanel info = new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML);

    return new BasicTablePanel<DisposalTransitiveHoldAIPMetadata>(headerHolds, info, transitiveDisposalHolds.iterator(),

      new BasicTablePanel.ColumnInfo<>(messages.disposalHoldTitle(), 0,
        new TextColumn<DisposalTransitiveHoldAIPMetadata>() {
          @Override
          public String getValue(DisposalTransitiveHoldAIPMetadata transitiveHold) {
            DisposalHold hold = disposalHoldList.findDisposalHold(transitiveHold.getId());
            if (hold != null && hold.getTitle() != null) {
              return hold.getTitle();
            } else {
              return "";
            }
          }
        }),

      new BasicTablePanel.ColumnInfo<>(messages.disposalHoldAssociatedFrom(), 15,
        new TextColumn<DisposalTransitiveHoldAIPMetadata>() {
          @Override
          public String getValue(DisposalTransitiveHoldAIPMetadata transitiveHold) {
            if (transitiveHold != null && transitiveHold.getFromAIPs() != null) {
              return messages.disposalHoldAssociatedFromValue(transitiveHold.getFromAIPs().size());
            } else {
              return "";
            }
          }
        }),

      new BasicTablePanel.ColumnInfo<>(messages.disposalHoldStateCol(), 15,
        new Column<DisposalTransitiveHoldAIPMetadata, SafeHtml>(new SafeHtmlCell()) {
          @Override
          public SafeHtml getValue(DisposalTransitiveHoldAIPMetadata transitiveHold) {
            DisposalHold hold = disposalHoldList.findDisposalHold(transitiveHold.getId());
            return HtmlSnippetUtils.getDisposalHoldStateHtml(hold);
          }
        })

    );
  }
}