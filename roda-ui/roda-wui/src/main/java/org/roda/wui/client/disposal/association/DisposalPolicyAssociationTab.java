/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.disposal.association;

import static org.roda.core.data.common.RodaConstants.SEARCH_WITH_PREFILTER_HANDLER;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmation;
import org.roda.core.data.v2.disposal.confirmation.DisposalConfirmationState;
import org.roda.core.data.v2.disposal.hold.DisposalHold;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.core.data.v2.disposal.hold.DisposalHolds;
import org.roda.core.data.v2.disposal.metadata.DisposalHoldAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalTransitiveHoldAIPMetadata;
import org.roda.core.data.v2.disposal.metadata.DisposalTransitiveHoldsAIPMetadata;
import org.roda.core.data.v2.disposal.schedule.DisposalActionCode;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodCalculation;
import org.roda.core.data.v2.disposal.schedule.RetentionPeriodIntervalCode;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.disposalhold.DisassociateDisposalHoldRequest;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.actions.AipToolbarActions;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableObject;
import org.roda.wui.client.common.actions.widgets.ActionableWidgetBuilder;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.lists.utils.ActionMenuCell;
import org.roda.wui.client.common.lists.utils.BasicTablePanel;
import org.roda.wui.client.common.panels.GenericMetadataCardPanel;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.common.utils.HtmlSnippetUtils;
import org.roda.wui.client.common.utils.PermissionClientUtils;
import org.roda.wui.client.disposal.confirmations.ShowDisposalConfirmation;
import org.roda.wui.client.disposal.hold.ShowDisposalHold;
import org.roda.wui.client.disposal.schedule.ShowDisposalSchedule;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.search.Search;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.Humanize;
import org.roda.wui.common.client.tools.StringUtils;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;

import config.i18n.client.ClientMessages;

