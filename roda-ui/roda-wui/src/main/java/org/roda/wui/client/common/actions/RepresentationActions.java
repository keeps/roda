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
import org.roda.core.data.v2.index.select.SelectedItems;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.core.data.v2.ip.Permissions;
import org.roda.core.data.v2.jobs.Job;
import org.roda.wui.client.browse.BrowseRepresentation;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.LastSelectedItemsSingleton;
import org.roda.wui.client.common.actions.callbacks.ActionAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionLoadingAsyncCallback;
import org.roda.wui.client.common.actions.callbacks.ActionNoAsyncCallback;
import org.roda.wui.client.common.actions.model.ActionableBundle;
import org.roda.wui.client.common.actions.model.ActionableGroup;
import org.roda.wui.client.common.dialogs.Dialogs;
import org.roda.wui.client.common.dialogs.RepresentationDialogs;
import org.roda.wui.client.ingest.process.ShowJob;
import org.roda.wui.client.process.CreateSelectedJob;
import org.roda.wui.client.process.InternalProcess;
import org.roda.wui.common.client.tools.HistoryUtils;
import org.roda.wui.common.client.tools.RestUtils;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import config.i18n.client.ClientMessages;

public class RepresentationActions extends AbstractActionable<IndexedRepresentation> {
  private static final RepresentationActions GENERAL_INSTANCE = new RepresentationActions(null, null);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static final Set<RepresentationAction> POSSIBLE_ACTIONS_WITHOUT_REPRESENTATION = new HashSet<>(
    Arrays.asList(RepresentationAction.NEW));

  private static final Set<RepresentationAction> POSSIBLE_ACTIONS_ON_SINGLE_REPRESENTATION = new HashSet<>(
    Arrays.asList(RepresentationAction.DOWNLOAD, RepresentationAction.CHANGE_TYPE, RepresentationAction.REMOVE,
      RepresentationAction.NEW_PROCESS, RepresentationAction.IDENTIFY_FORMATS, RepresentationAction.CHANGE_STATE));

  private static final Set<RepresentationAction> POSSIBLE_ACTIONS_ON_MULTIPLE_REPRESENTATIONS = new HashSet<>(
    Arrays.asList(RepresentationAction.CHANGE_TYPE, RepresentationAction.REMOVE, RepresentationAction.NEW_PROCESS,
      RepresentationAction.IDENTIFY_FORMATS));

  private final String parentAipId;
  private final Permissions permissions;

  private RepresentationActions(String parentAipId, Permissions permissions) {
    this.parentAipId = parentAipId;
    this.permissions = permissions;
  }

  public enum RepresentationAction implements Action<IndexedRepresentation> {
    NEW(RodaConstants.PERMISSION_METHOD_CREATE_REPRESENTATION), DOWNLOAD(),
    CHANGE_TYPE(RodaConstants.PERMISSION_METHOD_CHANGE_REPRESENTATION_TYPE),
    REMOVE(RodaConstants.PERMISSION_METHOD_DELETE_REPRESENTATION),
    NEW_PROCESS(RodaConstants.PERMISSION_METHOD_CREATE_JOB),
    IDENTIFY_FORMATS(RodaConstants.PERMISSION_METHOD_CREATE_JOB),
    CHANGE_STATE(RodaConstants.PERMISSION_METHOD_CHANGE_REPRESENTATION_STATES);

    private List<String> methods;

    RepresentationAction(String... methods) {
      this.methods = Arrays.asList(methods);
    }

    @Override
    public List<String> getMethods() {
      return this.methods;
    }
  }

  @Override
  public RepresentationAction actionForName(String name) {
    return RepresentationAction.valueOf(name);
  }

  public static RepresentationActions get() {
    return GENERAL_INSTANCE;
  }

  public static RepresentationActions get(String parentAipId, Permissions permissions) {
    return new RepresentationActions(parentAipId, permissions);
  }

  public static RepresentationActions getWithoutNoRepresentationActions(String parentAipId, Permissions permissions) {
    return new RepresentationActions(parentAipId, permissions) {
      @Override
      public boolean canAct(Action<IndexedRepresentation> action) {
        return false;
      }
    };
  }

  @Override
  public boolean canAct(Action<IndexedRepresentation> action) {
    return hasPermissions(action, permissions) && POSSIBLE_ACTIONS_WITHOUT_REPRESENTATION.contains(action)
      && parentAipId != null;
  }

  @Override
  public boolean canAct(Action<IndexedRepresentation> action, IndexedRepresentation representation) {
    return hasPermissions(action, permissions) && POSSIBLE_ACTIONS_ON_SINGLE_REPRESENTATION.contains(action);
  }

