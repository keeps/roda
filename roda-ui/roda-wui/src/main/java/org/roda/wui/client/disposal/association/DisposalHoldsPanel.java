package org.roda.wui.client.disposal.association;

import java.util.Collections;
import java.util.List;

import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposal.DisposalHold;
import org.roda.core.data.v2.ip.disposal.DisposalHoldAssociation;
import org.roda.core.data.v2.ip.disposal.DisposalHoldState;
import org.roda.core.data.v2.ip.disposal.DisposalHolds;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.dialogs.DisposalDialogs;
import org.roda.wui.client.common.dialogs.utils.DisposalHoldDialogResult;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.disposal.hold.ShowDisposalHold;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DisposalHoldsPanel extends Composite {
  interface MyUiBinder extends UiBinder<Widget, DisposalHoldsPanel> {
  }

  private static DisposalHoldsPanel.MyUiBinder uiBinder = GWT.create(DisposalHoldsPanel.MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel disposalHoldsPanel;

  @UiField
  FlowPanel panelBody;

  private DisposalHolds disposalHoldList = new DisposalHolds();

  public DisposalHoldsPanel(IndexedAIP aip) {
    initWidget(uiBinder.createAndBindUi(this));

    BrowserService.Util.getInstance().listDisposalHoldsAssociation(aip.getId(),
      new NoAsyncCallback<List<DisposalHoldAssociation>>() {
        @Override
        public void onSuccess(List<DisposalHoldAssociation> disposalHoldAssociations) {
          init(aip.getDisposalConfirmationId() != null, disposalHoldAssociations);
        }
      });

  }

  private void init(boolean onDisposalConfirmation, List<DisposalHoldAssociation> disposalHoldAssociations) {
    if (disposalHoldAssociations.isEmpty()) {
      disposalHoldsPanel.clear();
    } else {
      createDisposalHoldsTable(disposalHoldAssociations);
    }

    if (!onDisposalConfirmation) {
      disposalHoldsPanel.add(associateDisposalHoldButton());
    }
  }

  private void createDisposalHoldsTable(List<DisposalHoldAssociation> disposalHoldAssociations) {
    for (DisposalHoldAssociation association : disposalHoldAssociations) {
      BrowserService.Util.getInstance().retrieveDisposalHold(association.getId(), new NoAsyncCallback<DisposalHold>() {
        @Override
        public void onSuccess(DisposalHold disposalHold) {
          disposalHoldList.addObject(disposalHold);
          getDisposalHoldList(disposalHoldAssociations);
        }
      });
    }
  }

  private void getDisposalHoldList(List<DisposalHoldAssociation> disposalHoldAssociations) {
    panelBody.clear();
    BasicTablePanel<DisposalHoldAssociation> tableHolds = getBasicTablePanelForDisposalHolds(disposalHoldAssociations);
    tableHolds.getSelectionModel().addSelectionChangeHandler(event -> {
      DisposalHoldAssociation selectedObject = tableHolds.getSelectionModel().getSelectedObject();
      if (selectedObject != null) {
        List<String> path = HistoryUtils.getHistory(ShowDisposalHold.RESOLVER.getHistoryPath(), selectedObject.getId());
        HistoryUtils.newHistory(path);
      }
    });
    panelBody.add(tableHolds);
  }

  private Button associateDisposalHoldButton() {
    Button associateDisposalHoldButton = new Button();
    associateDisposalHoldButton.addStyleName("btn btn-plus");
    associateDisposalHoldButton.setText(messages.associateDisposalHoldButton());
    associateDisposalHoldButton.addClickHandler(clickEvent -> {
      BrowserService.Util.getInstance().listDisposalHolds(new NoAsyncCallback<DisposalHolds>() {
        @Override
        public void onSuccess(DisposalHolds holds) {
          holds.getObjects().removeIf(p -> DisposalHoldState.LIFTED.equals(p.getState()));
          DisposalDialogs.showDisposalHoldSelection("", holds, new NoAsyncCallback<DisposalHoldDialogResult>() {
            @Override
            public void onSuccess(DisposalHoldDialogResult result) {

            }
          });
        }
      });

    });
    return associateDisposalHoldButton;
  }

  private BasicTablePanel<DisposalHoldAssociation> getBasicTablePanelForDisposalHolds(
    List<DisposalHoldAssociation> disposalHoldAssociations) {
    Label headerHolds = new Label();
    HTMLPanel info = new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML);

    disposalHoldAssociations.sort(Collections.reverseOrder());

    return new BasicTablePanel<DisposalHoldAssociation>(headerHolds, info, disposalHoldAssociations.iterator(),

      new BasicTablePanel.ColumnInfo<>(messages.disposalHoldTitle(), 15, new TextColumn<DisposalHoldAssociation>() {
        @Override
        public String getValue(DisposalHoldAssociation association) {
          DisposalHold hold = disposalHoldList.findDisposalHold(association.getId());
          if (hold != null && hold.getTitle() != null) {
            return hold.getTitle();
          } else {
            return "";
          }
        }
      }),

      new BasicTablePanel.ColumnInfo<DisposalHoldAssociation>(messages.disposalHoldAssociatedOn(), 15,
        new TextColumn<DisposalHoldAssociation>() {
          @Override
          public String getValue(DisposalHoldAssociation association) {
            if (association != null && association.getAssociatedOn() != null) {
              return Humanize.formatDate(association.getAssociatedOn());
            } else {
              return "";
            }
          }
        }),

      new BasicTablePanel.ColumnInfo<DisposalHoldAssociation>(messages.disposalHoldLiftedOn(), 15,
        new TextColumn<DisposalHoldAssociation>() {
          @Override
          public String getValue(DisposalHoldAssociation association) {
            if (association != null && association.getLiftedOn() != null) {
              return Humanize.formatDate(association.getLiftedOn());
            } else {
              return "";
            }
          }
        }),

      new BasicTablePanel.ColumnInfo<>(messages.disposalHoldStateCol(), 8,
        new Column<DisposalHoldAssociation, SafeHtml>(new SafeHtmlCell()) {
          @Override
          public SafeHtml getValue(DisposalHoldAssociation association) {
            return HtmlSnippetUtils.getDisposalHoldStateHtml(association);
          }
        }));
  }
}