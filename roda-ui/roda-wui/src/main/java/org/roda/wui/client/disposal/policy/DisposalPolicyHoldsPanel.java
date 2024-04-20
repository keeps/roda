/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.policy;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHolds;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.hold.ShowDisposalHold;
import org.roda.wui.client.services.DisposalHoldRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.widgets.HTMLWidgetWrapper;

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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Tiago Fraga <tfraga@keep.pt>
 */

public class DisposalPolicyHoldsPanel extends Composite {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static DisposalPolicyHoldsPanel.MyUiBinder uiBinder = GWT.create(DisposalPolicyHoldsPanel.MyUiBinder.class);
  // Disposal Holds
  @UiField
  FlowPanel disposalHoldsDescription;
  @UiField
  ScrollPanel disposalHoldsTablePanel;

  public DisposalPolicyHoldsPanel() {
    initWidget(uiBinder.createAndBindUi(this));
    if (PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_HOLDS)) {
      Services services = new Services("List disposal holds", "get");
      services.disposalHoldResource(DisposalHoldRestService::listDisposalHolds)
        .whenComplete((disposalHolds, throwable) -> {
          if (throwable != null) {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          } else {
            init(disposalHoldsDescription, disposalHoldsTablePanel, disposalHolds);
          }
        });
    }
  }

  private void createDisposalHoldsDescription(FlowPanel disposalHoldsDescription) {
    Label header = new Label(messages.disposalHoldsTitle());
    header.addStyleName("h5");

    HTMLPanel info = new HTMLPanel("");
    info.add(new HTMLWidgetWrapper("DisposalHoldDescription.html"));
    info.addStyleName("page-description");

    disposalHoldsDescription.add(header);
    disposalHoldsDescription.add(info);
  }

  private void createDisposalHoldsPanel(ScrollPanel disposalHoldsTablePanel, DisposalHolds disposalHolds) {
    disposalHoldsTablePanel.addStyleName("basicTable");
    disposalHoldsTablePanel.addStyleName("basicTable-border");
    if (disposalHolds.getObjects().isEmpty()) {
      String someOfAObject = messages.someOfAObject(disposalHolds.getClass().getName());
      Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.noItemsToDisplayPreFilters(someOfAObject)));
      label.addStyleName("basicTableEmpty");
      disposalHoldsTablePanel.add(label);
    } else {
      FlowPanel holdsPanel = new FlowPanel();
      BasicTablePanel<DisposalHold> tableHolds = getBasicTablePanelForDisposalHolds(disposalHolds);
      tableHolds.getSelectionModel().addSelectionChangeHandler(event -> {
        DisposalHold selectedHold = tableHolds.getSelectionModel().getSelectedObject();
        if (selectedHold != null) {
          tableHolds.getSelectionModel().clear();
          List<String> path = HistoryUtils.getHistory(ShowDisposalHold.RESOLVER.getHistoryPath(), selectedHold.getId());
          HistoryUtils.newHistory(path);
        }
      });

      holdsPanel.add(tableHolds);
      disposalHoldsTablePanel.add(holdsPanel);
      disposalHoldsTablePanel.addStyleName("disposalPolicyScrollPanel");
    }
  }

  private void init(FlowPanel disposalHoldsDescription, ScrollPanel disposalHoldsTablePanel,
    DisposalHolds disposalHolds) {
    // Create disposal holds description
    createDisposalHoldsDescription(disposalHoldsDescription);

    // Disposal holds table
    createDisposalHoldsPanel(disposalHoldsTablePanel, disposalHolds);
  }

  private BasicTablePanel<DisposalHold> getBasicTablePanelForDisposalHolds(DisposalHolds disposalHolds) {
    if (disposalHolds.getObjects().isEmpty()) {
      return new BasicTablePanel<>(messages.noItemsToDisplayPreFilters(messages.disposalHoldsTitle()));
    } else {
      return new BasicTablePanel<DisposalHold>(disposalHolds.getObjects().iterator(),

        new BasicTablePanel.ColumnInfo<>(messages.disposalHoldTitle(), 15, new TextColumn<DisposalHold>() {
          @Override
          public String getValue(DisposalHold hold) {
            return hold.getTitle();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalScheduleMandate(), 0, new TextColumn<DisposalHold>() {
          @Override
          public String getValue(DisposalHold hold) {
            return hold.getMandate();
          }
        }),

        new BasicTablePanel.ColumnInfo<>(messages.disposalHoldStateCol(), 12,
          new Column<DisposalHold, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(DisposalHold hold) {
              return HtmlSnippetUtils.getDisposalHoldStateHtml(hold);
            }
          }));
    }
  }

  interface MyUiBinder extends UiBinder<Widget, DisposalPolicyHoldsPanel> {
  }
}