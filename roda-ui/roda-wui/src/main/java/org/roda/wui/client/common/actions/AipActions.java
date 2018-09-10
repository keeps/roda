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
import org.roda.core.data.v2.ip.AIPState;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.Permissions;
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
import org.roda.wui.client.common.dialogs.RepresentationDialogs;
import org.roda.wui.client.common.dialogs.SelectAipDialog;
import org.roda.wui.client.common.lists.utils.ClientSelectedItemsUtils;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
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
    DOWNLOAD_EVENTS(), APPRAISAL_ACCEPT(RodaConstants.PERMISSION_METHOD_APPRAISAL),
    APPRAISAL_REJECT(RodaConstants.PERMISSION_METHOD_APPRAISAL), DOWNLOAD_DOCUMENTATION(),
    CHANGE_TYPE(RodaConstants.PERMISSION_METHOD_CHANGE_AIP_TYPE);

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

                            Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(),
                              new AsyncCallback<Void>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                  if (result != null) {
                                    HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                                  } else {
                                    HistoryUtils.newHistory(InternalProcess.RESOLVER);
                                  }
                                  doActionCallbackUpdated();
                                }

                                @Override
                                public void onSuccess(final Void nothing) {
                                  HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                                  doActionCallbackDestroyed();
                                }
                              });
                          }

                          @Override
                          public void onFailure(Throwable caught) {
                            AsyncCallbackUtils.defaultFailureTreatment(caught);
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

                                if (result != null) {
                                  HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                                } else {
                                  HistoryUtils.newHistory(InternalProcess.RESOLVER);
                                }
                                callback.onSuccess(ActionImpact.UPDATED);
                              }

                              @Override
                              public void onFailureImpl(Throwable caught) {
                                AsyncCallbackUtils.defaultFailureTreatment(caught);
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
    HistoryUtils.newHistory(EditPermissions.AIP_RESOLVER, aip.getId());
    callback.onSuccess(ActionImpact.UPDATED);
  }

  private void updatePermissions(SelectedItems<IndexedAIP> aips, AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton.getInstance().setSelectedItems(aips);
    LastSelectedItemsSingleton.getInstance().setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(EditPermissions.AIP_RESOLVER);
    callback.onSuccess(ActionImpact.UPDATED);
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
                            HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                            doActionCallbackNone();
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
                                  HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                                  doActionCallbackDestroyed();
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
            HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
            doActionCallbackUpdated();
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
                    HistoryUtils.newHistory(IngestAppraisal.RESOLVER);
                    doActionCallbackDestroyed();
                  }

                  @Override
                  public void onSuccess(final Void nothing) {
                    HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                    doActionCallbackDestroyed();
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
                                HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                                doActionCallbackUpdated();
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

  @Override
  public ActionableBundle<IndexedAIP> createActionsBundle() {
    ActionableBundle<IndexedAIP> aipActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<IndexedAIP> managementGroup = new ActionableGroup<>(messages.intellectualEntity());
    managementGroup.addButton(messages.newArchivalPackage(), AipAction.NEW_CHILD_AIP_TOP, ActionImpact.UPDATED,
      "btn-plus-circle");
    managementGroup.addButton(messages.newArchivalPackage(), AipAction.NEW_CHILD_AIP_BELOW, ActionImpact.UPDATED,
      "btn-plus-circle");
    managementGroup.addButton(messages.changeTypeButton(), AipAction.CHANGE_TYPE, ActionImpact.UPDATED, "btn-edit");
    managementGroup.addButton(messages.moveArchivalPackage(), AipAction.MOVE_IN_HIERARCHY, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.archivalPackagePermissions(), AipAction.UPDATE_PERMISSIONS, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.removeArchivalPackage(), AipAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");
    managementGroup.addButton(messages.downloadButton(), AipAction.DOWNLOAD, ActionImpact.NONE, "btn-download");

    // PRESERVATION
    ActionableGroup<IndexedAIP> preservationGroup = new ActionableGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.newProcessPreservation(), AipAction.NEW_PROCESS, ActionImpact.UPDATED,
      "btn-play");
    preservationGroup.addButton(messages.preservationEventsDownloadButton(), AipAction.DOWNLOAD_EVENTS,
      ActionImpact.NONE, "btn-download");

    // APPRAISAL
    ActionableGroup<IndexedAIP> appraisalGroup = new ActionableGroup<>(messages.appraisalTitle());
    appraisalGroup.addButton(messages.appraisalAccept(), AipAction.APPRAISAL_ACCEPT, ActionImpact.UPDATED, "btn-play");
    appraisalGroup.addButton(messages.appraisalReject(), AipAction.APPRAISAL_REJECT, ActionImpact.DESTROYED, "btn-ban");
    appraisalGroup.addButton(messages.downloadDocumentation(), AipAction.DOWNLOAD_DOCUMENTATION, ActionImpact.NONE,
      "btn-download");

    aipActionableBundle.addGroup(managementGroup).addGroup(preservationGroup).addGroup(appraisalGroup);
    return aipActionableBundle;
  }
}
