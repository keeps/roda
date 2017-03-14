/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.wui.client.browse.BrowseAIP;
import org.roda.wui.client.browse.BrowseRepresentation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.CreateDescriptiveMetadata;
import org.roda.wui.client.browse.EditPermissions;
import org.roda.wui.client.browse.PreservationEvents;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.SelectAipDialog;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.ingest.appraisal.IngestAppraisal;
import org.roda.wui.client.management.UserLog;
import org.roda.wui.client.planning.RiskIncidenceRegister;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class AipActions extends AbstractActionable<IndexedAIP> {

  public static final IndexedAIP NO_AIP_OBJECT = null;
  public static final String NO_AIP_PARENT = null;
  public static final AIPState NO_AIP_STATE = null;

  private static final AipActions GENERAL_INSTANCE = new AipActions(NO_AIP_PARENT, NO_AIP_STATE);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<AipAction> POSSIBLE_ACTIONS_ON_NO_AIP = new HashSet<>(
    Arrays.asList(AipAction.NEW_CHILD_AIP));

  private static final Set<AipAction> POSSIBLE_ACTIONS_ON_SINGLE_AIP = new HashSet<>(
    Arrays.asList(AipAction.NEW_CHILD_AIP, AipAction.DOWNLOAD, AipAction.MOVE_IN_HIERARCHY,
      AipAction.UPDATE_PERMISSIONS, AipAction.ADD_REPRESENTATION, AipAction.REMOVE, AipAction.NEW_PROCESS,
      AipAction.SHOW_EVENTS, AipAction.SHOW_RISKS, AipAction.SHOW_LOGS));

  private static final Set<AipAction> POSSIBLE_ACTIONS_ON_MULTIPLE_AIPS = new HashSet<>(
    Arrays.asList(AipAction.MOVE_IN_HIERARCHY, AipAction.UPDATE_PERMISSIONS, AipAction.REMOVE, AipAction.NEW_PROCESS));

  private static final Set<AipAction> APPRAISAL_ACTIONS = new HashSet<>(
    Arrays.asList(AipAction.APPRAISAL_ACCEPT, AipAction.APPRAISAL_REJECT));

  private final String parentAipId;
  private final AIPState parentAipState;

  private AipActions(String parentAipId, AIPState parentAipState) {
    this.parentAipId = parentAipId;
    this.parentAipState = parentAipState;
  }

  public enum AipAction implements Actionable.Action<IndexedAIP> {
    NEW_CHILD_AIP, DOWNLOAD, MOVE_IN_HIERARCHY, UPDATE_PERMISSIONS, ADD_REPRESENTATION, REMOVE, NEW_PROCESS,
    SHOW_EVENTS, SHOW_RISKS, SHOW_LOGS, APPRAISAL_ACCEPT, APPRAISAL_REJECT;
  }

  public static AipActions get() {
    return GENERAL_INSTANCE;
  }

  public static AipActions get(String parentAipId, AIPState parentAipState) {
    return new AipActions(parentAipId, parentAipState);
  }

  @Override
  public boolean canAct(Actionable.Action<IndexedAIP> action, IndexedAIP aip) {
    boolean canAct;
    if (aip == NO_AIP_OBJECT) {
      canAct = POSSIBLE_ACTIONS_ON_NO_AIP.contains(action);
    } else if (AIPState.UNDER_APPRAISAL.equals(aip.getState())) {
      canAct = POSSIBLE_ACTIONS_ON_SINGLE_AIP.contains(action) || APPRAISAL_ACTIONS.contains(action);
    } else {
      canAct = POSSIBLE_ACTIONS_ON_SINGLE_AIP.contains(action);
    }

    return canAct;
  }

  @Override
  public boolean canAct(Actionable.Action<IndexedAIP> action, SelectedItems<IndexedAIP> selectedItems) {
    boolean canAct;
    if (AIPState.UNDER_APPRAISAL.equals(parentAipState)) {
      canAct = POSSIBLE_ACTIONS_ON_MULTIPLE_AIPS.contains(action) || APPRAISAL_ACTIONS.contains(action);
    } else {
      canAct = POSSIBLE_ACTIONS_ON_MULTIPLE_AIPS.contains(action);
    }

    return canAct;
  }

  @Override
  public void act(Actionable.Action<IndexedAIP> action, IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    if (AipAction.NEW_CHILD_AIP.equals(action)) {
      newChildAip(aip, callback);
    } else if (AipAction.DOWNLOAD.equals(action)) {
      download(aip, callback);
    } else if (AipAction.MOVE_IN_HIERARCHY.equals(action)) {
      move(aip, callback);
    } else if (AipAction.UPDATE_PERMISSIONS.equals(action)) {
      updatePermissions(aip, callback);
    } else if (AipAction.ADD_REPRESENTATION.equals(action)) {
      addRepresentation(aip, callback);
    } else if (AipAction.REMOVE.equals(action)) {
      remove(aip, callback);
    } else if (AipAction.NEW_PROCESS.equals(action)) {
      newProcess(aip, callback);
    } else if (AipAction.SHOW_EVENTS.equals(action)) {
      showEvents(aip, callback);
    } else if (AipAction.SHOW_RISKS.equals(action)) {
      showRisks(aip, callback);
    } else if (AipAction.SHOW_LOGS.equals(action)) {
      showLogs(aip, callback);
    } else if (AipAction.APPRAISAL_ACCEPT.equals(action)) {
      appraisalAccept(aip, callback);
    } else if (AipAction.APPRAISAL_REJECT.equals(action)) {
      appraisalReject(aip, callback);
    } else {
      callback.onFailure(new RequestNotValidException("Unsupported action in this context: " + action));
    }
  }

  /**
   * Act on multiple files from different representations
   */
  @Override
  public void act(Actionable.Action<IndexedAIP> action, SelectedItems<IndexedAIP> aips,
    AsyncCallback<ActionImpact> callback) {
    if (AipAction.MOVE_IN_HIERARCHY.equals(action)) {
      move(aips, callback);
    } else if (AipAction.UPDATE_PERMISSIONS.equals(action)) {
      updatePermissions(aips, callback);
    } else if (AipAction.REMOVE.equals(action)) {
      remove(aips, callback);
    } else if (AipAction.NEW_PROCESS.equals(action)) {
      newProcess(aips, callback);
    } else if (AipAction.APPRAISAL_ACCEPT.equals(action)) {
      appraisalAccept(aips, callback);
    } else if (AipAction.APPRAISAL_REJECT.equals(action)) {
      appraisalReject(aips, callback);
    } else {
      callback.onFailure(new RequestNotValidException("Unsupported action in this context: " + action));
    }
  }

  // ACTIONS
  private void newChildAip(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {

    String newChildParentAipId = aip != null ? aip.getId() : null;
    String aipType = RodaConstants.AIP_TYPE_MIXED;
    BrowserService.Util.getInstance().createAIP(newChildParentAipId, aipType, new AsyncCallback<String>() {

      @Override
      public void onFailure(Throwable caught) {
        callback.onFailure(caught);
      }

      @Override
      public void onSuccess(String itemAIPId) {
        LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
        HistoryUtils.newHistory(CreateDescriptiveMetadata.RESOLVER, RodaConstants.RODA_OBJECT_AIP, itemAIPId,
          CreateDescriptiveMetadata.NEW);
        callback.onSuccess(ActionImpact.UPDATED);
      }
    });
  }

  private void download(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri;
    downloadUri = RestUtils.createAIPDownloadUri(aip.getId());
    Window.Location.assign(downloadUri.asString());
    callback.onSuccess(ActionImpact.NONE);
  }

  private void move(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    final String aipId = aip.getId();
    boolean justActive = AIPState.ACTIVE.equals(aip.getState());

    Filter filter = new Filter(new NotSimpleFilterParameter(RodaConstants.AIP_ANCESTORS, aipId),
      new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, aipId));
    SelectAipDialog selectAipDialog = new SelectAipDialog(messages.moveItemTitle(), filter, justActive, false);
    selectAipDialog.setEmptyParentButtonVisible(aip.getParentID() != null);
    selectAipDialog.setSingleSelectionMode();
    selectAipDialog.showAndCenter();
    selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IndexedAIP>() {

      @Override
      public void onValueChange(ValueChangeEvent<IndexedAIP> event) {
        final IndexedAIP parentAIP = event.getValue();
        final String parentId = (parentAIP != null) ? parentAIP.getId() : null;
        final SelectedItemsList<IndexedAIP> selected = new SelectedItemsList<>(Arrays.asList(aipId),
          IndexedAIP.class.getName());

        Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
          RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
              // do nothing
            }

            @Override
            public void onSuccess(String details) {
              BrowserService.Util.getInstance().moveAIPInHierarchy(selected, parentId, details,
                new AsyncCallback<IndexedAIP>() {

                  @Override
                  public void onSuccess(IndexedAIP result) {
                    Toast.showInfo(messages.moveItemTitle(), messages.movingAIP());

                    if (result != null) {
                      HistoryUtils.newHistory(BrowseAIP.RESOLVER, result.getId());
                    } else {
                      HistoryUtils.newHistory(BrowseAIP.RESOLVER);
                    }
                  }

                  @Override
                  public void onFailure(Throwable caught) {
                    if (caught instanceof NotFoundException) {
                      Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
                    } else {
                      callback.onFailure(caught);
                    }
                  }
                });
            }
          });
      }
    });
  }

  private void move(final SelectedItems<IndexedAIP> selected, final AsyncCallback<ActionImpact> callback) {
    int counter = 0;
    boolean justActive = parentAipState != null ? AIPState.ACTIVE.equals(parentAipState) : true;

    // Move all selected
    Filter filter = new Filter();
    boolean showEmptyParentButton;

    if (parentAipId != null) {
      filter.add(new NotSimpleFilterParameter(RodaConstants.AIP_ANCESTORS, parentAipId));
      filter.add(new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, parentAipId));
      showEmptyParentButton = true;
    } else {
      if (selected instanceof SelectedItemsList) {
        SelectedItemsList<IndexedAIP> list = (SelectedItemsList<IndexedAIP>) selected;
        counter = list.getIds().size();
        if (counter <= RodaConstants.DIALOG_FILTER_LIMIT_NUMBER) {
          for (String id : list.getIds()) {
            filter.add(new NotSimpleFilterParameter(RodaConstants.AIP_ANCESTORS, id));
            filter.add(new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, id));
          }
        }
      }
      if (selected instanceof SelectedItemsFilter) {
        filter = ((SelectedItemsFilter<IndexedAIP>) selected).getFilter();
      } else {
        filter = Filter.ALL;
      }
      showEmptyParentButton = false;
    }

    SelectAipDialog selectAipDialog = new SelectAipDialog(messages.moveItemTitle(), filter, justActive, true);
    selectAipDialog.setEmptyParentButtonVisible(showEmptyParentButton);
    selectAipDialog.showAndCenter();
    if (counter > 0 && counter <= RodaConstants.DIALOG_FILTER_LIMIT_NUMBER) {
      selectAipDialog.addStyleName("object-dialog");
    }
    selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IndexedAIP>() {

      @Override
      public void onValueChange(ValueChangeEvent<IndexedAIP> event) {
        final IndexedAIP parentAIP = event.getValue();
        final String parentId = (parentAIP != null) ? parentAIP.getId() : null;

        Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
          RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
              // do nothing
            }

            @Override
            public void onSuccess(String details) {
              BrowserService.Util.getInstance().moveAIPInHierarchy(selected, parentId, details,
                new LoadingAsyncCallback<IndexedAIP>() {

                  @Override
                  public void onSuccessImpl(IndexedAIP result) {
                    if (result != null) {
                      HistoryUtils.newHistory(BrowseAIP.RESOLVER, result.getId());
                    } else {
                      HistoryUtils.newHistory(BrowseAIP.RESOLVER);
                    }
                    callback.onSuccess(ActionImpact.UPDATED);
                  }

                  @Override
                  public void onFailureImpl(Throwable caught) {
                    if (caught instanceof NotFoundException) {
                      Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
                    } else {
                      callback.onFailure(caught);
                    }
                  }
                });
            }
          });
      }
    });

  }

  private void updatePermissions(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(BrowseAIP.RESOLVER, EditPermissions.AIP_RESOLVER.getHistoryToken(), aip.getId());
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void updatePermissions(SelectedItems<IndexedAIP> aips, AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton.getInstance().setSelectedItems(aips);
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(BrowseAIP.RESOLVER, EditPermissions.AIP_RESOLVER.getHistoryToken());
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void addRepresentation(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // do nothing
        }

        @Override
        public void onSuccess(String details) {
          BrowserService.Util.getInstance().createRepresentation(aip.getId(), details,
            new LoadingAsyncCallback<String>() {

              @Override
              public void onSuccessImpl(String representationId) {
                HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, aip.getId(), representationId);
                callback.onSuccess(ActionImpact.UPDATED);
              }
            });
        }
      });
  }

  private void remove(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.removeConfirmDialogTitle(), messages.removeAllConfirmDialogMessage(),
      messages.dialogNo(), messages.dialogYes(), new AsyncCallback<Boolean>() {

        @Override
        public void onFailure(Throwable caught) {
          // nothing to do
        }

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

                @Override
                public void onFailure(Throwable caught) {
                  // do nothing
                }

                @Override
                public void onSuccess(final String details) {
                  final String parentId = aip.getParentID();

                  BrowserService.Util.getInstance().deleteAIP(objectToSelectedItems(aip), details,
                    new AsyncCallback<Void>() {

                      @Override
                      public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                      }

                      @Override
                      public void onSuccess(Void result) {
                        Toast.showInfo(messages.removingSuccessTitle(), messages.removingSuccessMessage(1L));
                        if (parentId != null) {
                          HistoryUtils.newHistory(BrowseAIP.RESOLVER, parentId);
                        } else {
                          HistoryUtils.newHistory(BrowseAIP.RESOLVER);
                        }
                        callback.onSuccess(ActionImpact.DESTROYED);
                      }
                    });
                }
              });
          }
        }
      });
  }

  private void remove(final SelectedItems<IndexedAIP> selected, final AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, selected, new AsyncCallback<Long>() {

      @Override
      public void onFailure(Throwable caught) {
        AsyncCallbackUtils.defaultFailureTreatment(caught);
      }

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.removeConfirmDialogTitle(),
          messages.removeSelectedConfirmDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
          new AsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), new AsyncCallback<String>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      // do nothing
                    }

                    @Override
                    public void onSuccess(final String details) {
                      BrowserService.Util.getInstance().deleteAIP(selected, details, new LoadingAsyncCallback<Void>() {

                        @Override
                        public void onFailureImpl(Throwable caught) {
                          callback.onFailure(caught);
                        }

                        @Override
                        public void onSuccessImpl(Void result) {
                          Toast.showInfo(messages.removingSuccessTitle(), messages.removingSuccessMessage(size));
                          callback.onSuccess(ActionImpact.DESTROYED);
                        }
                      });
                    }
                  });
              }
            }

            @Override
            public void onFailure(Throwable caught) {
              // do nothing
            }
          });
      }
    });
  }

  private void newProcess(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton.getInstance().setSelectedItems(objectToSelectedItems(aip));
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void newProcess(SelectedItems<IndexedAIP> aips, AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton.getInstance().setSelectedItems(aips);
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void showEvents(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    HistoryUtils.newHistory(BrowseAIP.RESOLVER, PreservationEvents.BROWSE_RESOLVER.getHistoryToken(), aip.getId());
    callback.onSuccess(ActionImpact.NONE);
  }

  private void showRisks(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    HistoryUtils.newHistory(RiskIncidenceRegister.RESOLVER, aip.getId());
    callback.onSuccess(ActionImpact.NONE);
  }

  private void showLogs(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    HistoryUtils.newHistory(UserLog.RESOLVER, aip.getId());
    callback.onSuccess(ActionImpact.NONE);
  }

  private void appraisalAccept(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    final boolean accept = true;
    String rejectReason = null;
    BrowserService.Util.getInstance().appraisal(objectToSelectedItems(aip), accept, rejectReason,
      LocaleInfo.getCurrentLocale().getLocaleName(), new LoadingAsyncCallback<Void>() {

        @Override
        public void onSuccessImpl(Void result) {
          Toast.showInfo(messages.dialogDone(), messages.itemWasAccepted());
          callback.onSuccess(ActionImpact.UPDATED);
        }
      });
  }

  private void appraisalAccept(final SelectedItems<IndexedAIP> aips, final AsyncCallback<ActionImpact> callback) {
    final boolean accept = true;
    String rejectReason = null;
    BrowserService.Util.getInstance().appraisal(aips, accept, rejectReason,
      LocaleInfo.getCurrentLocale().getLocaleName(), new LoadingAsyncCallback<Void>() {

        @Override
        public void onSuccessImpl(Void result) {
          Toast.showInfo(messages.dialogDone(), messages.itemWasAccepted());
          callback.onSuccess(ActionImpact.UPDATED);
        }
      });
  }

  private void appraisalReject(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    final boolean accept = false;
    Dialogs.showPromptDialog(messages.rejectMessage(), messages.rejectQuestion(), null, RegExp.compile(".+"),
      messages.dialogCancel(), messages.dialogOk(), new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // nothing to do
        }

        @Override
        public void onSuccess(final String rejectReason) {
          BrowserService.Util.getInstance().appraisal(objectToSelectedItems(aip), accept, rejectReason,
            LocaleInfo.getCurrentLocale().getLocaleName(), new LoadingAsyncCallback<Void>() {

              @Override
              public void onSuccessImpl(Void result) {
                Toast.showInfo(messages.dialogDone(), messages.itemWasRejected());
                HistoryUtils.newHistory(IngestAppraisal.RESOLVER);
                callback.onSuccess(ActionImpact.DESTROYED);
              }
            });
        }
      });
  }

  private void appraisalReject(final SelectedItems<IndexedAIP> aips, final AsyncCallback<ActionImpact> callback) {
    final boolean accept = false;
    Dialogs.showPromptDialog(messages.rejectMessage(), messages.rejectQuestion(), null, RegExp.compile(".+"),
      messages.dialogCancel(), messages.dialogOk(), new AsyncCallback<String>() {

        @Override
        public void onFailure(Throwable caught) {
          // nothing to do
        }

        @Override
        public void onSuccess(final String rejectReason) {
          BrowserService.Util.getInstance().appraisal(aips, accept, rejectReason,
            LocaleInfo.getCurrentLocale().getLocaleName(), new LoadingAsyncCallback<Void>() {

              @Override
              public void onSuccessImpl(Void result) {
                Toast.showInfo(messages.dialogDone(), messages.itemWasRejected());
                HistoryUtils.newHistory(IngestAppraisal.RESOLVER);
                callback.onSuccess(ActionImpact.DESTROYED);
              }
            });
        }
      });
  }

  @Override
  public Widget createActionsLayout(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    FlowPanel layout = createLayout();

    // MANAGEMENT
    addTitle(layout, messages.intellectualEntity(), aip, AipAction.NEW_CHILD_AIP, AipAction.MOVE_IN_HIERARCHY,
      AipAction.UPDATE_PERMISSIONS, AipAction.ADD_REPRESENTATION, AipAction.REMOVE, AipAction.DOWNLOAD);

    addButton(layout, messages.newArchivalPackage(), AipAction.NEW_CHILD_AIP, aip, ActionImpact.UPDATED, callback,
      "btn-plus");

    addButton(layout, messages.moveArchivalPackage(), AipAction.MOVE_IN_HIERARCHY, aip, ActionImpact.UPDATED, callback,
      "btn-edit");
    addButton(layout, messages.archivalPackagePermissions(), AipAction.UPDATE_PERMISSIONS, aip, ActionImpact.UPDATED,
      callback, "btn-edit");

    addButton(layout, messages.addRepresentation(), AipAction.ADD_REPRESENTATION, aip, ActionImpact.UPDATED, callback,
      "btn-plus");

    addButton(layout, messages.removeArchivalPackage(), AipAction.REMOVE, aip, ActionImpact.DESTROYED, callback,
      "btn-ban");

    addButton(layout, messages.downloadButton(), AipAction.DOWNLOAD, aip, ActionImpact.NONE, callback, "btn-download");

    // PRESERVATION
    addTitle(layout, messages.preservationTitle(), aip, AipAction.NEW_PROCESS, AipAction.SHOW_EVENTS,
      AipAction.SHOW_RISKS, AipAction.SHOW_LOGS);

    addButton(layout, messages.newProcessPreservation(), AipAction.NEW_PROCESS, aip, ActionImpact.UPDATED, callback,
      "btn-play");

    addButton(layout, messages.preservationEvents(), AipAction.SHOW_EVENTS, aip, ActionImpact.NONE, callback,
      "btn-clock");
    addButton(layout, messages.preservationLogs(), AipAction.SHOW_LOGS, aip, ActionImpact.NONE, callback, "btn-clock");
    addButton(layout, messages.preservationRisks(), AipAction.SHOW_RISKS, aip, ActionImpact.NONE, callback,
      "btn-exclamation-triangle");

    // APPRAISAL
    addTitle(layout, messages.appraisalTitle(), aip, AipAction.APPRAISAL_ACCEPT, AipAction.APPRAISAL_REJECT);

    addButton(layout, messages.appraisalAccept(), AipAction.APPRAISAL_ACCEPT, aip, ActionImpact.UPDATED, callback,
      "btn-play");

    addButton(layout, messages.appraisalReject(), AipAction.APPRAISAL_REJECT, aip, ActionImpact.DESTROYED, callback,
      "btn-ban");

    return layout;
  }

  @Override
  public Widget createActionsLayout(SelectedItems<IndexedAIP> aips, AsyncCallback<ActionImpact> callback) {
    FlowPanel layout = createLayout();

    // MANAGEMENT
    addTitle(layout, messages.intellectualEntity(), aips, AipAction.NEW_CHILD_AIP, AipAction.MOVE_IN_HIERARCHY,
      AipAction.UPDATE_PERMISSIONS, AipAction.ADD_REPRESENTATION, AipAction.REMOVE, AipAction.DOWNLOAD);

    addButton(layout, messages.moveArchivalPackage(), AipAction.MOVE_IN_HIERARCHY, aips, ActionImpact.UPDATED, callback,
      "btn-edit");
    addButton(layout, messages.archivalPackagePermissions(), AipAction.UPDATE_PERMISSIONS, aips, ActionImpact.UPDATED,
      callback, "btn-edit");

    addButton(layout, messages.addRepresentation(), AipAction.ADD_REPRESENTATION, aips, ActionImpact.UPDATED, callback,
      "btn-plus");

    addButton(layout, messages.removeArchivalPackage(), AipAction.REMOVE, aips, ActionImpact.DESTROYED, callback,
      "btn-ban");

    addButton(layout, messages.downloadButton(), AipAction.DOWNLOAD, aips, ActionImpact.NONE, callback, "btn-download");

    // PRESERVATION
    addTitle(layout, messages.preservationTitle(), aips, AipAction.NEW_PROCESS, AipAction.SHOW_EVENTS,
      AipAction.SHOW_RISKS, AipAction.SHOW_LOGS);

    addButton(layout, messages.newProcessPreservation(), AipAction.NEW_PROCESS, aips, ActionImpact.UPDATED, callback,
      "btn-play");

    addButton(layout, messages.preservationEvents(), AipAction.SHOW_EVENTS, aips, ActionImpact.NONE, callback,
      "btn-clock");
    addButton(layout, messages.preservationLogs(), AipAction.SHOW_LOGS, aips, ActionImpact.NONE, callback, "btn-clock");
    addButton(layout, messages.preservationRisks(), AipAction.SHOW_RISKS, aips, ActionImpact.NONE, callback,
      "btn-exclamation-triangle");

    // APPRAISAL
    addTitle(layout, messages.appraisalTitle(), aips, AipAction.APPRAISAL_ACCEPT, AipAction.APPRAISAL_REJECT);

    addButton(layout, messages.appraisalAccept(), AipAction.APPRAISAL_ACCEPT, aips, ActionImpact.UPDATED, callback,
      "btn-play");

    addButton(layout, messages.appraisalReject(), AipAction.APPRAISAL_REJECT, aips, ActionImpact.DESTROYED, callback,
      "btn-ban");

    return layout;
  }

}