public class DisposalPolicyAssociationTab extends GenericMetadataCardPanel<IndexedAIP> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final DisposalHolds disposalHoldList = new DisposalHolds();
  private final DisposalHolds transitiveHoldList = new DisposalHolds();
  private AsyncCallback<Actionable.ActionImpact> actionCallback;

  public DisposalPolicyAssociationTab(IndexedAIP indexedAIP, AsyncCallback<Actionable.ActionImpact> actionCallback) {
    this.actionCallback = actionCallback;
    setData(indexedAIP);
  }

  @Override
  protected FlowPanel createHeaderWidget(IndexedAIP data) {
    return new ActionableWidgetBuilder<IndexedAIP>(
      AipToolbarActions.get(data.getId(), data.getState(), data.getPermissions())).withActionCallback(actionCallback)
      .buildGroupedListWithObjects(new ActionableObject<>(data),
        List.of(AipToolbarActions.AIPAction.ASSOCIATE_DISPOSAL_SCHEDULE,
          AipToolbarActions.AIPAction.DISASSOCIATE_DISPOSAL_SCHEDULE,
          AipToolbarActions.AIPAction.ASSOCIATE_DISPOSAL_HOLD),
        List.of(AipToolbarActions.AIPAction.ASSOCIATE_DISPOSAL_SCHEDULE,
          AipToolbarActions.AIPAction.DISASSOCIATE_DISPOSAL_SCHEDULE,
          AipToolbarActions.AIPAction.ASSOCIATE_DISPOSAL_HOLD));
  }

  @Override
  protected void buildFields(IndexedAIP data) {
    boolean canReadDisposalSchedules = canReadDisposalSchedules();
    boolean canReadDisposalHolds = canReadDisposalHolds();
    boolean canReadDisposalConfirmations = canReadDisposalConfirmations();

    if (showNotAssigned(data, canReadDisposalSchedules, canReadDisposalHolds, canReadDisposalConfirmations)) {
      metadataContainer.add(getNoItemsToDisplay());
      return;
    }

    // 1. Fetch Disposal Confirmation (Async)
    CompletableFuture<DisposalConfirmation> confirmationFuture;
    if (canReadDisposalConfirmations && data.getDisposalConfirmationId() != null) {
      Services services = new Services("Retrieve disposal confirmation", "get");
      confirmationFuture = services.rodaEntityRestService(
        s -> s.findByUuid(data.getDisposalConfirmationId(), LocaleInfo.getCurrentLocale().getLocaleName()),
        DisposalConfirmation.class);
    } else {
      confirmationFuture = CompletableFuture.completedFuture(null);
    }

    // 2. Fetch Disposal Holds and Individual Holds (Async, nested)
    CompletableFuture<List<DisposalHoldAIPMetadata>> holdsFuture;
    CompletableFuture<DisposalTransitiveHoldsAIPMetadata> transitiveHoldsFuture;

    if (canReadDisposalHolds) {
      Services services = new Services("List disposal holds association", "get");
      holdsFuture = services.disposalHoldResource(s -> s.listDisposalHoldsAssociation(data.getId()))
        .thenCompose(associations -> {
          List<CompletableFuture<Void>> individualHoldTasks = new ArrayList<>();

          for (DisposalHoldAIPMetadata assoc : associations.getObjects()) {
            Services holdServices = new Services("Retrieve disposal hold", "get");
            CompletableFuture<Void> holdTask = holdServices
              .disposalHoldResource(s -> s.retrieveDisposalHold(assoc.getId()))
              .thenAccept(hold -> disposalHoldList.addObject(hold));

            individualHoldTasks.add(holdTask);
          }

          return CompletableFuture.allOf(individualHoldTasks.toArray(new CompletableFuture[0]))
            .thenApply(v -> associations.getObjects());
        });

      transitiveHoldsFuture = services.disposalHoldResource(s -> s.listTransitiveHolds(data.getId()))
        .thenCompose(disposalTransitiveHoldsAIPMetadata -> {
          List<CompletableFuture<Void>> individualHoldTasks = new ArrayList<>();

          for (DisposalTransitiveHoldAIPMetadata transitive : disposalTransitiveHoldsAIPMetadata.getObjects()) {
            Services holdServices = new Services("Retrieve disposal hold", "get");
            CompletableFuture<Void> holdTask = holdServices
              .disposalHoldResource(s -> s.retrieveDisposalHold(transitive.getId()))
              .thenAccept(hold -> transitiveHoldList.addObject(hold));

            individualHoldTasks.add(holdTask);
          }

          return CompletableFuture.allOf(individualHoldTasks.toArray(new CompletableFuture[0]))
            .thenApply(v -> disposalTransitiveHoldsAIPMetadata);
        });
    } else {
      holdsFuture = CompletableFuture.completedFuture(List.of());
      transitiveHoldsFuture = CompletableFuture.completedFuture(new DisposalTransitiveHoldsAIPMetadata());
    }

    // 3. Wait for all network calls to finish, then draw the DOM sequentially!
    CompletableFuture.allOf(confirmationFuture, holdsFuture, transitiveHoldsFuture).whenComplete((v, throwable) -> {
      if (throwable != null) {
        AsyncCallbackUtils.defaultFailureTreatment(throwable);
        return;
      }

      // A. Draw Confirmation (First)
      DisposalConfirmation confirmation = confirmationFuture.join();
      if (canReadDisposalConfirmations && confirmation != null) {
        buildDisposalConfirmation(confirmation);
      }

      // B. Draw Schedule (Second)
      if (canReadDisposalSchedules) {
        buildDisposalScheduleInformation(data);
      }

      // C. Draw Holds (Third)
      if (canReadDisposalHolds) {
        List<DisposalHoldAIPMetadata> associations = holdsFuture.join();
        boolean hasDirectHolds = associations != null && !associations.isEmpty();

        if (hasDirectHolds) {
          addSeparator(messages.disposalHoldsAssociationInformationTitle());
          getDisposalHoldList(data, associations);
        }

        DisposalTransitiveHoldsAIPMetadata transitiveHolds = transitiveHoldsFuture.join();
        boolean hasTransitiveHolds = transitiveHolds != null && transitiveHolds.getObjects() != null
          && !transitiveHolds.getObjects().isEmpty();

        if (hasTransitiveHolds) {
          buildTransitiveHoldsInformation(transitiveHolds.getObjects());
        }
      }
    });
  }

  private void buildTransitiveHoldsInformation(List<DisposalTransitiveHoldAIPMetadata> transitiveHolds) {
    addSeparator(messages.transitiveDisposalHoldsAssociationInformationTitle());
    getTransitiveDisposalHoldList(transitiveHolds);
  }

  private void getTransitiveDisposalHoldList(List<DisposalTransitiveHoldAIPMetadata> transitiveDisposalHolds) {
    BasicTablePanel<DisposalTransitiveHoldAIPMetadata> tableTransitiveHolds = getBasicTablePanelForTransitiveDisposalHolds(
      transitiveDisposalHolds);
    tableTransitiveHolds.getSelectionModel().addSelectionChangeHandler(event -> {
      DisposalTransitiveHoldAIPMetadata selectedObject = tableTransitiveHolds.getSelectionModel().getSelectedObject();
      if (selectedObject != null) {
        List<String> history = new ArrayList<>();
        history.add(SEARCH_WITH_PREFILTER_HANDLER);
        history.add("title");
        history.add(messages.searchPrefilterTransitiveHolds());
        history.add("@" + IndexedAIP.class.getSimpleName());
        history.add(RodaConstants.OPERATOR_OR);

        for (String aipId : selectedObject.getFromAIPs()) {
          history.add(RodaConstants.AIP_ID);
          history.add(aipId);
        }

        HistoryUtils.newHistory(Search.RESOLVER, history);
      }
    });
    metadataContainer.add(tableTransitiveHolds);
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
            DisposalHold hold = transitiveHoldList.findDisposalHold(transitiveHold.getId());
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
            DisposalHold hold = transitiveHoldList.findDisposalHold(transitiveHold.getId());
            return HtmlSnippetUtils.getDisposalHoldStateHtml(hold);
          }
        })

    );
  }

  private void buildDisposalConfirmation(DisposalConfirmation confirmation) {
    addSeparator(messages.disposalConfirmationAssociationInformationTitle());
    buildField(messages.disposalConfirmationAssociationTitle()).withValue(confirmation.getTitle())
      .onClick(event -> HistoryUtils.newHistory(ShowDisposalConfirmation.RESOLVER, confirmation.getId())).build();

    buildField(messages.disposalConfirmationCreationDate()).withValue(Humanize.formatDate(confirmation.getCreatedOn()))
      .build();

    if (DisposalConfirmationState.APPROVED.equals(confirmation.getState())
      || DisposalConfirmationState.PERMANENTLY_DELETED.equals(confirmation.getState())) {
      buildField("Executed on").withValue(Humanize.formatDate(confirmation.getExecutedOn())).build();
      buildField("Executed by").withValue(confirmation.getExecutedBy()).build();
    }

    buildField(messages.disposalConfirmationStatus())
      .withHtml(HtmlSnippetUtils.getDisposalConfirmationStateHTML(confirmation.getState())).build();
  }

  private void buildDisposalScheduleInformation(IndexedAIP data) {
    if (StringUtils.isNotBlank(data.getDisposalScheduleId())) {
      addSeparator(messages.disposalScheduleAssociationInformationTitle());

      buildField(messages.disposalScheduleAssociationTitle()).withValue(data.getDisposalScheduleName())
        .onClick(event -> HistoryUtils.newHistory(ShowDisposalSchedule.RESOLVER, data.getDisposalScheduleId())).build();

      buildField(messages.disposalActionLabel())
        .withHtml(HtmlSnippetUtils.getDisposalScheduleActionHtml(data.getDisposalAction())).build();

      if (!DisposalActionCode.RETAIN_PERMANENTLY.equals(data.getDisposalAction())) {
        if (RetentionPeriodCalculation.SUCCESS.equals(data.getRetentionPeriodState())) {
          buildRetentionPeriodCorrectCalculation(data);
        } else {
          buildRetentionPeriodFailedCalculation(data);
        }
      }

      buildField(messages.disposalScheduleAssociationTypeLabel())
        .withValue(messages.disposalScheduleAssociationType(data.getScheduleAssociationType().name())).build();
    }
  }

  private void getDisposalHoldList(IndexedAIP aip, List<DisposalHoldAIPMetadata> disposalHoldAssociations) {
    BasicTablePanel<DisposalHoldAIPMetadata> tableHolds = getBasicTablePanelForDisposalHolds(aip,
      disposalHoldAssociations);
    tableHolds.removeSelectionModel();
    metadataContainer.add(tableHolds);
  }

  private BasicTablePanel<DisposalHoldAIPMetadata> getBasicTablePanelForDisposalHolds(IndexedAIP aip,
    List<DisposalHoldAIPMetadata> disposalHoldAssociations) {
    Label headerHolds = new Label();
    HTMLPanel info = new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML);

    return new BasicTablePanel<DisposalHoldAIPMetadata>(headerHolds, info, disposalHoldAssociations.iterator(),

      new BasicTablePanel.ColumnInfo<>("", 1, new Column<DisposalHoldAIPMetadata, SafeHtml>(new SafeHtmlCell()) {
        @Override
        public SafeHtml getValue(DisposalHoldAIPMetadata association) {
          return SafeHtmlUtils.fromSafeConstant("<i class='fas fa-lock'></i>");
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
      }),

      new BasicTablePanel.ColumnInfo<>(messages.disposalHoldStateCol(), 10,
        new Column<DisposalHoldAIPMetadata, SafeHtml>(new SafeHtmlCell()) {
          @Override
          public SafeHtml getValue(DisposalHoldAIPMetadata association) {
            DisposalHold hold = disposalHoldList.findDisposalHold(association.getId());
            return HtmlSnippetUtils.getDisposalHoldStateHtml(hold);
          }
        }),

      new BasicTablePanel.ColumnInfo<DisposalHoldAIPMetadata>(messages.disposalHoldAssociatedOn(), 5,
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

      new BasicTablePanel.ColumnInfo<DisposalHoldAIPMetadata>(messages.disposalHoldAssociatedBy(), 10,
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
      new BasicTablePanel.ColumnInfo<>(messages.actions(), 5, getDisposalHoldActionsColumn(aip)));
  }

  private boolean showNotAssigned(IndexedAIP aip, boolean canReadDisposalSchedules, boolean canReadDisposalHolds,
    boolean canReadDisposalConfirmations) {
    if (canReadDisposalConfirmations && StringUtils.isNotBlank(aip.getDisposalConfirmationId())) {
      return false;
    }

    if (canReadDisposalSchedules && StringUtils.isNotBlank(aip.getDisposalScheduleId())) {
      return false;
    }

    if (canReadDisposalHolds) {
      if (aip.isOnHold()) {
        return false;
      }

      if (!aip.getDisposalHoldsId().isEmpty()) {
        return false;
      }

      if (!aip.getTransitiveDisposalHoldsId().isEmpty()) {
        return false;
      }
    }

    return true;
  }

  private SimplePanel getNoItemsToDisplay() {
    SimplePanel panel = new SimplePanel();
    panel.addStyleName("table-empty-inner");
    Label label = new HTML(SafeHtmlUtils.fromSafeConstant(messages.disposalPolicyNoneSummary()));
    label.addStyleName("table-empty-inner-label");
    panel.setWidget(label);
    return panel;
  }

  private void buildRetentionPeriodFailedCalculation(final IndexedAIP aip) {
    // Keep commented logic if required
    buildField(messages.disposalRetentionStartDateLabel()).withValue(aip.getRetentionPeriodDetails()).build();
  }

  private void buildRetentionPeriodCorrectCalculation(final IndexedAIP aip) {
    buildField(messages.disposalRetentionStartDateLabel())
      .withValue(Humanize.formatDate(aip.getRetentionPeriodStartDate())).build();

    if (DisposalActionCode.RETAIN_PERMANENTLY.equals(aip.getDisposalAction())) {
      buildField(messages.disposalRetentionDueDateLabel()).withValue(messages.permanentlyRetained()).build();
    } else {
      buildField(messages.disposalRetentionDueDateLabel()).withValue(Humanize.formatDate(aip.getOverdueDate())).build();

      if (aip.getRetentionPeriodInterval().equals(RetentionPeriodIntervalCode.NO_RETENTION_PERIOD)) {
        buildField(messages.disposalRetentionPeriodLabel())
          .withValue(messages.retentionPeriod(0, aip.getRetentionPeriodInterval().name())).build();
      } else {
        buildField(messages.disposalRetentionPeriodLabel())
          .withValue(
            messages.retentionPeriod(aip.getRetentionPeriodDuration(), aip.getRetentionPeriodInterval().name()))
          .build();
      }
    }
  }

  private Column<DisposalHoldAIPMetadata, DisposalHoldAIPMetadata> getDisposalHoldActionsColumn(IndexedAIP aip) {
    return new Column<DisposalHoldAIPMetadata, DisposalHoldAIPMetadata>(
      new ActionMenuCell<>((association, left, top) -> showDisposalHoldActionsMenu(aip, association, left, top))) {
      @Override
      public DisposalHoldAIPMetadata getValue(DisposalHoldAIPMetadata association) {
        return association;
      }
    };
  }

  private void showDisposalHoldActionsMenu(IndexedAIP aip, DisposalHoldAIPMetadata association, int left, int top) {
    PopupPanel popup = new PopupPanel(true);
    FlowPanel menuPanel = new FlowPanel();
    menuPanel.addStyleName("groupedActionableDropdown");

    if (canApplyDisposalHolds() && canDisassociateDisposalHold(association)) {
      Button disassociateBtn = new Button(messages.disassociateDisposalHoldButton());
      disassociateBtn
        .addStyleName("actionable-button actionable-button-destroyed actionable-button-label btn-lift-hold");
      disassociateBtn.addClickHandler(e -> {
        popup.hide();
        disassociateDisposalHoldFromAIP(aip, association);
      });
      menuPanel.add(disassociateBtn);
    }

    if (canReadDisposalHolds()) {
      Button viewBtn = new Button(messages.viewDisposalHoldButton());
      viewBtn.addStyleName("actionable-button actionable-button-none actionable-button-label btn-search");
      viewBtn.addClickHandler(e -> {
        popup.hide();
        goToDisposalHold(association);
      });
      menuPanel.add(viewBtn);
    }

    if (menuPanel.getWidgetCount() == 0) {
      return;
    }

    popup.setWidget(menuPanel);
    popup.setPopupPosition(left, top);
    popup.show();
  }

  private void disassociateDisposalHoldFromAIP(IndexedAIP aip, DisposalHoldAIPMetadata association) {
    Dialogs.showConfirmDialog(messages.disassociateDisposalHoldDialogTitle(),
      messages.disassociateDisposalHoldDialogMessage(1), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(actionCallback) {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, true,
              new ActionNoAsyncCallback<String>(actionCallback) {
                @Override
                public void onSuccess(String details) {
                  DisassociateDisposalHoldRequest request = new DisassociateDisposalHoldRequest();
                  request.setClear(false);
                  request.setDetails(details);
                  request.setSelectedItems(SelectedItemsUtils
                    .convertToRESTRequest(SelectedItemsList.create(IndexedAIP.class.getName(), aip.getId())));

                  Services services = new Services("Disassociate disposal hold", "job");
                  services.disposalHoldResource(s -> s.disassociateDisposalHold(request, association.getId()))
                    .whenComplete((job, throwable) -> {
                      if (throwable != null) {
                        actionCallback.onFailure(throwable);
                        HistoryUtils.newHistory(InternalProcess.RESOLVER);
                      } else {
                        actionCallback.onSuccess(Actionable.ActionImpact.UPDATED);
                      }
                    });
                }
              });
          }
        }
      });
  }

  private void goToDisposalHold(DisposalHoldAIPMetadata association) {
    HistoryUtils.newHistory(ShowDisposalHold.RESOLVER, association.getId());
  }

  private boolean canReadDisposalSchedules() {
    return PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_SCHEDULES);
  }

  private boolean canReadDisposalHolds() {
    return PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_LIST_DISPOSAL_HOLDS);
  }

  private boolean canApplyDisposalHolds() {
    return PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_HOLD);
  }

  private boolean canReadDisposalConfirmations() {
    return PermissionClientUtils.hasPermissions(RodaConstants.PERMISSION_METHOD_RETRIEVE_DISPOSAL_CONFIRMATION);
  }

  private boolean canDisassociateDisposalHold(DisposalHoldAIPMetadata association) {
    DisposalHold hold = disposalHoldList.findDisposalHold(association.getId());
    return hold == null || !DisposalHoldState.LIFTED.equals(hold.getState());
  }
}