  @Override
  public boolean canAct(Action<IndexedRepresentation> action, SelectedItems<IndexedRepresentation> selectedItems) {
    return hasPermissions(action, permissions) && POSSIBLE_ACTIONS_ON_MULTIPLE_REPRESENTATIONS.contains(action);
  }

  @Override
  public void act(Action<IndexedRepresentation> action, AsyncCallback<ActionImpact> callback) {
    if (RepresentationAction.NEW.equals(action)) {
      create(callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  private void create(AsyncCallback<ActionImpact> callback) {
    Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
      RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
      new ActionNoAsyncCallback<String>(callback) {
        @Override
        public void onSuccess(String details) {
          BrowserService.Util.getInstance().createRepresentation(parentAipId, details,
            new ActionLoadingAsyncCallback<String>(callback) {
              @Override
              public void onSuccessImpl(String representationId) {
                HistoryUtils.newHistory(BrowseRepresentation.RESOLVER, parentAipId, representationId);
                callback.onSuccess(ActionImpact.UPDATED);
              }
            });
        }
      });
  }

  @Override
  public void act(Action<IndexedRepresentation> action, IndexedRepresentation representation,
    AsyncCallback<ActionImpact> callback) {
    if (RepresentationAction.DOWNLOAD.equals(action)) {
      download(representation, callback);
    } else if (RepresentationAction.CHANGE_TYPE.equals(action)) {
      changeType(representation, callback);
    } else if (RepresentationAction.REMOVE.equals(action)) {
      remove(representation, callback);
    } else if (RepresentationAction.NEW_PROCESS.equals(action)) {
      newProcess(representation, callback);
    } else if (RepresentationAction.IDENTIFY_FORMATS.equals(action)) {
      identifyFormats(representation, callback);
    } else if (RepresentationAction.CHANGE_STATE.equals(action)) {
      changeState(representation, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  /**
   * Act on multiple files from different representations
   */
  @Override
  public void act(Action<IndexedRepresentation> action, SelectedItems<IndexedRepresentation> selectedItems,
    AsyncCallback<ActionImpact> callback) {
    if (RepresentationAction.REMOVE.equals(action)) {
      remove(selectedItems, callback);
    } else if (RepresentationAction.CHANGE_TYPE.equals(action)) {
      changeType(selectedItems, callback);
    } else if (RepresentationAction.NEW_PROCESS.equals(action)) {
      newProcess(selectedItems, callback);
    } else if (RepresentationAction.IDENTIFY_FORMATS.equals(action)) {
      identifyFormats(selectedItems, callback);
    } else {
      unsupportedAction(action, callback);
    }
  }

  // ACTIONS
  public void download(IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    SafeUri downloadUri = null;
    if (representation != null) {
      downloadUri = RestUtils.createRepresentationDownloadUri(representation.getAipId(), representation.getId());
    }
    if (downloadUri != null) {
      Window.Location.assign(downloadUri.asString());
    }
    callback.onSuccess(ActionImpact.NONE);
  }

  public void remove(final IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    remove(objectToSelectedItems(representation, IndexedRepresentation.class), callback);
  }

  public void remove(final SelectedItems<IndexedRepresentation> selectedList,
    final AsyncCallback<ActionImpact> callback) {

    Dialogs.showConfirmDialog(messages.representationRemoveTitle(), messages.representationRemoveMessage(),
      messages.dialogCancel(), messages.dialogYes(), new ActionNoAsyncCallback<Boolean>(callback) {

        @Override
        public void onSuccess(Boolean confirmed) {
          if (confirmed) {
            Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
              RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
              new ActionNoAsyncCallback<String>(callback) {

                @Override
                public void onSuccess(String details) {
                  BrowserService.Util.getInstance().deleteRepresentation(selectedList, details,
                    new AsyncCallback<Job>() {

                      @Override
                      public void onSuccess(Job result) {
                        Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(),
                          new ActionAsyncCallback<Void>(callback) {

                            @Override
                            public void onFailure(Throwable caught) {
                              doActionCallbackDestroyed();
                            }

                            @Override
                            public void onSuccess(final Void nothing) {
                              HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
                              doActionCallbackNone();
                            }
                          });
                      }

                      @Override
                      public void onFailure(Throwable caught) {
                        HistoryUtils.newHistory(InternalProcess.RESOLVER);
                        doActionCallbackNone();
                      }
                    });
                }
              });
          }
        }
      });
  }

  public void changeType(final IndexedRepresentation representations, final AsyncCallback<ActionImpact> callback) {
    changeType(objectToSelectedItems(representations, IndexedRepresentation.class), callback);
  }

  public void changeType(final SelectedItems<IndexedRepresentation> representations,
    final AsyncCallback<ActionImpact> callback) {

    BrowserService.Util.getInstance().retrieveRepresentationTypeOptions(LocaleInfo.getCurrentLocale().getLocaleName(),
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
                      BrowserService.Util.getInstance().changeRepresentationType(representations, newType, details,
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
                                doActionCallbackNone();
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

  public void newProcess(IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    newProcess(objectToSelectedItems(representation, IndexedRepresentation.class), callback);
  }

  public void newProcess(SelectedItems<IndexedRepresentation> selected, final AsyncCallback<ActionImpact> callback) {
    LastSelectedItemsSingleton selectedItems = LastSelectedItemsSingleton.getInstance();
    selectedItems.setSelectedItems(selected);
    selectedItems.setLastHistory(HistoryUtils.getCurrentHistoryPath());
    HistoryUtils.newHistory(CreateSelectedJob.RESOLVER, RodaConstants.JOB_PROCESS_ACTION);
    callback.onSuccess(ActionImpact.UPDATED);
  }

  public void identifyFormats(IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    identifyFormats(objectToSelectedItems(representation, IndexedRepresentation.class), callback);
  }

  public void identifyFormats(SelectedItems<IndexedRepresentation> selected,
    final AsyncCallback<ActionImpact> callback) {
    BrowserService.Util.getInstance().createFormatIdentificationJob(selected, new ActionAsyncCallback<Job>(callback) {
      @Override
      public void onSuccess(Job result) {
        Toast.showInfo(messages.identifyingFormatsTitle(), messages.identifyingFormatsDescription());

        Dialogs.showJobRedirectDialog(messages.removeJobCreatedMessage(), new AsyncCallback<Void>() {
          @Override
          public void onFailure(Throwable caught) {
            doActionCallbackUpdated();
          }

          @Override
          public void onSuccess(final Void nothing) {
            HistoryUtils.newHistory(ShowJob.RESOLVER, result.getId());
            doActionCallbackNone();
          }
        });
      }

      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof NotFoundException) {
          Toast.showError(messages.moveNoSuchObject(caught.getMessage()));
        }
        super.onFailure(caught);
      }
    });
  }

  public void changeState(final IndexedRepresentation representation, final AsyncCallback<ActionImpact> callback) {
    RepresentationDialogs.showPromptDialogRepresentationStates(messages.changeStatusTitle(), messages.cancelButton(),
      messages.confirmButton(), representation.getRepresentationStates(),
      new ActionNoAsyncCallback<List<String>>(callback) {

        @Override
        public void onSuccess(final List<String> newStates) {
          Dialogs.showPromptDialog(messages.outcomeDetailTitle(), null, null, messages.outcomeDetailPlaceholder(),
            RegExp.compile(".*"), messages.cancelButton(), messages.confirmButton(), false, false,
            new ActionNoAsyncCallback<String>(callback) {

              @Override
              public void onSuccess(String details) {
                BrowserService.Util.getInstance().changeRepresentationStates(representation, newStates, details,
                  new ActionLoadingAsyncCallback<Void>(callback) {

                    @Override
                    public void onSuccessImpl(Void nothing) {
                      Toast.showInfo(messages.dialogSuccess(), messages.changeStatusSuccessful());
                      doActionCallbackUpdated();
                    }
                  });
              }
            });
        }
      });
  }

  @Override
  public ActionableBundle<IndexedRepresentation> createActionsBundle() {
    ActionableBundle<IndexedRepresentation> representationActionableBundle = new ActionableBundle<>();

    // MANAGEMENT
    ActionableGroup<IndexedRepresentation> managementGroup = new ActionableGroup<>(messages.representation());
    managementGroup.addButton(messages.newRepresentationButton(), RepresentationAction.NEW, ActionImpact.UPDATED,
      "btn-plus-circle");
    managementGroup.addButton(messages.downloadButton(), RepresentationAction.DOWNLOAD, ActionImpact.NONE,
      "btn-download");
    managementGroup.addButton(messages.changeTypeButton(), RepresentationAction.CHANGE_TYPE, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.changeStatusButton(), RepresentationAction.CHANGE_STATE, ActionImpact.UPDATED,
      "btn-edit");
    managementGroup.addButton(messages.removeButton(), RepresentationAction.REMOVE, ActionImpact.DESTROYED, "btn-ban");

    // PRESERVATION
    ActionableGroup<IndexedRepresentation> preservationGroup = new ActionableGroup<>(messages.preservationTitle());
    preservationGroup.addButton(messages.newProcessPreservation(), RepresentationAction.NEW_PROCESS,
      ActionImpact.UPDATED, "btn-play");
    preservationGroup.addButton(messages.identifyFormatsButton(), RepresentationAction.IDENTIFY_FORMATS,
      ActionImpact.UPDATED, "btn-play");

    representationActionableBundle.addGroup(managementGroup).addGroup(preservationGroup);
    return representationActionableBundle;
  }
}
