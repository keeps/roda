/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.utils.SelectedItemsUtils;
import org.roda.core.data.v2.aip.AssessmentRequest;
import org.roda.core.data.v2.aip.MoveRequest;
import org.roda.core.data.v2.disposal.hold.DisposalHoldState;
import org.roda.core.data.v2.disposal.schedule.DisposalSchedule;
import org.roda.core.data.v2.disposal.schedule.DisposalScheduleState;
import org.roda.core.data.v2.generics.DeleteRequest;
import org.roda.core.data.v2.generics.select.SelectedItemsListRequest;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.NotSimpleFilterParameter;
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.index.select.SelectedItemsFilter;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.ip.disposalhold.DisassociateDisposalHoldRequest;
import org.roda.core.data.v2.representation.ChangeTypeRequest;
import org.roda.wui.client.browse.CreateDescriptiveMetadata;
import org.roda.wui.client.browse.EditPermissions;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
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
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.ingest.appraisal.IngestAppraisal;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.client.services.DisposalHoldRestService;
import org.roda.wui.client.services.DisposalScheduleRestService;
import org.roda.wui.client.services.Services;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.StringUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class AipSearchWrapperActions extends AbstractActionable<IndexedAIP> {

  public static final IndexedAIP NO_AIP_OBJECT = null;
  public static final String NO_AIP_PARENT = null;
  public static final AIPState NO_AIP_STATE = null;
  public static final String BTN_EDIT = "btn-edit";
  private static final AipSearchWrapperActions GENERAL_INSTANCE = new AipSearchWrapperActions(NO_AIP_PARENT,
    NO_AIP_STATE, null);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final Set<AipSearchWrapperAction> POSSIBLE_ACTIONS_ON_NO_AIP_TOP = new HashSet<>(
    List.of(AipSearchWrapperAction.NEW_CHILD_AIP_TOP));
  private static final Set<AipSearchWrapperAction> POSSIBLE_ACTIONS_ON_NO_AIP_BELOW = new HashSet<>(
    List.of(AipSearchWrapperAction.NEW_CHILD_AIP_BELOW));
  private static final Set<AipSearchWrapperAction> POSSIBLE_ACTIONS_ON_SINGLE_AIP = new HashSet<>(
    Arrays.asList(AipSearchWrapperAction.MOVE_IN_HIERARCHY, AipSearchWrapperAction.UPDATE_PERMISSIONS,
      AipSearchWrapperAction.REMOVE, AipSearchWrapperAction.NEW_PROCESS, AipSearchWrapperAction.CHANGE_TYPE,
      AipSearchWrapperAction.ASSOCIATE_DISPOSAL_SCHEDULE, AipSearchWrapperAction.ASSOCIATE_DISPOSAL_HOLD));
  private static final Set<AipSearchWrapperAction> POSSIBLE_ACTIONS_ON_MULTIPLE_AIPS = new HashSet<>(
    Arrays.asList(AipSearchWrapperAction.MOVE_IN_HIERARCHY, AipSearchWrapperAction.UPDATE_PERMISSIONS,
      AipSearchWrapperAction.REMOVE, AipSearchWrapperAction.NEW_PROCESS, AipSearchWrapperAction.CHANGE_TYPE,
      AipSearchWrapperAction.ASSOCIATE_DISPOSAL_SCHEDULE, AipSearchWrapperAction.ASSOCIATE_DISPOSAL_HOLD));
  private static final Set<AipSearchWrapperAction> APPRAISAL_ACTIONS = new HashSet<>(
    Arrays.asList(AipSearchWrapperAction.APPRAISAL_ACCEPT, AipSearchWrapperAction.APPRAISAL_REJECT));
  private final String parentAipId;
  private final AIPState parentAipState;
  private final Permissions permissions;

  private AipSearchWrapperActions(String parentAipId, AIPState parentAipState, Permissions permissions) {
    this.parentAipId = parentAipId;
    this.parentAipState = parentAipState;
    this.permissions = permissions;
  }

  public static AipSearchWrapperActions get() {
    return GENERAL_INSTANCE;
  }

  public static AipSearchWrapperActions get(String parentAipId, AIPState parentAipState, Permissions permissions) {
    return new AipSearchWrapperActions(parentAipId, parentAipState, permissions);
  }

  public static AipSearchWrapperActions getWithoutNoAipActions(String parentAipId, AIPState parentAipState,
    Permissions permissions) {
    return new AipSearchWrapperActions(parentAipId, parentAipState, permissions) {
      @Override
      public CanActResult contextCanAct(Action<IndexedAIP> action) {
        return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonNoObjectSelected());
      }
    };
  }

  @Override
  public AipSearchWrapperAction[] getActions() {
    return AipSearchWrapperAction.values();
  }

  @Override
  public AipSearchWrapperAction actionForName(String name) {
    return AipSearchWrapperAction.valueOf(name);
  }

  @Override
  public CanActResult userCanAct(Action<IndexedAIP> action) {
    return new CanActResult(hasPermissions(action, permissions), CanActResult.Reason.USER,
      messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedAIP> action) {
    if (!AIPState.UNDER_APPRAISAL.equals(parentAipState)) {
      if (Objects.equals(parentAipId, NO_AIP_PARENT)) {
        return new CanActResult(POSSIBLE_ACTIONS_ON_NO_AIP_TOP.contains(action), CanActResult.Reason.CONTEXT,
          messages.reasonNoParentObject());
      } else {
        return new CanActResult(POSSIBLE_ACTIONS_ON_NO_AIP_BELOW.contains(action), CanActResult.Reason.CONTEXT,
          messages.reasonNoObjectSelected());
      }
    } else {
      return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonAffectedAIPUnderAppraisal());
    }
  }

  @Override
  public CanActResult userCanAct(Action<IndexedAIP> action, IndexedAIP aip) {
    if (aip == NO_AIP_OBJECT) {
      return new CanActResult(hasPermissions(action, permissions), CanActResult.Reason.USER,
        messages.reasonUserLacksPermission());
    } else {
      return new CanActResult(hasPermissions(action, aip.getPermissions()), CanActResult.Reason.USER,
        messages.reasonUserLacksPermission());
    }
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedAIP> action, IndexedAIP aip) {
    if (aip == NO_AIP_OBJECT) {
      return new CanActResult(POSSIBLE_ACTIONS_ON_NO_AIP_BELOW.contains(action), CanActResult.Reason.CONTEXT,
        messages.reasonNoObjectSelected());
    } else if (AIPState.UNDER_APPRAISAL.equals(aip.getState()) && AIPState.UNDER_APPRAISAL.equals(parentAipState)
      && Objects.equals(parentAipId, NO_AIP_PARENT)) {
      return new CanActResult(APPRAISAL_ACTIONS.contains(action), CanActResult.Reason.CONTEXT,
        messages.reasonAffectedAIPUnderAppraisal());
    } else if (AIPState.UNDER_APPRAISAL.equals(aip.getState())) {
      return new CanActResult(APPRAISAL_ACTIONS.contains(action) || POSSIBLE_ACTIONS_ON_SINGLE_AIP.contains(action),
        CanActResult.Reason.CONTEXT, messages.reasonAIPUnderAppraisal());
    } else if (action.equals(AipSearchWrapperAction.REMOVE)
      && (aip.isOnHold() || StringUtils.isNotBlank(aip.getDisposalScheduleId()))) {
      return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonAIPProtectedByDisposalPolicy());
    } else if (StringUtils.isNotBlank(aip.getDisposalConfirmationId())
      && (action.equals(AipSearchWrapperAction.MOVE_IN_HIERARCHY)
        || action.equals(AipSearchWrapperAction.ASSOCIATE_DISPOSAL_SCHEDULE)
        || action.equals(AipSearchWrapperAction.ASSOCIATE_DISPOSAL_HOLD))) {
      return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonAIPProtectedByDisposalPolicy());
    } else if (action.equals(AipSearchWrapperAction.MOVE_IN_HIERARCHY) && aip.isOnHold()) {
      return new CanActResult(false, CanActResult.Reason.CONTEXT, messages.reasonAIPProtectedByDisposalPolicy());
    } else {
      return new CanActResult(POSSIBLE_ACTIONS_ON_SINGLE_AIP.contains(action), CanActResult.Reason.CONTEXT,
        messages.reasonCantActOnSingleObject());
    }
  }

  @Override
  public CanActResult userCanAct(Action<IndexedAIP> action, SelectedItems<IndexedAIP> objects) {
    return new CanActResult(hasPermissions(action, permissions), CanActResult.Reason.USER,
      messages.reasonUserLacksPermission());
  }

  @Override
  public CanActResult contextCanAct(Action<IndexedAIP> action, SelectedItems<IndexedAIP> objects) {
    if (AIPState.UNDER_APPRAISAL.equals(parentAipState)) {
      return new CanActResult(
        (!Objects.equals(parentAipId, NO_AIP_PARENT) && POSSIBLE_ACTIONS_ON_MULTIPLE_AIPS.contains(action))
          || APPRAISAL_ACTIONS.contains(action),
        CanActResult.Reason.CONTEXT, messages.reasonAffectedAIPUnderAppraisal());
    } else {
      return new CanActResult(POSSIBLE_ACTIONS_ON_MULTIPLE_AIPS.contains(action), CanActResult.Reason.CONTEXT,
        messages.reasonCantActOnMultipleObjects());
    }
  }

  @Override
  public void act(Action<IndexedAIP> action, AsyncCallback<ActionImpact> callback) {
    if (AipSearchWrapperAction.NEW_CHILD_AIP_TOP.equals(action)
      || AipSearchWrapperAction.NEW_CHILD_AIP_BELOW.equals(action)) {
      newChildAip(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<IndexedAIP> action, IndexedAIP aip, AsyncCallback<ActionImpact> callback) {
    if (AipSearchWrapperAction.MOVE_IN_HIERARCHY.equals(action)) {
      move(aip, callback);
    } else if (AipSearchWrapperAction.UPDATE_PERMISSIONS.equals(action)) {
      updatePermissions(aip, callback);
    } else if (AipSearchWrapperAction.REMOVE.equals(action)) {
      remove(aip, callback);
    } else if (AipSearchWrapperAction.NEW_PROCESS.equals(action)) {
      newProcess(aip, callback);
    } else if (AipSearchWrapperAction.APPRAISAL_ACCEPT.equals(action)) {
      appraisalAccept(aip, callback);
    } else if (AipSearchWrapperAction.APPRAISAL_REJECT.equals(action)) {
      appraisalReject(aip, callback);
    } else if (AipSearchWrapperAction.CHANGE_TYPE.equals(action)) {
      changeType(aip, callback);
    } else if (AipSearchWrapperAction.ASSOCIATE_DISPOSAL_SCHEDULE.equals(action)) {
      associateDisposalSchedule(aip, callback);
    } else if (AipSearchWrapperAction.ASSOCIATE_DISPOSAL_HOLD.equals(action)) {
      manageDisposalHold(aip, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  @Override
  public void act(Action<IndexedAIP> action, SelectedItems<IndexedAIP> aips, AsyncCallback<ActionImpact> callback) {
    if (AipSearchWrapperAction.MOVE_IN_HIERARCHY.equals(action)) {
      move(aips, callback);
    } else if (AipSearchWrapperAction.UPDATE_PERMISSIONS.equals(action)) {
      updatePermissions(aips, callback);
    } else if (AipSearchWrapperAction.REMOVE.equals(action)) {
      remove(aips, callback);
    } else if (AipSearchWrapperAction.NEW_PROCESS.equals(action)) {
      newProcess(aips, callback);
    } else if (AipSearchWrapperAction.APPRAISAL_ACCEPT.equals(action)) {
      appraisalAccept(aips, callback);
    } else if (AipSearchWrapperAction.APPRAISAL_REJECT.equals(action)) {
      appraisalReject(aips, callback);
    } else if (AipSearchWrapperAction.CHANGE_TYPE.equals(action)) {
      changeType(aips, callback);
    } else if (AipSearchWrapperAction.ASSOCIATE_DISPOSAL_SCHEDULE.equals(action)) {
      associateDisposalSchedule(aips, callback);
    } else if (AipSearchWrapperAction.ASSOCIATE_DISPOSAL_HOLD.equals(action)) {
      manageDisposalHold(aips, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  private void newChildAip(final AsyncCallback<ActionImpact> callback) {
    String aipType = RodaConstants.AIP_TYPE_MIXED;

    Services service = new Services("Create Child AIP", "create");

    service.aipResource(s -> s.createAIP(parentAipId, aipType)).whenComplete((value, error) -> {
      if (value != null) {
        LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
        callback.onSuccess(ActionImpact.NONE);
        HistoryUtils.newHistory(CreateDescriptiveMetadata.RESOLVER, RodaConstants.RODA_OBJECT_AIP, value.getId(),
          CreateDescriptiveMetadata.NEW);
      }
    });
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
                final SelectedItemsList<IndexedAIP> selected = new SelectedItemsList<>(Collections.singletonList(aipId),
                  IndexedAIP.class.getName());

                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new ActionNoAsyncCallback<String>(callback) {
                    @Override
                    public void onSuccess(String details) {

                      Services service = new Services("Move AIP", "move");

                      MoveRequest request = new MoveRequest();
                      request.setParentId(parentId);
                      request.setDetails(details);
                      request.setItemsToMove(SelectedItemsUtils.convertToRESTRequest(selected));

                      service.aipResource(s -> s.moveAIPInHierarchy(request)).whenComplete((value, error) -> {
                        if (error == null) {
                          Toast.showInfo(messages.moveItemTitle(), messages.movingAIP());

                          Dialogs.showJobRedirectDialog(messages.moveJobCreatedMessage(), new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                              doActionCallbackNone();
                              if (value != null) {
                                HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
                              } else {
                                HistoryUtils.newHistory(InternalProcess.RESOLVER);
                              }
                            }

                            @Override
                            public void onSuccess(final Void nothing) {
                              doActionCallbackNone();
                              HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
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
                boolean justActive = parentAipState == null || AIPState.ACTIVE.equals(parentAipState);
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
                          Services service = new Services("Move AIP", "move");

                          MoveRequest request = new MoveRequest();
                          request.setParentId(parentId);
                          request.setDetails(details);
                          request.setItemsToMove(SelectedItemsUtils.convertToRESTRequest(selected));

                          service.aipResource(s -> s.moveAIPInHierarchy(request)).whenComplete((value, error) -> {
                            if (error == null) {
                              Toast.showInfo(messages.runningInBackgroundTitle(),
                                messages.runningInBackgroundDescription());

                              doActionCallbackNone();
                              if (value != null) {
                                HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
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
            Services services = new Services("Delete AIP", "deletion");
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
              new ActionNoAsyncCallback<String>(callback) {

                @Override
                public void onSuccess(final String details) {
                  DeleteRequest request = new DeleteRequest();
                  request.setDetails(details);
                  request.setItemsToDelete(new SelectedItemsListRequest(Collections.singletonList(aip.getUUID())));
                  services.aipResource(s -> s.deleteAIPs(request)).whenComplete((value, error) -> {
                    if (error == null) {
                      Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(), new AsyncCallback<Void>() {

                        @Override
                        public void onFailure(Throwable caught) {
                          Toast.showInfo(messages.removingSuccessTitle(), messages.removingSuccessMessage(1L));
                          doActionCallbackDestroyed();
                        }

                        @Override
                        public void onSuccess(final Void nothing) {
                          doActionCallbackNone();
                          HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
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
                Services service = new Services("Delete AIPs", "deletion");
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new ActionNoAsyncCallback<String>(callback) {

                    @Override
                    public void onSuccess(final String details) {
                      DeleteRequest request = new DeleteRequest();
                      request.setDetails(details);
                      request.setSelectedItemsToDelete(selected);

                      service.aipResource(s -> s.deleteAIPs(request)).whenComplete((value, error) -> {
                        if (error == null) {
                          Toast.showInfo(messages.runningInBackgroundTitle(),
                            messages.runningInBackgroundDescription());

                          Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(), new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {

                              doActionCallbackDestroyed();
                            }

                            @Override
                            public void onSuccess(final Void nothing) {
                              doActionCallbackNone();
                              HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
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

  private void appraisalAccept(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    appraisalAccept(objectToSelectedItems(aip, IndexedAIP.class), callback);
  }

  private void appraisalAccept(final SelectedItems<IndexedAIP> aips, final AsyncCallback<ActionImpact> callback) {

    Services service = new Services("Accept assessment", "post");

    AssessmentRequest request = new AssessmentRequest();
    request.setItems(SelectedItemsUtils.convertToRESTRequest(aips));
    request.setAccept(true);

    service.aipResource(s -> s.appraisal(request)).whenComplete((value, error) -> {
      if (error == null) {
        Toast.showInfo(messages.runningInBackgroundTitle(), messages.runningInBackgroundDescription());

        Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onSuccess(ActionImpact.UPDATED);
          }

          @Override
          public void onSuccess(final Void nothing) {
            callback.onSuccess(ActionImpact.NONE);
            HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
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
          Services service = new Services("Reject Assessment", "post");

          AssessmentRequest request = new AssessmentRequest();
          request.setItems(SelectedItemsUtils.convertToRESTRequest(aips));
          request.setAccept(true);
          request.setRejectReason(rejectReason);

          service.aipResource(s -> s.appraisal(request)).whenComplete((value, error) -> {
            if (error == null) {
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
                  HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
                }
              });
            }
          });

        }
      });
  }

  private void changeType(final IndexedAIP aip, final AsyncCallback<ActionImpact> callback) {
    changeType(objectToSelectedItems(aip, IndexedAIP.class), callback);
  }

  private void changeType(final SelectedItems<IndexedAIP> aips, final AsyncCallback<ActionImpact> callback) {
    Services service = new Services("Change AIP type", "update");
    service.aipResource(s -> s.getTypeOptions(LocaleInfo.getCurrentLocale().getLocaleName()))
      .whenComplete((value, error) -> {
        if (error == null) {
          RepresentationDialogs.showPromptDialogRepresentationTypes(messages.changeTypeTitle(), null,
            messages.cancelButton(), messages.confirmButton(), value.getTypes(), value.isControlled(),
            new ActionNoAsyncCallback<String>(callback) {

              @Override
              public void onSuccess(final String newType) {
                Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
                  RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
                  new ActionNoAsyncCallback<String>(callback) {

                    @Override
                    public void onSuccess(String details) {
                      ChangeTypeRequest request = new ChangeTypeRequest();
                      request.setType(newType);
                      request.setDetails(details);
                      request.setItems(SelectedItemsUtils.convertToRESTRequest(aips));
                      service.aipResource(s -> s.changeAIPType(request)).whenComplete((value, error) -> {

                        if (error == null) {
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
                              HistoryUtils.newHistory(ShowJob.RESOLVER, value.getId());
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
        Services services = new Services("List disposal schedules", "get");
        services.disposalScheduleResource(DisposalScheduleRestService::listDisposalSchedules)
          .whenComplete((disposalSchedules, caught) -> {
            if (caught != null) {
              AsyncCallbackUtils.defaultFailureTreatment(caught);
              callback.onFailure(caught);
            } else {
              // Show the active disposal schedules only
              disposalSchedules.getObjects()
                .removeIf(schedule -> DisposalScheduleState.INACTIVE.equals(schedule.getState()));
              DisposalDialogs.showDisposalScheduleSelection(messages.disposalScheduleSelectionDialogTitle(),
                disposalSchedules, new ActionNoAsyncCallback<DisposalScheduleDialogResult>(callback) {
                  @Override
                  public void onFailure(Throwable caught) {
                    doActionCallbackNone();
                  }

                  @Override
                  public void onSuccess(DisposalScheduleDialogResult result) {
                    if (DisposalScheduleDialogResult.ActionType.ASSOCIATE.equals(result.getActionType())) {
                      associateDisposalSchedule(aips, size, result, callback);
                    } else if (DisposalScheduleDialogResult.ActionType.CLEAR.equals(result.getActionType())) {
                      disassociateDisposalSchedule(aips, size, callback);
                    }
                  }
                });
            }
          });
      }
    });
  }

  private void disassociateDisposalSchedule(SelectedItems<IndexedAIP> aips, Long size,
    AsyncCallback<ActionImpact> callback) {

    Dialogs.showConfirmDialog(messages.dissociateDisposalScheduleDialogTitle(),
      messages.dissociateDisposalScheduleDialogMessage(size), messages.dialogNo(), messages.dialogYes(),
      new ActionNoAsyncCallback<Boolean>(callback) {
        @Override
        public void onSuccess(Boolean result) {
          if (result) {
            Services services = new Services("Disassociate disposal schedule from AIP", "job");
            services
              .disposalScheduleResource(
                s -> s.disassociatedDisposalSchedule(SelectedItemsUtils.convertToRESTRequest(aips)))
              .whenComplete((job, throwable) -> {
                if (throwable != null) {
                  callback.onFailure(throwable);
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                } else {
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
            Services services = new Services("Associate disposal schedule", "job");
            services.disposalScheduleResource(s -> s
              .associatedDisposalSchedule(SelectedItemsUtils.convertToRESTRequest(aips), disposalSchedule.getId()))
              .whenComplete((job, throwable) -> {
                if (throwable != null) {
                  callback.onFailure(throwable);
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                } else {
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
        Services services = new Services("Get disposal holds", "get");
        services.disposalHoldResource(DisposalHoldRestService::listDisposalHolds)
          .whenComplete((disposalHolds, throwable) -> {
            if (throwable != null) {
              AsyncCallbackUtils.defaultFailureTreatment(throwable);
              callback.onFailure(throwable);
            } else {
              disposalHolds.getObjects().removeIf(p -> DisposalHoldState.LIFTED.equals(p.getState()));
              DisposalDialogs.showDisposalHoldSelection(messages.disposalHoldSelectionDialogTitle(), disposalHolds,
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
            Services services = new Services("Apply disposal hold", "job");
            services.disposalHoldResource(s -> s.applyDisposalHold(SelectedItemsUtils.convertToRESTRequest(aips),
              holdDialogResult.getDisposalHold().getId(), override)).whenComplete((job, throwable) -> {
                if (throwable != null) {
                  callback.onFailure(null);
                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                } else {
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
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
              new ActionNoAsyncCallback<String>(callback) {
                @Override
                public void onFailure(Throwable caught) {
                  // do nothing
                }

                @Override
                public void onSuccess(String details) {
                  DisassociateDisposalHoldRequest request = new DisassociateDisposalHoldRequest();
                  request.setSelectedItems(SelectedItemsUtils.convertToRESTRequest(aips));
                  request.setClear(true);
                  request.setDetails(details);
                  Services services = new Services("Disassociate disposal holds", "job");
                  services.disposalHoldResource(s -> s.disassociateDisposalHold(request, null))
                    .whenComplete((job, throwable) -> {
                      if (throwable != null) {
                        callback.onFailure(throwable);
                        HistoryUtils.newHistory(InternalProcess.RESOLVER);
                      } else {
                        Dialogs.showJobRedirectDialog(messages.jobCreatedMessage(), new AsyncCallback<Void>() {

                          @Override
                          public void onFailure(Throwable caught) {
                            Toast.showInfo(messages.runningInBackgroundTitle(),
                              messages.runningInBackgroundDescription());

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
    ActionableGroup<IndexedAIP> managementGroup = new ActionableGroup<>(messages.intellectualEntity(), "btn-search");
    managementGroup.addButton(messages.newArchivalPackage(), AipSearchWrapperAction.NEW_CHILD_AIP_TOP,
      ActionImpact.UPDATED, "btn-plus-circle");
    managementGroup.addButton(messages.newSublevel(), AipSearchWrapperAction.NEW_CHILD_AIP_BELOW, ActionImpact.UPDATED,
      "btn-plus-circle");
    managementGroup.addButton(messages.changeTypeButton(), AipSearchWrapperAction.CHANGE_TYPE, ActionImpact.UPDATED,
      BTN_EDIT);
    managementGroup.addButton(messages.moveArchivalPackage(), AipSearchWrapperAction.MOVE_IN_HIERARCHY,
      ActionImpact.UPDATED, BTN_EDIT);
    managementGroup.addButton(messages.archivalPackagePermissions(), AipSearchWrapperAction.UPDATE_PERMISSIONS,
      ActionImpact.UPDATED, BTN_EDIT);
    managementGroup.addButton(messages.removeArchivalPackage(), AipSearchWrapperAction.REMOVE, ActionImpact.DESTROYED,
      "btn-ban");

    // PRESERVATION
    ActionableGroup<IndexedAIP> preservationGroup = new ActionableGroup<>(messages.preservationTitle(),
      "btn-play-circle");
    preservationGroup.addButton(messages.newProcessPreservation(), AipSearchWrapperAction.NEW_PROCESS,
      ActionImpact.UPDATED, "btn-play");

    // APPRAISAL
    ActionableGroup<IndexedAIP> appraisalGroup = new ActionableGroup<>(messages.appraisalTitle());
    appraisalGroup.addButton(messages.appraisalAccept(), AipSearchWrapperAction.APPRAISAL_ACCEPT, ActionImpact.UPDATED,
      "btn-play");
    appraisalGroup.addButton(messages.appraisalReject(), AipSearchWrapperAction.APPRAISAL_REJECT,
      ActionImpact.DESTROYED, "btn-ban");

    // Disposal
    ActionableGroup<IndexedAIP> disposalGroup = new ActionableGroup<>(messages.disposalTitle(), "btn-calendar");
    disposalGroup.addButton(messages.associateDisposalScheduleButton(),
      AipSearchWrapperAction.ASSOCIATE_DISPOSAL_SCHEDULE, ActionImpact.NONE, "btn-calendar");
    disposalGroup.addButton(messages.associateDisposalHoldButton(), AipSearchWrapperAction.ASSOCIATE_DISPOSAL_HOLD,
      ActionImpact.NONE, "btn-lock");

    aipActionableBundle.addGroup(managementGroup).addGroup(disposalGroup).addGroup(preservationGroup)
      .addGroup(appraisalGroup);
    return aipActionableBundle;
  }

  public enum AipSearchWrapperAction implements Action<IndexedAIP> {
    NEW_CHILD_AIP_BELOW(RodaConstants.PERMISSION_METHOD_CREATE_AIP_BELOW),
    NEW_CHILD_AIP_TOP(RodaConstants.PERMISSION_METHOD_CREATE_AIP_TOP),
    MOVE_IN_HIERARCHY(RodaConstants.PERMISSION_METHOD_MOVE_AIP_IN_HIERARCHY),
    UPDATE_PERMISSIONS(RodaConstants.PERMISSION_METHOD_UPDATE_AIP_PERMISSIONS),
    REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_AIP), NEW_PROCESS(RodaConstants.PERMISSION_METHOD_CREATE_JOB),
    APPRAISAL_ACCEPT(RodaConstants.PERMISSION_METHOD_APPRAISAL),
    APPRAISAL_REJECT(RodaConstants.PERMISSION_METHOD_APPRAISAL),
    CHANGE_TYPE(RodaConstants.PERMISSION_METHOD_CHANGE_AIP_TYPE),
    ASSOCIATE_DISPOSAL_SCHEDULE(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_SCHEDULE),
    ASSOCIATE_DISPOSAL_HOLD(RodaConstants.PERMISSION_METHOD_ASSOCIATE_DISPOSAL_HOLD);

    private final List<String> methods;

    AipSearchWrapperAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }
}
