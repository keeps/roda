/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.association;

import java.util.Collections;
import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.core.data.v2.disposal.hold.DisposalHolds;
import org.roda.core.data.v2.disposal.metadata.DisposalHoldAIPMetadata;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.DisposalDialogs;
import org.roda.wui.client.common.dialogs.utils.DisposalHoldDialogResult;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.hold.ShowDisposalHold;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.services.DisposalHoldRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
  private static final DisposalHoldsPanel.MyUiBinder uiBinder = GWT.create(DisposalHoldsPanel.MyUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final DisposalHolds disposalHoldList = new DisposalHolds();
  @UiField
  FlowPanel disposalHoldsPanel;

  @UiField
  FlowPanel panelBody;

  @UiField
  FlowPanel panel;

  @UiField(provided = true)
  TransitiveDisposalHoldsPanel transitiveDisposalHoldsPanel;

  @UiField
  Button associateDisposalHoldButton;

  private IndexedAIP indexedAip;

  private DisposalHoldsPanel() {
    // empty constructor
  }

  public DisposalHoldsPanel(IndexedAIP indexedAip) {
    transitiveDisposalHoldsPanel = new TransitiveDisposalHoldsPanel(indexedAip);

    initWidget(uiBinder.createAndBindUi(this));
    this.indexedAip = indexedAip;

    Services services = new Services("List disposal holds association", "get");
    services.disposalHoldResource(s -> s.listDisposalHoldsAssociation(indexedAip.getId()))
      .whenComplete((associations, throwable) -> {
        if (throwable != null) {
          AsyncCallbackUtils.defaultFailureTreatment(throwable);
        } else {
          init(indexedAip.getDisposalConfirmationId() != null, associations.getObjects());
        }
      });
  }

  private void init(boolean onDisposalConfirmation, List<DisposalHoldAIPMetadata> disposalHoldAssociations) {
    if (disposalHoldAssociations.isEmpty()) {
      disposalHoldsPanel.remove(panel);
    } else {
      createDisposalHoldsTable(disposalHoldAssociations);
    }

    if (!onDisposalConfirmation) {
      PermissionClientUtils.bindPermission(associateDisposalHoldButton, null,
        RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_HOLD);
    } else {
      associateDisposalHoldButton.setVisible(false);
    }
  }

  private void createDisposalHoldsTable(List<DisposalHoldAIPMetadata> disposalHoldAssociations) {
    for (DisposalHoldAIPMetadata association : disposalHoldAssociations) {
      Services services = new Services("Retrieve disposal hold", "get");
      services.disposalHoldResource(s -> s.retrieveDisposalHold(association.getId()))
        .whenComplete((disposalHold, throwable) -> {
          if (throwable != null) {
            AsyncCallbackUtils.defaultFailureTreatment(throwable);
          } else {
            disposalHoldList.addObject(disposalHold);
            getDisposalHoldList(disposalHoldAssociations);
          }
        });
    }
  }

  private void getDisposalHoldList(List<DisposalHoldAIPMetadata> disposalHoldAssociations) {
    panelBody.clear();
    BasicTablePanel<DisposalHoldAIPMetadata> tableHolds = getBasicTablePanelForDisposalHolds(disposalHoldAssociations);
    tableHolds.getSelectionModel().addSelectionChangeHandler(event -> {
      DisposalHoldAIPMetadata selectedObject = tableHolds.getSelectionModel().getSelectedObject();
      if (selectedObject != null) {
        List<String> path = HistoryUtils.getHistory(ShowDisposalHold.RESOLVER.getHistoryPath(), selectedObject.getId());
        HistoryUtils.newHistory(path);
      }
    });
    panelBody.add(tableHolds);
  }

  @UiHandler("associateDisposalHoldButton")
  void handleManageDisposalHold(ClickEvent e) {
    Services services = new Services("List disposal holds", "get");
    services.disposalHoldResource(DisposalHoldRestService::listDisposalHolds)
      .whenComplete((disposalHolds, throwable) -> {
        if (throwable != null) {
          AsyncCallbackUtils.defaultFailureTreatment(throwable);
        } else {
          disposalHolds.getObjects().removeIf(p -> DisposalHoldState.LIFTED.equals(p.getState()));
          DisposalDialogs.showDisposalHoldSelection(messages.disposalHoldSelectionDialogTitle(), disposalHolds,
            new NoAsyncCallback<DisposalHoldDialogResult>() {
              @Override
              public void onSuccess(DisposalHoldDialogResult result) {
                SelectedItems<IndexedAIP> items = new SelectedItemsList<>(
                  Collections.singletonList(indexedAip.getUUID()), indexedAip.getClass().getName());
                if (DisposalHoldDialogResult.ActionType.CLEAR.equals(result.getActionType())) {
                  clearDisposalHolds(items);
                } else if (DisposalHoldDialogResult.ActionType.ASSOCIATE.equals(result.getActionType())) {
                  applyDisposalHold(items, result, false);
                } else if (DisposalHoldDialogResult.ActionType.OVERRIDE.equals(result.getActionType())) {
                  applyDisposalHold(items, result, true);
                }
              }
            });
        }
      });
  }

  private void clearDisposalHolds(final SelectedItems<IndexedAIP> aips) {
    Dialogs.showConfirmDialog(messages.clearDisposalHoldDialogTitle(), messages.clearDisposalHoldDialogMessage(1),
      messages.dialogNo(), messages.dialogYes(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            Services services = new Services("Disassociate disposal holds", "job");
            services.disposalHoldResource(s -> s.disassociateDisposalHold(SelectedItemsUtils.convertToRESTRequest(aips), null, true))
              .whenComplete((job, throwable) -> {
                if (throwable != null) {
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                } else {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                      Timer timer = new Timer() {
                        @Override
                        public void run() {
                          refresh();
                        }
                      };

                      timer.schedule(RodaConstants.ACTION_TIMEOUT);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                    }
                  });
                }
              });
          }
        }
      });
  }

  private void applyDisposalHold(final SelectedItems<IndexedAIP> aips, DisposalHoldDialogResult holdDialogResult,
    boolean override) {
    Dialogs.showConfirmDialog(messages.applyDisposalHoldDialogTitle(), messages.applyDisposalHoldDialogMessage(1),
      messages.dialogNo(), messages.dialogYes(), new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            Services services = new Services("Apply disposal hold", "job");
            services.disposalHoldResource(s -> s.applyDisposalHold(SelectedItemsUtils.convertToRESTRequest(aips),
              holdDialogResult.getDisposalHold().getId(), override)).whenComplete((job, throwable) -> {
                if (throwable != null) {
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                } else {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                      Timer timer = new Timer() {
                        @Override
                        public void run() {
                          refresh();
                        }
                      };

                      timer.schedule(RodaConstants.ACTION_TIMEOUT);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                    }
                  });
                }
              });
          }
        }
      });
  }

  private void refresh() {
    Services services = new Services("List disposal holds association", "get");
    services.disposalHoldResource(s -> s.listDisposalHoldsAssociation(indexedAip.getId()))
      .whenComplete((associations, throwable) -> {
        if (throwable != null) {
          AsyncCallbackUtils.defaultFailureTreatment(throwable);
        } else {
          init(indexedAip.getDisposalConfirmationId() != null, associations.getObjects());
        }
      });
  }

  private BasicTablePanel<DisposalHoldAIPMetadata> getBasicTablePanelForDisposalHolds(
    List<DisposalHoldAIPMetadata> disposalHoldAssociations) {
    Label headerHolds = new Label();
    HTMLPanel info = new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML);

    return new BasicTablePanel<DisposalHoldAIPMetadata>(headerHolds, info, disposalHoldAssociations.iterator(),

      new BasicTablePanel.ColumnInfo<>(messages.disposalHoldTitle(), 0, new TextColumn<DisposalHoldAIPMetadata>() {
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

      new BasicTablePanel.ColumnInfo<DisposalHoldAIPMetadata>(messages.disposalHoldAssociatedOn(), 15,
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

      new BasicTablePanel.ColumnInfo<DisposalHoldAIPMetadata>(messages.disposalHoldAssociatedBy(), 15,
        new TextColumn<DisposalHoldAIPMetadata>() {
          @Override
          public String getValue(DisposalHoldAIPMetadata association) {
            if (association != null && association.getAssociatedBy() != null) {
              return association.getAssociatedBy();
            } else {
              return "";
            }
          }
        }),

      new BasicTablePanel.ColumnInfo<>(messages.disposalHoldStateCol(), 15,
        new Column<DisposalHoldAIPMetadata, SafeHtml>(new SafeHtmlCell()) {
          @Override
          public SafeHtml getValue(DisposalHoldAIPMetadata association) {
            DisposalHold hold = disposalHoldList.findDisposalHold(association.getId());
            return HtmlSnippetUtils.getDisposalHoldStateHtml(hold);
          }
        }));
  }

  interface MyUiBinder extends UiBinder<Widget, DisposalHoldsPanel> {
  }
}