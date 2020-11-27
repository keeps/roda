package org.roda.wui.client.disposal.association;

import java.util.Collections;
import java.util.List;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHolds;
import org.roda.core.data.v2.ip.disposal.aipMetadata.DisposalHoldAIPMetadata;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.disposal.hold.ShowDisposalHold;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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

    BrowserService.Util.getInstance().listDisposalHoldsAssociation(indexedAip.getId(),
      new NoAsyncCallback<List<DisposalHoldAIPMetadata>>() {
        @Override
        public void onSuccess(List<DisposalHoldAIPMetadata> disposalHoldAssociations) {
          init(indexedAip.getDisposalConfirmationId() != null, disposalHoldAssociations);
        }
      });
  }

  private void init(boolean onDisposalConfirmation, List<DisposalHoldAIPMetadata> disposalHoldAssociations) {
    if (disposalHoldAssociations.isEmpty()) {
      transitiveDisposalHoldsPanel.remove(panel);
    } else {
      createTransitiveDisposalHoldsTable(disposalHoldAssociations);
    }
  }

  private void createTransitiveDisposalHoldsTable(List<DisposalHoldAIPMetadata> disposalHoldAssociations) {
    for (DisposalHoldAIPMetadata association : disposalHoldAssociations) {
      BrowserService.Util.getInstance().retrieveDisposalHold(association.getId(), new NoAsyncCallback<DisposalHold>() {
        @Override
        public void onSuccess(DisposalHold disposalHold) {
          disposalHoldList.addObject(disposalHold);
          getTransitiveDisposalHoldList(disposalHoldAssociations);
        }
      });
    }
  }

  private void getTransitiveDisposalHoldList(List<DisposalHoldAIPMetadata> disposalHoldAssociations) {
    panelBody.clear();
    BasicTablePanel<DisposalHoldAIPMetadata> tableHolds = getBasicTablePanelForTransitiveDisposalHolds(
      disposalHoldAssociations);
    tableHolds.getSelectionModel().addSelectionChangeHandler(event -> {
      DisposalHoldAIPMetadata selectedObject = tableHolds.getSelectionModel().getSelectedObject();
      if (selectedObject != null) {
        List<String> path = HistoryUtils.getHistory(ShowDisposalHold.RESOLVER.getHistoryPath(), selectedObject.getId());
        HistoryUtils.newHistory(path);
      }
    });
    panelBody.add(tableHolds);
  }

  private BasicTablePanel<DisposalHoldAIPMetadata> getBasicTablePanelForTransitiveDisposalHolds(
    List<DisposalHoldAIPMetadata> disposalHoldAssociations) {
    Label headerHolds = new Label();
    HTMLPanel info = new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML);

    disposalHoldAssociations.sort(Collections.reverseOrder());

    return new BasicTablePanel<DisposalHoldAIPMetadata>(headerHolds, info, disposalHoldAssociations.iterator(),

      new BasicTablePanel.ColumnInfo<>(messages.aipLevel(), 15, new TextColumn<DisposalHoldAIPMetadata>() {
        @Override
        public String getValue(DisposalHoldAIPMetadata association) {
          DisposalHold hold = disposalHoldList.findDisposalHold(association.getId());
          if (hold != null && hold.getTitle() != null) {
            return hold.getTitle();
          } else {
            return "";
          }
        }
      }),

      new BasicTablePanel.ColumnInfo<DisposalHoldAIPMetadata>(messages.aipGenericTitle(), 15,
        new TextColumn<DisposalHoldAIPMetadata>() {
          @Override
          public String getValue(DisposalHoldAIPMetadata association) {
            if (association != null && association.getAssociatedOn() != null) {
              return Humanize.formatDate(association.getAssociatedOn());
            } else {
              return "";
            }
          }
        }),

      new BasicTablePanel.ColumnInfo<>(messages.disposalHoldTitle(), 15, new TextColumn<DisposalHoldAIPMetadata>() {
        @Override
        public String getValue(DisposalHoldAIPMetadata association) {
          DisposalHold hold = disposalHoldList.findDisposalHold(association.getId());
          if (hold != null && hold.getTitle() != null) {
            return hold.getTitle();
          } else {
            return "";
          }
        }
      })

    );
  }
}