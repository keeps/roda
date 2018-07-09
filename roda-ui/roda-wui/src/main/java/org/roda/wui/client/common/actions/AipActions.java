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
import java.util.List;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowseAIP;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.CreateDescriptiveMetadata;
import org.roda.wui.client.browse.EditPermissions;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.LoadingAsyncCallback;
import org.roda.wui.client.common.NoAsyncCallback;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.RepresentationDialogs;
import org.roda.wui.client.common.dialogs.SelectAipDialog;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.utils.StringUtils;
import org.roda.wui.client.ingest.appraisal.IngestAppraisal;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

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
    Arrays.asList(AipAction.DOWNLOAD, AipAction.MOVE_IN_HIERARCHY, AipAction.UPDATE_PERMISSIONS, AipAction.REMOVE,
      AipAction.NEW_PROCESS, AipAction.DOWNLOAD_EVENTS, AipAction.DOWNLOAD_DOCUMENTATION, AipAction.CHANGE_TYPE));

  private static final Set<AipAction> POSSIBLE_ACTIONS_ON_MULTIPLE_AIPS = new HashSet<>(
    Arrays.asList(AipAction.MOVE_IN_HIERARCHY, AipAction.UPDATE_PERMISSIONS, AipAction.REMOVE, AipAction.NEW_PROCESS,
      AipAction.CHANGE_TYPE));

  private static final Set<AipAction> APPRAISAL_ACTIONS = new HashSet<>(
    Arrays.asList(AipAction.APPRAISAL_ACCEPT, AipAction.APPRAISAL_REJECT));

  private final String parentAipId;
  private final AIPState parentAipState;

  private AipActions(String parentAipId, AIPState parentAipState) {
    this.parentAipId = parentAipId;
    this.parentAipState = parentAipState;
  }

  public enum AipAction implements Actionable.Action<IndexedAIP> {
    NEW_CHILD_AIP, DOWNLOAD, MOVE_IN_HIERARCHY, UPDATE_PERMISSIONS, REMOVE, NEW_PROCESS, DOWNLOAD_EVENTS,
    APPRAISAL_ACCEPT, APPRAISAL_REJECT, DOWNLOAD_DOCUMENTATION, CHANGE_TYPE
  }

  public static AipActions get() {
    return GENERAL_INSTANCE;
  }

  public static AipActions get(String parentAipId, AIPState parentAipState) {
    return new AipActions(parentAipId, parentAipState);
  }

  public boolean canAct(Actionable.Action<IndexedAIP> action) {
    return POSSIBLE_ACTIONS_ON_NO_AIP.contains(action);
  }

  @Override
  public boolean canAct(Action<IndexedAIP> action, IndexedAIP aip) {
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
  public boolean canAct(Action<IndexedAIP> action, SelectedItems<IndexedAIP> objects) {
    boolean canAct;
    if (AIPState.UNDER_APPRAISAL.equals(parentAipState)) {
      canAct = POSSIBLE_ACTIONS_ON_MULTIPLE_AIPS.contains(action) || APPRAISAL_ACTIONS.contains(action);
    } else {
      canAct = POSSIBLE_ACTIONS_ON_MULTIPLE_AIPS.contains(action);
    }

    return canAct;
  }

  @Override
  public void act(Actionable.Action<IndexedAIP> action, AsyncCallback<ActionImpact> callback) {
    if (AipAction.NEW_CHILD_AIP.equals(action)) {
      newChildAip(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Actionable.Action<IndexedAIP> action, IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    if (AipAction.DOWNLOAD.equals(action)) {
      download(aip, callback);
    } else if (AipAction.MOVE_IN_HIERARCHY.equals(action)) {
      move(aip, callback);
    } else if (AipAction.UPDATE_PERMISSIONS.equals(action)) {
      updatePermissions(aip, callback);
    } else if (AipAction.REMOVE.equals(action)) {
      remove(aip, callback);
    } else if (AipAction.NEW_PROCESS.equals(action)) {
      newProcess(aip, callback);
    } else if (AipAction.DOWNLOAD_EVENTS.equals(action)) {
      downloadEvents(aip, callback);
    } else if (AipAction.APPRAISAL_ACCEPT.equals(action)) {
      appraisalAccept(aip, callback);
    } else if (AipAction.APPRAISAL_REJECT.equals(action)) {
      appraisalReject(aip, callback);
    } else if (AipAction.DOWNLOAD_DOCUMENTATION.equals(action)) {
      downloadDocumentation(aip, callback);
    } else if (AipAction.CHANGE_TYPE.equals(action)) {
      changeType(aip, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

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
    } else if (AipAction.CHANGE_TYPE.equals(action)) {
      changeType(aips, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  private void newChildAip(final AsyncCallback<ActionImpact> callback) {
    String aipType = RodaConstants.AIP_TYPE_MIXED;

    BrowserService.Util.getInstance().createAIP(parentAipId, aipType,
      new ActionAsyncCallback<String>(callback) {
        @Override
        public void onSuccess(String itemAIPId) {
        LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
        HistoryUtils.newHistory(CreateDescriptiveMetadata.RESOLVER, RodaConstants.RODA_OBJECT_AIP, itemAIPId,
          CreateDescriptiveMetadata.NEW);
          doActionCallbackUpdated();
      }
    });
  }
  private void download(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = RestUtils.createAIPDownloadUri(aip.getId());
    Window.Location.assign(downloadUri.asString());
    callback.onSuccess(ActionImpact.NONE);
  }

  private void move(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.moveConfirmDialogTitle(),
      messages.moveAllConfirmDialogMessageSingle(StringUtils.isNotBlank(aip.getTitle()) ? aip.getTitle() : aip.getId()),
      messages.dialogNo(), messages.dialogYes(),
      new NoAsyncCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            final String aipId = aip.getId();
            boolean justActive = AIPState.ACTIVE.equals(aip.getState());

            Filter filter = new Filter(new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, aipId));
            SelectAipDialog selectAipDialog = new SelectAipDialog(
              messages.moveItemTitle() + " " + (StringUtils.isNotBlank(aip.getTitle()) ? aip.getTitle() : aip.getId()),
              filter, justActive, false);
            selectAipDialog.setEmptyParentButtonVisible(true);
            selectAipDialog.setSingleSelectionMode();
            selectAipDialog.showAndCenter();
            selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IndexedAIP>() {

              @Override
              public void onValueChange(ValueChangeEvent<IndexedAIP> event) {
                final IndexedAIP parentAIP = event.getValue();
                final String parentId = (parentAIP != null) ? parentAIP.getId() : null;
                final SelectedItemsList<IndexedAIP> selected = new SelectedItemsList<>(Arrays.asList(aipId),
                  IndexedAIP.class.getName());

                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new NoAsyncCallback<String>() {
                    @Override
                    public void onSuccess(String details) {
                      BrowserService.Util.getInstance().moveAIPInHierarchy(selected, parentId, details,
                        new ActionAsyncCallback<Job>(callback) {

                          @Override
                          public void onSuccess(Job result) {
                            Toast.showInfo(messages.moveItemTitle(), messages.movingAIP());

                            if (result != null) {
                              HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                            } else {
                              HistoryUtils.newHistory(InternalProcess.RESOLVER);
                            }
                          }

                          @Override
                          public void onFailure(Throwable caught) {
                            if (caught instanceof NotFoundException) {
                              Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
                            } else {
                              super.onFailure(caught);
                            }
                          }
                        });
                    }
                  });
              }
            });
          }
        }
      });
  }

  private void move(final SelectedItems<IndexedAIP> selected, final AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, selected, new NoAsyncCallback<Long>() {

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.moveConfirmDialogTitle(), messages.moveSelectedConfirmDialogMessage(size),
          messages.dialogNo(), messages.dialogYes(), new NoAsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                int counter = 0;
                boolean justActive = parentAipState != null ? AIPState.ACTIVE.equals(parentAipState) : true;
                Filter filter = new Filter();

                if (selected instanceof SelectedItemsList) {
                  SelectedItemsList<IndexedAIP> list = (SelectedItemsList<IndexedAIP>) selected;
                  counter = list.getIds().size();
                  if (counter <= RodaConstants.DIALOG_FILTER_LIMIT_NUMBER) {
                    for (String id : list.getIds()) {
                      filter.add(new NotSimpleFilterParameter(RodaConstants.AIP_ANCESTORS, id));
                      filter.add(new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, id));
                    }
                  }

                  if (parentAipId != null) {
                    filter.add(new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, parentAipId));
                  }
                } else if (selected instanceof SelectedItemsFilter && parentAipId != null) {
                  filter.add(new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, parentAipId));
                }

                SelectAipDialog selectAipDialog = new SelectAipDialog(messages.moveItemTitle(), filter, justActive,
                  true);
                selectAipDialog.setEmptyParentButtonVisible(parentAipId != null);
                selectAipDialog.showAndCenter();
                if (counter > 0 && counter <= RodaConstants.DIALOG_FILTER_LIMIT_NUMBER) {
                  selectAipDialog.addStyleName("object-dialog");
                }

                selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IndexedAIP>() {

                  @Override
                  public void onValueChange(ValueChangeEvent<IndexedAIP> event) {
                    final IndexedAIP parentAIP = event.getValue();
                    final String parentId = (parentAIP != null) ? parentAIP.getId() : null;

                    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null,
                      messages.outcomeDetailPlaceholder(), RegExp.compile(".*"), messages.cancelButton(),
                      messages.confirmButton(), false, false, new NoAsyncCallback<String>() {

                        @Override
                        public void onSuccess(String details) {
                          BrowserService.Util.getInstance().moveAIPInHierarchy(selected, parentId, details,
                            new LoadingAsyncCallback<Job>() {

                              @Override
                              public void onSuccessImpl(Job result) {
                                Toast.showInfo(messages.runningInBackgroundTitle(),
                                  messages.runningInBackgroundDescription());

                                if (result != null) {
                                  HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                                } else {
                                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
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

  private void remove(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.removeConfirmDialogTitle(),
      messages
        .removeAllConfirmDialogMessageSingle(StringUtils.isNotBlank(aip.getTitle()) ? aip.getTitle() : aip.getId()),
      messages.dialogNo(), messages.dialogYes(),
      new NoAsyncCallback<Boolean>() {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
              new NoAsyncCallback<String>() {

                @Override
                public void onSuccess(final String details) {
                  final String parentId = aip.getParentID();

                  BrowserService.Util.getInstance().deleteAIP(objectToSelectedItems(aip, IndexedAIP.class), details,
                    new ActionAsyncCallback<Job>(callback) {

                      @Override
                      public void onSuccess(Job result) {
                        Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(), new AsyncCallback<Void>() {

                          @Override
                          public void onFailure(Throwable caught) {
                            Toast.showInfo(messages.removingSuccessTitle(), messages.removingSuccessMessage(1L));

                            Timer timer = new Timer() {
                              @Override
                              public void run() {
                                if (parentId != null) {
                                  HistoryUtils.newHistory(BrowseAIP.RESOLVER, parentId);
                                } else {
                                  HistoryUtils.newHistory(BrowseAIP.RESOLVER);
                                }
                                doActionCallbackDestroyed();
                              }
                            };

                            timer.schedule(RodaConstants.ACTION_TIMEOUT);
                          }

                          @Override
                          public void onSuccess(final Void nothing) {
                            HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                          }
                        });
                      }
                    });
                }
              });
          }
        }
      });
  }

  private void remove(final SelectedItems<IndexedAIP> selected, final AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, selected, new NoAsyncCallback<Long>() {

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.removeConfirmDialogTitle(),
          messages.removeSelectedConfirmDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
          new NoAsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new NoAsyncCallback<String>() {

                    @Override
                    public void onSuccess(final String details) {
                      BrowserService.Util.getInstance().deleteAIP(selected, details, new LoadingAsyncCallback<Job>() {

                        @Override
                        public void onFailureImpl(Throwable caught) {
                          callback.onFailure(caught);
                        }

                        @Override
                        public void onSuccessImpl(Job result) {
                          Toast.showInfo(messages.runningInBackgroundTitle(),
                            messages.runningInBackgroundDescription());

                          Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(), new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                              callback.onSuccess(ActionImpact.DESTROYED);
                            }

                            @Override
                            public void onSuccess(final Void nothing) {
                              HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                            }
                          });

                        }
                      });
                    }
                  });
              }
            }
          });
      }
    });
  }

  private void newProcess(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton.getInstance().setSelectedItems(objectToSelectedItems(aip, IndexedAIP.class));
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
    callback.onSuccess(ActionImpact.NONE);
  }

  private void newProcess(SelectedItems<IndexedAIP> aips, AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton.getInstance().setSelectedItems(aips);
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
    callback.onSuccess(ActionImpact.NONE);
  }

  private void downloadEvents(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = RestUtils.createPreservationMetadataDownloadUri(aip.getId());
    Window.Location.assign(downloadUri.asString());
    callback.onSuccess(ActionImpact.NONE);
  }

  private void appraisalAccept(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    final boolean accept = true;
    String rejectReason = null;
    BrowserService.Util.getInstance().appraisal(objectToSelectedItems(aip, IndexedAIP.class), accept, rejectReason,
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
    Dialogs.showPromptDialog(messages.rejectMessage(), messages.rejectQuestion(), null, null, RegExp.compile(".+"),
      messages.dialogCancel(), messages.dialogOk(), true, false, new NoAsyncCallback<String>() {

        @Override
        public void onSuccess(final String rejectReason) {
          BrowserService.Util.getInstance().appraisal(objectToSelectedItems(aip, IndexedAIP.class), accept,
            rejectReason,
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
    Dialogs.showPromptDialog(messages.rejectMessage(), messages.rejectQuestion(), null, null, RegExp.compile(".+"),
      messages.dialogCancel(), messages.dialogOk(), true, false, new NoAsyncCallback<String>() {

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

  private void downloadDocumentation(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    BrowserService.Util.getInstance().hasDocumentation(aip.getId(), new ActionAsyncCallback<Boolean>(callback) {

      @Override
      public void onSuccess(Boolean result) {
        if (result) {
          SafeUri downloadUri = RestUtils.createAIPPartDownloadUri(aip.getId(),
            RodaConstants.STORAGE_DIRECTORY_DOCUMENTATION);
          Window.Location.assign(downloadUri.asString());
        } else {
          Toast.showInfo(messages.downloadNoDocumentationTitle(), messages.downloadNoDocumentationDescription());
        }

        doActionCallbackNone();
      }
    });
  }

  public void changeType(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    changeType(objectToSelectedItems(aip, IndexedAIP.class), callback);
  }

  private void changeType(final SelectedItems<IndexedAIP> aips, final AsyncCallback<ActionImpact> callback) {
    BrowserService.Util.getInstance().retrieveAIPTypeOptions(LocaleInfo.getCurrentLocale().getLocaleName(),
      new NoAsyncCallback<Pair<Boolean, List<String>>>() {

        @Override
        public void onSuccess(Pair<Boolean, List<String>> result) {
          RepresentationDialogs.showPromptDialogRepresentationTypes(messages.changeTypeTitle(), null,
            messages.cancelButton(), messages.confirmButton(), result.getSecond(), result.getFirst(),
            new NoAsyncCallback<String>() {

              @Override
              public void onSuccess(final String newType) {
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new NoAsyncCallback<String>() {

                    @Override
                    public void onSuccess(String details) {
                      BrowserService.Util.getInstance().changeAIPType(aips, newType, details,
                        new LoadingAsyncCallback<Void>() {

                          @Override
                          public void onSuccessImpl(Void nothing) {
                            Toast.showInfo(messages.dialogSuccess(), messages.changeTypeSuccessful());
                            callback.onSuccess(ActionImpact.UPDATED);
                          }
                        });
                    }
                  });
              }
            });
        }
      });
  }

  @Override
  public ActionsBundle<IndexedAIP> createActionsBundle() {
    ActionsBundle<IndexedAIP> aipActionsBundle = new ActionsBundle<>();

    // MANAGEMENT
    ActionsGroup<IndexedAIP> managementGroup = new ActionsGroup<>(messages.intellectualEntity());
    managementGroup.addButton(messages.newArchivalPackage(), AipAction.NEW_CHILD_AIP, ActionImpact.UPDATED, "btn-plus");
    managementGroup.addButton(messages.changeTypeButton(), AipAction.CHANGE_TYPE, ActionImpact.UPDATED, "btn-edit");
    managementGroup.addButton(messages.moveArchivalPackage(), AipAction.MOVE_IN_HIERARCHY, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.archivalPackagePermissions(), AipAction.UPDATE_PERMISSIONS, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.removeArchivalPackage(), AipAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");
    managementGroup.addButton(messages.downloadButton(), AipAction.DOWNLOAD, ActionImpact.NONE, "btn-download");

    // PRESERVATION
    ActionsGroup<IndexedAIP> preservationGroup = new ActionsGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.newProcessPreservation(), AipAction.NEW_PROCESS, ActionImpact.UPDATED,
      "btn-play");
    preservationGroup.addButton(messages.preservationEventsDownloadButton(), AipAction.DOWNLOAD_EVENTS,
      ActionImpact.NONE, "btn-download");

    // APPRAISAL
    ActionsGroup<IndexedAIP> appraisalGroup = new ActionsGroup<>(messages.appraisalTitle());
    appraisalGroup.addButton(messages.appraisalAccept(), AipAction.APPRAISAL_ACCEPT, ActionImpact.UPDATED, "btn-play");
    appraisalGroup.addButton(messages.appraisalReject(), AipAction.APPRAISAL_REJECT, ActionImpact.DESTROYED, "btn-ban");
    appraisalGroup.addButton(messages.downloadDocumentation(), AipAction.DOWNLOAD_DOCUMENTATION, ActionImpact.NONE,
      "btn-download");

    aipActionsBundle.addGroup(managementGroup).addGroup(preservationGroup)
      .addGroup(appraisalGroup);

    return aipActionsBundle;
  }
}
