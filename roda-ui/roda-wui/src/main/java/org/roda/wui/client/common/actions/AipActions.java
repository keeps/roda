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
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIP;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.disposal.DisposalHoldState;
import org.roda.core.data.v2.ip.disposal.DisposalHolds;
import org.roda.core.data.v2.ip.disposal.DisposalSchedule;
import org.roda.core.data.v2.ip.disposal.DisposalScheduleState;
import org.roda.core.data.v2.ip.disposal.DisposalSchedules;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.browse.CreateDescriptiveMetadata;
import org.roda.wui.client.browse.EditPermissions;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionLoadingAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.DisposalDialogs;
import org.roda.wui.client.common.dialogs.RepresentationDialogs;
import org.roda.wui.client.common.dialogs.SelectAipDialog;
import org.roda.wui.client.common.dialogs.utils.DisposalHoldDialogResult;
import org.roda.wui.client.common.dialogs.utils.DisposalScheduleDialogResult;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.ingest.appraisal.IngestAppraisal;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.tools.StringUtils;
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

  private static final AipActions GENERAL_INSTANCE = new AipActions(NO_AIP_PARENT, NO_AIP_STATE, null);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<AipAction> POSSIBLE_ACTIONS_ON_NO_AIP_TOP = new HashSet<>(
    Arrays.asList(AipAction.NEW_CHILD_AIP_TOP));

  private static final Set<AipAction> POSSIBLE_ACTIONS_ON_NO_AIP_BELOW = new HashSet<>(
    Arrays.asList(AipAction.NEW_CHILD_AIP_BELOW));

  private static final Set<AipAction> POSSIBLE_ACTIONS_ON_SINGLE_AIP = new HashSet<>(Arrays.asList(AipAction.DOWNLOAD,
    AipAction.MOVE_IN_HIERARCHY, AipAction.UPDATE_PERMISSIONS, AipAction.REMOVE, AipAction.NEW_PROCESS,
    AipAction.DOWNLOAD_EVENTS, AipAction.DOWNLOAD_DOCUMENTATION, AipAction.DOWNLOAD_SUBMISSIONS, AipAction.CHANGE_TYPE,
    AipAction.ASSOCIATE_DISPOSAL_SCHEDULE, AipAction.ASSOCIATE_DISPOSAL_HOLD));

  private static final Set<AipAction> POSSIBLE_ACTIONS_ON_MULTIPLE_AIPS = new HashSet<>(
    Arrays.asList(AipAction.MOVE_IN_HIERARCHY, AipAction.UPDATE_PERMISSIONS, AipAction.REMOVE, AipAction.NEW_PROCESS,
      AipAction.CHANGE_TYPE, AipAction.ASSOCIATE_DISPOSAL_SCHEDULE, AipAction.ASSOCIATE_DISPOSAL_HOLD));

  private static final Set<AipAction> APPRAISAL_ACTIONS = new HashSet<>(
    Arrays.asList(AipAction.APPRAISAL_ACCEPT, AipAction.APPRAISAL_REJECT));

  private final String parentAipId;
  private final AIPState parentAipState;
  private final Permissions permissions;

  private AipActions(String parentAipId, AIPState parentAipState, Permissions permissions) {
    this.parentAipId = parentAipId;
    this.parentAipState = parentAipState;
    this.permissions = permissions;
  }

  public enum AipAction implements Action<IndexedAIP> {
    NEW_CHILD_AIP_BELOW(RodaConstants.PERMISSION_METHOD_CREATE_AIP_BELOW),
    NEW_CHILD_AIP_TOP(RodaConstants.PERMISSION_METHOD_CREATE_AIP_TOP), DOWNLOAD(),
    MOVE_IN_HIERARCHY(RodaConstants.PERMISSION_METHOD_MOVE_AIP_IN_HIERARCHY),
    UPDATE_PERMISSIONS(RodaConstants.PERMISSION_METHOD_UPDATE_AIP_PERMISSIONS),
    REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_AIP), NEW_PROCESS(RodaConstants.PERMISSION_METHOD_CREATE_JOB),
    DOWNLOAD_EVENTS(), DOWNLOAD_SUBMISSIONS(), APPRAISAL_ACCEPT(RodaConstants.PERMISSION_METHOD_APPRAISAL),
    APPRAISAL_REJECT(RodaConstants.PERMISSION_METHOD_APPRAISAL), DOWNLOAD_DOCUMENTATION(),
    CHANGE_TYPE(RodaConstants.PERMISSION_METHOD_CHANGE_AIP_TYPE),
    ASSOCIATE_DISPOSAL_SCHEDULE(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_SCHEDULE),
    ASSOCIATE_DISPOSAL_HOLD(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_HOLD);

    private List<String> methods;

    AipAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  @Override
  public AipAction[] getActions() {
    return AipAction.values();
  }

  @Override
  public AipAction actionForName(String name) {
    return AipAction.valueOf(name);
  }

  public static AipActions get() {
    return GENERAL_INSTANCE;
  }

  public static AipActions get(String parentAipId, AIPState parentAipState, Permissions permissions) {
    return new AipActions(parentAipId, parentAipState, permissions);
  }

  public static AipActions getWithoutNoAipActions(String parentAipId, AIPState parentAipState,
    Permissions permissions) {
    return new AipActions(parentAipId, parentAipState, permissions) {
      @Override
      public boolean canAct(Action<IndexedAIP> action) {
        return false;
      }
    };
  }

  @Override
  public boolean canAct(Action<IndexedAIP> action) {
    if (!AIPState.UNDER_APPRAISAL.equals(parentAipState)) {
      if (parentAipId == NO_AIP_PARENT) {
        return hasPermissions(action, permissions) && POSSIBLE_ACTIONS_ON_NO_AIP_TOP.contains(action);
      } else {
        return hasPermissions(action, permissions) && POSSIBLE_ACTIONS_ON_NO_AIP_BELOW.contains(action);
      }
    } else {
      return false;
    }
  }

  @Override
  public boolean canAct(Action<IndexedAIP> action, IndexedAIP aip) {
    if (aip == NO_AIP_OBJECT) {
      return hasPermissions(action, permissions) && POSSIBLE_ACTIONS_ON_NO_AIP_BELOW.contains(action);
    } else if (AIPState.UNDER_APPRAISAL.equals(aip.getState()) && AIPState.UNDER_APPRAISAL.equals(parentAipState)
      && parentAipId == NO_AIP_PARENT) {
      return hasPermissions(action, aip.getPermissions()) && APPRAISAL_ACTIONS.contains(action);
    } else if (AIPState.UNDER_APPRAISAL.equals(aip.getState())) {
      return hasPermissions(action, aip.getPermissions())
        && (POSSIBLE_ACTIONS_ON_SINGLE_AIP.contains(action) || APPRAISAL_ACTIONS.contains(action));
    } else if (action.equals(AipAction.REMOVE)
      && (aip.isOnHold() || StringUtils.isNotBlank(aip.getDisposalScheduleId()))) {
      return false;
    } else if (StringUtils.isNotBlank(aip.getDisposalConfirmationId()) && (action.equals(AipAction.MOVE_IN_HIERARCHY)
      || action.equals(AipAction.ASSOCIATE_DISPOSAL_SCHEDULE) || action.equals(AipAction.ASSOCIATE_DISPOSAL_HOLD))) {
      return false;
    } else if (action.equals(AipAction.MOVE_IN_HIERARCHY) && aip.isOnHold()) {
      return false;
    } else {
      return hasPermissions(action, aip.getPermissions()) && POSSIBLE_ACTIONS_ON_SINGLE_AIP.contains(action);
    }
  }

  @Override
  public boolean canAct(Action<IndexedAIP> action, SelectedItems<IndexedAIP> objects) {
    boolean canAct = false;

    if (hasPermissions(action, permissions)) {
      if (AIPState.UNDER_APPRAISAL.equals(parentAipState)) {
        canAct = (parentAipId != NO_AIP_PARENT && POSSIBLE_ACTIONS_ON_MULTIPLE_AIPS.contains(action))
          || APPRAISAL_ACTIONS.contains(action);
      } else {
        canAct = POSSIBLE_ACTIONS_ON_MULTIPLE_AIPS.contains(action);
      }
    }

    return canAct;
  }

  @Override
  public void act(Action<IndexedAIP> action, AsyncCallback<ActionImpact> callback) {
    if (AipAction.NEW_CHILD_AIP_TOP.equals(action) || AipAction.NEW_CHILD_AIP_BELOW.equals(action)) {
      newChildAip(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<IndexedAIP> action, IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
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
    } else if (AipAction.DOWNLOAD_SUBMISSIONS.equals(action)) {
      downloadSubmissions(aip, callback);
    } else if (AipAction.APPRAISAL_ACCEPT.equals(action)) {
      appraisalAccept(aip, callback);
    } else if (AipAction.APPRAISAL_REJECT.equals(action)) {
      appraisalReject(aip, callback);
    } else if (AipAction.DOWNLOAD_DOCUMENTATION.equals(action)) {
      downloadDocumentation(aip, callback);
    } else if (AipAction.CHANGE_TYPE.equals(action)) {
      changeType(aip, callback);
    } else if (AipAction.ASSOCIATE_DISPOSAL_SCHEDULE.equals(action)) {
      associateDisposalSchedule(aip, callback);
    } else if (AipAction.ASSOCIATE_DISPOSAL_HOLD.equals(action)) {
      manageDisposalHold(aip, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<IndexedAIP> action, SelectedItems<IndexedAIP> aips, AsyncCallback<ActionImpact> callback) {
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
    } else if (AipAction.ASSOCIATE_DISPOSAL_SCHEDULE.equals(action)) {
      associateDisposalSchedule(aips, callback);
    } else if (AipAction.ASSOCIATE_DISPOSAL_HOLD.equals(action)) {
      manageDisposalHold(aips, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  private void newChildAip(final AsyncCallback<ActionImpact> callback) {
    String aipType = RodaConstants.AIP_TYPE_MIXED;

    BrowserService.Util.getInstance().createAIP(parentAipId, aipType, new ActionAsyncCallback<String>(callback) {
      @Override
      public void onSuccess(String itemAIPId) {
        LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
        doActionCallbackNone();
        HistoryUtils.newHistory(CreateDescriptiveMetadata.RESOLVER, RodaConstants.RODA_OBJECT_AIP, itemAIPId,
          CreateDescriptiveMetadata.NEW);
      }
    });
  }

  private void download(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = RestUtils.createAIPDownloadUri(aip.getId());
    callback.onSuccess(ActionImpact.NONE);
    Window.Location.assign(downloadUri.asString());
  }

  private void move(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.moveConfirmDialogTitle(),
      messages.moveAllConfirmDialogMessageSingle(StringUtils.isNotBlank(aip.getTitle()) ? aip.getTitle() : aip.getId()),
      messages.dialogNo(), messages.dialogYes(), new ActionAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            final String aipId = aip.getId();
            boolean justActive = AIPState.ACTIVE.equals(aip.getState());

            Filter filter = new Filter(new NotSimpleFilterParameter(RodaConstants.INDEX_UUID, aipId));
            SelectAipDialog selectAipDialog = new SelectAipDialog(
              messages.moveItemTitle() + " " + (StringUtils.isNotBlank(aip.getTitle()) ? aip.getTitle() : aip.getId()),
              filter, justActive);
            selectAipDialog.setEmptyParentButtonVisible(true);
            selectAipDialog.setSingleSelectionMode();
            selectAipDialog.showAndCenter();
            selectAipDialog.addCloseHandler(e -> callback.onSuccess(ActionImpact.NONE));
            selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IndexedAIP>() {

              @Override
              public void onValueChange(ValueChangeEvent<IndexedAIP> event) {
                final IndexedAIP parentAIP = event.getValue();
                final String parentId = (parentAIP != null) ? parentAIP.getId() : null;
                final SelectedItemsList<IndexedAIP> selected = new SelectedItemsList<>(Arrays.asList(aipId),
                  IndexedAIP.class.getName());

                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new ActionNoAsyncCallback<String>(callback) {
                    @Override
                    public void onSuccess(String details) {
                      BrowserService.Util.getInstance().moveAIPInHierarchy(selected, parentId, details,
                        new ActionNoAsyncCallback<Job>(callback) {

                          @Override
                          public void onSuccess(Job result) {
                            Toast.showInfo(messages.moveItemTitle(), messages.movingAIP());

                            Dialogs.showJobRedirectDialog(messages.moveJobCreatedMessage(), new AsyncCallback<Void>() {

                              @Override
                              public void onFailure(Throwable caught) {
                                doActionCallbackNone();
                                if (result != null) {
                                  HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                                } else {
                                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                                }
                              }

                              @Override
                              public void onSuccess(final Void nothing) {
                                doActionCallbackNone();
                                HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                              }
                            });
                          }
                        });
                    }
                  });
              }
            });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void move(final SelectedItems<IndexedAIP> selected, final AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, selected, new ActionNoAsyncCallback<Long>(callback) {

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.moveConfirmDialogTitle(), messages.moveSelectedConfirmDialogMessage(size),
          messages.dialogNo(), messages.dialogYes(), new ActionNoAsyncCallback<Boolean>(callback) {

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

                SelectAipDialog selectAipDialog = new SelectAipDialog(messages.moveItemTitle(), filter, justActive);
                selectAipDialog.setEmptyParentButtonVisible(parentAipId != null);
                selectAipDialog.showAndCenter();
                if (counter > 0 && counter <= RodaConstants.DIALOG_FILTER_LIMIT_NUMBER) {
                  selectAipDialog.addStyleName("object-dialog");
                }

                selectAipDialog.addCloseHandler(e -> callback.onSuccess(ActionImpact.NONE));
                selectAipDialog.addValueChangeHandler(new ValueChangeHandler<IndexedAIP>() {

                  @Override
                  public void onValueChange(ValueChangeEvent<IndexedAIP> event) {
                    final IndexedAIP parentAIP = event.getValue();
                    final String parentId = (parentAIP != null) ? parentAIP.getId() : null;

                    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null,
                      messages.outcomeDetailPlaceholder(), RegExp.compile(".*"), messages.cancelButton(),
                      messages.confirmButton(), false, false, new ActionNoAsyncCallback<String>(callback) {

                        @Override
                        public void onSuccess(String details) {
                          BrowserService.Util.getInstance().moveAIPInHierarchy(selected, parentId, details,
                            new ActionLoadingAsyncCallback<Job>(callback) {

                              @Override
                              public void onSuccessImpl(Job result) {
                                Toast.showInfo(messages.runningInBackgroundTitle(),
                                  messages.runningInBackgroundDescription());

                                doActionCallbackNone();
                                if (result != null) {
                                  HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                                } else {
                                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
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
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(EditPermissions.AIP_RESOLVER, aip.getId());
  }

  private void updatePermissions(SelectedItems<IndexedAIP> aips, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton.getInstance().setSelectedItems(aips);
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(EditPermissions.AIP_RESOLVER);
  }

  private void remove(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.removeConfirmDialogTitle(),
      messages
        .removeAllConfirmDialogMessageSingle(StringUtils.isNotBlank(aip.getTitle()) ? aip.getTitle() : aip.getId()),
      messages.dialogNo(), messages.dialogYes(), new ActionNoAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
              new ActionNoAsyncCallback<String>(callback) {

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
                            doActionCallbackDestroyed();
                          }

                          @Override
                          public void onSuccess(final Void nothing) {
                            doActionCallbackNone();
                            HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                          }
                        });
                      }
                    });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void remove(final SelectedItems<IndexedAIP> selected, final AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, selected, new ActionNoAsyncCallback<Long>(callback) {

      @Override
      public void onSuccess(final Long size) {
        Dialogs.showConfirmDialog(messages.removeConfirmDialogTitle(),
          messages.removeSelectedConfirmDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
          new ActionNoAsyncCallback<Boolean>(callback) {

            @Override
            public void onSuccess(Boolean confirmed) {
              if (confirmed) {
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new ActionNoAsyncCallback<String>(callback) {

                    @Override
                    public void onSuccess(final String details) {
                      BrowserService.Util.getInstance().deleteAIP(selected, details,
                        new ActionLoadingAsyncCallback<Job>(callback) {

                          @Override
                          public void onSuccessImpl(Job result) {
                            Toast.showInfo(messages.runningInBackgroundTitle(),
                              messages.runningInBackgroundDescription());

                            Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(),
                              new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                  doActionCallbackDestroyed();
                                }

                                @Override
                                public void onSuccess(final Void nothing) {
                                  doActionCallbackNone();
                                  HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                                }
                              });
                          }
                        });
                    }
                  });
              } else {
                doActionCallbackNone();
              }
            }
          });
      }
    });
  }

  private void newProcess(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton.getInstance().setSelectedItems(objectToSelectedItems(aip, IndexedAIP.class));
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
  }

  private void newProcess(SelectedItems<IndexedAIP> aips, AsyncCallback<ActionImpact> callback) {
    callback.onSuccess(ActionImpact.NONE);
    LastSelectedItemsSingleton.getInstance().setSelectedItems(aips);
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
  }

  private void downloadEvents(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = RestUtils.createPreservationMetadataDownloadUri(aip.getId());
    callback.onSuccess(ActionImpact.NONE);
    Window.Location.assign(downloadUri.asString());
  }

  private void appraisalAccept(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    appraisalAccept(objectToSelectedItems(aip, IndexedAIP.class), callback);
  }

  private void appraisalAccept(final SelectedItems<IndexedAIP> aips, final AsyncCallback<ActionImpact> callback) {
    BrowserService.Util.getInstance().appraisal(aips, true, null, new ActionLoadingAsyncCallback<Job>(callback) {

      @Override
      public void onSuccessImpl(Job result) {
        Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

        Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            doActionCallbackUpdated();
          }

          @Override
          public void onSuccess(final Void nothing) {
            doActionCallbackNone();
            HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
          }
        });
      }
    });
  }

  private void appraisalReject(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    appraisalReject(objectToSelectedItems(aip, IndexedAIP.class), callback);
  }

  private void appraisalReject(final SelectedItems<IndexedAIP> aips, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showPromptDialog(messages.rejectMessage(), messages.rejectQuestion(), null, null, RegExp.compile(".+"),
      messages.dialogCancel(), messages.dialogOk(), true, false, new ActionNoAsyncCallback<String>(callback) {

        @Override
        public void onSuccess(final String rejectReason) {
          BrowserService.Util.getInstance().appraisal(aips, false, rejectReason,
            new ActionLoadingAsyncCallback<Job>(callback) {

              @Override
              public void onSuccessImpl(Job result) {
                Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                  @Override
                  public void onFailure(Throwable caught) {
                    doActionCallbackNone();
                    HistoryUtils.newHistory(IngestAppraisal.RESOLVER);
                  }

                  @Override
                  public void onSuccess(final Void nothing) {
                    doActionCallbackNone();
                    HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                  }
                });
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
          doActionCallbackNone();
          Window.Location.assign(downloadUri.asString());
        } else {
          Toast.showInfo(messages.downloadNoDocumentationTitle(), messages.downloadNoDocumentationDescription());
          doActionCallbackNone();
        }
      }
    });
  }

  private void downloadSubmissions(IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    BrowserService.Util.getInstance().hasSubmissions(aip.getId(), new ActionAsyncCallback<Boolean>(callback) {
      @Override
      public void onSuccess(Boolean result) {
        if (result) {
          SafeUri downloadUri = RestUtils.createAIPPartDownloadUri(aip.getId(),
            RodaConstants.STORAGE_DIRECTORY_SUBMISSION);
          doActionCallbackNone();
          Window.Location.assign(downloadUri.asString());
        } else {
          Toast.showInfo(messages.downloadNoSubmissionsTitle(), messages.downloadNoSubmissionsDescription());
          doActionCallbackNone();
        }
      }
    });
  }

  private void changeType(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    changeType(objectToSelectedItems(aip, IndexedAIP.class), callback);
  }

  private void changeType(final SelectedItems<IndexedAIP> aips, final AsyncCallback<ActionImpact> callback) {
    BrowserService.Util.getInstance().retrieveAIPTypeOptions(LocaleInfo.getCurrentLocale().getLocaleName(),
      new ActionNoAsyncCallback<Pair<Boolean, List<String>>>(callback) {

        @Override
        public void onSuccess(Pair<Boolean, List<String>> result) {
          RepresentationDialogs.showPromptDialogRepresentationTypes(messages.changeTypeTitle(), null,
            messages.cancelButton(), messages.confirmButton(), result.getSecond(), result.getFirst(),
            new ActionNoAsyncCallback<String>(callback) {

              @Override
              public void onSuccess(final String newType) {
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new ActionNoAsyncCallback<String>(callback) {

                    @Override
                    public void onSuccess(String details) {
                      BrowserService.Util.getInstance().changeAIPType(aips, newType, details,
                        new ActionLoadingAsyncCallback<Job>(callback) {

                          @Override
                          public void onSuccessImpl(Job result) {
                            Toast.showInfo(messages.runningInBackgroundTitle(),
                              messages.runningInBackgroundDescription());

                            Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                              @Override
                              public void onFailure(Throwable caught) {
                                doActionCallbackUpdated();
                              }

                              @Override
                              public void onSuccess(final Void nothing) {
                                doActionCallbackNone();
                                HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                              }
                            });
                          }
                        });
                    }
                  });
              }
            });
        }
      });
  }

  private void associateDisposalSchedule(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    associateDisposalSchedule(objectToSelectedItems(aip, IndexedAIP.class), callback);
  }

  private void associateDisposalSchedule(final SelectedItems<IndexedAIP> aips,
    final AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, aips, new ActionNoAsyncCallback<Long>(callback) {
      @Override
      public void onSuccess(final Long size) {
        BrowserService.Util.getInstance().listDisposalSchedules(new ActionNoAsyncCallback<DisposalSchedules>(callback) {
          @Override
          public void onSuccess(DisposalSchedules schedules) {
            // Show the active disposal schedules only
            schedules.getObjects().removeIf(schedule -> DisposalScheduleState.INACTIVE.equals(schedule.getState()));
            DisposalDialogs.showDisposalScheduleSelection(messages.disposalScheduleSelectionDialogTitle(), schedules,
              new ActionNoAsyncCallback<DisposalScheduleDialogResult>(callback) {
                @Override
                public void onFailure(Throwable caught) {
                  doActionCallbackNone();
                }

                @Override
                public void onSuccess(DisposalScheduleDialogResult result) {
                  if (DisposalScheduleDialogResult.ActionType.ASSOCIATE.equals(result.getActionType())) {
                    associateDisposalSchedule(aips, size, result, callback);
                  } else if (DisposalScheduleDialogResult.ActionType.CLEAR.equals(result.getActionType())) {
                    disassociateDisposalSchedule(aips, size, result, callback);
                  }
                }
              });
          }
        });
      }
    });
  }

  private void disassociateDisposalSchedule(SelectedItems<IndexedAIP> aips, Long size,
    DisposalScheduleDialogResult dialogResult, AsyncCallback<ActionImpact> callback) {

    Dialogs.showConfirmDialog(messages.dissociateDisposalScheduleDialogTitle(),
      messages.dissociateDisposalScheduleDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            BrowserService.Util.getInstance().disassociateDisposalSchedule(aips, new ActionAsyncCallback<Job>(callback) {
                @Override
                public void onFailure(Throwable caught) {
                  callback.onFailure(caught);
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                }

                @Override
                public void onSuccess(Job job) {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                      Timer timer = new Timer() {
                        @Override
                        public void run() {
                          doActionCallbackUpdated();
                        }
                      };

                      timer.schedule(RodaConstants.ACTION_TIMEOUT);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      doActionCallbackNone();
                      HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                    }
                  });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void associateDisposalSchedule(SelectedItems<IndexedAIP> aips, Long size,
    DisposalScheduleDialogResult dialogResult, AsyncCallback<ActionImpact> callback) {
    DisposalSchedule disposalSchedule = dialogResult.getDisposalSchedule();

    Dialogs.showConfirmDialog(messages.associateDisposalScheduleDialogTitle(),
      messages.associateDisposalScheduleDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            BrowserService.Util.getInstance().associateDisposalSchedule(aips, disposalSchedule.getId(),
              new ActionAsyncCallback<Job>(callback) {

                @Override
                public void onFailure(Throwable caught) {
                  callback.onFailure(caught);
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                }

                @Override
                public void onSuccess(Job job) {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                      Timer timer = new Timer() {
                        @Override
                        public void run() {
                          doActionCallbackUpdated();
                        }
                      };

                      timer.schedule(RodaConstants.ACTION_TIMEOUT);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      doActionCallbackNone();
                      HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                    }
                  });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void manageDisposalHold(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    manageDisposalHold(objectToSelectedItems(aip, IndexedAIP.class), callback);
  }

  private void manageDisposalHold(final SelectedItems<IndexedAIP> aips, final AsyncCallback<ActionImpact> callback) {
    ClientSelectedItemsUtils.size(IndexedAIP.class, aips, new ActionNoAsyncCallback<Long>(callback) {
      @Override
      public void onSuccess(final Long size) {
        BrowserService.Util.getInstance().listDisposalHolds(new ActionNoAsyncCallback<DisposalHolds>(callback) {
          @Override
          public void onSuccess(DisposalHolds holds) {
            holds.getObjects().removeIf(p -> DisposalHoldState.LIFTED.equals(p.getState()));
            DisposalDialogs.showDisposalHoldSelection(messages.disposalHoldSelectionDialogTitle(), holds,
              new ActionNoAsyncCallback<DisposalHoldDialogResult>(callback) {
                @Override
                public void onFailure(Throwable caught) {
                  doActionCallbackNone();
                }

                @Override
                public void onSuccess(DisposalHoldDialogResult result) {
                  if (DisposalHoldDialogResult.ActionType.CLEAR.equals(result.getActionType())) {
                    clearDisposalHolds(aips, size, callback);
                  } else if (DisposalHoldDialogResult.ActionType.ASSOCIATE.equals(result.getActionType())) {
                    applyDisposalHold(aips, size, result, false, callback);
                  } else if (DisposalHoldDialogResult.ActionType.OVERRIDE.equals(result.getActionType())) {
                    applyDisposalHold(aips, size, result, true, callback);
                  }
                }
              });
          }
        });
      }
    });
  }

  private void applyDisposalHold(final SelectedItems<IndexedAIP> aips, final Long size,
    DisposalHoldDialogResult holdDialogResult, boolean override, final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.applyDisposalHoldDialogTitle(),
      messages.applyDisposalHoldDialogMessage(size.intValue()), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            BrowserService.Util.getInstance().applyDisposalHold(aips, holdDialogResult.getDisposalHold().getId(),
              override, new ActionAsyncCallback<Job>(callback) {
                @Override
                public void onFailure(Throwable caught) {
                  callback.onFailure(caught);
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                }

                @Override
                public void onSuccess(Job job) {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                      Timer timer = new Timer() {
                        @Override
                        public void run() {
                          doActionCallbackUpdated();
                        }
                      };

                      timer.schedule(RodaConstants.ACTION_TIMEOUT);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      doActionCallbackNone();
                      HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                    }
                  });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  private void clearDisposalHolds(final SelectedItems<IndexedAIP> aips, final Long size,
    final AsyncCallback<ActionImpact> callback) {
    Dialogs.showConfirmDialog(messages.clearDisposalHoldDialogTitle(),
      messages.clearDisposalHoldDialogMessage(size.intValue()), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            BrowserService.Util.getInstance().disassociateDisposalHold(aips, null, true,
              new ActionAsyncCallback<Job>(callback) {
                @Override
                public void onFailure(Throwable caught) {
                  callback.onFailure(caught);
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                }

                @Override
                public void onSuccess(Job job) {
                  Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                      Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

                      Timer timer = new Timer() {
                        @Override
                        public void run() {
                          doActionCallbackUpdated();
                        }
                      };

                      timer.schedule(RodaConstants.ACTION_TIMEOUT);
                    }

                    @Override
                    public void onSuccess(final Void nothing) {
                      doActionCallbackNone();
                      HistoryUtils.newHistory(ShowJob.RESOLVER, job.getId());
                    }
                  });
                }
              });
          } else {
            doActionCallbackNone();
          }
        }
      });
  }

  @Override
  public ActionableBundle<IndexedAIP> createActionsBundle() {
    ActionableBundle<IndexedAIP> aipActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<IndexedAIP> managementGroup = new ActionableGroup<>(messages.intellectualEntity());
    managementGroup.addButton(messages.newArchivalPackage(), AipAction.NEW_CHILD_AIP_TOP, ActionImpact.UPDATED,
      "btn-plus-circle");
    managementGroup.addButton(messages.newSublevel(), AipAction.NEW_CHILD_AIP_BELOW, ActionImpact.UPDATED,
      "btn-plus-circle");
    managementGroup.addButton(messages.changeTypeButton(), AipAction.CHANGE_TYPE, ActionImpact.UPDATED, "btn-edit");
    managementGroup.addButton(messages.moveArchivalPackage(), AipAction.MOVE_IN_HIERARCHY, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.archivalPackagePermissions(), AipAction.UPDATE_PERMISSIONS, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.removeArchivalPackage(), AipAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");

    // PRESERVATION
    ActionableGroup<IndexedAIP> preservationGroup = new ActionableGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.newProcessPreservation(), AipAction.NEW_PROCESS, ActionImpact.UPDATED,
      "btn-play");

    // APPRAISAL
    ActionableGroup<IndexedAIP> appraisalGroup = new ActionableGroup<>(messages.appraisalTitle());
    appraisalGroup.addButton(messages.appraisalAccept(), AipAction.APPRAISAL_ACCEPT, ActionImpact.UPDATED, "btn-play");
    appraisalGroup.addButton(messages.appraisalReject(), AipAction.APPRAISAL_REJECT, ActionImpact.DESTROYED, "btn-ban");

    // DOWNLOAD
    ActionableGroup<IndexedAIP> downloadGroup = new ActionableGroup<>(messages.downloadButton());
    downloadGroup.addButton(messages.downloadButton() + " " + messages.oneOfAObject(AIP.class.getName()),
      AipAction.DOWNLOAD, ActionImpact.NONE, "btn-download");
    downloadGroup.addButton(messages.preservationEventsDownloadButton(), AipAction.DOWNLOAD_EVENTS, ActionImpact.NONE,
      "btn-download");
    downloadGroup.addButton(messages.downloadDocumentation(), AipAction.DOWNLOAD_DOCUMENTATION, ActionImpact.NONE,
      "btn-download");
    downloadGroup.addButton(messages.downloadSubmissions(), AipAction.DOWNLOAD_SUBMISSIONS, ActionImpact.NONE,
      "btn-download");

    // Disposal
    ActionableGroup<IndexedAIP> disposalGroup = new ActionableGroup<>(messages.disposalTitle());
    disposalGroup.addButton(messages.associateDisposalScheduleButton(), AipAction.ASSOCIATE_DISPOSAL_SCHEDULE,
      ActionImpact.NONE, "fas fa-calendar");
    disposalGroup.addButton(messages.associateDisposalHoldButton(), AipAction.ASSOCIATE_DISPOSAL_HOLD,
      ActionImpact.NONE, "fas fa-lock");

    aipActionableBundle.addGroup(managementGroup).addGroup(disposalGroup).addGroup(preservationGroup)
      .addGroup(appraisalGroup).addGroup(downloadGroup);
    return aipActionableBundle;
  }
}
